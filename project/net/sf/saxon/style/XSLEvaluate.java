/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.ArrayList;
import net.sf.saxon.expr.ErrorExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.ValueComparison;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.Choose;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLFallback;
import net.sf.saxon.style.XSLLocalParam;
import net.sf.saxon.style.XSLWithParam;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingIncident;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Whitespace;

public class XSLEvaluate
extends StyleElement {
    Expression xpath = null;
    SequenceType requiredType = SequenceType.ANY_SEQUENCE;
    Expression namespaceContext = null;
    Expression contextItem = null;
    Expression baseUri = null;
    Expression schemaAware = null;
    Expression withParams = null;
    Expression options = null;
    boolean hasFallbackChildren;

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    protected boolean isPermittedChild(StyleElement child) {
        return child instanceof XSLLocalParam;
    }

    protected ItemType getReturnedItemType() {
        return AnyItemType.getInstance();
    }

    @Override
    public boolean mayContainSequenceConstructor() {
        return false;
    }

    @Override
    public void prepareAttributes() {
        AttributeMap atts = this.attributes();
        String xpathAtt = null;
        String asAtt = null;
        String contextItemAtt = null;
        String baseUriAtt = null;
        String namespaceContextAtt = null;
        String schemaAwareAtt = null;
        String withParamsAtt = null;
        block20: for (AttributeInfo att : atts) {
            String f;
            NodeName attName = att.getNodeName();
            switch (f = attName.getDisplayName()) {
                case "xpath": {
                    xpathAtt = att.getValue();
                    this.xpath = this.makeExpression(xpathAtt, att);
                    continue block20;
                }
                case "as": {
                    asAtt = att.getValue();
                    continue block20;
                }
                case "context-item": {
                    contextItemAtt = att.getValue();
                    this.contextItem = this.makeExpression(contextItemAtt, att);
                    continue block20;
                }
                case "base-uri": {
                    baseUriAtt = att.getValue();
                    this.baseUri = this.makeAttributeValueTemplate(baseUriAtt, att);
                    continue block20;
                }
                case "namespace-context": {
                    namespaceContextAtt = att.getValue();
                    this.namespaceContext = this.makeExpression(namespaceContextAtt, att);
                    continue block20;
                }
                case "schema-aware": {
                    schemaAwareAtt = Whitespace.trim(att.getValue());
                    this.schemaAware = this.makeAttributeValueTemplate(schemaAwareAtt, att);
                    continue block20;
                }
                case "with-params": {
                    withParamsAtt = att.getValue();
                    this.withParams = this.makeExpression(withParamsAtt, att);
                    continue block20;
                }
            }
            if (attName.getLocalPart().equals("options") && attName.getURI().equals("http://saxon.sf.net/")) {
                if (!this.isExtensionAttributeAllowed(attName.getDisplayName())) continue;
                this.options = this.makeExpression(att.getValue(), att);
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        if (xpathAtt == null) {
            this.reportAbsence("xpath");
        }
        if (asAtt != null) {
            try {
                this.requiredType = this.makeSequenceType(asAtt);
            } catch (XPathException e) {
                this.compileErrorInAttribute(e.getMessage(), e.getErrorCodeLocalPart(), "as");
            }
        }
        if (contextItemAtt == null) {
            this.contextItem = Literal.makeEmptySequence();
        }
        if (schemaAwareAtt == null) {
            this.schemaAware = new StringLiteral("no");
        } else if (this.schemaAware instanceof StringLiteral) {
            this.checkAttributeValue("schema-aware", schemaAwareAtt, true, StyleElement.YES_NO);
        }
        if (withParamsAtt == null) {
            withParamsAtt = "map{}";
            this.withParams = this.makeExpression(withParamsAtt, null);
        }
        if (this.options == null) {
            this.options = this.makeExpression("map{}", null);
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.getContainingPackage().setRetainUnusedFunctions();
        if (this.xpath == null) {
            this.xpath = new StringLiteral("''");
        }
        this.xpath = this.typeCheck("xpath", this.xpath);
        this.baseUri = this.typeCheck("base-uri", this.baseUri);
        this.contextItem = this.typeCheck("context-item", this.contextItem);
        this.namespaceContext = this.typeCheck("namespace-context", this.namespaceContext);
        this.schemaAware = this.typeCheck("schema-aware", this.schemaAware);
        this.withParams = this.typeCheck("with-params", this.withParams);
        this.options = this.typeCheck("options", this.options);
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof XSLWithParam) continue;
            if (nodeInfo instanceof XSLFallback) {
                this.hasFallbackChildren = true;
                continue;
            }
            if (nodeInfo.getNodeKind() == 3) {
                if (Whitespace.isWhite(nodeInfo.getStringValueCS())) continue;
                this.compileError("No character data is allowed within xsl:evaluate", "XTSE0010");
                continue;
            }
            this.compileError("Child element " + Err.wrap(nodeInfo.getDisplayName(), 1) + " is not allowed as a child of xsl:evaluate", "XTSE0010");
        }
        try {
            ExpressionVisitor visitor = this.makeExpressionVisitor();
            TypeChecker typeChecker = this.getConfiguration().getTypeChecker(false);
            RoleDiagnostic role = new RoleDiagnostic(4, "xsl:evaluate/xpath", 0);
            this.xpath = typeChecker.staticTypeCheck(this.xpath, SequenceType.SINGLE_STRING, role, visitor);
            role = new RoleDiagnostic(4, "xsl:evaluate/context-item", 0);
            role.setErrorCode("XTTE3210");
            this.contextItem = typeChecker.staticTypeCheck(this.contextItem, SequenceType.OPTIONAL_ITEM, role, visitor);
            role = new RoleDiagnostic(4, "xsl:evaluate/namespace-context", 0);
            role.setErrorCode("XTTE3170");
            if (this.namespaceContext != null) {
                this.namespaceContext = typeChecker.staticTypeCheck(this.namespaceContext, SequenceType.SINGLE_NODE, role, visitor);
            }
            role = new RoleDiagnostic(4, "xsl:evaluate/with-params", 0);
            role.setErrorCode("XTTE3170");
            this.withParams = typeChecker.staticTypeCheck(this.withParams, SequenceType.makeSequenceType(MapType.ANY_MAP_TYPE, 16384), role, visitor);
            role = new RoleDiagnostic(4, "xsl:evaluate/saxon:options", 0);
            this.options = typeChecker.staticTypeCheck(this.options, SequenceType.makeSequenceType(MapType.ANY_MAP_TYPE, 16384), role, visitor);
        } catch (XPathException err) {
            this.compileError(err);
        }
    }

    public Expression getTargetExpression() {
        return this.xpath;
    }

    public Expression getContextItemExpression() {
        return this.contextItem;
    }

    public Expression getBaseUriExpression() {
        return this.baseUri;
    }

    public Expression getNamespaceContextExpression() {
        return this.namespaceContext;
    }

    public Expression getSchemaAwareExpression() {
        return this.schemaAware;
    }

    public Expression getWithParamsExpression() {
        return this.withParams;
    }

    public Expression getOptionsExpression() {
        return this.options;
    }

    public SequenceType getRequiredType() {
        return this.requiredType;
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        if (this.getConfiguration().getBooleanProperty(Feature.DISABLE_XSL_EVALUATE)) {
            this.validationError = new XmlProcessingIncident("xsl:evaluate is not available in this configuration", "XTDE3175");
            return this.fallbackProcessing(exec, decl, this);
        }
        Expression evaluateExpr = this.getConfiguration().makeEvaluateInstruction(this, decl);
        if (evaluateExpr instanceof ErrorExpression) {
            return evaluateExpr;
        }
        if (this.hasFallbackChildren) {
            Expression[] conditions = new Expression[2];
            Expression sysProp = SystemFunction.makeCall("system-property", this.makeRetainedStaticContext(), new StringLiteral("Q{http://www.w3.org/1999/XSL/Transform}supports-dynamic-evaluation"));
            conditions[0] = new ValueComparison(sysProp, 50, new StringLiteral("no"));
            conditions[1] = Literal.makeLiteral(BooleanValue.TRUE);
            Expression[] actions = new Expression[2];
            ArrayList<Expression> fallbackExpressions = new ArrayList<Expression>();
            for (NodeInfo nodeInfo : this.children(XSLFallback.class::isInstance)) {
                fallbackExpressions.add(((XSLFallback)nodeInfo).compileSequenceConstructor(exec, decl, false));
            }
            actions[0] = new Block(fallbackExpressions.toArray(new Expression[0]));
            actions[1] = evaluateExpr;
            return new Choose(conditions, actions);
        }
        return evaluateExpr;
    }
}


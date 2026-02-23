/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.function.BiConsumer;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.AttributeCreator;
import net.sf.saxon.expr.instruct.DummyNamespaceResolver;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.functions.NormalizeSpace_1;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.Orphan;
import net.sf.saxon.type.ComplexType;
import net.sf.saxon.type.SchemaDeclaration;
import net.sf.saxon.type.SchemaException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.ValidationFailure;

public final class FixedAttribute
extends AttributeCreator {
    private NodeName nodeName;

    public FixedAttribute(NodeName nodeName, int validationAction, SimpleType schemaType) {
        this.nodeName = nodeName;
        this.setSchemaType(schemaType);
        this.setValidationAction(validationAction);
        this.setOptions(0);
    }

    @Override
    public int getInstructionNameCode() {
        return 135;
    }

    @Override
    public String getExpressionName() {
        return "att";
    }

    public NodeName getAttributeName() {
        return this.nodeName;
    }

    @Override
    public void gatherProperties(BiConsumer<String, Object> consumer) {
        consumer.accept("name", this.getAttributeName());
    }

    @Override
    public void localTypeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        CharSequence value;
        ValidationFailure err;
        if (this.nodeName.equals(StandardNames.XML_ID_NAME) && !this.getSelect().isCallOn(NormalizeSpace_1.class)) {
            Expression select = SystemFunction.makeCall("normalize-space", this.getRetainedStaticContext(), this.getSelect());
            this.setSelect(select);
        }
        Configuration config = visitor.getConfiguration();
        ConversionRules rules = config.getConversionRules();
        SimpleType schemaType = this.getSchemaType();
        String errorCode = "XTTE1540";
        if (schemaType == null) {
            SchemaDeclaration decl;
            int validation = this.getValidationAction();
            if (validation == 1) {
                decl = config.getAttributeDeclaration(this.nodeName.getStructuredQName());
                if (decl == null) {
                    XPathException se = new XPathException("Strict validation fails: there is no global attribute declaration for " + this.nodeName.getDisplayName());
                    se.setErrorCode("XTTE1510");
                    se.setLocation(this.getLocation());
                    throw se;
                }
                schemaType = (SimpleType)decl.getType();
                errorCode = "XTTE1510";
            } else if (validation == 2) {
                decl = config.getAttributeDeclaration(this.nodeName.getStructuredQName());
                if (decl != null) {
                    schemaType = (SimpleType)decl.getType();
                    errorCode = "XTTE1515";
                } else {
                    visitor.getStaticContext().issueWarning("Lax validation has no effect: there is no global attribute declaration for " + this.nodeName.getDisplayName(), this.getLocation());
                }
            }
        }
        if (Literal.isAtomic(this.getSelect()) && schemaType != null && !schemaType.isNamespaceSensitive() && (err = schemaType.validateContent(value = ((Literal)this.getSelect()).getValue().getStringValueCS(), DummyNamespaceResolver.getInstance(), rules)) != null) {
            XPathException se = new XPathException("Attribute value " + Err.wrap(value, 4) + " does not the match the required type " + schemaType.getDescription() + ". " + err.getMessage());
            se.setErrorCode(errorCode);
            throw se;
        }
        if (this.getSelect() instanceof StringLiteral) {
            boolean special = false;
            String val = ((StringLiteral)this.getSelect()).getStringValue();
            for (int k = 0; k < val.length(); ++k) {
                char c = val.charAt(k);
                if (c >= '!' && c <= '~' && c != '<' && c != '>' && c != '&' && c != '\"' && c != '\'') continue;
                special = true;
                break;
            }
            if (!special) {
                this.setNoSpecialChars();
            }
        }
    }

    public int getAttributeFingerprint() {
        return this.nodeName.getFingerprint();
    }

    @Override
    public int getCardinality() {
        return 16384;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        FixedAttribute exp = new FixedAttribute(this.nodeName, this.getValidationAction(), this.getSchemaType());
        ExpressionTool.copyLocationInfo(this, exp);
        exp.setSelect(this.getSelect().copy(rebindings));
        exp.setInstruction(this.isInstruction());
        return exp;
    }

    @Override
    public NodeName evaluateNodeName(XPathContext context) {
        return this.nodeName;
    }

    @Override
    public void checkPermittedContents(SchemaType parentType, boolean whole) throws XPathException {
        SimpleType type;
        int fp = this.nodeName.getFingerprint();
        if (fp == 641 || fp == 643 || fp == 642 || fp == 644) {
            return;
        }
        if (parentType instanceof SimpleType) {
            XPathException err = new XPathException("Attribute " + this.nodeName.getDisplayName() + " is not permitted in the content model of the simple type " + parentType.getDescription());
            err.setIsTypeError(true);
            err.setLocation(this.getLocation());
            err.setErrorCode(this.getPackageData().isXSLT() ? "XTTE1510" : "XQDY0027");
            throw err;
        }
        try {
            type = ((ComplexType)parentType).getAttributeUseType(this.nodeName.getStructuredQName());
        } catch (SchemaException e) {
            throw new XPathException(e);
        }
        if (type == null) {
            XPathException err = new XPathException("Attribute " + this.nodeName.getDisplayName() + " is not permitted in the content model of the complex type " + parentType.getDescription());
            err.setIsTypeError(true);
            err.setLocation(this.getLocation());
            err.setErrorCode(this.getPackageData().isXSLT() ? "XTTE1510" : "XQDY0027");
            throw err;
        }
        try {
            this.getSelect().checkPermittedContents(type, true);
        } catch (XPathException e) {
            e.maybeSetLocation(this.getLocation());
            throw e;
        }
    }

    @Override
    public NodeInfo evaluateItem(XPathContext context) throws XPathException {
        Orphan o = (Orphan)super.evaluateItem(context);
        assert (o != null);
        this.validateOrphanAttribute(o, context);
        return o;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("att", this);
        out.emitAttribute("name", this.nodeName.getDisplayName());
        if (!this.nodeName.getStructuredQName().hasURI("")) {
            out.emitAttribute("nsuri", this.nodeName.getStructuredQName().getURI());
        }
        if (this.getValidationAction() != 4 && this.getValidationAction() != 8) {
            out.emitAttribute("validation", Validation.toString(this.getValidationAction()));
        }
        if (this.getSchemaType() != null) {
            out.emitAttribute("type", this.getSchemaType().getStructuredQName());
        }
        String flags = "";
        if (this.isLocal()) {
            flags = flags + "l";
        }
        if (!flags.isEmpty()) {
            out.emitAttribute("flags", flags);
        }
        this.getSelect().export(out);
        out.endElement();
    }

    @Override
    public String toShortString() {
        return "attr{" + this.nodeName.getDisplayName() + "=...}";
    }
}


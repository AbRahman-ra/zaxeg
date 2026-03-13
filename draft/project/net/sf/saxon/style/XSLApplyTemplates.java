/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.HashMap;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemChecker;
import net.sf.saxon.expr.SimpleStepExpression;
import net.sf.saxon.expr.instruct.ApplyTemplates;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.sort.SortExpression;
import net.sf.saxon.expr.sort.SortKeyDefinitionList;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.style.XSLSort;
import net.sf.saxon.style.XSLWithParam;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.RuleManager;
import net.sf.saxon.value.Whitespace;

public class XSLApplyTemplates
extends StyleElement {
    private Expression select;
    private Expression separator;
    private StructuredQName modeName;
    private boolean useCurrentMode = false;
    private boolean useTailRecursion = false;
    private boolean defaultedSelectExpression = true;
    private Mode mode;
    private String modeAttribute;

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        block20: for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            switch (f) {
                case "mode": {
                    this.modeAttribute = Whitespace.trim(value);
                    continue block20;
                }
                case "select": {
                    String selectAtt = value;
                    this.select = this.makeExpression(selectAtt, att);
                    this.defaultedSelectExpression = false;
                    continue block20;
                }
                case "separator": {
                    this.requireSyntaxExtensions("separator");
                    this.separator = this.makeAttributeValueTemplate(value, att);
                    continue block20;
                }
            }
            this.checkUnknownAttribute(attName);
        }
        if (this.modeAttribute != null) {
            switch (this.modeAttribute) {
                case "#current": {
                    this.useCurrentMode = true;
                    break;
                }
                case "#unnamed": {
                    this.modeName = Mode.UNNAMED_MODE_NAME;
                    break;
                }
                case "#default": {
                    break;
                }
                default: {
                    this.modeName = this.makeQName(this.modeAttribute, null, "mode");
                }
            }
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        if (this.useCurrentMode) {
            if (this.iterateAxis(0, new NameTest(1, 200, this.getNamePool())).next() == null) {
                this.issueWarning("Specifying mode=\"#current\" when not inside an xsl:template serves no useful purpose", this);
            }
        } else {
            PrincipalStylesheetModule psm = this.getPrincipalStylesheetModule();
            if (this.modeName == null) {
                this.modeName = this.getDefaultMode();
                if ((this.modeName == null || this.modeName.equals(Mode.UNNAMED_MODE_NAME)) && psm.isDeclaredModes() && !psm.getRuleManager().isUnnamedModeExplicit()) {
                    this.compileError("The unnamed mode must be explicitly declared in an xsl:mode declaration", "XTSE3085");
                }
            } else if (this.modeName.equals(Mode.UNNAMED_MODE_NAME) && psm.isDeclaredModes() && !psm.getRuleManager().isUnnamedModeExplicit()) {
                this.compileError("The #unnamed mode must be explicitly declared in an xsl:mode declaration", "XTSE3085");
            }
            SymbolicName symbolicName = new SymbolicName(174, this.modeName);
            StylesheetPackage containingPackage = decl.getSourceElement().getContainingPackage();
            HashMap<SymbolicName, Component> componentIndex = containingPackage.getComponentIndex();
            Component existing = componentIndex.get(symbolicName);
            if (existing != null) {
                this.mode = (Mode)existing.getActor();
            }
            if (this.mode == null) {
                if (psm.isDeclaredModes()) {
                    this.compileError("Mode name " + this.modeName.getDisplayName() + " must be explicitly declared in an xsl:mode declaration", "XTSE3085");
                }
                this.mode = psm.getRuleManager().obtainMode(this.modeName, true);
            }
        }
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo.getNodeKind() == 3) {
                if (Whitespace.isWhite(nodeInfo.getStringValueCS())) continue;
                this.compileError("No character data is allowed within xsl:apply-templates", "XTSE0010");
                continue;
            }
            if (nodeInfo instanceof XSLSort || nodeInfo instanceof XSLWithParam) continue;
            this.compileError("Invalid element " + Err.wrap(nodeInfo.getDisplayName(), 1) + " within xsl:apply-templates", "XTSE0010");
        }
        if (this.select == null) {
            Expression here = new ContextItemExpression();
            RoleDiagnostic roleDiagnostic = new RoleDiagnostic(13, "", 0);
            roleDiagnostic.setErrorCode("XTTE0510");
            here = new ItemChecker(here, AnyNodeTest.getInstance(), roleDiagnostic);
            this.select = new SimpleStepExpression(here, new AxisExpression(3, null));
            this.select.setLocation(this.allocateLocation());
            this.select.setRetainedStaticContext(this.makeRetainedStaticContext());
        }
        this.select = this.typeCheck("select", this.select);
        if (this.separator != null) {
            this.separator = this.typeCheck("separator", this.separator);
        }
    }

    @Override
    public boolean markTailCalls() {
        this.useTailRecursion = true;
        return true;
    }

    @Override
    public Expression compile(Compilation compilation, ComponentDeclaration decl) throws XPathException {
        SortKeyDefinitionList sortKeys = this.makeSortKeys(compilation, decl);
        if (sortKeys != null) {
            this.useTailRecursion = false;
        }
        assert (this.select != null);
        Expression sortedSequence = this.select;
        if (sortKeys != null) {
            sortedSequence = new SortExpression(this.select, sortKeys);
        }
        this.compileSequenceConstructor(compilation, decl, true);
        RuleManager rm = compilation.getPrincipalStylesheetModule().getRuleManager();
        ApplyTemplates app = new ApplyTemplates(sortedSequence, this.useCurrentMode, this.useTailRecursion, this.defaultedSelectExpression, this.isWithinDeclaredStreamableConstruct(), this.mode, rm);
        app.setActualParams(this.getWithParamInstructions(app, compilation, decl, false));
        app.setTunnelParams(this.getWithParamInstructions(app, compilation, decl, true));
        if (this.separator != null) {
            app.setSeparatorExpression(this.separator);
        }
        return app;
    }
}


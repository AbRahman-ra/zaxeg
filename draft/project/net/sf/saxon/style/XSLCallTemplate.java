/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.List;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.instruct.CallTemplate;
import net.sf.saxon.expr.instruct.NamedTemplate;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLFallback;
import net.sf.saxon.style.XSLWithParam;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Whitespace;

public class XSLCallTemplate
extends StyleElement {
    private static StructuredQName ERROR_TEMPLATE_NAME = new StructuredQName("saxon", "http://saxon.sf.net/", "error-template");
    private StructuredQName calledTemplateName;
    private NamedTemplate template = null;
    private boolean useTailRecursion = false;

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        String nameAttribute = null;
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            if (f.equals("name")) {
                nameAttribute = Whitespace.trim(value);
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        if (nameAttribute == null) {
            this.calledTemplateName = ERROR_TEMPLATE_NAME;
            this.reportAbsence("name");
            return;
        }
        this.calledTemplateName = this.makeQName(nameAttribute, null, "name");
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof XSLWithParam || nodeInfo instanceof XSLFallback && this.mayContainFallback()) continue;
            if (nodeInfo.getNodeKind() == 3) {
                if (Whitespace.isWhite(nodeInfo.getStringValueCS())) continue;
                this.compileError("No character data is allowed within xsl:call-template", "XTSE0010");
                continue;
            }
            this.compileError("Child element " + Err.wrap(nodeInfo.getDisplayName(), 1) + " is not allowed as a child of xsl:call-template", "XTSE0010");
        }
        if (!this.calledTemplateName.equals(ERROR_TEMPLATE_NAME)) {
            this.template = this.findTemplate(this.calledTemplateName);
        }
    }

    @Override
    public void postValidate() throws XPathException {
        if (this.template == null) {
            throw new AssertionError((Object)"Target template not known");
        }
        this.checkParams();
    }

    private void checkParams() throws XPathException {
        List<NamedTemplate.LocalParamInfo> declaredParams = this.template.getLocalParamDetails();
        for (NamedTemplate.LocalParamInfo localParamInfo : declaredParams) {
            if (!localParamInfo.isRequired || localParamInfo.isTunnel) continue;
            boolean ok = false;
            for (NodeInfo nodeInfo : this.children(XSLWithParam.class::isInstance)) {
                if (!((XSLWithParam)nodeInfo).getVariableQName().equals(localParamInfo.name)) continue;
                ok = true;
                break;
            }
            if (ok) continue;
            this.compileError("No value supplied for required parameter " + Err.wrap(localParamInfo.name.getDisplayName(), 5), "XTSE0690");
        }
        for (NodeInfo nodeInfo : this.children()) {
            if (!(nodeInfo instanceof XSLWithParam) || ((XSLWithParam)nodeInfo).isTunnelParam()) continue;
            XSLWithParam withParam = (XSLWithParam)nodeInfo;
            boolean ok = false;
            for (NamedTemplate.LocalParamInfo param : declaredParams) {
                if (!param.name.equals(withParam.getVariableQName()) || param.isTunnel) continue;
                ok = true;
                SequenceType required = param.requiredType;
                withParam.checkAgainstRequiredType(required);
                break;
            }
            if (ok || this.xPath10ModeIsEnabled()) continue;
            this.compileError("Parameter " + withParam.getVariableQName().getDisplayName() + " is not declared in the called template", "XTSE0680");
        }
    }

    private NamedTemplate findTemplate(StructuredQName templateName) throws XPathException {
        PrincipalStylesheetModule pack = this.getPrincipalStylesheetModule();
        NamedTemplate template = pack.getNamedTemplate(templateName);
        if (template == null) {
            if (templateName.hasURI("http://www.w3.org/1999/XSL/Transform") && templateName.getLocalPart().equals("original")) {
                return (NamedTemplate)this.getXslOriginal(200);
            }
            this.compileError("Cannot find a template named " + this.calledTemplateName, "XTSE0650");
        }
        return template;
    }

    @Override
    public boolean markTailCalls() {
        this.useTailRecursion = true;
        return true;
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        if (this.template == null) {
            return null;
        }
        CallTemplate call = new CallTemplate(this.template, this.calledTemplateName, this.useTailRecursion, this.isWithinDeclaredStreamableConstruct());
        call.setLocation(this.allocateLocation());
        call.setActualParameters(this.getWithParamInstructions(call, exec, decl, false), this.getWithParamInstructions(call, exec, decl, true));
        return call;
    }
}


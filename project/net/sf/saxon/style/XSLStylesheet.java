/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.AbsentExtensionElement;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.DataElement;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLModuleRoot;
import net.sf.saxon.trans.XPathException;

public class XSLStylesheet
extends XSLModuleRoot {
    @Override
    protected boolean mayContainParam() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        this.processDefaultCollationAttribute();
        this.processDefaultMode();
        String inputTypeAnnotationsAtt = null;
        block24: for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            switch (f) {
                case "version": {
                    continue block24;
                }
                case "id": {
                    continue block24;
                }
                case "extension-element-prefixes": {
                    continue block24;
                }
                case "exclude-result-prefixes": {
                    continue block24;
                }
                case "input-type-annotations": {
                    inputTypeAnnotationsAtt = value;
                    continue block24;
                }
            }
            this.checkUnknownAttribute(attName);
        }
        if (this.version == -1 && (this.getParent() == null || this.getParent().getNodeKind() == 9)) {
            this.reportAbsence("version");
        }
        if (inputTypeAnnotationsAtt != null) {
            switch (inputTypeAnnotationsAtt) {
                case "strip": {
                    break;
                }
                case "preserve": {
                    break;
                }
                case "unspecified": {
                    break;
                }
                default: {
                    this.invalidAttribute("input-type-annotations", "strip|preserve|unspecified");
                }
            }
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        if (this.validationError != null) {
            this.compileError(this.validationError);
        }
        if (this.getParent() != null && this.getParent().getNodeKind() != 9) {
            this.compileError(this.getDisplayName() + " must be the outermost element", "XTSE0010");
        }
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo.getNodeKind() == 3 || nodeInfo instanceof StyleElement && ((StyleElement)nodeInfo).isDeclaration() || nodeInfo instanceof DataElement || !(nodeInfo instanceof StyleElement) || !"http://www.w3.org/1999/XSL/Transform".equals(nodeInfo.getURI()) && !"".equals(nodeInfo.getURI()) || nodeInfo instanceof AbsentExtensionElement && ((StyleElement)nodeInfo).forwardsCompatibleModeIsEnabled()) continue;
            if ("http://www.w3.org/1999/XSL/Transform".equals(nodeInfo.getURI())) {
                ((StyleElement)nodeInfo).compileError("Element " + nodeInfo.getDisplayName() + " must not appear directly within " + this.getDisplayName(), "XTSE0010");
                continue;
            }
            ((StyleElement)nodeInfo).compileError("Element " + nodeInfo.getDisplayName() + " must not appear directly within " + this.getDisplayName() + " because it is not in a namespace", "XTSE0130");
        }
    }
}


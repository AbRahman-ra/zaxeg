/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLTemplate;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Whitespace;

public class XSLContextItem
extends StyleElement {
    private ItemType requiredType = AnyItemType.getInstance();
    private boolean mayBeOmitted = true;
    private boolean absentFocus = false;

    @Override
    public void prepareAttributes() {
        String asAtt = null;
        String useAtt = null;
        block20: for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            switch (f) {
                case "as": {
                    asAtt = Whitespace.trim(value);
                    continue block20;
                }
                case "use": {
                    useAtt = Whitespace.trim(value);
                    continue block20;
                }
            }
            this.checkUnknownAttribute(attName);
        }
        if (asAtt != null) {
            SequenceType st;
            try {
                st = this.makeSequenceType(asAtt);
            } catch (XPathException e) {
                st = SequenceType.SINGLE_ITEM;
                this.compileErrorInAttribute(e.getMessage(), e.getErrorCodeLocalPart(), "as");
            }
            if (st.getCardinality() != 16384) {
                this.compileError("The xsl:context-item/@use attribute must be an item type (no occurrence indicator allowed)", "XTSE0020");
                return;
            }
            this.requiredType = st.getPrimaryType();
        }
        if (useAtt != null) {
            switch (useAtt) {
                case "required": {
                    this.mayBeOmitted = false;
                    break;
                }
                case "optional": {
                    break;
                }
                case "absent": {
                    this.absentFocus = true;
                    break;
                }
                default: {
                    this.invalidAttribute("use", "required|optional|absent");
                }
            }
        }
        if (asAtt != null && this.absentFocus) {
            this.compileError("The 'as' attribute must be omitted when use='absent' is specified", "XTSE3089");
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        if (!(this.getParent() instanceof XSLTemplate)) {
            this.compileError("xsl:context-item can appear only as a child of xsl:template");
            return;
        }
        if (this.mayBeOmitted && ((XSLTemplate)this.getParent()).getTemplateName() == null) {
            this.compileError("xsl:context-item appearing in an xsl:template declaration with no name attribute must specify use=required", "XTSE0020");
        }
        ((XSLTemplate)this.getParent()).setContextItemRequirements(this.requiredType, this.mayBeOmitted, this.absentFocus);
        this.iterateAxis(11).forEachOrFail(prec -> {
            if (((NodeInfo)prec).getNodeKind() != 3 || !Whitespace.isWhite(prec.getStringValueCS())) {
                this.compileError("xsl:context-item must be the first child of xsl:template");
            }
        });
    }

    public ItemType getRequiredContextItemType() {
        return this.requiredType;
    }

    public boolean isMayBeOmitted() {
        return this.mayBeOmitted;
    }

    public boolean isAbsentFocus() {
        return this.absentFocus;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Component;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.StylesheetComponent;
import net.sf.saxon.style.XSLAttributeSet;
import net.sf.saxon.style.XSLFunction;
import net.sf.saxon.style.XSLGlobalVariable;
import net.sf.saxon.style.XSLOriginalLibrary;
import net.sf.saxon.style.XSLTemplate;
import net.sf.saxon.style.XSLUsePackage;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.XPathException;

public class XSLOverride
extends StyleElement {
    @Override
    public void prepareAttributes() {
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            this.checkUnknownAttribute(attName);
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo.getNodeKind() == 3) {
                this.compileError("Character content is not allowed as a child of xsl:override", "XTSE0010");
                continue;
            }
            if (nodeInfo instanceof XSLFunction || nodeInfo instanceof XSLTemplate || nodeInfo instanceof XSLGlobalVariable || nodeInfo instanceof XSLAttributeSet) continue;
            ((StyleElement)nodeInfo).compileError("Element " + nodeInfo.getDisplayName() + " is not allowed as a child of xsl:override", "XTSE0010");
        }
    }

    @Override
    public void postValidate() throws XPathException {
        XSLUsePackage parent = (XSLUsePackage)this.getParent();
        assert (parent != null);
        if (parent.getUsedPackage() != null) {
            for (NodeInfo nodeInfo : this.children()) {
                if (!(nodeInfo instanceof XSLFunction) && !(nodeInfo instanceof XSLTemplate) && !(nodeInfo instanceof XSLGlobalVariable) && !(nodeInfo instanceof XSLAttributeSet)) continue;
                StylesheetComponent procedure = (StylesheetComponent)((Object)nodeInfo);
                SymbolicName name = procedure.getSymbolicName();
                if (name == null) {
                    if (nodeInfo instanceof XSLTemplate) {
                        StructuredQName[] modeNames;
                        XSLTemplate decl = (XSLTemplate)nodeInfo;
                        if (decl.getMatch() == null) {
                            decl.compileError("An overriding template with no name must have a match pattern");
                        }
                        for (StructuredQName modeName : modeNames = decl.getModeNames()) {
                            if (modeName.equals(Mode.OMNI_MODE)) {
                                ((StyleElement)nodeInfo).compileError("An overriding template rule must not specify mode=\"#all\"", "XTSE3440");
                                continue;
                            }
                            if (!modeName.equals(Mode.UNNAMED_MODE_NAME) || !(modeName = decl.getDefaultMode()).equals(Mode.UNNAMED_MODE_NAME)) continue;
                            ((StyleElement)nodeInfo).compileError("An overriding template rule must not belong to the unnamed mode", "XTSE3440");
                        }
                        continue;
                    }
                    ((StyleElement)nodeInfo).compileError("An overriding component (other than a template rule) must have a name", "XTSE3440");
                    return;
                }
                Component overridden = parent.getUsedPackage().getComponent(name);
                if (overridden == null) {
                    ((StyleElement)nodeInfo).compileError("There is no " + StandardNames.getLocalName(name.getComponentKind()) + " named " + name.getShortName() + " in the used package", "XTSE3058");
                    return;
                }
                Visibility overriddenVis = overridden.getVisibility();
                if (overriddenVis == null) {
                    overriddenVis = Visibility.PRIVATE;
                }
                if (overriddenVis == Visibility.FINAL || overriddenVis == Visibility.PRIVATE) {
                    ((StyleElement)nodeInfo).compileError("The " + StandardNames.getLocalName(name.getComponentKind()) + " named " + name.getShortName() + " in the used package cannot be overridden because its visibility is " + overriddenVis.show(), "XTSE3060");
                    return;
                }
                procedure.checkCompatibility(overridden);
            }
        }
    }

    public void addXSLOverrideFunctionLibrary(FunctionLibraryList list) {
        list.addFunctionLibrary(XSLOriginalLibrary.getInstance());
    }
}


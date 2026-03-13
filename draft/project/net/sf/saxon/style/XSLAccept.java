/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Component;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.QNameTest;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.style.XSLAcceptExpose;
import net.sf.saxon.style.XSLUsePackage;
import net.sf.saxon.trans.ComponentTest;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.linked.NodeImpl;

public class XSLAccept
extends XSLAcceptExpose {
    @Override
    protected void prepareAttributes() {
        super.prepareAttributes();
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        NodeImpl parent = this.getParent();
        if (!(parent instanceof XSLUsePackage)) {
            this.compileError("Parent of xsl:accept must be xsl:use-package");
            return;
        }
        StylesheetPackage pack = ((XSLUsePackage)parent).getUsedPackage();
        if (pack != null) {
            for (ComponentTest test : this.getExplicitComponentTests()) {
                QNameTest nameTest = test.getQNameTest();
                if (!(nameTest instanceof NameTest)) continue;
                int kind = test.getComponentKind();
                SymbolicName sName = kind == 158 ? new SymbolicName.F(((NameTest)nameTest).getMatchingNodeName(), test.getArity()) : new SymbolicName(kind, ((NameTest)nameTest).getMatchingNodeName());
                Component comp = pack.getComponent(sName);
                boolean found = false;
                if (comp == null) {
                    if (kind == 158 && test.getArity() == -1) {
                        for (int i = 0; i <= pack.getMaxFunctionArity(); ++i) {
                            sName = new SymbolicName.F(((NameTest)nameTest).getMatchingNodeName(), i);
                            comp = pack.getComponent(sName);
                            if (comp == null) continue;
                            this.checkCompatibility(sName, comp.getVisibility(), this.getVisibility());
                            found = true;
                        }
                    }
                } else {
                    this.checkCompatibility(sName, comp.getVisibility(), this.getVisibility());
                    found = true;
                }
                if (found) continue;
                this.compileError("No " + sName.toString() + " exists in the used package", "XTSE3030");
            }
        }
    }

    protected void checkCompatibility(SymbolicName name, Visibility declared, Visibility exposed) {
        if (!XSLAccept.isCompatible(declared, exposed)) {
            String code = "XTSE3040";
            this.compileError("The " + name + " is declared as " + declared.show() + " and cannot be accepted as " + exposed.show(), code);
        }
    }

    public static boolean isCompatible(Visibility declared, Visibility exposed) {
        switch (declared) {
            case PUBLIC: {
                return exposed == Visibility.PUBLIC || exposed == Visibility.PRIVATE || exposed == Visibility.FINAL || exposed == Visibility.HIDDEN;
            }
            case ABSTRACT: {
                return exposed == Visibility.ABSTRACT || exposed == Visibility.HIDDEN;
            }
            case FINAL: {
                return exposed == Visibility.PRIVATE || exposed == Visibility.FINAL || exposed == Visibility.HIDDEN;
            }
        }
        return false;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.instruct.GlobalContextRequirement;
import net.sf.saxon.pattern.SameNameTest;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.XSLContextItem;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;

public class XSLGlobalContextItem
extends XSLContextItem {
    @Override
    public boolean isDeclaration() {
        return true;
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        AxisIterator prior = this.iterateAxis(11, new SameNameTest(this));
        if (prior.next() != null) {
            this.compileError("xsl:global-context-item must not appear twice within the same stylesheet module", "XTSE3087");
        }
    }

    @Override
    public void index(ComponentDeclaration decl, PrincipalStylesheetModule top) throws XPathException {
        this.prepareAttributes();
        GlobalContextRequirement req = new GlobalContextRequirement();
        req.setMayBeOmitted(this.isMayBeOmitted());
        req.setAbsentFocus(this.isAbsentFocus());
        req.addRequiredItemType(this.getRequiredContextItemType());
        try {
            top.getStylesheetPackage().setContextItemRequirements(req);
        } catch (XPathException e) {
            e.setLocation(decl.getSourceElement());
            throw e;
        }
    }
}


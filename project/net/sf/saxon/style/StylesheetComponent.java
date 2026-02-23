/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.instruct.Actor;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;

public interface StylesheetComponent {
    public SlotManager getSlotManager();

    public void optimize(ComponentDeclaration var1) throws XPathException;

    public void generateByteCode(Optimizer var1) throws XPathException;

    public Actor getActor() throws XPathException;

    public SymbolicName getSymbolicName();

    public void checkCompatibility(Component var1) throws XPathException;
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import net.sf.saxon.expr.Operand;
import net.sf.saxon.trans.XPathException;

public interface OperandProcessor {
    public void processOperand(Operand var1) throws XPathException;
}


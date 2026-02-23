/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;

public interface Binding {
    public SequenceType getRequiredType();

    public IntegerValue[] getIntegerBoundsForVariable();

    public Sequence evaluateVariable(XPathContext var1) throws XPathException;

    public boolean isGlobal();

    public boolean isAssignable();

    public StructuredQName getVariableQName();

    public void addReference(VariableReference var1, boolean var2);
}


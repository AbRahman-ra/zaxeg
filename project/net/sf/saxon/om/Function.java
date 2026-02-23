/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.om.Genre;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.FunctionItemType;

public interface Function
extends Item,
Callable,
GroundedValue {
    public boolean isMap();

    public boolean isArray();

    public FunctionItemType getFunctionItemType();

    public StructuredQName getFunctionName();

    public int getArity();

    public OperandRole[] getOperandRoles();

    public AnnotationList getAnnotations();

    public XPathContext makeNewContext(XPathContext var1, ContextOriginator var2);

    @Override
    public Sequence call(XPathContext var1, Sequence[] var2) throws XPathException;

    public boolean deepEquals(Function var1, XPathContext var2, AtomicComparer var3, int var4) throws XPathException;

    public String getDescription();

    public void export(ExpressionPresenter var1) throws XPathException;

    public boolean isTrustedResultType();

    @Override
    default public String toShortString() {
        return this.getDescription();
    }

    @Override
    default public Genre getGenre() {
        return Genre.FUNCTION;
    }

    @SafeVarargs
    public static Sequence[] argumentArray(Sequence ... args) {
        return args;
    }
}


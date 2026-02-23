/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.Arrays;
import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Function;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;

public abstract class AbstractFunction
implements Function {
    @Override
    public OperandRole[] getOperandRoles() {
        Object[] roles = new OperandRole[this.getArity()];
        Arrays.fill(roles, new OperandRole(0, OperandUsage.NAVIGATION));
        return roles;
    }

    @Override
    public AtomicSequence atomize() throws XPathException {
        throw new XPathException("Function items (other than arrays) cannot be atomized", "FOTY0013");
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isMap() {
        return false;
    }

    @Override
    public String getStringValue() {
        throw new UnsupportedOperationException("The string value of a function is not defined");
    }

    @Override
    public CharSequence getStringValueCS() {
        throw new UnsupportedOperationException("The string value of a function is not defined");
    }

    @Override
    public AnnotationList getAnnotations() {
        return AnnotationList.EMPTY;
    }

    @Override
    public boolean effectiveBooleanValue() throws XPathException {
        throw new XPathException("A function has no effective boolean value", "XPTY0004");
    }

    public void simplify() throws XPathException {
    }

    public void typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
    }

    @Override
    public XPathContext makeNewContext(XPathContext callingContext, ContextOriginator originator) {
        return callingContext;
    }

    @Override
    public boolean deepEquals(Function other, XPathContext context, AtomicComparer comparer, int flags) throws XPathException {
        throw new XPathException("Argument to deep-equal() contains a function item", "FOTY0015");
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        throw new UnsupportedOperationException("export() not implemented for " + this.getClass());
    }

    @Override
    public boolean isTrustedResultType() {
        return false;
    }
}


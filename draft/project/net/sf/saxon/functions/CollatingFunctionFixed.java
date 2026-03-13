/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.Properties;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.EqualityComparer;
import net.sf.saxon.expr.sort.GenericAtomicComparer;
import net.sf.saxon.expr.sort.SimpleCollation;
import net.sf.saxon.functions.CollatingFunctionFree;
import net.sf.saxon.functions.StatefulSystemFunction;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.lib.SubstringMatcher;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ErrorType;

public abstract class CollatingFunctionFixed
extends SystemFunction
implements StatefulSystemFunction {
    private String collationName;
    private StringCollator stringCollator = null;
    private AtomicComparer atomicComparer = null;

    public boolean isSubstringMatchingFunction() {
        return false;
    }

    public StringCollator getStringCollator() {
        return this.stringCollator;
    }

    @Override
    public void setRetainedStaticContext(RetainedStaticContext retainedStaticContext) {
        super.setRetainedStaticContext(retainedStaticContext);
        if (this.collationName == null) {
            this.collationName = retainedStaticContext.getDefaultCollationName();
            try {
                this.allocateCollator();
            } catch (XPathException xPathException) {
                // empty catch block
            }
        }
    }

    public void setCollationName(String collationName) throws XPathException {
        this.collationName = collationName;
        this.allocateCollator();
    }

    private void allocateCollator() throws XPathException {
        this.stringCollator = this.getRetainedStaticContext().getConfiguration().getCollation(this.collationName);
        if (this.stringCollator == null) {
            throw new XPathException("Unknown collation " + this.collationName, "FOCH0002");
        }
        if (this.isSubstringMatchingFunction()) {
            if (this.stringCollator instanceof SimpleCollation) {
                this.stringCollator = ((SimpleCollation)this.stringCollator).getSubstringMatcher();
            }
            if (!(this.stringCollator instanceof SubstringMatcher)) {
                throw new XPathException("The collation requested for " + this.getFunctionName().getDisplayName() + " does not support substring matching", "FOCH0004");
            }
        }
    }

    protected void preAllocateComparer(AtomicType type0, AtomicType type1, StaticContext env) {
        StringCollator collation = this.getStringCollator();
        if (type0 == ErrorType.getInstance() || type1 == ErrorType.getInstance()) {
            this.atomicComparer = EqualityComparer.getInstance();
            return;
        }
        this.atomicComparer = GenericAtomicComparer.makeAtomicComparer((BuiltInAtomicType)type0.getBuiltInBaseType(), (BuiltInAtomicType)type1.getBuiltInBaseType(), this.stringCollator, env.makeEarlyEvaluationContext());
    }

    public AtomicComparer getPreAllocatedAtomicComparer() {
        return this.atomicComparer;
    }

    public AtomicComparer getAtomicComparer(XPathContext context) {
        if (this.atomicComparer != null) {
            return this.atomicComparer.provideContext(context);
        }
        return new GenericAtomicComparer(this.getStringCollator(), context);
    }

    @Override
    public void exportAttributes(ExpressionPresenter out) {
        if (!this.collationName.equals("http://www.w3.org/2005/xpath-functions/collation/codepoint")) {
            out.emitAttribute("collation", this.collationName);
        }
    }

    @Override
    public void importAttributes(Properties attributes) throws XPathException {
        String collationName = attributes.getProperty("collation");
        if (collationName != null) {
            this.setCollationName(collationName);
        }
    }

    @Override
    public CollatingFunctionFixed copy() {
        SystemFunction copy = SystemFunction.makeFunction(this.getFunctionName().getLocalPart(), this.getRetainedStaticContext(), this.getArity());
        if (copy instanceof CollatingFunctionFree) {
            try {
                copy = ((CollatingFunctionFree)copy).bindCollation(this.collationName);
            } catch (XPathException e) {
                throw new AssertionError((Object)e);
            }
        }
        if (copy instanceof CollatingFunctionFixed) {
            ((CollatingFunctionFixed)copy).collationName = this.collationName;
            ((CollatingFunctionFixed)copy).atomicComparer = this.atomicComparer;
            ((CollatingFunctionFixed)copy).stringCollator = this.stringCollator;
            return (CollatingFunctionFixed)copy;
        }
        throw new IllegalStateException();
    }
}


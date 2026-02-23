/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trace;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.instruct.FixedAttribute;
import net.sf.saxon.trace.TraceCodeInjector;

public class XSLTTraceCodeInjector
extends TraceCodeInjector {
    @Override
    protected boolean isApplicable(Expression exp) {
        return exp.isInstruction() || exp instanceof LetExpression || exp instanceof FixedAttribute;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.accum;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.Rule;
import net.sf.saxon.trans.rules.RuleTarget;

public class AccumulatorRule
implements RuleTarget {
    private Expression newValueExpression;
    private SlotManager stackFrameMap;
    private boolean postDescent;
    private boolean capturing;

    public AccumulatorRule(Expression newValueExpression, SlotManager stackFrameMap, boolean postDescent) {
        this.newValueExpression = newValueExpression;
        this.stackFrameMap = stackFrameMap;
        this.postDescent = postDescent;
    }

    public Expression getNewValueExpression() {
        return this.newValueExpression;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        this.newValueExpression.export(out);
    }

    public SlotManager getStackFrameMap() {
        return this.stackFrameMap;
    }

    @Override
    public void registerRule(Rule rule) {
    }

    public void setCapturing(boolean capturing) {
        this.capturing = capturing;
    }

    public boolean isCapturing() {
        return this.capturing;
    }

    public boolean isPostDescent() {
        return this.postDescent;
    }
}


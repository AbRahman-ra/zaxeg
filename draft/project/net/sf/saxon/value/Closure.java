/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StackFrame;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.om.FocusIterator;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ManualIterator;

public class Closure
implements Sequence,
ContextOriginator {
    protected Expression expression;
    protected XPathContextMajor savedXPathContext;
    protected int depth = 0;
    protected SequenceIterator inputIterator;

    public static Sequence make(Expression expression, XPathContext context, int ref) throws XPathException {
        return context.getConfiguration().makeClosure(expression, ref, context);
    }

    public void saveContext(Expression expression, XPathContext context) throws XPathException {
        FocusIterator currentIterator;
        if ((expression.getDependencies() & 0x80) != 0) {
            StackFrame localStackFrame = context.getStackFrame();
            Sequence[] local = localStackFrame.getStackFrameValues();
            int[] slotsUsed = expression.getSlotsUsed();
            if (local != null) {
                SlotManager stackFrameMap = localStackFrame.getStackFrameMap();
                Sequence[] savedStackFrame = new Sequence[stackFrameMap.getNumberOfVariables()];
                for (int i : slotsUsed) {
                    if (local[i] instanceof Closure) {
                        int cdepth = ((Closure)local[i]).depth;
                        if (cdepth >= 10) {
                            local[i] = local[i].iterate().materialize();
                        } else if (cdepth + 1 > this.depth) {
                            this.depth = cdepth + 1;
                        }
                    }
                    savedStackFrame[i] = local[i];
                }
                this.savedXPathContext.setStackFrame(stackFrameMap, savedStackFrame);
            }
        }
        if ((currentIterator = context.getCurrentIterator()) != null) {
            Item contextItem = currentIterator.current();
            ManualIterator single = new ManualIterator(contextItem);
            this.savedXPathContext.setCurrentIterator(single);
        }
    }

    @Override
    public Item head() throws XPathException {
        return this.iterate().next();
    }

    public Expression getExpression() {
        return this.expression;
    }

    public XPathContextMajor getSavedXPathContext() {
        return this.savedXPathContext;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public void setSavedXPathContext(XPathContextMajor savedXPathContext) {
        this.savedXPathContext = savedXPathContext;
    }

    @Override
    public SequenceIterator iterate() throws XPathException {
        if (this.inputIterator == null) {
            this.inputIterator = this.expression.iterate(this.savedXPathContext);
            return this.inputIterator;
        }
        throw new IllegalStateException("A Closure can only be read once");
    }

    public GroundedValue reduce() throws XPathException {
        return this.iterate().materialize();
    }

    @Override
    public Sequence makeRepeatable() throws XPathException {
        return this.materialize();
    }
}


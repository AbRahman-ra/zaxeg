/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.GroupIterator;
import net.sf.saxon.expr.sort.MergeInstr;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;

public class CurrentMergeKey
extends SystemFunction
implements Callable {
    private MergeInstr controllingInstruction = null;

    public void setControllingInstruction(MergeInstr instruction) {
        this.controllingInstruction = instruction;
    }

    public MergeInstr getControllingInstruction() {
        return this.controllingInstruction;
    }

    @Override
    public Expression makeFunctionCall(Expression ... arguments) {
        return new SystemFunctionCall(this, arguments){

            @Override
            public Expression getScopingExpression() {
                return CurrentMergeKey.this.getControllingInstruction();
            }
        };
    }

    public SequenceIterator iterate(XPathContext c) throws XPathException {
        GroupIterator gi = c.getCurrentMergeGroupIterator();
        if (gi == null) {
            throw new XPathException("There is no current merge key", "XTDE3510");
        }
        return gi.getCurrentGroupingKey().iterate();
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        return SequenceTool.toLazySequence(this.iterate(context));
    }
}


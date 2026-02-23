/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.sort.GroupIterator;
import net.sf.saxon.functions.CurrentGroupCall;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;

public class CurrentGroupingKeyCall
extends Expression
implements Callable {
    @Override
    public Expression getScopingExpression() {
        return CurrentGroupCall.findControllingInstruction(this);
    }

    @Override
    protected int computeCardinality() {
        return 57344;
    }

    @Override
    public int getImplementationMethod() {
        return 2;
    }

    @Override
    public ItemType getItemType() {
        return BuiltInAtomicType.ANY_ATOMIC;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("currentGroupingKey");
        out.endElement();
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        return new CurrentGroupingKeyCall();
    }

    @Override
    public int getIntrinsicDependencies() {
        return 32;
    }

    @Override
    public SequenceIterator iterate(XPathContext c) throws XPathException {
        AtomicSequence result;
        GroupIterator gi = c.getCurrentGroupIterator();
        AtomicSequence atomicSequence = result = gi == null ? null : gi.getCurrentGroupingKey();
        if (result == null) {
            XPathException err = new XPathException("There is no current grouping key", "XTDE1071");
            err.setLocation(this.getLocation());
            throw err;
        }
        return result.iterate();
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        return SequenceTool.toLazySequence(this.iterate(context));
    }
}


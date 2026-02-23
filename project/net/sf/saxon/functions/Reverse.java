/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ReversibleIterator;
import net.sf.saxon.value.SequenceExtent;

public class Reverse
extends SystemFunction {
    @Override
    public int getSpecialProperties(Expression[] arguments) {
        int baseProps = arguments[0].getSpecialProperties();
        if ((baseProps & 0x40000) != 0) {
            return baseProps & 0xFFFBFFFF | 0x20000;
        }
        if ((baseProps & 0x20000) != 0) {
            return baseProps & 0xFFFDFFFF | 0x40000;
        }
        return baseProps;
    }

    public static <T extends Item> SequenceIterator getReverseIterator(SequenceIterator forwards) throws XPathException {
        if (forwards instanceof ReversibleIterator) {
            return ((ReversibleIterator)forwards).getReverseIterator();
        }
        SequenceExtent extent = new SequenceExtent(forwards);
        return extent.reverseIterate();
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        return SequenceTool.toLazySequence(Reverse.getReverseIterator(arguments[0].iterate()));
    }

    @Override
    public Expression makeOptimizedFunctionCall(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo, Expression ... arguments) throws XPathException {
        if (arguments[0].getCardinality() == 24576) {
            return arguments[0];
        }
        return super.makeOptimizedFunctionCall(visitor, contextInfo, arguments);
    }

    @Override
    public String getStreamerName() {
        return "Reverse";
    }
}


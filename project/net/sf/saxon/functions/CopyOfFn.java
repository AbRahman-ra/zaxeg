/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemMappingIterator;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.CopyOf;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.LazySequence;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.wrapper.VirtualCopy;

public class CopyOfFn
extends SystemFunction {
    @Override
    public int getCardinality(Expression[] arguments) {
        return arguments[0].getCardinality();
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        Sequence in = arguments.length == 0 ? context.getContextItem() : arguments[0];
        SequenceIterator input = in.iterate();
        ItemMappingIterator output = new ItemMappingIterator(input, item -> {
            if (!(item instanceof NodeInfo)) {
                return item;
            }
            VirtualCopy vc = VirtualCopy.makeVirtualCopy((NodeInfo)item);
            vc.getTreeInfo().setCopyAccumulators(true);
            return vc;
        });
        return new LazySequence(output);
    }

    @Override
    public Expression makeFunctionCall(Expression ... arguments) {
        Expression arg = arguments.length == 0 ? new ContextItemExpression() : arguments[0];
        CopyOf fn = new CopyOf(arg, true, 3, null, false);
        fn.setCopyAccumulators(true);
        fn.setSchemaAware(false);
        return fn;
    }
}


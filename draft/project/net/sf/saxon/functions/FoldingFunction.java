/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.Fold;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

public abstract class FoldingFunction
extends SystemFunction {
    public abstract Fold getFold(XPathContext var1, Sequence ... var2) throws XPathException;

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        Item item;
        Sequence[] additionalArgs = new Sequence[arguments.length - 1];
        System.arraycopy(arguments, 1, additionalArgs, 0, additionalArgs.length);
        Fold fold = this.getFold(context, additionalArgs);
        SequenceIterator iter = arguments[0].iterate();
        while ((item = iter.next()) != null) {
            fold.processItem(item);
            if (!fold.isFinished()) continue;
            break;
        }
        return fold.result();
    }

    @Override
    public String getStreamerName() {
        return "Fold";
    }
}


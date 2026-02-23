/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.HashSet;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.functions.CollatingFunctionFixed;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.ZeroOrMore;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;

public class DistinctValues
extends CollatingFunctionFixed {
    @Override
    public String getStreamerName() {
        return "DistinctValues";
    }

    @Override
    public ZeroOrMore<AtomicValue> call(XPathContext context, Sequence[] arguments) throws XPathException {
        StringCollator collator = this.getStringCollator();
        return new ZeroOrMore<AtomicValue>(new DistinctIterator(arguments[0].iterate(), collator, context));
    }

    public static class DistinctIterator
    implements SequenceIterator {
        private SequenceIterator base;
        private StringCollator collator;
        private XPathContext context;
        private HashSet<AtomicMatchKey> lookup = new HashSet(40);

        public DistinctIterator(SequenceIterator base, StringCollator collator, XPathContext context) {
            this.base = base;
            this.collator = collator;
            this.context = context;
        }

        @Override
        public AtomicValue next() throws XPathException {
            AtomicValue nextBase;
            AtomicMatchKey key;
            int implicitTimezone = this.context.getImplicitTimezone();
            do {
                if ((nextBase = (AtomicValue)this.base.next()) != null) continue;
                return null;
            } while (!this.lookup.add(key = nextBase.isNaN() ? AtomicMatchKey.NaN_MATCH_KEY : nextBase.getXPathComparable(false, this.collator, implicitTimezone)));
            return nextBase;
        }

        @Override
        public void close() {
            this.base.close();
        }
    }
}


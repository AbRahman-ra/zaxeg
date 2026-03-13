/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.streams.XdmStream;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.tree.iter.LookaheadIteratorImpl;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.tree.iter.UnfailingIterator;

public class XdmSequenceIterator<T extends XdmItem>
implements Iterator<T> {
    private final LookaheadIterator base;
    private boolean closed = false;

    protected XdmSequenceIterator(SequenceIterator base) {
        try {
            this.base = LookaheadIteratorImpl.makeLookaheadIterator(base);
        } catch (XPathException xe) {
            throw new SaxonApiUncheckedException(xe);
        }
    }

    public XdmSequenceIterator(UnfailingIterator base) {
        try {
            this.base = LookaheadIteratorImpl.makeLookaheadIterator(base);
        } catch (XPathException xe) {
            throw new SaxonApiUncheckedException(xe);
        }
    }

    public static XdmSequenceIterator<XdmNode> ofNodes(AxisIterator base) {
        return new XdmSequenceIterator<XdmNode>(base);
    }

    public static XdmSequenceIterator<XdmAtomicValue> ofAtomicValues(UnfailingIterator base) {
        return new XdmSequenceIterator<XdmAtomicValue>(base);
    }

    protected static XdmSequenceIterator<XdmNode> ofNode(XdmNode node) {
        return new XdmSequenceIterator<XdmNode>(SingletonIterator.makeIterator(node.getUnderlyingNode()));
    }

    @Override
    public boolean hasNext() {
        return !this.closed && this.base.hasNext();
    }

    @Override
    public T next() {
        try {
            Item it = this.base.next();
            if (it == null) {
                throw new NoSuchElementException();
            }
            return (T)XdmItem.wrapItem(it);
        } catch (XPathException xe) {
            throw new SaxonApiUncheckedException(xe);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public void close() {
        this.closed = true;
        this.base.close();
    }

    public XdmStream<T> stream() {
        Stream base = StreamSupport.stream(Spliterators.spliteratorUnknownSize(this, 16), false);
        base = (Stream)base.onClose(new Runnable(){

            @Override
            public void run() {
                XdmSequenceIterator.this.close();
            }
        });
        return new XdmStream(base);
    }
}


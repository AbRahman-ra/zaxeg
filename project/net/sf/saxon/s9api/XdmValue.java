/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.SequenceCopier;
import net.sf.saxon.expr.sort.DocumentOrderIterator;
import net.sf.saxon.expr.sort.GlobalOrderComparer;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.XdmArray;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmEmptySequence;
import net.sf.saxon.s9api.XdmExternalObject;
import net.sf.saxon.s9api.XdmFunctionItem;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmMap;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.streams.Step;
import net.sf.saxon.s9api.streams.XdmStream;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.UnfailingIterator;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.ExternalObject;
import net.sf.saxon.value.SequenceExtent;

public class XdmValue
implements Iterable<XdmItem> {
    private GroundedValue value;

    protected XdmValue() {
    }

    public XdmValue(Iterable<? extends XdmItem> items) {
        ArrayList<Item> values = new ArrayList<Item>();
        for (XdmItem xdmItem : items) {
            values.add(xdmItem.getUnderlyingValue());
        }
        this.value = new SequenceExtent(values);
    }

    public XdmValue(Iterator<? extends XdmItem> iterator) throws SaxonApiException {
        try {
            ArrayList<Item> values = new ArrayList<Item>();
            while (iterator.hasNext()) {
                values.add(iterator.next().getUnderlyingValue());
            }
            this.value = new SequenceExtent(values);
        } catch (SaxonApiUncheckedException e) {
            throw new SaxonApiException(e.getCause());
        }
    }

    public XdmValue(Stream<? extends XdmItem> stream) throws SaxonApiException {
        this(stream.iterator());
    }

    protected static XdmValue fromGroundedValue(GroundedValue value) {
        XdmValue xv = new XdmValue();
        xv.setValue(value);
        return xv;
    }

    protected void setValue(GroundedValue value) {
        this.value = value;
    }

    public static XdmValue wrap(Sequence value) {
        GroundedValue gv;
        if (value == null) {
            return XdmEmptySequence.getInstance();
        }
        try {
            gv = value.materialize();
        } catch (XPathException e) {
            throw new SaxonApiUncheckedException(e);
        }
        if (gv.getLength() == 0) {
            return XdmEmptySequence.getInstance();
        }
        if (gv.getLength() == 1) {
            Item first = gv.head();
            if (first instanceof NodeInfo) {
                return new XdmNode((NodeInfo)first);
            }
            if (first instanceof AtomicValue) {
                return new XdmAtomicValue((AtomicValue)first, true);
            }
            if (first instanceof MapItem) {
                return new XdmMap((MapItem)first);
            }
            if (first instanceof ArrayItem) {
                return new XdmArray((ArrayItem)first);
            }
            if (first instanceof Function) {
                return new XdmFunctionItem((Function)first);
            }
            if (first instanceof ExternalObject) {
                return new XdmExternalObject((Object)first);
            }
            throw new IllegalArgumentException("Unknown item type " + first.getClass());
        }
        return XdmValue.fromGroundedValue(gv);
    }

    public static XdmValue wrap(AtomicSequence value) {
        switch (value.getLength()) {
            case 0: {
                return XdmEmptySequence.getInstance();
            }
            case 1: {
                return new XdmAtomicValue(value.head(), true);
            }
        }
        return XdmValue.fromGroundedValue(value);
    }

    public XdmValue append(XdmValue otherValue) {
        ArrayList<Item> values = new ArrayList<Item>();
        for (XdmItem item : this) {
            values.add(item.getUnderlyingValue());
        }
        for (XdmItem item : otherValue) {
            values.add(item.getUnderlyingValue());
        }
        GroundedValue gv = SequenceExtent.makeSequenceExtent(values);
        return XdmValue.fromGroundedValue(gv);
    }

    public int size() {
        return this.value.getLength();
    }

    public boolean isEmpty() {
        return this.value.head() == null;
    }

    public XdmItem itemAt(int n) throws IndexOutOfBoundsException, SaxonApiUncheckedException {
        if (n < 0 || n >= this.size()) {
            throw new IndexOutOfBoundsException("" + n);
        }
        try {
            Item item = SequenceTool.itemAt(this.value, n);
            return (XdmItem)XdmItem.wrap(item);
        } catch (XPathException e) {
            throw new SaxonApiUncheckedException(e);
        }
    }

    @Override
    public XdmSequenceIterator<XdmItem> iterator() throws SaxonApiUncheckedException {
        try {
            GroundedValue v = this.getUnderlyingValue();
            return new XdmSequenceIterator<XdmItem>(v.iterate());
        } catch (XPathException e) {
            throw new SaxonApiUncheckedException(e);
        }
    }

    public GroundedValue getUnderlyingValue() {
        return this.value;
    }

    public String toString() {
        try {
            Item item;
            Configuration config = null;
            UnfailingIterator iter = this.value.iterate();
            while ((item = iter.next()) != null) {
                if (!(item instanceof NodeInfo)) continue;
                config = ((NodeInfo)item).getConfiguration();
                break;
            }
            if (config == null) {
                config = Configuration.newConfiguration();
            }
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            SerializationProperties properties = new SerializationProperties();
            properties.setProperty("method", "adaptive");
            properties.setProperty("indent", "true");
            properties.setProperty("omit-xml-declaration", "true");
            Receiver r = config.getSerializerFactory().getReceiver(result, properties);
            SequenceCopier.copySequence(this.value.iterate(), r);
            return writer.toString();
        } catch (XPathException e) {
            return super.toString();
        }
    }

    public static XdmValue makeSequence(Iterable<?> list) throws IllegalArgumentException {
        ArrayList<Item> result = new ArrayList<Item>();
        for (Object o : list) {
            XdmValue v = XdmValue.makeValue(o);
            if (v instanceof XdmItem) {
                result.add((Item)v.getUnderlyingValue());
                continue;
            }
            result.add(new XdmArray(v).getUnderlyingValue());
        }
        return XdmValue.wrap(SequenceExtent.makeSequenceExtent(result));
    }

    public static XdmValue makeValue(Object o) throws IllegalArgumentException {
        if (o instanceof Sequence) {
            return XdmValue.wrap((Sequence)o);
        }
        if (o instanceof XdmValue) {
            return (XdmValue)o;
        }
        if (o instanceof Map) {
            return XdmMap.makeMap((Map)o);
        }
        if (o instanceof Object[]) {
            return XdmArray.makeArray((Object[])o);
        }
        if (o instanceof Iterable) {
            return XdmValue.makeSequence((Iterable)o);
        }
        return XdmAtomicValue.makeAtomicValue(o);
    }

    public XdmValue documentOrder() throws SaxonApiException {
        try {
            UnfailingIterator iter = this.value.iterate();
            DocumentOrderIterator sorted = new DocumentOrderIterator(iter, GlobalOrderComparer.getInstance());
            return XdmValue.fromGroundedValue(sorted.materialize());
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public XdmStream<? extends XdmItem> stream() {
        return new XdmStream(StreamSupport.stream(this.spliterator(), false));
    }

    public <T extends XdmItem> XdmStream<T> select(Step<T> step) {
        return this.stream().flatMapToXdm(step);
    }
}


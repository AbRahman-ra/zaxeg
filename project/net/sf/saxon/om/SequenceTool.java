/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.RangeIterator;
import net.sf.saxon.expr.ReverseRangeIterator;
import net.sf.saxon.functions.Count;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.LazySequence;
import net.sf.saxon.om.MemoSequence;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.UnfailingIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.wrapper.VirtualNode;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.Closure;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DateValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.ExternalObject;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.HexBinaryValue;
import net.sf.saxon.value.MemoClosure;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.SingletonClosure;

public class SequenceTool {
    public static final int INDETERMINATE_ORDERING = Integer.MIN_VALUE;

    public static <T extends Item> GroundedValue toGroundedValue(SequenceIterator iterator) throws XPathException {
        return iterator.materialize();
    }

    public static Sequence toMemoSequence(SequenceIterator iterator) throws XPathException {
        if (iterator instanceof EmptyIterator) {
            return EmptySequence.getInstance();
        }
        if (iterator.getProperties().contains((Object)SequenceIterator.Property.GROUNDED)) {
            return iterator.materialize();
        }
        return new MemoSequence(iterator);
    }

    public static Sequence toLazySequence(SequenceIterator iterator) throws XPathException {
        if (iterator.getProperties().contains((Object)SequenceIterator.Property.GROUNDED) && !(iterator instanceof RangeIterator) && !(iterator instanceof ReverseRangeIterator)) {
            return iterator.materialize();
        }
        return new LazySequence(iterator);
    }

    public static Sequence toLazySequence2(SequenceIterator iterator) throws XPathException {
        if (iterator.getProperties().contains((Object)SequenceIterator.Property.GROUNDED) && !(iterator instanceof RangeIterator) && !(iterator instanceof ReverseRangeIterator)) {
            return iterator.materialize();
        }
        return new LazySequence(iterator);
    }

    public static boolean isUnrepeatable(Sequence seq) {
        return seq instanceof LazySequence || seq instanceof Closure && !(seq instanceof MemoClosure) && !(seq instanceof SingletonClosure);
    }

    public static int getLength(Sequence sequence) throws XPathException {
        if (sequence instanceof GroundedValue) {
            return ((GroundedValue)sequence).getLength();
        }
        return Count.count(sequence.iterate());
    }

    public static boolean hasLength(SequenceIterator iter, int length) throws XPathException {
        if (iter.getProperties().contains((Object)SequenceIterator.Property.LAST_POSITION_FINDER)) {
            return ((LastPositionFinder)((Object)iter)).getLength() == length;
        }
        int n = 0;
        while (iter.next() != null) {
            if (n++ != length) continue;
            iter.close();
            return false;
        }
        return length == 0;
    }

    public static boolean sameLength(SequenceIterator a, SequenceIterator b) throws XPathException {
        Item itB;
        Item itA;
        if (a.getProperties().contains((Object)SequenceIterator.Property.LAST_POSITION_FINDER)) {
            return SequenceTool.hasLength(b, ((LastPositionFinder)((Object)a)).getLength());
        }
        if (b.getProperties().contains((Object)SequenceIterator.Property.LAST_POSITION_FINDER)) {
            return SequenceTool.hasLength(a, ((LastPositionFinder)((Object)b)).getLength());
        }
        do {
            itA = a.next();
            itB = b.next();
        } while (itA != null && itB != null);
        if (itA != null) {
            a.close();
        }
        if (itB != null) {
            b.close();
        }
        return itA == null && itB == null;
    }

    public static Item itemAt(Sequence sequence, int index) throws XPathException {
        if (sequence instanceof Item && index == 0) {
            return (Item)sequence;
        }
        return sequence.materialize().itemAt(index);
    }

    public static Item asItem(Sequence sequence) throws XPathException {
        if (sequence instanceof Item) {
            return (Item)sequence;
        }
        SequenceIterator iter = sequence.iterate();
        Item first = iter.next();
        if (first == null) {
            return null;
        }
        if (iter.next() != null) {
            throw new XPathException("Sequence contains more than one item");
        }
        return first;
    }

    public static Object convertToJava(Item item) throws XPathException {
        if (item instanceof NodeInfo) {
            Object node = item;
            while (node instanceof VirtualNode) {
                node = ((VirtualNode)node).getRealNode();
            }
            return node;
        }
        if (item instanceof Function) {
            return item;
        }
        if (item instanceof ExternalObject) {
            return ((ExternalObject)item).getObject();
        }
        AtomicValue value = (AtomicValue)item;
        switch (value.getItemType().getPrimitiveType()) {
            case 513: 
            case 518: 
            case 529: 
            case 631: {
                return value.getStringValue();
            }
            case 514: {
                return ((BooleanValue)value).getBooleanValue() ? Boolean.TRUE : Boolean.FALSE;
            }
            case 515: {
                return ((BigDecimalValue)value).getDecimalValue();
            }
            case 533: {
                return ((NumericValue)value).longValue();
            }
            case 517: {
                return ((DoubleValue)value).getDoubleValue();
            }
            case 516: {
                return Float.valueOf(((FloatValue)value).getFloatValue());
            }
            case 519: {
                return ((DateTimeValue)value).getCalendar().getTime();
            }
            case 521: {
                return ((DateValue)value).getCalendar().getTime();
            }
            case 520: {
                return value.getStringValue();
            }
            case 528: {
                return ((Base64BinaryValue)value).getBinaryValue();
            }
            case 527: {
                return ((HexBinaryValue)value).getBinaryValue();
            }
        }
        return item;
    }

    public static String getStringValue(Sequence sequence) throws XPathException {
        FastStringBuffer fsb = new FastStringBuffer(64);
        sequence.iterate().forEachOrFail(item -> {
            if (!fsb.isEmpty()) {
                fsb.cat(' ');
            }
            fsb.cat(item.getStringValueCS());
        });
        return fsb.toString();
    }

    public static ItemType getItemType(Sequence sequence, TypeHierarchy th) {
        if (sequence instanceof Item) {
            return Type.getItemType((Item)sequence, th);
        }
        if (sequence instanceof GroundedValue) {
            try {
                Item item;
                ItemType type = null;
                SequenceIterator iter = sequence.iterate();
                while ((item = iter.next()) != null && (type = type == null ? Type.getItemType(item, th) : Type.getCommonSuperType(type, Type.getItemType(item, th), th)) != AnyItemType.getInstance()) {
                }
                return type == null ? ErrorType.getInstance() : type;
            } catch (XPathException err) {
                return AnyItemType.getInstance();
            }
        }
        return AnyItemType.getInstance();
    }

    public static UType getUType(Sequence sequence) {
        if (sequence instanceof Item) {
            return UType.getUType((Item)sequence);
        }
        if (sequence instanceof GroundedValue) {
            Item item;
            UType type = UType.VOID;
            UnfailingIterator iter = ((GroundedValue)sequence).iterate();
            while ((item = iter.next()) != null && (type = type.union(UType.getUType(item))) != UType.ANY) {
            }
            return type;
        }
        return UType.ANY;
    }

    public static int getCardinality(Sequence sequence) {
        if (sequence instanceof Item) {
            return 16384;
        }
        if (sequence instanceof GroundedValue) {
            int len = ((GroundedValue)sequence).getLength();
            switch (len) {
                case 0: {
                    return 8192;
                }
                case 1: {
                    return 16384;
                }
            }
            return 49152;
        }
        try {
            SequenceIterator iter = sequence.iterate();
            Item item = iter.next();
            if (item == null) {
                return 8192;
            }
            item = iter.next();
            return item == null ? 16384 : 49152;
        } catch (XPathException err) {
            return 49152;
        }
    }

    public static void process(Sequence value, Outputter output, Location locationId) throws XPathException {
        value.iterate().forEachOrFail(it -> output.append(it, locationId, 524288));
    }

    public static Sequence[] makeSequenceArray(int length) {
        return new Sequence[length];
    }

    public static Sequence[] fromItems(Item ... items) {
        Sequence[] seq = new Sequence[items.length];
        System.arraycopy(items, 0, seq, 0, items.length);
        return seq;
    }
}


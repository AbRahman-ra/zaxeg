/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.arrays;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.Fold;
import net.sf.saxon.functions.FoldingFunction;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.functions.registry.BuiltInFunctionSet;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.ma.arrays.ArraySort;
import net.sf.saxon.ma.arrays.SimpleArrayItem;
import net.sf.saxon.om.Chain;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.z.IntHashSet;

public class ArrayFunctionSet
extends BuiltInFunctionSet {
    public static ArrayFunctionSet THE_INSTANCE = new ArrayFunctionSet();

    public ArrayFunctionSet() {
        this.init();
    }

    public static ArrayFunctionSet getInstance() {
        return THE_INSTANCE;
    }

    private void init() {
        this.register("append", 2, ArrayAppend.class, ArrayItemType.ANY_ARRAY_TYPE, 16384, 0).arg(0, ArrayItemType.ANY_ARRAY_TYPE, 0x1004000, null).arg(1, AnyItemType.getInstance(), 0x800E000, null);
        SpecificFunctionType filterFunctionType = new SpecificFunctionType(new SequenceType[]{SequenceType.ANY_SEQUENCE}, SequenceType.SINGLE_BOOLEAN);
        this.register("filter", 2, ArrayFilter.class, ArrayItemType.ANY_ARRAY_TYPE, 16384, 0).arg(0, ArrayItemType.ANY_ARRAY_TYPE, 0x1004000, null).arg(1, filterFunctionType, 0x1004000, null);
        this.register("flatten", 1, ArrayFlatten.class, AnyItemType.getInstance(), 57344, 0).arg(0, AnyItemType.getInstance(), 0x200E000, null);
        SpecificFunctionType foldFunctionType = new SpecificFunctionType(new SequenceType[]{SequenceType.ANY_SEQUENCE, SequenceType.ANY_SEQUENCE}, SequenceType.ANY_SEQUENCE);
        this.register("fold-left", 3, ArrayFoldLeft.class, AnyItemType.getInstance(), 57344, 0).arg(0, ArrayItemType.ANY_ARRAY_TYPE, 0x1004000, null).arg(1, AnyItemType.getInstance(), 0x800E000, null).arg(2, foldFunctionType, 0x1004000, null);
        this.register("fold-right", 3, ArrayFoldRight.class, AnyItemType.getInstance(), 57344, 0).arg(0, ArrayItemType.ANY_ARRAY_TYPE, 0x1004000, null).arg(1, AnyItemType.getInstance(), 0x800E000, null).arg(2, foldFunctionType, 0x1004000, null);
        SpecificFunctionType forEachFunctionType = new SpecificFunctionType(new SequenceType[]{SequenceType.ANY_SEQUENCE}, SequenceType.ANY_SEQUENCE);
        this.register("for-each", 2, ArrayForEach.class, AnyItemType.getInstance(), 57344, 0).arg(0, ArrayItemType.ANY_ARRAY_TYPE, 0x1004000, null).arg(1, forEachFunctionType, 0x1004000, null);
        this.register("for-each-pair", 3, ArrayForEachPair.class, AnyItemType.getInstance(), 57344, 0).arg(0, ArrayItemType.ANY_ARRAY_TYPE, 0x1004000, null).arg(1, ArrayItemType.ANY_ARRAY_TYPE, 0x1004000, null).arg(2, foldFunctionType, 0x1004000, null);
        this.register("get", 2, ArrayGet.class, AnyItemType.getInstance(), 57344, 0).arg(0, ArrayItemType.ANY_ARRAY_TYPE, 0x1004000, null).arg(1, BuiltInAtomicType.INTEGER, 0x2004000, null);
        this.register("head", 1, ArrayHead.class, AnyItemType.getInstance(), 57344, 0).arg(0, ArrayItemType.ANY_ARRAY_TYPE, 0x1004000, null);
        this.register("insert-before", 3, ArrayInsertBefore.class, ArrayItemType.ANY_ARRAY_TYPE, 16384, 0).arg(0, ArrayItemType.ANY_ARRAY_TYPE, 0x1004000, null).arg(1, BuiltInAtomicType.INTEGER, 0x200E000, null).arg(2, AnyItemType.getInstance(), 0x800E000, null);
        this.register("join", 1, ArrayJoin.class, ArrayItemType.ANY_ARRAY_TYPE, 16384, 0).arg(0, ArrayItemType.ANY_ARRAY_TYPE, 0x100E000, null);
        this.register("put", 3, ArrayPut.class, ArrayItemType.ANY_ARRAY_TYPE, 16384, 0).arg(0, ArrayItemType.ANY_ARRAY_TYPE, 0x1004000, null).arg(1, BuiltInAtomicType.INTEGER, 0x100E000, null).arg(2, AnyItemType.getInstance(), 0x800E000, null);
        this.register("remove", 2, ArrayRemove.class, ArrayItemType.ANY_ARRAY_TYPE, 16384, 0).arg(0, ArrayItemType.ANY_ARRAY_TYPE, 0x1004000, null).arg(1, BuiltInAtomicType.INTEGER, 0x200E000, null);
        this.register("reverse", 1, ArrayReverse.class, ArrayItemType.ANY_ARRAY_TYPE, 16384, 0).arg(0, ArrayItemType.ANY_ARRAY_TYPE, 0x1004000, null);
        this.register("size", 1, ArraySize.class, BuiltInAtomicType.INTEGER, 16384, 0).arg(0, ArrayItemType.ANY_ARRAY_TYPE, 0x1004000, null);
        SpecificFunctionType sortFunctionType = new SpecificFunctionType(new SequenceType[]{SequenceType.ANY_SEQUENCE}, SequenceType.ATOMIC_SEQUENCE);
        this.register("sort", 1, ArraySort.class, ArrayItemType.ANY_ARRAY_TYPE, 16384, 0).arg(0, ArrayItemType.ANY_ARRAY_TYPE, 0x1004000, null);
        this.register("sort", 2, ArraySort.class, ArrayItemType.ANY_ARRAY_TYPE, 16384, 0).arg(0, ArrayItemType.ANY_ARRAY_TYPE, 0x1004000, null).arg(1, BuiltInAtomicType.STRING, 0x2006000, null);
        this.register("sort", 3, ArraySort.class, ArrayItemType.ANY_ARRAY_TYPE, 16384, 0).arg(0, ArrayItemType.ANY_ARRAY_TYPE, 0x1004000, null).arg(1, BuiltInAtomicType.STRING, 0x2006000, null).arg(2, sortFunctionType, 0x1004000, null);
        this.register("subarray", 2, ArraySubarray.class, ArrayItemType.ANY_ARRAY_TYPE, 16384, 0).arg(0, ArrayItemType.ANY_ARRAY_TYPE, 0x1004000, null).arg(1, BuiltInAtomicType.INTEGER, 0x2004000, null);
        this.register("subarray", 3, ArraySubarray.class, ArrayItemType.ANY_ARRAY_TYPE, 16384, 0).arg(0, ArrayItemType.ANY_ARRAY_TYPE, 0x1004000, null).arg(1, BuiltInAtomicType.INTEGER, 0x2004000, null).arg(2, BuiltInAtomicType.INTEGER, 0x2004000, null);
        this.register("tail", 1, ArrayTail.class, ArrayItemType.ANY_ARRAY_TYPE, 16384, 0).arg(0, ArrayItemType.ANY_ARRAY_TYPE, 0x1004000, null);
        this.register("_to-sequence", 1, ArrayToSequence.class, AnyItemType.getInstance(), 57344, 0).arg(0, ArrayItemType.ANY_ARRAY_TYPE, 0x1004000, null);
        this.register("_from-sequence", 1, ArrayFromSequence.class, ArrayItemType.ANY_ARRAY_TYPE, 16384, 0).arg(0, AnyItemType.getInstance(), 0x100E000, null);
    }

    @Override
    public String getNamespace() {
        return "http://www.w3.org/2005/xpath-functions/array";
    }

    @Override
    public String getConventionalPrefix() {
        return "array";
    }

    public static int checkSubscript(IntegerValue subscript, int limit) throws XPathException {
        int index = subscript.asSubscript();
        if (index <= 0) {
            throw new XPathException("Array subscript " + subscript.getStringValue() + " is out of range", "FOAY0001");
        }
        if (index > limit) {
            throw new XPathException("Array subscript " + subscript.getStringValue() + " exceeds limit (" + limit + ")", "FOAY0001");
        }
        return index;
    }

    public static class ArrayFromSequence
    extends FoldingFunction {
        @Override
        public ArrayItem call(XPathContext context, Sequence[] arguments) throws XPathException {
            return SimpleArrayItem.makeSimpleArrayItem(arguments[0].iterate());
        }

        @Override
        public Fold getFold(XPathContext context, Sequence ... additionalArguments) {
            return new Fold(){
                List<GroundedValue> members = new ArrayList<GroundedValue>();

                @Override
                public void processItem(Item item) {
                    this.members.add(item);
                }

                @Override
                public boolean isFinished() {
                    return false;
                }

                @Override
                public ArrayItem result() {
                    return new SimpleArrayItem(this.members);
                }
            };
        }
    }

    public static class ArrayToSequence
    extends SystemFunction {
        @Override
        public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
            ArrayItem array = (ArrayItem)arguments[0].head();
            return ArrayToSequence.toSequence(array);
        }

        public static Sequence toSequence(ArrayItem array) throws XPathException {
            ArrayList<GroundedValue> results = new ArrayList<GroundedValue>();
            for (Sequence sequence : array.members()) {
                results.add(sequence.materialize());
            }
            return new Chain(results);
        }
    }

    public static class ArrayTail
    extends SystemFunction {
        @Override
        public ArrayItem call(XPathContext context, Sequence[] arguments) throws XPathException {
            ArrayItem array = (ArrayItem)arguments[0].head();
            assert (array != null);
            if (array.arrayLength() < 1) {
                throw new XPathException("Argument to array:tail is an empty array", "FOAY0001");
            }
            return array.remove(0);
        }
    }

    public static class ArraySubarray
    extends SystemFunction {
        @Override
        public ArrayItem call(XPathContext context, Sequence[] arguments) throws XPathException {
            int length;
            ArrayItem array = (ArrayItem)arguments[0].head();
            assert (array != null);
            int start = ArrayFunctionSet.checkSubscript((IntegerValue)arguments[1].head(), array.arrayLength() + 1);
            if (arguments.length == 3) {
                IntegerValue len = (IntegerValue)arguments[2].head();
                int signum = len.signum();
                if (signum < 0) {
                    throw new XPathException("Specified length of subarray is less than zero", "FOAY0002");
                }
                length = signum == 0 ? 0 : ArrayFunctionSet.checkSubscript(len, array.arrayLength());
            } else {
                length = array.arrayLength() - start + 1;
            }
            if (start < 1) {
                throw new XPathException("Start position is less than one", "FOAY0001");
            }
            if (start > array.arrayLength() + 1) {
                throw new XPathException("Start position is out of bounds", "FOAY0001");
            }
            if (start + length > array.arrayLength() + 1) {
                throw new XPathException("Specified length of subarray is too great for start position given", "FOAY0001");
            }
            return array.subArray(start - 1, start + length - 1);
        }
    }

    public static class ArraySize
    extends SystemFunction {
        @Override
        public IntegerValue call(XPathContext context, Sequence[] arguments) throws XPathException {
            ArrayItem array = (ArrayItem)arguments[0].head();
            assert (array != null);
            return new Int64Value(array.arrayLength());
        }
    }

    public static class ArrayReverse
    extends SystemFunction {
        @Override
        public ArrayItem call(XPathContext context, Sequence[] arguments) throws XPathException {
            ArrayItem array = (ArrayItem)arguments[0].head();
            assert (array != null);
            ArrayList<GroundedValue> list = new ArrayList<GroundedValue>(1);
            for (int i = 0; i < array.arrayLength(); ++i) {
                list.add(array.get(array.arrayLength() - i - 1));
            }
            return new SimpleArrayItem(list);
        }
    }

    public static class ArrayRemove
    extends SystemFunction {
        @Override
        public ArrayItem call(XPathContext context, Sequence[] arguments) throws XPathException {
            ArrayItem array = (ArrayItem)arguments[0].head();
            if (arguments[1] instanceof IntegerValue) {
                int index = ArrayFunctionSet.checkSubscript((IntegerValue)arguments[1], array.arrayLength()) - 1;
                return array.remove(index);
            }
            IntHashSet positions = new IntHashSet();
            SequenceIterator arg1 = arguments[1].iterate();
            arg1.forEachOrFail(pos -> {
                int index = ArrayFunctionSet.checkSubscript((IntegerValue)pos, array.arrayLength()) - 1;
                positions.add(index);
            });
            return array.removeSeveral(positions);
        }
    }

    public static class ArrayPut
    extends SystemFunction {
        @Override
        public ArrayItem call(XPathContext context, Sequence[] arguments) throws XPathException {
            ArrayItem array = (ArrayItem)arguments[0].head();
            int index = ArrayFunctionSet.checkSubscript((IntegerValue)arguments[1].head(), array.arrayLength()) - 1;
            GroundedValue newVal = arguments[2].materialize();
            return array.put(index, newVal);
        }
    }

    public static class ArrayJoin
    extends SystemFunction {
        @Override
        public ArrayItem call(XPathContext context, Sequence[] arguments) throws XPathException {
            ArrayItem nextArray;
            SequenceIterator iterator = arguments[0].iterate();
            ArrayItem array = SimpleArrayItem.EMPTY_ARRAY;
            while ((nextArray = (ArrayItem)iterator.next()) != null) {
                array = array.concat(nextArray);
            }
            return array;
        }
    }

    public static class ArrayInsertBefore
    extends SystemFunction {
        @Override
        public ArrayItem call(XPathContext context, Sequence[] arguments) throws XPathException {
            ArrayItem array = (ArrayItem)arguments[0].head();
            assert (array != null);
            int index = ArrayFunctionSet.checkSubscript((IntegerValue)arguments[1].head(), array.arrayLength() + 1) - 1;
            if (index < 0 || index > array.arrayLength()) {
                throw new XPathException("Specified position is not in range", "FOAY0001");
            }
            Sequence newMember = arguments[2];
            return array.insert(index, newMember.materialize());
        }
    }

    public static class ArrayHead
    extends SystemFunction {
        @Override
        public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
            ArrayItem array = (ArrayItem)arguments[0].head();
            assert (array != null);
            if (array.arrayLength() == 0) {
                throw new XPathException("Argument to array:head is an empty array", "FOAY0001");
            }
            return array.get(0);
        }
    }

    public static class ArrayGet
    extends SystemFunction {
        @Override
        public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
            ArrayItem array = (ArrayItem)arguments[0].head();
            IntegerValue index = (IntegerValue)arguments[1].head();
            return array.get(ArrayFunctionSet.checkSubscript(index, array.arrayLength()) - 1);
        }
    }

    public static class ArrayForEachPair
    extends SystemFunction {
        @Override
        public ArrayItem call(XPathContext context, Sequence[] arguments) throws XPathException {
            ArrayItem array1 = (ArrayItem)arguments[0].head();
            assert (array1 != null);
            ArrayItem array2 = (ArrayItem)arguments[1].head();
            assert (array2 != null);
            Function fn = (Function)arguments[2].head();
            ArrayList<GroundedValue> list = new ArrayList<GroundedValue>(1);
            for (int i = 0; i < array1.arrayLength() && i < array2.arrayLength(); ++i) {
                list.add(ArrayForEachPair.dynamicCall(fn, context, new Sequence[]{array1.get(i), array2.get(i)}).materialize());
            }
            return new SimpleArrayItem(list);
        }
    }

    public static class ArrayForEach
    extends SystemFunction {
        @Override
        public ArrayItem call(XPathContext context, Sequence[] arguments) throws XPathException {
            ArrayItem array = (ArrayItem)arguments[0].head();
            assert (array != null);
            Function fn = (Function)arguments[1].head();
            ArrayList<GroundedValue> list = new ArrayList<GroundedValue>(1);
            for (int i = 0; i < array.arrayLength(); ++i) {
                list.add(ArrayForEach.dynamicCall(fn, context, new GroundedValue[]{array.get(i)}).materialize());
            }
            return new SimpleArrayItem(list);
        }
    }

    public static class ArrayFoldRight
    extends SystemFunction {
        @Override
        public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
            ArrayItem array = (ArrayItem)arguments[0].head();
            assert (array != null);
            Sequence zero = arguments[1];
            Function fn = (Function)arguments[2].head();
            for (int i = array.arrayLength() - 1; i >= 0; --i) {
                zero = ArrayFoldRight.dynamicCall(fn, context, new Sequence[]{array.get(i), zero});
            }
            return zero;
        }
    }

    public static class ArrayFoldLeft
    extends SystemFunction {
        @Override
        public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
            ArrayItem array = (ArrayItem)arguments[0].head();
            assert (array != null);
            int arraySize = array.arrayLength();
            Sequence zero = arguments[1];
            Function fn = (Function)arguments[2].head();
            for (int i = 0; i < arraySize; ++i) {
                zero = ArrayFoldLeft.dynamicCall(fn, context, new Sequence[]{zero, array.get(i)});
            }
            return zero;
        }
    }

    public static class ArrayFlatten
    extends SystemFunction {
        private void flatten(Sequence arg, List<Item> out) throws XPathException {
            arg.iterate().forEachOrFail(item -> {
                if (item instanceof ArrayItem) {
                    for (Sequence sequence : ((ArrayItem)item).members()) {
                        this.flatten(sequence, out);
                    }
                } else {
                    out.add(item);
                }
            });
        }

        @Override
        public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
            ArrayList<Item> out = new ArrayList<Item>();
            this.flatten(arguments[0], out);
            return SequenceExtent.makeSequenceExtent(out);
        }
    }

    public static class ArrayFilter
    extends SystemFunction {
        @Override
        public ArrayItem call(XPathContext context, Sequence[] arguments) throws XPathException {
            ArrayItem array = (ArrayItem)arguments[0].head();
            assert (array != null);
            Function fn = (Function)arguments[1].head();
            ArrayList<GroundedValue> list = new ArrayList<GroundedValue>(1);
            for (int i = 0; i < array.arrayLength(); ++i) {
                if (!((BooleanValue)ArrayFilter.dynamicCall(fn, context, new Sequence[]{array.get(i)}).head()).getBooleanValue()) continue;
                list.add(array.get(i));
            }
            return new SimpleArrayItem(list);
        }
    }

    public static class ArrayAppend
    extends SystemFunction {
        @Override
        public ArrayItem call(XPathContext context, Sequence[] arguments) throws XPathException {
            ArrayItem array = (ArrayItem)arguments[0].head();
            assert (array != null);
            return ArrayAppend.append(array, arguments[1]);
        }

        public static ArrayItem append(ArrayItem array, Sequence member) throws XPathException {
            ArrayList<GroundedValue> list = new ArrayList<GroundedValue>(1);
            list.add(member.materialize());
            SimpleArrayItem otherArray = new SimpleArrayItem(list);
            return array.concat(otherArray);
        }
    }
}


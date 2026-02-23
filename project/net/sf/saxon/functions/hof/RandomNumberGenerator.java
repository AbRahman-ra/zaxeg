/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.hof;

import java.util.LinkedList;
import java.util.Random;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.CallableFunction;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.ma.map.DictionaryMap;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.SequenceType;

public class RandomNumberGenerator
extends SystemFunction
implements Callable {
    public static final MapType RETURN_TYPE = new MapType(BuiltInAtomicType.STRING, SequenceType.SINGLE_ITEM);
    private static final FunctionItemType NEXT_FN_TYPE = new SpecificFunctionType(new SequenceType[0], SequenceType.makeSequenceType(RETURN_TYPE, 16384));
    private static final FunctionItemType PERMUTE_FN_TYPE = new SpecificFunctionType(new SequenceType[]{SequenceType.ANY_SEQUENCE}, SequenceType.ANY_SEQUENCE);

    private static MapItem generator(long seed, XPathContext context) throws XPathException {
        Random random = new Random(seed);
        double number = random.nextDouble();
        long nextSeed = random.nextLong();
        DictionaryMap map = new DictionaryMap();
        map.initialPut("number", new DoubleValue(number));
        map.initialPut("next", new CallableFunction(0, (Callable)new NextGenerator(nextSeed), NEXT_FN_TYPE));
        map.initialPut("permute", new CallableFunction(1, (Callable)new Permutation(nextSeed), PERMUTE_FN_TYPE));
        return map;
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        AtomicValue val;
        long seed = arguments.length == 0 ? context.getCurrentDateTime().getCalendar().getTimeInMillis() : ((val = (AtomicValue)arguments[0].head()) == null ? context.getCurrentDateTime().getCalendar().getTimeInMillis() : (long)val.hashCode());
        return RandomNumberGenerator.generator(seed, context);
    }

    private static class NextGenerator
    implements Callable {
        long nextSeed;

        public NextGenerator(long nextSeed) {
            this.nextSeed = nextSeed;
        }

        @Override
        public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
            return RandomNumberGenerator.generator(this.nextSeed, context);
        }

        public String toString() {
            return "random-number-generator.next";
        }
    }

    private static class Permutation
    implements Callable {
        Long nextSeed;

        public Permutation(Long nextSeed) {
            this.nextSeed = nextSeed;
        }

        @Override
        public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
            Item item;
            Sequence input = arguments[0];
            SequenceIterator iterator = input.iterate();
            LinkedList<Item> output = new LinkedList<Item>();
            Random random = new Random(this.nextSeed);
            while ((item = iterator.next()) != null) {
                int p = random.nextInt(output.size() + 1);
                output.add(p, item);
            }
            return new SequenceExtent(output);
        }

        public String toString() {
            return "random-number-generator.permute";
        }
    }
}


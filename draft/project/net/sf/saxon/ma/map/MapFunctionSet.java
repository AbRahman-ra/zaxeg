/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.functions.InsertBefore;
import net.sf.saxon.functions.OptionsParameter;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.functions.registry.BuiltInFunctionSet;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.ma.arrays.SimpleArrayItem;
import net.sf.saxon.ma.map.DictionaryMap;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.ma.map.MapUntypedContains;
import net.sf.saxon.ma.map.SingleEntryMap;
import net.sf.saxon.ma.map.TupleItemType;
import net.sf.saxon.ma.map.TupleType;
import net.sf.saxon.om.Chain;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class MapFunctionSet
extends BuiltInFunctionSet {
    public static MapFunctionSet THE_INSTANCE = new MapFunctionSet();

    public MapFunctionSet() {
        this.init();
    }

    public static MapFunctionSet getInstance() {
        return THE_INSTANCE;
    }

    private void init() {
        this.register("merge", 1, MapMerge.class, MapType.ANY_MAP_TYPE, 16384, 0).arg(0, MapType.ANY_MAP_TYPE, 0x100E000, null);
        SpecificFunctionType ON_DUPLICATES_CALLBACK_TYPE = new SpecificFunctionType(new SequenceType[]{SequenceType.ANY_SEQUENCE, SequenceType.ANY_SEQUENCE}, SequenceType.ANY_SEQUENCE);
        SequenceType oneOnDuplicatesFunction = SequenceType.makeSequenceType(ON_DUPLICATES_CALLBACK_TYPE, 16384);
        OptionsParameter mergeOptionDetails = new OptionsParameter();
        mergeOptionDetails.addAllowedOption("duplicates", SequenceType.SINGLE_STRING, new StringValue("use-first"));
        mergeOptionDetails.setAllowedValues("duplicates", "FOJS0005", "use-first", "use-last", "combine", "reject", "unspecified", "use-any", "use-callback");
        mergeOptionDetails.addAllowedOption("Q{http://saxon.sf.net/}duplicates-error-code", SequenceType.SINGLE_STRING, new StringValue("FOJS0003"));
        mergeOptionDetails.addAllowedOption("Q{http://saxon.sf.net/}key-type", SequenceType.SINGLE_STRING, new StringValue("anyAtomicType"));
        mergeOptionDetails.addAllowedOption("Q{http://saxon.sf.net/}final", SequenceType.SINGLE_BOOLEAN, BooleanValue.FALSE);
        mergeOptionDetails.addAllowedOption("Q{http://saxon.sf.net/}on-duplicates", oneOnDuplicatesFunction, null);
        this.register("merge", 2, MapMerge.class, MapType.ANY_MAP_TYPE, 16384, 0).arg(0, MapType.ANY_MAP_TYPE, 57344, null).arg(1, MapType.ANY_MAP_TYPE, 16384, null).optionDetails(mergeOptionDetails);
        this.register("entry", 2, MapEntry.class, MapType.ANY_MAP_TYPE, 16384, 0).arg(0, BuiltInAtomicType.ANY_ATOMIC, 0x2004000, null).arg(1, AnyItemType.getInstance(), 0x800E000, null);
        this.register("find", 2, MapFind.class, ArrayItemType.getInstance(), 16384, 0).arg(0, AnyItemType.getInstance(), 0x100E000, null).arg(1, BuiltInAtomicType.ANY_ATOMIC, 0x2004000, null);
        this.register("get", 2, MapGet.class, AnyItemType.getInstance(), 57344, 0).arg(0, MapType.ANY_MAP_TYPE, 0x1004000, null).arg(1, BuiltInAtomicType.ANY_ATOMIC, 0x2004000, null);
        this.register("put", 3, MapPut.class, MapType.ANY_MAP_TYPE, 16384, 0).arg(0, MapType.ANY_MAP_TYPE, 0x1004000, null).arg(1, BuiltInAtomicType.ANY_ATOMIC, 0x2004000, null).arg(2, AnyItemType.getInstance(), 0x800E000, null);
        this.register("contains", 2, MapContains.class, BuiltInAtomicType.BOOLEAN, 16384, 0).arg(0, MapType.ANY_MAP_TYPE, 0x1004000, null).arg(1, BuiltInAtomicType.ANY_ATOMIC, 0x2004000, null);
        this.register("remove", 2, MapRemove.class, MapType.ANY_MAP_TYPE, 16384, 0).arg(0, MapType.ANY_MAP_TYPE, 0x1004000, null).arg(1, BuiltInAtomicType.ANY_ATOMIC, 0x200E000, null);
        this.register("keys", 1, MapKeys.class, BuiltInAtomicType.ANY_ATOMIC, 57344, 0).arg(0, MapType.ANY_MAP_TYPE, 0x1004000, null);
        this.register("size", 1, MapSize.class, BuiltInAtomicType.INTEGER, 16384, 0).arg(0, MapType.ANY_MAP_TYPE, 0x1004000, null);
        SpecificFunctionType actionType = new SpecificFunctionType(new SequenceType[]{SequenceType.SINGLE_ATOMIC, SequenceType.ANY_SEQUENCE}, SequenceType.ANY_SEQUENCE);
        this.register("for-each", 2, MapForEach.class, AnyItemType.getInstance(), 57344, 0).arg(0, MapType.ANY_MAP_TYPE, 0x1004000, null).arg(1, actionType, 0x1004000, null);
        this.register("untyped-contains", 2, MapUntypedContains.class, BuiltInAtomicType.BOOLEAN, 16384, 0).arg(0, MapType.ANY_MAP_TYPE, 0x1004000, null).arg(1, BuiltInAtomicType.ANY_ATOMIC, 0x2004000, null);
    }

    @Override
    public String getNamespace() {
        return "http://www.w3.org/2005/xpath-functions/map";
    }

    @Override
    public String getConventionalPrefix() {
        return "map";
    }

    public static class MapSize
    extends SystemFunction {
        @Override
        public IntegerValue call(XPathContext context, Sequence[] arguments) throws XPathException {
            MapItem map = (MapItem)arguments[0].head();
            return new Int64Value(map.size());
        }
    }

    public static class MapRemove
    extends SystemFunction {
        @Override
        public MapItem call(XPathContext context, Sequence[] arguments) throws XPathException {
            AtomicValue key;
            MapItem map = (MapItem)arguments[0].head();
            SequenceIterator iter = arguments[1].iterate();
            while ((key = (AtomicValue)iter.next()) != null) {
                map = map.remove(key);
            }
            return map;
        }
    }

    public static class MapPut
    extends SystemFunction {
        @Override
        public MapItem call(XPathContext context, Sequence[] arguments) throws XPathException {
            MapItem baseMap = (MapItem)arguments[0].head();
            if (!(baseMap instanceof HashTrieMap)) {
                baseMap = HashTrieMap.copy(baseMap);
            }
            AtomicValue key = (AtomicValue)arguments[1].head();
            GroundedValue value = arguments[2].materialize();
            return baseMap.addEntry(key, value);
        }
    }

    public static class MapMerge
    extends SystemFunction {
        static final String finalKey = "Q{http://saxon.sf.net/}final";
        static final String keyTypeKey = "Q{http://saxon.sf.net/}key-type";
        static final String onDuplicatesKey = "Q{http://saxon.sf.net/}on-duplicates";
        static final String errorCodeKey = "Q{http://saxon.sf.net/}duplicates-error-code";
        private String duplicates = "use-first";
        private String duplicatesErrorCode = "FOJS0003";
        private Function onDuplicates = null;
        private boolean allStringKeys = false;
        private boolean treatAsFinal = false;

        @Override
        public Expression makeOptimizedFunctionCall(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo, Expression ... arguments) throws XPathException {
            if (arguments.length == 2 && arguments[1] instanceof Literal) {
                MapItem options = (MapItem)((Literal)arguments[1]).getValue().head();
                Map<String, Sequence> values = this.getDetails().optionDetails.processSuppliedOptions(options, visitor.getStaticContext().makeEarlyEvaluationContext());
                String duplicates = ((StringValue)values.get("duplicates")).getStringValue();
                String duplicatesErrorCode = ((StringValue)values.get(errorCodeKey)).getStringValue();
                Function onDuplicates = (Function)values.get(onDuplicatesKey);
                if (onDuplicates != null) {
                    duplicates = "use-callback";
                }
                boolean isFinal = ((BooleanValue)values.get(finalKey)).getBooleanValue();
                String keyType = ((StringValue)values.get(keyTypeKey)).getStringValue();
                MapMerge mm2 = (MapMerge)MapFunctionSet.getInstance().makeFunction("merge", 1);
                mm2.duplicates = duplicates;
                mm2.duplicatesErrorCode = duplicatesErrorCode;
                mm2.onDuplicates = onDuplicates;
                mm2.allStringKeys = keyType.equals("string");
                mm2.treatAsFinal = isFinal;
                return mm2.makeFunctionCall(arguments[0]);
            }
            return super.makeOptimizedFunctionCall(visitor, contextInfo, arguments);
        }

        @Override
        public ItemType getResultItemType(Expression[] args) {
            ItemType it = args[0].getItemType();
            if (it == ErrorType.getInstance()) {
                return MapType.EMPTY_MAP_TYPE;
            }
            if (it instanceof MapType) {
                boolean maybeCombined = true;
                if (args.length == 1) {
                    maybeCombined = false;
                } else if (args[1] instanceof Literal) {
                    MapItem options = (MapItem)((Literal)args[1]).getValue().head();
                    GroundedValue dupes = options.get(new StringValue("duplicates"));
                    try {
                        if (dupes != null && !"combine".equals(dupes.getStringValue())) {
                            maybeCombined = false;
                        }
                    } catch (XPathException xPathException) {
                        // empty catch block
                    }
                }
                if (maybeCombined) {
                    return new MapType(((MapType)it).getKeyType(), SequenceType.makeSequenceType(((MapType)it).getValueType().getPrimaryType(), 57344));
                }
                return it;
            }
            return super.getResultItemType(args);
        }

        @Override
        public MapItem call(XPathContext context, Sequence[] arguments) throws XPathException {
            MapItem next;
            MapItem baseMap;
            SequenceIterator iter;
            String duplicates = this.duplicates;
            String duplicatesErrorCode = this.duplicatesErrorCode;
            boolean allStringKeys = this.allStringKeys;
            boolean treatAsFinal = this.treatAsFinal;
            Function onDuplicates = this.onDuplicates;
            if (arguments.length > 1) {
                MapItem options = (MapItem)arguments[1].head();
                Map<String, Sequence> values = this.getDetails().optionDetails.processSuppliedOptions(options, context);
                duplicates = ((StringValue)values.get("duplicates")).getStringValue();
                duplicatesErrorCode = ((StringValue)values.get(errorCodeKey)).getStringValue();
                treatAsFinal = ((BooleanValue)values.get(finalKey)).getBooleanValue();
                allStringKeys = "string".equals(((StringValue)values.get(keyTypeKey)).getStringValue());
                onDuplicates = (Function)values.get(onDuplicatesKey);
                if (onDuplicates != null) {
                    duplicates = "use-callback";
                }
            }
            if (treatAsFinal && allStringKeys) {
                MapItem next2;
                iter = arguments[0].iterate();
                baseMap = new DictionaryMap();
                switch (duplicates) {
                    case "unspecified": 
                    case "use-any": 
                    case "use-last": {
                        while ((next2 = (MapItem)iter.next()) != null) {
                            for (KeyValuePair pair : next2.keyValuePairs()) {
                                if (!(pair.key instanceof StringValue)) {
                                    throw new XPathException("The keys in this map must all be strings (found " + pair.key.getItemType() + ")");
                                }
                                ((DictionaryMap)baseMap).initialPut(pair.key.getStringValue(), pair.value);
                            }
                        }
                        break;
                    }
                }
                while ((next2 = (MapItem)iter.next()) != null) {
                    for (KeyValuePair pair : next2.keyValuePairs()) {
                        if (!(pair.key instanceof StringValue)) {
                            throw new XPathException("The keys in this map must all be strings (found " + pair.key.getItemType() + ")");
                        }
                        GroundedValue existing = ((DictionaryMap)baseMap).get(pair.key);
                        if (existing != null) {
                            switch (duplicates) {
                                case "use-first": 
                                case "unspecified": 
                                case "use-any": {
                                    break;
                                }
                                case "use-last": {
                                    ((DictionaryMap)baseMap).initialPut(pair.key.getStringValue(), pair.value);
                                    break;
                                }
                                case "combine": {
                                    InsertBefore.InsertIterator combinedIter = new InsertBefore.InsertIterator(pair.value.iterate(), existing.iterate(), 1);
                                    GroundedValue combinedValue = combinedIter.materialize();
                                    ((DictionaryMap)baseMap).initialPut(pair.key.getStringValue(), combinedValue);
                                    break;
                                }
                                case "use-callback": {
                                    Sequence[] args = new Sequence[]{existing, pair.value};
                                    Sequence combined = onDuplicates.call(context, args);
                                    ((DictionaryMap)baseMap).initialPut(pair.key.getStringValue(), combined.materialize());
                                    break;
                                }
                                default: {
                                    throw new XPathException("Duplicate key in constructed map: " + Err.wrap(pair.key.getStringValueCS()), duplicatesErrorCode);
                                }
                            }
                            continue;
                        }
                        ((DictionaryMap)baseMap).initialPut(pair.key.getStringValue(), pair.value);
                    }
                }
                return baseMap;
            }
            iter = arguments[0].iterate();
            baseMap = (MapItem)iter.next();
            if (baseMap == null) {
                return new HashTrieMap();
            }
            while ((next = (MapItem)iter.next()) != null) {
                boolean inverse = next.size() > baseMap.size();
                MapItem larger = inverse ? next : baseMap;
                MapItem smaller = inverse ? baseMap : next;
                String dup = inverse ? this.invertDuplicates(duplicates) : duplicates;
                for (KeyValuePair pair : smaller.keyValuePairs()) {
                    GroundedValue existing = larger.get(pair.key);
                    if (existing != null) {
                        switch (dup) {
                            case "use-first": 
                            case "unspecified": 
                            case "use-any": {
                                break;
                            }
                            case "use-last": {
                                larger = larger.addEntry(pair.key, pair.value);
                                break;
                            }
                            case "combine": {
                                InsertBefore.InsertIterator combinedIter = new InsertBefore.InsertIterator(pair.value.iterate(), existing.iterate(), 1);
                                GroundedValue combinedValue = combinedIter.materialize();
                                larger = larger.addEntry(pair.key, combinedValue);
                                break;
                            }
                            case "combine-reverse": {
                                InsertBefore.InsertIterator combinedIter = new InsertBefore.InsertIterator(existing.iterate(), pair.value.iterate(), 1);
                                GroundedValue combinedValue = combinedIter.materialize();
                                larger = larger.addEntry(pair.key, combinedValue);
                                break;
                            }
                            case "use-callback": {
                                Sequence[] args;
                                assert (onDuplicates != null);
                                if (inverse) {
                                    Sequence[] sequenceArray;
                                    if (onDuplicates.getArity() == 2) {
                                        Sequence[] sequenceArray2 = new Sequence[2];
                                        sequenceArray2[0] = pair.value;
                                        sequenceArray = sequenceArray2;
                                        sequenceArray2[1] = existing;
                                    } else {
                                        Sequence[] sequenceArray3 = new Sequence[3];
                                        sequenceArray3[0] = pair.value;
                                        sequenceArray3[1] = existing;
                                        sequenceArray = sequenceArray3;
                                        sequenceArray3[2] = pair.key;
                                    }
                                    args = sequenceArray;
                                } else {
                                    Sequence[] sequenceArray;
                                    if (onDuplicates.getArity() == 2) {
                                        Sequence[] sequenceArray4 = new Sequence[2];
                                        sequenceArray4[0] = existing;
                                        sequenceArray = sequenceArray4;
                                        sequenceArray4[1] = pair.value;
                                    } else {
                                        Sequence[] sequenceArray5 = new Sequence[3];
                                        sequenceArray5[0] = existing;
                                        sequenceArray5[1] = pair.value;
                                        sequenceArray = sequenceArray5;
                                        sequenceArray5[2] = pair.key;
                                    }
                                    args = sequenceArray;
                                }
                                Sequence combined = onDuplicates.call(context, args);
                                larger = larger.addEntry(pair.key, combined.materialize());
                                break;
                            }
                            default: {
                                throw new XPathException("Duplicate key in constructed map: " + Err.wrap(pair.key.getStringValue()), duplicatesErrorCode);
                            }
                        }
                        continue;
                    }
                    larger = larger.addEntry(pair.key, pair.value);
                }
                baseMap = larger;
            }
            return baseMap;
        }

        private String invertDuplicates(String duplicates) {
            switch (duplicates) {
                case "use-first": 
                case "unspecified": 
                case "use-any": {
                    return "use-last";
                }
                case "use-last": {
                    return "use-first";
                }
                case "combine": {
                    return "combine-reverse";
                }
                case "combine-reverse": {
                    return "combine";
                }
            }
            return duplicates;
        }

        @Override
        public String getStreamerName() {
            return "NewMap";
        }

        @Override
        public void exportAdditionalArguments(SystemFunctionCall call, ExpressionPresenter out) throws XPathException {
            if (call.getArity() == 1) {
                HashTrieMap options = new HashTrieMap();
                options.initialPut(new StringValue("duplicates"), new StringValue(this.duplicates));
                options.initialPut(new StringValue("duplicates-error-code"), new StringValue(this.duplicatesErrorCode));
                Literal.exportValue(options, out);
            }
        }
    }

    public static class MapKeys
    extends SystemFunction {
        @Override
        public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
            MapItem map = (MapItem)arguments[0].head();
            assert (map != null);
            return SequenceTool.toLazySequence(map.keys());
        }
    }

    public static class MapForEach
    extends SystemFunction {
        @Override
        public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
            MapItem map = (MapItem)arguments[0].head();
            Function fn = (Function)arguments[1].head();
            ArrayList<GroundedValue> results = new ArrayList<GroundedValue>();
            for (KeyValuePair pair : map.keyValuePairs()) {
                Sequence seq = MapForEach.dynamicCall(fn, context, new Sequence[]{pair.key, pair.value});
                GroundedValue val = seq.materialize();
                if (val.getLength() <= 0) continue;
                results.add(val);
            }
            return new Chain(results);
        }
    }

    public static class MapEntry
    extends SystemFunction {
        @Override
        public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
            AtomicValue key = (AtomicValue)arguments[0].head();
            assert (key != null);
            GroundedValue value = arguments[1].iterate().materialize();
            return new SingleEntryMap(key, value);
        }

        @Override
        public ItemType getResultItemType(Expression[] args) {
            PlainType ku = args[0].getItemType().getAtomizedItemType();
            AtomicType ka = ku instanceof AtomicType ? (AtomicType)ku : ku.getPrimitiveItemType();
            return new MapType(ka, SequenceType.makeSequenceType(args[1].getItemType(), args[1].getCardinality()));
        }

        @Override
        public String getStreamerName() {
            return "MapEntry";
        }
    }

    public static class MapFind
    extends SystemFunction {
        @Override
        public ArrayItem call(XPathContext context, Sequence[] arguments) throws XPathException {
            ArrayList<GroundedValue> result = new ArrayList<GroundedValue>();
            AtomicValue key = (AtomicValue)arguments[1].head();
            this.processSequence(arguments[0], key, result);
            return new SimpleArrayItem(result);
        }

        private void processSequence(Sequence in, AtomicValue key, List<GroundedValue> result) throws XPathException {
            in.iterate().forEachOrFail(item -> {
                block4: {
                    block3: {
                        if (!(item instanceof ArrayItem)) break block3;
                        for (Sequence sequence : ((ArrayItem)item).members()) {
                            this.processSequence(sequence, key, result);
                        }
                        break block4;
                    }
                    if (!(item instanceof MapItem)) break block4;
                    GroundedValue value = ((MapItem)item).get(key);
                    if (value != null) {
                        result.add(value);
                    }
                    for (KeyValuePair entry : ((MapItem)item).keyValuePairs()) {
                        this.processSequence(entry.value, key, result);
                    }
                }
            });
        }
    }

    public static class MapGet
    extends SystemFunction {
        String pendingWarning = null;

        @Override
        public void supplyTypeInformation(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType, Expression[] arguments) throws XPathException {
            ItemType it = arguments[0].getItemType();
            if (it instanceof TupleType) {
                String key;
                if (arguments[1] instanceof Literal && ((TupleType)it).getFieldType(key = ((Literal)arguments[1]).getValue().getStringValue()) == null) {
                    XPathException xe = new XPathException("Field " + key + " is not defined for tuple type " + it, "SXTT0001");
                    xe.setIsTypeError(true);
                    throw xe;
                }
                TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
                Affinity relation = th.relationship(arguments[1].getItemType(), BuiltInAtomicType.STRING);
                if (relation == Affinity.DISJOINT) {
                    XPathException xe = new XPathException("Key for tuple type must be a string (actual type is " + arguments[1].getItemType(), "XPTY0004");
                    xe.setIsTypeError(true);
                    throw xe;
                }
            }
        }

        @Override
        public ItemType getResultItemType(Expression[] args) {
            ItemType mapType = args[0].getItemType();
            if (mapType instanceof TupleItemType && args[1] instanceof StringLiteral) {
                TupleItemType tit = (TupleItemType)mapType;
                String key = ((StringLiteral)args[1]).getStringValue();
                SequenceType valueType = tit.getFieldType(key);
                if (valueType == null) {
                    this.warning("Field " + key + " is not defined in tuple type");
                    return AnyItemType.getInstance();
                }
                return valueType.getPrimaryType();
            }
            if (mapType instanceof MapType) {
                return ((MapType)mapType).getValueType().getPrimaryType();
            }
            return super.getResultItemType(args);
        }

        @Override
        public int getCardinality(Expression[] args) {
            ItemType mapType = args[0].getItemType();
            if (mapType instanceof TupleItemType && args[1] instanceof StringLiteral) {
                TupleItemType tit = (TupleItemType)mapType;
                String key = ((StringLiteral)args[1]).getStringValue();
                SequenceType valueType = tit.getFieldType(key);
                if (valueType == null) {
                    this.warning("Field " + key + " is not defined in tuple type");
                    return 32768;
                }
                return valueType.getCardinality();
            }
            if (mapType instanceof MapType) {
                return Cardinality.union(((MapType)mapType).getValueType().getCardinality(), 8192);
            }
            return super.getCardinality(args);
        }

        @Override
        public Expression makeOptimizedFunctionCall(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo, Expression ... arguments) throws XPathException {
            if (this.pendingWarning != null && !this.pendingWarning.equals("DONE")) {
                visitor.issueWarning(this.pendingWarning, arguments[0].getLocation());
                this.pendingWarning = "DONE";
            }
            return null;
        }

        private void warning(String message) {
            if (!"DONE".equals(this.pendingWarning)) {
                this.pendingWarning = message;
            }
        }

        @Override
        public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
            MapItem map = (MapItem)arguments[0].head();
            assert (map != null);
            AtomicValue key = (AtomicValue)arguments[1].head();
            GroundedValue value = map.get(key);
            if (value == null) {
                return EmptySequence.getInstance();
            }
            return value;
        }
    }

    public static class MapContains
    extends SystemFunction {
        @Override
        public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
            AtomicValue key;
            MapItem map = (MapItem)arguments[0].head();
            return BooleanValue.get(map.get(key = (AtomicValue)arguments[1].head()) != null);
        }
    }
}


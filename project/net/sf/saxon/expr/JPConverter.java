/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExternalObjectModel;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.One;
import net.sf.saxon.om.OneOrMore;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.om.ZeroOrMore;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.s9api.XdmArray;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmEmptySequence;
import net.sf.saxon.s9api.XdmFunctionItem;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmMap;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyFunctionType;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.Closure;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DateValue;
import net.sf.saxon.value.DayTimeDurationValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.DurationValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.ExternalObject;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.GDayValue;
import net.sf.saxon.value.GMonthDayValue;
import net.sf.saxon.value.GMonthValue;
import net.sf.saxon.value.GYearMonthValue;
import net.sf.saxon.value.GYearValue;
import net.sf.saxon.value.HexBinaryValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.NotationValue;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.TimeValue;
import net.sf.saxon.value.UntypedAtomicValue;
import net.sf.saxon.value.YearMonthDurationValue;

public abstract class JPConverter {
    private static HashMap<Class<?>, JPConverter> converterMap = new HashMap();
    private static Map<Class<?>, ItemType> itemTypeMap;
    private static Map<Class<?>, Integer> cardinalityMap;

    public static JPConverter allocate(Class javaClass, Type genericType, Configuration config) {
        JPConverter c;
        if (QName.class.isAssignableFrom(javaClass)) {
            return FromQName.INSTANCE;
        }
        if (Sequence.class.isAssignableFrom(javaClass)) {
            if (genericType instanceof ParameterizedType) {
                Type[] params = ((ParameterizedType)genericType).getActualTypeArguments();
                if (params.length == 1 && params[0] instanceof Class && Item.class.isAssignableFrom((Class)params[0])) {
                    ItemType itemType = itemTypeMap.get((Class)params[0]);
                    Integer cardinality = cardinalityMap.get(javaClass);
                    if (itemType != null && cardinality != null) {
                        return new FromSequence(itemType, cardinality);
                    }
                }
            } else {
                ItemType itemType = itemTypeMap.get(javaClass);
                if (itemType != null) {
                    return new FromSequence(itemType, 24576);
                }
            }
        }
        if ((c = converterMap.get(javaClass)) != null) {
            return c;
        }
        if (javaClass.equals(Object.class)) {
            return FromObject.INSTANCE;
        }
        if (NodeInfo.class.isAssignableFrom(javaClass)) {
            return new FromSequence(AnyNodeTest.getInstance(), 24576);
        }
        if (Source.class.isAssignableFrom(javaClass) && !DOMSource.class.isAssignableFrom(javaClass)) {
            return FromSource.INSTANCE;
        }
        for (Map.Entry<Class<?>, JPConverter> e : converterMap.entrySet()) {
            if (!e.getKey().isAssignableFrom(javaClass)) continue;
            return (JPConverter)e.getValue();
        }
        List<ExternalObjectModel> externalObjectModels = config.getExternalObjectModels();
        for (ExternalObjectModel model : externalObjectModels) {
            JPConverter converter = model.getJPConverter(javaClass, config);
            if (converter == null) continue;
            return converter;
        }
        if (javaClass.isArray()) {
            Class<?> itemClass = javaClass.getComponentType();
            return new FromObjectArray(JPConverter.allocate(itemClass, null, config));
        }
        if (javaClass.equals(Void.TYPE)) {
            return VoidConverter.INSTANCE;
        }
        return new ExternalObjectWrapper(config.getJavaExternalObjectType(javaClass));
    }

    public abstract Sequence convert(Object var1, XPathContext var2) throws XPathException;

    public abstract ItemType getItemType();

    public int getCardinality() {
        return 16384;
    }

    static {
        converterMap.put(XdmValue.class, new FromXdmValue(AnyItemType.getInstance(), 57344));
        converterMap.put(XdmItem.class, new FromXdmValue(AnyItemType.getInstance(), 16384));
        converterMap.put(XdmAtomicValue.class, new FromXdmValue(BuiltInAtomicType.ANY_ATOMIC, 16384));
        converterMap.put(XdmNode.class, new FromXdmValue(AnyNodeTest.getInstance(), 16384));
        converterMap.put(XdmFunctionItem.class, new FromXdmValue(AnyFunctionType.getInstance(), 16384));
        converterMap.put(XdmMap.class, new FromXdmValue(MapType.ANY_MAP_TYPE, 16384));
        converterMap.put(XdmArray.class, new FromXdmValue(ArrayItemType.getInstance(), 16384));
        converterMap.put(XdmEmptySequence.class, new FromXdmValue(ErrorType.getInstance(), 8192));
        converterMap.put(SequenceIterator.class, FromSequenceIterator.INSTANCE);
        converterMap.put(Sequence.class, FromSequence.INSTANCE);
        converterMap.put(OneOrMore.class, FromSequence.INSTANCE);
        converterMap.put(One.class, FromSequence.INSTANCE);
        converterMap.put(ZeroOrOne.class, FromSequence.INSTANCE);
        converterMap.put(ZeroOrMore.class, FromSequence.INSTANCE);
        converterMap.put(String.class, FromString.INSTANCE);
        converterMap.put(Boolean.class, FromBoolean.INSTANCE);
        converterMap.put(Boolean.TYPE, FromBoolean.INSTANCE);
        converterMap.put(Double.class, FromDouble.INSTANCE);
        converterMap.put(Double.TYPE, FromDouble.INSTANCE);
        converterMap.put(Float.class, FromFloat.INSTANCE);
        converterMap.put(Float.TYPE, FromFloat.INSTANCE);
        converterMap.put(BigDecimal.class, FromBigDecimal.INSTANCE);
        converterMap.put(BigInteger.class, FromBigInteger.INSTANCE);
        converterMap.put(Long.class, FromLong.INSTANCE);
        converterMap.put(Long.TYPE, FromLong.INSTANCE);
        converterMap.put(Integer.class, FromInt.INSTANCE);
        converterMap.put(Integer.TYPE, FromInt.INSTANCE);
        converterMap.put(Short.class, FromShort.INSTANCE);
        converterMap.put(Short.TYPE, FromShort.INSTANCE);
        converterMap.put(Byte.class, FromByte.INSTANCE);
        converterMap.put(Byte.TYPE, FromByte.INSTANCE);
        converterMap.put(Character.class, FromCharacter.INSTANCE);
        converterMap.put(Character.TYPE, FromCharacter.INSTANCE);
        converterMap.put(URI.class, FromURI.INSTANCE);
        converterMap.put(URL.class, FromURI.INSTANCE);
        converterMap.put(Date.class, FromDate.INSTANCE);
        converterMap.put(Instant.class, FromInstant.INSTANCE);
        converterMap.put(LocalDateTime.class, FromLocalDateTime.INSTANCE);
        converterMap.put(ZonedDateTime.class, FromZonedDateTime.INSTANCE);
        converterMap.put(OffsetDateTime.class, FromOffsetDateTime.INSTANCE);
        converterMap.put(LocalDate.class, FromLocalDate.INSTANCE);
        converterMap.put(long[].class, FromLongArray.INSTANCE);
        converterMap.put(int[].class, FromIntArray.INSTANCE);
        converterMap.put(short[].class, FromShortArray.INSTANCE);
        converterMap.put(byte[].class, FromByteArray.INSTANCE);
        converterMap.put(char[].class, FromCharArray.INSTANCE);
        converterMap.put(double[].class, FromDoubleArray.INSTANCE);
        converterMap.put(float[].class, FromFloatArray.INSTANCE);
        converterMap.put(boolean[].class, FromBooleanArray.INSTANCE);
        converterMap.put(Collection.class, FromCollection.INSTANCE);
        itemTypeMap = new HashMap();
        itemTypeMap.put(BooleanValue.class, BuiltInAtomicType.BOOLEAN);
        itemTypeMap.put(StringValue.class, BuiltInAtomicType.STRING);
        itemTypeMap.put(DoubleValue.class, BuiltInAtomicType.DOUBLE);
        itemTypeMap.put(FloatValue.class, BuiltInAtomicType.FLOAT);
        itemTypeMap.put(BigDecimalValue.class, BuiltInAtomicType.DECIMAL);
        itemTypeMap.put(IntegerValue.class, BuiltInAtomicType.INTEGER);
        itemTypeMap.put(DurationValue.class, BuiltInAtomicType.DURATION);
        itemTypeMap.put(DayTimeDurationValue.class, BuiltInAtomicType.DAY_TIME_DURATION);
        itemTypeMap.put(YearMonthDurationValue.class, BuiltInAtomicType.YEAR_MONTH_DURATION);
        itemTypeMap.put(DateTimeValue.class, BuiltInAtomicType.DATE_TIME);
        itemTypeMap.put(DateValue.class, BuiltInAtomicType.DATE);
        itemTypeMap.put(TimeValue.class, BuiltInAtomicType.TIME);
        itemTypeMap.put(GYearValue.class, BuiltInAtomicType.G_YEAR);
        itemTypeMap.put(GYearMonthValue.class, BuiltInAtomicType.G_YEAR_MONTH);
        itemTypeMap.put(GMonthValue.class, BuiltInAtomicType.G_MONTH);
        itemTypeMap.put(GMonthDayValue.class, BuiltInAtomicType.G_MONTH_DAY);
        itemTypeMap.put(GDayValue.class, BuiltInAtomicType.G_DAY);
        itemTypeMap.put(AnyURIValue.class, BuiltInAtomicType.ANY_URI);
        itemTypeMap.put(QNameValue.class, BuiltInAtomicType.QNAME);
        itemTypeMap.put(NotationValue.class, BuiltInAtomicType.NOTATION);
        itemTypeMap.put(HexBinaryValue.class, BuiltInAtomicType.HEX_BINARY);
        itemTypeMap.put(Base64BinaryValue.class, BuiltInAtomicType.BASE64_BINARY);
        itemTypeMap.put(NodeInfo.class, AnyNodeTest.getInstance());
        itemTypeMap.put(TreeInfo.class, NodeKindTest.DOCUMENT);
        itemTypeMap.put(MapItem.class, MapType.getInstance());
        itemTypeMap.put(ArrayItem.class, ArrayItemType.getInstance());
        itemTypeMap.put(Function.class, AnyFunctionType.getInstance());
        itemTypeMap.put(AtomicValue.class, BuiltInAtomicType.ANY_ATOMIC);
        itemTypeMap.put(UntypedAtomicValue.class, BuiltInAtomicType.UNTYPED_ATOMIC);
        cardinalityMap = new HashMap();
        cardinalityMap.put(Sequence.class, 57344);
        cardinalityMap.put(ZeroOrMore.class, 57344);
        cardinalityMap.put(OneOrMore.class, 49152);
        cardinalityMap.put(One.class, 16384);
        cardinalityMap.put(ZeroOrOne.class, 24576);
        cardinalityMap.put(XdmValue.class, 57344);
        cardinalityMap.put(XdmItem.class, 57344);
        cardinalityMap.put(XdmEmptySequence.class, 8192);
    }

    public static class FromObjectArray
    extends JPConverter {
        private JPConverter itemConverter;

        public FromObjectArray(JPConverter itemConverter) {
            this.itemConverter = itemConverter;
        }

        @Override
        public Sequence convert(Object object, XPathContext context) throws XPathException {
            Object[] arrayObject = (Object[])object;
            ArrayList<Item> newArray = new ArrayList<Item>(arrayObject.length);
            boolean a = false;
            for (Object member : arrayObject) {
                if (member != null) {
                    try {
                        Item newItem = SequenceTool.asItem(this.itemConverter.convert(member, context));
                        if (newItem == null) continue;
                        newArray.add(newItem);
                        continue;
                    } catch (XPathException e) {
                        throw new XPathException("Returned array contains an object that cannot be converted to an Item (" + member.getClass() + "): " + e.getMessage(), "SXJE0051");
                    }
                }
                throw new XPathException("Returned array contains null values: cannot convert to items", "SXJE0051");
            }
            return new SequenceExtent(newArray);
        }

        @Override
        public ItemType getItemType() {
            return this.itemConverter.getItemType();
        }

        @Override
        public int getCardinality() {
            return 57344;
        }
    }

    public static class FromBooleanArray
    extends JPConverter {
        public static final FromBooleanArray INSTANCE = new FromBooleanArray();

        @Override
        public Sequence convert(Object object, XPathContext context) throws XPathException {
            Item[] array = new Item[((boolean[])object).length];
            for (int i = 0; i < array.length; ++i) {
                array[i] = BooleanValue.get(((boolean[])object)[i]);
            }
            return new SequenceExtent(array);
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.BOOLEAN;
        }

        @Override
        public int getCardinality() {
            return 57344;
        }
    }

    public static class FromFloatArray
    extends JPConverter {
        public static final FromFloatArray INSTANCE = new FromFloatArray();

        @Override
        public Sequence convert(Object object, XPathContext context) throws XPathException {
            Item[] array = new Item[((float[])object).length];
            for (int i = 0; i < array.length; ++i) {
                array[i] = new DoubleValue(((float[])object)[i]);
            }
            return new SequenceExtent(array);
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.FLOAT;
        }

        @Override
        public int getCardinality() {
            return 57344;
        }
    }

    public static class FromDoubleArray
    extends JPConverter {
        public static final FromDoubleArray INSTANCE = new FromDoubleArray();

        @Override
        public Sequence convert(Object object, XPathContext context) throws XPathException {
            Item[] array = new Item[((double[])object).length];
            for (int i = 0; i < array.length; ++i) {
                array[i] = new DoubleValue(((double[])object)[i]);
            }
            return new SequenceExtent(array);
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.DOUBLE;
        }

        @Override
        public int getCardinality() {
            return 57344;
        }
    }

    public static class FromCharArray
    extends JPConverter {
        public static final FromCharArray INSTANCE = new FromCharArray();

        @Override
        public StringValue convert(Object object, XPathContext context) throws XPathException {
            return StringValue.makeStringValue(new String((char[])object));
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.STRING;
        }
    }

    public static class FromByteArray
    extends JPConverter {
        public static final FromByteArray INSTANCE = new FromByteArray();

        @Override
        public Sequence convert(Object object, XPathContext context) throws XPathException {
            Item[] array = new Item[((byte[])object).length];
            for (int i = 0; i < array.length; ++i) {
                array[i] = Int64Value.makeDerived(0xFF & ((byte[])object)[i], BuiltInAtomicType.UNSIGNED_BYTE);
            }
            return new SequenceExtent(array);
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.UNSIGNED_BYTE;
        }

        @Override
        public int getCardinality() {
            return 57344;
        }
    }

    public static class FromShortArray
    extends JPConverter {
        public static final FromShortArray INSTANCE = new FromShortArray();

        @Override
        public Sequence convert(Object object, XPathContext context) throws XPathException {
            Item[] array = new Item[((short[])object).length];
            for (int i = 0; i < array.length; ++i) {
                array[i] = Int64Value.makeDerived(((short[])object)[i], BuiltInAtomicType.SHORT);
            }
            return new SequenceExtent(array);
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.SHORT;
        }

        @Override
        public int getCardinality() {
            return 57344;
        }
    }

    public static class FromIntArray
    extends JPConverter {
        public static final FromIntArray INSTANCE = new FromIntArray();

        @Override
        public Sequence convert(Object object, XPathContext context) throws XPathException {
            Item[] array = new Item[((int[])object).length];
            for (int i = 0; i < array.length; ++i) {
                array[i] = Int64Value.makeDerived(((int[])object)[i], BuiltInAtomicType.INT);
            }
            return new SequenceExtent(array);
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.INT;
        }

        @Override
        public int getCardinality() {
            return 57344;
        }
    }

    public static class FromLongArray
    extends JPConverter {
        public static final FromLongArray INSTANCE = new FromLongArray();

        @Override
        public Sequence convert(Object object, XPathContext context) throws XPathException {
            Item[] array = new Item[((long[])object).length];
            for (int i = 0; i < array.length; ++i) {
                array[i] = Int64Value.makeDerived(((long[])object)[i], BuiltInAtomicType.LONG);
            }
            return new SequenceExtent(array);
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.LONG;
        }

        @Override
        public int getCardinality() {
            return 57344;
        }
    }

    public static class FromSource
    extends JPConverter {
        public static final FromSource INSTANCE = new FromSource();

        @Override
        public NodeInfo convert(Object object, XPathContext context) throws XPathException {
            ParseOptions options = new ParseOptions();
            Controller controller = context.getController();
            if (controller != null) {
                options.setSchemaValidationMode(controller.getSchemaValidationMode());
            }
            if (object instanceof TreeInfo) {
                return ((TreeInfo)object).getRootNode();
            }
            return context.getConfiguration().buildDocumentTree((Source)object, options).getRootNode();
        }

        @Override
        public ItemType getItemType() {
            return AnyNodeTest.getInstance();
        }
    }

    public static class FromCollection
    extends JPConverter {
        public static final FromCollection INSTANCE = new FromCollection();

        @Override
        public Sequence convert(Object object, XPathContext context) throws XPathException {
            ArrayList<Item> list = new ArrayList<Item>(((Collection)object).size());
            boolean a = false;
            for (Object obj : (Collection)object) {
                JPConverter itemConverter = FromCollection.allocate(obj.getClass(), null, context.getConfiguration());
                try {
                    Item item = SequenceTool.asItem(itemConverter.convert(obj, context));
                    if (item == null) continue;
                    list.add(item);
                } catch (XPathException e) {
                    throw new XPathException("Returned Collection contains an object that cannot be converted to an Item (" + obj.getClass() + "): " + e.getMessage(), "SXJE0051");
                }
            }
            return new SequenceExtent(list);
        }

        @Override
        public ItemType getItemType() {
            return AnyItemType.getInstance();
        }

        @Override
        public int getCardinality() {
            return 57344;
        }
    }

    public static class VoidConverter
    extends JPConverter {
        public static final VoidConverter INSTANCE = new VoidConverter();

        @Override
        public EmptySequence convert(Object object, XPathContext context) throws XPathException {
            return EmptySequence.getInstance();
        }

        @Override
        public ItemType getItemType() {
            return AnyItemType.getInstance();
        }
    }

    public static class ExternalObjectWrapper
    extends JPConverter {
        private JavaExternalObjectType resultType;

        public ExternalObjectWrapper(JavaExternalObjectType resultType) {
            this.resultType = resultType;
        }

        @Override
        public ExternalObject<Object> convert(Object object, XPathContext context) throws XPathException {
            if (object == null) {
                return null;
            }
            if (this.resultType.getJavaClass().isInstance(object)) {
                return new ObjectValue<Object>(object);
            }
            throw new XPathException("Java external object of type " + object.getClass().getName() + " is not an instance of the required type " + this.resultType.getJavaClass().getName(), "XPTY0004");
        }

        @Override
        public JavaExternalObjectType getItemType() {
            return this.resultType;
        }
    }

    public static class FromLocalDate
    extends JPConverter {
        public static final FromLocalDate INSTANCE = new FromLocalDate();

        @Override
        public DateValue convert(Object object, XPathContext context) throws XPathException {
            return new DateValue((LocalDate)object);
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.DATE_TIME;
        }
    }

    public static class FromLocalDateTime
    extends JPConverter {
        public static final FromLocalDateTime INSTANCE = new FromLocalDateTime();

        @Override
        public DateTimeValue convert(Object object, XPathContext context) throws XPathException {
            return DateTimeValue.fromLocalDateTime((LocalDateTime)object);
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.DATE_TIME;
        }
    }

    public static class FromOffsetDateTime
    extends JPConverter {
        public static final FromOffsetDateTime INSTANCE = new FromOffsetDateTime();

        @Override
        public DateTimeValue convert(Object object, XPathContext context) throws XPathException {
            return DateTimeValue.fromOffsetDateTime((OffsetDateTime)object);
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.DATE_TIME;
        }
    }

    public static class FromZonedDateTime
    extends JPConverter {
        public static final FromZonedDateTime INSTANCE = new FromZonedDateTime();

        @Override
        public DateTimeValue convert(Object object, XPathContext context) throws XPathException {
            return DateTimeValue.fromZonedDateTime((ZonedDateTime)object);
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.DATE_TIME;
        }
    }

    public static class FromInstant
    extends JPConverter {
        public static final FromInstant INSTANCE = new FromInstant();

        @Override
        public DateTimeValue convert(Object object, XPathContext context) throws XPathException {
            return DateTimeValue.fromJavaInstant((Instant)object);
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.DATE_TIME;
        }
    }

    public static class FromDate
    extends JPConverter {
        public static final FromDate INSTANCE = new FromDate();

        @Override
        public DateTimeValue convert(Object object, XPathContext context) throws XPathException {
            return DateTimeValue.fromJavaDate((Date)object);
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.DATE_TIME;
        }
    }

    public static class FromURI
    extends JPConverter {
        public static final FromURI INSTANCE = new FromURI();

        @Override
        public AnyURIValue convert(Object object, XPathContext context) throws XPathException {
            return new AnyURIValue(object.toString());
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.ANY_URI;
        }
    }

    public static class FromQName
    extends JPConverter {
        public static final FromQName INSTANCE = new FromQName();

        @Override
        public QNameValue convert(Object object, XPathContext context) throws XPathException {
            QName qn = (QName)object;
            return new QNameValue(qn.getPrefix(), qn.getNamespaceURI(), qn.getLocalPart());
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.QNAME;
        }
    }

    public static class FromCharacter
    extends JPConverter {
        public static final FromCharacter INSTANCE = new FromCharacter();

        @Override
        public StringValue convert(Object object, XPathContext context) throws XPathException {
            return new StringValue(object.toString());
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.STRING;
        }
    }

    public static class FromByte
    extends JPConverter {
        public static final FromByte INSTANCE = new FromByte();

        @Override
        public Int64Value convert(Object object, XPathContext context) throws XPathException {
            return new Int64Value(((Byte)object).intValue());
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.INTEGER;
        }
    }

    public static class FromShort
    extends JPConverter {
        public static final FromShort INSTANCE = new FromShort();

        @Override
        public Int64Value convert(Object object, XPathContext context) throws XPathException {
            return new Int64Value(((Short)object).intValue());
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.INTEGER;
        }
    }

    public static class FromInt
    extends JPConverter {
        public static final FromInt INSTANCE = new FromInt();

        @Override
        public Int64Value convert(Object object, XPathContext context) throws XPathException {
            return new Int64Value(((Integer)object).intValue());
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.INTEGER;
        }
    }

    public static class FromLong
    extends JPConverter {
        public static final FromLong INSTANCE = new FromLong();

        @Override
        public Int64Value convert(Object object, XPathContext context) throws XPathException {
            return new Int64Value((Long)object);
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.INTEGER;
        }
    }

    public static class FromBigInteger
    extends JPConverter {
        public static final FromBigInteger INSTANCE = new FromBigInteger();

        @Override
        public IntegerValue convert(Object object, XPathContext context) throws XPathException {
            return IntegerValue.makeIntegerValue((BigInteger)object);
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.INTEGER;
        }
    }

    public static class FromBigDecimal
    extends JPConverter {
        public static final FromBigDecimal INSTANCE = new FromBigDecimal();

        @Override
        public BigDecimalValue convert(Object object, XPathContext context) throws XPathException {
            return new BigDecimalValue((BigDecimal)object);
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.DECIMAL;
        }
    }

    public static class FromFloat
    extends JPConverter {
        public static final FromFloat INSTANCE = new FromFloat();

        @Override
        public FloatValue convert(Object object, XPathContext context) throws XPathException {
            return new FloatValue(((Float)object).floatValue());
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.FLOAT;
        }
    }

    public static class FromDouble
    extends JPConverter {
        public static final FromDouble INSTANCE = new FromDouble();

        @Override
        public DoubleValue convert(Object object, XPathContext context) throws XPathException {
            return new DoubleValue((Double)object);
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.DOUBLE;
        }
    }

    public static class FromBoolean
    extends JPConverter {
        public static final FromBoolean INSTANCE = new FromBoolean();

        @Override
        public BooleanValue convert(Object object, XPathContext context) throws XPathException {
            return BooleanValue.get((Boolean)object);
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.BOOLEAN;
        }
    }

    public static class FromString
    extends JPConverter {
        public static final FromString INSTANCE = new FromString();

        @Override
        public StringValue convert(Object object, XPathContext context) throws XPathException {
            return new StringValue((String)object);
        }

        @Override
        public ItemType getItemType() {
            return BuiltInAtomicType.STRING;
        }
    }

    public static class FromSequence
    extends JPConverter {
        public static final FromSequence INSTANCE = new FromSequence(AnyItemType.getInstance(), 57344);
        private ItemType resultType;
        private int cardinality;

        public FromSequence(ItemType resultType, int cardinality) {
            this.resultType = resultType;
            this.cardinality = cardinality;
        }

        @Override
        public Sequence convert(Object object, XPathContext context) throws XPathException {
            return object instanceof Closure ? ((Closure)object).iterate().materialize() : (Sequence)object;
        }

        @Override
        public ItemType getItemType() {
            return this.resultType;
        }

        @Override
        public int getCardinality() {
            return this.cardinality;
        }
    }

    public static class FromXdmValue
    extends JPConverter {
        private ItemType resultType;
        private int cardinality;

        public FromXdmValue(ItemType resultType, int cardinality) {
            this.resultType = resultType;
            this.cardinality = cardinality;
        }

        @Override
        public Sequence convert(Object object, XPathContext context) throws XPathException {
            return ((XdmValue)object).getUnderlyingValue();
        }

        @Override
        public ItemType getItemType() {
            return this.resultType;
        }

        @Override
        public int getCardinality() {
            return this.cardinality;
        }
    }

    public static class FromSequenceIterator
    extends JPConverter {
        public static final FromSequenceIterator INSTANCE = new FromSequenceIterator();

        @Override
        public Sequence convert(Object object, XPathContext context) throws XPathException {
            return ((SequenceIterator)object).materialize();
        }

        @Override
        public ItemType getItemType() {
            return AnyItemType.getInstance();
        }

        @Override
        public int getCardinality() {
            return 57344;
        }
    }

    public static class FromObject
    extends JPConverter {
        public static final FromObject INSTANCE = new FromObject();

        @Override
        public Sequence convert(Object object, XPathContext context) throws XPathException {
            Class<?> theClass = object.getClass();
            JPConverter instanceConverter = FromObject.allocate(theClass, null, context.getConfiguration());
            if (instanceConverter instanceof FromObject) {
                instanceConverter = new ExternalObjectWrapper(context.getConfiguration().getJavaExternalObjectType(theClass));
            }
            return instanceConverter.convert(object, context);
        }

        @Override
        public ItemType getItemType() {
            return AnyItemType.getInstance();
        }

        @Override
        public int getCardinality() {
            return 57344;
        }
    }
}


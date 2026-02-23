/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.xml.namespace.QName;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExternalObjectModel;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
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
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.wrapper.VirtualNode;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.CalendarValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.Closure;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DateValue;
import net.sf.saxon.value.DayTimeDurationValue;
import net.sf.saxon.value.DecimalValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.DurationValue;
import net.sf.saxon.value.ExternalObject;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.GDayValue;
import net.sf.saxon.value.GMonthDayValue;
import net.sf.saxon.value.GMonthValue;
import net.sf.saxon.value.GYearMonthValue;
import net.sf.saxon.value.GYearValue;
import net.sf.saxon.value.HexBinaryValue;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.NotationValue;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.QualifiedNameValue;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.TimeValue;
import net.sf.saxon.value.UntypedAtomicValue;
import net.sf.saxon.value.YearMonthDurationValue;

public abstract class PJConverter {
    private static HashMap<Class, SequenceType> jpmap = new HashMap();

    public static SequenceType getEquivalentSequenceType(Class javaClass) {
        if (javaClass.isArray()) {
            Class<?> memberClass = javaClass.getComponentType();
            if (memberClass == Byte.TYPE) {
                return SequenceType.makeSequenceType(BuiltInAtomicType.UNSIGNED_BYTE, 57344);
            }
            SequenceType memberType = PJConverter.getEquivalentSequenceType(memberClass);
            if (memberType != null) {
                return SequenceType.makeSequenceType(memberType.getPrimaryType(), 57344);
            }
        }
        return jpmap.get(javaClass);
    }

    public static SequenceType getParameterizedSequenceType(Type javaType) {
        ParameterizedType aType;
        Type[] parameterArgTypes;
        if (javaType instanceof ParameterizedType && (parameterArgTypes = (aType = (ParameterizedType)javaType).getActualTypeArguments()).length == 1 && parameterArgTypes[0] instanceof Class && Item.class.isAssignableFrom((Class)parameterArgTypes[0])) {
            SequenceType memberType = PJConverter.getEquivalentSequenceType((Class)parameterArgTypes[0]);
            ItemType itemType = memberType == null ? null : memberType.getPrimaryType();
            Type collectionType = aType.getRawType();
            int cardinality = -1;
            if (collectionType.equals(ZeroOrOne.class)) {
                cardinality = 24576;
            } else if (collectionType.equals(One.class)) {
                cardinality = 16384;
            } else if (collectionType.equals(OneOrMore.class)) {
                cardinality = 49152;
            } else if (collectionType.equals(ZeroOrMore.class)) {
                cardinality = 57344;
            }
            if (itemType != null && cardinality != -1) {
                return SequenceType.makeSequenceType(itemType, cardinality);
            }
        }
        return null;
    }

    public abstract Object convert(Sequence var1, Class<?> var2, XPathContext var3) throws XPathException;

    public static PJConverter allocate(Configuration config, ItemType itemType, int cardinality, Class<?> targetClass) throws XPathException {
        TypeHierarchy th = config.getTypeHierarchy();
        if (targetClass == SequenceIterator.class) {
            return ToSequenceIterator.INSTANCE;
        }
        if (targetClass == Sequence.class || targetClass == Item.class) {
            return Identity.INSTANCE;
        }
        if (targetClass == One.class) {
            return ToOne.INSTANCE;
        }
        if (targetClass == ZeroOrOne.class) {
            return ToZeroOrOne.INSTANCE;
        }
        if (targetClass == OneOrMore.class) {
            return ToOneOrMore.INSTANCE;
        }
        if (targetClass == ZeroOrMore.class) {
            return ToZeroOrMore.INSTANCE;
        }
        if (targetClass == GroundedValue.class | targetClass == SequenceExtent.class) {
            return ToSequenceExtent.INSTANCE;
        }
        if (!itemType.isPlainType()) {
            List<ExternalObjectModel> externalObjectModels = config.getExternalObjectModels();
            for (ExternalObjectModel model : externalObjectModels) {
                PJConverter converter = model.getPJConverter(targetClass);
                if (converter == null) continue;
                return converter;
            }
            if (NodeInfo.class.isAssignableFrom(targetClass)) {
                return Identity.INSTANCE;
            }
        }
        if (Collection.class.isAssignableFrom(targetClass)) {
            return ToCollection.INSTANCE;
        }
        if (targetClass.isArray()) {
            PJConverter itemConverter = PJConverter.allocate(config, itemType, 16384, targetClass.getComponentType());
            return new ToArray(itemConverter);
        }
        if (!Cardinality.allowsMany(cardinality)) {
            if (itemType.isPlainType()) {
                if (itemType == ErrorType.getInstance()) {
                    return StringValueToString.INSTANCE;
                }
                if (th.isSubType(itemType, BuiltInAtomicType.STRING)) {
                    if (targetClass == Object.class || targetClass == String.class || targetClass == CharSequence.class) {
                        return StringValueToString.INSTANCE;
                    }
                    if (targetClass.isAssignableFrom(StringValue.class)) {
                        return Identity.INSTANCE;
                    }
                    if (targetClass == Character.TYPE || targetClass == Character.class) {
                        return StringValueToChar.INSTANCE;
                    }
                    throw PJConverter.cannotConvert(itemType, targetClass, config);
                }
                if (itemType == BuiltInAtomicType.UNTYPED_ATOMIC) {
                    if (targetClass == Object.class || targetClass == String.class || targetClass == CharSequence.class) {
                        return StringValueToString.INSTANCE;
                    }
                    if (targetClass.isAssignableFrom(UntypedAtomicValue.class)) {
                        return Identity.INSTANCE;
                    }
                    try {
                        final Constructor<?> constructor = targetClass.getConstructor(String.class);
                        return new PJConverter(){

                            @Override
                            public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
                                try {
                                    return constructor.newInstance(value.head().getStringValue());
                                } catch (IllegalAccessException | InstantiationException e) {
                                    throw new XPathException(e);
                                } catch (InvocationTargetException e) {
                                    throw new XPathException("Cannot convert untypedAtomic to " + targetClass.getName() + ": " + e.getMessage(), "FORG0001");
                                }
                            }
                        };
                    } catch (NoSuchMethodException e) {
                        throw PJConverter.cannotConvert(itemType, targetClass, config);
                    }
                }
                if (th.isSubType(itemType, BuiltInAtomicType.BOOLEAN)) {
                    if (targetClass == Object.class || targetClass == Boolean.class || targetClass == Boolean.TYPE) {
                        return BooleanValueToBoolean.INSTANCE;
                    }
                    if (targetClass.isAssignableFrom(BooleanValue.class)) {
                        return Identity.INSTANCE;
                    }
                    throw PJConverter.cannotConvert(itemType, targetClass, config);
                }
                if (th.isSubType(itemType, BuiltInAtomicType.INTEGER)) {
                    if (targetClass == Object.class || targetClass == BigInteger.class) {
                        return IntegerValueToBigInteger.INSTANCE;
                    }
                    if (targetClass == Long.TYPE || targetClass == Long.class) {
                        return IntegerValueToLong.INSTANCE;
                    }
                    if (targetClass == Integer.TYPE || targetClass == Integer.class) {
                        return IntegerValueToInt.INSTANCE;
                    }
                    if (targetClass == Short.TYPE || targetClass == Short.class) {
                        return IntegerValueToShort.INSTANCE;
                    }
                    if (targetClass == Byte.TYPE || targetClass == Byte.class) {
                        return IntegerValueToByte.INSTANCE;
                    }
                    if (targetClass == Character.TYPE || targetClass == Character.class) {
                        return IntegerValueToChar.INSTANCE;
                    }
                    if (targetClass == Double.TYPE || targetClass == Double.class) {
                        return NumericValueToDouble.INSTANCE;
                    }
                    if (targetClass == Float.TYPE || targetClass == Float.class) {
                        return NumericValueToFloat.INSTANCE;
                    }
                    if (targetClass == BigDecimal.class) {
                        return NumericValueToBigDecimal.INSTANCE;
                    }
                    if (targetClass.isAssignableFrom(IntegerValue.class)) {
                        return Identity.INSTANCE;
                    }
                    throw PJConverter.cannotConvert(itemType, targetClass, config);
                }
                if (th.isSubType(itemType, BuiltInAtomicType.DECIMAL)) {
                    if (targetClass == Object.class || targetClass == BigDecimal.class) {
                        return NumericValueToBigDecimal.INSTANCE;
                    }
                    if (targetClass == Double.TYPE || targetClass == Double.class) {
                        return NumericValueToDouble.INSTANCE;
                    }
                    if (targetClass == Float.TYPE || targetClass == Float.class) {
                        return NumericValueToFloat.INSTANCE;
                    }
                    if (targetClass.isAssignableFrom(BigDecimalValue.class)) {
                        return Identity.INSTANCE;
                    }
                    throw PJConverter.cannotConvert(itemType, targetClass, config);
                }
                if (th.isSubType(itemType, BuiltInAtomicType.FLOAT)) {
                    if (targetClass == Object.class || targetClass == Float.class || targetClass == Float.TYPE) {
                        return NumericValueToFloat.INSTANCE;
                    }
                    if (targetClass == Double.TYPE || targetClass == Double.class) {
                        return NumericValueToDouble.INSTANCE;
                    }
                    if (targetClass.isAssignableFrom(FloatValue.class)) {
                        return Identity.INSTANCE;
                    }
                    throw PJConverter.cannotConvert(itemType, targetClass, config);
                }
                if (th.isSubType(itemType, BuiltInAtomicType.DOUBLE)) {
                    if (targetClass == Object.class || targetClass == Double.class || targetClass == Double.TYPE) {
                        return NumericValueToDouble.INSTANCE;
                    }
                    if (targetClass.isAssignableFrom(DoubleValue.class)) {
                        return Identity.INSTANCE;
                    }
                    return Atomic.INSTANCE;
                }
                if (th.isSubType(itemType, BuiltInAtomicType.ANY_URI)) {
                    if (targetClass == Object.class || URI.class.isAssignableFrom(targetClass)) {
                        return AnyURIValueToURI.INSTANCE;
                    }
                    if (URL.class.isAssignableFrom(targetClass)) {
                        return AnyURIValueToURL.INSTANCE;
                    }
                    if (targetClass == String.class || targetClass == CharSequence.class) {
                        return StringValueToString.INSTANCE;
                    }
                    if (targetClass.isAssignableFrom(AnyURIValue.class)) {
                        return Identity.INSTANCE;
                    }
                    throw PJConverter.cannotConvert(itemType, targetClass, config);
                }
                if (th.isSubType(itemType, BuiltInAtomicType.QNAME)) {
                    if (targetClass == Object.class || targetClass == QName.class) {
                        return QualifiedNameValueToQName.INSTANCE;
                    }
                    if (targetClass.isAssignableFrom(QNameValue.class)) {
                        return Identity.INSTANCE;
                    }
                    throw PJConverter.cannotConvert(itemType, targetClass, config);
                }
                if (th.isSubType(itemType, BuiltInAtomicType.NOTATION)) {
                    if (targetClass == Object.class || targetClass == QName.class) {
                        return QualifiedNameValueToQName.INSTANCE;
                    }
                    if (targetClass.isAssignableFrom(NotationValue.class)) {
                        return Identity.INSTANCE;
                    }
                    throw PJConverter.cannotConvert(itemType, targetClass, config);
                }
                if (th.isSubType(itemType, BuiltInAtomicType.DURATION)) {
                    if (targetClass.isAssignableFrom(DurationValue.class)) {
                        return Identity.INSTANCE;
                    }
                    throw PJConverter.cannotConvert(itemType, targetClass, config);
                }
                if (th.isSubType(itemType, BuiltInAtomicType.DATE_TIME)) {
                    if (targetClass.isAssignableFrom(DateTimeValue.class)) {
                        return Identity.INSTANCE;
                    }
                    if (targetClass == Date.class) {
                        return CalendarValueToDate.INSTANCE;
                    }
                    if (targetClass == Calendar.class) {
                        return CalendarValueToCalendar.INSTANCE;
                    }
                    if (targetClass == Instant.class) {
                        return CalendarValueToInstant.INSTANCE;
                    }
                    if (targetClass == ZonedDateTime.class) {
                        return CalendarValueToZonedDateTime.INSTANCE;
                    }
                    if (targetClass == LocalDateTime.class) {
                        return CalendarValueToLocalDateTime.INSTANCE;
                    }
                    throw PJConverter.cannotConvert(itemType, targetClass, config);
                }
                if (th.isSubType(itemType, BuiltInAtomicType.DATE)) {
                    if (targetClass.isAssignableFrom(DateValue.class)) {
                        return Identity.INSTANCE;
                    }
                    if (targetClass == Date.class) {
                        return CalendarValueToDate.INSTANCE;
                    }
                    if (targetClass == Calendar.class) {
                        return CalendarValueToCalendar.INSTANCE;
                    }
                    if (targetClass == Instant.class) {
                        return CalendarValueToInstant.INSTANCE;
                    }
                    if (targetClass == ZonedDateTime.class) {
                        return CalendarValueToZonedDateTime.INSTANCE;
                    }
                    if (targetClass == OffsetDateTime.class) {
                        return CalendarValueToOffsetDateTime.INSTANCE;
                    }
                    if (targetClass == LocalDateTime.class) {
                        return CalendarValueToLocalDateTime.INSTANCE;
                    }
                    if (targetClass == LocalDate.class) {
                        return DateValueToLocalDate.INSTANCE;
                    }
                    throw PJConverter.cannotConvert(itemType, targetClass, config);
                }
                if (th.isSubType(itemType, BuiltInAtomicType.TIME)) {
                    if (targetClass.isAssignableFrom(TimeValue.class)) {
                        return Identity.INSTANCE;
                    }
                    if (targetClass == Date.class) {
                        return CalendarValueToDate.INSTANCE;
                    }
                    if (targetClass == Calendar.class) {
                        return CalendarValueToCalendar.INSTANCE;
                    }
                    if (targetClass == Instant.class) {
                        return CalendarValueToInstant.INSTANCE;
                    }
                    if (targetClass == ZonedDateTime.class) {
                        return CalendarValueToZonedDateTime.INSTANCE;
                    }
                    throw PJConverter.cannotConvert(itemType, targetClass, config);
                }
                if (th.isSubType(itemType, BuiltInAtomicType.G_YEAR)) {
                    if (targetClass.isAssignableFrom(GYearValue.class)) {
                        return Identity.INSTANCE;
                    }
                    throw PJConverter.cannotConvert(itemType, targetClass, config);
                }
                if (th.isSubType(itemType, BuiltInAtomicType.G_YEAR_MONTH)) {
                    if (targetClass.isAssignableFrom(GYearMonthValue.class)) {
                        return Identity.INSTANCE;
                    }
                    throw PJConverter.cannotConvert(itemType, targetClass, config);
                }
                if (th.isSubType(itemType, BuiltInAtomicType.G_MONTH)) {
                    if (targetClass.isAssignableFrom(GMonthValue.class)) {
                        return Identity.INSTANCE;
                    }
                    throw PJConverter.cannotConvert(itemType, targetClass, config);
                }
                if (th.isSubType(itemType, BuiltInAtomicType.G_MONTH_DAY)) {
                    if (targetClass.isAssignableFrom(GMonthDayValue.class)) {
                        return Identity.INSTANCE;
                    }
                    throw PJConverter.cannotConvert(itemType, targetClass, config);
                }
                if (th.isSubType(itemType, BuiltInAtomicType.G_DAY)) {
                    if (targetClass.isAssignableFrom(GDayValue.class)) {
                        return Identity.INSTANCE;
                    }
                    throw PJConverter.cannotConvert(itemType, targetClass, config);
                }
                if (th.isSubType(itemType, BuiltInAtomicType.BASE64_BINARY)) {
                    if (targetClass.isAssignableFrom(Base64BinaryValue.class)) {
                        return Identity.INSTANCE;
                    }
                    throw PJConverter.cannotConvert(itemType, targetClass, config);
                }
                if (th.isSubType(itemType, BuiltInAtomicType.HEX_BINARY)) {
                    if (targetClass.isAssignableFrom(HexBinaryValue.class)) {
                        return Identity.INSTANCE;
                    }
                    throw PJConverter.cannotConvert(itemType, targetClass, config);
                }
                return Atomic.INSTANCE;
            }
            if (itemType instanceof JavaExternalObjectType) {
                return UnwrapExternalObject.INSTANCE;
            }
            if (itemType instanceof ErrorType) {
                return ToNull.INSTANCE;
            }
            if (itemType instanceof NodeTest) {
                if (NodeInfo.class.isAssignableFrom(targetClass)) {
                    return Identity.INSTANCE;
                }
                return General.INSTANCE;
            }
            return General.INSTANCE;
        }
        return General.INSTANCE;
    }

    private static XPathException cannotConvert(ItemType source, Class target, Configuration config) {
        return new XPathException("Cannot convert from " + source + " to " + target.getName());
    }

    public static PJConverter allocateNodeListCreator(Configuration config, Object node) {
        List<ExternalObjectModel> externalObjectModels = config.getExternalObjectModels();
        for (ExternalObjectModel model : externalObjectModels) {
            PJConverter converter = model.getNodeListCreator(node);
            if (converter == null) continue;
            return converter;
        }
        return ToCollection.INSTANCE;
    }

    static {
        jpmap.put(Boolean.TYPE, SequenceType.SINGLE_BOOLEAN);
        jpmap.put(Boolean.class, SequenceType.OPTIONAL_BOOLEAN);
        jpmap.put(String.class, SequenceType.OPTIONAL_STRING);
        jpmap.put(CharSequence.class, SequenceType.OPTIONAL_STRING);
        jpmap.put(Long.TYPE, SequenceType.SINGLE_INTEGER);
        jpmap.put(Long.class, SequenceType.OPTIONAL_INTEGER);
        jpmap.put(Integer.TYPE, SequenceType.SINGLE_INTEGER);
        jpmap.put(Integer.class, SequenceType.OPTIONAL_INTEGER);
        jpmap.put(Short.TYPE, SequenceType.SINGLE_SHORT);
        jpmap.put(Short.class, SequenceType.OPTIONAL_SHORT);
        jpmap.put(Byte.TYPE, SequenceType.SINGLE_BYTE);
        jpmap.put(Byte.class, SequenceType.OPTIONAL_BYTE);
        jpmap.put(Float.TYPE, SequenceType.SINGLE_FLOAT);
        jpmap.put(Float.class, SequenceType.OPTIONAL_FLOAT);
        jpmap.put(Double.TYPE, SequenceType.SINGLE_DOUBLE);
        jpmap.put(Double.class, SequenceType.OPTIONAL_DOUBLE);
        jpmap.put(URI.class, SequenceType.OPTIONAL_ANY_URI);
        jpmap.put(URL.class, SequenceType.OPTIONAL_ANY_URI);
        jpmap.put(BigInteger.class, SequenceType.OPTIONAL_INTEGER);
        jpmap.put(BigDecimal.class, SequenceType.OPTIONAL_DECIMAL);
        jpmap.put(StringValue.class, SequenceType.OPTIONAL_STRING);
        jpmap.put(BooleanValue.class, SequenceType.OPTIONAL_BOOLEAN);
        jpmap.put(DoubleValue.class, SequenceType.OPTIONAL_DOUBLE);
        jpmap.put(FloatValue.class, SequenceType.OPTIONAL_FLOAT);
        jpmap.put(DecimalValue.class, SequenceType.OPTIONAL_DECIMAL);
        jpmap.put(IntegerValue.class, SequenceType.OPTIONAL_INTEGER);
        jpmap.put(AnyURIValue.class, SequenceType.OPTIONAL_ANY_URI);
        jpmap.put(QNameValue.class, SequenceType.OPTIONAL_QNAME);
        jpmap.put(NotationValue.class, SequenceType.OPTIONAL_NOTATION);
        jpmap.put(DateValue.class, SequenceType.OPTIONAL_DATE);
        jpmap.put(DateTimeValue.class, SequenceType.OPTIONAL_DATE_TIME);
        jpmap.put(TimeValue.class, SequenceType.OPTIONAL_TIME);
        jpmap.put(DurationValue.class, SequenceType.OPTIONAL_DURATION);
        jpmap.put(DayTimeDurationValue.class, SequenceType.OPTIONAL_DAY_TIME_DURATION);
        jpmap.put(YearMonthDurationValue.class, SequenceType.OPTIONAL_YEAR_MONTH_DURATION);
        jpmap.put(GYearValue.class, SequenceType.OPTIONAL_G_YEAR);
        jpmap.put(GYearMonthValue.class, SequenceType.OPTIONAL_G_YEAR_MONTH);
        jpmap.put(GMonthValue.class, SequenceType.OPTIONAL_G_MONTH);
        jpmap.put(GMonthDayValue.class, SequenceType.OPTIONAL_G_MONTH_DAY);
        jpmap.put(GDayValue.class, SequenceType.OPTIONAL_G_DAY);
        jpmap.put(Base64BinaryValue.class, SequenceType.OPTIONAL_BASE64_BINARY);
        jpmap.put(HexBinaryValue.class, SequenceType.OPTIONAL_HEX_BINARY);
        jpmap.put(Function.class, SequenceType.OPTIONAL_FUNCTION_ITEM);
        jpmap.put(MapItem.class, MapType.OPTIONAL_MAP_ITEM);
        jpmap.put(NodeInfo.class, SequenceType.OPTIONAL_NODE);
        jpmap.put(TreeInfo.class, SequenceType.OPTIONAL_DOCUMENT_NODE);
    }

    public static class General
    extends PJConverter {
        public static final General INSTANCE = new General();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            GroundedValue gv;
            Configuration config = context.getConfiguration();
            PJConverter converter = General.allocate(config, SequenceTool.getItemType(gv = value.materialize(), config.getTypeHierarchy()), SequenceTool.getCardinality(gv), targetClass);
            if (converter instanceof General) {
                converter = Identity.INSTANCE;
            }
            return converter.convert(gv, targetClass, context);
        }
    }

    public static class Atomic
    extends PJConverter {
        public static final Atomic INSTANCE = new Atomic();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            AtomicValue item = (AtomicValue)value.head();
            if (item == null) {
                return null;
            }
            Configuration config = context.getConfiguration();
            PJConverter converter = Atomic.allocate(config, item.getItemType(), 16384, targetClass);
            return converter.convert(item, targetClass, context);
        }
    }

    public static class CalendarValueToCalendar
    extends PJConverter {
        public static final CalendarValueToCalendar INSTANCE = new CalendarValueToCalendar();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            CalendarValue cv = (CalendarValue)value.head();
            return cv == null ? null : cv.getCalendar();
        }
    }

    public static class DateValueToLocalDate
    extends PJConverter {
        public static final DateValueToLocalDate INSTANCE = new DateValueToLocalDate();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            DateValue cv = (DateValue)value.head();
            return cv == null ? null : cv.toLocalDate();
        }
    }

    public static class CalendarValueToDate
    extends PJConverter {
        public static final CalendarValueToDate INSTANCE = new CalendarValueToDate();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            CalendarValue cv = (CalendarValue)value.head();
            return cv == null ? null : cv.getCalendar().getTime();
        }
    }

    public static class CalendarValueToLocalDateTime
    extends PJConverter {
        public static final CalendarValueToLocalDateTime INSTANCE = new CalendarValueToLocalDateTime();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            CalendarValue cv = (CalendarValue)value.head();
            return cv == null ? null : cv.toDateTime().toLocalDateTime();
        }
    }

    public static class CalendarValueToOffsetDateTime
    extends PJConverter {
        public static final CalendarValueToOffsetDateTime INSTANCE = new CalendarValueToOffsetDateTime();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            CalendarValue cv = (CalendarValue)value.head();
            return cv == null ? null : cv.toDateTime().toOffsetDateTime();
        }
    }

    public static class CalendarValueToZonedDateTime
    extends PJConverter {
        public static final CalendarValueToZonedDateTime INSTANCE = new CalendarValueToZonedDateTime();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            CalendarValue cv = (CalendarValue)value.head();
            return cv == null ? null : cv.toDateTime().toZonedDateTime();
        }
    }

    public static class CalendarValueToInstant
    extends PJConverter {
        public static final CalendarValueToInstant INSTANCE = new CalendarValueToInstant();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            CalendarValue cv = (CalendarValue)value.head();
            return cv == null ? null : cv.toDateTime().toJavaInstant();
        }
    }

    public static class QualifiedNameValueToQName
    extends PJConverter {
        public static final QualifiedNameValueToQName INSTANCE = new QualifiedNameValueToQName();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            QualifiedNameValue qv = (QualifiedNameValue)value.head();
            return qv == null ? null : qv.toJaxpQName();
        }
    }

    public static class AnyURIValueToURL
    extends PJConverter {
        public static final AnyURIValueToURL INSTANCE = new AnyURIValueToURL();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            AnyURIValue av = (AnyURIValue)value.head();
            try {
                return av == null ? null : new URL(((AnyURIValue)value).getStringValue());
            } catch (MalformedURLException err) {
                throw new XPathException("The anyURI value '" + value + "' is not an acceptable Java URL");
            }
        }
    }

    public static class AnyURIValueToURI
    extends PJConverter {
        public static final AnyURIValueToURI INSTANCE = new AnyURIValueToURI();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            AnyURIValue av = (AnyURIValue)value.head();
            try {
                return av == null ? null : new URI(((AnyURIValue)value).getStringValue());
            } catch (URISyntaxException err) {
                throw new XPathException("The anyURI value '" + value + "' is not an acceptable Java URI");
            }
        }
    }

    public static class NumericValueToFloat
    extends PJConverter {
        public static final NumericValueToFloat INSTANCE = new NumericValueToFloat();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            NumericValue nv = (NumericValue)value.head();
            assert (nv != null);
            return Float.valueOf(nv.getFloatValue());
        }
    }

    public static class NumericValueToDouble
    extends PJConverter {
        public static final NumericValueToDouble INSTANCE = new NumericValueToDouble();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            NumericValue nv = (NumericValue)value.head();
            assert (nv != null);
            return nv.getDoubleValue();
        }
    }

    public static class NumericValueToBigDecimal
    extends PJConverter {
        public static final NumericValueToBigDecimal INSTANCE = new NumericValueToBigDecimal();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            NumericValue nv = (NumericValue)value.head();
            return nv == null ? null : nv.getDecimalValue();
        }
    }

    public static class IntegerValueToChar
    extends PJConverter {
        public static final IntegerValueToChar INSTANCE = new IntegerValueToChar();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            IntegerValue iv = (IntegerValue)value.head();
            assert (iv != null);
            return Character.valueOf((char)iv.longValue());
        }
    }

    public static class IntegerValueToByte
    extends PJConverter {
        public static final IntegerValueToByte INSTANCE = new IntegerValueToByte();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            IntegerValue iv = (IntegerValue)value.head();
            assert (iv != null);
            return (byte)iv.longValue();
        }
    }

    public static class IntegerValueToShort
    extends PJConverter {
        public static final IntegerValueToShort INSTANCE = new IntegerValueToShort();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            IntegerValue iv = (IntegerValue)value.head();
            assert (iv != null);
            return (short)iv.longValue();
        }
    }

    public static class IntegerValueToInt
    extends PJConverter {
        public static final IntegerValueToInt INSTANCE = new IntegerValueToInt();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            IntegerValue iv = (IntegerValue)value.head();
            assert (iv != null);
            return (int)iv.longValue();
        }
    }

    public static class IntegerValueToLong
    extends PJConverter {
        public static final IntegerValueToLong INSTANCE = new IntegerValueToLong();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            IntegerValue iv = (IntegerValue)value.head();
            assert (iv != null);
            return iv.longValue();
        }
    }

    public static class IntegerValueToBigInteger
    extends PJConverter {
        public static final IntegerValueToBigInteger INSTANCE = new IntegerValueToBigInteger();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            IntegerValue val = (IntegerValue)value.head();
            return val == null ? null : val.asBigInteger();
        }
    }

    public static class BooleanValueToBoolean
    extends PJConverter {
        public static final BooleanValueToBoolean INSTANCE = new BooleanValueToBoolean();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            BooleanValue bv = (BooleanValue)value.head();
            assert (bv != null);
            return bv.getBooleanValue();
        }
    }

    public static class StringValueToChar
    extends PJConverter {
        public static final StringValueToChar INSTANCE = new StringValueToChar();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            Item first = value.head();
            if (first == null) {
                return null;
            }
            String str = first.getStringValue();
            if (str.length() == 1) {
                return Character.valueOf(str.charAt(0));
            }
            XPathException de = new XPathException("Cannot convert xs:string to Java char unless length is 1");
            de.setXPathContext(context);
            de.setErrorCode("SXJE0005");
            throw de;
        }
    }

    public static class StringValueToString
    extends PJConverter {
        public static final StringValueToString INSTANCE = new StringValueToString();

        @Override
        public String convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            Item first = value.head();
            return first == null ? null : first.getStringValue();
        }
    }

    public static class UnwrapExternalObject
    extends PJConverter {
        public static final UnwrapExternalObject INSTANCE = new UnwrapExternalObject();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            Object obj;
            ObjectValue<Sequence> head = value.head();
            if (head == null) {
                return null;
            }
            if (!(head instanceof ExternalObject)) {
                if (Sequence.class.isAssignableFrom(targetClass)) {
                    head = new ObjectValue<Sequence>(value);
                } else {
                    throw new XPathException("Expected external object of class " + targetClass.getName() + ", got " + head.getClass());
                }
            }
            if (!targetClass.isAssignableFrom((obj = ((ExternalObject)head).getObject()).getClass())) {
                throw new XPathException("External object has wrong class (is " + obj.getClass().getName() + ", expected " + targetClass.getName() + ")");
            }
            return obj;
        }
    }

    public static class Identity
    extends PJConverter {
        public static final Identity INSTANCE = new Identity();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            Object obj;
            if (value instanceof Closure) {
                value = ((Closure)value).reduce();
            }
            if (value instanceof ZeroOrOne) {
                value = ((ZeroOrOne)value).head();
            }
            if (value instanceof VirtualNode && targetClass.isAssignableFrom((obj = ((VirtualNode)value).getRealNode()).getClass())) {
                return obj;
            }
            if (targetClass.isAssignableFrom(value.getClass())) {
                return value;
            }
            GroundedValue gv = value.materialize();
            if (targetClass.isAssignableFrom(gv.getClass())) {
                return gv;
            }
            if (targetClass.isAssignableFrom((gv = gv.reduce()).getClass())) {
                return gv;
            }
            if (gv.getLength() == 0) {
                return null;
            }
            throw new XPathException("Cannot convert value " + value.getClass() + " of type " + SequenceTool.getItemType(value, context.getConfiguration().getTypeHierarchy()) + " to class " + targetClass.getName());
        }
    }

    public static class ToZeroOrMore
    extends PJConverter {
        public static final ToZeroOrMore INSTANCE = new ToZeroOrMore();

        @Override
        public ZeroOrMore<Item> convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            return new ZeroOrMore<Item>(value.iterate());
        }
    }

    public static class ToOneOrMore
    extends PJConverter {
        public static final ToOneOrMore INSTANCE = new ToOneOrMore();

        @Override
        public OneOrMore<Item> convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            return OneOrMore.makeOneOrMore(value);
        }
    }

    public static class ToZeroOrOne
    extends PJConverter {
        public static final ToZeroOrOne INSTANCE = new ToZeroOrOne();

        @Override
        public ZeroOrOne convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            return new ZeroOrOne<Item>(value.head());
        }
    }

    public static class ToOne
    extends PJConverter {
        public static final ToOne INSTANCE = new ToOne();

        @Override
        public One convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            return new One<Item>(value.head());
        }
    }

    public static class ToArray
    extends PJConverter {
        private PJConverter itemConverter;

        public ToArray(PJConverter itemConverter) {
            this.itemConverter = itemConverter;
        }

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            Item item;
            if (value instanceof ExternalObject && targetClass.isAssignableFrom(((ExternalObject)value).getObject().getClass())) {
                return ((ExternalObject)value).getObject();
            }
            Class<?> componentClass = targetClass.getComponentType();
            ArrayList<Object> list = new ArrayList<Object>(20);
            SequenceIterator iter = value.iterate();
            while ((item = iter.next()) != null) {
                Object obj = this.itemConverter.convert(item, componentClass, context);
                if (obj == null) continue;
                list.add(obj);
            }
            Object array = Array.newInstance(componentClass, list.size());
            for (int i = 0; i < list.size(); ++i) {
                Array.set(array, i, list.get(i));
            }
            return array;
        }
    }

    public static class ToCollection
    extends PJConverter {
        public static final ToCollection INSTANCE = new ToCollection();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            Item it;
            Collection<Object> list;
            if (targetClass.isAssignableFrom(ArrayList.class)) {
                list = new ArrayList(100);
            } else {
                try {
                    list = (Collection)targetClass.newInstance();
                } catch (InstantiationException e) {
                    XPathException de = new XPathException("Cannot instantiate collection class " + targetClass);
                    de.setXPathContext(context);
                    throw de;
                } catch (IllegalAccessException e) {
                    XPathException de = new XPathException("Cannot access collection class " + targetClass);
                    de.setXPathContext(context);
                    throw de;
                }
            }
            Configuration config = context.getConfiguration();
            SequenceIterator iter = value.iterate();
            while ((it = iter.next()) != null) {
                if (it instanceof AtomicValue) {
                    PJConverter pj = ToCollection.allocate(config, ((AtomicValue)it).getItemType(), 16384, Object.class);
                    list.add(pj.convert(it, Object.class, context));
                    continue;
                }
                if (it instanceof VirtualNode) {
                    list.add(((VirtualNode)it).getRealNode());
                    continue;
                }
                list.add(it);
            }
            return list;
        }
    }

    public static class ToSequenceExtent
    extends PJConverter {
        public static final ToSequenceExtent INSTANCE = new ToSequenceExtent();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            return value.iterate().materialize();
        }
    }

    public static class ToNull
    extends PJConverter {
        public static final ToNull INSTANCE = new ToNull();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            return null;
        }
    }

    public static class ToSequenceIterator
    extends PJConverter {
        public static final ToSequenceIterator INSTANCE = new ToSequenceIterator();

        @Override
        public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
            return value.iterate();
        }
    }
}


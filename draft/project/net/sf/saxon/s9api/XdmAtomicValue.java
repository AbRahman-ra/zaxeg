/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.type.StringToDouble;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DateValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.QualifiedNameValue;
import net.sf.saxon.value.StringToDouble11;
import net.sf.saxon.value.StringValue;

public class XdmAtomicValue
extends XdmItem {
    private XdmAtomicValue() {
    }

    protected XdmAtomicValue(AtomicValue value, boolean flag) {
        this.setValue(value);
    }

    public XdmAtomicValue(boolean value) {
        this(BooleanValue.get(value), true);
    }

    public XdmAtomicValue(long value) {
        this(Int64Value.makeDerived(value, BuiltInAtomicType.LONG), true);
    }

    public XdmAtomicValue(int value) {
        this(Int64Value.makeDerived(value, BuiltInAtomicType.INT), true);
    }

    public XdmAtomicValue(short value) {
        this(Int64Value.makeDerived(value, BuiltInAtomicType.SHORT), true);
    }

    public XdmAtomicValue(byte value) {
        this(Int64Value.makeDerived(value, BuiltInAtomicType.BYTE), true);
    }

    public XdmAtomicValue(BigDecimal value) {
        this(new BigDecimalValue(value), true);
    }

    public XdmAtomicValue(double value) {
        this(new DoubleValue(value), true);
    }

    public XdmAtomicValue(float value) {
        this(new FloatValue(value), true);
    }

    public XdmAtomicValue(String value) {
        this(new StringValue(value), true);
    }

    public XdmAtomicValue(URI value) {
        this(new AnyURIValue(value.toString()), true);
    }

    public XdmAtomicValue(QName value) {
        this(new QNameValue(value.getStructuredQName(), BuiltInAtomicType.QNAME), true);
    }

    public XdmAtomicValue(Instant value) {
        this(DateTimeValue.fromJavaInstant(value), true);
    }

    public XdmAtomicValue(ZonedDateTime value) {
        this(DateTimeValue.fromZonedDateTime(value), true);
    }

    public XdmAtomicValue(OffsetDateTime value) {
        this(DateTimeValue.fromOffsetDateTime(value), true);
    }

    public XdmAtomicValue(LocalDateTime value) {
        this(DateTimeValue.fromLocalDateTime(value), true);
    }

    public XdmAtomicValue(LocalDate value) {
        this(new DateValue(value), true);
    }

    public XdmAtomicValue(String lexicalForm, ItemType type) throws SaxonApiException {
        net.sf.saxon.type.ItemType it = type.getUnderlyingItemType();
        if (!it.isPlainType()) {
            throw new SaxonApiException("Requested type is not atomic");
        }
        if (((AtomicType)it).isAbstract()) {
            throw new SaxonApiException("Requested type is an abstract type");
        }
        if (((AtomicType)it).isNamespaceSensitive()) {
            throw new SaxonApiException("Requested type is namespace-sensitive");
        }
        try {
            StringConverter converter = ((AtomicType)it).getStringConverter(type.getConversionRules());
            this.setValue(converter.convertString(lexicalForm).asAtomic());
        } catch (ValidationException e) {
            throw new SaxonApiException(e);
        }
    }

    public static XdmAtomicValue makeAtomicValue(Object value) {
        if (value instanceof AtomicValue) {
            return new XdmAtomicValue((AtomicValue)value, true);
        }
        if (value instanceof Boolean) {
            return new XdmAtomicValue((Boolean)value);
        }
        if (value instanceof Integer) {
            return new XdmAtomicValue((Integer)value);
        }
        if (value instanceof Long) {
            return new XdmAtomicValue((Long)value);
        }
        if (value instanceof Short) {
            return new XdmAtomicValue((Short)value);
        }
        if (value instanceof Character) {
            return new XdmAtomicValue(((Character)value).charValue());
        }
        if (value instanceof Byte) {
            return new XdmAtomicValue((Byte)value);
        }
        if (value instanceof String) {
            return new XdmAtomicValue((String)value);
        }
        if (value instanceof Double) {
            return new XdmAtomicValue((Double)value);
        }
        if (value instanceof Float) {
            return new XdmAtomicValue(((Float)value).floatValue());
        }
        if (value instanceof BigDecimal) {
            return new XdmAtomicValue((BigDecimal)value);
        }
        if (value instanceof BigInteger) {
            return new XdmAtomicValue(IntegerValue.makeIntegerValue((BigInteger)value), true);
        }
        if (value instanceof URI) {
            return new XdmAtomicValue((URI)value);
        }
        if (value instanceof QName) {
            return new XdmAtomicValue((QName)value);
        }
        if (value instanceof ZonedDateTime) {
            return new XdmAtomicValue((ZonedDateTime)value);
        }
        if (value instanceof LocalDateTime) {
            return new XdmAtomicValue((LocalDateTime)value);
        }
        if (value instanceof LocalDate) {
            return new XdmAtomicValue((LocalDate)value);
        }
        if (value instanceof XdmAtomicValue) {
            return (XdmAtomicValue)value;
        }
        throw new IllegalArgumentException(value.toString());
    }

    @Override
    public AtomicValue getUnderlyingValue() {
        return (AtomicValue)super.getUnderlyingValue();
    }

    @Override
    public String toString() {
        return this.getStringValue();
    }

    public QName getPrimitiveTypeName() {
        AtomicValue value = this.getUnderlyingValue();
        BuiltInAtomicType type = value.getPrimitiveType();
        return new QName(type.getStructuredQName());
    }

    public QName getTypeName() {
        AtomicValue value = this.getUnderlyingValue();
        AtomicType type = value.getItemType();
        return new QName(type.getStructuredQName());
    }

    public Object getValue() {
        AtomicValue av = this.getUnderlyingValue();
        if (av instanceof StringValue) {
            return av.getStringValue();
        }
        if (av instanceof IntegerValue) {
            return ((IntegerValue)av).asBigInteger();
        }
        if (av instanceof DoubleValue) {
            return ((DoubleValue)av).getDoubleValue();
        }
        if (av instanceof FloatValue) {
            return Float.valueOf(((FloatValue)av).getFloatValue());
        }
        if (av instanceof BooleanValue) {
            return ((BooleanValue)av).getBooleanValue();
        }
        if (av instanceof BigDecimalValue) {
            return ((BigDecimalValue)av).getDecimalValue();
        }
        if (av instanceof DateTimeValue) {
            if (((DateTimeValue)av).hasTimezone()) {
                return ((DateTimeValue)av).toZonedDateTime();
            }
            return ((DateTimeValue)av).toLocalDateTime();
        }
        if (av instanceof DateValue) {
            return ((DateValue)av).toLocalDate();
        }
        if (av instanceof QNameValue) {
            QNameValue q = (QNameValue)av;
            return new QName(q.getPrefix(), q.getNamespaceURI(), q.getLocalName());
        }
        return av.getStringValue();
    }

    public boolean getBooleanValue() throws SaxonApiException {
        AtomicValue av = this.getUnderlyingValue();
        if (av instanceof BooleanValue) {
            return ((BooleanValue)av).getBooleanValue();
        }
        if (av instanceof NumericValue) {
            return !av.isNaN() && ((NumericValue)av).signum() != 0;
        }
        if (av instanceof StringValue) {
            String s = av.getStringValue().trim();
            return "1".equals(s) || "true".equals(s);
        }
        throw new SaxonApiException("Cannot cast item to a boolean");
    }

    public long getLongValue() throws SaxonApiException {
        AtomicValue av = this.getUnderlyingValue();
        if (av instanceof BooleanValue) {
            return ((BooleanValue)av).getBooleanValue() ? 0L : 1L;
        }
        if (av instanceof NumericValue) {
            try {
                return ((NumericValue)av).longValue();
            } catch (XPathException e) {
                throw new SaxonApiException("Cannot cast item to an integer");
            }
        }
        if (av instanceof StringValue) {
            StringToDouble converter = StringToDouble.getInstance();
            return (long)converter.stringToNumber(av.getStringValueCS());
        }
        throw new SaxonApiException("Cannot cast item to an integer");
    }

    public double getDoubleValue() throws SaxonApiException {
        AtomicValue av = this.getUnderlyingValue();
        if (av instanceof BooleanValue) {
            return ((BooleanValue)av).getBooleanValue() ? 0.0 : 1.0;
        }
        if (av instanceof NumericValue) {
            return ((NumericValue)av).getDoubleValue();
        }
        if (av instanceof StringValue) {
            try {
                StringToDouble11 converter = StringToDouble11.getInstance();
                return converter.stringToNumber(av.getStringValueCS());
            } catch (NumberFormatException e) {
                throw new SaxonApiException(e.getMessage());
            }
        }
        throw new SaxonApiException("Cannot cast item to a double");
    }

    public BigDecimal getDecimalValue() throws SaxonApiException {
        AtomicValue av = this.getUnderlyingValue();
        if (av instanceof BooleanValue) {
            return ((BooleanValue)av).getBooleanValue() ? BigDecimal.ZERO : BigDecimal.ONE;
        }
        if (av instanceof NumericValue) {
            try {
                return ((NumericValue)av).getDecimalValue();
            } catch (XPathException e) {
                throw new SaxonApiException("Cannot cast item to a decimal");
            }
        }
        if (av instanceof StringValue) {
            return new BigDecimal(av.getStringValueCS().toString());
        }
        throw new SaxonApiException("Cannot cast item to a decimal");
    }

    public QName getQNameValue() {
        AtomicValue av = this.getUnderlyingValue();
        if (av instanceof QualifiedNameValue) {
            return new QName(((QualifiedNameValue)av).getStructuredQName());
        }
        return null;
    }

    public Instant getInstant() {
        AtomicValue av = this.getUnderlyingValue();
        if (av instanceof DateTimeValue && ((DateTimeValue)av).hasTimezone()) {
            return ((DateTimeValue)av).toJavaInstant();
        }
        return null;
    }

    public ZonedDateTime getZonedDateTime() {
        AtomicValue av = this.getUnderlyingValue();
        if (av instanceof DateTimeValue && ((DateTimeValue)av).hasTimezone()) {
            return ((DateTimeValue)av).toZonedDateTime();
        }
        return null;
    }

    public OffsetDateTime getOffsetDateTime() {
        AtomicValue av = this.getUnderlyingValue();
        if (av instanceof DateTimeValue && ((DateTimeValue)av).hasTimezone()) {
            return ((DateTimeValue)av).toOffsetDateTime();
        }
        return null;
    }

    public LocalDateTime getLocalDateTime() {
        AtomicValue av = this.getUnderlyingValue();
        if (av instanceof DateTimeValue) {
            return ((DateTimeValue)av).toLocalDateTime();
        }
        return null;
    }

    public LocalDate getLocalDate() {
        AtomicValue av = this.getUnderlyingValue();
        if (av instanceof DateValue) {
            return ((DateValue)av).toLocalDate();
        }
        return null;
    }

    public boolean equals(Object other) {
        if (other instanceof XdmAtomicValue) {
            AtomicMatchKey a = this.getUnderlyingValue().asMapKey();
            AtomicMatchKey b = ((XdmAtomicValue)other).getUnderlyingValue().asMapKey();
            return a.equals(b);
        }
        return false;
    }

    public int hashCode() {
        return this.getUnderlyingValue().asMapKey().hashCode();
    }
}


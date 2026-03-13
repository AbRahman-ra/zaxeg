/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.NumberInput;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.LogicalType;
import com.fasterxml.jackson.databind.util.AccessPattern;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;

public class NumberDeserializers {
    private static final HashSet<String> _classNames;

    public static JsonDeserializer<?> find(Class<?> rawType, String clsName) {
        if (rawType.isPrimitive()) {
            if (rawType == Integer.TYPE) {
                return IntegerDeserializer.primitiveInstance;
            }
            if (rawType == Boolean.TYPE) {
                return BooleanDeserializer.primitiveInstance;
            }
            if (rawType == Long.TYPE) {
                return LongDeserializer.primitiveInstance;
            }
            if (rawType == Double.TYPE) {
                return DoubleDeserializer.primitiveInstance;
            }
            if (rawType == Character.TYPE) {
                return CharacterDeserializer.primitiveInstance;
            }
            if (rawType == Byte.TYPE) {
                return ByteDeserializer.primitiveInstance;
            }
            if (rawType == Short.TYPE) {
                return ShortDeserializer.primitiveInstance;
            }
            if (rawType == Float.TYPE) {
                return FloatDeserializer.primitiveInstance;
            }
            if (rawType == Void.TYPE) {
                return NullifyingDeserializer.instance;
            }
        } else if (_classNames.contains(clsName)) {
            if (rawType == Integer.class) {
                return IntegerDeserializer.wrapperInstance;
            }
            if (rawType == Boolean.class) {
                return BooleanDeserializer.wrapperInstance;
            }
            if (rawType == Long.class) {
                return LongDeserializer.wrapperInstance;
            }
            if (rawType == Double.class) {
                return DoubleDeserializer.wrapperInstance;
            }
            if (rawType == Character.class) {
                return CharacterDeserializer.wrapperInstance;
            }
            if (rawType == Byte.class) {
                return ByteDeserializer.wrapperInstance;
            }
            if (rawType == Short.class) {
                return ShortDeserializer.wrapperInstance;
            }
            if (rawType == Float.class) {
                return FloatDeserializer.wrapperInstance;
            }
            if (rawType == Number.class) {
                return NumberDeserializer.instance;
            }
            if (rawType == BigDecimal.class) {
                return BigDecimalDeserializer.instance;
            }
            if (rawType == BigInteger.class) {
                return BigIntegerDeserializer.instance;
            }
        } else {
            return null;
        }
        throw new IllegalArgumentException("Internal error: can't find deserializer for " + rawType.getName());
    }

    static {
        Class[] numberTypes;
        _classNames = new HashSet();
        for (Class cls : numberTypes = new Class[]{Boolean.class, Byte.class, Short.class, Character.class, Integer.class, Long.class, Float.class, Double.class, Number.class, BigDecimal.class, BigInteger.class}) {
            _classNames.add(cls.getName());
        }
    }

    @JacksonStdImpl
    public static class BigDecimalDeserializer
    extends StdScalarDeserializer<BigDecimal> {
        public static final BigDecimalDeserializer instance = new BigDecimalDeserializer();

        public BigDecimalDeserializer() {
            super(BigDecimal.class);
        }

        @Override
        public Object getEmptyValue(DeserializationContext ctxt) {
            return BigDecimal.ZERO;
        }

        @Override
        public final LogicalType logicalType() {
            return LogicalType.Float;
        }

        @Override
        public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text;
            switch (p.currentTokenId()) {
                case 7: 
                case 8: {
                    return p.getDecimalValue();
                }
                case 6: {
                    text = p.getText();
                    break;
                }
                case 1: {
                    text = ctxt.extractScalarFromObject(p, this, this._valueClass);
                    break;
                }
                case 3: {
                    return (BigDecimal)this._deserializeFromArray(p, ctxt);
                }
                default: {
                    return (BigDecimal)ctxt.handleUnexpectedToken(this.getValueType(ctxt), p);
                }
            }
            CoercionAction act = this._checkFromStringCoercion(ctxt, text);
            if (act == CoercionAction.AsNull) {
                return (BigDecimal)this.getNullValue(ctxt);
            }
            if (act == CoercionAction.AsEmpty) {
                return (BigDecimal)this.getEmptyValue(ctxt);
            }
            if (this._hasTextualNull(text = text.trim())) {
                return (BigDecimal)this.getNullValue(ctxt);
            }
            try {
                return new BigDecimal(text);
            } catch (IllegalArgumentException illegalArgumentException) {
                return (BigDecimal)ctxt.handleWeirdStringValue(this._valueClass, text, "not a valid representation", new Object[0]);
            }
        }
    }

    @JacksonStdImpl
    public static class BigIntegerDeserializer
    extends StdScalarDeserializer<BigInteger> {
        public static final BigIntegerDeserializer instance = new BigIntegerDeserializer();

        public BigIntegerDeserializer() {
            super(BigInteger.class);
        }

        @Override
        public Object getEmptyValue(DeserializationContext ctxt) {
            return BigInteger.ZERO;
        }

        @Override
        public final LogicalType logicalType() {
            return LogicalType.Integer;
        }

        @Override
        public BigInteger deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text;
            if (p.isExpectedNumberIntToken()) {
                return p.getBigIntegerValue();
            }
            switch (p.currentTokenId()) {
                case 6: {
                    text = p.getText();
                    break;
                }
                case 8: {
                    CoercionAction act = this._checkFloatToIntCoercion(p, ctxt, this._valueClass);
                    if (act == CoercionAction.AsNull) {
                        return (BigInteger)this.getNullValue(ctxt);
                    }
                    if (act == CoercionAction.AsEmpty) {
                        return (BigInteger)this.getEmptyValue(ctxt);
                    }
                    return p.getDecimalValue().toBigInteger();
                }
                case 1: {
                    text = ctxt.extractScalarFromObject(p, this, this._valueClass);
                    break;
                }
                case 3: {
                    return (BigInteger)this._deserializeFromArray(p, ctxt);
                }
                default: {
                    return (BigInteger)ctxt.handleUnexpectedToken(this.getValueType(ctxt), p);
                }
            }
            CoercionAction act = this._checkFromStringCoercion(ctxt, text);
            if (act == CoercionAction.AsNull) {
                return (BigInteger)this.getNullValue(ctxt);
            }
            if (act == CoercionAction.AsEmpty) {
                return (BigInteger)this.getEmptyValue(ctxt);
            }
            if (this._hasTextualNull(text = text.trim())) {
                return (BigInteger)this.getNullValue(ctxt);
            }
            try {
                return new BigInteger(text);
            } catch (IllegalArgumentException illegalArgumentException) {
                return (BigInteger)ctxt.handleWeirdStringValue(this._valueClass, text, "not a valid representation", new Object[0]);
            }
        }
    }

    @JacksonStdImpl
    public static class NumberDeserializer
    extends StdScalarDeserializer<Object> {
        public static final NumberDeserializer instance = new NumberDeserializer();

        public NumberDeserializer() {
            super(Number.class);
        }

        @Override
        public final LogicalType logicalType() {
            return LogicalType.Integer;
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text;
            switch (p.currentTokenId()) {
                case 6: {
                    text = p.getText();
                    break;
                }
                case 7: {
                    if (ctxt.hasSomeOfFeatures(F_MASK_INT_COERCIONS)) {
                        return this._coerceIntegral(p, ctxt);
                    }
                    return p.getNumberValue();
                }
                case 8: {
                    if (ctxt.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS) && !p.isNaN()) {
                        return p.getDecimalValue();
                    }
                    return p.getNumberValue();
                }
                case 1: {
                    text = ctxt.extractScalarFromObject(p, this, this._valueClass);
                    break;
                }
                case 3: {
                    return this._deserializeFromArray(p, ctxt);
                }
                default: {
                    return ctxt.handleUnexpectedToken(this.getValueType(ctxt), p);
                }
            }
            CoercionAction act = this._checkFromStringCoercion(ctxt, text);
            if (act == CoercionAction.AsNull) {
                return this.getNullValue(ctxt);
            }
            if (act == CoercionAction.AsEmpty) {
                return this.getEmptyValue(ctxt);
            }
            if (this._hasTextualNull(text = text.trim())) {
                return this.getNullValue(ctxt);
            }
            if (this._isPosInf(text)) {
                return Double.POSITIVE_INFINITY;
            }
            if (this._isNegInf(text)) {
                return Double.NEGATIVE_INFINITY;
            }
            if (this._isNaN(text)) {
                return Double.NaN;
            }
            try {
                if (!this._isIntNumber(text)) {
                    if (ctxt.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)) {
                        return new BigDecimal(text);
                    }
                    return Double.valueOf(text);
                }
                if (ctxt.isEnabled(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)) {
                    return new BigInteger(text);
                }
                long value = Long.parseLong(text);
                if (!ctxt.isEnabled(DeserializationFeature.USE_LONG_FOR_INTS) && value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE) {
                    return (int)value;
                }
                return value;
            } catch (IllegalArgumentException iae) {
                return ctxt.handleWeirdStringValue(this._valueClass, text, "not a valid number", new Object[0]);
            }
        }

        @Override
        public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
            switch (p.currentTokenId()) {
                case 6: 
                case 7: 
                case 8: {
                    return this.deserialize(p, ctxt);
                }
            }
            return typeDeserializer.deserializeTypedFromScalar(p, ctxt);
        }
    }

    @JacksonStdImpl
    public static class DoubleDeserializer
    extends PrimitiveOrWrapperDeserializer<Double> {
        private static final long serialVersionUID = 1L;
        static final DoubleDeserializer primitiveInstance = new DoubleDeserializer(Double.TYPE, 0.0);
        static final DoubleDeserializer wrapperInstance = new DoubleDeserializer(Double.class, null);

        public DoubleDeserializer(Class<Double> cls, Double nvl) {
            super(cls, LogicalType.Float, nvl, 0.0);
        }

        @Override
        public Double deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.hasToken(JsonToken.VALUE_NUMBER_FLOAT)) {
                return p.getDoubleValue();
            }
            if (this._primitive) {
                return this._parseDoublePrimitive(p, ctxt);
            }
            return this._parseDouble(p, ctxt);
        }

        @Override
        public Double deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
            if (p.hasToken(JsonToken.VALUE_NUMBER_FLOAT)) {
                return p.getDoubleValue();
            }
            if (this._primitive) {
                return this._parseDoublePrimitive(p, ctxt);
            }
            return this._parseDouble(p, ctxt);
        }

        protected final Double _parseDouble(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text;
            switch (p.currentTokenId()) {
                case 6: {
                    text = p.getText();
                    break;
                }
                case 11: {
                    return (Double)this.getNullValue(ctxt);
                }
                case 7: 
                case 8: {
                    return p.getDoubleValue();
                }
                case 1: {
                    text = ctxt.extractScalarFromObject(p, this, this._valueClass);
                    break;
                }
                case 3: {
                    return (Double)this._deserializeFromArray(p, ctxt);
                }
                default: {
                    return (Double)ctxt.handleUnexpectedToken(this.getValueType(ctxt), p);
                }
            }
            Double nan = this._checkDoubleSpecialValue(text);
            if (nan != null) {
                return nan;
            }
            CoercionAction act = this._checkFromStringCoercion(ctxt, text);
            if (act == CoercionAction.AsNull) {
                return (Double)this.getNullValue(ctxt);
            }
            if (act == CoercionAction.AsEmpty) {
                return (Double)this.getEmptyValue(ctxt);
            }
            if (this._checkTextualNull(ctxt, text = text.trim())) {
                return (Double)this.getNullValue(ctxt);
            }
            try {
                return DoubleDeserializer._parseDouble(text);
            } catch (IllegalArgumentException illegalArgumentException) {
                return (Double)ctxt.handleWeirdStringValue(this._valueClass, text, "not a valid `Double` value", new Object[0]);
            }
        }
    }

    @JacksonStdImpl
    public static class FloatDeserializer
    extends PrimitiveOrWrapperDeserializer<Float> {
        private static final long serialVersionUID = 1L;
        static final FloatDeserializer primitiveInstance = new FloatDeserializer(Float.TYPE, Float.valueOf(0.0f));
        static final FloatDeserializer wrapperInstance = new FloatDeserializer(Float.class, null);

        public FloatDeserializer(Class<Float> cls, Float nvl) {
            super(cls, LogicalType.Float, nvl, Float.valueOf(0.0f));
        }

        @Override
        public Float deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.hasToken(JsonToken.VALUE_NUMBER_FLOAT)) {
                return Float.valueOf(p.getFloatValue());
            }
            if (this._primitive) {
                return Float.valueOf(this._parseFloatPrimitive(p, ctxt));
            }
            return this._parseFloat(p, ctxt);
        }

        protected final Float _parseFloat(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text;
            switch (p.currentTokenId()) {
                case 6: {
                    text = p.getText();
                    break;
                }
                case 11: {
                    return (Float)this.getNullValue(ctxt);
                }
                case 7: 
                case 8: {
                    return Float.valueOf(p.getFloatValue());
                }
                case 1: {
                    text = ctxt.extractScalarFromObject(p, this, this._valueClass);
                    break;
                }
                case 3: {
                    return (Float)this._deserializeFromArray(p, ctxt);
                }
                default: {
                    return (Float)ctxt.handleUnexpectedToken(this.getValueType(ctxt), p);
                }
            }
            Float nan = this._checkFloatSpecialValue(text);
            if (nan != null) {
                return nan;
            }
            CoercionAction act = this._checkFromStringCoercion(ctxt, text);
            if (act == CoercionAction.AsNull) {
                return (Float)this.getNullValue(ctxt);
            }
            if (act == CoercionAction.AsEmpty) {
                return (Float)this.getEmptyValue(ctxt);
            }
            if (this._checkTextualNull(ctxt, text = text.trim())) {
                return (Float)this.getNullValue(ctxt);
            }
            try {
                return Float.valueOf(Float.parseFloat(text));
            } catch (IllegalArgumentException illegalArgumentException) {
                return (Float)ctxt.handleWeirdStringValue(this._valueClass, text, "not a valid `Float` value", new Object[0]);
            }
        }
    }

    @JacksonStdImpl
    public static final class LongDeserializer
    extends PrimitiveOrWrapperDeserializer<Long> {
        private static final long serialVersionUID = 1L;
        static final LongDeserializer primitiveInstance = new LongDeserializer(Long.TYPE, 0L);
        static final LongDeserializer wrapperInstance = new LongDeserializer(Long.class, null);

        public LongDeserializer(Class<Long> cls, Long nvl) {
            super(cls, LogicalType.Integer, nvl, 0L);
        }

        @Override
        public boolean isCachable() {
            return true;
        }

        @Override
        public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.isExpectedNumberIntToken()) {
                return p.getLongValue();
            }
            if (this._primitive) {
                return this._parseLongPrimitive(p, ctxt);
            }
            return this._parseLong(p, ctxt, Long.class);
        }
    }

    @JacksonStdImpl
    public static final class IntegerDeserializer
    extends PrimitiveOrWrapperDeserializer<Integer> {
        private static final long serialVersionUID = 1L;
        static final IntegerDeserializer primitiveInstance = new IntegerDeserializer(Integer.TYPE, 0);
        static final IntegerDeserializer wrapperInstance = new IntegerDeserializer(Integer.class, null);

        public IntegerDeserializer(Class<Integer> cls, Integer nvl) {
            super(cls, LogicalType.Integer, nvl, 0);
        }

        @Override
        public boolean isCachable() {
            return true;
        }

        @Override
        public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.isExpectedNumberIntToken()) {
                return p.getIntValue();
            }
            if (this._primitive) {
                return this._parseIntPrimitive(p, ctxt);
            }
            return this._parseInteger(p, ctxt, Integer.class);
        }

        @Override
        public Integer deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
            if (p.isExpectedNumberIntToken()) {
                return p.getIntValue();
            }
            if (this._primitive) {
                return this._parseIntPrimitive(p, ctxt);
            }
            return this._parseInteger(p, ctxt, Integer.class);
        }
    }

    @JacksonStdImpl
    public static class CharacterDeserializer
    extends PrimitiveOrWrapperDeserializer<Character> {
        private static final long serialVersionUID = 1L;
        static final CharacterDeserializer primitiveInstance = new CharacterDeserializer(Character.TYPE, Character.valueOf('\u0000'));
        static final CharacterDeserializer wrapperInstance = new CharacterDeserializer(Character.class, null);

        public CharacterDeserializer(Class<Character> cls, Character nvl) {
            super(cls, LogicalType.Integer, nvl, Character.valueOf('\u0000'));
        }

        @Override
        public Character deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text;
            switch (p.currentTokenId()) {
                case 6: {
                    text = p.getText();
                    break;
                }
                case 7: {
                    CoercionAction act = ctxt.findCoercionAction(this.logicalType(), this._valueClass, CoercionInputShape.Integer);
                    switch (act) {
                        case Fail: {
                            this._checkCoercionFail(ctxt, act, this._valueClass, p.getNumberValue(), "Integer value (" + p.getText() + ")");
                        }
                        case AsNull: {
                            return (Character)this.getNullValue(ctxt);
                        }
                        case AsEmpty: {
                            return (Character)this.getEmptyValue(ctxt);
                        }
                    }
                    int value = p.getIntValue();
                    if (value >= 0 && value <= 65535) {
                        return Character.valueOf((char)value);
                    }
                    return (Character)ctxt.handleWeirdNumberValue(this.handledType(), value, "value outside valid Character range (0x0000 - 0xFFFF)", new Object[0]);
                }
                case 11: {
                    if (this._primitive) {
                        this._verifyNullForPrimitive(ctxt);
                    }
                    return (Character)this.getNullValue(ctxt);
                }
                case 1: {
                    text = ctxt.extractScalarFromObject(p, this, this._valueClass);
                    break;
                }
                case 3: {
                    return (Character)this._deserializeFromArray(p, ctxt);
                }
                default: {
                    return (Character)ctxt.handleUnexpectedToken(this.getValueType(ctxt), p);
                }
            }
            if (text.length() == 1) {
                return Character.valueOf(text.charAt(0));
            }
            CoercionAction act = this._checkFromStringCoercion(ctxt, text);
            if (act == CoercionAction.AsNull) {
                return (Character)this.getNullValue(ctxt);
            }
            if (act == CoercionAction.AsEmpty) {
                return (Character)this.getEmptyValue(ctxt);
            }
            if (this._checkTextualNull(ctxt, text = text.trim())) {
                return (Character)this.getNullValue(ctxt);
            }
            return (Character)ctxt.handleWeirdStringValue(this.handledType(), text, "Expected either Integer value code or 1-character String", new Object[0]);
        }
    }

    @JacksonStdImpl
    public static class ShortDeserializer
    extends PrimitiveOrWrapperDeserializer<Short> {
        private static final long serialVersionUID = 1L;
        static final ShortDeserializer primitiveInstance = new ShortDeserializer(Short.TYPE, (short)0);
        static final ShortDeserializer wrapperInstance = new ShortDeserializer(Short.class, null);

        public ShortDeserializer(Class<Short> cls, Short nvl) {
            super(cls, LogicalType.Integer, nvl, (short)0);
        }

        @Override
        public Short deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.isExpectedNumberIntToken()) {
                return p.getShortValue();
            }
            if (this._primitive) {
                return this._parseShortPrimitive(p, ctxt);
            }
            return this._parseShort(p, ctxt);
        }

        protected Short _parseShort(JsonParser p, DeserializationContext ctxt) throws IOException {
            int value;
            String text;
            switch (p.currentTokenId()) {
                case 6: {
                    text = p.getText();
                    break;
                }
                case 8: {
                    CoercionAction act = this._checkFloatToIntCoercion(p, ctxt, this._valueClass);
                    if (act == CoercionAction.AsNull) {
                        return (Short)this.getNullValue(ctxt);
                    }
                    if (act == CoercionAction.AsEmpty) {
                        return (Short)this.getEmptyValue(ctxt);
                    }
                    return p.getShortValue();
                }
                case 11: {
                    return (Short)this.getNullValue(ctxt);
                }
                case 7: {
                    return p.getShortValue();
                }
                case 1: {
                    text = ctxt.extractScalarFromObject(p, this, this._valueClass);
                    break;
                }
                case 3: {
                    return (Short)this._deserializeFromArray(p, ctxt);
                }
                default: {
                    return (Short)ctxt.handleUnexpectedToken(this.getValueType(ctxt), p);
                }
            }
            CoercionAction act = this._checkFromStringCoercion(ctxt, text);
            if (act == CoercionAction.AsNull) {
                return (Short)this.getNullValue(ctxt);
            }
            if (act == CoercionAction.AsEmpty) {
                return (Short)this.getEmptyValue(ctxt);
            }
            if (this._checkTextualNull(ctxt, text = text.trim())) {
                return (Short)this.getNullValue(ctxt);
            }
            try {
                value = NumberInput.parseInt(text);
            } catch (IllegalArgumentException iae) {
                return (Short)ctxt.handleWeirdStringValue(this._valueClass, text, "not a valid Short value", new Object[0]);
            }
            if (this._shortOverflow(value)) {
                return (Short)ctxt.handleWeirdStringValue(this._valueClass, text, "overflow, value cannot be represented as 16-bit value", new Object[0]);
            }
            return (short)value;
        }
    }

    @JacksonStdImpl
    public static class ByteDeserializer
    extends PrimitiveOrWrapperDeserializer<Byte> {
        private static final long serialVersionUID = 1L;
        static final ByteDeserializer primitiveInstance = new ByteDeserializer(Byte.TYPE, (byte)0);
        static final ByteDeserializer wrapperInstance = new ByteDeserializer(Byte.class, null);

        public ByteDeserializer(Class<Byte> cls, Byte nvl) {
            super(cls, LogicalType.Integer, nvl, (byte)0);
        }

        @Override
        public Byte deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.isExpectedNumberIntToken()) {
                return p.getByteValue();
            }
            if (this._primitive) {
                return this._parseBytePrimitive(p, ctxt);
            }
            return this._parseByte(p, ctxt);
        }

        protected Byte _parseByte(JsonParser p, DeserializationContext ctxt) throws IOException {
            int value;
            String text;
            switch (p.currentTokenId()) {
                case 6: {
                    text = p.getText();
                    break;
                }
                case 8: {
                    CoercionAction act = this._checkFloatToIntCoercion(p, ctxt, this._valueClass);
                    if (act == CoercionAction.AsNull) {
                        return (Byte)this.getNullValue(ctxt);
                    }
                    if (act == CoercionAction.AsEmpty) {
                        return (Byte)this.getEmptyValue(ctxt);
                    }
                    return p.getByteValue();
                }
                case 11: {
                    return (Byte)this.getNullValue(ctxt);
                }
                case 7: {
                    return p.getByteValue();
                }
                case 3: {
                    return (Byte)this._deserializeFromArray(p, ctxt);
                }
                case 1: {
                    text = ctxt.extractScalarFromObject(p, this, this._valueClass);
                    break;
                }
                default: {
                    return (Byte)ctxt.handleUnexpectedToken(this.getValueType(ctxt), p);
                }
            }
            CoercionAction act = this._checkFromStringCoercion(ctxt, text);
            if (act == CoercionAction.AsNull) {
                return (Byte)this.getNullValue(ctxt);
            }
            if (act == CoercionAction.AsEmpty) {
                return (Byte)this.getEmptyValue(ctxt);
            }
            if (this._checkTextualNull(ctxt, text = text.trim())) {
                return (Byte)this.getNullValue(ctxt);
            }
            try {
                value = NumberInput.parseInt(text);
            } catch (IllegalArgumentException iae) {
                return (Byte)ctxt.handleWeirdStringValue(this._valueClass, text, "not a valid Byte value", new Object[0]);
            }
            if (this._byteOverflow(value)) {
                return (Byte)ctxt.handleWeirdStringValue(this._valueClass, text, "overflow, value cannot be represented as 8-bit value", new Object[0]);
            }
            return (byte)value;
        }
    }

    @JacksonStdImpl
    public static final class BooleanDeserializer
    extends PrimitiveOrWrapperDeserializer<Boolean> {
        private static final long serialVersionUID = 1L;
        static final BooleanDeserializer primitiveInstance = new BooleanDeserializer(Boolean.TYPE, Boolean.FALSE);
        static final BooleanDeserializer wrapperInstance = new BooleanDeserializer(Boolean.class, null);

        public BooleanDeserializer(Class<Boolean> cls, Boolean nvl) {
            super(cls, LogicalType.Boolean, nvl, Boolean.FALSE);
        }

        @Override
        public Boolean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonToken t = p.currentToken();
            if (t == JsonToken.VALUE_TRUE) {
                return Boolean.TRUE;
            }
            if (t == JsonToken.VALUE_FALSE) {
                return Boolean.FALSE;
            }
            if (this._primitive) {
                return this._parseBooleanPrimitive(p, ctxt);
            }
            return this._parseBoolean(p, ctxt, this._valueClass);
        }

        @Override
        public Boolean deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
            JsonToken t = p.currentToken();
            if (t == JsonToken.VALUE_TRUE) {
                return Boolean.TRUE;
            }
            if (t == JsonToken.VALUE_FALSE) {
                return Boolean.FALSE;
            }
            if (this._primitive) {
                return this._parseBooleanPrimitive(p, ctxt);
            }
            return this._parseBoolean(p, ctxt, this._valueClass);
        }
    }

    protected static abstract class PrimitiveOrWrapperDeserializer<T>
    extends StdScalarDeserializer<T> {
        private static final long serialVersionUID = 1L;
        protected final LogicalType _logicalType;
        protected final T _nullValue;
        protected final T _emptyValue;
        protected final boolean _primitive;

        protected PrimitiveOrWrapperDeserializer(Class<T> vc, LogicalType logicalType, T nvl, T empty) {
            super(vc);
            this._logicalType = logicalType;
            this._nullValue = nvl;
            this._emptyValue = empty;
            this._primitive = vc.isPrimitive();
        }

        @Deprecated
        protected PrimitiveOrWrapperDeserializer(Class<T> vc, T nvl, T empty) {
            this(vc, LogicalType.OtherScalar, nvl, empty);
        }

        @Override
        public AccessPattern getNullAccessPattern() {
            if (this._primitive) {
                return AccessPattern.DYNAMIC;
            }
            if (this._nullValue == null) {
                return AccessPattern.ALWAYS_NULL;
            }
            return AccessPattern.CONSTANT;
        }

        @Override
        public final T getNullValue(DeserializationContext ctxt) throws JsonMappingException {
            if (this._primitive && ctxt.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)) {
                ctxt.reportInputMismatch(this, "Cannot map `null` into type %s (set DeserializationConfig.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES to 'false' to allow)", ClassUtil.classNameOf(this.handledType()));
            }
            return this._nullValue;
        }

        @Override
        public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
            return this._emptyValue;
        }

        @Override
        public final LogicalType logicalType() {
            return this._logicalType;
        }
    }
}


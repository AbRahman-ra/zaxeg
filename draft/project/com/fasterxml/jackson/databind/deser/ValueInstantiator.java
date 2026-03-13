/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.PropertyValueBuffer;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import com.fasterxml.jackson.databind.type.LogicalType;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

public abstract class ValueInstantiator {
    public ValueInstantiator createContextual(DeserializationContext ctxt, BeanDescription beanDesc) throws JsonMappingException {
        return this;
    }

    public Class<?> getValueClass() {
        return Object.class;
    }

    public String getValueTypeDesc() {
        Class<?> cls = this.getValueClass();
        if (cls == null) {
            return "UNKNOWN";
        }
        return cls.getName();
    }

    public boolean canInstantiate() {
        return this.canCreateUsingDefault() || this.canCreateUsingDelegate() || this.canCreateUsingArrayDelegate() || this.canCreateFromObjectWith() || this.canCreateFromString() || this.canCreateFromInt() || this.canCreateFromLong() || this.canCreateFromDouble() || this.canCreateFromBoolean();
    }

    public boolean canCreateFromString() {
        return false;
    }

    public boolean canCreateFromInt() {
        return false;
    }

    public boolean canCreateFromLong() {
        return false;
    }

    public boolean canCreateFromBigInteger() {
        return false;
    }

    public boolean canCreateFromDouble() {
        return false;
    }

    public boolean canCreateFromBigDecimal() {
        return false;
    }

    public boolean canCreateFromBoolean() {
        return false;
    }

    public boolean canCreateUsingDefault() {
        return this.getDefaultCreator() != null;
    }

    public boolean canCreateUsingDelegate() {
        return false;
    }

    public boolean canCreateUsingArrayDelegate() {
        return false;
    }

    public boolean canCreateFromObjectWith() {
        return false;
    }

    public SettableBeanProperty[] getFromObjectArguments(DeserializationConfig config) {
        return null;
    }

    public JavaType getDelegateType(DeserializationConfig config) {
        return null;
    }

    public JavaType getArrayDelegateType(DeserializationConfig config) {
        return null;
    }

    public Object createUsingDefault(DeserializationContext ctxt) throws IOException {
        return ctxt.handleMissingInstantiator(this.getValueClass(), this, null, "no default no-arguments constructor found", new Object[0]);
    }

    public Object createFromObjectWith(DeserializationContext ctxt, Object[] args) throws IOException {
        return ctxt.handleMissingInstantiator(this.getValueClass(), this, null, "no creator with arguments specified", new Object[0]);
    }

    public Object createFromObjectWith(DeserializationContext ctxt, SettableBeanProperty[] props, PropertyValueBuffer buffer) throws IOException {
        return this.createFromObjectWith(ctxt, buffer.getParameters(props));
    }

    public Object createUsingDelegate(DeserializationContext ctxt, Object delegate) throws IOException {
        return ctxt.handleMissingInstantiator(this.getValueClass(), this, null, "no delegate creator specified", new Object[0]);
    }

    public Object createUsingArrayDelegate(DeserializationContext ctxt, Object delegate) throws IOException {
        return ctxt.handleMissingInstantiator(this.getValueClass(), this, null, "no array delegate creator specified", new Object[0]);
    }

    public Object createFromString(DeserializationContext ctxt, String value) throws IOException {
        return ctxt.handleMissingInstantiator(this.getValueClass(), this, ctxt.getParser(), "no String-argument constructor/factory method to deserialize from String value ('%s')", value);
    }

    public Object createFromInt(DeserializationContext ctxt, int value) throws IOException {
        return ctxt.handleMissingInstantiator(this.getValueClass(), this, null, "no int/Int-argument constructor/factory method to deserialize from Number value (%s)", value);
    }

    public Object createFromLong(DeserializationContext ctxt, long value) throws IOException {
        return ctxt.handleMissingInstantiator(this.getValueClass(), this, null, "no long/Long-argument constructor/factory method to deserialize from Number value (%s)", value);
    }

    public Object createFromBigInteger(DeserializationContext ctxt, BigInteger value) throws IOException {
        return ctxt.handleMissingInstantiator(this.getValueClass(), this, null, "no BigInteger-argument constructor/factory method to deserialize from Number value (%s)", value);
    }

    public Object createFromDouble(DeserializationContext ctxt, double value) throws IOException {
        return ctxt.handleMissingInstantiator(this.getValueClass(), this, null, "no double/Double-argument constructor/factory method to deserialize from Number value (%s)", value);
    }

    public Object createFromBigDecimal(DeserializationContext ctxt, BigDecimal value) throws IOException {
        return ctxt.handleMissingInstantiator(this.getValueClass(), this, null, "no BigDecimal/double/Double-argument constructor/factory method to deserialize from Number value (%s)", value);
    }

    public Object createFromBoolean(DeserializationContext ctxt, boolean value) throws IOException {
        return ctxt.handleMissingInstantiator(this.getValueClass(), this, null, "no boolean/Boolean-argument constructor/factory method to deserialize from boolean value (%s)", value);
    }

    public AnnotatedWithParams getDefaultCreator() {
        return null;
    }

    public AnnotatedWithParams getDelegateCreator() {
        return null;
    }

    public AnnotatedWithParams getArrayDelegateCreator() {
        return null;
    }

    public AnnotatedWithParams getWithArgsCreator() {
        return null;
    }

    @Deprecated
    protected Object _createFromStringFallbacks(DeserializationContext ctxt, String value) throws IOException {
        if (value.isEmpty() && ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)) {
            return null;
        }
        if (this.canCreateFromBoolean() && ctxt.findCoercionAction(LogicalType.Boolean, Boolean.class, CoercionInputShape.String) == CoercionAction.TryConvert) {
            String str = value.trim();
            if ("true".equals(str)) {
                return this.createFromBoolean(ctxt, true);
            }
            if ("false".equals(str)) {
                return this.createFromBoolean(ctxt, false);
            }
        }
        return ctxt.handleMissingInstantiator(this.getValueClass(), this, ctxt.getParser(), "no String-argument constructor/factory method to deserialize from String value ('%s')", value);
    }

    public static class Delegating
    extends ValueInstantiator
    implements Serializable {
        private static final long serialVersionUID = 1L;
        protected final ValueInstantiator _delegate;

        protected Delegating(ValueInstantiator delegate) {
            this._delegate = delegate;
        }

        @Override
        public ValueInstantiator createContextual(DeserializationContext ctxt, BeanDescription beanDesc) throws JsonMappingException {
            ValueInstantiator d = this._delegate.createContextual(ctxt, beanDesc);
            return d == this._delegate ? this : new Delegating(d);
        }

        protected ValueInstantiator delegate() {
            return this._delegate;
        }

        @Override
        public Class<?> getValueClass() {
            return this.delegate().getValueClass();
        }

        @Override
        public String getValueTypeDesc() {
            return this.delegate().getValueTypeDesc();
        }

        @Override
        public boolean canInstantiate() {
            return this.delegate().canInstantiate();
        }

        @Override
        public boolean canCreateFromString() {
            return this.delegate().canCreateFromString();
        }

        @Override
        public boolean canCreateFromInt() {
            return this.delegate().canCreateFromInt();
        }

        @Override
        public boolean canCreateFromLong() {
            return this.delegate().canCreateFromLong();
        }

        @Override
        public boolean canCreateFromDouble() {
            return this.delegate().canCreateFromDouble();
        }

        @Override
        public boolean canCreateFromBoolean() {
            return this.delegate().canCreateFromBoolean();
        }

        @Override
        public boolean canCreateUsingDefault() {
            return this.delegate().canCreateUsingDefault();
        }

        @Override
        public boolean canCreateUsingDelegate() {
            return this.delegate().canCreateUsingDelegate();
        }

        @Override
        public boolean canCreateUsingArrayDelegate() {
            return this.delegate().canCreateUsingArrayDelegate();
        }

        @Override
        public boolean canCreateFromObjectWith() {
            return this.delegate().canCreateFromObjectWith();
        }

        @Override
        public SettableBeanProperty[] getFromObjectArguments(DeserializationConfig config) {
            return this.delegate().getFromObjectArguments(config);
        }

        @Override
        public JavaType getDelegateType(DeserializationConfig config) {
            return this.delegate().getDelegateType(config);
        }

        @Override
        public JavaType getArrayDelegateType(DeserializationConfig config) {
            return this.delegate().getArrayDelegateType(config);
        }

        @Override
        public Object createUsingDefault(DeserializationContext ctxt) throws IOException {
            return this.delegate().createUsingDefault(ctxt);
        }

        @Override
        public Object createFromObjectWith(DeserializationContext ctxt, Object[] args) throws IOException {
            return this.delegate().createFromObjectWith(ctxt, args);
        }

        @Override
        public Object createFromObjectWith(DeserializationContext ctxt, SettableBeanProperty[] props, PropertyValueBuffer buffer) throws IOException {
            return this.delegate().createFromObjectWith(ctxt, props, buffer);
        }

        @Override
        public Object createUsingDelegate(DeserializationContext ctxt, Object delegate) throws IOException {
            return this.delegate().createUsingDelegate(ctxt, delegate);
        }

        @Override
        public Object createUsingArrayDelegate(DeserializationContext ctxt, Object delegate) throws IOException {
            return this.delegate().createUsingArrayDelegate(ctxt, delegate);
        }

        @Override
        public Object createFromString(DeserializationContext ctxt, String value) throws IOException {
            return this.delegate().createFromString(ctxt, value);
        }

        @Override
        public Object createFromInt(DeserializationContext ctxt, int value) throws IOException {
            return this.delegate().createFromInt(ctxt, value);
        }

        @Override
        public Object createFromLong(DeserializationContext ctxt, long value) throws IOException {
            return this.delegate().createFromLong(ctxt, value);
        }

        @Override
        public Object createFromBigInteger(DeserializationContext ctxt, BigInteger value) throws IOException {
            return this.delegate().createFromBigInteger(ctxt, value);
        }

        @Override
        public Object createFromDouble(DeserializationContext ctxt, double value) throws IOException {
            return this.delegate().createFromDouble(ctxt, value);
        }

        @Override
        public Object createFromBigDecimal(DeserializationContext ctxt, BigDecimal value) throws IOException {
            return this.delegate().createFromBigDecimal(ctxt, value);
        }

        @Override
        public Object createFromBoolean(DeserializationContext ctxt, boolean value) throws IOException {
            return this.delegate().createFromBoolean(ctxt, value);
        }

        @Override
        public AnnotatedWithParams getDefaultCreator() {
            return this.delegate().getDefaultCreator();
        }

        @Override
        public AnnotatedWithParams getDelegateCreator() {
            return this.delegate().getDelegateCreator();
        }

        @Override
        public AnnotatedWithParams getArrayDelegateCreator() {
            return this.delegate().getArrayDelegateCreator();
        }

        @Override
        public AnnotatedWithParams getWithArgsCreator() {
            return this.delegate().getWithArgsCreator();
        }
    }

    public static class Base
    extends ValueInstantiator
    implements Serializable {
        private static final long serialVersionUID = 1L;
        protected final Class<?> _valueType;

        public Base(Class<?> type) {
            this._valueType = type;
        }

        public Base(JavaType type) {
            this._valueType = type.getRawClass();
        }

        @Override
        public String getValueTypeDesc() {
            return this._valueType.getName();
        }

        @Override
        public Class<?> getValueClass() {
            return this._valueType;
        }
    }

    public static interface Gettable {
        public ValueInstantiator getValueInstantiator();
    }
}


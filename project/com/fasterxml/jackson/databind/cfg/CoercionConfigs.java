/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.cfg;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.cfg.MutableCoercionConfig;
import com.fasterxml.jackson.databind.type.LogicalType;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CoercionConfigs
implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int TARGET_TYPE_COUNT = LogicalType.values().length;
    protected CoercionAction _defaultAction;
    protected final MutableCoercionConfig _defaultCoercions;
    protected MutableCoercionConfig[] _perTypeCoercions;
    protected Map<Class<?>, MutableCoercionConfig> _perClassCoercions;

    public CoercionConfigs() {
        this(CoercionAction.TryConvert, new MutableCoercionConfig(), null, null);
    }

    protected CoercionConfigs(CoercionAction defaultAction, MutableCoercionConfig defaultCoercions, MutableCoercionConfig[] perTypeCoercions, Map<Class<?>, MutableCoercionConfig> perClassCoercions) {
        this._defaultCoercions = defaultCoercions;
        this._defaultAction = defaultAction;
        this._perTypeCoercions = perTypeCoercions;
        this._perClassCoercions = perClassCoercions;
    }

    public CoercionConfigs copy() {
        HashMap newPerClass;
        MutableCoercionConfig[] newPerType;
        if (this._perTypeCoercions == null) {
            newPerType = null;
        } else {
            int size = this._perTypeCoercions.length;
            newPerType = new MutableCoercionConfig[size];
            for (int i = 0; i < size; ++i) {
                newPerType[i] = CoercionConfigs._copy(this._perTypeCoercions[i]);
            }
        }
        if (this._perClassCoercions == null) {
            newPerClass = null;
        } else {
            newPerClass = new HashMap();
            for (Map.Entry<Class<?>, MutableCoercionConfig> entry : this._perClassCoercions.entrySet()) {
                newPerClass.put(entry.getKey(), entry.getValue().copy());
            }
        }
        return new CoercionConfigs(this._defaultAction, this._defaultCoercions.copy(), newPerType, newPerClass);
    }

    private static MutableCoercionConfig _copy(MutableCoercionConfig src) {
        if (src == null) {
            return null;
        }
        return src.copy();
    }

    public MutableCoercionConfig defaultCoercions() {
        return this._defaultCoercions;
    }

    public MutableCoercionConfig findOrCreateCoercion(LogicalType type) {
        MutableCoercionConfig config;
        if (this._perTypeCoercions == null) {
            this._perTypeCoercions = new MutableCoercionConfig[TARGET_TYPE_COUNT];
        }
        if ((config = this._perTypeCoercions[type.ordinal()]) == null) {
            this._perTypeCoercions[type.ordinal()] = config = new MutableCoercionConfig();
        }
        return config;
    }

    public MutableCoercionConfig findOrCreateCoercion(Class<?> type) {
        MutableCoercionConfig config;
        if (this._perClassCoercions == null) {
            this._perClassCoercions = new HashMap();
        }
        if ((config = this._perClassCoercions.get(type)) == null) {
            config = new MutableCoercionConfig();
            this._perClassCoercions.put(type, config);
        }
        return config;
    }

    public CoercionAction findCoercion(DeserializationConfig config, LogicalType targetType, Class<?> targetClass, CoercionInputShape inputShape) {
        boolean baseScalar;
        CoercionAction act;
        MutableCoercionConfig cc;
        if (this._perClassCoercions != null && targetClass != null && (cc = this._perClassCoercions.get(targetClass)) != null && (act = cc.findAction(inputShape)) != null) {
            return act;
        }
        if (this._perTypeCoercions != null && targetType != null && (cc = this._perTypeCoercions[targetType.ordinal()]) != null && (act = cc.findAction(inputShape)) != null) {
            return act;
        }
        CoercionAction act2 = this._defaultCoercions.findAction(inputShape);
        if (act2 != null) {
            return act2;
        }
        switch (inputShape) {
            case EmptyArray: {
                return config.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT) ? CoercionAction.AsNull : CoercionAction.Fail;
            }
            case Float: {
                if (targetType != LogicalType.Integer) break;
                return config.isEnabled(DeserializationFeature.ACCEPT_FLOAT_AS_INT) ? CoercionAction.TryConvert : CoercionAction.Fail;
            }
            case Integer: {
                if (targetType != LogicalType.Enum || !config.isEnabled(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)) break;
                return CoercionAction.Fail;
            }
        }
        boolean bl = baseScalar = targetType == LogicalType.Float || targetType == LogicalType.Integer || targetType == LogicalType.Boolean || targetType == LogicalType.DateTime;
        if (baseScalar && !config.isEnabled(MapperFeature.ALLOW_COERCION_OF_SCALARS)) {
            return CoercionAction.Fail;
        }
        if (inputShape == CoercionInputShape.EmptyString) {
            if (baseScalar || config.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)) {
                return CoercionAction.AsNull;
            }
            if (targetType == LogicalType.OtherScalar) {
                return CoercionAction.TryConvert;
            }
            return CoercionAction.Fail;
        }
        return this._defaultAction;
    }

    public CoercionAction findCoercionFromBlankString(DeserializationConfig config, LogicalType targetType, Class<?> targetClass, CoercionAction actionIfBlankNotAllowed) {
        MutableCoercionConfig cc;
        Boolean acceptBlankAsEmpty = null;
        CoercionAction action = null;
        if (this._perClassCoercions != null && targetClass != null && (cc = this._perClassCoercions.get(targetClass)) != null) {
            acceptBlankAsEmpty = cc.getAcceptBlankAsEmpty();
            action = cc.findAction(CoercionInputShape.EmptyString);
        }
        if (this._perTypeCoercions != null && targetType != null && (cc = this._perTypeCoercions[targetType.ordinal()]) != null) {
            if (acceptBlankAsEmpty == null) {
                acceptBlankAsEmpty = cc.getAcceptBlankAsEmpty();
            }
            if (action == null) {
                action = cc.findAction(CoercionInputShape.EmptyString);
            }
        }
        if (acceptBlankAsEmpty == null) {
            acceptBlankAsEmpty = this._defaultCoercions.getAcceptBlankAsEmpty();
        }
        if (action == null) {
            action = this._defaultCoercions.findAction(CoercionInputShape.EmptyString);
        }
        if (!Boolean.TRUE.equals(acceptBlankAsEmpty)) {
            return actionIfBlankNotAllowed;
        }
        if (action != null) {
            return action;
        }
        return config.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT) ? CoercionAction.AsNull : CoercionAction.Fail;
    }
}


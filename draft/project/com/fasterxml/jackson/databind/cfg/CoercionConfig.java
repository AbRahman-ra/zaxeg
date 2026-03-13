/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.cfg;

import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import java.io.Serializable;
import java.util.Arrays;

public class CoercionConfig
implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int INPUT_SHAPE_COUNT = CoercionInputShape.values().length;
    protected Boolean _acceptBlankAsEmpty;
    protected final CoercionAction[] _coercionsByShape;

    public CoercionConfig() {
        this._coercionsByShape = new CoercionAction[INPUT_SHAPE_COUNT];
        this._acceptBlankAsEmpty = false;
    }

    protected CoercionConfig(CoercionConfig src) {
        this._acceptBlankAsEmpty = src._acceptBlankAsEmpty;
        this._coercionsByShape = Arrays.copyOf(src._coercionsByShape, src._coercionsByShape.length);
    }

    public CoercionAction findAction(CoercionInputShape shape) {
        return this._coercionsByShape[shape.ordinal()];
    }

    public Boolean getAcceptBlankAsEmpty() {
        return this._acceptBlankAsEmpty;
    }
}


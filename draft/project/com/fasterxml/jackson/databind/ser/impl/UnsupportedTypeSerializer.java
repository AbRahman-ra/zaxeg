/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

public class UnsupportedTypeSerializer
extends StdSerializer<Object> {
    private static final long serialVersionUID = 1L;
    protected final JavaType _type;
    protected final String _message;

    public UnsupportedTypeSerializer(JavaType t, String msg) {
        super(Object.class);
        this._type = t;
        this._message = msg;
    }

    @Override
    public void serialize(Object value, JsonGenerator g, SerializerProvider ctxt) throws IOException {
        ctxt.reportBadDefinition(this._type, this._message);
    }
}


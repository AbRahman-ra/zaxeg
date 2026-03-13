/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.type.LogicalType;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public class AtomicLongDeserializer
extends StdScalarDeserializer<AtomicLong> {
    private static final long serialVersionUID = 1L;

    public AtomicLongDeserializer() {
        super(AtomicLong.class);
    }

    @Override
    public AtomicLong deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.isExpectedNumberIntToken()) {
            return new AtomicLong(p.getLongValue());
        }
        Long L = this._parseLong(p, ctxt, AtomicLong.class);
        return L == null ? null : new AtomicLong(L.intValue());
    }

    @Override
    public LogicalType logicalType() {
        return LogicalType.Integer;
    }

    @Override
    public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
        return new AtomicLong();
    }
}


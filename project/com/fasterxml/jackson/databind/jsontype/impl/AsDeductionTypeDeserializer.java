/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeDeserializer;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import java.io.IOException;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AsDeductionTypeDeserializer
extends AsPropertyTypeDeserializer {
    private static final long serialVersionUID = 1L;
    private static final BitSet EMPTY_CLASS_FINGERPRINT = new BitSet(0);
    private final Map<String, Integer> fieldBitIndex;
    private final Map<BitSet, String> subtypeFingerprints;

    public AsDeductionTypeDeserializer(JavaType bt, TypeIdResolver idRes, JavaType defaultImpl, DeserializationConfig config, Collection<NamedType> subtypes) {
        super(bt, idRes, null, false, defaultImpl, null);
        this.fieldBitIndex = new HashMap<String, Integer>();
        this.subtypeFingerprints = this.buildFingerprints(config, subtypes);
    }

    public AsDeductionTypeDeserializer(AsDeductionTypeDeserializer src, BeanProperty property) {
        super(src, property);
        this.fieldBitIndex = src.fieldBitIndex;
        this.subtypeFingerprints = src.subtypeFingerprints;
    }

    @Override
    public TypeDeserializer forProperty(BeanProperty prop) {
        return prop == this._property ? this : new AsDeductionTypeDeserializer(this, prop);
    }

    protected Map<BitSet, String> buildFingerprints(DeserializationConfig config, Collection<NamedType> subtypes) {
        boolean ignoreCase = config.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        int nextField = 0;
        HashMap<BitSet, String> fingerprints = new HashMap<BitSet, String>();
        for (NamedType subtype : subtypes) {
            JavaType subtyped = config.getTypeFactory().constructType(subtype.getType());
            List<BeanPropertyDefinition> properties = config.introspect(subtyped).findProperties();
            BitSet fingerprint = new BitSet(nextField + properties.size());
            for (BeanPropertyDefinition property : properties) {
                Integer bitIndex;
                String name = property.getName();
                if (ignoreCase) {
                    name = name.toLowerCase();
                }
                if ((bitIndex = this.fieldBitIndex.get(name)) == null) {
                    bitIndex = nextField;
                    this.fieldBitIndex.put(name, nextField++);
                }
                fingerprint.set(bitIndex);
            }
            String existingFingerprint = fingerprints.put(fingerprint, subtype.getType().getName());
            if (existingFingerprint == null) continue;
            throw new IllegalStateException(String.format("Subtypes %s and %s have the same signature and cannot be uniquely deduced.", existingFingerprint, subtype.getType().getName()));
        }
        return fingerprints;
    }

    @Override
    public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        String emptySubtype;
        JsonToken t = p.currentToken();
        if (t == JsonToken.START_OBJECT) {
            t = p.nextToken();
        } else if (t != JsonToken.FIELD_NAME) {
            return this._deserializeTypedUsingDefaultImpl(p, ctxt, null, "Unexpected input");
        }
        if (t == JsonToken.END_OBJECT && (emptySubtype = this.subtypeFingerprints.get(EMPTY_CLASS_FINGERPRINT)) != null) {
            return this._deserializeTypedForId(p, ctxt, null, emptySubtype);
        }
        LinkedList<BitSet> candidates = new LinkedList<BitSet>(this.subtypeFingerprints.keySet());
        TokenBuffer tb = new TokenBuffer(p, ctxt);
        boolean ignoreCase = ctxt.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        while (t == JsonToken.FIELD_NAME) {
            String name = p.currentName();
            if (ignoreCase) {
                name = name.toLowerCase();
            }
            tb.copyCurrentStructure(p);
            Integer bit = this.fieldBitIndex.get(name);
            if (bit != null) {
                AsDeductionTypeDeserializer.prune(candidates, bit);
                if (candidates.size() == 1) {
                    return this._deserializeTypedForId(p, ctxt, tb, this.subtypeFingerprints.get(candidates.get(0)));
                }
            }
            t = p.nextToken();
        }
        String msgToReportIfDefaultImplFailsToo = String.format("Cannot deduce unique subtype of %s (%d candidates match)", ClassUtil.getTypeDescription(this._baseType), candidates.size());
        return this._deserializeTypedUsingDefaultImpl(p, ctxt, tb, msgToReportIfDefaultImplFailsToo);
    }

    private static void prune(List<BitSet> candidates, int bit) {
        Iterator<BitSet> iter = candidates.iterator();
        while (iter.hasNext()) {
            if (iter.next().get(bit)) continue;
            iter.remove();
        }
    }
}


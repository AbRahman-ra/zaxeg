/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import java.util.HashMap;
import java.util.Map;
import net.sf.saxon.type.AnySimpleType;
import net.sf.saxon.type.AnyType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.BuiltInListType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.z.IntHashMap;

public abstract class BuiltInType {
    private static IntHashMap<SchemaType> lookup = new IntHashMap(100);
    private static Map<String, SchemaType> lookupByLocalName = new HashMap<String, SchemaType>(100);

    private BuiltInType() {
    }

    public static SchemaType getSchemaType(int fingerprint) {
        SchemaType st = lookup.get(fingerprint);
        if (st == null) {
            if (BuiltInAtomicType.DOUBLE == null || BuiltInListType.NMTOKENS == null) {
                // empty if block
            }
            st = lookup.get(fingerprint);
        }
        return st;
    }

    public static SchemaType getSchemaTypeByLocalName(String name) {
        SchemaType st = lookupByLocalName.get(name);
        if (st == null) {
            if (BuiltInAtomicType.DOUBLE == null || BuiltInListType.NMTOKENS == null) {
                // empty if block
            }
            st = lookupByLocalName.get(name);
        }
        return st;
    }

    static void register(int fingerprint, SchemaType type) {
        lookup.put(fingerprint, type);
        lookupByLocalName.put(type.getName(), type);
    }

    static {
        BuiltInType.register(573, AnySimpleType.getInstance());
        BuiltInType.register(572, AnyType.getInstance());
        BuiltInType.register(630, Untyped.getInstance());
        BuiltInType.register(575, ErrorType.getInstance());
    }
}


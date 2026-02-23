/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class JavaUtilCollectionsDeserializers {
    private static final int TYPE_SINGLETON_SET = 1;
    private static final int TYPE_SINGLETON_LIST = 2;
    private static final int TYPE_SINGLETON_MAP = 3;
    private static final int TYPE_UNMODIFIABLE_SET = 4;
    private static final int TYPE_UNMODIFIABLE_LIST = 5;
    private static final int TYPE_UNMODIFIABLE_MAP = 6;
    private static final int TYPE_SYNC_SET = 7;
    private static final int TYPE_SYNC_COLLECTION = 8;
    private static final int TYPE_SYNC_LIST = 9;
    private static final int TYPE_SYNC_MAP = 10;
    public static final int TYPE_AS_LIST = 11;
    private static final String PREFIX_JAVA_UTIL_COLLECTIONS = "java.util.Collections$";
    private static final Class<?> CLASS_AS_ARRAYS_LIST = Arrays.asList(null, null).getClass();
    private static final Class<?> CLASS_SINGLETON_SET;
    private static final Class<?> CLASS_SINGLETON_LIST;
    private static final Class<?> CLASS_SINGLETON_MAP;
    private static final Class<?> CLASS_UNMODIFIABLE_SET;
    private static final Class<?> CLASS_UNMODIFIABLE_LIST;
    private static final Class<?> CLASS_UNMODIFIABLE_LIST_ALIAS;
    private static final Class<?> CLASS_UNMODIFIABLE_MAP;

    public static JsonDeserializer<?> findForCollection(DeserializationContext ctxt, JavaType type) throws JsonMappingException {
        JavaUtilCollectionsConverter conv;
        if (type.hasRawClass(CLASS_AS_ARRAYS_LIST)) {
            conv = JavaUtilCollectionsDeserializers.converter(11, type, List.class);
        } else if (type.hasRawClass(CLASS_SINGLETON_LIST)) {
            conv = JavaUtilCollectionsDeserializers.converter(2, type, List.class);
        } else if (type.hasRawClass(CLASS_SINGLETON_SET)) {
            conv = JavaUtilCollectionsDeserializers.converter(1, type, Set.class);
        } else if (type.hasRawClass(CLASS_UNMODIFIABLE_LIST) || type.hasRawClass(CLASS_UNMODIFIABLE_LIST_ALIAS)) {
            conv = JavaUtilCollectionsDeserializers.converter(5, type, List.class);
        } else if (type.hasRawClass(CLASS_UNMODIFIABLE_SET)) {
            conv = JavaUtilCollectionsDeserializers.converter(4, type, Set.class);
        } else {
            String utilName = JavaUtilCollectionsDeserializers._findUtilSyncTypeName(type.getRawClass());
            if (utilName.endsWith("Set")) {
                conv = JavaUtilCollectionsDeserializers.converter(7, type, Set.class);
            } else if (utilName.endsWith("List")) {
                conv = JavaUtilCollectionsDeserializers.converter(9, type, List.class);
            } else if (utilName.endsWith("Collection")) {
                conv = JavaUtilCollectionsDeserializers.converter(8, type, Collection.class);
            } else {
                return null;
            }
        }
        return new StdDelegatingDeserializer<Object>(conv);
    }

    public static JsonDeserializer<?> findForMap(DeserializationContext ctxt, JavaType type) throws JsonMappingException {
        JavaUtilCollectionsConverter conv;
        if (type.hasRawClass(CLASS_SINGLETON_MAP)) {
            conv = JavaUtilCollectionsDeserializers.converter(3, type, Map.class);
        } else if (type.hasRawClass(CLASS_UNMODIFIABLE_MAP)) {
            conv = JavaUtilCollectionsDeserializers.converter(6, type, Map.class);
        } else {
            String utilName = JavaUtilCollectionsDeserializers._findUtilSyncTypeName(type.getRawClass());
            if (utilName.endsWith("Map")) {
                conv = JavaUtilCollectionsDeserializers.converter(10, type, Map.class);
            } else {
                return null;
            }
        }
        return new StdDelegatingDeserializer<Object>(conv);
    }

    static JavaUtilCollectionsConverter converter(int kind, JavaType concreteType, Class<?> rawSuper) {
        return new JavaUtilCollectionsConverter(kind, concreteType.findSuperType(rawSuper));
    }

    private static String _findUtilSyncTypeName(Class<?> raw) {
        String clsName = JavaUtilCollectionsDeserializers._findUtilCollectionsTypeName(raw);
        if (clsName != null && clsName.startsWith("Synchronized")) {
            return clsName.substring(12);
        }
        return "";
    }

    private static String _findUtilCollectionsTypeName(Class<?> raw) {
        String clsName = raw.getName();
        if (clsName.startsWith(PREFIX_JAVA_UTIL_COLLECTIONS)) {
            return clsName.substring(PREFIX_JAVA_UTIL_COLLECTIONS.length());
        }
        return "";
    }

    static {
        Set<Boolean> set = Collections.singleton(Boolean.TRUE);
        CLASS_SINGLETON_SET = set.getClass();
        CLASS_UNMODIFIABLE_SET = Collections.unmodifiableSet(set).getClass();
        List<Boolean> list = Collections.singletonList(Boolean.TRUE);
        CLASS_SINGLETON_LIST = list.getClass();
        CLASS_UNMODIFIABLE_LIST = Collections.unmodifiableList(list).getClass();
        CLASS_UNMODIFIABLE_LIST_ALIAS = Collections.unmodifiableList(new LinkedList()).getClass();
        Map<String, String> map = Collections.singletonMap("a", "b");
        CLASS_SINGLETON_MAP = map.getClass();
        CLASS_UNMODIFIABLE_MAP = Collections.unmodifiableMap(map).getClass();
    }

    private static class JavaUtilCollectionsConverter
    implements Converter<Object, Object> {
        private final JavaType _inputType;
        private final int _kind;

        JavaUtilCollectionsConverter(int kind, JavaType inputType) {
            this._inputType = inputType;
            this._kind = kind;
        }

        @Override
        public Object convert(Object value) {
            if (value == null) {
                return null;
            }
            switch (this._kind) {
                case 1: {
                    Set set = (Set)value;
                    this._checkSingleton(set.size());
                    return Collections.singleton(set.iterator().next());
                }
                case 2: {
                    List list = (List)value;
                    this._checkSingleton(list.size());
                    return Collections.singletonList(list.get(0));
                }
                case 3: {
                    Map map = (Map)value;
                    this._checkSingleton(map.size());
                    Map.Entry entry = map.entrySet().iterator().next();
                    return Collections.singletonMap(entry.getKey(), entry.getValue());
                }
                case 4: {
                    return Collections.unmodifiableSet((Set)value);
                }
                case 5: {
                    return Collections.unmodifiableList((List)value);
                }
                case 6: {
                    return Collections.unmodifiableMap((Map)value);
                }
                case 7: {
                    return Collections.synchronizedSet((Set)value);
                }
                case 9: {
                    return Collections.synchronizedList((List)value);
                }
                case 8: {
                    return Collections.synchronizedCollection((Collection)value);
                }
                case 10: {
                    return Collections.synchronizedMap((Map)value);
                }
            }
            return value;
        }

        @Override
        public JavaType getInputType(TypeFactory typeFactory) {
            return this._inputType;
        }

        @Override
        public JavaType getOutputType(TypeFactory typeFactory) {
            return this._inputType;
        }

        private void _checkSingleton(int size) {
            if (size != 1) {
                throw new IllegalArgumentException("Can not deserialize Singleton container from " + size + " entries");
            }
        }
    }
}


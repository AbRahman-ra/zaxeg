/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AccessorNamingStrategy;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.introspect.DefaultAccessorNamingStrategy;
import com.fasterxml.jackson.databind.introspect.POJOPropertiesCollector;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public class BasicClassIntrospector
extends ClassIntrospector
implements Serializable {
    private static final long serialVersionUID = 2L;
    private static final Class<?> CLS_OBJECT = Object.class;
    private static final Class<?> CLS_STRING = String.class;
    private static final Class<?> CLS_JSON_NODE = JsonNode.class;
    protected static final BasicBeanDescription STRING_DESC = BasicBeanDescription.forOtherUse(null, SimpleType.constructUnsafe(String.class), AnnotatedClassResolver.createPrimordial(CLS_STRING));
    protected static final BasicBeanDescription BOOLEAN_DESC = BasicBeanDescription.forOtherUse(null, SimpleType.constructUnsafe(Boolean.TYPE), AnnotatedClassResolver.createPrimordial(Boolean.TYPE));
    protected static final BasicBeanDescription INT_DESC = BasicBeanDescription.forOtherUse(null, SimpleType.constructUnsafe(Integer.TYPE), AnnotatedClassResolver.createPrimordial(Integer.TYPE));
    protected static final BasicBeanDescription LONG_DESC = BasicBeanDescription.forOtherUse(null, SimpleType.constructUnsafe(Long.TYPE), AnnotatedClassResolver.createPrimordial(Long.TYPE));
    protected static final BasicBeanDescription OBJECT_DESC = BasicBeanDescription.forOtherUse(null, SimpleType.constructUnsafe(Object.class), AnnotatedClassResolver.createPrimordial(CLS_OBJECT));

    @Override
    public ClassIntrospector copy() {
        return new BasicClassIntrospector();
    }

    @Override
    public BasicBeanDescription forSerialization(SerializationConfig config, JavaType type, ClassIntrospector.MixInResolver r) {
        BasicBeanDescription desc = this._findStdTypeDesc(config, type);
        if (desc == null && (desc = this._findStdJdkCollectionDesc(config, type)) == null) {
            desc = BasicBeanDescription.forSerialization(this.collectProperties(config, type, r, true));
        }
        return desc;
    }

    @Override
    public BasicBeanDescription forDeserialization(DeserializationConfig config, JavaType type, ClassIntrospector.MixInResolver r) {
        BasicBeanDescription desc = this._findStdTypeDesc(config, type);
        if (desc == null && (desc = this._findStdJdkCollectionDesc(config, type)) == null) {
            desc = BasicBeanDescription.forDeserialization(this.collectProperties(config, type, r, false));
        }
        return desc;
    }

    @Override
    public BasicBeanDescription forDeserializationWithBuilder(DeserializationConfig config, JavaType builderType, ClassIntrospector.MixInResolver r, BeanDescription valueTypeDesc) {
        return BasicBeanDescription.forDeserialization(this.collectPropertiesWithBuilder(config, builderType, r, valueTypeDesc, false));
    }

    @Override
    @Deprecated
    public BasicBeanDescription forDeserializationWithBuilder(DeserializationConfig config, JavaType type, ClassIntrospector.MixInResolver r) {
        return BasicBeanDescription.forDeserialization(this.collectPropertiesWithBuilder(config, type, r, null, false));
    }

    @Override
    public BasicBeanDescription forCreation(DeserializationConfig config, JavaType type, ClassIntrospector.MixInResolver r) {
        BasicBeanDescription desc = this._findStdTypeDesc(config, type);
        if (desc == null && (desc = this._findStdJdkCollectionDesc(config, type)) == null) {
            desc = BasicBeanDescription.forDeserialization(this.collectProperties(config, type, r, false));
        }
        return desc;
    }

    @Override
    public BasicBeanDescription forClassAnnotations(MapperConfig<?> config, JavaType type, ClassIntrospector.MixInResolver r) {
        BasicBeanDescription desc = this._findStdTypeDesc(config, type);
        if (desc == null) {
            desc = BasicBeanDescription.forOtherUse(config, type, this._resolveAnnotatedClass(config, type, r));
        }
        return desc;
    }

    @Override
    public BasicBeanDescription forDirectClassAnnotations(MapperConfig<?> config, JavaType type, ClassIntrospector.MixInResolver r) {
        BasicBeanDescription desc = this._findStdTypeDesc(config, type);
        if (desc == null) {
            desc = BasicBeanDescription.forOtherUse(config, type, this._resolveAnnotatedWithoutSuperTypes(config, type, r));
        }
        return desc;
    }

    protected POJOPropertiesCollector collectProperties(MapperConfig<?> config, JavaType type, ClassIntrospector.MixInResolver r, boolean forSerialization) {
        AnnotatedClass classDef = this._resolveAnnotatedClass(config, type, r);
        AccessorNamingStrategy accNaming = type.isRecordType() ? config.getAccessorNaming().forRecord(config, classDef) : config.getAccessorNaming().forPOJO(config, classDef);
        return this.constructPropertyCollector(config, classDef, type, forSerialization, accNaming);
    }

    @Deprecated
    protected POJOPropertiesCollector collectProperties(MapperConfig<?> config, JavaType type, ClassIntrospector.MixInResolver r, boolean forSerialization, String mutatorPrefix) {
        AnnotatedClass classDef = this._resolveAnnotatedClass(config, type, r);
        AccessorNamingStrategy accNaming = new DefaultAccessorNamingStrategy.Provider().withSetterPrefix(mutatorPrefix).forPOJO(config, classDef);
        return this.constructPropertyCollector(config, classDef, type, forSerialization, accNaming);
    }

    protected POJOPropertiesCollector collectPropertiesWithBuilder(MapperConfig<?> config, JavaType type, ClassIntrospector.MixInResolver r, BeanDescription valueTypeDesc, boolean forSerialization) {
        AnnotatedClass builderClassDef = this._resolveAnnotatedClass(config, type, r);
        AccessorNamingStrategy accNaming = config.getAccessorNaming().forBuilder(config, builderClassDef, valueTypeDesc);
        return this.constructPropertyCollector(config, builderClassDef, type, forSerialization, accNaming);
    }

    @Deprecated
    protected POJOPropertiesCollector collectPropertiesWithBuilder(MapperConfig<?> config, JavaType type, ClassIntrospector.MixInResolver r, boolean forSerialization) {
        return this.collectPropertiesWithBuilder(config, type, r, null, forSerialization);
    }

    protected POJOPropertiesCollector constructPropertyCollector(MapperConfig<?> config, AnnotatedClass classDef, JavaType type, boolean forSerialization, AccessorNamingStrategy accNaming) {
        return new POJOPropertiesCollector(config, forSerialization, type, classDef, accNaming);
    }

    @Deprecated
    protected POJOPropertiesCollector constructPropertyCollector(MapperConfig<?> config, AnnotatedClass ac, JavaType type, boolean forSerialization, String mutatorPrefix) {
        return new POJOPropertiesCollector(config, forSerialization, type, ac, mutatorPrefix);
    }

    protected BasicBeanDescription _findStdTypeDesc(MapperConfig<?> config, JavaType type) {
        Class<?> cls = type.getRawClass();
        if (cls.isPrimitive()) {
            if (cls == Integer.TYPE) {
                return INT_DESC;
            }
            if (cls == Long.TYPE) {
                return LONG_DESC;
            }
            if (cls == Boolean.TYPE) {
                return BOOLEAN_DESC;
            }
        } else if (ClassUtil.isJDKClass(cls)) {
            if (cls == CLS_OBJECT) {
                return OBJECT_DESC;
            }
            if (cls == CLS_STRING) {
                return STRING_DESC;
            }
            if (cls == Integer.class) {
                return INT_DESC;
            }
            if (cls == Long.class) {
                return LONG_DESC;
            }
            if (cls == Boolean.class) {
                return BOOLEAN_DESC;
            }
        } else if (CLS_JSON_NODE.isAssignableFrom(cls)) {
            return BasicBeanDescription.forOtherUse(config, type, AnnotatedClassResolver.createPrimordial(cls));
        }
        return null;
    }

    protected boolean _isStdJDKCollection(JavaType type) {
        if (!type.isContainerType() || type.isArrayType()) {
            return false;
        }
        Class<?> raw = type.getRawClass();
        return ClassUtil.isJDKClass(raw) && (Collection.class.isAssignableFrom(raw) || Map.class.isAssignableFrom(raw));
    }

    protected BasicBeanDescription _findStdJdkCollectionDesc(MapperConfig<?> cfg, JavaType type) {
        if (this._isStdJDKCollection(type)) {
            return BasicBeanDescription.forOtherUse(cfg, type, this._resolveAnnotatedClass(cfg, type, cfg));
        }
        return null;
    }

    protected AnnotatedClass _resolveAnnotatedClass(MapperConfig<?> config, JavaType type, ClassIntrospector.MixInResolver r) {
        return AnnotatedClassResolver.resolve(config, type, r);
    }

    protected AnnotatedClass _resolveAnnotatedWithoutSuperTypes(MapperConfig<?> config, JavaType type, ClassIntrospector.MixInResolver r) {
        return AnnotatedClassResolver.resolveWithoutSuperTypes(config, type, r);
    }
}


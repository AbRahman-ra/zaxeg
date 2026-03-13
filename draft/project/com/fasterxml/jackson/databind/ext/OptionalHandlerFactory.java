/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ext;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.ext.Java7Handlers;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class OptionalHandlerFactory
implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String PACKAGE_PREFIX_JAVAX_XML = "javax.xml.";
    private static final String SERIALIZERS_FOR_JAVAX_XML = "com.fasterxml.jackson.databind.ext.CoreXMLSerializers";
    private static final String DESERIALIZERS_FOR_JAVAX_XML = "com.fasterxml.jackson.databind.ext.CoreXMLDeserializers";
    private static final String SERIALIZER_FOR_DOM_NODE = "com.fasterxml.jackson.databind.ext.DOMSerializer";
    private static final String DESERIALIZER_FOR_DOM_DOCUMENT = "com.fasterxml.jackson.databind.ext.DOMDeserializer$DocumentDeserializer";
    private static final String DESERIALIZER_FOR_DOM_NODE = "com.fasterxml.jackson.databind.ext.DOMDeserializer$NodeDeserializer";
    private static final Class<?> CLASS_DOM_NODE;
    private static final Class<?> CLASS_DOM_DOCUMENT;
    private static final Java7Handlers _jdk7Helper;
    public static final OptionalHandlerFactory instance;
    private final Map<String, String> _sqlDeserializers = new HashMap<String, String>();
    private final Map<String, Object> _sqlSerializers;
    private static final String CLS_NAME_JAVA_SQL_TIMESTAMP = "java.sql.Timestamp";
    private static final String CLS_NAME_JAVA_SQL_DATE = "java.sql.Date";
    private static final String CLS_NAME_JAVA_SQL_TIME = "java.sql.Time";
    private static final String CLS_NAME_JAVA_SQL_BLOB = "java.sql.Blob";
    private static final String CLS_NAME_JAVA_SQL_SERIALBLOB = "javax.sql.rowset.serial.SerialBlob";

    protected OptionalHandlerFactory() {
        this._sqlDeserializers.put(CLS_NAME_JAVA_SQL_DATE, "com.fasterxml.jackson.databind.deser.std.DateDeserializers$SqlDateDeserializer");
        this._sqlDeserializers.put(CLS_NAME_JAVA_SQL_TIMESTAMP, "com.fasterxml.jackson.databind.deser.std.DateDeserializers$TimestampDeserializer");
        this._sqlSerializers = new HashMap<String, Object>();
        this._sqlSerializers.put(CLS_NAME_JAVA_SQL_TIMESTAMP, DateSerializer.instance);
        this._sqlSerializers.put(CLS_NAME_JAVA_SQL_DATE, "com.fasterxml.jackson.databind.ser.std.SqlDateSerializer");
        this._sqlSerializers.put(CLS_NAME_JAVA_SQL_TIME, "com.fasterxml.jackson.databind.ser.std.SqlTimeSerializer");
        this._sqlSerializers.put(CLS_NAME_JAVA_SQL_BLOB, "com.fasterxml.jackson.databind.ext.SqlBlobSerializer");
        this._sqlSerializers.put(CLS_NAME_JAVA_SQL_SERIALBLOB, "com.fasterxml.jackson.databind.ext.SqlBlobSerializer");
    }

    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
        JsonSerializer<?> ser;
        Class<?> rawType = type.getRawClass();
        if (this._IsXOfY(rawType, CLASS_DOM_NODE)) {
            return (JsonSerializer)this.instantiate(SERIALIZER_FOR_DOM_NODE, type);
        }
        if (_jdk7Helper != null && (ser = _jdk7Helper.getSerializerForJavaNioFilePath(rawType)) != null) {
            return ser;
        }
        String className = rawType.getName();
        Object sqlHandler = this._sqlSerializers.get(className);
        if (sqlHandler != null) {
            if (sqlHandler instanceof JsonSerializer) {
                return (JsonSerializer)sqlHandler;
            }
            return (JsonSerializer)this.instantiate((String)sqlHandler, type);
        }
        if (!className.startsWith(PACKAGE_PREFIX_JAVAX_XML) && !this.hasSuperClassStartingWith(rawType, PACKAGE_PREFIX_JAVAX_XML)) {
            return null;
        }
        String factoryName = SERIALIZERS_FOR_JAVAX_XML;
        Object ob = this.instantiate(factoryName, type);
        if (ob == null) {
            return null;
        }
        return ((Serializers)ob).findSerializer(config, type, beanDesc);
    }

    public JsonDeserializer<?> findDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
        JsonDeserializer<?> deser;
        Class<?> rawType = type.getRawClass();
        if (_jdk7Helper != null && (deser = _jdk7Helper.getDeserializerForJavaNioFilePath(rawType)) != null) {
            return deser;
        }
        if (this._IsXOfY(rawType, CLASS_DOM_NODE)) {
            return (JsonDeserializer)this.instantiate(DESERIALIZER_FOR_DOM_NODE, type);
        }
        if (this._IsXOfY(rawType, CLASS_DOM_DOCUMENT)) {
            return (JsonDeserializer)this.instantiate(DESERIALIZER_FOR_DOM_DOCUMENT, type);
        }
        String className = rawType.getName();
        String deserName = this._sqlDeserializers.get(className);
        if (deserName != null) {
            return (JsonDeserializer)this.instantiate(deserName, type);
        }
        if (!className.startsWith(PACKAGE_PREFIX_JAVAX_XML) && !this.hasSuperClassStartingWith(rawType, PACKAGE_PREFIX_JAVAX_XML)) {
            return null;
        }
        String factoryName = DESERIALIZERS_FOR_JAVAX_XML;
        Object ob = this.instantiate(factoryName, type);
        if (ob == null) {
            return null;
        }
        return ((Deserializers)ob).findBeanDeserializer(type, config, beanDesc);
    }

    public boolean hasDeserializerFor(Class<?> valueType) {
        if (this._IsXOfY(valueType, CLASS_DOM_NODE)) {
            return true;
        }
        if (this._IsXOfY(valueType, CLASS_DOM_DOCUMENT)) {
            return true;
        }
        String className = valueType.getName();
        if (className.startsWith(PACKAGE_PREFIX_JAVAX_XML) || this.hasSuperClassStartingWith(valueType, PACKAGE_PREFIX_JAVAX_XML)) {
            return true;
        }
        return this._sqlDeserializers.containsKey(className);
    }

    private boolean _IsXOfY(Class<?> valueType, Class<?> expType) {
        return expType != null && expType.isAssignableFrom(valueType);
    }

    private Object instantiate(String className, JavaType valueType) {
        try {
            return this.instantiate(Class.forName(className), valueType);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to find class `" + className + "` for handling values of type " + ClassUtil.getTypeDescription(valueType) + ", problem: (" + e.getClass().getName() + ") " + e.getMessage());
        }
    }

    private Object instantiate(Class<?> handlerClass, JavaType valueType) {
        try {
            return ClassUtil.createInstance(handlerClass, false);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to create instance of `" + handlerClass.getName() + "` for handling values of type " + ClassUtil.getTypeDescription(valueType) + ", problem: (" + e.getClass().getName() + ") " + e.getMessage());
        }
    }

    private boolean hasSuperClassStartingWith(Class<?> rawType, String prefix) {
        for (Class<?> supertype = rawType.getSuperclass(); supertype != null; supertype = supertype.getSuperclass()) {
            if (supertype == Object.class) {
                return false;
            }
            if (!supertype.getName().startsWith(prefix)) continue;
            return true;
        }
        return false;
    }

    static {
        Class<Document> doc = null;
        Class<Node> node = null;
        try {
            node = Node.class;
            doc = Document.class;
        } catch (Throwable throwable) {
            // empty catch block
        }
        CLASS_DOM_NODE = node;
        CLASS_DOM_DOCUMENT = doc;
        Java7Handlers x = null;
        try {
            x = Java7Handlers.instance();
        } catch (Throwable throwable) {
            // empty catch block
        }
        _jdk7Helper = x;
        instance = new OptionalHandlerFactory();
    }
}


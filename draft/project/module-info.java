/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

/* synthetic */ module com.fasterxml.jackson.databind {
    // version: 2.12.5

    requires static java.desktop;
    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires static java.sql;
    requires static java.xml;

    exports com.fasterxml.jackson.databind;
    exports com.fasterxml.jackson.databind.annotation;
    exports com.fasterxml.jackson.databind.cfg;
    exports com.fasterxml.jackson.databind.deser;
    exports com.fasterxml.jackson.databind.deser.impl;
    exports com.fasterxml.jackson.databind.deser.std;
    exports com.fasterxml.jackson.databind.exc;
    exports com.fasterxml.jackson.databind.ext;
    exports com.fasterxml.jackson.databind.introspect;
    exports com.fasterxml.jackson.databind.json;
    exports com.fasterxml.jackson.databind.jsonFormatVisitors;
    exports com.fasterxml.jackson.databind.jsonschema;
    exports com.fasterxml.jackson.databind.jsontype;
    exports com.fasterxml.jackson.databind.jsontype.impl;
    exports com.fasterxml.jackson.databind.module;
    exports com.fasterxml.jackson.databind.node;
    exports com.fasterxml.jackson.databind.ser;
    exports com.fasterxml.jackson.databind.ser.impl;
    exports com.fasterxml.jackson.databind.ser.std;
    exports com.fasterxml.jackson.databind.type;
    exports com.fasterxml.jackson.databind.util;

    uses Module;

    provides ObjectCodec with ObjectMapper;
}


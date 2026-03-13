/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class BeanUtil {
    @Deprecated
    public static String okNameForGetter(AnnotatedMethod am, boolean stdNaming) {
        String name = am.getName();
        String str = BeanUtil.okNameForIsGetter(am, name, stdNaming);
        if (str == null) {
            str = BeanUtil.okNameForRegularGetter(am, name, stdNaming);
        }
        return str;
    }

    @Deprecated
    public static String okNameForRegularGetter(AnnotatedMethod am, String name, boolean stdNaming) {
        if (name.startsWith("get")) {
            if ("getCallbacks".equals(name) ? BeanUtil.isCglibGetCallbacks(am) : "getMetaClass".equals(name) && BeanUtil.isGroovyMetaClassGetter(am)) {
                return null;
            }
            return stdNaming ? BeanUtil.stdManglePropertyName(name, 3) : BeanUtil.legacyManglePropertyName(name, 3);
        }
        return null;
    }

    @Deprecated
    public static String okNameForIsGetter(AnnotatedMethod am, String name, boolean stdNaming) {
        Class<?> rt;
        if (name.startsWith("is") && ((rt = am.getRawType()) == Boolean.class || rt == Boolean.TYPE)) {
            return stdNaming ? BeanUtil.stdManglePropertyName(name, 2) : BeanUtil.legacyManglePropertyName(name, 2);
        }
        return null;
    }

    @Deprecated
    public static String okNameForSetter(AnnotatedMethod am, boolean stdNaming) {
        return BeanUtil.okNameForMutator(am, "set", stdNaming);
    }

    @Deprecated
    public static String okNameForMutator(AnnotatedMethod am, String prefix, boolean stdNaming) {
        String name = am.getName();
        if (name.startsWith(prefix)) {
            return stdNaming ? BeanUtil.stdManglePropertyName(name, prefix.length()) : BeanUtil.legacyManglePropertyName(name, prefix.length());
        }
        return null;
    }

    public static Object getDefaultValue(JavaType type) {
        Class<?> cls = type.getRawClass();
        Class<?> prim = ClassUtil.primitiveType(cls);
        if (prim != null) {
            return ClassUtil.defaultValue(prim);
        }
        if (type.isContainerType() || type.isReferenceType()) {
            return JsonInclude.Include.NON_EMPTY;
        }
        if (cls == String.class) {
            return "";
        }
        if (type.isTypeOrSubTypeOf(Date.class)) {
            return new Date(0L);
        }
        if (type.isTypeOrSubTypeOf(Calendar.class)) {
            GregorianCalendar c = new GregorianCalendar();
            c.setTimeInMillis(0L);
            return c;
        }
        return null;
    }

    protected static boolean isCglibGetCallbacks(AnnotatedMethod am) {
        Class<?> compType;
        String className;
        Class<?> rt = am.getRawType();
        if (rt.isArray() && (className = (compType = rt.getComponentType()).getName()).contains(".cglib")) {
            return className.startsWith("net.sf.cglib") || className.startsWith("org.hibernate.repackage.cglib") || className.startsWith("org.springframework.cglib");
        }
        return false;
    }

    protected static boolean isGroovyMetaClassGetter(AnnotatedMethod am) {
        return am.getRawType().getName().startsWith("groovy.lang");
    }

    protected static String legacyManglePropertyName(String basename, int offset) {
        char d;
        int end = basename.length();
        if (end == offset) {
            return null;
        }
        char c = basename.charAt(offset);
        if (c == (d = Character.toLowerCase(c))) {
            return basename.substring(offset);
        }
        StringBuilder sb = new StringBuilder(end - offset);
        sb.append(d);
        for (int i = offset + 1; i < end; ++i) {
            c = basename.charAt(i);
            if (c == (d = Character.toLowerCase(c))) {
                sb.append(basename, i, end);
                break;
            }
            sb.append(d);
        }
        return sb.toString();
    }

    public static String stdManglePropertyName(String basename, int offset) {
        char c1;
        int end = basename.length();
        if (end == offset) {
            return null;
        }
        char c0 = basename.charAt(offset);
        if (c0 == (c1 = Character.toLowerCase(c0))) {
            return basename.substring(offset);
        }
        if (offset + 1 < end && Character.isUpperCase(basename.charAt(offset + 1))) {
            return basename.substring(offset);
        }
        StringBuilder sb = new StringBuilder(end - offset);
        sb.append(c1);
        sb.append(basename, offset + 1, end);
        return sb.toString();
    }

    public static String checkUnsupportedType(JavaType type) {
        String moduleName;
        String typeName;
        String className = type.getRawClass().getName();
        if (BeanUtil.isJava8TimeClass(className)) {
            if (className.indexOf(46, 10) >= 0) {
                return null;
            }
            typeName = "Java 8 date/time";
            moduleName = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310";
        } else if (BeanUtil.isJodaTimeClass(className)) {
            typeName = "Joda date/time";
            moduleName = "com.fasterxml.jackson.datatype:jackson-datatype-joda";
        } else {
            return null;
        }
        return String.format("%s type %s not supported by default: add Module \"%s\" to enable handling", typeName, ClassUtil.getTypeDescription(type), moduleName);
    }

    public static boolean isJava8TimeClass(Class<?> rawType) {
        return BeanUtil.isJava8TimeClass(rawType.getName());
    }

    private static boolean isJava8TimeClass(String className) {
        return className.startsWith("java.time.");
    }

    public static boolean isJodaTimeClass(Class<?> rawType) {
        return BeanUtil.isJodaTimeClass(rawType.getName());
    }

    private static boolean isJodaTimeClass(String className) {
        return className.startsWith("org.joda.time.");
    }
}


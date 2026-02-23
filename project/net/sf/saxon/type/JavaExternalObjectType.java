/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.ExternalObjectType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.ObjectValue;

public class JavaExternalObjectType
extends ExternalObjectType {
    protected Configuration config;
    protected Class<?> javaClass;

    public JavaExternalObjectType(Configuration config, Class<?> javaClass) {
        this.config = config;
        this.javaClass = javaClass;
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    @Override
    public String getName() {
        return this.javaClass.getName();
    }

    @Override
    public String getTargetNamespace() {
        return "http://saxon.sf.net/java-type";
    }

    @Override
    public StructuredQName getTypeName() {
        return JavaExternalObjectType.classNameToQName(this.javaClass.getName());
    }

    @Override
    public ItemType getPrimitiveItemType() {
        return this.config.getJavaExternalObjectType(Object.class);
    }

    public Affinity getRelationship(JavaExternalObjectType other) {
        Class<?> j2 = other.javaClass;
        if (this.javaClass.equals(j2)) {
            return Affinity.SAME_TYPE;
        }
        if (this.javaClass.isAssignableFrom(j2)) {
            return Affinity.SUBSUMES;
        }
        if (j2.isAssignableFrom(this.javaClass)) {
            return Affinity.SUBSUMED_BY;
        }
        if (this.javaClass.isInterface() || j2.isInterface()) {
            return Affinity.OVERLAPS;
        }
        return Affinity.DISJOINT;
    }

    public Class<?> getJavaClass() {
        return this.javaClass;
    }

    @Override
    public boolean matches(Item item, TypeHierarchy th) {
        if (item instanceof ObjectValue) {
            Object obj = ((ObjectValue)item).getObject();
            return this.javaClass.isAssignableFrom(obj.getClass());
        }
        return false;
    }

    @Override
    public String toString() {
        return JavaExternalObjectType.classNameToQName(this.javaClass.getName()).getEQName();
    }

    public String getDisplayName() {
        return "java-type:" + this.javaClass.getName();
    }

    public int hashCode() {
        return this.javaClass.hashCode();
    }

    public boolean equals(Object obj) {
        return obj instanceof JavaExternalObjectType && this.javaClass == ((JavaExternalObjectType)obj).javaClass;
    }

    public static String classNameToLocalName(String className) {
        return className.replace('$', '-').replace("[", "_-");
    }

    public static String localNameToClassName(String className) {
        FastStringBuffer fsb = new FastStringBuffer(className.length());
        boolean atStart = true;
        for (int i = 0; i < className.length(); ++i) {
            char c = className.charAt(i);
            if (atStart) {
                if (c == '_' && i + 1 < className.length() && className.charAt(i + 1) == '-') {
                    fsb.cat('[');
                    ++i;
                    continue;
                }
                atStart = false;
                fsb.cat(c == '-' ? (char)'$' : (char)c);
                continue;
            }
            fsb.cat(c == '-' ? (char)'$' : (char)c);
        }
        return fsb.toString();
    }

    public static StructuredQName classNameToQName(String className) {
        return new StructuredQName("jt", "http://saxon.sf.net/java-type", JavaExternalObjectType.classNameToLocalName(className));
    }
}


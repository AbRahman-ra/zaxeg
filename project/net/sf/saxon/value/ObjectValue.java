/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.util.Objects;
import net.sf.saxon.om.Genre;
import net.sf.saxon.trans.Err;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.ExternalObject;
import net.sf.saxon.value.StringValue;

public class ObjectValue<T>
implements ExternalObject<T> {
    private T value;

    public ObjectValue(T object) {
        this.value = Objects.requireNonNull(object, "External object cannot wrap a Java null");
    }

    @Override
    public Genre getGenre() {
        return Genre.EXTERNAL;
    }

    @Override
    public String getStringValue() {
        return this.value.toString();
    }

    @Override
    public CharSequence getStringValueCS() {
        return this.value.toString();
    }

    @Override
    public StringValue atomize() {
        return new StringValue(this.getStringValue());
    }

    @Override
    public ItemType getItemType(TypeHierarchy th) {
        return th.getConfiguration().getJavaExternalObjectType(this.value.getClass());
    }

    public static String displayTypeName(Object value) {
        return "java-type:" + value.getClass().getName();
    }

    @Override
    public boolean effectiveBooleanValue() {
        return true;
    }

    @Override
    public T getObject() {
        return this.value;
    }

    public boolean equals(Object other) {
        if (other instanceof ObjectValue) {
            T o = ((ObjectValue)other).value;
            return this.value.equals(o);
        }
        return false;
    }

    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public String toShortString() {
        String v = this.value.toString();
        if (v.startsWith(this.value.getClass().getName())) {
            return v;
        }
        return "(" + this.value.getClass().getSimpleName() + ")" + Err.truncate30(this.value.toString());
    }
}


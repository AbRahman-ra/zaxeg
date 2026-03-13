/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sun.jna;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.TypeMapper;
import com.sun.jna.WString;
import java.util.Iterator;

public abstract class Union
extends Structure {
    private Structure.StructField activeField;
    private Structure.StructField biggestField;

    protected Union() {
    }

    protected Union(int size) {
        super(size);
    }

    protected Union(int size, int alignType) {
        super(size, alignType);
    }

    protected Union(TypeMapper mapper) {
        super(mapper);
    }

    protected Union(int size, int alignType, TypeMapper mapper) {
        super(size, alignType, mapper);
    }

    public void setType(Class type) {
        this.ensureAllocated();
        Iterator i = this.fields().values().iterator();
        while (i.hasNext()) {
            Structure.StructField f = (Structure.StructField)i.next();
            if (f.type != type) continue;
            this.activeField = f;
            return;
        }
        throw new IllegalArgumentException("No field of type " + type + " in " + this);
    }

    public Object readField(String name) {
        this.ensureAllocated();
        Structure.StructField f = (Structure.StructField)this.fields().get(name);
        if (f != null) {
            this.setType(f.type);
        }
        return super.readField(name);
    }

    public void writeField(String name) {
        this.ensureAllocated();
        Structure.StructField f = (Structure.StructField)this.fields().get(name);
        if (f != null) {
            this.setType(f.type);
        }
        super.writeField(name);
    }

    public void writeField(String name, Object value) {
        this.ensureAllocated();
        Structure.StructField f = (Structure.StructField)this.fields().get(name);
        if (f != null) {
            this.setType(f.type);
        }
        super.writeField(name, value);
    }

    public Object getTypedValue(Class type) {
        this.ensureAllocated();
        Iterator i = this.fields().values().iterator();
        while (i.hasNext()) {
            Structure.StructField f = (Structure.StructField)i.next();
            if (f.type != type) continue;
            this.activeField = f;
            this.read();
            return this.getField(this.activeField);
        }
        throw new IllegalArgumentException("No field of type " + type + " in " + this);
    }

    public Object setTypedValue(Object object) {
        this.ensureAllocated();
        Structure.StructField f = this.findField(object.getClass());
        if (f != null) {
            this.activeField = f;
            this.setField(f, object);
            return this;
        }
        throw new IllegalArgumentException("No field of type " + object.getClass() + " in " + this);
    }

    private Structure.StructField findField(Class type) {
        Iterator i = this.fields().values().iterator();
        while (i.hasNext()) {
            Structure.StructField f = (Structure.StructField)i.next();
            if (!f.type.isAssignableFrom(type)) continue;
            return f;
        }
        return null;
    }

    void writeField(Structure.StructField field) {
        if (field == this.activeField) {
            super.writeField(field);
        }
    }

    Object readField(Structure.StructField field) {
        if (field == this.activeField || !Structure.class.isAssignableFrom(field.type) && !String.class.isAssignableFrom(field.type) && !WString.class.isAssignableFrom(field.type)) {
            return super.readField(field);
        }
        return null;
    }

    int calculateSize(boolean force) {
        int size = super.calculateSize(force);
        if (size != -1) {
            int fsize = 0;
            Iterator i = this.fields().values().iterator();
            while (i.hasNext()) {
                Structure.StructField f = (Structure.StructField)i.next();
                f.offset = 0;
                if (f.size <= fsize) continue;
                fsize = f.size;
                this.biggestField = f;
            }
            size = this.calculateAlignedSize(fsize);
        }
        return size;
    }

    protected int getNativeAlignment(Class type, Object value, boolean isFirstElement) {
        return super.getNativeAlignment(type, value, true);
    }

    Pointer getTypeInfo() {
        return Union.getTypeInfo(this.getField(this.biggestField));
    }
}


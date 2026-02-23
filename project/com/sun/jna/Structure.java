/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sun.jna;

import com.sun.jna.Callback;
import com.sun.jna.CallbackReference;
import com.sun.jna.FromNativeContext;
import com.sun.jna.FromNativeConverter;
import com.sun.jna.Function;
import com.sun.jna.IntegerType;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeMapped;
import com.sun.jna.NativeMappedConverter;
import com.sun.jna.NativeString;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.StructureReadContext;
import com.sun.jna.StructureWriteContext;
import com.sun.jna.ToNativeContext;
import com.sun.jna.ToNativeConverter;
import com.sun.jna.TypeMapper;
import com.sun.jna.Union;
import com.sun.jna.WString;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public abstract class Structure {
    private static final boolean REVERSE_FIELDS;
    static boolean REQUIRES_FIELD_ORDER;
    static final boolean isPPC;
    static final boolean isSPARC;
    public static final int ALIGN_DEFAULT = 0;
    public static final int ALIGN_NONE = 1;
    public static final int ALIGN_GNUC = 2;
    public static final int ALIGN_MSVC = 3;
    private static final int MAX_GNUC_ALIGNMENT;
    protected static final int CALCULATE_SIZE = -1;
    private Pointer memory;
    private int size = -1;
    private int alignType;
    private int structAlignment;
    private final Map structFields = new LinkedHashMap();
    private final Map nativeStrings = new HashMap();
    private TypeMapper typeMapper;
    private long typeInfo;
    private List fieldOrder;
    private boolean autoRead = true;
    private boolean autoWrite = true;
    private static Set reading;
    private static Set writing;
    static /* synthetic */ Class class$java$lang$Void;
    static /* synthetic */ Class class$com$sun$jna$Union;

    protected Structure() {
        this(-1);
    }

    protected Structure(int size) {
        this(size, 0);
    }

    protected Structure(int size, int alignment) {
        this(size, alignment, null);
    }

    protected Structure(TypeMapper mapper) {
        this(-1, 0, mapper);
    }

    protected Structure(int size, int alignment, TypeMapper mapper) {
        this.setAlignType(alignment);
        this.setTypeMapper(mapper);
        this.allocateMemory(size);
    }

    Map fields() {
        return this.structFields;
    }

    protected void setTypeMapper(TypeMapper mapper) {
        Class<?> declaring;
        if (mapper == null && (declaring = this.getClass().getDeclaringClass()) != null) {
            mapper = Native.getTypeMapper(declaring);
        }
        this.typeMapper = mapper;
        this.size = -1;
        this.memory = null;
    }

    protected void setAlignType(int alignType) {
        if (alignType == 0) {
            Class<?> declaring = this.getClass().getDeclaringClass();
            if (declaring != null) {
                alignType = Native.getStructureAlignment(declaring);
            }
            if (alignType == 0) {
                alignType = Platform.isWindows() ? 3 : 2;
            }
        }
        this.alignType = alignType;
        this.size = -1;
        this.memory = null;
    }

    protected void useMemory(Pointer m) {
        this.useMemory(m, 0);
    }

    protected void useMemory(Pointer m, int offset) {
        this.memory = m.share(offset, this.size());
    }

    protected void ensureAllocated() {
        if (this.size == -1) {
            this.allocateMemory();
        }
    }

    protected void allocateMemory() {
        this.allocateMemory(this.calculateSize(true));
    }

    protected void allocateMemory(int size) {
        if (size == -1) {
            size = this.calculateSize(false);
        } else if (size <= 0) {
            throw new IllegalArgumentException("Structure size must be greater than zero: " + size);
        }
        if (size != -1) {
            this.memory = new Memory(size);
            this.memory.clear(size);
            this.size = size;
            if (this instanceof ByValue) {
                this.getTypeInfo();
            }
        }
    }

    public int size() {
        this.ensureAllocated();
        return this.size;
    }

    public void clear() {
        this.memory.clear(this.size());
    }

    public Pointer getPointer() {
        this.ensureAllocated();
        return this.memory;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void read() {
        this.ensureAllocated();
        Set set = reading;
        synchronized (set) {
            if (reading.contains(this)) {
                return;
            }
            reading.add(this);
        }
        try {
            Iterator i = this.structFields.values().iterator();
            while (i.hasNext()) {
                this.readField((StructField)i.next());
            }
        } finally {
            set = reading;
            synchronized (set) {
                reading.remove(this);
            }
        }
    }

    public Object readField(String name) {
        this.ensureAllocated();
        StructField f = (StructField)this.structFields.get(name);
        if (f == null) {
            throw new IllegalArgumentException("No such field: " + name);
        }
        return this.readField(f);
    }

    Object getField(StructField structField) {
        try {
            return structField.field.get(this);
        } catch (Exception e) {
            throw new Error("Exception reading field '" + structField.name + "' in " + this.getClass() + ": " + e);
        }
    }

    void setField(StructField structField, Object value) {
        try {
            structField.field.set(this, value);
        } catch (IllegalAccessException e) {
            throw new Error("Unexpectedly unable to write to field '" + structField.name + "' within " + this.getClass() + ": " + e);
        }
    }

    static Structure updateStructureByReference(Class type, Structure s, Pointer address) {
        if (address == null) {
            s = null;
        } else {
            if (s == null || !address.equals(s.getPointer())) {
                s = Structure.newInstance(type);
                s.useMemory(address);
            }
            if (s.getAutoRead()) {
                s.read();
            }
        }
        return s;
    }

    Object readField(StructField structField) {
        int offset = structField.offset;
        Class nativeType = structField.type;
        FromNativeConverter readConverter = structField.readConverter;
        if (readConverter != null) {
            nativeType = readConverter.nativeType();
        }
        Object currentValue = Structure.class.isAssignableFrom(nativeType) || Callback.class.isAssignableFrom(nativeType) || Buffer.class.isAssignableFrom(nativeType) || Pointer.class.isAssignableFrom(nativeType) || nativeType.isArray() ? this.getField(structField) : null;
        Object result = this.readValue(offset, nativeType, currentValue);
        if (readConverter != null) {
            result = readConverter.fromNative(result, structField.context);
        }
        this.setField(structField, result);
        return result;
    }

    private Object readValue(int offset, Class nativeType, Object currentValue) {
        Object result = null;
        if (Structure.class.isAssignableFrom(nativeType)) {
            Structure s = (Structure)currentValue;
            if (ByReference.class.isAssignableFrom(nativeType)) {
                s = Structure.updateStructureByReference(nativeType, s, this.memory.getPointer(offset));
            } else {
                s.useMemory(this.memory, offset);
                s.read();
            }
            result = s;
        } else if (nativeType == Boolean.TYPE || nativeType == Boolean.class) {
            result = Function.valueOf(this.memory.getInt(offset) != 0);
        } else if (nativeType == Byte.TYPE || nativeType == Byte.class) {
            result = new Byte(this.memory.getByte(offset));
        } else if (nativeType == Short.TYPE || nativeType == Short.class) {
            result = new Short(this.memory.getShort(offset));
        } else if (nativeType == Character.TYPE || nativeType == Character.class) {
            result = new Character(this.memory.getChar(offset));
        } else if (nativeType == Integer.TYPE || nativeType == Integer.class) {
            result = new Integer(this.memory.getInt(offset));
        } else if (nativeType == Long.TYPE || nativeType == Long.class) {
            result = new Long(this.memory.getLong(offset));
        } else if (nativeType == Float.TYPE || nativeType == Float.class) {
            result = new Float(this.memory.getFloat(offset));
        } else if (nativeType == Double.TYPE || nativeType == Double.class) {
            result = new Double(this.memory.getDouble(offset));
        } else if (nativeType == Pointer.class) {
            Pointer p = this.memory.getPointer(offset);
            if (p != null) {
                Pointer oldp;
                Pointer pointer = oldp = currentValue instanceof Pointer ? (Pointer)currentValue : null;
                result = oldp == null || p.peer != oldp.peer ? p : oldp;
            }
        } else if (nativeType == String.class) {
            Pointer p = this.memory.getPointer(offset);
            result = p != null ? p.getString(0L) : null;
        } else if (nativeType == WString.class) {
            Pointer p = this.memory.getPointer(offset);
            result = p != null ? new WString(p.getString(0L, true)) : null;
        } else if (Callback.class.isAssignableFrom(nativeType)) {
            Pointer fp = this.memory.getPointer(offset);
            if (fp == null) {
                result = null;
            } else {
                Callback cb = (Callback)currentValue;
                Pointer oldfp = CallbackReference.getFunctionPointer(cb);
                if (!fp.equals(oldfp)) {
                    cb = CallbackReference.getCallback(nativeType, fp);
                }
                result = cb;
            }
        } else if (nativeType.isArray()) {
            result = currentValue;
            if (result == null) {
                throw new IllegalStateException("Array field in Structure not initialized");
            }
            this.readArrayValue(offset, result, nativeType.getComponentType());
        } else {
            throw new IllegalArgumentException("Unsupported field type \"" + nativeType + "\"");
        }
        return result;
    }

    private void readArrayValue(int offset, Object o, Class cls) {
        int length = 0;
        length = Array.getLength(o);
        Object result = o;
        if (cls == Byte.TYPE) {
            this.memory.read((long)offset, (byte[])result, 0, length);
        } else if (cls == Short.TYPE) {
            this.memory.read((long)offset, (short[])result, 0, length);
        } else if (cls == Character.TYPE) {
            this.memory.read((long)offset, (char[])result, 0, length);
        } else if (cls == Integer.TYPE) {
            this.memory.read((long)offset, (int[])result, 0, length);
        } else if (cls == Long.TYPE) {
            this.memory.read((long)offset, (long[])result, 0, length);
        } else if (cls == Float.TYPE) {
            this.memory.read((long)offset, (float[])result, 0, length);
        } else if (cls == Double.TYPE) {
            this.memory.read((long)offset, (double[])result, 0, length);
        } else if (Pointer.class.isAssignableFrom(cls)) {
            this.memory.read((long)offset, (Pointer[])result, 0, length);
        } else if (Structure.class.isAssignableFrom(cls)) {
            Structure[] sarray = (Structure[])result;
            if (ByReference.class.isAssignableFrom(cls)) {
                Pointer[] parray = this.memory.getPointerArray(offset, sarray.length);
                for (int i = 0; i < sarray.length; ++i) {
                    sarray[i] = Structure.updateStructureByReference(cls, sarray[i], parray[i]);
                }
            } else {
                for (int i = 0; i < sarray.length; ++i) {
                    if (sarray[i] == null) {
                        sarray[i] = Structure.newInstance(cls);
                    }
                    sarray[i].useMemory(this.memory, offset + i * sarray[i].size());
                    sarray[i].read();
                }
            }
        } else if (NativeMapped.class.isAssignableFrom(cls)) {
            NativeMapped[] array = (NativeMapped[])result;
            NativeMappedConverter tc = NativeMappedConverter.getInstance(cls);
            int size = Structure.getNativeSize(result.getClass(), result) / array.length;
            for (int i = 0; i < array.length; ++i) {
                Object value = this.readValue(offset + size * i, tc.nativeType(), array[i]);
                array[i] = (NativeMapped)tc.fromNative(value, new FromNativeContext(cls));
            }
        } else {
            throw new IllegalArgumentException("Array of " + cls + " not supported");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void write() {
        this.ensureAllocated();
        if (this instanceof ByValue) {
            this.getTypeInfo();
        }
        Set set = writing;
        synchronized (set) {
            if (writing.contains(this)) {
                return;
            }
            writing.add(this);
        }
        try {
            Iterator i = this.structFields.values().iterator();
            while (i.hasNext()) {
                StructField sf = (StructField)i.next();
                if (sf.isVolatile) continue;
                this.writeField(sf);
            }
        } finally {
            set = writing;
            synchronized (set) {
                writing.remove(this);
            }
        }
    }

    public void writeField(String name) {
        this.ensureAllocated();
        StructField f = (StructField)this.structFields.get(name);
        if (f == null) {
            throw new IllegalArgumentException("No such field: " + name);
        }
        this.writeField(f);
    }

    public void writeField(String name, Object value) {
        this.ensureAllocated();
        StructField f = (StructField)this.structFields.get(name);
        if (f == null) {
            throw new IllegalArgumentException("No such field: " + name);
        }
        this.setField(f, value);
        this.writeField(f);
    }

    void writeField(StructField structField) {
        int offset = structField.offset;
        Object value = this.getField(structField);
        Class nativeType = structField.type;
        ToNativeConverter converter = structField.writeConverter;
        if (converter != null) {
            value = converter.toNative(value, new StructureWriteContext(this, structField.field));
            nativeType = converter.nativeType();
        }
        if (String.class == nativeType || WString.class == nativeType) {
            boolean wide;
            boolean bl = wide = nativeType == WString.class;
            if (value != null) {
                NativeString nativeString = new NativeString(value.toString(), wide);
                this.nativeStrings.put(structField.name, nativeString);
                value = nativeString.getPointer();
            } else {
                value = null;
                this.nativeStrings.remove(structField.name);
            }
        }
        if (!this.writeValue(offset, value, nativeType)) {
            String msg = "Structure field \"" + structField.name + "\" was declared as " + structField.type + (structField.type == nativeType ? "" : " (native type " + nativeType + ")") + ", which is not supported within a Structure";
            throw new IllegalArgumentException(msg);
        }
    }

    private boolean writeValue(int offset, Object value, Class nativeType) {
        if (nativeType == Boolean.TYPE || nativeType == Boolean.class) {
            this.memory.setInt(offset, Boolean.TRUE.equals(value) ? -1 : 0);
        } else if (nativeType == Byte.TYPE || nativeType == Byte.class) {
            this.memory.setByte(offset, value == null ? (byte)0 : (Byte)value);
        } else if (nativeType == Short.TYPE || nativeType == Short.class) {
            this.memory.setShort(offset, value == null ? (short)0 : (Short)value);
        } else if (nativeType == Character.TYPE || nativeType == Character.class) {
            this.memory.setChar(offset, value == null ? (char)'\u0000' : ((Character)value).charValue());
        } else if (nativeType == Integer.TYPE || nativeType == Integer.class) {
            this.memory.setInt(offset, value == null ? 0 : (Integer)value);
        } else if (nativeType == Long.TYPE || nativeType == Long.class) {
            this.memory.setLong(offset, value == null ? 0L : (Long)value);
        } else if (nativeType == Float.TYPE || nativeType == Float.class) {
            this.memory.setFloat(offset, value == null ? 0.0f : ((Float)value).floatValue());
        } else if (nativeType == Double.TYPE || nativeType == Double.class) {
            this.memory.setDouble(offset, value == null ? 0.0 : (Double)value);
        } else if (nativeType == Pointer.class) {
            this.memory.setPointer(offset, (Pointer)value);
        } else if (nativeType == String.class) {
            this.memory.setPointer(offset, (Pointer)value);
        } else if (nativeType == WString.class) {
            this.memory.setPointer(offset, (Pointer)value);
        } else if (Structure.class.isAssignableFrom(nativeType)) {
            Structure s = (Structure)value;
            if (ByReference.class.isAssignableFrom(nativeType)) {
                this.memory.setPointer(offset, s == null ? null : s.getPointer());
                if (s != null) {
                    s.write();
                }
            } else {
                s.useMemory(this.memory, offset);
                s.write();
            }
        } else if (Callback.class.isAssignableFrom(nativeType)) {
            this.memory.setPointer(offset, CallbackReference.getFunctionPointer((Callback)value));
        } else {
            if (nativeType.isArray()) {
                return this.writeArrayValue(offset, value, nativeType.getComponentType());
            }
            return false;
        }
        return true;
    }

    private boolean writeArrayValue(int offset, Object value, Class cls) {
        if (cls == Byte.TYPE) {
            byte[] buf = (byte[])value;
            this.memory.write((long)offset, buf, 0, buf.length);
        } else if (cls == Short.TYPE) {
            short[] buf = (short[])value;
            this.memory.write((long)offset, buf, 0, buf.length);
        } else if (cls == Character.TYPE) {
            char[] buf = (char[])value;
            this.memory.write((long)offset, buf, 0, buf.length);
        } else if (cls == Integer.TYPE) {
            int[] buf = (int[])value;
            this.memory.write((long)offset, buf, 0, buf.length);
        } else if (cls == Long.TYPE) {
            long[] buf = (long[])value;
            this.memory.write((long)offset, buf, 0, buf.length);
        } else if (cls == Float.TYPE) {
            float[] buf = (float[])value;
            this.memory.write((long)offset, buf, 0, buf.length);
        } else if (cls == Double.TYPE) {
            double[] buf = (double[])value;
            this.memory.write((long)offset, buf, 0, buf.length);
        } else if (Pointer.class.isAssignableFrom(cls)) {
            Pointer[] buf = (Pointer[])value;
            this.memory.write((long)offset, buf, 0, buf.length);
        } else if (Structure.class.isAssignableFrom(cls)) {
            Structure[] sbuf = (Structure[])value;
            if (ByReference.class.isAssignableFrom(cls)) {
                Pointer[] buf = new Pointer[sbuf.length];
                for (int i = 0; i < sbuf.length; ++i) {
                    buf[i] = sbuf[i] == null ? null : sbuf[i].getPointer();
                }
                this.memory.write((long)offset, buf, 0, buf.length);
            } else {
                for (int i = 0; i < sbuf.length; ++i) {
                    if (sbuf[i] == null) {
                        sbuf[i] = Structure.newInstance(cls);
                    }
                    sbuf[i].useMemory(this.memory, offset + i * sbuf[i].size());
                    sbuf[i].write();
                }
            }
        } else if (NativeMapped.class.isAssignableFrom(cls)) {
            NativeMapped[] buf = (NativeMapped[])value;
            NativeMappedConverter tc = NativeMappedConverter.getInstance(cls);
            Class nativeType = tc.nativeType();
            int size = Structure.getNativeSize(value.getClass(), value) / buf.length;
            for (int i = 0; i < buf.length; ++i) {
                Object element = tc.toNative(buf[i], new ToNativeContext());
                if (this.writeValue(offset + i * size, element, nativeType)) continue;
                return false;
            }
        } else {
            throw new IllegalArgumentException("Inline array of " + cls + " not supported");
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected List getFieldOrder() {
        Structure structure = this;
        synchronized (structure) {
            if (this.fieldOrder == null) {
                this.fieldOrder = new ArrayList();
            }
            return this.fieldOrder;
        }
    }

    protected void setFieldOrder(String[] fields) {
        this.getFieldOrder().addAll(Arrays.asList(fields));
    }

    protected void sortFields(Field[] fields, String[] names) {
        block0: for (int i = 0; i < names.length; ++i) {
            for (int f = i; f < fields.length; ++f) {
                if (!names[i].equals(fields[f].getName())) continue;
                Field tmp = fields[f];
                fields[f] = fields[i];
                fields[i] = tmp;
                continue block0;
            }
        }
    }

    int calculateSize(boolean force) {
        int i;
        this.structAlignment = 1;
        int calculatedSize = 0;
        Field[] fields = this.getClass().getFields();
        ArrayList<Field> flist = new ArrayList<Field>();
        for (i = 0; i < fields.length; ++i) {
            int modifiers = fields[i].getModifiers();
            if (Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) continue;
            flist.add(fields[i]);
        }
        fields = flist.toArray(new Field[flist.size()]);
        if (REVERSE_FIELDS) {
            for (i = 0; i < fields.length / 2; ++i) {
                int idx = fields.length - 1 - i;
                Field tmp = fields[i];
                fields[i] = fields[idx];
                fields[idx] = tmp;
            }
        } else if (REQUIRES_FIELD_ORDER) {
            List fieldOrder = this.getFieldOrder();
            if (fieldOrder.size() < fields.length) {
                if (force) {
                    throw new Error("This VM does not store fields in a predictable order; you must use setFieldOrder: " + System.getProperty("java.vendor") + ", " + System.getProperty("java.version"));
                }
                return -1;
            }
            this.sortFields(fields, fieldOrder.toArray(new String[fieldOrder.size()]));
        }
        for (int i2 = 0; i2 < fields.length; ++i2) {
            Field field = fields[i2];
            int modifiers = field.getModifiers();
            Class type = field.getType();
            StructField structField = new StructField();
            structField.isVolatile = Modifier.isVolatile(modifiers);
            structField.field = field;
            if (Modifier.isFinal(modifiers)) {
                field.setAccessible(true);
            }
            structField.name = field.getName();
            structField.type = type;
            if ((class$com$sun$jna$Callback == null ? Structure.class$("com.sun.jna.Callback") : class$com$sun$jna$Callback).isAssignableFrom(type) && !type.isInterface()) {
                throw new IllegalArgumentException("Structure Callback field '" + field.getName() + "' must be an interface");
            }
            if (type.isArray() && (class$com$sun$jna$Structure == null ? Structure.class$("com.sun.jna.Structure") : class$com$sun$jna$Structure).equals(type.getComponentType())) {
                String msg = "Nested Structure arrays must use a derived Structure type so that the size of the elements can be determined";
                throw new IllegalArgumentException(msg);
            }
            int fieldAlignment = 1;
            if (!Modifier.isPublic(field.getModifiers())) continue;
            Object value = this.getField(structField);
            if (value == null) {
                if ((class$com$sun$jna$Structure == null ? Structure.class$("com.sun.jna.Structure") : class$com$sun$jna$Structure).isAssignableFrom(type) && !(class$com$sun$jna$Structure$ByReference == null ? Structure.class$("com.sun.jna.Structure$ByReference") : class$com$sun$jna$Structure$ByReference).isAssignableFrom(type)) {
                    try {
                        value = Structure.newInstance(type);
                        this.setField(structField, value);
                    } catch (IllegalArgumentException e) {
                        String msg = "Can't determine size of nested structure: " + e.getMessage();
                        throw new IllegalArgumentException(msg);
                    }
                } else if (type.isArray()) {
                    if (force) {
                        throw new IllegalStateException("Array fields must be initialized");
                    }
                    return -1;
                }
            }
            Class nativeType = type;
            if ((class$com$sun$jna$NativeMapped == null ? Structure.class$("com.sun.jna.NativeMapped") : class$com$sun$jna$NativeMapped).isAssignableFrom(type)) {
                NativeMappedConverter tc = NativeMappedConverter.getInstance(type);
                if (value == null) {
                    value = tc.defaultValue();
                    this.setField(structField, value);
                }
                nativeType = tc.nativeType();
                structField.writeConverter = tc;
                structField.readConverter = tc;
                structField.context = new StructureReadContext(this, field);
            } else if (this.typeMapper != null) {
                ToNativeConverter writeConverter = this.typeMapper.getToNativeConverter(type);
                FromNativeConverter readConverter = this.typeMapper.getFromNativeConverter(type);
                if (writeConverter != null && readConverter != null) {
                    nativeType = (value = writeConverter.toNative(value, new StructureWriteContext(this, structField.field))) != null ? value.getClass() : (class$com$sun$jna$Pointer == null ? Structure.class$("com.sun.jna.Pointer") : class$com$sun$jna$Pointer);
                    structField.writeConverter = writeConverter;
                    structField.readConverter = readConverter;
                    structField.context = new StructureReadContext(this, field);
                } else if (writeConverter != null || readConverter != null) {
                    String msg = "Structures require bidirectional type conversion for " + type;
                    throw new IllegalArgumentException(msg);
                }
            }
            try {
                structField.size = Structure.getNativeSize(nativeType, value);
                fieldAlignment = this.getNativeAlignment(nativeType, value, i2 == 0);
            } catch (IllegalArgumentException e) {
                if (!force && this.typeMapper == null) {
                    return -1;
                }
                throw e;
            }
            this.structAlignment = Math.max(this.structAlignment, fieldAlignment);
            if (calculatedSize % fieldAlignment != 0) {
                calculatedSize += fieldAlignment - calculatedSize % fieldAlignment;
            }
            structField.offset = calculatedSize;
            calculatedSize += structField.size;
            this.structFields.put(structField.name, structField);
        }
        if (calculatedSize > 0) {
            return this.calculateAlignedSize(calculatedSize);
        }
        throw new IllegalArgumentException("Structure " + this.getClass() + " has unknown size (ensure " + "all fields are public)");
    }

    int calculateAlignedSize(int calculatedSize) {
        if (this.alignType != 1 && calculatedSize % this.structAlignment != 0) {
            calculatedSize += this.structAlignment - calculatedSize % this.structAlignment;
        }
        return calculatedSize;
    }

    protected int getStructAlignment() {
        if (this.size == -1) {
            this.calculateSize(true);
        }
        return this.structAlignment;
    }

    protected int getNativeAlignment(Class type, Object value, boolean isFirstElement) {
        int alignment = 1;
        if (NativeMapped.class.isAssignableFrom(type)) {
            NativeMappedConverter tc = NativeMappedConverter.getInstance(type);
            type = tc.nativeType();
            value = tc.toNative(value, new ToNativeContext());
        }
        int size = Structure.getNativeSize(type, value);
        if (type.isPrimitive() || Long.class == type || Integer.class == type || Short.class == type || Character.class == type || Byte.class == type || Boolean.class == type || Float.class == type || Double.class == type) {
            alignment = size;
        } else if (Pointer.class == type || Buffer.class.isAssignableFrom(type) || Callback.class.isAssignableFrom(type) || WString.class == type || String.class == type) {
            alignment = Pointer.SIZE;
        } else if (Structure.class.isAssignableFrom(type)) {
            if (ByReference.class.isAssignableFrom(type)) {
                alignment = Pointer.SIZE;
            } else {
                if (value == null) {
                    value = Structure.newInstance(type);
                }
                alignment = ((Structure)value).getStructAlignment();
            }
        } else if (type.isArray()) {
            alignment = this.getNativeAlignment(type.getComponentType(), null, isFirstElement);
        } else {
            throw new IllegalArgumentException("Type " + type + " has unknown " + "native alignment");
        }
        if (this.alignType == 1) {
            alignment = 1;
        } else if (this.alignType == 3) {
            alignment = Math.min(8, alignment);
        } else if (!(this.alignType != 2 || isFirstElement && Platform.isMac() && isPPC)) {
            alignment = Math.min(MAX_GNUC_ALIGNMENT, alignment);
        }
        return alignment;
    }

    private static int getNativeSize(Class type, Object value) {
        if (type.isArray()) {
            int len = Array.getLength(value);
            if (len > 0) {
                Object o = Array.get(value, 0);
                return len * Structure.getNativeSize(type.getComponentType(), o);
            }
            throw new IllegalArgumentException("Arrays of length zero not allowed in structure: " + type);
        }
        if (Buffer.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("the type \"" + type.getName() + "\" is not supported as a structure field: ");
        }
        if (Structure.class.isAssignableFrom(type) && !ByReference.class.isAssignableFrom(type)) {
            if (value == null) {
                value = Structure.newInstance(type);
            }
            return ((Structure)value).size();
        }
        try {
            return Native.getNativeSize(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("The type \"" + type.getName() + "\" is not supported as a structure field: " + e.getMessage());
        }
    }

    public String toString() {
        return this.toString(0);
    }

    private String format(Class type) {
        String s = type.getName();
        int dot = s.lastIndexOf(".");
        return s.substring(dot + 1);
    }

    private String toString(int indent) {
        String LS = System.getProperty("line.separator");
        String name = this.format(this.getClass()) + "(" + this.getPointer() + ")";
        if (!(this.getPointer() instanceof Memory)) {
            name = name + " (" + this.size() + " bytes)";
        }
        String prefix = "";
        for (int idx = 0; idx < indent; ++idx) {
            prefix = prefix + "  ";
        }
        String contents = "";
        Iterator i = this.structFields.values().iterator();
        while (i.hasNext()) {
            StructField sf = (StructField)i.next();
            Object value = this.getField(sf);
            String type = this.format(sf.type);
            String index = "";
            contents = contents + prefix;
            if (sf.type.isArray() && value != null) {
                type = this.format(sf.type.getComponentType());
                index = "[" + Array.getLength(value) + "]";
            }
            contents = contents + "  " + type + " " + sf.name + index + "@" + Integer.toHexString(sf.offset);
            if (value instanceof Structure) {
                if (value instanceof ByReference) {
                    String v = value.toString();
                    if (v.indexOf(LS) != -1) {
                        v = v.substring(0, v.indexOf(LS));
                    }
                    value = v + "...}";
                } else {
                    value = ((Structure)value).toString(indent + 1);
                }
            }
            contents = contents + "=" + String.valueOf(value).trim();
            contents = contents + LS;
            if (i.hasNext()) continue;
            contents = contents + prefix + "}";
        }
        if (indent == 0 && Boolean.getBoolean("jna.dump_memory")) {
            byte[] buf = this.getPointer().getByteArray(0L, this.size());
            int BYTES_PER_ROW = 4;
            contents = contents + LS + "memory dump" + LS;
            for (int i2 = 0; i2 < buf.length; ++i2) {
                if (i2 % 4 == 0) {
                    contents = contents + "[";
                }
                if (buf[i2] >= 0 && buf[i2] < 16) {
                    contents = contents + "0";
                }
                contents = contents + Integer.toHexString(buf[i2] & 0xFF);
                if (i2 % 4 != 3 || i2 >= buf.length - 1) continue;
                contents = contents + "]" + LS;
            }
            contents = contents + "]";
        }
        return name + " {" + LS + contents;
    }

    public Structure[] toArray(Structure[] array) {
        if (this.size == -1) {
            this.allocateMemory();
        }
        if (Memory.class.equals(this.memory.getClass())) {
            Memory m = (Memory)this.memory;
            int requiredSize = array.length * this.size();
            if (m.getSize() < (long)requiredSize) {
                m = new Memory(requiredSize);
                m.clear();
                this.useMemory(m);
            }
        }
        array[0] = this;
        int size = this.size();
        for (int i = 1; i < array.length; ++i) {
            array[i] = Structure.newInstance(this.getClass());
            array[i].useMemory(this.memory.share(i * size, size));
            array[i].read();
        }
        return array;
    }

    public Structure[] toArray(int size) {
        return this.toArray((Structure[])Array.newInstance(this.getClass(), size));
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Structure && ((Structure)o).size() == this.size() && (o.getClass().isAssignableFrom(this.getClass()) || this.getClass().isAssignableFrom(o.getClass()))) {
            Structure s = (Structure)o;
            Pointer p1 = this.getPointer();
            Pointer p2 = s.getPointer();
            for (int i = 0; i < this.size(); ++i) {
                if (p1.getByte(i) == p2.getByte(i)) continue;
                return false;
            }
            return true;
        }
        return false;
    }

    public int hashCode() {
        Pointer p = this.getPointer();
        return p != null ? p.hashCode() : 0;
    }

    Pointer getTypeInfo() {
        Pointer p = Structure.getTypeInfo(this);
        this.typeInfo = p.peer;
        return p;
    }

    public void setAutoSynch(boolean auto) {
        this.setAutoRead(auto);
        this.setAutoWrite(auto);
    }

    public void setAutoRead(boolean auto) {
        this.autoRead = auto;
    }

    public boolean getAutoRead() {
        return this.autoRead;
    }

    public void setAutoWrite(boolean auto) {
        this.autoWrite = auto;
    }

    public boolean getAutoWrite() {
        return this.autoWrite;
    }

    static Pointer getTypeInfo(Object obj) {
        return FFIType.get(obj);
    }

    public static Structure newInstance(Class type) throws IllegalArgumentException {
        try {
            Structure s = (Structure)type.newInstance();
            if (s instanceof ByValue) {
                s.allocateMemory();
            }
            return s;
        } catch (InstantiationException e) {
            String msg = "Can't instantiate " + type + " (" + e + ")";
            throw new IllegalArgumentException(msg);
        } catch (IllegalAccessException e) {
            String msg = "Instantiation of " + type + " not allowed, is it public? (" + e + ")";
            throw new IllegalArgumentException(msg);
        }
    }

    static {
        Field[] fields = MemberOrder.class.getFields();
        REVERSE_FIELDS = "last".equals(fields[0].getName());
        REQUIRES_FIELD_ORDER = !"middle".equals(fields[1].getName());
        String arch = System.getProperty("os.arch").toLowerCase();
        isPPC = "ppc".equals(arch) || "powerpc".equals(arch);
        isSPARC = "sparc".equals(arch);
        MAX_GNUC_ALIGNMENT = isSPARC ? 8 : Native.LONG_SIZE;
        reading = new HashSet();
        writing = new HashSet();
    }

    private static class FFIType
    extends Structure {
        private static Map typeInfoMap = new WeakHashMap();
        private static final int FFI_TYPE_STRUCT = 13;
        public size_t size;
        public short alignment;
        public short type = (short)13;
        public Pointer elements;

        private FFIType(Structure ref) {
            Pointer[] els = new Pointer[ref.fields().size() + 1];
            int idx = 0;
            Iterator i = ref.fields().values().iterator();
            while (i.hasNext()) {
                StructField sf = (StructField)i.next();
                els[idx++] = FFIType.get(ref.getField(sf), sf.type);
            }
            this.init(els);
        }

        private FFIType(Object array, Class type) {
            int length = Array.getLength(array);
            Pointer[] els = new Pointer[length + 1];
            Pointer p = FFIType.get(null, type.getComponentType());
            for (int i = 0; i < length; ++i) {
                els[i] = p;
            }
            this.init(els);
        }

        private void init(Pointer[] els) {
            this.elements = new Memory(Pointer.SIZE * els.length);
            this.elements.write(0L, els, 0, els.length);
            this.write();
        }

        static Pointer get(Object obj) {
            if (obj == null) {
                return FFITypes.ffi_type_pointer;
            }
            return FFIType.get(obj, obj.getClass());
        }

        private static Pointer get(Object obj, Class cls) {
            Map map = typeInfoMap;
            synchronized (map) {
                Object o = typeInfoMap.get(cls);
                if (o instanceof Pointer) {
                    return (Pointer)o;
                }
                if (o instanceof FFIType) {
                    return ((FFIType)o).getPointer();
                }
                if ((class$java$nio$Buffer == null ? (class$java$nio$Buffer = Structure.class$("java.nio.Buffer")) : class$java$nio$Buffer).isAssignableFrom(cls) || (class$com$sun$jna$Callback == null ? (class$com$sun$jna$Callback = Structure.class$("com.sun.jna.Callback")) : class$com$sun$jna$Callback).isAssignableFrom(cls)) {
                    typeInfoMap.put(cls, FFITypes.ffi_type_pointer);
                    return FFITypes.ffi_type_pointer;
                }
                if ((class$com$sun$jna$Structure == null ? (class$com$sun$jna$Structure = Structure.class$("com.sun.jna.Structure")) : class$com$sun$jna$Structure).isAssignableFrom(cls)) {
                    if (obj == null) {
                        obj = FFIType.newInstance(cls);
                    }
                    if ((class$com$sun$jna$Structure$ByReference == null ? (class$com$sun$jna$Structure$ByReference = Structure.class$("com.sun.jna.Structure$ByReference")) : class$com$sun$jna$Structure$ByReference).isAssignableFrom(cls)) {
                        typeInfoMap.put(cls, FFITypes.ffi_type_pointer);
                        return FFITypes.ffi_type_pointer;
                    }
                    if ((class$com$sun$jna$Union == null ? (class$com$sun$jna$Union = Structure.class$("com.sun.jna.Union")) : class$com$sun$jna$Union).isAssignableFrom(cls)) {
                        return ((Union)obj).getTypeInfo();
                    }
                    FFIType type = new FFIType((Structure)obj);
                    typeInfoMap.put(cls, type);
                    return type.getPointer();
                }
                if ((class$com$sun$jna$NativeMapped == null ? (class$com$sun$jna$NativeMapped = Structure.class$("com.sun.jna.NativeMapped")) : class$com$sun$jna$NativeMapped).isAssignableFrom(cls)) {
                    NativeMappedConverter c = NativeMappedConverter.getInstance(cls);
                    return FFIType.get(c.toNative(obj, new ToNativeContext()), c.nativeType());
                }
                if (cls.isArray()) {
                    FFIType type = new FFIType(obj, cls);
                    typeInfoMap.put(obj, type);
                    return type.getPointer();
                }
                throw new IllegalArgumentException("Unsupported structure field type " + cls);
            }
        }

        static {
            if (Native.POINTER_SIZE == 0) {
                throw new Error("Native library not initialized");
            }
            if (FFITypes.ffi_type_void == null) {
                throw new Error("FFI types not initialized");
            }
            typeInfoMap.put(Void.TYPE, FFITypes.ffi_type_void);
            typeInfoMap.put(class$java$lang$Void == null ? (class$java$lang$Void = Structure.class$("java.lang.Void")) : class$java$lang$Void, FFITypes.ffi_type_void);
            typeInfoMap.put(Float.TYPE, FFITypes.ffi_type_float);
            typeInfoMap.put(class$java$lang$Float == null ? (class$java$lang$Float = Structure.class$("java.lang.Float")) : class$java$lang$Float, FFITypes.ffi_type_float);
            typeInfoMap.put(Double.TYPE, FFITypes.ffi_type_double);
            typeInfoMap.put(class$java$lang$Double == null ? (class$java$lang$Double = Structure.class$("java.lang.Double")) : class$java$lang$Double, FFITypes.ffi_type_double);
            typeInfoMap.put(Long.TYPE, FFITypes.ffi_type_sint64);
            typeInfoMap.put(class$java$lang$Long == null ? (class$java$lang$Long = Structure.class$("java.lang.Long")) : class$java$lang$Long, FFITypes.ffi_type_sint64);
            typeInfoMap.put(Integer.TYPE, FFITypes.ffi_type_sint32);
            typeInfoMap.put(class$java$lang$Integer == null ? (class$java$lang$Integer = Structure.class$("java.lang.Integer")) : class$java$lang$Integer, FFITypes.ffi_type_sint32);
            typeInfoMap.put(Short.TYPE, FFITypes.ffi_type_sint16);
            typeInfoMap.put(class$java$lang$Short == null ? (class$java$lang$Short = Structure.class$("java.lang.Short")) : class$java$lang$Short, FFITypes.ffi_type_sint16);
            Pointer ctype = Native.WCHAR_SIZE == 2 ? FFITypes.ffi_type_uint16 : FFITypes.ffi_type_uint32;
            typeInfoMap.put(Character.TYPE, ctype);
            typeInfoMap.put(class$java$lang$Character == null ? (class$java$lang$Character = Structure.class$("java.lang.Character")) : class$java$lang$Character, ctype);
            typeInfoMap.put(Byte.TYPE, FFITypes.ffi_type_sint8);
            typeInfoMap.put(class$java$lang$Byte == null ? (class$java$lang$Byte = Structure.class$("java.lang.Byte")) : class$java$lang$Byte, FFITypes.ffi_type_sint8);
            typeInfoMap.put(class$com$sun$jna$Pointer == null ? (class$com$sun$jna$Pointer = Structure.class$("com.sun.jna.Pointer")) : class$com$sun$jna$Pointer, FFITypes.ffi_type_pointer);
            typeInfoMap.put(class$java$lang$String == null ? (class$java$lang$String = Structure.class$("java.lang.String")) : class$java$lang$String, FFITypes.ffi_type_pointer);
            typeInfoMap.put(class$com$sun$jna$WString == null ? (class$com$sun$jna$WString = Structure.class$("com.sun.jna.WString")) : class$com$sun$jna$WString, FFITypes.ffi_type_pointer);
        }

        private static class FFITypes {
            private static Pointer ffi_type_void;
            private static Pointer ffi_type_float;
            private static Pointer ffi_type_double;
            private static Pointer ffi_type_longdouble;
            private static Pointer ffi_type_uint8;
            private static Pointer ffi_type_sint8;
            private static Pointer ffi_type_uint16;
            private static Pointer ffi_type_sint16;
            private static Pointer ffi_type_uint32;
            private static Pointer ffi_type_sint32;
            private static Pointer ffi_type_uint64;
            private static Pointer ffi_type_sint64;
            private static Pointer ffi_type_pointer;

            private FFITypes() {
            }
        }

        public static class size_t
        extends IntegerType {
            public size_t() {
                this(0L);
            }

            public size_t(long value) {
                super(Native.POINTER_SIZE, value);
            }
        }
    }

    class StructField {
        public String name;
        public Class type;
        public Field field;
        public int size = -1;
        public int offset = -1;
        public boolean isVolatile;
        public FromNativeConverter readConverter;
        public ToNativeConverter writeConverter;
        public FromNativeContext context;

        StructField() {
        }
    }

    private static class MemberOrder {
        public int first;
        public int middle;
        public int last;

        private MemberOrder() {
        }
    }

    public static interface ByReference {
    }

    public static interface ByValue {
    }
}


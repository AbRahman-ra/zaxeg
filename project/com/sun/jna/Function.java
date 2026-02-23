/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  [Lcom.sun.jna.Pointer;
 *  [Lcom.sun.jna.Structure;
 *  [Lcom.sun.jna.WString;
 *  [Ljava.lang.String;
 */
package com.sun.jna;

import [Lcom.sun.jna.Pointer;;
import [Lcom.sun.jna.Structure;;
import [Lcom.sun.jna.WString;;
import [Ljava.lang.String;;
import com.sun.jna.Callback;
import com.sun.jna.CallbackReference;
import com.sun.jna.FromNativeConverter;
import com.sun.jna.FunctionParameterContext;
import com.sun.jna.FunctionResultContext;
import com.sun.jna.Memory;
import com.sun.jna.MethodParameterContext;
import com.sun.jna.MethodResultContext;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.NativeMapped;
import com.sun.jna.NativeMappedConverter;
import com.sun.jna.NativeString;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.Structure;
import com.sun.jna.ToNativeConverter;
import com.sun.jna.TypeMapper;
import com.sun.jna.WString;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

public class Function
extends Pointer {
    public static final int MAX_NARGS = 256;
    public static final int C_CONVENTION = 0;
    public static final int ALT_CONVENTION = 1;
    static final Integer INTEGER_TRUE = new Integer(-1);
    static final Integer INTEGER_FALSE = new Integer(0);
    private NativeLibrary library;
    private String functionName;
    private int callingConvention;
    static final String OPTION_INVOKING_METHOD = "invoking-method";
    static /* synthetic */ Class array$Lcom$sun$jna$Structure$ByReference;

    public static Function getFunction(String libraryName, String functionName) {
        return NativeLibrary.getInstance(libraryName).getFunction(functionName);
    }

    public static Function getFunction(String libraryName, String functionName, int callConvention) {
        return NativeLibrary.getInstance(libraryName).getFunction(functionName, callConvention);
    }

    Function(NativeLibrary library, String functionName, int callingConvention) {
        this.checkCallingConvention(callingConvention);
        if (functionName == null) {
            throw new NullPointerException("Function name must not be null");
        }
        this.library = library;
        this.functionName = functionName;
        this.callingConvention = callingConvention;
        try {
            this.peer = library.getSymbolAddress(functionName);
        } catch (UnsatisfiedLinkError e) {
            throw new UnsatisfiedLinkError("Error looking up function '" + functionName + "': " + e.getMessage());
        }
    }

    Function(Pointer functionAddress, int callingConvention) {
        this.checkCallingConvention(callingConvention);
        if (functionAddress == null || functionAddress.peer == 0L) {
            throw new NullPointerException("Function address may not be null");
        }
        this.functionName = functionAddress.toString();
        this.callingConvention = callingConvention;
        this.peer = functionAddress.peer;
    }

    private void checkCallingConvention(int convention) throws IllegalArgumentException {
        switch (convention) {
            case 0: 
            case 1: {
                break;
            }
            default: {
                throw new IllegalArgumentException("Unrecognized calling convention: " + convention);
            }
        }
    }

    public String getName() {
        return this.functionName;
    }

    public int getCallingConvention() {
        return this.callingConvention;
    }

    public Object invoke(Class returnType, Object[] inArgs) {
        return this.invoke(returnType, inArgs, Collections.EMPTY_MAP);
    }

    public Object invoke(Class returnType, Object[] inArgs, Map options) {
        Object[] args = new Object[]{};
        if (inArgs != null) {
            if (inArgs.length > 256) {
                throw new UnsupportedOperationException("Maximum argument count is 256");
            }
            args = new Object[inArgs.length];
            System.arraycopy(inArgs, 0, args, 0, args.length);
        }
        TypeMapper mapper = (TypeMapper)options.get("type-mapper");
        Method invokingMethod = (Method)options.get(OPTION_INVOKING_METHOD);
        for (int i = 0; i < args.length; ++i) {
            args[i] = this.convertArgument(args, i, invokingMethod, mapper);
        }
        Class nativeType = returnType;
        FromNativeConverter resultConverter = null;
        if (NativeMapped.class.isAssignableFrom(returnType)) {
            NativeMappedConverter tc = NativeMappedConverter.getInstance(returnType);
            resultConverter = tc;
            nativeType = tc.nativeType();
        } else if (mapper != null && (resultConverter = mapper.getFromNativeConverter(returnType)) != null) {
            nativeType = resultConverter.nativeType();
        }
        Object result = this.invoke(args, nativeType);
        if (resultConverter != null) {
            FunctionResultContext context = invokingMethod != null ? new MethodResultContext(returnType, this, inArgs, invokingMethod) : new FunctionResultContext(returnType, this, inArgs);
            result = resultConverter.fromNative(result, context);
        }
        if (inArgs != null) {
            for (int i = 0; i < inArgs.length; ++i) {
                Object inArg = inArgs[i];
                if (inArg == null) continue;
                if (inArg instanceof Structure) {
                    if (inArg instanceof Structure.ByValue || !((Structure)inArg).getAutoRead()) continue;
                    ((Structure)inArg).read();
                    continue;
                }
                if (args[i] instanceof PostCallRead) {
                    ((PostCallRead)args[i]).read();
                    if (!(args[i] instanceof PointerArray)) continue;
                    PointerArray array = (PointerArray)args[i];
                    if (!(array$Lcom$sun$jna$Structure$ByReference == null ? Function.class$("[Lcom.sun.jna.Structure$ByReference;") : array$Lcom$sun$jna$Structure$ByReference).isAssignableFrom(inArg.getClass())) continue;
                    Class<?> type = inArg.getClass().getComponentType();
                    Structure[] ss = (Structure[])inArg;
                    for (int si = 0; si < ss.length; ++si) {
                        Pointer p = array.getPointer(Pointer.SIZE * si);
                        ss[si] = Structure.updateStructureByReference(type, ss[si], p);
                    }
                    continue;
                }
                if (!(array$Lcom$sun$jna$Structure == null ? Function.class$("[Lcom.sun.jna.Structure;") : array$Lcom$sun$jna$Structure).isAssignableFrom(inArg.getClass())) continue;
                Structure[] ss = (Structure[])inArg;
                for (int si = 0; si < ss.length; ++si) {
                    if (!ss[si].getAutoRead()) continue;
                    ss[si].read();
                }
            }
        }
        return result;
    }

    Object invoke(Object[] args, Class returnType) {
        Object result = null;
        if (returnType == null || returnType == Void.TYPE || returnType == Void.class) {
            this.invokeVoid(this.callingConvention, args);
            result = null;
        } else if (returnType == Boolean.TYPE || returnType == Boolean.class) {
            result = Function.valueOf(this.invokeInt(this.callingConvention, args) != 0);
        } else if (returnType == Byte.TYPE || returnType == Byte.class) {
            result = new Byte((byte)this.invokeInt(this.callingConvention, args));
        } else if (returnType == Short.TYPE || returnType == Short.class) {
            result = new Short((short)this.invokeInt(this.callingConvention, args));
        } else if (returnType == Character.TYPE || returnType == Character.class) {
            result = new Character((char)this.invokeInt(this.callingConvention, args));
        } else if (returnType == Integer.TYPE || returnType == Integer.class) {
            result = new Integer(this.invokeInt(this.callingConvention, args));
        } else if (returnType == Long.TYPE || returnType == Long.class) {
            result = new Long(this.invokeLong(this.callingConvention, args));
        } else if (returnType == Float.TYPE || returnType == Float.class) {
            result = new Float(this.invokeFloat(this.callingConvention, args));
        } else if (returnType == Double.TYPE || returnType == Double.class) {
            result = new Double(this.invokeDouble(this.callingConvention, args));
        } else if (returnType == String.class) {
            result = this.invokeString(this.callingConvention, args, false);
        } else if (returnType == WString.class) {
            String s = this.invokeString(this.callingConvention, args, true);
            if (s != null) {
                result = new WString(s);
            }
        } else if (Pointer.class.isAssignableFrom(returnType)) {
            result = this.invokePointer(this.callingConvention, args);
        } else if (Structure.class.isAssignableFrom(returnType)) {
            if (Structure.ByValue.class.isAssignableFrom(returnType)) {
                Structure s = this.invokeStructure(this.callingConvention, args, Structure.newInstance(returnType));
                if (s.getAutoRead()) {
                    s.read();
                }
                result = s;
            } else {
                result = this.invokePointer(this.callingConvention, args);
                if (result != null) {
                    Structure s = Structure.newInstance(returnType);
                    s.useMemory((Pointer)result);
                    if (s.getAutoRead()) {
                        s.read();
                    }
                    result = s;
                }
            }
        } else if (Callback.class.isAssignableFrom(returnType)) {
            result = this.invokePointer(this.callingConvention, args);
            if (result != null) {
                result = CallbackReference.getCallback(returnType, (Pointer)result);
            }
        } else if (returnType == String;.class) {
            Pointer p = this.invokePointer(this.callingConvention, args);
            if (p != null) {
                result = p.getStringArray(0L);
            }
        } else if (returnType == WString;.class) {
            Pointer p = this.invokePointer(this.callingConvention, args);
            if (p != null) {
                String[] arr = p.getStringArray(0L, true);
                WString[] warr = new WString[arr.length];
                for (int i = 0; i < arr.length; ++i) {
                    warr[i] = new WString(arr[i]);
                }
                result = warr;
            }
        } else if (returnType == Pointer;.class) {
            Pointer p = this.invokePointer(this.callingConvention, args);
            if (p != null) {
                result = p.getPointerArray(0L);
            }
        } else {
            throw new IllegalArgumentException("Unsupported return type " + returnType + " in function " + this.getName());
        }
        return result;
    }

    private Object convertArgument(Object[] args, int index, Method invokingMethod, TypeMapper mapper) {
        Object arg = args[index];
        if (arg != null) {
            Class<?> type = arg.getClass();
            ToNativeConverter converter = null;
            if (NativeMapped.class.isAssignableFrom(type)) {
                converter = NativeMappedConverter.getInstance(type);
            } else if (mapper != null) {
                converter = mapper.getToNativeConverter(type);
            }
            if (converter != null) {
                FunctionParameterContext context = invokingMethod != null ? new MethodParameterContext(this, args, index, invokingMethod) : new FunctionParameterContext(this, args, index);
                arg = converter.toNative(arg, context);
            }
        }
        if (arg == null || this.isPrimitiveArray(arg.getClass())) {
            return arg;
        }
        Class<?> argClass = arg.getClass();
        if (arg instanceof Structure) {
            Structure struct = (Structure)arg;
            if (struct.getAutoWrite()) {
                struct.write();
            }
            if (struct instanceof Structure.ByValue) {
                Class<?> ptype = struct.getClass();
                if (invokingMethod != null) {
                    Class<?>[] ptypes = invokingMethod.getParameterTypes();
                    if (Function.isVarArgs(invokingMethod)) {
                        if (index < ptypes.length - 1) {
                            ptype = ptypes[index];
                        } else {
                            Class<?> etype = ptypes[ptypes.length - 1].getComponentType();
                            if (etype != Object.class) {
                                ptype = etype;
                            }
                        }
                    } else {
                        ptype = ptypes[index];
                    }
                }
                if (Structure.ByValue.class.isAssignableFrom(ptype)) {
                    return struct;
                }
            }
            return struct.getPointer();
        }
        if (arg instanceof Callback) {
            return CallbackReference.getFunctionPointer((Callback)arg);
        }
        if (arg instanceof String) {
            return new NativeString((String)arg, false).getPointer();
        }
        if (arg instanceof WString) {
            return new NativeString(arg.toString(), true).getPointer();
        }
        if (arg instanceof Boolean) {
            return Boolean.TRUE.equals(arg) ? INTEGER_TRUE : INTEGER_FALSE;
        }
        if (String;.class == argClass) {
            return new StringArray((String[])arg);
        }
        if (WString;.class == argClass) {
            return new StringArray((WString[])arg);
        }
        if (Pointer;.class == argClass) {
            return new PointerArray((Pointer[])arg);
        }
        if (Structure;.class.isAssignableFrom(argClass)) {
            Class<?> type;
            Structure[] ss = (Structure[])arg;
            boolean byRef = Structure.ByReference.class.isAssignableFrom(type = argClass.getComponentType());
            if (byRef) {
                Pointer[] pointers = new Pointer[ss.length + 1];
                for (int i = 0; i < ss.length; ++i) {
                    pointers[i] = ss[i] != null ? ss[i].getPointer() : null;
                }
                return new PointerArray(pointers);
            }
            if (ss.length == 0) {
                throw new IllegalArgumentException("Structure array must have non-zero length");
            }
            if (ss[0] == null) {
                Structure struct = Structure.newInstance(type);
                int size = struct.size();
                Memory m = new Memory(size * ss.length);
                struct.useMemory(m);
                Structure[] tmp = struct.toArray(ss.length);
                for (int si = 0; si < ss.length; ++si) {
                    ss[si] = tmp[si];
                }
                return ss[0].getPointer();
            }
            Pointer base = ss[0].getPointer();
            int size = ss[0].size();
            if (ss[0].getAutoWrite()) {
                ss[0].write();
            }
            for (int si = 1; si < ss.length; ++si) {
                if (ss[si].getPointer().peer != base.peer + (long)(size * si)) {
                    String msg = "Structure array elements must use contiguous memory (at element index " + si + ")";
                    throw new IllegalArgumentException(msg);
                }
                if (!ss[si].getAutoWrite()) continue;
                ss[si].write();
            }
            return base;
        }
        if (argClass.isArray()) {
            throw new IllegalArgumentException("Unsupported array argument type: " + argClass.getComponentType());
        }
        if (arg != null && !Native.isSupportedNativeType(arg.getClass())) {
            throw new IllegalArgumentException("Unsupported argument type " + arg.getClass().getName() + " at parameter " + index + " of function " + this.getName());
        }
        return arg;
    }

    private boolean isPrimitiveArray(Class argClass) {
        return argClass.isArray() && argClass.getComponentType().isPrimitive();
    }

    private native int invokeInt(int var1, Object[] var2);

    private native long invokeLong(int var1, Object[] var2);

    public void invoke(Object[] args) {
        this.invoke(Void.class, args);
    }

    private native void invokeVoid(int var1, Object[] var2);

    private native float invokeFloat(int var1, Object[] var2);

    private native double invokeDouble(int var1, Object[] var2);

    private String invokeString(int callingConvention, Object[] args, boolean wide) {
        Pointer ptr = this.invokePointer(callingConvention, args);
        String s = null;
        if (ptr != null) {
            s = wide ? ptr.getString(0L, wide) : ptr.getString(0L);
        }
        return s;
    }

    private native Pointer invokePointer(int var1, Object[] var2);

    private native Structure invokeStructure(int var1, Object[] var2, Structure var3);

    public String toString() {
        if (this.library != null) {
            return "native function " + this.functionName + "(" + this.library.getName() + ")@0x" + Long.toHexString(this.peer);
        }
        return "native function@0x" + Long.toHexString(this.peer);
    }

    public Pointer invokePointer(Object[] args) {
        return (Pointer)this.invoke(Pointer.class, args);
    }

    public String invokeString(Object[] args, boolean wide) {
        Class clazz = wide ? WString.class : String.class;
        Object o = this.invoke(clazz, args);
        return o != null ? o.toString() : null;
    }

    public int invokeInt(Object[] args) {
        return (Integer)this.invoke(Integer.class, args);
    }

    public long invokeLong(Object[] args) {
        return (Long)this.invoke(Long.class, args);
    }

    public float invokeFloat(Object[] args) {
        return ((Float)this.invoke(Float.class, args)).floatValue();
    }

    public double invokeDouble(Object[] args) {
        return (Double)this.invoke(Double.class, args);
    }

    public void invokeVoid(Object[] args) {
        this.invoke(Void.class, args);
    }

    public boolean equals(Object o) {
        if (o instanceof Function) {
            Function other = (Function)o;
            return other.callingConvention == this.callingConvention && other.peer == this.peer;
        }
        return false;
    }

    static Object[] concatenateVarArgs(Object[] inArgs) {
        if (inArgs != null && inArgs.length > 0) {
            Class<?> argType;
            Object lastArg = inArgs[inArgs.length - 1];
            Class<?> clazz = argType = lastArg != null ? lastArg.getClass() : null;
            if (argType != null && argType.isArray()) {
                Object[] varArgs = (Object[])lastArg;
                Object[] fullArgs = new Object[inArgs.length + varArgs.length];
                System.arraycopy(inArgs, 0, fullArgs, 0, inArgs.length - 1);
                System.arraycopy(varArgs, 0, fullArgs, inArgs.length - 1, varArgs.length);
                fullArgs[fullArgs.length - 1] = null;
                inArgs = fullArgs;
            }
        }
        return inArgs;
    }

    static boolean isVarArgs(Method m) {
        try {
            Method v = m.getClass().getMethod("isVarArgs", new Class[0]);
            return Boolean.TRUE.equals(v.invoke(m, new Object[0]));
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException invocationTargetException) {
            // empty catch block
        }
        return false;
    }

    static Boolean valueOf(boolean b) {
        return b ? Boolean.TRUE : Boolean.FALSE;
    }

    private static class PointerArray
    extends Memory
    implements PostCallRead {
        private Pointer[] original;

        public PointerArray(Pointer[] arg) {
            super(Pointer.SIZE * (arg.length + 1));
            this.original = arg;
            for (int i = 0; i < arg.length; ++i) {
                this.setPointer(i * Pointer.SIZE, arg[i]);
            }
            this.setPointer(Pointer.SIZE * arg.length, null);
        }

        public void read() {
            for (int i = 0; i < this.original.length; ++i) {
                this.original[i] = this.getPointer(i * Pointer.SIZE);
            }
        }
    }

    public static interface PostCallRead {
        public void read();
    }
}


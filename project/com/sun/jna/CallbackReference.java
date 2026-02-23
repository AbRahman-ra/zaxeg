/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  [Lcom.sun.jna.WString;
 *  [Ljava.lang.Object;
 *  [Ljava.lang.String;
 */
package com.sun.jna;

import [Lcom.sun.jna.WString;;
import [Ljava.lang.Object;;
import [Ljava.lang.String;;
import com.sun.jna.AltCallingConvention;
import com.sun.jna.Callback;
import com.sun.jna.CallbackParameterContext;
import com.sun.jna.CallbackProxy;
import com.sun.jna.CallbackResultContext;
import com.sun.jna.FromNativeConverter;
import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeMapped;
import com.sun.jna.NativeMappedConverter;
import com.sun.jna.NativeString;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.Structure;
import com.sun.jna.ToNativeConverter;
import com.sun.jna.TypeMapper;
import com.sun.jna.WString;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

class CallbackReference
extends WeakReference {
    static final Map callbackMap = new WeakHashMap();
    static final Map altCallbackMap = new WeakHashMap();
    private static final Map allocations = new WeakHashMap();
    private static final Method PROXY_CALLBACK_METHOD;
    Pointer cbstruct;
    CallbackProxy proxy;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Callback getCallback(Class type, Pointer p) {
        if (p != null) {
            Map map;
            if (!type.isInterface()) {
                throw new IllegalArgumentException("Callback type must be an interface");
            }
            Map map2 = map = AltCallingConvention.class.isAssignableFrom(type) ? altCallbackMap : callbackMap;
            synchronized (map2) {
                Iterator i = map.keySet().iterator();
                while (i.hasNext()) {
                    CallbackReference cbref;
                    Pointer cbp;
                    Callback cb = (Callback)i.next();
                    if (!type.isAssignableFrom(cb.getClass()) || !p.equals(cbp = (cbref = (CallbackReference)map.get(cb)) != null ? cbref.getTrampoline() : CallbackReference.getNativeFunctionPointer(cb))) continue;
                    return cb;
                }
                int ctype = AltCallingConvention.class.isAssignableFrom(type) ? 1 : 0;
                Map options = Native.getLibraryOptions(type);
                NativeFunctionHandler h = new NativeFunctionHandler(p, ctype, options);
                Callback cb = (Callback)Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type, NativeFunctionProxy.class}, h);
                h.options.put("invoking-method", CallbackReference.getCallbackMethod(cb));
                map.put(cb, null);
                return cb;
            }
        }
        return null;
    }

    private CallbackReference(Callback callback, int callingConvention) {
        super(callback);
        TypeMapper mapper = Native.getTypeMapper(Native.findCallbackClass(callback.getClass()));
        this.proxy = callback instanceof CallbackProxy ? (CallbackProxy)callback : new DefaultCallbackProxy(CallbackReference.getCallbackMethod(callback), mapper);
        Class[] nativeParamTypes = this.proxy.getParameterTypes();
        Class returnType = this.proxy.getReturnType();
        if (mapper != null) {
            for (int i = 0; i < nativeParamTypes.length; ++i) {
                FromNativeConverter rc = mapper.getFromNativeConverter(nativeParamTypes[i]);
                if (rc == null) continue;
                nativeParamTypes[i] = rc.nativeType();
            }
            ToNativeConverter tn = mapper.getToNativeConverter(returnType);
            if (tn != null) {
                returnType = tn.nativeType();
            }
        }
        for (int i = 0; i < nativeParamTypes.length; ++i) {
            nativeParamTypes[i] = this.getNativeType(nativeParamTypes[i]);
            if (CallbackReference.isAllowableNativeType(nativeParamTypes[i])) continue;
            String msg = "Callback argument " + nativeParamTypes[i] + " requires custom type conversion";
            throw new IllegalArgumentException(msg);
        }
        if (!CallbackReference.isAllowableNativeType(returnType = this.getNativeType(returnType))) {
            String msg = "Callback return type " + returnType + " requires custom type conversion";
            throw new IllegalArgumentException(msg);
        }
        this.cbstruct = CallbackReference.createNativeCallback(this.proxy, PROXY_CALLBACK_METHOD, nativeParamTypes, returnType, callingConvention);
    }

    private Class getNativeType(Class cls) {
        if (Structure.class.isAssignableFrom(cls)) {
            Structure.newInstance(cls);
            if (!Structure.ByValue.class.isAssignableFrom(cls)) {
                return Pointer.class;
            }
        } else {
            if (NativeMapped.class.isAssignableFrom(cls)) {
                return NativeMappedConverter.getInstance(cls).nativeType();
            }
            if (cls == String.class || cls == WString.class || cls == String;.class || cls == WString;.class || Callback.class.isAssignableFrom(cls)) {
                return Pointer.class;
            }
        }
        return cls;
    }

    private static Method checkMethod(Method m) {
        if (m.getParameterTypes().length > 256) {
            String msg = "Method signature exceeds the maximum parameter count: " + m;
            throw new IllegalArgumentException(msg);
        }
        return m;
    }

    private static Method getCallbackMethod(Callback callback) {
        Class cls = Native.findCallbackClass(callback.getClass());
        Method[] pubMethods = cls.getDeclaredMethods();
        Method[] classMethods = cls.getMethods();
        HashSet<Method> pmethods = new HashSet<Method>(Arrays.asList(pubMethods));
        pmethods.retainAll(Arrays.asList(classMethods));
        Iterator i = pmethods.iterator();
        while (i.hasNext()) {
            Method m = (Method)i.next();
            if (!Callback.FORBIDDEN_NAMES.contains(m.getName())) continue;
            i.remove();
        }
        Method[] methods = pmethods.toArray(new Method[pmethods.size()]);
        if (methods.length == 1) {
            return CallbackReference.checkMethod(methods[0]);
        }
        for (int i2 = 0; i2 < methods.length; ++i2) {
            Method m = methods[i2];
            if (!"callback".equals(m.getName())) continue;
            return CallbackReference.checkMethod(m);
        }
        String msg = "Callback must implement a single public method, or one public method named 'callback'";
        throw new IllegalArgumentException(msg);
    }

    public Pointer getTrampoline() {
        return this.cbstruct.getPointer(0L);
    }

    protected void finalize() {
        CallbackReference.freeNativeCallback(this.cbstruct.peer);
        this.cbstruct.peer = 0L;
    }

    private Callback getCallback() {
        return (Callback)this.get();
    }

    private static Pointer getNativeFunctionPointer(Callback cb) {
        if (cb instanceof NativeFunctionProxy) {
            NativeFunctionHandler handler = (NativeFunctionHandler)Proxy.getInvocationHandler(cb);
            return handler.getPointer();
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Pointer getFunctionPointer(Callback cb) {
        Map map;
        Pointer fp = null;
        if (cb == null) {
            return null;
        }
        fp = CallbackReference.getNativeFunctionPointer(cb);
        if (fp != null) {
            return fp;
        }
        int callingConvention = cb instanceof AltCallingConvention ? 1 : 0;
        Map map2 = map = callingConvention == 1 ? altCallbackMap : callbackMap;
        synchronized (map2) {
            CallbackReference cbref = (CallbackReference)map.get(cb);
            if (cbref == null) {
                cbref = new CallbackReference(cb, callingConvention);
                map.put(cb, cbref);
            }
            return cbref.getTrampoline();
        }
    }

    private static boolean isAllowableNativeType(Class cls) {
        return cls == Void.TYPE || cls == Void.class || cls == Boolean.TYPE || cls == Boolean.class || cls == Byte.TYPE || cls == Byte.class || cls == Short.TYPE || cls == Short.class || cls == Character.TYPE || cls == Character.class || cls == Integer.TYPE || cls == Integer.class || cls == Long.TYPE || cls == Long.class || cls == Float.TYPE || cls == Float.class || cls == Double.TYPE || cls == Double.class || Structure.ByValue.class.isAssignableFrom(cls) && Structure.class.isAssignableFrom(cls) || Pointer.class.isAssignableFrom(cls);
    }

    private static synchronized native Pointer createNativeCallback(CallbackProxy var0, Method var1, Class[] var2, Class var3, int var4);

    private static synchronized native void freeNativeCallback(long var0);

    static {
        try {
            PROXY_CALLBACK_METHOD = CallbackProxy.class.getMethod("callback", Object;.class);
        } catch (Exception e) {
            throw new Error("Error looking up CallbackProxy.callback() method");
        }
    }

    private static class NativeFunctionHandler
    implements InvocationHandler {
        private Function function;
        private Map options;

        public NativeFunctionHandler(Pointer address, int callingConvention, Map libOptions) {
            this.function = new Function(address, callingConvention){

                public String getName() {
                    String str = super.getName();
                    if (NativeFunctionHandler.this.options.containsKey("invoking-method")) {
                        Method m = (Method)NativeFunctionHandler.this.options.get("invoking-method");
                        Class cls = Native.findCallbackClass(m.getDeclaringClass());
                        str = str + " (" + cls.getName() + ")";
                    }
                    return str;
                }
            };
            this.options = new HashMap();
            if (libOptions != null) {
                this.options.putAll(libOptions);
            }
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (Library.Handler.OBJECT_TOSTRING.equals(method)) {
                return "Proxy interface to " + this.function;
            }
            if (Library.Handler.OBJECT_HASHCODE.equals(method)) {
                return new Integer(this.hashCode());
            }
            if (Library.Handler.OBJECT_EQUALS.equals(method)) {
                Object o = args[0];
                if (o != null && Proxy.isProxyClass(o.getClass())) {
                    return Function.valueOf(Proxy.getInvocationHandler(o) == this);
                }
                return Boolean.FALSE;
            }
            if (Function.isVarArgs(method)) {
                args = Function.concatenateVarArgs(args);
            }
            return this.function.invoke(method.getReturnType(), args, this.options);
        }

        public Pointer getPointer() {
            return this.function;
        }
    }

    private class DefaultCallbackProxy
    implements CallbackProxy {
        private Method callbackMethod;
        private ToNativeConverter toNative;
        private FromNativeConverter[] fromNative;

        public DefaultCallbackProxy(Method callbackMethod, TypeMapper mapper) {
            this.callbackMethod = callbackMethod;
            Class<?>[] argTypes = callbackMethod.getParameterTypes();
            Class<?> returnType = callbackMethod.getReturnType();
            this.fromNative = new FromNativeConverter[argTypes.length];
            if ((class$com$sun$jna$NativeMapped == null ? (class$com$sun$jna$NativeMapped = CallbackReference.class$("com.sun.jna.NativeMapped")) : class$com$sun$jna$NativeMapped).isAssignableFrom(returnType)) {
                this.toNative = NativeMappedConverter.getInstance(returnType);
            } else if (mapper != null) {
                this.toNative = mapper.getToNativeConverter(returnType);
            }
            for (int i = 0; i < this.fromNative.length; ++i) {
                if ((class$com$sun$jna$NativeMapped == null ? CallbackReference.class$("com.sun.jna.NativeMapped") : class$com$sun$jna$NativeMapped).isAssignableFrom(argTypes[i])) {
                    this.fromNative[i] = new NativeMappedConverter(argTypes[i]);
                    continue;
                }
                if (mapper == null) continue;
                this.fromNative[i] = mapper.getFromNativeConverter(argTypes[i]);
            }
            if (!callbackMethod.isAccessible()) {
                try {
                    callbackMethod.setAccessible(true);
                } catch (SecurityException e) {
                    throw new IllegalArgumentException("Callback method is inaccessible, make sure the interface is public: " + callbackMethod);
                }
            }
        }

        private Object callback_inner(Object[] args) {
            Class<?>[] paramTypes = this.callbackMethod.getParameterTypes();
            Object[] callbackArgs = new Object[args.length];
            for (int i = 0; i < args.length; ++i) {
                Class<?> type = paramTypes[i];
                Object arg = args[i];
                if (this.fromNative[i] != null) {
                    CallbackParameterContext context = new CallbackParameterContext(type, this.callbackMethod, args, i);
                    arg = this.fromNative[i].fromNative(arg, context);
                }
                callbackArgs[i] = this.convertArgument(arg, type);
            }
            Object result = null;
            Callback cb = CallbackReference.this.getCallback();
            if (cb != null) {
                try {
                    result = this.convertResult(this.callbackMethod.invoke(cb, callbackArgs));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        public Object callback(Object[] args) {
            try {
                return this.callback_inner(args);
            } catch (Throwable t) {
                t.printStackTrace();
                return null;
            }
        }

        private Object convertArgument(Object value, Class dstType) {
            if (value instanceof Pointer) {
                if (dstType == (class$java$lang$String == null ? (class$java$lang$String = CallbackReference.class$("java.lang.String")) : class$java$lang$String)) {
                    value = ((Pointer)value).getString(0L);
                } else if (dstType == (class$com$sun$jna$WString == null ? (class$com$sun$jna$WString = CallbackReference.class$("com.sun.jna.WString")) : class$com$sun$jna$WString)) {
                    value = new WString(((Pointer)value).getString(0L, true));
                } else if (dstType == (array$Ljava$lang$String == null ? (array$Ljava$lang$String = CallbackReference.class$("[Ljava.lang.String;")) : array$Ljava$lang$String) || dstType == (array$Lcom$sun$jna$WString == null ? (array$Lcom$sun$jna$WString = CallbackReference.class$("[Lcom.sun.jna.WString;")) : array$Lcom$sun$jna$WString)) {
                    value = ((Pointer)value).getStringArray(0L, dstType == (array$Lcom$sun$jna$WString == null ? (array$Lcom$sun$jna$WString = CallbackReference.class$("[Lcom.sun.jna.WString;")) : array$Lcom$sun$jna$WString));
                } else if ((class$com$sun$jna$Callback == null ? (class$com$sun$jna$Callback = CallbackReference.class$("com.sun.jna.Callback")) : class$com$sun$jna$Callback).isAssignableFrom(dstType)) {
                    value = CallbackReference.getCallback(dstType, (Pointer)value);
                } else if ((class$com$sun$jna$Structure == null ? (class$com$sun$jna$Structure = CallbackReference.class$("com.sun.jna.Structure")) : class$com$sun$jna$Structure).isAssignableFrom(dstType)) {
                    Structure s = Structure.newInstance(dstType);
                    Pointer old = s.getPointer();
                    s.useMemory((Pointer)value);
                    s.read();
                    if ((class$com$sun$jna$Structure$ByValue == null ? (class$com$sun$jna$Structure$ByValue = CallbackReference.class$("com.sun.jna.Structure$ByValue")) : class$com$sun$jna$Structure$ByValue).isAssignableFrom(dstType)) {
                        s.useMemory(old);
                        s.write();
                    }
                    value = s;
                }
            } else if ((Boolean.TYPE == dstType || (class$java$lang$Boolean == null ? (class$java$lang$Boolean = CallbackReference.class$("java.lang.Boolean")) : class$java$lang$Boolean) == dstType) && value instanceof Number) {
                value = Function.valueOf(((Number)value).intValue() != 0);
            }
            return value;
        }

        private Object convertResult(Object value) {
            Class<?> cls;
            if (this.toNative != null) {
                value = this.toNative.toNative(value, new CallbackResultContext(this.callbackMethod));
            }
            if (value == null) {
                return null;
            }
            if ((class$com$sun$jna$Structure == null ? (class$com$sun$jna$Structure = CallbackReference.class$("com.sun.jna.Structure")) : class$com$sun$jna$Structure).isAssignableFrom(cls = value.getClass())) {
                if ((class$com$sun$jna$Structure$ByValue == null ? (class$com$sun$jna$Structure$ByValue = CallbackReference.class$("com.sun.jna.Structure$ByValue")) : class$com$sun$jna$Structure$ByValue).isAssignableFrom(cls)) {
                    return value;
                }
                return ((Structure)value).getPointer();
            }
            if (cls == Boolean.TYPE || cls == (class$java$lang$Boolean == null ? (class$java$lang$Boolean = CallbackReference.class$("java.lang.Boolean")) : class$java$lang$Boolean)) {
                return Boolean.TRUE.equals(value) ? Function.INTEGER_TRUE : Function.INTEGER_FALSE;
            }
            if (cls == (class$java$lang$String == null ? (class$java$lang$String = CallbackReference.class$("java.lang.String")) : class$java$lang$String) || cls == (class$com$sun$jna$WString == null ? (class$com$sun$jna$WString = CallbackReference.class$("com.sun.jna.WString")) : class$com$sun$jna$WString)) {
                NativeString ns = new NativeString(value.toString(), cls == (class$com$sun$jna$WString == null ? (class$com$sun$jna$WString = CallbackReference.class$("com.sun.jna.WString")) : class$com$sun$jna$WString));
                allocations.put(value, ns);
                return ns.getPointer();
            }
            if (cls == (array$Ljava$lang$String == null ? (array$Ljava$lang$String = CallbackReference.class$("[Ljava.lang.String;")) : array$Ljava$lang$String) || cls == (class$com$sun$jna$WString == null ? (class$com$sun$jna$WString = CallbackReference.class$("com.sun.jna.WString")) : class$com$sun$jna$WString)) {
                StringArray sa = cls == (array$Ljava$lang$String == null ? (array$Ljava$lang$String = CallbackReference.class$("[Ljava.lang.String;")) : array$Ljava$lang$String) ? new StringArray((String[])value) : new StringArray((WString[])value);
                allocations.put(value, sa);
                return sa;
            }
            if ((class$com$sun$jna$Callback == null ? (class$com$sun$jna$Callback = CallbackReference.class$("com.sun.jna.Callback")) : class$com$sun$jna$Callback).isAssignableFrom(cls)) {
                return CallbackReference.getFunctionPointer((Callback)value);
            }
            return value;
        }

        public Class[] getParameterTypes() {
            return this.callbackMethod.getParameterTypes();
        }

        public Class getReturnType() {
            return this.callbackMethod.getReturnType();
        }
    }

    private static interface NativeFunctionProxy {
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sun.jna;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.NativeLibrary;
import com.sun.jna.NativeMapped;
import com.sun.jna.NativeMappedConverter;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.TypeMapper;
import com.sun.jna.WString;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Window;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class Native {
    private static Map typeMappers = new WeakHashMap();
    private static Map alignments = new WeakHashMap();
    private static Map options = new WeakHashMap();
    private static Map libraries = new WeakHashMap();
    public static final int POINTER_SIZE;
    public static final int LONG_SIZE;
    public static final int WCHAR_SIZE;
    private static final ThreadLocal lastError;

    private Native() {
    }

    private static native void initIDs();

    public static synchronized native void setProtected(boolean var0);

    public static synchronized native boolean isProtected();

    public static synchronized native void setPreserveLastError(boolean var0);

    public static synchronized native boolean getPreserveLastError();

    public static long getWindowID(Window w) throws HeadlessException {
        return Native.getComponentID(w);
    }

    public static long getComponentID(Component c) throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException("No native windows when headless");
        }
        if (c.isLightweight()) {
            throw new IllegalArgumentException("Component must be heavyweight");
        }
        if (!c.isDisplayable()) {
            throw new IllegalStateException("Component must be displayable");
        }
        if (Platform.isX11() && System.getProperty("java.version").startsWith("1.4") && !c.isVisible()) {
            throw new IllegalStateException("Component must be visible");
        }
        return Native.getWindowHandle0(c);
    }

    public static Pointer getWindowPointer(Window w) throws HeadlessException {
        return Native.getComponentPointer(w);
    }

    public static Pointer getComponentPointer(Component c) throws HeadlessException {
        return new Pointer(Native.getComponentID(c));
    }

    private static native long getWindowHandle0(Component var0);

    public static Pointer getByteBufferPointer(ByteBuffer b) {
        return Native.getDirectBufferPointer(b);
    }

    public static native Pointer getDirectBufferPointer(Buffer var0);

    public static String toString(byte[] buf) {
        int term;
        String encoding = System.getProperty("jna.encoding");
        String s = null;
        if (encoding != null) {
            try {
                s = new String(buf, encoding);
            } catch (UnsupportedEncodingException e) {
                // empty catch block
            }
        }
        if (s == null) {
            s = new String(buf);
        }
        if ((term = s.indexOf(0)) != -1) {
            s = s.substring(0, term);
        }
        return s;
    }

    public static String toString(char[] buf) {
        String s = new String(buf);
        int term = s.indexOf(0);
        if (term != -1) {
            s = s.substring(0, term);
        }
        return s;
    }

    public static Object loadLibrary(String name, Class interfaceClass) {
        return Native.loadLibrary(name, interfaceClass, Collections.EMPTY_MAP);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Object loadLibrary(String name, Class interfaceClass, Map libOptions) {
        Library.Handler handler = new Library.Handler(name, interfaceClass, libOptions);
        ClassLoader loader = interfaceClass.getClassLoader();
        Library proxy = (Library)Proxy.newProxyInstance(loader, new Class[]{interfaceClass}, handler);
        Map map = libraries;
        synchronized (map) {
            if (!libOptions.isEmpty()) {
                options.put(interfaceClass, libOptions);
            }
            if (libOptions.containsKey("type-mapper")) {
                typeMappers.put(interfaceClass, libOptions.get("type-mapper"));
            }
            if (libOptions.containsKey("structure-alignment")) {
                alignments.put(interfaceClass, libOptions.get("structure-alignment"));
            }
            libraries.put(interfaceClass, new WeakReference<Library>(proxy));
        }
        return proxy;
    }

    private static void loadLibraryInstance(Class cls) {
        if (cls != null && !libraries.containsKey(cls)) {
            try {
                Field[] fields = cls.getFields();
                for (int i = 0; i < fields.length; ++i) {
                    Field field = fields[i];
                    if (field.getType() != cls || !Modifier.isStatic(field.getModifiers())) continue;
                    libraries.put(cls, new WeakReference<Object>(field.get(null)));
                    break;
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not access instance of " + cls + " (" + e + ")");
            }
        }
    }

    static Class findCallbackClass(Class type) {
        if (!Callback.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(type.getName() + " is not derived from com.sun.jna.Callback");
        }
        if (type.isInterface()) {
            return type;
        }
        Class<?>[] ifaces = type.getInterfaces();
        for (int i = 0; i < ifaces.length; ++i) {
            if (!(class$com$sun$jna$Callback == null ? Native.class$("com.sun.jna.Callback") : class$com$sun$jna$Callback).isAssignableFrom(ifaces[i])) continue;
            if (ifaces[i].getMethods().length != 1) break;
            return ifaces[i];
        }
        if (Callback.class.isAssignableFrom(type.getSuperclass())) {
            return Native.findCallbackClass(type.getSuperclass());
        }
        return type;
    }

    static Class findEnclosingLibraryClass(Class cls) {
        Class fromDeclaring;
        if (cls == null) {
            return null;
        }
        if (Library.class.isAssignableFrom(cls)) {
            return cls;
        }
        if (Callback.class.isAssignableFrom(cls)) {
            cls = Native.findCallbackClass(cls);
        }
        if ((fromDeclaring = Native.findEnclosingLibraryClass(cls.getDeclaringClass())) != null) {
            return fromDeclaring;
        }
        return Native.findEnclosingLibraryClass(cls.getSuperclass());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Map getLibraryOptions(Class type) {
        Map map = libraries;
        synchronized (map) {
            Class interfaceClass = Native.findEnclosingLibraryClass(type);
            if (interfaceClass != null) {
                Native.loadLibraryInstance(interfaceClass);
            } else {
                interfaceClass = type;
            }
            if (!options.containsKey(interfaceClass)) {
                try {
                    Field field = interfaceClass.getField("OPTIONS");
                    field.setAccessible(true);
                    options.put(interfaceClass, field.get(null));
                } catch (NoSuchFieldException e) {
                } catch (Exception e) {
                    throw new IllegalArgumentException("OPTIONS must be a public field of type java.util.Map (" + e + "): " + interfaceClass);
                }
            }
            return (Map)options.get(interfaceClass);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static TypeMapper getTypeMapper(Class cls) {
        Map map = libraries;
        synchronized (map) {
            Class interfaceClass = Native.findEnclosingLibraryClass(cls);
            if (interfaceClass != null) {
                Native.loadLibraryInstance(interfaceClass);
            } else {
                interfaceClass = cls;
            }
            if (!typeMappers.containsKey(interfaceClass)) {
                try {
                    Field field = interfaceClass.getField("TYPE_MAPPER");
                    field.setAccessible(true);
                    typeMappers.put(interfaceClass, field.get(null));
                } catch (NoSuchFieldException e) {
                    Map options = Native.getLibraryOptions(cls);
                    if (options != null && options.containsKey("type-mapper")) {
                        typeMappers.put(interfaceClass, options.get("type-mapper"));
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("TYPE_MAPPER must be a public field of type " + TypeMapper.class.getName() + " (" + e + "): " + interfaceClass);
                }
            }
            return (TypeMapper)typeMappers.get(interfaceClass);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int getStructureAlignment(Class cls) {
        Map map = libraries;
        synchronized (map) {
            Integer value;
            Class interfaceClass = Native.findEnclosingLibraryClass(cls);
            if (interfaceClass != null) {
                Native.loadLibraryInstance(interfaceClass);
            } else {
                interfaceClass = cls;
            }
            if (!alignments.containsKey(interfaceClass)) {
                try {
                    Field field = interfaceClass.getField("STRUCTURE_ALIGNMENT");
                    field.setAccessible(true);
                    alignments.put(interfaceClass, field.get(null));
                } catch (NoSuchFieldException e) {
                    Map options = Native.getLibraryOptions(interfaceClass);
                    if (options != null && options.containsKey("structure-alignment")) {
                        alignments.put(interfaceClass, options.get("structure-alignment"));
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("STRUCTURE_ALIGNMENT must be a public field of type int (" + e + "): " + interfaceClass);
                }
            }
            int n = (value = (Integer)alignments.get(interfaceClass)) != null ? value : 0;
            return n;
        }
    }

    static byte[] getBytes(String s) {
        String encoding = System.getProperty("jna.encoding");
        if (encoding != null) {
            try {
                return s.getBytes(encoding);
            } catch (UnsupportedEncodingException unsupportedEncodingException) {
                // empty catch block
            }
        }
        return s.getBytes();
    }

    public static byte[] toByteArray(String s) {
        byte[] bytes = Native.getBytes(s);
        byte[] buf = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, buf, 0, bytes.length);
        return buf;
    }

    public static char[] toCharArray(String s) {
        char[] chars = s.toCharArray();
        char[] buf = new char[chars.length + 1];
        System.arraycopy(chars, 0, buf, 0, chars.length);
        return buf;
    }

    private static String getNativeLibraryResourcePath() {
        String osPrefix;
        String arch = System.getProperty("os.arch").toLowerCase();
        if (Platform.isWindows()) {
            osPrefix = "win32-" + arch;
        } else if (Platform.isMac()) {
            osPrefix = "darwin";
        } else if (Platform.isLinux()) {
            if ("x86".equals(arch)) {
                arch = "i386";
            } else if ("x86_64".equals(arch)) {
                arch = "amd64";
            }
            osPrefix = "linux-" + arch;
        } else if (Platform.isSolaris()) {
            osPrefix = "sunos-" + arch;
        } else {
            osPrefix = System.getProperty("os.name").toLowerCase();
            int space = osPrefix.indexOf(" ");
            if (space != -1) {
                osPrefix = osPrefix.substring(0, space);
            }
            osPrefix = osPrefix + "-" + arch;
        }
        return "/com/sun/jna/" + osPrefix;
    }

    private static void loadNativeLibrary() {
        String libName = "jnidispatch";
        String bootPath = System.getProperty("jna.boot.library.path");
        if (bootPath != null) {
            String[] dirs = bootPath.split(File.pathSeparator);
            for (int i = 0; i < dirs.length; ++i) {
                String path = new File(new File(dirs[i]), System.mapLibraryName(libName)).getAbsolutePath();
                try {
                    System.load(path);
                    return;
                } catch (UnsatisfiedLinkError ex) {
                    String ext;
                    String orig;
                    if (!Platform.isMac()) continue;
                    if (path.endsWith("dylib")) {
                        orig = "dylib";
                        ext = "jnilib";
                    } else {
                        orig = "jnilib";
                        ext = "dylib";
                    }
                    try {
                        System.load(path.substring(0, path.lastIndexOf(orig)) + ext);
                        return;
                    } catch (UnsatisfiedLinkError ex2) {
                        // empty catch block
                    }
                    continue;
                }
            }
        }
        try {
            System.loadLibrary(libName);
        } catch (UnsatisfiedLinkError e) {
            Native.loadNativeLibraryFromJar();
        }
    }

    private static void loadNativeLibraryFromJar() {
        String resourceName;
        String libname = System.mapLibraryName("jnidispatch");
        URL url = Native.class.getResource(resourceName = Native.getNativeLibraryResourcePath() + "/" + libname);
        if (url == null && Platform.isMac() && resourceName.endsWith(".dylib")) {
            resourceName = resourceName.substring(0, resourceName.lastIndexOf(".dylib")) + ".jnilib";
            url = Native.class.getResource(resourceName);
        }
        if (url == null) {
            throw new UnsatisfiedLinkError("jnidispatch (" + resourceName + ") not found in resource path");
        }
        File lib = null;
        if (url.getProtocol().toLowerCase().equals("file")) {
            lib = new File(URLDecoder.decode(url.getPath()));
        } else {
            InputStream is = Native.class.getResourceAsStream(resourceName);
            if (is == null) {
                throw new Error("Can't obtain jnidispatch InputStream");
            }
            FileOutputStream fos = null;
            try {
                int count;
                lib = File.createTempFile("jna", null);
                lib.deleteOnExit();
                if (Platform.deleteNativeLibraryAfterVMExit()) {
                    Runtime.getRuntime().addShutdownHook(new DeleteNativeLibrary(lib));
                }
                fos = new FileOutputStream(lib);
                byte[] buf = new byte[1024];
                while ((count = is.read(buf, 0, buf.length)) > 0) {
                    fos.write(buf, 0, count);
                }
            } catch (IOException e) {
                throw new Error("Failed to create temporary file for jnidispatch library: " + e);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {}
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {}
                }
            }
        }
        System.load(lib.getAbsolutePath());
    }

    private static native int pointerSize();

    private static native int longSize();

    private static native int wideCharSize();

    private static native String getNativeVersion();

    private static native String getAPIChecksum();

    public static int getLastError() {
        return (Integer)lastError.get();
    }

    public static native void setLastError(int var0);

    static void updateLastError(int e) {
        lastError.set(new Integer(e));
    }

    public static Library synchronizedLibrary(final Library library) {
        Class<?> cls = library.getClass();
        if (!Proxy.isProxyClass(cls)) {
            throw new IllegalArgumentException("Library must be a proxy class");
        }
        InvocationHandler ih = Proxy.getInvocationHandler(library);
        if (!(ih instanceof Library.Handler)) {
            throw new IllegalArgumentException("Unrecognized proxy handler: " + ih);
        }
        final Library.Handler handler = (Library.Handler)ih;
        InvocationHandler newHandler = new InvocationHandler(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                NativeLibrary nativeLibrary = handler.getNativeLibrary();
                synchronized (nativeLibrary) {
                    return handler.invoke(library, method, args);
                }
            }
        };
        return (Library)Proxy.newProxyInstance(cls.getClassLoader(), cls.getInterfaces(), newHandler);
    }

    public static String getWebStartLibraryPath(String libName) {
        if (System.getProperty("javawebstart.version") == null) {
            return null;
        }
        try {
            ClassLoader cl = Native.class.getClassLoader();
            Method m = ClassLoader.class.getDeclaredMethod("findLibrary", String.class);
            m.setAccessible(true);
            String libpath = (String)m.invoke(cl, libName);
            if (libpath != null) {
                return new File(libpath).getParent();
            }
            String msg = "Library '" + libName + "' was not found by class loader " + cl;
            throw new IllegalArgumentException(msg);
        } catch (Exception e) {
            return null;
        }
    }

    public static int getNativeSize(Class cls) {
        if (NativeMapped.class.isAssignableFrom(cls)) {
            cls = NativeMappedConverter.getInstance(cls).nativeType();
        }
        if (cls == Boolean.TYPE || cls == Boolean.class) {
            return 4;
        }
        if (cls == Byte.TYPE || cls == Byte.class) {
            return 1;
        }
        if (cls == Short.TYPE || cls == Short.class) {
            return 2;
        }
        if (cls == Character.TYPE || cls == Character.class) {
            return WCHAR_SIZE;
        }
        if (cls == Integer.TYPE || cls == Integer.class) {
            return 4;
        }
        if (cls == Long.TYPE || cls == Long.class) {
            return 8;
        }
        if (cls == Float.TYPE || cls == Float.class) {
            return 4;
        }
        if (cls == Double.TYPE || cls == Double.class) {
            return 8;
        }
        if (Structure.class.isAssignableFrom(cls)) {
            if (Structure.ByValue.class.isAssignableFrom(cls)) {
                return Structure.newInstance(cls).size();
            }
            return POINTER_SIZE;
        }
        if (Pointer.class.isAssignableFrom(cls) || Buffer.class.isAssignableFrom(cls) || Callback.class.isAssignableFrom(cls) || String.class == cls || WString.class == cls) {
            return POINTER_SIZE;
        }
        throw new IllegalArgumentException("Native size for type \"" + cls.getName() + "\" is unknown");
    }

    public static boolean isSupportedNativeType(Class cls) {
        if (Structure.class.isAssignableFrom(cls)) {
            return true;
        }
        try {
            return Native.getNativeSize(cls) != 0;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static void main(String[] args) {
        String version;
        String DEFAULT_TITLE = "Java Native Access (JNA)";
        String UNKNOWN_VERSION = "unknown - package information missing";
        Package pkg = Native.class.getPackage();
        String title = pkg.getSpecificationTitle();
        if (title == null) {
            title = "Java Native Access (JNA)";
        }
        if ((version = pkg.getSpecificationVersion()) == null) {
            version = "unknown - package information missing";
        }
        title = title + " API Version " + version;
        System.out.println(title);
        version = pkg.getImplementationVersion();
        if (version == null) {
            version = "unknown - package information missing";
        }
        System.out.println("Version: " + version);
        System.out.println(" Native: " + Native.getNativeVersion() + " (" + Native.getAPIChecksum() + ")");
        System.exit(0);
    }

    static {
        Native.loadNativeLibrary();
        POINTER_SIZE = Native.pointerSize();
        LONG_SIZE = Native.longSize();
        WCHAR_SIZE = Native.wideCharSize();
        Native.initIDs();
        if (Boolean.getBoolean("jna.protected")) {
            Native.setProtected(true);
        }
        lastError = new ThreadLocal(){

            protected synchronized Object initialValue() {
                return new Integer(0);
            }
        };
    }

    public static class DeleteNativeLibrary
    extends Thread {
        private File file;

        public DeleteNativeLibrary(File file) {
            this.file = file;
        }

        private boolean unload(String path) {
            try {
                ClassLoader cl = this.getClass().getClassLoader();
                Field f = (class$java$lang$ClassLoader == null ? (class$java$lang$ClassLoader = Native.class$("java.lang.ClassLoader")) : class$java$lang$ClassLoader).getDeclaredField("nativeLibraries");
                f.setAccessible(true);
                List libs = (List)f.get(cl);
                Iterator i = libs.iterator();
                while (i.hasNext()) {
                    Object lib = i.next();
                    f = lib.getClass().getDeclaredField("name");
                    f.setAccessible(true);
                    String name = (String)f.get(lib);
                    if (!name.equals(path)) continue;
                    Method m = lib.getClass().getDeclaredMethod("finalize", new Class[0]);
                    m.setAccessible(true);
                    m.invoke(lib, new Object[0]);
                    return true;
                }
            } catch (Exception exception) {
                // empty catch block
            }
            return false;
        }

        public void run() {
            if (!this.unload(this.file.getAbsolutePath()) || !this.file.delete()) {
                try {
                    Runtime.getRuntime().exec(new String[]{System.getProperty("java.home") + "/bin/java", "-cp", System.getProperty("java.class.path"), this.getClass().getName(), this.file.getAbsolutePath()});
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public static void main(String[] args) {
            File file;
            if (args.length == 1 && (file = new File(args[0])).exists()) {
                long start = System.currentTimeMillis();
                while (!file.delete() && file.exists()) {
                    try {
                        Thread.sleep(10L);
                    } catch (InterruptedException e) {
                        // empty catch block
                    }
                    if (System.currentTimeMillis() - start <= 5000L) continue;
                    System.err.println("Could not remove temp file: " + file.getAbsolutePath());
                    break;
                }
            }
            System.exit(0);
        }
    }
}


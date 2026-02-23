/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sun.jna;

import com.sun.jna.Function;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class NativeLibrary {
    private long handle;
    private final String libraryName;
    private final String libraryPath;
    private final Map functions = new HashMap();
    private static final Map libraries = new HashMap();
    private static final Map searchPaths = Collections.synchronizedMap(new HashMap());
    private static final List librarySearchPath = new LinkedList();
    private static final List userSearchPath = new LinkedList();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private NativeLibrary(String libraryName, String libraryPath, long handle) {
        this.libraryName = this.getLibraryName(libraryName);
        this.libraryPath = libraryPath;
        this.handle = handle;
        if (Platform.isWindows() && "kernel32".equals(this.libraryName.toLowerCase())) {
            Map map = this.functions;
            synchronized (map) {
                Function f = new Function(this, "GetLastError", 1){

                    Object invoke(Object[] args, Class returnType) {
                        return new Integer(Native.getLastError());
                    }
                };
                this.functions.put("GetLastError", f);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static NativeLibrary loadLibrary(String libraryName) {
        long handle;
        String libraryPath;
        block23: {
            List customPaths;
            LinkedList<String> searchPath = new LinkedList<String>();
            String webstartPath = Native.getWebStartLibraryPath(libraryName);
            if (webstartPath != null) {
                searchPath.add(webstartPath);
            }
            if ((customPaths = (List)searchPaths.get(libraryName)) != null) {
                List list = customPaths;
                synchronized (list) {
                    searchPath.addAll(0, customPaths);
                }
            }
            searchPath.addAll(userSearchPath);
            libraryPath = NativeLibrary.findLibraryPath(libraryName, searchPath);
            handle = 0L;
            try {
                handle = NativeLibrary.open(libraryPath);
            } catch (UnsatisfiedLinkError e) {
                searchPath.addAll(librarySearchPath);
            }
            try {
                if (handle == 0L) {
                    libraryPath = NativeLibrary.findLibraryPath(libraryName, searchPath);
                    handle = NativeLibrary.open(libraryPath);
                }
            } catch (UnsatisfiedLinkError e) {
                if (Platform.isLinux()) {
                    libraryPath = NativeLibrary.matchLibrary(libraryName, searchPath);
                    if (libraryPath != null) {
                        try {
                            handle = NativeLibrary.open(libraryPath);
                        } catch (UnsatisfiedLinkError e2) {
                            e = e2;
                        }
                    }
                } else if (Platform.isMac() && !libraryName.endsWith(".dylib")) {
                    libraryPath = "/System/Library/Frameworks/" + libraryName + ".framework/" + libraryName;
                    if (new File(libraryPath).exists()) {
                        try {
                            handle = NativeLibrary.open(libraryPath);
                        } catch (UnsatisfiedLinkError e2) {
                            e = e2;
                        }
                    }
                } else if (Platform.isWindows()) {
                    libraryPath = NativeLibrary.findLibraryPath("lib" + libraryName, searchPath);
                    try {
                        handle = NativeLibrary.open(libraryPath);
                    } catch (UnsatisfiedLinkError e2) {
                        e = e2;
                    }
                }
                if (handle != 0L) break block23;
                throw new UnsatisfiedLinkError("Unable to load library '" + libraryName + "': " + e.getMessage());
            }
        }
        return new NativeLibrary(libraryName, libraryPath, handle);
    }

    private String getLibraryName(String libraryName) {
        String suffix;
        int suffixStart;
        String simplified = libraryName;
        String BASE = "---";
        String template = NativeLibrary.mapLibraryName("---");
        int prefixEnd = template.indexOf("---");
        if (prefixEnd > 0 && simplified.startsWith(template.substring(0, prefixEnd))) {
            simplified = simplified.substring(prefixEnd);
        }
        if ((suffixStart = simplified.indexOf(suffix = template.substring(prefixEnd + "---".length()))) != -1) {
            simplified = simplified.substring(0, suffixStart);
        }
        return simplified;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final NativeLibrary getInstance(String libraryName) {
        if (libraryName == null) {
            throw new NullPointerException("Library name may not be null");
        }
        Map map = libraries;
        synchronized (map) {
            NativeLibrary library;
            WeakReference<NativeLibrary> ref = (WeakReference<NativeLibrary>)libraries.get(libraryName);
            NativeLibrary nativeLibrary = library = ref != null ? (NativeLibrary)ref.get() : null;
            if (library == null) {
                library = NativeLibrary.loadLibrary(libraryName);
                ref = new WeakReference<NativeLibrary>(library);
                libraries.put(library.getName(), ref);
                libraries.put(library.getFile().getAbsolutePath(), ref);
                libraries.put(library.getFile().getName(), ref);
            }
            return library;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void addSearchPath(String libraryName, String path) {
        Map map = searchPaths;
        synchronized (map) {
            List<String> customPaths = (List<String>)searchPaths.get(libraryName);
            if (customPaths == null) {
                customPaths = Collections.synchronizedList(new LinkedList());
                searchPaths.put(libraryName, customPaths);
            }
            customPaths.add(path);
        }
    }

    public Function getFunction(String functionName) {
        return this.getFunction(functionName, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Function getFunction(String functionName, int callingConvention) {
        if (functionName == null) {
            throw new NullPointerException("Function name may not be null");
        }
        Map map = this.functions;
        synchronized (map) {
            Function function = (Function)this.functions.get(functionName);
            if (function == null) {
                function = new Function(this, functionName, callingConvention);
                this.functions.put(functionName, function);
            }
            return function;
        }
    }

    public Pointer getGlobalVariableAddress(String symbolName) {
        try {
            return new Pointer(this.getSymbolAddress(symbolName));
        } catch (UnsatisfiedLinkError e) {
            throw new UnsatisfiedLinkError("Error looking up '" + symbolName + "': " + e.getMessage());
        }
    }

    long getSymbolAddress(String name) {
        if (this.handle == 0L) {
            throw new UnsatisfiedLinkError("Library has been unloaded");
        }
        return NativeLibrary.findSymbol(this.handle, name);
    }

    public String toString() {
        return "Native Library <" + this.libraryPath + "@" + this.handle + ">";
    }

    public String getName() {
        return this.libraryName;
    }

    public File getFile() {
        return new File(this.libraryPath);
    }

    protected void finalize() {
        this.dispose();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void dispose() {
        Object object = libraries;
        synchronized (object) {
            libraries.remove(this.getName());
            libraries.remove(this.getFile().getAbsolutePath());
            libraries.remove(this.getFile().getName());
        }
        object = this;
        synchronized (object) {
            if (this.handle != 0L) {
                NativeLibrary.close(this.handle);
                this.handle = 0L;
            }
        }
    }

    private static List initPaths(String key) {
        String value = System.getProperty(key, "");
        if ("".equals(value)) {
            return Collections.EMPTY_LIST;
        }
        StringTokenizer st = new StringTokenizer(value, File.pathSeparator);
        ArrayList<String> list = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String path = st.nextToken();
            if ("".equals(path)) continue;
            list.add(path);
        }
        return list;
    }

    private static String findLibraryPath(String libName, List searchPath) {
        if (new File(libName).isAbsolute()) {
            return libName;
        }
        String name = NativeLibrary.mapLibraryName(libName);
        Iterator it = searchPath.iterator();
        while (it.hasNext()) {
            File file = new File(new File((String)it.next()), name);
            if (!file.exists()) continue;
            return file.getAbsolutePath();
        }
        return name;
    }

    private static String mapLibraryName(String libName) {
        if (Platform.isMac()) {
            if (libName.startsWith("lib") && (libName.endsWith(".dylib") || libName.endsWith(".jnilib"))) {
                return libName;
            }
            String name = System.mapLibraryName(libName);
            if (name.endsWith(".jnilib")) {
                return name.substring(0, name.lastIndexOf(".jnilib")) + ".dylib";
            }
            return name;
        }
        if (Platform.isLinux() && NativeLibrary.isVersionedName(libName)) {
            return libName;
        }
        return System.mapLibraryName(libName);
    }

    private static boolean isVersionedName(String name) {
        int so;
        if (name.startsWith("lib") && (so = name.lastIndexOf(".so.")) != -1 && so + 4 < name.length()) {
            for (int i = so + 4; i < name.length(); ++i) {
                char ch = name.charAt(i);
                if (Character.isDigit(ch) || ch == '.') continue;
                return false;
            }
            return true;
        }
        return false;
    }

    static String matchLibrary(final String libName, List searchPath) {
        File lib = new File(libName);
        if (lib.isAbsolute()) {
            searchPath = Arrays.asList(lib.getParent());
        }
        FilenameFilter filter = new FilenameFilter(){

            public boolean accept(File dir, String filename) {
                return (filename.startsWith("lib" + libName + ".so") || filename.startsWith(libName + ".so") && libName.startsWith("lib")) && NativeLibrary.isVersionedName(filename);
            }
        };
        LinkedList<File> matches = new LinkedList<File>();
        Iterator<String> it = searchPath.iterator();
        while (it.hasNext()) {
            File[] files = new File(it.next()).listFiles(filter);
            if (files == null || files.length <= 0) continue;
            matches.addAll(Arrays.asList(files));
        }
        double bestVersion = -1.0;
        String bestMatch = null;
        Iterator it2 = matches.iterator();
        while (it2.hasNext()) {
            String path = ((File)it2.next()).getAbsolutePath();
            String ver = path.substring(path.lastIndexOf(".so.") + 4);
            double version = NativeLibrary.parseVersion(ver);
            if (!(version > bestVersion)) continue;
            bestVersion = version;
            bestMatch = path;
        }
        return bestMatch;
    }

    static double parseVersion(String ver) {
        double v = 0.0;
        double divisor = 1.0;
        int dot = ver.indexOf(".");
        while (ver != null) {
            String num;
            if (dot != -1) {
                num = ver.substring(0, dot);
                ver = ver.substring(dot + 1);
                dot = ver.indexOf(".");
            } else {
                num = ver;
                ver = null;
            }
            try {
                v += (double)Integer.parseInt(num) / divisor;
            } catch (NumberFormatException e) {
                return 0.0;
            }
            divisor *= 100.0;
        }
        return v;
    }

    private static native long open(String var0);

    private static native void close(long var0);

    private static native long findSymbol(long var0, String var2);

    static {
        if (Native.POINTER_SIZE == 0) {
            throw new Error("Native library not initialized");
        }
        userSearchPath.addAll(NativeLibrary.initPaths("jna.library.path"));
        String webstartPath = Native.getWebStartLibraryPath("jnidispatch");
        if (webstartPath != null) {
            librarySearchPath.add(webstartPath);
        }
        if (System.getProperty("jna.platform.library.path") == null && !Platform.isWindows()) {
            String platformPath = "";
            String sep = "";
            String archPath = "";
            if (Platform.isLinux() || Platform.isSolaris() || Platform.isFreeBSD()) {
                archPath = (Platform.isSolaris() ? "/" : "") + Pointer.SIZE * 8;
            }
            String[] paths = new String[]{"/usr/lib" + archPath, "/lib" + archPath, "/usr/lib", "/lib"};
            if (Platform.isLinux() && Pointer.SIZE == 8) {
                paths = new String[]{"/usr/lib" + archPath, "/lib" + archPath};
            }
            for (int i = 0; i < paths.length; ++i) {
                File dir = new File(paths[i]);
                if (!dir.exists() || !dir.isDirectory()) continue;
                platformPath = platformPath + sep + paths[i];
                sep = File.pathSeparator;
            }
            if (!"".equals(platformPath)) {
                System.setProperty("jna.platform.library.path", platformPath);
            }
        }
        librarySearchPath.addAll(NativeLibrary.initPaths("jna.platform.library.path"));
    }
}


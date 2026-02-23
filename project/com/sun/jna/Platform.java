/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sun.jna;

public final class Platform {
    private static final int UNSPECIFIED = -1;
    private static final int MAC = 0;
    private static final int LINUX = 1;
    private static final int WINDOWS = 2;
    private static final int SOLARIS = 3;
    private static final int FREEBSD = 4;
    private static final int OPENBSD = 5;
    private static final int WINDOWSCE = 6;
    private static final int osType;

    private Platform() {
    }

    public static final boolean isMac() {
        return osType == 0;
    }

    public static final boolean isLinux() {
        return osType == 1;
    }

    public static final boolean isWindowsCE() {
        return osType == 6;
    }

    public static final boolean isWindows() {
        return osType == 2 || osType == 6;
    }

    public static final boolean isSolaris() {
        return osType == 3;
    }

    public static final boolean isFreeBSD() {
        return osType == 4;
    }

    public static final boolean isOpenBSD() {
        return osType == 5;
    }

    public static final boolean isX11() {
        return !Platform.isWindows() && !Platform.isMac();
    }

    public static final boolean deleteNativeLibraryAfterVMExit() {
        return osType == 2;
    }

    public static final boolean hasRuntimeExec() {
        return !Platform.isWindowsCE() || !"J9".equals(System.getProperty("java.vm.name"));
    }

    static {
        String osName = System.getProperty("os.name");
        osType = osName.startsWith("Linux") ? 1 : (osName.startsWith("Mac") || osName.startsWith("Darwin") ? 0 : (osName.startsWith("Windows CE") ? 6 : (osName.startsWith("Windows") ? 2 : (osName.startsWith("Solaris") || osName.startsWith("SunOS") ? 3 : (osName.startsWith("FreeBSD") ? 4 : (osName.startsWith("OpenBSD") ? 5 : -1))))));
    }
}


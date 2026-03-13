/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

public abstract class CopyOptions {
    public static final int ALL_NAMESPACES = 2;
    public static final int TYPE_ANNOTATIONS = 4;
    public static final int FOR_UPDATE = 8;

    public static boolean includes(int options, int option) {
        return (options & option) == option;
    }

    public static int getStartDocumentProperties(int copyOptions) {
        return CopyOptions.includes(copyOptions, 8) ? 32768 : 0;
    }
}


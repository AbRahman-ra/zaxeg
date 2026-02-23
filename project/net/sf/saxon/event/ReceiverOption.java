/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

public class ReceiverOption {
    public static final int NONE = 0;
    public static final int DISABLE_ESCAPING = 1;
    public static final int DISABLE_CHARACTER_MAPS = 2;
    public static final int NO_SPECIAL_CHARS = 4;
    public static final int DEFAULTED_VALUE = 8;
    public static final int NILLED_ELEMENT = 16;
    public static final int REJECT_DUPLICATES = 32;
    public static final int NAMESPACE_OK = 64;
    public static final int DISINHERIT_NAMESPACES = 128;
    public static final int USE_NULL_MARKERS = 256;
    public static final int NILLABLE_ELEMENT = 512;
    public static final int WHOLE_TEXT_NODE = 1024;
    public static final int IS_ID = 2048;
    public static final int IS_IDREF = 4096;
    public static final int ID_IDREF_CHECKED = 8192;
    public static final int TERMINATE = 16384;
    public static final int MUTABLE_TREE = 32768;
    public static final int REFUSE_NAMESPACES = 65536;
    public static final int BEQUEATH_INHERITED_NAMESPACES_ONLY = 131072;
    public static final int HAS_CHILDREN = 262144;
    public static final int ALL_NAMESPACES = 524288;
    public static final int NOT_A_DUPLICATE = 0x100000;
    public static final int SEPARATOR = 0x100000;

    public static boolean contains(int options, int option) {
        return (options & option) != 0;
    }
}


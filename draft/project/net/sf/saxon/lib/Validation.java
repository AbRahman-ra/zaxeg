/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

public final class Validation {
    public static final int INVALID = -1;
    public static final int STRICT = 1;
    public static final int LAX = 2;
    public static final int PRESERVE = 3;
    public static final int STRIP = 4;
    public static final int SKIP = 4;
    public static final int DEFAULT = 0;
    public static final int BY_TYPE = 8;

    private Validation() {
    }

    public static int getCode(String value) {
        if (value.equals("strict")) {
            return 1;
        }
        if (value.equals("lax")) {
            return 2;
        }
        if (value.equals("preserve")) {
            return 3;
        }
        if (value.equals("strip")) {
            return 4;
        }
        return -1;
    }

    public static String toString(int value) {
        switch (value) {
            case 1: {
                return "strict";
            }
            case 2: {
                return "lax";
            }
            case 3: {
                return "preserve";
            }
            case 4: {
                return "skip";
            }
            case 8: {
                return "by type";
            }
        }
        return "invalid";
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.parser;

public class OptimizerOptions {
    public static final int LOOP_LIFTING = 1;
    public static final int EXTRACT_GLOBALS = 2;
    public static final int INLINE_VARIABLES = 4;
    public static final int INLINE_FUNCTIONS = 8;
    public static final int INDEX_VARIABLES = 16;
    public static final int CREATE_KEYS = 32;
    public static final int BYTE_CODE = 64;
    public static final int COMMON_SUBEXPRESSIONS = 128;
    public static final int MISCELLANEOUS = 256;
    public static final int SWITCH = 512;
    public static final int JIT = 1024;
    public static final int RULE_SET = 2048;
    public static final int REGEX_CACHE = 4096;
    public static final int VOID_EXPRESSIONS = 8192;
    public static final int TAIL_CALLS = 16384;
    public static final int CONSTANT_FOLDING = 32768;
    private int options;
    public static final OptimizerOptions FULL_HE_OPTIMIZATION = new OptimizerOptions("lvmt");
    public static final OptimizerOptions FULL_EE_OPTIMIZATION = new OptimizerOptions(-1);

    public OptimizerOptions(int options) {
        this.options = options;
    }

    public OptimizerOptions(String flags) {
        int opt = 0;
        if (flags.startsWith("-")) {
            opt = -1;
            for (int i = 0; i < flags.length(); ++i) {
                char c = flags.charAt(i);
                opt &= ~this.decodeFlag(c);
            }
        } else {
            for (int i = 0; i < flags.length(); ++i) {
                char c = flags.charAt(i);
                opt |= this.decodeFlag(c);
            }
        }
        this.options = opt;
    }

    private int decodeFlag(char flag) {
        switch (flag) {
            case 'c': {
                return 64;
            }
            case 'd': {
                return 8192;
            }
            case 'e': {
                return 4096;
            }
            case 'f': {
                return 8;
            }
            case 'g': {
                return 2;
            }
            case 'j': {
                return 1024;
            }
            case 'k': {
                return 32;
            }
            case 'l': {
                return 1;
            }
            case 'm': {
                return 256;
            }
            case 'n': {
                return 32768;
            }
            case 'r': {
                return 2048;
            }
            case 's': {
                return 128;
            }
            case 't': {
                return 16384;
            }
            case 'v': {
                return 4;
            }
            case 'w': {
                return 512;
            }
            case 'x': {
                return 16;
            }
        }
        return 0;
    }

    public OptimizerOptions intersect(OptimizerOptions other) {
        return new OptimizerOptions(this.options & other.options);
    }

    public OptimizerOptions union(OptimizerOptions other) {
        return new OptimizerOptions(this.options | other.options);
    }

    public OptimizerOptions except(OptimizerOptions other) {
        return new OptimizerOptions(this.options & ~other.options);
    }

    public String toString() {
        String result = "";
        if (this.isSet(64)) {
            result = result + "c";
        }
        if (this.isSet(8192)) {
            result = result + "d";
        }
        if (this.isSet(4096)) {
            result = result + "e";
        }
        if (this.isSet(8)) {
            result = result + "f";
        }
        if (this.isSet(2)) {
            result = result + "g";
        }
        if (this.isSet(1024)) {
            result = result + "j";
        }
        if (this.isSet(32)) {
            result = result + "k";
        }
        if (this.isSet(1)) {
            result = result + "l";
        }
        if (this.isSet(256)) {
            result = result + "m";
        }
        if (this.isSet(32768)) {
            result = result + "n";
        }
        if (this.isSet(2048)) {
            result = result + "r";
        }
        if (this.isSet(128)) {
            result = result + "s";
        }
        if (this.isSet(16384)) {
            result = result + "t";
        }
        if (this.isSet(4)) {
            result = result + "v";
        }
        if (this.isSet(512)) {
            result = result + "w";
        }
        if (this.isSet(16)) {
            result = result + "x";
        }
        return result;
    }

    public boolean isSet(int option) {
        return (this.options & option) != 0;
    }

    public int getOptions() {
        return this.options;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.qtesla;

public class QTESLASecurityCategory {
    public static final int PROVABLY_SECURE_I = 5;
    public static final int PROVABLY_SECURE_III = 6;

    private QTESLASecurityCategory() {
    }

    static void validate(int n) {
        switch (n) {
            case 5: 
            case 6: {
                break;
            }
            default: {
                throw new IllegalArgumentException("unknown security category: " + n);
            }
        }
    }

    static int getPrivateSize(int n) {
        switch (n) {
            case 5: {
                return 5224;
            }
            case 6: {
                return 12392;
            }
        }
        throw new IllegalArgumentException("unknown security category: " + n);
    }

    static int getPublicSize(int n) {
        switch (n) {
            case 5: {
                return 14880;
            }
            case 6: {
                return 38432;
            }
        }
        throw new IllegalArgumentException("unknown security category: " + n);
    }

    static int getSignatureSize(int n) {
        switch (n) {
            case 5: {
                return 2592;
            }
            case 6: {
                return 5664;
            }
        }
        throw new IllegalArgumentException("unknown security category: " + n);
    }

    public static String getName(int n) {
        switch (n) {
            case 5: {
                return "qTESLA-p-I";
            }
            case 6: {
                return "qTESLA-p-III";
            }
        }
        throw new IllegalArgumentException("unknown security category: " + n);
    }
}


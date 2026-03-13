/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

public class LicenseException
extends RuntimeException {
    private int reason;
    public static final int EXPIRED = 1;
    public static final int INVALID = 2;
    public static final int NOT_FOUND = 3;
    public static final int WRONG_FEATURES = 4;
    public static final int CANNOT_READ = 5;
    public static final int WRONG_CONFIGURATION = 6;

    public LicenseException(String message, int reason) {
        super(message);
        this.reason = reason;
    }

    public void setReason(int reason) {
        this.reason = reason;
    }

    public int getReason() {
        return this.reason;
    }
}


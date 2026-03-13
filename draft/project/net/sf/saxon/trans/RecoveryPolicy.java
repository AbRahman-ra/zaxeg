/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

public enum RecoveryPolicy {
    RECOVER_SILENTLY,
    RECOVER_WITH_WARNINGS,
    DO_NOT_RECOVER;


    public static RecoveryPolicy fromString(String s) {
        switch (s) {
            case "recoverSilently": {
                return RECOVER_SILENTLY;
            }
            case "recoverWithWarnings": {
                return RECOVER_WITH_WARNINGS;
            }
            case "doNotRecover": {
                return DO_NOT_RECOVER;
            }
        }
        throw new IllegalArgumentException("Unrecognized value of RECOVERY_POLICY_NAME = '" + s + "'");
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto;

import java.security.Permission;
import java.util.HashSet;
import java.util.Set;

public class CryptoServicesPermission
extends Permission {
    public static final String GLOBAL_CONFIG = "globalConfig";
    public static final String THREAD_LOCAL_CONFIG = "threadLocalConfig";
    public static final String DEFAULT_RANDOM = "defaultRandomConfig";
    private final Set<String> actions = new HashSet<String>();

    public CryptoServicesPermission(String string) {
        super(string);
        this.actions.add(string);
    }

    public boolean implies(Permission permission) {
        if (permission instanceof CryptoServicesPermission) {
            CryptoServicesPermission cryptoServicesPermission = (CryptoServicesPermission)permission;
            if (this.getName().equals(cryptoServicesPermission.getName())) {
                return true;
            }
            if (this.actions.containsAll(cryptoServicesPermission.actions)) {
                return true;
            }
        }
        return false;
    }

    public boolean equals(Object object) {
        if (object instanceof CryptoServicesPermission) {
            CryptoServicesPermission cryptoServicesPermission = (CryptoServicesPermission)object;
            if (this.actions.equals(cryptoServicesPermission.actions)) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        return this.actions.hashCode();
    }

    public String getActions() {
        return this.actions.toString();
    }
}


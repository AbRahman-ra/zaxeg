/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.util;

public class Objects {
    public static boolean areEqual(Object object, Object object2) {
        return object == object2 || null != object && null != object2 && object.equals(object2);
    }

    public static int hashCode(Object object) {
        return null == object ? 0 : object.hashCode();
    }
}


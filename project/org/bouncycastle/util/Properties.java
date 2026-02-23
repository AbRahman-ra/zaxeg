/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.util;

import java.math.BigInteger;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Security;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.bouncycastle.util.Strings;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Properties {
    private static final ThreadLocal threadProperties = new ThreadLocal();

    private Properties() {
    }

    public static boolean isOverrideSet(String string) {
        try {
            return Properties.isSetTrue(Properties.getPropertyValue(string));
        } catch (AccessControlException accessControlException) {
            return false;
        }
    }

    public static boolean isOverrideSetTo(String string, boolean bl) {
        try {
            String string2 = Properties.getPropertyValue(string);
            if (bl) {
                return Properties.isSetTrue(string2);
            }
            return Properties.isSetFalse(string2);
        } catch (AccessControlException accessControlException) {
            return false;
        }
    }

    public static boolean setThreadOverride(String string, boolean bl) {
        boolean bl2 = Properties.isOverrideSet(string);
        HashMap<String, String> hashMap = (HashMap<String, String>)threadProperties.get();
        if (hashMap == null) {
            hashMap = new HashMap<String, String>();
            threadProperties.set(hashMap);
        }
        hashMap.put(string, bl ? "true" : "false");
        return bl2;
    }

    public static boolean removeThreadOverride(String string) {
        String string2;
        Map map = (Map)threadProperties.get();
        if (map != null && (string2 = (String)map.remove(string)) != null) {
            if (map.isEmpty()) {
                threadProperties.remove();
            }
            return "true".equals(Strings.toLowerCase(string2));
        }
        return false;
    }

    public static BigInteger asBigInteger(String string) {
        String string2 = Properties.getPropertyValue(string);
        if (string2 != null) {
            return new BigInteger(string2);
        }
        return null;
    }

    public static Set<String> asKeySet(String string) {
        HashSet<String> hashSet = new HashSet<String>();
        String string2 = Properties.getPropertyValue(string);
        if (string2 != null) {
            StringTokenizer stringTokenizer = new StringTokenizer(string2, ",");
            while (stringTokenizer.hasMoreElements()) {
                hashSet.add(Strings.toLowerCase(stringTokenizer.nextToken()).trim());
            }
        }
        return Collections.unmodifiableSet(hashSet);
    }

    public static String getPropertyValue(String string) {
        String string2;
        String string3 = (String)AccessController.doPrivileged(new PrivilegedAction(){

            public Object run() {
                return Security.getProperty(string);
            }
        });
        if (string3 != null) {
            return string3;
        }
        Map map = (Map)threadProperties.get();
        if (map != null && (string2 = (String)map.get(string)) != null) {
            return string2;
        }
        return (String)AccessController.doPrivileged(new PrivilegedAction(){

            public Object run() {
                return System.getProperty(string);
            }
        });
    }

    private static boolean isSetFalse(String string) {
        if (string == null || string.length() != 5) {
            return false;
        }
        return !(string.charAt(0) != 'f' && string.charAt(0) != 'F' || string.charAt(1) != 'a' && string.charAt(1) != 'A' || string.charAt(2) != 'l' && string.charAt(2) != 'L' || string.charAt(3) != 's' && string.charAt(3) != 'S' || string.charAt(4) != 'e' && string.charAt(4) != 'E');
    }

    private static boolean isSetTrue(String string) {
        if (string == null || string.length() != 4) {
            return false;
        }
        return !(string.charAt(0) != 't' && string.charAt(0) != 'T' || string.charAt(1) != 'r' && string.charAt(1) != 'R' || string.charAt(2) != 'u' && string.charAt(2) != 'U' || string.charAt(3) != 'e' && string.charAt(3) != 'E');
    }
}


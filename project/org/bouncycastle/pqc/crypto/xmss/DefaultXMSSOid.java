/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.xmss;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.pqc.crypto.xmss.XMSSOid;

public final class DefaultXMSSOid
implements XMSSOid {
    private static final Map<String, DefaultXMSSOid> oidLookupTable;
    private final int oid;
    private final String stringRepresentation;

    private DefaultXMSSOid(int n, String string) {
        this.oid = n;
        this.stringRepresentation = string;
    }

    public static DefaultXMSSOid lookup(String string, int n, int n2, int n3, int n4) {
        if (string == null) {
            throw new NullPointerException("algorithmName == null");
        }
        return oidLookupTable.get(DefaultXMSSOid.createKey(string, n, n2, n3, n4));
    }

    private static String createKey(String string, int n, int n2, int n3, int n4) {
        if (string == null) {
            throw new NullPointerException("algorithmName == null");
        }
        return string + "-" + n + "-" + n2 + "-" + n3 + "-" + n4;
    }

    public int getOid() {
        return this.oid;
    }

    public String toString() {
        return this.stringRepresentation;
    }

    static {
        HashMap<String, DefaultXMSSOid> hashMap = new HashMap<String, DefaultXMSSOid>();
        hashMap.put(DefaultXMSSOid.createKey("SHA-256", 32, 16, 67, 10), new DefaultXMSSOid(1, "XMSS_SHA2_10_256"));
        hashMap.put(DefaultXMSSOid.createKey("SHA-256", 32, 16, 67, 16), new DefaultXMSSOid(2, "XMSS_SHA2_16_256"));
        hashMap.put(DefaultXMSSOid.createKey("SHA-256", 32, 16, 67, 20), new DefaultXMSSOid(3, "XMSS_SHA2_20_256"));
        hashMap.put(DefaultXMSSOid.createKey("SHA-512", 64, 16, 131, 10), new DefaultXMSSOid(4, "XMSS_SHA2_10_512"));
        hashMap.put(DefaultXMSSOid.createKey("SHA-512", 64, 16, 131, 16), new DefaultXMSSOid(5, "XMSS_SHA2_16_512"));
        hashMap.put(DefaultXMSSOid.createKey("SHA-512", 64, 16, 131, 20), new DefaultXMSSOid(6, "XMSS_SHA2_20_512"));
        hashMap.put(DefaultXMSSOid.createKey("SHAKE128", 32, 16, 67, 10), new DefaultXMSSOid(7, "XMSS_SHAKE_10_256"));
        hashMap.put(DefaultXMSSOid.createKey("SHAKE128", 32, 16, 67, 16), new DefaultXMSSOid(8, "XMSS_SHAKE_16_256"));
        hashMap.put(DefaultXMSSOid.createKey("SHAKE128", 32, 16, 67, 20), new DefaultXMSSOid(9, "XMSS_SHAKE_20_256"));
        hashMap.put(DefaultXMSSOid.createKey("SHAKE256", 64, 16, 131, 10), new DefaultXMSSOid(10, "XMSS_SHAKE_10_512"));
        hashMap.put(DefaultXMSSOid.createKey("SHAKE256", 64, 16, 131, 16), new DefaultXMSSOid(11, "XMSS_SHAKE_16_512"));
        hashMap.put(DefaultXMSSOid.createKey("SHAKE256", 64, 16, 131, 20), new DefaultXMSSOid(12, "XMSS_SHAKE_20_512"));
        oidLookupTable = Collections.unmodifiableMap(hashMap);
    }
}


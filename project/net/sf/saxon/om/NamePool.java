/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.om.StructuredQName;

public final class NamePool {
    public static final int FP_MASK = 1048575;
    public static final int USER_DEFINED_MASK = 1047552;
    private static final int MAX_FINGERPRINT = 1048575;
    private final ConcurrentHashMap<StructuredQName, Integer> qNameToInteger = new ConcurrentHashMap(1000);
    private final ConcurrentHashMap<Integer, StructuredQName> integerToQName = new ConcurrentHashMap(1000);
    private AtomicInteger unique = new AtomicInteger(1024);
    private ConcurrentHashMap<String, String> suggestedPrefixes = new ConcurrentHashMap();

    public void suggestPrefix(String prefix, String uri) {
        this.suggestedPrefixes.put(uri, prefix);
    }

    public StructuredQName getUnprefixedQName(int nameCode) {
        int fp = nameCode & 0xFFFFF;
        if ((fp & 0xFFC00) == 0) {
            return StandardNames.getUnprefixedQName(fp);
        }
        return this.integerToQName.get(fp);
    }

    public StructuredQName getStructuredQName(int fingerprint) {
        return this.getUnprefixedQName(fingerprint);
    }

    public static boolean isPrefixed(int nameCode) {
        return (nameCode & 0x3FF00000) != 0;
    }

    public String suggestPrefixForURI(String uri) {
        if (uri.equals("http://www.w3.org/XML/1998/namespace")) {
            return "xml";
        }
        return this.suggestedPrefixes.get(uri);
    }

    public synchronized int allocateFingerprint(String uri, String local) {
        int fp;
        if ((NamespaceConstant.isReserved(uri) || "http://saxon.sf.net/".equals(uri)) && (fp = StandardNames.getFingerprint(uri, local)) != -1) {
            return fp;
        }
        StructuredQName qName = new StructuredQName("", uri, local);
        Integer existing = this.qNameToInteger.get(qName);
        if (existing != null) {
            return existing;
        }
        int next = this.unique.getAndIncrement();
        if (next > 1048575) {
            throw new NamePoolLimitException("Too many distinct names in NamePool");
        }
        existing = this.qNameToInteger.putIfAbsent(qName, next);
        if (existing == null) {
            this.integerToQName.put(next, qName);
            return next;
        }
        return existing;
    }

    public String getURI(int nameCode) {
        int fp = nameCode & 0xFFFFF;
        if ((fp & 0xFFC00) == 0) {
            return StandardNames.getURI(fp);
        }
        return this.getUnprefixedQName(fp).getURI();
    }

    public String getLocalName(int nameCode) {
        return this.getUnprefixedQName(nameCode).getLocalPart();
    }

    public String getDisplayName(int nameCode) {
        return this.getStructuredQName(nameCode).getDisplayName();
    }

    public String getClarkName(int nameCode) {
        return this.getUnprefixedQName(nameCode).getClarkName();
    }

    public String getEQName(int nameCode) {
        return this.getUnprefixedQName(nameCode).getEQName();
    }

    public int allocateClarkName(String expandedName) {
        String localName;
        String namespace;
        if (expandedName.charAt(0) == '{') {
            int closeBrace = expandedName.indexOf(125);
            if (closeBrace < 0) {
                throw new IllegalArgumentException("No closing '}' in Clark name");
            }
            namespace = expandedName.substring(1, closeBrace);
            if (closeBrace == expandedName.length()) {
                throw new IllegalArgumentException("Missing local part in Clark name");
            }
            localName = expandedName.substring(closeBrace + 1);
        } else {
            namespace = "";
            localName = expandedName;
        }
        return this.allocateFingerprint(namespace, localName);
    }

    public int getFingerprint(String uri, String localName) {
        int fp;
        if ((NamespaceConstant.isReserved(uri) || uri.equals("http://saxon.sf.net/")) && (fp = StandardNames.getFingerprint(uri, localName)) != -1) {
            return fp;
        }
        Integer fp2 = this.qNameToInteger.get(new StructuredQName("", uri, localName));
        return fp2 == null ? -1 : fp2;
    }

    public static class NamePoolLimitException
    extends RuntimeException {
        public NamePoolLimitException(String message) {
            super(message);
        }
    }
}


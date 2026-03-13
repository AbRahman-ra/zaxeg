/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.om.IdentityComparable;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;

public class FingerprintedQName
implements NodeName {
    private StructuredQName qName;
    private int fingerprint = -1;

    public FingerprintedQName(String prefix, String uri, String localName) {
        this.qName = new StructuredQName(prefix, uri, localName);
    }

    public FingerprintedQName(String prefix, String uri, String localName, int fingerprint) {
        this.qName = new StructuredQName(prefix, uri, localName);
        this.fingerprint = fingerprint;
    }

    public FingerprintedQName(String prefix, String uri, String localName, NamePool pool) {
        this.qName = new StructuredQName(prefix, uri, localName);
        this.fingerprint = pool.allocateFingerprint(uri, localName);
    }

    public FingerprintedQName(StructuredQName qName, int fingerprint) {
        this.qName = qName;
        this.fingerprint = fingerprint;
    }

    public FingerprintedQName(StructuredQName qName, NamePool pool) {
        this.qName = qName;
        this.fingerprint = pool.allocateFingerprint(qName.getURI(), qName.getLocalPart());
    }

    public static FingerprintedQName fromClarkName(String expandedName) {
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
        return new FingerprintedQName("", namespace, localName);
    }

    public static FingerprintedQName fromEQName(String expandedName) {
        String localName;
        String namespace;
        if (expandedName.startsWith("Q{")) {
            int closeBrace = expandedName.indexOf(125, 2);
            if (closeBrace < 0) {
                throw new IllegalArgumentException("No closing '}' in EQName");
            }
            namespace = expandedName.substring(2, closeBrace);
            if (closeBrace == expandedName.length()) {
                throw new IllegalArgumentException("Missing local part in EQName");
            }
            localName = expandedName.substring(closeBrace + 1);
        } else {
            namespace = "";
            localName = expandedName;
        }
        return new FingerprintedQName("", namespace, localName);
    }

    @Override
    public boolean hasFingerprint() {
        return this.fingerprint != -1;
    }

    @Override
    public int getFingerprint() {
        return this.fingerprint;
    }

    @Override
    public int obtainFingerprint(NamePool pool) {
        if (this.fingerprint == -1) {
            this.fingerprint = pool.allocateFingerprint(this.getURI(), this.getLocalPart());
        }
        return this.fingerprint;
    }

    @Override
    public String getDisplayName() {
        return this.qName.getDisplayName();
    }

    @Override
    public String getPrefix() {
        return this.qName.getPrefix();
    }

    @Override
    public String getURI() {
        return this.qName.getURI();
    }

    @Override
    public String getLocalPart() {
        return this.qName.getLocalPart();
    }

    @Override
    public StructuredQName getStructuredQName() {
        return this.qName;
    }

    @Override
    public boolean hasURI(String ns) {
        return this.qName.hasURI(ns);
    }

    @Override
    public NamespaceBinding getNamespaceBinding() {
        return this.qName.getNamespaceBinding();
    }

    @Override
    public int identityHashCode() {
        return 0;
    }

    public boolean equals(Object other) {
        if (other instanceof NodeName) {
            if (this.fingerprint != -1 && ((NodeName)other).hasFingerprint()) {
                return this.getFingerprint() == ((NodeName)other).getFingerprint();
            }
            return this.getLocalPart().equals(((NodeName)other).getLocalPart()) && this.hasURI(((NodeName)other).getURI());
        }
        return false;
    }

    public int hashCode() {
        return this.qName.hashCode();
    }

    @Override
    public boolean isIdentical(IdentityComparable other) {
        return other instanceof NodeName && this.equals(other) && this.getPrefix().equals(((NodeName)other).getPrefix());
    }

    public String toString() {
        return this.qName.getDisplayName();
    }
}


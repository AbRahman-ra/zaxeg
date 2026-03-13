/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.om.IdentityComparable;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;

public class CodedName
implements NodeName {
    private int fingerprint;
    private String prefix;
    private NamePool pool;

    public CodedName(int fingerprint, String prefix, NamePool pool) {
        this.fingerprint = fingerprint;
        this.prefix = prefix;
        this.pool = pool;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public String getURI() {
        return this.pool.getURI(this.fingerprint);
    }

    @Override
    public String getLocalPart() {
        return this.pool.getLocalName(this.fingerprint);
    }

    @Override
    public String getDisplayName() {
        return this.prefix.isEmpty() ? this.getLocalPart() : this.prefix + ":" + this.getLocalPart();
    }

    @Override
    public StructuredQName getStructuredQName() {
        StructuredQName qn = this.pool.getUnprefixedQName(this.fingerprint);
        if (this.prefix.isEmpty()) {
            return qn;
        }
        return new StructuredQName(this.prefix, qn.getURI(), qn.getLocalPart());
    }

    @Override
    public boolean hasURI(String ns) {
        return this.getURI().equals(ns);
    }

    @Override
    public NamespaceBinding getNamespaceBinding() {
        return new NamespaceBinding(this.prefix, this.pool.getURI(this.fingerprint));
    }

    @Override
    public boolean hasFingerprint() {
        return true;
    }

    @Override
    public int getFingerprint() {
        return this.fingerprint;
    }

    @Override
    public int obtainFingerprint(NamePool namePool) {
        return this.fingerprint;
    }

    public int hashCode() {
        return StructuredQName.computeHashCode(this.getURI(), this.getLocalPart());
    }

    public boolean equals(Object obj) {
        if (obj instanceof NodeName) {
            NodeName n = (NodeName)obj;
            if (n.hasFingerprint()) {
                return this.getFingerprint() == n.getFingerprint();
            }
            return n.getLocalPart().equals(this.getLocalPart()) && n.hasURI(this.getURI());
        }
        return false;
    }

    @Override
    public boolean isIdentical(IdentityComparable other) {
        return other instanceof NodeName && this.equals(other) && this.getPrefix().equals(((NodeName)other).getPrefix());
    }

    @Override
    public int identityHashCode() {
        return this.hashCode() ^ this.getPrefix().hashCode();
    }

    public String toString() {
        return this.getDisplayName();
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.om.IdentityComparable;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;

public final class NoNamespaceName
implements NodeName {
    private String localName;
    private int nameCode = -1;

    public NoNamespaceName(String localName) {
        this.localName = localName;
    }

    public NoNamespaceName(String localName, int nameCode) {
        this.localName = localName;
        this.nameCode = nameCode;
    }

    @Override
    public String getPrefix() {
        return "";
    }

    @Override
    public String getURI() {
        return "";
    }

    @Override
    public String getLocalPart() {
        return this.localName;
    }

    @Override
    public String getDisplayName() {
        return this.localName;
    }

    @Override
    public StructuredQName getStructuredQName() {
        return new StructuredQName("", "", this.getLocalPart());
    }

    @Override
    public boolean hasURI(String ns) {
        return ns.isEmpty();
    }

    @Override
    public NamespaceBinding getNamespaceBinding() {
        return NamespaceBinding.DEFAULT_UNDECLARATION;
    }

    @Override
    public boolean hasFingerprint() {
        return this.nameCode != -1;
    }

    @Override
    public int getFingerprint() {
        return this.nameCode & 0xFFFFF;
    }

    @Override
    public int obtainFingerprint(NamePool namePool) {
        if (this.nameCode == -1) {
            this.nameCode = namePool.allocateFingerprint("", this.localName);
            return this.nameCode;
        }
        return this.nameCode;
    }

    public int hashCode() {
        return StructuredQName.computeHashCode("", this.localName);
    }

    public boolean equals(Object obj) {
        return obj instanceof NodeName && ((NodeName)obj).getLocalPart().equals(this.localName) && ((NodeName)obj).hasURI("");
    }

    public String toString() {
        return this.localName;
    }

    @Override
    public boolean isIdentical(IdentityComparable other) {
        return other instanceof NodeName && this.equals(other) && ((NodeName)other).getPrefix().isEmpty();
    }

    @Override
    public int identityHashCode() {
        return this.hashCode() ^ this.getPrefix().hashCode();
    }
}


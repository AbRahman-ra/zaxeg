/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.om.IdentityComparable;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.StructuredQName;

public interface NodeName
extends IdentityComparable {
    public String getPrefix();

    public String getURI();

    public String getLocalPart();

    public String getDisplayName();

    public StructuredQName getStructuredQName();

    public boolean hasURI(String var1);

    public NamespaceBinding getNamespaceBinding();

    public boolean hasFingerprint();

    public int getFingerprint();

    public int obtainFingerprint(NamePool var1);
}


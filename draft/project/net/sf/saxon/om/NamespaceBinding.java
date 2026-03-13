/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.Iterator;
import net.sf.saxon.om.NamespaceBindingSet;
import net.sf.saxon.tree.jiter.MonoIterator;

public final class NamespaceBinding
implements NamespaceBindingSet {
    private String prefix;
    private String uri;
    public static final NamespaceBinding XML = new NamespaceBinding("xml", "http://www.w3.org/XML/1998/namespace");
    public static final NamespaceBinding DEFAULT_UNDECLARATION = new NamespaceBinding("", "");
    public static final NamespaceBinding[] EMPTY_ARRAY = new NamespaceBinding[0];

    public NamespaceBinding(String prefix, String uri) {
        this.prefix = prefix;
        this.uri = uri;
        if (prefix == null || uri == null) {
            throw new NullPointerException();
        }
    }

    @Override
    public String getURI(String prefix) {
        return prefix.equals(this.prefix) ? this.uri : null;
    }

    public static NamespaceBinding makeNamespaceBinding(CharSequence prefix, CharSequence uri) {
        if (prefix.length() == 0 && uri.length() == 0) {
            return DEFAULT_UNDECLARATION;
        }
        if (prefix.equals("xml") && uri.equals("http://www.w3.org/XML/1998/namespace")) {
            return XML;
        }
        return new NamespaceBinding(prefix.toString(), uri.toString());
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getURI() {
        return this.uri;
    }

    public boolean isXmlNamespace() {
        return this.prefix.equals("xml");
    }

    public boolean isDefaultUndeclaration() {
        return this.prefix.isEmpty() && this.uri.isEmpty();
    }

    @Override
    public Iterator<NamespaceBinding> iterator() {
        return new MonoIterator<NamespaceBinding>(this);
    }

    public boolean equals(Object obj) {
        return obj instanceof NamespaceBinding && this.prefix.equals(((NamespaceBinding)obj).getPrefix()) && this.uri.equals(((NamespaceBinding)obj).getURI());
    }

    public int hashCode() {
        return this.prefix.hashCode() ^ this.uri.hashCode();
    }

    public String toString() {
        return this.prefix + "=" + this.uri;
    }
}


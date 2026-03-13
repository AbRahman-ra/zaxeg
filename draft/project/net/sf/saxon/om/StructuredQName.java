/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import javax.xml.namespace.QName;
import net.sf.saxon.om.IdentityComparable;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.QNameException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.Whitespace;

public class StructuredQName
implements IdentityComparable {
    private char[] content;
    private int localNameStart;
    private int prefixStart;
    private int cachedHashCode = -1;

    private StructuredQName(char[] content, int localNameStart, int prefixStart) {
        this.content = content;
        this.localNameStart = localNameStart;
        this.prefixStart = prefixStart;
    }

    public StructuredQName(String prefix, String uri, String localName) {
        if (uri == null) {
            uri = "";
        }
        int plen = prefix.length();
        int ulen = uri.length();
        int llen = localName.length();
        this.localNameStart = ulen;
        this.prefixStart = ulen + llen;
        this.content = new char[ulen + llen + plen];
        uri.getChars(0, ulen, this.content, 0);
        localName.getChars(0, llen, this.content, ulen);
        prefix.getChars(0, plen, this.content, ulen + llen);
    }

    public static StructuredQName fromClarkName(String expandedName) {
        String localName;
        String namespace;
        if (expandedName.startsWith("Q{")) {
            expandedName = expandedName.substring(1);
        }
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
        return new StructuredQName("", namespace, localName);
    }

    public static StructuredQName fromLexicalQName(CharSequence lexicalName, boolean useDefault, boolean allowEQName, NamespaceResolver resolver) throws XPathException {
        lexicalName = Whitespace.trimWhitespace(lexicalName);
        if (allowEQName && lexicalName.length() >= 4 && lexicalName.charAt(0) == 'Q' && lexicalName.charAt(1) == '{') {
            String name = lexicalName.toString();
            int endBrace = name.indexOf(125);
            if (endBrace < 0) {
                throw new XPathException("Invalid EQName: closing brace not found", "FOCA0002");
            }
            if (endBrace == name.length() - 1) {
                throw new XPathException("Invalid EQName: local part is missing", "FOCA0002");
            }
            String uri = name.substring(2, endBrace);
            if (uri.contains("{")) {
                throw new XPathException("Namespace URI must not contain '{'", "FOCA0002");
            }
            String local = name.substring(endBrace + 1);
            if (!NameChecker.isValidNCName(local)) {
                throw new XPathException("Invalid EQName: local part is not a valid NCName", "FOCA0002");
            }
            return new StructuredQName("", uri, local);
        }
        try {
            String[] parts = NameChecker.getQNameParts(lexicalName);
            String uri = resolver.getURIForPrefix(parts[0], useDefault);
            if (uri == null) {
                if (NameChecker.isValidNCName(parts[0])) {
                    XPathException de = new XPathException("Namespace prefix '" + parts[0] + "' has not been declared");
                    de.setErrorCode("FONS0004");
                    throw de;
                }
                XPathException de = new XPathException("Invalid namespace prefix '" + parts[0] + "'");
                de.setErrorCode("FOCA0002");
                throw de;
            }
            return new StructuredQName(parts[0], uri, parts[1]);
        } catch (QNameException e) {
            throw new XPathException(e.getMessage(), "FOCA0002");
        }
    }

    public static StructuredQName fromEQName(CharSequence eqName) {
        if ((eqName = Whitespace.trimWhitespace(eqName)).length() >= 4 && eqName.charAt(0) == 'Q' && eqName.charAt(1) == '{') {
            String name = eqName.toString();
            int endBrace = name.indexOf(125);
            if (endBrace < 0) {
                throw new IllegalArgumentException("Invalid EQName: closing brace not found");
            }
            if (endBrace == name.length() - 1) {
                throw new IllegalArgumentException("Invalid EQName: local part is missing");
            }
            String uri = name.substring(2, endBrace);
            if (uri.indexOf(123) >= 0) {
                throw new IllegalArgumentException("Invalid EQName: open brace in URI part");
            }
            String local = name.substring(endBrace + 1);
            return new StructuredQName("", uri, local);
        }
        return new StructuredQName("", "", eqName.toString());
    }

    public String getPrefix() {
        return new String(this.content, this.prefixStart, this.content.length - this.prefixStart);
    }

    public String getURI() {
        if (this.localNameStart == 0) {
            return "";
        }
        return new String(this.content, 0, this.localNameStart);
    }

    public boolean hasURI(String uri) {
        if (this.localNameStart != uri.length()) {
            return false;
        }
        for (int i = this.localNameStart - 1; i >= 0; --i) {
            if (this.content[i] == uri.charAt(i)) continue;
            return false;
        }
        return true;
    }

    public String getLocalPart() {
        return new String(this.content, this.localNameStart, this.prefixStart - this.localNameStart);
    }

    public String getDisplayName() {
        if (this.prefixStart == this.content.length) {
            return this.getLocalPart();
        }
        FastStringBuffer buff = new FastStringBuffer(this.content.length - this.localNameStart + 1);
        buff.append(this.content, this.prefixStart, this.content.length - this.prefixStart);
        buff.cat(':');
        buff.append(this.content, this.localNameStart, this.prefixStart - this.localNameStart);
        return buff.toString();
    }

    public StructuredQName getStructuredQName() {
        return this;
    }

    public String getClarkName() {
        FastStringBuffer buff = new FastStringBuffer(this.content.length - this.prefixStart + 2);
        if (this.localNameStart > 0) {
            buff.cat('{');
            buff.append(this.content, 0, this.localNameStart);
            buff.cat('}');
        }
        buff.append(this.content, this.localNameStart, this.prefixStart - this.localNameStart);
        return buff.toString();
    }

    public String getEQName() {
        FastStringBuffer buff = new FastStringBuffer(this.content.length - this.prefixStart + 2);
        buff.append("Q{");
        if (this.localNameStart > 0) {
            buff.append(this.content, 0, this.localNameStart);
        }
        buff.cat('}');
        buff.append(this.content, this.localNameStart, this.prefixStart - this.localNameStart);
        return buff.toString();
    }

    public String toString() {
        return this.getDisplayName();
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof StructuredQName) {
            int c = ((StructuredQName)other).cachedHashCode;
            if (c != -1 && c != this.hashCode()) {
                return false;
            }
            StructuredQName sq2 = (StructuredQName)other;
            if (this.localNameStart != sq2.localNameStart || this.prefixStart != sq2.prefixStart) {
                return false;
            }
            for (int i = this.prefixStart - 1; i >= 0; --i) {
                if (this.content[i] == sq2.content[i]) continue;
                return false;
            }
            return true;
        }
        return false;
    }

    public int hashCode() {
        if (this.cachedHashCode == -1) {
            int h = -2147180533;
            h ^= this.prefixStart;
            h ^= this.localNameStart;
            for (int i = this.localNameStart; i < this.prefixStart; ++i) {
                h ^= this.content[i] << (i & 0x1F);
            }
            this.cachedHashCode = h;
            return this.cachedHashCode;
        }
        return this.cachedHashCode;
    }

    public static int computeHashCode(CharSequence uri, CharSequence local) {
        int h = -2147180533;
        int localLen = local.length();
        int uriLen = uri.length();
        int totalLen = localLen + uriLen;
        h ^= totalLen;
        h ^= uriLen;
        int i = 0;
        int j = uriLen;
        while (i < localLen) {
            h ^= local.charAt(i) << (j & 0x1F);
            ++i;
            ++j;
        }
        return h;
    }

    public QName toJaxpQName() {
        return new QName(this.getURI(), this.getLocalPart(), this.getPrefix());
    }

    public NamespaceBinding getNamespaceBinding() {
        return NamespaceBinding.makeNamespaceBinding(this.getPrefix(), this.getURI());
    }

    @Override
    public boolean isIdentical(IdentityComparable other) {
        return this.equals(other) && ((StructuredQName)other).getPrefix().equals(this.getPrefix());
    }

    @Override
    public int identityHashCode() {
        return this.hashCode() ^ this.getPrefix().hashCode();
    }
}


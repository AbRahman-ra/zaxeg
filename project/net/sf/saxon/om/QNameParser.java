/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.QNameException;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.XQueryParser;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Whitespace;

public class QNameParser {
    private NamespaceResolver resolver;
    private boolean acceptEQName = false;
    private String errorOnBadSyntax = "XPST0003";
    private String errorOnUnresolvedPrefix = "XPST0081";
    private XQueryParser.Unescaper unescaper = null;

    public QNameParser(NamespaceResolver resolver) {
        this.resolver = resolver;
    }

    public QNameParser withNamespaceResolver(NamespaceResolver resolver) {
        QNameParser qp2 = this.copy();
        qp2.resolver = resolver;
        return qp2;
    }

    public QNameParser withAcceptEQName(boolean acceptEQName) {
        if (acceptEQName == this.acceptEQName) {
            return this;
        }
        QNameParser qp2 = this.copy();
        qp2.acceptEQName = acceptEQName;
        return qp2;
    }

    public QNameParser withErrorOnBadSyntax(String code) {
        if (code.equals(this.errorOnBadSyntax)) {
            return this;
        }
        QNameParser qp2 = this.copy();
        qp2.errorOnBadSyntax = code;
        return qp2;
    }

    public QNameParser withErrorOnUnresolvedPrefix(String code) {
        if (code.equals(this.errorOnUnresolvedPrefix)) {
            return this;
        }
        QNameParser qp2 = this.copy();
        qp2.errorOnUnresolvedPrefix = code;
        return qp2;
    }

    public QNameParser withUnescaper(XQueryParser.Unescaper unescaper) {
        QNameParser qp2 = this.copy();
        qp2.unescaper = unescaper;
        return qp2;
    }

    private QNameParser copy() {
        QNameParser qp2 = new QNameParser(this.resolver);
        qp2.acceptEQName = this.acceptEQName;
        qp2.errorOnBadSyntax = this.errorOnBadSyntax;
        qp2.errorOnUnresolvedPrefix = this.errorOnUnresolvedPrefix;
        qp2.unescaper = this.unescaper;
        return qp2;
    }

    public StructuredQName parse(CharSequence lexicalName, String defaultNS) throws XPathException {
        lexicalName = Whitespace.trimWhitespace(lexicalName);
        if (this.acceptEQName && lexicalName.length() >= 4 && lexicalName.charAt(0) == 'Q' && lexicalName.charAt(1) == '{') {
            String name = lexicalName.toString();
            int endBrace = name.indexOf(125);
            if (endBrace < 0) {
                throw new XPathException("Invalid EQName: closing brace not found", this.errorOnBadSyntax);
            }
            if (endBrace == name.length() - 1) {
                throw new XPathException("Invalid EQName: local part is missing", this.errorOnBadSyntax);
            }
            String uri = Whitespace.collapseWhitespace(name.substring(2, endBrace)).toString();
            if (uri.contains("{")) {
                throw new XPathException("Invalid EQName: URI contains opening brace", this.errorOnBadSyntax);
            }
            if (this.unescaper != null && uri.contains("&")) {
                uri = this.unescaper.unescape(uri).toString();
            }
            if (uri.equals("http://www.w3.org/2000/xmlns/")) {
                throw new XPathException("The string 'http://www.w3.org/2000/xmlns/' cannot be used as a namespace URI", "XQST0070");
            }
            String local = name.substring(endBrace + 1);
            this.checkLocalName(local);
            return new StructuredQName("", uri, local);
        }
        try {
            String[] parts = NameChecker.getQNameParts(lexicalName);
            this.checkLocalName(parts[1]);
            if (parts[0].isEmpty()) {
                return new StructuredQName("", defaultNS, parts[1]);
            }
            String uri = this.resolver.getURIForPrefix(parts[0], false);
            if (uri == null) {
                throw new XPathException("Namespace prefix '" + parts[0] + "' has not been declared", this.errorOnUnresolvedPrefix);
            }
            return new StructuredQName(parts[0], uri, parts[1]);
        } catch (QNameException e) {
            throw new XPathException(e.getMessage(), this.errorOnBadSyntax);
        }
    }

    private void checkLocalName(String local) throws XPathException {
        if (!NameChecker.isValidNCName(local)) {
            throw new XPathException("Invalid EQName: local part is not a valid NCName", this.errorOnBadSyntax);
        }
    }
}


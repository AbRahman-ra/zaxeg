/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.util.Objects;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;

public class QName {
    private StructuredQName sqName;
    public static final QName XS_STRING = new QName("xs", "http://www.w3.org/2001/XMLSchema", "string");
    public static final QName XS_BOOLEAN = new QName("xs", "http://www.w3.org/2001/XMLSchema", "boolean");
    public static final QName XS_DECIMAL = new QName("xs", "http://www.w3.org/2001/XMLSchema", "decimal");
    public static final QName XS_FLOAT = new QName("xs", "http://www.w3.org/2001/XMLSchema", "float");
    public static final QName XS_DOUBLE = new QName("xs", "http://www.w3.org/2001/XMLSchema", "double");
    public static final QName XS_DURATION = new QName("xs", "http://www.w3.org/2001/XMLSchema", "duration");
    public static final QName XS_DATE_TIME = new QName("xs", "http://www.w3.org/2001/XMLSchema", "dateTime");
    public static final QName XS_TIME = new QName("xs", "http://www.w3.org/2001/XMLSchema", "time");
    public static final QName XS_DATE = new QName("xs", "http://www.w3.org/2001/XMLSchema", "date");
    public static final QName XS_G_YEAR_MONTH = new QName("xs", "http://www.w3.org/2001/XMLSchema", "gYearMonth");
    public static final QName XS_G_YEAR = new QName("xs", "http://www.w3.org/2001/XMLSchema", "gYear");
    public static final QName XS_G_MONTH_DAY = new QName("xs", "http://www.w3.org/2001/XMLSchema", "gMonthDay");
    public static final QName XS_G_DAY = new QName("xs", "http://www.w3.org/2001/XMLSchema", "gDay");
    public static final QName XS_G_MONTH = new QName("xs", "http://www.w3.org/2001/XMLSchema", "gMonth");
    public static final QName XS_HEX_BINARY = new QName("xs", "http://www.w3.org/2001/XMLSchema", "hexBinary");
    public static final QName XS_BASE64_BINARY = new QName("xs", "http://www.w3.org/2001/XMLSchema", "base64Binary");
    public static final QName XS_ANY_URI = new QName("xs", "http://www.w3.org/2001/XMLSchema", "anyURI");
    public static final QName XS_QNAME = new QName("xs", "http://www.w3.org/2001/XMLSchema", "QName");
    public static final QName XS_NOTATION = new QName("xs", "http://www.w3.org/2001/XMLSchema", "NOTATION");
    public static final QName XS_INTEGER = new QName("xs", "http://www.w3.org/2001/XMLSchema", "integer");
    public static final QName XS_NON_POSITIVE_INTEGER = new QName("xs", "http://www.w3.org/2001/XMLSchema", "nonPositiveInteger");
    public static final QName XS_NEGATIVE_INTEGER = new QName("xs", "http://www.w3.org/2001/XMLSchema", "negativeInteger");
    public static final QName XS_LONG = new QName("xs", "http://www.w3.org/2001/XMLSchema", "long");
    public static final QName XS_INT = new QName("xs", "http://www.w3.org/2001/XMLSchema", "int");
    public static final QName XS_SHORT = new QName("xs", "http://www.w3.org/2001/XMLSchema", "short");
    public static final QName XS_BYTE = new QName("xs", "http://www.w3.org/2001/XMLSchema", "byte");
    public static final QName XS_NON_NEGATIVE_INTEGER = new QName("xs", "http://www.w3.org/2001/XMLSchema", "nonNegativeInteger");
    public static final QName XS_POSITIVE_INTEGER = new QName("xs", "http://www.w3.org/2001/XMLSchema", "positiveInteger");
    public static final QName XS_UNSIGNED_LONG = new QName("xs", "http://www.w3.org/2001/XMLSchema", "unsignedLong");
    public static final QName XS_UNSIGNED_INT = new QName("xs", "http://www.w3.org/2001/XMLSchema", "unsignedInt");
    public static final QName XS_UNSIGNED_SHORT = new QName("xs", "http://www.w3.org/2001/XMLSchema", "unsignedShort");
    public static final QName XS_UNSIGNED_BYTE = new QName("xs", "http://www.w3.org/2001/XMLSchema", "unsignedByte");
    public static final QName XS_NORMALIZED_STRING = new QName("xs", "http://www.w3.org/2001/XMLSchema", "normalizedString");
    public static final QName XS_TOKEN = new QName("xs", "http://www.w3.org/2001/XMLSchema", "token");
    public static final QName XS_LANGUAGE = new QName("xs", "http://www.w3.org/2001/XMLSchema", "language");
    public static final QName XS_NMTOKEN = new QName("xs", "http://www.w3.org/2001/XMLSchema", "NMTOKEN");
    public static final QName XS_NMTOKENS = new QName("xs", "http://www.w3.org/2001/XMLSchema", "NMTOKENS");
    public static final QName XS_NAME = new QName("xs", "http://www.w3.org/2001/XMLSchema", "Name");
    public static final QName XS_NCNAME = new QName("xs", "http://www.w3.org/2001/XMLSchema", "NCName");
    public static final QName XS_ID = new QName("xs", "http://www.w3.org/2001/XMLSchema", "ID");
    public static final QName XS_IDREF = new QName("xs", "http://www.w3.org/2001/XMLSchema", "IDREF");
    public static final QName XS_IDREFS = new QName("xs", "http://www.w3.org/2001/XMLSchema", "IDREFS");
    public static final QName XS_ENTITY = new QName("xs", "http://www.w3.org/2001/XMLSchema", "ENTITY");
    public static final QName XS_ENTITIES = new QName("xs", "http://www.w3.org/2001/XMLSchema", "ENTITIES");
    public static final QName XS_UNTYPED = new QName("xs", "http://www.w3.org/2001/XMLSchema", "untyped");
    public static final QName XS_UNTYPED_ATOMIC = new QName("xs", "http://www.w3.org/2001/XMLSchema", "untypedAtomic");
    public static final QName XS_ANY_ATOMIC_TYPE = new QName("xs", "http://www.w3.org/2001/XMLSchema", "anyAtomicType");
    public static final QName XS_YEAR_MONTH_DURATION = new QName("xs", "http://www.w3.org/2001/XMLSchema", "yearMonthDuration");
    public static final QName XS_DAY_TIME_DURATION = new QName("xs", "http://www.w3.org/2001/XMLSchema", "dayTimeDuration");
    public static final QName XS_DATE_TIME_STAMP = new QName("xs", "http://www.w3.org/2001/XMLSchema", "dateTimeStamp");

    public QName(String prefix, String uri, String localName) {
        this.sqName = new StructuredQName(prefix, uri, localName);
    }

    public QName(String uri, String lexical) {
        uri = uri == null ? "" : uri;
        int colon = lexical.indexOf(58);
        if (colon < 0) {
            this.sqName = new StructuredQName("", uri, lexical);
        } else {
            String prefix = lexical.substring(0, colon);
            String local = lexical.substring(colon + 1);
            this.sqName = new StructuredQName(prefix, uri, local);
        }
    }

    public QName(String localName) {
        int colon = localName.indexOf(58);
        if (colon >= 0) {
            throw new IllegalArgumentException("Local name contains a colon");
        }
        this.sqName = new StructuredQName("", "", localName);
    }

    public QName(String lexicalQName, XdmNode element) {
        if (lexicalQName.startsWith("{")) {
            lexicalQName = "Q" + lexicalQName;
        }
        try {
            NodeInfo node = element.getUnderlyingValue();
            this.sqName = StructuredQName.fromLexicalQName(lexicalQName, true, true, node.getAllNamespaces());
        } catch (XPathException err) {
            throw new IllegalArgumentException(err);
        }
    }

    public QName(javax.xml.namespace.QName qName) {
        this.sqName = new StructuredQName(qName.getPrefix(), qName.getNamespaceURI(), qName.getLocalPart());
    }

    public QName(StructuredQName sqName) {
        this.sqName = Objects.requireNonNull(sqName);
    }

    public static QName fromClarkName(String expandedName) throws IllegalArgumentException {
        String localName;
        String namespaceURI;
        if (expandedName == null || expandedName.isEmpty()) {
            throw new IllegalArgumentException("Supplied Clark name is null or empty");
        }
        if (expandedName.charAt(0) == '{') {
            int closeBrace = expandedName.indexOf(125);
            if (closeBrace < 0) {
                throw new IllegalArgumentException("No closing '}' in Clark name");
            }
            namespaceURI = expandedName.substring(1, closeBrace);
            if (closeBrace == expandedName.length()) {
                throw new IllegalArgumentException("Missing local part in Clark name");
            }
            localName = expandedName.substring(closeBrace + 1);
        } else {
            namespaceURI = "";
            localName = expandedName;
        }
        return new QName("", namespaceURI, localName);
    }

    public static QName fromEQName(String expandedName) {
        String localName;
        String namespaceURI;
        if (expandedName.charAt(0) == 'Q' && expandedName.charAt(1) == '{') {
            int closeBrace = expandedName.indexOf(125);
            if (closeBrace < 0) {
                throw new IllegalArgumentException("No closing '}' in EQName");
            }
            namespaceURI = expandedName.substring(2, closeBrace);
            if (closeBrace == expandedName.length()) {
                throw new IllegalArgumentException("Missing local part in EQName");
            }
            localName = expandedName.substring(closeBrace + 1);
        } else {
            namespaceURI = "";
            localName = expandedName;
        }
        return new QName("", namespaceURI, localName);
    }

    public boolean isValid(Processor processor) {
        String prefix = this.getPrefix();
        if (prefix.length() > 0 && !NameChecker.isValidNCName(prefix)) {
            return false;
        }
        return NameChecker.isValidNCName(this.getLocalName());
    }

    public String getPrefix() {
        return this.sqName.getPrefix();
    }

    public String getNamespaceURI() {
        return this.sqName.getURI();
    }

    public String getLocalName() {
        return this.sqName.getLocalPart();
    }

    public String getClarkName() {
        String uri = this.getNamespaceURI();
        if (uri.isEmpty()) {
            return this.getLocalName();
        }
        return "{" + uri + "}" + this.getLocalName();
    }

    public String getEQName() {
        String uri = this.getNamespaceURI();
        if (uri.length() == 0) {
            return this.getLocalName();
        }
        return "Q{" + uri + "}" + this.getLocalName();
    }

    public String toString() {
        return this.sqName.getDisplayName();
    }

    public int hashCode() {
        return this.sqName.hashCode();
    }

    public boolean equals(Object other) {
        return other instanceof QName && this.sqName.equals(((QName)other).sqName);
    }

    public StructuredQName getStructuredQName() {
        return this.sqName;
    }
}


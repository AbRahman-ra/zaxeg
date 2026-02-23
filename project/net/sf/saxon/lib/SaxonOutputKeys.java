/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.StringTokenizer;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.QNameException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.value.BigDecimalValue;

public class SaxonOutputKeys {
    public static final String SAXON_XQUERY_METHOD = "Q{http://saxon.sf.net/}xquery";
    public static final String SAXON_BASE64_BINARY_METHOD = "Q{http://saxon.sf.net/}base64Binary";
    public static final String SAXON_HEX_BINARY_METHOD = "Q{http://saxon.sf.net/}hexBinary";
    public static final String SAXON_XML_TO_JSON_METHOD = "Q{http://saxon.sf.net/}xml-to-json";
    public static final String ALLOW_DUPLICATE_NAMES = "allow-duplicate-names";
    public static final String BUILD_TREE = "build-tree";
    public static final String INDENT_SPACES = "{http://saxon.sf.net/}indent-spaces";
    public static final String LINE_LENGTH = "{http://saxon.sf.net/}line-length";
    public static final String SINGLE_QUOTES = "{http://saxon.sf.net/}single-quotes";
    public static final String SUPPRESS_INDENTATION = "suppress-indentation";
    public static final String HTML_VERSION = "html-version";
    public static final String ITEM_SEPARATOR = "item-separator";
    public static final String JSON_NODE_OUTPUT_METHOD = "json-node-output-method";
    public static final String ATTRIBUTE_ORDER = "{http://saxon.sf.net/}attribute-order";
    public static final String CANONICAL = "{http://saxon.sf.net/}canonical";
    public static final String PROPERTY_ORDER = "{http://saxon.sf.net/}property-order";
    public static final String DOUBLE_SPACE = "{http://saxon.sf.net/}double-space";
    public static final String NEWLINE = "{http://saxon.sf.net/}newline";
    public static final String STYLESHEET_VERSION = "{http://saxon.sf.net/}stylesheet-version";
    public static final String USE_CHARACTER_MAPS = "use-character-maps";
    public static final String INCLUDE_CONTENT_TYPE = "include-content-type";
    public static final String UNDECLARE_PREFIXES = "undeclare-prefixes";
    public static final String ESCAPE_URI_ATTRIBUTES = "escape-uri-attributes";
    public static final String CHARACTER_REPRESENTATION = "{http://saxon.sf.net/}character-representation";
    public static final String NEXT_IN_CHAIN = "{http://saxon.sf.net/}next-in-chain";
    public static final String NEXT_IN_CHAIN_BASE_URI = "{http://saxon.sf.net/}next-in-chain-base-uri";
    public static final String PARAMETER_DOCUMENT = "parameter-document";
    public static final String PARAMETER_DOCUMENT_BASE_URI = "{http://saxon.sf.net/}parameter-document-base-uri";
    public static final String BYTE_ORDER_MARK = "byte-order-mark";
    public static final String NORMALIZATION_FORM = "normalization-form";
    public static final String RECOGNIZE_BINARY = "{http://saxon.sf.net/}recognize-binary";
    public static final String REQUIRE_WELL_FORMED = "{http://saxon.sf.net/}require-well-formed";
    public static final String SUPPLY_SOURCE_LOCATOR = "{http://saxon.sf.net/}supply-source-locator";
    public static final String WRAP = "{http://saxon.sf.net/}wrap-result-sequence";
    public static final String UNFAILING = "{http://saxon.sf.net/}unfailing";

    private SaxonOutputKeys() {
    }

    public static String parseListOfNodeNames(String value, NamespaceResolver nsResolver, boolean useDefaultNS, boolean prevalidated, boolean allowStar, String errorCode) throws XPathException {
        StringBuilder s = new StringBuilder();
        StringTokenizer st = new StringTokenizer(value, " \t\n\r", false);
        while (st.hasMoreTokens()) {
            String displayname = st.nextToken();
            if (allowStar && "*".equals(displayname)) {
                s.append(' ').append(displayname);
                continue;
            }
            if (prevalidated || nsResolver == null) {
                s.append(' ').append(displayname);
                continue;
            }
            if (displayname.startsWith("Q{")) {
                s.append(' ').append(displayname);
                continue;
            }
            try {
                String[] parts = NameChecker.getQNameParts(displayname);
                String muri = nsResolver.getURIForPrefix(parts[0], useDefaultNS);
                if (muri == null) {
                    throw new XPathException("Namespace prefix '" + parts[0] + "' has not been declared", errorCode);
                }
                s.append(" Q{").append(muri).append('}').append(parts[1]);
            } catch (QNameException err) {
                throw new XPathException("Invalid QName. " + err.getMessage(), errorCode);
            }
        }
        return s.toString();
    }

    public static boolean isUnstrippedProperty(String key) {
        return ITEM_SEPARATOR.equals(key) || NEWLINE.equals(key);
    }

    public static boolean isXhtmlHtmlVersion5(Properties properties) {
        String htmlVersion = properties.getProperty(HTML_VERSION);
        try {
            return htmlVersion != null && ((BigDecimalValue)BigDecimalValue.makeDecimalValue(htmlVersion, false).asAtomic()).getDecimalValue().equals(BigDecimal.valueOf(5L));
        } catch (ValidationException e) {
            return false;
        }
    }

    public static boolean isHtmlVersion5(Properties properties) {
        String htmlVersion = properties.getProperty(HTML_VERSION);
        if (htmlVersion == null) {
            htmlVersion = properties.getProperty("version");
        }
        if (htmlVersion != null) {
            try {
                return ((BigDecimalValue)BigDecimalValue.makeDecimalValue(htmlVersion, false).asAtomic()).getDecimalValue().equals(BigDecimal.valueOf(5L));
            } catch (ValidationException e) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBuildTree(Properties properties) {
        String buildTreeProperty = properties.getProperty(BUILD_TREE);
        if (buildTreeProperty != null) {
            return "yes".equals(buildTreeProperty);
        }
        String method = properties.getProperty("method");
        return !"json".equals(method) && !"adaptive".equals(method);
    }
}


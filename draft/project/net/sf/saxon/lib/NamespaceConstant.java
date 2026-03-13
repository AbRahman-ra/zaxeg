/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

public class NamespaceConstant {
    public static final String NULL = "";
    public static final String XML = "http://www.w3.org/XML/1998/namespace";
    public static final String XSLT = "http://www.w3.org/1999/XSL/Transform";
    public static final String SAXON = "http://saxon.sf.net/";
    public static final String SAXON6 = "http://icl.com/saxon";
    public static final String SAXON_XSLT_EXPORT = "http://ns.saxonica.com/xslt/export";
    public static final String SCHEMA = "http://www.w3.org/2001/XMLSchema";
    public static final String SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String SCHEMA_VERSIONING = "http://www.w3.org/2007/XMLSchema-versioning";
    public static final String SQL = "http://saxon.sf.net/sql";
    public static final String EXSLT_COMMON = "http://exslt.org/common";
    public static final String EXSLT_MATH = "http://exslt.org/math";
    public static final String EXSLT_SETS = "http://exslt.org/sets";
    public static final String EXSLT_DATES_AND_TIMES = "http://exslt.org/dates-and-times";
    public static final String EXSLT_RANDOM = "http://exslt.org/random";
    public static final String FN = "http://www.w3.org/2005/xpath-functions";
    public static final String OUTPUT = "http://www.w3.org/2010/xslt-xquery-serialization";
    public static final String ERR = "http://www.w3.org/2005/xqt-errors";
    public static final String LOCAL = "http://www.w3.org/2005/xquery-local-functions";
    public static final String MATH = "http://www.w3.org/2005/xpath-functions/math";
    public static final String MAP_FUNCTIONS = "http://www.w3.org/2005/xpath-functions/map";
    public static final String ARRAY_FUNCTIONS = "http://www.w3.org/2005/xpath-functions/array";
    public static final String XHTML = "http://www.w3.org/1999/xhtml";
    public static final String SVG = "http://www.w3.org/2000/svg";
    public static final String MATHML = "http://www.w3.org/1998/Math/MathML";
    public static final String XMLNS = "http://www.w3.org/2000/xmlns/";
    public static final String XLINK = "http://www.w3.org/1999/xlink";
    public static final String XQUERY = "http://www.w3.org/2012/xquery";
    public static final String JAVA_TYPE = "http://saxon.sf.net/java-type";
    public static final String DOT_NET_TYPE = "http://saxon.sf.net/clitype";
    public static final String ANONYMOUS = "http://ns.saxonica.com/anonymous-type";
    public static final String SCM = "http://ns.saxonica.com/schema-component-model";
    public static final String OBJECT_MODEL_SAXON = "http://saxon.sf.net/jaxp/xpath/om";
    public static final String OBJECT_MODEL_XOM = "http://www.xom.nu/jaxp/xpath/xom";
    public static final String OBJECT_MODEL_JDOM = "http://jdom.org/jaxp/xpath/jdom";
    public static final String OBJECT_MODEL_AXIOM = "http://ws.apache.org/jaxp/xpath/axiom";
    public static final String OBJECT_MODEL_DOM4J = "http://www.dom4j.org/jaxp/xpath/dom4j";
    public static final String OBJECT_MODEL_DOT_NET_DOM = "http://saxon.sf.net/object-model/dotnet/dom";
    public static final String OBJECT_MODEL_DOMINO = "http://saxon.sf.net/object-model/domino";
    public static final String CODEPOINT_COLLATION_URI = "http://www.w3.org/2005/xpath-functions/collation/codepoint";
    public static final String HTML5_CASE_BLIND_COLLATION_URI = "http://www.w3.org/2005/xpath-functions/collation/html-ascii-case-insensitive";
    public static final String SAXON_GENERATED_VARIABLE = "http://saxon.sf.net/generated-variable";
    public static final String SAXON_CONFIGURATION = "http://saxon.sf.net/ns/configuration";
    public static final String EXPATH_ZIP = "http://expath.org/ns/zip";
    public static final String GLOBAL_JS = "http://saxonica.com/ns/globalJS";
    public static final String PHP = "http://php.net/xsl";
    public static final String IXSL = "http://saxonica.com/ns/interactiveXSLT";

    private NamespaceConstant() {
    }

    public static String getConventionalPrefix(String uri) {
        switch (uri) {
            case "http://www.w3.org/1999/XSL/Transform": {
                return "xsl";
            }
            case "http://www.w3.org/2005/xpath-functions": {
                return "fn";
            }
            case "http://www.w3.org/XML/1998/namespace": {
                return "xml";
            }
            case "http://www.w3.org/2001/XMLSchema": {
                return "xs";
            }
            case "http://www.w3.org/2001/XMLSchema-instance": {
                return "xsi";
            }
            case "http://saxonica.com/ns/interactiveXSLT": {
                return "ixsl";
            }
            case "http://saxonica.com/ns/globalJS": {
                return "js";
            }
            case "http://saxon.sf.net/": {
                return "saxon";
            }
            case "http://saxon.sf.net/generated-variable": {
                return "vv";
            }
            case "http://www.w3.org/2005/xpath-functions/math": {
                return "math";
            }
            case "http://www.w3.org/2005/xpath-functions/map": {
                return "map";
            }
            case "http://www.w3.org/2005/xpath-functions/array": {
                return "array";
            }
            case "http://www.w3.org/2005/xqt-errors": {
                return "err";
            }
        }
        return null;
    }

    public static String getUriForConventionalPrefix(String prefix) {
        switch (prefix) {
            case "xsl": {
                return XSLT;
            }
            case "fn": {
                return FN;
            }
            case "xml": {
                return XML;
            }
            case "xs": {
                return SCHEMA;
            }
            case "xsi": {
                return SCHEMA_INSTANCE;
            }
            case "err": {
                return ERR;
            }
            case "ixsl": {
                return IXSL;
            }
            case "js": {
                return GLOBAL_JS;
            }
            case "saxon": {
                return SAXON;
            }
            case "vv": {
                return SAXON_GENERATED_VARIABLE;
            }
            case "math": {
                return MATH;
            }
            case "map": {
                return MAP_FUNCTIONS;
            }
            case "array": {
                return ARRAY_FUNCTIONS;
            }
        }
        return null;
    }

    public static boolean isReserved(String uri) {
        return uri != null && (uri.equals(XSLT) || uri.equals(FN) || uri.equals(MATH) || uri.equals(MAP_FUNCTIONS) || uri.equals(ARRAY_FUNCTIONS) || uri.equals(XML) || uri.equals(SCHEMA) || uri.equals(SCHEMA_INSTANCE) || uri.equals(ERR) || uri.equals(XMLNS));
    }

    public static boolean isReservedInQuery31(String uri) {
        return uri.equals(FN) || uri.equals(XML) || uri.equals(SCHEMA) || uri.equals(SCHEMA_INSTANCE) || uri.equals(MATH) || uri.equals(XQUERY) || uri.equals(MAP_FUNCTIONS) || uri.equals(ARRAY_FUNCTIONS);
    }

    public static String findSimilarNamespace(String candidate) {
        if (NamespaceConstant.isSimilar(candidate, XML)) {
            return XML;
        }
        if (NamespaceConstant.isSimilar(candidate, SCHEMA)) {
            return SCHEMA;
        }
        if (NamespaceConstant.isSimilar(candidate, XSLT)) {
            return XSLT;
        }
        if (NamespaceConstant.isSimilar(candidate, SCHEMA_INSTANCE)) {
            return SCHEMA_INSTANCE;
        }
        if (NamespaceConstant.isSimilar(candidate, FN)) {
            return FN;
        }
        if (NamespaceConstant.isSimilar(candidate, SAXON)) {
            return SAXON;
        }
        if (NamespaceConstant.isSimilar(candidate, EXSLT_COMMON)) {
            return EXSLT_COMMON;
        }
        if (NamespaceConstant.isSimilar(candidate, EXSLT_MATH)) {
            return EXSLT_MATH;
        }
        if (NamespaceConstant.isSimilar(candidate, EXSLT_DATES_AND_TIMES)) {
            return EXSLT_DATES_AND_TIMES;
        }
        if (NamespaceConstant.isSimilar(candidate, EXSLT_RANDOM)) {
            return EXSLT_RANDOM;
        }
        if (NamespaceConstant.isSimilar(candidate, XHTML)) {
            return XHTML;
        }
        if (NamespaceConstant.isSimilar(candidate, ERR)) {
            return ERR;
        }
        if (NamespaceConstant.isSimilar(candidate, JAVA_TYPE)) {
            return JAVA_TYPE;
        }
        if (NamespaceConstant.isSimilar(candidate, DOT_NET_TYPE)) {
            return DOT_NET_TYPE;
        }
        return null;
    }

    private static boolean isSimilar(String s1, String s2) {
        if (s1.equalsIgnoreCase(s2)) {
            return true;
        }
        if (s1.startsWith(s2) && s1.length() - s2.length() < 3) {
            return true;
        }
        if (s2.startsWith(s1) && s2.length() - s1.length() < 3) {
            return true;
        }
        if (s1.length() > 8 && Math.abs(s2.length() - s1.length()) < 3) {
            int diff = 0;
            for (int i = 0; i < s1.length(); ++i) {
                char c1 = s1.charAt(i);
                if (i < s2.length() && c1 == s2.charAt(i) || i > 0 && i < s2.length() - 1 && c1 == s2.charAt(i - 1) || i + 1 < s2.length() && c1 == s2.charAt(i + 1)) continue;
                ++diff;
            }
            return diff < 3;
        }
        return false;
    }
}


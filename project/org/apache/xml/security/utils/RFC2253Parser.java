/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils;

import java.io.IOException;
import java.io.StringReader;

public class RFC2253Parser {
    public static String rfc2253toXMLdsig(String dn) {
        String normalized = RFC2253Parser.normalize(dn, true);
        return RFC2253Parser.rfctoXML(normalized);
    }

    public static String xmldsigtoRFC2253(String dn) {
        String normalized = RFC2253Parser.normalize(dn, false);
        return RFC2253Parser.xmltoRFC(normalized);
    }

    public static String normalize(String dn) {
        return RFC2253Parser.normalize(dn, true);
    }

    public static String normalize(String dn, boolean toXml) {
        if (dn == null || dn.equals("")) {
            return "";
        }
        try {
            int k;
            String DN = RFC2253Parser.semicolonToComma(dn);
            StringBuilder sb = new StringBuilder();
            int i = 0;
            int l = 0;
            int j = 0;
            while ((k = DN.indexOf(44, j)) >= 0) {
                if (k > 0 && DN.charAt(k - 1) != '\\' && (l += RFC2253Parser.countQuotes(DN, j, k)) % 2 == 0) {
                    sb.append(RFC2253Parser.parseRDN(DN.substring(i, k).trim(), toXml) + ",");
                    i = k + 1;
                    l = 0;
                }
                j = k + 1;
            }
            sb.append(RFC2253Parser.parseRDN(RFC2253Parser.trim(DN.substring(i)), toXml));
            return sb.toString();
        } catch (IOException ex) {
            return dn;
        }
    }

    static String parseRDN(String str, boolean toXml) throws IOException {
        int k;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int l = 0;
        int j = 0;
        while ((k = str.indexOf(43, j)) >= 0) {
            if (k > 0 && str.charAt(k - 1) != '\\' && (l += RFC2253Parser.countQuotes(str, j, k)) % 2 == 0) {
                sb.append(RFC2253Parser.parseATAV(RFC2253Parser.trim(str.substring(i, k)), toXml) + "+");
                i = k + 1;
                l = 0;
            }
            j = k + 1;
        }
        sb.append(RFC2253Parser.parseATAV(RFC2253Parser.trim(str.substring(i)), toXml));
        return sb.toString();
    }

    static String parseATAV(String str, boolean toXml) throws IOException {
        int i = str.indexOf(61);
        if (i == -1 || i > 0 && str.charAt(i - 1) == '\\') {
            return str;
        }
        String attrType = RFC2253Parser.normalizeAT(str.substring(0, i));
        String attrValue = null;
        attrValue = attrType.charAt(0) >= '0' && attrType.charAt(0) <= '9' ? str.substring(i + 1) : RFC2253Parser.normalizeV(str.substring(i + 1), toXml);
        return attrType + "=" + attrValue;
    }

    static String normalizeAT(String str) {
        String at = str.toUpperCase().trim();
        if (at.startsWith("OID")) {
            at = at.substring(3);
        }
        return at;
    }

    static String normalizeV(String str, boolean toXml) throws IOException {
        String value = RFC2253Parser.trim(str);
        if (value.startsWith("\"")) {
            StringBuilder sb = new StringBuilder();
            StringReader sr = new StringReader(value.substring(1, value.length() - 1));
            int i = 0;
            while ((i = sr.read()) > -1) {
                char c = (char)i;
                if (c == ',' || c == '=' || c == '+' || c == '<' || c == '>' || c == '#' || c == ';') {
                    sb.append('\\');
                }
                sb.append(c);
            }
            value = RFC2253Parser.trim(sb.toString());
        }
        if (toXml) {
            if (value.startsWith("#")) {
                value = '\\' + value;
            }
        } else if (value.startsWith("\\#")) {
            value = value.substring(1);
        }
        return value;
    }

    static String rfctoXML(String string) {
        try {
            String s = RFC2253Parser.changeLess32toXML(string);
            return RFC2253Parser.changeWStoXML(s);
        } catch (Exception e) {
            return string;
        }
    }

    static String xmltoRFC(String string) {
        try {
            String s = RFC2253Parser.changeLess32toRFC(string);
            return RFC2253Parser.changeWStoRFC(s);
        } catch (Exception e) {
            return string;
        }
    }

    static String changeLess32toRFC(String string) throws IOException {
        StringBuilder sb = new StringBuilder();
        StringReader sr = new StringReader(string);
        int i = 0;
        while ((i = sr.read()) > -1) {
            char c = (char)i;
            if (c == '\\') {
                sb.append(c);
                char c1 = (char)sr.read();
                char c2 = (char)sr.read();
                if ((c1 >= '0' && c1 <= '9' || c1 >= 'A' && c1 <= 'F' || c1 >= 'a' && c1 <= 'f') && (c2 >= '0' && c2 <= '9' || c2 >= 'A' && c2 <= 'F' || c2 >= 'a' && c2 <= 'f')) {
                    char ch = (char)Byte.parseByte("" + c1 + c2, 16);
                    sb.append(ch);
                    continue;
                }
                sb.append(c1);
                sb.append(c2);
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    static String changeLess32toXML(String string) throws IOException {
        StringBuilder sb = new StringBuilder();
        StringReader sr = new StringReader(string);
        int i = 0;
        while ((i = sr.read()) > -1) {
            if (i < 32) {
                sb.append('\\');
                sb.append(Integer.toHexString(i));
                continue;
            }
            sb.append((char)i);
        }
        return sb.toString();
    }

    static String changeWStoXML(String string) throws IOException {
        StringBuilder sb = new StringBuilder();
        StringReader sr = new StringReader(string);
        int i = 0;
        while ((i = sr.read()) > -1) {
            char c = (char)i;
            if (c == '\\') {
                char c1 = (char)sr.read();
                if (c1 == ' ') {
                    sb.append('\\');
                    String s = "20";
                    sb.append(s);
                    continue;
                }
                sb.append('\\');
                sb.append(c1);
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    static String changeWStoRFC(String string) {
        int k;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int j = 0;
        while ((k = string.indexOf("\\20", j)) >= 0) {
            sb.append(RFC2253Parser.trim(string.substring(i, k)) + "\\ ");
            i = k + 3;
            j = k + 3;
        }
        sb.append(string.substring(i));
        return sb.toString();
    }

    static String semicolonToComma(String str) {
        return RFC2253Parser.removeWSandReplace(str, ";", ",");
    }

    static String removeWhiteSpace(String str, String symbol) {
        return RFC2253Parser.removeWSandReplace(str, symbol, symbol);
    }

    static String removeWSandReplace(String str, String symbol, String replace) {
        int k;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int l = 0;
        int j = 0;
        while ((k = str.indexOf(symbol, j)) >= 0) {
            if (k > 0 && str.charAt(k - 1) != '\\' && (l += RFC2253Parser.countQuotes(str, j, k)) % 2 == 0) {
                sb.append(RFC2253Parser.trim(str.substring(i, k)) + replace);
                i = k + 1;
                l = 0;
            }
            j = k + 1;
        }
        sb.append(RFC2253Parser.trim(str.substring(i)));
        return sb.toString();
    }

    private static int countQuotes(String s, int i, int j) {
        int k = 0;
        for (int l = i; l < j; ++l) {
            if (s.charAt(l) != '\"') continue;
            ++k;
        }
        return k;
    }

    static String trim(String str) {
        String trimed = str.trim();
        int i = str.indexOf(trimed) + trimed.length();
        if (str.length() > i && trimed.endsWith("\\") && !trimed.endsWith("\\\\") && str.charAt(i) == ' ') {
            trimed = trimed + " ";
        }
        return trimed;
    }
}


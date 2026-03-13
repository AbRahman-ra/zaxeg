/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.om.QNameException;
import net.sf.saxon.serialize.charcode.UTF16CharacterSet;
import net.sf.saxon.serialize.charcode.XMLCharacterData;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;

public abstract class NameChecker {
    public static boolean isQName(String name) {
        int colon = name.indexOf(58);
        if (colon < 0) {
            return NameChecker.isValidNCName(name);
        }
        return colon != 0 && colon != name.length() - 1 && NameChecker.isValidNCName(name.substring(0, colon)) && NameChecker.isValidNCName(name.substring(colon + 1));
    }

    public static String getPrefix(String qname) {
        int colon = qname.indexOf(58);
        if (colon < 0) {
            return "";
        }
        return qname.substring(0, colon);
    }

    public static String[] getQNameParts(CharSequence qname) throws QNameException {
        String[] parts = new String[2];
        int colon = -1;
        int len = qname.length();
        for (int i = 0; i < len; ++i) {
            if (qname.charAt(i) != ':') continue;
            colon = i;
            break;
        }
        if (colon < 0) {
            parts[0] = "";
            parts[1] = qname.toString();
            if (!NameChecker.isValidNCName(parts[1])) {
                throw new QNameException("Invalid QName " + Err.wrap(qname));
            }
        } else {
            if (colon == 0) {
                throw new QNameException("QName cannot start with colon: " + Err.wrap(qname));
            }
            if (colon == len - 1) {
                throw new QNameException("QName cannot end with colon: " + Err.wrap(qname));
            }
            parts[0] = qname.subSequence(0, colon).toString();
            parts[1] = qname.subSequence(colon + 1, len).toString();
            if (!NameChecker.isValidNCName(parts[1])) {
                if (!NameChecker.isValidNCName(parts[0])) {
                    throw new QNameException("Both the prefix " + Err.wrap(parts[0]) + " and the local part " + Err.wrap(parts[1]) + " are invalid");
                }
                throw new QNameException("Invalid QName local part " + Err.wrap(parts[1]));
            }
        }
        return parts;
    }

    public static String[] checkQNameParts(CharSequence qname) throws XPathException {
        try {
            String[] parts = NameChecker.getQNameParts(qname);
            if (parts[0].length() > 0 && !NameChecker.isValidNCName(parts[0])) {
                throw new XPathException("Invalid QName prefix " + Err.wrap(parts[0]));
            }
            return parts;
        } catch (QNameException e) {
            XPathException err = new XPathException(e.getMessage());
            err.setErrorCode("FORG0001");
            throw err;
        }
    }

    public static boolean isValidNCName(CharSequence ncName) {
        if (ncName.length() == 0) {
            return false;
        }
        int s = 1;
        char ch = ncName.charAt(0);
        if (UTF16CharacterSet.isHighSurrogate(ch)) {
            if (!NameChecker.isNCNameStartChar(UTF16CharacterSet.combinePair(ch, ncName.charAt(1)))) {
                return false;
            }
            s = 2;
        } else if (!NameChecker.isNCNameStartChar(ch)) {
            return false;
        }
        for (int i = s; i < ncName.length(); ++i) {
            ch = ncName.charAt(i);
            if (!(UTF16CharacterSet.isHighSurrogate(ch) ? !NameChecker.isNCNameChar(UTF16CharacterSet.combinePair(ch, ncName.charAt(++i))) : !NameChecker.isNCNameChar(ch))) continue;
            return false;
        }
        return true;
    }

    public static boolean isValidNmtoken(CharSequence nmtoken) {
        if (nmtoken.length() == 0) {
            return false;
        }
        for (int i = 0; i < nmtoken.length(); ++i) {
            char ch = nmtoken.charAt(i);
            if (!(UTF16CharacterSet.isHighSurrogate(ch) ? !NameChecker.isNCNameChar(UTF16CharacterSet.combinePair(ch, nmtoken.charAt(++i))) : ch != ':' && !NameChecker.isNCNameChar(ch))) continue;
            return false;
        }
        return true;
    }

    public static boolean isNCNameChar(int ch) {
        return XMLCharacterData.isNCName11(ch);
    }

    public static boolean isNCNameStartChar(int ch) {
        return XMLCharacterData.isNCNameStart11(ch);
    }
}


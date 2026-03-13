/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.x500.style;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.DERUniversalString;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.X500NameStyle;
import org.bouncycastle.asn1.x500.style.X500NameTokenizer;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Hex;

public class IETFUtils {
    private static String unescape(String string) {
        if (string.length() == 0 || string.indexOf(92) < 0 && string.indexOf(34) < 0) {
            return string.trim();
        }
        char[] cArray = string.toCharArray();
        boolean bl = false;
        boolean bl2 = false;
        StringBuffer stringBuffer = new StringBuffer(string.length());
        int n = 0;
        if (cArray[0] == '\\' && cArray[1] == '#') {
            n = 2;
            stringBuffer.append("\\#");
        }
        boolean bl3 = false;
        int n2 = 0;
        char c = '\u0000';
        for (int i = n; i != cArray.length; ++i) {
            char c2 = cArray[i];
            if (c2 != ' ') {
                bl3 = true;
            }
            if (c2 == '\"') {
                if (!bl) {
                    bl2 = !bl2;
                } else {
                    stringBuffer.append(c2);
                }
                bl = false;
                continue;
            }
            if (c2 == '\\' && !bl && !bl2) {
                bl = true;
                n2 = stringBuffer.length();
                continue;
            }
            if (c2 == ' ' && !bl && !bl3) continue;
            if (bl && IETFUtils.isHexDigit(c2)) {
                if (c != '\u0000') {
                    stringBuffer.append((char)(IETFUtils.convertHex(c) * 16 + IETFUtils.convertHex(c2)));
                    bl = false;
                    c = '\u0000';
                    continue;
                }
                c = c2;
                continue;
            }
            stringBuffer.append(c2);
            bl = false;
        }
        if (stringBuffer.length() > 0) {
            while (stringBuffer.charAt(stringBuffer.length() - 1) == ' ' && n2 != stringBuffer.length() - 1) {
                stringBuffer.setLength(stringBuffer.length() - 1);
            }
        }
        return stringBuffer.toString();
    }

    private static boolean isHexDigit(char c) {
        return '0' <= c && c <= '9' || 'a' <= c && c <= 'f' || 'A' <= c && c <= 'F';
    }

    private static int convertHex(char c) {
        if ('0' <= c && c <= '9') {
            return c - 48;
        }
        if ('a' <= c && c <= 'f') {
            return c - 97 + 10;
        }
        return c - 65 + 10;
    }

    public static RDN[] rDNsFromString(String string, X500NameStyle x500NameStyle) {
        X500NameTokenizer x500NameTokenizer = new X500NameTokenizer(string);
        X500NameBuilder x500NameBuilder = new X500NameBuilder(x500NameStyle);
        while (x500NameTokenizer.hasMoreTokens()) {
            Object object;
            String string2;
            Object object2;
            X500NameTokenizer x500NameTokenizer2;
            String string3 = x500NameTokenizer.nextToken();
            if (string3.indexOf(43) > 0) {
                x500NameTokenizer2 = new X500NameTokenizer(string3, '+');
                object2 = new X500NameTokenizer(x500NameTokenizer2.nextToken(), '=');
                string2 = ((X500NameTokenizer)object2).nextToken();
                if (!((X500NameTokenizer)object2).hasMoreTokens()) {
                    throw new IllegalArgumentException("badly formatted directory string");
                }
                object = ((X500NameTokenizer)object2).nextToken();
                ASN1ObjectIdentifier aSN1ObjectIdentifier = x500NameStyle.attrNameToOID(string2.trim());
                if (x500NameTokenizer2.hasMoreTokens()) {
                    Vector<ASN1ObjectIdentifier> vector = new Vector<ASN1ObjectIdentifier>();
                    Vector<String> vector2 = new Vector<String>();
                    vector.addElement(aSN1ObjectIdentifier);
                    vector2.addElement(IETFUtils.unescape((String)object));
                    while (x500NameTokenizer2.hasMoreTokens()) {
                        object2 = new X500NameTokenizer(x500NameTokenizer2.nextToken(), '=');
                        string2 = ((X500NameTokenizer)object2).nextToken();
                        if (!((X500NameTokenizer)object2).hasMoreTokens()) {
                            throw new IllegalArgumentException("badly formatted directory string");
                        }
                        object = ((X500NameTokenizer)object2).nextToken();
                        aSN1ObjectIdentifier = x500NameStyle.attrNameToOID(string2.trim());
                        vector.addElement(aSN1ObjectIdentifier);
                        vector2.addElement(IETFUtils.unescape((String)object));
                    }
                    x500NameBuilder.addMultiValuedRDN(IETFUtils.toOIDArray(vector), IETFUtils.toValueArray(vector2));
                    continue;
                }
                x500NameBuilder.addRDN(aSN1ObjectIdentifier, IETFUtils.unescape((String)object));
                continue;
            }
            x500NameTokenizer2 = new X500NameTokenizer(string3, '=');
            object2 = x500NameTokenizer2.nextToken();
            if (!x500NameTokenizer2.hasMoreTokens()) {
                throw new IllegalArgumentException("badly formatted directory string");
            }
            string2 = x500NameTokenizer2.nextToken();
            object = x500NameStyle.attrNameToOID(((String)object2).trim());
            x500NameBuilder.addRDN((ASN1ObjectIdentifier)object, IETFUtils.unescape(string2));
        }
        return x500NameBuilder.build().getRDNs();
    }

    private static String[] toValueArray(Vector vector) {
        String[] stringArray = new String[vector.size()];
        for (int i = 0; i != stringArray.length; ++i) {
            stringArray[i] = (String)vector.elementAt(i);
        }
        return stringArray;
    }

    private static ASN1ObjectIdentifier[] toOIDArray(Vector vector) {
        ASN1ObjectIdentifier[] aSN1ObjectIdentifierArray = new ASN1ObjectIdentifier[vector.size()];
        for (int i = 0; i != aSN1ObjectIdentifierArray.length; ++i) {
            aSN1ObjectIdentifierArray[i] = (ASN1ObjectIdentifier)vector.elementAt(i);
        }
        return aSN1ObjectIdentifierArray;
    }

    public static String[] findAttrNamesForOID(ASN1ObjectIdentifier aSN1ObjectIdentifier, Hashtable hashtable) {
        int n = 0;
        String[] stringArray = hashtable.elements();
        while (stringArray.hasMoreElements()) {
            if (!aSN1ObjectIdentifier.equals(stringArray.nextElement())) continue;
            ++n;
        }
        stringArray = new String[n];
        n = 0;
        Enumeration enumeration = hashtable.keys();
        while (enumeration.hasMoreElements()) {
            String string = (String)enumeration.nextElement();
            if (!aSN1ObjectIdentifier.equals(hashtable.get(string))) continue;
            stringArray[n++] = string;
        }
        return stringArray;
    }

    public static ASN1ObjectIdentifier decodeAttrName(String string, Hashtable hashtable) {
        if (Strings.toUpperCase(string).startsWith("OID.")) {
            return new ASN1ObjectIdentifier(string.substring(4));
        }
        if (string.charAt(0) >= '0' && string.charAt(0) <= '9') {
            return new ASN1ObjectIdentifier(string);
        }
        ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier)hashtable.get(Strings.toLowerCase(string));
        if (aSN1ObjectIdentifier == null) {
            throw new IllegalArgumentException("Unknown object id - " + string + " - passed to distinguished name");
        }
        return aSN1ObjectIdentifier;
    }

    public static ASN1Encodable valueFromHexString(String string, int n) throws IOException {
        byte[] byArray = new byte[(string.length() - n) / 2];
        for (int i = 0; i != byArray.length; ++i) {
            char c = string.charAt(i * 2 + n);
            char c2 = string.charAt(i * 2 + n + 1);
            byArray[i] = (byte)(IETFUtils.convertHex(c) << 4 | IETFUtils.convertHex(c2));
        }
        return ASN1Primitive.fromByteArray(byArray);
    }

    public static void appendRDN(StringBuffer stringBuffer, RDN rDN, Hashtable hashtable) {
        if (rDN.isMultiValued()) {
            AttributeTypeAndValue[] attributeTypeAndValueArray = rDN.getTypesAndValues();
            boolean bl = true;
            for (int i = 0; i != attributeTypeAndValueArray.length; ++i) {
                if (bl) {
                    bl = false;
                } else {
                    stringBuffer.append('+');
                }
                IETFUtils.appendTypeAndValue(stringBuffer, attributeTypeAndValueArray[i], hashtable);
            }
        } else if (rDN.getFirst() != null) {
            IETFUtils.appendTypeAndValue(stringBuffer, rDN.getFirst(), hashtable);
        }
    }

    public static void appendTypeAndValue(StringBuffer stringBuffer, AttributeTypeAndValue attributeTypeAndValue, Hashtable hashtable) {
        String string = (String)hashtable.get(attributeTypeAndValue.getType());
        if (string != null) {
            stringBuffer.append(string);
        } else {
            stringBuffer.append(attributeTypeAndValue.getType().getId());
        }
        stringBuffer.append('=');
        stringBuffer.append(IETFUtils.valueToString(attributeTypeAndValue.getValue()));
    }

    public static String valueToString(ASN1Encodable aSN1Encodable) {
        StringBuffer stringBuffer = new StringBuffer();
        if (aSN1Encodable instanceof ASN1String && !(aSN1Encodable instanceof DERUniversalString)) {
            String string = ((ASN1String)((Object)aSN1Encodable)).getString();
            if (string.length() > 0 && string.charAt(0) == '#') {
                stringBuffer.append('\\');
            }
            stringBuffer.append(string);
        } else {
            try {
                stringBuffer.append('#');
                stringBuffer.append(Hex.toHexString(aSN1Encodable.toASN1Primitive().getEncoded("DER")));
            } catch (IOException iOException) {
                throw new IllegalArgumentException("Other value has no encoded form");
            }
        }
        int n = stringBuffer.length();
        int n2 = 0;
        if (stringBuffer.length() >= 2 && stringBuffer.charAt(0) == '\\' && stringBuffer.charAt(1) == '#') {
            n2 += 2;
        }
        block5: while (n2 != n) {
            switch (stringBuffer.charAt(n2)) {
                case '\"': 
                case '+': 
                case ',': 
                case ';': 
                case '<': 
                case '=': 
                case '>': 
                case '\\': {
                    stringBuffer.insert(n2, "\\");
                    n2 += 2;
                    ++n;
                    continue block5;
                }
            }
            ++n2;
        }
        if (stringBuffer.length() > 0) {
            for (int i = 0; stringBuffer.length() > i && stringBuffer.charAt(i) == ' '; i += 2) {
                stringBuffer.insert(i, "\\");
            }
        }
        for (int i = stringBuffer.length() - 1; i >= 0 && stringBuffer.charAt(i) == ' '; --i) {
            stringBuffer.insert(i, '\\');
        }
        return stringBuffer.toString();
    }

    public static String canonicalize(String string) {
        int n;
        int n2;
        int n3;
        ASN1Primitive aSN1Primitive;
        if (string.length() > 0 && string.charAt(0) == '#' && (aSN1Primitive = IETFUtils.decodeObject(string)) instanceof ASN1String) {
            string = ((ASN1String)((Object)aSN1Primitive)).getString();
        }
        if ((n3 = (string = Strings.toLowerCase(string)).length()) < 2) {
            return string;
        }
        int n4 = n3 - 1;
        for (n2 = 0; n2 < n4 && string.charAt(n2) == '\\' && string.charAt(n2 + 1) == ' '; n2 += 2) {
        }
        int n5 = n2 + 1;
        for (n = n4; n > n5 && string.charAt(n - 1) == '\\' && string.charAt(n) == ' '; n -= 2) {
        }
        if (n2 > 0 || n < n4) {
            string = string.substring(n2, n + 1);
        }
        return IETFUtils.stripInternalSpaces(string);
    }

    public static String canonicalString(ASN1Encodable aSN1Encodable) {
        return IETFUtils.canonicalize(IETFUtils.valueToString(aSN1Encodable));
    }

    private static ASN1Primitive decodeObject(String string) {
        try {
            return ASN1Primitive.fromByteArray(Hex.decodeStrict(string, 1, string.length() - 1));
        } catch (IOException iOException) {
            throw new IllegalStateException("unknown encoding in name: " + iOException);
        }
    }

    public static String stripInternalSpaces(String string) {
        if (string.indexOf("  ") < 0) {
            return string;
        }
        StringBuffer stringBuffer = new StringBuffer();
        char c = string.charAt(0);
        stringBuffer.append(c);
        for (int i = 1; i < string.length(); ++i) {
            char c2 = string.charAt(i);
            if (c == ' ' && c2 == ' ') continue;
            stringBuffer.append(c2);
            c = c2;
        }
        return stringBuffer.toString();
    }

    public static boolean rDNAreEqual(RDN rDN, RDN rDN2) {
        AttributeTypeAndValue[] attributeTypeAndValueArray;
        if (rDN.size() != rDN2.size()) {
            return false;
        }
        AttributeTypeAndValue[] attributeTypeAndValueArray2 = rDN.getTypesAndValues();
        if (attributeTypeAndValueArray2.length != (attributeTypeAndValueArray = rDN2.getTypesAndValues()).length) {
            return false;
        }
        for (int i = 0; i != attributeTypeAndValueArray2.length; ++i) {
            if (IETFUtils.atvAreEqual(attributeTypeAndValueArray2[i], attributeTypeAndValueArray[i])) continue;
            return false;
        }
        return true;
    }

    private static boolean atvAreEqual(AttributeTypeAndValue attributeTypeAndValue, AttributeTypeAndValue attributeTypeAndValue2) {
        String string;
        ASN1ObjectIdentifier aSN1ObjectIdentifier;
        if (attributeTypeAndValue == attributeTypeAndValue2) {
            return true;
        }
        if (null == attributeTypeAndValue || null == attributeTypeAndValue2) {
            return false;
        }
        ASN1ObjectIdentifier aSN1ObjectIdentifier2 = attributeTypeAndValue.getType();
        if (!aSN1ObjectIdentifier2.equals(aSN1ObjectIdentifier = attributeTypeAndValue2.getType())) {
            return false;
        }
        String string2 = IETFUtils.canonicalString(attributeTypeAndValue.getValue());
        return string2.equals(string = IETFUtils.canonicalString(attributeTypeAndValue2.getValue()));
    }
}


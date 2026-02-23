/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.I18n;
import org.apache.xml.security.utils.UnsyncByteArrayOutputStream;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class Base64 {
    public static final int BASE64DEFAULTLENGTH = 76;
    private static final int BASELENGTH = 255;
    private static final int LOOKUPLENGTH = 64;
    private static final int TWENTYFOURBITGROUP = 24;
    private static final int EIGHTBIT = 8;
    private static final int SIXTEENBIT = 16;
    private static final int FOURBYTE = 4;
    private static final int SIGN = -128;
    private static final char PAD = '=';
    private static final byte[] base64Alphabet;
    private static final char[] lookUpBase64Alphabet;

    private Base64() {
    }

    static final byte[] getBytes(BigInteger big, int bitlen) {
        if ((bitlen = bitlen + 7 >> 3 << 3) < big.bitLength()) {
            throw new IllegalArgumentException(I18n.translate("utils.Base64.IllegalBitlength"));
        }
        byte[] bigBytes = big.toByteArray();
        if (big.bitLength() % 8 != 0 && big.bitLength() / 8 + 1 == bitlen / 8) {
            return bigBytes;
        }
        int startSrc = 0;
        int bigLen = bigBytes.length;
        if (big.bitLength() % 8 == 0) {
            startSrc = 1;
            --bigLen;
        }
        int startDst = bitlen / 8 - bigLen;
        byte[] resizedBytes = new byte[bitlen / 8];
        System.arraycopy(bigBytes, startSrc, resizedBytes, startDst, bigLen);
        return resizedBytes;
    }

    public static final String encode(BigInteger big) {
        return Base64.encode(Base64.getBytes(big, big.bitLength()));
    }

    public static final byte[] encode(BigInteger big, int bitlen) {
        if ((bitlen = bitlen + 7 >> 3 << 3) < big.bitLength()) {
            throw new IllegalArgumentException(I18n.translate("utils.Base64.IllegalBitlength"));
        }
        byte[] bigBytes = big.toByteArray();
        if (big.bitLength() % 8 != 0 && big.bitLength() / 8 + 1 == bitlen / 8) {
            return bigBytes;
        }
        int startSrc = 0;
        int bigLen = bigBytes.length;
        if (big.bitLength() % 8 == 0) {
            startSrc = 1;
            --bigLen;
        }
        int startDst = bitlen / 8 - bigLen;
        byte[] resizedBytes = new byte[bitlen / 8];
        System.arraycopy(bigBytes, startSrc, resizedBytes, startDst, bigLen);
        return resizedBytes;
    }

    public static final BigInteger decodeBigIntegerFromElement(Element element) throws Base64DecodingException {
        return new BigInteger(1, Base64.decode(element));
    }

    public static final BigInteger decodeBigIntegerFromText(Text text) throws Base64DecodingException {
        return new BigInteger(1, Base64.decode(text.getData()));
    }

    public static final void fillElementWithBigInteger(Element element, BigInteger biginteger) {
        String encodedInt = Base64.encode(biginteger);
        if (!XMLUtils.ignoreLineBreaks() && encodedInt.length() > 76) {
            encodedInt = "\n" + encodedInt + "\n";
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(encodedInt);
        element.appendChild(text);
    }

    public static final byte[] decode(Element element) throws Base64DecodingException {
        StringBuffer sb = new StringBuffer();
        for (Node sibling = element.getFirstChild(); sibling != null; sibling = sibling.getNextSibling()) {
            if (sibling.getNodeType() != 3) continue;
            Text t = (Text)sibling;
            sb.append(t.getData());
        }
        return Base64.decode(sb.toString());
    }

    public static final Element encodeToElement(Document doc, String localName, byte[] bytes) {
        Element el = XMLUtils.createElementInSignatureSpace(doc, localName);
        Text text = doc.createTextNode(Base64.encode(bytes));
        el.appendChild(text);
        return el;
    }

    public static final byte[] decode(byte[] base64) throws Base64DecodingException {
        return Base64.decodeInternal(base64, -1);
    }

    public static final String encode(byte[] binaryData) {
        return XMLUtils.ignoreLineBreaks() ? Base64.encode(binaryData, Integer.MAX_VALUE) : Base64.encode(binaryData, 76);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final byte[] decode(BufferedReader reader) throws IOException, Base64DecodingException {
        byte[] retBytes = null;
        UnsyncByteArrayOutputStream baos = null;
        try {
            String line;
            baos = new UnsyncByteArrayOutputStream();
            while (null != (line = reader.readLine())) {
                byte[] bytes = Base64.decode(line);
                baos.write(bytes);
            }
            retBytes = baos.toByteArray();
        } finally {
            baos.close();
        }
        return retBytes;
    }

    protected static final boolean isWhiteSpace(byte octect) {
        return octect == 32 || octect == 13 || octect == 10 || octect == 9;
    }

    protected static final boolean isPad(byte octect) {
        return octect == 61;
    }

    public static final String encode(byte[] binaryData, int length) {
        byte val2;
        byte val1;
        if (length < 4) {
            length = Integer.MAX_VALUE;
        }
        if (binaryData == null) {
            return null;
        }
        int lengthDataBits = binaryData.length * 8;
        if (lengthDataBits == 0) {
            return "";
        }
        int fewerThan24bits = lengthDataBits % 24;
        int numberTriplets = lengthDataBits / 24;
        int numberQuartet = fewerThan24bits != 0 ? numberTriplets + 1 : numberTriplets;
        int quartesPerLine = length / 4;
        int numberLines = (numberQuartet - 1) / quartesPerLine;
        char[] encodedData = null;
        encodedData = new char[numberQuartet * 4 + numberLines];
        byte k = 0;
        byte l = 0;
        byte b1 = 0;
        byte b2 = 0;
        byte b3 = 0;
        int encodedIndex = 0;
        int dataIndex = 0;
        int i = 0;
        for (int line = 0; line < numberLines; ++line) {
            for (int quartet = 0; quartet < 19; ++quartet) {
                b1 = binaryData[dataIndex++];
                b2 = binaryData[dataIndex++];
                b3 = binaryData[dataIndex++];
                l = (byte)(b2 & 0xF);
                k = (byte)(b1 & 3);
                byte val12 = (b1 & 0xFFFFFF80) == 0 ? (byte)(b1 >> 2) : (byte)(b1 >> 2 ^ 0xC0);
                byte val22 = (b2 & 0xFFFFFF80) == 0 ? (byte)(b2 >> 4) : (byte)(b2 >> 4 ^ 0xF0);
                byte val3 = (b3 & 0xFFFFFF80) == 0 ? (byte)(b3 >> 6) : (byte)(b3 >> 6 ^ 0xFC);
                encodedData[encodedIndex++] = lookUpBase64Alphabet[val12];
                encodedData[encodedIndex++] = lookUpBase64Alphabet[val22 | k << 4];
                encodedData[encodedIndex++] = lookUpBase64Alphabet[l << 2 | val3];
                encodedData[encodedIndex++] = lookUpBase64Alphabet[b3 & 0x3F];
                ++i;
            }
            encodedData[encodedIndex++] = 10;
        }
        while (i < numberTriplets) {
            b1 = binaryData[dataIndex++];
            b2 = binaryData[dataIndex++];
            b3 = binaryData[dataIndex++];
            l = (byte)(b2 & 0xF);
            k = (byte)(b1 & 3);
            val1 = (b1 & 0xFFFFFF80) == 0 ? (byte)(b1 >> 2) : (byte)(b1 >> 2 ^ 0xC0);
            val2 = (b2 & 0xFFFFFF80) == 0 ? (byte)(b2 >> 4) : (byte)(b2 >> 4 ^ 0xF0);
            byte val3 = (b3 & 0xFFFFFF80) == 0 ? (byte)(b3 >> 6) : (byte)(b3 >> 6 ^ 0xFC);
            encodedData[encodedIndex++] = lookUpBase64Alphabet[val1];
            encodedData[encodedIndex++] = lookUpBase64Alphabet[val2 | k << 4];
            encodedData[encodedIndex++] = lookUpBase64Alphabet[l << 2 | val3];
            encodedData[encodedIndex++] = lookUpBase64Alphabet[b3 & 0x3F];
            ++i;
        }
        if (fewerThan24bits == 8) {
            b1 = binaryData[dataIndex];
            k = (byte)(b1 & 3);
            val1 = (b1 & 0xFFFFFF80) == 0 ? (byte)(b1 >> 2) : (byte)(b1 >> 2 ^ 0xC0);
            encodedData[encodedIndex++] = lookUpBase64Alphabet[val1];
            encodedData[encodedIndex++] = lookUpBase64Alphabet[k << 4];
            encodedData[encodedIndex++] = 61;
            encodedData[encodedIndex++] = 61;
        } else if (fewerThan24bits == 16) {
            b1 = binaryData[dataIndex];
            b2 = binaryData[dataIndex + 1];
            l = (byte)(b2 & 0xF);
            k = (byte)(b1 & 3);
            val1 = (b1 & 0xFFFFFF80) == 0 ? (byte)(b1 >> 2) : (byte)(b1 >> 2 ^ 0xC0);
            val2 = (b2 & 0xFFFFFF80) == 0 ? (byte)(b2 >> 4) : (byte)(b2 >> 4 ^ 0xF0);
            encodedData[encodedIndex++] = lookUpBase64Alphabet[val1];
            encodedData[encodedIndex++] = lookUpBase64Alphabet[val2 | k << 4];
            encodedData[encodedIndex++] = lookUpBase64Alphabet[l << 2];
            encodedData[encodedIndex++] = 61;
        }
        return new String(encodedData);
    }

    public static final byte[] decode(String encoded) throws Base64DecodingException {
        if (encoded == null) {
            return null;
        }
        byte[] bytes = new byte[encoded.length()];
        int len = Base64.getBytesInternal(encoded, bytes);
        return Base64.decodeInternal(bytes, len);
    }

    protected static final int getBytesInternal(String s, byte[] result) {
        int length = s.length();
        int newSize = 0;
        for (int i = 0; i < length; ++i) {
            byte dataS = (byte)s.charAt(i);
            if (Base64.isWhiteSpace(dataS)) continue;
            result[newSize++] = dataS;
        }
        return newSize;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected static final byte[] decodeInternal(byte[] base64Data, int len) throws Base64DecodingException {
        if (len == -1) {
            len = Base64.removeWhiteSpace(base64Data);
        }
        if (len % 4 != 0) {
            throw new Base64DecodingException("decoding.divisible.four");
        }
        int numberQuadruple = len / 4;
        if (numberQuadruple == 0) {
            return new byte[0];
        }
        byte[] decodedData = null;
        byte b1 = 0;
        byte b2 = 0;
        byte b3 = 0;
        byte b4 = 0;
        int i = 0;
        int encodedIndex = 0;
        int dataIndex = 0;
        dataIndex = (numberQuadruple - 1) * 4;
        encodedIndex = (numberQuadruple - 1) * 3;
        b1 = base64Alphabet[base64Data[dataIndex++]];
        b2 = base64Alphabet[base64Data[dataIndex++]];
        if (b1 == -1 || b2 == -1) {
            throw new Base64DecodingException("decoding.general");
        }
        byte d3 = base64Data[dataIndex++];
        b3 = base64Alphabet[d3];
        byte d4 = base64Data[dataIndex++];
        b4 = base64Alphabet[d4];
        if (b3 == -1 || b4 == -1) {
            if (Base64.isPad(d3) && Base64.isPad(d4)) {
                if ((b2 & 0xF) != 0) {
                    throw new Base64DecodingException("decoding.general");
                }
                decodedData = new byte[encodedIndex + 1];
                decodedData[encodedIndex] = (byte)(b1 << 2 | b2 >> 4);
            } else {
                if (Base64.isPad(d3) || !Base64.isPad(d4)) throw new Base64DecodingException("decoding.general");
                if ((b3 & 3) != 0) {
                    throw new Base64DecodingException("decoding.general");
                }
                decodedData = new byte[encodedIndex + 2];
                decodedData[encodedIndex++] = (byte)(b1 << 2 | b2 >> 4);
                decodedData[encodedIndex] = (byte)((b2 & 0xF) << 4 | b3 >> 2 & 0xF);
            }
        } else {
            decodedData = new byte[encodedIndex + 3];
            decodedData[encodedIndex++] = (byte)(b1 << 2 | b2 >> 4);
            decodedData[encodedIndex++] = (byte)((b2 & 0xF) << 4 | b3 >> 2 & 0xF);
            decodedData[encodedIndex++] = (byte)(b3 << 6 | b4);
        }
        encodedIndex = 0;
        dataIndex = 0;
        for (i = numberQuadruple - 1; i > 0; --i) {
            b1 = base64Alphabet[base64Data[dataIndex++]];
            b2 = base64Alphabet[base64Data[dataIndex++]];
            b3 = base64Alphabet[base64Data[dataIndex++]];
            b4 = base64Alphabet[base64Data[dataIndex++]];
            if (b1 == -1 || b2 == -1 || b3 == -1 || b4 == -1) {
                throw new Base64DecodingException("decoding.general");
            }
            decodedData[encodedIndex++] = (byte)(b1 << 2 | b2 >> 4);
            decodedData[encodedIndex++] = (byte)((b2 & 0xF) << 4 | b3 >> 2 & 0xF);
            decodedData[encodedIndex++] = (byte)(b3 << 6 | b4);
        }
        return decodedData;
    }

    public static final void decode(String base64Data, OutputStream os) throws Base64DecodingException, IOException {
        byte[] bytes = new byte[base64Data.length()];
        int len = Base64.getBytesInternal(base64Data, bytes);
        Base64.decode(bytes, os, len);
    }

    public static final void decode(byte[] base64Data, OutputStream os) throws Base64DecodingException, IOException {
        Base64.decode(base64Data, os, -1);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected static final void decode(byte[] base64Data, OutputStream os, int len) throws Base64DecodingException, IOException {
        if (len == -1) {
            len = Base64.removeWhiteSpace(base64Data);
        }
        if (len % 4 != 0) {
            throw new Base64DecodingException("decoding.divisible.four");
        }
        int numberQuadruple = len / 4;
        if (numberQuadruple == 0) {
            return;
        }
        byte b1 = 0;
        byte b2 = 0;
        byte b3 = 0;
        byte b4 = 0;
        int i = 0;
        int dataIndex = 0;
        for (i = numberQuadruple - 1; i > 0; --i) {
            b1 = base64Alphabet[base64Data[dataIndex++]];
            b2 = base64Alphabet[base64Data[dataIndex++]];
            b3 = base64Alphabet[base64Data[dataIndex++]];
            b4 = base64Alphabet[base64Data[dataIndex++]];
            if (b1 == -1 || b2 == -1 || b3 == -1 || b4 == -1) {
                throw new Base64DecodingException("decoding.general");
            }
            os.write((byte)(b1 << 2 | b2 >> 4));
            os.write((byte)((b2 & 0xF) << 4 | b3 >> 2 & 0xF));
            os.write((byte)(b3 << 6 | b4));
        }
        b1 = base64Alphabet[base64Data[dataIndex++]];
        b2 = base64Alphabet[base64Data[dataIndex++]];
        if (b1 == -1 || b2 == -1) {
            throw new Base64DecodingException("decoding.general");
        }
        byte d3 = base64Data[dataIndex++];
        b3 = base64Alphabet[d3];
        byte d4 = base64Data[dataIndex++];
        b4 = base64Alphabet[d4];
        if (b3 == -1 || b4 == -1) {
            if (Base64.isPad(d3) && Base64.isPad(d4)) {
                if ((b2 & 0xF) != 0) {
                    throw new Base64DecodingException("decoding.general");
                }
                os.write((byte)(b1 << 2 | b2 >> 4));
                return;
            } else {
                if (Base64.isPad(d3) || !Base64.isPad(d4)) throw new Base64DecodingException("decoding.general");
                if ((b3 & 3) != 0) {
                    throw new Base64DecodingException("decoding.general");
                }
                os.write((byte)(b1 << 2 | b2 >> 4));
                os.write((byte)((b2 & 0xF) << 4 | b3 >> 2 & 0xF));
            }
            return;
        } else {
            os.write((byte)(b1 << 2 | b2 >> 4));
            os.write((byte)((b2 & 0xF) << 4 | b3 >> 2 & 0xF));
            os.write((byte)(b3 << 6 | b4));
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static final void decode(InputStream is, OutputStream os) throws Base64DecodingException, IOException {
        int read;
        byte b1 = 0;
        byte b2 = 0;
        byte b3 = 0;
        byte b4 = 0;
        int index = 0;
        byte[] data = new byte[4];
        while ((read = is.read()) > 0) {
            byte readed = (byte)read;
            if (Base64.isWhiteSpace(readed)) continue;
            if (Base64.isPad(readed)) {
                data[index++] = readed;
                if (index != 3) break;
                data[index++] = (byte)is.read();
                break;
            }
            data[index++] = readed;
            if (data[index++] == -1) {
                throw new Base64DecodingException("decoding.general");
            }
            if (index != 4) continue;
            index = 0;
            b1 = base64Alphabet[data[0]];
            b2 = base64Alphabet[data[1]];
            b3 = base64Alphabet[data[2]];
            b4 = base64Alphabet[data[3]];
            os.write((byte)(b1 << 2 | b2 >> 4));
            os.write((byte)((b2 & 0xF) << 4 | b3 >> 2 & 0xF));
            os.write((byte)(b3 << 6 | b4));
        }
        byte d1 = data[0];
        byte d2 = data[1];
        byte d3 = data[2];
        byte d4 = data[3];
        b1 = base64Alphabet[d1];
        b2 = base64Alphabet[d2];
        b3 = base64Alphabet[d3];
        b4 = base64Alphabet[d4];
        if (b3 == -1 || b4 == -1) {
            if (Base64.isPad(d3) && Base64.isPad(d4)) {
                if ((b2 & 0xF) != 0) {
                    throw new Base64DecodingException("decoding.general");
                }
                os.write((byte)(b1 << 2 | b2 >> 4));
                return;
            } else {
                if (Base64.isPad(d3) || !Base64.isPad(d4)) throw new Base64DecodingException("decoding.general");
                b3 = base64Alphabet[d3];
                if ((b3 & 3) != 0) {
                    throw new Base64DecodingException("decoding.general");
                }
                os.write((byte)(b1 << 2 | b2 >> 4));
                os.write((byte)((b2 & 0xF) << 4 | b3 >> 2 & 0xF));
            }
            return;
        } else {
            os.write((byte)(b1 << 2 | b2 >> 4));
            os.write((byte)((b2 & 0xF) << 4 | b3 >> 2 & 0xF));
            os.write((byte)(b3 << 6 | b4));
        }
    }

    protected static final int removeWhiteSpace(byte[] data) {
        if (data == null) {
            return 0;
        }
        int newSize = 0;
        for (byte dataS : data) {
            if (Base64.isWhiteSpace(dataS)) continue;
            data[newSize++] = dataS;
        }
        return newSize;
    }

    static {
        int i;
        base64Alphabet = new byte[255];
        lookUpBase64Alphabet = new char[64];
        for (i = 0; i < 255; ++i) {
            Base64.base64Alphabet[i] = -1;
        }
        for (i = 90; i >= 65; --i) {
            Base64.base64Alphabet[i] = (byte)(i - 65);
        }
        for (i = 122; i >= 97; --i) {
            Base64.base64Alphabet[i] = (byte)(i - 97 + 26);
        }
        for (i = 57; i >= 48; --i) {
            Base64.base64Alphabet[i] = (byte)(i - 48 + 52);
        }
        Base64.base64Alphabet[43] = 62;
        Base64.base64Alphabet[47] = 63;
        for (i = 0; i <= 25; ++i) {
            Base64.lookUpBase64Alphabet[i] = (char)(65 + i);
        }
        i = 26;
        int j = 0;
        while (i <= 51) {
            Base64.lookUpBase64Alphabet[i] = (char)(97 + j);
            ++i;
            ++j;
        }
        i = 52;
        j = 0;
        while (i <= 61) {
            Base64.lookUpBase64Alphabet[i] = (char)(48 + j);
            ++i;
            ++j;
        }
        Base64.lookUpBase64Alphabet[62] = 43;
        Base64.lookUpBase64Alphabet[63] = 47;
    }
}


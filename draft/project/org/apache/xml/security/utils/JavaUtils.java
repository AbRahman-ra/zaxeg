/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecurityPermission;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.UnsyncByteArrayOutputStream;

public class JavaUtils {
    private static Log log = LogFactory.getLog(JavaUtils.class);
    private static final SecurityPermission REGISTER_PERMISSION = new SecurityPermission("org.apache.xml.security.register");

    private JavaUtils() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static byte[] getBytesFromFile(String fileName) throws FileNotFoundException, IOException {
        byte[] refBytes = null;
        FileInputStream fisRef = null;
        OutputStream baos = null;
        try {
            int len;
            fisRef = new FileInputStream(fileName);
            baos = new UnsyncByteArrayOutputStream();
            byte[] buf = new byte[1024];
            while ((len = fisRef.read(buf)) > 0) {
                ((UnsyncByteArrayOutputStream)baos).write(buf, 0, len);
            }
            refBytes = ((UnsyncByteArrayOutputStream)baos).toByteArray();
        } finally {
            if (baos != null) {
                baos.close();
            }
            if (fisRef != null) {
                fisRef.close();
            }
        }
        return refBytes;
    }

    public static void writeBytesToFilename(String filename, byte[] bytes) {
        block7: {
            FileOutputStream fos = null;
            try {
                if (filename != null && bytes != null) {
                    File f = new File(filename);
                    fos = new FileOutputStream(f);
                    fos.write(bytes);
                    fos.close();
                } else if (log.isDebugEnabled()) {
                    log.debug("writeBytesToFilename got null byte[] pointed");
                }
            } catch (IOException ex) {
                if (fos == null) break block7;
                try {
                    fos.close();
                } catch (IOException ioe) {
                    if (!log.isDebugEnabled()) break block7;
                    log.debug(ioe);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static byte[] getBytesFromStream(InputStream inputStream) throws IOException {
        UnsyncByteArrayOutputStream baos = null;
        byte[] retBytes = null;
        try {
            int len;
            baos = new UnsyncByteArrayOutputStream();
            byte[] buf = new byte[4096];
            while ((len = inputStream.read(buf)) > 0) {
                baos.write(buf, 0, len);
            }
            retBytes = baos.toByteArray();
        } finally {
            baos.close();
        }
        return retBytes;
    }

    public static byte[] convertDsaASN1toXMLDSIG(byte[] asn1Bytes, int size) throws IOException {
        int sLength;
        int j;
        int rLength;
        int i;
        if (asn1Bytes[0] != 48 || asn1Bytes[1] != asn1Bytes.length - 2 || asn1Bytes[2] != 2) {
            throw new IOException("Invalid ASN.1 format of DSA signature");
        }
        for (i = rLength = asn1Bytes[3]; i > 0 && asn1Bytes[4 + rLength - i] == 0; --i) {
        }
        for (j = sLength = asn1Bytes[5 + rLength]; j > 0 && asn1Bytes[6 + rLength + sLength - j] == 0; --j) {
        }
        if (i > size || asn1Bytes[4 + rLength] != 2 || j > size) {
            throw new IOException("Invalid ASN.1 format of DSA signature");
        }
        byte[] xmldsigBytes = new byte[size * 2];
        System.arraycopy(asn1Bytes, 4 + rLength - i, xmldsigBytes, size - i, i);
        System.arraycopy(asn1Bytes, 6 + rLength + sLength - j, xmldsigBytes, size * 2 - j, j);
        return xmldsigBytes;
    }

    public static byte[] convertDsaXMLDSIGtoASN1(byte[] xmldsigBytes, int size) throws IOException {
        int k;
        int i;
        int totalSize = size * 2;
        if (xmldsigBytes.length != totalSize) {
            throw new IOException("Invalid XMLDSIG format of DSA signature");
        }
        for (i = size; i > 0 && xmldsigBytes[size - i] == 0; --i) {
        }
        int j = i;
        if (xmldsigBytes[size - i] < 0) {
            ++j;
        }
        for (k = size; k > 0 && xmldsigBytes[totalSize - k] == 0; --k) {
        }
        int l = k;
        if (xmldsigBytes[totalSize - k] < 0) {
            ++l;
        }
        byte[] asn1Bytes = new byte[6 + j + l];
        asn1Bytes[0] = 48;
        asn1Bytes[1] = (byte)(4 + j + l);
        asn1Bytes[2] = 2;
        asn1Bytes[3] = (byte)j;
        System.arraycopy(xmldsigBytes, size - i, asn1Bytes, 4 + j - i, i);
        asn1Bytes[4 + j] = 2;
        asn1Bytes[5 + j] = (byte)l;
        System.arraycopy(xmldsigBytes, totalSize - k, asn1Bytes, 6 + j + l - k, k);
        return asn1Bytes;
    }

    public static void checkRegisterPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(REGISTER_PERMISSION);
        }
    }
}


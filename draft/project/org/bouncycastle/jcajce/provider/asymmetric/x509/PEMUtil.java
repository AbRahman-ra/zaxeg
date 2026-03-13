/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.asymmetric.x509;

import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.util.encoders.Base64;

class PEMUtil {
    private final Boundaries[] _supportedBoundaries;

    PEMUtil(String string) {
        this._supportedBoundaries = new Boundaries[]{new Boundaries(string), new Boundaries("X509 " + string), new Boundaries("PKCS7")};
    }

    private String readLine(InputStream inputStream) throws IOException {
        int n;
        StringBuffer stringBuffer = new StringBuffer();
        while (true) {
            if ((n = inputStream.read()) != 13 && n != 10 && n >= 0) {
                stringBuffer.append((char)n);
                continue;
            }
            if (n < 0 || stringBuffer.length() != 0) break;
        }
        if (n < 0) {
            if (stringBuffer.length() == 0) {
                return null;
            }
            return stringBuffer.toString();
        }
        if (n == 13) {
            inputStream.mark(1);
            n = inputStream.read();
            if (n == 10) {
                inputStream.mark(1);
            }
            if (n > 0) {
                inputStream.reset();
            }
        }
        return stringBuffer.toString();
    }

    private Boundaries getBoundaries(String string) {
        for (int i = 0; i != this._supportedBoundaries.length; ++i) {
            Boundaries boundaries = this._supportedBoundaries[i];
            if (!boundaries.isTheExpectedHeader(string) && !boundaries.isTheExpectedFooter(string)) continue;
            return boundaries;
        }
        return null;
    }

    ASN1Sequence readPEMObject(InputStream inputStream, boolean bl) throws IOException {
        String string;
        StringBuffer stringBuffer = new StringBuffer();
        Boundaries boundaries = null;
        while (boundaries == null && (string = this.readLine(inputStream)) != null) {
            boundaries = this.getBoundaries(string);
            if (boundaries == null || boundaries.isTheExpectedHeader(string)) continue;
            throw new IOException("malformed PEM data: found footer where header was expected");
        }
        if (boundaries == null) {
            if (!bl) {
                return null;
            }
            throw new IOException("malformed PEM data: no header found");
        }
        Boundaries boundaries2 = null;
        while (boundaries2 == null && (string = this.readLine(inputStream)) != null) {
            boundaries2 = this.getBoundaries(string);
            if (boundaries2 != null) {
                if (boundaries.isTheExpectedFooter(string)) continue;
                throw new IOException("malformed PEM data: header/footer mismatch");
            }
            stringBuffer.append(string);
        }
        if (boundaries2 == null) {
            throw new IOException("malformed PEM data: no footer found");
        }
        if (stringBuffer.length() != 0) {
            try {
                return ASN1Sequence.getInstance(Base64.decode(stringBuffer.toString()));
            } catch (Exception exception) {
                throw new IOException("malformed PEM data encountered");
            }
        }
        return null;
    }

    private class Boundaries {
        private final String _header;
        private final String _footer;

        private Boundaries(String string) {
            this._header = "-----BEGIN " + string + "-----";
            this._footer = "-----END " + string + "-----";
        }

        public boolean isTheExpectedHeader(String string) {
            return string.startsWith(this._header);
        }

        public boolean isTheExpectedFooter(String string) {
            return string.startsWith(this._footer);
        }
    }
}


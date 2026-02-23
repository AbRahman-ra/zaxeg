/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto;

import java.io.InputStream;
import javax.xml.crypto.Data;

public class OctetStreamData
implements Data {
    private InputStream octetStream;
    private String uri;
    private String mimeType;

    public OctetStreamData(InputStream octetStream) {
        if (octetStream == null) {
            throw new NullPointerException("octetStream is null");
        }
        this.octetStream = octetStream;
    }

    public OctetStreamData(InputStream octetStream, String uri, String mimeType) {
        if (octetStream == null) {
            throw new NullPointerException("octetStream is null");
        }
        this.octetStream = octetStream;
        this.uri = uri;
        this.mimeType = mimeType;
    }

    public InputStream getOctetStream() {
        return this.octetStream;
    }

    public String getURI() {
        return this.uri;
    }

    public String getMimeType() {
        return this.mimeType;
    }
}


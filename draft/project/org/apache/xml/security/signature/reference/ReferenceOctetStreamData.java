/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.signature.reference;

import java.io.InputStream;
import org.apache.xml.security.signature.reference.ReferenceData;

public class ReferenceOctetStreamData
implements ReferenceData {
    private InputStream octetStream;
    private String uri;
    private String mimeType;

    public ReferenceOctetStreamData(InputStream octetStream) {
        if (octetStream == null) {
            throw new NullPointerException("octetStream is null");
        }
        this.octetStream = octetStream;
    }

    public ReferenceOctetStreamData(InputStream octetStream, String uri, String mimeType) {
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


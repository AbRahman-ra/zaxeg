/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.mime;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bouncycastle.mime.Headers;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class MimeWriter {
    protected final Headers headers;

    protected MimeWriter(Headers headers) {
        this.headers = headers;
    }

    public Headers getHeaders() {
        return this.headers;
    }

    public abstract OutputStream getContentStream() throws IOException;

    protected static List<String> mapToLines(Map<String, String> map) {
        ArrayList<String> arrayList = new ArrayList<String>(map.size());
        for (String string : map.keySet()) {
            arrayList.add(string + ": " + map.get(string));
        }
        return arrayList;
    }
}


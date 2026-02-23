/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.mime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bouncycastle.mime.LineReader;
import org.bouncycastle.util.Iterable;
import org.bouncycastle.util.Strings;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Headers
implements Iterable<String> {
    private final Map<String, List> headers = new TreeMap<String, List>(String.CASE_INSENSITIVE_ORDER);
    private final List<String> headersAsPresented;
    private final String contentTransferEncoding;
    private String boundary;
    private boolean multipart;
    private String contentType;
    private Map<String, String> contentTypeParameters;

    private static List<String> parseHeaders(InputStream inputStream) throws IOException {
        String string;
        ArrayList<String> arrayList = new ArrayList<String>();
        LineReader lineReader = new LineReader(inputStream);
        while ((string = lineReader.readLine()) != null && string.length() != 0) {
            arrayList.add(string);
        }
        return arrayList;
    }

    public Headers(InputStream inputStream, String string) throws IOException {
        this(Headers.parseHeaders(inputStream), string);
    }

    public Headers(List<String> list, String string) {
        int n;
        this.headersAsPresented = list;
        String string2 = "";
        Object object = list.iterator();
        while (object.hasNext()) {
            String string3 = object.next();
            if (string3.startsWith(" ") || string3.startsWith("\t")) {
                string2 = string2 + string3.trim();
                continue;
            }
            if (string2.length() != 0) {
                this.put(string2.substring(0, string2.indexOf(58)).trim(), string2.substring(string2.indexOf(58) + 1).trim());
            }
            string2 = string3;
        }
        if (string2.trim().length() != 0) {
            this.put(string2.substring(0, string2.indexOf(58)).trim(), string2.substring(string2.indexOf(58) + 1).trim());
        }
        if ((n = ((String)(object = this.getValues("Content-Type") == null ? "text/plain" : this.getValues("Content-Type")[0])).indexOf(59)) < 0) {
            this.contentType = object;
            this.contentTypeParameters = Collections.EMPTY_MAP;
        } else {
            this.contentType = ((String)object).substring(0, n);
            this.contentTypeParameters = this.createContentTypeParameters(((String)object).substring(n + 1).trim());
        }
        String string4 = this.contentTransferEncoding = this.getValues("Content-Transfer-Encoding") == null ? string : this.getValues("Content-Transfer-Encoding")[0];
        if (this.contentType.indexOf("multipart") >= 0) {
            this.multipart = true;
            String string5 = this.contentTypeParameters.get("boundary");
            this.boundary = string5.substring(1, string5.length() - 1);
        } else {
            this.boundary = null;
            this.multipart = false;
        }
    }

    public Map<String, String> getContentTypeAttributes() {
        return this.contentTypeParameters;
    }

    private Map<String, String> createContentTypeParameters(String string) {
        String[] stringArray = string.split(";");
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<String, String>();
        for (int i = 0; i != stringArray.length; ++i) {
            String string2 = stringArray[i];
            int n = string2.indexOf(61);
            if (n < 0) {
                throw new IllegalArgumentException("malformed Content-Type header");
            }
            linkedHashMap.put(string2.substring(0, n).trim(), string2.substring(n + 1).trim());
        }
        return Collections.unmodifiableMap(linkedHashMap);
    }

    public boolean isMultipart() {
        return this.multipart;
    }

    public String getBoundary() {
        return this.boundary;
    }

    public String getContentType() {
        return this.contentType;
    }

    public String getContentTransferEncoding() {
        return this.contentTransferEncoding;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void put(String string, String string2) {
        Headers headers = this;
        synchronized (headers) {
            KV kV = new KV(string, string2);
            ArrayList<KV> arrayList = this.headers.get(string);
            if (arrayList == null) {
                arrayList = new ArrayList<KV>();
                this.headers.put(string, arrayList);
            }
            arrayList.add(kV);
        }
    }

    public Iterator<String> getNames() {
        return this.headers.keySet().iterator();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String[] getValues(String string) {
        Headers headers = this;
        synchronized (headers) {
            List list = this.headers.get(string);
            if (list == null) {
                return null;
            }
            String[] stringArray = new String[list.size()];
            for (int i = 0; i < list.size(); ++i) {
                stringArray[i] = ((KV)list.get((int)i)).value;
            }
            return stringArray;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isEmpty() {
        Headers headers = this;
        synchronized (headers) {
            return this.headers.isEmpty();
        }
    }

    public boolean containsKey(String string) {
        return this.headers.containsKey(string);
    }

    @Override
    public Iterator<String> iterator() {
        return this.headers.keySet().iterator();
    }

    public void dumpHeaders(OutputStream outputStream) throws IOException {
        Iterator<String> iterator = this.headersAsPresented.iterator();
        while (iterator.hasNext()) {
            outputStream.write(Strings.toUTF8ByteArray(iterator.next().toString()));
            outputStream.write(13);
            outputStream.write(10);
        }
    }

    private class KV {
        public final String key;
        public final String value;

        public KV(String string, String string2) {
            this.key = string;
            this.value = string2;
        }

        public KV(KV kV) {
            this.key = kV.key;
            this.value = kV.value;
        }
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.mime;

import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.mime.BoundaryLimitedInputStream;
import org.bouncycastle.mime.Headers;
import org.bouncycastle.mime.LineReader;
import org.bouncycastle.mime.MimeContext;
import org.bouncycastle.mime.MimeMultipartContext;
import org.bouncycastle.mime.MimeParser;
import org.bouncycastle.mime.MimeParserContext;
import org.bouncycastle.mime.MimeParserListener;
import org.bouncycastle.mime.encoding.Base64InputStream;
import org.bouncycastle.mime.encoding.QuotedPrintableInputStream;

public class BasicMimeParser
implements MimeParser {
    private final InputStream src;
    private final MimeParserContext parserContext;
    private final String defaultContentTransferEncoding;
    private Headers headers;
    private boolean isMultipart = false;
    private final String boundary;

    public BasicMimeParser(InputStream inputStream) throws IOException {
        this(null, new Headers(inputStream, "7bit"), inputStream);
    }

    public BasicMimeParser(MimeParserContext mimeParserContext, InputStream inputStream) throws IOException {
        this(mimeParserContext, new Headers(inputStream, mimeParserContext.getDefaultContentTransferEncoding()), inputStream);
    }

    public BasicMimeParser(Headers headers, InputStream inputStream) {
        this(null, headers, inputStream);
    }

    public BasicMimeParser(MimeParserContext mimeParserContext, Headers headers, InputStream inputStream) {
        if (headers.isMultipart()) {
            this.isMultipart = true;
            this.boundary = headers.getBoundary();
        } else {
            this.boundary = null;
        }
        this.headers = headers;
        this.parserContext = mimeParserContext;
        this.src = inputStream;
        this.defaultContentTransferEncoding = mimeParserContext != null ? mimeParserContext.getDefaultContentTransferEncoding() : "7bit";
    }

    public void parse(MimeParserListener mimeParserListener) throws IOException {
        MimeContext mimeContext = mimeParserListener.createContext(this.parserContext, this.headers);
        if (this.isMultipart) {
            String string;
            MimeMultipartContext mimeMultipartContext = (MimeMultipartContext)mimeContext;
            String string2 = "--" + this.boundary;
            boolean bl = false;
            int n = 0;
            LineReader lineReader = new LineReader(this.src);
            while ((string = lineReader.readLine()) != null && !"--".equals(string)) {
                MimeContext mimeContext2;
                Headers headers;
                InputStream inputStream;
                if (bl) {
                    inputStream = new BoundaryLimitedInputStream(this.src, this.boundary);
                    headers = new Headers(inputStream, this.defaultContentTransferEncoding);
                    mimeContext2 = mimeMultipartContext.createContext(n++);
                    inputStream = mimeContext2.applyContext(headers, inputStream);
                    mimeParserListener.object(this.parserContext, headers, this.processStream(headers, inputStream));
                    if (inputStream.read() < 0) continue;
                    throw new IOException("MIME object not fully processed");
                }
                if (!string2.equals(string)) continue;
                bl = true;
                inputStream = new BoundaryLimitedInputStream(this.src, this.boundary);
                headers = new Headers(inputStream, this.defaultContentTransferEncoding);
                mimeContext2 = mimeMultipartContext.createContext(n++);
                inputStream = mimeContext2.applyContext(headers, inputStream);
                mimeParserListener.object(this.parserContext, headers, this.processStream(headers, inputStream));
                if (inputStream.read() < 0) continue;
                throw new IOException("MIME object not fully processed");
            }
        } else {
            InputStream inputStream = mimeContext.applyContext(this.headers, this.src);
            mimeParserListener.object(this.parserContext, this.headers, this.processStream(this.headers, inputStream));
        }
    }

    public boolean isMultipart() {
        return this.isMultipart;
    }

    private InputStream processStream(Headers headers, InputStream inputStream) {
        if (headers.getContentTransferEncoding().equals("base64")) {
            return new Base64InputStream(inputStream);
        }
        if (headers.getContentTransferEncoding().equals("quoted-printable")) {
            return new QuotedPrintableInputStream(inputStream);
        }
        return inputStream;
    }
}


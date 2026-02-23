/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.mime.smime;

import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.mime.BasicMimeParser;
import org.bouncycastle.mime.Headers;
import org.bouncycastle.mime.MimeParser;
import org.bouncycastle.mime.MimeParserProvider;
import org.bouncycastle.mime.smime.SMimeParserContext;
import org.bouncycastle.mime.smime.SMimeUtils;
import org.bouncycastle.operator.DigestCalculatorProvider;

public class SMimeParserProvider
implements MimeParserProvider {
    private final String defaultContentTransferEncoding;
    private final DigestCalculatorProvider digestCalculatorProvider;

    public SMimeParserProvider(String string, DigestCalculatorProvider digestCalculatorProvider) {
        this.defaultContentTransferEncoding = string;
        this.digestCalculatorProvider = digestCalculatorProvider;
    }

    public MimeParser createParser(InputStream inputStream) throws IOException {
        return new BasicMimeParser(new SMimeParserContext(this.defaultContentTransferEncoding, this.digestCalculatorProvider), SMimeUtils.autoBuffer(inputStream));
    }

    public MimeParser createParser(Headers headers, InputStream inputStream) throws IOException {
        return new BasicMimeParser(new SMimeParserContext(this.defaultContentTransferEncoding, this.digestCalculatorProvider), headers, SMimeUtils.autoBuffer(inputStream));
    }
}


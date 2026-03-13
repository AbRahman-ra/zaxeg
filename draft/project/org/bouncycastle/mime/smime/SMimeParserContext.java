/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.mime.smime;

import org.bouncycastle.mime.MimeParserContext;
import org.bouncycastle.operator.DigestCalculatorProvider;

public class SMimeParserContext
implements MimeParserContext {
    private final String defaultContentTransferEncoding;
    private final DigestCalculatorProvider digestCalculatorProvider;

    public SMimeParserContext(String string, DigestCalculatorProvider digestCalculatorProvider) {
        this.defaultContentTransferEncoding = string;
        this.digestCalculatorProvider = digestCalculatorProvider;
    }

    public String getDefaultContentTransferEncoding() {
        return this.defaultContentTransferEncoding;
    }

    public DigestCalculatorProvider getDigestCalculatorProvider() {
        return this.digestCalculatorProvider;
    }
}


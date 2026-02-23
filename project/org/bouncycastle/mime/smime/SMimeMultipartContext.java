/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.mime.smime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.mime.CanonicalOutputStream;
import org.bouncycastle.mime.Headers;
import org.bouncycastle.mime.MimeContext;
import org.bouncycastle.mime.MimeMultipartContext;
import org.bouncycastle.mime.MimeParserContext;
import org.bouncycastle.mime.smime.SMimeParserContext;
import org.bouncycastle.mime.smime.SMimeUtils;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.io.TeeInputStream;
import org.bouncycastle.util.io.TeeOutputStream;

public class SMimeMultipartContext
implements MimeMultipartContext {
    private final SMimeParserContext parserContext;
    private DigestCalculator[] calculators;

    public SMimeMultipartContext(MimeParserContext mimeParserContext, Headers headers) {
        this.parserContext = (SMimeParserContext)mimeParserContext;
        this.calculators = this.createDigestCalculators(headers);
    }

    DigestCalculator[] getDigestCalculators() {
        return this.calculators;
    }

    OutputStream getDigestOutputStream() {
        if (this.calculators.length == 1) {
            return this.calculators[0].getOutputStream();
        }
        OutputStream outputStream = this.calculators[0].getOutputStream();
        for (int i = 1; i < this.calculators.length; ++i) {
            outputStream = new TeeOutputStream(this.calculators[i].getOutputStream(), outputStream);
        }
        return outputStream;
    }

    private DigestCalculator[] createDigestCalculators(Headers headers) {
        try {
            Map<String, String> map = headers.getContentTypeAttributes();
            String string = map.get("micalg");
            if (string == null) {
                throw new IllegalStateException("No micalg field on content-type header");
            }
            String[] stringArray = string.substring(string.indexOf(61) + 1).split(",");
            DigestCalculator[] digestCalculatorArray = new DigestCalculator[stringArray.length];
            for (int i = 0; i < stringArray.length; ++i) {
                String string2 = SMimeUtils.lessQuotes(stringArray[i]).trim();
                digestCalculatorArray[i] = this.parserContext.getDigestCalculatorProvider().get(new AlgorithmIdentifier(SMimeUtils.getDigestOID(string2)));
            }
            return digestCalculatorArray;
        } catch (OperatorCreationException operatorCreationException) {
            return null;
        }
    }

    public MimeContext createContext(final int n) throws IOException {
        return new MimeContext(){

            public InputStream applyContext(Headers headers, InputStream inputStream) throws IOException {
                if (n == 0) {
                    OutputStream outputStream = SMimeMultipartContext.this.getDigestOutputStream();
                    headers.dumpHeaders(outputStream);
                    outputStream.write(13);
                    outputStream.write(10);
                    return new TeeInputStream(inputStream, new CanonicalOutputStream(SMimeMultipartContext.this.parserContext, headers, outputStream));
                }
                return inputStream;
            }
        };
    }

    public InputStream applyContext(Headers headers, InputStream inputStream) throws IOException {
        return inputStream;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.mime.smime;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bouncycastle.cms.CMSAttributeTableGenerator;
import org.bouncycastle.cms.CMSEnvelopedDataStreamGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.OriginatorInformation;
import org.bouncycastle.cms.RecipientInfoGenerator;
import org.bouncycastle.mime.Headers;
import org.bouncycastle.mime.MimeIOException;
import org.bouncycastle.mime.MimeWriter;
import org.bouncycastle.mime.encoding.Base64OutputStream;
import org.bouncycastle.mime.smime.SMimeUtils;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.util.Strings;

public class SMIMEEnvelopedWriter
extends MimeWriter {
    private final CMSEnvelopedDataStreamGenerator envGen;
    private final OutputEncryptor outEnc;
    private final OutputStream mimeOut;
    private final String contentTransferEncoding;

    private SMIMEEnvelopedWriter(Builder builder, OutputEncryptor outputEncryptor, OutputStream outputStream) {
        super(new Headers(SMIMEEnvelopedWriter.mapToLines(builder.headers), builder.contentTransferEncoding));
        this.envGen = builder.envGen;
        this.contentTransferEncoding = builder.contentTransferEncoding;
        this.outEnc = outputEncryptor;
        this.mimeOut = outputStream;
    }

    public OutputStream getContentStream() throws IOException {
        this.headers.dumpHeaders(this.mimeOut);
        this.mimeOut.write(Strings.toByteArray("\r\n"));
        try {
            OutputStream outputStream = this.mimeOut;
            if ("base64".equals(this.contentTransferEncoding)) {
                outputStream = new Base64OutputStream(outputStream);
            }
            OutputStream outputStream2 = this.envGen.open(SMimeUtils.createUnclosable(outputStream), this.outEnc);
            return new ContentOutputStream(outputStream2, outputStream);
        } catch (CMSException cMSException) {
            throw new MimeIOException(cMSException.getMessage(), cMSException);
        }
    }

    public static class Builder {
        private static final String[] stdHeaders = new String[]{"Content-Type", "Content-Disposition", "Content-Transfer-Encoding", "Content-Description"};
        private static final String[] stdValues = new String[]{"application/pkcs7-mime; name=\"smime.p7m\"; smime-type=enveloped-data", "attachment; filename=\"smime.p7m\"", "base64", "S/MIME Encrypted Message"};
        private final CMSEnvelopedDataStreamGenerator envGen = new CMSEnvelopedDataStreamGenerator();
        private final Map<String, String> headers = new LinkedHashMap<String, String>();
        String contentTransferEncoding = "base64";

        public Builder() {
            for (int i = 0; i != stdHeaders.length; ++i) {
                this.headers.put(stdHeaders[i], stdValues[i]);
            }
        }

        public Builder setBufferSize(int n) {
            this.envGen.setBufferSize(n);
            return this;
        }

        public Builder setUnprotectedAttributeGenerator(CMSAttributeTableGenerator cMSAttributeTableGenerator) {
            this.envGen.setUnprotectedAttributeGenerator(cMSAttributeTableGenerator);
            return this;
        }

        public Builder setOriginatorInfo(OriginatorInformation originatorInformation) {
            this.envGen.setOriginatorInfo(originatorInformation);
            return this;
        }

        public Builder withHeader(String string, String string2) {
            this.headers.put(string, string2);
            return this;
        }

        public Builder addRecipientInfoGenerator(RecipientInfoGenerator recipientInfoGenerator) {
            this.envGen.addRecipientInfoGenerator(recipientInfoGenerator);
            return this;
        }

        public SMIMEEnvelopedWriter build(OutputStream outputStream, OutputEncryptor outputEncryptor) {
            return new SMIMEEnvelopedWriter(this, outputEncryptor, SMimeUtils.autoBuffer(outputStream));
        }
    }

    private class ContentOutputStream
    extends OutputStream {
        private final OutputStream main;
        private final OutputStream backing;

        ContentOutputStream(OutputStream outputStream, OutputStream outputStream2) {
            this.main = outputStream;
            this.backing = outputStream2;
        }

        public void write(byte[] byArray) throws IOException {
            this.main.write(byArray);
        }

        public void write(byte[] byArray, int n, int n2) throws IOException {
            this.main.write(byArray, n, n2);
        }

        public void write(int n) throws IOException {
            this.main.write(n);
        }

        public void close() throws IOException {
            this.main.close();
            if (this.backing != null) {
                this.backing.close();
            }
        }
    }
}


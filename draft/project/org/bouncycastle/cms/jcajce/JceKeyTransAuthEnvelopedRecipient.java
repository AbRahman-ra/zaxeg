/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.cms.jcajce;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.PrivateKey;
import javax.crypto.Cipher;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.RecipientOperator;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipient;
import org.bouncycastle.jcajce.io.CipherInputStream;
import org.bouncycastle.operator.InputAEADDecryptor;

public class JceKeyTransAuthEnvelopedRecipient
extends JceKeyTransRecipient {
    public JceKeyTransAuthEnvelopedRecipient(PrivateKey privateKey) {
        super(privateKey);
    }

    public RecipientOperator getRecipientOperator(AlgorithmIdentifier algorithmIdentifier, final AlgorithmIdentifier algorithmIdentifier2, byte[] byArray) throws CMSException {
        Key key = this.extractSecretKey(algorithmIdentifier, algorithmIdentifier2, byArray);
        final Cipher cipher = this.contentHelper.createContentCipher(key, algorithmIdentifier2);
        return new RecipientOperator(new InputAEADDecryptor(){

            public AlgorithmIdentifier getAlgorithmIdentifier() {
                return algorithmIdentifier2;
            }

            public InputStream getInputStream(InputStream inputStream) {
                return new CipherInputStream(inputStream, cipher);
            }

            public OutputStream getAADStream() {
                return new AADStream(cipher);
            }

            public byte[] getMAC() {
                return new byte[0];
            }
        });
    }

    private static class AADStream
    extends OutputStream {
        private Cipher cipher;
        private byte[] oneByte = new byte[1];

        public AADStream(Cipher cipher) {
            this.cipher = cipher;
        }

        public void write(byte[] byArray, int n, int n2) throws IOException {
            this.cipher.updateAAD(byArray, n, n2);
        }

        public void write(int n) throws IOException {
            this.oneByte[0] = (byte)n;
            this.cipher.updateAAD(this.oneByte);
        }
    }
}


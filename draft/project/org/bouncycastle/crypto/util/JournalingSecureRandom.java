/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.util.Arrays;

public class JournalingSecureRandom
extends SecureRandom {
    private static byte[] EMPTY_TRANSCRIPT = new byte[0];
    private final SecureRandom base;
    private TranscriptStream tOut = new TranscriptStream();
    private byte[] transcript;
    private int index = 0;

    public JournalingSecureRandom() {
        this(CryptoServicesRegistrar.getSecureRandom());
    }

    public JournalingSecureRandom(SecureRandom secureRandom) {
        this.base = secureRandom;
        this.transcript = EMPTY_TRANSCRIPT;
    }

    public JournalingSecureRandom(byte[] byArray, SecureRandom secureRandom) {
        this.base = secureRandom;
        this.transcript = Arrays.clone(byArray);
    }

    public final void nextBytes(byte[] byArray) {
        if (this.index >= this.transcript.length) {
            this.base.nextBytes(byArray);
        } else {
            int n;
            for (n = 0; n != byArray.length && this.index < this.transcript.length; ++n) {
                byArray[n] = this.transcript[this.index++];
            }
            if (n != byArray.length) {
                byte[] byArray2 = new byte[byArray.length - n];
                this.base.nextBytes(byArray2);
                System.arraycopy(byArray2, 0, byArray, n, byArray2.length);
            }
        }
        try {
            this.tOut.write(byArray);
        } catch (IOException iOException) {
            throw new IllegalStateException("unable to record transcript: " + iOException.getMessage());
        }
    }

    public void clear() {
        Arrays.fill(this.transcript, (byte)0);
        this.tOut.clear();
    }

    public void reset() {
        this.index = 0;
        if (this.index == this.transcript.length) {
            this.transcript = this.tOut.toByteArray();
        }
        this.tOut.reset();
    }

    public byte[] getTranscript() {
        return this.tOut.toByteArray();
    }

    public byte[] getFullTranscript() {
        if (this.index == this.transcript.length) {
            return this.tOut.toByteArray();
        }
        return Arrays.clone(this.transcript);
    }

    private class TranscriptStream
    extends ByteArrayOutputStream {
        private TranscriptStream() {
        }

        public void clear() {
            Arrays.fill(this.buf, (byte)0);
        }
    }
}


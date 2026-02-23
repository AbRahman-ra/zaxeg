/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.modes;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.engines.ChaCha7539Engine;
import org.bouncycastle.crypto.macs.Poly1305;
import org.bouncycastle.crypto.modes.AEADCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public class ChaCha20Poly1305
implements AEADCipher {
    private static final int BUF_SIZE = 64;
    private static final int KEY_SIZE = 32;
    private static final int NONCE_SIZE = 12;
    private static final int MAC_SIZE = 16;
    private static final byte[] ZEROES = new byte[15];
    private static final long AAD_LIMIT = -1L;
    private static final long DATA_LIMIT = 274877906880L;
    private final ChaCha7539Engine chacha20;
    private final Mac poly1305;
    private final byte[] key = new byte[32];
    private final byte[] nonce = new byte[12];
    private final byte[] buf = new byte[80];
    private final byte[] mac = new byte[16];
    private byte[] initialAAD;
    private long aadCount;
    private long dataCount;
    private int state = 0;
    private int bufPos;

    public ChaCha20Poly1305() {
        this(new Poly1305());
    }

    public ChaCha20Poly1305(Mac mac) {
        if (null == mac) {
            throw new NullPointerException("'poly1305' cannot be null");
        }
        if (16 != mac.getMacSize()) {
            throw new IllegalArgumentException("'poly1305' must be a 128-bit MAC");
        }
        this.chacha20 = new ChaCha7539Engine();
        this.poly1305 = mac;
    }

    public String getAlgorithmName() {
        return "ChaCha20Poly1305";
    }

    public void init(boolean bl, CipherParameters cipherParameters) throws IllegalArgumentException {
        ParametersWithIV parametersWithIV;
        byte[] byArray;
        KeyParameter keyParameter;
        if (cipherParameters instanceof AEADParameters) {
            AEADParameters aEADParameters = (AEADParameters)cipherParameters;
            int n = aEADParameters.getMacSize();
            if (128 != n) {
                throw new IllegalArgumentException("Invalid value for MAC size: " + n);
            }
            keyParameter = aEADParameters.getKey();
            byArray = aEADParameters.getNonce();
            parametersWithIV = new ParametersWithIV(keyParameter, byArray);
            this.initialAAD = aEADParameters.getAssociatedText();
        } else if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV2 = (ParametersWithIV)cipherParameters;
            keyParameter = (KeyParameter)parametersWithIV2.getParameters();
            byArray = parametersWithIV2.getIV();
            parametersWithIV = parametersWithIV2;
            this.initialAAD = null;
        } else {
            throw new IllegalArgumentException("invalid parameters passed to ChaCha20Poly1305");
        }
        if (null == keyParameter) {
            if (0 == this.state) {
                throw new IllegalArgumentException("Key must be specified in initial init");
            }
        } else if (32 != keyParameter.getKey().length) {
            throw new IllegalArgumentException("Key must be 256 bits");
        }
        if (null == byArray || 12 != byArray.length) {
            throw new IllegalArgumentException("Nonce must be 96 bits");
        }
        if (0 != this.state && bl && Arrays.areEqual(this.nonce, byArray) && (null == keyParameter || Arrays.areEqual(this.key, keyParameter.getKey()))) {
            throw new IllegalArgumentException("cannot reuse nonce for ChaCha20Poly1305 encryption");
        }
        if (null != keyParameter) {
            System.arraycopy(keyParameter.getKey(), 0, this.key, 0, 32);
        }
        System.arraycopy(byArray, 0, this.nonce, 0, 12);
        this.chacha20.init(true, parametersWithIV);
        this.state = bl ? 1 : 5;
        this.reset(true, false);
    }

    public int getOutputSize(int n) {
        int n2 = Math.max(0, n) + this.bufPos;
        switch (this.state) {
            case 5: 
            case 6: 
            case 7: {
                return Math.max(0, n2 - 16);
            }
            case 1: 
            case 2: 
            case 3: {
                return n2 + 16;
            }
        }
        throw new IllegalStateException();
    }

    public int getUpdateOutputSize(int n) {
        int n2 = Math.max(0, n) + this.bufPos;
        switch (this.state) {
            case 5: 
            case 6: 
            case 7: {
                n2 = Math.max(0, n2 - 16);
                break;
            }
            case 1: 
            case 2: 
            case 3: {
                break;
            }
            default: {
                throw new IllegalStateException();
            }
        }
        return n2 - n2 % 64;
    }

    public void processAADByte(byte by) {
        this.checkAAD();
        this.aadCount = this.incrementCount(this.aadCount, 1, -1L);
        this.poly1305.update(by);
    }

    public void processAADBytes(byte[] byArray, int n, int n2) {
        if (null == byArray) {
            throw new NullPointerException("'in' cannot be null");
        }
        if (n < 0) {
            throw new IllegalArgumentException("'inOff' cannot be negative");
        }
        if (n2 < 0) {
            throw new IllegalArgumentException("'len' cannot be negative");
        }
        if (n > byArray.length - n2) {
            throw new DataLengthException("Input buffer too short");
        }
        this.checkAAD();
        if (n2 > 0) {
            this.aadCount = this.incrementCount(this.aadCount, n2, -1L);
            this.poly1305.update(byArray, n, n2);
        }
    }

    public int processByte(byte by, byte[] byArray, int n) throws DataLengthException {
        this.checkData();
        switch (this.state) {
            case 7: {
                this.buf[this.bufPos] = by;
                if (++this.bufPos == this.buf.length) {
                    this.poly1305.update(this.buf, 0, 64);
                    this.processData(this.buf, 0, 64, byArray, n);
                    System.arraycopy(this.buf, 64, this.buf, 0, 16);
                    this.bufPos = 16;
                    return 64;
                }
                return 0;
            }
            case 3: {
                this.buf[this.bufPos] = by;
                if (++this.bufPos == 64) {
                    this.processData(this.buf, 0, 64, byArray, n);
                    this.poly1305.update(byArray, n, 64);
                    this.bufPos = 0;
                    return 64;
                }
                return 0;
            }
        }
        throw new IllegalStateException();
    }

    public int processBytes(byte[] byArray, int n, int n2, byte[] byArray2, int n3) throws DataLengthException {
        if (null == byArray) {
            throw new NullPointerException("'in' cannot be null");
        }
        if (null == byArray2) {
            // empty if block
        }
        if (n < 0) {
            throw new IllegalArgumentException("'inOff' cannot be negative");
        }
        if (n2 < 0) {
            throw new IllegalArgumentException("'len' cannot be negative");
        }
        if (n > byArray.length - n2) {
            throw new DataLengthException("Input buffer too short");
        }
        if (n3 < 0) {
            throw new IllegalArgumentException("'outOff' cannot be negative");
        }
        this.checkData();
        int n4 = 0;
        switch (this.state) {
            case 7: {
                for (int i = 0; i < n2; ++i) {
                    this.buf[this.bufPos] = byArray[n + i];
                    if (++this.bufPos != this.buf.length) continue;
                    this.poly1305.update(this.buf, 0, 64);
                    this.processData(this.buf, 0, 64, byArray2, n3 + n4);
                    System.arraycopy(this.buf, 64, this.buf, 0, 16);
                    this.bufPos = 16;
                    n4 += 64;
                }
                break;
            }
            case 3: {
                if (this.bufPos != 0) {
                    while (n2 > 0) {
                        --n2;
                        this.buf[this.bufPos] = byArray[n++];
                        if (++this.bufPos != 64) continue;
                        this.processData(this.buf, 0, 64, byArray2, n3);
                        this.poly1305.update(byArray2, n3, 64);
                        this.bufPos = 0;
                        n4 = 64;
                        break;
                    }
                }
                while (n2 >= 64) {
                    this.processData(byArray, n, 64, byArray2, n3 + n4);
                    this.poly1305.update(byArray2, n3 + n4, 64);
                    n += 64;
                    n2 -= 64;
                    n4 += 64;
                }
                if (n2 <= 0) break;
                System.arraycopy(byArray, n, this.buf, 0, n2);
                this.bufPos = n2;
                break;
            }
            default: {
                throw new IllegalStateException();
            }
        }
        return n4;
    }

    public int doFinal(byte[] byArray, int n) throws IllegalStateException, InvalidCipherTextException {
        if (null == byArray) {
            throw new NullPointerException("'out' cannot be null");
        }
        if (n < 0) {
            throw new IllegalArgumentException("'outOff' cannot be negative");
        }
        this.checkData();
        Arrays.clear(this.mac);
        int n2 = 0;
        switch (this.state) {
            case 7: {
                if (this.bufPos < 16) {
                    throw new InvalidCipherTextException("data too short");
                }
                n2 = this.bufPos - 16;
                if (n > byArray.length - n2) {
                    throw new OutputLengthException("Output buffer too short");
                }
                if (n2 > 0) {
                    this.poly1305.update(this.buf, 0, n2);
                    this.processData(this.buf, 0, n2, byArray, n);
                }
                this.finishData(8);
                if (Arrays.constantTimeAreEqual(16, this.mac, 0, this.buf, n2)) break;
                throw new InvalidCipherTextException("mac check in ChaCha20Poly1305 failed");
            }
            case 3: {
                n2 = this.bufPos + 16;
                if (n > byArray.length - n2) {
                    throw new OutputLengthException("Output buffer too short");
                }
                if (this.bufPos > 0) {
                    this.processData(this.buf, 0, this.bufPos, byArray, n);
                    this.poly1305.update(byArray, n, this.bufPos);
                }
                this.finishData(4);
                System.arraycopy(this.mac, 0, byArray, n + this.bufPos, 16);
                break;
            }
            default: {
                throw new IllegalStateException();
            }
        }
        this.reset(false, true);
        return n2;
    }

    public byte[] getMac() {
        return Arrays.clone(this.mac);
    }

    public void reset() {
        this.reset(true, true);
    }

    private void checkAAD() {
        switch (this.state) {
            case 5: {
                this.state = 6;
                break;
            }
            case 1: {
                this.state = 2;
                break;
            }
            case 2: 
            case 6: {
                break;
            }
            case 4: {
                throw new IllegalStateException("ChaCha20Poly1305 cannot be reused for encryption");
            }
            default: {
                throw new IllegalStateException();
            }
        }
    }

    private void checkData() {
        switch (this.state) {
            case 5: 
            case 6: {
                this.finishAAD(7);
                break;
            }
            case 1: 
            case 2: {
                this.finishAAD(3);
                break;
            }
            case 3: 
            case 7: {
                break;
            }
            case 4: {
                throw new IllegalStateException("ChaCha20Poly1305 cannot be reused for encryption");
            }
            default: {
                throw new IllegalStateException();
            }
        }
    }

    private void finishAAD(int n) {
        this.padMAC(this.aadCount);
        this.state = n;
    }

    private void finishData(int n) {
        this.padMAC(this.dataCount);
        byte[] byArray = new byte[16];
        Pack.longToLittleEndian(this.aadCount, byArray, 0);
        Pack.longToLittleEndian(this.dataCount, byArray, 8);
        this.poly1305.update(byArray, 0, 16);
        this.poly1305.doFinal(this.mac, 0);
        this.state = n;
    }

    private long incrementCount(long l, int n, long l2) {
        if (l + Long.MIN_VALUE > l2 - (long)n + Long.MIN_VALUE) {
            throw new IllegalStateException("Limit exceeded");
        }
        return l + (long)n;
    }

    private void initMAC() {
        byte[] byArray = new byte[64];
        try {
            this.chacha20.processBytes(byArray, 0, 64, byArray, 0);
            this.poly1305.init(new KeyParameter(byArray, 0, 32));
        } finally {
            Arrays.clear(byArray);
        }
    }

    private void padMAC(long l) {
        int n = (int)l & 0xF;
        if (0 != n) {
            this.poly1305.update(ZEROES, 0, 16 - n);
        }
    }

    private void processData(byte[] byArray, int n, int n2, byte[] byArray2, int n3) {
        if (n3 > byArray2.length - n2) {
            throw new OutputLengthException("Output buffer too short");
        }
        this.chacha20.processBytes(byArray, n, n2, byArray2, n3);
        this.dataCount = this.incrementCount(this.dataCount, n2, 274877906880L);
    }

    private void reset(boolean bl, boolean bl2) {
        Arrays.clear(this.buf);
        if (bl) {
            Arrays.clear(this.mac);
        }
        this.aadCount = 0L;
        this.dataCount = 0L;
        this.bufPos = 0;
        switch (this.state) {
            case 1: 
            case 5: {
                break;
            }
            case 6: 
            case 7: 
            case 8: {
                this.state = 5;
                break;
            }
            case 2: 
            case 3: 
            case 4: {
                this.state = 4;
                return;
            }
            default: {
                throw new IllegalStateException();
            }
        }
        if (bl2) {
            this.chacha20.reset();
        }
        this.initMAC();
        if (null != this.initialAAD) {
            this.processAADBytes(this.initialAAD, 0, this.initialAAD.length);
        }
    }

    private static final class State {
        static final int UNINITIALIZED = 0;
        static final int ENC_INIT = 1;
        static final int ENC_AAD = 2;
        static final int ENC_DATA = 3;
        static final int ENC_FINAL = 4;
        static final int DEC_INIT = 5;
        static final int DEC_AAD = 6;
        static final int DEC_DATA = 7;
        static final int DEC_FINAL = 8;

        private State() {
        }
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bouncycastle.pqc.crypto.lms.Composer;
import org.bouncycastle.pqc.crypto.lms.DigestUtil;
import org.bouncycastle.pqc.crypto.lms.HSS;
import org.bouncycastle.pqc.crypto.lms.HSSPublicKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMS;
import org.bouncycastle.pqc.crypto.lms.LMSContext;
import org.bouncycastle.pqc.crypto.lms.LMSContextBasedSigner;
import org.bouncycastle.pqc.crypto.lms.LMSKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSParameters;
import org.bouncycastle.pqc.crypto.lms.LMSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSSignature;
import org.bouncycastle.pqc.crypto.lms.LMSSignedPubKey;
import org.bouncycastle.pqc.crypto.lms.SeedDerive;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.io.Streams;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class HSSPrivateKeyParameters
extends LMSKeyParameters
implements LMSContextBasedSigner {
    private final int l;
    private final boolean isShard;
    private List<LMSPrivateKeyParameters> keys;
    private List<LMSSignature> sig;
    private final long indexLimit;
    private long index = 0L;
    private HSSPublicKeyParameters publicKey;

    public HSSPrivateKeyParameters(int n, List<LMSPrivateKeyParameters> list, List<LMSSignature> list2, long l, long l2) {
        super(true);
        this.l = n;
        this.keys = Collections.unmodifiableList(list);
        this.sig = Collections.unmodifiableList(list2);
        this.index = l;
        this.indexLimit = l2;
        this.isShard = false;
        this.resetKeyToIndex();
    }

    private HSSPrivateKeyParameters(int n, List<LMSPrivateKeyParameters> list, List<LMSSignature> list2, long l, long l2, boolean bl) {
        super(true);
        this.l = n;
        this.keys = Collections.unmodifiableList(list);
        this.sig = Collections.unmodifiableList(list2);
        this.index = l;
        this.indexLimit = l2;
        this.isShard = bl;
    }

    public static HSSPrivateKeyParameters getInstance(byte[] byArray, byte[] byArray2) throws IOException {
        HSSPrivateKeyParameters hSSPrivateKeyParameters = HSSPrivateKeyParameters.getInstance(byArray);
        hSSPrivateKeyParameters.publicKey = HSSPublicKeyParameters.getInstance(byArray2);
        return hSSPrivateKeyParameters;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static HSSPrivateKeyParameters getInstance(Object object) throws IOException {
        if (object instanceof HSSPrivateKeyParameters) {
            return (HSSPrivateKeyParameters)object;
        }
        if (object instanceof DataInputStream) {
            int n;
            if (((DataInputStream)object).readInt() != 0) {
                throw new IllegalStateException("unknown version for hss private key");
            }
            int n2 = ((DataInputStream)object).readInt();
            long l = ((DataInputStream)object).readLong();
            long l2 = ((DataInputStream)object).readLong();
            boolean bl = ((DataInputStream)object).readBoolean();
            ArrayList<LMSPrivateKeyParameters> arrayList = new ArrayList<LMSPrivateKeyParameters>();
            ArrayList<LMSSignature> arrayList2 = new ArrayList<LMSSignature>();
            for (n = 0; n < n2; ++n) {
                arrayList.add(LMSPrivateKeyParameters.getInstance(object));
            }
            for (n = 0; n < n2 - 1; ++n) {
                arrayList2.add(LMSSignature.getInstance(object));
            }
            return new HSSPrivateKeyParameters(n2, arrayList, arrayList2, l, l2, bl);
        }
        if (object instanceof byte[]) {
            InputStream inputStream = null;
            try {
                inputStream = new DataInputStream(new ByteArrayInputStream((byte[])object));
                HSSPrivateKeyParameters hSSPrivateKeyParameters = HSSPrivateKeyParameters.getInstance(inputStream);
                return hSSPrivateKeyParameters;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
        if (object instanceof InputStream) {
            return HSSPrivateKeyParameters.getInstance(Streams.readAll((InputStream)object));
        }
        throw new IllegalArgumentException("cannot parse " + object);
    }

    public int getL() {
        return this.l;
    }

    public synchronized long getIndex() {
        return this.index;
    }

    public synchronized LMSParameters[] getLMSParameters() {
        int n = this.keys.size();
        LMSParameters[] lMSParametersArray = new LMSParameters[n];
        for (int i = 0; i < n; ++i) {
            LMSPrivateKeyParameters lMSPrivateKeyParameters = this.keys.get(i);
            lMSParametersArray[i] = new LMSParameters(lMSPrivateKeyParameters.getSigParameters(), lMSPrivateKeyParameters.getOtsParameters());
        }
        return lMSParametersArray;
    }

    synchronized void incIndex() {
        ++this.index;
    }

    private static HSSPrivateKeyParameters makeCopy(HSSPrivateKeyParameters hSSPrivateKeyParameters) {
        try {
            return HSSPrivateKeyParameters.getInstance(hSSPrivateKeyParameters.getEncoded());
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void updateHierarchy(LMSPrivateKeyParameters[] lMSPrivateKeyParametersArray, LMSSignature[] lMSSignatureArray) {
        HSSPrivateKeyParameters hSSPrivateKeyParameters = this;
        synchronized (hSSPrivateKeyParameters) {
            this.keys = Collections.unmodifiableList(java.util.Arrays.asList(lMSPrivateKeyParametersArray));
            this.sig = Collections.unmodifiableList(java.util.Arrays.asList(lMSSignatureArray));
        }
    }

    boolean isShard() {
        return this.isShard;
    }

    long getIndexLimit() {
        return this.indexLimit;
    }

    @Override
    public long getUsagesRemaining() {
        return this.indexLimit - this.index;
    }

    LMSPrivateKeyParameters getRootKey() {
        return this.keys.get(0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public HSSPrivateKeyParameters extractKeyShard(int n) {
        HSSPrivateKeyParameters hSSPrivateKeyParameters = this;
        synchronized (hSSPrivateKeyParameters) {
            if (this.getUsagesRemaining() < (long)n) {
                throw new IllegalArgumentException("usageCount exceeds usages remaining in current leaf");
            }
            long l = this.index + (long)n;
            long l2 = this.index;
            this.index += (long)n;
            ArrayList<LMSPrivateKeyParameters> arrayList = new ArrayList<LMSPrivateKeyParameters>(this.getKeys());
            ArrayList<LMSSignature> arrayList2 = new ArrayList<LMSSignature>(this.getSig());
            HSSPrivateKeyParameters hSSPrivateKeyParameters2 = HSSPrivateKeyParameters.makeCopy(new HSSPrivateKeyParameters(this.l, arrayList, arrayList2, l2, l, true));
            this.resetKeyToIndex();
            return hSSPrivateKeyParameters2;
        }
    }

    synchronized List<LMSPrivateKeyParameters> getKeys() {
        return this.keys;
    }

    synchronized List<LMSSignature> getSig() {
        return this.sig;
    }

    void resetKeyToIndex() {
        LMSPrivateKeyParameters[] lMSPrivateKeyParametersArray;
        int n;
        List<LMSPrivateKeyParameters> list = this.getKeys();
        long[] lArray = new long[list.size()];
        long l = this.getIndex();
        for (n = list.size() - 1; n >= 0; --n) {
            lMSPrivateKeyParametersArray = list.get(n).getSigParameters();
            int n2 = (1 << lMSPrivateKeyParametersArray.getH()) - 1;
            lArray[n] = l & (long)n2;
            l >>>= lMSPrivateKeyParametersArray.getH();
        }
        n = 0;
        lMSPrivateKeyParametersArray = list.toArray(new LMSPrivateKeyParameters[list.size()]);
        LMSSignature[] lMSSignatureArray = this.sig.toArray(new LMSSignature[this.sig.size()]);
        LMSPrivateKeyParameters lMSPrivateKeyParameters = this.getRootKey();
        if ((long)(lMSPrivateKeyParametersArray[0].getIndex() - 1) != lArray[0]) {
            lMSPrivateKeyParametersArray[0] = LMS.generateKeys(lMSPrivateKeyParameters.getSigParameters(), lMSPrivateKeyParameters.getOtsParameters(), (int)lArray[0], lMSPrivateKeyParameters.getI(), lMSPrivateKeyParameters.getMasterSecret());
            n = 1;
        }
        for (int i = 1; i < lArray.length; ++i) {
            boolean bl;
            LMSPrivateKeyParameters lMSPrivateKeyParameters2 = lMSPrivateKeyParametersArray[i - 1];
            byte[] byArray = new byte[16];
            byte[] byArray2 = new byte[32];
            SeedDerive seedDerive = new SeedDerive(lMSPrivateKeyParameters2.getI(), lMSPrivateKeyParameters2.getMasterSecret(), DigestUtil.getDigest(lMSPrivateKeyParameters2.getOtsParameters().getDigestOID()));
            seedDerive.setQ((int)lArray[i - 1]);
            seedDerive.setJ(-2);
            seedDerive.deriveSeed(byArray2, true);
            byte[] byArray3 = new byte[32];
            seedDerive.deriveSeed(byArray3, false);
            System.arraycopy(byArray3, 0, byArray, 0, byArray.length);
            boolean bl2 = i < lArray.length - 1 ? lArray[i] == (long)(lMSPrivateKeyParametersArray[i].getIndex() - 1) : lArray[i] == (long)lMSPrivateKeyParametersArray[i].getIndex();
            boolean bl3 = bl = Arrays.areEqual(byArray, lMSPrivateKeyParametersArray[i].getI()) && Arrays.areEqual(byArray2, lMSPrivateKeyParametersArray[i].getMasterSecret());
            if (!bl) {
                lMSPrivateKeyParametersArray[i] = LMS.generateKeys(list.get(i).getSigParameters(), list.get(i).getOtsParameters(), (int)lArray[i], byArray, byArray2);
                lMSSignatureArray[i - 1] = LMS.generateSign(lMSPrivateKeyParametersArray[i - 1], lMSPrivateKeyParametersArray[i].getPublicKey().toByteArray());
                n = 1;
                continue;
            }
            if (bl2) continue;
            lMSPrivateKeyParametersArray[i] = LMS.generateKeys(list.get(i).getSigParameters(), list.get(i).getOtsParameters(), (int)lArray[i], byArray, byArray2);
            n = 1;
        }
        if (n != 0) {
            this.updateHierarchy(lMSPrivateKeyParametersArray, lMSSignatureArray);
        }
    }

    public synchronized HSSPublicKeyParameters getPublicKey() {
        return new HSSPublicKeyParameters(this.l, this.getRootKey().getPublicKey());
    }

    void replaceConsumedKey(int n) {
        SeedDerive seedDerive = this.keys.get(n - 1).getCurrentOTSKey().getDerivationFunction();
        seedDerive.setJ(-2);
        byte[] byArray = new byte[32];
        seedDerive.deriveSeed(byArray, true);
        byte[] byArray2 = new byte[32];
        seedDerive.deriveSeed(byArray2, false);
        byte[] byArray3 = new byte[16];
        System.arraycopy(byArray2, 0, byArray3, 0, byArray3.length);
        ArrayList<LMSPrivateKeyParameters> arrayList = new ArrayList<LMSPrivateKeyParameters>(this.keys);
        LMSPrivateKeyParameters lMSPrivateKeyParameters = this.keys.get(n);
        arrayList.set(n, LMS.generateKeys(lMSPrivateKeyParameters.getSigParameters(), lMSPrivateKeyParameters.getOtsParameters(), 0, byArray3, byArray));
        ArrayList<LMSSignature> arrayList2 = new ArrayList<LMSSignature>(this.sig);
        arrayList2.set(n - 1, LMS.generateSign((LMSPrivateKeyParameters)arrayList.get(n - 1), ((LMSPrivateKeyParameters)arrayList.get(n)).getPublicKey().toByteArray()));
        this.keys = Collections.unmodifiableList(arrayList);
        this.sig = Collections.unmodifiableList(arrayList2);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        HSSPrivateKeyParameters hSSPrivateKeyParameters = (HSSPrivateKeyParameters)object;
        if (this.l != hSSPrivateKeyParameters.l) {
            return false;
        }
        if (this.isShard != hSSPrivateKeyParameters.isShard) {
            return false;
        }
        if (this.indexLimit != hSSPrivateKeyParameters.indexLimit) {
            return false;
        }
        if (this.index != hSSPrivateKeyParameters.index) {
            return false;
        }
        if (!this.keys.equals(hSSPrivateKeyParameters.keys)) {
            return false;
        }
        return this.sig.equals(hSSPrivateKeyParameters.sig);
    }

    @Override
    public synchronized byte[] getEncoded() throws IOException {
        Composer composer = Composer.compose().u32str(0).u32str(this.l).u64str(this.index).u64str(this.indexLimit).bool(this.isShard);
        for (LMSPrivateKeyParameters encodable : this.keys) {
            composer.bytes(encodable);
        }
        for (LMSSignature lMSSignature : this.sig) {
            composer.bytes(lMSSignature);
        }
        return composer.build();
    }

    public int hashCode() {
        int n = this.l;
        n = 31 * n + (this.isShard ? 1 : 0);
        n = 31 * n + this.keys.hashCode();
        n = 31 * n + this.sig.hashCode();
        n = 31 * n + (int)(this.indexLimit ^ this.indexLimit >>> 32);
        n = 31 * n + (int)(this.index ^ this.index >>> 32);
        return n;
    }

    protected Object clone() throws CloneNotSupportedException {
        return HSSPrivateKeyParameters.makeCopy(this);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public LMSContext generateLMSContext() {
        LMSSignedPubKey[] lMSSignedPubKeyArray;
        LMSPrivateKeyParameters lMSPrivateKeyParameters;
        int n = this.getL();
        HSSPrivateKeyParameters hSSPrivateKeyParameters = this;
        synchronized (hSSPrivateKeyParameters) {
            HSS.rangeTestKeys(this);
            List<LMSPrivateKeyParameters> list = this.getKeys();
            List<LMSSignature> list2 = this.getSig();
            lMSPrivateKeyParameters = this.getKeys().get(n - 1);
            lMSSignedPubKeyArray = new LMSSignedPubKey[n - 1];
            for (int i = 0; i < n - 1; ++i) {
                lMSSignedPubKeyArray[i] = new LMSSignedPubKey(list2.get(i), list.get(i + 1).getPublicKey());
            }
            this.incIndex();
        }
        return lMSPrivateKeyParameters.generateLMSContext().withSignedPublicKeys(lMSSignedPubKeyArray);
    }

    @Override
    public byte[] generateSignature(LMSContext lMSContext) {
        try {
            return HSS.generateSignature(this.getL(), lMSContext).getEncoded();
        } catch (IOException iOException) {
            throw new IllegalStateException("unable to encode signature: " + iOException.getMessage(), iOException);
        }
    }
}


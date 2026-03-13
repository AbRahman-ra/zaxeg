/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.util;

import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA224Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.digests.SHA512tDigest;
import org.bouncycastle.crypto.digests.SHAKEDigest;

public final class DigestFactory {
    private static final Map cloneMap = new HashMap();

    public static Digest createMD5() {
        return new MD5Digest();
    }

    public static Digest createSHA1() {
        return new SHA1Digest();
    }

    public static Digest createSHA224() {
        return new SHA224Digest();
    }

    public static Digest createSHA256() {
        return new SHA256Digest();
    }

    public static Digest createSHA384() {
        return new SHA384Digest();
    }

    public static Digest createSHA512() {
        return new SHA512Digest();
    }

    public static Digest createSHA512_224() {
        return new SHA512tDigest(224);
    }

    public static Digest createSHA512_256() {
        return new SHA512tDigest(256);
    }

    public static Digest createSHA3_224() {
        return new SHA3Digest(224);
    }

    public static Digest createSHA3_256() {
        return new SHA3Digest(256);
    }

    public static Digest createSHA3_384() {
        return new SHA3Digest(384);
    }

    public static Digest createSHA3_512() {
        return new SHA3Digest(512);
    }

    public static Digest createSHAKE128() {
        return new SHAKEDigest(128);
    }

    public static Digest createSHAKE256() {
        return new SHAKEDigest(256);
    }

    public static Digest cloneDigest(Digest digest) {
        return ((Cloner)cloneMap.get(digest.getAlgorithmName())).createClone(digest);
    }

    static {
        cloneMap.put(DigestFactory.createMD5().getAlgorithmName(), new Cloner(){

            public Digest createClone(Digest digest) {
                return new MD5Digest((MD5Digest)digest);
            }
        });
        cloneMap.put(DigestFactory.createSHA1().getAlgorithmName(), new Cloner(){

            public Digest createClone(Digest digest) {
                return new MD5Digest((MD5Digest)digest);
            }
        });
        cloneMap.put(DigestFactory.createSHA224().getAlgorithmName(), new Cloner(){

            public Digest createClone(Digest digest) {
                return new SHA224Digest((SHA224Digest)digest);
            }
        });
        cloneMap.put(DigestFactory.createSHA256().getAlgorithmName(), new Cloner(){

            public Digest createClone(Digest digest) {
                return new SHA256Digest((SHA256Digest)digest);
            }
        });
        cloneMap.put(DigestFactory.createSHA384().getAlgorithmName(), new Cloner(){

            public Digest createClone(Digest digest) {
                return new SHA384Digest((SHA384Digest)digest);
            }
        });
        cloneMap.put(DigestFactory.createSHA512().getAlgorithmName(), new Cloner(){

            public Digest createClone(Digest digest) {
                return new SHA512Digest((SHA512Digest)digest);
            }
        });
        cloneMap.put(DigestFactory.createSHA3_224().getAlgorithmName(), new Cloner(){

            public Digest createClone(Digest digest) {
                return new SHA3Digest((SHA3Digest)digest);
            }
        });
        cloneMap.put(DigestFactory.createSHA3_256().getAlgorithmName(), new Cloner(){

            public Digest createClone(Digest digest) {
                return new SHA3Digest((SHA3Digest)digest);
            }
        });
        cloneMap.put(DigestFactory.createSHA3_384().getAlgorithmName(), new Cloner(){

            public Digest createClone(Digest digest) {
                return new SHA3Digest((SHA3Digest)digest);
            }
        });
        cloneMap.put(DigestFactory.createSHA3_512().getAlgorithmName(), new Cloner(){

            public Digest createClone(Digest digest) {
                return new SHA3Digest((SHA3Digest)digest);
            }
        });
        cloneMap.put(DigestFactory.createSHAKE128().getAlgorithmName(), new Cloner(){

            public Digest createClone(Digest digest) {
                return new SHAKEDigest((SHAKEDigest)digest);
            }
        });
        cloneMap.put(DigestFactory.createSHAKE256().getAlgorithmName(), new Cloner(){

            public Digest createClone(Digest digest) {
                return new SHAKEDigest((SHAKEDigest)digest);
            }
        });
    }

    private static interface Cloner {
        public Digest createClone(Digest var1);
    }
}


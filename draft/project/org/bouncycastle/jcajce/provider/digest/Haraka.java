/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.digest;

import org.bouncycastle.crypto.digests.Haraka256Digest;
import org.bouncycastle.crypto.digests.Haraka512Digest;
import org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import org.bouncycastle.jcajce.provider.digest.BCMessageDigest;
import org.bouncycastle.jcajce.provider.digest.DigestAlgorithmProvider;

public class Haraka {
    private Haraka() {
    }

    public static class Digest256
    extends BCMessageDigest
    implements Cloneable {
        public Digest256() {
            super(new Haraka256Digest());
        }

        public Object clone() throws CloneNotSupportedException {
            Digest256 digest256 = (Digest256)super.clone();
            digest256.digest = new Haraka256Digest((Haraka256Digest)this.digest);
            return digest256;
        }
    }

    public static class Digest512
    extends BCMessageDigest
    implements Cloneable {
        public Digest512() {
            super(new Haraka512Digest());
        }

        public Object clone() throws CloneNotSupportedException {
            Digest512 digest512 = (Digest512)super.clone();
            digest512.digest = new Haraka512Digest((Haraka512Digest)this.digest);
            return digest512;
        }
    }

    public static class Mappings
    extends DigestAlgorithmProvider {
        private static final String PREFIX = Haraka.class.getName();

        public void configure(ConfigurableProvider configurableProvider) {
            configurableProvider.addAlgorithm("MessageDigest.HARAKA-256", PREFIX + "$Digest256");
            configurableProvider.addAlgorithm("MessageDigest.HARAKA-512", PREFIX + "$Digest512");
        }
    }
}


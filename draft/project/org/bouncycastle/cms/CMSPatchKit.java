/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.cms;

import java.io.IOException;
import org.bouncycastle.asn1.cms.SignerInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.SignerInformation;

public class CMSPatchKit {
    public static SignerInformation createNonDERSignerInfo(SignerInformation signerInformation) {
        return new DLSignerInformation(signerInformation);
    }

    public static SignerInformation createWithSignatureAlgorithm(SignerInformation signerInformation, AlgorithmIdentifier algorithmIdentifier) {
        return new ModEncAlgSignerInformation(signerInformation, algorithmIdentifier);
    }

    private static class DLSignerInformation
    extends SignerInformation {
        protected DLSignerInformation(SignerInformation signerInformation) {
            super(signerInformation);
        }

        public byte[] getEncodedSignedAttributes() throws IOException {
            return this.signedAttributeSet.getEncoded("DL");
        }
    }

    private static class ModEncAlgSignerInformation
    extends SignerInformation {
        protected ModEncAlgSignerInformation(SignerInformation signerInformation, AlgorithmIdentifier algorithmIdentifier) {
            super(signerInformation, ModEncAlgSignerInformation.editEncAlg(signerInformation.info, algorithmIdentifier));
        }

        private static SignerInfo editEncAlg(SignerInfo signerInfo, AlgorithmIdentifier algorithmIdentifier) {
            return new SignerInfo(signerInfo.getSID(), signerInfo.getDigestAlgorithm(), signerInfo.getAuthenticatedAttributes(), algorithmIdentifier, signerInfo.getEncryptedDigest(), signerInfo.getUnauthenticatedAttributes());
        }
    }
}


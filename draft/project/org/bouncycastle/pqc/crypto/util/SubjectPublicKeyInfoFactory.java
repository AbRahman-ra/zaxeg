/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.util;

import java.io.IOException;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.isara.IsaraObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.pqc.asn1.McElieceCCA2PublicKey;
import org.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import org.bouncycastle.pqc.asn1.SPHINCS256KeyParams;
import org.bouncycastle.pqc.asn1.XMSSKeyParams;
import org.bouncycastle.pqc.asn1.XMSSMTKeyParams;
import org.bouncycastle.pqc.asn1.XMSSMTPublicKey;
import org.bouncycastle.pqc.asn1.XMSSPublicKey;
import org.bouncycastle.pqc.crypto.lms.Composer;
import org.bouncycastle.pqc.crypto.lms.HSSPublicKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSPublicKeyParameters;
import org.bouncycastle.pqc.crypto.mceliece.McElieceCCA2PublicKeyParameters;
import org.bouncycastle.pqc.crypto.newhope.NHPublicKeyParameters;
import org.bouncycastle.pqc.crypto.qtesla.QTESLAPublicKeyParameters;
import org.bouncycastle.pqc.crypto.sphincs.SPHINCSPublicKeyParameters;
import org.bouncycastle.pqc.crypto.util.Utils;
import org.bouncycastle.pqc.crypto.xmss.XMSSMTPublicKeyParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSPublicKeyParameters;

public class SubjectPublicKeyInfoFactory {
    private SubjectPublicKeyInfoFactory() {
    }

    public static SubjectPublicKeyInfo createSubjectPublicKeyInfo(AsymmetricKeyParameter asymmetricKeyParameter) throws IOException {
        if (asymmetricKeyParameter instanceof QTESLAPublicKeyParameters) {
            QTESLAPublicKeyParameters qTESLAPublicKeyParameters = (QTESLAPublicKeyParameters)asymmetricKeyParameter;
            AlgorithmIdentifier algorithmIdentifier = Utils.qTeslaLookupAlgID(qTESLAPublicKeyParameters.getSecurityCategory());
            return new SubjectPublicKeyInfo(algorithmIdentifier, qTESLAPublicKeyParameters.getPublicData());
        }
        if (asymmetricKeyParameter instanceof SPHINCSPublicKeyParameters) {
            SPHINCSPublicKeyParameters sPHINCSPublicKeyParameters = (SPHINCSPublicKeyParameters)asymmetricKeyParameter;
            AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(PQCObjectIdentifiers.sphincs256, new SPHINCS256KeyParams(Utils.sphincs256LookupTreeAlgID(sPHINCSPublicKeyParameters.getTreeDigest())));
            return new SubjectPublicKeyInfo(algorithmIdentifier, sPHINCSPublicKeyParameters.getKeyData());
        }
        if (asymmetricKeyParameter instanceof NHPublicKeyParameters) {
            NHPublicKeyParameters nHPublicKeyParameters = (NHPublicKeyParameters)asymmetricKeyParameter;
            AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(PQCObjectIdentifiers.newHope);
            return new SubjectPublicKeyInfo(algorithmIdentifier, nHPublicKeyParameters.getPubData());
        }
        if (asymmetricKeyParameter instanceof LMSPublicKeyParameters) {
            LMSPublicKeyParameters lMSPublicKeyParameters = (LMSPublicKeyParameters)asymmetricKeyParameter;
            byte[] byArray = Composer.compose().u32str(1).bytes(lMSPublicKeyParameters).build();
            AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_alg_hss_lms_hashsig);
            return new SubjectPublicKeyInfo(algorithmIdentifier, new DEROctetString(byArray));
        }
        if (asymmetricKeyParameter instanceof HSSPublicKeyParameters) {
            HSSPublicKeyParameters hSSPublicKeyParameters = (HSSPublicKeyParameters)asymmetricKeyParameter;
            byte[] byArray = Composer.compose().u32str(hSSPublicKeyParameters.getL()).bytes(hSSPublicKeyParameters.getLMSPublicKey()).build();
            AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_alg_hss_lms_hashsig);
            return new SubjectPublicKeyInfo(algorithmIdentifier, new DEROctetString(byArray));
        }
        if (asymmetricKeyParameter instanceof XMSSPublicKeyParameters) {
            XMSSPublicKeyParameters xMSSPublicKeyParameters = (XMSSPublicKeyParameters)asymmetricKeyParameter;
            byte[] byArray = xMSSPublicKeyParameters.getPublicSeed();
            byte[] byArray2 = xMSSPublicKeyParameters.getRoot();
            byte[] byArray3 = xMSSPublicKeyParameters.getEncoded();
            if (byArray3.length > byArray.length + byArray2.length) {
                AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(IsaraObjectIdentifiers.id_alg_xmss);
                return new SubjectPublicKeyInfo(algorithmIdentifier, new DEROctetString(byArray3));
            }
            AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(PQCObjectIdentifiers.xmss, new XMSSKeyParams(xMSSPublicKeyParameters.getParameters().getHeight(), Utils.xmssLookupTreeAlgID(xMSSPublicKeyParameters.getTreeDigest())));
            return new SubjectPublicKeyInfo(algorithmIdentifier, new XMSSPublicKey(byArray, byArray2));
        }
        if (asymmetricKeyParameter instanceof XMSSMTPublicKeyParameters) {
            XMSSMTPublicKeyParameters xMSSMTPublicKeyParameters = (XMSSMTPublicKeyParameters)asymmetricKeyParameter;
            byte[] byArray = xMSSMTPublicKeyParameters.getPublicSeed();
            byte[] byArray4 = xMSSMTPublicKeyParameters.getRoot();
            byte[] byArray5 = xMSSMTPublicKeyParameters.getEncoded();
            if (byArray5.length > byArray.length + byArray4.length) {
                AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(IsaraObjectIdentifiers.id_alg_xmssmt);
                return new SubjectPublicKeyInfo(algorithmIdentifier, new DEROctetString(byArray5));
            }
            AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(PQCObjectIdentifiers.xmss_mt, new XMSSMTKeyParams(xMSSMTPublicKeyParameters.getParameters().getHeight(), xMSSMTPublicKeyParameters.getParameters().getLayers(), Utils.xmssLookupTreeAlgID(xMSSMTPublicKeyParameters.getTreeDigest())));
            return new SubjectPublicKeyInfo(algorithmIdentifier, new XMSSMTPublicKey(xMSSMTPublicKeyParameters.getPublicSeed(), xMSSMTPublicKeyParameters.getRoot()));
        }
        if (asymmetricKeyParameter instanceof McElieceCCA2PublicKeyParameters) {
            McElieceCCA2PublicKeyParameters mcElieceCCA2PublicKeyParameters = (McElieceCCA2PublicKeyParameters)asymmetricKeyParameter;
            McElieceCCA2PublicKey mcElieceCCA2PublicKey = new McElieceCCA2PublicKey(mcElieceCCA2PublicKeyParameters.getN(), mcElieceCCA2PublicKeyParameters.getT(), mcElieceCCA2PublicKeyParameters.getG(), Utils.getAlgorithmIdentifier(mcElieceCCA2PublicKeyParameters.getDigest()));
            AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(PQCObjectIdentifiers.mcElieceCca2);
            return new SubjectPublicKeyInfo(algorithmIdentifier, mcElieceCCA2PublicKey);
        }
        throw new IOException("key parameters not recognized");
    }
}


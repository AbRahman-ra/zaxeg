/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.util;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.pqc.asn1.McElieceCCA2PrivateKey;
import org.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import org.bouncycastle.pqc.asn1.SPHINCS256KeyParams;
import org.bouncycastle.pqc.asn1.XMSSKeyParams;
import org.bouncycastle.pqc.asn1.XMSSMTKeyParams;
import org.bouncycastle.pqc.asn1.XMSSMTPrivateKey;
import org.bouncycastle.pqc.asn1.XMSSPrivateKey;
import org.bouncycastle.pqc.crypto.lms.Composer;
import org.bouncycastle.pqc.crypto.lms.HSSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.mceliece.McElieceCCA2PrivateKeyParameters;
import org.bouncycastle.pqc.crypto.newhope.NHPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.qtesla.QTESLAPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.sphincs.SPHINCSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.util.Utils;
import org.bouncycastle.pqc.crypto.xmss.BDS;
import org.bouncycastle.pqc.crypto.xmss.BDSStateMap;
import org.bouncycastle.pqc.crypto.xmss.XMSSMTPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSUtil;
import org.bouncycastle.util.Pack;

public class PrivateKeyInfoFactory {
    private PrivateKeyInfoFactory() {
    }

    public static PrivateKeyInfo createPrivateKeyInfo(AsymmetricKeyParameter asymmetricKeyParameter) throws IOException {
        return PrivateKeyInfoFactory.createPrivateKeyInfo(asymmetricKeyParameter, null);
    }

    public static PrivateKeyInfo createPrivateKeyInfo(AsymmetricKeyParameter asymmetricKeyParameter, ASN1Set aSN1Set) throws IOException {
        if (asymmetricKeyParameter instanceof QTESLAPrivateKeyParameters) {
            QTESLAPrivateKeyParameters qTESLAPrivateKeyParameters = (QTESLAPrivateKeyParameters)asymmetricKeyParameter;
            AlgorithmIdentifier algorithmIdentifier = Utils.qTeslaLookupAlgID(qTESLAPrivateKeyParameters.getSecurityCategory());
            return new PrivateKeyInfo(algorithmIdentifier, new DEROctetString(qTESLAPrivateKeyParameters.getSecret()), aSN1Set);
        }
        if (asymmetricKeyParameter instanceof SPHINCSPrivateKeyParameters) {
            SPHINCSPrivateKeyParameters sPHINCSPrivateKeyParameters = (SPHINCSPrivateKeyParameters)asymmetricKeyParameter;
            AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(PQCObjectIdentifiers.sphincs256, new SPHINCS256KeyParams(Utils.sphincs256LookupTreeAlgID(sPHINCSPrivateKeyParameters.getTreeDigest())));
            return new PrivateKeyInfo(algorithmIdentifier, new DEROctetString(sPHINCSPrivateKeyParameters.getKeyData()));
        }
        if (asymmetricKeyParameter instanceof NHPrivateKeyParameters) {
            NHPrivateKeyParameters nHPrivateKeyParameters = (NHPrivateKeyParameters)asymmetricKeyParameter;
            AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(PQCObjectIdentifiers.newHope);
            short[] sArray = nHPrivateKeyParameters.getSecData();
            byte[] byArray = new byte[sArray.length * 2];
            for (int i = 0; i != sArray.length; ++i) {
                Pack.shortToLittleEndian(sArray[i], byArray, i * 2);
            }
            return new PrivateKeyInfo(algorithmIdentifier, new DEROctetString(byArray));
        }
        if (asymmetricKeyParameter instanceof LMSPrivateKeyParameters) {
            LMSPrivateKeyParameters lMSPrivateKeyParameters = (LMSPrivateKeyParameters)asymmetricKeyParameter;
            byte[] byArray = Composer.compose().u32str(1).bytes(lMSPrivateKeyParameters).build();
            byte[] byArray2 = Composer.compose().u32str(1).bytes(lMSPrivateKeyParameters.getPublicKey()).build();
            AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_alg_hss_lms_hashsig);
            return new PrivateKeyInfo(algorithmIdentifier, new DEROctetString(byArray), aSN1Set, byArray2);
        }
        if (asymmetricKeyParameter instanceof HSSPrivateKeyParameters) {
            HSSPrivateKeyParameters hSSPrivateKeyParameters = (HSSPrivateKeyParameters)asymmetricKeyParameter;
            byte[] byArray = Composer.compose().u32str(hSSPrivateKeyParameters.getL()).bytes(hSSPrivateKeyParameters).build();
            byte[] byArray3 = Composer.compose().u32str(hSSPrivateKeyParameters.getL()).bytes(hSSPrivateKeyParameters.getPublicKey().getLMSPublicKey()).build();
            AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_alg_hss_lms_hashsig);
            return new PrivateKeyInfo(algorithmIdentifier, new DEROctetString(byArray), aSN1Set, byArray3);
        }
        if (asymmetricKeyParameter instanceof XMSSPrivateKeyParameters) {
            XMSSPrivateKeyParameters xMSSPrivateKeyParameters = (XMSSPrivateKeyParameters)asymmetricKeyParameter;
            AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(PQCObjectIdentifiers.xmss, new XMSSKeyParams(xMSSPrivateKeyParameters.getParameters().getHeight(), Utils.xmssLookupTreeAlgID(xMSSPrivateKeyParameters.getTreeDigest())));
            return new PrivateKeyInfo(algorithmIdentifier, PrivateKeyInfoFactory.xmssCreateKeyStructure(xMSSPrivateKeyParameters), aSN1Set);
        }
        if (asymmetricKeyParameter instanceof XMSSMTPrivateKeyParameters) {
            XMSSMTPrivateKeyParameters xMSSMTPrivateKeyParameters = (XMSSMTPrivateKeyParameters)asymmetricKeyParameter;
            AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(PQCObjectIdentifiers.xmss_mt, new XMSSMTKeyParams(xMSSMTPrivateKeyParameters.getParameters().getHeight(), xMSSMTPrivateKeyParameters.getParameters().getLayers(), Utils.xmssLookupTreeAlgID(xMSSMTPrivateKeyParameters.getTreeDigest())));
            return new PrivateKeyInfo(algorithmIdentifier, PrivateKeyInfoFactory.xmssmtCreateKeyStructure(xMSSMTPrivateKeyParameters), aSN1Set);
        }
        if (asymmetricKeyParameter instanceof McElieceCCA2PrivateKeyParameters) {
            McElieceCCA2PrivateKeyParameters mcElieceCCA2PrivateKeyParameters = (McElieceCCA2PrivateKeyParameters)asymmetricKeyParameter;
            McElieceCCA2PrivateKey mcElieceCCA2PrivateKey = new McElieceCCA2PrivateKey(mcElieceCCA2PrivateKeyParameters.getN(), mcElieceCCA2PrivateKeyParameters.getK(), mcElieceCCA2PrivateKeyParameters.getField(), mcElieceCCA2PrivateKeyParameters.getGoppaPoly(), mcElieceCCA2PrivateKeyParameters.getP(), Utils.getAlgorithmIdentifier(mcElieceCCA2PrivateKeyParameters.getDigest()));
            AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(PQCObjectIdentifiers.mcElieceCca2);
            return new PrivateKeyInfo(algorithmIdentifier, mcElieceCCA2PrivateKey);
        }
        throw new IOException("key parameters not recognized");
    }

    private static XMSSPrivateKey xmssCreateKeyStructure(XMSSPrivateKeyParameters xMSSPrivateKeyParameters) throws IOException {
        byte[] byArray = xMSSPrivateKeyParameters.getEncoded();
        int n = xMSSPrivateKeyParameters.getParameters().getTreeDigestSize();
        int n2 = xMSSPrivateKeyParameters.getParameters().getHeight();
        int n3 = 4;
        int n4 = n;
        int n5 = n;
        int n6 = n;
        int n7 = n;
        int n8 = 0;
        int n9 = (int)XMSSUtil.bytesToXBigEndian(byArray, n8, n3);
        if (!XMSSUtil.isIndexValid(n2, n9)) {
            throw new IllegalArgumentException("index out of bounds");
        }
        byte[] byArray2 = XMSSUtil.extractBytesAtOffset(byArray, n8 += n3, n4);
        byte[] byArray3 = XMSSUtil.extractBytesAtOffset(byArray, n8 += n4, n5);
        byte[] byArray4 = XMSSUtil.extractBytesAtOffset(byArray, n8 += n5, n6);
        byte[] byArray5 = XMSSUtil.extractBytesAtOffset(byArray, n8 += n6, n7);
        byte[] byArray6 = XMSSUtil.extractBytesAtOffset(byArray, n8 += n7, byArray.length - n8);
        BDS bDS = null;
        try {
            bDS = (BDS)XMSSUtil.deserialize(byArray6, BDS.class);
        } catch (ClassNotFoundException classNotFoundException) {
            throw new IOException("cannot parse BDS: " + classNotFoundException.getMessage());
        }
        if (bDS.getMaxIndex() != (1 << n2) - 1) {
            return new XMSSPrivateKey(n9, byArray2, byArray3, byArray4, byArray5, byArray6, bDS.getMaxIndex());
        }
        return new XMSSPrivateKey(n9, byArray2, byArray3, byArray4, byArray5, byArray6);
    }

    private static XMSSMTPrivateKey xmssmtCreateKeyStructure(XMSSMTPrivateKeyParameters xMSSMTPrivateKeyParameters) throws IOException {
        byte[] byArray = xMSSMTPrivateKeyParameters.getEncoded();
        int n = xMSSMTPrivateKeyParameters.getParameters().getTreeDigestSize();
        int n2 = xMSSMTPrivateKeyParameters.getParameters().getHeight();
        int n3 = (n2 + 7) / 8;
        int n4 = n;
        int n5 = n;
        int n6 = n;
        int n7 = n;
        int n8 = 0;
        int n9 = (int)XMSSUtil.bytesToXBigEndian(byArray, n8, n3);
        if (!XMSSUtil.isIndexValid(n2, n9)) {
            throw new IllegalArgumentException("index out of bounds");
        }
        byte[] byArray2 = XMSSUtil.extractBytesAtOffset(byArray, n8 += n3, n4);
        byte[] byArray3 = XMSSUtil.extractBytesAtOffset(byArray, n8 += n4, n5);
        byte[] byArray4 = XMSSUtil.extractBytesAtOffset(byArray, n8 += n5, n6);
        byte[] byArray5 = XMSSUtil.extractBytesAtOffset(byArray, n8 += n6, n7);
        byte[] byArray6 = XMSSUtil.extractBytesAtOffset(byArray, n8 += n7, byArray.length - n8);
        BDSStateMap bDSStateMap = null;
        try {
            bDSStateMap = (BDSStateMap)XMSSUtil.deserialize(byArray6, BDSStateMap.class);
        } catch (ClassNotFoundException classNotFoundException) {
            throw new IOException("cannot parse BDSStateMap: " + classNotFoundException.getMessage());
        }
        if (bDSStateMap.getMaxIndex() != (1L << n2) - 1L) {
            return new XMSSMTPrivateKey(n9, byArray2, byArray3, byArray4, byArray5, byArray6, bDSStateMap.getMaxIndex());
        }
        return new XMSSMTPrivateKey(n9, byArray2, byArray3, byArray4, byArray5, byArray6);
    }
}


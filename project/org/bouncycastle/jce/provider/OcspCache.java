/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jce.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Extension;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.ocsp.BasicOCSPResponse;
import org.bouncycastle.asn1.ocsp.CertID;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.ocsp.OCSPRequest;
import org.bouncycastle.asn1.ocsp.OCSPResponse;
import org.bouncycastle.asn1.ocsp.Request;
import org.bouncycastle.asn1.ocsp.ResponseBytes;
import org.bouncycastle.asn1.ocsp.ResponseData;
import org.bouncycastle.asn1.ocsp.Signature;
import org.bouncycastle.asn1.ocsp.SingleResponse;
import org.bouncycastle.asn1.ocsp.TBSRequest;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.jcajce.PKIXCertRevocationCheckerParameters;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jce.provider.ProvOcspRevocationChecker;
import org.bouncycastle.util.io.Streams;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
class OcspCache {
    private static final int DEFAULT_TIMEOUT = 15000;
    private static final int DEFAULT_MAX_RESPONSE_SIZE = 32768;
    private static Map<URI, WeakReference<Map<CertID, OCSPResponse>>> cache = Collections.synchronizedMap(new WeakHashMap());

    OcspCache() {
    }

    static OCSPResponse getOcspResponse(CertID certID, PKIXCertRevocationCheckerParameters pKIXCertRevocationCheckerParameters, URI uRI, X509Certificate x509Certificate, List<Extension> list, JcaJceHelper jcaJceHelper) throws CertPathValidatorException {
        byte[] byArray;
        Object object;
        Object object2;
        Object object3;
        Object object4;
        Object object5;
        HashMap<CertID, OCSPResponse> hashMap = null;
        WeakReference<Map<CertID, OCSPResponse>> weakReference = cache.get(uRI);
        if (weakReference != null) {
            hashMap = (HashMap<CertID, OCSPResponse>)weakReference.get();
        }
        if (hashMap != null && (object5 = (OCSPResponse)hashMap.get(certID)) != null) {
            object4 = BasicOCSPResponse.getInstance(ASN1OctetString.getInstance(((OCSPResponse)object5).getResponseBytes().getResponse()).getOctets());
            object3 = ResponseData.getInstance(((BasicOCSPResponse)object4).getTbsResponseData());
            object2 = ((ResponseData)object3).getResponses();
            for (int i = 0; i != ((ASN1Sequence)object2).size(); ++i) {
                SingleResponse singleResponse = SingleResponse.getInstance(((ASN1Sequence)object2).getObjectAt(i));
                if (!certID.equals(singleResponse.getCertID())) continue;
                object = singleResponse.getNextUpdate();
                try {
                    if (object == null || !pKIXCertRevocationCheckerParameters.getValidDate().after(((ASN1GeneralizedTime)object).getDate())) continue;
                    hashMap.remove(certID);
                    object5 = null;
                    continue;
                } catch (ParseException parseException) {
                    hashMap.remove(certID);
                    object5 = null;
                }
            }
            if (object5 != null) {
                return object5;
            }
        }
        try {
            object5 = uRI.toURL();
        } catch (MalformedURLException malformedURLException) {
            throw new CertPathValidatorException("configuration error: " + malformedURLException.getMessage(), (Throwable)malformedURLException, pKIXCertRevocationCheckerParameters.getCertPath(), pKIXCertRevocationCheckerParameters.getIndex());
        }
        object4 = new ASN1EncodableVector();
        ((ASN1EncodableVector)object4).add(new Request(certID, null));
        object3 = list;
        object2 = new ASN1EncodableVector();
        byte[] byArray2 = null;
        for (int i = 0; i != object3.size(); ++i) {
            object = (Extension)object3.get(i);
            byArray = object.getValue();
            if (OCSPObjectIdentifiers.id_pkix_ocsp_nonce.getId().equals(object.getId())) {
                byArray2 = byArray;
            }
            ((ASN1EncodableVector)object2).add(new org.bouncycastle.asn1.x509.Extension(new ASN1ObjectIdentifier(object.getId()), object.isCritical(), byArray));
        }
        TBSRequest tBSRequest = new TBSRequest(null, (ASN1Sequence)new DERSequence((ASN1EncodableVector)object4), Extensions.getInstance(new DERSequence((ASN1EncodableVector)object2)));
        object = null;
        try {
            OCSPResponse oCSPResponse;
            byArray = new OCSPRequest(tBSRequest, (Signature)object).getEncoded();
            HttpURLConnection httpURLConnection = (HttpURLConnection)((URL)object5).openConnection();
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setReadTimeout(15000);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-type", "application/ocsp-request");
            httpURLConnection.setRequestProperty("Content-length", String.valueOf(byArray.length));
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(byArray);
            outputStream.flush();
            InputStream inputStream = httpURLConnection.getInputStream();
            int n = httpURLConnection.getContentLength();
            if (n < 0) {
                n = 32768;
            }
            if (0 == (oCSPResponse = OCSPResponse.getInstance(Streams.readAllLimited(inputStream, n))).getResponseStatus().getIntValue()) {
                boolean bl = false;
                ResponseBytes responseBytes = ResponseBytes.getInstance(oCSPResponse.getResponseBytes());
                if (responseBytes.getResponseType().equals(OCSPObjectIdentifiers.id_pkix_ocsp_basic)) {
                    BasicOCSPResponse basicOCSPResponse = BasicOCSPResponse.getInstance(responseBytes.getResponse().getOctets());
                    bl = ProvOcspRevocationChecker.validatedOcspResponse(basicOCSPResponse, pKIXCertRevocationCheckerParameters, byArray2, x509Certificate, jcaJceHelper);
                }
                if (!bl) {
                    throw new CertPathValidatorException("OCSP response failed to validate", null, pKIXCertRevocationCheckerParameters.getCertPath(), pKIXCertRevocationCheckerParameters.getIndex());
                }
                weakReference = cache.get(uRI);
                if (weakReference != null) {
                    hashMap = (Map)weakReference.get();
                    hashMap.put(certID, oCSPResponse);
                } else {
                    hashMap = new HashMap<CertID, OCSPResponse>();
                    hashMap.put(certID, oCSPResponse);
                    cache.put(uRI, new WeakReference(hashMap));
                }
                return oCSPResponse;
            }
            throw new CertPathValidatorException("OCSP responder failed: " + oCSPResponse.getResponseStatus().getValue(), null, pKIXCertRevocationCheckerParameters.getCertPath(), pKIXCertRevocationCheckerParameters.getIndex());
        } catch (IOException iOException) {
            throw new CertPathValidatorException("configuration error: " + iOException.getMessage(), (Throwable)iOException, pKIXCertRevocationCheckerParameters.getCertPath(), pKIXCertRevocationCheckerParameters.getIndex());
        }
    }
}


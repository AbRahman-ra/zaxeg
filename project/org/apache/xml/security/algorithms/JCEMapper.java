/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.algorithms;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.JavaUtils;
import org.w3c.dom.Element;

public class JCEMapper {
    private static Log log = LogFactory.getLog(JCEMapper.class);
    private static Map<String, Algorithm> algorithmsMap = new ConcurrentHashMap<String, Algorithm>();
    private static String providerName = null;

    public static void register(String id, Algorithm algorithm) {
        JavaUtils.checkRegisterPermission();
        algorithmsMap.put(id, algorithm);
    }

    public static void registerDefaultAlgorithms() {
        algorithmsMap.put("http://www.w3.org/2001/04/xmldsig-more#md5", new Algorithm("", "MD5", "MessageDigest"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmlenc#ripemd160", new Algorithm("", "RIPEMD160", "MessageDigest"));
        algorithmsMap.put("http://www.w3.org/2000/09/xmldsig#sha1", new Algorithm("", "SHA-1", "MessageDigest"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmldsig-more#sha224", new Algorithm("SHA-224", "SHA-224", "MessageDigest"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmlenc#sha256", new Algorithm("", "SHA-256", "MessageDigest"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmldsig-more#sha384", new Algorithm("", "SHA-384", "MessageDigest"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmlenc#sha512", new Algorithm("", "SHA-512", "MessageDigest"));
        algorithmsMap.put("http://www.w3.org/2000/09/xmldsig#dsa-sha1", new Algorithm("", "SHA1withDSA", "Signature"));
        algorithmsMap.put("http://www.w3.org/2009/xmldsig11#dsa-sha256", new Algorithm("", "SHA256withDSA", "Signature"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmldsig-more#rsa-md5", new Algorithm("", "MD5withRSA", "Signature"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmldsig-more#rsa-ripemd160", new Algorithm("", "RIPEMD160withRSA", "Signature"));
        algorithmsMap.put("http://www.w3.org/2000/09/xmldsig#rsa-sha1", new Algorithm("", "SHA1withRSA", "Signature"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmldsig-more#rsa-sha224", new Algorithm("SHA224withRSA", "SHA224withRSA", "Signature"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", new Algorithm("", "SHA256withRSA", "Signature"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmldsig-more#rsa-sha384", new Algorithm("", "SHA384withRSA", "Signature"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmldsig-more#rsa-sha512", new Algorithm("", "SHA512withRSA", "Signature"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1", new Algorithm("", "SHA1withECDSA", "Signature"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha224", new Algorithm("SHA224withECDSA", "SHA224withECDSA", "Signature"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256", new Algorithm("", "SHA256withECDSA", "Signature"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384", new Algorithm("", "SHA384withECDSA", "Signature"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512", new Algorithm("", "SHA512withECDSA", "Signature"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmldsig-more#hmac-md5", new Algorithm("", "HmacMD5", "Mac"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmldsig-more#hmac-ripemd160", new Algorithm("", "HMACRIPEMD160", "Mac"));
        algorithmsMap.put("http://www.w3.org/2000/09/xmldsig#hmac-sha1", new Algorithm("", "HmacSHA1", "Mac"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmldsig-more#hmac-sha224", new Algorithm("", "HmacSHA224", "Mac"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmldsig-more#hmac-sha256", new Algorithm("", "HmacSHA256", "Mac"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmldsig-more#hmac-sha384", new Algorithm("", "HmacSHA384", "Mac"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmldsig-more#hmac-sha512", new Algorithm("", "HmacSHA512", "Mac"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmlenc#tripledes-cbc", new Algorithm("DESede", "DESede/CBC/ISO10126Padding", "BlockEncryption", 192));
        algorithmsMap.put("http://www.w3.org/2001/04/xmlenc#aes128-cbc", new Algorithm("AES", "AES/CBC/ISO10126Padding", "BlockEncryption", 128));
        algorithmsMap.put("http://www.w3.org/2001/04/xmlenc#aes192-cbc", new Algorithm("AES", "AES/CBC/ISO10126Padding", "BlockEncryption", 192));
        algorithmsMap.put("http://www.w3.org/2001/04/xmlenc#aes256-cbc", new Algorithm("AES", "AES/CBC/ISO10126Padding", "BlockEncryption", 256));
        algorithmsMap.put("http://www.w3.org/2009/xmlenc11#aes128-gcm", new Algorithm("AES", "AES/GCM/NoPadding", "BlockEncryption", 128));
        algorithmsMap.put("http://www.w3.org/2009/xmlenc11#aes192-gcm", new Algorithm("AES", "AES/GCM/NoPadding", "BlockEncryption", 192));
        algorithmsMap.put("http://www.w3.org/2009/xmlenc11#aes256-gcm", new Algorithm("AES", "AES/GCM/NoPadding", "BlockEncryption", 256));
        algorithmsMap.put("http://www.w3.org/2001/04/xmlenc#rsa-1_5", new Algorithm("RSA", "RSA/ECB/PKCS1Padding", "KeyTransport"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p", new Algorithm("RSA", "RSA/ECB/OAEPPadding", "KeyTransport"));
        algorithmsMap.put("http://www.w3.org/2009/xmlenc11#rsa-oaep", new Algorithm("RSA", "RSA/ECB/OAEPPadding", "KeyTransport"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmlenc#dh", new Algorithm("", "", "KeyAgreement"));
        algorithmsMap.put("http://www.w3.org/2001/04/xmlenc#kw-tripledes", new Algorithm("DESede", "DESedeWrap", "SymmetricKeyWrap", 192));
        algorithmsMap.put("http://www.w3.org/2001/04/xmlenc#kw-aes128", new Algorithm("AES", "AESWrap", "SymmetricKeyWrap", 128));
        algorithmsMap.put("http://www.w3.org/2001/04/xmlenc#kw-aes192", new Algorithm("AES", "AESWrap", "SymmetricKeyWrap", 192));
        algorithmsMap.put("http://www.w3.org/2001/04/xmlenc#kw-aes256", new Algorithm("AES", "AESWrap", "SymmetricKeyWrap", 256));
    }

    public static String translateURItoJCEID(String algorithmURI) {
        Algorithm algorithm;
        if (log.isDebugEnabled()) {
            log.debug("Request for URI " + algorithmURI);
        }
        if ((algorithm = algorithmsMap.get(algorithmURI)) != null) {
            return algorithm.jceName;
        }
        return null;
    }

    public static String getAlgorithmClassFromURI(String algorithmURI) {
        Algorithm algorithm;
        if (log.isDebugEnabled()) {
            log.debug("Request for URI " + algorithmURI);
        }
        if ((algorithm = algorithmsMap.get(algorithmURI)) != null) {
            return algorithm.algorithmClass;
        }
        return null;
    }

    public static int getKeyLengthFromURI(String algorithmURI) {
        Algorithm algorithm;
        if (log.isDebugEnabled()) {
            log.debug("Request for URI " + algorithmURI);
        }
        if ((algorithm = algorithmsMap.get(algorithmURI)) != null) {
            return algorithm.keyLength;
        }
        return 0;
    }

    public static String getJCEKeyAlgorithmFromURI(String algorithmURI) {
        Algorithm algorithm;
        if (log.isDebugEnabled()) {
            log.debug("Request for URI " + algorithmURI);
        }
        if ((algorithm = algorithmsMap.get(algorithmURI)) != null) {
            return algorithm.requiredKey;
        }
        return null;
    }

    public static String getProviderId() {
        return providerName;
    }

    public static void setProviderId(String provider) {
        JavaUtils.checkRegisterPermission();
        providerName = provider;
    }

    public static class Algorithm {
        final String requiredKey;
        final String jceName;
        final String algorithmClass;
        final int keyLength;

        public Algorithm(Element el) {
            this.requiredKey = el.getAttributeNS(null, "RequiredKey");
            this.jceName = el.getAttributeNS(null, "JCEName");
            this.algorithmClass = el.getAttributeNS(null, "AlgorithmClass");
            this.keyLength = el.hasAttribute("KeyLength") ? Integer.parseInt(el.getAttributeNS(null, "KeyLength")) : 0;
        }

        public Algorithm(String requiredKey, String jceName) {
            this(requiredKey, jceName, null, 0);
        }

        public Algorithm(String requiredKey, String jceName, String algorithmClass) {
            this(requiredKey, jceName, algorithmClass, 0);
        }

        public Algorithm(String requiredKey, String jceName, int keyLength) {
            this(requiredKey, jceName, null, keyLength);
        }

        public Algorithm(String requiredKey, String jceName, String algorithmClass, int keyLength) {
            this.requiredKey = requiredKey;
            this.jceName = jceName;
            this.algorithmClass = algorithmClass;
            this.keyLength = keyLength;
        }
    }
}


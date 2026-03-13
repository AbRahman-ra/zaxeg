/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.encryption;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivilegedAction;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.algorithms.JCEMapper;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.apache.xml.security.encryption.AbstractSerializer;
import org.apache.xml.security.encryption.AgreementMethod;
import org.apache.xml.security.encryption.CipherData;
import org.apache.xml.security.encryption.CipherReference;
import org.apache.xml.security.encryption.CipherValue;
import org.apache.xml.security.encryption.DocumentSerializer;
import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.EncryptionMethod;
import org.apache.xml.security.encryption.EncryptionProperties;
import org.apache.xml.security.encryption.EncryptionProperty;
import org.apache.xml.security.encryption.Reference;
import org.apache.xml.security.encryption.ReferenceList;
import org.apache.xml.security.encryption.Serializer;
import org.apache.xml.security.encryption.Transforms;
import org.apache.xml.security.encryption.XMLCipherInput;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.keyresolver.KeyResolverException;
import org.apache.xml.security.keys.keyresolver.KeyResolverSpi;
import org.apache.xml.security.keys.keyresolver.implementations.EncryptedKeyResolver;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.transforms.InvalidTransformException;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.utils.Base64;
import org.apache.xml.security.utils.ClassLoaderUtils;
import org.apache.xml.security.utils.ElementProxy;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class XMLCipher {
    private static Log log = LogFactory.getLog(XMLCipher.class);
    private static final boolean gcmUseIvParameterSpec = AccessController.doPrivileged(new PrivilegedAction<Boolean>(){

        @Override
        public Boolean run() {
            return Boolean.getBoolean("org.apache.xml.security.cipher.gcm.useIvParameterSpec");
        }
    });
    public static final String TRIPLEDES = "http://www.w3.org/2001/04/xmlenc#tripledes-cbc";
    public static final String AES_128 = "http://www.w3.org/2001/04/xmlenc#aes128-cbc";
    public static final String AES_256 = "http://www.w3.org/2001/04/xmlenc#aes256-cbc";
    public static final String AES_192 = "http://www.w3.org/2001/04/xmlenc#aes192-cbc";
    public static final String AES_128_GCM = "http://www.w3.org/2009/xmlenc11#aes128-gcm";
    public static final String AES_192_GCM = "http://www.w3.org/2009/xmlenc11#aes192-gcm";
    public static final String AES_256_GCM = "http://www.w3.org/2009/xmlenc11#aes256-gcm";
    public static final String RSA_v1dot5 = "http://www.w3.org/2001/04/xmlenc#rsa-1_5";
    public static final String RSA_OAEP = "http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p";
    public static final String RSA_OAEP_11 = "http://www.w3.org/2009/xmlenc11#rsa-oaep";
    public static final String DIFFIE_HELLMAN = "http://www.w3.org/2001/04/xmlenc#dh";
    public static final String TRIPLEDES_KeyWrap = "http://www.w3.org/2001/04/xmlenc#kw-tripledes";
    public static final String AES_128_KeyWrap = "http://www.w3.org/2001/04/xmlenc#kw-aes128";
    public static final String AES_256_KeyWrap = "http://www.w3.org/2001/04/xmlenc#kw-aes256";
    public static final String AES_192_KeyWrap = "http://www.w3.org/2001/04/xmlenc#kw-aes192";
    public static final String SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";
    public static final String SHA256 = "http://www.w3.org/2001/04/xmlenc#sha256";
    public static final String SHA512 = "http://www.w3.org/2001/04/xmlenc#sha512";
    public static final String RIPEMD_160 = "http://www.w3.org/2001/04/xmlenc#ripemd160";
    public static final String XML_DSIG = "http://www.w3.org/2000/09/xmldsig#";
    public static final String N14C_XML = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
    public static final String N14C_XML_WITH_COMMENTS = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments";
    public static final String EXCL_XML_N14C = "http://www.w3.org/2001/10/xml-exc-c14n#";
    public static final String EXCL_XML_N14C_WITH_COMMENTS = "http://www.w3.org/2001/10/xml-exc-c14n#WithComments";
    public static final String PHYSICAL_XML_N14C = "http://santuario.apache.org/c14n/physical";
    public static final String BASE64_ENCODING = "http://www.w3.org/2000/09/xmldsig#base64";
    public static final int ENCRYPT_MODE = 1;
    public static final int DECRYPT_MODE = 2;
    public static final int UNWRAP_MODE = 4;
    public static final int WRAP_MODE = 3;
    private static final String ENC_ALGORITHMS = "http://www.w3.org/2001/04/xmlenc#tripledes-cbc\nhttp://www.w3.org/2001/04/xmlenc#aes128-cbc\nhttp://www.w3.org/2001/04/xmlenc#aes256-cbc\nhttp://www.w3.org/2001/04/xmlenc#aes192-cbc\nhttp://www.w3.org/2001/04/xmlenc#rsa-1_5\nhttp://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p\nhttp://www.w3.org/2009/xmlenc11#rsa-oaep\nhttp://www.w3.org/2001/04/xmlenc#kw-tripledes\nhttp://www.w3.org/2001/04/xmlenc#kw-aes128\nhttp://www.w3.org/2001/04/xmlenc#kw-aes256\nhttp://www.w3.org/2001/04/xmlenc#kw-aes192\nhttp://www.w3.org/2009/xmlenc11#aes128-gcm\nhttp://www.w3.org/2009/xmlenc11#aes192-gcm\nhttp://www.w3.org/2009/xmlenc11#aes256-gcm\n";
    private Cipher contextCipher;
    private int cipherMode = Integer.MIN_VALUE;
    private String algorithm = null;
    private String requestedJCEProvider = null;
    private Canonicalizer canon;
    private Document contextDocument;
    private Factory factory;
    private Serializer serializer;
    private Key key;
    private Key kek;
    private EncryptedKey ek;
    private EncryptedData ed;
    private SecureRandom random;
    private boolean secureValidation;
    private String digestAlg;
    private List<KeyResolverSpi> internalKeyResolvers;

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
        serializer.setCanonicalizer(this.canon);
    }

    public Serializer getSerializer() {
        return this.serializer;
    }

    private XMLCipher(String transformation, String provider, String canonAlg, String digestMethod) throws XMLEncryptionException {
        if (log.isDebugEnabled()) {
            log.debug("Constructing XMLCipher...");
        }
        this.factory = new Factory();
        this.algorithm = transformation;
        this.requestedJCEProvider = provider;
        this.digestAlg = digestMethod;
        try {
            this.canon = canonAlg == null ? Canonicalizer.getInstance(PHYSICAL_XML_N14C) : Canonicalizer.getInstance(canonAlg);
        } catch (InvalidCanonicalizerException ice) {
            throw new XMLEncryptionException("empty", ice);
        }
        if (this.serializer == null) {
            this.serializer = new DocumentSerializer();
        }
        this.serializer.setCanonicalizer(this.canon);
        if (transformation != null) {
            this.contextCipher = this.constructCipher(transformation, digestMethod);
        }
    }

    private static boolean isValidEncryptionAlgorithm(String algorithm) {
        return algorithm.equals(TRIPLEDES) || algorithm.equals(AES_128) || algorithm.equals(AES_256) || algorithm.equals(AES_192) || algorithm.equals(AES_128_GCM) || algorithm.equals(AES_192_GCM) || algorithm.equals(AES_256_GCM) || algorithm.equals(RSA_v1dot5) || algorithm.equals(RSA_OAEP) || algorithm.equals(RSA_OAEP_11) || algorithm.equals(TRIPLEDES_KeyWrap) || algorithm.equals(AES_128_KeyWrap) || algorithm.equals(AES_256_KeyWrap) || algorithm.equals(AES_192_KeyWrap);
    }

    private static void validateTransformation(String transformation) {
        if (null == transformation) {
            throw new NullPointerException("Transformation unexpectedly null...");
        }
        if (!XMLCipher.isValidEncryptionAlgorithm(transformation)) {
            log.warn("Algorithm non-standard, expected one of http://www.w3.org/2001/04/xmlenc#tripledes-cbc\nhttp://www.w3.org/2001/04/xmlenc#aes128-cbc\nhttp://www.w3.org/2001/04/xmlenc#aes256-cbc\nhttp://www.w3.org/2001/04/xmlenc#aes192-cbc\nhttp://www.w3.org/2001/04/xmlenc#rsa-1_5\nhttp://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p\nhttp://www.w3.org/2009/xmlenc11#rsa-oaep\nhttp://www.w3.org/2001/04/xmlenc#kw-tripledes\nhttp://www.w3.org/2001/04/xmlenc#kw-aes128\nhttp://www.w3.org/2001/04/xmlenc#kw-aes256\nhttp://www.w3.org/2001/04/xmlenc#kw-aes192\nhttp://www.w3.org/2009/xmlenc11#aes128-gcm\nhttp://www.w3.org/2009/xmlenc11#aes192-gcm\nhttp://www.w3.org/2009/xmlenc11#aes256-gcm\n");
        }
    }

    public static XMLCipher getInstance(String transformation) throws XMLEncryptionException {
        if (log.isDebugEnabled()) {
            log.debug("Getting XMLCipher with transformation");
        }
        XMLCipher.validateTransformation(transformation);
        return new XMLCipher(transformation, null, null, null);
    }

    public static XMLCipher getInstance(String transformation, String canon) throws XMLEncryptionException {
        if (log.isDebugEnabled()) {
            log.debug("Getting XMLCipher with transformation and c14n algorithm");
        }
        XMLCipher.validateTransformation(transformation);
        return new XMLCipher(transformation, null, canon, null);
    }

    public static XMLCipher getInstance(String transformation, String canon, String digestMethod) throws XMLEncryptionException {
        if (log.isDebugEnabled()) {
            log.debug("Getting XMLCipher with transformation and c14n algorithm");
        }
        XMLCipher.validateTransformation(transformation);
        return new XMLCipher(transformation, null, canon, digestMethod);
    }

    public static XMLCipher getProviderInstance(String transformation, String provider) throws XMLEncryptionException {
        if (log.isDebugEnabled()) {
            log.debug("Getting XMLCipher with transformation and provider");
        }
        if (null == provider) {
            throw new NullPointerException("Provider unexpectedly null..");
        }
        XMLCipher.validateTransformation(transformation);
        return new XMLCipher(transformation, provider, null, null);
    }

    public static XMLCipher getProviderInstance(String transformation, String provider, String canon) throws XMLEncryptionException {
        if (log.isDebugEnabled()) {
            log.debug("Getting XMLCipher with transformation, provider and c14n algorithm");
        }
        if (null == provider) {
            throw new NullPointerException("Provider unexpectedly null..");
        }
        XMLCipher.validateTransformation(transformation);
        return new XMLCipher(transformation, provider, canon, null);
    }

    public static XMLCipher getProviderInstance(String transformation, String provider, String canon, String digestMethod) throws XMLEncryptionException {
        if (log.isDebugEnabled()) {
            log.debug("Getting XMLCipher with transformation, provider and c14n algorithm");
        }
        if (null == provider) {
            throw new NullPointerException("Provider unexpectedly null..");
        }
        XMLCipher.validateTransformation(transformation);
        return new XMLCipher(transformation, provider, canon, digestMethod);
    }

    public static XMLCipher getInstance() throws XMLEncryptionException {
        if (log.isDebugEnabled()) {
            log.debug("Getting XMLCipher with no arguments");
        }
        return new XMLCipher(null, null, null, null);
    }

    public static XMLCipher getProviderInstance(String provider) throws XMLEncryptionException {
        if (log.isDebugEnabled()) {
            log.debug("Getting XMLCipher with provider");
        }
        return new XMLCipher(null, provider, null, null);
    }

    public void init(int opmode, Key key) throws XMLEncryptionException {
        if (log.isDebugEnabled()) {
            log.debug("Initializing XMLCipher...");
        }
        this.ek = null;
        this.ed = null;
        switch (opmode) {
            case 1: {
                if (log.isDebugEnabled()) {
                    log.debug("opmode = ENCRYPT_MODE");
                }
                this.ed = this.createEncryptedData(1, "NO VALUE YET");
                break;
            }
            case 2: {
                if (!log.isDebugEnabled()) break;
                log.debug("opmode = DECRYPT_MODE");
                break;
            }
            case 3: {
                if (log.isDebugEnabled()) {
                    log.debug("opmode = WRAP_MODE");
                }
                this.ek = this.createEncryptedKey(1, "NO VALUE YET");
                break;
            }
            case 4: {
                if (!log.isDebugEnabled()) break;
                log.debug("opmode = UNWRAP_MODE");
                break;
            }
            default: {
                log.error("Mode unexpectedly invalid");
                throw new XMLEncryptionException("Invalid mode in init");
            }
        }
        this.cipherMode = opmode;
        this.key = key;
    }

    public void setSecureValidation(boolean secureValidation) {
        this.secureValidation = secureValidation;
    }

    public void registerInternalKeyResolver(KeyResolverSpi keyResolver) {
        if (this.internalKeyResolvers == null) {
            this.internalKeyResolvers = new ArrayList<KeyResolverSpi>();
        }
        this.internalKeyResolvers.add(keyResolver);
    }

    public EncryptedData getEncryptedData() {
        if (log.isDebugEnabled()) {
            log.debug("Returning EncryptedData");
        }
        return this.ed;
    }

    public EncryptedKey getEncryptedKey() {
        if (log.isDebugEnabled()) {
            log.debug("Returning EncryptedKey");
        }
        return this.ek;
    }

    public void setKEK(Key kek) {
        this.kek = kek;
    }

    public Element martial(EncryptedData encryptedData) {
        return this.factory.toElement(encryptedData);
    }

    public Element martial(Document context, EncryptedData encryptedData) {
        this.contextDocument = context;
        return this.factory.toElement(encryptedData);
    }

    public Element martial(EncryptedKey encryptedKey) {
        return this.factory.toElement(encryptedKey);
    }

    public Element martial(Document context, EncryptedKey encryptedKey) {
        this.contextDocument = context;
        return this.factory.toElement(encryptedKey);
    }

    public Element martial(ReferenceList referenceList) {
        return this.factory.toElement(referenceList);
    }

    public Element martial(Document context, ReferenceList referenceList) {
        this.contextDocument = context;
        return this.factory.toElement(referenceList);
    }

    private Document encryptElement(Element element) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Encrypting element...");
        }
        if (null == element) {
            log.error("Element unexpectedly null...");
        }
        if (this.cipherMode != 1 && log.isDebugEnabled()) {
            log.debug("XMLCipher unexpectedly not in ENCRYPT_MODE...");
        }
        if (this.algorithm == null) {
            throw new XMLEncryptionException("XMLCipher instance without transformation specified");
        }
        this.encryptData(this.contextDocument, element, false);
        Element encryptedElement = this.factory.toElement(this.ed);
        Node sourceParent = element.getParentNode();
        sourceParent.replaceChild(encryptedElement, element);
        return this.contextDocument;
    }

    private Document encryptElementContent(Element element) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Encrypting element content...");
        }
        if (null == element) {
            log.error("Element unexpectedly null...");
        }
        if (this.cipherMode != 1 && log.isDebugEnabled()) {
            log.debug("XMLCipher unexpectedly not in ENCRYPT_MODE...");
        }
        if (this.algorithm == null) {
            throw new XMLEncryptionException("XMLCipher instance without transformation specified");
        }
        this.encryptData(this.contextDocument, element, true);
        Element encryptedElement = this.factory.toElement(this.ed);
        XMLCipher.removeContent(element);
        element.appendChild(encryptedElement);
        return this.contextDocument;
    }

    public Document doFinal(Document context, Document source) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Processing source document...");
        }
        if (null == context) {
            log.error("Context document unexpectedly null...");
        }
        if (null == source) {
            log.error("Source document unexpectedly null...");
        }
        this.contextDocument = context;
        Document result = null;
        switch (this.cipherMode) {
            case 2: {
                result = this.decryptElement(source.getDocumentElement());
                break;
            }
            case 1: {
                result = this.encryptElement(source.getDocumentElement());
                break;
            }
            case 3: 
            case 4: {
                break;
            }
            default: {
                throw new XMLEncryptionException("empty", new IllegalStateException());
            }
        }
        return result;
    }

    public Document doFinal(Document context, Element element) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Processing source element...");
        }
        if (null == context) {
            log.error("Context document unexpectedly null...");
        }
        if (null == element) {
            log.error("Source element unexpectedly null...");
        }
        this.contextDocument = context;
        Document result = null;
        switch (this.cipherMode) {
            case 2: {
                result = this.decryptElement(element);
                break;
            }
            case 1: {
                result = this.encryptElement(element);
                break;
            }
            case 3: 
            case 4: {
                break;
            }
            default: {
                throw new XMLEncryptionException("empty", new IllegalStateException());
            }
        }
        return result;
    }

    public Document doFinal(Document context, Element element, boolean content) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Processing source element...");
        }
        if (null == context) {
            log.error("Context document unexpectedly null...");
        }
        if (null == element) {
            log.error("Source element unexpectedly null...");
        }
        this.contextDocument = context;
        Document result = null;
        switch (this.cipherMode) {
            case 2: {
                if (content) {
                    result = this.decryptElementContent(element);
                    break;
                }
                result = this.decryptElement(element);
                break;
            }
            case 1: {
                if (content) {
                    result = this.encryptElementContent(element);
                    break;
                }
                result = this.encryptElement(element);
                break;
            }
            case 3: 
            case 4: {
                break;
            }
            default: {
                throw new XMLEncryptionException("empty", new IllegalStateException());
            }
        }
        return result;
    }

    public EncryptedData encryptData(Document context, Element element) throws Exception {
        return this.encryptData(context, element, false);
    }

    public EncryptedData encryptData(Document context, String type, InputStream serializedData) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Encrypting element...");
        }
        if (null == context) {
            log.error("Context document unexpectedly null...");
        }
        if (null == serializedData) {
            log.error("Serialized data unexpectedly null...");
        }
        if (this.cipherMode != 1 && log.isDebugEnabled()) {
            log.debug("XMLCipher unexpectedly not in ENCRYPT_MODE...");
        }
        return this.encryptData(context, null, type, serializedData);
    }

    public EncryptedData encryptData(Document context, Element element, boolean contentMode) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Encrypting element...");
        }
        if (null == context) {
            log.error("Context document unexpectedly null...");
        }
        if (null == element) {
            log.error("Element unexpectedly null...");
        }
        if (this.cipherMode != 1 && log.isDebugEnabled()) {
            log.debug("XMLCipher unexpectedly not in ENCRYPT_MODE...");
        }
        if (contentMode) {
            return this.encryptData(context, element, "http://www.w3.org/2001/04/xmlenc#Content", null);
        }
        return this.encryptData(context, element, "http://www.w3.org/2001/04/xmlenc#Element", null);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private EncryptedData encryptData(Document context, Element element, String type, InputStream serializedData) throws Exception {
        this.contextDocument = context;
        if (this.algorithm == null) {
            throw new XMLEncryptionException("XMLCipher instance without transformation specified");
        }
        if (this.serializer instanceof AbstractSerializer) {
            ((AbstractSerializer)this.serializer).setSecureValidation(this.secureValidation);
        }
        byte[] serializedOctets = null;
        if (serializedData == null) {
            if (type.equals("http://www.w3.org/2001/04/xmlenc#Content")) {
                NodeList children = element.getChildNodes();
                if (null == children) {
                    Object[] exArgs = new Object[]{"Element has no content."};
                    throw new XMLEncryptionException("empty", exArgs);
                }
                serializedOctets = this.serializer.serializeToByteArray(children);
            } else {
                serializedOctets = this.serializer.serializeToByteArray(element);
            }
            if (log.isDebugEnabled()) {
                log.debug("Serialized octets:\n" + new String(serializedOctets, "UTF-8"));
            }
        }
        byte[] encryptedBytes = null;
        Cipher c = this.contextCipher == null ? this.constructCipher(this.algorithm, null) : this.contextCipher;
        byte[] iv = null;
        try {
            if (AES_128_GCM.equals(this.algorithm) || AES_192_GCM.equals(this.algorithm) || AES_256_GCM.equals(this.algorithm)) {
                if (this.random == null) {
                    this.random = SecureRandom.getInstance("SHA1PRNG");
                }
                iv = new byte[12];
                this.random.nextBytes(iv);
                AlgorithmParameterSpec paramSpec = this.constructBlockCipherParameters(this.algorithm, iv);
                c.init(this.cipherMode, this.key, paramSpec);
            } else {
                c.init(this.cipherMode, this.key);
            }
        } catch (InvalidKeyException ike) {
            throw new XMLEncryptionException("empty", ike);
        } catch (NoSuchAlgorithmException ex) {
            throw new XMLEncryptionException("empty", ex);
        }
        try {
            if (serializedData != null) {
                int numBytes;
                byte[] buf = new byte[8192];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((numBytes = serializedData.read(buf)) != -1) {
                    byte[] data = c.update(buf, 0, numBytes);
                    baos.write(data);
                }
                baos.write(c.doFinal());
                encryptedBytes = baos.toByteArray();
            } else {
                encryptedBytes = c.doFinal(serializedOctets);
                if (log.isDebugEnabled()) {
                    log.debug("Expected cipher.outputSize = " + Integer.toString(c.getOutputSize(serializedOctets.length)));
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Actual cipher.outputSize = " + Integer.toString(encryptedBytes.length));
            }
        } catch (IllegalStateException ise) {
            throw new XMLEncryptionException("empty", ise);
        } catch (IllegalBlockSizeException ibse) {
            throw new XMLEncryptionException("empty", ibse);
        } catch (BadPaddingException bpe) {
            throw new XMLEncryptionException("empty", bpe);
        } catch (UnsupportedEncodingException uee) {
            throw new XMLEncryptionException("empty", uee);
        }
        if (c.getIV() != null) {
            iv = c.getIV();
        }
        byte[] finalEncryptedBytes = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, finalEncryptedBytes, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, finalEncryptedBytes, iv.length, encryptedBytes.length);
        String base64EncodedEncryptedOctets = Base64.encode(finalEncryptedBytes);
        if (log.isDebugEnabled()) {
            log.debug("Encrypted octets:\n" + base64EncodedEncryptedOctets);
            log.debug("Encrypted octets length = " + base64EncodedEncryptedOctets.length());
        }
        try {
            CipherData cd = this.ed.getCipherData();
            CipherValue cv = cd.getCipherValue();
            cv.setValue(base64EncodedEncryptedOctets);
            if (type != null) {
                this.ed.setType(new URI(type).toString());
            }
            EncryptionMethod method = this.factory.newEncryptionMethod(new URI(this.algorithm).toString());
            method.setDigestAlgorithm(this.digestAlg);
            this.ed.setEncryptionMethod(method);
            return this.ed;
        } catch (URISyntaxException ex) {
            throw new XMLEncryptionException("empty", ex);
        }
    }

    private AlgorithmParameterSpec constructBlockCipherParameters(String algorithm, byte[] iv) {
        if (AES_128_GCM.equals(algorithm) || AES_192_GCM.equals(algorithm) || AES_256_GCM.equals(algorithm)) {
            if (gcmUseIvParameterSpec) {
                log.debug("Saw AES-GCM block cipher, using IvParameterSpec due to system property override: " + algorithm);
                return new IvParameterSpec(iv);
            }
            log.debug("Saw AES-GCM block cipher, attempting to create GCMParameterSpec: " + algorithm);
            try {
                Class<?> gcmSpecClass = ClassLoaderUtils.loadClass("javax.crypto.spec.GCMParameterSpec", this.getClass());
                AlgorithmParameterSpec gcmSpec = (AlgorithmParameterSpec)gcmSpecClass.getConstructor(Integer.TYPE, byte[].class).newInstance(128, iv);
                log.debug("Successfully created GCMParameterSpec");
                return gcmSpec;
            } catch (Exception e) {
                log.debug("Failed to create GCMParameterSpec, falling back to returning IvParameterSpec", e);
                return new IvParameterSpec(iv);
            }
        }
        log.debug("Saw non-AES-GCM mode block cipher, returning IvParameterSpec: " + algorithm);
        return new IvParameterSpec(iv);
    }

    public EncryptedData loadEncryptedData(Document context, Element element) throws XMLEncryptionException {
        if (log.isDebugEnabled()) {
            log.debug("Loading encrypted element...");
        }
        if (null == context) {
            throw new NullPointerException("Context document unexpectedly null...");
        }
        if (null == element) {
            throw new NullPointerException("Element unexpectedly null...");
        }
        if (this.cipherMode != 2) {
            throw new XMLEncryptionException("XMLCipher unexpectedly not in DECRYPT_MODE...");
        }
        this.contextDocument = context;
        this.ed = this.factory.newEncryptedData(element);
        return this.ed;
    }

    public EncryptedKey loadEncryptedKey(Document context, Element element) throws XMLEncryptionException {
        if (log.isDebugEnabled()) {
            log.debug("Loading encrypted key...");
        }
        if (null == context) {
            throw new NullPointerException("Context document unexpectedly null...");
        }
        if (null == element) {
            throw new NullPointerException("Element unexpectedly null...");
        }
        if (this.cipherMode != 4 && this.cipherMode != 2) {
            throw new XMLEncryptionException("XMLCipher unexpectedly not in UNWRAP_MODE or DECRYPT_MODE...");
        }
        this.contextDocument = context;
        this.ek = this.factory.newEncryptedKey(element);
        return this.ek;
    }

    public EncryptedKey loadEncryptedKey(Element element) throws XMLEncryptionException {
        return this.loadEncryptedKey(element.getOwnerDocument(), element);
    }

    public EncryptedKey encryptKey(Document doc, Key key) throws XMLEncryptionException {
        return this.encryptKey(doc, key, null, null);
    }

    public EncryptedKey encryptKey(Document doc, Key key, String mgfAlgorithm, byte[] oaepParams) throws XMLEncryptionException {
        if (log.isDebugEnabled()) {
            log.debug("Encrypting key ...");
        }
        if (null == key) {
            log.error("Key unexpectedly null...");
        }
        if (this.cipherMode != 3) {
            log.debug("XMLCipher unexpectedly not in WRAP_MODE...");
        }
        if (this.algorithm == null) {
            throw new XMLEncryptionException("XMLCipher instance without transformation specified");
        }
        this.contextDocument = doc;
        byte[] encryptedBytes = null;
        Cipher c = this.contextCipher == null ? this.constructCipher(this.algorithm, null) : this.contextCipher;
        try {
            OAEPParameterSpec oaepParameters = this.constructOAEPParameters(this.algorithm, this.digestAlg, mgfAlgorithm, oaepParams);
            if (oaepParameters == null) {
                c.init(3, this.key);
            } else {
                c.init(3, this.key, oaepParameters);
            }
            encryptedBytes = c.wrap(key);
        } catch (InvalidKeyException ike) {
            throw new XMLEncryptionException("empty", ike);
        } catch (IllegalBlockSizeException ibse) {
            throw new XMLEncryptionException("empty", ibse);
        } catch (InvalidAlgorithmParameterException e) {
            throw new XMLEncryptionException("empty", e);
        }
        String base64EncodedEncryptedOctets = Base64.encode(encryptedBytes);
        if (log.isDebugEnabled()) {
            log.debug("Encrypted key octets:\n" + base64EncodedEncryptedOctets);
            log.debug("Encrypted key octets length = " + base64EncodedEncryptedOctets.length());
        }
        CipherValue cv = this.ek.getCipherData().getCipherValue();
        cv.setValue(base64EncodedEncryptedOctets);
        try {
            EncryptionMethod method = this.factory.newEncryptionMethod(new URI(this.algorithm).toString());
            method.setDigestAlgorithm(this.digestAlg);
            method.setMGFAlgorithm(mgfAlgorithm);
            method.setOAEPparams(oaepParams);
            this.ek.setEncryptionMethod(method);
        } catch (URISyntaxException ex) {
            throw new XMLEncryptionException("empty", ex);
        }
        return this.ek;
    }

    public Key decryptKey(EncryptedKey encryptedKey, String algorithm) throws XMLEncryptionException {
        Key ret;
        if (log.isDebugEnabled()) {
            log.debug("Decrypting key from previously loaded EncryptedKey...");
        }
        if (this.cipherMode != 4 && log.isDebugEnabled()) {
            log.debug("XMLCipher unexpectedly not in UNWRAP_MODE...");
        }
        if (algorithm == null) {
            throw new XMLEncryptionException("Cannot decrypt a key without knowing the algorithm");
        }
        if (this.key == null) {
            block17: {
                KeyInfo ki;
                if (log.isDebugEnabled()) {
                    log.debug("Trying to find a KEK via key resolvers");
                }
                if ((ki = encryptedKey.getKeyInfo()) != null) {
                    ki.setSecureValidation(this.secureValidation);
                    try {
                        String keyWrapAlg = encryptedKey.getEncryptionMethod().getAlgorithm();
                        String keyType = JCEMapper.getJCEKeyAlgorithmFromURI(keyWrapAlg);
                        this.key = "RSA".equals(keyType) ? ki.getPrivateKey() : ki.getSecretKey();
                    } catch (Exception e) {
                        if (!log.isDebugEnabled()) break block17;
                        log.debug(e);
                    }
                }
            }
            if (this.key == null) {
                log.error("XMLCipher::decryptKey called without a KEK and cannot resolve");
                throw new XMLEncryptionException("Unable to decrypt without a KEK");
            }
        }
        XMLCipherInput cipherInput = new XMLCipherInput(encryptedKey);
        cipherInput.setSecureValidation(this.secureValidation);
        byte[] encryptedBytes = cipherInput.getBytes();
        String jceKeyAlgorithm = JCEMapper.getJCEKeyAlgorithmFromURI(algorithm);
        if (log.isDebugEnabled()) {
            log.debug("JCE Key Algorithm: " + jceKeyAlgorithm);
        }
        Cipher c = this.contextCipher == null ? this.constructCipher(encryptedKey.getEncryptionMethod().getAlgorithm(), encryptedKey.getEncryptionMethod().getDigestAlgorithm()) : this.contextCipher;
        try {
            EncryptionMethod encMethod = encryptedKey.getEncryptionMethod();
            OAEPParameterSpec oaepParameters = this.constructOAEPParameters(encMethod.getAlgorithm(), encMethod.getDigestAlgorithm(), encMethod.getMGFAlgorithm(), encMethod.getOAEPparams());
            if (oaepParameters == null) {
                c.init(4, this.key);
            } else {
                c.init(4, this.key, oaepParameters);
            }
            ret = c.unwrap(encryptedBytes, jceKeyAlgorithm, 3);
        } catch (InvalidKeyException ike) {
            throw new XMLEncryptionException("empty", ike);
        } catch (NoSuchAlgorithmException nsae) {
            throw new XMLEncryptionException("empty", nsae);
        } catch (InvalidAlgorithmParameterException e) {
            throw new XMLEncryptionException("empty", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Decryption of key type " + algorithm + " OK");
        }
        return ret;
    }

    private OAEPParameterSpec constructOAEPParameters(String encryptionAlgorithm, String digestAlgorithm, String mgfAlgorithm, byte[] oaepParams) {
        if (RSA_OAEP.equals(encryptionAlgorithm) || RSA_OAEP_11.equals(encryptionAlgorithm)) {
            String jceDigestAlgorithm = "SHA-1";
            if (digestAlgorithm != null) {
                jceDigestAlgorithm = JCEMapper.translateURItoJCEID(digestAlgorithm);
            }
            PSource.PSpecified pSource = PSource.PSpecified.DEFAULT;
            if (oaepParams != null) {
                pSource = new PSource.PSpecified(oaepParams);
            }
            MGF1ParameterSpec mgfParameterSpec = new MGF1ParameterSpec("SHA-1");
            if (RSA_OAEP_11.equals(encryptionAlgorithm)) {
                if ("http://www.w3.org/2009/xmlenc11#mgf1sha256".equals(mgfAlgorithm)) {
                    mgfParameterSpec = new MGF1ParameterSpec("SHA-256");
                } else if ("http://www.w3.org/2009/xmlenc11#mgf1sha384".equals(mgfAlgorithm)) {
                    mgfParameterSpec = new MGF1ParameterSpec("SHA-384");
                } else if ("http://www.w3.org/2009/xmlenc11#mgf1sha512".equals(mgfAlgorithm)) {
                    mgfParameterSpec = new MGF1ParameterSpec("SHA-512");
                }
            }
            return new OAEPParameterSpec(jceDigestAlgorithm, "MGF1", mgfParameterSpec, pSource);
        }
        return null;
    }

    private Cipher constructCipher(String algorithm, String digestAlgorithm) throws XMLEncryptionException {
        Cipher c;
        String jceAlgorithm = JCEMapper.translateURItoJCEID(algorithm);
        if (log.isDebugEnabled()) {
            log.debug("JCE Algorithm = " + jceAlgorithm);
        }
        try {
            c = this.requestedJCEProvider == null ? Cipher.getInstance(jceAlgorithm) : Cipher.getInstance(jceAlgorithm, this.requestedJCEProvider);
        } catch (NoSuchAlgorithmException nsae) {
            c = this.constructCipher(algorithm, digestAlgorithm, nsae);
        } catch (NoSuchProviderException nspre) {
            throw new XMLEncryptionException("empty", nspre);
        } catch (NoSuchPaddingException nspae) {
            throw new XMLEncryptionException("empty", nspae);
        }
        return c;
    }

    private Cipher constructCipher(String algorithm, String digestAlgorithm, Exception nsae) throws XMLEncryptionException {
        if (!RSA_OAEP.equals(algorithm)) {
            throw new XMLEncryptionException("empty", nsae);
        }
        if (digestAlgorithm == null || SHA1.equals(digestAlgorithm)) {
            try {
                if (this.requestedJCEProvider == null) {
                    return Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
                }
                return Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding", this.requestedJCEProvider);
            } catch (Exception ex) {
                throw new XMLEncryptionException("empty", ex);
            }
        }
        if (SHA256.equals(digestAlgorithm)) {
            try {
                if (this.requestedJCEProvider == null) {
                    return Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
                }
                return Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", this.requestedJCEProvider);
            } catch (Exception ex) {
                throw new XMLEncryptionException("empty", ex);
            }
        }
        if ("http://www.w3.org/2001/04/xmldsig-more#sha384".equals(digestAlgorithm)) {
            try {
                if (this.requestedJCEProvider == null) {
                    return Cipher.getInstance("RSA/ECB/OAEPWithSHA-384AndMGF1Padding");
                }
                return Cipher.getInstance("RSA/ECB/OAEPWithSHA-384AndMGF1Padding", this.requestedJCEProvider);
            } catch (Exception ex) {
                throw new XMLEncryptionException("empty", ex);
            }
        }
        if (SHA512.equals(digestAlgorithm)) {
            try {
                if (this.requestedJCEProvider == null) {
                    return Cipher.getInstance("RSA/ECB/OAEPWithSHA-512AndMGF1Padding");
                }
                return Cipher.getInstance("RSA/ECB/OAEPWithSHA-512AndMGF1Padding", this.requestedJCEProvider);
            } catch (Exception ex) {
                throw new XMLEncryptionException("empty", ex);
            }
        }
        throw new XMLEncryptionException("empty", nsae);
    }

    public Key decryptKey(EncryptedKey encryptedKey) throws XMLEncryptionException {
        return this.decryptKey(encryptedKey, this.ed.getEncryptionMethod().getAlgorithm());
    }

    private static void removeContent(Node node) {
        while (node.hasChildNodes()) {
            node.removeChild(node.getFirstChild());
        }
    }

    private Document decryptElement(Element element) throws XMLEncryptionException {
        if (log.isDebugEnabled()) {
            log.debug("Decrypting element...");
        }
        if (this.serializer instanceof AbstractSerializer) {
            ((AbstractSerializer)this.serializer).setSecureValidation(this.secureValidation);
        }
        if (this.cipherMode != 2) {
            log.error("XMLCipher unexpectedly not in DECRYPT_MODE...");
        }
        byte[] octets = this.decryptToByteArray(element);
        if (log.isDebugEnabled()) {
            log.debug("Decrypted octets:\n" + new String(octets));
        }
        Node sourceParent = element.getParentNode();
        Node decryptedNode = this.serializer.deserialize(octets, sourceParent);
        if (sourceParent != null && 9 == sourceParent.getNodeType()) {
            this.contextDocument.removeChild(this.contextDocument.getDocumentElement());
            this.contextDocument.appendChild(decryptedNode);
        } else if (sourceParent != null) {
            sourceParent.replaceChild(decryptedNode, element);
        }
        return this.contextDocument;
    }

    private Document decryptElementContent(Element element) throws XMLEncryptionException {
        Element e = (Element)element.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "EncryptedData").item(0);
        if (null == e) {
            throw new XMLEncryptionException("No EncryptedData child element.");
        }
        return this.decryptElement(e);
    }

    public byte[] decryptToByteArray(Element element) throws XMLEncryptionException {
        Cipher c;
        if (log.isDebugEnabled()) {
            log.debug("Decrypting to ByteArray...");
        }
        if (this.cipherMode != 2) {
            log.error("XMLCipher unexpectedly not in DECRYPT_MODE...");
        }
        EncryptedData encryptedData = this.factory.newEncryptedData(element);
        if (this.key == null) {
            block21: {
                KeyInfo ki = encryptedData.getKeyInfo();
                if (ki != null) {
                    try {
                        String encMethodAlgorithm = encryptedData.getEncryptionMethod().getAlgorithm();
                        EncryptedKeyResolver resolver = new EncryptedKeyResolver(encMethodAlgorithm, this.kek);
                        if (this.internalKeyResolvers != null) {
                            int size = this.internalKeyResolvers.size();
                            for (int i = 0; i < size; ++i) {
                                resolver.registerInternalKeyResolver(this.internalKeyResolvers.get(i));
                            }
                        }
                        ki.registerInternalKeyResolver(resolver);
                        ki.setSecureValidation(this.secureValidation);
                        this.key = ki.getSecretKey();
                    } catch (KeyResolverException kre) {
                        if (!log.isDebugEnabled()) break block21;
                        log.debug(kre);
                    }
                }
            }
            if (this.key == null) {
                log.error("XMLCipher::decryptElement called without a key and unable to resolve");
                throw new XMLEncryptionException("encryption.nokey");
            }
        }
        XMLCipherInput cipherInput = new XMLCipherInput(encryptedData);
        cipherInput.setSecureValidation(this.secureValidation);
        byte[] encryptedBytes = cipherInput.getBytes();
        String jceAlgorithm = JCEMapper.translateURItoJCEID(encryptedData.getEncryptionMethod().getAlgorithm());
        if (log.isDebugEnabled()) {
            log.debug("JCE Algorithm = " + jceAlgorithm);
        }
        try {
            c = this.requestedJCEProvider == null ? Cipher.getInstance(jceAlgorithm) : Cipher.getInstance(jceAlgorithm, this.requestedJCEProvider);
        } catch (NoSuchAlgorithmException nsae) {
            throw new XMLEncryptionException("empty", nsae);
        } catch (NoSuchProviderException nspre) {
            throw new XMLEncryptionException("empty", nspre);
        } catch (NoSuchPaddingException nspae) {
            throw new XMLEncryptionException("empty", nspae);
        }
        int ivLen = c.getBlockSize();
        String alg = encryptedData.getEncryptionMethod().getAlgorithm();
        if (AES_128_GCM.equals(alg) || AES_192_GCM.equals(alg) || AES_256_GCM.equals(alg)) {
            ivLen = 12;
        }
        byte[] ivBytes = new byte[ivLen];
        System.arraycopy(encryptedBytes, 0, ivBytes, 0, ivLen);
        AlgorithmParameterSpec paramSpec = this.constructBlockCipherParameters(this.algorithm, ivBytes);
        try {
            c.init(this.cipherMode, this.key, paramSpec);
        } catch (InvalidKeyException ike) {
            throw new XMLEncryptionException("empty", ike);
        } catch (InvalidAlgorithmParameterException iape) {
            throw new XMLEncryptionException("empty", iape);
        }
        try {
            return c.doFinal(encryptedBytes, ivLen, encryptedBytes.length - ivLen);
        } catch (IllegalBlockSizeException ibse) {
            throw new XMLEncryptionException("empty", ibse);
        } catch (BadPaddingException bpe) {
            throw new XMLEncryptionException("empty", bpe);
        }
    }

    public EncryptedData createEncryptedData(int type, String value) throws XMLEncryptionException {
        EncryptedData result = null;
        CipherData data = null;
        switch (type) {
            case 2: {
                CipherReference cipherReference = this.factory.newCipherReference(value);
                data = this.factory.newCipherData(type);
                data.setCipherReference(cipherReference);
                result = this.factory.newEncryptedData(data);
                break;
            }
            case 1: {
                CipherValue cipherValue = this.factory.newCipherValue(value);
                data = this.factory.newCipherData(type);
                data.setCipherValue(cipherValue);
                result = this.factory.newEncryptedData(data);
            }
        }
        return result;
    }

    public EncryptedKey createEncryptedKey(int type, String value) throws XMLEncryptionException {
        EncryptedKey result = null;
        CipherData data = null;
        switch (type) {
            case 2: {
                CipherReference cipherReference = this.factory.newCipherReference(value);
                data = this.factory.newCipherData(type);
                data.setCipherReference(cipherReference);
                result = this.factory.newEncryptedKey(data);
                break;
            }
            case 1: {
                CipherValue cipherValue = this.factory.newCipherValue(value);
                data = this.factory.newCipherData(type);
                data.setCipherValue(cipherValue);
                result = this.factory.newEncryptedKey(data);
            }
        }
        return result;
    }

    public AgreementMethod createAgreementMethod(String algorithm) {
        return this.factory.newAgreementMethod(algorithm);
    }

    public CipherData createCipherData(int type) {
        return this.factory.newCipherData(type);
    }

    public CipherReference createCipherReference(String uri) {
        return this.factory.newCipherReference(uri);
    }

    public CipherValue createCipherValue(String value) {
        return this.factory.newCipherValue(value);
    }

    public EncryptionMethod createEncryptionMethod(String algorithm) {
        return this.factory.newEncryptionMethod(algorithm);
    }

    public EncryptionProperties createEncryptionProperties() {
        return this.factory.newEncryptionProperties();
    }

    public EncryptionProperty createEncryptionProperty() {
        return this.factory.newEncryptionProperty();
    }

    public ReferenceList createReferenceList(int type) {
        return this.factory.newReferenceList(type);
    }

    public Transforms createTransforms() {
        return this.factory.newTransforms();
    }

    public Transforms createTransforms(Document doc) {
        return this.factory.newTransforms(doc);
    }

    private class Factory {
        private Factory() {
        }

        AgreementMethod newAgreementMethod(String algorithm) {
            return new AgreementMethodImpl(algorithm);
        }

        CipherData newCipherData(int type) {
            return new CipherDataImpl(type);
        }

        CipherReference newCipherReference(String uri) {
            return new CipherReferenceImpl(uri);
        }

        CipherValue newCipherValue(String value) {
            return new CipherValueImpl(value);
        }

        EncryptedData newEncryptedData(CipherData data) {
            return new EncryptedDataImpl(data);
        }

        EncryptedKey newEncryptedKey(CipherData data) {
            return new EncryptedKeyImpl(data);
        }

        EncryptionMethod newEncryptionMethod(String algorithm) {
            return new EncryptionMethodImpl(algorithm);
        }

        EncryptionProperties newEncryptionProperties() {
            return new EncryptionPropertiesImpl();
        }

        EncryptionProperty newEncryptionProperty() {
            return new EncryptionPropertyImpl();
        }

        ReferenceList newReferenceList(int type) {
            return new ReferenceListImpl(type);
        }

        Transforms newTransforms() {
            return new TransformsImpl();
        }

        Transforms newTransforms(Document doc) {
            return new TransformsImpl(doc);
        }

        CipherData newCipherData(Element element) throws XMLEncryptionException {
            if (null == element) {
                throw new NullPointerException("element is null");
            }
            int type = 0;
            Element e = null;
            if (element.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "CipherValue").getLength() > 0) {
                type = 1;
                e = (Element)element.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "CipherValue").item(0);
            } else if (element.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "CipherReference").getLength() > 0) {
                type = 2;
                e = (Element)element.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "CipherReference").item(0);
            }
            CipherData result = this.newCipherData(type);
            if (type == 1) {
                result.setCipherValue(this.newCipherValue(e));
            } else if (type == 2) {
                result.setCipherReference(this.newCipherReference(e));
            }
            return result;
        }

        CipherReference newCipherReference(Element element) throws XMLEncryptionException {
            Attr uriAttr = element.getAttributeNodeNS(null, "URI");
            CipherReferenceImpl result = new CipherReferenceImpl(uriAttr);
            NodeList transformsElements = element.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "Transforms");
            Element transformsElement = (Element)transformsElements.item(0);
            if (transformsElement != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Creating a DSIG based Transforms element");
                }
                try {
                    result.setTransforms(new TransformsImpl(transformsElement));
                } catch (XMLSignatureException xse) {
                    throw new XMLEncryptionException("empty", xse);
                } catch (InvalidTransformException ite) {
                    throw new XMLEncryptionException("empty", ite);
                } catch (XMLSecurityException xse) {
                    throw new XMLEncryptionException("empty", xse);
                }
            }
            return result;
        }

        CipherValue newCipherValue(Element element) {
            String value = XMLUtils.getFullTextChildrenFromElement(element);
            return this.newCipherValue(value);
        }

        EncryptedData newEncryptedData(Element element) throws XMLEncryptionException {
            Element encryptionPropertiesElement;
            Element keyInfoElement;
            EncryptedData result = null;
            NodeList dataElements = element.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "CipherData");
            Element dataElement = (Element)dataElements.item(dataElements.getLength() - 1);
            CipherData data = this.newCipherData(dataElement);
            result = this.newEncryptedData(data);
            result.setId(element.getAttributeNS(null, "Id"));
            result.setType(element.getAttributeNS(null, "Type"));
            result.setMimeType(element.getAttributeNS(null, "MimeType"));
            result.setEncoding(element.getAttributeNS(null, "Encoding"));
            Element encryptionMethodElement = (Element)element.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "EncryptionMethod").item(0);
            if (null != encryptionMethodElement) {
                result.setEncryptionMethod(this.newEncryptionMethod(encryptionMethodElement));
            }
            if (null != (keyInfoElement = (Element)element.getElementsByTagNameNS(XMLCipher.XML_DSIG, "KeyInfo").item(0))) {
                KeyInfo ki = this.newKeyInfo(keyInfoElement);
                result.setKeyInfo(ki);
            }
            if (null != (encryptionPropertiesElement = (Element)element.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "EncryptionProperties").item(0))) {
                result.setEncryptionProperties(this.newEncryptionProperties(encryptionPropertiesElement));
            }
            return result;
        }

        EncryptedKey newEncryptedKey(Element element) throws XMLEncryptionException {
            Element carriedNameElement;
            Element referenceListElement;
            Element encryptionPropertiesElement;
            Element keyInfoElement;
            EncryptedKey result = null;
            NodeList dataElements = element.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "CipherData");
            Element dataElement = (Element)dataElements.item(dataElements.getLength() - 1);
            CipherData data = this.newCipherData(dataElement);
            result = this.newEncryptedKey(data);
            result.setId(element.getAttributeNS(null, "Id"));
            result.setType(element.getAttributeNS(null, "Type"));
            result.setMimeType(element.getAttributeNS(null, "MimeType"));
            result.setEncoding(element.getAttributeNS(null, "Encoding"));
            result.setRecipient(element.getAttributeNS(null, "Recipient"));
            Element encryptionMethodElement = (Element)element.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "EncryptionMethod").item(0);
            if (null != encryptionMethodElement) {
                result.setEncryptionMethod(this.newEncryptionMethod(encryptionMethodElement));
            }
            if (null != (keyInfoElement = (Element)element.getElementsByTagNameNS(XMLCipher.XML_DSIG, "KeyInfo").item(0))) {
                KeyInfo ki = this.newKeyInfo(keyInfoElement);
                result.setKeyInfo(ki);
            }
            if (null != (encryptionPropertiesElement = (Element)element.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "EncryptionProperties").item(0))) {
                result.setEncryptionProperties(this.newEncryptionProperties(encryptionPropertiesElement));
            }
            if (null != (referenceListElement = (Element)element.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "ReferenceList").item(0))) {
                result.setReferenceList(this.newReferenceList(referenceListElement));
            }
            if (null != (carriedNameElement = (Element)element.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "CarriedKeyName").item(0))) {
                result.setCarriedName(carriedNameElement.getFirstChild().getNodeValue());
            }
            return result;
        }

        KeyInfo newKeyInfo(Element element) throws XMLEncryptionException {
            try {
                KeyInfo ki = new KeyInfo(element, null);
                ki.setSecureValidation(XMLCipher.this.secureValidation);
                if (XMLCipher.this.internalKeyResolvers != null) {
                    int size = XMLCipher.this.internalKeyResolvers.size();
                    for (int i = 0; i < size; ++i) {
                        ki.registerInternalKeyResolver((KeyResolverSpi)XMLCipher.this.internalKeyResolvers.get(i));
                    }
                }
                return ki;
            } catch (XMLSecurityException xse) {
                throw new XMLEncryptionException("Error loading Key Info", xse);
            }
        }

        EncryptionMethod newEncryptionMethod(Element element) {
            Element mgfElement;
            Element digestElement;
            Element oaepParamsElement;
            String encAlgorithm = element.getAttributeNS(null, "Algorithm");
            EncryptionMethod result = this.newEncryptionMethod(encAlgorithm);
            Element keySizeElement = (Element)element.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "KeySize").item(0);
            if (null != keySizeElement) {
                result.setKeySize(Integer.valueOf(keySizeElement.getFirstChild().getNodeValue()));
            }
            if (null != (oaepParamsElement = (Element)element.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "OAEPparams").item(0))) {
                try {
                    String oaepParams = oaepParamsElement.getFirstChild().getNodeValue();
                    result.setOAEPparams(Base64.decode(oaepParams.getBytes("UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("UTF-8 not supported", e);
                } catch (Base64DecodingException e) {
                    throw new RuntimeException("BASE-64 decoding error", e);
                }
            }
            if ((digestElement = (Element)element.getElementsByTagNameNS(XMLCipher.XML_DSIG, "DigestMethod").item(0)) != null) {
                String digestAlgorithm = digestElement.getAttributeNS(null, "Algorithm");
                result.setDigestAlgorithm(digestAlgorithm);
            }
            if ((mgfElement = (Element)element.getElementsByTagNameNS("http://www.w3.org/2009/xmlenc11#", "MGF").item(0)) != null && !XMLCipher.RSA_OAEP.equals(XMLCipher.this.algorithm)) {
                String mgfAlgorithm = mgfElement.getAttributeNS(null, "Algorithm");
                result.setMGFAlgorithm(mgfAlgorithm);
            }
            return result;
        }

        EncryptionProperties newEncryptionProperties(Element element) {
            EncryptionProperties result = this.newEncryptionProperties();
            result.setId(element.getAttributeNS(null, "Id"));
            NodeList encryptionPropertyList = element.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "EncryptionProperty");
            for (int i = 0; i < encryptionPropertyList.getLength(); ++i) {
                Node n = encryptionPropertyList.item(i);
                if (null == n) continue;
                result.addEncryptionProperty(this.newEncryptionProperty((Element)n));
            }
            return result;
        }

        EncryptionProperty newEncryptionProperty(Element element) {
            EncryptionProperty result = this.newEncryptionProperty();
            result.setTarget(element.getAttributeNS(null, "Target"));
            result.setId(element.getAttributeNS(null, "Id"));
            return result;
        }

        ReferenceList newReferenceList(Element element) {
            int type = 0;
            if (null != element.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "DataReference").item(0)) {
                type = 1;
            } else if (null != element.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "KeyReference").item(0)) {
                type = 2;
            }
            ReferenceListImpl result = new ReferenceListImpl(type);
            NodeList list = null;
            switch (type) {
                case 1: {
                    list = element.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "DataReference");
                    for (int i = 0; i < list.getLength(); ++i) {
                        String uri = ((Element)list.item(i)).getAttributeNS(null, "URI");
                        result.add(result.newDataReference(uri));
                    }
                    break;
                }
                case 2: {
                    list = element.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "KeyReference");
                    for (int i = 0; i < list.getLength(); ++i) {
                        String uri = ((Element)list.item(i)).getAttributeNS(null, "URI");
                        result.add(result.newKeyReference(uri));
                    }
                    break;
                }
            }
            return result;
        }

        Element toElement(EncryptedData encryptedData) {
            return ((EncryptedDataImpl)encryptedData).toElement();
        }

        Element toElement(EncryptedKey encryptedKey) {
            return ((EncryptedKeyImpl)encryptedKey).toElement();
        }

        Element toElement(ReferenceList referenceList) {
            return ((ReferenceListImpl)referenceList).toElement();
        }

        /*
         * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
         */
        private class ReferenceListImpl
        implements ReferenceList {
            private Class<?> sentry;
            private List<Reference> references;

            public ReferenceListImpl(int type) {
                if (type == 1) {
                    this.sentry = DataReference.class;
                } else if (type == 2) {
                    this.sentry = KeyReference.class;
                } else {
                    throw new IllegalArgumentException();
                }
                this.references = new LinkedList<Reference>();
            }

            @Override
            public void add(Reference reference) {
                if (!reference.getClass().equals(this.sentry)) {
                    throw new IllegalArgumentException();
                }
                this.references.add(reference);
            }

            @Override
            public void remove(Reference reference) {
                if (!reference.getClass().equals(this.sentry)) {
                    throw new IllegalArgumentException();
                }
                this.references.remove(reference);
            }

            @Override
            public int size() {
                return this.references.size();
            }

            @Override
            public boolean isEmpty() {
                return this.references.isEmpty();
            }

            @Override
            public Iterator<Reference> getReferences() {
                return this.references.iterator();
            }

            Element toElement() {
                Element result = ElementProxy.createElementForFamily(XMLCipher.this.contextDocument, "http://www.w3.org/2001/04/xmlenc#", "ReferenceList");
                for (Reference reference : this.references) {
                    result.appendChild(((ReferenceImpl)reference).toElement());
                }
                return result;
            }

            @Override
            public Reference newDataReference(String uri) {
                return new DataReference(uri);
            }

            @Override
            public Reference newKeyReference(String uri) {
                return new KeyReference(uri);
            }

            private class KeyReference
            extends ReferenceImpl {
                KeyReference(String uri) {
                    super(uri);
                }

                public String getType() {
                    return "KeyReference";
                }
            }

            private class DataReference
            extends ReferenceImpl {
                DataReference(String uri) {
                    super(uri);
                }

                public String getType() {
                    return "DataReference";
                }
            }

            /*
             * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
             */
            private abstract class ReferenceImpl
            implements Reference {
                private String uri;
                private List<Element> referenceInformation;

                ReferenceImpl(String uri) {
                    this.uri = uri;
                    this.referenceInformation = new LinkedList<Element>();
                }

                @Override
                public abstract String getType();

                @Override
                public String getURI() {
                    return this.uri;
                }

                @Override
                public Iterator<Element> getElementRetrievalInformation() {
                    return this.referenceInformation.iterator();
                }

                @Override
                public void setURI(String uri) {
                    this.uri = uri;
                }

                @Override
                public void removeElementRetrievalInformation(Element node) {
                    this.referenceInformation.remove(node);
                }

                @Override
                public void addElementRetrievalInformation(Element node) {
                    this.referenceInformation.add(node);
                }

                public Element toElement() {
                    String tagName = this.getType();
                    Element result = ElementProxy.createElementForFamily(XMLCipher.this.contextDocument, "http://www.w3.org/2001/04/xmlenc#", tagName);
                    result.setAttributeNS(null, "URI", this.uri);
                    return result;
                }
            }
        }

        private class TransformsImpl
        extends org.apache.xml.security.transforms.Transforms
        implements Transforms {
            public TransformsImpl() {
                super(XMLCipher.this.contextDocument);
            }

            public TransformsImpl(Document doc) {
                if (doc == null) {
                    throw new RuntimeException("Document is null");
                }
                this.doc = doc;
                this.constructionElement = this.createElementForFamilyLocal(this.doc, this.getBaseNamespace(), this.getBaseLocalName());
            }

            public TransformsImpl(Element element) throws XMLSignatureException, InvalidTransformException, XMLSecurityException, TransformationException {
                super(element, "");
            }

            public Element toElement() {
                if (this.doc == null) {
                    this.doc = XMLCipher.this.contextDocument;
                }
                return this.getElement();
            }

            public org.apache.xml.security.transforms.Transforms getDSTransforms() {
                return this;
            }

            public String getBaseNamespace() {
                return "http://www.w3.org/2001/04/xmlenc#";
            }
        }

        /*
         * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
         */
        private class EncryptionPropertyImpl
        implements EncryptionProperty {
            private String target = null;
            private String id = null;
            private Map<String, String> attributeMap = new HashMap<String, String>();
            private List<Element> encryptionInformation = new LinkedList<Element>();

            @Override
            public String getTarget() {
                return this.target;
            }

            @Override
            public void setTarget(String target) {
                if (target == null || target.length() == 0) {
                    this.target = null;
                } else if (target.startsWith("#")) {
                    this.target = target;
                } else {
                    URI tmpTarget = null;
                    try {
                        tmpTarget = new URI(target);
                    } catch (URISyntaxException ex) {
                        throw (IllegalArgumentException)new IllegalArgumentException().initCause(ex);
                    }
                    this.target = tmpTarget.toString();
                }
            }

            @Override
            public String getId() {
                return this.id;
            }

            @Override
            public void setId(String id) {
                this.id = id;
            }

            @Override
            public String getAttribute(String attribute) {
                return this.attributeMap.get(attribute);
            }

            @Override
            public void setAttribute(String attribute, String value) {
                this.attributeMap.put(attribute, value);
            }

            @Override
            public Iterator<Element> getEncryptionInformation() {
                return this.encryptionInformation.iterator();
            }

            @Override
            public void addEncryptionInformation(Element info) {
                this.encryptionInformation.add(info);
            }

            @Override
            public void removeEncryptionInformation(Element info) {
                this.encryptionInformation.remove(info);
            }

            Element toElement() {
                Element result = XMLUtils.createElementInEncryptionSpace(XMLCipher.this.contextDocument, "EncryptionProperty");
                if (null != this.target) {
                    result.setAttributeNS(null, "Target", this.target);
                }
                if (null != this.id) {
                    result.setAttributeNS(null, "Id", this.id);
                }
                if (!this.attributeMap.isEmpty()) {
                    for (String attribute : this.attributeMap.keySet()) {
                        result.setAttributeNS("http://www.w3.org/XML/1998/namespace", attribute, this.attributeMap.get(attribute));
                    }
                }
                if (!this.encryptionInformation.isEmpty()) {
                    for (Element element : this.encryptionInformation) {
                        result.appendChild(element);
                    }
                }
                return result;
            }
        }

        /*
         * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
         */
        private class EncryptionPropertiesImpl
        implements EncryptionProperties {
            private String id = null;
            private List<EncryptionProperty> encryptionProperties = new LinkedList<EncryptionProperty>();

            @Override
            public String getId() {
                return this.id;
            }

            @Override
            public void setId(String id) {
                this.id = id;
            }

            @Override
            public Iterator<EncryptionProperty> getEncryptionProperties() {
                return this.encryptionProperties.iterator();
            }

            @Override
            public void addEncryptionProperty(EncryptionProperty property) {
                this.encryptionProperties.add(property);
            }

            @Override
            public void removeEncryptionProperty(EncryptionProperty property) {
                this.encryptionProperties.remove(property);
            }

            Element toElement() {
                Element result = XMLUtils.createElementInEncryptionSpace(XMLCipher.this.contextDocument, "EncryptionProperties");
                if (null != this.id) {
                    result.setAttributeNS(null, "Id", this.id);
                }
                Iterator<EncryptionProperty> itr = this.getEncryptionProperties();
                while (itr.hasNext()) {
                    result.appendChild(((EncryptionPropertyImpl)itr.next()).toElement());
                }
                return result;
            }
        }

        /*
         * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
         */
        private class EncryptionMethodImpl
        implements EncryptionMethod {
            private String algorithm = null;
            private int keySize = Integer.MIN_VALUE;
            private byte[] oaepParams = null;
            private List<Element> encryptionMethodInformation = null;
            private String digestAlgorithm = null;
            private String mgfAlgorithm = null;

            public EncryptionMethodImpl(String algorithm) {
                URI tmpAlgorithm = null;
                try {
                    tmpAlgorithm = new URI(algorithm);
                } catch (URISyntaxException ex) {
                    throw (IllegalArgumentException)new IllegalArgumentException().initCause(ex);
                }
                this.algorithm = tmpAlgorithm.toString();
                this.encryptionMethodInformation = new LinkedList<Element>();
            }

            @Override
            public String getAlgorithm() {
                return this.algorithm;
            }

            @Override
            public int getKeySize() {
                return this.keySize;
            }

            @Override
            public void setKeySize(int size) {
                this.keySize = size;
            }

            @Override
            public byte[] getOAEPparams() {
                return this.oaepParams;
            }

            @Override
            public void setOAEPparams(byte[] params) {
                this.oaepParams = params;
            }

            @Override
            public void setDigestAlgorithm(String digestAlgorithm) {
                this.digestAlgorithm = digestAlgorithm;
            }

            @Override
            public String getDigestAlgorithm() {
                return this.digestAlgorithm;
            }

            @Override
            public void setMGFAlgorithm(String mgfAlgorithm) {
                this.mgfAlgorithm = mgfAlgorithm;
            }

            @Override
            public String getMGFAlgorithm() {
                return this.mgfAlgorithm;
            }

            @Override
            public Iterator<Element> getEncryptionMethodInformation() {
                return this.encryptionMethodInformation.iterator();
            }

            @Override
            public void addEncryptionMethodInformation(Element info) {
                this.encryptionMethodInformation.add(info);
            }

            @Override
            public void removeEncryptionMethodInformation(Element info) {
                this.encryptionMethodInformation.remove(info);
            }

            Element toElement() {
                Element result = XMLUtils.createElementInEncryptionSpace(XMLCipher.this.contextDocument, "EncryptionMethod");
                result.setAttributeNS(null, "Algorithm", this.algorithm);
                if (this.keySize > 0) {
                    result.appendChild(XMLUtils.createElementInEncryptionSpace(XMLCipher.this.contextDocument, "KeySize").appendChild(XMLCipher.this.contextDocument.createTextNode(String.valueOf(this.keySize))));
                }
                if (null != this.oaepParams) {
                    Element oaepElement = XMLUtils.createElementInEncryptionSpace(XMLCipher.this.contextDocument, "OAEPparams");
                    oaepElement.appendChild(XMLCipher.this.contextDocument.createTextNode(Base64.encode(this.oaepParams)));
                    result.appendChild(oaepElement);
                }
                if (this.digestAlgorithm != null) {
                    Element digestElement = XMLUtils.createElementInSignatureSpace(XMLCipher.this.contextDocument, "DigestMethod");
                    digestElement.setAttributeNS(null, "Algorithm", this.digestAlgorithm);
                    digestElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + ElementProxy.getDefaultPrefix(XMLCipher.XML_DSIG), XMLCipher.XML_DSIG);
                    result.appendChild(digestElement);
                }
                if (this.mgfAlgorithm != null) {
                    Element mgfElement = XMLUtils.createElementInEncryption11Space(XMLCipher.this.contextDocument, "MGF");
                    mgfElement.setAttributeNS(null, "Algorithm", this.mgfAlgorithm);
                    mgfElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + ElementProxy.getDefaultPrefix("http://www.w3.org/2009/xmlenc11#"), "http://www.w3.org/2009/xmlenc11#");
                    result.appendChild(mgfElement);
                }
                Iterator<Element> itr = this.encryptionMethodInformation.iterator();
                while (itr.hasNext()) {
                    result.appendChild(itr.next());
                }
                return result;
            }
        }

        private abstract class EncryptedTypeImpl {
            private String id = null;
            private String type = null;
            private String mimeType = null;
            private String encoding = null;
            private EncryptionMethod encryptionMethod = null;
            private KeyInfo keyInfo = null;
            private CipherData cipherData = null;
            private EncryptionProperties encryptionProperties = null;

            protected EncryptedTypeImpl(CipherData data) {
                this.cipherData = data;
            }

            public String getId() {
                return this.id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getType() {
                return this.type;
            }

            public void setType(String type) {
                if (type == null || type.length() == 0) {
                    this.type = null;
                } else {
                    URI tmpType = null;
                    try {
                        tmpType = new URI(type);
                    } catch (URISyntaxException ex) {
                        throw (IllegalArgumentException)new IllegalArgumentException().initCause(ex);
                    }
                    this.type = tmpType.toString();
                }
            }

            public String getMimeType() {
                return this.mimeType;
            }

            public void setMimeType(String type) {
                this.mimeType = type;
            }

            public String getEncoding() {
                return this.encoding;
            }

            public void setEncoding(String encoding) {
                if (encoding == null || encoding.length() == 0) {
                    this.encoding = null;
                } else {
                    URI tmpEncoding = null;
                    try {
                        tmpEncoding = new URI(encoding);
                    } catch (URISyntaxException ex) {
                        throw (IllegalArgumentException)new IllegalArgumentException().initCause(ex);
                    }
                    this.encoding = tmpEncoding.toString();
                }
            }

            public EncryptionMethod getEncryptionMethod() {
                return this.encryptionMethod;
            }

            public void setEncryptionMethod(EncryptionMethod method) {
                this.encryptionMethod = method;
            }

            public KeyInfo getKeyInfo() {
                return this.keyInfo;
            }

            public void setKeyInfo(KeyInfo info) {
                this.keyInfo = info;
            }

            public CipherData getCipherData() {
                return this.cipherData;
            }

            public EncryptionProperties getEncryptionProperties() {
                return this.encryptionProperties;
            }

            public void setEncryptionProperties(EncryptionProperties properties) {
                this.encryptionProperties = properties;
            }
        }

        private class EncryptedKeyImpl
        extends EncryptedTypeImpl
        implements EncryptedKey {
            private String keyRecipient;
            private ReferenceList referenceList;
            private String carriedName;

            public EncryptedKeyImpl(CipherData data) {
                super(data);
                this.keyRecipient = null;
                this.referenceList = null;
                this.carriedName = null;
            }

            public String getRecipient() {
                return this.keyRecipient;
            }

            public void setRecipient(String recipient) {
                this.keyRecipient = recipient;
            }

            public ReferenceList getReferenceList() {
                return this.referenceList;
            }

            public void setReferenceList(ReferenceList list) {
                this.referenceList = list;
            }

            public String getCarriedName() {
                return this.carriedName;
            }

            public void setCarriedName(String name) {
                this.carriedName = name;
            }

            Element toElement() {
                Element result = ElementProxy.createElementForFamily(XMLCipher.this.contextDocument, "http://www.w3.org/2001/04/xmlenc#", "EncryptedKey");
                if (null != super.getId()) {
                    result.setAttributeNS(null, "Id", super.getId());
                }
                if (null != super.getType()) {
                    result.setAttributeNS(null, "Type", super.getType());
                }
                if (null != super.getMimeType()) {
                    result.setAttributeNS(null, "MimeType", super.getMimeType());
                }
                if (null != super.getEncoding()) {
                    result.setAttributeNS(null, "Encoding", super.getEncoding());
                }
                if (null != this.getRecipient()) {
                    result.setAttributeNS(null, "Recipient", this.getRecipient());
                }
                if (null != super.getEncryptionMethod()) {
                    result.appendChild(((EncryptionMethodImpl)super.getEncryptionMethod()).toElement());
                }
                if (null != super.getKeyInfo()) {
                    result.appendChild(super.getKeyInfo().getElement().cloneNode(true));
                }
                result.appendChild(((CipherDataImpl)super.getCipherData()).toElement());
                if (null != super.getEncryptionProperties()) {
                    result.appendChild(((EncryptionPropertiesImpl)super.getEncryptionProperties()).toElement());
                }
                if (this.referenceList != null && !this.referenceList.isEmpty()) {
                    result.appendChild(((ReferenceListImpl)this.getReferenceList()).toElement());
                }
                if (null != this.carriedName) {
                    Element element = ElementProxy.createElementForFamily(XMLCipher.this.contextDocument, "http://www.w3.org/2001/04/xmlenc#", "CarriedKeyName");
                    Text node = XMLCipher.this.contextDocument.createTextNode(this.carriedName);
                    element.appendChild(node);
                    result.appendChild(element);
                }
                return result;
            }
        }

        private class EncryptedDataImpl
        extends EncryptedTypeImpl
        implements EncryptedData {
            public EncryptedDataImpl(CipherData data) {
                super(data);
            }

            Element toElement() {
                Element result = ElementProxy.createElementForFamily(XMLCipher.this.contextDocument, "http://www.w3.org/2001/04/xmlenc#", "EncryptedData");
                if (null != super.getId()) {
                    result.setAttributeNS(null, "Id", super.getId());
                }
                if (null != super.getType()) {
                    result.setAttributeNS(null, "Type", super.getType());
                }
                if (null != super.getMimeType()) {
                    result.setAttributeNS(null, "MimeType", super.getMimeType());
                }
                if (null != super.getEncoding()) {
                    result.setAttributeNS(null, "Encoding", super.getEncoding());
                }
                if (null != super.getEncryptionMethod()) {
                    result.appendChild(((EncryptionMethodImpl)super.getEncryptionMethod()).toElement());
                }
                if (null != super.getKeyInfo()) {
                    result.appendChild(super.getKeyInfo().getElement().cloneNode(true));
                }
                result.appendChild(((CipherDataImpl)super.getCipherData()).toElement());
                if (null != super.getEncryptionProperties()) {
                    result.appendChild(((EncryptionPropertiesImpl)super.getEncryptionProperties()).toElement());
                }
                return result;
            }
        }

        private class CipherValueImpl
        implements CipherValue {
            private String cipherValue = null;

            public CipherValueImpl(String value) {
                this.cipherValue = value;
            }

            public String getValue() {
                return this.cipherValue;
            }

            public void setValue(String value) {
                this.cipherValue = value;
            }

            Element toElement() {
                Element result = XMLUtils.createElementInEncryptionSpace(XMLCipher.this.contextDocument, "CipherValue");
                result.appendChild(XMLCipher.this.contextDocument.createTextNode(this.cipherValue));
                return result;
            }
        }

        private class CipherReferenceImpl
        implements CipherReference {
            private String referenceURI = null;
            private Transforms referenceTransforms = null;
            private Attr referenceNode = null;

            public CipherReferenceImpl(String uri) {
                this.referenceURI = uri;
                this.referenceNode = null;
            }

            public CipherReferenceImpl(Attr uri) {
                this.referenceURI = uri.getNodeValue();
                this.referenceNode = uri;
            }

            public String getURI() {
                return this.referenceURI;
            }

            public Attr getURIAsAttr() {
                return this.referenceNode;
            }

            public Transforms getTransforms() {
                return this.referenceTransforms;
            }

            public void setTransforms(Transforms transforms) {
                this.referenceTransforms = transforms;
            }

            Element toElement() {
                Element result = XMLUtils.createElementInEncryptionSpace(XMLCipher.this.contextDocument, "CipherReference");
                result.setAttributeNS(null, "URI", this.referenceURI);
                if (null != this.referenceTransforms) {
                    result.appendChild(((TransformsImpl)this.referenceTransforms).toElement());
                }
                return result;
            }
        }

        private class CipherDataImpl
        implements CipherData {
            private static final String valueMessage = "Data type is reference type.";
            private static final String referenceMessage = "Data type is value type.";
            private CipherValue cipherValue = null;
            private CipherReference cipherReference = null;
            private int cipherType = Integer.MIN_VALUE;

            public CipherDataImpl(int type) {
                this.cipherType = type;
            }

            public CipherValue getCipherValue() {
                return this.cipherValue;
            }

            public void setCipherValue(CipherValue value) throws XMLEncryptionException {
                if (this.cipherType == 2) {
                    throw new XMLEncryptionException("empty", new UnsupportedOperationException(valueMessage));
                }
                this.cipherValue = value;
            }

            public CipherReference getCipherReference() {
                return this.cipherReference;
            }

            public void setCipherReference(CipherReference reference) throws XMLEncryptionException {
                if (this.cipherType == 1) {
                    throw new XMLEncryptionException("empty", new UnsupportedOperationException(referenceMessage));
                }
                this.cipherReference = reference;
            }

            public int getDataType() {
                return this.cipherType;
            }

            Element toElement() {
                Element result = XMLUtils.createElementInEncryptionSpace(XMLCipher.this.contextDocument, "CipherData");
                if (this.cipherType == 1) {
                    result.appendChild(((CipherValueImpl)this.cipherValue).toElement());
                } else if (this.cipherType == 2) {
                    result.appendChild(((CipherReferenceImpl)this.cipherReference).toElement());
                }
                return result;
            }
        }

        /*
         * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
         */
        private class AgreementMethodImpl
        implements AgreementMethod {
            private byte[] kaNonce = null;
            private List<Element> agreementMethodInformation = new LinkedList<Element>();
            private KeyInfo originatorKeyInfo = null;
            private KeyInfo recipientKeyInfo = null;
            private String algorithmURI = null;

            public AgreementMethodImpl(String algorithm) {
                URI tmpAlgorithm = null;
                try {
                    tmpAlgorithm = new URI(algorithm);
                } catch (URISyntaxException ex) {
                    throw (IllegalArgumentException)new IllegalArgumentException().initCause(ex);
                }
                this.algorithmURI = tmpAlgorithm.toString();
            }

            @Override
            public byte[] getKANonce() {
                return this.kaNonce;
            }

            @Override
            public void setKANonce(byte[] kanonce) {
                this.kaNonce = kanonce;
            }

            @Override
            public Iterator<Element> getAgreementMethodInformation() {
                return this.agreementMethodInformation.iterator();
            }

            @Override
            public void addAgreementMethodInformation(Element info) {
                this.agreementMethodInformation.add(info);
            }

            @Override
            public void revoveAgreementMethodInformation(Element info) {
                this.agreementMethodInformation.remove(info);
            }

            @Override
            public KeyInfo getOriginatorKeyInfo() {
                return this.originatorKeyInfo;
            }

            @Override
            public void setOriginatorKeyInfo(KeyInfo keyInfo) {
                this.originatorKeyInfo = keyInfo;
            }

            @Override
            public KeyInfo getRecipientKeyInfo() {
                return this.recipientKeyInfo;
            }

            @Override
            public void setRecipientKeyInfo(KeyInfo keyInfo) {
                this.recipientKeyInfo = keyInfo;
            }

            @Override
            public String getAlgorithm() {
                return this.algorithmURI;
            }
        }
    }
}


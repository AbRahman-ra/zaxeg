/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.crypto.SecretKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.content.DEREncodedKeyValue;
import org.apache.xml.security.keys.content.KeyInfoReference;
import org.apache.xml.security.keys.content.KeyName;
import org.apache.xml.security.keys.content.KeyValue;
import org.apache.xml.security.keys.content.MgmtData;
import org.apache.xml.security.keys.content.PGPData;
import org.apache.xml.security.keys.content.RetrievalMethod;
import org.apache.xml.security.keys.content.SPKIData;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.keys.content.keyvalues.DSAKeyValue;
import org.apache.xml.security.keys.content.keyvalues.RSAKeyValue;
import org.apache.xml.security.keys.keyresolver.KeyResolver;
import org.apache.xml.security.keys.keyresolver.KeyResolverException;
import org.apache.xml.security.keys.keyresolver.KeyResolverSpi;
import org.apache.xml.security.keys.storage.StorageResolver;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.SignatureElementProxy;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KeyInfo
extends SignatureElementProxy {
    private static Log log = LogFactory.getLog(KeyInfo.class);
    private List<X509Data> x509Datas = null;
    private List<EncryptedKey> encryptedKeys = null;
    private static final List<StorageResolver> nullList;
    private List<StorageResolver> storageResolvers = nullList;
    private List<KeyResolverSpi> internalKeyResolvers = new ArrayList<KeyResolverSpi>();
    private boolean secureValidation;

    public KeyInfo(Document doc) {
        super(doc);
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public KeyInfo(Element element, String baseURI) throws XMLSecurityException {
        super(element, baseURI);
        Attr attr = element.getAttributeNodeNS(null, "Id");
        if (attr != null) {
            element.setIdAttributeNode(attr, true);
        }
    }

    public void setSecureValidation(boolean secureValidation) {
        this.secureValidation = secureValidation;
    }

    public void setId(String id) {
        if (id != null) {
            this.constructionElement.setAttributeNS(null, "Id", id);
            this.constructionElement.setIdAttributeNS(null, "Id", true);
        }
    }

    public String getId() {
        return this.constructionElement.getAttributeNS(null, "Id");
    }

    public void addKeyName(String keynameString) {
        this.add(new KeyName(this.doc, keynameString));
    }

    public void add(KeyName keyname) {
        this.constructionElement.appendChild(keyname.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void addKeyValue(PublicKey pk) {
        this.add(new KeyValue(this.doc, pk));
    }

    public void addKeyValue(Element unknownKeyValueElement) {
        this.add(new KeyValue(this.doc, unknownKeyValueElement));
    }

    public void add(DSAKeyValue dsakeyvalue) {
        this.add(new KeyValue(this.doc, dsakeyvalue));
    }

    public void add(RSAKeyValue rsakeyvalue) {
        this.add(new KeyValue(this.doc, rsakeyvalue));
    }

    public void add(PublicKey pk) {
        this.add(new KeyValue(this.doc, pk));
    }

    public void add(KeyValue keyvalue) {
        this.constructionElement.appendChild(keyvalue.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void addMgmtData(String mgmtdata) {
        this.add(new MgmtData(this.doc, mgmtdata));
    }

    public void add(MgmtData mgmtdata) {
        this.constructionElement.appendChild(mgmtdata.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void add(PGPData pgpdata) {
        this.constructionElement.appendChild(pgpdata.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void addRetrievalMethod(String uri, Transforms transforms, String Type2) {
        this.add(new RetrievalMethod(this.doc, uri, transforms, Type2));
    }

    public void add(RetrievalMethod retrievalmethod) {
        this.constructionElement.appendChild(retrievalmethod.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void add(SPKIData spkidata) {
        this.constructionElement.appendChild(spkidata.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void add(X509Data x509data) {
        if (this.x509Datas == null) {
            this.x509Datas = new ArrayList<X509Data>();
        }
        this.x509Datas.add(x509data);
        this.constructionElement.appendChild(x509data.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void add(EncryptedKey encryptedKey) throws XMLEncryptionException {
        if (this.encryptedKeys == null) {
            this.encryptedKeys = new ArrayList<EncryptedKey>();
        }
        this.encryptedKeys.add(encryptedKey);
        XMLCipher cipher = XMLCipher.getInstance();
        this.constructionElement.appendChild(cipher.martial(encryptedKey));
    }

    public void addDEREncodedKeyValue(PublicKey pk) throws XMLSecurityException {
        this.add(new DEREncodedKeyValue(this.doc, pk));
    }

    public void add(DEREncodedKeyValue derEncodedKeyValue) {
        this.constructionElement.appendChild(derEncodedKeyValue.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void addKeyInfoReference(String URI2) throws XMLSecurityException {
        this.add(new KeyInfoReference(this.doc, URI2));
    }

    public void add(KeyInfoReference keyInfoReference) {
        this.constructionElement.appendChild(keyInfoReference.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void addUnknownElement(Element element) {
        this.constructionElement.appendChild(element);
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public int lengthKeyName() {
        return this.length("http://www.w3.org/2000/09/xmldsig#", "KeyName");
    }

    public int lengthKeyValue() {
        return this.length("http://www.w3.org/2000/09/xmldsig#", "KeyValue");
    }

    public int lengthMgmtData() {
        return this.length("http://www.w3.org/2000/09/xmldsig#", "MgmtData");
    }

    public int lengthPGPData() {
        return this.length("http://www.w3.org/2000/09/xmldsig#", "PGPData");
    }

    public int lengthRetrievalMethod() {
        return this.length("http://www.w3.org/2000/09/xmldsig#", "RetrievalMethod");
    }

    public int lengthSPKIData() {
        return this.length("http://www.w3.org/2000/09/xmldsig#", "SPKIData");
    }

    public int lengthX509Data() {
        if (this.x509Datas != null) {
            return this.x509Datas.size();
        }
        return this.length("http://www.w3.org/2000/09/xmldsig#", "X509Data");
    }

    public int lengthDEREncodedKeyValue() {
        return this.length("http://www.w3.org/2009/xmldsig11#", "DEREncodedKeyValue");
    }

    public int lengthKeyInfoReference() {
        return this.length("http://www.w3.org/2009/xmldsig11#", "KeyInfoReference");
    }

    public int lengthUnknownElement() {
        int res = 0;
        for (Node childNode = this.constructionElement.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
            if (childNode.getNodeType() != 1 || !childNode.getNamespaceURI().equals("http://www.w3.org/2000/09/xmldsig#")) continue;
            ++res;
        }
        return res;
    }

    public KeyName itemKeyName(int i) throws XMLSecurityException {
        Element e = XMLUtils.selectDsNode(this.constructionElement.getFirstChild(), "KeyName", i);
        if (e != null) {
            return new KeyName(e, this.baseURI);
        }
        return null;
    }

    public KeyValue itemKeyValue(int i) throws XMLSecurityException {
        Element e = XMLUtils.selectDsNode(this.constructionElement.getFirstChild(), "KeyValue", i);
        if (e != null) {
            return new KeyValue(e, this.baseURI);
        }
        return null;
    }

    public MgmtData itemMgmtData(int i) throws XMLSecurityException {
        Element e = XMLUtils.selectDsNode(this.constructionElement.getFirstChild(), "MgmtData", i);
        if (e != null) {
            return new MgmtData(e, this.baseURI);
        }
        return null;
    }

    public PGPData itemPGPData(int i) throws XMLSecurityException {
        Element e = XMLUtils.selectDsNode(this.constructionElement.getFirstChild(), "PGPData", i);
        if (e != null) {
            return new PGPData(e, this.baseURI);
        }
        return null;
    }

    public RetrievalMethod itemRetrievalMethod(int i) throws XMLSecurityException {
        Element e = XMLUtils.selectDsNode(this.constructionElement.getFirstChild(), "RetrievalMethod", i);
        if (e != null) {
            return new RetrievalMethod(e, this.baseURI);
        }
        return null;
    }

    public SPKIData itemSPKIData(int i) throws XMLSecurityException {
        Element e = XMLUtils.selectDsNode(this.constructionElement.getFirstChild(), "SPKIData", i);
        if (e != null) {
            return new SPKIData(e, this.baseURI);
        }
        return null;
    }

    public X509Data itemX509Data(int i) throws XMLSecurityException {
        if (this.x509Datas != null) {
            return this.x509Datas.get(i);
        }
        Element e = XMLUtils.selectDsNode(this.constructionElement.getFirstChild(), "X509Data", i);
        if (e != null) {
            return new X509Data(e, this.baseURI);
        }
        return null;
    }

    public EncryptedKey itemEncryptedKey(int i) throws XMLSecurityException {
        if (this.encryptedKeys != null) {
            return this.encryptedKeys.get(i);
        }
        Element e = XMLUtils.selectXencNode(this.constructionElement.getFirstChild(), "EncryptedKey", i);
        if (e != null) {
            XMLCipher cipher = XMLCipher.getInstance();
            cipher.init(4, null);
            return cipher.loadEncryptedKey(e);
        }
        return null;
    }

    public DEREncodedKeyValue itemDEREncodedKeyValue(int i) throws XMLSecurityException {
        Element e = XMLUtils.selectDs11Node(this.constructionElement.getFirstChild(), "DEREncodedKeyValue", i);
        if (e != null) {
            return new DEREncodedKeyValue(e, this.baseURI);
        }
        return null;
    }

    public KeyInfoReference itemKeyInfoReference(int i) throws XMLSecurityException {
        Element e = XMLUtils.selectDs11Node(this.constructionElement.getFirstChild(), "KeyInfoReference", i);
        if (e != null) {
            return new KeyInfoReference(e, this.baseURI);
        }
        return null;
    }

    public Element itemUnknownElement(int i) {
        int res = 0;
        for (Node childNode = this.constructionElement.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
            if (childNode.getNodeType() != 1 || !childNode.getNamespaceURI().equals("http://www.w3.org/2000/09/xmldsig#") || ++res != i) continue;
            return (Element)childNode;
        }
        return null;
    }

    public boolean isEmpty() {
        return this.constructionElement.getFirstChild() == null;
    }

    public boolean containsKeyName() {
        return this.lengthKeyName() > 0;
    }

    public boolean containsKeyValue() {
        return this.lengthKeyValue() > 0;
    }

    public boolean containsMgmtData() {
        return this.lengthMgmtData() > 0;
    }

    public boolean containsPGPData() {
        return this.lengthPGPData() > 0;
    }

    public boolean containsRetrievalMethod() {
        return this.lengthRetrievalMethod() > 0;
    }

    public boolean containsSPKIData() {
        return this.lengthSPKIData() > 0;
    }

    public boolean containsUnknownElement() {
        return this.lengthUnknownElement() > 0;
    }

    public boolean containsX509Data() {
        return this.lengthX509Data() > 0;
    }

    public boolean containsDEREncodedKeyValue() {
        return this.lengthDEREncodedKeyValue() > 0;
    }

    public boolean containsKeyInfoReference() {
        return this.lengthKeyInfoReference() > 0;
    }

    public PublicKey getPublicKey() throws KeyResolverException {
        PublicKey pk = this.getPublicKeyFromInternalResolvers();
        if (pk != null) {
            if (log.isDebugEnabled()) {
                log.debug("I could find a key using the per-KeyInfo key resolvers");
            }
            return pk;
        }
        if (log.isDebugEnabled()) {
            log.debug("I couldn't find a key using the per-KeyInfo key resolvers");
        }
        if ((pk = this.getPublicKeyFromStaticResolvers()) != null) {
            if (log.isDebugEnabled()) {
                log.debug("I could find a key using the system-wide key resolvers");
            }
            return pk;
        }
        if (log.isDebugEnabled()) {
            log.debug("I couldn't find a key using the system-wide key resolvers");
        }
        return null;
    }

    PublicKey getPublicKeyFromStaticResolvers() throws KeyResolverException {
        Iterator<KeyResolverSpi> it = KeyResolver.iterator();
        while (it.hasNext()) {
            KeyResolverSpi keyResolver = it.next();
            keyResolver.setSecureValidation(this.secureValidation);
            String uri = this.getBaseURI();
            for (Node currentChild = this.constructionElement.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
                if (currentChild.getNodeType() != 1) continue;
                for (StorageResolver storage : this.storageResolvers) {
                    PublicKey pk = keyResolver.engineLookupAndResolvePublicKey((Element)currentChild, uri, storage);
                    if (pk == null) continue;
                    return pk;
                }
            }
        }
        return null;
    }

    PublicKey getPublicKeyFromInternalResolvers() throws KeyResolverException {
        for (KeyResolverSpi keyResolver : this.internalKeyResolvers) {
            if (log.isDebugEnabled()) {
                log.debug("Try " + keyResolver.getClass().getName());
            }
            keyResolver.setSecureValidation(this.secureValidation);
            String uri = this.getBaseURI();
            for (Node currentChild = this.constructionElement.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
                if (currentChild.getNodeType() != 1) continue;
                for (StorageResolver storage : this.storageResolvers) {
                    PublicKey pk = keyResolver.engineLookupAndResolvePublicKey((Element)currentChild, uri, storage);
                    if (pk == null) continue;
                    return pk;
                }
            }
        }
        return null;
    }

    public X509Certificate getX509Certificate() throws KeyResolverException {
        X509Certificate cert = this.getX509CertificateFromInternalResolvers();
        if (cert != null) {
            if (log.isDebugEnabled()) {
                log.debug("I could find a X509Certificate using the per-KeyInfo key resolvers");
            }
            return cert;
        }
        if (log.isDebugEnabled()) {
            log.debug("I couldn't find a X509Certificate using the per-KeyInfo key resolvers");
        }
        if ((cert = this.getX509CertificateFromStaticResolvers()) != null) {
            if (log.isDebugEnabled()) {
                log.debug("I could find a X509Certificate using the system-wide key resolvers");
            }
            return cert;
        }
        if (log.isDebugEnabled()) {
            log.debug("I couldn't find a X509Certificate using the system-wide key resolvers");
        }
        return null;
    }

    X509Certificate getX509CertificateFromStaticResolvers() throws KeyResolverException {
        if (log.isDebugEnabled()) {
            log.debug("Start getX509CertificateFromStaticResolvers() with " + KeyResolver.length() + " resolvers");
        }
        String uri = this.getBaseURI();
        Iterator<KeyResolverSpi> it = KeyResolver.iterator();
        while (it.hasNext()) {
            KeyResolverSpi keyResolver = it.next();
            keyResolver.setSecureValidation(this.secureValidation);
            X509Certificate cert = this.applyCurrentResolver(uri, keyResolver);
            if (cert == null) continue;
            return cert;
        }
        return null;
    }

    private X509Certificate applyCurrentResolver(String uri, KeyResolverSpi keyResolver) throws KeyResolverException {
        for (Node currentChild = this.constructionElement.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
            if (currentChild.getNodeType() != 1) continue;
            for (StorageResolver storage : this.storageResolvers) {
                X509Certificate cert = keyResolver.engineLookupResolveX509Certificate((Element)currentChild, uri, storage);
                if (cert == null) continue;
                return cert;
            }
        }
        return null;
    }

    X509Certificate getX509CertificateFromInternalResolvers() throws KeyResolverException {
        if (log.isDebugEnabled()) {
            log.debug("Start getX509CertificateFromInternalResolvers() with " + this.lengthInternalKeyResolver() + " resolvers");
        }
        String uri = this.getBaseURI();
        for (KeyResolverSpi keyResolver : this.internalKeyResolvers) {
            if (log.isDebugEnabled()) {
                log.debug("Try " + keyResolver.getClass().getName());
            }
            keyResolver.setSecureValidation(this.secureValidation);
            X509Certificate cert = this.applyCurrentResolver(uri, keyResolver);
            if (cert == null) continue;
            return cert;
        }
        return null;
    }

    public SecretKey getSecretKey() throws KeyResolverException {
        SecretKey sk = this.getSecretKeyFromInternalResolvers();
        if (sk != null) {
            if (log.isDebugEnabled()) {
                log.debug("I could find a secret key using the per-KeyInfo key resolvers");
            }
            return sk;
        }
        if (log.isDebugEnabled()) {
            log.debug("I couldn't find a secret key using the per-KeyInfo key resolvers");
        }
        if ((sk = this.getSecretKeyFromStaticResolvers()) != null) {
            if (log.isDebugEnabled()) {
                log.debug("I could find a secret key using the system-wide key resolvers");
            }
            return sk;
        }
        if (log.isDebugEnabled()) {
            log.debug("I couldn't find a secret key using the system-wide key resolvers");
        }
        return null;
    }

    SecretKey getSecretKeyFromStaticResolvers() throws KeyResolverException {
        Iterator<KeyResolverSpi> it = KeyResolver.iterator();
        while (it.hasNext()) {
            KeyResolverSpi keyResolver = it.next();
            keyResolver.setSecureValidation(this.secureValidation);
            String uri = this.getBaseURI();
            for (Node currentChild = this.constructionElement.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
                if (currentChild.getNodeType() != 1) continue;
                for (StorageResolver storage : this.storageResolvers) {
                    SecretKey sk = keyResolver.engineLookupAndResolveSecretKey((Element)currentChild, uri, storage);
                    if (sk == null) continue;
                    return sk;
                }
            }
        }
        return null;
    }

    SecretKey getSecretKeyFromInternalResolvers() throws KeyResolverException {
        for (KeyResolverSpi keyResolver : this.internalKeyResolvers) {
            if (log.isDebugEnabled()) {
                log.debug("Try " + keyResolver.getClass().getName());
            }
            keyResolver.setSecureValidation(this.secureValidation);
            String uri = this.getBaseURI();
            for (Node currentChild = this.constructionElement.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
                if (currentChild.getNodeType() != 1) continue;
                for (StorageResolver storage : this.storageResolvers) {
                    SecretKey sk = keyResolver.engineLookupAndResolveSecretKey((Element)currentChild, uri, storage);
                    if (sk == null) continue;
                    return sk;
                }
            }
        }
        return null;
    }

    public PrivateKey getPrivateKey() throws KeyResolverException {
        PrivateKey pk = this.getPrivateKeyFromInternalResolvers();
        if (pk != null) {
            if (log.isDebugEnabled()) {
                log.debug("I could find a private key using the per-KeyInfo key resolvers");
            }
            return pk;
        }
        if (log.isDebugEnabled()) {
            log.debug("I couldn't find a secret key using the per-KeyInfo key resolvers");
        }
        if ((pk = this.getPrivateKeyFromStaticResolvers()) != null) {
            if (log.isDebugEnabled()) {
                log.debug("I could find a private key using the system-wide key resolvers");
            }
            return pk;
        }
        if (log.isDebugEnabled()) {
            log.debug("I couldn't find a private key using the system-wide key resolvers");
        }
        return null;
    }

    PrivateKey getPrivateKeyFromStaticResolvers() throws KeyResolverException {
        Iterator<KeyResolverSpi> it = KeyResolver.iterator();
        while (it.hasNext()) {
            KeyResolverSpi keyResolver = it.next();
            keyResolver.setSecureValidation(this.secureValidation);
            String uri = this.getBaseURI();
            for (Node currentChild = this.constructionElement.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
                PrivateKey pk;
                if (currentChild.getNodeType() != 1 || (pk = keyResolver.engineLookupAndResolvePrivateKey((Element)currentChild, uri, null)) == null) continue;
                return pk;
            }
        }
        return null;
    }

    PrivateKey getPrivateKeyFromInternalResolvers() throws KeyResolverException {
        for (KeyResolverSpi keyResolver : this.internalKeyResolvers) {
            if (log.isDebugEnabled()) {
                log.debug("Try " + keyResolver.getClass().getName());
            }
            keyResolver.setSecureValidation(this.secureValidation);
            String uri = this.getBaseURI();
            for (Node currentChild = this.constructionElement.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
                PrivateKey pk;
                if (currentChild.getNodeType() != 1 || (pk = keyResolver.engineLookupAndResolvePrivateKey((Element)currentChild, uri, null)) == null) continue;
                return pk;
            }
        }
        return null;
    }

    public void registerInternalKeyResolver(KeyResolverSpi realKeyResolver) {
        this.internalKeyResolvers.add(realKeyResolver);
    }

    int lengthInternalKeyResolver() {
        return this.internalKeyResolvers.size();
    }

    KeyResolverSpi itemInternalKeyResolver(int i) {
        return this.internalKeyResolvers.get(i);
    }

    public void addStorageResolver(StorageResolver storageResolver) {
        if (this.storageResolvers == nullList) {
            this.storageResolvers = new ArrayList<StorageResolver>();
        }
        this.storageResolvers.add(storageResolver);
    }

    public String getBaseLocalName() {
        return "KeyInfo";
    }

    static {
        ArrayList<Object> list = new ArrayList<Object>(1);
        list.add(null);
        nullList = Collections.unmodifiableList(list);
    }
}


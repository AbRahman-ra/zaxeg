/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.encryption;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.encryption.CipherData;
import org.apache.xml.security.encryption.CipherReference;
import org.apache.xml.security.encryption.EncryptedType;
import org.apache.xml.security.encryption.Transforms;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.utils.Base64;
import org.apache.xml.security.utils.resolver.ResourceResolver;
import org.apache.xml.security.utils.resolver.ResourceResolverException;
import org.w3c.dom.Attr;

public class XMLCipherInput {
    private static Log logger = LogFactory.getLog(XMLCipherInput.class);
    private CipherData cipherData;
    private int mode;
    private boolean secureValidation;

    public XMLCipherInput(CipherData data) throws XMLEncryptionException {
        this.cipherData = data;
        this.mode = 2;
        if (this.cipherData == null) {
            throw new XMLEncryptionException("CipherData is null");
        }
    }

    public XMLCipherInput(EncryptedType input) throws XMLEncryptionException {
        this.cipherData = input == null ? null : input.getCipherData();
        this.mode = 2;
        if (this.cipherData == null) {
            throw new XMLEncryptionException("CipherData is null");
        }
    }

    public void setSecureValidation(boolean secureValidation) {
        this.secureValidation = secureValidation;
    }

    public byte[] getBytes() throws XMLEncryptionException {
        if (this.mode == 2) {
            return this.getDecryptBytes();
        }
        return null;
    }

    private byte[] getDecryptBytes() throws XMLEncryptionException {
        String base64EncodedEncryptedOctets = null;
        if (this.cipherData.getDataType() == 2) {
            Transforms transforms;
            if (logger.isDebugEnabled()) {
                logger.debug("Found a reference type CipherData");
            }
            CipherReference cr = this.cipherData.getCipherReference();
            Attr uriAttr = cr.getURIAsAttr();
            XMLSignatureInput input = null;
            try {
                ResourceResolver resolver = ResourceResolver.getInstance(uriAttr, null, this.secureValidation);
                input = resolver.resolve(uriAttr, null, this.secureValidation);
            } catch (ResourceResolverException ex) {
                throw new XMLEncryptionException("empty", ex);
            }
            if (input != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Managed to resolve URI \"" + cr.getURI() + "\"");
                }
            } else if (logger.isDebugEnabled()) {
                logger.debug("Failed to resolve URI \"" + cr.getURI() + "\"");
            }
            if ((transforms = cr.getTransforms()) != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Have transforms in cipher reference");
                }
                try {
                    org.apache.xml.security.transforms.Transforms dsTransforms = transforms.getDSTransforms();
                    dsTransforms.setSecureValidation(this.secureValidation);
                    input = dsTransforms.performTransforms(input);
                } catch (TransformationException ex) {
                    throw new XMLEncryptionException("empty", ex);
                }
            }
            try {
                return input.getBytes();
            } catch (IOException ex) {
                throw new XMLEncryptionException("empty", ex);
            } catch (CanonicalizationException ex) {
                throw new XMLEncryptionException("empty", ex);
            }
        }
        if (this.cipherData.getDataType() != 1) {
            throw new XMLEncryptionException("CipherData.getDataType() returned unexpected value");
        }
        base64EncodedEncryptedOctets = this.cipherData.getCipherValue().getValue();
        if (logger.isDebugEnabled()) {
            logger.debug("Encrypted octets:\n" + base64EncodedEncryptedOctets);
        }
        try {
            return Base64.decode(base64EncodedEncryptedOctets);
        } catch (Base64DecodingException bde) {
            throw new XMLEncryptionException("empty", bde);
        }
    }
}


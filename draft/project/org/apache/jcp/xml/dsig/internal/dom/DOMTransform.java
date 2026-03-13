/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.spec.AlgorithmParameterSpec;
import javax.xml.crypto.Data;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.TransformService;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import org.apache.jcp.xml.dsig.internal.dom.DOMStructure;
import org.apache.jcp.xml.dsig.internal.dom.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DOMTransform
extends DOMStructure
implements Transform {
    protected TransformService spi;

    public DOMTransform(TransformService spi) {
        this.spi = spi;
    }

    public DOMTransform(Element transElem, XMLCryptoContext context, Provider provider) throws MarshalException {
        String algorithm = DOMUtils.getAttributeValue(transElem, "Algorithm");
        if (provider == null) {
            try {
                this.spi = TransformService.getInstance(algorithm, "DOM");
            } catch (NoSuchAlgorithmException e1) {
                throw new MarshalException(e1);
            }
        }
        try {
            this.spi = TransformService.getInstance(algorithm, "DOM", provider);
        } catch (NoSuchAlgorithmException nsae) {
            try {
                this.spi = TransformService.getInstance(algorithm, "DOM");
            } catch (NoSuchAlgorithmException e2) {
                throw new MarshalException(e2);
            }
        }
        try {
            this.spi.init(new javax.xml.crypto.dom.DOMStructure(transElem), context);
        } catch (InvalidAlgorithmParameterException iape) {
            throw new MarshalException(iape);
        }
    }

    public final AlgorithmParameterSpec getParameterSpec() {
        return this.spi.getParameterSpec();
    }

    public final String getAlgorithm() {
        return this.spi.getAlgorithm();
    }

    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);
        Element transformElem = null;
        transformElem = parent.getLocalName().equals("Transforms") ? DOMUtils.createElement(ownerDoc, "Transform", "http://www.w3.org/2000/09/xmldsig#", dsPrefix) : DOMUtils.createElement(ownerDoc, "CanonicalizationMethod", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
        DOMUtils.setAttribute(transformElem, "Algorithm", this.getAlgorithm());
        this.spi.marshalParams(new javax.xml.crypto.dom.DOMStructure(transformElem), context);
        parent.appendChild(transformElem);
    }

    public Data transform(Data data, XMLCryptoContext xc) throws TransformException {
        return this.spi.transform(data, xc);
    }

    public Data transform(Data data, XMLCryptoContext xc, OutputStream os) throws TransformException {
        return this.spi.transform(data, xc, os);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Transform)) {
            return false;
        }
        Transform otransform = (Transform)o;
        return this.getAlgorithm().equals(otransform.getAlgorithm()) && DOMUtils.paramsEqual(this.getParameterSpec(), otransform.getParameterSpec());
    }

    public int hashCode() {
        int result = 17;
        result = 31 * result + this.getAlgorithm().hashCode();
        AlgorithmParameterSpec spec = this.getParameterSpec();
        if (spec != null) {
            result = 31 * result + spec.hashCode();
        }
        return result;
    }

    Data transform(Data data, XMLCryptoContext xc, DOMSignContext context) throws MarshalException, TransformException {
        this.marshal(context.getParent(), DOMUtils.getSignaturePrefix(context), context);
        return this.transform(data, xc);
    }
}


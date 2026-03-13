/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import java.security.InvalidAlgorithmParameterException;
import javax.xml.crypto.Data;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import org.apache.jcp.xml.dsig.internal.dom.ApacheCanonicalizer;
import org.apache.jcp.xml.dsig.internal.dom.DOMSubTreeData;
import org.apache.jcp.xml.dsig.internal.dom.Utils;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;

public final class DOMCanonicalXMLC14N11Method
extends ApacheCanonicalizer {
    public static final String C14N_11 = "http://www.w3.org/2006/12/xml-c14n11";
    public static final String C14N_11_WITH_COMMENTS = "http://www.w3.org/2006/12/xml-c14n11#WithComments";

    public void init(TransformParameterSpec params) throws InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException("no parameters should be specified for Canonical XML 1.1 algorithm");
        }
    }

    public Data transform(Data data, XMLCryptoContext xc) throws TransformException {
        DOMSubTreeData subTree;
        if (data instanceof DOMSubTreeData && (subTree = (DOMSubTreeData)data).excludeComments()) {
            try {
                this.apacheCanonicalizer = Canonicalizer.getInstance(C14N_11);
                boolean secVal = Utils.secureValidation(xc);
                this.apacheCanonicalizer.setSecureValidation(secVal);
            } catch (InvalidCanonicalizerException ice) {
                throw new TransformException("Couldn't find Canonicalizer for: http://www.w3.org/2006/12/xml-c14n11: " + ice.getMessage(), ice);
            }
        }
        return this.canonicalize(data, xc);
    }
}


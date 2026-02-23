/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Set;
import javax.xml.crypto.Data;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.NodeSetData;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.TransformService;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcp.xml.dsig.internal.dom.ApacheData;
import org.apache.jcp.xml.dsig.internal.dom.ApacheNodeSetData;
import org.apache.jcp.xml.dsig.internal.dom.ApacheOctetStreamData;
import org.apache.jcp.xml.dsig.internal.dom.DOMSubTreeData;
import org.apache.jcp.xml.dsig.internal.dom.DOMUtils;
import org.apache.jcp.xml.dsig.internal.dom.Utils;
import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.transforms.Transform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class ApacheCanonicalizer
extends TransformService {
    private static Log log;
    protected Canonicalizer apacheCanonicalizer;
    private Transform apacheTransform;
    protected String inclusiveNamespaces;
    protected C14NMethodParameterSpec params;
    protected Document ownerDoc;
    protected Element transformElem;

    public final AlgorithmParameterSpec getParameterSpec() {
        return this.params;
    }

    public void init(XMLStructure parent, XMLCryptoContext context) throws InvalidAlgorithmParameterException {
        if (context != null && !(context instanceof DOMCryptoContext)) {
            throw new ClassCastException("context must be of type DOMCryptoContext");
        }
        if (parent == null) {
            throw new NullPointerException();
        }
        if (!(parent instanceof DOMStructure)) {
            throw new ClassCastException("parent must be of type DOMStructure");
        }
        this.transformElem = (Element)((DOMStructure)parent).getNode();
        this.ownerDoc = DOMUtils.getOwnerDocument(this.transformElem);
    }

    public void marshalParams(XMLStructure parent, XMLCryptoContext context) throws MarshalException {
        if (context != null && !(context instanceof DOMCryptoContext)) {
            throw new ClassCastException("context must be of type DOMCryptoContext");
        }
        if (parent == null) {
            throw new NullPointerException();
        }
        if (!(parent instanceof DOMStructure)) {
            throw new ClassCastException("parent must be of type DOMStructure");
        }
        this.transformElem = (Element)((DOMStructure)parent).getNode();
        this.ownerDoc = DOMUtils.getOwnerDocument(this.transformElem);
    }

    public Data canonicalize(Data data, XMLCryptoContext xc) throws TransformException {
        return this.canonicalize(data, xc, null);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public Data canonicalize(Data data, XMLCryptoContext xc, OutputStream os) throws TransformException {
        if (this.apacheCanonicalizer == null) {
            try {
                this.apacheCanonicalizer = Canonicalizer.getInstance(this.getAlgorithm());
                boolean secVal = Utils.secureValidation(xc);
                this.apacheCanonicalizer.setSecureValidation(secVal);
                if (log.isDebugEnabled()) {
                    log.debug("Created canonicalizer for algorithm: " + this.getAlgorithm());
                }
            } catch (InvalidCanonicalizerException ice) {
                throw new TransformException("Couldn't find Canonicalizer for: " + this.getAlgorithm() + ": " + ice.getMessage(), ice);
            }
        }
        if (os != null) {
            this.apacheCanonicalizer.setWriter(os);
        } else {
            this.apacheCanonicalizer.setWriter(new ByteArrayOutputStream());
        }
        try {
            Set<Node> nodeSet = null;
            if (data instanceof ApacheData) {
                XMLSignatureInput in = ((ApacheData)data).getXMLSignatureInput();
                if (in.isElement()) {
                    if (this.inclusiveNamespaces == null) return new OctetStreamData(new ByteArrayInputStream(this.apacheCanonicalizer.canonicalizeSubtree(in.getSubNode())));
                    return new OctetStreamData(new ByteArrayInputStream(this.apacheCanonicalizer.canonicalizeSubtree(in.getSubNode(), this.inclusiveNamespaces)));
                }
                if (!in.isNodeSet()) return new OctetStreamData(new ByteArrayInputStream(this.apacheCanonicalizer.canonicalize(Utils.readBytesFromStream(in.getOctetStream()))));
                nodeSet = in.getNodeSet();
            } else {
                if (data instanceof DOMSubTreeData) {
                    DOMSubTreeData subTree = (DOMSubTreeData)data;
                    if (this.inclusiveNamespaces == null) return new OctetStreamData(new ByteArrayInputStream(this.apacheCanonicalizer.canonicalizeSubtree(subTree.getRoot())));
                    return new OctetStreamData(new ByteArrayInputStream(this.apacheCanonicalizer.canonicalizeSubtree(subTree.getRoot(), this.inclusiveNamespaces)));
                }
                if (!(data instanceof NodeSetData)) return new OctetStreamData(new ByteArrayInputStream(this.apacheCanonicalizer.canonicalize(Utils.readBytesFromStream(((OctetStreamData)data).getOctetStream()))));
                NodeSetData nsd = (NodeSetData)data;
                Set<Node> ns = Utils.toNodeSet(nsd.iterator());
                nodeSet = ns;
                if (log.isDebugEnabled()) {
                    log.debug("Canonicalizing " + nodeSet.size() + " nodes");
                }
            }
            if (this.inclusiveNamespaces == null) return new OctetStreamData(new ByteArrayInputStream(this.apacheCanonicalizer.canonicalizeXPathNodeSet(nodeSet)));
            return new OctetStreamData(new ByteArrayInputStream(this.apacheCanonicalizer.canonicalizeXPathNodeSet(nodeSet, this.inclusiveNamespaces)));
        } catch (Exception e) {
            throw new TransformException(e);
        }
    }

    public Data transform(Data data, XMLCryptoContext xc, OutputStream os) throws TransformException {
        XMLSignatureInput in;
        if (data == null) {
            throw new NullPointerException("data must not be null");
        }
        if (os == null) {
            throw new NullPointerException("output stream must not be null");
        }
        if (this.ownerDoc == null) {
            throw new TransformException("transform must be marshalled");
        }
        if (this.apacheTransform == null) {
            try {
                this.apacheTransform = new Transform(this.ownerDoc, this.getAlgorithm(), this.transformElem.getChildNodes());
                this.apacheTransform.setElement(this.transformElem, xc.getBaseURI());
                boolean secVal = Utils.secureValidation(xc);
                this.apacheTransform.setSecureValidation(secVal);
                if (log.isDebugEnabled()) {
                    log.debug("Created transform for algorithm: " + this.getAlgorithm());
                }
            } catch (Exception ex) {
                throw new TransformException("Couldn't find Transform for: " + this.getAlgorithm(), ex);
            }
        }
        if (data instanceof ApacheData) {
            if (log.isDebugEnabled()) {
                log.debug("ApacheData = true");
            }
            in = ((ApacheData)data).getXMLSignatureInput();
        } else if (data instanceof NodeSetData) {
            if (log.isDebugEnabled()) {
                log.debug("isNodeSet() = true");
            }
            if (data instanceof DOMSubTreeData) {
                DOMSubTreeData subTree = (DOMSubTreeData)data;
                in = new XMLSignatureInput(subTree.getRoot());
                in.setExcludeComments(subTree.excludeComments());
            } else {
                Set<Node> nodeSet = Utils.toNodeSet(((NodeSetData)data).iterator());
                in = new XMLSignatureInput(nodeSet);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("isNodeSet() = false");
            }
            try {
                in = new XMLSignatureInput(((OctetStreamData)data).getOctetStream());
            } catch (Exception ex) {
                throw new TransformException(ex);
            }
        }
        boolean secVal = Utils.secureValidation(xc);
        in.setSecureValidation(secVal);
        try {
            in = this.apacheTransform.performTransform(in, os);
            if (!in.isNodeSet() && !in.isElement()) {
                return null;
            }
            if (in.isOctetStream()) {
                return new ApacheOctetStreamData(in);
            }
            return new ApacheNodeSetData(in);
        } catch (Exception ex) {
            throw new TransformException(ex);
        }
    }

    public final boolean isFeatureSupported(String feature) {
        if (feature == null) {
            throw new NullPointerException();
        }
        return false;
    }

    static {
        Init.init();
        log = LogFactory.getLog(ApacheCanonicalizer.class);
    }
}


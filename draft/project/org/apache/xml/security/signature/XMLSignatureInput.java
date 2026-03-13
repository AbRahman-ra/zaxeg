/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.signature;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.implementations.Canonicalizer11_OmitComments;
import org.apache.xml.security.c14n.implementations.Canonicalizer20010315OmitComments;
import org.apache.xml.security.c14n.implementations.CanonicalizerBase;
import org.apache.xml.security.exceptions.XMLSecurityRuntimeException;
import org.apache.xml.security.signature.NodeFilter;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.signature.XMLSignatureInputDebugger;
import org.apache.xml.security.utils.IgnoreAllErrorHandler;
import org.apache.xml.security.utils.JavaUtils;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class XMLSignatureInput {
    private InputStream inputOctetStreamProxy = null;
    private Set<Node> inputNodeSet = null;
    private Node subNode = null;
    private Node excludeNode = null;
    private boolean excludeComments = false;
    private boolean isNodeSet = false;
    private byte[] bytes = null;
    private boolean secureValidation;
    private String mimeType = null;
    private String sourceURI = null;
    private List<NodeFilter> nodeFilters = new ArrayList<NodeFilter>();
    private boolean needsToBeExpanded = false;
    private OutputStream outputStream = null;

    public XMLSignatureInput(byte[] inputOctets) {
        this.bytes = inputOctets;
    }

    public XMLSignatureInput(InputStream inputOctetStream) {
        this.inputOctetStreamProxy = inputOctetStream;
    }

    public XMLSignatureInput(Node rootNode) {
        this.subNode = rootNode;
    }

    public XMLSignatureInput(Set<Node> inputNodeSet) {
        this.inputNodeSet = inputNodeSet;
    }

    public boolean isNeedsToBeExpanded() {
        return this.needsToBeExpanded;
    }

    public void setNeedsToBeExpanded(boolean needsToBeExpanded) {
        this.needsToBeExpanded = needsToBeExpanded;
    }

    public Set<Node> getNodeSet() throws CanonicalizationException, ParserConfigurationException, IOException, SAXException {
        return this.getNodeSet(false);
    }

    public Set<Node> getInputNodeSet() {
        return this.inputNodeSet;
    }

    public Set<Node> getNodeSet(boolean circumvent) throws ParserConfigurationException, IOException, SAXException, CanonicalizationException {
        if (this.inputNodeSet != null) {
            return this.inputNodeSet;
        }
        if (this.inputOctetStreamProxy == null && this.subNode != null) {
            if (circumvent) {
                XMLUtils.circumventBug2650(XMLUtils.getOwnerDocument(this.subNode));
            }
            this.inputNodeSet = new LinkedHashSet<Node>();
            XMLUtils.getSet(this.subNode, this.inputNodeSet, this.excludeNode, this.excludeComments);
            return this.inputNodeSet;
        }
        if (this.isOctetStream()) {
            this.convertToNodes();
            LinkedHashSet<Node> result = new LinkedHashSet<Node>();
            XMLUtils.getSet(this.subNode, result, null, false);
            return result;
        }
        throw new RuntimeException("getNodeSet() called but no input data present");
    }

    public InputStream getOctetStream() throws IOException {
        if (this.inputOctetStreamProxy != null) {
            return this.inputOctetStreamProxy;
        }
        if (this.bytes != null) {
            this.inputOctetStreamProxy = new ByteArrayInputStream(this.bytes);
            return this.inputOctetStreamProxy;
        }
        return null;
    }

    public InputStream getOctetStreamReal() {
        return this.inputOctetStreamProxy;
    }

    public byte[] getBytes() throws IOException, CanonicalizationException {
        byte[] inputBytes = this.getBytesFromInputStream();
        if (inputBytes != null) {
            return inputBytes;
        }
        Canonicalizer20010315OmitComments c14nizer = new Canonicalizer20010315OmitComments();
        this.bytes = c14nizer.engineCanonicalize(this);
        return this.bytes;
    }

    public boolean isNodeSet() {
        return this.inputOctetStreamProxy == null && this.inputNodeSet != null || this.isNodeSet;
    }

    public boolean isElement() {
        return this.inputOctetStreamProxy == null && this.subNode != null && this.inputNodeSet == null && !this.isNodeSet;
    }

    public boolean isOctetStream() {
        return (this.inputOctetStreamProxy != null || this.bytes != null) && this.inputNodeSet == null && this.subNode == null;
    }

    public boolean isOutputStreamSet() {
        return this.outputStream != null;
    }

    public boolean isByteArray() {
        return this.bytes != null && this.inputNodeSet == null && this.subNode == null;
    }

    public boolean isInitialized() {
        return this.isOctetStream() || this.isNodeSet();
    }

    public String getMIMEType() {
        return this.mimeType;
    }

    public void setMIMEType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getSourceURI() {
        return this.sourceURI;
    }

    public void setSourceURI(String sourceURI) {
        this.sourceURI = sourceURI;
    }

    public String toString() {
        if (this.isNodeSet()) {
            return "XMLSignatureInput/NodeSet/" + this.inputNodeSet.size() + " nodes/" + this.getSourceURI();
        }
        if (this.isElement()) {
            return "XMLSignatureInput/Element/" + this.subNode + " exclude " + this.excludeNode + " comments:" + this.excludeComments + "/" + this.getSourceURI();
        }
        try {
            return "XMLSignatureInput/OctetStream/" + this.getBytes().length + " octets/" + this.getSourceURI();
        } catch (IOException iex) {
            return "XMLSignatureInput/OctetStream//" + this.getSourceURI();
        } catch (CanonicalizationException cex) {
            return "XMLSignatureInput/OctetStream//" + this.getSourceURI();
        }
    }

    public String getHTMLRepresentation() throws XMLSignatureException {
        XMLSignatureInputDebugger db = new XMLSignatureInputDebugger(this);
        return db.getHTMLRepresentation();
    }

    public String getHTMLRepresentation(Set<String> inclusiveNamespaces) throws XMLSignatureException {
        XMLSignatureInputDebugger db = new XMLSignatureInputDebugger(this, inclusiveNamespaces);
        return db.getHTMLRepresentation();
    }

    public Node getExcludeNode() {
        return this.excludeNode;
    }

    public void setExcludeNode(Node excludeNode) {
        this.excludeNode = excludeNode;
    }

    public Node getSubNode() {
        return this.subNode;
    }

    public boolean isExcludeComments() {
        return this.excludeComments;
    }

    public void setExcludeComments(boolean excludeComments) {
        this.excludeComments = excludeComments;
    }

    public void updateOutputStream(OutputStream diOs) throws CanonicalizationException, IOException {
        this.updateOutputStream(diOs, false);
    }

    public void updateOutputStream(OutputStream diOs, boolean c14n11) throws CanonicalizationException, IOException {
        if (diOs == this.outputStream) {
            return;
        }
        if (this.bytes != null) {
            diOs.write(this.bytes);
        } else if (this.inputOctetStreamProxy == null) {
            CanonicalizerBase c14nizer = null;
            c14nizer = c14n11 ? new Canonicalizer11_OmitComments() : new Canonicalizer20010315OmitComments();
            c14nizer.setWriter(diOs);
            c14nizer.engineCanonicalize(this);
        } else {
            byte[] buffer = new byte[4096];
            int bytesread = 0;
            try {
                while ((bytesread = this.inputOctetStreamProxy.read(buffer)) != -1) {
                    diOs.write(buffer, 0, bytesread);
                }
            } catch (IOException ex) {
                this.inputOctetStreamProxy.close();
                throw ex;
            }
        }
    }

    public void setOutputStream(OutputStream os) {
        this.outputStream = os;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private byte[] getBytesFromInputStream() throws IOException {
        if (this.bytes != null) {
            return this.bytes;
        }
        if (this.inputOctetStreamProxy == null) {
            return null;
        }
        try {
            this.bytes = JavaUtils.getBytesFromStream(this.inputOctetStreamProxy);
        } finally {
            this.inputOctetStreamProxy.close();
        }
        return this.bytes;
    }

    public void addNodeFilter(NodeFilter filter) {
        if (this.isOctetStream()) {
            try {
                this.convertToNodes();
            } catch (Exception e) {
                throw new XMLSecurityRuntimeException("signature.XMLSignatureInput.nodesetReference", e);
            }
        }
        this.nodeFilters.add(filter);
    }

    public List<NodeFilter> getNodeFilters() {
        return this.nodeFilters;
    }

    public void setNodeSet(boolean b) {
        this.isNodeSet = b;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void convertToNodes() throws CanonicalizationException, ParserConfigurationException, IOException, SAXException {
        DocumentBuilder db = XMLUtils.createDocumentBuilder(false, this.secureValidation);
        try {
            db.setErrorHandler(new IgnoreAllErrorHandler());
            Document doc = db.parse(this.getOctetStream());
            this.subNode = doc;
        } catch (SAXException ex) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write("<container>".getBytes("UTF-8"));
            baos.write(this.getBytes());
            baos.write("</container>".getBytes("UTF-8"));
            byte[] result = baos.toByteArray();
            Document document = db.parse(new ByteArrayInputStream(result));
            this.subNode = document.getDocumentElement().getFirstChild().getFirstChild();
        } finally {
            if (this.inputOctetStreamProxy != null) {
                this.inputOctetStreamProxy.close();
            }
            this.inputOctetStreamProxy = null;
            this.bytes = null;
        }
    }

    public boolean isSecureValidation() {
        return this.secureValidation;
    }

    public void setSecureValidation(boolean secureValidation) {
        this.secureValidation = secureValidation;
    }
}


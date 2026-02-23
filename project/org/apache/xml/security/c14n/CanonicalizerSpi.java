/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.c14n;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class CanonicalizerSpi {
    protected boolean reset = false;
    protected boolean secureValidation;

    public byte[] engineCanonicalize(byte[] inputBytes) throws ParserConfigurationException, IOException, SAXException, CanonicalizationException {
        ByteArrayInputStream bais = new ByteArrayInputStream(inputBytes);
        InputSource in = new InputSource(bais);
        DocumentBuilder db = XMLUtils.createDocumentBuilder(false, this.secureValidation);
        Document document = db.parse(in);
        return this.engineCanonicalizeSubTree(document);
    }

    public byte[] engineCanonicalizeXPathNodeSet(NodeList xpathNodeSet) throws CanonicalizationException {
        return this.engineCanonicalizeXPathNodeSet(XMLUtils.convertNodelistToSet(xpathNodeSet));
    }

    public byte[] engineCanonicalizeXPathNodeSet(NodeList xpathNodeSet, String inclusiveNamespaces) throws CanonicalizationException {
        return this.engineCanonicalizeXPathNodeSet(XMLUtils.convertNodelistToSet(xpathNodeSet), inclusiveNamespaces);
    }

    public abstract String engineGetURI();

    public abstract boolean engineGetIncludeComments();

    public abstract byte[] engineCanonicalizeXPathNodeSet(Set<Node> var1) throws CanonicalizationException;

    public abstract byte[] engineCanonicalizeXPathNodeSet(Set<Node> var1, String var2) throws CanonicalizationException;

    public abstract byte[] engineCanonicalizeSubTree(Node var1) throws CanonicalizationException;

    public abstract byte[] engineCanonicalizeSubTree(Node var1, String var2) throws CanonicalizationException;

    public abstract void setWriter(OutputStream var1);

    public boolean isSecureValidation() {
        return this.secureValidation;
    }

    public void setSecureValidation(boolean secureValidation) {
        this.secureValidation = secureValidation;
    }
}


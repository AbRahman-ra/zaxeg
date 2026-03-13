/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.encryption;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xml.security.encryption.AbstractSerializer;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DocumentSerializer
extends AbstractSerializer {
    public Node deserialize(byte[] source, Node ctx) throws XMLEncryptionException {
        byte[] fragment = DocumentSerializer.createContext(source, ctx);
        return this.deserialize(ctx, new InputSource(new ByteArrayInputStream(fragment)));
    }

    @Deprecated
    public Node deserialize(String source, Node ctx) throws XMLEncryptionException {
        String fragment = DocumentSerializer.createContext(source, ctx);
        return this.deserialize(ctx, new InputSource(new StringReader(fragment)));
    }

    private Node deserialize(Node ctx, InputSource inputSource) throws XMLEncryptionException {
        try {
            DocumentBuilder db = XMLUtils.createDocumentBuilder(false, this.secureValidation);
            Document d = db.parse(inputSource);
            Document contextDocument = null;
            contextDocument = 9 == ctx.getNodeType() ? (Document)ctx : ctx.getOwnerDocument();
            Element fragElt = (Element)contextDocument.importNode(d.getDocumentElement(), true);
            DocumentFragment result = contextDocument.createDocumentFragment();
            Node child = fragElt.getFirstChild();
            while (child != null) {
                fragElt.removeChild(child);
                result.appendChild(child);
                child = fragElt.getFirstChild();
            }
            return result;
        } catch (SAXException se) {
            throw new XMLEncryptionException("empty", se);
        } catch (ParserConfigurationException pce) {
            throw new XMLEncryptionException("empty", pce);
        } catch (IOException ioe) {
            throw new XMLEncryptionException("empty", ioe);
        }
    }
}


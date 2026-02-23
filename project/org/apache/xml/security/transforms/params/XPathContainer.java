/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.transforms.params;

import org.apache.xml.security.transforms.TransformParam;
import org.apache.xml.security.utils.SignatureElementProxy;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class XPathContainer
extends SignatureElementProxy
implements TransformParam {
    public XPathContainer(Document doc) {
        super(doc);
    }

    public void setXPath(String xpath) {
        for (Node childNode = this.constructionElement.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
            Node nodeToBeRemoved = childNode;
            this.constructionElement.removeChild(nodeToBeRemoved);
        }
        Text xpathText = this.doc.createTextNode(xpath);
        this.constructionElement.appendChild(xpathText);
    }

    public String getXPath() {
        return this.getTextFromTextChild();
    }

    public String getBaseLocalName() {
        return "XPath";
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.apache.xml.dtm.DTM
 *  org.apache.xpath.NodeSetDTM
 *  org.apache.xpath.XPathContext
 *  org.apache.xpath.functions.Function
 *  org.apache.xpath.objects.XNodeSet
 *  org.apache.xpath.objects.XObject
 */
package org.apache.xml.security.transforms.implementations;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTM;
import org.apache.xml.security.utils.I18n;
import org.apache.xml.security.utils.XMLUtils;
import org.apache.xpath.NodeSetDTM;
import org.apache.xpath.XPathContext;
import org.apache.xpath.functions.Function;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class FuncHere
extends Function {
    private static final long serialVersionUID = 1L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        Document xpathOwnerDoc;
        Document currentDoc;
        Node xpathOwnerNode = (Node)xctxt.getOwnerObject();
        if (xpathOwnerNode == null) {
            return null;
        }
        int xpathOwnerNodeDTM = xctxt.getDTMHandleFromNode(xpathOwnerNode);
        int currentNode = xctxt.getCurrentNode();
        DTM dtm = xctxt.getDTM(currentNode);
        int docContext = dtm.getDocument();
        if (-1 == docContext) {
            this.error(xctxt, "ER_CONTEXT_HAS_NO_OWNERDOC", null);
        }
        if ((currentDoc = XMLUtils.getOwnerDocument(dtm.getNode(currentNode))) != (xpathOwnerDoc = XMLUtils.getOwnerDocument(xpathOwnerNode))) {
            throw new TransformerException(I18n.translate("xpath.funcHere.documentsDiffer"));
        }
        XNodeSet nodes = new XNodeSet(xctxt.getDTMManager());
        NodeSetDTM nodeSet = nodes.mutableNodeset();
        int hereNode = -1;
        switch (dtm.getNodeType(xpathOwnerNodeDTM)) {
            case 2: 
            case 7: {
                hereNode = xpathOwnerNodeDTM;
                nodeSet.addNode(hereNode);
                break;
            }
            case 3: {
                hereNode = dtm.getParent(xpathOwnerNodeDTM);
                nodeSet.addNode(hereNode);
                break;
            }
        }
        nodeSet.detach();
        return nodes;
    }

    public void fixupVariables(Vector vars, int globalsSize) {
    }
}


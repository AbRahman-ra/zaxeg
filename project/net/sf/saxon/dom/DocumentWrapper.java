/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.dom;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DOMNodeWrapper;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.om.GenericTreeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.tree.iter.AxisIterator;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DocumentWrapper
extends GenericTreeInfo {
    protected boolean domLevel3;
    public final Node docNode;
    private Map<String, NodeInfo> idIndex = null;

    public DocumentWrapper(Node doc, String baseURI, Configuration config) {
        super(config);
        if (doc.getNodeType() != 9 && doc.getNodeType() != 11) {
            throw new IllegalArgumentException("Node must be a DOM Document or DocumentFragment");
        }
        if (config.getExternalObjectModel(doc.getClass()) == null) {
            throw new IllegalArgumentException("Node class " + doc.getClass().getName() + " is not recognized in this Saxon configuration");
        }
        this.domLevel3 = true;
        this.docNode = doc;
        this.setRootNode(this.wrap(doc));
        this.setSystemId(baseURI);
    }

    public DOMNodeWrapper wrap(Node node) {
        return DOMNodeWrapper.makeWrapper(node, this);
    }

    public void setDOMLevel(int level) {
        if (level != 2 && level != 3) {
            throw new IllegalArgumentException("DOM Level must be 2 or 3");
        }
        this.domLevel3 = level == 3;
    }

    public int getDOMLevel() {
        return this.domLevel3 ? 3 : 2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public NodeInfo selectID(String id, boolean getParent) {
        Node node = this.docNode;
        synchronized (node) {
            NodeInfo e;
            Element el;
            Node node2 = ((DOMNodeWrapper)this.getRootNode()).node;
            if (node2 instanceof Document && (el = ((Document)node2).getElementById(id)) != null) {
                return this.wrap(el);
            }
            if (this.idIndex != null) {
                return this.idIndex.get(id);
            }
            this.idIndex = new HashMap<String, NodeInfo>();
            AxisIterator iter = this.getRootNode().iterateAxis(4, NodeKindTest.ELEMENT);
            while ((e = iter.next()) != null) {
                String xmlId = e.getAttributeValue("http://www.w3.org/XML/1998/namespace", "id");
                if (xmlId == null) continue;
                this.idIndex.put(xmlId, e);
            }
            return this.idIndex.get(id);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Iterator<String> getUnparsedEntityNames() {
        Node node = this.docNode;
        synchronized (node) {
            Node node2 = ((DOMNodeWrapper)this.getRootNode()).node;
            if (node2 instanceof Document) {
                DocumentType docType = ((Document)node2).getDoctype();
                if (docType == null) {
                    List ls = Collections.emptyList();
                    return ls.iterator();
                }
                NamedNodeMap map = docType.getEntities();
                if (map == null) {
                    List ls = Collections.emptyList();
                    return ls.iterator();
                }
                ArrayList<String> names = new ArrayList<String>(map.getLength());
                for (int i = 0; i < map.getLength(); ++i) {
                    Entity e = (Entity)map.item(i);
                    if (e.getNotationName() == null) continue;
                    names.add(e.getLocalName());
                }
                return names.iterator();
            }
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String[] getUnparsedEntity(String name) {
        Node node = this.docNode;
        synchronized (node) {
            Node node2 = ((DOMNodeWrapper)this.getRootNode()).node;
            if (node2 instanceof Document) {
                DocumentType docType = ((Document)node2).getDoctype();
                if (docType == null) {
                    return null;
                }
                NamedNodeMap map = docType.getEntities();
                if (map == null) {
                    return null;
                }
                Entity entity = (Entity)map.getNamedItem(name);
                if (entity == null || entity.getNotationName() == null) {
                    return null;
                }
                String systemId = entity.getSystemId();
                try {
                    String base;
                    URI systemIdURI = new URI(systemId);
                    if (!systemIdURI.isAbsolute() && (base = this.getRootNode().getBaseURI()) != null) {
                        systemId = ResolveURI.makeAbsolute(systemId, base).toString();
                    }
                } catch (URISyntaxException uRISyntaxException) {
                    // empty catch block
                }
                return new String[]{systemId, entity.getPublicId()};
            }
            return null;
        }
    }
}


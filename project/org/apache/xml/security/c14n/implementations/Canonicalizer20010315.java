/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.c14n.implementations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.helper.C14nHelper;
import org.apache.xml.security.c14n.implementations.CanonicalizerBase;
import org.apache.xml.security.c14n.implementations.NameSpaceSymbTable;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class Canonicalizer20010315
extends CanonicalizerBase {
    private static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";
    private static final String XML_LANG_URI = "http://www.w3.org/XML/1998/namespace";
    private boolean firstCall = true;
    private final SortedSet<Attr> result = new TreeSet<Attr>(COMPARE);
    private XmlAttrStack xmlattrStack = new XmlAttrStack();

    public Canonicalizer20010315(boolean includeComments) {
        super(includeComments);
    }

    @Override
    public byte[] engineCanonicalizeXPathNodeSet(Set<Node> xpathNodeSet, String inclusiveNamespaces) throws CanonicalizationException {
        throw new CanonicalizationException("c14n.Canonicalizer.UnsupportedOperation");
    }

    @Override
    public byte[] engineCanonicalizeSubTree(Node rootNode, String inclusiveNamespaces) throws CanonicalizationException {
        throw new CanonicalizationException("c14n.Canonicalizer.UnsupportedOperation");
    }

    @Override
    protected Iterator<Attr> handleAttributesSubtree(Element element, NameSpaceSymbTable ns) throws CanonicalizationException {
        if (!element.hasAttributes() && !this.firstCall) {
            return null;
        }
        SortedSet<Attr> result = this.result;
        result.clear();
        if (element.hasAttributes()) {
            NamedNodeMap attrs = element.getAttributes();
            int attrsLength = attrs.getLength();
            for (int i = 0; i < attrsLength; ++i) {
                Node n;
                Attr attribute = (Attr)attrs.item(i);
                String NUri = attribute.getNamespaceURI();
                String NName = attribute.getLocalName();
                String NValue = attribute.getValue();
                if (!XMLNS_URI.equals(NUri)) {
                    result.add(attribute);
                    continue;
                }
                if ("xml".equals(NName) && XML_LANG_URI.equals(NValue) || (n = ns.addMappingAndRender(NName, NValue, attribute)) == null) continue;
                result.add((Attr)n);
                if (!C14nHelper.namespaceIsRelative(attribute)) continue;
                Object[] exArgs = new Object[]{element.getTagName(), NName, attribute.getNodeValue()};
                throw new CanonicalizationException("c14n.Canonicalizer.RelativeNamespace", exArgs);
            }
        }
        if (this.firstCall) {
            ns.getUnrenderedNodes(result);
            this.xmlattrStack.getXmlnsAttr(result);
            this.firstCall = false;
        }
        return result.iterator();
    }

    @Override
    protected Iterator<Attr> handleAttributes(Element element, NameSpaceSymbTable ns) throws CanonicalizationException {
        this.xmlattrStack.push(ns.getLevel());
        boolean isRealVisible = this.isVisibleDO(element, ns.getLevel()) == 1;
        SortedSet<Attr> result = this.result;
        result.clear();
        if (element.hasAttributes()) {
            NamedNodeMap attrs = element.getAttributes();
            int attrsLength = attrs.getLength();
            for (int i = 0; i < attrsLength; ++i) {
                Attr attribute = (Attr)attrs.item(i);
                String NUri = attribute.getNamespaceURI();
                String NName = attribute.getLocalName();
                String NValue = attribute.getValue();
                if (!XMLNS_URI.equals(NUri)) {
                    if (XML_LANG_URI.equals(NUri)) {
                        this.xmlattrStack.addXmlnsAttr(attribute);
                        continue;
                    }
                    if (!isRealVisible) continue;
                    result.add(attribute);
                    continue;
                }
                if ("xml".equals(NName) && XML_LANG_URI.equals(NValue)) continue;
                if (this.isVisible(attribute)) {
                    Node n;
                    if (!isRealVisible && ns.removeMappingIfRender(NName) || (n = ns.addMappingAndRender(NName, NValue, attribute)) == null) continue;
                    result.add((Attr)n);
                    if (!C14nHelper.namespaceIsRelative(attribute)) continue;
                    Object[] exArgs = new Object[]{element.getTagName(), NName, attribute.getNodeValue()};
                    throw new CanonicalizationException("c14n.Canonicalizer.RelativeNamespace", exArgs);
                }
                if (isRealVisible && !"xmlns".equals(NName)) {
                    ns.removeMapping(NName);
                    continue;
                }
                ns.addMapping(NName, NValue, attribute);
            }
        }
        if (isRealVisible) {
            Attr xmlns = element.getAttributeNodeNS(XMLNS_URI, "xmlns");
            Node n = null;
            if (xmlns == null) {
                n = ns.getMapping("xmlns");
            } else if (!this.isVisible(xmlns)) {
                n = ns.addMappingAndRender("xmlns", "", this.getNullNode(xmlns.getOwnerDocument()));
            }
            if (n != null) {
                result.add((Attr)n);
            }
            this.xmlattrStack.getXmlnsAttr(result);
            ns.getUnrenderedNodes(result);
        }
        return result.iterator();
    }

    @Override
    protected void circumventBugIfNeeded(XMLSignatureInput input) throws CanonicalizationException, ParserConfigurationException, IOException, SAXException {
        if (!input.isNeedsToBeExpanded()) {
            return;
        }
        Document doc = null;
        doc = input.getSubNode() != null ? XMLUtils.getOwnerDocument(input.getSubNode()) : XMLUtils.getOwnerDocument(input.getNodeSet());
        XMLUtils.circumventBug2650(doc);
    }

    @Override
    protected void handleParent(Element e, NameSpaceSymbTable ns) {
        if (!e.hasAttributes() && e.getNamespaceURI() == null) {
            return;
        }
        this.xmlattrStack.push(-1);
        NamedNodeMap attrs = e.getAttributes();
        int attrsLength = attrs.getLength();
        for (int i = 0; i < attrsLength; ++i) {
            Attr attribute = (Attr)attrs.item(i);
            String NName = attribute.getLocalName();
            String NValue = attribute.getNodeValue();
            if (XMLNS_URI.equals(attribute.getNamespaceURI())) {
                if ("xml".equals(NName) && XML_LANG_URI.equals(NValue)) continue;
                ns.addMapping(NName, NValue, attribute);
                continue;
            }
            if (!XML_LANG_URI.equals(attribute.getNamespaceURI())) continue;
            this.xmlattrStack.addXmlnsAttr(attribute);
        }
        if (e.getNamespaceURI() != null) {
            String Name4;
            String NName = e.getPrefix();
            String NValue = e.getNamespaceURI();
            if (NName == null || NName.equals("")) {
                NName = "xmlns";
                Name4 = "xmlns";
            } else {
                Name4 = "xmlns:" + NName;
            }
            Attr n = e.getOwnerDocument().createAttributeNS(XMLNS_URI, Name4);
            n.setValue(NValue);
            ns.addMapping(NName, NValue, n);
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    private static class XmlAttrStack {
        int currentLevel = 0;
        int lastlevel = 0;
        XmlsStackElement cur;
        List<XmlsStackElement> levels = new ArrayList<XmlsStackElement>();

        private XmlAttrStack() {
        }

        void push(int level) {
            this.currentLevel = level;
            if (this.currentLevel == -1) {
                return;
            }
            this.cur = null;
            while (this.lastlevel >= this.currentLevel) {
                this.levels.remove(this.levels.size() - 1);
                int newSize = this.levels.size();
                if (newSize == 0) {
                    this.lastlevel = 0;
                    return;
                }
                this.lastlevel = this.levels.get((int)(newSize - 1)).level;
            }
        }

        void addXmlnsAttr(Attr n) {
            if (this.cur == null) {
                this.cur = new XmlsStackElement();
                this.cur.level = this.currentLevel;
                this.levels.add(this.cur);
                this.lastlevel = this.currentLevel;
            }
            this.cur.nodes.add(n);
        }

        void getXmlnsAttr(Collection<Attr> col) {
            int size = this.levels.size() - 1;
            if (this.cur == null) {
                this.cur = new XmlsStackElement();
                this.cur.level = this.currentLevel;
                this.lastlevel = this.currentLevel;
                this.levels.add(this.cur);
            }
            boolean parentRendered = false;
            XmlsStackElement e = null;
            if (size == -1) {
                parentRendered = true;
            } else {
                e = this.levels.get(size);
                if (e.rendered && e.level + 1 == this.currentLevel) {
                    parentRendered = true;
                }
            }
            if (parentRendered) {
                col.addAll(this.cur.nodes);
                this.cur.rendered = true;
                return;
            }
            HashMap<String, Attr> loa = new HashMap<String, Attr>();
            while (size >= 0) {
                e = this.levels.get(size);
                for (Attr n : e.nodes) {
                    if (loa.containsKey(n.getName())) continue;
                    loa.put(n.getName(), n);
                }
                --size;
            }
            this.cur.rendered = true;
            col.addAll(loa.values());
        }

        static class XmlsStackElement {
            int level;
            boolean rendered = false;
            List<Attr> nodes = new ArrayList<Attr>();

            XmlsStackElement() {
            }
        }
    }
}


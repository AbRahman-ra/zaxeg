/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.c14n.implementations;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public abstract class Canonicalizer11
extends CanonicalizerBase {
    private static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";
    private static final String XML_LANG_URI = "http://www.w3.org/XML/1998/namespace";
    private static Log log = LogFactory.getLog(Canonicalizer11.class);
    private final SortedSet<Attr> result = new TreeSet<Attr>(COMPARE);
    private boolean firstCall = true;
    private XmlAttrStack xmlattrStack = new XmlAttrStack();

    public Canonicalizer11(boolean includeComments) {
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
                        if (NName.equals("id")) {
                            if (!isRealVisible) continue;
                            result.add(attribute);
                            continue;
                        }
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
            if ("id".equals(NName) || !XML_LANG_URI.equals(attribute.getNamespaceURI())) continue;
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

    private static String joinURI(String baseURI, String relativeURI) throws URISyntaxException {
        String tquery;
        String tpath;
        String tauthority;
        String tscheme;
        String bscheme = null;
        String bauthority = null;
        String bpath = "";
        String bquery = null;
        if (baseURI != null) {
            if (baseURI.endsWith("..")) {
                baseURI = baseURI + "/";
            }
            URI base = new URI(baseURI);
            bscheme = base.getScheme();
            bauthority = base.getAuthority();
            bpath = base.getPath();
            bquery = base.getQuery();
        }
        URI r = new URI(relativeURI);
        String rscheme = r.getScheme();
        String rauthority = r.getAuthority();
        String rpath = r.getPath();
        String rquery = r.getQuery();
        if (rscheme != null && rscheme.equals(bscheme)) {
            rscheme = null;
        }
        if (rscheme != null) {
            tscheme = rscheme;
            tauthority = rauthority;
            tpath = Canonicalizer11.removeDotSegments(rpath);
            tquery = rquery;
        } else {
            if (rauthority != null) {
                tauthority = rauthority;
                tpath = Canonicalizer11.removeDotSegments(rpath);
                tquery = rquery;
            } else {
                if (rpath.length() == 0) {
                    tpath = bpath;
                    tquery = rquery != null ? rquery : bquery;
                } else {
                    if (rpath.startsWith("/")) {
                        tpath = Canonicalizer11.removeDotSegments(rpath);
                    } else {
                        int last;
                        tpath = bauthority != null && bpath.length() == 0 ? "/" + rpath : ((last = bpath.lastIndexOf(47)) == -1 ? rpath : bpath.substring(0, last + 1) + rpath);
                        tpath = Canonicalizer11.removeDotSegments(tpath);
                    }
                    tquery = rquery;
                }
                tauthority = bauthority;
            }
            tscheme = bscheme;
        }
        return new URI(tscheme, tauthority, tpath, tquery, null).toString();
    }

    private static String removeDotSegments(String path) {
        if (log.isDebugEnabled()) {
            log.debug("STEP   OUTPUT BUFFER\t\tINPUT BUFFER");
        }
        String input = path;
        while (input.indexOf("//") > -1) {
            input = input.replaceAll("//", "/");
        }
        StringBuilder output = new StringBuilder();
        if (input.charAt(0) == '/') {
            output.append("/");
            input = input.substring(1);
        }
        Canonicalizer11.printStep("1 ", output.toString(), input);
        while (input.length() != 0) {
            String segment;
            int index;
            if (input.startsWith("./")) {
                input = input.substring(2);
                Canonicalizer11.printStep("2A", output.toString(), input);
                continue;
            }
            if (input.startsWith("../")) {
                input = input.substring(3);
                if (!output.toString().equals("/")) {
                    output.append("../");
                }
                Canonicalizer11.printStep("2A", output.toString(), input);
                continue;
            }
            if (input.startsWith("/./")) {
                input = input.substring(2);
                Canonicalizer11.printStep("2B", output.toString(), input);
                continue;
            }
            if (input.equals("/.")) {
                input = input.replaceFirst("/.", "/");
                Canonicalizer11.printStep("2B", output.toString(), input);
                continue;
            }
            if (input.startsWith("/../")) {
                input = input.substring(3);
                if (output.length() == 0) {
                    output.append("/");
                } else if (output.toString().endsWith("../")) {
                    output.append("..");
                } else if (output.toString().endsWith("..")) {
                    output.append("/..");
                } else {
                    index = output.lastIndexOf("/");
                    if (index == -1) {
                        output = new StringBuilder();
                        if (input.charAt(0) == '/') {
                            input = input.substring(1);
                        }
                    } else {
                        output = output.delete(index, output.length());
                    }
                }
                Canonicalizer11.printStep("2C", output.toString(), input);
                continue;
            }
            if (input.equals("/..")) {
                input = input.replaceFirst("/..", "/");
                if (output.length() == 0) {
                    output.append("/");
                } else if (output.toString().endsWith("../")) {
                    output.append("..");
                } else if (output.toString().endsWith("..")) {
                    output.append("/..");
                } else {
                    index = output.lastIndexOf("/");
                    if (index == -1) {
                        output = new StringBuilder();
                        if (input.charAt(0) == '/') {
                            input = input.substring(1);
                        }
                    } else {
                        output = output.delete(index, output.length());
                    }
                }
                Canonicalizer11.printStep("2C", output.toString(), input);
                continue;
            }
            if (input.equals(".")) {
                input = "";
                Canonicalizer11.printStep("2D", output.toString(), input);
                continue;
            }
            if (input.equals("..")) {
                if (!output.toString().equals("/")) {
                    output.append("..");
                }
                input = "";
                Canonicalizer11.printStep("2D", output.toString(), input);
                continue;
            }
            int end = -1;
            int begin = input.indexOf(47);
            if (begin == 0) {
                end = input.indexOf(47, 1);
            } else {
                end = begin;
                begin = 0;
            }
            if (end == -1) {
                segment = input.substring(begin);
                input = "";
            } else {
                segment = input.substring(begin, end);
                input = input.substring(end);
            }
            output.append(segment);
            Canonicalizer11.printStep("2E", output.toString(), input);
        }
        if (output.toString().endsWith("..")) {
            output.append("/");
            Canonicalizer11.printStep("3 ", output.toString(), input);
        }
        return output.toString();
    }

    private static void printStep(String step, String output, String input) {
        if (log.isDebugEnabled()) {
            log.debug(" " + step + ":   " + output);
            if (output.length() == 0) {
                log.debug("\t\t\t\t" + input);
            } else {
                log.debug("\t\t\t" + input);
            }
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
            Iterator<Attr> it;
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
            ArrayList<Attr> baseAttrs = new ArrayList<Attr>();
            boolean successiveOmitted = true;
            while (size >= 0) {
                e = this.levels.get(size);
                if (e.rendered) {
                    successiveOmitted = false;
                }
                it = e.nodes.iterator();
                while (it.hasNext() && successiveOmitted) {
                    Attr n = it.next();
                    if (n.getLocalName().equals("base") && !e.rendered) {
                        baseAttrs.add(n);
                        continue;
                    }
                    if (loa.containsKey(n.getName())) continue;
                    loa.put(n.getName(), n);
                }
                --size;
            }
            if (!baseAttrs.isEmpty()) {
                it = col.iterator();
                String base = null;
                Attr baseAttr = null;
                while (it.hasNext()) {
                    Attr n = it.next();
                    if (!n.getLocalName().equals("base")) continue;
                    base = n.getValue();
                    baseAttr = n;
                    break;
                }
                for (Attr n : baseAttrs) {
                    if (base == null) {
                        base = n.getValue();
                        baseAttr = n;
                        continue;
                    }
                    try {
                        base = Canonicalizer11.joinURI(n.getValue(), base);
                    } catch (URISyntaxException ue) {
                        if (!log.isDebugEnabled()) continue;
                        log.debug(ue.getMessage(), ue);
                    }
                }
                if (base != null && base.length() != 0) {
                    baseAttr.setValue(base);
                    col.add(baseAttr);
                }
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


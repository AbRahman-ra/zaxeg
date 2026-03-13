/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.signature;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Set;
import org.apache.xml.security.c14n.helper.AttrCompare;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class XMLSignatureInputDebugger {
    private Set<Node> xpathNodeSet;
    private Set<String> inclusiveNamespaces;
    private Document doc = null;
    private Writer writer = null;
    static final String HTMLPrefix = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n<html>\n<head>\n<title>Caninical XML node set</title>\n<style type=\"text/css\">\n<!-- \n.INCLUDED { \n   color: #000000; \n   background-color: \n   #FFFFFF; \n   font-weight: bold; } \n.EXCLUDED { \n   color: #666666; \n   background-color: \n   #999999; } \n.INCLUDEDINCLUSIVENAMESPACE { \n   color: #0000FF; \n   background-color: #FFFFFF; \n   font-weight: bold; \n   font-style: italic; } \n.EXCLUDEDINCLUSIVENAMESPACE { \n   color: #0000FF; \n   background-color: #999999; \n   font-style: italic; } \n--> \n</style> \n</head>\n<body bgcolor=\"#999999\">\n<h1>Explanation of the output</h1>\n<p>The following text contains the nodeset of the given Reference before it is canonicalized. There exist four different styles to indicate how a given node is treated.</p>\n<ul>\n<li class=\"INCLUDED\">A node which is in the node set is labeled using the INCLUDED style.</li>\n<li class=\"EXCLUDED\">A node which is <em>NOT</em> in the node set is labeled EXCLUDED style.</li>\n<li class=\"INCLUDEDINCLUSIVENAMESPACE\">A namespace which is in the node set AND in the InclusiveNamespaces PrefixList is labeled using the INCLUDEDINCLUSIVENAMESPACE style.</li>\n<li class=\"EXCLUDEDINCLUSIVENAMESPACE\">A namespace which is in NOT the node set AND in the InclusiveNamespaces PrefixList is labeled using the INCLUDEDINCLUSIVENAMESPACE style.</li>\n</ul>\n<h1>Output</h1>\n<pre>\n";
    static final String HTMLSuffix = "</pre></body></html>";
    static final String HTMLExcludePrefix = "<span class=\"EXCLUDED\">";
    static final String HTMLIncludePrefix = "<span class=\"INCLUDED\">";
    static final String HTMLIncludeOrExcludeSuffix = "</span>";
    static final String HTMLIncludedInclusiveNamespacePrefix = "<span class=\"INCLUDEDINCLUSIVENAMESPACE\">";
    static final String HTMLExcludedInclusiveNamespacePrefix = "<span class=\"EXCLUDEDINCLUSIVENAMESPACE\">";
    private static final int NODE_BEFORE_DOCUMENT_ELEMENT = -1;
    private static final int NODE_NOT_BEFORE_OR_AFTER_DOCUMENT_ELEMENT = 0;
    private static final int NODE_AFTER_DOCUMENT_ELEMENT = 1;
    static final AttrCompare ATTR_COMPARE = new AttrCompare();

    public XMLSignatureInputDebugger(XMLSignatureInput xmlSignatureInput) {
        this.xpathNodeSet = !xmlSignatureInput.isNodeSet() ? null : xmlSignatureInput.getInputNodeSet();
    }

    public XMLSignatureInputDebugger(XMLSignatureInput xmlSignatureInput, Set<String> inclusiveNamespace) {
        this(xmlSignatureInput);
        this.inclusiveNamespaces = inclusiveNamespace;
    }

    public String getHTMLRepresentation() throws XMLSignatureException {
        if (this.xpathNodeSet == null || this.xpathNodeSet.size() == 0) {
            return "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n<html>\n<head>\n<title>Caninical XML node set</title>\n<style type=\"text/css\">\n<!-- \n.INCLUDED { \n   color: #000000; \n   background-color: \n   #FFFFFF; \n   font-weight: bold; } \n.EXCLUDED { \n   color: #666666; \n   background-color: \n   #999999; } \n.INCLUDEDINCLUSIVENAMESPACE { \n   color: #0000FF; \n   background-color: #FFFFFF; \n   font-weight: bold; \n   font-style: italic; } \n.EXCLUDEDINCLUSIVENAMESPACE { \n   color: #0000FF; \n   background-color: #999999; \n   font-style: italic; } \n--> \n</style> \n</head>\n<body bgcolor=\"#999999\">\n<h1>Explanation of the output</h1>\n<p>The following text contains the nodeset of the given Reference before it is canonicalized. There exist four different styles to indicate how a given node is treated.</p>\n<ul>\n<li class=\"INCLUDED\">A node which is in the node set is labeled using the INCLUDED style.</li>\n<li class=\"EXCLUDED\">A node which is <em>NOT</em> in the node set is labeled EXCLUDED style.</li>\n<li class=\"INCLUDEDINCLUSIVENAMESPACE\">A namespace which is in the node set AND in the InclusiveNamespaces PrefixList is labeled using the INCLUDEDINCLUSIVENAMESPACE style.</li>\n<li class=\"EXCLUDEDINCLUSIVENAMESPACE\">A namespace which is in NOT the node set AND in the InclusiveNamespaces PrefixList is labeled using the INCLUDEDINCLUSIVENAMESPACE style.</li>\n</ul>\n<h1>Output</h1>\n<pre>\n<blink>no node set, sorry</blink></pre></body></html>";
        }
        Node n = this.xpathNodeSet.iterator().next();
        this.doc = XMLUtils.getOwnerDocument(n);
        try {
            this.writer = new StringWriter();
            this.canonicalizeXPathNodeSet(this.doc);
            this.writer.close();
            String string = this.writer.toString();
            return string;
        } catch (IOException ex) {
            throw new XMLSignatureException("empty", ex);
        } finally {
            this.xpathNodeSet = null;
            this.doc = null;
            this.writer = null;
        }
    }

    private void canonicalizeXPathNodeSet(Node currentNode) throws XMLSignatureException, IOException {
        short currentNodeType = currentNode.getNodeType();
        switch (currentNodeType) {
            case 2: 
            case 6: 
            case 11: 
            case 12: {
                throw new XMLSignatureException("empty");
            }
            case 9: {
                this.writer.write(HTMLPrefix);
                for (Node currentChild = currentNode.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
                    this.canonicalizeXPathNodeSet(currentChild);
                }
                this.writer.write(HTMLSuffix);
                break;
            }
            case 8: {
                if (this.xpathNodeSet.contains(currentNode)) {
                    this.writer.write(HTMLIncludePrefix);
                } else {
                    this.writer.write(HTMLExcludePrefix);
                }
                int position = this.getPositionRelativeToDocumentElement(currentNode);
                if (position == 1) {
                    this.writer.write("\n");
                }
                this.outputCommentToWriter((Comment)currentNode);
                if (position == -1) {
                    this.writer.write("\n");
                }
                this.writer.write(HTMLIncludeOrExcludeSuffix);
                break;
            }
            case 7: {
                if (this.xpathNodeSet.contains(currentNode)) {
                    this.writer.write(HTMLIncludePrefix);
                } else {
                    this.writer.write(HTMLExcludePrefix);
                }
                int position = this.getPositionRelativeToDocumentElement(currentNode);
                if (position == 1) {
                    this.writer.write("\n");
                }
                this.outputPItoWriter((ProcessingInstruction)currentNode);
                if (position == -1) {
                    this.writer.write("\n");
                }
                this.writer.write(HTMLIncludeOrExcludeSuffix);
                break;
            }
            case 3: 
            case 4: {
                if (this.xpathNodeSet.contains(currentNode)) {
                    this.writer.write(HTMLIncludePrefix);
                } else {
                    this.writer.write(HTMLExcludePrefix);
                }
                this.outputTextToWriter(currentNode.getNodeValue());
                for (Node nextSibling = currentNode.getNextSibling(); nextSibling != null && (nextSibling.getNodeType() == 3 || nextSibling.getNodeType() == 4); nextSibling = nextSibling.getNextSibling()) {
                    this.outputTextToWriter(nextSibling.getNodeValue());
                }
                this.writer.write(HTMLIncludeOrExcludeSuffix);
                break;
            }
            case 1: {
                Element currentElement = (Element)currentNode;
                if (this.xpathNodeSet.contains(currentNode)) {
                    this.writer.write(HTMLIncludePrefix);
                } else {
                    this.writer.write(HTMLExcludePrefix);
                }
                this.writer.write("&lt;");
                this.writer.write(currentElement.getTagName());
                this.writer.write(HTMLIncludeOrExcludeSuffix);
                NamedNodeMap attrs = currentElement.getAttributes();
                int attrsLength = attrs.getLength();
                Attr[] attrs2 = new Attr[attrsLength];
                for (int i = 0; i < attrsLength; ++i) {
                    attrs2[i] = (Attr)attrs.item(i);
                }
                Arrays.sort(attrs2, ATTR_COMPARE);
                Attr[] attrs3 = attrs2;
                for (int i = 0; i < attrsLength; ++i) {
                    Attr a = attrs3[i];
                    boolean included = this.xpathNodeSet.contains(a);
                    boolean inclusive = this.inclusiveNamespaces.contains(a.getName());
                    if (included) {
                        if (inclusive) {
                            this.writer.write(HTMLIncludedInclusiveNamespacePrefix);
                        } else {
                            this.writer.write(HTMLIncludePrefix);
                        }
                    } else if (inclusive) {
                        this.writer.write(HTMLExcludedInclusiveNamespacePrefix);
                    } else {
                        this.writer.write(HTMLExcludePrefix);
                    }
                    this.outputAttrToWriter(a.getNodeName(), a.getNodeValue());
                    this.writer.write(HTMLIncludeOrExcludeSuffix);
                }
                if (this.xpathNodeSet.contains(currentNode)) {
                    this.writer.write(HTMLIncludePrefix);
                } else {
                    this.writer.write(HTMLExcludePrefix);
                }
                this.writer.write("&gt;");
                this.writer.write(HTMLIncludeOrExcludeSuffix);
                for (Node currentChild = currentNode.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
                    this.canonicalizeXPathNodeSet(currentChild);
                }
                if (this.xpathNodeSet.contains(currentNode)) {
                    this.writer.write(HTMLIncludePrefix);
                } else {
                    this.writer.write(HTMLExcludePrefix);
                }
                this.writer.write("&lt;/");
                this.writer.write(currentElement.getTagName());
                this.writer.write("&gt;");
                this.writer.write(HTMLIncludeOrExcludeSuffix);
                break;
            }
        }
    }

    private int getPositionRelativeToDocumentElement(Node currentNode) {
        if (currentNode == null) {
            return 0;
        }
        Document doc = currentNode.getOwnerDocument();
        if (currentNode.getParentNode() != doc) {
            return 0;
        }
        Element documentElement = doc.getDocumentElement();
        if (documentElement == null) {
            return 0;
        }
        if (documentElement == currentNode) {
            return 0;
        }
        for (Node x = currentNode; x != null; x = x.getNextSibling()) {
            if (x != documentElement) continue;
            return -1;
        }
        return 1;
    }

    private void outputAttrToWriter(String name, String value) throws IOException {
        this.writer.write(" ");
        this.writer.write(name);
        this.writer.write("=\"");
        int length = value.length();
        block8: for (int i = 0; i < length; ++i) {
            char c = value.charAt(i);
            switch (c) {
                case '&': {
                    this.writer.write("&amp;amp;");
                    continue block8;
                }
                case '<': {
                    this.writer.write("&amp;lt;");
                    continue block8;
                }
                case '\"': {
                    this.writer.write("&amp;quot;");
                    continue block8;
                }
                case '\t': {
                    this.writer.write("&amp;#x9;");
                    continue block8;
                }
                case '\n': {
                    this.writer.write("&amp;#xA;");
                    continue block8;
                }
                case '\r': {
                    this.writer.write("&amp;#xD;");
                    continue block8;
                }
                default: {
                    this.writer.write(c);
                }
            }
        }
        this.writer.write("\"");
    }

    private void outputPItoWriter(ProcessingInstruction currentPI) throws IOException {
        if (currentPI == null) {
            return;
        }
        this.writer.write("&lt;?");
        String target = currentPI.getTarget();
        int length = target.length();
        block8: for (int i = 0; i < length; ++i) {
            char c = target.charAt(i);
            switch (c) {
                case '\r': {
                    this.writer.write("&amp;#xD;");
                    continue block8;
                }
                case ' ': {
                    this.writer.write("&middot;");
                    continue block8;
                }
                case '\n': {
                    this.writer.write("&para;\n");
                    continue block8;
                }
                default: {
                    this.writer.write(c);
                }
            }
        }
        String data = currentPI.getData();
        length = data.length();
        if (length > 0) {
            this.writer.write(" ");
            block9: for (int i = 0; i < length; ++i) {
                char c = data.charAt(i);
                switch (c) {
                    case '\r': {
                        this.writer.write("&amp;#xD;");
                        continue block9;
                    }
                    default: {
                        this.writer.write(c);
                    }
                }
            }
        }
        this.writer.write("?&gt;");
    }

    private void outputCommentToWriter(Comment currentComment) throws IOException {
        if (currentComment == null) {
            return;
        }
        this.writer.write("&lt;!--");
        String data = currentComment.getData();
        int length = data.length();
        block5: for (int i = 0; i < length; ++i) {
            char c = data.charAt(i);
            switch (c) {
                case '\r': {
                    this.writer.write("&amp;#xD;");
                    continue block5;
                }
                case ' ': {
                    this.writer.write("&middot;");
                    continue block5;
                }
                case '\n': {
                    this.writer.write("&para;\n");
                    continue block5;
                }
                default: {
                    this.writer.write(c);
                }
            }
        }
        this.writer.write("--&gt;");
    }

    private void outputTextToWriter(String text) throws IOException {
        if (text == null) {
            return;
        }
        int length = text.length();
        block8: for (int i = 0; i < length; ++i) {
            char c = text.charAt(i);
            switch (c) {
                case '&': {
                    this.writer.write("&amp;amp;");
                    continue block8;
                }
                case '<': {
                    this.writer.write("&amp;lt;");
                    continue block8;
                }
                case '>': {
                    this.writer.write("&amp;gt;");
                    continue block8;
                }
                case '\r': {
                    this.writer.write("&amp;#xD;");
                    continue block8;
                }
                case ' ': {
                    this.writer.write("&middot;");
                    continue block8;
                }
                case '\n': {
                    this.writer.write("&para;\n");
                    continue block8;
                }
                default: {
                    this.writer.write(c);
                }
            }
        }
    }
}


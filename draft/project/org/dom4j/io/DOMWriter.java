/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.io;

import java.util.List;
import org.dom4j.Attribute;
import org.dom4j.CDATA;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Entity;
import org.dom4j.Namespace;
import org.dom4j.ProcessingInstruction;
import org.dom4j.Text;
import org.dom4j.io.JAXPHelper;
import org.dom4j.io.SAXHelper;
import org.dom4j.tree.NamespaceStack;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DocumentType;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;

public class DOMWriter {
    private static boolean loggedWarning = false;
    private static final String[] DEFAULT_DOM_DOCUMENT_CLASSES = new String[]{"org.apache.xerces.dom.DocumentImpl", "gnu.xml.dom.DomDocument", "org.apache.crimson.tree.XmlDocument", "com.sun.xml.tree.XmlDocument", "oracle.xml.parser.v2.XMLDocument", "oracle.xml.parser.XMLDocument", "org.dom4j.dom.DOMDocument"};
    private Class domDocumentClass;
    private NamespaceStack namespaceStack = new NamespaceStack();
    static /* synthetic */ Class class$org$dom4j$io$DOMWriter;

    public DOMWriter() {
    }

    public DOMWriter(Class domDocumentClass) {
        this.domDocumentClass = domDocumentClass;
    }

    public Class getDomDocumentClass() throws DocumentException {
        Class<?> result = this.domDocumentClass;
        if (result == null) {
            int size = DEFAULT_DOM_DOCUMENT_CLASSES.length;
            for (int i = 0; i < size; ++i) {
                try {
                    String name = DEFAULT_DOM_DOCUMENT_CLASSES[i];
                    result = Class.forName(name, true, (class$org$dom4j$io$DOMWriter == null ? DOMWriter.class$("org.dom4j.io.DOMWriter") : class$org$dom4j$io$DOMWriter).getClassLoader());
                    if (result == null) continue;
                    break;
                } catch (Exception e) {
                    // empty catch block
                }
            }
        }
        return result;
    }

    public void setDomDocumentClass(Class domDocumentClass) {
        this.domDocumentClass = domDocumentClass;
    }

    public void setDomDocumentClassName(String name) throws DocumentException {
        try {
            this.domDocumentClass = Class.forName(name, true, (class$org$dom4j$io$DOMWriter == null ? (class$org$dom4j$io$DOMWriter = DOMWriter.class$("org.dom4j.io.DOMWriter")) : class$org$dom4j$io$DOMWriter).getClassLoader());
        } catch (Exception e) {
            throw new DocumentException("Could not load the DOM Document class: " + name, e);
        }
    }

    public org.w3c.dom.Document write(Document document) throws DocumentException {
        if (document instanceof org.w3c.dom.Document) {
            return (org.w3c.dom.Document)((Object)document);
        }
        this.resetNamespaceStack();
        org.w3c.dom.Document domDocument = this.createDomDocument(document);
        this.appendDOMTree(domDocument, (Node)domDocument, document.content());
        this.namespaceStack.clear();
        return domDocument;
    }

    public org.w3c.dom.Document write(Document document, DOMImplementation domImpl) throws DocumentException {
        if (document instanceof org.w3c.dom.Document) {
            return (org.w3c.dom.Document)((Object)document);
        }
        this.resetNamespaceStack();
        org.w3c.dom.Document domDocument = this.createDomDocument(document, domImpl);
        this.appendDOMTree(domDocument, (Node)domDocument, document.content());
        this.namespaceStack.clear();
        return domDocument;
    }

    protected void appendDOMTree(org.w3c.dom.Document domDocument, Node domCurrent, List content) {
        int size = content.size();
        for (int i = 0; i < size; ++i) {
            Object object = content.get(i);
            if (object instanceof Element) {
                this.appendDOMTree(domDocument, domCurrent, (Element)object);
                continue;
            }
            if (object instanceof String) {
                this.appendDOMTree(domDocument, domCurrent, (String)object);
                continue;
            }
            if (object instanceof Text) {
                Text text = (Text)object;
                this.appendDOMTree(domDocument, domCurrent, text.getText());
                continue;
            }
            if (object instanceof CDATA) {
                this.appendDOMTree(domDocument, domCurrent, (CDATA)object);
                continue;
            }
            if (object instanceof Comment) {
                this.appendDOMTree(domDocument, domCurrent, (Comment)object);
                continue;
            }
            if (object instanceof Entity) {
                this.appendDOMTree(domDocument, domCurrent, (Entity)object);
                continue;
            }
            if (!(object instanceof ProcessingInstruction)) continue;
            this.appendDOMTree(domDocument, domCurrent, (ProcessingInstruction)object);
        }
    }

    protected void appendDOMTree(org.w3c.dom.Document domDocument, Node domCurrent, Element element) {
        int i;
        String elUri = element.getNamespaceURI();
        String elName = element.getQualifiedName();
        org.w3c.dom.Element domElement = domDocument.createElementNS(elUri, elName);
        int stackSize = this.namespaceStack.size();
        Namespace elementNamespace = element.getNamespace();
        if (this.isNamespaceDeclaration(elementNamespace)) {
            this.namespaceStack.push(elementNamespace);
            this.writeNamespace(domElement, elementNamespace);
        }
        List declaredNamespaces = element.declaredNamespaces();
        int size = declaredNamespaces.size();
        for (i = 0; i < size; ++i) {
            Namespace namespace = (Namespace)declaredNamespaces.get(i);
            if (!this.isNamespaceDeclaration(namespace)) continue;
            this.namespaceStack.push(namespace);
            this.writeNamespace(domElement, namespace);
        }
        size = element.attributeCount();
        for (i = 0; i < size; ++i) {
            Attribute attribute = element.attribute(i);
            String attUri = attribute.getNamespaceURI();
            String attName = attribute.getQualifiedName();
            String value = attribute.getValue();
            domElement.setAttributeNS(attUri, attName, value);
        }
        this.appendDOMTree(domDocument, (Node)domElement, element.content());
        domCurrent.appendChild(domElement);
        while (this.namespaceStack.size() > stackSize) {
            this.namespaceStack.pop();
        }
    }

    protected void appendDOMTree(org.w3c.dom.Document domDocument, Node domCurrent, CDATA cdata) {
        CDATASection domCDATA = domDocument.createCDATASection(cdata.getText());
        domCurrent.appendChild(domCDATA);
    }

    protected void appendDOMTree(org.w3c.dom.Document domDocument, Node domCurrent, Comment comment) {
        org.w3c.dom.Comment domComment = domDocument.createComment(comment.getText());
        domCurrent.appendChild(domComment);
    }

    protected void appendDOMTree(org.w3c.dom.Document domDocument, Node domCurrent, String text) {
        org.w3c.dom.Text domText = domDocument.createTextNode(text);
        domCurrent.appendChild(domText);
    }

    protected void appendDOMTree(org.w3c.dom.Document domDocument, Node domCurrent, Entity entity) {
        EntityReference domEntity = domDocument.createEntityReference(entity.getName());
        domCurrent.appendChild(domEntity);
    }

    protected void appendDOMTree(org.w3c.dom.Document domDoc, Node domCurrent, ProcessingInstruction pi) {
        org.w3c.dom.ProcessingInstruction domPI = domDoc.createProcessingInstruction(pi.getTarget(), pi.getText());
        domCurrent.appendChild(domPI);
    }

    protected void writeNamespace(org.w3c.dom.Element domElement, Namespace namespace) {
        String attributeName = this.attributeNameForNamespace(namespace);
        domElement.setAttribute(attributeName, namespace.getURI());
    }

    protected String attributeNameForNamespace(Namespace namespace) {
        String xmlns = "xmlns";
        String prefix = namespace.getPrefix();
        if (prefix.length() > 0) {
            return xmlns + ":" + prefix;
        }
        return xmlns;
    }

    protected org.w3c.dom.Document createDomDocument(Document document) throws DocumentException {
        org.w3c.dom.Document result = null;
        if (this.domDocumentClass != null) {
            try {
                result = (org.w3c.dom.Document)this.domDocumentClass.newInstance();
            } catch (Exception e) {
                throw new DocumentException("Could not instantiate an instance of DOM Document with class: " + this.domDocumentClass.getName(), e);
            }
        }
        result = this.createDomDocumentViaJAXP();
        if (result == null) {
            Class theClass = this.getDomDocumentClass();
            try {
                result = (org.w3c.dom.Document)theClass.newInstance();
            } catch (Exception e) {
                throw new DocumentException("Could not instantiate an instance of DOM Document with class: " + theClass.getName(), e);
            }
        }
        return result;
    }

    protected org.w3c.dom.Document createDomDocumentViaJAXP() throws DocumentException {
        try {
            return JAXPHelper.createDocument(false, true);
        } catch (Throwable e) {
            if (!loggedWarning) {
                loggedWarning = true;
                if (SAXHelper.isVerboseErrorReporting()) {
                    System.out.println("Warning: Caught exception attempting to use JAXP to create a W3C DOM document");
                    System.out.println("Warning: Exception was: " + e);
                    e.printStackTrace();
                } else {
                    System.out.println("Warning: Error occurred using JAXP to create a DOM document.");
                }
            }
            return null;
        }
    }

    protected org.w3c.dom.Document createDomDocument(Document document, DOMImplementation domImpl) throws DocumentException {
        String namespaceURI = null;
        String qualifiedName = null;
        DocumentType docType = null;
        return domImpl.createDocument(namespaceURI, qualifiedName, docType);
    }

    protected boolean isNamespaceDeclaration(Namespace ns) {
        String uri;
        return ns != null && ns != Namespace.NO_NAMESPACE && ns != Namespace.XML_NAMESPACE && (uri = ns.getURI()) != null && uri.length() > 0 && !this.namespaceStack.contains(ns);
    }

    protected void resetNamespaceStack() {
        this.namespaceStack.clear();
        this.namespaceStack.push(Namespace.XML_NAMESPACE);
    }

    static /* synthetic */ Class class$(String x0) {
        try {
            return Class.forName(x0);
        } catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }
}


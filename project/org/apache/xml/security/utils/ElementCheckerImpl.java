/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.utils.ElementChecker;
import org.apache.xml.security.utils.ElementProxy;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class ElementCheckerImpl
implements ElementChecker {
    public boolean isNamespaceElement(Node el, String type, String ns) {
        return el != null && ns == el.getNamespaceURI() && el.getLocalName().equals(type);
    }

    public static class EmptyChecker
    extends ElementCheckerImpl {
        public void guaranteeThatElementInCorrectSpace(ElementProxy expected, Element actual) throws XMLSecurityException {
        }
    }

    public static class FullChecker
    extends ElementCheckerImpl {
        public void guaranteeThatElementInCorrectSpace(ElementProxy expected, Element actual) throws XMLSecurityException {
            String expectedLocalname = expected.getBaseLocalName();
            String expectedNamespace = expected.getBaseNamespace();
            String localnameIS = actual.getLocalName();
            String namespaceIS = actual.getNamespaceURI();
            if (!expectedNamespace.equals(namespaceIS) || !expectedLocalname.equals(localnameIS)) {
                Object[] exArgs = new Object[]{namespaceIS + ":" + localnameIS, expectedNamespace + ":" + expectedLocalname};
                throw new XMLSecurityException("xml.WrongElement", exArgs);
            }
        }
    }

    public static class InternedNsChecker
    extends ElementCheckerImpl {
        public void guaranteeThatElementInCorrectSpace(ElementProxy expected, Element actual) throws XMLSecurityException {
            String expectedLocalname = expected.getBaseLocalName();
            String expectedNamespace = expected.getBaseNamespace();
            String localnameIS = actual.getLocalName();
            String namespaceIS = actual.getNamespaceURI();
            if (expectedNamespace != namespaceIS || !expectedLocalname.equals(localnameIS)) {
                Object[] exArgs = new Object[]{namespaceIS + ":" + localnameIS, expectedNamespace + ":" + expectedLocalname};
                throw new XMLSecurityException("xml.WrongElement", exArgs);
            }
        }
    }
}


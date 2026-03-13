/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.utils.ElementProxy;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public interface ElementChecker {
    public void guaranteeThatElementInCorrectSpace(ElementProxy var1, Element var2) throws XMLSecurityException;

    public boolean isNamespaceElement(Node var1, String var2, String var3);
}


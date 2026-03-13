/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.saxpath;

import org.jaxen.saxpath.SAXPathEventSource;
import org.jaxen.saxpath.SAXPathException;

public interface XPathReader
extends SAXPathEventSource {
    public void parse(String var1) throws SAXPathException;
}


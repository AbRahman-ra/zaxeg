/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils;

import org.apache.xml.security.utils.XPathAPI;
import org.apache.xml.security.utils.XPathFactory;
import org.apache.xml.security.utils.XalanXPathAPI;

public class XalanXPathFactory
extends XPathFactory {
    public XPathAPI newXPathAPI() {
        return new XalanXPathAPI();
    }
}


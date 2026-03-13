/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils;

import org.apache.xml.security.utils.JDKXPathAPI;
import org.apache.xml.security.utils.XPathAPI;
import org.apache.xml.security.utils.XPathFactory;

public class JDKXPathFactory
extends XPathFactory {
    public XPathAPI newXPathAPI() {
        return new JDKXPathAPI();
    }
}


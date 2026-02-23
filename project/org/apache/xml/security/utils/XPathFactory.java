/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils;

import org.apache.xml.security.utils.ClassLoaderUtils;
import org.apache.xml.security.utils.JDKXPathFactory;
import org.apache.xml.security.utils.XPathAPI;
import org.apache.xml.security.utils.XalanXPathAPI;
import org.apache.xml.security.utils.XalanXPathFactory;

public abstract class XPathFactory {
    private static boolean xalanInstalled;

    protected static synchronized boolean isXalanInstalled() {
        return xalanInstalled;
    }

    public static XPathFactory newInstance() {
        if (!XPathFactory.isXalanInstalled()) {
            return new JDKXPathFactory();
        }
        if (XalanXPathAPI.isInstalled()) {
            return new XalanXPathFactory();
        }
        return new JDKXPathFactory();
    }

    public abstract XPathAPI newXPathAPI();

    static {
        try {
            Class<?> funcTableClass = ClassLoaderUtils.loadClass("org.apache.xpath.compiler.FunctionTable", XPathFactory.class);
            if (funcTableClass != null) {
                xalanInstalled = true;
            }
        } catch (Exception exception) {
            // empty catch block
        }
    }
}


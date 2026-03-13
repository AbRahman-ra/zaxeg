/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.saxpath.helpers;

import org.jaxen.saxpath.SAXPathException;
import org.jaxen.saxpath.XPathReader;

public class XPathReaderFactory {
    public static final String DRIVER_PROPERTY = "org.saxpath.driver";
    protected static final String DEFAULT_DRIVER = "org.jaxen.saxpath.base.XPathReader";

    private XPathReaderFactory() {
    }

    public static XPathReader createReader() throws SAXPathException {
        String className = null;
        try {
            className = System.getProperty(DRIVER_PROPERTY);
        } catch (SecurityException securityException) {
            // empty catch block
        }
        if (className == null || className.length() == 0) {
            className = DEFAULT_DRIVER;
        }
        return XPathReaderFactory.createReader(className);
    }

    public static XPathReader createReader(String className) throws SAXPathException {
        Class<?> readerClass = null;
        XPathReader reader = null;
        try {
            readerClass = Class.forName(className, true, XPathReaderFactory.class.getClassLoader());
            if (!XPathReader.class.isAssignableFrom(readerClass)) {
                throw new SAXPathException("Class [" + className + "] does not implement the org.jaxen.saxpath.XPathReader interface.");
            }
        } catch (ClassNotFoundException e) {
            throw new SAXPathException(e);
        }
        try {
            reader = (XPathReader)readerClass.newInstance();
        } catch (IllegalAccessException e) {
            throw new SAXPathException(e);
        } catch (InstantiationException e) {
            throw new SAXPathException(e);
        }
        return reader;
    }
}


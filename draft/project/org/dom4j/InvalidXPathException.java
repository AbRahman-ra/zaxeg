/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j;

public class InvalidXPathException
extends IllegalArgumentException {
    private static final long serialVersionUID = 3257009869058881592L;

    public InvalidXPathException(String xpath) {
        super("Invalid XPath expression: " + xpath);
    }

    public InvalidXPathException(String xpath, String reason) {
        super("Invalid XPath expression: " + xpath + " " + reason);
    }

    public InvalidXPathException(String xpath, Throwable t) {
        super("Invalid XPath expression: '" + xpath + "'. Caused by: " + t.getMessage());
    }
}


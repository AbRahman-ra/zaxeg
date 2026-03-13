/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.pattern;

import org.jaxen.Context;
import org.jaxen.Navigator;
import org.jaxen.pattern.NodeTest;

public class NamespaceTest
extends NodeTest {
    private String prefix;
    private short nodeType;

    public NamespaceTest(String prefix, short nodeType) {
        if (prefix == null) {
            prefix = "";
        }
        this.prefix = prefix;
        this.nodeType = nodeType;
    }

    public boolean matches(Object node, Context context) {
        Navigator navigator = context.getNavigator();
        String uri = this.getURI(node, context);
        if (this.nodeType == 1) {
            return navigator.isElement(node) && uri.equals(navigator.getElementNamespaceUri(node));
        }
        if (this.nodeType == 2) {
            return navigator.isAttribute(node) && uri.equals(navigator.getAttributeNamespaceUri(node));
        }
        return false;
    }

    public double getPriority() {
        return -0.25;
    }

    public short getMatchType() {
        return this.nodeType;
    }

    public String getText() {
        return this.prefix + ":";
    }

    public String toString() {
        return super.toString() + "[ prefix: " + this.prefix + " type: " + this.nodeType + " ]";
    }

    protected String getURI(Object node, Context context) {
        String uri = context.getNavigator().translateNamespacePrefixToUri(this.prefix, node);
        if (uri == null) {
            uri = context.getContextSupport().translateNamespacePrefixToUri(this.prefix);
        }
        if (uri == null) {
            uri = "";
        }
        return uri;
    }
}


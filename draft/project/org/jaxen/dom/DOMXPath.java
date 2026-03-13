/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.dom;

import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.dom.DocumentNavigator;

public class DOMXPath
extends BaseXPath {
    private static final long serialVersionUID = 5551221776001439091L;

    public DOMXPath(String xpathExpr) throws JaxenException {
        super(xpathExpr, DocumentNavigator.getInstance());
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.jdom;

import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.jdom.DocumentNavigator;

public class JDOMXPath
extends BaseXPath {
    private static final long serialVersionUID = 6426091824802286928L;

    public JDOMXPath(String xpathExpr) throws JaxenException {
        super(xpathExpr, DocumentNavigator.getInstance());
    }
}


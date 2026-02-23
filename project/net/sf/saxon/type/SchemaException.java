/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;

public class SchemaException
extends XPathException {
    public SchemaException(String message, Location locator) {
        super(message, null, locator);
    }

    public SchemaException(String message) {
        super(message);
    }

    public SchemaException(Throwable exception) {
        super(exception);
    }

    public SchemaException(String message, Throwable exception) {
        super(message, exception);
    }
}


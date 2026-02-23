/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.dom;

import org.w3c.dom.DOMException;

public class DOMExceptionImpl
extends DOMException {
    public short code;
    public static final short INVALID_STATE_ERR = 11;
    public static final short SYNTAX_ERR = 12;
    public static final short INVALID_MODIFICATION_ERR = 13;
    public static final short NAMESPACE_ERR = 14;
    public static final short INVALID_ACCESS_ERR = 15;

    public DOMExceptionImpl(short code, String message) {
        super(code, message);
    }
}


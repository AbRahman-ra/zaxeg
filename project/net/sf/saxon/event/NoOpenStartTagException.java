/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.lib.StandardDiagnostics;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;

public class NoOpenStartTagException
extends XPathException {
    public static NoOpenStartTagException makeNoOpenStartTagException(int nodeKind, String name, HostLanguage hostLanguage, boolean parentIsDocument, Location startElementLocationId) {
        String errorCode;
        String message;
        String kind;
        if (parentIsDocument) {
            kind = nodeKind == 2 ? "an attribute" : "a namespace";
            message = "Cannot create " + kind + " node (" + name + ") whose parent is a document node";
            errorCode = hostLanguage == HostLanguage.XSLT ? "XTDE0420" : "XPTY0004";
        } else {
            kind = nodeKind == 2 ? "An attribute" : "A namespace";
            message = kind + " node (" + name + ") cannot be created after a child of the containing element";
            String string = errorCode = hostLanguage == HostLanguage.XSLT ? "XTDE0410" : "XQTY0024";
        }
        if (startElementLocationId != null && startElementLocationId.getLineNumber() != -1) {
            message = message + ". Most recent element start tag was output at line " + startElementLocationId.getLineNumber() + " of module " + new StandardDiagnostics().abbreviateLocationURI(startElementLocationId.getSystemId());
        }
        NoOpenStartTagException err = new NoOpenStartTagException(message);
        err.setErrorCode(errorCode);
        return err;
    }

    public NoOpenStartTagException(String message) {
        super(message);
    }
}


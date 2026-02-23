/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.event.CopyInformee;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Location;

public class LocationCopier
implements CopyInformee<Location> {
    private final boolean wholeDocument;

    public LocationCopier(boolean wholeDocument) {
        this.wholeDocument = wholeDocument;
    }

    @Override
    public Location notifyElementNode(NodeInfo element) {
        String systemId = this.wholeDocument ? element.getSystemId() : element.getBaseURI();
        int lineNumber = element.getLineNumber();
        int columnNumber = element.getColumnNumber();
        return new Loc(systemId, lineNumber, columnNumber);
    }
}


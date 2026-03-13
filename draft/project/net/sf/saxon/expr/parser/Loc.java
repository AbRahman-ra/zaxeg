/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.parser;

import javax.xml.transform.SourceLocator;
import net.sf.saxon.s9api.Location;
import org.xml.sax.Locator;

public class Loc
implements Location {
    private String systemId;
    private int lineNumber;
    private int columnNumber = -1;
    public static Loc NONE = new Loc(null, -1, -1);

    public Loc(SourceLocator loc) {
        this.systemId = loc.getSystemId();
        this.lineNumber = loc.getLineNumber();
        this.columnNumber = loc.getColumnNumber();
    }

    public static Loc makeFromSax(Locator loc) {
        return new Loc(loc.getSystemId(), loc.getLineNumber(), loc.getColumnNumber());
    }

    public Loc(String systemId, int lineNumber, int columnNumber) {
        this.systemId = systemId;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    @Override
    public String getSystemId() {
        return this.systemId;
    }

    @Override
    public String getPublicId() {
        return null;
    }

    @Override
    public int getLineNumber() {
        return this.lineNumber;
    }

    @Override
    public int getColumnNumber() {
        return this.columnNumber;
    }

    @Override
    public Location saveLocation() {
        return this;
    }

    public static boolean isUnknown(Location location) {
        return location == null || (location.getSystemId() == null || location.getSystemId().isEmpty()) && location.getLineNumber() == -1;
    }
}


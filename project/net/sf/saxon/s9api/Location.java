/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import javax.xml.transform.SourceLocator;
import org.xml.sax.Locator;

public interface Location
extends SourceLocator,
Locator {
    @Override
    public String getSystemId();

    @Override
    public String getPublicId();

    @Override
    public int getLineNumber();

    @Override
    public int getColumnNumber();

    public Location saveLocation();
}


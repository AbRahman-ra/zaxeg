/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trace;

import java.util.Iterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Location;

public interface InstructionInfo
extends Location {
    public StructuredQName getObjectName();

    public Object getProperty(String var1);

    public Iterator<String> getProperties();
}


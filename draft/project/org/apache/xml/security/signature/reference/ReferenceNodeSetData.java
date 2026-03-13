/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.signature.reference;

import java.util.Iterator;
import org.apache.xml.security.signature.reference.ReferenceData;
import org.w3c.dom.Node;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public interface ReferenceNodeSetData
extends ReferenceData {
    public Iterator<Node> iterator();
}


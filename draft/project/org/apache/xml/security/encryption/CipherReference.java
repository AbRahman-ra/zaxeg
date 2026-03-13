/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.encryption;

import org.apache.xml.security.encryption.Transforms;
import org.w3c.dom.Attr;

public interface CipherReference {
    public String getURI();

    public Attr getURIAsAttr();

    public Transforms getTransforms();

    public void setTransforms(Transforms var1);
}


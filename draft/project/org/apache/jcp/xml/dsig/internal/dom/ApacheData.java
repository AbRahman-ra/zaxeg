/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import javax.xml.crypto.Data;
import org.apache.xml.security.signature.XMLSignatureInput;

public interface ApacheData
extends Data {
    public XMLSignatureInput getXMLSignatureInput();
}


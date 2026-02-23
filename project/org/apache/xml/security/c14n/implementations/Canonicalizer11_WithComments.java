/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.c14n.implementations;

import org.apache.xml.security.c14n.implementations.Canonicalizer11;

public class Canonicalizer11_WithComments
extends Canonicalizer11 {
    public Canonicalizer11_WithComments() {
        super(true);
    }

    public final String engineGetURI() {
        return "http://www.w3.org/2006/12/xml-c14n11#WithComments";
    }

    public final boolean engineGetIncludeComments() {
        return true;
    }
}


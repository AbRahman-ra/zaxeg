/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.content.keyvalues;

import java.security.PublicKey;
import org.apache.xml.security.exceptions.XMLSecurityException;

public interface KeyValueContent {
    public PublicKey getPublicKey() throws XMLSecurityException;
}


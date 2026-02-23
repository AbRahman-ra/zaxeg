/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.cms;

import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.cms.CMSException;

interface CMSSecureReadable {
    public ASN1ObjectIdentifier getContentType();

    public InputStream getInputStream() throws IOException, CMSException;
}


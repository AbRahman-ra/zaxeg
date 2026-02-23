/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils;

import java.io.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.algorithms.SignatureAlgorithm;
import org.apache.xml.security.signature.XMLSignatureException;

public class SignerOutputStream
extends ByteArrayOutputStream {
    private static Log log = LogFactory.getLog(SignerOutputStream.class);
    final SignatureAlgorithm sa;

    public SignerOutputStream(SignatureAlgorithm sa) {
        this.sa = sa;
    }

    public void write(byte[] arg0) {
        try {
            this.sa.update(arg0);
        } catch (XMLSignatureException e) {
            throw new RuntimeException("" + e);
        }
    }

    public void write(int arg0) {
        try {
            this.sa.update((byte)arg0);
        } catch (XMLSignatureException e) {
            throw new RuntimeException("" + e);
        }
    }

    public void write(byte[] arg0, int arg1, int arg2) {
        if (log.isDebugEnabled()) {
            log.debug("Canonicalized SignedInfo:");
            StringBuilder sb = new StringBuilder(arg2);
            for (int i = arg1; i < arg1 + arg2; ++i) {
                sb.append((char)arg0[i]);
            }
            log.debug(sb.toString());
        }
        try {
            this.sa.update(arg0, arg1, arg2);
        } catch (XMLSignatureException e) {
            throw new RuntimeException("" + e);
        }
    }
}


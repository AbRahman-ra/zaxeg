/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils;

import java.io.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;

public class DigesterOutputStream
extends ByteArrayOutputStream {
    private static final Log log = LogFactory.getLog(DigesterOutputStream.class);
    final MessageDigestAlgorithm mda;

    public DigesterOutputStream(MessageDigestAlgorithm mda) {
        this.mda = mda;
    }

    public void write(byte[] arg0) {
        this.write(arg0, 0, arg0.length);
    }

    public void write(int arg0) {
        this.mda.update((byte)arg0);
    }

    public void write(byte[] arg0, int arg1, int arg2) {
        if (log.isDebugEnabled()) {
            log.debug("Pre-digested input:");
            StringBuilder sb = new StringBuilder(arg2);
            for (int i = arg1; i < arg1 + arg2; ++i) {
                sb.append((char)arg0[i]);
            }
            log.debug(sb.toString());
        }
        this.mda.update(arg0, arg1, arg2);
    }

    public byte[] getDigestValue() {
        return this.mda.digest();
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.UnsyncByteArrayOutputStream;

public class DigesterOutputStream
extends OutputStream {
    private static Log log = LogFactory.getLog(DigesterOutputStream.class);
    private final boolean buffer;
    private UnsyncByteArrayOutputStream bos;
    private final MessageDigest md;

    public DigesterOutputStream(MessageDigest md) {
        this(md, false);
    }

    public DigesterOutputStream(MessageDigest md, boolean buffer) {
        this.md = md;
        this.buffer = buffer;
        if (buffer) {
            this.bos = new UnsyncByteArrayOutputStream();
        }
    }

    public void write(int input) {
        if (this.buffer) {
            this.bos.write(input);
        }
        this.md.update((byte)input);
    }

    public void write(byte[] input, int offset, int len) {
        if (this.buffer) {
            this.bos.write(input, offset, len);
        }
        if (log.isDebugEnabled()) {
            log.debug("Pre-digested input:");
            StringBuilder sb = new StringBuilder(len);
            for (int i = offset; i < offset + len; ++i) {
                sb.append((char)input[i]);
            }
            log.debug(sb.toString());
        }
        this.md.update(input, offset, len);
    }

    public byte[] getDigestValue() {
        return this.md.digest();
    }

    public InputStream getInputStream() {
        if (this.buffer) {
            return new ByteArrayInputStream(this.bos.toByteArray());
        }
        return null;
    }

    public void close() throws IOException {
        if (this.buffer) {
            this.bos.close();
        }
    }
}


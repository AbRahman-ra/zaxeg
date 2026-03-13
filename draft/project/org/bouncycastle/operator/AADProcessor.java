/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.operator;

import java.io.OutputStream;

public interface AADProcessor {
    public OutputStream getAADStream();

    public byte[] getMAC();
}


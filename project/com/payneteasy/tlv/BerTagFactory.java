/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.payneteasy.tlv;

import com.payneteasy.tlv.BerTag;

public interface BerTagFactory {
    public BerTag createTag(byte[] var1, int var2, int var3);
}


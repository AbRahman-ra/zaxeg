/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.payneteasy.tlv;

import com.payneteasy.tlv.BerTag;
import com.payneteasy.tlv.BerTagFactory;

public class DefaultBerTagFactory
implements BerTagFactory {
    @Override
    public BerTag createTag(byte[] aBuf, int aOffset, int aLength) {
        return new BerTag(aBuf, aOffset, aLength);
    }
}


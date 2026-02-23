/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.payneteasy.tlv;

import com.payneteasy.tlv.BerTlv;
import com.payneteasy.tlv.BerTlvs;
import com.payneteasy.tlv.HexUtil;
import com.payneteasy.tlv.IBerTlvLogger;

public class BerTlvLogger {
    public static void log(String aPadding, BerTlvs aTlv, IBerTlvLogger aLogger) {
        for (BerTlv tlv : aTlv.getList()) {
            BerTlvLogger.log(aPadding, tlv, aLogger);
        }
    }

    public static void log(String aPadding, BerTlv aTlv, IBerTlvLogger aLogger) {
        if (aTlv == null) {
            aLogger.debug("{} is null", aPadding);
            return;
        }
        if (aTlv.isConstructed()) {
            aLogger.debug("{} [{}]", aPadding, HexUtil.toHexString(aTlv.getTag().bytes));
            for (BerTlv child : aTlv.getValues()) {
                BerTlvLogger.log(aPadding + "    ", child, aLogger);
            }
        } else {
            aLogger.debug("{} [{}] {}", aPadding, HexUtil.toHexString(aTlv.getTag().bytes), aTlv.getHexValue());
        }
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.payneteasy.tlv;

import com.payneteasy.tlv.BerTag;
import com.payneteasy.tlv.BerTlv;
import java.util.ArrayList;
import java.util.List;

public class BerTlvs {
    private final List<BerTlv> tlvs;

    protected BerTlvs(List<BerTlv> aTlvs) {
        this.tlvs = aTlvs;
    }

    public BerTlv find(BerTag aTag) {
        for (BerTlv tlv : this.tlvs) {
            BerTlv found = tlv.find(aTag);
            if (found == null) continue;
            return found;
        }
        return null;
    }

    public List<BerTlv> findAll(BerTag aTag) {
        ArrayList<BerTlv> list = new ArrayList<BerTlv>();
        for (BerTlv tlv : this.tlvs) {
            list.addAll(tlv.findAll(aTag));
        }
        return list;
    }

    public List<BerTlv> getList() {
        return this.tlvs;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        BerTlvs berTlvs = (BerTlvs)o;
        return this.tlvs != null ? this.tlvs.equals(berTlvs.tlvs) : berTlvs.tlvs == null;
    }

    public int hashCode() {
        return this.tlvs != null ? this.tlvs.hashCode() : 0;
    }

    public String toString() {
        return "BerTlvs{tlvs=" + this.tlvs + '}';
    }
}


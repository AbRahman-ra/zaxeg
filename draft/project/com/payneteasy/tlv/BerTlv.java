/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.payneteasy.tlv;

import com.payneteasy.tlv.BerTag;
import com.payneteasy.tlv.HexUtil;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BerTlv {
    private static final Charset ASCII = Charset.forName("US-ASCII");
    private final BerTag theTag;
    private final byte[] theValue;
    protected final List<BerTlv> theList;

    public BerTlv(BerTag aTag, List<BerTlv> aList) {
        this.theTag = aTag;
        this.theList = aList;
        this.theValue = null;
    }

    public BerTlv(BerTag aTag, byte[] aValue) {
        this.theTag = aTag;
        this.theValue = aValue;
        this.theList = null;
    }

    public BerTag getTag() {
        return this.theTag;
    }

    public boolean isPrimitive() {
        return !this.theTag.isConstructed();
    }

    public boolean isConstructed() {
        return this.theTag.isConstructed();
    }

    public boolean isTag(BerTag aTag) {
        return this.theTag.equals(aTag);
    }

    public BerTlv find(BerTag aTag) {
        if (aTag.equals(this.getTag())) {
            return this;
        }
        if (this.isConstructed()) {
            for (BerTlv tlv : this.theList) {
                BerTlv ret = tlv.find(aTag);
                if (ret == null) continue;
                return ret;
            }
            return null;
        }
        return null;
    }

    public List<BerTlv> findAll(BerTag aTag) {
        ArrayList<BerTlv> list = new ArrayList<BerTlv>();
        if (aTag.equals(this.getTag())) {
            list.add(this);
            return list;
        }
        if (this.isConstructed()) {
            for (BerTlv tlv : this.theList) {
                list.addAll(tlv.findAll(aTag));
            }
        }
        return list;
    }

    public String getHexValue() {
        if (this.isConstructed()) {
            throw new IllegalStateException("Tag is CONSTRUCTED " + HexUtil.toHexString(this.theTag.bytes));
        }
        return HexUtil.toHexString(this.theValue);
    }

    public String getTextValue() {
        return this.getTextValue(ASCII);
    }

    public String getTextValue(Charset aCharset) {
        if (this.isConstructed()) {
            throw new IllegalStateException("TLV is constructed");
        }
        return new String(this.theValue, aCharset);
    }

    public byte[] getBytesValue() {
        if (this.isConstructed()) {
            throw new IllegalStateException("TLV [" + this.theTag + "]is constructed");
        }
        return this.theValue;
    }

    public int getIntValue() {
        int i = 0;
        int j = 0;
        int number = 0;
        for (i = 0; i < this.theValue.length; ++i) {
            j = this.theValue[i];
            number = number * 256 + (j < 0 ? (j += 256) : j);
        }
        return number;
    }

    public List<BerTlv> getValues() {
        if (this.isPrimitive()) {
            throw new IllegalStateException("Tag is PRIMITIVE");
        }
        return this.theList;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        BerTlv berTlv = (BerTlv)o;
        if (this.theTag != null ? !this.theTag.equals(berTlv.theTag) : berTlv.theTag != null) {
            return false;
        }
        if (!Arrays.equals(this.theValue, berTlv.theValue)) {
            return false;
        }
        return this.theList != null ? this.theList.equals(berTlv.theList) : berTlv.theList == null;
    }

    public int hashCode() {
        int result = this.theTag != null ? this.theTag.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(this.theValue);
        result = 31 * result + (this.theList != null ? this.theList.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "BerTlv{theTag=" + this.theTag + ", theValue=" + Arrays.toString(this.theValue) + ", theList=" + this.theList + '}';
    }
}


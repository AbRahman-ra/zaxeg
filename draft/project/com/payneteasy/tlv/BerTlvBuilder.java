/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.payneteasy.tlv;

import com.payneteasy.tlv.BerTag;
import com.payneteasy.tlv.BerTlv;
import com.payneteasy.tlv.BerTlvParser;
import com.payneteasy.tlv.BerTlvs;
import com.payneteasy.tlv.HexUtil;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BerTlvBuilder {
    private static final Charset ASCII = Charset.forName("US-ASCII");
    private static final BigDecimal HUNDRED = new BigDecimal(100);
    private static final int DEFAULT_SIZE = 5120;
    private final int theBufferOffset;
    private int theLengthPosition;
    private int thePos;
    private final byte[] theBuffer;
    private final BerTag theTemplate;

    public BerTlvBuilder() {
        this((BerTag)null);
    }

    public BerTlvBuilder(BerTag aTemplate) {
        this(aTemplate, new byte[5120], 0, 5120);
    }

    public BerTlvBuilder(BerTlvs tlvs) {
        this((BerTag)null);
        for (BerTlv tlv : tlvs.getList()) {
            this.addBerTlv(tlv);
        }
    }

    public BerTlvBuilder(BerTag aTemplate, byte[] aBuffer, int aOffset, int aLength) {
        this.theTemplate = aTemplate;
        this.theBuffer = aBuffer;
        this.thePos = aOffset;
        this.theBufferOffset = aOffset;
    }

    public static BerTlvBuilder from(BerTlv aTlv) {
        return BerTlvBuilder.from(aTlv, 5120);
    }

    public static BerTlvBuilder from(BerTlv aTlv, int bufferSize) {
        if (aTlv.isConstructed()) {
            BerTlvBuilder builder = BerTlvBuilder.template(aTlv.getTag(), bufferSize);
            for (BerTlv tlv : aTlv.theList) {
                builder.addBerTlv(tlv);
            }
            return builder;
        }
        return new BerTlvBuilder(null, new byte[bufferSize], 0, bufferSize).addBerTlv(aTlv);
    }

    public static BerTlvBuilder template(BerTag aTemplate) {
        return BerTlvBuilder.template(aTemplate, 5120);
    }

    public static BerTlvBuilder template(BerTag aTemplate, int bufferSize) {
        return new BerTlvBuilder(aTemplate, new byte[bufferSize], 0, bufferSize);
    }

    public BerTlvBuilder addEmpty(BerTag aObject) {
        return this.addBytes(aObject, new byte[0], 0, 0);
    }

    public BerTlvBuilder addByte(BerTag aObject, byte aByte) {
        int len = aObject.bytes.length;
        System.arraycopy(aObject.bytes, 0, this.theBuffer, this.thePos, len);
        this.thePos += len;
        this.theBuffer[this.thePos++] = 1;
        this.theBuffer[this.thePos++] = aByte;
        return this;
    }

    public BerTlvBuilder addAmount(BerTag aObject, BigDecimal aAmount) {
        BigDecimal numeric = aAmount.multiply(HUNDRED);
        StringBuilder sb = new StringBuilder(12);
        sb.append(numeric.longValue());
        while (sb.length() < 12) {
            sb.insert(0, '0');
        }
        return this.addHex(aObject, sb.toString());
    }

    public BerTlvBuilder addDate(BerTag aObject, Date aDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyMMdd");
        return this.addHex(aObject, format.format(aDate));
    }

    public BerTlvBuilder addTime(BerTag aObject, Date aDate) {
        SimpleDateFormat format = new SimpleDateFormat("HHmmss");
        return this.addHex(aObject, format.format(aDate));
    }

    public int build() {
        if (this.theTemplate != null) {
            int tagLen = this.theTemplate.bytes.length;
            int lengthBytesCount = this.calculateBytesCountForLength(this.thePos);
            System.arraycopy(this.theBuffer, this.theBufferOffset, this.theBuffer, tagLen + lengthBytesCount, this.thePos);
            System.arraycopy(this.theTemplate.bytes, 0, this.theBuffer, this.theBufferOffset, this.theTemplate.bytes.length);
            this.fillLength(this.theBuffer, tagLen, this.thePos);
            this.thePos += tagLen + lengthBytesCount;
        }
        return this.thePos;
    }

    private void fillLength(byte[] aBuffer, int aOffset, int aLength) {
        if (aLength < 128) {
            aBuffer[aOffset] = (byte)aLength;
        } else if (aLength < 256) {
            aBuffer[aOffset] = -127;
            aBuffer[aOffset + 1] = (byte)aLength;
        } else if (aLength < 65536) {
            aBuffer[aOffset] = -126;
            aBuffer[aOffset + 1] = (byte)(aLength / 256);
            aBuffer[aOffset + 2] = (byte)(aLength % 256);
        } else if (aLength < 0x1000000) {
            aBuffer[aOffset] = -125;
            aBuffer[aOffset + 1] = (byte)(aLength / 65536);
            aBuffer[aOffset + 2] = (byte)(aLength / 256);
            aBuffer[aOffset + 3] = (byte)(aLength % 256);
        } else {
            throw new IllegalStateException("length [" + aLength + "] out of range (0x1000000)");
        }
    }

    private int calculateBytesCountForLength(int aLength) {
        int ret;
        if (aLength < 128) {
            ret = 1;
        } else if (aLength < 256) {
            ret = 2;
        } else if (aLength < 65536) {
            ret = 3;
        } else if (aLength < 0x1000000) {
            ret = 4;
        } else {
            throw new IllegalStateException("length [" + aLength + "] out of range (0x1000000)");
        }
        return ret;
    }

    public BerTlvBuilder addHex(BerTag aObject, String aHex) {
        byte[] buffer = HexUtil.parseHex(aHex);
        return this.addBytes(aObject, buffer, 0, buffer.length);
    }

    public BerTlvBuilder addBytes(BerTag aObject, byte[] aBytes) {
        return this.addBytes(aObject, aBytes, 0, aBytes.length);
    }

    public BerTlvBuilder addBytes(BerTag aTag, byte[] aBytes, int aFrom, int aLength) {
        int tagLength = aTag.bytes.length;
        int lengthBytesCount = this.calculateBytesCountForLength(aLength);
        System.arraycopy(aTag.bytes, 0, this.theBuffer, this.thePos, tagLength);
        this.thePos += tagLength;
        this.fillLength(this.theBuffer, this.thePos, aLength);
        this.thePos += lengthBytesCount;
        System.arraycopy(aBytes, aFrom, this.theBuffer, this.thePos, aLength);
        this.thePos += aLength;
        return this;
    }

    public BerTlvBuilder add(BerTlvBuilder aBuilder) {
        byte[] array = aBuilder.buildArray();
        System.arraycopy(array, 0, this.theBuffer, this.thePos, array.length);
        this.thePos += array.length;
        return this;
    }

    public BerTlvBuilder addBerTlv(BerTlv aTlv) {
        if (aTlv.isConstructed()) {
            return this.add(BerTlvBuilder.from(aTlv, this.theBuffer.length));
        }
        return this.addBytes(aTlv.getTag(), aTlv.getBytesValue());
    }

    public BerTlvBuilder addText(BerTag aTag, String aText) {
        return this.addText(aTag, aText, ASCII);
    }

    public BerTlvBuilder addText(BerTag aTag, String aText, Charset aCharset) {
        byte[] buffer = aText.getBytes(aCharset);
        return this.addBytes(aTag, buffer, 0, buffer.length);
    }

    public BerTlvBuilder addIntAsHex(BerTag aObject, int aCode, int aLength) {
        StringBuilder sb = new StringBuilder(aLength * 2);
        sb.append(aCode);
        while (sb.length() < aLength * 2) {
            sb.insert(0, '0');
        }
        return this.addHex(aObject, sb.toString());
    }

    public byte[] buildArray() {
        int count = this.build();
        byte[] buf = new byte[count];
        System.arraycopy(this.theBuffer, 0, buf, 0, count);
        return buf;
    }

    public BerTlv buildTlv() {
        int count = this.build();
        return new BerTlvParser().parseConstructed(this.theBuffer, this.theBufferOffset, count);
    }

    public BerTlvs buildTlvs() {
        int count = this.build();
        return new BerTlvParser().parse(this.theBuffer, this.theBufferOffset, count);
    }
}


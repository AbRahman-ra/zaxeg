/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.payneteasy.tlv;

import com.payneteasy.tlv.BerTag;
import com.payneteasy.tlv.BerTagFactory;
import com.payneteasy.tlv.BerTlv;
import com.payneteasy.tlv.BerTlvs;
import com.payneteasy.tlv.DefaultBerTagFactory;
import com.payneteasy.tlv.HexUtil;
import com.payneteasy.tlv.IBerTlvLogger;
import java.util.ArrayList;

public class BerTlvParser {
    private static final BerTagFactory DEFAULT_TAG_FACTORY = new DefaultBerTagFactory();
    private final BerTagFactory tagFactory;
    private final IBerTlvLogger log;
    private static final IBerTlvLogger EMPTY_LOGGER = new IBerTlvLogger(){

        @Override
        public boolean isDebugEnabled() {
            return false;
        }

        @Override
        public void debug(String aFormat, Object ... args) {
        }
    };

    public BerTlvParser() {
        this(DEFAULT_TAG_FACTORY, EMPTY_LOGGER);
    }

    public BerTlvParser(IBerTlvLogger aLogger) {
        this(DEFAULT_TAG_FACTORY, aLogger);
    }

    public BerTlvParser(BerTagFactory aTagFactory) {
        this(aTagFactory, EMPTY_LOGGER);
    }

    public BerTlvParser(BerTagFactory aTagFactory, IBerTlvLogger aLogger) {
        this.tagFactory = aTagFactory;
        this.log = aLogger;
    }

    public BerTlv parseConstructed(byte[] aBuf) {
        return this.parseConstructed(aBuf, 0, aBuf.length);
    }

    public BerTlv parseConstructed(byte[] aBuf, int aOffset, int aLen) {
        ParseResult result = this.parseWithResult(0, aBuf, aOffset, aLen);
        return result.tlv;
    }

    public BerTlvs parse(byte[] aBuf) {
        return this.parse(aBuf, 0, aBuf.length);
    }

    public BerTlvs parse(byte[] aBuf, int aOffset, int aLen) {
        ArrayList<BerTlv> tlvs = new ArrayList<BerTlv>();
        if (aLen == 0) {
            return new BerTlvs(tlvs);
        }
        int offset = aOffset;
        for (int i = 0; i < 100; ++i) {
            ParseResult result = this.parseWithResult(0, aBuf, offset, aLen - offset);
            tlvs.add(result.tlv);
            if (result.offset >= aOffset + aLen) break;
            offset = result.offset;
        }
        return new BerTlvs(tlvs);
    }

    private ParseResult parseWithResult(int aLevel, byte[] aBuf, int aOffset, int aLen) {
        String levelPadding = this.createLevelPadding(aLevel);
        if (aOffset + aLen > aBuf.length) {
            throw new IllegalStateException("Length is out of the range [offset=" + aOffset + ",  len=" + aLen + ", array.length=" + aBuf.length + ", level=" + aLevel + "]");
        }
        if (this.log.isDebugEnabled()) {
            this.log.debug("{}parseWithResult(level={}, offset={}, len={}, buf={})", levelPadding, aLevel, aOffset, aLen, HexUtil.toFormattedHexString(aBuf, aOffset, aLen));
        }
        int tagBytesCount = this.getTagBytesCount(aBuf, aOffset);
        BerTag tag = this.createTag(levelPadding, aBuf, aOffset, tagBytesCount);
        if (this.log.isDebugEnabled()) {
            this.log.debug("{}tag = {}, tagBytesCount={}, tagBuf={}", levelPadding, tag, tagBytesCount, HexUtil.toFormattedHexString(aBuf, aOffset, tagBytesCount));
        }
        int lengthBytesCount = BerTlvParser.getLengthBytesCount(aBuf, aOffset + tagBytesCount);
        int valueLength = this.getDataLength(aBuf, aOffset + tagBytesCount);
        if (this.log.isDebugEnabled()) {
            this.log.debug("{}lenBytesCount = {}, len = {}, lenBuf = {}", levelPadding, lengthBytesCount, valueLength, HexUtil.toFormattedHexString(aBuf, aOffset + tagBytesCount, lengthBytesCount));
        }
        if (tag.isConstructed()) {
            ArrayList<BerTlv> list = new ArrayList<BerTlv>();
            this.addChildren(aLevel, aBuf, aOffset + tagBytesCount + lengthBytesCount, levelPadding, lengthBytesCount, valueLength, list);
            int resultOffset = aOffset + tagBytesCount + lengthBytesCount + valueLength;
            if (this.log.isDebugEnabled()) {
                this.log.debug("{}returning constructed offset = {}", levelPadding, resultOffset);
            }
            return new ParseResult(new BerTlv(tag, list), resultOffset);
        }
        byte[] value = new byte[valueLength];
        System.arraycopy(aBuf, aOffset + tagBytesCount + lengthBytesCount, value, 0, valueLength);
        int resultOffset = aOffset + tagBytesCount + lengthBytesCount + valueLength;
        if (this.log.isDebugEnabled()) {
            this.log.debug("{}value = {}", levelPadding, HexUtil.toFormattedHexString(value));
            this.log.debug("{}returning primitive offset = {}", levelPadding, resultOffset);
        }
        return new ParseResult(new BerTlv(tag, value), resultOffset);
    }

    private void addChildren(int aLevel, byte[] aBuf, int aOffset, String levelPadding, int aDataBytesCount, int valueLength, ArrayList<BerTlv> list) {
        int startPosition = aOffset;
        int len = valueLength;
        while (startPosition < aOffset + valueLength) {
            ParseResult result = this.parseWithResult(aLevel + 1, aBuf, startPosition, len);
            list.add(result.tlv);
            startPosition = result.offset;
            len = aOffset + valueLength - startPosition;
            if (!this.log.isDebugEnabled()) continue;
            this.log.debug("{}level {}: adding {} with offset {}, startPosition={}, aDataBytesCount={}, valueLength={}", levelPadding, aLevel, result.tlv.getTag(), result.offset, startPosition, aDataBytesCount, valueLength);
        }
    }

    private String createLevelPadding(int aLevel) {
        if (!this.log.isDebugEnabled()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < aLevel * 4; ++i) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private BerTag createTag(String aLevelPadding, byte[] aBuf, int aOffset, int aLength) {
        if (this.log.isDebugEnabled()) {
            this.log.debug("{}Creating tag {}...", aLevelPadding, HexUtil.toFormattedHexString(aBuf, aOffset, aLength));
        }
        return this.tagFactory.createTag(aBuf, aOffset, aLength);
    }

    private int getTagBytesCount(byte[] aBuf, int aOffset) {
        if ((aBuf[aOffset] & 0x1F) == 31) {
            int len = 2;
            for (int i = aOffset + 1; i < aOffset + 10 && (aBuf[i] & 0x80) == 128; ++i) {
                ++len;
            }
            return len;
        }
        return 1;
    }

    private int getDataLength(byte[] aBuf, int aOffset) {
        int length = aBuf[aOffset] & 0xFF;
        if ((length & 0x80) == 128) {
            int numberOfBytes = length & 0x7F;
            if (numberOfBytes > 3) {
                throw new IllegalStateException(String.format("At position %d the len is more then 3 [%d]", aOffset, numberOfBytes));
            }
            length = 0;
            for (int i = aOffset + 1; i < aOffset + 1 + numberOfBytes; ++i) {
                length = length * 256 + (aBuf[i] & 0xFF);
            }
        }
        return length;
    }

    private static int getLengthBytesCount(byte[] aBuf, int aOffset) {
        int len = aBuf[aOffset] & 0xFF;
        if ((len & 0x80) == 128) {
            return 1 + (len & 0x7F);
        }
        return 1;
    }

    private static class ParseResult {
        private final BerTlv tlv;
        private final int offset;

        public ParseResult(BerTlv aTlv, int aOffset) {
            this.tlv = aTlv;
            this.offset = aOffset;
        }

        public String toString() {
            return "ParseResult{tlv=" + this.tlv + ", offset=" + this.offset + '}';
        }
    }
}


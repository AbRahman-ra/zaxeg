/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.math.ec;

import java.math.BigInteger;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.ECPointMap;
import org.bouncycastle.math.ec.PreCompCallback;
import org.bouncycastle.math.ec.PreCompInfo;
import org.bouncycastle.math.ec.WNafPreCompInfo;

public abstract class WNafUtil {
    public static final String PRECOMP_NAME = "bc_wnaf";
    private static final int[] DEFAULT_WINDOW_SIZE_CUTOFFS = new int[]{13, 41, 121, 337, 897, 2305};
    private static final int MAX_WIDTH = 16;
    private static final byte[] EMPTY_BYTES = new byte[0];
    private static final int[] EMPTY_INTS = new int[0];
    private static final ECPoint[] EMPTY_POINTS = new ECPoint[0];

    public static void configureBasepoint(ECPoint eCPoint) {
        ECCurve eCCurve = eCPoint.getCurve();
        if (null == eCCurve) {
            return;
        }
        BigInteger bigInteger = eCCurve.getOrder();
        int n = null == bigInteger ? eCCurve.getFieldSize() + 1 : bigInteger.bitLength();
        int n2 = Math.min(16, WNafUtil.getWindowSize(n) + 3);
        eCCurve.precompute(eCPoint, PRECOMP_NAME, new PreCompCallback(){

            public PreCompInfo precompute(PreCompInfo preCompInfo) {
                WNafPreCompInfo wNafPreCompInfo;
                WNafPreCompInfo wNafPreCompInfo2 = wNafPreCompInfo = preCompInfo instanceof WNafPreCompInfo ? (WNafPreCompInfo)preCompInfo : null;
                if (null != wNafPreCompInfo && wNafPreCompInfo.getConfWidth() == n2) {
                    wNafPreCompInfo.setPromotionCountdown(0);
                    return wNafPreCompInfo;
                }
                WNafPreCompInfo wNafPreCompInfo3 = new WNafPreCompInfo();
                wNafPreCompInfo3.setPromotionCountdown(0);
                wNafPreCompInfo3.setConfWidth(n2);
                if (null != wNafPreCompInfo) {
                    wNafPreCompInfo3.setPreComp(wNafPreCompInfo.getPreComp());
                    wNafPreCompInfo3.setPreCompNeg(wNafPreCompInfo.getPreCompNeg());
                    wNafPreCompInfo3.setTwice(wNafPreCompInfo.getTwice());
                    wNafPreCompInfo3.setWidth(wNafPreCompInfo.getWidth());
                }
                return wNafPreCompInfo3;
            }
        });
    }

    public static int[] generateCompactNaf(BigInteger bigInteger) {
        if (bigInteger.bitLength() >>> 16 != 0) {
            throw new IllegalArgumentException("'k' must have bitlength < 2^16");
        }
        if (bigInteger.signum() == 0) {
            return EMPTY_INTS;
        }
        BigInteger bigInteger2 = bigInteger.shiftLeft(1).add(bigInteger);
        int n = bigInteger2.bitLength();
        int[] nArray = new int[n >> 1];
        BigInteger bigInteger3 = bigInteger2.xor(bigInteger);
        int n2 = n - 1;
        int n3 = 0;
        int n4 = 0;
        for (int i = 1; i < n2; ++i) {
            if (!bigInteger3.testBit(i)) {
                ++n4;
                continue;
            }
            int n5 = bigInteger.testBit(i) ? -1 : 1;
            nArray[n3++] = n5 << 16 | n4;
            n4 = 1;
            ++i;
        }
        nArray[n3++] = 0x10000 | n4;
        if (nArray.length > n3) {
            nArray = WNafUtil.trim(nArray, n3);
        }
        return nArray;
    }

    public static int[] generateCompactWindowNaf(int n, BigInteger bigInteger) {
        if (n == 2) {
            return WNafUtil.generateCompactNaf(bigInteger);
        }
        if (n < 2 || n > 16) {
            throw new IllegalArgumentException("'width' must be in the range [2, 16]");
        }
        if (bigInteger.bitLength() >>> 16 != 0) {
            throw new IllegalArgumentException("'k' must have bitlength < 2^16");
        }
        if (bigInteger.signum() == 0) {
            return EMPTY_INTS;
        }
        int[] nArray = new int[bigInteger.bitLength() / n + 1];
        int n2 = 1 << n;
        int n3 = n2 - 1;
        int n4 = n2 >>> 1;
        boolean bl = false;
        int n5 = 0;
        int n6 = 0;
        while (n6 <= bigInteger.bitLength()) {
            if (bigInteger.testBit(n6) == bl) {
                ++n6;
                continue;
            }
            bigInteger = bigInteger.shiftRight(n6);
            int n7 = bigInteger.intValue() & n3;
            if (bl) {
                ++n7;
            }
            boolean bl2 = bl = (n7 & n4) != 0;
            if (bl) {
                n7 -= n2;
            }
            int n8 = n5 > 0 ? n6 - 1 : n6;
            nArray[n5++] = n7 << 16 | n8;
            n6 = n;
        }
        if (nArray.length > n5) {
            nArray = WNafUtil.trim(nArray, n5);
        }
        return nArray;
    }

    public static byte[] generateJSF(BigInteger bigInteger, BigInteger bigInteger2) {
        int n = Math.max(bigInteger.bitLength(), bigInteger2.bitLength()) + 1;
        byte[] byArray = new byte[n];
        BigInteger bigInteger3 = bigInteger;
        BigInteger bigInteger4 = bigInteger2;
        int n2 = 0;
        int n3 = 0;
        int n4 = 0;
        int n5 = 0;
        while (n3 | n4 || bigInteger3.bitLength() > n5 || bigInteger4.bitLength() > n5) {
            int n6;
            int n7 = (bigInteger3.intValue() >>> n5) + n3 & 7;
            int n8 = (bigInteger4.intValue() >>> n5) + n4 & 7;
            int n9 = n7 & 1;
            if (n9 != 0 && n7 + (n9 -= n7 & 2) == 4 && (n8 & 3) == 2) {
                n9 = -n9;
            }
            if ((n6 = n8 & 1) != 0 && n8 + (n6 -= n8 & 2) == 4 && (n7 & 3) == 2) {
                n6 = -n6;
            }
            if (n3 << 1 == 1 + n9) {
                n3 ^= 1;
            }
            if (n4 << 1 == 1 + n6) {
                n4 ^= 1;
            }
            if (++n5 == 30) {
                n5 = 0;
                bigInteger3 = bigInteger3.shiftRight(30);
                bigInteger4 = bigInteger4.shiftRight(30);
            }
            byArray[n2++] = (byte)(n9 << 4 | n6 & 0xF);
        }
        if (byArray.length > n2) {
            byArray = WNafUtil.trim(byArray, n2);
        }
        return byArray;
    }

    public static byte[] generateNaf(BigInteger bigInteger) {
        if (bigInteger.signum() == 0) {
            return EMPTY_BYTES;
        }
        BigInteger bigInteger2 = bigInteger.shiftLeft(1).add(bigInteger);
        int n = bigInteger2.bitLength() - 1;
        byte[] byArray = new byte[n];
        BigInteger bigInteger3 = bigInteger2.xor(bigInteger);
        for (int i = 1; i < n; ++i) {
            if (!bigInteger3.testBit(i)) continue;
            byArray[i - 1] = (byte)(bigInteger.testBit(i) ? -1 : 1);
            ++i;
        }
        byArray[n - 1] = 1;
        return byArray;
    }

    public static byte[] generateWindowNaf(int n, BigInteger bigInteger) {
        if (n == 2) {
            return WNafUtil.generateNaf(bigInteger);
        }
        if (n < 2 || n > 8) {
            throw new IllegalArgumentException("'width' must be in the range [2, 8]");
        }
        if (bigInteger.signum() == 0) {
            return EMPTY_BYTES;
        }
        byte[] byArray = new byte[bigInteger.bitLength() + 1];
        int n2 = 1 << n;
        int n3 = n2 - 1;
        int n4 = n2 >>> 1;
        boolean bl = false;
        int n5 = 0;
        int n6 = 0;
        while (n6 <= bigInteger.bitLength()) {
            if (bigInteger.testBit(n6) == bl) {
                ++n6;
                continue;
            }
            bigInteger = bigInteger.shiftRight(n6);
            int n7 = bigInteger.intValue() & n3;
            if (bl) {
                ++n7;
            }
            boolean bl2 = bl = (n7 & n4) != 0;
            if (bl) {
                n7 -= n2;
            }
            n5 += n5 > 0 ? n6 - 1 : n6;
            byArray[n5++] = (byte)n7;
            n6 = n;
        }
        if (byArray.length > n5) {
            byArray = WNafUtil.trim(byArray, n5);
        }
        return byArray;
    }

    public static int getNafWeight(BigInteger bigInteger) {
        if (bigInteger.signum() == 0) {
            return 0;
        }
        BigInteger bigInteger2 = bigInteger.shiftLeft(1).add(bigInteger);
        BigInteger bigInteger3 = bigInteger2.xor(bigInteger);
        return bigInteger3.bitCount();
    }

    public static WNafPreCompInfo getWNafPreCompInfo(ECPoint eCPoint) {
        return WNafUtil.getWNafPreCompInfo(eCPoint.getCurve().getPreCompInfo(eCPoint, PRECOMP_NAME));
    }

    public static WNafPreCompInfo getWNafPreCompInfo(PreCompInfo preCompInfo) {
        return preCompInfo instanceof WNafPreCompInfo ? (WNafPreCompInfo)preCompInfo : null;
    }

    public static int getWindowSize(int n) {
        return WNafUtil.getWindowSize(n, DEFAULT_WINDOW_SIZE_CUTOFFS, 16);
    }

    public static int getWindowSize(int n, int n2) {
        return WNafUtil.getWindowSize(n, DEFAULT_WINDOW_SIZE_CUTOFFS, n2);
    }

    public static int getWindowSize(int n, int[] nArray) {
        return WNafUtil.getWindowSize(n, nArray, 16);
    }

    public static int getWindowSize(int n, int[] nArray, int n2) {
        int n3;
        for (n3 = 0; n3 < nArray.length && n >= nArray[n3]; ++n3) {
        }
        return Math.max(2, Math.min(n2, n3 + 2));
    }

    public static WNafPreCompInfo precompute(ECPoint eCPoint, int n, boolean bl) {
        ECCurve eCCurve = eCPoint.getCurve();
        return (WNafPreCompInfo)eCCurve.precompute(eCPoint, PRECOMP_NAME, new PreCompCallback(){

            public PreCompInfo precompute(PreCompInfo preCompInfo) {
                int n5;
                int n2;
                int n3;
                int n4;
                WNafPreCompInfo wNafPreCompInfo = preCompInfo instanceof WNafPreCompInfo ? (WNafPreCompInfo)preCompInfo : null;
                if (this.checkExisting(wNafPreCompInfo, n4 = Math.max(2, Math.min(16, n)), n3 = 1 << n4 - 2, bl)) {
                    wNafPreCompInfo.decrementPromotionCountdown();
                    return wNafPreCompInfo;
                }
                WNafPreCompInfo wNafPreCompInfo2 = new WNafPreCompInfo();
                ECPoint[] eCPointArray = null;
                ECPoint[] eCPointArray2 = null;
                ECPoint eCPoint4 = null;
                if (null != wNafPreCompInfo) {
                    n2 = wNafPreCompInfo.decrementPromotionCountdown();
                    wNafPreCompInfo2.setPromotionCountdown(n2);
                    n5 = wNafPreCompInfo.getConfWidth();
                    wNafPreCompInfo2.setConfWidth(n5);
                    eCPointArray = wNafPreCompInfo.getPreComp();
                    eCPointArray2 = wNafPreCompInfo.getPreCompNeg();
                    eCPoint4 = wNafPreCompInfo.getTwice();
                }
                n4 = Math.min(16, Math.max(wNafPreCompInfo2.getConfWidth(), n4));
                n3 = 1 << n4 - 2;
                n2 = 0;
                if (null == eCPointArray) {
                    eCPointArray = EMPTY_POINTS;
                } else {
                    n2 = eCPointArray.length;
                }
                if (n2 < n3) {
                    eCPointArray = WNafUtil.resizeTable(eCPointArray, n3);
                    if (n3 == 1) {
                        eCPointArray[0] = eCPoint.normalize();
                    } else {
                        n5 = n2;
                        if (n5 == 0) {
                            eCPointArray[0] = eCPoint;
                            n5 = 1;
                        }
                        ECFieldElement eCFieldElement = null;
                        if (n3 == 2) {
                            eCPointArray[1] = eCPoint.threeTimes();
                        } else {
                            ECPoint eCPoint2 = eCPoint4;
                            ECPoint eCPoint3 = eCPointArray[n5 - 1];
                            if (null == eCPoint2 && !(eCPoint4 = (eCPoint2 = eCPointArray[0].twice())).isInfinity() && ECAlgorithms.isFpCurve(eCCurve) && eCCurve.getFieldSize() >= 64) {
                                switch (eCCurve.getCoordinateSystem()) {
                                    case 2: 
                                    case 3: 
                                    case 4: {
                                        eCFieldElement = eCPoint4.getZCoord(0);
                                        eCPoint2 = eCCurve.createPoint(eCPoint4.getXCoord().toBigInteger(), eCPoint4.getYCoord().toBigInteger());
                                        ECFieldElement eCFieldElement2 = eCFieldElement.square();
                                        ECFieldElement eCFieldElement3 = eCFieldElement2.multiply(eCFieldElement);
                                        eCPoint3 = eCPoint3.scaleX(eCFieldElement2).scaleY(eCFieldElement3);
                                        if (n2 != 0) break;
                                        eCPointArray[0] = eCPoint3;
                                        break;
                                    }
                                }
                            }
                            while (n5 < n3) {
                                eCPointArray[n5++] = eCPoint3 = eCPoint3.add(eCPoint2);
                            }
                        }
                        eCCurve.normalizeAll(eCPointArray, n2, n3 - n2, eCFieldElement);
                    }
                }
                if (bl) {
                    if (null == eCPointArray2) {
                        n5 = 0;
                        eCPointArray2 = new ECPoint[n3];
                    } else {
                        n5 = eCPointArray2.length;
                        if (n5 < n3) {
                            eCPointArray2 = WNafUtil.resizeTable(eCPointArray2, n3);
                        }
                    }
                    while (n5 < n3) {
                        eCPointArray2[n5] = eCPointArray[n5].negate();
                        ++n5;
                    }
                }
                wNafPreCompInfo2.setPreComp(eCPointArray);
                wNafPreCompInfo2.setPreCompNeg(eCPointArray2);
                wNafPreCompInfo2.setTwice(eCPoint4);
                wNafPreCompInfo2.setWidth(n4);
                return wNafPreCompInfo2;
            }

            private boolean checkExisting(WNafPreCompInfo wNafPreCompInfo, int n3, int n2, boolean bl2) {
                return null != wNafPreCompInfo && wNafPreCompInfo.getWidth() >= Math.max(wNafPreCompInfo.getConfWidth(), n3) && this.checkTable(wNafPreCompInfo.getPreComp(), n2) && (!bl2 || this.checkTable(wNafPreCompInfo.getPreCompNeg(), n2));
            }

            private boolean checkTable(ECPoint[] eCPointArray, int n2) {
                return null != eCPointArray && eCPointArray.length >= n2;
            }
        });
    }

    public static WNafPreCompInfo precomputeWithPointMap(ECPoint eCPoint, ECPointMap eCPointMap, WNafPreCompInfo wNafPreCompInfo, boolean bl) {
        ECCurve eCCurve = eCPoint.getCurve();
        return (WNafPreCompInfo)eCCurve.precompute(eCPoint, PRECOMP_NAME, new PreCompCallback(){

            public PreCompInfo precompute(PreCompInfo preCompInfo) {
                Object object;
                int n;
                int n2;
                WNafPreCompInfo wNafPreCompInfo3 = preCompInfo instanceof WNafPreCompInfo ? (WNafPreCompInfo)preCompInfo : null;
                if (this.checkExisting(wNafPreCompInfo3, n2 = wNafPreCompInfo.getWidth(), n = wNafPreCompInfo.getPreComp().length, bl)) {
                    wNafPreCompInfo3.decrementPromotionCountdown();
                    return wNafPreCompInfo3;
                }
                WNafPreCompInfo wNafPreCompInfo2 = new WNafPreCompInfo();
                wNafPreCompInfo2.setPromotionCountdown(wNafPreCompInfo.getPromotionCountdown());
                ECPoint eCPoint = wNafPreCompInfo.getTwice();
                if (null != eCPoint) {
                    object = eCPointMap.map(eCPoint);
                    wNafPreCompInfo2.setTwice((ECPoint)object);
                }
                object = wNafPreCompInfo.getPreComp();
                ECPoint[] eCPointArray = new ECPoint[((ECPoint[])object).length];
                for (int i = 0; i < ((ECPoint[])object).length; ++i) {
                    eCPointArray[i] = eCPointMap.map(object[i]);
                }
                wNafPreCompInfo2.setPreComp(eCPointArray);
                wNafPreCompInfo2.setWidth(n2);
                if (bl) {
                    ECPoint[] eCPointArray2 = new ECPoint[eCPointArray.length];
                    for (int i = 0; i < eCPointArray2.length; ++i) {
                        eCPointArray2[i] = eCPointArray[i].negate();
                    }
                    wNafPreCompInfo2.setPreCompNeg(eCPointArray2);
                }
                return wNafPreCompInfo2;
            }

            private boolean checkExisting(WNafPreCompInfo wNafPreCompInfo2, int n, int n2, boolean bl2) {
                return null != wNafPreCompInfo2 && wNafPreCompInfo2.getWidth() >= n && this.checkTable(wNafPreCompInfo2.getPreComp(), n2) && (!bl2 || this.checkTable(wNafPreCompInfo2.getPreCompNeg(), n2));
            }

            private boolean checkTable(ECPoint[] eCPointArray, int n) {
                return null != eCPointArray && eCPointArray.length >= n;
            }
        });
    }

    private static byte[] trim(byte[] byArray, int n) {
        byte[] byArray2 = new byte[n];
        System.arraycopy(byArray, 0, byArray2, 0, byArray2.length);
        return byArray2;
    }

    private static int[] trim(int[] nArray, int n) {
        int[] nArray2 = new int[n];
        System.arraycopy(nArray, 0, nArray2, 0, nArray2.length);
        return nArray2;
    }

    private static ECPoint[] resizeTable(ECPoint[] eCPointArray, int n) {
        ECPoint[] eCPointArray2 = new ECPoint[n];
        System.arraycopy(eCPointArray, 0, eCPointArray2, 0, eCPointArray.length);
        return eCPointArray2;
    }
}


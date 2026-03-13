/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.tsp.ers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.bouncycastle.asn1.tsp.PartialHashtree;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.tsp.ers.ByteArrayComparator;
import org.bouncycastle.tsp.ers.ERSData;
import org.bouncycastle.tsp.ers.ExpUtil;
import org.bouncycastle.tsp.ers.SortedHashList;
import org.bouncycastle.util.io.Streams;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
class ERSUtil {
    private static final Comparator<byte[]> hashComp = new ByteArrayComparator();

    private ERSUtil() {
    }

    static byte[] calculateDigest(DigestCalculator digestCalculator, byte[] byArray) {
        try {
            OutputStream outputStream = digestCalculator.getOutputStream();
            outputStream.write(byArray);
            outputStream.close();
            return digestCalculator.getDigest();
        } catch (IOException iOException) {
            throw ExpUtil.createIllegalState("unable to calculate hash: " + iOException.getMessage(), iOException);
        }
    }

    static byte[] calculateBranchHash(DigestCalculator digestCalculator, byte[] byArray, byte[] byArray2) {
        if (hashComp.compare(byArray, byArray2) <= 0) {
            return ERSUtil.calculateDigest(digestCalculator, byArray, byArray2);
        }
        return ERSUtil.calculateDigest(digestCalculator, byArray2, byArray);
    }

    static byte[] calculateBranchHash(DigestCalculator digestCalculator, byte[][] byArray) {
        if (byArray.length == 2) {
            return ERSUtil.calculateBranchHash(digestCalculator, byArray[0], byArray[1]);
        }
        return ERSUtil.calculateDigest(digestCalculator, ERSUtil.buildHashList(byArray).iterator());
    }

    static byte[] calculateDigest(DigestCalculator digestCalculator, byte[] byArray, byte[] byArray2) {
        try {
            OutputStream outputStream = digestCalculator.getOutputStream();
            outputStream.write(byArray);
            outputStream.write(byArray2);
            outputStream.close();
            return digestCalculator.getDigest();
        } catch (IOException iOException) {
            throw ExpUtil.createIllegalState("unable to calculate hash: " + iOException.getMessage(), iOException);
        }
    }

    static byte[] calculateDigest(DigestCalculator digestCalculator, Iterator<byte[]> iterator) {
        try {
            OutputStream outputStream = digestCalculator.getOutputStream();
            while (iterator.hasNext()) {
                outputStream.write(iterator.next());
            }
            outputStream.close();
            return digestCalculator.getDigest();
        } catch (IOException iOException) {
            throw ExpUtil.createIllegalState("unable to calculate hash: " + iOException.getMessage(), iOException);
        }
    }

    static byte[] calculateDigest(DigestCalculator digestCalculator, InputStream inputStream) {
        try {
            OutputStream outputStream = digestCalculator.getOutputStream();
            Streams.pipeAll(inputStream, outputStream);
            outputStream.close();
            return digestCalculator.getDigest();
        } catch (IOException iOException) {
            throw ExpUtil.createIllegalState("unable to calculate hash: " + iOException.getMessage(), iOException);
        }
    }

    static byte[] computeNodeHash(DigestCalculator digestCalculator, PartialHashtree partialHashtree) {
        byte[][] byArray = partialHashtree.getValues();
        if (byArray.length > 1) {
            return ERSUtil.calculateDigest(digestCalculator, ERSUtil.buildHashList(byArray).iterator());
        }
        return byArray[0];
    }

    static List<byte[]> buildHashList(byte[][] byArray) {
        SortedHashList sortedHashList = new SortedHashList();
        for (int i = 0; i != byArray.length; ++i) {
            sortedHashList.add(byArray[i]);
        }
        return sortedHashList.toList();
    }

    static List<byte[]> buildHashList(DigestCalculator digestCalculator, List<ERSData> list) {
        SortedHashList sortedHashList = new SortedHashList();
        for (int i = 0; i != list.size(); ++i) {
            sortedHashList.add(list.get(i).getHash(digestCalculator));
        }
        return sortedHashList.toList();
    }
}


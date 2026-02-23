/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.tsp.ers;

import java.util.ArrayList;
import org.bouncycastle.asn1.tsp.PartialHashtree;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.tsp.ers.ERSRootNodeCalculator;
import org.bouncycastle.tsp.ers.ERSUtil;

public class BinaryTreeRootCalculator
implements ERSRootNodeCalculator {
    public byte[] computeRootHash(DigestCalculator digestCalculator, PartialHashtree[] partialHashtreeArray) {
        ArrayList<Object> arrayList;
        ArrayList<byte[]> arrayList2 = new ArrayList<byte[]>();
        for (int i = 0; i <= partialHashtreeArray.length - 2; i += 2) {
            byte[] byArray = ERSUtil.computeNodeHash(digestCalculator, partialHashtreeArray[i]);
            byte[] byArray2 = ERSUtil.computeNodeHash(digestCalculator, partialHashtreeArray[i + 1]);
            arrayList2.add(ERSUtil.calculateBranchHash(digestCalculator, byArray, byArray2));
        }
        if (partialHashtreeArray.length % 2 == 1) {
            arrayList2.add(ERSUtil.computeNodeHash(digestCalculator, partialHashtreeArray[partialHashtreeArray.length - 1]));
        }
        do {
            arrayList = new ArrayList<Object>((arrayList2.size() + 1) / 2);
            for (int i = 0; i <= arrayList2.size() - 2; i += 2) {
                arrayList.add(ERSUtil.calculateBranchHash(digestCalculator, (byte[])arrayList2.get(i), (byte[])arrayList2.get(i + 1)));
            }
            if (arrayList2.size() % 2 != 1) continue;
            arrayList.add(arrayList2.get(arrayList2.size() - 1));
        } while ((arrayList2 = arrayList).size() > 1);
        return (byte[])arrayList2.get(0);
    }
}


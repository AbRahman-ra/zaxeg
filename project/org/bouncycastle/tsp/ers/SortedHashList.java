/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.tsp.ers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.bouncycastle.tsp.ers.ByteArrayComparator;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class SortedHashList {
    private static final Comparator<byte[]> hashComp = new ByteArrayComparator();
    private final LinkedList<byte[]> baseList = new LinkedList();

    public void add(byte[] byArray) {
        if (this.baseList.size() == 0) {
            this.baseList.addFirst(byArray);
        } else if (hashComp.compare(byArray, this.baseList.get(0)) < 0) {
            this.baseList.addFirst(byArray);
        } else {
            int n;
            for (n = 1; n < this.baseList.size() && hashComp.compare(this.baseList.get(n), byArray) <= 0; ++n) {
            }
            if (n == this.baseList.size()) {
                this.baseList.add(byArray);
            } else {
                this.baseList.add(n, byArray);
            }
        }
    }

    public List<byte[]> toList() {
        return new ArrayList<byte[]>(this.baseList);
    }
}


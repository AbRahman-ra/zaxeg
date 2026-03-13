/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize.codenorm;

import java.util.BitSet;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.z.IntHashMap;
import net.sf.saxon.z.IntToIntMap;

public class NormalizerData {
    static final String copyright = "Copyright (c) 1998-1999 Unicode, Inc.";
    public static final int NOT_COMPOSITE = 65535;
    private IntToIntMap canonicalClass;
    private IntHashMap decompose;
    private IntToIntMap compose;
    private BitSet isCompatibility;
    private BitSet isExcluded;

    public int getCanonicalClass(int ch) {
        return this.canonicalClass.get(ch);
    }

    public char getPairwiseComposition(int first, int second) {
        if (first < 0 || first > 0x10FFFF || second < 0 || second > 0x10FFFF) {
            return '\uffff';
        }
        return (char)this.compose.get(first << 16 | second);
    }

    public void getRecursiveDecomposition(boolean canonical, int ch, FastStringBuffer buffer) {
        String decomp = (String)this.decompose.get(ch);
        if (!(decomp == null || canonical && this.isCompatibility.get(ch))) {
            for (int i = 0; i < decomp.length(); ++i) {
                this.getRecursiveDecomposition(canonical, decomp.charAt(i), buffer);
            }
        } else {
            buffer.appendWideChar(ch);
        }
    }

    NormalizerData(IntToIntMap canonicalClass, IntHashMap decompose, IntToIntMap compose, BitSet isCompatibility, BitSet isExcluded) {
        this.canonicalClass = canonicalClass;
        this.decompose = decompose;
        this.compose = compose;
        this.isCompatibility = isCompatibility;
        this.isExcluded = isExcluded;
    }

    boolean getExcluded(char ch) {
        return this.isExcluded.get(ch);
    }

    String getRawDecompositionMapping(char ch) {
        return (String)this.decompose.get(ch);
    }
}


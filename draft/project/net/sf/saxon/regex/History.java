/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

import java.util.HashMap;
import java.util.Map;
import net.sf.saxon.regex.Operation;
import net.sf.saxon.z.IntHashSet;
import net.sf.saxon.z.IntSet;

public class History {
    private Map<Operation, IntSet> zeroLengthMatches = new HashMap<Operation, IntSet>();

    public boolean isDuplicateZeroLengthMatch(Operation op, int position) {
        IntSet positions = this.zeroLengthMatches.get(op);
        if (positions == null) {
            positions = new IntHashSet(position);
            positions.add(position);
            this.zeroLengthMatches.put(op, positions);
            return false;
        }
        return !positions.add(position);
    }
}


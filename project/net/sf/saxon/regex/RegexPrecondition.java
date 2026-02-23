/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

import net.sf.saxon.regex.Operation;

public class RegexPrecondition {
    public Operation operation;
    public int fixedPosition;
    public int minPosition;

    public RegexPrecondition(Operation op, int fixedPos, int minPos) {
        this.operation = op;
        this.fixedPosition = fixedPos;
        this.minPosition = minPos;
    }
}


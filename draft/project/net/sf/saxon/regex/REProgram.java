/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import net.sf.saxon.regex.Operation;
import net.sf.saxon.regex.REFlags;
import net.sf.saxon.regex.RegexPrecondition;
import net.sf.saxon.regex.UnicodeString;

public class REProgram {
    static final int OPT_HASBACKREFS = 1;
    static final int OPT_HASBOL = 2;
    Operation operation;
    REFlags flags;
    UnicodeString prefix;
    IntPredicate initialCharClass;
    List<RegexPrecondition> preconditions = new ArrayList<RegexPrecondition>();
    int minimumLength = 0;
    int fixedLength = -1;
    int optimizationFlags;
    int maxParens = -1;
    int backtrackingLimit = -1;

    public REProgram(Operation operation, int parens, REFlags flags) {
        this.flags = flags;
        this.setOperation(operation);
        this.maxParens = parens;
    }

    private void setOperation(Operation operation) {
        this.operation = operation;
        this.optimizationFlags = 0;
        this.prefix = null;
        this.operation = operation.optimize(this, this.flags);
        if (operation instanceof Operation.OpSequence) {
            Operation first = ((Operation.OpSequence)operation).getOperations().get(0);
            if (first instanceof Operation.OpBOL) {
                this.optimizationFlags |= 2;
            } else if (first instanceof Operation.OpAtom) {
                this.prefix = ((Operation.OpAtom)first).getAtom();
            } else if (first instanceof Operation.OpCharClass) {
                this.initialCharClass = ((Operation.OpCharClass)first).getPredicate();
            }
            this.addPrecondition(operation, -1, 0);
        }
        this.minimumLength = operation.getMinimumMatchLength();
        this.fixedLength = operation.getMatchLength();
    }

    public void setBacktrackingLimit(int limit) {
        this.backtrackingLimit = limit;
    }

    public int getBacktrackingLimit() {
        return this.backtrackingLimit;
    }

    private void addPrecondition(Operation op, int fixedPosition, int minPosition) {
        if (op instanceof Operation.OpAtom || op instanceof Operation.OpCharClass) {
            this.preconditions.add(new RegexPrecondition(op, fixedPosition, minPosition));
        } else if (op instanceof Operation.OpRepeat && ((Operation.OpRepeat)op).min >= 1) {
            Operation.OpRepeat parent = (Operation.OpRepeat)op;
            Operation child = parent.op;
            if (child instanceof Operation.OpAtom || child instanceof Operation.OpCharClass) {
                if (parent.min == 1) {
                    this.preconditions.add(new RegexPrecondition(parent, fixedPosition, minPosition));
                } else {
                    Operation.OpRepeat parent2 = new Operation.OpRepeat(child, parent.min, parent.min, true);
                    this.preconditions.add(new RegexPrecondition(parent2, fixedPosition, minPosition));
                }
            } else {
                this.addPrecondition(child, fixedPosition, minPosition);
            }
        } else if (op instanceof Operation.OpCapture) {
            this.addPrecondition(((Operation.OpCapture)op).childOp, fixedPosition, minPosition);
        } else if (op instanceof Operation.OpSequence) {
            int fp = fixedPosition;
            int mp = minPosition;
            for (Operation o : ((Operation.OpSequence)op).getOperations()) {
                if (o instanceof Operation.OpBOL) {
                    fp = 0;
                }
                this.addPrecondition(o, fp, mp);
                fp = fp != -1 && o.getMatchLength() != -1 ? (fp += o.getMatchLength()) : -1;
                mp += o.getMinimumMatchLength();
            }
        }
    }

    public boolean isNullable() {
        int m = this.operation.matchesEmptyString();
        return (m & 7) != 0;
    }

    public UnicodeString getPrefix() {
        return this.prefix;
    }
}


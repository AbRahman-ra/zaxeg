/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.function.IntPredicate;
import net.sf.saxon.expr.sort.EmptyIntIterator;
import net.sf.saxon.regex.CaseVariants;
import net.sf.saxon.regex.RECompiler;
import net.sf.saxon.regex.REFlags;
import net.sf.saxon.regex.REMatcher;
import net.sf.saxon.regex.REProgram;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.regex.charclass.CharacterClass;
import net.sf.saxon.regex.charclass.EmptyCharacterClass;
import net.sf.saxon.regex.charclass.IntSetCharacterClass;
import net.sf.saxon.regex.charclass.SingletonCharacterClass;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.z.IntHashSet;
import net.sf.saxon.z.IntIterator;
import net.sf.saxon.z.IntRangeSet;
import net.sf.saxon.z.IntSet;
import net.sf.saxon.z.IntSetPredicate;
import net.sf.saxon.z.IntSingletonIterator;
import net.sf.saxon.z.IntSingletonSet;
import net.sf.saxon.z.IntStepIterator;

public abstract class Operation {
    static final int MATCHES_ZLS_AT_START = 1;
    static final int MATCHES_ZLS_AT_END = 2;
    static final int MATCHES_ZLS_ANYWHERE = 7;
    static final int MATCHES_ZLS_NEVER = 1024;

    public abstract IntIterator iterateMatches(REMatcher var1, int var2);

    public int getMatchLength() {
        return -1;
    }

    public int getMinimumMatchLength() {
        int fixed = this.getMatchLength();
        return fixed < 0 ? 0 : fixed;
    }

    public abstract int matchesEmptyString();

    public boolean containsCapturingExpressions() {
        return false;
    }

    public CharacterClass getInitialCharacterClass(boolean caseBlind) {
        return EmptyCharacterClass.getComplement();
    }

    public Operation optimize(REProgram program, REFlags flags) {
        return this;
    }

    public abstract String display();

    private static class ForceProgressIterator
    implements IntIterator {
        private IntIterator base;
        int countZeroLength = 0;
        int currentPos = -1;

        ForceProgressIterator(IntIterator base) {
            this.base = base;
        }

        @Override
        public boolean hasNext() {
            return this.countZeroLength <= 3 && this.base.hasNext();
        }

        @Override
        public int next() {
            int p = this.base.next();
            if (p == this.currentPos) {
                ++this.countZeroLength;
            } else {
                this.countZeroLength = 0;
                this.currentPos = p;
            }
            return p;
        }
    }

    public static class OpTrace
    extends Operation {
        private Operation base;
        private static int counter = 0;

        OpTrace(Operation base) {
            this.base = base;
        }

        @Override
        public IntIterator iterateMatches(REMatcher matcher, int position) {
            final IntIterator baseIter = this.base.iterateMatches(matcher, position);
            final int iterNr = counter++;
            String clName = baseIter.getClass().getName();
            int lastDot = clName.lastIndexOf(".");
            String iterName = clName.substring(lastDot + 1);
            System.err.println("Iterating over " + this.base.getClass().getSimpleName() + " " + this.base.display() + " at position " + position + " returning " + iterName + " " + iterNr);
            return new IntIterator(){

                @Override
                public boolean hasNext() {
                    boolean b = baseIter.hasNext();
                    System.err.println("IntIterator " + iterNr + " hasNext() = " + b);
                    return b;
                }

                @Override
                public int next() {
                    int n = baseIter.next();
                    System.err.println("IntIterator " + iterNr + " next() = " + n);
                    return n;
                }
            };
        }

        @Override
        public int getMatchLength() {
            return this.base.getMatchLength();
        }

        @Override
        public int matchesEmptyString() {
            return this.base.matchesEmptyString();
        }

        @Override
        public Operation optimize(REProgram program, REFlags flags) {
            this.base = this.base.optimize(program, flags);
            return this;
        }

        @Override
        public String display() {
            return this.base.display();
        }
    }

    public static class OpNothing
    extends Operation {
        @Override
        public IntIterator iterateMatches(REMatcher matcher, int position) {
            return new IntSingletonIterator(position);
        }

        @Override
        public int matchesEmptyString() {
            return 7;
        }

        @Override
        public int getMatchLength() {
            return 0;
        }

        @Override
        public String display() {
            return "()";
        }
    }

    public static class OpBackReference
    extends Operation {
        int groupNr;

        OpBackReference(int groupNr) {
            this.groupNr = groupNr;
        }

        @Override
        public int matchesEmptyString() {
            return 0;
        }

        @Override
        public IntIterator iterateMatches(REMatcher matcher, int position) {
            int s = matcher.startBackref[this.groupNr];
            int e = matcher.endBackref[this.groupNr];
            if (s == -1 || e == -1) {
                return EmptyIntIterator.getInstance();
            }
            if (s == e) {
                return new IntSingletonIterator(position);
            }
            UnicodeString search = matcher.search;
            int l = e - s;
            if (search.isEnd(position + l - 1)) {
                return EmptyIntIterator.getInstance();
            }
            if (matcher.program.flags.isCaseIndependent()) {
                for (int i = 0; i < l; ++i) {
                    if (matcher.equalCaseBlind(search.uCharAt(position + i), search.uCharAt(s + i))) continue;
                    return EmptyIntIterator.getInstance();
                }
            } else {
                for (int i = 0; i < l; ++i) {
                    if (search.uCharAt(position + i) == search.uCharAt(s + i)) continue;
                    return EmptyIntIterator.getInstance();
                }
            }
            return new IntSingletonIterator(position + l);
        }

        @Override
        public String display() {
            return "\\" + this.groupNr;
        }
    }

    public static class OpCapture
    extends Operation {
        int groupNr;
        Operation childOp;

        OpCapture(Operation childOp, int group) {
            this.childOp = childOp;
            this.groupNr = group;
        }

        @Override
        public int getMatchLength() {
            return this.childOp.getMatchLength();
        }

        @Override
        public int getMinimumMatchLength() {
            return this.childOp.getMinimumMatchLength();
        }

        @Override
        public int matchesEmptyString() {
            return this.childOp.matchesEmptyString();
        }

        @Override
        public Operation optimize(REProgram program, REFlags flags) {
            this.childOp = this.childOp.optimize(program, flags);
            return this;
        }

        @Override
        public IntIterator iterateMatches(final REMatcher matcher, final int position) {
            if ((matcher.program.optimizationFlags & 1) != 0) {
                matcher.startBackref[this.groupNr] = position;
            }
            final IntIterator base = this.childOp.iterateMatches(matcher, position);
            return new IntIterator(){

                @Override
                public boolean hasNext() {
                    return base.hasNext();
                }

                @Override
                public int next() {
                    int next = base.next();
                    if (groupNr >= matcher.captureState.parenCount) {
                        matcher.captureState.parenCount = groupNr + 1;
                    }
                    matcher.setParenStart(groupNr, position);
                    matcher.setParenEnd(groupNr, next);
                    if ((matcher.program.optimizationFlags & 1) != 0) {
                        matcher.startBackref[groupNr] = position;
                        matcher.endBackref[groupNr] = next;
                    }
                    return next;
                }
            };
        }

        @Override
        public String display() {
            return "(" + this.childOp.display() + ")";
        }
    }

    public static class OpEOL
    extends Operation {
        @Override
        public int getMatchLength() {
            return 0;
        }

        @Override
        public int matchesEmptyString() {
            return 2;
        }

        @Override
        public IntIterator iterateMatches(REMatcher matcher, int position) {
            UnicodeString search = matcher.search;
            if (matcher.program.flags.isMultiLine()) {
                if (search.isEnd(0) || search.isEnd(position) || matcher.isNewline(position)) {
                    return new IntSingletonIterator(position);
                }
                return EmptyIntIterator.getInstance();
            }
            if (search.isEnd(0) || search.isEnd(position)) {
                return new IntSingletonIterator(position);
            }
            return EmptyIntIterator.getInstance();
        }

        @Override
        public String display() {
            return "$";
        }
    }

    public static class OpBOL
    extends Operation {
        @Override
        public int getMatchLength() {
            return 0;
        }

        @Override
        public int matchesEmptyString() {
            return 1;
        }

        @Override
        public IntIterator iterateMatches(REMatcher matcher, int position) {
            if (position != 0) {
                if (matcher.program.flags.isMultiLine() && matcher.isNewline(position - 1) && !matcher.search.isEnd(position)) {
                    return new IntSingletonIterator(position);
                }
                return EmptyIntIterator.getInstance();
            }
            return new IntSingletonIterator(position);
        }

        @Override
        public String display() {
            return "^";
        }
    }

    public static class OpEndProgram
    extends Operation {
        @Override
        public int getMatchLength() {
            return 0;
        }

        @Override
        public int matchesEmptyString() {
            return 7;
        }

        @Override
        public IntIterator iterateMatches(REMatcher matcher, int position) {
            if (matcher.anchoredMatch) {
                if (matcher.search.isEnd(position)) {
                    return new IntSingletonIterator(position);
                }
                return EmptyIntIterator.getInstance();
            }
            matcher.setParenEnd(0, position);
            return new IntSingletonIterator(position);
        }

        @Override
        public String display() {
            return "\\Z";
        }
    }

    public static class OpReluctantFixed
    extends OpRepeat {
        private int len;

        OpReluctantFixed(Operation op, int min, int max, int len) {
            super(op, min, max, false);
            this.len = len;
        }

        @Override
        public int getMatchLength() {
            return this.min == this.max ? this.min * this.len : -1;
        }

        @Override
        public int matchesEmptyString() {
            if (this.min == 0) {
                return 7;
            }
            return this.op.matchesEmptyString();
        }

        @Override
        public Operation optimize(REProgram program, REFlags flags) {
            this.op = this.op.optimize(program, flags);
            return this;
        }

        @Override
        public IntIterator iterateMatches(final REMatcher matcher, final int position) {
            return new IntIterator(){
                private int pos;
                private int count;
                private boolean started;
                {
                    this.pos = position;
                    this.count = 0;
                    this.started = false;
                }

                @Override
                public boolean hasNext() {
                    if (!this.started) {
                        this.started = true;
                        while (this.count < min) {
                            IntIterator child = op.iterateMatches(matcher, this.pos);
                            if (child.hasNext()) {
                                this.pos = child.next();
                                ++this.count;
                                continue;
                            }
                            return false;
                        }
                        return true;
                    }
                    if (this.count < max) {
                        matcher.clearCapturedGroupsBeyond(this.pos);
                        IntIterator child = op.iterateMatches(matcher, this.pos);
                        if (child.hasNext()) {
                            this.pos = child.next();
                            ++this.count;
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public int next() {
                    return this.pos;
                }
            };
        }
    }

    public static class OpRepeat
    extends Operation {
        protected Operation op;
        protected int min;
        protected int max;
        boolean greedy;

        OpRepeat(Operation op, int min, int max, boolean greedy) {
            this.op = op;
            this.min = min;
            this.max = max;
            this.greedy = greedy;
        }

        Operation getRepeatedOperation() {
            return this.op;
        }

        @Override
        public int matchesEmptyString() {
            if (this.min == 0) {
                return 7;
            }
            return this.op.matchesEmptyString();
        }

        @Override
        public boolean containsCapturingExpressions() {
            return this.op instanceof OpCapture || this.op.containsCapturingExpressions();
        }

        @Override
        public CharacterClass getInitialCharacterClass(boolean caseBlind) {
            return this.op.getInitialCharacterClass(caseBlind);
        }

        @Override
        public int getMatchLength() {
            return this.min == this.max && this.op.getMatchLength() >= 0 ? this.min * this.op.getMatchLength() : -1;
        }

        @Override
        public int getMinimumMatchLength() {
            return this.min * this.op.getMinimumMatchLength();
        }

        @Override
        public Operation optimize(REProgram program, REFlags flags) {
            this.op = this.op.optimize(program, flags);
            if (this.min == 0 && this.op.matchesEmptyString() == 7) {
                this.min = 1;
            }
            return this;
        }

        @Override
        public IntIterator iterateMatches(final REMatcher matcher, final int position) {
            final Stack<IntIterator> iterators = new Stack<IntIterator>();
            final Stack<Integer> positions = new Stack<Integer>();
            final int bound = Math.min(this.max, matcher.search.uLength() - position + 1);
            int p = position;
            if (this.greedy) {
                if (this.min == 0 && !matcher.history.isDuplicateZeroLengthMatch(this, position)) {
                    iterators.push(new IntSingletonIterator(position));
                    positions.push(p);
                }
                for (int i = 0; i < bound; ++i) {
                    IntIterator it = this.op.iterateMatches(matcher, p);
                    if (!it.hasNext()) {
                        if (!iterators.isEmpty()) break;
                        return EmptyIntIterator.getInstance();
                    }
                    p = it.next();
                    iterators.push(it);
                    positions.push(p);
                }
                IntIterator base = new IntIterator(){
                    boolean primed = true;

                    private void advance() {
                        IntIterator top = (IntIterator)iterators.peek();
                        if (top.hasNext()) {
                            IntIterator it;
                            int p = top.next();
                            positions.pop();
                            positions.push(p);
                            while (iterators.size() < bound && (it = op.iterateMatches(matcher, p)).hasNext()) {
                                p = it.next();
                                iterators.push(it);
                                positions.push(p);
                            }
                        } else {
                            iterators.pop();
                            positions.pop();
                        }
                    }

                    @Override
                    public boolean hasNext() {
                        if (this.primed && iterators.size() >= min) {
                            return !iterators.isEmpty();
                        }
                        if (iterators.isEmpty()) {
                            return false;
                        }
                        do {
                            this.advance();
                        } while (iterators.size() < min && !iterators.isEmpty());
                        return !iterators.isEmpty();
                    }

                    @Override
                    public int next() {
                        this.primed = false;
                        return (Integer)positions.peek();
                    }
                };
                return new ForceProgressIterator(base);
            }
            IntIterator iter = new IntIterator(){
                private int pos;
                private int counter;
                {
                    this.pos = position;
                    this.counter = 0;
                }

                private void advance() {
                    IntIterator it = op.iterateMatches(matcher, this.pos);
                    if (it.hasNext()) {
                        this.pos = it.next();
                        if (++this.counter > max) {
                            this.pos = -1;
                        }
                    } else if (min == 0 && this.counter == 0) {
                        ++this.counter;
                    } else {
                        this.pos = -1;
                    }
                }

                @Override
                public boolean hasNext() {
                    do {
                        this.advance();
                    } while (this.counter < min && this.pos >= 0);
                    return this.pos >= 0;
                }

                @Override
                public int next() {
                    return this.pos;
                }
            };
            return new ForceProgressIterator(iter);
        }

        @Override
        public String display() {
            String quantifier = this.min == 0 && this.max == Integer.MAX_VALUE ? "*" : (this.min == 1 && this.max == Integer.MAX_VALUE ? "+" : (this.min == 0 && this.max == 1 ? "?" : "{" + this.min + "," + this.max + "}"));
            if (!this.greedy) {
                quantifier = quantifier + "?";
            }
            return this.op.display() + quantifier;
        }
    }

    public static class OpUnambiguousRepeat
    extends OpRepeat {
        OpUnambiguousRepeat(Operation op, int min, int max) {
            super(op, min, max, true);
        }

        @Override
        public int matchesEmptyString() {
            if (this.min == 0) {
                return 7;
            }
            return this.op.matchesEmptyString();
        }

        @Override
        public int getMatchLength() {
            if (this.op.getMatchLength() != -1 && this.min == this.max) {
                return this.op.getMatchLength() * this.min;
            }
            return -1;
        }

        @Override
        public Operation optimize(REProgram program, REFlags flags) {
            this.op = this.op.optimize(program, flags);
            return this;
        }

        @Override
        public IntIterator iterateMatches(REMatcher matcher, int position) {
            IntIterator it;
            int matches;
            int guard = matcher.search.uLength();
            int p = position;
            for (matches = 0; matches < this.max && p <= guard && (it = this.op.iterateMatches(matcher, p)).hasNext(); ++matches) {
                p = it.next();
            }
            if (matches < this.min) {
                return EmptyIntIterator.getInstance();
            }
            return new IntSingletonIterator(p);
        }
    }

    public static class OpGreedyFixed
    extends OpRepeat {
        private int len;

        OpGreedyFixed(Operation op, int min, int max, int len) {
            super(op, min, max, true);
            this.len = len;
        }

        @Override
        public int getMatchLength() {
            return this.min == this.max ? this.min * this.len : -1;
        }

        @Override
        public int matchesEmptyString() {
            if (this.min == 0) {
                return 7;
            }
            return this.op.matchesEmptyString();
        }

        @Override
        public Operation optimize(REProgram program, REFlags flags) {
            if (this.max == 0) {
                return new OpNothing();
            }
            if (this.op.getMatchLength() == 0) {
                return this.op;
            }
            this.op = this.op.optimize(program, flags);
            return this;
        }

        @Override
        public IntIterator iterateMatches(REMatcher matcher, int position) {
            int p;
            int guard = matcher.search.uLength();
            if (this.max < Integer.MAX_VALUE) {
                guard = Math.min(guard, position + this.len * this.max);
            }
            if (position >= guard && this.min > 0) {
                return EmptyIntIterator.getInstance();
            }
            int matches = 0;
            for (p = position; p <= guard; p += this.len) {
                IntIterator it = this.op.iterateMatches(matcher, p);
                boolean matched = false;
                if (it.hasNext()) {
                    matched = true;
                    it.next();
                }
                if (!matched) break;
                if (++matches != this.max) continue;
                break;
            }
            if (matches < this.min) {
                return EmptyIntIterator.getInstance();
            }
            return new IntStepIterator(p, -this.len, position + this.len * this.min);
        }
    }

    public static class OpAtom
    extends Operation {
        private UnicodeString atom;
        private int len;

        OpAtom(UnicodeString atom) {
            this.atom = atom;
            this.len = atom.uLength();
        }

        UnicodeString getAtom() {
            return this.atom;
        }

        @Override
        public int getMatchLength() {
            return this.len;
        }

        @Override
        public int matchesEmptyString() {
            return this.len == 0 ? 7 : 1024;
        }

        @Override
        public CharacterClass getInitialCharacterClass(boolean caseBlind) {
            int ch;
            int[] variants;
            if (this.len == 0) {
                return EmptyCharacterClass.getInstance();
            }
            if (caseBlind && (variants = CaseVariants.getCaseVariants(ch = this.atom.uCharAt(0))).length > 0) {
                IntHashSet set = new IntHashSet(variants.length);
                set.add(ch);
                for (int v : variants) {
                    set.add(v);
                }
                return new IntSetCharacterClass(set);
            }
            return new SingletonCharacterClass(this.atom.uCharAt(0));
        }

        @Override
        public IntIterator iterateMatches(REMatcher matcher, int position) {
            UnicodeString in = matcher.search;
            if (position + this.len > in.uLength()) {
                return EmptyIntIterator.getInstance();
            }
            if (matcher.program.flags.isCaseIndependent()) {
                for (int i = 0; i < this.len; ++i) {
                    if (matcher.equalCaseBlind(in.uCharAt(position + i), this.atom.uCharAt(i))) continue;
                    return EmptyIntIterator.getInstance();
                }
            } else {
                for (int i = 0; i < this.len; ++i) {
                    if (in.uCharAt(position + i) == this.atom.uCharAt(i)) continue;
                    return EmptyIntIterator.getInstance();
                }
            }
            return new IntSingletonIterator(position + this.len);
        }

        @Override
        public String display() {
            return this.atom.toString();
        }
    }

    public static class OpCharClass
    extends Operation {
        private IntPredicate predicate;

        OpCharClass(IntPredicate predicate) {
            this.predicate = predicate;
        }

        public IntPredicate getPredicate() {
            return this.predicate;
        }

        @Override
        public int getMatchLength() {
            return 1;
        }

        @Override
        public int matchesEmptyString() {
            return 1024;
        }

        @Override
        public CharacterClass getInitialCharacterClass(boolean caseBlind) {
            if (this.predicate instanceof CharacterClass) {
                return (CharacterClass)this.predicate;
            }
            return super.getInitialCharacterClass(caseBlind);
        }

        @Override
        public IntIterator iterateMatches(REMatcher matcher, int position) {
            UnicodeString in = matcher.search;
            if (position < in.uLength() && this.predicate.test(in.uCharAt(position))) {
                return new IntSingletonIterator(position + 1);
            }
            return EmptyIntIterator.getInstance();
        }

        @Override
        public String display() {
            if (this.predicate instanceof IntSetPredicate) {
                IntSet s = ((IntSetPredicate)this.predicate).getIntSet();
                if (s instanceof IntSingletonSet) {
                    return "" + (char)((IntSingletonSet)s).getMember();
                }
                if (s instanceof IntRangeSet) {
                    FastStringBuffer fsb = new FastStringBuffer(64);
                    IntRangeSet irs = (IntRangeSet)s;
                    fsb.append("[");
                    for (int i = 0; i < irs.getNumberOfRanges(); ++i) {
                        fsb.cat((char)irs.getStartPoints()[1]);
                        fsb.append("-");
                        fsb.cat((char)irs.getEndPoints()[1]);
                    }
                    fsb.append("[");
                    return fsb.toString();
                }
                return "[....]";
            }
            return "[....]";
        }
    }

    public static class OpSequence
    extends Operation {
        private List<Operation> operations;

        OpSequence(List<Operation> operations) {
            this.operations = operations;
        }

        public List<Operation> getOperations() {
            return this.operations;
        }

        @Override
        public int getMatchLength() {
            int len = 0;
            for (Operation o : this.operations) {
                int i = o.getMatchLength();
                if (i == -1) {
                    return -1;
                }
                len += i;
            }
            return len;
        }

        @Override
        public int getMinimumMatchLength() {
            int len = 0;
            for (Operation o : this.operations) {
                len += o.getMinimumMatchLength();
            }
            return len;
        }

        @Override
        public int matchesEmptyString() {
            boolean bl;
            boolean matchesEmptyAnywhere = true;
            boolean matchesEmptyNowhere = false;
            for (Operation operation : this.operations) {
                int m = operation.matchesEmptyString();
                if (m == 1024) {
                    return 1024;
                }
                if (m == 7) continue;
                matchesEmptyAnywhere = false;
                break;
            }
            if (matchesEmptyAnywhere) {
                return 7;
            }
            boolean matchesBOL = true;
            for (Operation o : this.operations) {
                if ((o.matchesEmptyString() & 1) != 0) continue;
                matchesBOL = false;
                break;
            }
            if (matchesBOL) {
                return 1;
            }
            boolean bl2 = true;
            for (Operation o : this.operations) {
                if ((o.matchesEmptyString() & 2) != 0) continue;
                bl = false;
                break;
            }
            if (bl) {
                return 2;
            }
            return 0;
        }

        @Override
        public boolean containsCapturingExpressions() {
            for (Operation o : this.operations) {
                if (!(o instanceof OpCapture) && !o.containsCapturingExpressions()) continue;
                return true;
            }
            return false;
        }

        @Override
        public CharacterClass getInitialCharacterClass(boolean caseBlind) {
            CharacterClass result = EmptyCharacterClass.getInstance();
            for (Operation o : this.operations) {
                result = RECompiler.makeUnion(result, o.getInitialCharacterClass(caseBlind));
                if (o.matchesEmptyString() != 1024) continue;
                return result;
            }
            return result;
        }

        @Override
        public String display() {
            FastStringBuffer fsb = new FastStringBuffer(64);
            for (Operation op : this.operations) {
                fsb.append(op.display());
            }
            return fsb.toString();
        }

        @Override
        public Operation optimize(REProgram program, REFlags flags) {
            if (this.operations.size() == 0) {
                return new OpNothing();
            }
            if (this.operations.size() == 1) {
                return this.operations.get(0);
            }
            for (int i = 0; i < this.operations.size() - 1; ++i) {
                Operation o1r;
                Operation o2;
                Operation o1 = this.operations.get(i);
                if (o1 != (o2 = o1.optimize(program, flags))) {
                    this.operations.set(i, o2);
                }
                if (!(o2 instanceof OpRepeat) || !((o1r = ((OpRepeat)o1).getRepeatedOperation()) instanceof OpAtom) && !(o1r instanceof OpCharClass)) continue;
                Operation o2r = this.operations.get(i + 1);
                if (((OpRepeat)o1).min != ((OpRepeat)o1).max && !RECompiler.noAmbiguity(o1r, o2r, flags.isCaseIndependent(), !((OpRepeat)o1).greedy)) continue;
                this.operations.set(i, new OpUnambiguousRepeat(o1r, ((OpRepeat)o1).min, ((OpRepeat)o1).max));
            }
            return this;
        }

        @Override
        public IntIterator iterateMatches(final REMatcher matcher, final int position) {
            final Stack iterators = new Stack();
            final REMatcher.State savedState = this.containsCapturingExpressions() ? matcher.captureState() : null;
            final int backtrackingLimit = matcher.getProgram().getBacktrackingLimit();
            return new IntIterator(){
                private boolean primed = false;
                private int nextPos;

                private int advance() {
                    int counter = 0;
                    while (!iterators.isEmpty()) {
                        IntIterator top = (IntIterator)iterators.peek();
                        while (top.hasNext()) {
                            int p = top.next();
                            matcher.clearCapturedGroupsBeyond(p);
                            int i = iterators.size();
                            if (i >= operations.size()) {
                                return p;
                            }
                            top = ((Operation)operations.get(i)).iterateMatches(matcher, p);
                            iterators.push(top);
                        }
                        iterators.pop();
                        if (backtrackingLimit < 0 || counter++ <= backtrackingLimit) continue;
                        throw new UncheckedXPathException(new XPathException("Regex backtracking limit exceeded processing " + matcher.operation.display() + ". Simplify the regular expression, or set Feature.REGEX_BACKTRACKING_LIMIT to -1 to remove this limit."));
                    }
                    if (savedState != null) {
                        matcher.resetState(savedState);
                    }
                    return -1;
                }

                @Override
                public boolean hasNext() {
                    if (!this.primed) {
                        iterators.push(((Operation)operations.get(0)).iterateMatches(matcher, position));
                        this.primed = true;
                    }
                    this.nextPos = this.advance();
                    return this.nextPos >= 0;
                }

                @Override
                public int next() {
                    return this.nextPos;
                }
            };
        }
    }

    public static class OpChoice
    extends Operation {
        List<Operation> branches;

        OpChoice(List<Operation> branches) {
            this.branches = branches;
        }

        @Override
        public int getMatchLength() {
            int fixed = this.branches.get(0).getMatchLength();
            for (int i = 1; i < this.branches.size(); ++i) {
                if (this.branches.get(i).getMatchLength() == fixed) continue;
                return -1;
            }
            return fixed;
        }

        @Override
        public int getMinimumMatchLength() {
            int min = this.branches.get(0).getMinimumMatchLength();
            for (int i = 1; i < this.branches.size(); ++i) {
                int m = this.branches.get(i).getMinimumMatchLength();
                if (m >= min) continue;
                min = m;
            }
            return min;
        }

        @Override
        public int matchesEmptyString() {
            int m = 0;
            for (Operation branch : this.branches) {
                int b = branch.matchesEmptyString();
                if (b == 1024) continue;
                m |= b;
            }
            return m;
        }

        @Override
        public boolean containsCapturingExpressions() {
            for (Operation o : this.branches) {
                if (!(o instanceof OpCapture) && !o.containsCapturingExpressions()) continue;
                return true;
            }
            return false;
        }

        @Override
        public CharacterClass getInitialCharacterClass(boolean caseBlind) {
            CharacterClass result = EmptyCharacterClass.getInstance();
            for (Operation o : this.branches) {
                result = RECompiler.makeUnion(result, o.getInitialCharacterClass(caseBlind));
            }
            return result;
        }

        @Override
        public Operation optimize(REProgram program, REFlags flags) {
            for (int i = 0; i < this.branches.size(); ++i) {
                Operation o2;
                Operation o1 = this.branches.get(i);
                if (o1 == (o2 = o1.optimize(program, flags))) continue;
                this.branches.set(i, o2);
            }
            return this;
        }

        @Override
        public IntIterator iterateMatches(final REMatcher matcher, final int position) {
            return new IntIterator(){
                Iterator<Operation> branchIter;
                IntIterator currentIter;
                Operation currentOp;
                {
                    this.branchIter = branches.iterator();
                    this.currentIter = null;
                    this.currentOp = null;
                }

                @Override
                public boolean hasNext() {
                    while (true) {
                        if (this.currentIter == null) {
                            if (this.branchIter.hasNext()) {
                                matcher.clearCapturedGroupsBeyond(position);
                                this.currentOp = this.branchIter.next();
                                this.currentIter = this.currentOp.iterateMatches(matcher, position);
                            } else {
                                return false;
                            }
                        }
                        if (this.currentIter.hasNext()) {
                            return true;
                        }
                        this.currentIter = null;
                    }
                }

                @Override
                public int next() {
                    return this.currentIter.next();
                }
            };
        }

        @Override
        public String display() {
            FastStringBuffer fsb = new FastStringBuffer(64);
            fsb.append("(?:");
            boolean first = true;
            for (Operation branch : this.branches) {
                if (first) {
                    first = false;
                } else {
                    fsb.cat('|');
                }
                fsb.append(branch.display());
            }
            fsb.append(")");
            return fsb.toString();
        }
    }
}


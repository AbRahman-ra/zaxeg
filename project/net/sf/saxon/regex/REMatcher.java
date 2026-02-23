/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntPredicate;
import net.sf.saxon.regex.CaseVariants;
import net.sf.saxon.regex.History;
import net.sf.saxon.regex.Operation;
import net.sf.saxon.regex.REProgram;
import net.sf.saxon.regex.RESyntaxException;
import net.sf.saxon.regex.RegexPrecondition;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.z.IntIterator;

public class REMatcher {
    static final int MAX_PAREN = 16;
    REProgram program;
    UnicodeString search;
    History history = new History();
    int maxParen = 16;
    State captureState = new State();
    int[] startBackref;
    int[] endBackref;
    Operation operation;
    boolean anchoredMatch;

    public REMatcher(REProgram program) {
        this.setProgram(program);
    }

    public void setProgram(REProgram program) {
        this.program = program;
        if (program != null && program.maxParens != -1) {
            this.operation = program.operation;
            this.maxParen = program.maxParens;
        } else {
            this.maxParen = 16;
        }
    }

    public REProgram getProgram() {
        return this.program;
    }

    public int getParenCount() {
        return this.captureState.parenCount;
    }

    public UnicodeString getParen(int which) {
        int start;
        if (which < this.captureState.parenCount && (start = this.getParenStart(which)) >= 0) {
            return this.search.uSubstring(start, this.getParenEnd(which));
        }
        return null;
    }

    public final int getParenStart(int which) {
        if (which < this.captureState.startn.length) {
            return this.captureState.startn[which];
        }
        return -1;
    }

    public final int getParenEnd(int which) {
        if (which < this.captureState.endn.length) {
            return this.captureState.endn[which];
        }
        return -1;
    }

    protected final void setParenStart(int which, int i) {
        while (which > this.captureState.startn.length - 1) {
            int[] s2 = new int[this.captureState.startn.length * 2];
            System.arraycopy(this.captureState.startn, 0, s2, 0, this.captureState.startn.length);
            Arrays.fill(s2, this.captureState.startn.length, s2.length, -1);
            this.captureState.startn = s2;
        }
        this.captureState.startn[which] = i;
    }

    protected final void setParenEnd(int which, int i) {
        while (which > this.captureState.endn.length - 1) {
            int[] e2 = new int[this.captureState.endn.length * 2];
            System.arraycopy(this.captureState.endn, 0, e2, 0, this.captureState.endn.length);
            Arrays.fill(e2, this.captureState.endn.length, e2.length, -1);
            this.captureState.endn = e2;
        }
        this.captureState.endn[which] = i;
    }

    protected void clearCapturedGroupsBeyond(int pos) {
        int i;
        for (i = 0; i < this.captureState.startn.length; ++i) {
            if (this.captureState.startn[i] < pos) continue;
            this.captureState.endn[i] = this.captureState.startn[i];
        }
        if (this.startBackref != null) {
            for (i = 0; i < this.startBackref.length; ++i) {
                if (this.startBackref[i] < pos) continue;
                this.endBackref[i] = this.startBackref[i];
            }
        }
    }

    protected boolean matchAt(int i, boolean anchored) {
        IntIterator iter;
        this.captureState.parenCount = 1;
        this.anchoredMatch = anchored;
        this.setParenStart(0, i);
        if ((this.program.optimizationFlags & 1) != 0) {
            this.startBackref = new int[this.maxParen];
            this.endBackref = new int[this.maxParen];
        }
        if ((iter = this.operation.iterateMatches(this, i)).hasNext()) {
            int idx = iter.next();
            this.setParenEnd(0, idx);
            return true;
        }
        this.captureState.parenCount = 0;
        return false;
    }

    public boolean anchoredMatch(UnicodeString search) {
        this.search = search;
        return this.matchAt(0, true);
    }

    public boolean match(UnicodeString search, int i) {
        this.search = search;
        this.captureState = new State();
        if ((this.program.optimizationFlags & 2) == 2) {
            if (!this.program.flags.isMultiLine()) {
                return i == 0 && this.checkPreconditions(i) && this.matchAt(i, false);
            }
            int nl = i;
            if (this.matchAt(nl, false)) {
                return true;
            }
            do {
                if ((nl = search.uIndexOf(10, nl) + 1) < search.uLength() && nl > 0) continue;
                return false;
            } while (!this.matchAt(nl, false));
            return true;
        }
        int actualLength = search.uLength() - i;
        if (actualLength < this.program.minimumLength) {
            return false;
        }
        if (this.program.prefix == null) {
            if (this.program.initialCharClass != null) {
                IntPredicate pred = this.program.initialCharClass;
                while (!search.isEnd(i)) {
                    if (pred.test(search.uCharAt(i)) && this.matchAt(i, false)) {
                        return true;
                    }
                    ++i;
                }
                return false;
            }
            if (!this.checkPreconditions(i)) {
                return false;
            }
            while (!search.isEnd(i - 1)) {
                if (this.matchAt(i, false)) {
                    return true;
                }
                ++i;
            }
            return false;
        }
        UnicodeString prefix = this.program.prefix;
        int prefixLength = prefix.uLength();
        boolean ignoreCase = this.program.flags.isCaseIndependent();
        while (!search.isEnd(i + prefixLength - 1)) {
            int k;
            int j;
            boolean prefixOK = true;
            if (ignoreCase) {
                j = i;
                for (k = 0; k < prefixLength; ++k) {
                    if (!this.equalCaseBlind(search.uCharAt(j), prefix.uCharAt(k))) {
                        prefixOK = false;
                        break;
                    }
                    ++j;
                }
            } else {
                j = i;
                for (k = 0; k < prefixLength; ++k) {
                    if (search.uCharAt(j) != prefix.uCharAt(k)) {
                        prefixOK = false;
                        break;
                    }
                    ++j;
                }
            }
            if (prefixOK && this.matchAt(i, false)) {
                return true;
            }
            ++i;
        }
        return false;
    }

    private boolean checkPreconditions(int start) {
        for (RegexPrecondition condition : this.program.preconditions) {
            if (condition.fixedPosition != -1) {
                boolean match = condition.operation.iterateMatches(this, condition.fixedPosition).hasNext();
                if (match) continue;
                return false;
            }
            int i = start;
            if (i < condition.minPosition) {
                i = condition.minPosition;
            }
            boolean found = false;
            while (!this.search.isEnd(i)) {
                if ((condition.fixedPosition == -1 || condition.fixedPosition == i) && condition.operation.iterateMatches(this, i).hasNext()) {
                    found = true;
                    break;
                }
                ++i;
            }
            if (found) continue;
            return false;
        }
        return true;
    }

    public boolean match(String search) {
        UnicodeString uString = UnicodeString.makeUnicodeString(search);
        return this.match(uString, 0);
    }

    public List<UnicodeString> split(UnicodeString s) {
        ArrayList<UnicodeString> v = new ArrayList<UnicodeString>();
        int pos = 0;
        int len = s.uLength();
        while (pos < len && this.match(s, pos)) {
            int start = this.getParenStart(0);
            int newpos = this.getParenEnd(0);
            if (newpos == pos) {
                v.add(s.uSubstring(pos, start + 1));
            } else {
                v.add(s.uSubstring(pos, start));
            }
            pos = ++newpos;
        }
        UnicodeString remainder = s.uSubstring(pos, len);
        v.add(remainder);
        return v;
    }

    public CharSequence replace(UnicodeString in, UnicodeString replacement) {
        int i;
        FastStringBuffer sb = new FastStringBuffer(in.uLength() * 2);
        int pos = 0;
        int len = in.uLength();
        while (pos < len && this.match(in, pos)) {
            int newpos;
            for (i = pos; i < this.getParenStart(0); ++i) {
                sb.appendWideChar(in.uCharAt(i));
            }
            if (!this.program.flags.isLiteral()) {
                int maxCapture = this.program.maxParens - 1;
                for (int i2 = 0; i2 < replacement.uLength(); ++i2) {
                    int ch = replacement.uCharAt(i2);
                    if (ch == 92) {
                        if ((ch = replacement.uCharAt(++i2)) == 92 || ch == 36) {
                            sb.cat((char)ch);
                            continue;
                        }
                        throw new RESyntaxException("Invalid escape '" + ch + "' in replacement string");
                    }
                    if (ch == 36) {
                        int j;
                        UnicodeString captured;
                        if ((ch = replacement.uCharAt(++i2)) < 48 || ch > 57) {
                            throw new RESyntaxException("$ in replacement string must be followed by a digit");
                        }
                        int n = ch - 48;
                        if (maxCapture <= 9) {
                            if (maxCapture < n || (captured = this.getParen(n)) == null) continue;
                            for (j = 0; j < captured.uLength(); ++j) {
                                sb.appendWideChar(captured.uCharAt(j));
                            }
                            continue;
                        }
                        while (++i2 < replacement.uLength()) {
                            ch = replacement.uCharAt(i2);
                            if (ch >= 48 && ch <= 57) {
                                int m = n * 10 + (ch - 48);
                                if (m > maxCapture) {
                                    --i2;
                                    break;
                                }
                                n = m;
                                continue;
                            }
                            --i2;
                            break;
                        }
                        if ((captured = this.getParen(n)) == null) continue;
                        for (j = 0; j < captured.uLength(); ++j) {
                            sb.appendWideChar(captured.uCharAt(j));
                        }
                        continue;
                    }
                    sb.appendWideChar(ch);
                }
            } else {
                for (i = 0; i < replacement.uLength(); ++i) {
                    sb.appendWideChar(replacement.uCharAt(i));
                }
            }
            if ((newpos = this.getParenEnd(0)) == pos) {
                // empty if block
            }
            pos = ++newpos;
        }
        for (i = pos; i < len; ++i) {
            sb.appendWideChar(in.uCharAt(i));
        }
        return sb.condense();
    }

    public CharSequence replaceWith(UnicodeString in, Function<CharSequence, CharSequence> replacer) {
        FastStringBuffer sb = new FastStringBuffer(in.uLength() * 2);
        int pos = 0;
        int len = in.uLength();
        while (pos < len && this.match(in, pos)) {
            for (int i = pos; i < this.getParenStart(0); ++i) {
                sb.appendWideChar(in.uCharAt(i));
            }
            CharSequence matchingSubstring = in.subSequence(this.getParenStart(0), this.getParenEnd(0));
            CharSequence replacement = replacer.apply(matchingSubstring);
            sb.append(replacement);
            int newpos = this.getParenEnd(0);
            if (newpos == pos) {
                // empty if block
            }
            pos = ++newpos;
        }
        for (int i = pos; i < len; ++i) {
            sb.appendWideChar(in.uCharAt(i));
        }
        return sb.condense();
    }

    boolean isNewline(int i) {
        return this.search.uCharAt(i) == 10;
    }

    boolean equalCaseBlind(int c1, int c2) {
        if (c1 == c2) {
            return true;
        }
        for (int v : CaseVariants.getCaseVariants(c2)) {
            if (c1 != v) continue;
            return true;
        }
        return false;
    }

    public State captureState() {
        return new State(this.captureState);
    }

    public void resetState(State state) {
        this.captureState = new State(state);
    }

    public static class State {
        int parenCount;
        int[] startn;
        int[] endn;

        public State() {
            this.parenCount = 0;
            this.startn = new int[3];
            this.startn[2] = -1;
            this.startn[1] = -1;
            this.startn[0] = -1;
            this.endn = new int[3];
            this.endn[2] = -1;
            this.endn[1] = -1;
            this.endn[0] = -1;
        }

        public State(State s) {
            this.parenCount = s.parenCount;
            this.startn = Arrays.copyOf(s.startn, s.startn.length);
            this.endn = Arrays.copyOf(s.endn, s.endn.length);
        }
    }
}


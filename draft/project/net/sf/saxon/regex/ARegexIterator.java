/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.regex.REMatcher;
import net.sf.saxon.regex.RegexIterator;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.z.IntHashMap;
import net.sf.saxon.z.IntToIntHashMap;

public class ARegexIterator
implements RegexIterator,
LastPositionFinder {
    private UnicodeString theString;
    private UnicodeString regex;
    private REMatcher matcher;
    private UnicodeString current;
    private UnicodeString next;
    private int prevEnd = 0;
    private IntToIntHashMap nestingTable = null;
    private boolean skip = false;

    public ARegexIterator(UnicodeString string, UnicodeString regex, REMatcher matcher) {
        this.theString = string;
        this.regex = regex;
        this.matcher = matcher;
        this.next = null;
    }

    @Override
    public int getLength() throws XPathException {
        ARegexIterator another = new ARegexIterator(this.theString, this.regex, new REMatcher(this.matcher.getProgram()));
        int n = 0;
        while (another.next() != null) {
            ++n;
        }
        return n;
    }

    @Override
    public StringValue next() throws XPathException {
        try {
            if (this.next == null && this.prevEnd >= 0) {
                int searchStart = this.prevEnd;
                if (this.skip && ++searchStart >= this.theString.uLength()) {
                    if (this.prevEnd < this.theString.uLength()) {
                        this.current = this.theString.uSubstring(this.prevEnd, this.theString.uLength());
                        this.next = null;
                    } else {
                        this.current = null;
                        this.prevEnd = -1;
                        return null;
                    }
                }
                if (this.matcher.match(this.theString, searchStart)) {
                    int end;
                    int start = this.matcher.getParenStart(0);
                    boolean bl = this.skip = start == (end = this.matcher.getParenEnd(0));
                    if (this.prevEnd == start) {
                        this.next = null;
                        this.current = this.theString.uSubstring(start, end);
                        this.prevEnd = end;
                    } else {
                        this.current = this.theString.uSubstring(this.prevEnd, start);
                        this.next = this.theString.uSubstring(start, end);
                    }
                } else {
                    if (this.prevEnd >= this.theString.uLength()) {
                        this.current = null;
                        this.prevEnd = -1;
                        return null;
                    }
                    this.current = this.theString.uSubstring(this.prevEnd, this.theString.uLength());
                    this.next = null;
                    this.prevEnd = -1;
                }
            } else if (this.prevEnd >= 0) {
                this.current = this.next;
                this.next = null;
                this.prevEnd = this.matcher.getParenEnd(0);
            } else {
                this.current = null;
                return null;
            }
            return this.currentStringValue();
        } catch (StackOverflowError e) {
            throw new XPathException.StackOverflow("Stack overflow (excessive recursion) during regular expression evaluation", "SXRE0001", Loc.NONE);
        }
    }

    private StringValue currentStringValue() {
        return StringValue.makeStringValue(this.current);
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LAST_POSITION_FINDER);
    }

    @Override
    public boolean isMatching() {
        return this.next == null && this.prevEnd >= 0;
    }

    @Override
    public String getRegexGroup(int number) {
        if (!this.isMatching()) {
            return null;
        }
        if (number >= this.matcher.getParenCount() || number < 0) {
            return "";
        }
        UnicodeString us = this.matcher.getParen(number);
        return us == null ? "" : us.toString();
    }

    @Override
    public int getNumberOfGroups() {
        return this.matcher.getParenCount();
    }

    @Override
    public void processMatchingSubstring(RegexIterator.MatchHandler action) throws XPathException {
        int c = this.matcher.getParenCount() - 1;
        if (c == 0) {
            action.characters(this.current.toString());
        } else {
            IntHashMap actions = new IntHashMap(c);
            for (int i = 1; i <= c; ++i) {
                int start = this.matcher.getParenStart(i) - this.matcher.getParenStart(0);
                if (start == -1) continue;
                int end = this.matcher.getParenEnd(i) - this.matcher.getParenStart(0);
                if (start < end) {
                    ArrayList<Integer> s = (ArrayList<Integer>)actions.get(start);
                    if (s == null) {
                        s = new ArrayList<Integer>(4);
                        actions.put(start, s);
                    }
                    s.add(i);
                    ArrayList<Integer> e = (ArrayList<Integer>)actions.get(end);
                    if (e == null) {
                        e = new ArrayList<Integer>(4);
                        actions.put(end, e);
                    }
                    e.add(0, -i);
                    continue;
                }
                if (this.nestingTable == null) {
                    this.nestingTable = ARegexIterator.computeNestingTable(this.regex);
                }
                int parentGroup = this.nestingTable.get(i);
                ArrayList<Integer> s = (ArrayList<Integer>)actions.get(start);
                if (s == null) {
                    s = new ArrayList<Integer>(4);
                    actions.put(start, s);
                    s.add(i);
                    s.add(-i);
                    continue;
                }
                int pos = s.size();
                for (int e = 0; e < s.size(); ++e) {
                    if ((Integer)s.get(e) != -parentGroup) continue;
                    pos = e;
                    break;
                }
                s.add(pos, -i);
                s.add(pos, i);
            }
            FastStringBuffer buff = new FastStringBuffer(this.current.uLength());
            for (int i = 0; i < this.current.uLength() + 1; ++i) {
                List events = (List)actions.get(i);
                if (events != null) {
                    if (buff.length() > 0) {
                        action.characters(buff);
                        buff.setLength(0);
                    }
                    for (Integer group : events) {
                        if (group > 0) {
                            action.onGroupStart(group);
                            continue;
                        }
                        action.onGroupEnd(-group.intValue());
                    }
                }
                if (i >= this.current.uLength()) continue;
                buff.appendWideChar(this.current.uCharAt(i));
            }
            if (buff.length() > 0) {
                action.characters(buff);
            }
        }
    }

    public static IntToIntHashMap computeNestingTable(UnicodeString regex) {
        IntToIntHashMap nestingTable = new IntToIntHashMap(16);
        int[] stack = new int[regex.uLength()];
        int tos = 0;
        boolean[] captureStack = new boolean[regex.uLength()];
        int captureTos = 0;
        int group = 1;
        int inBrackets = 0;
        stack[tos++] = 0;
        for (int i = 0; i < regex.uLength(); ++i) {
            boolean capture;
            int ch = regex.uCharAt(i);
            if (ch == 92) {
                ++i;
                continue;
            }
            if (ch == 91) {
                ++inBrackets;
                continue;
            }
            if (ch == 93) {
                --inBrackets;
                continue;
            }
            if (ch == 40 && inBrackets == 0) {
                capture = regex.uCharAt(i + 1) != 63;
                captureStack[captureTos++] = capture;
                if (!capture) continue;
                nestingTable.put(group, stack[tos - 1]);
                stack[tos++] = group++;
                continue;
            }
            if (ch != 41 || inBrackets != 0 || !(capture = captureStack[--captureTos])) continue;
            --tos;
        }
        return nestingTable;
    }
}


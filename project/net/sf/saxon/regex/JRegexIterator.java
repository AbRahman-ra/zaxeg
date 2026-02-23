/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.functions.Count;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.regex.RegexIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.z.IntHashMap;
import net.sf.saxon.z.IntToIntHashMap;

public class JRegexIterator
implements RegexIterator,
LastPositionFinder {
    private String theString;
    private Pattern pattern;
    private Matcher matcher;
    private String current;
    private String next;
    private int prevEnd = 0;
    private IntToIntHashMap nestingTable = null;

    public JRegexIterator(String string, Pattern pattern) {
        this.theString = string;
        this.pattern = pattern;
        this.matcher = pattern.matcher(string);
        this.next = null;
    }

    @Override
    public int getLength() throws XPathException {
        JRegexIterator another = new JRegexIterator(this.theString, this.pattern);
        return Count.steppingCount(another);
    }

    @Override
    public StringValue next() {
        if (this.next == null && this.prevEnd >= 0) {
            if (this.matcher.find()) {
                int start = this.matcher.start();
                int end = this.matcher.end();
                if (this.prevEnd == start) {
                    this.next = null;
                    this.current = this.theString.substring(start, end);
                    this.prevEnd = end;
                } else {
                    this.current = this.theString.substring(this.prevEnd, start);
                    this.next = this.theString.substring(start, end);
                }
            } else {
                if (this.prevEnd >= this.theString.length()) {
                    this.current = null;
                    this.prevEnd = -1;
                    return null;
                }
                this.current = this.theString.substring(this.prevEnd);
                this.next = null;
                this.prevEnd = -1;
            }
        } else if (this.prevEnd >= 0) {
            this.current = this.next;
            this.next = null;
            this.prevEnd = this.matcher.end();
        } else {
            this.current = null;
            return null;
        }
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
        if (number > this.matcher.groupCount() || number < 0) {
            return "";
        }
        String s = this.matcher.group(number);
        if (s == null) {
            return "";
        }
        return s;
    }

    @Override
    public int getNumberOfGroups() {
        return this.matcher.groupCount();
    }

    @Override
    public void processMatchingSubstring(RegexIterator.MatchHandler action) throws XPathException {
        int c = this.matcher.groupCount();
        if (c == 0) {
            action.characters(this.current);
        } else {
            IntHashMap actions = new IntHashMap(c);
            for (int i = 1; i <= c; ++i) {
                int start = this.matcher.start(i) - this.matcher.start();
                if (start == -1) continue;
                int end = this.matcher.end(i) - this.matcher.start();
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
                    this.computeNestingTable();
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
            FastStringBuffer buff = new FastStringBuffer(this.current.length());
            for (int i = 0; i < this.current.length() + 1; ++i) {
                List events = (List)actions.get(i);
                if (events != null) {
                    if (buff.length() > 0) {
                        action.characters(buff);
                        buff.setLength(0);
                    }
                    Iterator ii = events.iterator();
                    while (ii.hasNext()) {
                        int group = (Integer)ii.next();
                        if (group > 0) {
                            action.onGroupStart(group);
                            continue;
                        }
                        action.onGroupEnd(-group);
                    }
                }
                if (i >= this.current.length()) continue;
                buff.cat(this.current.charAt(i));
            }
            if (buff.length() > 0) {
                action.characters(buff);
            }
        }
    }

    private void computeNestingTable() {
        this.nestingTable = new IntToIntHashMap(16);
        String s = this.pattern.pattern();
        int[] stack = new int[s.length()];
        int tos = 0;
        int group = 1;
        int inBrackets = 0;
        stack[tos++] = 0;
        for (int i = 0; i < s.length(); ++i) {
            char ch = s.charAt(i);
            if (ch == '\'') {
                ++i;
                continue;
            }
            if (ch == '[') {
                ++inBrackets;
                continue;
            }
            if (ch == ']') {
                --inBrackets;
                continue;
            }
            if (ch == '(' && s.charAt(i + 1) != '?' && inBrackets == 0) {
                this.nestingTable.put(group, stack[tos - 1]);
                stack[tos++] = group++;
                continue;
            }
            if (ch != ')' || inBrackets != 0) continue;
            --tos;
        }
    }
}


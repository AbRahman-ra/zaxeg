/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.rule;

import org.dom4j.Node;
import org.dom4j.rule.Action;
import org.dom4j.rule.Pattern;

public class Rule
implements Comparable {
    private String mode;
    private int importPrecedence;
    private double priority;
    private int appearenceCount;
    private Pattern pattern;
    private Action action;

    public Rule() {
        this.priority = 0.5;
    }

    public Rule(Pattern pattern) {
        this.pattern = pattern;
        this.priority = pattern.getPriority();
    }

    public Rule(Pattern pattern, Action action) {
        this(pattern);
        this.action = action;
    }

    public Rule(Rule that, Pattern pattern) {
        this.mode = that.mode;
        this.importPrecedence = that.importPrecedence;
        this.priority = that.priority;
        this.appearenceCount = that.appearenceCount;
        this.action = that.action;
        this.pattern = pattern;
    }

    public boolean equals(Object that) {
        if (that instanceof Rule) {
            return this.compareTo((Rule)that) == 0;
        }
        return false;
    }

    public int hashCode() {
        return this.importPrecedence + this.appearenceCount;
    }

    public int compareTo(Object that) {
        if (that instanceof Rule) {
            return this.compareTo((Rule)that);
        }
        return this.getClass().getName().compareTo(that.getClass().getName());
    }

    public int compareTo(Rule that) {
        int answer = this.importPrecedence - that.importPrecedence;
        if (answer == 0 && (answer = (int)Math.round(this.priority - that.priority)) == 0) {
            answer = this.appearenceCount - that.appearenceCount;
        }
        return answer;
    }

    public String toString() {
        return super.toString() + "[ pattern: " + this.getPattern() + " action: " + this.getAction() + " ]";
    }

    public final boolean matches(Node node) {
        return this.pattern.matches(node);
    }

    public Rule[] getUnionRules() {
        Pattern[] patterns = this.pattern.getUnionPatterns();
        if (patterns == null) {
            return null;
        }
        int size = patterns.length;
        Rule[] answer = new Rule[size];
        for (int i = 0; i < size; ++i) {
            answer[i] = new Rule(this, patterns[i]);
        }
        return answer;
    }

    public final short getMatchType() {
        return this.pattern.getMatchType();
    }

    public final String getMatchesNodeName() {
        return this.pattern.getMatchesNodeName();
    }

    public String getMode() {
        return this.mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getImportPrecedence() {
        return this.importPrecedence;
    }

    public void setImportPrecedence(int importPrecedence) {
        this.importPrecedence = importPrecedence;
    }

    public double getPriority() {
        return this.priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    public int getAppearenceCount() {
        return this.appearenceCount;
    }

    public void setAppearenceCount(int appearenceCount) {
        this.appearenceCount = appearenceCount;
    }

    public Pattern getPattern() {
        return this.pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Action getAction() {
        return this.action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}


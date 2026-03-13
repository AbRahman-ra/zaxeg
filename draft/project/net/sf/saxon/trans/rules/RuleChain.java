/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans.rules;

import net.sf.saxon.trans.rules.Rule;

public class RuleChain {
    private Rule head;
    public Object optimizationData;

    public RuleChain() {
        this.head = null;
    }

    public RuleChain(Rule head) {
        this.head = head;
    }

    public Rule head() {
        return this.head;
    }

    public void setHead(Rule head) {
        this.head = head;
    }

    public int getLength() {
        int i = 0;
        for (Rule r = this.head(); r != null; r = r.getNext()) {
            ++i;
        }
        return i;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans.rules;

import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.TemplateRule;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.RuleTarget;

public class Rule {
    protected Pattern pattern;
    protected RuleTarget action;
    protected int precedence;
    protected int minImportPrecedence;
    protected double priority;
    protected Rule next;
    protected int sequence;
    protected int part;
    private boolean alwaysMatches;
    private int rank;

    public Rule() {
    }

    public Rule(Pattern p, RuleTarget o, int prec, int min, double prio, int seq, int part) {
        this.pattern = p;
        this.action = o;
        this.precedence = prec;
        this.minImportPrecedence = min;
        this.priority = prio;
        this.next = null;
        this.sequence = seq;
        this.part = part;
        o.registerRule(this);
    }

    protected void copyFrom(Rule r, boolean copyChain) {
        this.pattern = r.pattern.copy(new RebindingMap());
        this.action = r.action instanceof TemplateRule ? ((TemplateRule)r.action).copy() : r.action;
        this.precedence = r.precedence;
        this.minImportPrecedence = r.minImportPrecedence;
        this.priority = r.priority;
        this.sequence = r.sequence;
        this.part = r.part;
        this.next = r.next == null || !copyChain ? null : r.next.copy(true);
        this.action.registerRule(this);
    }

    public Rule copy(boolean copyChain) {
        Rule r2 = new Rule();
        r2.copyFrom(this, copyChain);
        return r2;
    }

    public int getSequence() {
        return this.sequence;
    }

    public int getPartNumber() {
        return this.part;
    }

    public void setAction(RuleTarget action) {
        this.action = action;
    }

    public RuleTarget getAction() {
        return this.action;
    }

    public Rule getNext() {
        return this.next;
    }

    public void setNext(Rule next) {
        this.next = next;
    }

    public Pattern getPattern() {
        return this.pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public int getPrecedence() {
        return this.precedence;
    }

    public int getMinImportPrecedence() {
        return this.minImportPrecedence;
    }

    public double getPriority() {
        return this.priority;
    }

    public void setAlwaysMatches(boolean matches) {
        this.alwaysMatches = matches;
    }

    public boolean isAlwaysMatches() {
        return this.alwaysMatches;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return this.rank;
    }

    public void export(ExpressionPresenter out, boolean modeStreamable) throws XPathException {
        RuleTarget target = this.getAction();
        TemplateRule template = null;
        if (target instanceof TemplateRule) {
            int e;
            template = (TemplateRule)target;
            int s = out.startElement("templateRule");
            out.emitAttribute("prec", this.getPrecedence() + "");
            out.emitAttribute("prio", this.getPriority() + "");
            out.emitAttribute("seq", this.getSequence() + "");
            if (this.part != 0) {
                out.emitAttribute("part", "" + this.part);
            }
            out.emitAttribute("rank", "" + this.getRank());
            out.emitAttribute("minImp", this.getMinImportPrecedence() + "");
            out.emitAttribute("slots", template.getStackFrameMap().getNumberOfVariables() + "");
            out.emitAttribute("matches", this.pattern.getItemType().getFullAlphaCode());
            template.explainProperties(out);
            this.exportOtherProperties(out);
            out.setChildRole("match");
            this.getPattern().export(out);
            if (template.getBody() != null) {
                out.setChildRole("action");
                template.getBody().export(out);
            }
            if (s != (e = out.endElement())) {
                throw new IllegalStateException("exported expression tree unbalanced in template at line " + (template != null ? template.getLineNumber() + " of " + template.getSystemId() : ""));
            }
        } else {
            target.export(out);
        }
    }

    public void exportOtherProperties(ExpressionPresenter out) throws XPathException {
    }

    public int compareRank(Rule other) {
        return this.rank - other.rank;
    }

    public int compareComputedRank(Rule other) {
        if (this.precedence == other.precedence) {
            return Double.compare(this.priority, other.priority);
        }
        if (this.precedence < other.precedence) {
            return -1;
        }
        return 1;
    }

    public boolean matches(Item item, XPathContextMajor context) throws XPathException {
        return this.alwaysMatches || this.pattern.matches(item, context);
    }
}


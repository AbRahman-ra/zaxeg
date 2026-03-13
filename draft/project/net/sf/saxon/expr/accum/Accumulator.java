/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.accum;

import java.util.Map;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.accum.AccumulatorRule;
import net.sf.saxon.expr.instruct.Actor;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.trans.SimpleMode;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.Rule;
import net.sf.saxon.value.SequenceType;

public class Accumulator
extends Actor {
    private StructuredQName accumulatorName;
    private SimpleMode preDescentRules = new SimpleMode(new StructuredQName("saxon", "http://saxon.sf.net/", "preDescent"));
    private SimpleMode postDescentRules = new SimpleMode(new StructuredQName("saxon", "http://saxon.sf.net/", "postDescent"));
    private Expression initialValueExpression;
    private SequenceType type;
    private boolean streamable;
    private boolean universallyApplicable;
    private int importPrecedence;
    private boolean tracing;
    private SlotManager slotManagerForInitialValueExpression;

    public Accumulator() {
        this.body = Literal.makeEmptySequence();
    }

    @Override
    public SymbolicName getSymbolicName() {
        return new SymbolicName(129, this.getAccumulatorName());
    }

    public boolean isDeclaredStreamable() {
        return this.streamable;
    }

    public void setDeclaredStreamable(boolean streamable) {
        this.streamable = streamable;
    }

    public StructuredQName getAccumulatorName() {
        return this.accumulatorName;
    }

    public void setAccumulatorName(StructuredQName firstName) {
        this.accumulatorName = firstName;
    }

    public int getImportPrecedence() {
        return this.importPrecedence;
    }

    public void setImportPrecedence(int importPrecedence) {
        this.importPrecedence = importPrecedence;
    }

    public void setUniversallyApplicable(boolean universal) {
        this.universallyApplicable = universal;
    }

    public boolean isUniversallyApplicable() {
        return this.universallyApplicable;
    }

    public boolean isTracing() {
        return this.tracing;
    }

    public void setTracing(boolean tracing) {
        this.tracing = tracing;
    }

    public SlotManager getSlotManagerForInitialValueExpression() {
        return this.slotManagerForInitialValueExpression;
    }

    public void setSlotManagerForInitialValueExpression(SlotManager slotManagerForInitialValueExpression) {
        this.slotManagerForInitialValueExpression = slotManagerForInitialValueExpression;
    }

    public SimpleMode getPreDescentRules() {
        return this.preDescentRules;
    }

    public void setPreDescentRules(SimpleMode preDescentRules) {
        this.preDescentRules = preDescentRules;
    }

    public SimpleMode getPostDescentRules() {
        return this.postDescentRules;
    }

    public void setPostDescentRules(SimpleMode postDescentRules) {
        this.postDescentRules = postDescentRules;
    }

    public Expression getInitialValueExpression() {
        return this.initialValueExpression;
    }

    public void setInitialValueExpression(Expression initialValueExpression) {
        this.initialValueExpression = initialValueExpression;
    }

    public void addChildExpression(Expression expression) {
        Expression e = Block.makeBlock(this.getBody(), expression);
        this.setBody(e);
    }

    public SequenceType getType() {
        return this.type;
    }

    public void setType(SequenceType type) {
        this.type = type;
    }

    public boolean isCompatible(Accumulator other) {
        return this.getAccumulatorName().equals(other.getAccumulatorName());
    }

    public StructuredQName getObjectName() {
        return this.accumulatorName;
    }

    @Override
    public void export(ExpressionPresenter presenter) throws XPathException {
        this.export(presenter, null);
    }

    public void export(final ExpressionPresenter out, Map<Component, Integer> componentIdMap) throws XPathException {
        out.startElement("accumulator");
        out.emitAttribute("name", this.getObjectName());
        out.emitAttribute("line", this.getLineNumber() + "");
        out.emitAttribute("module", this.getSystemId());
        out.emitAttribute("as", this.type.toAlphaCode());
        out.emitAttribute("streamable", this.streamable ? "1" : "0");
        out.emitAttribute("slots", this.getSlotManagerForInitialValueExpression().getNumberOfVariables() + "");
        if (componentIdMap != null) {
            out.emitAttribute("binds", "" + this.getDeclaringComponent().listComponentReferences(componentIdMap));
        }
        if (this.isUniversallyApplicable()) {
            out.emitAttribute("flags", "u");
        }
        out.setChildRole("init");
        this.initialValueExpression.export(out);
        Mode.RuleAction action = new Mode.RuleAction(){

            @Override
            public void processRule(Rule r) throws XPathException {
                out.startElement("accRule");
                out.emitAttribute("slots", ((AccumulatorRule)r.getAction()).getStackFrameMap().getNumberOfVariables() + "");
                out.emitAttribute("rank", "" + r.getRank());
                if (((AccumulatorRule)r.getAction()).isCapturing()) {
                    out.emitAttribute("flags", "c");
                }
                r.getPattern().export(out);
                r.getAction().export(out);
                out.endElement();
            }
        };
        try {
            out.startElement("pre");
            out.emitAttribute("slots", this.preDescentRules.getStackFrameSlotsNeeded() + "");
            this.preDescentRules.processRules(action);
            out.endElement();
            out.startElement("post");
            out.emitAttribute("slots", this.postDescentRules.getStackFrameSlotsNeeded() + "");
            this.postDescentRules.processRules(action);
            out.endElement();
        } catch (XPathException e) {
            throw new AssertionError((Object)e);
        }
        out.endElement();
    }
}


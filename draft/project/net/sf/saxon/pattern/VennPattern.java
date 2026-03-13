/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import java.util.HashSet;
import java.util.Set;
import net.sf.saxon.expr.LocalBinding;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.NodeTestPattern;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;

public abstract class VennPattern
extends Pattern {
    protected Pattern p1;
    protected Pattern p2;

    public VennPattern(Pattern p1, Pattern p2) {
        this.p1 = p1;
        this.p2 = p2;
        this.adoptChildExpression(p1);
        this.adoptChildExpression(p2);
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operandList(new Operand(this, this.p1, OperandRole.SAME_FOCUS_ACTION), new Operand(this, this.p2, OperandRole.SAME_FOCUS_ACTION));
    }

    @Override
    public Pattern simplify() throws XPathException {
        this.p1 = this.p1.simplify();
        this.p2 = this.p2.simplify();
        return this;
    }

    @Override
    public Pattern typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        this.mustBeNodePattern(this.p1);
        this.p1 = this.p1.typeCheck(visitor, contextItemType);
        this.mustBeNodePattern(this.p2);
        this.p2 = this.p2.typeCheck(visitor, contextItemType);
        return this;
    }

    private void mustBeNodePattern(Pattern p) throws XPathException {
        ItemType it;
        if (p instanceof NodeTestPattern && !((it = p.getItemType()) instanceof NodeTest)) {
            XPathException err = new XPathException("The operands of a union, intersect, or except pattern must be patterns that match nodes", "XPTY0004");
            err.setIsTypeError(true);
            throw err;
        }
    }

    @Override
    public void bindCurrent(LocalBinding binding) {
        this.p1.bindCurrent(binding);
        this.p2.bindCurrent(binding);
    }

    @Override
    public boolean isMotionless() {
        return this.p1.isMotionless() && this.p2.isMotionless();
    }

    @Override
    public int allocateSlots(SlotManager slotManager, int nextFree) {
        nextFree = this.p1.allocateSlots(slotManager, nextFree);
        nextFree = this.p2.allocateSlots(slotManager, nextFree);
        return nextFree;
    }

    public void gatherComponentPatterns(Set<Pattern> set) {
        if (this.p1 instanceof VennPattern) {
            ((VennPattern)this.p1).gatherComponentPatterns(set);
        } else {
            set.add(this.p1);
        }
        if (this.p2 instanceof VennPattern) {
            ((VennPattern)this.p2).gatherComponentPatterns(set);
        } else {
            set.add(this.p2);
        }
    }

    @Override
    public int getDependencies() {
        return this.p1.getDependencies() | this.p2.getDependencies();
    }

    public Pattern getLHS() {
        return this.p1;
    }

    public Pattern getRHS() {
        return this.p2;
    }

    @Override
    public boolean matchesCurrentGroup() {
        return this.p1.matchesCurrentGroup() || this.p2.matchesCurrentGroup();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof VennPattern) {
            HashSet<Pattern> s0 = new HashSet<Pattern>(10);
            this.gatherComponentPatterns(s0);
            HashSet<Pattern> s1 = new HashSet<Pattern>(10);
            ((VennPattern)other).gatherComponentPatterns(s1);
            return s0.equals(s1);
        }
        return false;
    }

    @Override
    public int computeHashCode() {
        return 0x9BD723A6 ^ this.p1.hashCode() ^ this.p2.hashCode();
    }

    protected abstract String getOperatorName();

    @Override
    public String reconstruct() {
        return this.p1 + " " + this.getOperatorName() + " " + this.p2;
    }

    @Override
    public void export(ExpressionPresenter presenter) throws XPathException {
        presenter.startElement("p.venn");
        presenter.emitAttribute("op", this.getOperatorName());
        this.p1.export(presenter);
        this.p2.export(presenter);
        presenter.endElement();
    }
}


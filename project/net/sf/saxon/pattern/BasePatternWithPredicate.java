/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LocalBinding;
import net.sf.saxon.expr.LocalVariableReference;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMinor;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.functions.Current;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.pattern.PatternWithPredicate;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ManualIterator;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.UType;

public class BasePatternWithPredicate
extends Pattern
implements PatternWithPredicate {
    Operand basePatternOp;
    Operand predicateOp;

    public BasePatternWithPredicate(Pattern basePattern, Expression predicate) {
        this.basePatternOp = new Operand(this, basePattern, OperandRole.ATOMIC_SEQUENCE);
        this.predicateOp = new Operand(this, predicate, OperandRole.FOCUS_CONTROLLED_ACTION);
        this.adoptChildExpression(this.getBasePattern());
        this.adoptChildExpression(this.getPredicate());
    }

    @Override
    public Expression getPredicate() {
        return this.predicateOp.getChildExpression();
    }

    public Pattern getBasePattern() {
        return (Pattern)this.basePatternOp.getChildExpression();
    }

    @Override
    public void bindCurrent(LocalBinding binding) {
        Expression predicate = this.getPredicate();
        if (predicate.isCallOn(Current.class)) {
            this.predicateOp.setChildExpression(new LocalVariableReference(binding));
        } else if (ExpressionTool.callsFunction(predicate, Current.FN_CURRENT, false)) {
            BasePatternWithPredicate.replaceCurrent(predicate, binding);
        }
        this.getBasePattern().bindCurrent(binding);
    }

    @Override
    public boolean matchesCurrentGroup() {
        return this.getBasePattern().matchesCurrentGroup();
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operandList(this.basePatternOp, this.predicateOp);
    }

    @Override
    public int allocateSlots(SlotManager slotManager, int nextFree) {
        int n = ExpressionTool.allocateSlots(this.getPredicate(), nextFree, slotManager);
        return this.getBasePattern().allocateSlots(slotManager, n);
    }

    @Override
    public boolean matches(Item item, XPathContext context) throws XPathException {
        if (!this.getBasePattern().matches(item, context)) {
            return false;
        }
        return this.matchesPredicate(item, context);
    }

    private boolean matchesPredicate(Item item, XPathContext context) throws XPathException {
        XPathContextMinor c2 = context.newMinorContext();
        ManualIterator si = new ManualIterator(item);
        c2.setCurrentIterator(si);
        c2.setCurrentOutputUri(null);
        try {
            return this.getPredicate().effectiveBooleanValue(c2);
        } catch (XPathException.Circularity | XPathException.StackOverflow e) {
            throw e;
        } catch (XPathException ex) {
            this.handleDynamicError(ex, c2);
            return false;
        }
    }

    @Override
    public boolean matchesBeneathAnchor(NodeInfo node, NodeInfo anchor, XPathContext context) throws XPathException {
        return this.getBasePattern().matchesBeneathAnchor(node, anchor, context) && this.matchesPredicate(node, context);
    }

    @Override
    public UType getUType() {
        return this.getBasePattern().getUType();
    }

    @Override
    public int getFingerprint() {
        return this.getBasePattern().getFingerprint();
    }

    @Override
    public ItemType getItemType() {
        return this.getBasePattern().getItemType();
    }

    @Override
    public int getDependencies() {
        return this.getPredicate().getDependencies();
    }

    @Override
    public Pattern typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        this.basePatternOp.setChildExpression(this.getBasePattern().typeCheck(visitor, contextItemType));
        ContextItemStaticInfo cit = visitor.getConfiguration().makeContextItemStaticInfo(this.getBasePattern().getItemType(), false);
        this.predicateOp.setChildExpression(this.getPredicate().typeCheck(visitor, cit));
        return this;
    }

    @Override
    public Pattern optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.basePatternOp.setChildExpression(this.getBasePattern().optimize(visitor, contextInfo));
        ContextItemStaticInfo cit = visitor.getConfiguration().makeContextItemStaticInfo(this.getBasePattern().getItemType(), false);
        this.predicateOp.setChildExpression(this.getPredicate().optimize(visitor, cit));
        this.predicateOp.setChildExpression(visitor.obtainOptimizer().eliminateCommonSubexpressions(this.getPredicate()));
        return this;
    }

    @Override
    public Pattern convertToTypedPattern(String val) throws XPathException {
        Pattern b2 = this.getBasePattern().convertToTypedPattern(val);
        if (b2 == this.getBasePattern()) {
            return this;
        }
        return new BasePatternWithPredicate(b2, this.getPredicate());
    }

    @Override
    public String reconstruct() {
        return this.getBasePattern() + "[" + this.getPredicate() + "]";
    }

    @Override
    public String toShortString() {
        return this.getBasePattern().toShortString() + "[" + this.getPredicate().toShortString() + "]";
    }

    @Override
    public Pattern copy(RebindingMap rebindings) {
        BasePatternWithPredicate n = new BasePatternWithPredicate(this.getBasePattern().copy(rebindings), this.getPredicate().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, n);
        n.setOriginalText(this.getOriginalText());
        return n;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BasePatternWithPredicate && ((BasePatternWithPredicate)obj).getBasePattern().isEqual(this.getBasePattern()) && ((BasePatternWithPredicate)obj).getPredicate().isEqual(this.getPredicate());
    }

    @Override
    public int computeHashCode() {
        return this.getBasePattern().hashCode() ^ this.getPredicate().hashCode();
    }

    @Override
    public void export(ExpressionPresenter presenter) throws XPathException {
        presenter.startElement("p.withPredicate");
        this.getBasePattern().export(presenter);
        this.getPredicate().export(presenter);
        presenter.endElement();
    }
}


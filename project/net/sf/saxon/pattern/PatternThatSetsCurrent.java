/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.LocalBinding;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.LocalVariableBinding;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.functions.Current;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.SequenceType;

public class PatternThatSetsCurrent
extends Pattern {
    private LocalVariableBinding binding;
    private Pattern wrappedPattern;

    public PatternThatSetsCurrent(Pattern wrappedPattern) {
        this(wrappedPattern, new LocalVariableBinding(Current.FN_CURRENT, SequenceType.SINGLE_ITEM));
    }

    public PatternThatSetsCurrent(Pattern wrappedPattern, LocalVariableBinding binding) {
        this.wrappedPattern = wrappedPattern;
        this.binding = binding;
        binding.setRequiredType(SequenceType.makeSequenceType(wrappedPattern.getItemType(), 16384));
        this.adoptChildExpression(wrappedPattern);
        this.setPriority(wrappedPattern.getDefaultPriority());
    }

    @Override
    public Iterable<Operand> operands() {
        return new Operand(this, this.wrappedPattern, OperandRole.SINGLE_ATOMIC);
    }

    public LocalBinding getCurrentBinding() {
        return this.binding;
    }

    @Override
    public boolean hasVariableBinding(Binding binding) {
        return binding == this.binding;
    }

    @Override
    public int allocateSlots(SlotManager slotManager, int nextFree) {
        slotManager.allocateSlotNumber(Current.FN_CURRENT);
        this.binding.setSlotNumber(nextFree++);
        return this.wrappedPattern.allocateSlots(slotManager, nextFree);
    }

    @Override
    public boolean matches(Item item, XPathContext context) throws XPathException {
        context.setLocalVariable(this.binding.getLocalSlotNumber(), item);
        return this.wrappedPattern.matches(item, context);
    }

    @Override
    public ItemType getItemType() {
        return this.wrappedPattern.getItemType();
    }

    @Override
    public Pattern simplify() throws XPathException {
        this.wrappedPattern = this.wrappedPattern.simplify();
        return this;
    }

    @Override
    public Pattern typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        this.wrappedPattern = this.wrappedPattern.typeCheck(visitor, contextItemType);
        return this;
    }

    @Override
    public UType getUType() {
        return this.wrappedPattern.getUType();
    }

    @Override
    public int getFingerprint() {
        return this.wrappedPattern.getFingerprint();
    }

    @Override
    public String reconstruct() {
        return this.wrappedPattern.toString();
    }

    @Override
    public HostLanguage getHostLanguage() {
        return this.wrappedPattern.getHostLanguage();
    }

    @Override
    public boolean isMotionless() {
        return this.wrappedPattern.isMotionless();
    }

    @Override
    public boolean matchesBeneathAnchor(NodeInfo node, NodeInfo anchor, XPathContext context) throws XPathException {
        return this.wrappedPattern.matchesBeneathAnchor(node, anchor, context);
    }

    @Override
    public Pattern convertToTypedPattern(String val) throws XPathException {
        Pattern w2 = this.wrappedPattern.convertToTypedPattern(val);
        if (w2 == this.wrappedPattern) {
            return this;
        }
        return new PatternThatSetsCurrent(w2);
    }

    @Override
    public Pattern copy(RebindingMap rebindings) {
        LocalVariableBinding newCurrent = new LocalVariableBinding(Current.FN_CURRENT, SequenceType.SINGLE_ITEM);
        rebindings.put(this.binding, newCurrent);
        PatternThatSetsCurrent n = new PatternThatSetsCurrent(this.wrappedPattern.copy(rebindings), newCurrent);
        ExpressionTool.copyLocationInfo(this, n);
        n.setOriginalText(this.getOriginalText());
        return n;
    }

    @Override
    public void export(ExpressionPresenter presenter) throws XPathException {
        presenter.startElement("p.withCurrent");
        this.wrappedPattern.export(presenter);
        presenter.endElement();
    }

    public Pattern getWrappedPattern() {
        return this.wrappedPattern;
    }
}


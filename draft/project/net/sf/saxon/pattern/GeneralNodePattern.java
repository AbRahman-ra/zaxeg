/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
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
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.pattern.PatternMaker;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.ManualIterator;
import net.sf.saxon.type.AlphaCode;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;

public final class GeneralNodePattern
extends Pattern {
    private Expression equivalentExpr = null;
    private NodeTest itemType = null;

    public GeneralNodePattern(Expression expr, NodeTest itemType) {
        this.equivalentExpr = expr;
        this.itemType = itemType;
    }

    @Override
    public Iterable<Operand> operands() {
        return new Operand(this, this.equivalentExpr, OperandRole.SAME_FOCUS_ACTION);
    }

    @Override
    public boolean isMotionless() {
        return false;
    }

    @Override
    public Pattern typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        ContextItemStaticInfo cit = visitor.getConfiguration().getDefaultContextItemStaticInfo();
        this.equivalentExpr = this.equivalentExpr.typeCheck(visitor, cit);
        return this;
    }

    @Override
    public Pattern optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Configuration config = visitor.getConfiguration();
        ContextItemStaticInfo defaultInfo = config.getDefaultContextItemStaticInfo();
        this.equivalentExpr = this.equivalentExpr.optimize(visitor, defaultInfo);
        if (this.equivalentExpr instanceof FilterExpression && !((FilterExpression)this.equivalentExpr).isFilterIsPositional()) {
            try {
                return PatternMaker.fromExpression(this.equivalentExpr, config, true).typeCheck(visitor, defaultInfo);
            } catch (XPathException xPathException) {
                // empty catch block
            }
        }
        return this;
    }

    @Override
    public int getDependencies() {
        return this.equivalentExpr.getDependencies() & 0x180;
    }

    @Override
    public void bindCurrent(LocalBinding binding) {
        if (ExpressionTool.callsFunction(this.equivalentExpr, Current.FN_CURRENT, false)) {
            if (this.equivalentExpr.isCallOn(Current.class)) {
                this.equivalentExpr = new LocalVariableReference(binding);
            } else {
                GeneralNodePattern.replaceCurrent(this.equivalentExpr, binding);
            }
        }
    }

    @Override
    public int allocateSlots(SlotManager slotManager, int nextFree) {
        return ExpressionTool.allocateSlots(this.equivalentExpr, nextFree, slotManager);
    }

    @Override
    public boolean matches(Item item, XPathContext context) throws XPathException {
        NodeInfo a;
        TypeHierarchy th = context.getConfiguration().getTypeHierarchy();
        if (!this.itemType.matches(item, th)) {
            return false;
        }
        AxisIterator anc = ((NodeInfo)item).iterateAxis(1);
        do {
            if ((a = anc.next()) != null) continue;
            return false;
        } while (!this.matchesBeneathAnchor((NodeInfo)item, a, context));
        return true;
    }

    @Override
    public boolean matchesBeneathAnchor(NodeInfo node, NodeInfo anchor, XPathContext context) throws XPathException {
        if (!this.itemType.test(node)) {
            return false;
        }
        if (anchor == null) {
            NodeInfo ancestor;
            AxisIterator ancestors = node.iterateAxis(1);
            do {
                if ((ancestor = ancestors.next()) != null) continue;
                return false;
            } while (!this.matchesBeneathAnchor(node, ancestor, context));
            return true;
        }
        XPathContextMinor c2 = context.newMinorContext();
        ManualIterator iter = new ManualIterator(anchor);
        c2.setCurrentIterator(iter);
        try {
            NodeInfo n;
            SequenceIterator nsv = this.equivalentExpr.iterate(c2);
            do {
                if ((n = (NodeInfo)nsv.next()) != null) continue;
                return false;
            } while (!n.equals(node));
            return true;
        } catch (XPathException.Circularity | XPathException.StackOverflow e) {
            throw e;
        } catch (XPathException e) {
            this.handleDynamicError(e, c2);
            return false;
        }
    }

    @Override
    public UType getUType() {
        return this.itemType.getUType();
    }

    @Override
    public int getFingerprint() {
        return this.itemType.getFingerprint();
    }

    @Override
    public ItemType getItemType() {
        return this.itemType;
    }

    public Expression getEquivalentExpr() {
        return this.equivalentExpr;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof GeneralNodePattern) {
            GeneralNodePattern lpp = (GeneralNodePattern)other;
            return this.equivalentExpr.isEqual(lpp.equivalentExpr);
        }
        return false;
    }

    @Override
    public int computeHashCode() {
        return 0x146B9 ^ this.equivalentExpr.hashCode();
    }

    @Override
    public Pattern copy(RebindingMap rebindings) {
        GeneralNodePattern n = new GeneralNodePattern(this.equivalentExpr.copy(rebindings), this.itemType);
        ExpressionTool.copyLocationInfo(this, n);
        n.setOriginalText(this.getOriginalText());
        return n;
    }

    @Override
    public void export(ExpressionPresenter presenter) throws XPathException {
        presenter.startElement("p.genNode");
        presenter.emitAttribute("test", AlphaCode.fromItemType(this.itemType));
        this.equivalentExpr.export(presenter);
        presenter.endElement();
    }
}


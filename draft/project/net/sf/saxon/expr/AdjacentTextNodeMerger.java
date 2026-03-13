/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.EmptyTextNodeRemover;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.UnaryExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.Choose;
import net.sf.saxon.expr.instruct.ValueOf;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AdjacentTextNodeMergingIterator;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Cardinality;

public class AdjacentTextNodeMerger
extends UnaryExpression {
    public AdjacentTextNodeMerger(Expression p0) {
        super(p0);
    }

    public static Expression makeAdjacentTextNodeMerger(Expression base) {
        if (base instanceof Literal && ((Literal)base).getValue() instanceof AtomicSequence) {
            return base;
        }
        return new AdjacentTextNodeMerger(base);
    }

    @Override
    protected OperandRole getOperandRole() {
        return OperandRole.SAME_FOCUS_ACTION;
    }

    @Override
    public Expression simplify() throws XPathException {
        Expression operand = this.getBaseExpression();
        if (operand instanceof Literal && ((Literal)operand).getValue() instanceof AtomicValue) {
            return operand;
        }
        return super.simplify();
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getOperand().typeCheck(visitor, contextInfo);
        TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
        if (th.relationship(this.getBaseExpression().getItemType(), NodeKindTest.TEXT) == Affinity.DISJOINT) {
            Expression base = this.getBaseExpression();
            base.setParentExpression(this.getParentExpression());
            return base;
        }
        if (!Cardinality.allowsMany(this.getBaseExpression().getCardinality())) {
            Expression base = this.getBaseExpression();
            base.setParentExpression(this.getParentExpression());
            return base;
        }
        if (this.getBaseExpression() instanceof Choose) {
            Choose choose = (Choose)this.getBaseExpression();
            for (int i = 0; i < choose.size(); ++i) {
                AdjacentTextNodeMerger atm2 = new AdjacentTextNodeMerger(choose.getAction(i));
                choose.setAction(i, atm2.typeCheck(visitor, contextInfo));
            }
            return choose;
        }
        if (this.getBaseExpression() instanceof Block) {
            Block block = (Block)this.getBaseExpression();
            Operand[] actions = block.getOperanda();
            boolean prevtext = false;
            boolean needed = false;
            boolean maybeEmpty = false;
            for (Operand o : actions) {
                boolean maybetext;
                Expression action = o.getChildExpression();
                if (action instanceof ValueOf) {
                    maybetext = true;
                    Expression content = ((ValueOf)action).getSelect();
                    maybeEmpty = content instanceof StringLiteral ? (maybeEmpty |= ((StringLiteral)content).getStringValue().isEmpty()) : true;
                } else {
                    maybetext = th.relationship(action.getItemType(), NodeKindTest.TEXT) != Affinity.DISJOINT;
                    maybeEmpty |= maybetext;
                }
                if (prevtext && maybetext) {
                    needed = true;
                    break;
                }
                if (maybetext && Cardinality.allowsMany(action.getCardinality())) {
                    needed = true;
                    break;
                }
                prevtext = maybetext;
            }
            if (!needed) {
                if (maybeEmpty) {
                    return new EmptyTextNodeRemover(block);
                }
                return block;
            }
        }
        return this;
    }

    @Override
    public ItemType getItemType() {
        return this.getBaseExpression().getItemType();
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        return this.getBaseExpression().getStaticUType(contextItemType);
    }

    @Override
    public int computeCardinality() {
        return this.getBaseExpression().getCardinality() | 0x2000;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        AdjacentTextNodeMerger a2 = new AdjacentTextNodeMerger(this.getBaseExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, a2);
        return a2;
    }

    @Override
    public int getImplementationMethod() {
        return 30;
    }

    @Override
    public String getStreamerName() {
        return "AdjacentTextNodeMerger";
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        return new AdjacentTextNodeMergingIterator(this.getBaseExpression().iterate(context));
    }

    @Override
    public String getExpressionName() {
        return "mergeAdj";
    }

    public static boolean isTextNode(Item item) {
        return item instanceof NodeInfo && ((NodeInfo)item).getNodeKind() == 3;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.SignificantItemDetector;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.OnEmptyExpr;
import net.sf.saxon.expr.instruct.OnNonEmptyExpr;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Action;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.Cardinality;

public class ConditionalBlock
extends Instruction {
    private Operand[] operanda;
    private boolean allNodesUntyped;

    public ConditionalBlock(Expression[] children) {
        this.operanda = new Operand[children.length];
        for (int i = 0; i < children.length; ++i) {
            this.operanda[i] = new Operand(this, children[i], OperandRole.SAME_FOCUS_ACTION);
        }
    }

    public ConditionalBlock(List<Expression> children) {
        this(children.toArray(new Expression[children.size()]));
    }

    public Expression getChildExpression(int n) {
        return this.operanda[n].getChildExpression();
    }

    public int size() {
        return this.operanda.length;
    }

    @Override
    public Iterable<Operand> operands() {
        return Arrays.asList(this.operanda);
    }

    @Override
    public String getExpressionName() {
        return "condSeq";
    }

    @Override
    public int computeSpecialProperties() {
        if (this.size() == 0) {
            return 0xDFF0000;
        }
        int p = super.computeSpecialProperties();
        if (this.allNodesUntyped) {
            p |= 0x8000000;
        }
        boolean allAxisExpressions = true;
        boolean allChildAxis = true;
        boolean allSubtreeAxis = true;
        for (Operand o : this.operands()) {
            Expression child = o.getChildExpression();
            if (!(child instanceof AxisExpression)) {
                allAxisExpressions = false;
                allChildAxis = false;
                allSubtreeAxis = false;
                break;
            }
            int axis = ((AxisExpression)child).getAxis();
            if (axis != 3) {
                allChildAxis = false;
            }
            if (AxisInfo.isSubtreeAxis[axis]) continue;
            allSubtreeAxis = false;
        }
        if (allAxisExpressions) {
            p |= 0x1810000;
            if (allChildAxis) {
                p |= 0x80000;
            }
            if (allSubtreeAxis) {
                p |= 0x100000;
            }
            if (this.size() == 2 && ((AxisExpression)this.getChildExpression(0)).getAxis() == 2 && ((AxisExpression)this.getChildExpression(1)).getAxis() == 3) {
                p |= 0x20000;
            }
        }
        return p;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        Expression[] c2 = new Expression[this.size()];
        for (int c = 0; c < this.size(); ++c) {
            c2[c] = this.getChildExpression(c).copy(rebindings);
        }
        ConditionalBlock b2 = new ConditionalBlock(c2);
        for (int c = 0; c < this.size(); ++c) {
            b2.adoptChildExpression(c2[c]);
        }
        b2.allNodesUntyped = this.allNodesUntyped;
        ExpressionTool.copyLocationInfo(this, b2);
        return b2;
    }

    @Override
    public final ItemType getItemType() {
        if (this.size() == 0) {
            return ErrorType.getInstance();
        }
        ItemType t1 = this.getChildExpression(0).getItemType();
        TypeHierarchy th = this.getConfiguration().getTypeHierarchy();
        for (int i = 1; i < this.size(); ++i) {
            if (!((t1 = Type.getCommonSuperType(t1, this.getChildExpression(i).getItemType(), th)) instanceof AnyItemType)) continue;
            return t1;
        }
        return t1;
    }

    @Override
    public final int getCardinality() {
        if (this.size() == 0) {
            return 8192;
        }
        int c1 = this.getChildExpression(0).getCardinality();
        for (int i = 1; i < this.size() && (c1 = Cardinality.sum(c1, this.getChildExpression(i).getCardinality())) != 57344; ++i) {
        }
        return c1;
    }

    @Override
    public final boolean mayCreateNewNodes() {
        return this.someOperandCreatesNewNodes();
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.typeCheckChildren(visitor, contextInfo);
        if (Block.neverReturnsTypedNodes(this, visitor.getConfiguration().getTypeHierarchy())) {
            this.resetLocalStaticProperties();
            this.allNodesUntyped = true;
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        int c;
        Expression e = super.optimize(visitor, contextInfo);
        if (e != this) {
            return e;
        }
        int lastOrdinaryInstruction = -1;
        boolean alwaysNonEmpty = false;
        boolean alwaysEmpty = true;
        for (int c2 = 0; c2 < this.size(); ++c2) {
            if (this.getChildExpression(c2) instanceof OnEmptyExpr || this.getChildExpression(c2) instanceof OnNonEmptyExpr) continue;
            lastOrdinaryInstruction = c2;
            if (this.getChildExpression(c2).getItemType().getUType().intersection(UType.DOCUMENT.union(UType.TEXT)).equals(UType.VOID)) {
                int card = this.getChildExpression(c2).getCardinality();
                if (!Cardinality.allowsZero(card)) {
                    alwaysNonEmpty = true;
                }
                if (card == 8192) continue;
                alwaysEmpty = false;
                continue;
            }
            alwaysEmpty = false;
            alwaysNonEmpty = false;
            break;
        }
        if (alwaysEmpty) {
            visitor.getStaticContext().issueWarning("The result of the sequence constructor will always be empty, so xsl:on-empty instructions will always be evaluated, and xsl:on-non-empty instructions will never be evaluated", this.getLocation());
            ArrayList<Expression> retain = new ArrayList<Expression>();
            for (c = 0; c < this.size(); ++c) {
                if (this.getChildExpression(c) instanceof OnNonEmptyExpr) continue;
                if (this.getChildExpression(c) instanceof OnEmptyExpr) {
                    retain.add(((OnEmptyExpr)this.getChildExpression(c)).getBaseExpression());
                    continue;
                }
                retain.add(this.getChildExpression(c));
            }
            return Block.makeBlock(retain);
        }
        if (alwaysNonEmpty) {
            visitor.getStaticContext().issueWarning("The result of the sequence constructor will never be empty, so xsl:on-empty instructions will never be evaluated, and xsl:on-non-empty instructions will always be evaluated", this.getLocation());
            ArrayList<Expression> retain = new ArrayList<Expression>();
            for (c = 0; c < this.size(); ++c) {
                if (this.getChildExpression(c) instanceof OnEmptyExpr) continue;
                if (this.getChildExpression(c) instanceof OnNonEmptyExpr) {
                    retain.add(((OnNonEmptyExpr)this.getChildExpression(c)).getBaseExpression());
                    continue;
                }
                retain.add(this.getChildExpression(c));
            }
            return Block.makeBlock(retain);
        }
        if (lastOrdinaryInstruction == -1) {
            ArrayList<Expression> retain = new ArrayList<Expression>();
            for (c = 0; c < this.size(); ++c) {
                if (!(this.getChildExpression(c) instanceof OnEmptyExpr)) continue;
                retain.add(((OnEmptyExpr)this.getChildExpression(c)).getBaseExpression());
            }
            return Block.makeBlock(retain);
        }
        return this;
    }

    @Override
    public void checkPermittedContents(SchemaType parentType, boolean whole) throws XPathException {
        for (Operand o : this.operands()) {
            Expression child = o.getChildExpression();
            child.checkPermittedContents(parentType, false);
        }
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("condSeq", this);
        for (Operand o : this.operands()) {
            Expression child = o.getChildExpression();
            child.export(out);
        }
        out.endElement();
    }

    @Override
    public String toShortString() {
        return "(" + this.getChildExpression(0).toShortString() + ", ...)";
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        Expression child;
        ArrayList<OnNonEmptyExpr> onNonEmptyPending = new ArrayList<OnNonEmptyExpr>();
        Action action = () -> {
            for (Expression e : onNonEmptyPending) {
                e.process(output, context);
            }
        };
        SignificantItemDetector significantItemDetector = new SignificantItemDetector(output, action);
        for (Operand o : this.operands()) {
            child = o.getChildExpression();
            try {
                if (child instanceof OnEmptyExpr) continue;
                if (child instanceof OnNonEmptyExpr) {
                    if (significantItemDetector.isEmpty()) {
                        onNonEmptyPending.add((OnNonEmptyExpr)child);
                        continue;
                    }
                    child.process(output, context);
                    continue;
                }
                child.process(significantItemDetector, context);
            } catch (XPathException e) {
                e.maybeSetLocation(child.getLocation());
                e.maybeSetContext(context);
                throw e;
            }
        }
        if (significantItemDetector.isEmpty()) {
            for (Operand o : this.operands()) {
                child = o.getChildExpression();
                if (!(child instanceof OnEmptyExpr)) continue;
                child.process(output, context);
            }
        }
        return null;
    }

    @Override
    public int getImplementationMethod() {
        return 4;
    }

    @Override
    public String getStreamerName() {
        return "ConditionalBlock";
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LocalVariableReference;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.Tuple;
import net.sf.saxon.expr.oper.OperandArray;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;

public class TupleExpression
extends Expression {
    private OperandArray operanda;

    protected void setOperanda(OperandArray operanda) {
        this.operanda = operanda;
    }

    protected OperandArray getOperanda() {
        return this.operanda;
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operanda.operands();
    }

    public void setVariables(List<LocalVariableReference> refs) {
        Expression[] e = new Expression[refs.size()];
        for (int i = 0; i < refs.size(); ++i) {
            e[i] = refs.get(i);
        }
        this.setOperanda(new OperandArray((Expression)this, e, OperandRole.SAME_FOCUS_ACTION));
    }

    public int getSize() {
        return this.getOperanda().getNumberOfOperands();
    }

    public LocalVariableReference getSlot(int i) {
        return (LocalVariableReference)this.getOperanda().getOperandExpression(i);
    }

    public void setSlot(int i, LocalVariableReference ref) {
        this.getOperanda().setOperand(i, ref);
    }

    public boolean includesBinding(Binding binding) {
        for (Operand o : this.operands()) {
            if (((LocalVariableReference)o.getChildExpression()).getBinding() != binding) continue;
            return true;
        }
        return false;
    }

    @Override
    public ItemType getItemType() {
        return this.getConfiguration().getJavaExternalObjectType(Object.class);
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        for (int i = 0; i < this.getSize(); ++i) {
            this.operanda.getOperand(i).typeCheck(visitor, contextInfo);
        }
        return this;
    }

    @Override
    public int getImplementationMethod() {
        return 1;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TupleExpression)) {
            return false;
        }
        TupleExpression t2 = (TupleExpression)other;
        if (this.getOperanda().getNumberOfOperands() != t2.getOperanda().getNumberOfOperands()) {
            return false;
        }
        for (int i = 0; i < this.getSize(); ++i) {
            if (this.getSlot(i).isEqual(t2.getSlot(i))) continue;
            return false;
        }
        return true;
    }

    @Override
    public int computeHashCode() {
        int h = 77;
        for (Operand o : this.operands()) {
            h ^= o.getChildExpression().hashCode();
        }
        return h;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        int n = this.getOperanda().getNumberOfOperands();
        ArrayList<LocalVariableReference> refs2 = new ArrayList<LocalVariableReference>(n);
        for (int i = 0; i < n; ++i) {
            refs2.add((LocalVariableReference)this.getSlot(i).copy(rebindings));
        }
        TupleExpression t2 = new TupleExpression();
        ExpressionTool.copyLocationInfo(this, t2);
        t2.setVariables(refs2);
        return t2;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("tuple", this);
        for (Operand o : this.operands()) {
            o.getChildExpression().export(out);
        }
        out.endElement();
    }

    @Override
    public Tuple evaluateItem(XPathContext context) throws XPathException {
        int n = this.getSize();
        Sequence[] tuple = new Sequence[n];
        for (int i = 0; i < n; ++i) {
            tuple[i] = this.getSlot(i).evaluateVariable(context);
        }
        return new Tuple(tuple);
    }

    @Override
    public String getExpressionName() {
        return "tuple";
    }

    public void setCurrentTuple(XPathContext context, Tuple tuple) throws XPathException {
        Sequence[] members = tuple.getMembers();
        int n = this.getSize();
        for (int i = 0; i < n; ++i) {
            context.setLocalVariable(this.getSlot(i).getBinding().getLocalSlotNumber(), members[i]);
        }
    }

    @Override
    public int computeCardinality() {
        return 16384;
    }

    @Override
    public int getIntrinsicDependencies() {
        return 0;
    }
}


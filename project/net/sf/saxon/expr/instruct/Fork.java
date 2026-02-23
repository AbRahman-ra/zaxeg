/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.Arrays;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;

public class Fork
extends Instruction {
    Operand[] operanda;

    public Fork(Operand[] prongs) {
        this.operanda = new Operand[prongs.length];
        for (int i = 0; i < prongs.length; ++i) {
            this.operanda[i] = new Operand(this, prongs[i].getChildExpression(), OperandRole.SAME_FOCUS_ACTION);
        }
    }

    public Fork(Expression[] prongs) {
        this.operanda = new Operand[prongs.length];
        for (int i = 0; i < prongs.length; ++i) {
            this.operanda[i] = new Operand(this, prongs[i], OperandRole.SAME_FOCUS_ACTION);
        }
    }

    @Override
    public Iterable<Operand> operands() {
        return Arrays.asList(this.operanda);
    }

    @Override
    public int getInstructionNameCode() {
        return 156;
    }

    public int getSize() {
        return this.operanda.length;
    }

    public Expression getProng(int i) {
        return this.operanda[i].getChildExpression();
    }

    @Override
    public ItemType getItemType() {
        if (this.getSize() == 0) {
            return ErrorType.getInstance();
        }
        ItemType t1 = null;
        for (Operand o : this.operands()) {
            ItemType t2 = o.getChildExpression().getItemType();
            if (!((t1 = t1 == null ? t2 : Type.getCommonSuperType(t1, t2)) instanceof AnyItemType)) continue;
            return t1;
        }
        return t1;
    }

    @Override
    public String getStreamerName() {
        return "Fork";
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        Expression[] e2 = new Expression[this.getSize()];
        int i = 0;
        for (Operand o : this.operands()) {
            e2[i++] = o.getChildExpression().copy(rebindings);
        }
        Fork f2 = new Fork(e2);
        ExpressionTool.copyLocationInfo(this, f2);
        return f2;
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        for (Operand o : this.operands()) {
            o.getChildExpression().process(output, context);
        }
        return null;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("fork", this);
        for (Operand o : this.operands()) {
            o.getChildExpression().export(out);
        }
        out.endElement();
    }
}


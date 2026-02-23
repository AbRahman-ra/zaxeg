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
import net.sf.saxon.expr.instruct.LocalParam;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;

public class LocalParamBlock
extends Instruction {
    Operand[] operanda;

    public LocalParamBlock(LocalParam[] params) {
        this.operanda = new Operand[params.length];
        for (int i = 0; i < params.length; ++i) {
            this.operanda[i] = new Operand(this, params[i], OperandRole.NAVIGATE);
        }
    }

    @Override
    public String getExpressionName() {
        return "params";
    }

    @Override
    public Iterable<Operand> operands() {
        return Arrays.asList(this.operanda);
    }

    public int getNumberOfParams() {
        return this.operanda.length;
    }

    @Override
    public int computeSpecialProperties() {
        return 0;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        LocalParam[] lps2 = new LocalParam[this.getNumberOfParams()];
        int i = 0;
        for (Operand o : this.operands()) {
            LocalParam oldLps = (LocalParam)o.getChildExpression();
            LocalParam newLps = oldLps.copy(rebindings);
            rebindings.put(oldLps, newLps);
            lps2[i++] = newLps;
        }
        return new LocalParamBlock(lps2);
    }

    @Override
    public final ItemType getItemType() {
        return ErrorType.getInstance();
    }

    @Override
    public final int getCardinality() {
        return 8192;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("params", this);
        for (Operand o : this.operands()) {
            o.getChildExpression().export(out);
        }
        out.endElement();
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        for (Operand o : this.operands()) {
            LocalParam param = (LocalParam)o.getChildExpression();
            try {
                context.setLocalVariable(param.getSlotNumber(), param.getSelectValue(context));
            } catch (XPathException e) {
                e.maybeSetLocation(param.getLocation());
                e.maybeSetContext(context);
                throw e;
            }
        }
        return null;
    }

    @Override
    public int getImplementationMethod() {
        return 4;
    }
}


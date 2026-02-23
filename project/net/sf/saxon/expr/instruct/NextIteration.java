/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.ArrayList;
import java.util.Arrays;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.TailCallLoop;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.instruct.WithParam;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;

public class NextIteration
extends Instruction
implements TailCallLoop.TailCallInfo {
    private WithParam[] actualParams = null;

    public void setParameters(WithParam[] actualParams) {
        this.actualParams = actualParams;
    }

    public WithParam[] getParameters() {
        return this.actualParams;
    }

    @Override
    public boolean isLiftable(boolean forStreaming) {
        return false;
    }

    @Override
    public int getInstructionNameCode() {
        return 177;
    }

    @Override
    public Expression simplify() throws XPathException {
        WithParam.simplify(this.actualParams);
        return this;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        WithParam.typeCheck(this.actualParams, visitor, contextInfo);
        return this;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        NextIteration c2 = new NextIteration();
        ExpressionTool.copyLocationInfo(this, c2);
        c2.actualParams = WithParam.copy(c2, this.actualParams, rebindings);
        return c2;
    }

    @Override
    public Iterable<Operand> operands() {
        ArrayList<Operand> list = new ArrayList<Operand>();
        WithParam.gatherOperands(this, this.actualParams, list);
        return list;
    }

    @Override
    public String getStreamerName() {
        return "NextIteration";
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        XPathContext c = context;
        while (!(c instanceof XPathContextMajor)) {
            c = c.getCaller();
        }
        XPathContextMajor cm = (XPathContextMajor)c;
        if (this.actualParams.length == 1) {
            cm.setLocalVariable(this.actualParams[0].getSlotNumber(), this.actualParams[0].getSelectValue(context));
        } else {
            Sequence[] oldVars = cm.getAllVariableValues();
            Sequence[] newVars = Arrays.copyOf(oldVars, oldVars.length);
            for (WithParam wp : this.actualParams) {
                newVars[wp.getSlotNumber()] = wp.getSelectValue(context);
            }
            cm.resetAllVariableValues(newVars);
        }
        cm.requestTailCall(this, null);
        return null;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("nextIteration", this);
        if (this.actualParams != null && this.actualParams.length > 0) {
            WithParam.exportParameters(this.actualParams, out, false);
        }
        out.endElement();
    }
}


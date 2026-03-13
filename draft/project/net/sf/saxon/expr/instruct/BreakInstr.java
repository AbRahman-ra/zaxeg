/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.Collections;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.TailCallLoop;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;

public class BreakInstr
extends Instruction
implements TailCallLoop.TailCallInfo {
    @Override
    public Iterable<Operand> operands() {
        return Collections.emptyList();
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        BreakInstr b2 = new BreakInstr();
        ExpressionTool.copyLocationInfo(this, b2);
        return b2;
    }

    @Override
    public boolean mayCreateNewNodes() {
        return true;
    }

    @Override
    public boolean isLiftable(boolean forStreaming) {
        return false;
    }

    @Override
    public int getInstructionNameCode() {
        return 137;
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        this.markContext(context);
        return null;
    }

    public void markContext(XPathContext context) {
        XPathContext c = context;
        while (!(c instanceof XPathContextMajor)) {
            c = c.getCaller();
        }
        ((XPathContextMajor)c).requestTailCall(this, null);
    }

    @Override
    public String getExpressionName() {
        return "xsl:break";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("break", this);
        out.endElement();
    }
}


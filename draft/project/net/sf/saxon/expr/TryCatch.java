/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.event.EventMonitor;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.OutputterEventBuffer;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.TailCallLoop;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.XPathContextMinor;
import net.sf.saxon.expr.instruct.BreakInstr;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.QNameTest;
import net.sf.saxon.s9api.XmlProcessingError;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.Cardinality;

public class TryCatch
extends Expression {
    private Operand tryOp;
    private List<CatchClause> catchClauses = new ArrayList<CatchClause>();
    private boolean rollbackOutput;

    public TryCatch(Expression tryExpr) {
        this.tryOp = new Operand(this, tryExpr, OperandRole.SAME_FOCUS_ACTION);
    }

    public void addCatchExpression(QNameTest test, Expression catchExpr) {
        CatchClause clause = new CatchClause();
        clause.catchOp = new Operand(this, catchExpr, OperandRole.SAME_FOCUS_ACTION);
        clause.nameTest = test;
        this.catchClauses.add(clause);
    }

    public void setRollbackOutput(boolean rollback) {
        this.rollbackOutput = rollback;
    }

    public boolean isRollbackOutput() {
        return this.rollbackOutput;
    }

    public Operand getTryOperand() {
        return this.tryOp;
    }

    public Expression getTryExpr() {
        return this.tryOp.getChildExpression();
    }

    public List<CatchClause> getCatchClauses() {
        return this.catchClauses;
    }

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    public boolean allowExtractingCommonSubexpressions() {
        return false;
    }

    @Override
    public int computeCardinality() {
        int card = this.getTryExpr().getCardinality();
        for (CatchClause catchClause : this.catchClauses) {
            card = Cardinality.union(card, catchClause.catchOp.getChildExpression().getCardinality());
        }
        return card;
    }

    @Override
    public ItemType getItemType() {
        ItemType type = this.getTryExpr().getItemType();
        for (CatchClause catchClause : this.catchClauses) {
            type = Type.getCommonSuperType(type, catchClause.catchOp.getChildExpression().getItemType());
        }
        return type;
    }

    @Override
    public Iterable<Operand> operands() {
        ArrayList<Operand> list = new ArrayList<Operand>();
        list.add(this.tryOp);
        for (CatchClause cc : this.catchClauses) {
            list.add(cc.catchOp);
        }
        return list;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.optimizeChildren(visitor, contextInfo);
        for (Expression e = this.getParentExpression(); e != null; e = e.getParentExpression()) {
            if (!(e instanceof LetExpression) || !ExpressionTool.dependsOnVariable(this.getTryExpr(), new Binding[]{(LetExpression)e})) continue;
            ((LetExpression)e).setNeedsEagerEvaluation(true);
        }
        return this;
    }

    @Override
    public int getImplementationMethod() {
        return 2;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TryCatch && ((TryCatch)other).tryOp.getChildExpression().isEqual(this.tryOp.getChildExpression()) && ((TryCatch)other).catchClauses.equals(this.catchClauses);
    }

    @Override
    public int computeHashCode() {
        int h = -2090134880;
        for (int i = 0; i < this.catchClauses.size(); ++i) {
            h ^= this.catchClauses.get(i).hashCode() << i;
        }
        return h + this.tryOp.getChildExpression().hashCode();
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        TryCatch t2 = new TryCatch(this.tryOp.getChildExpression().copy(rebindings));
        for (CatchClause clause : this.catchClauses) {
            t2.addCatchExpression(clause.nameTest, clause.catchOp.getChildExpression().copy(rebindings));
        }
        t2.setRollbackOutput(this.rollbackOutput);
        ExpressionTool.copyLocationInfo(this, t2);
        return t2;
    }

    @Override
    public Item evaluateItem(XPathContext c) throws XPathException {
        XPathContextMinor c1 = c.newMinorContext();
        try {
            return ExpressionTool.eagerEvaluate(this.tryOp.getChildExpression(), c1).head();
        } catch (XPathException err) {
            if (err.isGlobalError()) {
                err.setIsGlobalError(false);
            } else {
                StructuredQName code = err.getErrorCodeQName();
                if (code == null) {
                    code = new StructuredQName("saxon", "http://saxon.sf.net/", "XXXX9999");
                }
                for (CatchClause clause : this.catchClauses) {
                    if (!clause.nameTest.matches(code)) continue;
                    Expression caught = clause.catchOp.getChildExpression();
                    XPathContextMajor c2 = c.newContext();
                    c2.setCurrentException(err);
                    return caught.evaluateItem(c2);
                }
            }
            err.setHasBeenReported(false);
            throw err;
        }
    }

    @Override
    public SequenceIterator iterate(XPathContext c) throws XPathException {
        XPathContextMajor c1 = c.newContext();
        c1.createThreadManager();
        c1.setErrorReporter(new FilteringErrorReporter(c.getErrorReporter()));
        try {
            GroundedValue v = ExpressionTool.eagerEvaluate(this.tryOp.getChildExpression(), c1);
            c1.waitForChildThreads();
            TailCallLoop.TailCallInfo tci = c1.getTailCallInfo();
            if (tci instanceof BreakInstr) {
                ((BreakInstr)tci).markContext(c);
            }
            return v.iterate();
        } catch (XPathException err) {
            if (err.isGlobalError()) {
                err.setIsGlobalError(false);
            } else {
                StructuredQName code = err.getErrorCodeQName();
                if (code == null) {
                    code = new StructuredQName("saxon", "http://saxon.sf.net/", "XXXX9999");
                }
                for (CatchClause clause : this.catchClauses) {
                    if (!clause.nameTest.matches(code)) continue;
                    Expression caught = clause.catchOp.getChildExpression();
                    XPathContextMajor c2 = c.newContext();
                    c2.setCurrentException(err);
                    GroundedValue v = ExpressionTool.eagerEvaluate(caught, c2);
                    TailCallLoop.TailCallInfo tci = c2.getTailCallInfo();
                    if (tci instanceof BreakInstr) {
                        ((BreakInstr)tci).markContext(c);
                    }
                    return v.iterate();
                }
            }
            err.setHasBeenReported(false);
            throw err;
        }
    }

    @Override
    public void process(Outputter output, XPathContext context) throws XPathException {
        Outputter o2;
        PipelineConfiguration pipe = output.getPipelineConfiguration();
        XPathContextMajor c1 = context.newContext();
        c1.createThreadManager();
        c1.setErrorReporter(new FilteringErrorReporter(context.getErrorReporter()));
        if (this.rollbackOutput) {
            o2 = new OutputterEventBuffer();
            o2.setPipelineConfiguration(pipe);
        } else {
            o2 = new EventMonitor(output);
            o2.setPipelineConfiguration(pipe);
        }
        try {
            this.tryOp.getChildExpression().process(o2, c1);
            c1.waitForChildThreads();
            TailCallLoop.TailCallInfo tci = c1.getTailCallInfo();
            if (tci instanceof BreakInstr) {
                ((BreakInstr)tci).markContext(context);
            }
            if (this.rollbackOutput) {
                ((OutputterEventBuffer)o2).replay(output);
            }
        } catch (XPathException err) {
            if (err.isGlobalError()) {
                err.setIsGlobalError(false);
            } else {
                StructuredQName code = err.getErrorCodeQName();
                if (code == null) {
                    code = new StructuredQName("saxon", "http://saxon.sf.net/", "XXXX9999");
                }
                for (CatchClause clause : this.catchClauses) {
                    if (!clause.nameTest.matches(code)) continue;
                    if (o2 instanceof EventMonitor && ((EventMonitor)o2).hasBeenWrittenTo()) {
                        String message = err.getMessage() + ". The error could not be caught, because rollback-output=no was specified, and output was already written to the result tree";
                        XPathException xe = new XPathException(message, "XTDE3530");
                        xe.setLocation(err.getLocator());
                        xe.setXPathContext(context);
                        throw xe;
                    }
                    Expression caught = clause.catchOp.getChildExpression();
                    XPathContextMajor c2 = context.newContext();
                    c2.setCurrentException(err);
                    GroundedValue v = ExpressionTool.eagerEvaluate(caught, c2);
                    TailCallLoop.TailCallInfo tci = c2.getTailCallInfo();
                    if (tci instanceof BreakInstr) {
                        ((BreakInstr)tci).markContext(context);
                    }
                    v.iterate().forEachOrFail(item -> output.append(item));
                    return;
                }
            }
            err.setHasBeenReported(false);
            throw err;
        }
    }

    @Override
    public String getExpressionName() {
        return "tryCatch";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("try", this);
        if (this.rollbackOutput) {
            out.emitAttribute("flags", "r");
        }
        this.tryOp.getChildExpression().export(out);
        for (CatchClause clause : this.catchClauses) {
            out.startElement("catch");
            out.emitAttribute("errors", clause.nameTest.exportQNameTest());
            clause.catchOp.getChildExpression().export(out);
            out.endElement();
        }
        out.endElement();
    }

    @Override
    public String getStreamerName() {
        return "TryCatch";
    }

    private class FilteringErrorReporter
    implements ErrorReporter {
        private ErrorReporter base;

        FilteringErrorReporter(ErrorReporter base) {
            this.base = base;
        }

        private boolean isCaught(XmlProcessingError err) {
            StructuredQName code = err.getErrorCode().getStructuredQName();
            for (CatchClause clause : TryCatch.this.catchClauses) {
                if (!clause.nameTest.matches(code)) continue;
                return true;
            }
            return false;
        }

        @Override
        public void report(XmlProcessingError error) {
            if (error.isWarning() || !this.isCaught(error)) {
                this.base.report(error);
            }
        }
    }

    public static class CatchClause {
        public int slotNumber = -1;
        public Operand catchOp;
        public QNameTest nameTest;
    }
}


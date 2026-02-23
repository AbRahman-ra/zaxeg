/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.List;
import net.sf.saxon.expr.EvaluationMode;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.Evaluator;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;

public class WithParam {
    public static WithParam[] EMPTY_ARRAY = new WithParam[0];
    private Operand selectOp;
    private boolean typeChecked = false;
    private int slotNumber = -1;
    private SequenceType requiredType;
    private StructuredQName variableQName;
    private Evaluator evaluator = null;

    public void setSelectExpression(Expression parent, Expression select) {
        this.selectOp = new Operand(parent, select, OperandRole.NAVIGATE);
    }

    public Operand getSelectOperand() {
        return this.selectOp;
    }

    public Expression getSelectExpression() {
        return this.selectOp.getChildExpression();
    }

    public void setRequiredType(SequenceType required) {
        this.requiredType = required;
    }

    public SequenceType getRequiredType() {
        return this.requiredType;
    }

    public int getSlotNumber() {
        return this.slotNumber;
    }

    public void setSlotNumber(int s) {
        this.slotNumber = s;
    }

    public void setVariableQName(StructuredQName s) {
        this.variableQName = s;
    }

    public StructuredQName getVariableQName() {
        return this.variableQName;
    }

    public void setTypeChecked(boolean checked) {
        this.typeChecked = checked;
    }

    public int getInstructionNameCode() {
        return 209;
    }

    public static void simplify(WithParam[] params) throws XPathException {
        if (params != null) {
            for (WithParam param : params) {
                param.selectOp.setChildExpression(param.selectOp.getChildExpression().simplify());
            }
        }
    }

    public static void typeCheck(WithParam[] params, ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        if (params != null) {
            for (WithParam param : params) {
                param.selectOp.typeCheck(visitor, contextItemType);
            }
        }
    }

    public static void optimize(ExpressionVisitor visitor, WithParam[] params, ContextItemStaticInfo contextItemType) throws XPathException {
        if (params != null) {
            for (WithParam param : params) {
                param.selectOp.optimize(visitor, contextItemType);
                param.computeEvaluator();
            }
        }
    }

    public EvaluationMode getEvaluationMode() {
        if (this.evaluator == null) {
            this.computeEvaluator();
        }
        return this.evaluator.getEvaluationMode();
    }

    private void computeEvaluator() {
        this.evaluator = ExpressionTool.lazyEvaluator(this.selectOp.getChildExpression(), true);
    }

    public static WithParam[] copy(Expression parent, WithParam[] params, RebindingMap rebindings) {
        if (params == null) {
            return null;
        }
        WithParam[] result = new WithParam[params.length];
        for (int i = 0; i < params.length; ++i) {
            result[i] = new WithParam();
            result[i].slotNumber = params[i].slotNumber;
            result[i].typeChecked = params[i].typeChecked;
            result[i].selectOp = new Operand(parent, params[i].selectOp.getChildExpression().copy(rebindings), OperandRole.NAVIGATE);
            result[i].requiredType = params[i].requiredType;
            result[i].variableQName = params[i].variableQName;
        }
        return result;
    }

    public static void gatherOperands(Expression parent, WithParam[] params, List<Operand> list) {
        if (params != null) {
            for (WithParam param : params) {
                list.add(param.selectOp);
            }
        }
    }

    public static void exportParameters(WithParam[] params, ExpressionPresenter out, boolean tunnel) throws XPathException {
        if (params != null) {
            for (WithParam param : params) {
                out.startElement("withParam");
                out.emitAttribute("name", param.variableQName);
                String flags = "";
                if (tunnel) {
                    flags = flags + "t";
                }
                if (param.isTypeChecked()) {
                    flags = flags + "c";
                }
                if (!flags.isEmpty()) {
                    out.emitAttribute("flags", flags);
                }
                ExpressionPresenter.ExportOptions options = (ExpressionPresenter.ExportOptions)out.getOptions();
                if (param.getRequiredType() != SequenceType.ANY_SEQUENCE) {
                    out.emitAttribute("as", param.getRequiredType().toAlphaCode());
                }
                if (param.getSlotNumber() != -1) {
                    out.emitAttribute("slot", param.getSlotNumber() + "");
                }
                param.selectOp.getChildExpression().export(out);
                out.endElement();
            }
        }
    }

    public Sequence getSelectValue(XPathContext context) throws XPathException {
        if (this.evaluator == null) {
            this.computeEvaluator();
        }
        int savedOutputState = context.getTemporaryOutputState();
        context.setTemporaryOutputState(209);
        Sequence result = this.evaluator.evaluate(this.selectOp.getChildExpression(), context);
        context.setTemporaryOutputState(savedOutputState);
        return result;
    }

    public boolean isTypeChecked() {
        return this.typeChecked;
    }
}


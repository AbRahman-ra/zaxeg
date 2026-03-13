/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.ContextSwitchingExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.TailCallLoop;
import net.sf.saxon.expr.TryCatch;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.XPathContextMinor;
import net.sf.saxon.expr.instruct.BreakInstr;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.LocalParam;
import net.sf.saxon.expr.instruct.LocalParamBlock;
import net.sf.saxon.expr.instruct.NextIteration;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.om.FocusIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.SequenceType;

public final class IterateInstr
extends Instruction
implements ContextSwitchingExpression {
    private Operand selectOp;
    private Operand actionOp;
    private Operand initiallyOp;
    private Operand onCompletionOp;

    public IterateInstr(Expression select, LocalParamBlock initiallyExp, Expression action, Expression onCompletion) {
        if (onCompletion == null) {
            onCompletion = Literal.makeEmptySequence();
        }
        this.selectOp = new Operand(this, select, OperandRole.FOCUS_CONTROLLING_SELECT);
        this.actionOp = new Operand(this, action, OperandRole.FOCUS_CONTROLLED_ACTION);
        this.initiallyOp = new Operand(this, initiallyExp, new OperandRole(16, OperandUsage.NAVIGATION, SequenceType.ANY_SEQUENCE, expr -> expr instanceof LocalParamBlock));
        this.onCompletionOp = new Operand(this, onCompletion, new OperandRole(2, OperandUsage.TRANSMISSION));
    }

    public void setSelect(Expression select) {
        this.selectOp.setChildExpression(select);
    }

    public LocalParamBlock getInitiallyExp() {
        return (LocalParamBlock)this.initiallyOp.getChildExpression();
    }

    public void setInitiallyExp(LocalParamBlock initiallyExp) {
        this.initiallyOp.setChildExpression(initiallyExp);
    }

    public void setAction(Expression action) {
        this.actionOp.setChildExpression(action);
    }

    public Expression getOnCompletion() {
        return this.onCompletionOp.getChildExpression();
    }

    public void setOnCompletion(Expression onCompletion) {
        this.onCompletionOp.setChildExpression(onCompletion);
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operandList(this.selectOp, this.actionOp, this.initiallyOp, this.onCompletionOp);
    }

    @Override
    public int getInstructionNameCode() {
        return 164;
    }

    @Override
    public Expression getSelectExpression() {
        return this.selectOp.getChildExpression();
    }

    @Override
    public Expression getActionExpression() {
        return this.actionOp.getChildExpression();
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.selectOp.typeCheck(visitor, contextInfo);
        this.initiallyOp.typeCheck(visitor, contextInfo);
        ItemType selectType = this.getSelectExpression().getItemType();
        if (selectType == ErrorType.getInstance()) {
            selectType = AnyItemType.getInstance();
        }
        ContextItemStaticInfo cit = visitor.getConfiguration().makeContextItemStaticInfo(selectType, false);
        cit.setContextSettingExpression(this.getSelectExpression());
        this.actionOp.typeCheck(visitor, cit);
        this.onCompletionOp.typeCheck(visitor, ContextItemStaticInfo.ABSENT);
        if (Literal.isEmptySequence(this.getOnCompletion()) && (Literal.isEmptySequence(this.getSelectExpression()) || Literal.isEmptySequence(this.getActionExpression()))) {
            return this.getOnCompletion();
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.selectOp.optimize(visitor, contextInfo);
        this.initiallyOp.optimize(visitor, contextInfo);
        ContextItemStaticInfo cit2 = visitor.getConfiguration().makeContextItemStaticInfo(this.getSelectExpression().getItemType(), false);
        cit2.setContextSettingExpression(this.getSelectExpression());
        this.actionOp.optimize(visitor, cit2);
        this.onCompletionOp.optimize(visitor, ContextItemStaticInfo.ABSENT);
        if (Literal.isEmptySequence(this.getOnCompletion()) && (Literal.isEmptySequence(this.getSelectExpression()) || Literal.isEmptySequence(this.getActionExpression()))) {
            return this.getOnCompletion();
        }
        return this;
    }

    public boolean isCompilable() {
        return !IterateInstr.containsBreakOrNextIterationWithinTryCatch(this, false);
    }

    private static boolean containsBreakOrNextIterationWithinTryCatch(Expression exp, boolean withinTryCatch) {
        if (exp instanceof BreakInstr || exp instanceof NextIteration) {
            return withinTryCatch;
        }
        boolean found = false;
        boolean inTryCatch = withinTryCatch || exp instanceof TryCatch;
        for (Operand o : exp.operands()) {
            if (!IterateInstr.containsBreakOrNextIterationWithinTryCatch(o.getChildExpression(), inTryCatch)) continue;
            found = true;
            break;
        }
        return found;
    }

    @Override
    public final ItemType getItemType() {
        if (Literal.isEmptySequence(this.getOnCompletion())) {
            return this.getActionExpression().getItemType();
        }
        TypeHierarchy th = this.getConfiguration().getTypeHierarchy();
        return Type.getCommonSuperType(this.getActionExpression().getItemType(), this.getOnCompletion().getItemType(), th);
    }

    @Override
    public final boolean mayCreateNewNodes() {
        return (this.getActionExpression().getSpecialProperties() & this.getOnCompletion().getSpecialProperties() & 0x800000) == 0;
    }

    @Override
    public boolean hasVariableBinding(Binding binding) {
        LocalParamBlock paramBlock = this.getInitiallyExp();
        for (Operand o : paramBlock.operands()) {
            LocalParam setter = (LocalParam)o.getChildExpression();
            if (setter != binding) continue;
            return true;
        }
        return false;
    }

    @Override
    public String getStreamerName() {
        return "Iterate";
    }

    @Override
    public int getImplementationMethod() {
        return 4;
    }

    @Override
    public void checkPermittedContents(SchemaType parentType, boolean whole) throws XPathException {
        this.getActionExpression().checkPermittedContents(parentType, false);
        this.getOnCompletion().checkPermittedContents(parentType, false);
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        IterateInstr exp = new IterateInstr(this.getSelectExpression().copy(rebindings), (LocalParamBlock)this.getInitiallyExp().copy(rebindings), this.getActionExpression().copy(rebindings), this.getOnCompletion().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        Item item;
        XPathContextMajor c2 = context.newContext();
        c2.setOrigin(this);
        FocusIterator iter = c2.trackFocus(this.getSelectExpression().iterate(context));
        c2.setCurrentTemplateRule(null);
        PipelineConfiguration pipe = output.getPipelineConfiguration();
        pipe.setXPathContext(c2);
        boolean tracing = context.getController().isTracing();
        TraceListener listener = tracing ? context.getController().getTraceListener() : null;
        this.getInitiallyExp().process(output, context);
        while ((item = iter.next()) != null) {
            TailCallLoop.TailCallInfo comp;
            if (tracing) {
                listener.startCurrentItem(item);
            }
            this.getActionExpression().process(output, c2);
            if (tracing) {
                listener.endCurrentItem(item);
            }
            if ((comp = c2.getTailCallInfo()) == null || !(comp instanceof BreakInstr)) continue;
            iter.close();
            return null;
        }
        XPathContextMinor c3 = context.newMinorContext();
        c3.setCurrentIterator(null);
        this.getOnCompletion().process(output, c3);
        pipe.setXPathContext(context);
        return null;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("iterate", this);
        out.setChildRole("select");
        this.getSelectExpression().export(out);
        out.setChildRole("params");
        this.getInitiallyExp().export(out);
        if (!Literal.isEmptySequence(this.getOnCompletion())) {
            out.setChildRole("on-completion");
            this.getOnCompletion().export(out);
        }
        out.setChildRole("action");
        this.getActionExpression().export(out);
        out.endElement();
    }
}


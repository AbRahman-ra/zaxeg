/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.Controller;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.ContextMappingFunction;
import net.sf.saxon.expr.ContextMappingIterator;
import net.sf.saxon.expr.ContextSwitchingExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.PendingUpdateList;
import net.sf.saxon.expr.SimpleStepExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.XPathContextMinor;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.instruct.TailCallReturner;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.om.FocusIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.PrependSequenceIterator;
import net.sf.saxon.tree.util.Orphan;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.Cardinality;

public class ForEach
extends Instruction
implements ContextMappingFunction,
ContextSwitchingExpression {
    protected boolean containsTailCall;
    protected Operand selectOp;
    protected Operand actionOp;
    protected Operand separatorOp;
    protected Operand threadsOp;
    protected boolean isInstruction;

    public ForEach(Expression select, Expression action) {
        this(select, action, false, null);
    }

    public ForEach(Expression select, Expression action, boolean containsTailCall, Expression threads) {
        this.selectOp = new Operand(this, select, OperandRole.FOCUS_CONTROLLING_SELECT);
        this.actionOp = new Operand(this, action, OperandRole.FOCUS_CONTROLLED_ACTION);
        if (threads != null) {
            this.threadsOp = new Operand(this, threads, OperandRole.SINGLE_ATOMIC);
        }
        this.containsTailCall = containsTailCall && action instanceof TailCallReturner;
    }

    public void setSeparatorExpression(Expression separator) {
        this.separatorOp = new Operand(this, separator, OperandRole.SINGLE_ATOMIC);
    }

    public Expression getSeparatorExpression() {
        return this.separatorOp == null ? null : this.separatorOp.getChildExpression();
    }

    public void setInstruction(boolean inst) {
        this.isInstruction = inst;
    }

    @Override
    public boolean isInstruction() {
        return this.isInstruction;
    }

    public Expression getSelect() {
        return this.selectOp.getChildExpression();
    }

    public void setSelect(Expression select) {
        this.selectOp.setChildExpression(select);
    }

    public Expression getAction() {
        return this.actionOp.getChildExpression();
    }

    public void setAction(Expression action) {
        this.actionOp.setChildExpression(action);
    }

    public Expression getThreads() {
        return this.threadsOp == null ? null : this.threadsOp.getChildExpression();
    }

    public void setThreads(Expression threads) {
        if (threads != null) {
            if (this.threadsOp == null) {
                this.threadsOp = new Operand(this, threads, OperandRole.SINGLE_ATOMIC);
            } else {
                this.threadsOp.setChildExpression(threads);
            }
        }
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operandSparseList(this.selectOp, this.actionOp, this.separatorOp, this.threadsOp);
    }

    @Override
    public int getInstructionNameCode() {
        return 155;
    }

    @Override
    public Expression getSelectExpression() {
        return this.getSelect();
    }

    public void setSelectExpression(Expression select) {
        this.setSelect(select);
    }

    public void setActionExpression(Expression action) {
        this.setAction(action);
    }

    @Override
    public Expression getActionExpression() {
        return this.getAction();
    }

    public Expression getNumberOfThreadsExpression() {
        return this.getThreads();
    }

    @Override
    public final ItemType getItemType() {
        return this.getAction().getItemType();
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        if (this.isInstruction()) {
            return super.getStaticUType(contextItemType);
        }
        return this.getAction().getStaticUType(this.getSelect().getStaticUType(contextItemType));
    }

    @Override
    public final boolean mayCreateNewNodes() {
        int props = this.getAction().getSpecialProperties();
        return (props & 0x800000) == 0;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.selectOp.typeCheck(visitor, contextInfo);
        ItemType selectType = this.getSelect().getItemType();
        if (selectType == ErrorType.getInstance()) {
            return Literal.makeEmptySequence();
        }
        ContextItemStaticInfo cit = visitor.getConfiguration().makeContextItemStaticInfo(this.getSelect().getItemType(), false);
        cit.setContextSettingExpression(this.getSelect());
        this.actionOp.typeCheck(visitor, cit);
        if (!Cardinality.allowsMany(this.getSelect().getCardinality())) {
            this.actionOp.setOperandRole(this.actionOp.getOperandRole().modifyProperty(32, true));
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.selectOp.optimize(visitor, contextInfo);
        ContextItemStaticInfo cit = visitor.getConfiguration().makeContextItemStaticInfo(this.getSelect().getItemType(), false);
        cit.setContextSettingExpression(this.getSelect());
        this.actionOp.optimize(visitor, cit);
        if (!visitor.isOptimizeForStreaming()) {
            if (Literal.isEmptySequence(this.getSelect())) {
                return this.getSelect();
            }
            if (Literal.isEmptySequence(this.getAction())) {
                return this.getAction();
            }
        }
        if (this.getSelect().getCardinality() == 16384 && this.getAction() instanceof AxisExpression) {
            return new SimpleStepExpression(this.getSelect(), this.getAction());
        }
        if (this.threadsOp != null && !Literal.isEmptySequence(this.getThreads())) {
            return visitor.obtainOptimizer().generateMultithreadedInstruction(this);
        }
        return this;
    }

    @Override
    public Expression unordered(boolean retainAllNodes, boolean forStreaming) throws XPathException {
        this.setSelect(this.getSelect().unordered(retainAllNodes, forStreaming));
        this.setAction(this.getAction().unordered(retainAllNodes, forStreaming));
        return this;
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        PathMap.PathMapNodeSet target = this.getSelect().addToPathMap(pathMap, pathMapNodeSet);
        return this.getAction().addToPathMap(pathMap, target);
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        ForEach f2 = new ForEach(this.getSelect().copy(rebindings), this.getAction().copy(rebindings), this.containsTailCall, this.getThreads());
        if (this.separatorOp != null) {
            f2.setSeparatorExpression(this.getSeparatorExpression().copy(rebindings));
        }
        ExpressionTool.copyLocationInfo(this, f2);
        f2.setInstruction(this.isInstruction());
        return f2;
    }

    @Override
    public int computeSpecialProperties() {
        int p = super.computeSpecialProperties();
        p = this.getSelect().getCardinality() == 16384 ? (p |= this.getAction().getSpecialProperties()) : (p |= this.getAction().getSpecialProperties() & 0x8000000);
        return p;
    }

    @Override
    public boolean alwaysCreatesNewNodes() {
        return this.getAction() instanceof Instruction && ((Instruction)this.getAction()).alwaysCreatesNewNodes();
    }

    @Override
    public boolean isUpdatingExpression() {
        return this.getAction().isUpdatingExpression();
    }

    @Override
    public void checkForUpdatingSubexpressions() throws XPathException {
        if (this.getSelect().isUpdatingExpression()) {
            XPathException err = new XPathException("Updating expression appears in a context where it is not permitted", "XUST0001");
            err.setLocation(this.getSelect().getLocation());
            throw err;
        }
    }

    @Override
    public int getImplementationMethod() {
        return 30;
    }

    @Override
    public void checkPermittedContents(SchemaType parentType, boolean whole) throws XPathException {
        this.getAction().checkPermittedContents(parentType, false);
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        Controller controller = context.getController();
        assert (controller != null);
        XPathContextMajor c2 = context.newContext();
        c2.setOrigin(this);
        FocusIterator iter = c2.trackFocus(this.getSelect().iterate(context));
        c2.setCurrentTemplateRule(null);
        Expression action = this.getAction();
        if (this.containsTailCall) {
            if (controller.isTracing()) {
                TraceListener listener = controller.getTraceListener();
                assert (listener != null);
                Item item2 = iter.next();
                if (item2 == null) {
                    return null;
                }
                listener.startCurrentItem(item2);
                TailCall tc = ((TailCallReturner)((Object)action)).processLeavingTail(output, c2);
                listener.endCurrentItem(item2);
                return tc;
            }
            Item item3 = iter.next();
            if (item3 == null) {
                return null;
            }
            return ((TailCallReturner)((Object)action)).processLeavingTail(output, c2);
        }
        PipelineConfiguration pipe = output.getPipelineConfiguration();
        pipe.setXPathContext(c2);
        NodeInfo separator = null;
        if (this.separatorOp != null) {
            separator = this.makeSeparator(context);
        }
        if (controller.isTracing() || separator != null) {
            Item item4;
            TraceListener listener = controller.getTraceListener();
            boolean first = true;
            while ((item4 = iter.next()) != null) {
                if (controller.isTracing()) {
                    assert (listener != null);
                    listener.startCurrentItem(item4);
                }
                if (separator != null) {
                    if (first) {
                        first = false;
                    } else {
                        output.append(separator);
                    }
                }
                action.process(output, c2);
                if (!controller.isTracing()) continue;
                listener.endCurrentItem(item4);
            }
        } else {
            iter.forEachOrFail(item -> action.process(output, c2));
        }
        pipe.setXPathContext(context);
        return null;
    }

    protected NodeInfo makeSeparator(XPathContext context) throws XPathException {
        CharSequence sepValue = this.separatorOp.getChildExpression().evaluateAsString(context);
        Orphan orphan = new Orphan(context.getConfiguration());
        orphan.setNodeKind((short)3);
        orphan.setStringValue(sepValue);
        Orphan separator = orphan;
        return separator;
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        XPathContextMinor c2 = context.newMinorContext();
        c2.trackFocus(this.getSelect().iterate(context));
        if (this.separatorOp == null) {
            return new ContextMappingIterator(this, c2);
        }
        NodeInfo separator = this.makeSeparator(context);
        ContextMappingFunction mapper = cxt -> {
            if (cxt.getCurrentIterator().position() == 1) {
                return this.map(cxt);
            }
            return new PrependSequenceIterator(separator, this.map(cxt));
        };
        return new ContextMappingIterator(mapper, c2);
    }

    @Override
    public SequenceIterator map(XPathContext context) throws XPathException {
        return this.getAction().iterate(context);
    }

    @Override
    public void evaluatePendingUpdates(XPathContext context, PendingUpdateList pul) throws XPathException {
        Item item;
        XPathContextMinor c2 = context.newMinorContext();
        c2.trackFocus(this.getSelect().iterate(context));
        FocusIterator iter = c2.getCurrentIterator();
        while ((item = iter.next()) != null) {
            this.getAction().evaluatePendingUpdates(c2, pul);
        }
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("forEach", this);
        this.getSelect().export(out);
        this.getAction().export(out);
        if (this.separatorOp != null) {
            out.setChildRole("separator");
            this.separatorOp.getChildExpression().export(out);
        }
        this.explainThreads(out);
        out.endElement();
    }

    protected void explainThreads(ExpressionPresenter out) throws XPathException {
    }

    @Override
    public String toString() {
        return ExpressionTool.parenthesize(this.getSelect()) + " ! " + ExpressionTool.parenthesize(this.getAction());
    }

    @Override
    public String toShortString() {
        return this.getSelect().toShortString() + "!" + this.getAction().toShortString();
    }

    @Override
    public String getExpressionName() {
        return "forEach";
    }

    @Override
    public String getStreamerName() {
        return "ForEach";
    }
}


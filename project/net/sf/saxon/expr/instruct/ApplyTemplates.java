/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.ArrayList;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.ComponentInvocation;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.ITemplateCall;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.ParameterSet;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.instruct.WithParam;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.trans.SimpleMode;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.RuleManager;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.util.Orphan;

public class ApplyTemplates
extends Instruction
implements ITemplateCall,
ComponentInvocation {
    private Operand selectOp;
    private Operand separatorOp;
    private WithParam[] actualParams;
    private WithParam[] tunnelParams;
    protected boolean useCurrentMode = false;
    protected boolean useTailRecursion = false;
    protected Mode mode;
    protected boolean implicitSelect;
    protected boolean inStreamableConstruct = false;
    protected RuleManager ruleManager;
    private int bindingSlot = -1;

    protected ApplyTemplates() {
    }

    public ApplyTemplates(Expression select, boolean useCurrentMode, boolean useTailRecursion, boolean implicitSelect, boolean inStreamableConstruct, Mode mode, RuleManager ruleManager) {
        this.selectOp = new Operand(this, select, OperandRole.SINGLE_ATOMIC);
        this.init(select, useCurrentMode, useTailRecursion, mode);
        this.implicitSelect = implicitSelect;
        this.inStreamableConstruct = inStreamableConstruct;
        this.ruleManager = ruleManager;
    }

    protected void init(Expression select, boolean useCurrentMode, boolean useTailRecursion, Mode mode) {
        this.setSelect(select);
        this.useCurrentMode = useCurrentMode;
        this.useTailRecursion = useTailRecursion;
        this.mode = mode;
        this.adoptChildExpression(select);
    }

    public void setMode(SimpleMode target) {
        this.mode = target;
    }

    public void setSeparatorExpression(Expression separator) {
        this.separatorOp = new Operand(this, separator, OperandRole.SINGLE_ATOMIC);
    }

    public Expression getSeparatorExpression() {
        return this.separatorOp == null ? null : this.separatorOp.getChildExpression();
    }

    @Override
    public WithParam[] getActualParams() {
        return this.actualParams;
    }

    @Override
    public WithParam[] getTunnelParams() {
        return this.tunnelParams;
    }

    public void setActualParams(WithParam[] params) {
        this.actualParams = params;
    }

    public void setTunnelParams(WithParam[] params) {
        this.tunnelParams = params;
    }

    @Override
    public Iterable<Operand> operands() {
        ArrayList<Operand> operanda = new ArrayList<Operand>();
        operanda.add(this.selectOp);
        if (this.separatorOp != null) {
            operanda.add(this.separatorOp);
        }
        WithParam.gatherOperands(this, this.getActualParams(), operanda);
        WithParam.gatherOperands(this, this.getTunnelParams(), operanda);
        return operanda;
    }

    @Override
    public int getInstructionNameCode() {
        return 133;
    }

    @Override
    public int getImplementationMethod() {
        return super.getImplementationMethod() | 8;
    }

    @Override
    public Expression simplify() throws XPathException {
        WithParam.simplify(this.getActualParams());
        WithParam.simplify(this.getTunnelParams());
        this.setSelect(this.getSelect().simplify());
        return this;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        WithParam.typeCheck(this.actualParams, visitor, contextInfo);
        WithParam.typeCheck(this.tunnelParams, visitor, contextInfo);
        try {
            this.selectOp.typeCheck(visitor, contextInfo);
        } catch (XPathException e) {
            if (this.implicitSelect) {
                String code = e.getErrorCodeLocalPart();
                if ("XPTY0020".equals(code) || "XPTY0019".equals(code)) {
                    XPathException err = new XPathException("Cannot apply-templates to child nodes when the context item is an atomic value");
                    err.setErrorCode("XTTE0510");
                    err.setIsTypeError(true);
                    throw err;
                }
                if ("XPDY0002".equals(code)) {
                    XPathException err = new XPathException("Cannot apply-templates to child nodes when the context item is absent");
                    err.setErrorCode("XTTE0510");
                    err.setIsTypeError(true);
                    throw err;
                }
            }
            throw e;
        }
        this.adoptChildExpression(this.getSelect());
        if (Literal.isEmptySequence(this.getSelect())) {
            return this.getSelect();
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        WithParam.optimize(visitor, this.actualParams, contextInfo);
        WithParam.optimize(visitor, this.tunnelParams, contextInfo);
        this.selectOp.typeCheck(visitor, contextInfo);
        this.selectOp.optimize(visitor, contextInfo);
        if (Literal.isEmptySequence(this.getSelect())) {
            return this.getSelect();
        }
        return this;
    }

    @Override
    public int getIntrinsicDependencies() {
        return super.getIntrinsicDependencies() | (this.useCurrentMode ? 1 : 0);
    }

    public RuleManager getRuleManager() {
        return this.ruleManager;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        ApplyTemplates a2 = new ApplyTemplates(this.getSelect().copy(rebindings), this.useCurrentMode, this.useTailRecursion, this.implicitSelect, this.inStreamableConstruct, this.mode, this.ruleManager);
        a2.setActualParams(WithParam.copy(a2, this.getActualParams(), rebindings));
        a2.setTunnelParams(WithParam.copy(a2, this.getTunnelParams(), rebindings));
        ExpressionTool.copyLocationInfo(this, a2);
        a2.ruleManager = this.ruleManager;
        if (this.separatorOp != null) {
            a2.setSeparatorExpression(this.getSeparatorExpression().copy(rebindings));
        }
        return a2;
    }

    @Override
    public final boolean mayCreateNewNodes() {
        return true;
    }

    @Override
    public void process(Outputter output, XPathContext context) throws XPathException {
        this.apply(output, context, false);
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        return this.apply(output, context, this.useTailRecursion);
    }

    protected NodeInfo makeSeparator(XPathContext context) throws XPathException {
        CharSequence sepValue = this.separatorOp.getChildExpression().evaluateAsString(context);
        Orphan orphan = new Orphan(context.getConfiguration());
        orphan.setNodeKind((short)3);
        orphan.setStringValue(sepValue);
        Orphan separator = orphan;
        return separator;
    }

    protected TailCall apply(Outputter output, XPathContext context, boolean returnTailCall) throws XPathException {
        Component.M targetMode = this.getTargetMode(context);
        Mode thisMode = targetMode.getActor();
        NodeInfo separator = null;
        if (this.separatorOp != null) {
            separator = this.makeSeparator(context);
        }
        ParameterSet params = ApplyTemplates.assembleParams(context, this.getActualParams());
        ParameterSet tunnels = ApplyTemplates.assembleTunnelParams(context, this.getTunnelParams());
        if (returnTailCall) {
            XPathContextMajor c2 = context.newContext();
            c2.setOrigin(this);
            return new ApplyTemplatesPackage(ExpressionTool.lazyEvaluate(this.getSelect(), context, false), targetMode, params, tunnels, separator, output, c2, this.getLocation());
        }
        SequenceIterator iter = this.getSelect().iterate(context);
        if (iter instanceof EmptyIterator) {
            return null;
        }
        XPathContextMajor c2 = context.newContext();
        c2.trackFocus(iter);
        c2.setCurrentMode(targetMode);
        c2.setOrigin(this);
        c2.setCurrentComponent(targetMode);
        if (this.inStreamableConstruct) {
            c2.setCurrentGroupIterator(null);
        }
        PipelineConfiguration pipe = output.getPipelineConfiguration();
        pipe.setXPathContext(c2);
        try {
            for (TailCall tc = thisMode.applyTemplates(params, tunnels, separator, output, c2, this.getLocation()); tc != null; tc = tc.processLeavingTail()) {
            }
        } catch (StackOverflowError e) {
            XPathException.StackOverflow err = new XPathException.StackOverflow("Too many nested apply-templates calls. The stylesheet may be looping.", "SXLM0001", this.getLocation());
            err.setXPathContext(context);
            throw err;
        }
        pipe.setXPathContext(context);
        return null;
    }

    public Component.M getTargetMode(XPathContext context) {
        Component.M targetMode;
        if (this.useCurrentMode) {
            targetMode = context.getCurrentMode();
        } else if (this.bindingSlot >= 0) {
            targetMode = (Component.M)context.getTargetComponent(this.bindingSlot);
            if (targetMode.getVisibility() == Visibility.ABSTRACT) {
                throw new AssertionError((Object)"Modes cannot be abstract");
            }
        } else {
            targetMode = this.mode.getDeclaringComponent();
        }
        return targetMode;
    }

    public Expression getSelectExpression() {
        return this.getSelect();
    }

    public boolean isImplicitSelect() {
        return this.implicitSelect;
    }

    public boolean useTailRecursion() {
        return this.useTailRecursion;
    }

    public boolean usesCurrentMode() {
        return this.useCurrentMode;
    }

    public Mode getMode() {
        return this.mode;
    }

    @Override
    public Component getFixedTarget() {
        return this.mode.getDeclaringComponent();
    }

    @Override
    public SymbolicName getSymbolicName() {
        return this.mode == null ? null : this.mode.getSymbolicName();
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        PathMap.PathMapNodeSet result = super.addToPathMap(pathMap, pathMapNodeSet);
        result.setReturnable(false);
        return new PathMap.PathMapNodeSet(pathMap.makeNewRoot(this));
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("applyT", this);
        if (this.mode != null && !this.mode.isUnnamedMode()) {
            out.emitAttribute("mode", this.mode.getModeName());
        }
        String flags = "";
        if (this.useCurrentMode) {
            flags = "c";
        }
        if (this.useTailRecursion) {
            flags = flags + "t";
        }
        if (this.implicitSelect) {
            flags = flags + "i";
        }
        if (!flags.isEmpty()) {
            out.emitAttribute("flags", flags);
        }
        out.emitAttribute("bSlot", "" + this.getBindingSlot());
        out.setChildRole("select");
        this.getSelect().export(out);
        if (this.separatorOp != null) {
            out.setChildRole("separator");
            this.getSeparatorExpression().export(out);
        }
        if (this.getActualParams().length != 0) {
            WithParam.exportParameters(this.getActualParams(), out, false);
        }
        if (this.getTunnelParams().length != 0) {
            WithParam.exportParameters(this.getTunnelParams(), out, true);
        }
        out.endElement();
    }

    public Expression getSelect() {
        return this.selectOp.getChildExpression();
    }

    public void setSelect(Expression select) {
        this.selectOp.setChildExpression(select);
    }

    @Override
    public void setBindingSlot(int slot) {
        this.bindingSlot = slot;
    }

    @Override
    public int getBindingSlot() {
        return this.bindingSlot;
    }

    @Override
    public String getStreamerName() {
        return "ApplyTemplates";
    }

    protected static class ApplyTemplatesPackage
    implements TailCall {
        private Sequence selectedItems;
        private Component.M targetMode;
        private ParameterSet params;
        private ParameterSet tunnelParams;
        private NodeInfo separator;
        private XPathContextMajor evaluationContext;
        private Outputter output;
        private Location locationId;

        ApplyTemplatesPackage(Sequence selectedItems, Component.M targetMode, ParameterSet params, ParameterSet tunnelParams, NodeInfo separator, Outputter output, XPathContextMajor context, Location locationId) {
            this.selectedItems = selectedItems;
            this.targetMode = targetMode;
            this.params = params;
            this.tunnelParams = tunnelParams;
            this.separator = separator;
            this.output = output;
            this.evaluationContext = context;
            this.locationId = locationId;
        }

        @Override
        public TailCall processLeavingTail() throws XPathException {
            this.evaluationContext.trackFocus(this.selectedItems.iterate());
            this.evaluationContext.setCurrentMode(this.targetMode);
            this.evaluationContext.setCurrentComponent(this.targetMode);
            return this.targetMode.getActor().applyTemplates(this.params, this.tunnelParams, this.separator, this.output, this.evaluationContext, this.locationId);
        }
    }
}


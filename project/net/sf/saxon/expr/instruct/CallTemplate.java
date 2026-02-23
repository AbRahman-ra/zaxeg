/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.ArrayList;
import java.util.Arrays;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.ComponentInvocation;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.ITemplateCall;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.NamedTemplate;
import net.sf.saxon.expr.instruct.ParameterSet;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.instruct.WithParam;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.SequenceType;

public class CallTemplate
extends Instruction
implements ITemplateCall,
ComponentInvocation {
    private NamedTemplate template;
    private StructuredQName calledTemplateName;
    private WithParam[] actualParams = WithParam.EMPTY_ARRAY;
    private WithParam[] tunnelParams = WithParam.EMPTY_ARRAY;
    private boolean useTailRecursion;
    private int bindingSlot = -1;
    private boolean isWithinDeclaredStreamableConstruct;

    public CallTemplate(NamedTemplate template, StructuredQName calledTemplateName, boolean useTailRecursion, boolean inStreamable) {
        this.template = template;
        this.calledTemplateName = calledTemplateName;
        this.useTailRecursion = useTailRecursion;
        this.isWithinDeclaredStreamableConstruct = inStreamable;
    }

    public void setActualParameters(WithParam[] actualParams, WithParam[] tunnelParams) {
        this.actualParams = actualParams;
        this.tunnelParams = tunnelParams;
        for (WithParam actualParam : actualParams) {
            this.adoptChildExpression(actualParam.getSelectExpression());
        }
        for (WithParam tunnelParam : tunnelParams) {
            this.adoptChildExpression(tunnelParam.getSelectExpression());
        }
    }

    public StructuredQName getCalledTemplateName() {
        return this.calledTemplateName;
    }

    @Override
    public SymbolicName getSymbolicName() {
        return this.calledTemplateName == null ? null : new SymbolicName(200, this.calledTemplateName);
    }

    public Component getTarget() {
        return this.template.getDeclaringComponent();
    }

    @Override
    public Component getFixedTarget() {
        Component c = this.getTarget();
        Visibility v = c.getVisibility();
        if (v == Visibility.PRIVATE || v == Visibility.FINAL) {
            return c;
        }
        return null;
    }

    @Override
    public WithParam[] getActualParams() {
        return this.actualParams;
    }

    @Override
    public WithParam[] getTunnelParams() {
        return this.tunnelParams;
    }

    public void setTargetTemplate(NamedTemplate target) {
        this.template = target;
    }

    public NamedTemplate getTargetTemplate() {
        return this.template;
    }

    public boolean usesTailRecursion() {
        return this.useTailRecursion;
    }

    @Override
    public int getInstructionNameCode() {
        return 138;
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
    public Expression simplify() throws XPathException {
        WithParam.simplify(this.actualParams);
        WithParam.simplify(this.tunnelParams);
        return this;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        WithParam.typeCheck(this.actualParams, visitor, contextInfo);
        WithParam.typeCheck(this.tunnelParams, visitor, contextInfo);
        boolean backwards = visitor.getStaticContext().isInBackwardsCompatibleMode();
        TypeChecker tc = visitor.getConfiguration().getTypeChecker(backwards);
        for (int p = 0; p < this.actualParams.length; ++p) {
            WithParam wp = this.actualParams[p];
            NamedTemplate.LocalParamInfo lp = this.template.getLocalParamInfo(wp.getVariableQName());
            if (lp == null) continue;
            SequenceType req = lp.requiredType;
            RoleDiagnostic role = new RoleDiagnostic(8, wp.getVariableQName().getDisplayName(), p);
            role.setErrorCode("XTTE0590");
            Expression select = tc.staticTypeCheck(wp.getSelectExpression(), req, role, visitor);
            wp.setSelectExpression(this, select);
            wp.setTypeChecked(true);
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        WithParam.optimize(visitor, this.actualParams, contextItemType);
        WithParam.optimize(visitor, this.tunnelParams, contextItemType);
        return this;
    }

    @Override
    public int computeCardinality() {
        if (this.template == null) {
            return 57344;
        }
        return this.template.getRequiredType().getCardinality();
    }

    @Override
    public ItemType getItemType() {
        if (this.template == null) {
            return AnyItemType.getInstance();
        }
        return this.template.getRequiredType().getPrimaryType();
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        CallTemplate ct = new CallTemplate(this.template, this.calledTemplateName, this.useTailRecursion, this.isWithinDeclaredStreamableConstruct);
        ExpressionTool.copyLocationInfo(this, ct);
        ct.actualParams = WithParam.copy(ct, this.actualParams, rebindings);
        ct.tunnelParams = WithParam.copy(ct, this.tunnelParams, rebindings);
        return ct;
    }

    @Override
    public int getIntrinsicDependencies() {
        return 639;
    }

    @Override
    public final boolean mayCreateNewNodes() {
        return true;
    }

    @Override
    public Iterable<Operand> operands() {
        ArrayList<Operand> list = new ArrayList<Operand>(10);
        WithParam.gatherOperands(this, this.actualParams, list);
        WithParam.gatherOperands(this, this.tunnelParams, list);
        return list;
    }

    @Override
    public void process(Outputter output, XPathContext context) throws XPathException {
        Component target = this.getFixedTarget();
        if (this.bindingSlot >= 0 && (target = context.getTargetComponent(this.bindingSlot)).isHiddenAbstractComponent()) {
            XPathException err = new XPathException("Cannot call an abstract template (" + this.calledTemplateName.getDisplayName() + ") with no implementation", "XTDE3052");
            err.setLocation(this.getLocation());
            throw err;
        }
        NamedTemplate t = (NamedTemplate)target.getActor();
        XPathContextMajor c2 = context.newContext();
        c2.setCurrentComponent(target);
        c2.setOrigin(this);
        c2.openStackFrame(t.getStackFrameMap());
        c2.setLocalParameters(CallTemplate.assembleParams(context, this.actualParams));
        c2.setTunnelParameters(CallTemplate.assembleTunnelParams(context, this.tunnelParams));
        if (this.isWithinDeclaredStreamableConstruct) {
            c2.setCurrentGroupIterator(null);
        }
        c2.setCurrentMergeGroupIterator(null);
        try {
            for (TailCall tc = t.expand(output, c2); tc != null; tc = tc.processLeavingTail()) {
            }
        } catch (StackOverflowError e) {
            XPathException.StackOverflow err = new XPathException.StackOverflow("Too many nested template or function calls. The stylesheet may be looping.", "SXLM0001", this.getLocation());
            err.setXPathContext(context);
            throw err;
        }
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        if (this.useTailRecursion) {
            Component targetComponent = this.bindingSlot >= 0 ? context.getTargetComponent(this.bindingSlot) : this.getFixedTarget();
            if (targetComponent == null) {
                throw new XPathException("Internal Saxon error: No binding available for call-template instruction", "SXPK0001", this.getLocation());
            }
            if (targetComponent.isHiddenAbstractComponent()) {
                throw new XPathException("Cannot call an abstract template (" + this.calledTemplateName.getDisplayName() + ") with no implementation", "XTDE3052", this.getLocation());
            }
            ParameterSet params = CallTemplate.assembleParams(context, this.actualParams);
            ParameterSet tunnels = CallTemplate.assembleTunnelParams(context, this.tunnelParams);
            if (params == null) {
                params = ParameterSet.EMPTY_PARAMETER_SET;
            }
            Arrays.fill(context.getStackFrame().getStackFrameValues(), null);
            return new CallTemplatePackage(targetComponent, params, tunnels, this, output, context);
        }
        this.process(output, context);
        return null;
    }

    @Override
    public StructuredQName getObjectName() {
        return this.template == null ? null : this.template.getTemplateName();
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("callT", this);
        String flags = "";
        if (this.template != null && this.template.getTemplateName() != null) {
            out.emitAttribute("name", this.template.getTemplateName());
        }
        out.emitAttribute("bSlot", "" + this.getBindingSlot());
        if (this.isWithinDeclaredStreamableConstruct) {
            flags = flags + "d";
        }
        if (this.useTailRecursion) {
            flags = flags + "t";
        }
        if (!flags.isEmpty()) {
            out.emitAttribute("flags", flags);
        }
        if (this.actualParams.length > 0) {
            WithParam.exportParameters(this.actualParams, out, false);
        }
        if (this.tunnelParams.length > 0) {
            WithParam.exportParameters(this.tunnelParams, out, true);
        }
        out.endElement();
    }

    @Override
    public String toString() {
        FastStringBuffer buff = new FastStringBuffer(64);
        buff.append("CallTemplate#");
        if (this.template.getObjectName() != null) {
            buff.append(this.template.getObjectName().getDisplayName());
        }
        boolean first = true;
        for (WithParam p : this.getActualParams()) {
            buff.append(first ? "(" : ", ");
            buff.append(p.getVariableQName().getDisplayName());
            buff.append("=");
            buff.append(p.getSelectExpression().toString());
            first = false;
        }
        if (!first) {
            buff.append(")");
        }
        return buff.toString();
    }

    @Override
    public String toShortString() {
        FastStringBuffer buff = new FastStringBuffer(64);
        buff.append("CallTemplate#");
        buff.append(this.template.getObjectName().getDisplayName());
        return buff.toString();
    }

    @Override
    public String getStreamerName() {
        return "CallTemplate";
    }

    public static class CallTemplatePackage
    implements TailCall {
        private Component targetComponent;
        private ParameterSet params;
        private ParameterSet tunnelParams;
        private CallTemplate instruction;
        private Outputter output;
        private XPathContext evaluationContext;

        public CallTemplatePackage(Component targetComponent, ParameterSet params, ParameterSet tunnelParams, CallTemplate instruction, Outputter output, XPathContext evaluationContext) {
            this.targetComponent = targetComponent;
            if (!(targetComponent.getActor() instanceof NamedTemplate)) {
                throw new ClassCastException("Target of call-template must be a named template");
            }
            this.params = params;
            this.tunnelParams = tunnelParams;
            this.instruction = instruction;
            this.output = output;
            this.evaluationContext = evaluationContext;
        }

        @Override
        public TailCall processLeavingTail() throws XPathException {
            NamedTemplate template = (NamedTemplate)this.targetComponent.getActor();
            XPathContextMajor c2 = this.evaluationContext.newContext();
            c2.setCurrentComponent(this.targetComponent);
            c2.setOrigin(this.instruction);
            c2.setLocalParameters(this.params);
            c2.setTunnelParameters(this.tunnelParams);
            c2.openStackFrame(template.getStackFrameMap());
            c2.setCurrentMergeGroupIterator(null);
            return template.expand(this.output, c2);
        }
    }
}


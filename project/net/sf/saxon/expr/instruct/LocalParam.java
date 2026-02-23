/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.EvaluationMode;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.LocalBinding;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.Evaluator;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;

public final class LocalParam
extends Instruction
implements LocalBinding {
    private Operand conversionOp = null;
    private Evaluator conversionEvaluator = null;
    private static final int REQUIRED = 4;
    private static final int TUNNEL = 8;
    private static final int IMPLICITLY_REQUIRED = 16;
    private byte properties = 0;
    private Operand selectOp = null;
    protected StructuredQName variableQName;
    private SequenceType requiredType;
    protected int slotNumber = -999;
    protected int referenceCount = 10;
    protected Evaluator evaluator = null;

    public void setSelectExpression(Expression select) {
        if (select != null) {
            if (this.selectOp == null) {
                this.selectOp = new Operand(this, select, OperandRole.NAVIGATE);
            } else {
                this.selectOp.setChildExpression(select);
            }
        } else {
            this.selectOp = null;
        }
        this.evaluator = null;
    }

    public Expression getSelectExpression() {
        return this.selectOp == null ? null : this.selectOp.getChildExpression();
    }

    public void setRequiredType(SequenceType required) {
        this.requiredType = required;
    }

    @Override
    public SequenceType getRequiredType() {
        return this.requiredType;
    }

    public void setRequiredParam(boolean requiredParam) {
        this.properties = requiredParam ? (byte)(this.properties | 4) : (byte)(this.properties & 0xFFFFFFFB);
    }

    public void setImplicitlyRequiredParam(boolean requiredParam) {
        this.properties = requiredParam ? (byte)(this.properties | 0x10) : (byte)(this.properties & 0xFFFFFFEF);
    }

    public void setTunnel(boolean tunnel) {
        this.properties = tunnel ? (byte)(this.properties | 8) : (byte)(this.properties & 0xFFFFFFF7);
    }

    public void setReferenceCount(int refCount) {
        this.referenceCount = refCount;
    }

    @Override
    public int getCardinality() {
        return 8192;
    }

    @Override
    public boolean isAssignable() {
        return false;
    }

    @Override
    public boolean isGlobal() {
        return false;
    }

    @Override
    public int getLocalSlotNumber() {
        return this.slotNumber;
    }

    public final boolean isRequiredParam() {
        return (this.properties & 4) != 0;
    }

    public final boolean isImplicitlyRequiredParam() {
        return (this.properties & 0x10) != 0;
    }

    public final boolean isTunnelParam() {
        return (this.properties & 8) != 0;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        Expression e2 = super.typeCheck(visitor, contextItemType);
        if (e2 != this) {
            return e2;
        }
        this.checkAgainstRequiredType(visitor);
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        Expression e2 = super.optimize(visitor, contextItemType);
        if (e2 != this) {
            return e2;
        }
        return this;
    }

    public void computeEvaluationMode() {
        if (this.getSelectExpression() != null) {
            this.evaluator = this.referenceCount == 10000 ? Evaluator.MAKE_INDEXED_VARIABLE : ExpressionTool.lazyEvaluator(this.getSelectExpression(), this.referenceCount > 1);
        }
    }

    @Override
    public LocalParam copy(RebindingMap rebindings) {
        LocalParam p2 = new LocalParam();
        if (this.conversionOp != null) {
            assert (this.getConversion() != null);
            p2.setConversion(this.getConversion().copy(rebindings));
        }
        p2.conversionEvaluator = this.conversionEvaluator;
        p2.properties = this.properties;
        if (this.selectOp != null) {
            assert (this.getSelectExpression() != null);
            p2.setSelectExpression(this.getSelectExpression().copy(rebindings));
        }
        p2.variableQName = this.variableQName;
        p2.requiredType = this.requiredType;
        p2.slotNumber = this.slotNumber;
        p2.referenceCount = this.referenceCount;
        p2.evaluator = this.evaluator;
        return p2;
    }

    @Override
    public void addReference(VariableReference ref, boolean isLoopingReference) {
    }

    public void checkAgainstRequiredType(ExpressionVisitor visitor) throws XPathException {
        RoleDiagnostic role = new RoleDiagnostic(3, this.variableQName.getDisplayName(), 0);
        SequenceType r = this.requiredType;
        Expression select = this.getSelectExpression();
        if (r != null && select != null) {
            select = visitor.getConfiguration().getTypeChecker(false).staticTypeCheck(select, this.requiredType, role, visitor);
        }
    }

    public Sequence getSelectValue(XPathContext context) throws XPathException {
        Expression select = this.getSelectExpression();
        if (select == null) {
            throw new AssertionError((Object)"Internal error: No select expression");
        }
        if (select instanceof Literal) {
            return ((Literal)select).getValue();
        }
        int savedOutputState = context.getTemporaryOutputState();
        context.setTemporaryOutputState(209);
        Evaluator eval = this.evaluator == null ? Evaluator.EAGER_SEQUENCE : this.evaluator;
        Sequence result = eval.evaluate(select, context);
        context.setTemporaryOutputState(savedOutputState);
        return result;
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

    @Override
    public StructuredQName getVariableQName() {
        return this.variableQName;
    }

    public void setConversion(Expression convertor) {
        if (convertor != null) {
            if (this.conversionOp == null) {
                this.conversionOp = new Operand(this, convertor, OperandRole.SINGLE_ATOMIC);
            }
            this.conversionEvaluator = ExpressionTool.eagerEvaluator(convertor);
        } else {
            this.conversionOp = null;
        }
    }

    public Expression getConversion() {
        return this.conversionOp == null ? null : this.conversionOp.getChildExpression();
    }

    public EvaluationMode getConversionEvaluationMode() {
        return this.conversionEvaluator.getEvaluationMode();
    }

    @Override
    public int getInstructionNameCode() {
        return 189;
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operandSparseList(this.selectOp, this.conversionOp);
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        int wasSupplied = context.useLocalParameter(this.variableQName, this.slotNumber, this.isTunnelParam());
        switch (wasSupplied) {
            case 2: {
                break;
            }
            case 1: {
                if (this.conversionOp == null) break;
                context.setLocalVariable(this.slotNumber, this.conversionEvaluator.evaluate(this.getConversion(), context));
                break;
            }
            case 0: {
                if (this.isRequiredParam() || this.isImplicitlyRequiredParam()) {
                    String name = "$" + this.getVariableQName().getDisplayName();
                    int suppliedAsTunnel = context.useLocalParameter(this.variableQName, this.slotNumber, !this.isTunnelParam());
                    String message = "No value supplied for required parameter " + name;
                    if (this.isImplicitlyRequiredParam()) {
                        message = message + ". A value is required because the default value is not a valid instance of the required type";
                    }
                    if (suppliedAsTunnel != 0) {
                        message = this.isTunnelParam() ? message + ". A non-tunnel parameter with this name was supplied, but a tunnel parameter is required" : message + ". A tunnel parameter with this name was supplied, but a non-tunnel parameter is required";
                    }
                    XPathException e = new XPathException(message);
                    e.setXPathContext(context);
                    e.setErrorCode("XTDE0700");
                    throw e;
                }
                context.setLocalVariable(this.slotNumber, this.getSelectValue(context));
            }
        }
        return null;
    }

    @Override
    public IntegerValue[] getIntegerBoundsForVariable() {
        return null;
    }

    @Override
    public Sequence evaluateVariable(XPathContext c) {
        return c.evaluateLocalVariable(this.slotNumber);
    }

    public boolean isCompatible(LocalParam other) {
        return this.getVariableQName().equals(other.getVariableQName()) && this.getRequiredType().equals(other.getRequiredType()) && this.isTunnelParam() == other.isTunnelParam();
    }

    @Override
    public boolean isLiftable(boolean forStreaming) {
        return false;
    }

    @Override
    public boolean hasVariableBinding(Binding binding) {
        return this == binding;
    }

    @Override
    public ItemType getItemType() {
        return ErrorType.getInstance();
    }

    @Override
    public int computeCardinality() {
        return 57344;
    }

    @Override
    public int computeSpecialProperties() {
        return 0x2000000;
    }

    @Override
    public boolean mayCreateNewNodes() {
        return false;
    }

    @Override
    public String getExpressionName() {
        return "param";
    }

    @Override
    public String toShortString() {
        return "$" + this.getVariableQName().getDisplayName();
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        Expression conversion;
        out.startElement("param", this);
        out.emitAttribute("name", this.getVariableQName());
        out.emitAttribute("slot", "" + this.getSlotNumber());
        String flags = this.getFlags();
        if (!flags.isEmpty()) {
            out.emitAttribute("flags", flags);
        }
        ExpressionPresenter.ExportOptions options = (ExpressionPresenter.ExportOptions)out.getOptions();
        if (this.getRequiredType() != SequenceType.ANY_SEQUENCE) {
            out.emitAttribute("as", this.getRequiredType().toAlphaCode());
        }
        if (this.getSelectExpression() != null) {
            out.setChildRole("select");
            this.getSelectExpression().export(out);
        }
        if ((conversion = this.getConversion()) != null) {
            out.setChildRole("conversion");
            conversion.export(out);
        }
        out.endElement();
    }

    private String getFlags() {
        String flags = "";
        if (this.isTunnelParam()) {
            flags = flags + "t";
        }
        if (this.isRequiredParam()) {
            flags = flags + "r";
        }
        if (this.isImplicitlyRequiredParam()) {
            flags = flags + "i";
        }
        return flags;
    }

    @Override
    public void setIndexedVariable() {
    }

    @Override
    public boolean isIndexedVariable() {
        return false;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.Properties;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FunctionCall;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.functions.AbstractFunction;
import net.sf.saxon.functions.registry.BuiltInFunctionSet;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;

public abstract class SystemFunction
extends AbstractFunction {
    private int arity;
    private BuiltInFunctionSet.Entry details;
    private RetainedStaticContext retainedStaticContext;

    public static Expression makeCall(String name, RetainedStaticContext rsc, Expression ... arguments) {
        SystemFunction f = SystemFunction.makeFunction(name, rsc, arguments.length);
        if (f == null) {
            return null;
        }
        Expression expr = f.makeFunctionCall(arguments);
        expr.setRetainedStaticContext(rsc);
        return expr;
    }

    public static SystemFunction makeFunction(String name, RetainedStaticContext rsc, int arity) {
        if (rsc == null) {
            throw new NullPointerException();
        }
        SystemFunction fn = rsc.getConfiguration().makeSystemFunction(name, arity);
        if (fn == null) {
            rsc.getConfiguration().makeSystemFunction(name, arity);
            throw new IllegalStateException(name);
        }
        fn.setRetainedStaticContext(rsc);
        return fn;
    }

    public Expression makeFunctionCall(Expression ... arguments) {
        SystemFunctionCall e = new SystemFunctionCall(this, arguments);
        ((Expression)e).setRetainedStaticContext(this.getRetainedStaticContext());
        return e;
    }

    public void setArity(int arity) {
        this.arity = arity;
    }

    public int getNetCost() {
        return 1;
    }

    public Expression makeOptimizedFunctionCall(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo, Expression ... arguments) throws XPathException {
        Optimizer opt = visitor.obtainOptimizer();
        if (opt.isOptionSet(32768)) {
            return this.fixArguments(arguments);
        }
        return null;
    }

    public Expression fixArguments(Expression ... arguments) throws XPathException {
        for (int i = 0; i < this.getArity(); ++i) {
            if (!Literal.isEmptySequence(arguments[i]) || this.resultIfEmpty(i) == null) continue;
            return Literal.makeLiteral(this.details.resultIfEmpty[i].materialize());
        }
        return null;
    }

    protected Sequence resultIfEmpty(int arg) {
        return this.details.resultIfEmpty[arg];
    }

    public RetainedStaticContext getRetainedStaticContext() {
        return this.retainedStaticContext;
    }

    public void setRetainedStaticContext(RetainedStaticContext retainedStaticContext) {
        this.retainedStaticContext = retainedStaticContext;
    }

    public boolean dependsOnContextItem() {
        return (this.details.properties & 0x4004) != 0;
    }

    public void setDetails(BuiltInFunctionSet.Entry entry) {
        this.details = entry;
    }

    public BuiltInFunctionSet.Entry getDetails() {
        return this.details;
    }

    @Override
    public StructuredQName getFunctionName() {
        return this.details.name;
    }

    @Override
    public String getDescription() {
        return this.details.name.getDisplayName();
    }

    @Override
    public int getArity() {
        return this.arity;
    }

    @Override
    public OperandRole[] getOperandRoles() {
        OperandRole[] roles = new OperandRole[this.getArity()];
        OperandUsage[] usages = this.details.usage;
        try {
            for (int i = 0; i < this.getArity(); ++i) {
                roles[i] = new OperandRole(0, usages[i], this.getRequiredType(i));
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return roles;
    }

    public IntegerValue[] getIntegerBounds() {
        return null;
    }

    public void supplyTypeInformation(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType, Expression[] arguments) throws XPathException {
    }

    public boolean equals(Object o) {
        return o instanceof SystemFunction && super.equals(o);
    }

    public String getErrorCodeForTypeErrors() {
        return "XPTY0004";
    }

    public SequenceType getRequiredType(int arg) {
        if (this.details == null) {
            return SequenceType.ANY_SEQUENCE;
        }
        return this.details.argumentTypes[arg];
    }

    public ItemType getResultItemType() {
        return this.details.itemType;
    }

    @Override
    public FunctionItemType getFunctionItemType() {
        SequenceType resultType = SequenceType.makeSequenceType(this.getResultItemType(), this.details.cardinality);
        return new SpecificFunctionType(this.details.argumentTypes, resultType);
    }

    public ItemType getResultItemType(Expression[] args) {
        if ((this.details.properties & 1) != 0) {
            return args[0].getItemType();
        }
        if ((this.details.properties & 2) != 0) {
            return args[0].getItemType().getPrimitiveItemType();
        }
        return this.details.itemType;
    }

    public int getCardinality(Expression[] args) {
        int c = this.details.cardinality;
        if (c == 24576 && (this.details.properties & 0x8000) != 0 && !Cardinality.allowsZero(args[0].getCardinality())) {
            return 16384;
        }
        return c;
    }

    public int getSpecialProperties(Expression[] arguments) {
        if ((this.details.properties & 0x10000) != 0) {
            return 0x400000;
        }
        int p = 0x800000;
        if ((this.details.properties & 0x2000) != 0) {
            p |= 0x2000000;
        }
        return p;
    }

    protected NodeInfo getContextNode(XPathContext context) throws XPathException {
        Item item = context.getContextItem();
        if (item == null) {
            XPathException err = new XPathException("Context item for " + this.getFunctionName() + "() is absent", "XPDY0002");
            err.maybeSetContext(context);
            throw err;
        }
        if (!(item instanceof NodeInfo)) {
            XPathException err = new XPathException("Context item for " + this.getFunctionName() + "() is not a node", "XPTY0004");
            err.maybeSetContext(context);
            throw err;
        }
        return (NodeInfo)item;
    }

    public static Sequence dynamicCall(Function f, XPathContext context, Sequence[] args) throws XPathException {
        context = f.makeNewContext(context, null);
        context.setCurrentOutputUri(null);
        return f.call(context, args);
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("fnRef");
        StructuredQName qName = this.getFunctionName();
        String name = qName.hasURI("http://www.w3.org/2005/xpath-functions") ? qName.getLocalPart() : qName.getEQName();
        out.emitAttribute("name", name);
        out.emitAttribute("arity", this.getArity() + "");
        if ((this.getDetails().properties & 0x38) != 0) {
            out.emitRetainedStaticContext(this.getRetainedStaticContext(), null);
        }
        out.endElement();
    }

    public Expression typeCheckCaller(FunctionCall caller, ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        return caller;
    }

    @Override
    public boolean isTrustedResultType() {
        return true;
    }

    public String getStaticBaseUriString() {
        return this.getRetainedStaticContext().getStaticBaseUriString();
    }

    public void exportAttributes(ExpressionPresenter out) {
    }

    public void exportAdditionalArguments(SystemFunctionCall call, ExpressionPresenter out) throws XPathException {
    }

    public void importAttributes(Properties attributes) throws XPathException {
    }

    public String getCompilerName() {
        return null;
    }

    public String getStreamerName() {
        return null;
    }

    @Override
    public String toShortString() {
        return this.getFunctionName().getDisplayName() + '#' + this.getArity();
    }

    public String toString() {
        return this.getFunctionName().getDisplayName() + '#' + this.getArity();
    }
}


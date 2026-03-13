/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.Arrays;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemChecker;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Negatable;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.StaticFunctionCall;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.AnalyzeString;
import net.sf.saxon.expr.oper.OperandArray;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.Evaluator;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.functions.BooleanFn;
import net.sf.saxon.functions.CollectionFn;
import net.sf.saxon.functions.Concat;
import net.sf.saxon.functions.CurrentMergeGroup;
import net.sf.saxon.functions.CurrentMergeKey;
import net.sf.saxon.functions.Doc;
import net.sf.saxon.functions.DocumentFn;
import net.sf.saxon.functions.Empty;
import net.sf.saxon.functions.Error;
import net.sf.saxon.functions.Exists;
import net.sf.saxon.functions.KeyFn;
import net.sf.saxon.functions.NotFn;
import net.sf.saxon.functions.PushableFunction;
import net.sf.saxon.functions.RegexGroup;
import net.sf.saxon.functions.Reverse;
import net.sf.saxon.functions.Root_1;
import net.sf.saxon.functions.StatefulSystemFunction;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.functions.TreatFn;
import net.sf.saxon.functions.registry.BuiltInFunctionSet;
import net.sf.saxon.ma.map.MapFunctionSet;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.pattern.NodeSetPattern;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.IntegerValue;

public class SystemFunctionCall
extends StaticFunctionCall
implements Negatable {
    public Evaluator[] argumentEvaluators;

    public SystemFunctionCall(SystemFunction target, Expression[] arguments) {
        super(target, arguments);
        this.argumentEvaluators = new Evaluator[arguments.length];
        Arrays.fill(this.argumentEvaluators, Evaluator.LAZY_SEQUENCE);
    }

    @Override
    public void setRetainedStaticContext(RetainedStaticContext rsc) {
        super.setRetainedStaticContext(rsc);
        this.getTargetFunction().setRetainedStaticContext(rsc);
    }

    @Override
    public Expression preEvaluate(ExpressionVisitor visitor) throws XPathException {
        SystemFunction target = this.getTargetFunction();
        if ((target.getDetails().properties & 0x200) == 0) {
            return super.preEvaluate(visitor);
        }
        return this;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.typeCheckChildren(visitor, contextInfo);
        this.checkFunctionCall(this.getTargetFunction(), visitor);
        this.getTargetFunction().supplyTypeInformation(visitor, contextInfo, this.getArguments());
        if ((this.getTargetFunction().getDetails().properties & 0x200) == 0) {
            return this.preEvaluateIfConstant(visitor);
        }
        this.allocateArgumentEvaluators(this.getArguments());
        return this;
    }

    private void allocateArgumentEvaluators(Expression[] arguments) {
        for (int i = 0; i < arguments.length; ++i) {
            int cardinality;
            Expression arg = arguments[i];
            int n = cardinality = this.isCallOn(Concat.class) ? 24576 : this.getTargetFunction().getDetails().argumentTypes[i].getCardinality();
            this.argumentEvaluators[i] = arg instanceof Literal ? Evaluator.LITERAL : (arg instanceof VariableReference ? Evaluator.VARIABLE : (cardinality == 16384 ? Evaluator.SINGLE_ITEM : (cardinality == 24576 ? Evaluator.OPTIONAL_ITEM : Evaluator.LAZY_SEQUENCE)));
        }
    }

    @Override
    public SystemFunction getTargetFunction() {
        return (SystemFunction)super.getTargetFunction();
    }

    @Override
    public int getIntrinsicDependencies() {
        int properties = this.getTargetFunction().getDetails().properties;
        int dep = 0;
        if ((properties & 0x200) != 0) {
            dep = 1024;
        }
        if ((properties & 0x5804) != 0) {
            if ((properties & 0x4000) != 0) {
                dep |= 0x10;
            }
            if ((properties & 4) != 0) {
                dep |= 2;
            }
            if ((properties & 0x800) != 0) {
                dep |= 4;
            }
            if ((properties & 0x1000) != 0) {
                dep |= 8;
            }
        }
        if ((properties & 8) != 0) {
            dep |= 0x800;
        }
        if ((properties & 0x20) != 0) {
            dep |= 0x800;
        }
        if (this.isCallOn(RegexGroup.class) || this.isCallOn(CurrentMergeGroup.class) || this.isCallOn(CurrentMergeKey.class)) {
            dep |= 0x20;
        }
        return dep;
    }

    @Override
    protected int computeCardinality() {
        return this.getTargetFunction().getCardinality(this.getArguments());
    }

    @Override
    protected int computeSpecialProperties() {
        return this.getTargetFunction().getSpecialProperties(this.getArguments());
    }

    @Override
    public int getNetCost() {
        return this.getTargetFunction().getNetCost();
    }

    @Override
    public Expression getScopingExpression() {
        if (this.isCallOn(RegexGroup.class)) {
            for (Expression parent = this.getParentExpression(); parent != null; parent = parent.getParentExpression()) {
                if (!(parent instanceof AnalyzeString)) continue;
                return parent;
            }
            return null;
        }
        return super.getScopingExpression();
    }

    @Override
    public boolean isLiftable(boolean forStreaming) {
        return super.isLiftable(forStreaming) && !this.isCallOn(CurrentMergeGroup.class) && !this.isCallOn(CurrentMergeKey.class) && (!forStreaming || !this.isCallOn(MapFunctionSet.MapEntry.class));
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Expression sfo;
        Optimizer opt = visitor.obtainOptimizer();
        Expression sf = super.optimize(visitor, contextInfo);
        if (sf == this && (sfo = this.getTargetFunction().makeOptimizedFunctionCall(visitor, contextInfo, this.getArguments())) != null) {
            sfo.setParentExpression(this.getParentExpression());
            ExpressionTool.copyLocationInfo(this, sfo);
            if (sfo instanceof SystemFunctionCall) {
                ((SystemFunctionCall)sfo).allocateArgumentEvaluators(((SystemFunctionCall)sfo).getArguments());
            }
            return sfo;
        }
        if (sf instanceof SystemFunctionCall && opt.isOptionSet(32768)) {
            BuiltInFunctionSet.Entry details = ((SystemFunctionCall)sf).getTargetFunction().getDetails();
            if ((details.properties & 0x400) != 0) {
                this.setArg(0, this.getArg(0).unordered(true, visitor.isOptimizeForStreaming()));
            }
            if (this.getArity() <= details.resultIfEmpty.length) {
                for (int i = 0; i < this.getArity(); ++i) {
                    if (!Literal.isEmptySequence(this.getArg(i)) || details.resultIfEmpty[i] == null) continue;
                    return Literal.makeLiteral(details.resultIfEmpty[i].materialize(), this);
                }
            }
            ((SystemFunctionCall)sf).allocateArgumentEvaluators(((SystemFunctionCall)sf).getArguments());
        }
        return sf;
    }

    @Override
    public boolean isVacuousExpression() {
        return this.isCallOn(Error.class);
    }

    @Override
    public ItemType getItemType() {
        return this.getTargetFunction().getResultItemType(this.getArguments());
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        Expression[] args = new Expression[this.getArity()];
        for (int i = 0; i < args.length; ++i) {
            args[i] = this.getArg(i).copy(rebindings);
        }
        SystemFunction target = this.getTargetFunction();
        if (target instanceof StatefulSystemFunction) {
            target = ((StatefulSystemFunction)((Object)target)).copy();
        }
        return target.makeFunctionCall(args);
    }

    @Override
    public IntegerValue[] getIntegerBounds() {
        SystemFunction fn = this.getTargetFunction();
        if ((fn.getDetails().properties & 0x100) != 0) {
            return this.getArg(0).getIntegerBounds();
        }
        return fn.getIntegerBounds();
    }

    @Override
    public boolean isNegatable(TypeHierarchy th) {
        return this.isCallOn(NotFn.class) || this.isCallOn(BooleanFn.class) || this.isCallOn(Empty.class) || this.isCallOn(Exists.class);
    }

    @Override
    public Expression negate() {
        SystemFunction fn = this.getTargetFunction();
        if (fn instanceof NotFn) {
            Expression arg = this.getArg(0);
            if (arg.getItemType() == BuiltInAtomicType.BOOLEAN && arg.getCardinality() == 16384) {
                return arg;
            }
            return SystemFunction.makeCall("boolean", this.getRetainedStaticContext(), arg);
        }
        if (fn instanceof BooleanFn) {
            return SystemFunction.makeCall("not", this.getRetainedStaticContext(), this.getArg(0));
        }
        if (fn instanceof Exists) {
            return SystemFunction.makeCall("empty", this.getRetainedStaticContext(), this.getArg(0));
        }
        if (fn instanceof Empty) {
            return SystemFunction.makeCall("exists", this.getRetainedStaticContext(), this.getArg(0));
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression unordered(boolean retainAllNodes, boolean forStreaming) throws XPathException {
        SystemFunction fn = this.getTargetFunction();
        if (fn instanceof Reverse) {
            return this.getArg(0);
        }
        if (fn instanceof TreatFn) {
            this.setArg(0, this.getArg(0).unordered(retainAllNodes, forStreaming));
        }
        return this;
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        if (this.isCallOn(Doc.class) || this.isCallOn(DocumentFn.class) || this.isCallOn(CollectionFn.class)) {
            this.getArg(0).addToPathMap(pathMap, pathMapNodeSet);
            return new PathMap.PathMapNodeSet(pathMap.makeNewRoot(this));
        }
        if (this.isCallOn(KeyFn.class)) {
            return ((KeyFn)this.getTargetFunction()).addToPathMap(pathMap, pathMapNodeSet);
        }
        return super.addToPathMap(pathMap, pathMapNodeSet);
    }

    @Override
    public Pattern toPattern(Configuration config) throws XPathException {
        SystemFunction fn = this.getTargetFunction();
        if (fn instanceof Root_1 && (this.getArg(0) instanceof ContextItemExpression || this.getArg(0) instanceof ItemChecker && ((ItemChecker)this.getArg(0)).getBaseExpression() instanceof ContextItemExpression)) {
            return new NodeSetPattern(this);
        }
        return super.toPattern(config);
    }

    @Override
    public Sequence[] evaluateArguments(XPathContext context) throws XPathException {
        OperandArray operanda = this.getOperanda();
        int numArgs = operanda.getNumberOfOperands();
        Sequence[] actualArgs = new Sequence[numArgs];
        for (int i = 0; i < numArgs; ++i) {
            Expression exp = operanda.getOperandExpression(i);
            actualArgs[i] = this.argumentEvaluators[i].evaluate(exp, context);
        }
        return actualArgs;
    }

    @Override
    public void resetLocalStaticProperties() {
        super.resetLocalStaticProperties();
        if (this.argumentEvaluators != null) {
            this.allocateArgumentEvaluators(this.getArguments());
        }
    }

    @Override
    public void process(Outputter output, XPathContext context) throws XPathException {
        SystemFunction target = this.getTargetFunction();
        if (target instanceof PushableFunction) {
            Sequence[] actualArgs = this.evaluateArguments(context);
            try {
                ((PushableFunction)((Object)target)).process(output, context, actualArgs);
            } catch (XPathException e) {
                e.maybeSetLocation(this.getLocation());
                e.maybeSetContext(context);
                e.maybeSetFailingExpression(this);
                throw e;
            }
        } else {
            super.process(output, context);
        }
    }

    @Override
    public String getExpressionName() {
        return "sysFuncCall";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        if (this.getFunctionName().hasURI("http://www.w3.org/2005/xpath-functions")) {
            out.startElement("fn", this);
            out.emitAttribute("name", this.getFunctionName().getLocalPart());
            this.getTargetFunction().exportAttributes(out);
            for (Operand o : this.operands()) {
                o.getChildExpression().export(out);
            }
            this.getTargetFunction().exportAdditionalArguments(this, out);
            out.endElement();
        } else {
            out.startElement("ifCall", this);
            out.emitAttribute("name", this.getFunctionName());
            out.emitAttribute("type", this.getTargetFunction().getFunctionItemType().getResultType().toAlphaCode());
            this.getTargetFunction().exportAttributes(out);
            for (Operand o : this.operands()) {
                o.getChildExpression().export(out);
            }
            this.getTargetFunction().exportAdditionalArguments(this, out);
            out.endElement();
        }
    }

    public static abstract class Optimized
    extends SystemFunctionCall {
        public Optimized(SystemFunction target, Expression[] arguments) {
            super(target, arguments);
        }

        @Override
        public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
            return this;
        }
    }
}


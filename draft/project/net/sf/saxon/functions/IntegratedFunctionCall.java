/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.CardinalityCheckingIterator;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FunctionCall;
import net.sf.saxon.expr.ItemMappingFunction;
import net.sf.saxon.expr.ItemMappingIterator;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.SequenceType;

public class IntegratedFunctionCall
extends FunctionCall
implements Callable {
    private StructuredQName name;
    private ExtensionFunctionCall function;
    private SequenceType resultType = SequenceType.ANY_SEQUENCE;
    private int state = 0;

    public IntegratedFunctionCall(StructuredQName name, ExtensionFunctionCall function) {
        this.name = name;
        this.function = function;
    }

    public void setResultType(SequenceType resultType) {
        this.resultType = resultType;
    }

    @Override
    public StructuredQName getFunctionName() {
        return this.name;
    }

    @Override
    public Function getTargetFunction(XPathContext context) {
        return null;
    }

    public ExtensionFunctionCall getFunction() {
        return this.function;
    }

    @Override
    public void checkArguments(ExpressionVisitor visitor) throws XPathException {
        ExtensionFunctionDefinition definition = this.function.getDefinition();
        this.checkArgumentCount(definition.getMinimumNumberOfArguments(), definition.getMaximumNumberOfArguments());
        int args = this.getArity();
        SequenceType[] declaredArgumentTypes = definition.getArgumentTypes();
        if (declaredArgumentTypes == null || args != 0 && declaredArgumentTypes.length == 0) {
            throw new XPathException("Integrated function " + this.getDisplayName() + " failed to declare its argument types");
        }
        SequenceType[] actualArgumentTypes = new SequenceType[args];
        TypeChecker tc = visitor.getConfiguration().getTypeChecker(false);
        for (int i = 0; i < args; ++i) {
            this.setArg(i, tc.staticTypeCheck(this.getArg(i), i < declaredArgumentTypes.length ? declaredArgumentTypes[i] : declaredArgumentTypes[declaredArgumentTypes.length - 1], new RoleDiagnostic(0, this.getFunctionName().getDisplayName(), i), visitor));
            actualArgumentTypes[i] = SequenceType.makeSequenceType(this.getArg(i).getItemType(), this.getArg(i).getCardinality());
        }
        this.resultType = definition.getResultType(actualArgumentTypes);
        if (this.state == 0) {
            this.function.supplyStaticContext(visitor.getStaticContext(), 0, this.getArguments());
        }
        ++this.state;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Expression exp = super.typeCheck(visitor, contextInfo);
        if (exp instanceof IntegratedFunctionCall) {
            Expression exp2 = ((IntegratedFunctionCall)exp).function.rewrite(visitor.getStaticContext(), this.getArguments());
            if (exp2 == null) {
                return exp;
            }
            ExpressionTool.copyLocationInfo(this, exp2);
            return exp2.simplify().typeCheck(visitor, contextInfo).optimize(visitor, contextInfo);
        }
        return exp;
    }

    @Override
    public Expression preEvaluate(ExpressionVisitor visitor) {
        return this;
    }

    @Override
    public ItemType getItemType() {
        return this.resultType.getPrimaryType();
    }

    @Override
    protected int computeCardinality() {
        return this.resultType.getCardinality();
    }

    @Override
    public int getIntrinsicDependencies() {
        ExtensionFunctionDefinition definition = this.function.getDefinition();
        return definition.dependsOnFocus() ? 30 : 0;
    }

    @Override
    protected int computeSpecialProperties() {
        ExtensionFunctionDefinition definition = this.function.getDefinition();
        return definition.hasSideEffects() ? 0x2000000 : 0x800000;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        ExtensionFunctionCall newCall = this.function.getDefinition().makeCallExpression();
        newCall.setDefinition(this.function.getDefinition());
        this.function.copyLocalData(newCall);
        IntegratedFunctionCall copy = new IntegratedFunctionCall(this.getFunctionName(), newCall);
        Expression[] args = new Expression[this.getArity()];
        for (int i = 0; i < args.length; ++i) {
            args[i] = this.getArg(i).copy(rebindings);
        }
        copy.setArguments(args);
        copy.resultType = this.resultType;
        copy.state = this.state;
        ExpressionTool.copyLocationInfo(this, copy);
        return copy;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("ifCall", this);
        out.emitAttribute("name", this.getFunctionName());
        out.emitAttribute("type", this.resultType.toAlphaCode());
        for (Operand o : this.operands()) {
            o.getChildExpression().export(out);
        }
        out.endElement();
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        SequenceIterator result;
        ExtensionFunctionDefinition definition = this.function.getDefinition();
        Sequence[] argValues = new Sequence[this.getArity()];
        for (int i = 0; i < argValues.length; ++i) {
            argValues[i] = SequenceTool.toLazySequence(this.getArg(i).iterate(context));
        }
        RoleDiagnostic role = new RoleDiagnostic(5, this.getFunctionName().getDisplayName(), 0);
        Configuration config = context.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();
        try {
            result = this.function.call(context, argValues).iterate();
        } catch (XPathException e) {
            e.maybeSetLocation(this.getLocation());
            throw e;
        }
        if (!definition.trustResultType()) {
            ItemType type;
            int card = this.resultType.getCardinality();
            if (card != 57344) {
                result = new CardinalityCheckingIterator(result, card, role, this.getLocation());
            }
            if ((type = this.resultType.getPrimaryType()) != AnyItemType.getInstance()) {
                result = new ItemMappingIterator(result, item -> {
                    if (!type.matches(item, th)) {
                        String msg = role.composeErrorMessage(type, item, th);
                        XPathException err = new XPathException(msg, "XPTY0004");
                        err.setLocation(this.getLocation());
                        throw err;
                    }
                    return item;
                }, true);
            }
            if (th.relationship(type, AnyNodeTest.getInstance()) != Affinity.DISJOINT) {
                result = new ItemMappingIterator(result, new ConfigurationCheckingFunction(context.getConfiguration()), true);
            }
        }
        return result;
    }

    @Override
    public boolean effectiveBooleanValue(XPathContext context) throws XPathException {
        Sequence[] argValues = new Sequence[this.getArity()];
        for (int i = 0; i < argValues.length; ++i) {
            argValues[i] = SequenceTool.toLazySequence(this.getArg(i).iterate(context));
        }
        try {
            return this.function.effectiveBooleanValue(context, argValues);
        } catch (XPathException e) {
            e.maybeSetLocation(this.getLocation());
            throw e;
        }
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        return this.function.call(context, arguments);
    }

    public static class ConfigurationCheckingFunction
    implements ItemMappingFunction {
        private Configuration config;

        public ConfigurationCheckingFunction(Configuration config) {
            this.config = config;
        }

        @Override
        public Item mapItem(Item item) throws XPathException {
            if (item instanceof NodeInfo && !this.config.isCompatible(((NodeInfo)item).getConfiguration())) {
                throw new XPathException("Node returned by extension function was created with an incompatible Configuration", "SXXP0004");
            }
            return item;
        }
    }
}


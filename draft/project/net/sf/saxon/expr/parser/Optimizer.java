/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.parser;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ExpressionOwner;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.GeneralComparison;
import net.sf.saxon.expr.IsLastExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OrExpression;
import net.sf.saxon.expr.QuantifiedExpression;
import net.sf.saxon.expr.SlashExpression;
import net.sf.saxon.expr.UserFunctionCall;
import net.sf.saxon.expr.ValueComparison;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Choose;
import net.sf.saxon.expr.instruct.NamedTemplate;
import net.sf.saxon.expr.instruct.NumberInstruction;
import net.sf.saxon.expr.instruct.TemplateRule;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.ICompilerService;
import net.sf.saxon.expr.parser.OptimizerOptions;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.expr.sort.DocumentSorter;
import net.sf.saxon.functions.PositionAndLast;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.NodeSetPattern;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.style.XSLFunction;
import net.sf.saxon.style.XSLTemplate;
import net.sf.saxon.trans.GlobalVariableManager;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.RuleTarget;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.BooleanValue;

public class Optimizer {
    protected Configuration config;
    private OptimizerOptions optimizerOptions = OptimizerOptions.FULL_EE_OPTIMIZATION;
    protected boolean tracing;

    public Optimizer(Configuration config) {
        this.config = config;
        this.tracing = config.getBooleanProperty(Feature.TRACE_OPTIMIZER_DECISIONS);
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    public void setOptimizerOptions(OptimizerOptions options) {
        this.optimizerOptions = options;
    }

    public OptimizerOptions getOptimizerOptions() {
        return this.optimizerOptions;
    }

    public boolean isOptionSet(int option) {
        return this.optimizerOptions.isSet(option);
    }

    public Expression optimizeValueComparison(ValueComparison vc, ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Expression rhs;
        Expression lhs = vc.getLhsExpression();
        Expression e2 = this.optimizePositionVsLast(lhs, rhs = vc.getRhsExpression(), vc.getOperator());
        if (e2 != null) {
            this.trace("Rewrote position() ~= last()", e2);
            return e2;
        }
        e2 = this.optimizePositionVsLast(rhs, lhs, Token.inverse(vc.getOperator()));
        if (e2 != null) {
            this.trace("Rewrote last() ~= position()", e2);
            return e2;
        }
        return vc;
    }

    private Expression optimizePositionVsLast(Expression lhs, Expression rhs, int operator) {
        if (lhs.isCallOn(PositionAndLast.Position.class) && rhs.isCallOn(PositionAndLast.Last.class)) {
            switch (operator) {
                case 50: 
                case 54: {
                    IsLastExpression iletrue = new IsLastExpression(true);
                    ExpressionTool.copyLocationInfo(lhs, iletrue);
                    return iletrue;
                }
                case 51: 
                case 53: {
                    IsLastExpression ilefalse = new IsLastExpression(false);
                    ExpressionTool.copyLocationInfo(lhs, ilefalse);
                    return ilefalse;
                }
                case 52: {
                    return Literal.makeLiteral(BooleanValue.FALSE, lhs);
                }
                case 55: {
                    return Literal.makeLiteral(BooleanValue.TRUE, lhs);
                }
            }
        }
        return null;
    }

    public Expression optimizeGeneralComparison(ExpressionVisitor visitor, GeneralComparison gc, boolean backwardsCompatible, ContextItemStaticInfo contextItemType) {
        return gc;
    }

    public Expression optimizeSaxonStreamFunction(ExpressionVisitor visitor, ContextItemStaticInfo cisi, Expression select) throws XPathException {
        if (select.getItemType().isPlainType()) {
            return select;
        }
        return null;
    }

    public Expression convertPathExpressionToKey(SlashExpression pathExp, ExpressionVisitor visitor) throws XPathException {
        return null;
    }

    public Expression tryIndexedFilter(FilterExpression f, ExpressionVisitor visitor, boolean indexFirstOperand, boolean contextIsDoc) {
        return f;
    }

    public FilterExpression reorderPredicates(FilterExpression f, ExpressionVisitor visitor, ContextItemStaticInfo cisi) throws XPathException {
        return f;
    }

    public FilterExpression convertToFilterExpression(SlashExpression pathExp, TypeHierarchy th) throws XPathException {
        return null;
    }

    public int isIndexableFilter(Expression filter) {
        return 0;
    }

    public GroundedValue makeIndexedValue(SequenceIterator iter) throws XPathException {
        throw new UnsupportedOperationException("Indexing requires Saxon-EE");
    }

    public void optimizeNodeSetPattern(NodeSetPattern pattern) {
    }

    public void prepareForStreaming(Expression exp) throws XPathException {
    }

    public Sequence evaluateStreamingArgument(Expression expr, XPathContext context) throws XPathException {
        return ExpressionTool.eagerEvaluate(expr, context);
    }

    public boolean isVariableReplaceableByDot(Expression exp, Binding[] binding) {
        for (Operand o : exp.operands()) {
            if (!(o.hasSameFocus() ? !this.isVariableReplaceableByDot(o.getChildExpression(), binding) : ExpressionTool.dependsOnVariable(o.getChildExpression(), binding))) continue;
            return false;
        }
        return true;
    }

    public Expression makeConditionalDocumentSorter(DocumentSorter sorter, SlashExpression path) throws XPathException {
        return sorter;
    }

    public Expression tryInlineFunctionCall(UserFunctionCall functionCall, ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) {
        return functionCall;
    }

    public Expression promoteExpressionsToGlobal(Expression body, GlobalVariableManager gvManager, ExpressionVisitor visitor) throws XPathException {
        return null;
    }

    public Expression eliminateCommonSubexpressions(Expression in) {
        return in;
    }

    public Expression trySwitch(Choose choose, ExpressionVisitor visitor) {
        return choose;
    }

    public Expression tryGeneralComparison(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType, OrExpression orExpr) throws XPathException {
        return orExpr;
    }

    public RuleTarget makeInversion(Pattern pattern, NamedTemplate template) throws XPathException {
        return null;
    }

    public void makeCopyOperationsExplicit(Expression parent, Operand child) throws XPathException {
    }

    public void checkStreamability(XSLTemplate sourceTemplate, TemplateRule compiledTemplate) throws XPathException {
    }

    public Expression optimizeQuantifiedExpressionForStreaming(QuantifiedExpression expr) throws XPathException {
        return expr;
    }

    public Expression generateMultithreadedInstruction(Expression instruction) {
        return instruction;
    }

    public Expression compileToByteCode(ICompilerService compilerService, Expression expr, String objectName, int evaluationMethods) {
        return null;
    }

    public Expression makeByteCodeCandidate(ExpressionOwner owner, Expression expr, String objectName, int requiredEvaluationModes) {
        return expr;
    }

    public void injectByteCodeCandidates(Expression exp) throws XPathException {
    }

    public Expression optimizeNumberInstruction(NumberInstruction ni, ContextItemStaticInfo contextInfo) {
        return null;
    }

    public void assessFunctionStreamability(XSLFunction reporter, UserFunction compiledFunction) throws XPathException {
        throw new XPathException("Streamable stylesheet functions are not supported in Saxon-HE", "XTSE3430");
    }

    public void trace(String message, Expression exp) {
        if (this.tracing) {
            Logger err = this.getConfiguration().getLogger();
            err.info("OPT : At line " + exp.getLocation().getLineNumber() + " of " + exp.getLocation().getSystemId());
            err.info("OPT : " + message);
            err.info("OPT : Expression after rewrite: " + exp.toString());
            exp.verifyParentPointers();
        }
    }

    public static void trace(Configuration config, String message, Expression exp) {
        if (config.getBooleanProperty(Feature.TRACE_OPTIMIZER_DECISIONS)) {
            Logger err = config.getLogger();
            err.info("OPT : At line " + exp.getLocation().getLineNumber() + " of " + exp.getLocation().getSystemId());
            err.info("OPT : " + message);
            err.info("OPT : Expression after rewrite: " + exp.toString());
            exp.verifyParentPointers();
        }
    }
}


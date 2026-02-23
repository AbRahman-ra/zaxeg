/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.parser;

import java.util.IdentityHashMap;
import java.util.Map;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.EvaluationMode;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.LocalVariableReference;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.PseudoExpression;
import net.sf.saxon.expr.TryCatch;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.instruct.ConditionalInstruction;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.SequenceType;

public class LoopLifter {
    private Expression root;
    private Configuration config;
    private int sequence = 0;
    private boolean changed = false;
    private boolean tracing = false;
    private boolean streaming = false;
    private Map<Expression, ExpInfo> expInfoMap = new IdentityHashMap<Expression, ExpInfo>();

    public static Expression process(Expression exp, ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        if (exp instanceof Literal || exp instanceof VariableReference) {
            return exp;
        }
        LoopLifter lifter = new LoopLifter(exp, visitor.getConfiguration(), visitor.isOptimizeForStreaming());
        RetainedStaticContext rsc = exp.getRetainedStaticContext();
        lifter.gatherInfo(exp);
        lifter.loopLift(exp);
        lifter.root.setRetainedStaticContext(rsc);
        lifter.root.setParentExpression(null);
        if (lifter.changed) {
            ExpressionTool.resetPropertiesWithinSubtree(lifter.root);
            Expression e2 = lifter.root.optimize(visitor, contextInfo);
            e2.setParentExpression(null);
            return e2;
        }
        return lifter.root;
    }

    public LoopLifter(Expression root, Configuration config, boolean streaming) {
        this.root = root;
        this.config = config;
        this.tracing = config.getBooleanProperty(Feature.TRACE_OPTIMIZER_DECISIONS);
        this.streaming = streaming;
    }

    public Expression getRoot() {
        return this.root;
    }

    public void gatherInfo(Expression exp) {
        this.gatherInfo(exp, 0, 0, false);
    }

    private void gatherInfo(Expression exp, int level, int loopLevel, boolean multiThreaded) {
        ExpInfo info = new ExpInfo();
        info.expression = exp;
        info.loopLevel = loopLevel;
        info.multiThreaded = multiThreaded;
        this.expInfoMap.put(exp, info);
        Expression scope = exp.getScopingExpression();
        if (scope != null) {
            this.markDependencies(exp, scope);
        }
        boolean threaded = multiThreaded || exp.isMultiThreaded(this.config);
        Expression choose = this.getContainingConditional(exp);
        if (choose != null) {
            this.markDependencies(exp, choose);
        }
        for (Operand o : exp.operands()) {
            this.gatherInfo(o.getChildExpression(), level + 1, o.isEvaluatedRepeatedly() ? loopLevel + 1 : loopLevel, threaded);
        }
    }

    private Expression getContainingConditional(Expression exp) {
        for (Expression parent = exp.getParentExpression(); parent != null; parent = parent.getParentExpression()) {
            if (parent instanceof ConditionalInstruction) {
                Operand o = ExpressionTool.findOperand(parent, exp);
                if (o == null) {
                    throw new AssertionError();
                }
                if (o.getOperandRole().isInChoiceGroup()) {
                    return parent;
                }
            } else if (parent instanceof TryCatch) {
                return parent;
            }
            exp = parent;
        }
        return null;
    }

    private boolean mayReturnStreamedNodes(Expression exp) {
        return this.streaming && !exp.getItemType().getUType().intersection(UType.ANY_NODE).equals(UType.VOID);
    }

    private void markDependencies(Expression exp, Expression variableSetter) {
        if (variableSetter != null) {
            for (Expression parent = exp; parent != null && parent != variableSetter; parent = parent.getParentExpression()) {
                try {
                    this.expInfoMap.get((Object)parent).dependees.put(variableSetter, true);
                    continue;
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        }
    }

    private void loopLift(Expression exp) {
        ExpInfo info = this.expInfoMap.get(exp);
        if (!info.multiThreaded) {
            if (info.loopLevel > 0 && exp.getNetCost() > 0) {
                if (info.dependees.isEmpty() && exp.isLiftable(this.streaming) && !this.mayReturnStreamedNodes(exp)) {
                    this.root = this.lift(exp, this.root);
                } else {
                    Expression child = exp;
                    ExpInfo expInfo = this.expInfoMap.get(exp);
                    for (Expression parent = exp.getParentExpression(); parent != null; parent = parent.getParentExpression()) {
                        if (expInfo.dependees.get(parent) != null) {
                            ExpInfo childInfo = this.expInfoMap.get(child);
                            if (expInfo.loopLevel == childInfo.loopLevel) break;
                            Operand o = ExpressionTool.findOperand(parent, child);
                            assert (o != null);
                            if (!exp.isLiftable(this.streaming) || child instanceof PseudoExpression || o.getOperandRole().isConstrainedClass()) break;
                            Expression lifted = this.lift(exp, child);
                            o.setChildExpression(lifted);
                            break;
                        }
                        child = parent;
                    }
                }
            }
            for (Operand o : exp.operands()) {
                if (o.getOperandRole().isConstrainedClass()) continue;
                this.loopLift(o.getChildExpression());
            }
        }
    }

    private Expression lift(Expression child, Expression newAction) {
        this.changed = true;
        ExpInfo childInfo = this.expInfoMap.get(child);
        ExpInfo actionInfo = this.expInfoMap.get(newAction);
        int hoist = childInfo.loopLevel - actionInfo.loopLevel;
        Expression oldParent = child.getParentExpression();
        Operand oldOperand = ExpressionTool.findOperand(oldParent, child);
        assert (oldOperand != null);
        LetExpression let = new LetExpression();
        let.setVariableQName(new StructuredQName("vv", "http://saxon.sf.net/generated-variable", "v" + this.sequence++));
        SequenceType type = SequenceType.makeSequenceType(child.getItemType(), child.getCardinality());
        let.setRequiredType(type);
        ExpressionTool.copyLocationInfo(child, let);
        let.setSequence(child);
        let.setNeedsLazyEvaluation(true);
        let.setEvaluationMode(Cardinality.allowsMany(child.getCardinality()) ? EvaluationMode.MAKE_MEMO_CLOSURE : EvaluationMode.MAKE_SINGLETON_CLOSURE);
        let.setAction(newAction);
        let.adoptChildExpression(newAction);
        ExpInfo letInfo = new ExpInfo();
        letInfo.expression = let;
        letInfo.dependees = childInfo.dependees;
        letInfo.dependees.putAll(actionInfo.dependees);
        letInfo.loopLevel = actionInfo.loopLevel;
        this.expInfoMap.put(let, letInfo);
        try {
            ExpressionTool.processExpressionTree(child, null, (expression, result) -> {
                ExpInfo info = this.expInfoMap.get(expression);
                info.loopLevel -= hoist;
                return false;
            });
        } catch (XPathException e) {
            e.printStackTrace();
        }
        LocalVariableReference var = new LocalVariableReference(let);
        int properties = child.getSpecialProperties() & 0x4000000;
        var.setStaticType(type, null, properties);
        var.setInLoop(true);
        let.addReference(var, true);
        ExpressionTool.copyLocationInfo(child, var);
        oldOperand.setChildExpression(var);
        if (this.tracing) {
            Logger err = this.config.getLogger();
            err.info("OPT : At line " + child.getLocation().getLineNumber() + " of " + child.getLocation().getSystemId());
            err.info("OPT : Lifted (" + child.toShortString() + ") above (" + newAction.toShortString() + ") on line " + newAction.getLocation().getLineNumber());
            err.info("OPT : Expression after rewrite: " + let);
        }
        return let;
    }

    private static class ExpInfo {
        Expression expression;
        int loopLevel;
        boolean multiThreaded;
        Map<Expression, Boolean> dependees = new IdentityHashMap<Expression, Boolean>();

        private ExpInfo() {
        }
    }
}


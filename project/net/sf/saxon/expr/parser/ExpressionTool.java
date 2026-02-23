/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.parser;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import javax.xml.transform.SourceLocator;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.SequenceCollector;
import net.sf.saxon.expr.Assignation;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.ContextSwitchingExpression;
import net.sf.saxon.expr.CurrentItemExpression;
import net.sf.saxon.expr.ErrorExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.FunctionCall;
import net.sf.saxon.expr.GlobalVariableReference;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.LocalBinding;
import net.sf.saxon.expr.LocalVariableReference;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.RootExpression;
import net.sf.saxon.expr.SingleItemFilter;
import net.sf.saxon.expr.SlashExpression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.StaticFunctionCall;
import net.sf.saxon.expr.SuppliedParameterReference;
import net.sf.saxon.expr.TailExpression;
import net.sf.saxon.expr.TryCatch;
import net.sf.saxon.expr.UserFunctionCall;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.Clause;
import net.sf.saxon.expr.flwor.FLWORExpression;
import net.sf.saxon.expr.flwor.LocalVariableBinding;
import net.sf.saxon.expr.instruct.ApplyImports;
import net.sf.saxon.expr.instruct.ApplyTemplates;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.CallTemplate;
import net.sf.saxon.expr.instruct.ForEachGroup;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.LocalParam;
import net.sf.saxon.expr.instruct.NextMatch;
import net.sf.saxon.expr.instruct.ParentNodeConstructor;
import net.sf.saxon.expr.instruct.ResultDocument;
import net.sf.saxon.expr.instruct.SimpleNodeConstructor;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.instruct.TraceExpression;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.parser.CodeInjector;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.Evaluator;
import net.sf.saxon.expr.parser.ExpressionAction;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.LoopLifter;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.expr.parser.XPathParser;
import net.sf.saxon.expr.sort.ConditionalSorter;
import net.sf.saxon.expr.sort.DocumentSorter;
import net.sf.saxon.functions.ApplyFn;
import net.sf.saxon.functions.Current;
import net.sf.saxon.functions.CurrentGroup;
import net.sf.saxon.functions.IriToUri;
import net.sf.saxon.functions.RegexGroup;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.StandardLogger;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.query.QueryModule;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ExpressionContext;
import net.sf.saxon.style.ScopedBindingElement;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.NoDynamicContextException;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.Closure;
import net.sf.saxon.value.ExternalObject;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class ExpressionTool {
    private ExpressionTool() {
    }

    public static Expression make(String expression, StaticContext env, int start, int terminator, CodeInjector codeInjector) throws XPathException {
        int languageLevel = env.getXPathVersion();
        XPathParser parser = env.getConfiguration().newExpressionParser("XP", false, languageLevel);
        if (codeInjector != null) {
            parser.setCodeInjector(codeInjector);
        }
        if (terminator == -1) {
            terminator = 0;
        }
        Expression exp = parser.parse(expression, start, terminator, env);
        ExpressionTool.setDeepRetainedStaticContext(exp, env.makeRetainedStaticContext());
        exp = exp.simplify();
        return exp;
    }

    public static void setDeepRetainedStaticContext(Expression exp, RetainedStaticContext rsc) {
        if (exp.getLocalRetainedStaticContext() == null) {
            exp.setRetainedStaticContextLocally(rsc);
        } else {
            rsc = exp.getLocalRetainedStaticContext();
        }
        for (Operand o : exp.operands()) {
            ExpressionTool.setDeepRetainedStaticContext(o.getChildExpression(), rsc);
        }
    }

    public static void copyLocationInfo(Expression from, Expression to) {
        if (from != null && to != null) {
            if (to.getLocation() == null || to.getLocation() == Loc.NONE) {
                to.setLocation(from.getLocation());
            }
            if (to.getLocalRetainedStaticContext() == null) {
                to.setRetainedStaticContextLocally(from.getLocalRetainedStaticContext());
            }
        }
    }

    public static Expression unsortedIfHomogeneous(Expression exp, boolean forStreaming) throws XPathException {
        if (exp instanceof Literal) {
            return exp;
        }
        if (exp.getItemType() instanceof AnyItemType) {
            return exp;
        }
        return exp.unordered(false, forStreaming);
    }

    public static Expression injectCode(Expression exp, CodeInjector injector) {
        if (exp instanceof FLWORExpression) {
            ((FLWORExpression)exp).injectCode(injector);
        } else if (!(exp instanceof TraceExpression)) {
            for (Operand o : exp.operands()) {
                o.setChildExpression(ExpressionTool.injectCode(o.getChildExpression(), injector));
            }
        }
        return injector.inject(exp);
    }

    public static Evaluator lazyEvaluator(Expression exp, boolean repeatable) {
        if (exp instanceof Literal) {
            return Evaluator.LITERAL;
        }
        if (exp instanceof VariableReference) {
            return Evaluator.VARIABLE;
        }
        if (exp instanceof SuppliedParameterReference) {
            return Evaluator.SUPPLIED_PARAMETER;
        }
        if ((exp.getDependencies() & 0x6D) != 0) {
            return ExpressionTool.eagerEvaluator(exp);
        }
        if (exp instanceof ErrorExpression) {
            return Evaluator.SINGLE_ITEM;
        }
        if (!Cardinality.allowsMany(exp.getCardinality())) {
            return ExpressionTool.eagerEvaluator(exp);
        }
        if (exp instanceof TailExpression) {
            TailExpression tail = (TailExpression)exp;
            Expression base = tail.getBaseExpression();
            if (base instanceof VariableReference) {
                return Evaluator.LAZY_TAIL;
            }
            if (repeatable) {
                return Evaluator.MEMO_CLOSURE;
            }
            return Evaluator.LAZY_SEQUENCE;
        }
        if (exp instanceof Block && ((Block)exp).isCandidateForSharedAppend()) {
            return Evaluator.SHARED_APPEND;
        }
        if (repeatable) {
            return Evaluator.MEMO_CLOSURE;
        }
        return Evaluator.LAZY_SEQUENCE;
    }

    public static Evaluator eagerEvaluator(Expression exp) {
        if (exp instanceof Literal && !(((Literal)exp).getValue() instanceof Closure)) {
            return Evaluator.LITERAL;
        }
        if (exp instanceof VariableReference) {
            return Evaluator.VARIABLE;
        }
        int m = exp.getImplementationMethod();
        if ((m & 1) != 0 && !Cardinality.allowsMany(exp.getCardinality())) {
            if (Cardinality.allowsZero(exp.getCardinality())) {
                return Evaluator.OPTIONAL_ITEM;
            }
            return Evaluator.SINGLE_ITEM;
        }
        if ((m & 2) != 0) {
            return Evaluator.EAGER_SEQUENCE;
        }
        return Evaluator.PROCESS;
    }

    public static Sequence lazyEvaluate(Expression exp, XPathContext context, boolean repeatable) throws XPathException {
        Evaluator evaluator = ExpressionTool.lazyEvaluator(exp, repeatable);
        return evaluator.evaluate(exp, context);
    }

    public static GroundedValue eagerEvaluate(Expression exp, XPathContext context) throws XPathException {
        Evaluator evaluator = ExpressionTool.eagerEvaluator(exp);
        return evaluator.evaluate(exp, context).materialize();
    }

    public static int markTailFunctionCalls(Expression exp, StructuredQName qName, int arity) {
        return exp.markTailFunctionCalls(qName, arity);
    }

    public static String indent(int level) {
        FastStringBuffer fsb = new FastStringBuffer(level);
        for (int i = 0; i < level; ++i) {
            fsb.append("  ");
        }
        return fsb.toString();
    }

    public static boolean contains(Expression a, Expression b) {
        for (Expression temp = b; temp != null; temp = temp.getParentExpression()) {
            if (temp != a) continue;
            return true;
        }
        return false;
    }

    public static boolean containsLocalParam(Expression exp) {
        return ExpressionTool.contains(exp, true, e -> e instanceof LocalParam);
    }

    public static boolean containsLocalVariableReference(Expression exp) {
        return ExpressionTool.contains(exp, false, e -> {
            if (e instanceof LocalVariableReference) {
                LocalVariableReference vref = (LocalVariableReference)e;
                LocalBinding binding = vref.getBinding();
                return !(binding instanceof Expression) || !ExpressionTool.contains(exp, (Expression)((Object)binding));
            }
            return false;
        });
    }

    public static boolean contains(Expression exp, boolean sameFocusOnly, Predicate<Expression> predicate) {
        if (predicate.test(exp)) {
            return true;
        }
        for (Operand info : exp.operands()) {
            if (!info.hasSameFocus() && sameFocusOnly || !ExpressionTool.contains(info.getChildExpression(), sameFocusOnly, predicate)) continue;
            return true;
        }
        return false;
    }

    public static boolean changesXsltContext(Expression exp) {
        if ((exp = exp.getInterpretedExpression()) instanceof ResultDocument || exp instanceof CallTemplate || exp instanceof ApplyTemplates || exp instanceof NextMatch || exp instanceof ApplyImports || exp.isCallOn(RegexGroup.class) || exp.isCallOn(CurrentGroup.class)) {
            return true;
        }
        for (Operand o : exp.operands()) {
            if (!ExpressionTool.changesXsltContext(o.getChildExpression())) continue;
            return true;
        }
        return false;
    }

    public static boolean isLoopingSubexpression(Expression child, Expression ancestor) {
        Expression parent;
        while ((parent = child.getParentExpression()) != null) {
            if (ExpressionTool.hasLoopingSubexpression(parent, child)) {
                return true;
            }
            if (parent == ancestor) {
                return false;
            }
            child = parent;
        }
        return false;
    }

    public static boolean isLoopingReference(VariableReference reference, Binding binding) {
        Expression child = reference;
        Expression parent = child.getParentExpression();
        while (parent != null) {
            if (parent instanceof FLWORExpression) {
                if (parent.hasVariableBinding(binding)) {
                    return ((FLWORExpression)parent).hasLoopingVariableReference(binding);
                }
                if (ExpressionTool.hasLoopingSubexpression(parent, child)) {
                    return true;
                }
            } else {
                if (parent.getExpressionName().equals("tryCatch")) {
                    return true;
                }
                if (parent instanceof ForEachGroup && parent.hasVariableBinding(binding)) {
                    return false;
                }
                if (ExpressionTool.hasLoopingSubexpression(parent, child)) {
                    return true;
                }
                if (parent.hasVariableBinding(binding)) {
                    return false;
                }
            }
            child = parent;
            parent = child.getParentExpression();
        }
        return true;
    }

    public static boolean hasLoopingSubexpression(Expression parent, Expression child) {
        for (Operand info : parent.operands()) {
            if (info.getChildExpression() != child) continue;
            return info.isEvaluatedRepeatedly();
        }
        return false;
    }

    public static Expression getFocusSettingContainer(Expression exp) {
        Expression child = exp;
        Expression parent = child.getParentExpression();
        while (parent != null) {
            Operand o = ExpressionTool.findOperand(parent, child);
            if (o == null) {
                throw new AssertionError();
            }
            if (!o.hasSameFocus()) {
                return parent;
            }
            child = parent;
            parent = child.getParentExpression();
        }
        return null;
    }

    public static Expression getContextDocumentSettingContainer(Expression exp) {
        Expression child = exp;
        Expression parent = child.getParentExpression();
        while (parent != null) {
            ContextSwitchingExpression switcher;
            if (parent instanceof ContextSwitchingExpression && child == (switcher = (ContextSwitchingExpression)((Object)parent)).getActionExpression() && switcher.getSelectExpression().hasSpecialProperty(65536)) {
                parent.resetLocalStaticProperties();
                parent.getSpecialProperties();
                return ExpressionTool.getContextDocumentSettingContainer(parent);
            }
            Operand o = ExpressionTool.findOperand(parent, child);
            if (o == null) {
                throw new AssertionError();
            }
            if (!o.hasSameFocus()) {
                return parent;
            }
            child = parent;
            parent = child.getParentExpression();
        }
        return null;
    }

    public static void resetStaticProperties(Expression exp) {
        int i = 0;
        while (exp != null) {
            exp.resetLocalStaticProperties();
            exp = exp.getParentExpression();
            if (i++ <= 100000) continue;
            throw new IllegalStateException("Loop in parent expression chain");
        }
    }

    public static boolean equalOrNull(Object x, Object y) {
        if (x == null) {
            return y == null;
        }
        return x.equals(y);
    }

    public static SequenceIterator getIteratorFromProcessMethod(Expression exp, XPathContext context) throws XPathException {
        Controller controller = context.getController();
        assert (controller != null);
        SequenceCollector seq = controller.allocateSequenceOutputter();
        exp.process(new ComplexContentOutputter(seq), context);
        seq.close();
        return seq.iterate();
    }

    public static Item getItemFromProcessMethod(Expression exp, XPathContext context) throws XPathException {
        Controller controller = context.getController();
        if (controller == null) {
            throw new NoDynamicContextException("No controller available");
        }
        SequenceCollector seq = controller.allocateSequenceOutputter(1);
        exp.process(new ComplexContentOutputter(seq), context);
        seq.close();
        Item result = seq.getFirstItem();
        seq.reset();
        return result;
    }

    public static int allocateSlots(Expression exp, int nextFree, SlotManager frame) {
        if (exp instanceof Assignation) {
            ((Assignation)exp).setSlotNumber(nextFree);
            int count = ((Assignation)exp).getRequiredSlots();
            nextFree += count;
            if (frame != null) {
                frame.allocateSlotNumber(((Assignation)exp).getVariableQName());
            }
        }
        if (exp instanceof LocalParam && ((LocalParam)exp).getSlotNumber() < 0) {
            ((LocalParam)exp).setSlotNumber(nextFree++);
        }
        if (exp instanceof FLWORExpression) {
            for (Clause c : ((FLWORExpression)exp).getClauseList()) {
                for (LocalVariableBinding b : c.getRangeVariables()) {
                    b.setSlotNumber(nextFree++);
                    frame.allocateSlotNumber(b.getVariableQName());
                }
            }
        }
        if (exp instanceof VariableReference) {
            VariableReference var = (VariableReference)exp;
            Binding binding = var.getBinding();
            if (exp instanceof LocalVariableReference) {
                ((LocalVariableReference)var).setSlotNumber(((LocalBinding)binding).getLocalSlotNumber());
            }
            if (binding instanceof Assignation && ((LocalBinding)binding).getLocalSlotNumber() < 0) {
                Logger err;
                Assignation decl = (Assignation)binding;
                try {
                    err = exp.getConfiguration().getLogger();
                } catch (Exception ex) {
                    err = new StandardLogger();
                }
                String msg = "*** Internal Saxon error: local variable encountered whose binding has been deleted";
                err.error(msg);
                err.error("Variable name: " + decl.getVariableName());
                err.error("Line number of reference: " + var.getLocation().getLineNumber() + " in " + var.getLocation().getSystemId());
                err.error("Line number of declaration: " + decl.getLocation().getLineNumber() + " in " + decl.getLocation().getSystemId());
                err.error("DECLARATION:");
                try {
                    decl.explain(err);
                } catch (Exception exception) {
                    // empty catch block
                }
                throw new IllegalStateException(msg);
            }
        }
        if (exp instanceof Pattern) {
            nextFree = ((Pattern)exp).allocateSlots(frame, nextFree);
        } else if (exp instanceof ScopedBindingElement) {
            nextFree = ((ScopedBindingElement)((Object)exp)).allocateSlots(frame, nextFree);
        } else {
            for (Operand o : exp.operands()) {
                nextFree = ExpressionTool.allocateSlots(o.getChildExpression(), nextFree, frame);
            }
        }
        return nextFree;
    }

    public static boolean effectiveBooleanValue(SequenceIterator iterator) throws XPathException {
        Item first = iterator.next();
        if (first == null) {
            return false;
        }
        if (first instanceof NodeInfo) {
            iterator.close();
            return true;
        }
        if (first instanceof AtomicValue) {
            if (first instanceof BooleanValue) {
                if (iterator.next() != null) {
                    iterator.close();
                    ExpressionTool.ebvError("a sequence of two or more items starting with a boolean");
                }
                iterator.close();
                return ((BooleanValue)first).getBooleanValue();
            }
            if (first instanceof StringValue) {
                if (iterator.next() != null) {
                    iterator.close();
                    ExpressionTool.ebvError("a sequence of two or more items starting with a string");
                }
                return !((StringValue)first).isZeroLength();
            }
            if (first instanceof NumericValue) {
                NumericValue n;
                if (iterator.next() != null) {
                    iterator.close();
                    ExpressionTool.ebvError("a sequence of two or more items starting with a numeric value");
                }
                return (n = (NumericValue)first).compareTo(0L) != 0 && !n.isNaN();
            }
            iterator.close();
            ExpressionTool.ebvError("a sequence starting with an atomic value of type " + ((AtomicValue)first).getItemType().getTypeName().getDisplayName());
            return false;
        }
        if (first instanceof Function) {
            iterator.close();
            if (first instanceof ArrayItem) {
                ExpressionTool.ebvError("a sequence starting with an array item (" + first.toShortString() + ")");
                return false;
            }
            if (first instanceof MapItem) {
                ExpressionTool.ebvError("a sequence starting with a map (" + first.toShortString() + ")");
                return false;
            }
            ExpressionTool.ebvError("a sequence starting with a function (" + first.toShortString() + ")");
            return false;
        }
        if (first instanceof ObjectValue) {
            if (iterator.next() != null) {
                iterator.close();
                ExpressionTool.ebvError("a sequence of two or more items starting with an external object value");
            }
            return true;
        }
        ExpressionTool.ebvError("a sequence starting with an item of unknown kind");
        return false;
    }

    public static boolean effectiveBooleanValue(Item item) throws XPathException {
        if (item == null) {
            return false;
        }
        if (item instanceof NodeInfo) {
            return true;
        }
        if (item instanceof AtomicValue) {
            if (item instanceof BooleanValue) {
                return ((BooleanValue)item).getBooleanValue();
            }
            if (item instanceof StringValue) {
                return !((StringValue)item).isZeroLength();
            }
            if (item instanceof NumericValue) {
                NumericValue n = (NumericValue)item;
                return n.compareTo(0L) != 0 && !n.isNaN();
            }
            if (item instanceof ExternalObject) {
                return true;
            }
            ExpressionTool.ebvError("an atomic value of type " + ((AtomicValue)item).getPrimitiveType().getDisplayName());
            return false;
        }
        ExpressionTool.ebvError(item.getGenre().toString());
        return false;
    }

    public static void ebvError(String reason) throws XPathException {
        XPathException err = new XPathException("Effective boolean value is not defined for " + reason);
        err.setErrorCode("FORG0006");
        err.setIsTypeError(true);
        throw err;
    }

    public static void ebvError(String reason, Expression cause) throws XPathException {
        XPathException err = new XPathException("Effective boolean value is not defined for " + reason);
        err.setErrorCode("FORG0006");
        err.setIsTypeError(true);
        err.setFailingExpression(cause);
        throw err;
    }

    public static boolean dependsOnFocus(Expression exp) {
        return (exp.getDependencies() & 0x1E) != 0;
    }

    public static boolean dependsOnVariable(Expression exp, Binding[] bindingList) {
        return bindingList != null && bindingList.length != 0 && ExpressionTool.contains(exp, false, e -> {
            if (e instanceof VariableReference) {
                for (Binding binding : bindingList) {
                    if (((VariableReference)e).getBinding() != binding) continue;
                    return true;
                }
            }
            return false;
        });
    }

    public static void gatherReferencedVariables(Expression e, List<Binding> list) {
        if (e instanceof VariableReference) {
            Binding binding = ((VariableReference)e).getBinding();
            if (!list.contains(binding)) {
                list.add(binding);
            }
        } else {
            for (Operand o : e.operands()) {
                if (o.getOperandRole().isInChoiceGroup()) continue;
                ExpressionTool.gatherReferencedVariables(o.getChildExpression(), list);
            }
        }
    }

    public static boolean refersToVariableOrFunction(Expression exp) {
        return ExpressionTool.contains(exp, false, e -> e instanceof VariableReference || e instanceof UserFunctionCall || e instanceof Binding || e instanceof CallTemplate || e instanceof ApplyTemplates || e instanceof ApplyImports || ExpressionTool.isCallOnSystemFunction(e, "function-lookup") || e.isCallOn(ApplyFn.class));
    }

    public static boolean isCallOnSystemFunction(Expression e, String localName) {
        return e instanceof StaticFunctionCall && localName.equals(((StaticFunctionCall)e).getFunctionName().getLocalPart());
    }

    public static boolean callsFunction(Expression exp, StructuredQName qName, boolean sameFocusOnly) {
        return ExpressionTool.contains(exp, sameFocusOnly, e -> e instanceof FunctionCall && qName.equals(((FunctionCall)e).getFunctionName()));
    }

    public static boolean containsSubexpression(Expression exp, Class<? extends Expression> subClass) {
        return ExpressionTool.contains(exp, false, e -> subClass.isAssignableFrom(e.getClass()));
    }

    public static void gatherCalledFunctions(Expression e, List<UserFunction> list) {
        if (e instanceof UserFunctionCall) {
            UserFunction function = ((UserFunctionCall)e).getFunction();
            if (!list.contains(function)) {
                list.add(function);
            }
        } else {
            for (Operand o : e.operands()) {
                ExpressionTool.gatherCalledFunctions(o.getChildExpression(), list);
            }
        }
    }

    public static void gatherCalledFunctionNames(Expression e, List<SymbolicName> list) {
        if (e instanceof UserFunctionCall) {
            list.add(((UserFunctionCall)e).getSymbolicName());
        } else {
            for (Operand o : e.operands()) {
                ExpressionTool.gatherCalledFunctionNames(o.getChildExpression(), list);
            }
        }
    }

    public static Expression optimizeComponentBody(Expression body, Compilation compilation, ExpressionVisitor visitor, ContextItemStaticInfo cisi, boolean extractGlobals) throws XPathException {
        Configuration config = visitor.getConfiguration();
        Optimizer opt = visitor.obtainOptimizer();
        StaticContext env = visitor.getStaticContext();
        boolean compileWithTracing = config.isCompileWithTracing();
        if (!compileWithTracing) {
            if (compilation != null) {
                compileWithTracing = compilation.getCompilerInfo().isCompileWithTracing();
            } else if (env instanceof QueryModule) {
                compileWithTracing = ((QueryModule)env).getUserQueryContext().isCompileWithTracing();
            } else if (env instanceof ExpressionContext) {
                compileWithTracing = ((ExpressionContext)env).getStyleElement().getCompilation().getCompilerInfo().isCompileWithTracing();
            }
        }
        if (opt.isOptionSet(256) && !compileWithTracing) {
            Expression exp2;
            ExpressionTool.resetPropertiesWithinSubtree(body);
            if (opt.isOptionSet(256)) {
                body = body.optimize(visitor, cisi);
            }
            body.setParentExpression(null);
            if (extractGlobals && compilation != null && (exp2 = opt.promoteExpressionsToGlobal(body, compilation.getPrincipalStylesheetModule(), visitor)) != null) {
                ExpressionTool.resetPropertiesWithinSubtree(exp2);
                body = exp2.optimize(visitor, cisi);
            }
            if (opt.isOptionSet(1)) {
                body = LoopLifter.process(body, visitor, cisi);
            }
        } else {
            body = ExpressionTool.avoidDocumentSort(body);
        }
        if (!visitor.isOptimizeForStreaming()) {
            body = opt.eliminateCommonSubexpressions(body);
        }
        opt.injectByteCodeCandidates(body);
        opt.prepareForStreaming(body);
        ExpressionTool.computeEvaluationModesForUserFunctionCalls(body);
        body.restoreParentPointers();
        return body;
    }

    private static Expression avoidDocumentSort(Expression exp) {
        DocumentSorter sorter;
        Expression eliminatedSorter;
        if (exp instanceof DocumentSorter) {
            Expression base = ((DocumentSorter)exp).getBaseExpression();
            if (base.hasSpecialProperty(131072)) {
                return base;
            }
            return exp;
        }
        if (exp instanceof ConditionalSorter && (eliminatedSorter = ExpressionTool.avoidDocumentSort(sorter = ((ConditionalSorter)exp).getDocumentSorter())) != sorter) {
            return eliminatedSorter;
        }
        for (Operand o : exp.operands()) {
            o.setChildExpression(ExpressionTool.avoidDocumentSort(o.getChildExpression()));
        }
        return exp;
    }

    public static void computeEvaluationModesForUserFunctionCalls(Expression exp) throws XPathException {
        ExpressionTool.processExpressionTree(exp, null, (expression, result) -> {
            if (expression instanceof UserFunctionCall) {
                ((UserFunctionCall)expression).allocateArgumentEvaluators();
            }
            if (expression instanceof LocalParam) {
                ((LocalParam)expression).computeEvaluationMode();
            }
            return false;
        });
    }

    public static void clearStreamabilityData(Expression exp) throws XPathException {
        ExpressionTool.processExpressionTree(exp, null, (expression, result) -> {
            expression.setExtraProperty("P+S", null);
            expression.setExtraProperty("inversion", null);
            return false;
        });
    }

    public static void resetPropertiesWithinSubtree(Expression exp) {
        LocalVariableReference ref;
        LocalBinding binding;
        exp.resetLocalStaticProperties();
        if (exp instanceof LocalVariableReference && (binding = (ref = (LocalVariableReference)exp).getBinding()) instanceof Assignation) {
            binding.addReference(ref, ref.isInLoop());
        }
        for (Operand o : exp.operands()) {
            ExpressionTool.resetPropertiesWithinSubtree(o.getChildExpression());
            o.getChildExpression().setParentExpression(exp);
        }
    }

    public static Expression resolveCallsToCurrentFunction(Expression exp) {
        if (exp.isCallOn(Current.class)) {
            ContextItemExpression cie = new ContextItemExpression();
            ExpressionTool.copyLocationInfo(exp, cie);
            return cie;
        }
        if (ExpressionTool.callsFunction(exp, Current.FN_CURRENT, true)) {
            ExpressionTool.replaceTrivialCallsToCurrent(exp);
        }
        if (ExpressionTool.callsFunction(exp, Current.FN_CURRENT, false)) {
            LetExpression let = new LetExpression();
            let.setVariableQName(new StructuredQName("vv", "http://saxon.sf.net/generated-variable", "current" + exp.hashCode()));
            let.setRequiredType(SequenceType.SINGLE_ITEM);
            let.setSequence(new CurrentItemExpression());
            ExpressionTool.replaceCallsToCurrent(exp, let);
            let.setAction(exp);
            return let;
        }
        return exp;
    }

    public static void gatherVariableReferences(Expression exp, Binding binding, List<VariableReference> list) {
        if (exp instanceof VariableReference && ((VariableReference)exp).getBinding() == binding) {
            list.add((VariableReference)exp);
        } else {
            for (Operand o : exp.operands()) {
                ExpressionTool.gatherVariableReferences(o.getChildExpression(), binding, list);
            }
        }
    }

    public static boolean processExpressionTree(Expression root, Object result, ExpressionAction action) throws XPathException {
        boolean done = action.process(root, result);
        if (!done) {
            for (Operand o : root.operands()) {
                done = ExpressionTool.processExpressionTree(o.getChildExpression(), result, action);
                if (!done) continue;
                return true;
            }
        }
        return false;
    }

    public static boolean replaceSelectedSubexpressions(Expression exp, ExpressionSelector selector, Expression replacement, boolean mustCopy) {
        boolean replaced = false;
        for (Operand o : exp.operands()) {
            Expression child;
            if (replaced) {
                mustCopy = true;
            }
            if (selector.matches(child = o.getChildExpression())) {
                Expression e2 = mustCopy ? replacement.copy(new RebindingMap()) : replacement;
                o.setChildExpression(e2);
                replaced = true;
                continue;
            }
            replaced = ExpressionTool.replaceSelectedSubexpressions(child, selector, replacement, mustCopy);
        }
        return replaced;
    }

    public static void replaceVariableReferences(Expression exp, Binding binding, Expression replacement, boolean mustCopy) {
        ExpressionSelector selector = child -> child instanceof VariableReference && ((VariableReference)child).getBinding() == binding;
        ExpressionTool.replaceSelectedSubexpressions(exp, selector, replacement, mustCopy);
    }

    public static int getReferenceCount(Expression exp, Binding binding, boolean inLoop) {
        int rcount = 0;
        if (exp instanceof VariableReference && ((VariableReference)exp).getBinding() == binding) {
            if (((VariableReference)exp).isFiltered()) {
                return 10000;
            }
            rcount += inLoop ? 10 : 1;
        } else {
            boolean childLoop;
            Operand info;
            Expression child;
            if ((exp.getDependencies() & 0x80) == 0) {
                return 0;
            }
            Iterator<Operand> iterator = exp.operands().iterator();
            while (iterator.hasNext() && (rcount += ExpressionTool.getReferenceCount(child = (info = iterator.next()).getChildExpression(), binding, childLoop = inLoop || info.isEvaluatedRepeatedly())) < 10000) {
            }
        }
        return rcount;
    }

    public static int expressionSize(Expression exp) {
        exp = exp.getInterpretedExpression();
        int total = 1;
        for (Operand o : exp.operands()) {
            total += ExpressionTool.expressionSize(o.getChildExpression());
        }
        return total;
    }

    public static void rebindVariableReferences(Expression exp, Binding oldBinding, Binding newBinding) {
        if (exp instanceof VariableReference) {
            if (((VariableReference)exp).getBinding() == oldBinding) {
                ((VariableReference)exp).fixup(newBinding);
            }
        } else {
            for (Operand o : exp.operands()) {
                ExpressionTool.rebindVariableReferences(o.getChildExpression(), oldBinding, newBinding);
            }
        }
    }

    public static Expression makePathExpression(Expression start, Expression step) {
        SlashExpression stepPath;
        if (start instanceof RootExpression && step instanceof AxisExpression && ((AxisExpression)step).getAxis() == 9) {
            return Literal.makeEmptySequence();
        }
        SlashExpression expr = new SlashExpression(start, step);
        if (step instanceof SlashExpression && ExpressionTool.isFilteredAxisPath((stepPath = (SlashExpression)step).getSelectExpression()) && ExpressionTool.isFilteredAxisPath(stepPath.getActionExpression())) {
            expr.setStart(ExpressionTool.makePathExpression(start, stepPath.getSelectExpression()));
            expr.setStep(stepPath.getActionExpression());
        }
        return expr;
    }

    public static Operand findOperand(Expression parentExpression, Expression childExpression) {
        for (Operand o : parentExpression.operands()) {
            if (o.getChildExpression() != childExpression) continue;
            return o;
        }
        return null;
    }

    private static boolean isFilteredAxisPath(Expression exp) {
        return ExpressionTool.unfilteredExpression(exp, true) instanceof AxisExpression;
    }

    public static Expression unfilteredExpression(Expression exp, boolean allowPositional) {
        if (exp instanceof FilterExpression && (allowPositional || !((FilterExpression)exp).isFilterIsPositional())) {
            return ExpressionTool.unfilteredExpression(((FilterExpression)exp).getSelectExpression(), allowPositional);
        }
        if (exp instanceof SingleItemFilter && allowPositional) {
            return ExpressionTool.unfilteredExpression(((SingleItemFilter)exp).getBaseExpression(), allowPositional);
        }
        return exp;
    }

    public static Expression tryToFactorOutDot(Expression exp, ItemType contextItemType) {
        if (exp instanceof ContextItemExpression) {
            return null;
        }
        if (exp instanceof LetExpression && ((LetExpression)exp).getSequence() instanceof ContextItemExpression) {
            Expression action = ((LetExpression)exp).getAction();
            boolean changed = ExpressionTool.factorOutDot(action, (LetExpression)exp);
            if (changed) {
                exp.resetLocalStaticProperties();
            }
            return exp;
        }
        if ((exp.getDependencies() & 0x12) != 0) {
            LetExpression let = new LetExpression();
            let.setVariableQName(new StructuredQName("saxon", "http://saxon.sf.net/", "dot" + exp.hashCode()));
            let.setRequiredType(SequenceType.makeSequenceType(contextItemType, 16384));
            let.setSequence(new ContextItemExpression());
            let.setAction(exp);
            boolean changed = ExpressionTool.factorOutDot(exp, let);
            if (changed) {
                return let;
            }
            return exp;
        }
        return null;
    }

    public static boolean factorOutDot(Expression exp, Binding variable) {
        boolean changed = false;
        if ((exp.getDependencies() & 0x12) != 0) {
            for (Operand info : exp.operands()) {
                VariableReference ref;
                if (!info.hasSameFocus()) continue;
                Expression child = info.getChildExpression();
                if (child instanceof ContextItemExpression) {
                    ref = variable.isGlobal() ? new GlobalVariableReference((GlobalVariable)variable) : new LocalVariableReference((LocalBinding)variable);
                    ExpressionTool.copyLocationInfo(child, ref);
                    info.setChildExpression(ref);
                    changed = true;
                    continue;
                }
                if (child instanceof AxisExpression || child instanceof RootExpression) {
                    ref = variable.isGlobal() ? new GlobalVariableReference((GlobalVariable)variable) : new LocalVariableReference((LocalBinding)variable);
                    ExpressionTool.copyLocationInfo(child, ref);
                    Expression path = ExpressionTool.makePathExpression(ref, child);
                    info.setChildExpression(path);
                    changed = true;
                    continue;
                }
                changed |= ExpressionTool.factorOutDot(child, variable);
            }
        }
        if (changed) {
            exp.resetLocalStaticProperties();
        }
        return changed;
    }

    public static boolean inlineVariableReferences(Expression expr, Binding binding, Expression replacement) {
        return ExpressionTool.inlineVariableReferencesInternal(expr, binding, replacement);
    }

    public static boolean inlineVariableReferencesInternal(Expression expr, Binding binding, Expression replacement) {
        if (expr instanceof TryCatch && !(replacement instanceof Literal)) {
            return false;
        }
        boolean found = false;
        for (Operand o : expr.operands()) {
            Expression child = o.getChildExpression();
            if (child instanceof VariableReference && ((VariableReference)child).getBinding() == binding) {
                Expression copy;
                try {
                    copy = replacement.copy(new RebindingMap());
                    ExpressionTool.copyLocationInfo(child, copy);
                } catch (UnsupportedOperationException err) {
                    copy = replacement;
                }
                o.setChildExpression(copy);
                found = true;
                continue;
            }
            found |= ExpressionTool.inlineVariableReferencesInternal(child, binding, replacement);
        }
        if (found) {
            expr.resetLocalStaticProperties();
        }
        return found;
    }

    public static boolean replaceTrivialCallsToCurrent(Expression expr) {
        boolean found = false;
        for (Operand o : expr.operands()) {
            if (!o.hasSameFocus()) continue;
            Expression child = o.getChildExpression();
            if (child.isCallOn(Current.class)) {
                CurrentItemExpression var = new CurrentItemExpression();
                ExpressionTool.copyLocationInfo(child, var);
                o.setChildExpression(var);
                found = true;
                continue;
            }
            found = ExpressionTool.replaceTrivialCallsToCurrent(child);
        }
        if (found) {
            expr.resetLocalStaticProperties();
        }
        return found;
    }

    public static boolean replaceCallsToCurrent(Expression expr, LocalBinding binding) {
        boolean found = false;
        for (Operand o : expr.operands()) {
            Expression child = o.getChildExpression();
            if (child.isCallOn(Current.class)) {
                LocalVariableReference var = new LocalVariableReference(binding);
                ExpressionTool.copyLocationInfo(child, var);
                o.setChildExpression(var);
                binding.addReference(var, true);
                found = true;
                continue;
            }
            found = ExpressionTool.replaceCallsToCurrent(child, binding);
        }
        if (found) {
            expr.resetLocalStaticProperties();
        }
        return found;
    }

    public static boolean isNotAllowedInUpdatingContext(Expression exp) {
        return !exp.isUpdatingExpression() && !exp.isVacuousExpression();
    }

    public static String getCurrentDirectory() {
        String dir;
        try {
            dir = System.getProperty("user.dir");
        } catch (Exception geterr) {
            return null;
        }
        if (!dir.endsWith("/")) {
            dir = dir + '/';
        }
        URI currentDirectoryURL = new File(dir).toURI();
        return currentDirectoryURL.toString();
    }

    public static URI getBaseURI(StaticContext env, SourceLocator locator, boolean fail) throws XPathException {
        URI expressionBaseURI;
        block6: {
            expressionBaseURI = null;
            String base = null;
            try {
                base = env.getStaticBaseURI();
                if (base == null) {
                    base = ExpressionTool.getCurrentDirectory();
                }
                if (base != null) {
                    expressionBaseURI = new URI(base);
                }
            } catch (URISyntaxException e) {
                String esc = IriToUri.iriToUri(base).toString();
                try {
                    expressionBaseURI = new URI(esc);
                } catch (URISyntaxException e2) {
                    expressionBaseURI = null;
                }
                if (expressionBaseURI != null || !fail) break block6;
                XPathException err = new XPathException("The base URI " + Err.wrap(env.getStaticBaseURI(), 7) + " is not a valid URI");
                err.setLocator(locator);
                throw err;
            }
        }
        return expressionBaseURI;
    }

    public static String parenthesize(Expression exp) {
        if (exp.operands().iterator().hasNext()) {
            return "(" + exp.toString() + ")";
        }
        return exp.toString();
    }

    public static String parenthesizeShort(Expression exp) {
        if (ExpressionTool.hasTwoOrMoreOperands(exp)) {
            return "(" + exp.toShortString() + ")";
        }
        return exp.toShortString();
    }

    private static boolean hasTwoOrMoreOperands(Expression exp) {
        Iterator<Operand> ops = exp.operands().iterator();
        if (!ops.hasNext()) {
            return false;
        }
        ops.next();
        return ops.hasNext();
    }

    public static void validateTree(Expression exp) {
        try {
            for (Operand o : exp.checkedOperands()) {
                ExpressionTool.validateTree(o.getChildExpression());
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public static boolean isLocalConstructor(Expression child) {
        if (!(child instanceof ParentNodeConstructor) && !(child instanceof SimpleNodeConstructor)) {
            return false;
        }
        for (Expression parent = child.getParentExpression(); parent != null; parent = parent.getParentExpression()) {
            if (parent instanceof ParentNodeConstructor) {
                return true;
            }
            Operand o = ExpressionTool.findOperand(parent, child);
            if (o.getUsage() != OperandUsage.TRANSMISSION) {
                return false;
            }
            child = parent;
        }
        return false;
    }

    public static interface ExpressionSelector {
        public boolean matches(Expression var1);
    }
}


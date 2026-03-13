/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.parser;

import java.util.ArrayList;
import net.sf.saxon.Controller;
import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.SequenceCollector;
import net.sf.saxon.expr.EvaluationMode;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.SuppliedParameterReference;
import net.sf.saxon.expr.TailExpression;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.om.Chain;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.LazySequence;
import net.sf.saxon.om.MemoSequence;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerRange;
import net.sf.saxon.value.MemoClosure;
import net.sf.saxon.value.SingletonClosure;

public abstract class Evaluator {
    public static final Evaluator EMPTY_SEQUENCE = new Evaluator(){

        @Override
        public Sequence evaluate(Expression expr, XPathContext context) {
            return EmptySequence.getInstance();
        }

        @Override
        public EvaluationMode getEvaluationMode() {
            return EvaluationMode.RETURN_EMPTY_SEQUENCE;
        }
    };
    public static final Evaluator LITERAL = new Evaluator(){

        @Override
        public Sequence evaluate(Expression expr, XPathContext context) {
            return ((Literal)expr).getValue();
        }

        @Override
        public EvaluationMode getEvaluationMode() {
            return EvaluationMode.EVALUATE_LITERAL;
        }
    };
    public static final Evaluator VARIABLE = new Evaluator(){

        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            try {
                return ((VariableReference)expr).evaluateVariable(context);
            } catch (ClassCastException e) {
                assert (false);
                return LAZY_SEQUENCE.evaluate(expr, context);
            }
        }

        @Override
        public EvaluationMode getEvaluationMode() {
            return EvaluationMode.EVALUATE_AND_MATERIALIZE_VARIABLE;
        }
    };
    public static final Evaluator SUPPLIED_PARAMETER = new Evaluator(){

        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            try {
                return ((SuppliedParameterReference)expr).evaluateVariable(context);
            } catch (ClassCastException e) {
                assert (false);
                return LAZY_SEQUENCE.evaluate(expr, context);
            }
        }

        @Override
        public EvaluationMode getEvaluationMode() {
            return EvaluationMode.EVALUATE_SUPPLIED_PARAMETER;
        }
    };
    public static final Evaluator SINGLE_ITEM = new Evaluator(){

        @Override
        public Item evaluate(Expression expr, XPathContext context) throws XPathException {
            return expr.evaluateItem(context);
        }

        @Override
        public EvaluationMode getEvaluationMode() {
            return EvaluationMode.CALL_EVALUATE_SINGLE_ITEM;
        }
    };
    public static final Evaluator OPTIONAL_ITEM = new Evaluator(){

        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            Item item = expr.evaluateItem(context);
            return item == null ? EmptySequence.getInstance() : item;
        }

        @Override
        public EvaluationMode getEvaluationMode() {
            return EvaluationMode.CALL_EVALUATE_OPTIONAL_ITEM;
        }
    };
    public static final Evaluator LAZY_SEQUENCE = new Evaluator(){

        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            SequenceIterator iter = expr.iterate(context);
            return new LazySequence(iter);
        }

        @Override
        public EvaluationMode getEvaluationMode() {
            return EvaluationMode.MAKE_CLOSURE;
        }
    };
    public static final Evaluator MEMO_SEQUENCE = new Evaluator(){

        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            SequenceIterator iter = expr.iterate(context);
            return new MemoSequence(iter);
        }

        @Override
        public EvaluationMode getEvaluationMode() {
            return EvaluationMode.MAKE_MEMO_CLOSURE;
        }
    };
    public static final Evaluator MEMO_CLOSURE = new Evaluator(){

        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            return new MemoClosure(expr, context);
        }

        @Override
        public EvaluationMode getEvaluationMode() {
            return EvaluationMode.MAKE_MEMO_CLOSURE;
        }
    };
    public static final Evaluator SINGLETON_CLOSURE = new Evaluator(){

        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            return new SingletonClosure(expr, context);
        }

        @Override
        public EvaluationMode getEvaluationMode() {
            return EvaluationMode.MAKE_SINGLETON_CLOSURE;
        }
    };
    public static final Evaluator EAGER_SEQUENCE = new Evaluator(){

        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            SequenceIterator iter = expr.iterate(context);
            return iter.materialize();
        }

        @Override
        public EvaluationMode getEvaluationMode() {
            return EvaluationMode.ITERATE_AND_MATERIALIZE;
        }
    };
    public static final Evaluator SHARED_APPEND = new Evaluator(){

        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            if (expr instanceof Block) {
                Block block = (Block)expr;
                Operand[] children = block.getOperanda();
                ArrayList<GroundedValue> subsequences = new ArrayList<GroundedValue>(children.length);
                for (Operand o : children) {
                    Expression child = o.getChildExpression();
                    if (Cardinality.allowsMany(child.getCardinality())) {
                        subsequences.add(child.iterate(context).materialize());
                        continue;
                    }
                    Item j = child.evaluateItem(context);
                    if (j == null) continue;
                    subsequences.add(j);
                }
                return new Chain(subsequences);
            }
            return expr.iterate(context).materialize();
        }

        @Override
        public EvaluationMode getEvaluationMode() {
            return EvaluationMode.SHARED_APPEND_EXPRESSION;
        }
    };
    public static final Evaluator STREAMING_ARGUMENT = new Evaluator(){

        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            return context.getConfiguration().obtainOptimizer().evaluateStreamingArgument(expr, context);
        }

        @Override
        public EvaluationMode getEvaluationMode() {
            return EvaluationMode.STREAMING_ARGUMENT;
        }
    };
    public static final Evaluator MAKE_INDEXED_VARIABLE = new Evaluator(){

        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            return context.getConfiguration().obtainOptimizer().makeIndexedValue(expr.iterate(context));
        }

        @Override
        public EvaluationMode getEvaluationMode() {
            return EvaluationMode.MAKE_INDEXED_VARIABLE;
        }
    };
    public static final Evaluator PROCESS = new Evaluator(){

        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            Controller controller = context.getController();
            SequenceCollector seq = controller.allocateSequenceOutputter();
            ComplexContentOutputter out = new ComplexContentOutputter(seq);
            out.open();
            expr.process(out, context);
            out.close();
            Sequence val = seq.getSequence();
            seq.reset();
            return val;
        }

        @Override
        public EvaluationMode getEvaluationMode() {
            return EvaluationMode.PROCESS;
        }
    };
    public static final Evaluator LAZY_TAIL = new Evaluator(){

        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            TailExpression tail = (TailExpression)expr;
            VariableReference vr = (VariableReference)tail.getBaseExpression();
            Sequence base = VARIABLE.evaluate(vr, context);
            if (base instanceof MemoClosure) {
                SequenceIterator it = base.iterate();
                base = it.materialize();
            }
            if (base instanceof IntegerRange) {
                long end;
                long start = ((IntegerRange)base).getStart() + (long)tail.getStart() - 1L;
                if (start == (end = ((IntegerRange)base).getEnd())) {
                    return Int64Value.makeIntegerValue(end);
                }
                if (start > end) {
                    return EmptySequence.getInstance();
                }
                return new IntegerRange(start, end);
            }
            if (base instanceof GroundedValue) {
                GroundedValue baseSeq = (GroundedValue)base;
                return baseSeq.subsequence(tail.getStart() - 1, baseSeq.getLength() - tail.getStart() + 1);
            }
            return new MemoClosure(tail, context);
        }

        @Override
        public EvaluationMode getEvaluationMode() {
            return EvaluationMode.LAZY_TAIL_EXPRESSION;
        }
    };

    public abstract Sequence evaluate(Expression var1, XPathContext var2) throws XPathException;

    public abstract EvaluationMode getEvaluationMode();
}


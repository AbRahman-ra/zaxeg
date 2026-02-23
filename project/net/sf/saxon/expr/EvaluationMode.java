/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.parser.Evaluator;

public enum EvaluationMode {
    UNDECIDED(-1, Evaluator.EAGER_SEQUENCE),
    EVALUATE_LITERAL(0, Evaluator.LITERAL),
    EVALUATE_VARIABLE(1, Evaluator.VARIABLE),
    MAKE_CLOSURE(3, Evaluator.LAZY_SEQUENCE),
    MAKE_MEMO_CLOSURE(4, Evaluator.MEMO_CLOSURE),
    RETURN_EMPTY_SEQUENCE(5, Evaluator.EMPTY_SEQUENCE),
    EVALUATE_AND_MATERIALIZE_VARIABLE(6, Evaluator.VARIABLE),
    CALL_EVALUATE_OPTIONAL_ITEM(7, Evaluator.OPTIONAL_ITEM),
    ITERATE_AND_MATERIALIZE(8, Evaluator.EAGER_SEQUENCE),
    PROCESS(9, Evaluator.PROCESS),
    LAZY_TAIL_EXPRESSION(10, Evaluator.LAZY_TAIL),
    SHARED_APPEND_EXPRESSION(11, Evaluator.SHARED_APPEND),
    MAKE_INDEXED_VARIABLE(12, Evaluator.MAKE_INDEXED_VARIABLE),
    MAKE_SINGLETON_CLOSURE(13, Evaluator.SINGLETON_CLOSURE),
    EVALUATE_SUPPLIED_PARAMETER(14, Evaluator.SUPPLIED_PARAMETER),
    STREAMING_ARGUMENT(15, Evaluator.STREAMING_ARGUMENT),
    CALL_EVALUATE_SINGLE_ITEM(16, Evaluator.SINGLE_ITEM);

    private final int code;
    private final Evaluator evaluator;

    private EvaluationMode(int code, Evaluator evaluator) {
        this.code = code;
        this.evaluator = evaluator;
    }

    public int getCode() {
        return this.code;
    }

    public Evaluator getEvaluator() {
        return this.evaluator;
    }

    public static EvaluationMode forCode(int code) {
        for (EvaluationMode eval : EvaluationMode.values()) {
            if (eval.getCode() != code) continue;
            return eval;
        }
        return ITERATE_AND_MATERIALIZE;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.GeneralComparison;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.Token;

public class GeneralComparison20
extends GeneralComparison {
    public GeneralComparison20(Expression p0, int op, Expression p1) {
        super(p0, op, p1);
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        GeneralComparison20 gc = new GeneralComparison20(this.getLhsExpression().copy(rebindings), this.operator, this.getRhsExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, gc);
        gc.setRetainedStaticContext(this.getRetainedStaticContext());
        gc.comparer = this.comparer;
        gc.singletonOperator = this.singletonOperator;
        gc.needsRuntimeCheck = this.needsRuntimeCheck;
        gc.comparisonCardinality = this.comparisonCardinality;
        return gc;
    }

    @Override
    protected GeneralComparison getInverseComparison() {
        GeneralComparison20 gc = new GeneralComparison20(this.getRhsExpression(), Token.inverse(this.operator), this.getLhsExpression());
        gc.setRetainedStaticContext(this.getRetainedStaticContext());
        return gc;
    }
}


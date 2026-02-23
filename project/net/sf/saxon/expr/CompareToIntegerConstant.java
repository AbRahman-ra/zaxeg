/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.CompareToConstant;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.DoubleSortComparer;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.NumericValue;

public class CompareToIntegerConstant
extends CompareToConstant {
    private long comparand;

    public CompareToIntegerConstant(Expression operand, int operator, long comparand) {
        super(operand);
        this.operator = operator;
        this.comparand = comparand;
    }

    public long getComparand() {
        return this.comparand;
    }

    @Override
    public Expression getRhsExpression() {
        return new Literal(new Int64Value(this.comparand));
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        CompareToIntegerConstant c2 = new CompareToIntegerConstant(this.getLhsExpression().copy(rebindings), this.operator, this.comparand);
        ExpressionTool.copyLocationInfo(this, c2);
        return c2;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CompareToIntegerConstant && ((CompareToIntegerConstant)other).getLhsExpression().isEqual(this.getLhsExpression()) && ((CompareToIntegerConstant)other).comparand == this.comparand && ((CompareToIntegerConstant)other).operator == this.operator;
    }

    @Override
    public int computeHashCode() {
        int h = -2090134880;
        return h + this.getLhsExpression().hashCode() ^ (int)this.comparand;
    }

    @Override
    public boolean effectiveBooleanValue(XPathContext context) throws XPathException {
        NumericValue n = (NumericValue)this.getLhsExpression().evaluateItem(context);
        if (n.isNaN()) {
            return this.operator == 51;
        }
        int c = n.compareTo(this.comparand);
        return this.interpretComparisonResult(c);
    }

    @Override
    public int computeCardinality() {
        return 16384;
    }

    @Override
    public String getExpressionName() {
        return "compareToInt";
    }

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        destination.startElement("compareToInt", this);
        destination.emitAttribute("op", Token.tokens[this.operator]);
        destination.emitAttribute("val", this.comparand + "");
        this.getLhsExpression().export(destination);
        destination.endElement();
    }

    @Override
    public String toString() {
        return ExpressionTool.parenthesize(this.getLhsExpression()) + " " + Token.tokens[this.operator] + " " + this.comparand;
    }

    @Override
    public String toShortString() {
        return this.getLhsExpression().toShortString() + " " + Token.tokens[this.operator] + " " + this.comparand;
    }

    @Override
    public AtomicComparer getAtomicComparer() {
        return DoubleSortComparer.getInstance();
    }
}


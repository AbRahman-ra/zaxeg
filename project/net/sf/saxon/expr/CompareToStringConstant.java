/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.CompareToConstant;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.CodepointCollatingComparer;
import net.sf.saxon.expr.sort.CodepointCollator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;

public class CompareToStringConstant
extends CompareToConstant {
    private String comparand;

    public CompareToStringConstant(Expression operand, int operator, String comparand) {
        super(operand);
        this.operator = operator;
        this.comparand = comparand;
    }

    public String getComparand() {
        return this.comparand;
    }

    @Override
    public Expression getRhsExpression() {
        return new StringLiteral(this.comparand);
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        CompareToStringConstant c2 = new CompareToStringConstant(this.getLhsExpression().copy(rebindings), this.operator, this.comparand);
        ExpressionTool.copyLocationInfo(this, c2);
        return c2;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CompareToStringConstant && ((CompareToStringConstant)other).getLhsExpression().isEqual(this.getLhsExpression()) && ((CompareToStringConstant)other).comparand.equals(this.comparand) && ((CompareToStringConstant)other).operator == this.operator;
    }

    @Override
    public int computeHashCode() {
        int h = -2008345952;
        return h + this.getLhsExpression().hashCode() ^ this.comparand.hashCode();
    }

    @Override
    public boolean effectiveBooleanValue(XPathContext context) throws XPathException {
        CharSequence s = this.getLhsExpression().evaluateAsString(context);
        int c = CodepointCollator.compareCS(s, this.comparand);
        return this.interpretComparisonResult(c);
    }

    @Override
    public int computeCardinality() {
        return 16384;
    }

    @Override
    public String getExpressionName() {
        return "compareToString";
    }

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        destination.startElement("compareToString", this);
        destination.emitAttribute("op", Token.tokens[this.operator]);
        destination.emitAttribute("val", this.comparand);
        this.getLhsExpression().export(destination);
        destination.endElement();
    }

    @Override
    public String toString() {
        return ExpressionTool.parenthesize(this.getLhsExpression()) + " " + Token.tokens[this.operator] + " \"" + this.comparand + "\"";
    }

    @Override
    public String toShortString() {
        return this.getLhsExpression().toShortString() + " " + Token.tokens[this.operator] + " \"" + this.comparand + "\"";
    }

    @Override
    public AtomicComparer getAtomicComparer() {
        return CodepointCollatingComparer.getInstance();
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.BinaryExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.RangeIterator;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerRange;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;

public class RangeExpression
extends BinaryExpression {
    public RangeExpression(Expression start, Expression end) {
        super(start, 29, end);
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getLhs().typeCheck(visitor, contextInfo);
        this.getRhs().typeCheck(visitor, contextInfo);
        boolean backCompat = visitor.getStaticContext().isInBackwardsCompatibleMode();
        TypeChecker tc = visitor.getConfiguration().getTypeChecker(backCompat);
        RoleDiagnostic role0 = new RoleDiagnostic(1, "to", 0);
        this.setLhsExpression(tc.staticTypeCheck(this.getLhsExpression(), SequenceType.OPTIONAL_INTEGER, role0, visitor));
        RoleDiagnostic role1 = new RoleDiagnostic(1, "to", 1);
        this.setRhsExpression(tc.staticTypeCheck(this.getRhsExpression(), SequenceType.OPTIONAL_INTEGER, role1, visitor));
        return this.makeConstantRange();
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getLhs().optimize(visitor, contextInfo);
        this.getRhs().optimize(visitor, contextInfo);
        return this.makeConstantRange();
    }

    private Expression makeConstantRange() throws XPathException {
        if (this.getLhsExpression() instanceof Literal && this.getRhsExpression() instanceof Literal) {
            GroundedValue v0 = ((Literal)this.getLhsExpression()).getValue();
            GroundedValue v1 = ((Literal)this.getRhsExpression()).getValue();
            if (v0 instanceof Int64Value && v1 instanceof Int64Value) {
                Literal result;
                long i1;
                long i0 = ((Int64Value)v0).longValue();
                if (i0 > (i1 = ((Int64Value)v1).longValue())) {
                    result = Literal.makeEmptySequence();
                } else if (i0 == i1) {
                    result = Literal.makeLiteral(Int64Value.makeIntegerValue(i0), this);
                } else {
                    if (i1 - i0 > Integer.MAX_VALUE) {
                        throw new XPathException("Maximum length of sequence in Saxon is 2147483647", "XPDY0130");
                    }
                    result = Literal.makeLiteral(new IntegerRange(i0, i1), this);
                }
                ExpressionTool.copyLocationInfo(this, result);
                return result;
            }
        }
        return this;
    }

    @Override
    public ItemType getItemType() {
        return BuiltInAtomicType.INTEGER;
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        return UType.DECIMAL;
    }

    @Override
    public int computeCardinality() {
        return 57344;
    }

    @Override
    public IntegerValue[] getIntegerBounds() {
        IntegerValue[] start = this.getLhsExpression().getIntegerBounds();
        IntegerValue[] end = this.getLhsExpression().getIntegerBounds();
        if (start == null || end == null) {
            return null;
        }
        return new IntegerValue[]{start[0], end[1]};
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        RangeExpression exp = new RangeExpression(this.getLhsExpression().copy(rebindings), this.getRhsExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    @Override
    public int getImplementationMethod() {
        return 2;
    }

    @Override
    public String getExpressionName() {
        return "range";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("to", this);
        this.getLhsExpression().export(out);
        this.getRhsExpression().export(out);
        out.endElement();
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        IntegerValue av1 = (IntegerValue)this.getLhsExpression().evaluateItem(context);
        IntegerValue av2 = (IntegerValue)this.getRhsExpression().evaluateItem(context);
        return RangeIterator.makeRangeIterator(av1, av2);
    }
}


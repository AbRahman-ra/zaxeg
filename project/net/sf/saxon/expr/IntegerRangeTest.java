/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.UntypedAtomicValue;

public class IntegerRangeTest
extends Expression {
    private Operand valueOp;
    private Operand minOp;
    private Operand maxOp;

    public IntegerRangeTest(Expression value, Expression min, Expression max) {
        this.valueOp = new Operand(this, value, OperandRole.ATOMIC_SEQUENCE);
        this.minOp = new Operand(this, min, OperandRole.SINGLE_ATOMIC);
        this.maxOp = new Operand(this, max, OperandRole.SINGLE_ATOMIC);
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operandList(this.valueOp, this.minOp, this.maxOp);
    }

    public Expression getValue() {
        return this.valueOp.getChildExpression();
    }

    public void setValue(Expression value) {
        this.valueOp.setChildExpression(value);
    }

    public Expression getMin() {
        return this.minOp.getChildExpression();
    }

    public void setMin(Expression min) {
        this.minOp.setChildExpression(min);
    }

    public Expression getMax() {
        return this.maxOp.getChildExpression();
    }

    public void setMax(Expression max) {
        this.maxOp.setChildExpression(max);
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        if (Literal.isEmptySequence(this.getMin()) || Literal.isEmptySequence(this.getMax()) || Literal.isEmptySequence(this.getValue())) {
            return new Literal(BooleanValue.FALSE);
        }
        if (this.getMin() instanceof Literal && this.getMax() instanceof Literal && this.getValue() instanceof Literal) {
            BooleanValue result = this.evaluateItem(visitor.makeDynamicContext());
            return new Literal(result);
        }
        return this;
    }

    @Override
    public ItemType getItemType() {
        return BuiltInAtomicType.BOOLEAN;
    }

    @Override
    public int computeCardinality() {
        return 16384;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        IntegerRangeTest exp = new IntegerRangeTest(this.getValue().copy(rebindings), this.getMin().copy(rebindings), this.getMax().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    @Override
    public int getImplementationMethod() {
        return 1;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof IntegerRangeTest && ((IntegerRangeTest)other).getValue().isEqual(this.getValue()) && ((IntegerRangeTest)other).getMin().isEqual(this.getMin()) && ((IntegerRangeTest)other).getMax().isEqual(this.getMax());
    }

    @Override
    public int computeHashCode() {
        int h = this.getValue().hashCode() + 77;
        return h ^= this.getMin().hashCode() ^ this.getMax().hashCode();
    }

    @Override
    public BooleanValue evaluateItem(XPathContext c) throws XPathException {
        AtomicValue atom;
        IntegerValue minVal = null;
        IntegerValue maxVal = null;
        StringConverter toDouble = null;
        SequenceIterator iter = this.getValue().iterate(c);
        while ((atom = (AtomicValue)iter.next()) != null) {
            NumericValue v;
            if (minVal == null) {
                minVal = (IntegerValue)this.getMin().evaluateItem(c);
                if (minVal == null) {
                    return BooleanValue.FALSE;
                }
                maxVal = (IntegerValue)this.getMax().evaluateItem(c);
                if (maxVal == null || maxVal.compareTo(minVal) < 0) {
                    return BooleanValue.FALSE;
                }
            }
            if (atom instanceof UntypedAtomicValue) {
                ConversionResult result;
                if (toDouble == null) {
                    toDouble = BuiltInAtomicType.DOUBLE.getStringConverter(c.getConfiguration().getConversionRules());
                }
                if ((result = toDouble.convertString(atom.getStringValueCS())) instanceof ValidationFailure) {
                    XPathException e = new XPathException("Failed to convert untypedAtomic value {" + atom.getStringValueCS() + "}  to xs:integer", "FORG0001");
                    e.setLocation(this.getLocation());
                    throw e;
                }
                v = (DoubleValue)result.asAtomic();
            } else if (atom instanceof NumericValue) {
                v = (NumericValue)atom;
            } else {
                XPathException e = new XPathException("Cannot compare value of type " + atom.getUType() + " to xs:integer", "XPTY0004");
                e.setIsTypeError(true);
                e.setLocation(this.getLocation());
                throw e;
            }
            if (!v.isWholeNumber() || v.compareTo(minVal) < 0 || v.compareTo(maxVal) > 0) continue;
            return BooleanValue.TRUE;
        }
        return BooleanValue.FALSE;
    }

    @Override
    public String getExpressionName() {
        return "intRangeTest";
    }

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        destination.startElement("intRangeTest", this);
        this.getValue().export(destination);
        this.getMin().export(destination);
        this.getMax().export(destination);
        destination.endElement();
    }

    @Override
    public String toString() {
        return ExpressionTool.parenthesize(this.getValue()) + " = (" + ExpressionTool.parenthesize(this.getMin()) + " to " + ExpressionTool.parenthesize(this.getMax()) + ")";
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.UnaryExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.SequenceType;

public class NegateExpression
extends UnaryExpression {
    private boolean backwardsCompatible;

    public NegateExpression(Expression base) {
        super(base);
    }

    public void setBackwardsCompatible(boolean compatible) {
        this.backwardsCompatible = compatible;
    }

    public boolean isBackwardsCompatible() {
        return this.backwardsCompatible;
    }

    @Override
    protected OperandRole getOperandRole() {
        return OperandRole.SINGLE_ATOMIC;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        GroundedValue v;
        this.getOperand().typeCheck(visitor, contextInfo);
        RoleDiagnostic role = new RoleDiagnostic(9, "-", 0);
        Expression operand = visitor.getConfiguration().getTypeChecker(this.backwardsCompatible).staticTypeCheck(this.getBaseExpression(), SequenceType.OPTIONAL_NUMERIC, role, visitor);
        this.setBaseExpression(operand);
        if (operand instanceof Literal && (v = ((Literal)operand).getValue()) instanceof NumericValue) {
            return Literal.makeLiteral(((NumericValue)v).negate(), this);
        }
        return this;
    }

    @Override
    public ItemType getItemType() {
        return this.getBaseExpression().getItemType().getPrimitiveItemType();
    }

    @Override
    public int computeCardinality() {
        return this.getBaseExpression().getCardinality() & 0xFFFF7FFF;
    }

    @Override
    public int getImplementationMethod() {
        return 1;
    }

    @Override
    public NumericValue evaluateItem(XPathContext context) throws XPathException {
        NumericValue v1 = (NumericValue)this.getBaseExpression().evaluateItem(context);
        if (v1 == null) {
            return this.backwardsCompatible ? DoubleValue.NaN : null;
        }
        return v1.negate();
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        NegateExpression exp = new NegateExpression(this.getBaseExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    @Override
    protected String displayOperator(Configuration config) {
        return "-";
    }

    @Override
    public String getExpressionName() {
        return "minus";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("minus", this);
        if (this.backwardsCompatible) {
            out.emitAttribute("vn", "1");
        }
        this.getBaseExpression().export(out);
        out.endElement();
    }
}


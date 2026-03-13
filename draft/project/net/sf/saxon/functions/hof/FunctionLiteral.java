/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.hof;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.functions.AbstractFunction;
import net.sf.saxon.functions.hof.UserFunctionReference;
import net.sf.saxon.om.Function;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.FunctionItemType;

public class FunctionLiteral
extends Literal {
    public FunctionLiteral(Function value) {
        super(value);
    }

    @Override
    public Function getValue() {
        return (Function)super.getValue();
    }

    @Override
    public Expression simplify() throws XPathException {
        if (this.getValue() instanceof AbstractFunction) {
            ((AbstractFunction)this.getValue()).simplify();
        }
        return this;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        if (this.getValue() instanceof AbstractFunction) {
            ((AbstractFunction)this.getValue()).typeCheck(visitor, contextInfo);
        }
        return this;
    }

    @Override
    public FunctionItemType getItemType() {
        return this.getValue().getFunctionItemType();
    }

    @Override
    public int computeCardinality() {
        return 16384;
    }

    @Override
    public int computeSpecialProperties() {
        return 0x800000;
    }

    @Override
    public boolean isVacuousExpression() {
        return false;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        FunctionLiteral fl2 = new FunctionLiteral(this.getValue());
        ExpressionTool.copyLocationInfo(this, fl2);
        return fl2;
    }

    @Override
    public void setRetainedStaticContext(RetainedStaticContext rsc) {
        super.setRetainedStaticContext(rsc);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FunctionLiteral && ((FunctionLiteral)obj).getValue() == this.getValue();
    }

    @Override
    public int computeHashCode() {
        return this.getValue().hashCode();
    }

    @Override
    public String getExpressionName() {
        return "namedFunctionRef";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        Function f = this.getValue();
        if (f instanceof UserFunction) {
            new UserFunctionReference((UserFunction)f).export(out);
        } else {
            f.export(out);
        }
    }
}


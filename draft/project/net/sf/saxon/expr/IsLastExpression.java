/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.BooleanValue;

public final class IsLastExpression
extends Expression {
    private boolean condition;

    public IsLastExpression(boolean condition) {
        this.condition = condition;
    }

    public boolean getCondition() {
        return this.condition;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) {
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) {
        return this;
    }

    @Override
    public int computeSpecialProperties() {
        int p = super.computeSpecialProperties();
        return p | 0x800000;
    }

    @Override
    public BooleanValue evaluateItem(XPathContext c) throws XPathException {
        return BooleanValue.get(this.condition == c.isAtLast());
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
    public int getIntrinsicDependencies() {
        return 12;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        IsLastExpression exp = new IsLastExpression(this.condition);
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    @Override
    public int getImplementationMethod() {
        return 1;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof IsLastExpression && ((IsLastExpression)other).condition == this.condition;
    }

    @Override
    public int computeHashCode() {
        return this.condition ? 594252192 : -1989438816;
    }

    @Override
    public String getExpressionName() {
        return "isLast";
    }

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        destination.startElement("isLast", this);
        destination.emitAttribute("test", this.condition ? "1" : "0");
        destination.endElement();
    }

    @Override
    public String toString() {
        if (this.condition) {
            return "position() eq last()";
        }
        return "position() ne last()";
    }

    @Override
    public String getStreamerName() {
        return "IsLastExpr";
    }
}


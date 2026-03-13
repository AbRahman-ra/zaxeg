/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SingleItemFilter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.GeneralNodePattern;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.NodeTestPattern;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.pattern.SimplePositionalPattern;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;

public final class FirstItemExpression
extends SingleItemFilter {
    private FirstItemExpression(Expression base) {
        super(base);
    }

    public static Expression makeFirstItemExpression(Expression base) {
        if (base instanceof FirstItemExpression) {
            return base;
        }
        return new FirstItemExpression(base);
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        FirstItemExpression e2 = new FirstItemExpression(this.getBaseExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, e2);
        return e2;
    }

    @Override
    public Pattern toPattern(Configuration config) throws XPathException {
        Pattern basePattern = this.getBaseExpression().toPattern(config);
        ItemType type = basePattern.getItemType();
        if (type instanceof NodeTest) {
            Expression baseExpr = this.getBaseExpression();
            if (baseExpr instanceof AxisExpression && ((AxisExpression)baseExpr).getAxis() == 3 && basePattern instanceof NodeTestPattern) {
                return new SimplePositionalPattern((NodeTest)type, 1);
            }
            return new GeneralNodePattern(this, (NodeTest)type);
        }
        return basePattern;
    }

    @Override
    public int getImplementationMethod() {
        return 1;
    }

    @Override
    public Item evaluateItem(XPathContext context) throws XPathException {
        SequenceIterator iter = this.getBaseExpression().iterate(context);
        Item result = iter.next();
        iter.close();
        return result;
    }

    @Override
    public String getExpressionName() {
        return "first";
    }

    @Override
    public String toShortString() {
        return this.getBaseExpression().toShortString() + "[1]";
    }

    @Override
    public String getStreamerName() {
        return "FirstItemExpression";
    }
}


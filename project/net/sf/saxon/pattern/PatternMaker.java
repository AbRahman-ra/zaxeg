/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.FirstItemExpression;
import net.sf.saxon.expr.SlashExpression;
import net.sf.saxon.expr.SubscriptExpression;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trans.XPathException;

public class PatternMaker {
    public static Pattern fromExpression(Expression expression, Configuration config, boolean is30) throws XPathException {
        Pattern result = expression.toPattern(config);
        ExpressionTool.copyLocationInfo(expression, result);
        return result;
    }

    public static int getAxisForPathStep(Expression step) throws XPathException {
        if (step instanceof AxisExpression) {
            return AxisInfo.inverseAxis[((AxisExpression)step).getAxis()];
        }
        if (step instanceof FilterExpression) {
            return PatternMaker.getAxisForPathStep(((FilterExpression)step).getSelectExpression());
        }
        if (step instanceof FirstItemExpression) {
            return PatternMaker.getAxisForPathStep(((FirstItemExpression)step).getBaseExpression());
        }
        if (step instanceof SubscriptExpression) {
            return PatternMaker.getAxisForPathStep(((SubscriptExpression)step).getBaseExpression());
        }
        if (step instanceof SlashExpression) {
            return PatternMaker.getAxisForPathStep(((SlashExpression)step).getFirstStep());
        }
        if (step instanceof ContextItemExpression) {
            return 12;
        }
        throw new XPathException("The path in a pattern must contain simple steps");
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.expr.AtomicSequenceConverter;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FirstItemExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.expr.parser.XPathParser;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.style.ExpressionContext;
import net.sf.saxon.style.XSLAnalyzeString;
import net.sf.saxon.style.XSLLeafNodeConstructor;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.StringValue;

public abstract class AttributeValueTemplate {
    private AttributeValueTemplate() {
    }

    public static Expression make(String avt, StaticContext env) throws XPathException {
        Expression result;
        int languageLevel = env.getXPathVersion();
        ArrayList<Expression> components = new ArrayList<Expression>(5);
        int len = avt.length();
        int last = 0;
        while (last < len) {
            int i0 = avt.indexOf("{", last);
            int i1 = avt.indexOf("{{", last);
            int i8 = avt.indexOf("}", last);
            int i9 = avt.indexOf("}}", last);
            if (!(i0 >= 0 && len >= i0 || i8 >= 0 && len >= i8)) {
                AttributeValueTemplate.addStringComponent(components, avt, last, len);
                break;
            }
            if (i8 >= 0 && (i0 < 0 || i8 < i0)) {
                if (i8 != i9) {
                    XPathException err = new XPathException("Closing curly brace in attribute value template \"" + avt.substring(0, len) + "\" must be doubled");
                    err.setErrorCode("XTSE0370");
                    err.setIsStaticError(true);
                    throw err;
                }
                AttributeValueTemplate.addStringComponent(components, avt, last, i8 + 1);
                last = i8 + 2;
                continue;
            }
            if (i1 >= 0 && i1 == i0) {
                AttributeValueTemplate.addStringComponent(components, avt, last, i1 + 1);
                last = i1 + 2;
                continue;
            }
            if (i0 >= 0) {
                if (i0 > last) {
                    AttributeValueTemplate.addStringComponent(components, avt, last, i0);
                }
                XPathParser parser = env.getConfiguration().newExpressionParser("XP", false, languageLevel);
                parser.setLanguage(XPathParser.ParsedLanguage.XPATH, 31);
                parser.setAllowAbsentExpression(true);
                Expression exp = parser.parse(avt, i0 + 1, 215, env);
                exp.setRetainedStaticContext(env.makeRetainedStaticContext());
                exp = exp.simplify();
                last = parser.getTokenizer().currentTokenStartOffset + 1;
                if (env instanceof ExpressionContext && ((ExpressionContext)env).getStyleElement() instanceof XSLAnalyzeString && AttributeValueTemplate.isIntegerOrIntegerPair(exp)) {
                    env.issueWarning("Found {" + AttributeValueTemplate.showIntegers(exp) + "} in regex attribute: perhaps {{" + AttributeValueTemplate.showIntegers(exp) + "}} was intended? (The attribute is an AVT, so curly braces should be doubled)", exp.getLocation());
                }
                if (env.isInBackwardsCompatibleMode()) {
                    components.add(AttributeValueTemplate.makeFirstItem(exp, env));
                    continue;
                }
                components.add(XSLLeafNodeConstructor.makeSimpleContentConstructor(exp, new StringLiteral(StringValue.SINGLE_SPACE), env).simplify());
                continue;
            }
            throw new IllegalStateException("Internal error parsing AVT");
        }
        if (components.isEmpty()) {
            result = new StringLiteral(StringValue.EMPTY_STRING);
        } else if (components.size() == 1) {
            result = ((Expression)components.get(0)).simplify();
        } else {
            Expression[] args = new Expression[components.size()];
            components.toArray(args);
            Expression fn = SystemFunction.makeCall("concat", new RetainedStaticContext(env), args);
            result = fn.simplify();
        }
        result.setLocation(env.getContainingLocation());
        return result;
    }

    private static boolean isIntegerOrIntegerPair(Expression exp) {
        if (exp instanceof Literal) {
            GroundedValue val = ((Literal)exp).getValue();
            if (val instanceof IntegerValue) {
                return true;
            }
            if (val.getLength() == 2) {
                return val.itemAt(0) instanceof IntegerValue && val.itemAt(1) instanceof IntegerValue;
            }
        }
        return false;
    }

    private static String showIntegers(Expression exp) {
        if (exp instanceof Literal) {
            GroundedValue val = ((Literal)exp).getValue();
            if (val instanceof IntegerValue) {
                return val.toString();
            }
            if (val.getLength() == 2 && val.itemAt(0) instanceof IntegerValue && val.itemAt(1) instanceof IntegerValue) {
                return val.itemAt(0).toString() + "," + val.itemAt(1).toString();
            }
        }
        return "";
    }

    private static void addStringComponent(List<Expression> components, String avt, int start, int end) {
        if (start < end) {
            components.add(new StringLiteral(avt.substring(start, end)));
        }
    }

    public static Expression makeFirstItem(Expression exp, StaticContext env) {
        if (Literal.isEmptySequence(exp)) {
            return exp;
        }
        TypeHierarchy th = env.getConfiguration().getTypeHierarchy();
        if (!exp.getItemType().isPlainType()) {
            exp = Atomizer.makeAtomizer(exp, null);
        }
        if (Cardinality.allowsMany(exp.getCardinality())) {
            exp = FirstItemExpression.makeFirstItemExpression(exp);
        }
        if (!th.isSubType(exp.getItemType(), BuiltInAtomicType.STRING)) {
            exp = new AtomicSequenceConverter(exp, BuiltInAtomicType.STRING);
            ((AtomicSequenceConverter)exp).allocateConverterStatically(env.getConfiguration(), false);
        }
        return exp;
    }
}


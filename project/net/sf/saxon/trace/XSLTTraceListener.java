/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trace;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.instruct.FixedAttribute;
import net.sf.saxon.expr.instruct.FixedElement;
import net.sf.saxon.expr.instruct.GlobalParam;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.NamedTemplate;
import net.sf.saxon.expr.instruct.TemplateRule;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.parser.CodeInjector;
import net.sf.saxon.functions.Trace;
import net.sf.saxon.trace.AbstractTraceListener;
import net.sf.saxon.trace.Traceable;
import net.sf.saxon.trace.XSLTTraceCodeInjector;

public class XSLTTraceListener
extends AbstractTraceListener {
    @Override
    public CodeInjector getCodeInjector() {
        return new XSLTTraceCodeInjector();
    }

    @Override
    protected String getOpeningAttributes() {
        return "xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"";
    }

    @Override
    protected String tag(Traceable info) {
        return XSLTTraceListener.tagName(info);
    }

    public static String tagName(Traceable info) {
        if (info instanceof Expression) {
            Expression expr = (Expression)info;
            if (expr instanceof FixedElement) {
                return "LRE";
            }
            if (expr instanceof FixedAttribute) {
                return "ATTR";
            }
            if (expr instanceof LetExpression) {
                return "xsl:variable";
            }
            if (expr.isCallOn(Trace.class)) {
                return "fn:trace";
            }
            return expr.getExpressionName();
        }
        if (info instanceof UserFunction) {
            return "xsl:function";
        }
        if (info instanceof TemplateRule) {
            return "xsl:template";
        }
        if (info instanceof NamedTemplate) {
            return "xsl:template";
        }
        if (info instanceof GlobalParam) {
            return "xsl:param";
        }
        if (info instanceof GlobalVariable) {
            return "xsl:variable";
        }
        if (info instanceof Trace) {
            return "fn:trace";
        }
        return "misc";
    }
}


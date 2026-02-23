/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trace;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.flwor.ClauseInfo;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.functions.Trace;
import net.sf.saxon.om.Item;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trace.AbstractTraceListener;
import net.sf.saxon.trace.Traceable;
import net.sf.saxon.trace.TraceableComponent;
import net.sf.saxon.trans.Mode;

public class XQueryTraceListener
extends AbstractTraceListener {
    @Override
    protected String getOpeningAttributes() {
        return "";
    }

    @Override
    protected String tag(Traceable info) {
        if (info instanceof TraceableComponent) {
            if (info instanceof GlobalVariable) {
                return "variable";
            }
            if (info instanceof UserFunction) {
                return "function";
            }
            if (info instanceof XQueryExpression) {
                return "query";
            }
            return "misc";
        }
        if (info instanceof Trace) {
            return "fn:trace";
        }
        if (info instanceof ClauseInfo) {
            return ((ClauseInfo)info).getClause().getClauseKey().toString();
        }
        if (info instanceof Expression) {
            String s = ((Expression)info).getExpressionName();
            if (s.startsWith("xsl:")) {
                s = s.substring(4);
            }
            switch (s) {
                case "value-of": {
                    return "text";
                }
                case "LRE": {
                    return "element";
                }
                case "ATTR": {
                    return "attribute";
                }
            }
            return s;
        }
        return null;
    }

    @Override
    public void startRuleSearch() {
    }

    @Override
    public void endRuleSearch(Object rule, Mode mode, Item item) {
    }
}


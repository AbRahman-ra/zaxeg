/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.JaxenException;
import org.jaxen.expr.DefaultExpr;
import org.jaxen.expr.Expr;
import org.jaxen.expr.FunctionCallExpr;

public class DefaultFunctionCallExpr
extends DefaultExpr
implements FunctionCallExpr {
    private static final long serialVersionUID = -4747789292572193708L;
    private String prefix;
    private String functionName;
    private List parameters;

    public DefaultFunctionCallExpr(String prefix, String functionName) {
        this.prefix = prefix;
        this.functionName = functionName;
        this.parameters = new ArrayList();
    }

    public void addParameter(Expr parameter) {
        this.parameters.add(parameter);
    }

    public List getParameters() {
        return this.parameters;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getFunctionName() {
        return this.functionName;
    }

    public String getText() {
        StringBuffer buf = new StringBuffer();
        String prefix = this.getPrefix();
        if (prefix != null && prefix.length() > 0) {
            buf.append(prefix);
            buf.append(":");
        }
        buf.append(this.getFunctionName());
        buf.append("(");
        Iterator paramIter = this.getParameters().iterator();
        while (paramIter.hasNext()) {
            Expr eachParam = (Expr)paramIter.next();
            buf.append(eachParam.getText());
            if (!paramIter.hasNext()) continue;
            buf.append(", ");
        }
        buf.append(")");
        return buf.toString();
    }

    public Expr simplify() {
        List paramExprs = this.getParameters();
        int paramSize = paramExprs.size();
        ArrayList<Expr> newParams = new ArrayList<Expr>(paramSize);
        for (int i = 0; i < paramSize; ++i) {
            Expr eachParam = (Expr)paramExprs.get(i);
            newParams.add(eachParam.simplify());
        }
        this.parameters = newParams;
        return this;
    }

    public String toString() {
        String prefix = this.getPrefix();
        if (prefix == null) {
            return "[(DefaultFunctionCallExpr): " + this.getFunctionName() + "(" + this.getParameters() + ") ]";
        }
        return "[(DefaultFunctionCallExpr): " + this.getPrefix() + ":" + this.getFunctionName() + "(" + this.getParameters() + ") ]";
    }

    public Object evaluate(Context context) throws JaxenException {
        String prefix = this.getPrefix();
        String namespaceURI = null;
        if (prefix != null && !"".equals(prefix)) {
            namespaceURI = context.translateNamespacePrefixToUri(prefix);
        }
        Function func = context.getFunction(namespaceURI, prefix, this.getFunctionName());
        List paramValues = this.evaluateParams(context);
        return func.call(context, paramValues);
    }

    public List evaluateParams(Context context) throws JaxenException {
        List paramExprs = this.getParameters();
        int paramSize = paramExprs.size();
        ArrayList<Object> paramValues = new ArrayList<Object>(paramSize);
        for (int i = 0; i < paramSize; ++i) {
            Expr eachParam = (Expr)paramExprs.get(i);
            Object eachValue = eachParam.evaluate(context);
            paramValues.add(eachValue);
        }
        return paramValues;
    }
}


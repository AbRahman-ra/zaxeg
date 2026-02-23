/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.Context;
import org.jaxen.UnresolvableException;
import org.jaxen.expr.DefaultExpr;
import org.jaxen.expr.VariableReferenceExpr;

class DefaultVariableReferenceExpr
extends DefaultExpr
implements VariableReferenceExpr {
    private static final long serialVersionUID = 8832095437149358674L;
    private String prefix;
    private String localName;

    DefaultVariableReferenceExpr(String prefix, String variableName) {
        this.prefix = prefix;
        this.localName = variableName;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getVariableName() {
        return this.localName;
    }

    public String toString() {
        return "[(DefaultVariableReferenceExpr): " + this.getQName() + "]";
    }

    private String getQName() {
        if ("".equals(this.prefix)) {
            return this.localName;
        }
        return this.prefix + ":" + this.localName;
    }

    public String getText() {
        return "$" + this.getQName();
    }

    public Object evaluate(Context context) throws UnresolvableException {
        String prefix = this.getPrefix();
        String namespaceURI = null;
        if (prefix != null && !"".equals(prefix)) {
            namespaceURI = context.translateNamespacePrefixToUri(prefix);
        }
        return context.getVariableValue(namespaceURI, prefix, this.localName);
    }
}


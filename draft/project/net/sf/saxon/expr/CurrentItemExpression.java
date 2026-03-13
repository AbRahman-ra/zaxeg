/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.ContextItemExpression;

public class CurrentItemExpression
extends ContextItemExpression {
    public CurrentItemExpression() {
        this.setErrorCodeForUndefinedContext("XTDE1360", false);
    }
}


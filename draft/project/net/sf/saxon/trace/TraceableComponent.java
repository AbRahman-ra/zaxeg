/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trace;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.trace.Traceable;

public interface TraceableComponent
extends Traceable {
    public Expression getBody();

    public void setBody(Expression var1);

    public String getTracingTag();
}


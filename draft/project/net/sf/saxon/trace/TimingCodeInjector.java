/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trace;

import net.sf.saxon.expr.instruct.ComponentTracer;
import net.sf.saxon.trace.TraceCodeInjector;
import net.sf.saxon.trace.TraceableComponent;

public class TimingCodeInjector
extends TraceCodeInjector {
    @Override
    public void process(TraceableComponent component) {
        ComponentTracer trace = new ComponentTracer(component);
        component.setBody(trace);
    }
}


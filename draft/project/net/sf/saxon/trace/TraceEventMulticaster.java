/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trace;

import java.util.Map;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.om.Item;
import net.sf.saxon.trace.Traceable;
import net.sf.saxon.trans.SimpleMode;

public class TraceEventMulticaster
implements TraceListener {
    protected final TraceListener a;
    protected final TraceListener b;

    protected TraceEventMulticaster(TraceListener a, TraceListener b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public void setOutputDestination(Logger stream) {
        this.a.setOutputDestination(stream);
        this.b.setOutputDestination(stream);
    }

    protected TraceListener remove(TraceListener oldl) {
        if (oldl == this.a) {
            return this.b;
        }
        if (oldl == this.b) {
            return this.a;
        }
        TraceListener a2 = TraceEventMulticaster.removeInternal(this.a, oldl);
        TraceListener b2 = TraceEventMulticaster.removeInternal(this.b, oldl);
        if (a2 == this.a && b2 == this.b) {
            return this;
        }
        return TraceEventMulticaster.addInternal(a2, b2);
    }

    @Override
    public void open(Controller controller) {
        this.a.open(controller);
        this.b.open(controller);
    }

    @Override
    public void close() {
        this.a.close();
        this.b.close();
    }

    @Override
    public void enter(Traceable element, Map<String, Object> properties, XPathContext context) {
        this.a.enter(element, properties, context);
        this.b.enter(element, properties, context);
    }

    @Override
    public void leave(Traceable element) {
        this.a.leave(element);
        this.b.leave(element);
    }

    @Override
    public void startCurrentItem(Item item) {
        this.a.startCurrentItem(item);
        this.b.startCurrentItem(item);
    }

    @Override
    public void endCurrentItem(Item item) {
        this.a.endCurrentItem(item);
        this.b.endCurrentItem(item);
    }

    @Override
    public void startRuleSearch() {
        this.a.startRuleSearch();
        this.b.startRuleSearch();
    }

    public void endRuleSearch(Object rule, SimpleMode mode, Item item) {
        this.a.endRuleSearch(rule, mode, item);
        this.b.endRuleSearch(rule, mode, item);
    }

    public static TraceListener add(TraceListener a, TraceListener b) {
        return TraceEventMulticaster.addInternal(a, b);
    }

    public static TraceListener remove(TraceListener l, TraceListener oldl) {
        return TraceEventMulticaster.removeInternal(l, oldl);
    }

    protected static TraceListener addInternal(TraceListener a, TraceListener b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return new TraceEventMulticaster(a, b);
    }

    protected static TraceListener removeInternal(TraceListener l, TraceListener oldl) {
        if (l == oldl || l == null) {
            return null;
        }
        if (l instanceof TraceEventMulticaster) {
            return ((TraceEventMulticaster)l).remove(oldl);
        }
        return l;
    }
}


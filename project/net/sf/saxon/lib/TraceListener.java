/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.util.EventListener;
import java.util.Map;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.om.Item;
import net.sf.saxon.trace.Traceable;
import net.sf.saxon.trans.Mode;

public interface TraceListener
extends EventListener {
    public void setOutputDestination(Logger var1);

    public void open(Controller var1);

    public void close();

    default public void enter(Traceable instruction, Map<String, Object> properties, XPathContext context) {
    }

    default public void leave(Traceable instruction) {
    }

    public void startCurrentItem(Item var1);

    public void endCurrentItem(Item var1);

    default public void startRuleSearch() {
    }

    default public void endRuleSearch(Object rule, Mode mode, Item item) {
    }
}


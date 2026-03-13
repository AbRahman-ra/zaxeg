/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.util.Stack;
import net.sf.saxon.event.ContentHandlerProxy;
import org.xml.sax.Locator;

public class ContentHandlerProxyLocator
implements Locator {
    private ContentHandlerProxy parent = null;

    public ContentHandlerProxyLocator(ContentHandlerProxy parent) {
        this.parent = parent;
    }

    @Override
    public String getPublicId() {
        return null;
    }

    @Override
    public String getSystemId() {
        return this.parent.getCurrentLocation().getSystemId();
    }

    @Override
    public int getLineNumber() {
        return this.parent.getCurrentLocation().getLineNumber();
    }

    @Override
    public int getColumnNumber() {
        return this.parent.getCurrentLocation().getColumnNumber();
    }

    public Stack getContextItemStack() {
        ContentHandlerProxy.ContentHandlerProxyTraceListener traceListener = this.parent.getTraceListener();
        if (traceListener == null) {
            return null;
        }
        return traceListener.getContextItemStack();
    }
}


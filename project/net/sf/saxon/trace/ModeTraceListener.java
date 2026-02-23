/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trace;

import java.util.Stack;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.TemplateRule;
import net.sf.saxon.om.Item;
import net.sf.saxon.trace.AbstractTraceListener;
import net.sf.saxon.trace.Traceable;

public class ModeTraceListener
extends AbstractTraceListener {
    private Stack<Item> stack = new Stack();

    @Override
    protected String getOpeningAttributes() {
        return "xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"";
    }

    @Override
    public void startCurrentItem(Item item) {
        if (this.stack.empty() || this.stack.peek() != item) {
            super.startCurrentItem(item);
            this.stack.push(item);
        }
    }

    @Override
    public void endCurrentItem(Item item) {
        if (this.stack.peek() == item) {
            super.endCurrentItem(item);
            this.stack.pop();
        }
    }

    public void enter(Traceable info, XPathContext context) {
        if (info instanceof TemplateRule) {
            String file = this.abbreviateLocationURI(info.getLocation().getSystemId());
            String msg = AbstractTraceListener.spaces(this.indent) + "<rule match=\"" + this.escape(((TemplateRule)info).getMatchPattern().toString()) + '\"' + " line=\"" + info.getLocation().getLineNumber() + '\"' + " module=\"" + this.escape(file) + '\"' + '>';
            this.out.info(msg);
            ++this.indent;
        }
    }

    @Override
    public void leave(Traceable info) {
        if (info instanceof TemplateRule) {
            --this.indent;
            this.out.info(AbstractTraceListener.spaces(this.indent) + "</rule>");
        }
    }

    @Override
    protected String tag(Traceable info) {
        return "";
    }
}


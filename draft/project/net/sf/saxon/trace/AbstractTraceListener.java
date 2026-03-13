/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trace;

import java.util.Map;
import net.sf.saxon.Controller;
import net.sf.saxon.Version;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.parser.CodeInjector;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.StandardDiagnostics;
import net.sf.saxon.lib.StandardLogger;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.TraceCodeInjector;
import net.sf.saxon.trace.Traceable;
import net.sf.saxon.trace.TraceableComponent;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public abstract class AbstractTraceListener
extends StandardDiagnostics
implements TraceListener {
    protected int indent = 0;
    private int detail = 2;
    protected Logger out = new StandardLogger();
    private static StringBuffer spaceBuffer = new StringBuffer("                ");

    public CodeInjector getCodeInjector() {
        return new TraceCodeInjector();
    }

    public void setLevelOfDetail(int level) {
        this.detail = level;
    }

    @Override
    public void open(Controller controller) {
        this.out.info("<trace saxon-version=\"" + Version.getProductVersion() + "\" " + this.getOpeningAttributes() + '>');
        ++this.indent;
    }

    protected abstract String getOpeningAttributes();

    @Override
    public void close() {
        --this.indent;
        this.out.info("</trace>");
    }

    @Override
    public void enter(Traceable info, Map<String, Object> properties, XPathContext context) {
        if (this.isApplicable(info)) {
            Location loc = info.getLocation();
            String tag = this.tag(info);
            String file = this.abbreviateLocationURI(loc.getSystemId());
            StringBuilder msg = new StringBuilder(AbstractTraceListener.spaces(this.indent) + '<' + tag);
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                Object val = entry.getValue();
                if (val instanceof StructuredQName) {
                    val = ((StructuredQName)val).getDisplayName();
                } else if (val instanceof StringValue) {
                    val = ((StringValue)val).getStringValue();
                }
                if (val == null) continue;
                msg.append(' ').append(entry.getKey()).append("=\"").append(this.escape(val.toString())).append('\"');
            }
            msg.append(" line=\"").append(loc.getLineNumber()).append('\"');
            int col = loc.getColumnNumber();
            if (col >= 0) {
                msg.append(" column=\"").append(loc.getColumnNumber()).append('\"');
            }
            msg.append(" module=\"").append(this.escape(file)).append('\"');
            msg.append(">");
            this.out.info(msg.toString());
            ++this.indent;
        }
    }

    public String escape(String in) {
        if (in == null) {
            return "";
        }
        CharSequence collapsed = Whitespace.collapseWhitespace(in);
        FastStringBuffer sb = new FastStringBuffer(collapsed.length() + 10);
        for (int i = 0; i < collapsed.length(); ++i) {
            char c = collapsed.charAt(i);
            if (c == '<') {
                sb.append("&lt;");
                continue;
            }
            if (c == '>') {
                sb.append("&gt;");
                continue;
            }
            if (c == '&') {
                sb.append("&amp;");
                continue;
            }
            if (c == '\"') {
                sb.append("&#34;");
                continue;
            }
            if (c == '\n') {
                sb.append("&#xA;");
                continue;
            }
            if (c == '\r') {
                sb.append("&#xD;");
                continue;
            }
            if (c == '\t') {
                sb.append("&#x9;");
                continue;
            }
            sb.cat(c);
        }
        return sb.toString();
    }

    @Override
    public void leave(Traceable info) {
        if (this.isApplicable(info)) {
            String tag = this.tag(info);
            --this.indent;
            this.out.info(AbstractTraceListener.spaces(this.indent) + "</" + tag + '>');
        }
    }

    protected boolean isApplicable(Traceable info) {
        return this.level(info) <= this.detail;
    }

    protected abstract String tag(Traceable var1);

    protected int level(Traceable info) {
        if (info instanceof TraceableComponent) {
            return 1;
        }
        if (info instanceof Instruction) {
            return 2;
        }
        return 3;
    }

    @Override
    public void startCurrentItem(Item item) {
        if (item instanceof NodeInfo && this.detail > 0) {
            NodeInfo curr = (NodeInfo)item;
            this.out.info(AbstractTraceListener.spaces(this.indent) + "<source node=\"" + Navigator.getPath(curr) + "\" line=\"" + curr.getLineNumber() + "\" file=\"" + this.abbreviateLocationURI(curr.getSystemId()) + "\">");
        }
        ++this.indent;
    }

    @Override
    public void endCurrentItem(Item item) {
        --this.indent;
        if (item instanceof NodeInfo && this.detail > 0) {
            NodeInfo curr = (NodeInfo)item;
            this.out.info(AbstractTraceListener.spaces(this.indent) + "</source><!-- " + Navigator.getPath(curr) + " -->");
        }
    }

    protected static String spaces(int n) {
        while (spaceBuffer.length() < n) {
            spaceBuffer.append(spaceBuffer);
        }
        return spaceBuffer.substring(0, n);
    }

    @Override
    public void setOutputDestination(Logger stream) {
        this.out = stream;
    }

    public Logger getOutputDestination() {
        return this.out;
    }

    @Override
    public void endRuleSearch(Object rule, Mode mode, Item item) {
    }

    @Override
    public void startRuleSearch() {
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trace;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.PreparedStylesheet;
import net.sf.saxon.event.PushToReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.TransformerReceiver;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.GlobalParameterSet;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.NamedTemplate;
import net.sf.saxon.expr.instruct.TemplateRule;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.StandardLogger;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Push;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.trace.Traceable;
import net.sf.saxon.trace.TraceableComponent;
import net.sf.saxon.trans.CompilerInfo;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XsltController;
import net.sf.saxon.value.StringValue;

public class TimingTraceListener
implements TraceListener {
    private int runs = 0;
    private Logger out = new StandardLogger();
    private long t_total;
    private Stack<ComponentMetrics> metrics = new Stack();
    private HashMap<Traceable, ComponentMetrics> instructMap = new HashMap();
    protected Configuration config = null;
    private Map<Traceable, Integer> recursionDepth = new HashMap<Traceable, Integer>();
    private HostLanguage lang = HostLanguage.XSLT;

    @Override
    public void setOutputDestination(Logger stream) {
        this.out = stream;
    }

    @Override
    public void open(Controller controller) {
        this.config = controller.getConfiguration();
        this.lang = controller.getExecutable().getHostLanguage();
        this.t_total = System.nanoTime();
    }

    @Override
    public void close() {
        this.t_total = System.nanoTime() - this.t_total;
        ++this.runs;
        try {
            PreparedStylesheet sheet = this.getStyleSheet();
            XsltController controller = sheet.newController();
            SerializationProperties props = new SerializationProperties();
            props.setProperty("method", "html");
            props.setProperty("indent", "yes");
            controller.setTraceListener(null);
            TransformerReceiver tr = new TransformerReceiver(controller);
            controller.initializeController(new GlobalParameterSet());
            tr.open();
            Receiver result = this.config.getSerializerFactory().getReceiver((Result)this.out.asStreamResult(), props, controller.makePipelineConfiguration());
            tr.setDestination(result);
            PushToReceiver push = new PushToReceiver(tr);
            Push.Document doc = push.document(true);
            Push.Element trace = doc.element("trace").attribute("t-total", Double.toString((double)this.t_total / 1000000.0));
            for (ComponentMetrics ins : this.instructMap.values()) {
                String name;
                Push.Element fn = trace.element("fn");
                if (ins.component.getObjectName() != null) {
                    name = ins.component.getObjectName().getDisplayName();
                    fn.attribute("name", name);
                } else if (ins.properties.get("name") != null) {
                    name = ins.properties.get("name").toString();
                    fn.attribute("name", name);
                }
                if (ins.properties.get("match") != null) {
                    name = ins.properties.get("match").toString();
                    fn.attribute("match", name);
                }
                if (ins.properties.get("mode") != null) {
                    name = ins.properties.get("mode").toString();
                    fn.attribute("mode", name);
                }
                fn.attribute("construct", ins.component.getTracingTag()).attribute("file", ins.component.getLocation().getSystemId()).attribute("count", Long.toString(ins.count / (long)this.runs)).attribute("t-sum-net", Double.toString((double)ins.net / (double)this.runs / 1000000.0)).attribute("t-avg-net", Double.toString((double)ins.net / (double)ins.count / 1000000.0)).attribute("t-sum", Double.toString((double)ins.gross / (double)this.runs / 1000000.0)).attribute("t-avg", Double.toString((double)ins.gross / (double)ins.count / 1000000.0)).attribute("line", Long.toString(ins.component.getLocation().getLineNumber())).close();
            }
            doc.close();
        } catch (TransformerException e) {
            System.err.println("Unable to transform timing profile information: " + e.getMessage());
        } catch (SaxonApiException e) {
            System.err.println("Unable to generate timing profile information: " + e.getMessage());
        }
    }

    @Override
    public void enter(Traceable instruction, Map<String, Object> properties, XPathContext context) {
        if (this.isTarget(instruction)) {
            long start = System.nanoTime();
            ComponentMetrics metric = new ComponentMetrics();
            metric.component = (TraceableComponent)instruction;
            metric.properties = properties;
            metric.gross = start;
            this.metrics.add(metric);
            Integer depth = this.recursionDepth.get(instruction);
            if (depth == null) {
                this.recursionDepth.put(instruction, 0);
            } else {
                this.recursionDepth.put(instruction, depth + 1);
            }
        }
    }

    private boolean isTarget(Traceable traceable) {
        return traceable instanceof UserFunction || traceable instanceof GlobalVariable || traceable instanceof NamedTemplate || traceable instanceof TemplateRule;
    }

    @Override
    public void leave(Traceable instruction) {
        if (this.isTarget(instruction)) {
            ComponentMetrics metric = this.metrics.peek();
            long duration = System.nanoTime() - metric.gross;
            metric.net = duration - metric.net;
            metric.gross = duration;
            ComponentMetrics foundInstructDetails = this.instructMap.get(instruction);
            if (foundInstructDetails == null) {
                metric.count = 1L;
                this.instructMap.put(instruction, metric);
            } else {
                ++foundInstructDetails.count;
                Integer depth = this.recursionDepth.get(instruction);
                depth = depth - 1;
                this.recursionDepth.put(instruction, depth);
                if (depth == 0) {
                    foundInstructDetails.gross += metric.gross;
                }
                foundInstructDetails.net += metric.net;
            }
            this.metrics.pop();
            if (!this.metrics.isEmpty()) {
                ComponentMetrics parentInstruct = this.metrics.peek();
                parentInstruct.net += duration;
            }
        }
    }

    @Override
    public void startCurrentItem(Item item) {
    }

    @Override
    public void endCurrentItem(Item item) {
    }

    private PreparedStylesheet getStyleSheet() throws XPathException {
        InputStream in = this.getStylesheetInputStream();
        StreamSource ss = new StreamSource(in, "profile.xsl");
        CompilerInfo info = this.config.getDefaultXsltCompilerInfo();
        info.setParameter(new StructuredQName("", "", "lang"), new StringValue(this.lang == HostLanguage.XSLT ? "XSLT" : "XQuery"));
        return Compilation.compileSingletonPackage(this.config, info, ss);
    }

    private InputStream getStylesheetInputStream() {
        ArrayList<String> messages = new ArrayList<String>();
        ArrayList<ClassLoader> classLoaders = new ArrayList<ClassLoader>();
        return Configuration.locateResource("profile.xsl", messages, classLoaders);
    }

    private static class ComponentMetrics {
        TraceableComponent component;
        Map<String, Object> properties;
        long gross;
        long net;
        long count;

        private ComponentMetrics() {
        }
    }
}


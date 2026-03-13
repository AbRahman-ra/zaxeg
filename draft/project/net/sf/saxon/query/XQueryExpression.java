/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.query;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ExpressionOwner;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.instruct.GlobalContextRequirement;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.LoopLifter;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.SerializerFactory;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.MutableNodeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.QueryModule;
import net.sf.saxon.query.UpdateAgent;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trace.TraceableComponent;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.tree.iter.ManualIterator;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;

public class XQueryExpression
implements Location,
ExpressionOwner,
TraceableComponent {
    protected Expression expression;
    protected SlotManager stackFrameMap;
    protected Executable executable;
    protected QueryModule mainModule;

    public XQueryExpression(Expression exp, QueryModule mainModule, boolean streaming) throws XPathException {
        Executable exec = mainModule.getExecutable();
        Configuration config = mainModule.getConfiguration();
        this.stackFrameMap = config.makeSlotManager();
        this.executable = exec;
        this.mainModule = mainModule;
        exp.setRetainedStaticContext(mainModule.makeRetainedStaticContext());
        try {
            ExpressionVisitor visitor = ExpressionVisitor.make(mainModule);
            Optimizer optimizer = visitor.obtainOptimizer();
            visitor.setOptimizeForStreaming(streaming);
            exp = exp.simplify();
            exp.checkForUpdatingSubexpressions();
            GlobalContextRequirement contextReq = exec.getGlobalContextRequirement();
            ItemType req = contextReq == null ? AnyItemType.getInstance() : contextReq.getRequiredItemType();
            ContextItemStaticInfo cit = config.makeContextItemStaticInfo(req, true);
            Expression e2 = exp.typeCheck(visitor, cit);
            if (e2 != exp) {
                e2.setRetainedStaticContext(exp.getRetainedStaticContext());
                e2.setParentExpression(null);
                exp = e2;
            }
            if (optimizer.isOptionSet(256) && (e2 = exp.optimize(visitor, cit)) != exp) {
                e2.setRetainedStaticContext(exp.getRetainedStaticContext());
                e2.setParentExpression(null);
                exp = e2;
            }
            if (optimizer.isOptionSet(1) && (e2 = LoopLifter.process(exp, visitor, cit)) != exp) {
                e2.setRetainedStaticContext(exp.getRetainedStaticContext());
                e2.setParentExpression(null);
                exp = e2;
            }
        } catch (XPathException err) {
            mainModule.reportStaticError(err);
            throw err;
        }
        ExpressionTool.allocateSlots(exp, 0, this.stackFrameMap);
        ExpressionTool.computeEvaluationModesForUserFunctionCalls(exp);
        for (GlobalVariable var : this.getPackageData().getGlobalVariableList()) {
            Expression top = var.getBody();
            if (top == null) continue;
            ExpressionTool.computeEvaluationModesForUserFunctionCalls(top);
        }
        this.expression = exp;
        this.executable.setConfiguration(config);
    }

    public Expression getExpression() {
        return this.expression;
    }

    @Override
    public Expression getBody() {
        return this.getExpression();
    }

    @Override
    public Expression getChildExpression() {
        return this.expression;
    }

    @Override
    public void setBody(Expression expression) {
        this.setChildExpression(expression);
    }

    @Override
    public StructuredQName getObjectName() {
        return null;
    }

    @Override
    public String getTracingTag() {
        return "query";
    }

    @Override
    public Location getLocation() {
        return this;
    }

    public PackageData getPackageData() {
        return this.mainModule.getPackageData();
    }

    public Configuration getConfiguration() {
        return this.mainModule.getConfiguration();
    }

    public boolean usesContextItem() {
        if (ExpressionTool.dependsOnFocus(this.expression)) {
            return true;
        }
        List<GlobalVariable> map = this.getPackageData().getGlobalVariableList();
        if (map != null) {
            for (GlobalVariable var : map) {
                Expression select = var.getBody();
                if (select == null || !ExpressionTool.dependsOnFocus(select)) continue;
                return true;
            }
        }
        return false;
    }

    public boolean isUpdateQuery() {
        return false;
    }

    public SlotManager getStackFrameMap() {
        return this.stackFrameMap;
    }

    public void explainPathMap() {
    }

    public QueryModule getMainModule() {
        return this.mainModule;
    }

    public StructuredQName[] getExternalVariableNames() {
        List<StructuredQName> list = this.stackFrameMap.getVariableMap();
        StructuredQName[] names = new StructuredQName[this.stackFrameMap.getNumberOfVariables()];
        for (int i = 0; i < names.length; ++i) {
            names[i] = list.get(i);
        }
        return names;
    }

    public List<Object> evaluate(DynamicQueryContext env) throws XPathException {
        if (this.isUpdateQuery()) {
            throw new XPathException("Cannot call evaluate() on an updating query");
        }
        ArrayList<Object> list = new ArrayList<Object>(100);
        this.iterator(env).forEachOrFail(item -> list.add(SequenceTool.convertToJava(item)));
        return list;
    }

    public Object evaluateSingle(DynamicQueryContext env) throws XPathException {
        if (this.isUpdateQuery()) {
            throw new XPathException("Cannot call evaluateSingle() on an updating query");
        }
        SequenceIterator iterator = this.iterator(env);
        Item item = iterator.next();
        if (item == null) {
            return null;
        }
        return SequenceTool.convertToJava(item);
    }

    public SequenceIterator iterator(DynamicQueryContext env) throws XPathException {
        if (this.isUpdateQuery()) {
            throw new XPathException("Cannot call iterator() on an updating query");
        }
        if (!env.getConfiguration().isCompatible(this.getExecutable().getConfiguration())) {
            throw new XPathException("The query must be compiled and executed under the same Configuration", "SXXP0004");
        }
        Controller controller = this.newController(env);
        try {
            Item contextItem = controller.getGlobalContextItem();
            if (contextItem instanceof NodeInfo && ((NodeInfo)contextItem).getTreeInfo().isTyped() && !this.getExecutable().isSchemaAware()) {
                throw new XPathException("A typed input document can only be used with a schema-aware query");
            }
            XPathContextMajor context = this.initialContext(env, controller);
            if (controller.getTraceListener() != null) {
                controller.preEvaluateGlobals(context);
            }
            context.openStackFrame(this.stackFrameMap);
            SequenceIterator iterator = this.expression.iterate(context);
            if (iterator.getProperties().contains((Object)SequenceIterator.Property.GROUNDED)) {
                return iterator;
            }
            return new ErrorReportingIterator(iterator, controller.getErrorReporter());
        } catch (XPathException err) {
            TransformerException terr = err;
            while (terr.getException() instanceof TransformerException) {
                terr = (TransformerException)terr.getException();
            }
            XPathException de = XPathException.makeXPathException(terr);
            controller.reportFatalError(de);
            throw de;
        }
    }

    public void run(DynamicQueryContext env, Result result, Properties outputProperties) throws XPathException {
        Receiver out;
        boolean mustClose;
        if (this.isUpdateQuery()) {
            throw new XPathException("Cannot call run() on an updating query");
        }
        if (!env.getConfiguration().isCompatible(this.getExecutable().getConfiguration())) {
            throw new XPathException("The query must be compiled and executed under the same Configuration", "SXXP0004");
        }
        Item contextItem = env.getContextItem();
        if (contextItem instanceof NodeInfo && ((NodeInfo)contextItem).getTreeInfo().isTyped() && !this.getExecutable().isSchemaAware()) {
            throw new XPathException("A typed input document can only be used with a schema-aware query");
        }
        Controller controller = this.newController(env);
        if (result instanceof Receiver) {
            ((Receiver)result).getPipelineConfiguration().setController(controller);
        }
        Properties actualProperties = this.validateOutputProperties(controller, outputProperties);
        XPathContextMajor context = this.initialContext(env, controller);
        TraceListener tracer = controller.getTraceListener();
        if (tracer != null) {
            controller.preEvaluateGlobals(context);
        }
        context.openStackFrame(this.stackFrameMap);
        boolean bl = mustClose = result instanceof StreamResult && ((StreamResult)result).getOutputStream() == null;
        if (result instanceof Receiver) {
            out = (Receiver)result;
        } else {
            SerializerFactory sf = context.getConfiguration().getSerializerFactory();
            PipelineConfiguration pipe = controller.makePipelineConfiguration();
            pipe.setHostLanguage(HostLanguage.XQUERY);
            out = sf.getReceiver(result, new SerializationProperties(actualProperties), pipe);
        }
        ComplexContentOutputter dest = new ComplexContentOutputter(out);
        dest.open();
        try {
            this.expression.process(dest, context);
        } catch (XPathException err) {
            controller.reportFatalError(err);
            throw err;
        } finally {
            try {
                if (tracer != null) {
                    tracer.close();
                }
                dest.close();
            } catch (XPathException e) {
                e.printStackTrace();
            }
        }
        if (result instanceof StreamResult) {
            this.closeStreamIfNecessary((StreamResult)result, mustClose);
        }
    }

    protected void closeStreamIfNecessary(StreamResult result, boolean mustClose) throws XPathException {
        OutputStream os;
        if (mustClose && (os = result.getOutputStream()) != null) {
            try {
                os.close();
            } catch (IOException err) {
                throw new XPathException(err);
            }
        }
    }

    public void runStreamed(DynamicQueryContext dynamicEnv, Source source, Result result, Properties outputProperties) throws XPathException {
        throw new XPathException("Streaming requires Saxon-EE");
    }

    protected Properties validateOutputProperties(Controller controller, Properties outputProperties) {
        Properties baseProperties = controller.getExecutable().getPrimarySerializationProperties().getProperties();
        SerializerFactory sf = controller.getConfiguration().getSerializerFactory();
        if (outputProperties != null) {
            Enumeration<?> iter = outputProperties.propertyNames();
            while (iter.hasMoreElements()) {
                String key = (String)iter.nextElement();
                String value = outputProperties.getProperty(key);
                try {
                    value = sf.checkOutputProperty(key, value);
                    baseProperties.setProperty(key, value);
                } catch (XPathException dynamicError) {
                    outputProperties.remove(key);
                    XmlProcessingException err = new XmlProcessingException(dynamicError);
                    err.setWarning(true);
                    controller.getErrorReporter().report(err);
                }
            }
        }
        if (baseProperties.getProperty("method") == null) {
            baseProperties.setProperty("method", "xml");
        }
        return baseProperties;
    }

    public Set<MutableNodeInfo> runUpdate(DynamicQueryContext dynamicEnv) throws XPathException {
        throw new XPathException("Calling runUpdate() on a non-updating query");
    }

    public void runUpdate(DynamicQueryContext dynamicEnv, UpdateAgent agent) throws XPathException {
        throw new XPathException("Calling runUpdate() on a non-updating query");
    }

    protected XPathContextMajor initialContext(DynamicQueryContext dynamicEnv, Controller controller) throws XPathException {
        Item contextItem = controller.getGlobalContextItem();
        XPathContextMajor context = controller.newXPathContext();
        if (contextItem != null) {
            ManualIterator single = new ManualIterator(contextItem);
            context.setCurrentIterator(single);
            controller.setGlobalContextItem(contextItem);
        }
        return context;
    }

    public Controller newController(DynamicQueryContext env) throws XPathException {
        Controller controller = new Controller(this.executable.getConfiguration(), this.executable);
        env.initializeController(controller);
        return controller;
    }

    public void explain(ExpressionPresenter out) throws XPathException {
        out.startElement("query");
        this.mainModule.getKeyManager().exportKeys(out, null);
        this.getExecutable().explainGlobalVariables(out);
        this.mainModule.explainGlobalFunctions(out);
        out.startElement("body");
        this.expression.export(out);
        out.endElement();
        out.endElement();
        out.close();
    }

    public Executable getExecutable() {
        return this.executable;
    }

    public void setAllowDocumentProjection(boolean allowed) {
        if (allowed) {
            throw new UnsupportedOperationException("Document projection requires Saxon-EE");
        }
    }

    public boolean isDocumentProjectionAllowed() {
        return false;
    }

    @Override
    public String getPublicId() {
        return null;
    }

    @Override
    public String getSystemId() {
        return this.mainModule.getSystemId();
    }

    @Override
    public int getLineNumber() {
        return -1;
    }

    @Override
    public int getColumnNumber() {
        return -1;
    }

    @Override
    public Location saveLocation() {
        return this;
    }

    public HostLanguage getHostLanguage() {
        return HostLanguage.XQUERY;
    }

    @Override
    public void setChildExpression(Expression expr) {
        this.expression = expr;
    }

    private class ErrorReportingIterator
    implements SequenceIterator {
        private SequenceIterator base;
        private ErrorReporter reporter;

        public ErrorReportingIterator(SequenceIterator base, ErrorReporter reporter) {
            this.base = base;
            this.reporter = reporter;
        }

        @Override
        public Item next() throws XPathException {
            try {
                return this.base.next();
            } catch (XPathException e1) {
                e1.maybeSetLocation(XQueryExpression.this.expression.getLocation());
                XmlProcessingException err = new XmlProcessingException(e1);
                this.reporter.report(err);
                e1.setHasBeenReported(true);
                throw e1;
            }
        }

        @Override
        public void close() {
            this.base.close();
        }
    }
}


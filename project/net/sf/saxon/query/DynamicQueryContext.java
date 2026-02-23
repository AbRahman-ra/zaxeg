/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.query;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.URIResolver;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.instruct.GlobalParameterSet;
import net.sf.saxon.functions.AccessorFn;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.ErrorReporterToListener;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.StandardErrorReporter;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.DateTimeValue;

public class DynamicQueryContext {
    private Item contextItem;
    private GlobalParameterSet parameters = new GlobalParameterSet();
    private Configuration config;
    private URIResolver uriResolver;
    private ErrorReporter errorReporter;
    private TraceListener traceListener;
    private UnparsedTextURIResolver unparsedTextURIResolver;
    private DateTimeValue currentDateTime;
    private Logger traceFunctionDestination;
    private int validationMode = 0;
    private boolean applyConversionRules = true;

    public DynamicQueryContext(Configuration config) {
        this.config = config;
        this.uriResolver = config.getURIResolver();
        this.errorReporter = new StandardErrorReporter();
        this.traceFunctionDestination = config.getLogger();
    }

    public int getSchemaValidationMode() {
        return this.validationMode;
    }

    public void setSchemaValidationMode(int validationMode) {
        this.validationMode = validationMode;
    }

    public void setApplyFunctionConversionRulesToExternalVariables(boolean convert) {
        this.applyConversionRules = convert;
    }

    public boolean isApplyFunctionConversionRulesToExternalVariables() {
        return this.applyConversionRules;
    }

    public void setContextItem(Item item) {
        if (item == null) {
            throw new NullPointerException("Context item cannot be null");
        }
        if (item instanceof NodeInfo && !((NodeInfo)item).getConfiguration().isCompatible(this.config)) {
            throw new IllegalArgumentException("Supplied node must be built using the same or a compatible Configuration");
        }
        this.contextItem = item;
    }

    public Item getContextItem() {
        return this.contextItem;
    }

    public void setParameter(StructuredQName expandedName, GroundedValue value) {
        if (this.parameters == null) {
            this.parameters = new GlobalParameterSet();
        }
        this.parameters.put(expandedName, value);
    }

    public void clearParameters() {
        this.parameters = new GlobalParameterSet();
    }

    public GroundedValue getParameter(StructuredQName expandedName) {
        if (this.parameters == null) {
            return null;
        }
        return this.parameters.get(expandedName);
    }

    public GlobalParameterSet getParameters() {
        if (this.parameters == null) {
            return new GlobalParameterSet();
        }
        return this.parameters;
    }

    public void setURIResolver(URIResolver resolver) {
        this.uriResolver = resolver;
    }

    public URIResolver getURIResolver() {
        return this.uriResolver;
    }

    public void setUnparsedTextURIResolver(UnparsedTextURIResolver resolver) {
        this.unparsedTextURIResolver = resolver;
    }

    public UnparsedTextURIResolver getUnparsedTextURIResolver() {
        return this.unparsedTextURIResolver;
    }

    public void setErrorListener(ErrorListener listener) {
        this.errorReporter = new ErrorReporterToListener(listener);
    }

    public ErrorListener getErrorListener() {
        ErrorReporter uel = this.getErrorReporter();
        if (uel instanceof ErrorReporterToListener) {
            return ((ErrorReporterToListener)uel).getErrorListener();
        }
        return null;
    }

    public void setErrorReporter(ErrorReporter reporter) {
        this.errorReporter = reporter;
    }

    public ErrorReporter getErrorReporter() {
        return this.errorReporter;
    }

    public void setTraceListener(TraceListener listener) {
        this.traceListener = listener;
    }

    public TraceListener getTraceListener() {
        return this.traceListener;
    }

    public void setTraceFunctionDestination(Logger stream) {
        this.traceFunctionDestination = stream;
    }

    public Logger getTraceFunctionDestination() {
        return this.traceFunctionDestination;
    }

    public DateTimeValue getCurrentDateTime() {
        return this.currentDateTime;
    }

    public void setCurrentDateTime(DateTimeValue dateTime) throws XPathException {
        this.currentDateTime = dateTime;
        if (dateTime.getComponent(AccessorFn.Component.TIMEZONE) == null) {
            throw new XPathException("Supplied date/time must include a timezone");
        }
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    public void initializeController(Controller controller) throws XPathException {
        controller.setURIResolver(this.getURIResolver());
        controller.setErrorReporter(this.getErrorReporter());
        controller.addTraceListener(this.getTraceListener());
        if (this.unparsedTextURIResolver != null) {
            controller.setUnparsedTextURIResolver(this.unparsedTextURIResolver);
        }
        controller.setTraceFunctionDestination(this.getTraceFunctionDestination());
        controller.setSchemaValidationMode(this.getSchemaValidationMode());
        DateTimeValue currentDateTime = this.getCurrentDateTime();
        if (currentDateTime != null) {
            try {
                controller.setCurrentDateTime(currentDateTime);
            } catch (XPathException e) {
                throw new AssertionError((Object)e);
            }
        }
        controller.setGlobalContextItem(this.contextItem);
        controller.initializeController(this.parameters);
        controller.setApplyFunctionConversionRulesToExternalVariables(this.applyConversionRules);
    }
}


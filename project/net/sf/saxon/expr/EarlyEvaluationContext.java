/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.Collections;
import java.util.Iterator;
import javax.xml.transform.URIResolver;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.StackFrame;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.XPathContextMinor;
import net.sf.saxon.expr.instruct.ParameterSet;
import net.sf.saxon.expr.sort.GroupIterator;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.om.FocusIterator;
import net.sf.saxon.om.FocusTrackingIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.regex.RegexIterator;
import net.sf.saxon.trans.NoDynamicContextException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.Rule;
import net.sf.saxon.value.DateTimeValue;

public class EarlyEvaluationContext
implements XPathContext {
    private Configuration config;

    public EarlyEvaluationContext(Configuration config) {
        this.config = config;
    }

    @Override
    public Sequence evaluateLocalVariable(int slotnumber) {
        this.notAllowed();
        return null;
    }

    @Override
    public XPathContext getCaller() {
        return null;
    }

    @Override
    public URIResolver getURIResolver() {
        return this.config.getURIResolver();
    }

    @Override
    public ErrorReporter getErrorReporter() {
        return this.config.makeErrorReporter();
    }

    @Override
    public Component getCurrentComponent() {
        this.notAllowed();
        return null;
    }

    @Override
    public Configuration getConfiguration() {
        return this.config;
    }

    @Override
    public Item getContextItem() {
        return null;
    }

    @Override
    public Controller getController() {
        return null;
    }

    @Override
    public GroupIterator getCurrentGroupIterator() {
        this.notAllowed();
        return null;
    }

    @Override
    public GroupIterator getCurrentMergeGroupIterator() {
        this.notAllowed();
        return null;
    }

    @Override
    public FocusTrackingIterator getCurrentIterator() {
        return null;
    }

    @Override
    public Component.M getCurrentMode() {
        this.notAllowed();
        return null;
    }

    @Override
    public RegexIterator getCurrentRegexIterator() {
        return null;
    }

    @Override
    public Rule getCurrentTemplateRule() {
        return null;
    }

    @Override
    public int getLast() throws XPathException {
        XPathException err = new XPathException("The context item is absent");
        err.setErrorCode("XPDY0002");
        throw err;
    }

    @Override
    public ParameterSet getLocalParameters() {
        this.notAllowed();
        return null;
    }

    @Override
    public NamePool getNamePool() {
        return this.config.getNamePool();
    }

    @Override
    public StackFrame getStackFrame() {
        this.notAllowed();
        return null;
    }

    @Override
    public ParameterSet getTunnelParameters() {
        this.notAllowed();
        return null;
    }

    @Override
    public boolean isAtLast() throws XPathException {
        XPathException err = new XPathException("The context item is absent");
        err.setErrorCode("XPDY0002");
        throw err;
    }

    @Override
    public XPathContextMajor newCleanContext() {
        this.notAllowed();
        return null;
    }

    @Override
    public XPathContextMajor newContext() {
        Controller controller = new Controller(this.config);
        return controller.newXPathContext();
    }

    @Override
    public XPathContextMinor newMinorContext() {
        return this.newContext().newMinorContext();
    }

    @Override
    public void setCaller(XPathContext caller) {
    }

    @Override
    public void setCurrentIterator(FocusIterator iter) {
        this.notAllowed();
    }

    @Override
    public FocusIterator trackFocus(SequenceIterator iter) {
        this.notAllowed();
        return null;
    }

    @Override
    public void setLocalVariable(int slotNumber, Sequence value) {
        this.notAllowed();
    }

    @Override
    public int useLocalParameter(StructuredQName parameterId, int slotNumber, boolean isTunnel) {
        return 0;
    }

    @Override
    public DateTimeValue getCurrentDateTime() throws NoDynamicContextException {
        throw new NoDynamicContextException("current-dateTime");
    }

    @Override
    public int getImplicitTimezone() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Iterator iterateStackFrames() {
        return Collections.EMPTY_LIST.iterator();
    }

    @Override
    public XPathException getCurrentException() {
        return null;
    }

    @Override
    public void waitForChildThreads() throws XPathException {
        this.getCaller().waitForChildThreads();
    }

    @Override
    public void setTemporaryOutputState(int temporary) {
    }

    @Override
    public int getTemporaryOutputState() {
        return 0;
    }

    @Override
    public void setCurrentOutputUri(String uri) {
    }

    @Override
    public String getCurrentOutputUri() {
        return null;
    }

    private void notAllowed() {
        throw new UnsupportedOperationException(new NoDynamicContextException("Internal error: early evaluation of subexpression with no context"));
    }

    @Override
    public XPathContextMajor.ThreadManager getThreadManager() {
        return null;
    }

    @Override
    public Component getTargetComponent(int bindingSlot) {
        return null;
    }
}


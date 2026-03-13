/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.Iterator;
import java.util.function.Function;
import javax.xml.transform.URIResolver;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.StackFrame;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.ParameterSet;
import net.sf.saxon.expr.sort.GroupIterator;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.om.FocusIterator;
import net.sf.saxon.om.FocusTrackingIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.regex.RegexIterator;
import net.sf.saxon.trace.ContextStackIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.Rule;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.value.DateTimeValue;

public class XPathContextMinor
implements XPathContext {
    Controller controller;
    FocusIterator currentIterator;
    LastValue last = null;
    XPathContext caller = null;
    protected StackFrame stackFrame;
    protected String currentDestination = "";
    protected int temporaryOutputState = 0;

    protected XPathContextMinor() {
    }

    @Override
    public XPathContextMajor newContext() {
        return XPathContextMajor.newContext(this);
    }

    @Override
    public XPathContextMinor newMinorContext() {
        XPathContextMinor c = new XPathContextMinor();
        c.controller = this.controller;
        c.caller = this;
        c.currentIterator = this.currentIterator;
        c.last = this.last;
        c.stackFrame = this.stackFrame;
        c.currentDestination = this.currentDestination;
        c.temporaryOutputState = this.temporaryOutputState;
        return c;
    }

    @Override
    public void setCaller(XPathContext caller) {
        this.caller = caller;
    }

    @Override
    public XPathContextMajor newCleanContext() {
        XPathContextMajor c = new XPathContextMajor(this.getController());
        c.setCaller(this);
        return c;
    }

    @Override
    public ParameterSet getLocalParameters() {
        return this.getCaller().getLocalParameters();
    }

    @Override
    public ParameterSet getTunnelParameters() {
        return this.getCaller().getTunnelParameters();
    }

    @Override
    public final Controller getController() {
        return this.controller;
    }

    @Override
    public final Configuration getConfiguration() {
        return this.controller.getConfiguration();
    }

    @Override
    public final NamePool getNamePool() {
        return this.controller.getConfiguration().getNamePool();
    }

    @Override
    public final XPathContext getCaller() {
        return this.caller;
    }

    @Override
    public void setCurrentIterator(FocusIterator iter) {
        this.currentIterator = iter;
        this.last = new LastValue(-1);
    }

    @Override
    public FocusIterator trackFocus(SequenceIterator iter) {
        Function<SequenceIterator, FocusTrackingIterator> factory = this.controller.getFocusTrackerFactory(false);
        FocusIterator fit = factory.apply(iter);
        this.setCurrentIterator(fit);
        return fit;
    }

    public FocusIterator trackFocusMultithreaded(SequenceIterator iter) {
        Function<SequenceIterator, FocusTrackingIterator> factory = this.controller.getFocusTrackerFactory(true);
        FocusIterator fit = factory.apply(iter);
        this.setCurrentIterator(fit);
        return fit;
    }

    @Override
    public final FocusIterator getCurrentIterator() {
        return this.currentIterator;
    }

    @Override
    public final Item getContextItem() {
        if (this.currentIterator == null) {
            return null;
        }
        return this.currentIterator.current();
    }

    @Override
    public final int getLast() throws XPathException {
        if (this.currentIterator == null) {
            XPathException e = new XPathException("The context item is absent, so last() is undefined");
            e.setXPathContext(this);
            e.setErrorCode("XPDY0002");
            throw e;
        }
        if (this.last.value >= 0) {
            return this.last.value;
        }
        return this.currentIterator.getLength();
    }

    @Override
    public final boolean isAtLast() throws XPathException {
        if (this.currentIterator.getProperties().contains((Object)SequenceIterator.Property.LOOKAHEAD)) {
            return !((LookaheadIterator)((Object)this.currentIterator)).hasNext();
        }
        return this.currentIterator.position() == this.getLast();
    }

    @Override
    public URIResolver getURIResolver() {
        return this.caller.getURIResolver();
    }

    @Override
    public ErrorReporter getErrorReporter() {
        return this.caller.getErrorReporter();
    }

    @Override
    public XPathException getCurrentException() {
        return this.caller.getCurrentException();
    }

    @Override
    public XPathContextMajor.ThreadManager getThreadManager() {
        return this.caller.getThreadManager();
    }

    @Override
    public Component getCurrentComponent() {
        return this.caller.getCurrentComponent();
    }

    @Override
    public StackFrame getStackFrame() {
        return this.stackFrame;
    }

    public void makeStackFrameMutable() {
        if (this.stackFrame == StackFrame.EMPTY) {
            this.stackFrame = new StackFrame(null, SequenceTool.makeSequenceArray(0));
        }
    }

    @Override
    public final Sequence evaluateLocalVariable(int slotnumber) {
        return this.stackFrame.slots[slotnumber];
    }

    @Override
    public final void setLocalVariable(int slotNumber, Sequence value) throws XPathException {
        value = value.makeRepeatable();
        try {
            this.stackFrame.slots[slotNumber] = value;
        } catch (ArrayIndexOutOfBoundsException e) {
            if (slotNumber == -999) {
                throw new AssertionError((Object)"Internal error: Cannot set local variable: no slot allocated");
            }
            throw new AssertionError((Object)("Internal error: Cannot set local variable (slot " + slotNumber + " of " + this.getStackFrame().getStackFrameValues().length + ")"));
        }
    }

    @Override
    public synchronized void waitForChildThreads() throws XPathException {
        this.getCaller().waitForChildThreads();
    }

    @Override
    public void setTemporaryOutputState(int temporary) {
        this.temporaryOutputState = temporary;
    }

    @Override
    public int getTemporaryOutputState() {
        return this.temporaryOutputState;
    }

    @Override
    public void setCurrentOutputUri(String uri) {
        this.currentDestination = uri;
    }

    @Override
    public String getCurrentOutputUri() {
        return this.currentDestination;
    }

    @Override
    public int useLocalParameter(StructuredQName parameterId, int slotNumber, boolean isTunnel) throws XPathException {
        return this.getCaller().useLocalParameter(parameterId, slotNumber, isTunnel);
    }

    @Override
    public Component.M getCurrentMode() {
        return this.getCaller().getCurrentMode();
    }

    @Override
    public Rule getCurrentTemplateRule() {
        return null;
    }

    @Override
    public GroupIterator getCurrentGroupIterator() {
        return this.getCaller().getCurrentGroupIterator();
    }

    @Override
    public GroupIterator getCurrentMergeGroupIterator() {
        return this.getCaller().getCurrentMergeGroupIterator();
    }

    @Override
    public RegexIterator getCurrentRegexIterator() {
        return this.getCaller().getCurrentRegexIterator();
    }

    @Override
    public DateTimeValue getCurrentDateTime() {
        return this.controller.getCurrentDateTime();
    }

    @Override
    public final int getImplicitTimezone() {
        return this.controller.getImplicitTimezone();
    }

    @Override
    public Iterator iterateStackFrames() {
        return new ContextStackIterator(this);
    }

    @Override
    public Component getTargetComponent(int bindingSlot) {
        return this.getCaller().getTargetComponent(bindingSlot);
    }

    protected static class LastValue {
        public int value = 0;

        public LastValue(int count) {
            this.value = count;
        }
    }
}


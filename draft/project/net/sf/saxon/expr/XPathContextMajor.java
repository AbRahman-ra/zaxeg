/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.Arrays;
import javax.xml.transform.URIResolver;
import net.sf.saxon.Controller;
import net.sf.saxon.PreparedStylesheet;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.ComponentBinding;
import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.StackFrame;
import net.sf.saxon.expr.TailCallLoop;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMinor;
import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.instruct.ParameterSet;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.sort.GroupIterator;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.StandardURIResolver;
import net.sf.saxon.om.FocusTrackingIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.regex.RegexIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XsltController;
import net.sf.saxon.trans.rules.Rule;
import net.sf.saxon.trans.rules.RuleManager;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.tree.iter.UnfailingIterator;

public class XPathContextMajor
extends XPathContextMinor {
    private ParameterSet localParameters;
    private ParameterSet tunnelParameters;
    private TailCallLoop.TailCallInfo tailCallInfo;
    private Component.M currentMode;
    private Rule currentTemplate;
    private GroupIterator currentGroupIterator;
    private GroupIterator currentMergeGroupIterator;
    private RegexIterator currentRegexIterator;
    private ContextOriginator origin;
    private ThreadManager threadManager = null;
    private URIResolver uriResolver;
    private ErrorReporter errorReporter;
    private Component currentComponent;
    XPathException currentException;

    public XPathContextMajor(Controller controller) {
        this.controller = controller;
        this.stackFrame = StackFrame.EMPTY;
        this.origin = controller;
    }

    private XPathContextMajor() {
    }

    public XPathContextMajor(Item item, Executable exec) {
        Controller controller = this.controller = exec instanceof PreparedStylesheet ? new XsltController(exec.getConfiguration(), (PreparedStylesheet)exec) : new Controller(exec.getConfiguration(), exec);
        if (item != null) {
            UnfailingIterator iter = SingletonIterator.makeIterator(item);
            this.currentIterator = new FocusTrackingIterator(iter);
            try {
                this.currentIterator.next();
            } catch (XPathException xPathException) {
                // empty catch block
            }
            this.last = new XPathContextMinor.LastValue(1);
        }
        this.origin = this.controller;
    }

    @Override
    public XPathContextMajor newContext() {
        XPathContextMajor c = new XPathContextMajor();
        c.controller = this.controller;
        c.currentIterator = this.currentIterator;
        c.stackFrame = this.stackFrame;
        c.localParameters = this.localParameters;
        c.tunnelParameters = this.tunnelParameters;
        c.last = this.last;
        c.currentDestination = this.currentDestination;
        c.currentMode = this.currentMode;
        c.currentTemplate = this.currentTemplate;
        c.currentRegexIterator = this.currentRegexIterator;
        c.currentGroupIterator = this.currentGroupIterator;
        c.currentMergeGroupIterator = this.currentMergeGroupIterator;
        c.currentException = this.currentException;
        c.caller = this;
        c.tailCallInfo = null;
        c.temporaryOutputState = this.temporaryOutputState;
        c.threadManager = this.threadManager;
        c.currentComponent = this.currentComponent;
        c.errorReporter = this.errorReporter;
        c.uriResolver = this.uriResolver;
        return c;
    }

    public static XPathContextMajor newContext(XPathContextMinor prev) {
        XPathContextMajor c = new XPathContextMajor();
        XPathContext p = prev;
        while (!(p instanceof XPathContextMajor)) {
            p = p.getCaller();
        }
        c.controller = p.getController();
        c.currentIterator = prev.getCurrentIterator();
        c.stackFrame = prev.getStackFrame();
        c.localParameters = p.getLocalParameters();
        c.tunnelParameters = p.getTunnelParameters();
        c.last = prev.last;
        c.currentDestination = prev.currentDestination;
        c.currentMode = p.getCurrentMode();
        c.currentTemplate = p.getCurrentTemplateRule();
        c.currentRegexIterator = p.getCurrentRegexIterator();
        c.currentGroupIterator = p.getCurrentGroupIterator();
        c.currentMergeGroupIterator = p.getCurrentMergeGroupIterator();
        c.caller = prev;
        c.tailCallInfo = null;
        c.threadManager = ((XPathContextMajor)p).threadManager;
        c.currentComponent = ((XPathContextMajor)p).currentComponent;
        c.errorReporter = ((XPathContextMajor)p).errorReporter;
        c.uriResolver = ((XPathContextMajor)p).uriResolver;
        c.temporaryOutputState = prev.temporaryOutputState;
        return c;
    }

    public static XPathContextMajor newThreadContext(XPathContextMinor prev) {
        XPathContextMajor c = XPathContextMajor.newContext(prev);
        c.stackFrame = prev.stackFrame.copy();
        return c;
    }

    @Override
    public ThreadManager getThreadManager() {
        return this.threadManager;
    }

    public void createThreadManager() {
        this.threadManager = this.getConfiguration().makeThreadManager();
    }

    @Override
    public void waitForChildThreads() throws XPathException {
        if (this.threadManager != null) {
            this.threadManager.waitForChildThreads();
        }
    }

    @Override
    public ParameterSet getLocalParameters() {
        if (this.localParameters == null) {
            this.localParameters = new ParameterSet();
        }
        return this.localParameters;
    }

    public void setLocalParameters(ParameterSet localParameters) {
        this.localParameters = localParameters;
    }

    @Override
    public ParameterSet getTunnelParameters() {
        return this.tunnelParameters;
    }

    public void setTunnelParameters(ParameterSet tunnelParameters) {
        this.tunnelParameters = tunnelParameters;
    }

    public void setOrigin(ContextOriginator expr) {
        this.origin = expr;
    }

    public ContextOriginator getOrigin() {
        return this.origin;
    }

    public void setStackFrame(SlotManager map, Sequence[] variables) {
        this.stackFrame = new StackFrame(map, variables);
        if (map != null && variables.length != map.getNumberOfVariables()) {
            if (variables.length > map.getNumberOfVariables()) {
                throw new IllegalStateException("Attempting to set more local variables (" + variables.length + ") than the stackframe can accommodate (" + map.getNumberOfVariables() + ")");
            }
            this.stackFrame.slots = new Sequence[map.getNumberOfVariables()];
            System.arraycopy(variables, 0, this.stackFrame.slots, 0, variables.length);
        }
    }

    public void resetStackFrameMap(SlotManager map, int numberOfParams) {
        this.stackFrame.map = map;
        if (this.stackFrame.slots.length != map.getNumberOfVariables()) {
            Sequence[] v2 = new Sequence[map.getNumberOfVariables()];
            System.arraycopy(this.stackFrame.slots, 0, v2, 0, numberOfParams);
            this.stackFrame.slots = v2;
        } else {
            Arrays.fill(this.stackFrame.slots, numberOfParams, this.stackFrame.slots.length, null);
        }
    }

    public Sequence[] getAllVariableValues() {
        return this.stackFrame.getStackFrameValues();
    }

    public void resetAllVariableValues(Sequence[] values) {
        this.stackFrame.setStackFrameValues(values);
    }

    public void resetParameterValues(Sequence[] values) {
        System.arraycopy(values, 0, this.stackFrame.slots, 0, values.length);
    }

    public void requestTailCall(TailCallLoop.TailCallInfo targetFn, Sequence[] variables) {
        if (variables != null) {
            if (variables.length > this.stackFrame.slots.length) {
                this.stackFrame.slots = Arrays.copyOf(variables, variables.length);
            } else {
                System.arraycopy(variables, 0, this.stackFrame.slots, 0, variables.length);
            }
        }
        this.tailCallInfo = targetFn;
    }

    public TailCallLoop.TailCallInfo getTailCallInfo() {
        TailCallLoop.TailCallInfo fn = this.tailCallInfo;
        this.tailCallInfo = null;
        return fn;
    }

    public void openStackFrame(SlotManager map) {
        int numberOfSlots = map.getNumberOfVariables();
        this.stackFrame = numberOfSlots == 0 ? StackFrame.EMPTY : new StackFrame(map, new Sequence[numberOfSlots]);
    }

    public void openStackFrame(int numberOfVariables) {
        this.stackFrame = new StackFrame(new SlotManager(numberOfVariables), SequenceTool.makeSequenceArray(numberOfVariables));
    }

    public void setCurrentMode(Component.M mode) {
        this.currentMode = mode;
    }

    @Override
    public Component.M getCurrentMode() {
        Component.M m = this.currentMode;
        if (m == null) {
            RuleManager rm = this.getController().getRuleManager();
            if (rm != null) {
                return rm.getUnnamedMode().getDeclaringComponent();
            }
            return null;
        }
        return m;
    }

    public void setCurrentTemplateRule(Rule rule) {
        this.currentTemplate = rule;
    }

    @Override
    public Rule getCurrentTemplateRule() {
        return this.currentTemplate;
    }

    public void setCurrentGroupIterator(GroupIterator iterator) {
        this.currentGroupIterator = iterator;
    }

    @Override
    public GroupIterator getCurrentGroupIterator() {
        return this.currentGroupIterator;
    }

    public void setCurrentMergeGroupIterator(GroupIterator iterator) {
        this.currentMergeGroupIterator = iterator;
    }

    @Override
    public GroupIterator getCurrentMergeGroupIterator() {
        return this.currentMergeGroupIterator;
    }

    public void setCurrentRegexIterator(RegexIterator currentRegexIterator) {
        this.currentRegexIterator = currentRegexIterator;
    }

    @Override
    public RegexIterator getCurrentRegexIterator() {
        return this.currentRegexIterator;
    }

    @Override
    public int useLocalParameter(StructuredQName paramName, int slotNumber, boolean isTunnel) throws XPathException {
        Sequence val;
        ParameterSet params;
        ParameterSet parameterSet = params = isTunnel ? this.getTunnelParameters() : this.localParameters;
        if (params == null) {
            return 0;
        }
        int index = params.getIndex(paramName);
        if (index < 0) {
            return 0;
        }
        this.stackFrame.slots[slotNumber] = val = params.getValue(index);
        boolean checked = params.isTypeChecked(index);
        return checked ? 2 : 1;
    }

    public void setURIResolver(URIResolver resolver) {
        this.uriResolver = resolver;
        if (resolver instanceof StandardURIResolver) {
            ((StandardURIResolver)resolver).setConfiguration(this.getConfiguration());
        }
    }

    @Override
    public URIResolver getURIResolver() {
        return this.uriResolver == null ? this.controller.getURIResolver() : this.uriResolver;
    }

    public void setErrorReporter(ErrorReporter reporter) {
        this.errorReporter = reporter;
    }

    @Override
    public ErrorReporter getErrorReporter() {
        return this.errorReporter == null ? this.controller.getErrorReporter() : this.errorReporter;
    }

    public void setCurrentException(XPathException exception) {
        this.currentException = exception;
    }

    @Override
    public XPathException getCurrentException() {
        return this.currentException;
    }

    @Override
    public Component getCurrentComponent() {
        return this.currentComponent;
    }

    public void setCurrentComponent(Component component) {
        this.currentComponent = component;
    }

    @Override
    public Component getTargetComponent(int bindingSlot) {
        try {
            ComponentBinding binding = this.currentComponent.getComponentBindings().get(bindingSlot);
            return binding.getTarget();
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw e;
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static abstract class ThreadManager {
        public abstract void waitForChildThreads() throws XPathException;
    }
}


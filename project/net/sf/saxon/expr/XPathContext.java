/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.Iterator;
import javax.xml.transform.URIResolver;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.StackFrame;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.XPathContextMinor;
import net.sf.saxon.expr.instruct.ParameterSet;
import net.sf.saxon.expr.sort.GroupIterator;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.om.FocusIterator;
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

public interface XPathContext {
    public XPathContextMajor newContext();

    public XPathContextMajor newCleanContext();

    public XPathContextMinor newMinorContext();

    public ParameterSet getLocalParameters();

    public ParameterSet getTunnelParameters();

    public Controller getController();

    public Configuration getConfiguration();

    public NamePool getNamePool();

    public void setCaller(XPathContext var1);

    public XPathContext getCaller();

    public FocusIterator trackFocus(SequenceIterator var1);

    public void setCurrentIterator(FocusIterator var1);

    public FocusIterator getCurrentIterator();

    public Item getContextItem();

    public int getLast() throws XPathException;

    public boolean isAtLast() throws XPathException;

    public URIResolver getURIResolver();

    public ErrorReporter getErrorReporter();

    public Component getCurrentComponent();

    public int useLocalParameter(StructuredQName var1, int var2, boolean var3) throws XPathException;

    public StackFrame getStackFrame();

    public Sequence evaluateLocalVariable(int var1);

    public void setLocalVariable(int var1, Sequence var2) throws XPathException;

    public void setTemporaryOutputState(int var1);

    public int getTemporaryOutputState();

    public void setCurrentOutputUri(String var1);

    public String getCurrentOutputUri();

    public Component.M getCurrentMode();

    public Rule getCurrentTemplateRule();

    public GroupIterator getCurrentGroupIterator();

    public GroupIterator getCurrentMergeGroupIterator();

    public RegexIterator getCurrentRegexIterator();

    public DateTimeValue getCurrentDateTime() throws NoDynamicContextException;

    public int getImplicitTimezone();

    public Iterator iterateStackFrames();

    public XPathException getCurrentException();

    public XPathContextMajor.ThreadManager getThreadManager();

    public void waitForChildThreads() throws XPathException;

    public Component getTargetComponent(int var1);
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.sxpath;

import javax.xml.transform.URIResolver;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.lib.CollectionFinder;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.sxpath.XPathVariable;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ManualIterator;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.SequenceType;

public class XPathDynamicContext {
    private ItemType contextItemType;
    private XPathContextMajor contextObject;
    private SlotManager stackFrameMap;

    protected XPathDynamicContext(ItemType contextItemType, XPathContextMajor contextObject, SlotManager stackFrameMap) {
        this.contextItemType = contextItemType;
        this.contextObject = contextObject;
        this.stackFrameMap = stackFrameMap;
    }

    public void setContextItem(Item item) throws XPathException {
        if (item instanceof NodeInfo && !((NodeInfo)item).getConfiguration().isCompatible(this.contextObject.getConfiguration())) {
            throw new XPathException("Supplied node must be built using the same or a compatible Configuration", "SXXP0004");
        }
        TypeHierarchy th = this.contextObject.getConfiguration().getTypeHierarchy();
        if (!this.contextItemType.matches(item, th)) {
            throw new XPathException("Supplied context item does not match required context item type " + this.contextItemType);
        }
        ManualIterator iter = new ManualIterator(item);
        this.contextObject.setCurrentIterator(iter);
    }

    public Item getContextItem() {
        return this.contextObject.getContextItem();
    }

    public void setVariable(XPathVariable variable, Sequence value) throws XPathException {
        StructuredQName expectedName;
        Item item;
        XPathException err;
        SequenceType requiredType = variable.getRequiredType();
        if (requiredType != SequenceType.ANY_SEQUENCE && (err = TypeChecker.testConformance(value, requiredType, this.contextObject)) != null) {
            throw err;
        }
        SequenceIterator iter = value.iterate();
        while ((item = iter.next()) != null) {
            if (!(item instanceof NodeInfo) || ((NodeInfo)item).getConfiguration().isCompatible(this.contextObject.getConfiguration())) continue;
            throw new XPathException("Supplied node must be built using the same or a compatible Configuration", "SXXP0004");
        }
        int slot = variable.getLocalSlotNumber();
        StructuredQName structuredQName = expectedName = slot >= this.stackFrameMap.getNumberOfVariables() ? null : this.stackFrameMap.getVariableMap().get(slot);
        if (!variable.getVariableQName().equals(expectedName)) {
            throw new XPathException("Supplied XPathVariable is bound to the wrong slot: perhaps it was created using a different static context");
        }
        this.contextObject.setLocalVariable(slot, value);
    }

    public void setURIResolver(URIResolver resolver) {
        this.contextObject.setURIResolver(resolver);
    }

    public URIResolver getURIResolver() {
        return this.contextObject.getURIResolver();
    }

    public CollectionFinder getCollectionFinder() {
        return this.contextObject.getController().getCollectionFinder();
    }

    public void setCollectionFinder(CollectionFinder cf) {
        this.contextObject.getController().setCollectionFinder(cf);
    }

    public void setErrorReporter(ErrorReporter listener) {
        this.contextObject.setErrorReporter(listener);
    }

    public ErrorReporter getErrorReporter() {
        return this.contextObject.getErrorReporter();
    }

    public XPathContext getXPathContextObject() {
        return this.contextObject;
    }

    public void setUnparsedTextURIResolver(UnparsedTextURIResolver resolver) {
        this.contextObject.getController().setUnparsedTextURIResolver(resolver);
    }

    public UnparsedTextURIResolver getUnparsedTextURIResolver() {
        return this.contextObject.getController().getUnparsedTextURIResolver();
    }

    protected void checkExternalVariables(SlotManager stackFrameMap, int numberOfExternals) throws XPathException {
        Sequence[] stack = this.contextObject.getStackFrame().getStackFrameValues();
        for (int i = 0; i < numberOfExternals; ++i) {
            if (stack[i] != null) continue;
            StructuredQName qname = stackFrameMap.getVariableMap().get(i);
            throw new XPathException("No value has been supplied for variable $" + qname.getDisplayName());
        }
    }
}


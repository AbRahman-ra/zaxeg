/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.util.Map;
import javax.xml.transform.URIResolver;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.streams.XdmStream;
import net.sf.saxon.sxpath.XPathDynamicContext;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.sxpath.XPathVariable;
import net.sf.saxon.trans.XPathException;

public class XPathSelector
implements Iterable<XdmItem> {
    private XPathExpression exp;
    private XPathDynamicContext dynamicContext;
    private Map<StructuredQName, XPathVariable> declaredVariables;

    protected XPathSelector(XPathExpression exp, Map<StructuredQName, XPathVariable> declaredVariables) {
        this.exp = exp;
        this.declaredVariables = declaredVariables;
        this.dynamicContext = exp.createDynamicContext();
    }

    public void setContextItem(XdmItem item) throws SaxonApiException {
        Item it;
        if (item == null) {
            throw new NullPointerException("contextItem");
        }
        if (!this.exp.getInternalExpression().getPackageData().isSchemaAware() && (it = item.getUnderlyingValue().head()) instanceof NodeInfo && ((NodeInfo)it).getTreeInfo().isTyped()) {
            throw new SaxonApiException("The supplied node has been schema-validated, but the XPath expression was compiled without schema-awareness");
        }
        try {
            this.dynamicContext.setContextItem(item.getUnderlyingValue());
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public XdmItem getContextItem() {
        return XdmItem.wrapItem(this.dynamicContext.getContextItem());
    }

    public void setVariable(QName name, XdmValue value) throws SaxonApiException {
        StructuredQName qn = name.getStructuredQName();
        XPathVariable var = this.declaredVariables.get(qn);
        if (var == null) {
            throw new SaxonApiException(new XPathException("Variable has not been declared: " + name));
        }
        try {
            this.dynamicContext.setVariable(var, value.getUnderlyingValue());
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public void setURIResolver(URIResolver resolver) {
        this.dynamicContext.setURIResolver(resolver);
    }

    public URIResolver getURIResolver() {
        return this.dynamicContext.getURIResolver();
    }

    public void setErrorReporter(ErrorReporter reporter) {
        this.dynamicContext.setErrorReporter(reporter);
    }

    public XdmValue evaluate() throws SaxonApiException {
        GroundedValue value;
        try {
            value = this.exp.iterate(this.dynamicContext).materialize();
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
        return XdmValue.wrap(value);
    }

    public XdmItem evaluateSingle() throws SaxonApiException {
        try {
            Item i = this.exp.evaluateSingle(this.dynamicContext);
            if (i == null) {
                return null;
            }
            return (XdmItem)XdmValue.wrap(i);
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    @Override
    public XdmSequenceIterator<XdmItem> iterator() throws SaxonApiUncheckedException {
        try {
            return new XdmSequenceIterator<XdmItem>(this.exp.iterate(this.dynamicContext));
        } catch (XPathException e) {
            throw new SaxonApiUncheckedException(e);
        }
    }

    public XdmStream<XdmItem> stream() throws SaxonApiUncheckedException {
        return ((XdmSequenceIterator)this.iterator()).stream();
    }

    public boolean effectiveBooleanValue() throws SaxonApiException {
        try {
            return this.exp.effectiveBooleanValue(this.dynamicContext);
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public XPathDynamicContext getUnderlyingXPathContext() {
        return this.dynamicContext;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.stream.Stream;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.ConstructedItemType;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.streams.Step;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.sxpath.XPathVariable;

public class XPathExecutable {
    private XPathExpression exp;
    private Processor processor;
    private IndependentContext env;

    protected XPathExecutable(XPathExpression exp, Processor processor, IndependentContext env) {
        this.exp = exp;
        this.processor = processor;
        this.env = env;
    }

    public XPathSelector load() {
        LinkedHashMap<StructuredQName, XPathVariable> declaredVariables = new LinkedHashMap<StructuredQName, XPathVariable>();
        Iterator<XPathVariable> iter = this.env.iterateExternalVariables();
        while (iter.hasNext()) {
            XPathVariable var = iter.next();
            declaredVariables.put(var.getVariableQName(), var);
        }
        return new XPathSelector(this.exp, declaredVariables);
    }

    public Step<XdmItem> asStep() {
        return new Step<XdmItem>(){

            @Override
            public Stream<? extends XdmItem> apply(XdmItem item) {
                try {
                    XPathSelector selector = XPathExecutable.this.load();
                    selector.setContextItem(item);
                    Iterator result = selector.iterator();
                    return ((XdmSequenceIterator)result).stream();
                } catch (SaxonApiException e) {
                    throw new SaxonApiUncheckedException(e);
                }
            }
        };
    }

    public ItemType getResultItemType() {
        net.sf.saxon.type.ItemType it = this.exp.getInternalExpression().getItemType();
        return new ConstructedItemType(it, this.processor);
    }

    public OccurrenceIndicator getResultCardinality() {
        int card = this.exp.getInternalExpression().getCardinality();
        return OccurrenceIndicator.getOccurrenceIndicator(card);
    }

    public Iterator<QName> iterateExternalVariables() {
        final Iterator<XPathVariable> varIterator = this.env.iterateExternalVariables();
        return new Iterator<QName>(){

            @Override
            public boolean hasNext() {
                return varIterator.hasNext();
            }

            @Override
            public QName next() {
                return new QName(((XPathVariable)varIterator.next()).getVariableQName());
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
        };
    }

    public ItemType getRequiredItemTypeForVariable(QName variableName) {
        XPathVariable var = this.env.getExternalVariable(variableName.getStructuredQName());
        if (var == null) {
            return null;
        }
        return new ConstructedItemType(var.getRequiredType().getPrimaryType(), this.processor);
    }

    public OccurrenceIndicator getRequiredCardinalityForVariable(QName variableName) {
        XPathVariable var = this.env.getExternalVariable(variableName.getStructuredQName());
        if (var == null) {
            return null;
        }
        return OccurrenceIndicator.getOccurrenceIndicator(var.getRequiredType().getCardinality());
    }

    public XPathExpression getUnderlyingExpression() {
        return this.exp;
    }

    public StaticContext getUnderlyingStaticContext() {
        return this.env;
    }
}


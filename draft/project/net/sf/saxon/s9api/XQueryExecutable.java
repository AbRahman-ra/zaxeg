/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.s9api.ConstructedItemType;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;

public class XQueryExecutable {
    Processor processor;
    XQueryExpression exp;

    protected XQueryExecutable(Processor processor, XQueryExpression exp) {
        this.processor = processor;
        this.exp = exp;
    }

    public XQueryEvaluator load() {
        return new XQueryEvaluator(this.processor, this.exp);
    }

    public ItemType getResultItemType() {
        net.sf.saxon.type.ItemType it = this.exp.getExpression().getItemType();
        return new ConstructedItemType(it, this.processor);
    }

    public OccurrenceIndicator getResultCardinality() {
        int card = this.exp.getExpression().getCardinality();
        return OccurrenceIndicator.getOccurrenceIndicator(card);
    }

    public boolean isUpdateQuery() {
        return this.exp.isUpdateQuery();
    }

    public void explain(Destination destination) throws SaxonApiException {
        Configuration config = this.processor.getUnderlyingConfiguration();
        try {
            PipelineConfiguration pipe = config.makePipelineConfiguration();
            this.exp.explain(new ExpressionPresenter(config, destination.getReceiver(pipe, config.obtainDefaultSerializationProperties())));
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public XQueryExpression getUnderlyingCompiledQuery() {
        return this.exp;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import net.sf.saxon.Configuration;
import net.sf.saxon.PreparedStylesheet;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.instruct.GlobalParam;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.ConstructedItemType;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.WhitespaceStrippingPolicy;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;

public class XsltExecutable {
    Processor processor;
    PreparedStylesheet preparedStylesheet;

    protected XsltExecutable(Processor processor, PreparedStylesheet preparedStylesheet) {
        this.processor = processor;
        this.preparedStylesheet = preparedStylesheet;
    }

    public Processor getProcessor() {
        return this.processor;
    }

    public XsltTransformer load() {
        XsltTransformer xt = new XsltTransformer(this.processor, this.preparedStylesheet.newController(), this.preparedStylesheet.getCompileTimeParams());
        StructuredQName initialTemplate = this.preparedStylesheet.getDefaultInitialTemplateName();
        if (initialTemplate != null) {
            xt.setInitialTemplate(new QName(initialTemplate));
        }
        return xt;
    }

    public Xslt30Transformer load30() {
        return new Xslt30Transformer(this.processor, this.preparedStylesheet.newController(), this.preparedStylesheet.getCompileTimeParams());
    }

    public void explain(Destination destination) throws SaxonApiException {
        Configuration config = this.processor.getUnderlyingConfiguration();
        try {
            Receiver out = destination.getReceiver(config.makePipelineConfiguration(), config.obtainDefaultSerializationProperties());
            this.preparedStylesheet.explain(new ExpressionPresenter(config, out));
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public void export(OutputStream destination) throws SaxonApiException {
        String target = this.preparedStylesheet.getTopLevelPackage().getTargetEdition();
        if (target == null) {
            target = this.getProcessor().getSaxonEdition();
        }
        this.export(destination, target);
    }

    public void export(OutputStream destination, String target) throws SaxonApiException {
        Configuration config = this.processor.getUnderlyingConfiguration();
        try {
            StylesheetPackage topLevelPackage = this.preparedStylesheet.getTopLevelPackage();
            ExpressionPresenter presenter = config.newExpressionExporter(target, destination, topLevelPackage);
            presenter.setRelocatable(topLevelPackage.isRelocatable());
            topLevelPackage.export(presenter);
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
        try {
            destination.close();
        } catch (IOException e) {
            throw new SaxonApiException(e);
        }
    }

    public WhitespaceStrippingPolicy getWhitespaceStrippingPolicy() {
        StylesheetPackage top = this.preparedStylesheet.getTopLevelPackage();
        if (top.isStripsWhitespace()) {
            return new WhitespaceStrippingPolicy(this.preparedStylesheet.getTopLevelPackage());
        }
        return WhitespaceStrippingPolicy.UNSPECIFIED;
    }

    public HashMap<QName, ParameterDetails> getGlobalParameters() {
        Map<StructuredQName, GlobalParam> globals = this.preparedStylesheet.getGlobalParameters();
        HashMap<QName, ParameterDetails> params = new HashMap<QName, ParameterDetails>();
        for (GlobalParam v : globals.values()) {
            ParameterDetails details = new ParameterDetails(v.getRequiredType(), v.isRequiredParam());
            params.put(new QName(v.getVariableQName()), details);
        }
        return params;
    }

    public PreparedStylesheet getUnderlyingCompiledStylesheet() {
        return this.preparedStylesheet;
    }

    public class ParameterDetails {
        private SequenceType type;
        private boolean isRequired;

        protected ParameterDetails(SequenceType type, boolean isRequired) {
            this.type = type;
            this.isRequired = isRequired;
        }

        public ItemType getDeclaredItemType() {
            return new ConstructedItemType(this.type.getPrimaryType(), XsltExecutable.this.processor);
        }

        public OccurrenceIndicator getDeclaredCardinality() {
            return OccurrenceIndicator.getOccurrenceIndicator(this.type.getCardinality());
        }

        public SequenceType getUnderlyingDeclaredType() {
            return this.type;
        }

        public boolean isRequired() {
            return this.isRequired;
        }
    }
}


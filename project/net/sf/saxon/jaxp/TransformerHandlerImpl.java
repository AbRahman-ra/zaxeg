/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.jaxp;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.TransformerHandler;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceivingContentHandler;
import net.sf.saxon.jaxp.TransformerImpl;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trans.XsltController;
import net.sf.saxon.tree.tiny.TinyBuilder;
import org.xml.sax.SAXException;

public class TransformerHandlerImpl
extends ReceivingContentHandler
implements TransformerHandler {
    private TransformerImpl transformer;
    private Builder builder;
    private Receiver receiver;
    private Result result;
    private String systemId;
    private boolean started = false;

    protected TransformerHandlerImpl(TransformerImpl transformer) {
        this.transformer = transformer;
        XsltController controller = transformer.getUnderlyingXsltTransformer().getUnderlyingController();
        Configuration config = transformer.getConfiguration();
        int validation = controller.getSchemaValidationMode();
        this.builder = controller.makeBuilder();
        if (this.builder instanceof TinyBuilder) {
            ((TinyBuilder)this.builder).setStatistics(config.getTreeStatistics().SOURCE_DOCUMENT_STATISTICS);
        }
        PipelineConfiguration pipe = this.builder.getPipelineConfiguration();
        ParseOptions options = pipe.getParseOptions();
        options.setCheckEntityReferences(true);
        this.setPipelineConfiguration(pipe);
        this.receiver = controller.makeStripper(this.builder);
        if (controller.isStylesheetStrippingTypeAnnotations()) {
            this.receiver = config.getAnnotationStripper(this.receiver);
        }
        if (validation != 3) {
            options.setSchemaValidationMode(validation);
            options.setSpaceStrippingRule(NoElementsSpaceStrippingRule.getInstance());
            this.receiver = config.getDocumentValidator(this.receiver, this.getSystemId(), options, null);
        }
        this.setReceiver(this.receiver);
    }

    @Override
    public void startDocument() throws SAXException {
        if (this.started) {
            throw new UnsupportedOperationException("The TransformerHandler is not serially reusable. The startDocument() method must be called once only.");
        }
        this.started = true;
        super.startDocument();
    }

    @Override
    public Transformer getTransformer() {
        return this.transformer;
    }

    @Override
    public void setSystemId(String url) {
        this.systemId = url;
        this.receiver.setSystemId(url);
    }

    @Override
    public String getSystemId() {
        return this.systemId;
    }

    @Override
    public void setResult(Result result) {
        if (result == null) {
            throw new IllegalArgumentException("Result must not be null");
        }
        this.result = result;
    }

    public Result getResult() {
        return this.result;
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        NodeInfo doc = this.builder.getCurrentRoot();
        doc.getTreeInfo().setSpaceStrippingRule(this.transformer.getUnderlyingXsltTransformer().getUnderlyingController().getSpaceStrippingRule());
        this.builder.reset();
        if (doc == null) {
            throw new SAXException("No source document has been built");
        }
        try {
            this.transformer.transform(doc, this.result);
        } catch (TransformerException err) {
            throw new SAXException(err);
        }
    }
}


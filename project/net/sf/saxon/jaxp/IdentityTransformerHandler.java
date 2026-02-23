/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.jaxp;

import java.util.Properties;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceivingContentHandler;
import net.sf.saxon.event.Stripper;
import net.sf.saxon.jaxp.IdentityTransformer;
import net.sf.saxon.lib.SerializerFactory;
import net.sf.saxon.om.AllElementsSpaceStrippingRule;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.XPathException;
import org.xml.sax.SAXException;

public class IdentityTransformerHandler
extends ReceivingContentHandler
implements TransformerHandler {
    private Result result;
    private String systemId;
    private IdentityTransformer controller;

    protected IdentityTransformerHandler(IdentityTransformer controller) {
        this.controller = controller;
        this.setPipelineConfiguration(controller.getConfiguration().makePipelineConfiguration());
    }

    @Override
    public Transformer getTransformer() {
        return this.controller;
    }

    @Override
    public void setSystemId(String url) {
        this.systemId = url;
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
    public void startDocument() throws SAXException {
        if (this.result == null) {
            this.result = new StreamResult(System.out);
        }
        try {
            Properties props = this.controller.getOutputProperties();
            Configuration config = this.getConfiguration();
            SerializerFactory sf = config.getSerializerFactory();
            Receiver out = sf.getReceiver(this.result, new SerializationProperties(props));
            this.setPipelineConfiguration(out.getPipelineConfiguration());
            if (config.isStripsAllWhiteSpace()) {
                out = new Stripper(AllElementsSpaceStrippingRule.getInstance(), out);
            }
            this.setReceiver(out);
        } catch (XPathException err) {
            throw new SAXException(err);
        }
        super.startDocument();
    }
}


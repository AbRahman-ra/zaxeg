/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.jaxp;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.TransformerHandler;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.jaxp.AbstractTransformerImpl;
import net.sf.saxon.jaxp.FilterImpl;
import net.sf.saxon.jaxp.TransformerHandlerImpl;
import net.sf.saxon.lib.SerializerFactory;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XsltController;
import org.xml.sax.XMLFilter;

public class TransformerImpl
extends AbstractTransformerImpl {
    private XsltTransformer xsltTransformer;

    protected TransformerImpl(XsltExecutable e, XsltTransformer t) {
        super(e);
        this.xsltTransformer = t;
    }

    @Override
    public void transform(Source xmlSource, Result outputTarget) throws XPathException {
        try {
            Destination destination;
            this.xsltTransformer.setSource(xmlSource);
            if (outputTarget.getSystemId() != null) {
                this.xsltTransformer.setBaseOutputURI(outputTarget.getSystemId());
            }
            if ((destination = this.makeDestination(outputTarget)) == null) {
                SerializerFactory sf = this.getConfiguration().getSerializerFactory();
                Receiver r = sf.getReceiver(outputTarget, new SerializationProperties(this.getLocalOutputProperties()), this.getConfiguration().makePipelineConfiguration());
                this.transform(xmlSource, r);
                return;
            }
            this.xsltTransformer.setDestination(destination);
            this.xsltTransformer.transform();
            if (destination instanceof Serializer && ((Serializer)destination).isMustCloseAfterUse()) {
                destination.close();
            }
        } catch (SaxonApiException e) {
            throw XPathException.makeXPathException(e);
        }
    }

    @Override
    protected void setConvertedParameter(QName name, XdmValue value) {
        this.xsltTransformer.setParameter(name, value);
    }

    @Override
    public void clearParameters() {
        super.clearParameters();
        this.xsltTransformer.clearParameters();
    }

    @Override
    public void setURIResolver(URIResolver resolver) {
        super.setURIResolver(resolver);
        this.xsltTransformer.setURIResolver(resolver);
    }

    @Override
    public void setErrorListener(ErrorListener listener) throws IllegalArgumentException {
        super.setErrorListener(listener);
        this.xsltTransformer.setErrorListener(listener);
    }

    public void setInitialTemplate(String name) {
        this.xsltTransformer.setInitialTemplate(QName.fromClarkName(name));
    }

    public void setInitialMode(String name) throws IllegalArgumentException {
        this.xsltTransformer.setInitialMode(QName.fromClarkName(name));
    }

    public XsltTransformer getUnderlyingXsltTransformer() {
        return this.xsltTransformer;
    }

    @Override
    public XsltController getUnderlyingController() {
        return this.xsltTransformer.getUnderlyingController();
    }

    public TransformerHandler newTransformerHandler() {
        return new TransformerHandlerImpl(this);
    }

    @Override
    public XMLFilter newXMLFilter() {
        return new FilterImpl(this);
    }

    @Override
    public void reset() {
        super.reset();
        this.getUnderlyingController().reset();
    }
}


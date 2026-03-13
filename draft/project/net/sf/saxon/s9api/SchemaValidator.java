/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import net.sf.saxon.event.EventSource;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.jaxp.ReceivingDestination;
import net.sf.saxon.lib.InvalidityHandler;
import net.sf.saxon.s9api.AbstractDestination;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public abstract class SchemaValidator
extends AbstractDestination {
    public abstract void setLax(boolean var1);

    public abstract boolean isLax();

    public abstract void setErrorListener(ErrorListener var1);

    public abstract ErrorListener getErrorListener();

    public abstract void setInvalidityHandler(InvalidityHandler var1);

    public abstract InvalidityHandler getInvalidityHandler();

    public abstract void setCollectStatistics(boolean var1);

    public abstract boolean isCollectStatistics();

    public abstract void reportValidationStatistics(Destination var1) throws SaxonApiException;

    public abstract void setValidityReporting(Destination var1) throws SaxonApiException;

    public abstract void setUseXsiSchemaLocation(boolean var1);

    public abstract boolean isUseXsiSchemaLocation();

    public abstract void setDestination(Destination var1);

    public abstract Destination getDestination();

    public abstract void setDocumentElementName(QName var1);

    public abstract QName getDocumentElementName();

    public abstract void setDocumentElementTypeName(QName var1) throws SaxonApiException;

    public abstract QName getDocumentElementTypeName();

    protected abstract SchemaType getDocumentElementType();

    public abstract void setExpandAttributeDefaults(boolean var1);

    public abstract boolean isExpandAttributeDefaults();

    public abstract void setParameter(QName var1, XdmValue var2);

    public abstract XdmValue getParameter(QName var1);

    public abstract void validate(Source var1) throws SaxonApiException;

    public abstract void validateMultiple(Iterable<Source> var1) throws SaxonApiException;

    public Source asSource(final Source input) {
        return new EventSource(){
            {
                this.setSystemId(input.getSystemId());
            }

            @Override
            public void send(Receiver out) throws XPathException {
                SchemaValidator.this.setDestination(new ReceivingDestination(out));
                try {
                    SchemaValidator.this.validate(input);
                } catch (SaxonApiException e) {
                    throw XPathException.makeXPathException(e);
                }
            }
        };
    }

    @Override
    public abstract Receiver getReceiver(PipelineConfiguration var1, SerializationProperties var2) throws SaxonApiException;

    @Override
    public abstract void close() throws SaxonApiException;
}


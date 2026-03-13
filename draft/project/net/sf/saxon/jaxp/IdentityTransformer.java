/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.jaxp;

import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.Sender;
import net.sf.saxon.lib.ErrorReporterToListener;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.SerializerFactory;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import org.xml.sax.SAXParseException;

public class IdentityTransformer
extends Transformer {
    private Configuration configuration;
    private Properties localOutputProperties;
    private URIResolver uriResolver;
    private ErrorListener errorListener;

    protected IdentityTransformer(Configuration config) {
        this.configuration = config;
        this.uriResolver = config.getURIResolver();
    }

    @Override
    public void reset() {
        this.localOutputProperties = null;
        this.uriResolver = this.getConfiguration().getURIResolver();
        this.errorListener = null;
    }

    @Override
    public void setURIResolver(URIResolver resolver) {
        this.uriResolver = resolver;
    }

    @Override
    public URIResolver getURIResolver() {
        return this.uriResolver;
    }

    @Override
    public void setErrorListener(ErrorListener listener) throws IllegalArgumentException {
        this.errorListener = Objects.requireNonNull(listener);
    }

    @Override
    public ErrorListener getErrorListener() {
        return this.errorListener;
    }

    @Override
    public void setOutputProperties(Properties properties) {
        if (properties == null) {
            this.localOutputProperties = null;
        } else {
            for (String key : properties.stringPropertyNames()) {
                this.setOutputProperty(key, properties.getProperty(key));
            }
        }
    }

    @Override
    public Properties getOutputProperties() {
        String key;
        Properties newProps = new Properties();
        Properties sheetProperties = this.getStylesheetOutputProperties();
        Enumeration<?> keys = sheetProperties.propertyNames();
        while (keys.hasMoreElements()) {
            key = (String)keys.nextElement();
            newProps.setProperty(key, sheetProperties.getProperty(key));
        }
        if (this.localOutputProperties != null) {
            keys = this.localOutputProperties.propertyNames();
            while (keys.hasMoreElements()) {
                key = (String)keys.nextElement();
                newProps.setProperty(key, this.localOutputProperties.getProperty(key));
            }
        }
        return newProps;
    }

    protected Properties getStylesheetOutputProperties() {
        return new Properties();
    }

    protected Properties getLocalOutputProperties() {
        if (this.localOutputProperties == null) {
            this.makeLocalOutputProperties();
        }
        return this.localOutputProperties;
    }

    private void makeLocalOutputProperties() {
        this.localOutputProperties = new Properties();
    }

    @Override
    public String getOutputProperty(String name) throws IllegalArgumentException {
        try {
            this.getConfiguration().getSerializerFactory().checkOutputProperty(name, null);
        } catch (XPathException err) {
            throw new IllegalArgumentException(err.getMessage());
        }
        String value = null;
        if (this.localOutputProperties != null) {
            value = this.localOutputProperties.getProperty(name);
        }
        if (value == null) {
            value = this.getStylesheetOutputProperties().getProperty(name);
        }
        return value;
    }

    @Override
    public void setOutputProperty(String name, String value) throws IllegalArgumentException {
        if (this.localOutputProperties == null) {
            this.makeLocalOutputProperties();
        }
        try {
            value = this.getConfiguration().getSerializerFactory().checkOutputProperty(name, value);
        } catch (XPathException err) {
            throw new IllegalArgumentException(err.getMessage());
        }
        this.localOutputProperties.setProperty(name, value);
    }

    @Override
    public void setParameter(String name, Object value) {
    }

    @Override
    public Object getParameter(String name) {
        return null;
    }

    @Override
    public void clearParameters() {
    }

    @Override
    public void transform(Source source, Result result) throws TransformerException {
        try {
            SerializerFactory sf = this.getConfiguration().getSerializerFactory();
            Receiver receiver = sf.getReceiver(result, new SerializationProperties(this.getOutputProperties()));
            ParseOptions options = receiver.getPipelineConfiguration().getParseOptions();
            if (this.errorListener != null) {
                options.setErrorReporter(new ErrorReporterToListener(this.errorListener));
            }
            options.setContinueAfterValidationErrors(true);
            Sender.send(source, receiver, options);
        } catch (XPathException err) {
            Throwable cause = err.getException();
            if (cause instanceof SAXParseException) {
                SAXParseException spe = (SAXParseException)cause;
                if ((cause = spe.getException()) instanceof RuntimeException) {
                    this.reportFatalError(err);
                }
            } else {
                this.reportFatalError(err);
            }
            throw err;
        }
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    protected void reportFatalError(XPathException err) {
        try {
            if (this.errorListener != null) {
                this.errorListener.error(err);
            } else {
                this.getConfiguration().makeErrorReporter().report(new XmlProcessingException(err));
            }
        } catch (TransformerException transformerException) {
            // empty catch block
        }
    }
}


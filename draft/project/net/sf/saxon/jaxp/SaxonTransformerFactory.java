/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.jaxp;

import java.io.File;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.jaxp.AbstractTransformerImpl;
import net.sf.saxon.jaxp.IdentityTransformer;
import net.sf.saxon.jaxp.IdentityTransformerHandler;
import net.sf.saxon.jaxp.TemplatesHandlerImpl;
import net.sf.saxon.jaxp.TemplatesImpl;
import net.sf.saxon.jaxp.TransformerImpl;
import net.sf.saxon.lib.ErrorReporterToListener;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.StandardErrorListener;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.trans.CompilerInfo;
import net.sf.saxon.trans.ConfigurationReader;
import net.sf.saxon.trans.XPathException;
import org.xml.sax.XMLFilter;

public class SaxonTransformerFactory
extends SAXTransformerFactory
implements Configuration.ApiProvider {
    private Processor processor;
    private ErrorListener errorListener = new StandardErrorListener();
    private static final String FEATURE_SECURE_PROCESSING = "http://javax.xml.XMLConstants/feature/secure-processing";

    public SaxonTransformerFactory() {
        this.processor = new Processor(true);
    }

    public SaxonTransformerFactory(Configuration config) {
        this.processor = new Processor(config);
    }

    public void setConfiguration(Configuration config) {
        this.processor.setConfigurationProperty(Feature.CONFIGURATION, config);
    }

    public Configuration getConfiguration() {
        return this.processor.getUnderlyingConfiguration();
    }

    @Override
    public Transformer newTransformer(Source source) throws TransformerConfigurationException {
        Templates templates = this.newTemplates(source);
        return templates.newTransformer();
    }

    @Override
    public Transformer newTransformer() {
        return new IdentityTransformer(this.processor.getUnderlyingConfiguration());
    }

    @Override
    public synchronized Templates newTemplates(Source source) throws TransformerConfigurationException {
        try {
            XsltCompiler compiler = this.processor.newXsltCompiler();
            if (this.errorListener != null) {
                compiler.setErrorReporter(new ErrorReporterToListener(this.errorListener));
            }
            XsltExecutable executable = compiler.compile(source);
            return new TemplatesImpl(executable);
        } catch (SaxonApiException e) {
            throw new TransformerConfigurationException(e);
        }
    }

    public synchronized Templates newTemplates(Source source, CompilerInfo info) throws TransformerConfigurationException {
        try {
            XsltCompiler compiler = this.processor.newXsltCompiler();
            compiler.getUnderlyingCompilerInfo().copyFrom(info);
            return new TemplatesImpl(compiler.compile(source));
        } catch (SaxonApiException e) {
            throw new TransformerConfigurationException(e);
        }
    }

    @Override
    public Source getAssociatedStylesheet(Source source, String media, String title, String charset) throws TransformerConfigurationException {
        try {
            XsltCompiler compiler = this.processor.newXsltCompiler();
            if (this.errorListener != null) {
                compiler.setErrorReporter(new ErrorReporterToListener(this.errorListener));
            }
            return compiler.getAssociatedStylesheet(source, media, title, charset);
        } catch (SaxonApiException e) {
            throw new TransformerConfigurationException(e);
        }
    }

    @Override
    public void setURIResolver(URIResolver resolver) {
        this.getConfiguration().setURIResolver(resolver);
    }

    @Override
    public URIResolver getURIResolver() {
        return this.getConfiguration().getURIResolver();
    }

    @Override
    public boolean getFeature(String name) {
        switch (name) {
            case "http://javax.xml.transform.sax.SAXSource/feature": 
            case "http://javax.xml.transform.sax.SAXResult/feature": 
            case "http://javax.xml.transform.dom.DOMSource/feature": 
            case "http://javax.xml.transform.dom.DOMResult/feature": 
            case "http://javax.xml.transform.stream.StreamSource/feature": 
            case "http://javax.xml.transform.stream.StreamResult/feature": 
            case "http://javax.xml.transform.sax.SAXTransformerFactory/feature": 
            case "http://javax.xml.transform.sax.SAXTransformerFactory/feature/xmlfilter": {
                return true;
            }
            case "http://javax.xml.XMLConstants/feature/secure-processing": {
                return !this.getConfiguration().getBooleanProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS);
            }
        }
        try {
            Object val = this.getConfiguration().getConfigurationProperty(name);
            return val instanceof Boolean && (Boolean)val != false;
        } catch (IllegalArgumentException err) {
            return false;
        }
    }

    @Override
    public void setAttribute(String name, Object value) throws IllegalArgumentException {
        switch (name) {
            case "http://saxon.sf.net/feature/configuration": {
                this.setConfiguration((Configuration)value);
                break;
            }
            case "http://saxon.sf.net/feature/configuration-file": {
                ConfigurationReader reader = new ConfigurationReader();
                try {
                    this.setConfiguration(reader.makeConfiguration(new StreamSource(new File((String)value))));
                    break;
                } catch (XPathException err) {
                    throw new IllegalArgumentException(err);
                }
            }
            case "http://javax.xml.XMLConstants/property/accessExternalDTD": {
                this.getConfiguration().setConfigurationProperty(Feature.XML_PARSER_PROPERTY.name + "http://javax.xml.XMLConstants/property/accessExternalDTD", value);
                break;
            }
            case "http://javax.xml.XMLConstants/property/accessExternalStylesheet": {
                this.getConfiguration().setConfigurationProperty(Feature.ALLOWED_PROTOCOLS, value.toString());
                break;
            }
            default: {
                this.getConfiguration().setConfigurationProperty(name, value);
            }
        }
    }

    @Override
    public Object getAttribute(String name) throws IllegalArgumentException {
        if (name.equals("http://javax.xml.XMLConstants/property/accessExternalDTD")) {
            return this.getConfiguration().getConfigurationProperty(Feature.XML_PARSER_PROPERTY + "http://javax.xml.XMLConstants/property/accessExternalDTD");
        }
        if (name.equals("http://javax.xml.XMLConstants/property/accessExternalStylesheet")) {
            return this.getConfiguration().getConfigurationProperty(Feature.ALLOWED_PROTOCOLS);
        }
        return this.getConfiguration().getConfigurationProperty(name);
    }

    @Override
    public void setErrorListener(ErrorListener listener) throws IllegalArgumentException {
        if (listener == null) {
            throw new IllegalArgumentException();
        }
        this.errorListener = listener;
    }

    @Override
    public ErrorListener getErrorListener() {
        return this.errorListener;
    }

    @Override
    public TransformerHandler newTransformerHandler(Source src) throws TransformerConfigurationException {
        Templates tmpl = this.newTemplates(src);
        return this.newTransformerHandler(tmpl);
    }

    @Override
    public TransformerHandler newTransformerHandler(Templates templates) throws TransformerConfigurationException {
        if (!(templates instanceof TemplatesImpl)) {
            throw new TransformerConfigurationException("Templates object was not created by Saxon");
        }
        TransformerImpl transformer = (TransformerImpl)templates.newTransformer();
        return transformer.newTransformerHandler();
    }

    @Override
    public TransformerHandler newTransformerHandler() {
        IdentityTransformer transformer = new IdentityTransformer(this.getConfiguration());
        return new IdentityTransformerHandler(transformer);
    }

    @Override
    public TemplatesHandler newTemplatesHandler() {
        return new TemplatesHandlerImpl(this.processor);
    }

    @Override
    public XMLFilter newXMLFilter(Source src) throws TransformerConfigurationException {
        Templates tmpl = this.newTemplates(src);
        return this.newXMLFilter(tmpl);
    }

    @Override
    public XMLFilter newXMLFilter(Templates templates) throws TransformerConfigurationException {
        if (!(templates instanceof TemplatesImpl)) {
            throw new TransformerConfigurationException("Supplied Templates object was not created using Saxon");
        }
        AbstractTransformerImpl transformer = (AbstractTransformerImpl)templates.newTransformer();
        return transformer.newXMLFilter();
    }

    @Override
    public void setFeature(String name, boolean value) throws TransformerConfigurationException {
        if (name.equals(FEATURE_SECURE_PROCESSING)) {
            this.getConfiguration().setBooleanProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS, !value);
        } else {
            try {
                this.getConfiguration().setBooleanProperty(name, value);
            } catch (IllegalArgumentException err) {
                throw new TransformerConfigurationException("Unsupported TransformerFactory feature: " + name);
            }
        }
    }

    public Processor getProcessor() {
        return this.processor;
    }
}


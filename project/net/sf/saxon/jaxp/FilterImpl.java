/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.jaxp;

import java.io.IOException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import net.sf.saxon.Version;
import net.sf.saxon.event.ContentHandlerProxy;
import net.sf.saxon.jaxp.AbstractXMLFilter;
import net.sf.saxon.jaxp.TransformerImpl;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class FilterImpl
extends AbstractXMLFilter {
    private TransformerImpl transformer;

    FilterImpl(TransformerImpl transformer) {
        this.transformer = transformer;
    }

    @Override
    public void parse(InputSource input) throws IOException, SAXException {
        if (this.parser == null) {
            try {
                this.parser = Version.platform.loadParser();
            } catch (Exception err) {
                throw new SAXException(err);
            }
        }
        SAXSource source = new SAXSource();
        source.setInputSource(input);
        source.setXMLReader(this.parser);
        ContentHandlerProxy result = new ContentHandlerProxy();
        result.setPipelineConfiguration(this.transformer.getConfiguration().makePipelineConfiguration());
        result.setUnderlyingContentHandler(this.contentHandler);
        if (this.lexicalHandler != null) {
            result.setLexicalHandler(this.lexicalHandler);
        }
        try {
            result.setOutputProperties(this.transformer.getOutputProperties());
            this.transformer.transform(source, result);
        } catch (TransformerException err) {
            Throwable cause = err.getException();
            if (cause != null && cause instanceof SAXException) {
                throw (SAXException)cause;
            }
            if (cause != null && cause instanceof IOException) {
                throw (IOException)cause;
            }
            throw new SAXException(err);
        }
    }

    public Transformer getTransformer() {
        return this.transformer;
    }
}


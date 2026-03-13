/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.jaxp;

import java.io.IOException;
import javax.xml.transform.sax.SAXSource;
import net.sf.saxon.Version;
import net.sf.saxon.jaxp.AbstractXMLFilter;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.SAXDestination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Xslt30Transformer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class StreamingFilterImpl
extends AbstractXMLFilter {
    private Xslt30Transformer transformer;

    StreamingFilterImpl(Xslt30Transformer transformer) {
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
        if (this.lexicalHandler != null && this.lexicalHandler != this.contentHandler) {
            throw new IllegalStateException("ContentHandler and LexicalHandler must be the same object");
        }
        SAXSource source = new SAXSource();
        source.setInputSource(input);
        source.setXMLReader(this.parser);
        SAXDestination result = new SAXDestination(this.contentHandler);
        try {
            this.transformer.applyTemplates(source, (Destination)result);
        } catch (SaxonApiException err) {
            throw new SAXException(err);
        }
    }

    public Xslt30Transformer getTransformer() {
        return this.transformer;
    }
}


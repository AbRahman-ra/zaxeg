/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.dom;

import java.io.File;
import java.util.Objects;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.sax.SAXSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentOverNodeInfo;
import net.sf.saxon.event.Sender;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.om.AllElementsSpaceStrippingRule;
import net.sf.saxon.om.IgnorableSpaceStrippingRule;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.tree.tiny.TinyDocumentImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DocumentBuilderImpl
extends DocumentBuilder {
    private Configuration config;
    private ParseOptions parseOptions = new ParseOptions();

    public void setConfiguration(Configuration config) {
        this.config = config;
    }

    public Configuration getConfiguration() {
        if (this.config == null) {
            this.config = new Configuration();
        }
        return this.config;
    }

    @Override
    public boolean isNamespaceAware() {
        return true;
    }

    public void setValidating(boolean state) {
        this.parseOptions.setDTDValidationMode(state ? 1 : 4);
    }

    @Override
    public boolean isValidating() {
        return this.parseOptions.getDTDValidationMode() == 1;
    }

    @Override
    public Document newDocument() {
        throw new UnsupportedOperationException("The only way to build a document using this DocumentBuilder is with the parse() method");
    }

    @Override
    public Document parse(InputSource in) throws SAXException {
        try {
            if (this.config == null) {
                this.config = new Configuration();
            }
            TinyBuilder builder = new TinyBuilder(this.config.makePipelineConfiguration());
            builder.setStatistics(this.config.getTreeStatistics().SOURCE_DOCUMENT_STATISTICS);
            SAXSource source = new SAXSource(in);
            source.setSystemId(in.getSystemId());
            Sender.send(source, builder, this.parseOptions);
            TinyDocumentImpl doc = (TinyDocumentImpl)builder.getCurrentRoot();
            builder.reset();
            return (Document)((Object)DocumentOverNodeInfo.wrap(doc));
        } catch (XPathException err) {
            throw new SAXException(err);
        }
    }

    @Override
    public Document parse(File f) throws SAXException {
        Objects.requireNonNull(f);
        String uri = f.toURI().toString();
        InputSource in = new InputSource(uri);
        return this.parse(in);
    }

    @Override
    public void setEntityResolver(EntityResolver er) {
        this.parseOptions.setEntityResolver(er);
    }

    @Override
    public void setErrorHandler(ErrorHandler eh) {
        this.parseOptions.setErrorHandler(eh);
    }

    @Override
    public DOMImplementation getDOMImplementation() {
        return this.newDocument().getImplementation();
    }

    public void setXIncludeAware(boolean state) {
        this.parseOptions.setXIncludeAware(state);
    }

    @Override
    public boolean isXIncludeAware() {
        return this.parseOptions.isXIncludeAware();
    }

    public void setStripSpace(int stripAction) {
        switch (stripAction) {
            case 2: {
                this.parseOptions.setSpaceStrippingRule(AllElementsSpaceStrippingRule.getInstance());
                break;
            }
            case 0: {
                this.parseOptions.setSpaceStrippingRule(NoElementsSpaceStrippingRule.getInstance());
                break;
            }
            case 1: {
                this.parseOptions.setSpaceStrippingRule(IgnorableSpaceStrippingRule.getInstance());
                break;
            }
            case 3: {
                this.parseOptions.setSpaceStrippingRule(null);
                break;
            }
            default: {
                throw new IllegalArgumentException();
            }
        }
    }

    public int getStripSpace() {
        SpaceStrippingRule rule = this.parseOptions.getSpaceStrippingRule();
        if (rule == AllElementsSpaceStrippingRule.getInstance()) {
            return 2;
        }
        if (rule == NoElementsSpaceStrippingRule.getInstance()) {
            return 0;
        }
        if (rule == IgnorableSpaceStrippingRule.getInstance()) {
            return 1;
        }
        if (rule == null) {
            return 1;
        }
        return 4;
    }

    public void setParseOptions(ParseOptions options) {
        this.parseOptions = options;
    }

    public ParseOptions getParseOptions() {
        return this.parseOptions;
    }
}


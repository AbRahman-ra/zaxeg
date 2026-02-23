/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.xml.transform.ErrorListener;
import net.sf.saxon.expr.parser.OptimizerOptions;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.s9api.ConstructedItemType;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.StaticError;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingAbort;

public class XQueryCompiler {
    private Processor processor;
    private StaticQueryContext staticQueryContext;
    private ItemType requiredContextItemType;
    private String encoding;

    protected XQueryCompiler(Processor processor) {
        this.processor = processor;
        this.staticQueryContext = processor.getUnderlyingConfiguration().newStaticQueryContext();
    }

    public Processor getProcessor() {
        return this.processor;
    }

    public void setBaseURI(URI baseURI) {
        if (baseURI == null) {
            this.staticQueryContext.setBaseURI(null);
        } else {
            if (!baseURI.isAbsolute()) {
                throw new IllegalArgumentException("Base URI must be an absolute URI: " + baseURI);
            }
            this.staticQueryContext.setBaseURI(baseURI.toString());
        }
    }

    public URI getBaseURI() {
        try {
            return new URI(this.staticQueryContext.getBaseURI());
        } catch (URISyntaxException err) {
            throw new IllegalStateException(err);
        }
    }

    public void setErrorListener(ErrorListener listener) {
        this.staticQueryContext.setErrorListener(listener);
    }

    public ErrorListener getErrorListener() {
        return this.staticQueryContext.getErrorListener();
    }

    public void setErrorReporter(ErrorReporter reporter) {
        this.staticQueryContext.setErrorReporter(reporter);
    }

    public ErrorReporter getErrorReporter() {
        return this.staticQueryContext.getErrorReporter();
    }

    public void setCompileWithTracing(boolean option) {
        this.staticQueryContext.setCompileWithTracing(option);
    }

    public boolean isCompileWithTracing() {
        return this.staticQueryContext.isCompileWithTracing();
    }

    public void setModuleURIResolver(ModuleURIResolver resolver) {
        this.staticQueryContext.setModuleURIResolver(resolver);
    }

    public ModuleURIResolver getModuleURIResolver() {
        return this.staticQueryContext.getModuleURIResolver();
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public void setUpdatingEnabled(boolean updating) {
        if (updating && !this.staticQueryContext.getConfiguration().isLicensedFeature(4)) {
            throw new UnsupportedOperationException("XQuery Update is not supported in this Saxon Configuration");
        }
        this.staticQueryContext.setUpdatingEnabled(updating);
    }

    public boolean isUpdatingEnabled() {
        return this.staticQueryContext.isUpdatingEnabled();
    }

    public void setSchemaAware(boolean schemaAware) {
        if (schemaAware && !this.processor.getUnderlyingConfiguration().isLicensedFeature(4)) {
            throw new UnsupportedOperationException("Schema-awareness requires a Saxon-EE license");
        }
        this.staticQueryContext.setSchemaAware(schemaAware);
    }

    public boolean isSchemaAware() {
        return this.staticQueryContext.isSchemaAware();
    }

    public void setStreaming(boolean option) {
        this.staticQueryContext.setStreaming(option);
        if (option && !this.processor.getUnderlyingConfiguration().isLicensedFeature(4)) {
            throw new UnsupportedOperationException("Streaming requires a Saxon-EE license");
        }
        if (option) {
            this.setRequiredContextItemType(new ConstructedItemType(NodeKindTest.DOCUMENT, this.getProcessor()));
        }
    }

    public boolean isStreaming() {
        return this.staticQueryContext.isStreaming();
    }

    public String getLanguageVersion() {
        return "3.1";
    }

    public void declareNamespace(String prefix, String uri) {
        this.staticQueryContext.declareNamespace(prefix, uri);
    }

    public void declareDefaultCollation(String uri) {
        this.staticQueryContext.declareDefaultCollation(uri);
    }

    public void setRequiredContextItemType(ItemType type) {
        this.requiredContextItemType = type;
        this.staticQueryContext.setRequiredContextItemType(type.getUnderlyingItemType());
    }

    public ItemType getRequiredContextItemType() {
        return this.requiredContextItemType;
    }

    public void setFastCompilation(boolean fast) {
        if (fast) {
            this.staticQueryContext.setOptimizerOptions(new OptimizerOptions(0));
        } else {
            this.staticQueryContext.setOptimizerOptions(this.getProcessor().getUnderlyingConfiguration().getOptimizerOptions());
        }
    }

    public boolean isFastCompilation() {
        return this.staticQueryContext.getOptimizerOptions().getOptions() == 0;
    }

    public void compileLibrary(String query) throws SaxonApiException {
        try {
            this.staticQueryContext.compileLibrary(query);
        } catch (UncheckedXPathException | XPathException | XmlProcessingAbort e) {
            throw new SaxonApiException(e);
        }
    }

    public void compileLibrary(File query) throws SaxonApiException, IOException {
        try (FileInputStream stream = new FileInputStream(query);){
            String savedBaseUri = this.staticQueryContext.getBaseURI();
            this.staticQueryContext.setBaseURI(query.toURI().toString());
            this.staticQueryContext.compileLibrary(stream, this.encoding);
            this.staticQueryContext.setBaseURI(savedBaseUri);
        } catch (UncheckedXPathException | XPathException | XmlProcessingAbort e) {
            throw new SaxonApiException(e);
        }
    }

    public void compileLibrary(Reader query) throws SaxonApiException {
        try {
            this.staticQueryContext.compileLibrary(query);
        } catch (IOException | UncheckedXPathException | XPathException | XmlProcessingAbort e) {
            throw new SaxonApiException(e);
        }
    }

    public void compileLibrary(InputStream query) throws SaxonApiException {
        try {
            this.staticQueryContext.compileLibrary(query, this.encoding);
        } catch (IOException | UncheckedXPathException | XPathException | XmlProcessingAbort e) {
            throw new SaxonApiException(e);
        }
    }

    public XQueryExecutable compile(String query) throws SaxonApiException {
        try {
            return new XQueryExecutable(this.processor, this.staticQueryContext.compileQuery(query));
        } catch (UncheckedXPathException e) {
            throw new SaxonApiException(e.getXPathException());
        } catch (XPathException | XmlProcessingAbort e) {
            throw new SaxonApiException(e);
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public XQueryExecutable compile(File query) throws SaxonApiException, IOException {
        try (FileInputStream stream = new FileInputStream(query);){
            String savedBaseUri = this.staticQueryContext.getBaseURI();
            this.staticQueryContext.setBaseURI(query.toURI().toString());
            XQueryExecutable exec = new XQueryExecutable(this.processor, this.staticQueryContext.compileQuery(stream, this.encoding));
            this.staticQueryContext.setBaseURI(savedBaseUri);
            XQueryExecutable xQueryExecutable = exec;
            return xQueryExecutable;
        } catch (UncheckedXPathException e) {
            throw new SaxonApiException(e.getXPathException());
        } catch (XPathException | XmlProcessingAbort e) {
            throw new SaxonApiException(e);
        }
    }

    public XQueryExecutable compile(InputStream query) throws SaxonApiException {
        try {
            return new XQueryExecutable(this.processor, this.staticQueryContext.compileQuery(query, this.encoding));
        } catch (UncheckedXPathException e) {
            throw new SaxonApiException(e.getXPathException());
        } catch (XPathException | XmlProcessingAbort e) {
            throw new SaxonApiException(e);
        }
    }

    public XQueryExecutable compile(Reader query) throws SaxonApiException, IOException {
        try {
            return new XQueryExecutable(this.processor, this.staticQueryContext.compileQuery(query));
        } catch (UncheckedXPathException e) {
            throw new SaxonApiException(e.getXPathException());
        } catch (XPathException | XmlProcessingAbort e) {
            throw new SaxonApiException(e);
        }
    }

    public StaticQueryContext getUnderlyingStaticContext() {
        return this.staticQueryContext;
    }

    public void setErrorList(List<? super StaticError> errorList) {
        this.setErrorReporter(errorList::add);
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.util.List;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import net.sf.saxon.event.FilterFactory;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.type.SchemaType;
import org.xml.sax.EntityResolver;
import org.xml.sax.XMLReader;

public class AugmentedSource
implements Source {
    private Source source;
    private ParseOptions options = new ParseOptions();
    private String systemID;

    private AugmentedSource(Source source) {
        if (source instanceof AugmentedSource) {
            throw new IllegalArgumentException("Contained source must not be an AugmentedSource");
        }
        this.source = source;
    }

    public AugmentedSource(Source source, ParseOptions options) {
        if (source instanceof AugmentedSource) {
            throw new IllegalArgumentException("Contained source must not be an AugmentedSource");
        }
        this.source = source;
        this.options = options;
    }

    public static AugmentedSource makeAugmentedSource(Source source) {
        if (source instanceof AugmentedSource) {
            return (AugmentedSource)source;
        }
        return new AugmentedSource(source);
    }

    public void addFilter(FilterFactory filter) {
        this.options.addFilter(filter);
    }

    public List<FilterFactory> getFilters() {
        return this.options.getFilters();
    }

    public Source getContainedSource() {
        return this.source;
    }

    public ParseOptions getParseOptions() {
        return this.options;
    }

    public void setModel(TreeModel model) {
        this.options.setModel(model);
    }

    public TreeModel getModel() {
        return this.options.getModel();
    }

    public void setSchemaValidationMode(int option) {
        this.options.setSchemaValidationMode(option);
    }

    public int getSchemaValidation() {
        return this.options.getSchemaValidationMode();
    }

    public void setTopLevelElement(StructuredQName elementName) {
        this.options.setTopLevelElement(elementName);
    }

    public StructuredQName getTopLevelElement() {
        return this.options.getTopLevelElement();
    }

    public void setTopLevelType(SchemaType type) {
        this.options.setTopLevelType(type);
    }

    public SchemaType getTopLevelType() {
        return this.options.getTopLevelType();
    }

    public void setDTDValidationMode(int option) {
        this.options.setDTDValidationMode(option);
    }

    public int getDTDValidation() {
        return this.options.getDTDValidationMode();
    }

    public void setLineNumbering(boolean lineNumbering) {
        this.options.setLineNumbering(lineNumbering);
    }

    public boolean isLineNumbering() {
        return this.options.isLineNumbering();
    }

    public boolean isLineNumberingSet() {
        return this.options.isLineNumberingSet();
    }

    public void setXMLReader(XMLReader parser) {
        this.options.setXMLReader(parser);
        if (this.source instanceof SAXSource) {
            ((SAXSource)this.source).setXMLReader(parser);
        }
    }

    public XMLReader getXMLReader() {
        XMLReader parser = this.options.getXMLReader();
        if (parser != null) {
            return parser;
        }
        if (this.source instanceof SAXSource) {
            return ((SAXSource)this.source).getXMLReader();
        }
        return null;
    }

    public void setWrapDocument(Boolean wrap) {
        this.options.setWrapDocument(wrap);
    }

    public Boolean getWrapDocument() {
        return this.options.getWrapDocument();
    }

    @Override
    public void setSystemId(String id) {
        this.systemID = id;
    }

    @Override
    public String getSystemId() {
        return this.systemID != null ? this.systemID : this.source.getSystemId();
    }

    public void setXIncludeAware(boolean state) {
        this.options.setXIncludeAware(state);
    }

    public boolean isXIncludeAwareSet() {
        return this.options.isXIncludeAwareSet();
    }

    public boolean isXIncludeAware() {
        return this.options.isXIncludeAware();
    }

    public void setEntityResolver(EntityResolver resolver) {
        this.options.setEntityResolver(resolver);
    }

    public EntityResolver getEntityResolver() {
        return this.options.getEntityResolver();
    }

    public void setErrorReporter(ErrorReporter listener) {
        this.options.setErrorReporter(listener);
    }

    public ErrorReporter getErrorReporter() {
        return this.options.getErrorReporter();
    }

    public void setPleaseCloseAfterUse(boolean close) {
        this.options.setPleaseCloseAfterUse(close);
    }

    public boolean isPleaseCloseAfterUse() {
        return this.options.isPleaseCloseAfterUse();
    }

    public void close() {
        ParseOptions.close(this.source);
    }
}


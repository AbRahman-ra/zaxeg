/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.jaxp;

import javax.xml.transform.Templates;
import javax.xml.transform.sax.TemplatesHandler;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.CommentStripper;
import net.sf.saxon.event.ReceivingContentHandler;
import net.sf.saxon.event.Stripper;
import net.sf.saxon.jaxp.TemplatesImpl;
import net.sf.saxon.om.StylesheetSpaceStrippingRule;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.StyleNodeFactory;
import net.sf.saxon.style.UseWhenFilter;
import net.sf.saxon.style.XSLModuleRoot;
import net.sf.saxon.trans.CompilerInfo;
import net.sf.saxon.tree.linked.DocumentImpl;
import net.sf.saxon.tree.linked.ElementImpl;
import net.sf.saxon.tree.linked.LinkedTreeBuilder;
import net.sf.saxon.value.NestedIntegerValue;
import org.xml.sax.Locator;

public class TemplatesHandlerImpl
extends ReceivingContentHandler
implements TemplatesHandler {
    private Processor processor;
    private LinkedTreeBuilder builder;
    private StyleNodeFactory nodeFactory;
    private Templates templates;
    private String systemId;

    protected TemplatesHandlerImpl(Processor processor) {
        this.processor = processor;
        Configuration config = processor.getUnderlyingConfiguration();
        this.setPipelineConfiguration(config.makePipelineConfiguration());
        CompilerInfo info = new CompilerInfo(config.getDefaultXsltCompilerInfo());
        Compilation compilation = new Compilation(config, info);
        compilation.setMinimalPackageData();
        this.nodeFactory = compilation.getStyleNodeFactory(true);
        this.builder = new LinkedTreeBuilder(this.getPipelineConfiguration());
        this.builder.setNodeFactory(this.nodeFactory);
        this.builder.setLineNumbering(true);
        UseWhenFilter useWhenFilter = new UseWhenFilter(compilation, this.builder, NestedIntegerValue.TWO);
        StylesheetSpaceStrippingRule rule = new StylesheetSpaceStrippingRule(config.getNamePool());
        Stripper styleStripper = new Stripper(rule, useWhenFilter);
        CommentStripper commentStripper = new CommentStripper(styleStripper);
        this.setReceiver(commentStripper);
    }

    @Override
    public Templates getTemplates() {
        if (this.templates == null) {
            DocumentImpl doc = (DocumentImpl)this.builder.getCurrentRoot();
            if (doc == null) {
                return null;
            }
            ElementImpl top = doc.getDocumentElement();
            if (!(top instanceof XSLModuleRoot)) {
                throw new IllegalStateException("Input is not a stylesheet");
            }
            this.builder.reset();
            try {
                XsltCompiler compiler = this.processor.newXsltCompiler();
                this.templates = new TemplatesImpl(compiler.compile(doc));
            } catch (SaxonApiException tce) {
                throw new IllegalStateException(tce.getMessage());
            }
        }
        return this.templates;
    }

    @Override
    public void setSystemId(String url) {
        this.systemId = url;
        this.builder.setSystemId(url);
        super.setDocumentLocator(new Locator(){

            @Override
            public int getColumnNumber() {
                return -1;
            }

            @Override
            public int getLineNumber() {
                return -1;
            }

            @Override
            public String getPublicId() {
                return null;
            }

            @Override
            public String getSystemId() {
                return TemplatesHandlerImpl.this.systemId;
            }
        });
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        if (this.systemId == null) {
            super.setDocumentLocator(locator);
        }
    }

    @Override
    public String getSystemId() {
        return this.systemId;
    }
}


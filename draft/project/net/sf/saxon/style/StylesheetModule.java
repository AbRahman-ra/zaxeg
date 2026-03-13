/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.PreparedStylesheet;
import net.sf.saxon.event.CheckSumFilter;
import net.sf.saxon.event.CommentStripper;
import net.sf.saxon.event.PIGrabber;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.Sender;
import net.sf.saxon.event.Sink;
import net.sf.saxon.event.Stripper;
import net.sf.saxon.event.Valve;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.lib.AugmentedSource;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.om.DocumentKey;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StylesheetSpaceStrippingRule;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.DataElement;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.style.UseWhenFilter;
import net.sf.saxon.style.XSLGeneralIncorporate;
import net.sf.saxon.style.XSLModuleRoot;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.packages.IPackageLoader;
import net.sf.saxon.tree.linked.DocumentImpl;
import net.sf.saxon.tree.linked.LinkedTreeBuilder;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.value.NestedIntegerValue;
import net.sf.saxon.value.Whitespace;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class StylesheetModule {
    private StyleElement rootElement;
    private int precedence;
    private int minImportPrecedence;
    private StylesheetModule importer;
    boolean wasIncluded;
    private int inputTypeAnnotations = 0;
    protected List<ComponentDeclaration> topLevel = new ArrayList<ComponentDeclaration>();

    public StylesheetModule(StyleElement rootElement, int precedence) {
        this.rootElement = rootElement;
        this.precedence = precedence;
    }

    public static DocumentImpl loadStylesheetModule(Source styleSource, boolean topLevelModule, Compilation compilation, NestedIntegerValue precedence) throws XPathException {
        DocumentKey docURI;
        String systemId = styleSource.getSystemId();
        DocumentKey documentKey = docURI = systemId == null ? null : new DocumentKey(systemId);
        if (systemId != null && compilation.getImportStack().contains(docURI)) {
            throw new XPathException("The stylesheet module includes/imports itself directly or indirectly", "XTSE0180");
        }
        compilation.getImportStack().push(docURI);
        Configuration config = compilation.getConfiguration();
        PipelineConfiguration pipe = config.makePipelineConfiguration();
        pipe.setErrorReporter(compilation.getCompilerInfo().getErrorReporter());
        LinkedTreeBuilder styleBuilder = new LinkedTreeBuilder(pipe);
        pipe.setURIResolver(compilation.getCompilerInfo().getURIResolver());
        styleBuilder.setSystemId(styleSource.getSystemId());
        styleBuilder.setNodeFactory(compilation.getStyleNodeFactory(topLevelModule));
        styleBuilder.setLineNumbering(true);
        UseWhenFilter useWhenFilter = new UseWhenFilter(compilation, styleBuilder, precedence);
        useWhenFilter.setSystemId(styleSource.getSystemId());
        StylesheetSpaceStrippingRule rule = new StylesheetSpaceStrippingRule(config.getNamePool());
        Stripper styleStripper = new Stripper(rule, useWhenFilter);
        CommentStripper commentStripper = new CommentStripper(styleStripper);
        ParseOptions options = StylesheetModule.makeStylesheetParseOptions(styleSource, pipe);
        try {
            StylesheetModule.sendStylesheetSource(styleSource, config, commentStripper, options);
            DocumentImpl doc = (DocumentImpl)styleBuilder.getCurrentRoot();
            styleBuilder.reset();
            compilation.getImportStack().pop();
            DocumentImpl documentImpl = doc;
            return documentImpl;
        } catch (XPathException err) {
            if (topLevelModule && !err.hasBeenReported()) {
                compilation.reportError(err);
            }
            throw err;
        } finally {
            if (options.isPleaseCloseAfterUse()) {
                ParseOptions.close(styleSource);
            }
        }
    }

    private static ParseOptions makeStylesheetParseOptions(Source styleSource, PipelineConfiguration pipe) {
        ParseOptions options = styleSource instanceof AugmentedSource ? ((AugmentedSource)styleSource).getParseOptions() : new ParseOptions();
        options.setSchemaValidationMode(4);
        options.setDTDValidationMode(4);
        options.setLineNumbering(true);
        options.setSpaceStrippingRule(NoElementsSpaceStrippingRule.getInstance());
        options.setErrorReporter(pipe.getErrorReporter());
        return options;
    }

    private static void sendStylesheetSource(Source styleSource, Configuration config, Receiver sourcePipeline, ParseOptions options) throws XPathException {
        boolean knownParser;
        boolean bl = knownParser = options.getXMLReader() != null || options.getXMLReaderMaker() != null || styleSource instanceof SAXSource && ((SAXSource)styleSource).getXMLReader() != null;
        if (knownParser) {
            Sender.send(styleSource, sourcePipeline, options);
        } else {
            XMLReader styleParser = config.getStyleParser();
            options.setXMLReader(styleParser);
            Sender.send(styleSource, sourcePipeline, options);
            config.reuseStyleParser(styleParser);
        }
    }

    public static PreparedStylesheet loadStylesheet(Source styleSource, Compilation compilation) throws XPathException {
        Valve valve;
        String systemId;
        DocumentKey docURI;
        if (styleSource instanceof SAXSource && compilation.getConfiguration().getBooleanProperty(Feature.IGNORE_SAX_SOURCE_PARSER)) {
            ((SAXSource)styleSource).setXMLReader(null);
        }
        DocumentKey documentKey = docURI = (systemId = styleSource.getSystemId()) == null ? null : new DocumentKey(systemId);
        if (systemId != null && compilation.getImportStack().contains(docURI)) {
            throw new XPathException("The stylesheet module includes/imports itself directly or indirectly", "XTSE0180");
        }
        compilation.getImportStack().push(docURI);
        compilation.setMinimalPackageData();
        Configuration config = compilation.getConfiguration();
        PipelineConfiguration pipe = config.makePipelineConfiguration();
        pipe.setErrorReporter(compilation.getCompilerInfo().getErrorReporter());
        LinkedTreeBuilder styleBuilder = new LinkedTreeBuilder(pipe);
        pipe.setURIResolver(compilation.getCompilerInfo().getURIResolver());
        styleBuilder.setSystemId(styleSource.getSystemId());
        styleBuilder.setNodeFactory(compilation.getStyleNodeFactory(true));
        styleBuilder.setLineNumbering(true);
        UseWhenFilter useWhenFilter = new UseWhenFilter(compilation, styleBuilder, NestedIntegerValue.TWO);
        useWhenFilter.setSystemId(styleSource.getSystemId());
        StylesheetSpaceStrippingRule rule = new StylesheetSpaceStrippingRule(config.getNamePool());
        Stripper styleStripper = new Stripper(rule, useWhenFilter);
        CommentStripper commentStripper = new CommentStripper(styleStripper);
        TinyBuilder packageBuilder = new TinyBuilder(pipe);
        packageBuilder.setSystemId(styleSource.getSystemId());
        CheckSumFilter checksummer = new CheckSumFilter(packageBuilder);
        checksummer.setCheckExistingChecksum(true);
        Valve sourcePipeline = valve = new Valve("http://ns.saxonica.com/xslt/export", commentStripper, checksummer);
        ParseOptions options = StylesheetModule.makeStylesheetParseOptions(styleSource, pipe);
        try {
            StylesheetModule.sendStylesheetSource(styleSource, config, sourcePipeline, options);
            if (valve.wasDiverted()) {
                if (!checksummer.isChecksumCorrect()) {
                    throw new XPathException("Compiled package cannot be loaded: incorrect checksum");
                }
                IPackageLoader loader = config.makePackageLoader();
                StylesheetPackage pack = loader.loadPackageDoc(packageBuilder.getCurrentRoot());
                compilation.setPackageData(pack);
                PreparedStylesheet pss = new PreparedStylesheet(compilation);
                pack.checkForAbstractComponents();
                pack.updatePreparedStylesheet(pss);
                PreparedStylesheet preparedStylesheet = pss;
                return preparedStylesheet;
            }
            NodeInfo doc = styleBuilder.getCurrentRoot();
            styleBuilder.reset();
            compilation.getImportStack().pop();
            PreparedStylesheet pss = new PreparedStylesheet(compilation);
            PrincipalStylesheetModule psm = compilation.compilePackage(doc);
            if (compilation.getErrorCount() > 0) {
                XPathException e = new XPathException("Errors were reported during stylesheet compilation");
                e.setHasBeenReported(true);
                throw e;
            }
            psm.getStylesheetPackage().checkForAbstractComponents();
            psm.getStylesheetPackage().updatePreparedStylesheet(pss);
            pss.addPackage(compilation.getPackageData());
            PreparedStylesheet preparedStylesheet = pss;
            return preparedStylesheet;
        } catch (XPathException err) {
            if (!err.hasBeenReported()) {
                compilation.reportError(err);
            }
            throw err;
        } finally {
            if (options.isPleaseCloseAfterUse()) {
                ParseOptions.close(styleSource);
            }
        }
    }

    public static Source getAssociatedStylesheet(Configuration config, URIResolver resolver, Source source, String media, String title, String charset) throws XPathException {
        PIGrabber grabber;
        block6: {
            grabber = new PIGrabber(new Sink(config.makePipelineConfiguration()));
            grabber.setFactory(config);
            grabber.setCriteria(media, title);
            grabber.setBaseURI(source.getSystemId());
            grabber.setURIResolver(resolver);
            try {
                Sender.send(source, grabber, null);
            } catch (XPathException err) {
                if (grabber.isTerminated()) break block6;
                throw new XPathException("Failed while looking for xml-stylesheet PI", err);
            }
        }
        try {
            Source[] sources = grabber.getAssociatedStylesheets();
            if (sources == null) {
                throw new XPathException("No matching <?xml-stylesheet?> processing instruction found");
            }
            return StylesheetModule.compositeStylesheet(config, source.getSystemId(), sources);
        } catch (TransformerException err) {
            if (err instanceof XPathException) {
                throw (XPathException)err;
            }
            throw new XPathException(err);
        }
    }

    private static Source compositeStylesheet(Configuration config, String baseURI, Source[] sources) throws XPathException {
        if (sources.length == 1) {
            return sources[0];
        }
        if (sources.length == 0) {
            throw new XPathException("No stylesheets were supplied");
        }
        StringBuilder sb = new StringBuilder(250);
        sb.append("<xsl:stylesheet version='1.0' ");
        sb.append(" xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>");
        for (Source source : sources) {
            sb.append("<xsl:import href='").append(source.getSystemId()).append("'/>");
        }
        sb.append("</xsl:stylesheet>");
        InputSource composite = new InputSource();
        composite.setSystemId(baseURI);
        composite.setCharacterStream(new StringReader(sb.toString()));
        return new SAXSource(config.getSourceParser(), composite);
    }

    public void setImporter(StylesheetModule importer) {
        this.importer = importer;
    }

    public StylesheetModule getImporter() {
        return this.importer;
    }

    public PrincipalStylesheetModule getPrincipalStylesheetModule() {
        return this.importer.getPrincipalStylesheetModule();
    }

    public StyleElement getRootElement() {
        return this.rootElement;
    }

    public XSLModuleRoot getStylesheetElement() {
        return (XSLModuleRoot)this.rootElement;
    }

    public Configuration getConfiguration() {
        return this.rootElement.getConfiguration();
    }

    public int getPrecedence() {
        return this.wasIncluded ? this.importer.getPrecedence() : this.precedence;
    }

    public void setWasIncluded() {
        this.wasIncluded = true;
    }

    public void setMinImportPrecedence(int min) {
        this.minImportPrecedence = min;
    }

    public int getMinImportPrecedence() {
        return this.minImportPrecedence;
    }

    public void spliceIncludes() throws XPathException {
        if (this.topLevel == null || this.topLevel.size() == 0) {
            this.topLevel = new ArrayList<ComponentDeclaration>(50);
        }
        this.minImportPrecedence = this.precedence;
        StyleElement previousElement = this.rootElement;
        for (NodeInfo nodeInfo : this.getStylesheetElement().children()) {
            if (nodeInfo.getNodeKind() == 3) {
                if (Whitespace.isWhite(nodeInfo.getStringValueCS())) continue;
                previousElement.compileError("No character data is allowed between top-level elements", "XTSE0120");
                continue;
            }
            if (nodeInfo instanceof DataElement) {
                if (!((DataElement)nodeInfo).getNodeName().getURI().isEmpty()) continue;
                Loc loc = new Loc(nodeInfo);
                previousElement.compileError("Top-level elements must be in a namespace: " + ((DataElement)nodeInfo).getNodeName().getLocalPart() + " is not", "XTSE0130", loc);
                continue;
            }
            previousElement = (StyleElement)nodeInfo;
            if (nodeInfo instanceof XSLGeneralIncorporate) {
                XSLGeneralIncorporate xslinc = (XSLGeneralIncorporate)nodeInfo;
                xslinc.processAttributes();
                xslinc.validateInstruction();
                int errors = ((XSLGeneralIncorporate)nodeInfo).getCompilation().getErrorCount();
                StylesheetModule inc = xslinc.getIncludedStylesheet(this, this.precedence);
                if (inc == null) {
                    return;
                }
                errors = ((XSLGeneralIncorporate)nodeInfo).getCompilation().getErrorCount() - errors;
                if (errors > 0) {
                    xslinc.compileError("Reported " + errors + (errors == 1 ? " error" : " errors") + " in " + (xslinc.isImport() ? "imported" : "included") + " stylesheet module", "XTSE0165");
                }
                if (xslinc.isImport()) {
                    this.precedence = inc.getPrecedence() + 1;
                } else {
                    this.precedence = inc.getPrecedence();
                    inc.setMinImportPrecedence(this.minImportPrecedence);
                    inc.setWasIncluded();
                }
                List<ComponentDeclaration> incchildren = inc.topLevel;
                for (ComponentDeclaration decl : incchildren) {
                    int last = this.topLevel.size() - 1;
                    if (last < 0 || decl.getPrecedence() >= this.topLevel.get(last).getPrecedence()) {
                        this.topLevel.add(decl);
                        continue;
                    }
                    while (last >= 0 && decl.getPrecedence() < this.topLevel.get(last).getPrecedence()) {
                        --last;
                    }
                    this.topLevel.add(last + 1, decl);
                }
                continue;
            }
            ComponentDeclaration decl = new ComponentDeclaration(this, (StyleElement)nodeInfo);
            this.topLevel.add(decl);
        }
    }

    public int getInputTypeAnnotations() {
        return this.inputTypeAnnotations;
    }

    public void setInputTypeAnnotations(int annotations) throws XPathException {
        this.inputTypeAnnotations |= annotations;
        if (this.inputTypeAnnotations == 3) {
            this.getPrincipalStylesheetModule().compileError("One stylesheet module specifies input-type-annotations='strip', another specifies input-type-annotations='preserve'", "XTSE0265");
        }
        if (annotations == 1) {
            this.getPrincipalStylesheetModule().getStylesheetPackage().setStripsTypeAnnotations(true);
        }
    }
}


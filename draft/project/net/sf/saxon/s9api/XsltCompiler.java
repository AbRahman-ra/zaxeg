/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.PreparedStylesheet;
import net.sf.saxon.expr.parser.OptimizerOptions;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.StaticError;
import net.sf.saxon.s9api.UnprefixedElementMatchingPolicy;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltPackage;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.PackageVersionRanges;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.StylesheetModule;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trace.XSLTTraceCodeInjector;
import net.sf.saxon.trans.CompilerInfo;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingAbort;
import net.sf.saxon.trans.packages.IPackageLoader;
import net.sf.saxon.trans.packages.PackageDetails;
import net.sf.saxon.trans.packages.PackageLibrary;
import net.sf.saxon.trans.packages.VersionedPackageName;
import net.sf.saxon.tree.linked.DocumentImpl;

public class XsltCompiler {
    private Processor processor;
    private Configuration config;
    private CompilerInfo compilerInfo;

    protected XsltCompiler(Processor processor) {
        this.processor = processor;
        this.config = processor.getUnderlyingConfiguration();
        this.compilerInfo = new CompilerInfo(this.config.getDefaultXsltCompilerInfo());
        this.compilerInfo.setGenerateByteCode(this.config.isGenerateByteCode(HostLanguage.XSLT));
        this.compilerInfo.setTargetEdition(this.config.getEditionCode());
        this.compilerInfo.setJustInTimeCompilation(this.config.isJITEnabled());
    }

    public Processor getProcessor() {
        return this.processor;
    }

    public void setURIResolver(URIResolver resolver) {
        this.compilerInfo.setURIResolver(resolver);
    }

    public void setParameter(QName name, XdmValue value) {
        try {
            this.compilerInfo.setParameter(name.getStructuredQName(), value.getUnderlyingValue().materialize());
        } catch (XPathException e) {
            throw new SaxonApiUncheckedException(e);
        }
    }

    public void clearParameters() {
        this.compilerInfo.clearParameters();
    }

    public URIResolver getURIResolver() {
        return this.compilerInfo.getURIResolver();
    }

    public void setErrorListener(ErrorListener listener) {
        this.compilerInfo.setErrorListener(listener);
    }

    public ErrorListener getErrorListener() {
        return this.compilerInfo.getErrorListener();
    }

    public void setErrorList(List<? super StaticError> errorList) {
        this.compilerInfo.setErrorReporter(errorList::add);
    }

    public void setErrorReporter(ErrorReporter reporter) {
        this.compilerInfo.setErrorReporter(reporter);
    }

    public ErrorReporter getErrorReporter() {
        return this.compilerInfo.getErrorReporter();
    }

    public void setSchemaAware(boolean schemaAware) {
        this.compilerInfo.setSchemaAware(schemaAware);
    }

    public boolean isSchemaAware() {
        return this.compilerInfo.isSchemaAware();
    }

    public boolean isRelocatable() {
        return this.compilerInfo.isRelocatable();
    }

    public void setRelocatable(boolean relocatable) {
        this.compilerInfo.setRelocatable(relocatable);
    }

    public void setTargetEdition(String edition) {
        if (!("EE".equals(edition) || "PE".equals(edition) || "HE".equals(edition) || "JS".equals(edition))) {
            throw new IllegalArgumentException("Unknown Saxon edition " + edition);
        }
        this.compilerInfo.setTargetEdition(edition);
    }

    public String getTargetEdition() {
        return this.compilerInfo.getTargetEdition();
    }

    public void declareDefaultCollation(String uri) {
        StringCollator c;
        try {
            c = this.getProcessor().getUnderlyingConfiguration().getCollation(uri);
        } catch (XPathException e) {
            c = null;
        }
        if (c == null) {
            throw new IllegalStateException("Unknown collation " + uri);
        }
        this.compilerInfo.setDefaultCollation(uri);
    }

    public String getDefaultCollation() {
        return this.compilerInfo.getDefaultCollation();
    }

    public void setXsltLanguageVersion(String version) {
    }

    public String getXsltLanguageVersion() {
        return "3.0";
    }

    public boolean isAssertionsEnabled() {
        return this.compilerInfo.isAssertionsEnabled();
    }

    public void setAssertionsEnabled(boolean enabled) {
        this.compilerInfo.setAssertionsEnabled(enabled);
    }

    public void setFastCompilation(boolean fast) {
        if (fast) {
            this.compilerInfo.setOptimizerOptions(new OptimizerOptions(1024));
        } else {
            this.compilerInfo.setOptimizerOptions(this.getProcessor().getUnderlyingConfiguration().getOptimizerOptions());
        }
    }

    public boolean isFastCompilation() {
        return this.compilerInfo.getOptimizerOptions().getOptions() == 1024;
    }

    public void setCompileWithTracing(boolean option) {
        if (option) {
            this.compilerInfo.setCodeInjector(new XSLTTraceCodeInjector());
        } else {
            this.compilerInfo.setCodeInjector(null);
        }
    }

    public boolean isCompileWithTracing() {
        return this.compilerInfo.isCompileWithTracing();
    }

    public void setGenerateByteCode(boolean option) {
        this.compilerInfo.setGenerateByteCode(option);
    }

    public boolean isGenerateByteCode() {
        return this.compilerInfo.isGenerateByteCode();
    }

    public void importXQueryEnvironment(XQueryCompiler queryCompiler) {
        this.compilerInfo.setXQueryLibraries(queryCompiler.getUnderlyingStaticContext().getCompiledLibraries());
    }

    public Source getAssociatedStylesheet(Source source, String media, String title, String charset) throws SaxonApiException {
        try {
            return StylesheetModule.getAssociatedStylesheet(this.config, this.compilerInfo.getURIResolver(), source, media, title, charset);
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public XsltPackage compilePackage(Source source) throws SaxonApiException {
        try {
            Compilation compilation = source instanceof DocumentImpl && ((DocumentImpl)source).getDocumentElement() instanceof StyleElement ? ((StyleElement)((DocumentImpl)source).getDocumentElement()).getCompilation() : new Compilation(this.config, new CompilerInfo(this.compilerInfo));
            compilation.setLibraryPackage(true);
            XsltPackage pack = new XsltPackage(this, compilation.compilePackage(source).getStylesheetPackage());
            int errors = compilation.getErrorCount();
            if (errors > 0) {
                String count = errors == 1 ? "one error" : errors + " errors";
                throw new SaxonApiException("Package compilation failed: " + count + " reported");
            }
            return pack;
        } catch (UncheckedXPathException e) {
            throw new SaxonApiException(e.getXPathException());
        } catch (XPathException | XmlProcessingAbort e) {
            throw new SaxonApiException(e);
        }
    }

    private PackageLibrary getPackageLibrary() {
        return this.compilerInfo.getPackageLibrary();
    }

    public XsltPackage loadLibraryPackage(URI location) throws SaxonApiException {
        return this.loadLibraryPackage(new StreamSource(location.toString()));
    }

    public XsltPackage loadLibraryPackage(Source input) throws SaxonApiException {
        try {
            IPackageLoader loader = this.processor.getUnderlyingConfiguration().makePackageLoader();
            if (loader != null) {
                StylesheetPackage pack = loader.loadPackage(input);
                return new XsltPackage(this, pack);
            }
            throw new SaxonApiException("Loading library package requires Saxon PE or higher");
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public XsltExecutable loadExecutablePackage(URI location) throws SaxonApiException {
        return this.loadLibraryPackage(location).link();
    }

    public void importPackage(XsltPackage thePackage) throws SaxonApiException {
        if (thePackage.getProcessor() != this.processor) {
            throw new SaxonApiException("The imported package and the XsltCompiler must belong to the same Processor");
        }
        this.compilerInfo.getPackageLibrary().addPackage(thePackage.getUnderlyingPreparedPackage());
    }

    public void importPackage(XsltPackage thePackage, String packageName, String version) throws SaxonApiException {
        try {
            if (thePackage.getProcessor() != this.processor) {
                throw new SaxonApiException("The imported package and the XsltCompiler must belong to the same Processor");
            }
            PackageDetails details = new PackageDetails();
            if (packageName == null) {
                packageName = thePackage.getName();
            }
            if (version == null) {
                version = thePackage.getVersion();
            }
            details.nameAndVersion = new VersionedPackageName(packageName, version);
            details.loadedPackage = thePackage.getUnderlyingPreparedPackage();
            this.compilerInfo.getPackageLibrary().addPackage(details);
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public XsltPackage obtainPackage(String packageName, String versionRange) throws SaxonApiException {
        try {
            PackageVersionRanges pvr = new PackageVersionRanges(versionRange);
            PackageDetails details = this.getPackageLibrary().findPackage(packageName, pvr);
            if (details != null) {
                if (details.loadedPackage != null) {
                    return new XsltPackage(this, details.loadedPackage);
                }
                if (details.sourceLocation != null) {
                    XsltPackage pack = this.compilePackage(details.sourceLocation);
                    details.loadedPackage = pack.getUnderlyingPreparedPackage();
                    return pack;
                }
            }
            return null;
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public XsltPackage obtainPackageWithAlias(String alias) throws SaxonApiException {
        PackageDetails details = this.getPackageLibrary().findDetailsForAlias(alias);
        if (details == null) {
            throw new SaxonApiException("No package with alias " + alias + " found in package library");
        }
        try {
            StylesheetPackage pack = this.getPackageLibrary().obtainLoadedPackage(details, new ArrayList<VersionedPackageName>());
            return new XsltPackage(this, pack);
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public XsltExecutable compile(Source source) throws SaxonApiException {
        Objects.requireNonNull(source);
        try {
            CompilerInfo ci2 = new CompilerInfo(this.compilerInfo);
            PreparedStylesheet pss = Compilation.compileSingletonPackage(this.config, ci2, source);
            return new XsltExecutable(this.processor, pss);
        } catch (UncheckedXPathException e) {
            throw new SaxonApiException(e.getXPathException());
        } catch (XPathException | XmlProcessingAbort e) {
            throw new SaxonApiException(e);
        }
    }

    public CompilerInfo getUnderlyingCompilerInfo() {
        return this.compilerInfo;
    }

    public void setJustInTimeCompilation(boolean jit) {
        if (jit && !this.config.isLicensedFeature(2)) {
            throw new UnsupportedOperationException("XSLT just-in-time compilation requires a Saxon-EE license");
        }
        this.compilerInfo.setJustInTimeCompilation(jit);
    }

    public boolean isJustInTimeCompilation() {
        return this.compilerInfo.isJustInTimeCompilation();
    }

    public String getDefaultElementNamespace() {
        return this.compilerInfo.getDefaultElementNamespace();
    }

    public void setDefaultElementNamespace(String defaultNS) {
        this.compilerInfo.setDefaultElementNamespace(defaultNS);
    }

    public UnprefixedElementMatchingPolicy getUnprefixedElementMatchingPolicy() {
        return this.compilerInfo.getUnprefixedElementMatchingPolicy();
    }

    public void setUnprefixedElementMatchingPolicy(UnprefixedElementMatchingPolicy unprefixedElementMatchingPolicy) {
        this.compilerInfo.setUnprefixedElementMatchingPolicy(unprefixedElementMatchingPolicy);
    }
}


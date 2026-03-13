/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import java.util.Collection;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.URIResolver;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.instruct.GlobalParameterSet;
import net.sf.saxon.expr.parser.CodeInjector;
import net.sf.saxon.expr.parser.OptimizerOptions;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.ErrorReporterToListener;
import net.sf.saxon.lib.OutputURIResolver;
import net.sf.saxon.lib.StandardErrorReporter;
import net.sf.saxon.lib.StandardOutputResolver;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.QueryLibrary;
import net.sf.saxon.s9api.UnprefixedElementMatchingPolicy;
import net.sf.saxon.trans.packages.PackageLibrary;

public class CompilerInfo {
    private Configuration config;
    private URIResolver uriResolver;
    private OutputURIResolver outputURIResolver = StandardOutputResolver.getInstance();
    private ErrorReporter errorReporter = new StandardErrorReporter();
    private CodeInjector codeInjector;
    private int recoveryPolicy = 1;
    private boolean schemaAware;
    private String messageReceiverClassName = "net.sf.saxon.serialize.MessageEmitter";
    private StructuredQName defaultInitialMode;
    private StructuredQName defaultInitialTemplate;
    private GlobalParameterSet suppliedParameters = new GlobalParameterSet();
    private String defaultCollation;
    private PackageLibrary packageLibrary;
    private boolean assertionsEnabled = false;
    private String targetEdition = "HE";
    private boolean relocatable = false;
    private Collection<QueryLibrary> queryLibraries;
    private OptimizerOptions optimizerOptions;
    private String defaultNamespaceForElementsAndTypes = "";
    private UnprefixedElementMatchingPolicy unprefixedElementMatchingPolicy = UnprefixedElementMatchingPolicy.DEFAULT_NAMESPACE;

    public CompilerInfo(Configuration config) {
        this.config = config;
        this.packageLibrary = new PackageLibrary(this);
        this.optimizerOptions = config.getOptimizerOptions();
    }

    public CompilerInfo(CompilerInfo info) {
        this.copyFrom(info);
    }

    public void copyFrom(CompilerInfo info) {
        this.config = info.config;
        this.uriResolver = info.uriResolver;
        this.outputURIResolver = info.outputURIResolver;
        this.errorReporter = info.errorReporter;
        this.codeInjector = info.codeInjector;
        this.recoveryPolicy = info.recoveryPolicy;
        this.schemaAware = info.schemaAware;
        this.messageReceiverClassName = info.messageReceiverClassName;
        this.defaultInitialMode = info.defaultInitialMode;
        this.defaultInitialTemplate = info.defaultInitialTemplate;
        this.suppliedParameters = new GlobalParameterSet(info.suppliedParameters);
        this.defaultCollation = info.defaultCollation;
        this.assertionsEnabled = info.assertionsEnabled;
        this.targetEdition = info.targetEdition;
        this.packageLibrary = new PackageLibrary(info.packageLibrary);
        this.relocatable = info.relocatable;
        this.optimizerOptions = info.optimizerOptions;
        this.queryLibraries = info.queryLibraries;
        this.defaultNamespaceForElementsAndTypes = info.defaultNamespaceForElementsAndTypes;
        this.unprefixedElementMatchingPolicy = info.unprefixedElementMatchingPolicy;
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    public void setJustInTimeCompilation(boolean jit) {
        this.optimizerOptions = jit ? this.optimizerOptions.union(new OptimizerOptions(1024)) : this.optimizerOptions.except(new OptimizerOptions(1024));
    }

    public boolean isJustInTimeCompilation() {
        return this.optimizerOptions.isSet(1024);
    }

    public void setURIResolver(URIResolver resolver) {
        this.uriResolver = resolver;
    }

    public void setParameter(StructuredQName name, GroundedValue seq) {
        this.suppliedParameters.put(name, seq);
    }

    public GlobalParameterSet getParameters() {
        return this.suppliedParameters;
    }

    public void clearParameters() {
        this.suppliedParameters.clear();
    }

    public void setTargetEdition(String edition) {
        this.targetEdition = edition;
    }

    public String getTargetEdition() {
        return this.targetEdition;
    }

    public boolean isRelocatable() {
        return this.relocatable;
    }

    public void setRelocatable(boolean relocatable) {
        this.relocatable = relocatable;
    }

    public void setPackageLibrary(PackageLibrary library) {
        this.packageLibrary = library;
    }

    public PackageLibrary getPackageLibrary() {
        return this.packageLibrary;
    }

    public boolean isAssertionsEnabled() {
        return this.assertionsEnabled;
    }

    public void setAssertionsEnabled(boolean enabled) {
        this.assertionsEnabled = enabled;
    }

    public void setOptimizerOptions(OptimizerOptions options) {
        this.optimizerOptions = options;
    }

    public OptimizerOptions getOptimizerOptions() {
        return this.optimizerOptions;
    }

    public void setGenerateByteCode(boolean option) {
        this.optimizerOptions = option ? this.optimizerOptions.union(new OptimizerOptions(64)) : this.optimizerOptions.except(new OptimizerOptions(64));
    }

    public boolean isGenerateByteCode() {
        return this.optimizerOptions.isSet(64);
    }

    public URIResolver getURIResolver() {
        return this.uriResolver;
    }

    public OutputURIResolver getOutputURIResolver() {
        return this.outputURIResolver;
    }

    public void setOutputURIResolver(OutputURIResolver outputURIResolver) {
        this.outputURIResolver = outputURIResolver;
    }

    public void setErrorListener(ErrorListener listener) {
        this.setErrorReporter(new ErrorReporterToListener(listener));
    }

    public ErrorListener getErrorListener() {
        if (this.errorReporter instanceof ErrorReporterToListener) {
            return ((ErrorReporterToListener)this.errorReporter).getErrorListener();
        }
        return null;
    }

    public void setErrorReporter(ErrorReporter reporter) {
        this.errorReporter = reporter;
    }

    public ErrorReporter getErrorReporter() {
        return this.errorReporter;
    }

    public String getMessageReceiverClassName() {
        return this.messageReceiverClassName;
    }

    public void setMessageReceiverClassName(String messageReceiverClassName) {
        this.messageReceiverClassName = messageReceiverClassName;
    }

    public void setDefaultCollation(String collation) {
        this.defaultCollation = collation;
    }

    public String getDefaultCollation() {
        return this.defaultCollation;
    }

    public void setCodeInjector(CodeInjector injector) {
        this.codeInjector = injector;
    }

    public CodeInjector getCodeInjector() {
        return this.codeInjector;
    }

    public boolean isCompileWithTracing() {
        return this.codeInjector != null;
    }

    public void setSchemaAware(boolean schemaAware) {
        this.schemaAware = schemaAware;
    }

    public boolean isSchemaAware() {
        return this.schemaAware;
    }

    public void setDefaultInitialTemplate(StructuredQName initialTemplate) {
        this.defaultInitialTemplate = initialTemplate;
    }

    public StructuredQName getDefaultInitialTemplate() {
        return this.defaultInitialTemplate;
    }

    public void setDefaultInitialMode(StructuredQName initialMode) {
        this.defaultInitialMode = initialMode;
    }

    public StructuredQName getDefaultInitialMode() {
        return this.defaultInitialMode;
    }

    public void setXsltVersion(int version) {
    }

    public int getXsltVersion() {
        return 30;
    }

    public String getDefaultElementNamespace() {
        return this.defaultNamespaceForElementsAndTypes;
    }

    public void setDefaultElementNamespace(String defaultNamespaceForElementsAndTypes) {
        this.defaultNamespaceForElementsAndTypes = defaultNamespaceForElementsAndTypes;
    }

    public UnprefixedElementMatchingPolicy getUnprefixedElementMatchingPolicy() {
        return this.unprefixedElementMatchingPolicy;
    }

    public void setUnprefixedElementMatchingPolicy(UnprefixedElementMatchingPolicy unprefixedElementMatchingPolicy) {
        this.unprefixedElementMatchingPolicy = unprefixedElementMatchingPolicy;
    }

    public void setXQueryLibraries(Collection<QueryLibrary> libraries) {
        this.queryLibraries = libraries;
    }

    public Collection<QueryLibrary> getQueryLibraries() {
        return this.queryLibraries;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.xml.transform.Source;
import net.sf.saxon.Configuration;
import net.sf.saxon.PreparedStylesheet;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.instruct.GlobalParameterSet;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.om.DocumentKey;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.QNameParser;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.XmlProcessingError;
import net.sf.saxon.style.LiteralResultElement;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StyleNodeFactory;
import net.sf.saxon.style.StylesheetModule;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.style.XSLPackage;
import net.sf.saxon.trans.CompilerInfo;
import net.sf.saxon.trans.Timer;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingAbort;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.trans.XmlProcessingIncident;
import net.sf.saxon.trans.packages.PackageDetails;
import net.sf.saxon.trans.packages.PackageLibrary;
import net.sf.saxon.trans.packages.UsePack;
import net.sf.saxon.trans.packages.VersionedPackageName;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.NestedIntegerValue;

public class Compilation {
    public static boolean TIMING = false;
    private Configuration config;
    private CompilerInfo compilerInfo;
    private PrincipalStylesheetModule principalStylesheetModule;
    private int errorCount = 0;
    private boolean schemaAware;
    private QNameParser qNameParser;
    private Map<StructuredQName, ValueAndPrecedence> staticVariables = new HashMap<StructuredQName, ValueAndPrecedence>();
    private Map<DocumentKey, TreeInfo> stylesheetModules = new HashMap<DocumentKey, TreeInfo>();
    private Stack<DocumentKey> importStack = new Stack();
    private PackageData packageData;
    private boolean preScan = true;
    private boolean createsSecondaryResultDocuments = false;
    private boolean libraryPackage = false;
    private VersionedPackageName expectedNameAndVersion = null;
    private List<UsePack> packageDependencies = new ArrayList<UsePack>();
    private List<VersionedPackageName> usingPackages = new ArrayList<VersionedPackageName>();
    private GlobalParameterSet suppliedParameters;
    private boolean fallbackToNonStreaming = false;
    public Timer timer = null;

    public Compilation(Configuration config, CompilerInfo info) {
        this.config = config;
        this.compilerInfo = info;
        this.schemaAware = info.isSchemaAware();
        this.preScan = info.isJustInTimeCompilation();
        this.suppliedParameters = this.compilerInfo.getParameters();
        this.qNameParser = new QNameParser(null).withAcceptEQName(true).withErrorOnBadSyntax("XTSE0020").withErrorOnUnresolvedPrefix("XTSE0280");
        if (TIMING) {
            this.timer = new Timer();
        }
    }

    public static PreparedStylesheet compileSingletonPackage(Configuration config, CompilerInfo compilerInfo, Source source) throws XPathException {
        try {
            Compilation compilation = new Compilation(config, compilerInfo);
            return StylesheetModule.loadStylesheet(source, compilation);
        } catch (XPathException err) {
            if (!err.hasBeenReported()) {
                compilerInfo.getErrorReporter().report(new XmlProcessingException(err));
            }
            throw err;
        }
    }

    public void setUsingPackages(List<VersionedPackageName> users) {
        this.usingPackages = users;
    }

    public void setPackageData(PackageData pack) {
        this.packageData = pack;
    }

    public void setMinimalPackageData() {
        if (this.getPackageData() == null) {
            PackageData pd = new PackageData(this.getConfiguration());
            pd.setHostLanguage(HostLanguage.XSLT);
            pd.setTargetEdition(this.compilerInfo.getTargetEdition());
            pd.setSchemaAware(this.schemaAware);
            this.packageData = pd;
        }
    }

    public void setExpectedNameAndVersion(VersionedPackageName vpn) {
        this.expectedNameAndVersion = vpn;
    }

    public void registerPackageDependency(UsePack use) {
        this.packageDependencies.add(use);
    }

    public void satisfyPackageDependencies(XSLPackage thisPackage) throws XPathException {
        PackageLibrary library = this.compilerInfo.getPackageLibrary();
        library.getCompilerInfo().setTargetEdition(this.compilerInfo.getTargetEdition());
        XPathException lastError = null;
        for (UsePack use : this.packageDependencies) {
            StylesheetPackage used;
            VersionedPackageName existing;
            PackageDetails details = library.findPackage(use.packageName, use.ranges);
            if (details == null) {
                throw new XPathException("Cannot find package " + use.packageName + " (version " + use.ranges + ")", "XTSE3000", use.location);
            }
            if (details.loadedPackage != null && this.usingPackages.contains(existing = new VersionedPackageName((used = details.loadedPackage).getPackageName(), used.getPackageVersion()))) {
                FastStringBuffer buffer = new FastStringBuffer(1024);
                for (VersionedPackageName n : this.usingPackages) {
                    buffer.append(n.packageName);
                    buffer.append(", ");
                }
                buffer.append("and ");
                buffer.append(thisPackage.getName());
                throw new XPathException("There is a cycle of package dependencies involving " + buffer, "XTSE3005");
            }
            try {
                ArrayList<VersionedPackageName> disallowed = new ArrayList<VersionedPackageName>(this.usingPackages);
                disallowed.add(details.nameAndVersion);
                used = library.obtainLoadedPackage(details, disallowed);
            } catch (XPathException err) {
                if (!err.hasBeenReported()) {
                    this.reportError(err);
                }
                lastError = err;
            }
        }
        if (lastError != null) {
            throw lastError;
        }
    }

    public PrincipalStylesheetModule compilePackage(Source source) throws XPathException {
        XSLPackage xslpackage;
        NodeInfo document;
        this.setMinimalPackageData();
        NodeInfo outermost = null;
        if (source instanceof NodeInfo) {
            NodeInfo root = (NodeInfo)source;
            if (root.getNodeKind() == 9) {
                document = root;
                outermost = document.iterateAxis(3, NodeKindTest.ELEMENT).next();
            } else if (root.getNodeKind() == 1) {
                document = root.getRoot();
                outermost = root;
            }
        }
        if (!(outermost instanceof XSLPackage)) {
            document = StylesheetModule.loadStylesheetModule(source, true, this, NestedIntegerValue.TWO);
            outermost = document.iterateAxis(3, NodeKindTest.ELEMENT).next();
        }
        if (outermost instanceof LiteralResultElement) {
            document = ((LiteralResultElement)outermost).makeStylesheet(true);
            outermost = document.iterateAxis(3, NodeKindTest.ELEMENT).next();
        }
        try {
            if (!(outermost instanceof XSLPackage)) {
                throw new XPathException("Outermost element must be xsl:package, xsl:stylesheet, or xsl:transform");
            }
            xslpackage = (XSLPackage)outermost;
        } catch (XPathException e) {
            if (!e.hasBeenReported()) {
                this.getCompilerInfo().getErrorReporter().report(new XmlProcessingException(e));
            }
            throw e;
        }
        if (TIMING) {
            this.timer.report("Built stylesheet documents");
        }
        CompilerInfo info = this.getCompilerInfo();
        StyleNodeFactory factory = this.getStyleNodeFactory(true);
        PrincipalStylesheetModule psm = factory.newPrincipalModule(xslpackage);
        StylesheetPackage pack = psm.getStylesheetPackage();
        pack.setVersion(xslpackage.getVersion());
        pack.setPackageVersion(xslpackage.getPackageVersion());
        pack.setPackageName(xslpackage.getName());
        pack.setSchemaAware(info.isSchemaAware() || this.isSchemaAware());
        pack.createFunctionLibrary();
        psm.getRuleManager().setCompilerInfo(info);
        this.setPrincipalStylesheetModule(psm);
        this.packageData = null;
        this.satisfyPackageDependencies(xslpackage);
        if (TIMING) {
            this.timer.report("Preparing package");
        }
        try {
            psm.preprocess(this);
        } catch (XPathException e) {
            info.getErrorReporter().report(new XmlProcessingException(e));
            throw e;
        }
        if (this.getErrorCount() == 0) {
            try {
                psm.fixup();
            } catch (XPathException e) {
                this.reportError(e);
            }
        }
        if (TIMING) {
            this.timer.report("Fixup");
        }
        if (this.getErrorCount() == 0) {
            try {
                psm.combineAttributeSets(this);
            } catch (XPathException e) {
                this.reportError(e);
            }
        }
        if (TIMING) {
            this.timer.report("Combine attribute sets");
        }
        if (this.getErrorCount() == 0) {
            try {
                psm.compile(this);
            } catch (XPathException e) {
                this.reportError(e);
            }
        }
        if (this.getErrorCount() == 0) {
            try {
                psm.complete();
            } catch (XPathException e) {
                this.reportError(e);
            }
        }
        if (TIMING) {
            this.timer.report("Completion");
        }
        psm.getStylesheetPackage().setCreatesSecondaryResultDocuments(this.createsSecondaryResultDocuments);
        if (this.isFallbackToNonStreaming()) {
            psm.getStylesheetPackage().setFallbackToNonStreaming();
        }
        if (TIMING) {
            this.timer.report("Streaming fallback");
        }
        return psm;
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    public CompilerInfo getCompilerInfo() {
        return this.compilerInfo;
    }

    public PackageData getPackageData() {
        if (this.packageData != null) {
            return this.packageData;
        }
        return this.principalStylesheetModule == null ? null : this.principalStylesheetModule.getStylesheetPackage();
    }

    public boolean isSchemaAware() {
        return this.schemaAware;
    }

    public void setSchemaAware(boolean schemaAware) {
        this.schemaAware = schemaAware;
        this.getPackageData().setSchemaAware(schemaAware);
    }

    public StyleNodeFactory getStyleNodeFactory(boolean topLevel) {
        StyleNodeFactory factory = this.getConfiguration().makeStyleNodeFactory(this);
        factory.setTopLevelModule(topLevel);
        return factory;
    }

    private void setPrincipalStylesheetModule(PrincipalStylesheetModule module) {
        this.principalStylesheetModule = module;
    }

    public PrincipalStylesheetModule getPrincipalStylesheetModule() {
        return this.principalStylesheetModule;
    }

    public void reportError(XmlProcessingError err) {
        ErrorReporter reporter = this.compilerInfo.getErrorReporter();
        if (reporter != null) {
            reporter.report(err);
        }
        ++this.errorCount;
        if (err.getFatalErrorMessage() != null) {
            throw new XmlProcessingAbort(err.getFatalErrorMessage());
        }
    }

    public void reportError(XPathException err) {
        err.setHostLanguage(HostLanguage.XSLT);
        ErrorReporter el = this.compilerInfo.getErrorReporter();
        if (el == null) {
            el = this.getConfiguration().makeErrorReporter();
        }
        if (!err.hasBeenReported()) {
            ++this.errorCount;
            try {
                el.report(new XmlProcessingException(err));
                err.setHasBeenReported(true);
            } catch (Exception exception) {}
        } else if (this.errorCount == 0) {
            ++this.errorCount;
        }
    }

    public int getErrorCount() {
        return this.errorCount;
    }

    public void reportWarning(XPathException err) {
        err.setHostLanguage(HostLanguage.XSLT);
        ErrorReporter reporter = this.compilerInfo.getErrorReporter();
        if (reporter == null) {
            reporter = this.getConfiguration().makeErrorReporter();
        }
        if (reporter != null) {
            XmlProcessingException error = new XmlProcessingException(err);
            error.setWarning(true);
            reporter.report(error);
        }
    }

    public void reportWarning(String message, String errorCode, Location location) {
        XmlProcessingIncident error = new XmlProcessingIncident(message, errorCode, location).asWarning();
        error.setHostLanguage(HostLanguage.XSLT);
        this.compilerInfo.getErrorReporter().report(error);
    }

    public void declareStaticVariable(StructuredQName name, GroundedValue value, NestedIntegerValue precedence, boolean isParam) throws XPathException {
        ValueAndPrecedence vp = this.staticVariables.get(name);
        if (vp != null) {
            if (vp.precedence.compareTo(precedence) < 0) {
                if (!this.valuesAreCompatible(value, vp.value)) {
                    throw new XPathException("Incompatible values assigned for static variable " + name.getDisplayName(), "XTSE3450");
                }
                if (vp.isParam != isParam) {
                    throw new XPathException("Static variable " + name.getDisplayName() + " cannot be redeclared as a param", "XTSE3450");
                }
            } else {
                return;
            }
        }
        this.staticVariables.put(name, new ValueAndPrecedence(value, precedence, isParam));
    }

    private boolean valuesAreCompatible(GroundedValue val0, GroundedValue val1) {
        if (val0.getLength() != val1.getLength()) {
            return false;
        }
        if (val0.getLength() == 1) {
            Item i0 = val0.head();
            Item i1 = val1.head();
            if (i0 instanceof AtomicValue) {
                return i1 instanceof AtomicValue && ((AtomicValue)i0).isIdentical((AtomicValue)i1);
            }
            if (i0 instanceof NodeInfo) {
                return i1 instanceof NodeInfo && i0.equals(i1);
            }
            return i0 == i1;
        }
        for (int i = 0; i < val0.getLength(); ++i) {
            if (this.valuesAreCompatible(val0.itemAt(i), val1.itemAt(i))) continue;
            return false;
        }
        return true;
    }

    public GroundedValue getStaticVariable(StructuredQName name) {
        ValueAndPrecedence vp = this.staticVariables.get(name);
        return vp == null ? null : vp.value;
    }

    public NestedIntegerValue getStaticVariablePrecedence(StructuredQName name) {
        ValueAndPrecedence vp = this.staticVariables.get(name);
        return vp == null ? null : vp.precedence;
    }

    public Map<DocumentKey, TreeInfo> getStylesheetModules() {
        return this.stylesheetModules;
    }

    public Stack<DocumentKey> getImportStack() {
        return this.importStack;
    }

    public QNameParser getQNameParser() {
        return this.qNameParser;
    }

    public boolean isPreScan() {
        return this.preScan;
    }

    public void setPreScan(boolean preScan) {
        this.preScan = preScan;
    }

    public boolean isCreatesSecondaryResultDocuments() {
        return this.createsSecondaryResultDocuments;
    }

    public void setCreatesSecondaryResultDocuments(boolean createsSecondaryResultDocuments) {
        this.createsSecondaryResultDocuments = createsSecondaryResultDocuments;
    }

    public boolean isLibraryPackage() {
        return this.libraryPackage;
    }

    public void setLibraryPackage(boolean libraryPackage) {
        this.libraryPackage = libraryPackage;
    }

    public void setParameter(StructuredQName name, GroundedValue seq) {
        this.suppliedParameters.put(name, seq);
    }

    public GlobalParameterSet getParameters() {
        return this.suppliedParameters;
    }

    public void clearParameters() {
        this.suppliedParameters = new GlobalParameterSet();
    }

    public boolean isFallbackToNonStreaming() {
        return this.fallbackToNonStreaming;
    }

    public void setFallbackToNonStreaming(boolean fallbackToNonStreaming) {
        this.fallbackToNonStreaming = fallbackToNonStreaming;
    }

    private static class ValueAndPrecedence {
        public GroundedValue value;
        public NestedIntegerValue precedence;
        public boolean isParam;

        public ValueAndPrecedence(GroundedValue v, NestedIntegerValue p, boolean isParam) {
            this.value = v;
            this.precedence = p;
            this.isParam = isParam;
        }
    }
}


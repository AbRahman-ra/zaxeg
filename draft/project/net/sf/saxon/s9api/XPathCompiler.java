/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.parser.OptimizerOptions;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltPackage;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.sxpath.XPathEvaluator;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.sxpath.XPathVariable;
import net.sf.saxon.trans.DecimalFormatManager;
import net.sf.saxon.trans.DecimalSymbols;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingIncident;
import net.sf.saxon.value.SequenceType;

public class XPathCompiler {
    private Processor processor;
    private XPathEvaluator evaluator;
    private IndependentContext env;
    private ItemType requiredContextItemType;
    private Map<String, XPathExecutable> cache = null;

    protected XPathCompiler(Processor processor) {
        this.processor = processor;
        this.evaluator = new XPathEvaluator(processor.getUnderlyingConfiguration());
        this.env = (IndependentContext)this.evaluator.getStaticContext();
    }

    public Processor getProcessor() {
        return this.processor;
    }

    public void setBackwardsCompatible(boolean option) {
        if (this.cache != null) {
            this.cache.clear();
        }
        this.env.setBackwardsCompatibilityMode(option);
    }

    public boolean isBackwardsCompatible() {
        return this.env.isInBackwardsCompatibleMode();
    }

    public void setSchemaAware(boolean schemaAware) {
        if (schemaAware && !this.processor.getUnderlyingConfiguration().isLicensedFeature(1)) {
            throw new UnsupportedOperationException("Schema processing requires a licensed Saxon-EE configuration");
        }
        this.env.setSchemaAware(schemaAware);
    }

    public boolean isSchemaAware() {
        return this.env.getPackageData().isSchemaAware();
    }

    public void setLanguageVersion(String value) {
        int version;
        if (this.cache != null) {
            this.cache.clear();
        }
        if ("1.0".equals(value)) {
            version = 20;
            this.env.setBackwardsCompatibilityMode(true);
        } else if ("2.0".equals(value)) {
            version = 20;
        } else if ("3.0".equals(value) || "3.05".equals(value)) {
            version = 30;
        } else if ("3.1".equals(value)) {
            version = 31;
        } else {
            throw new IllegalArgumentException("XPath version");
        }
        this.env.setXPathLanguageLevel(version);
        this.env.setDefaultFunctionLibrary(version);
    }

    public String getLanguageVersion() {
        if (this.env.getXPathVersion() == 20) {
            return "2.0";
        }
        if (this.env.getXPathVersion() == 30) {
            return "3.0";
        }
        if (this.env.getXPathVersion() == 31) {
            return "3.1";
        }
        throw new IllegalStateException("Unknown XPath version " + this.env.getXPathVersion());
    }

    public void setBaseURI(URI uri) {
        if (this.cache != null) {
            this.cache.clear();
        }
        if (uri == null) {
            this.env.setBaseURI(null);
        } else {
            if (!uri.isAbsolute()) {
                throw new IllegalArgumentException("Supplied base URI must be absolute");
            }
            this.env.setBaseURI(uri.toString());
        }
    }

    public URI getBaseURI() {
        try {
            return new URI(this.env.getStaticBaseURI());
        } catch (URISyntaxException err) {
            throw new IllegalStateException(err);
        }
    }

    public void setWarningHandler(ErrorReporter reporter) {
        this.env.setWarningHandler((String message, Location location) -> reporter.report(new XmlProcessingIncident((String)message, "SXWN9000", (Location)location).asWarning()));
    }

    public void declareNamespace(String prefix, String uri) {
        if (this.cache != null) {
            this.cache.clear();
        }
        this.env.declareNamespace(prefix, uri);
    }

    public void importSchemaNamespace(String uri) {
        if (this.cache != null) {
            this.cache.clear();
        }
        this.env.getImportedSchemaNamespaces().add(uri);
        this.env.setSchemaAware(true);
    }

    public void setAllowUndeclaredVariables(boolean allow) {
        if (this.cache != null) {
            this.cache.clear();
        }
        this.env.setAllowUndeclaredVariables(allow);
    }

    public boolean isAllowUndeclaredVariables() {
        return this.env.isAllowUndeclaredVariables();
    }

    public void declareVariable(QName qname) {
        if (this.cache != null) {
            this.cache.clear();
        }
        this.env.declareVariable(qname.getNamespaceURI(), qname.getLocalName());
    }

    public void declareVariable(QName qname, ItemType itemType, OccurrenceIndicator occurrences) {
        if (this.cache != null) {
            this.cache.clear();
        }
        XPathVariable var = this.env.declareVariable(qname.getNamespaceURI(), qname.getLocalName());
        var.setRequiredType(SequenceType.makeSequenceType(itemType.getUnderlyingItemType(), occurrences.getCardinality()));
    }

    public void addXsltFunctionLibrary(XsltPackage libraryPackage) {
        ((FunctionLibraryList)this.env.getFunctionLibrary()).addFunctionLibrary(libraryPackage.getUnderlyingPreparedPackage().getPublicFunctions());
    }

    public void setRequiredContextItemType(ItemType type) {
        this.requiredContextItemType = type;
        this.env.setRequiredContextItemType(type.getUnderlyingItemType());
    }

    public ItemType getRequiredContextItemType() {
        return this.requiredContextItemType;
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
        this.env.setDefaultCollationName(uri);
    }

    public void setCaching(boolean caching) {
        if (caching) {
            if (this.cache == null) {
                this.cache = new ConcurrentHashMap<String, XPathExecutable>();
            }
        } else {
            this.cache = null;
        }
    }

    public boolean isCaching() {
        return this.cache != null;
    }

    public void setFastCompilation(boolean fast) {
        if (fast) {
            this.env.setOptimizerOptions(new OptimizerOptions(0));
        } else {
            this.env.setOptimizerOptions(this.getProcessor().getUnderlyingConfiguration().getOptimizerOptions());
        }
    }

    public boolean isFastCompilation() {
        return this.env.getOptimizerOptions().getOptions() == 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public XPathExecutable compile(String source) throws SaxonApiException {
        Objects.requireNonNull(source);
        if (this.cache != null) {
            XPathCompiler xPathCompiler = this;
            synchronized (xPathCompiler) {
                XPathExecutable expr = this.cache.get(source);
                if (expr == null) {
                    expr = this.internalCompile(source);
                    this.cache.put(source, expr);
                }
                return expr;
            }
        }
        return this.internalCompile(source);
    }

    private XPathExecutable internalCompile(String source) throws SaxonApiException {
        try {
            this.env.getDecimalFormatManager().checkConsistency();
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
        XPathEvaluator eval = this.evaluator;
        IndependentContext ic = this.env;
        if (ic.isAllowUndeclaredVariables()) {
            eval = new XPathEvaluator(this.processor.getUnderlyingConfiguration());
            ic = new IndependentContext(this.env);
            eval.setStaticContext(ic);
            Iterator<XPathVariable> iter = this.env.iterateExternalVariables();
            while (iter.hasNext()) {
                XPathVariable var = iter.next();
                XPathVariable var2 = ic.declareVariable(var.getVariableQName());
                var2.setRequiredType(var.getRequiredType());
            }
        }
        try {
            XPathExpression cexp = eval.createExpression(source);
            return new XPathExecutable(cexp, this.processor, ic);
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public XdmValue evaluate(String expression, XdmItem contextItem) throws SaxonApiException {
        Objects.requireNonNull(expression);
        XPathSelector selector = this.compile(expression).load();
        if (contextItem != null) {
            selector.setContextItem(contextItem);
        }
        return selector.evaluate();
    }

    public XdmItem evaluateSingle(String expression, XdmItem contextItem) throws SaxonApiException {
        Objects.requireNonNull(expression);
        XPathSelector selector = this.compile(expression).load();
        if (contextItem != null) {
            selector.setContextItem(contextItem);
        }
        return selector.evaluateSingle();
    }

    public XPathExecutable compilePattern(String source) throws SaxonApiException {
        Objects.requireNonNull(source);
        try {
            this.env.getDecimalFormatManager().checkConsistency();
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
        try {
            XPathExpression cexp = this.evaluator.createPattern(source);
            return new XPathExecutable(cexp, this.processor, this.env);
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public void setDecimalFormatProperty(QName format, String property, String value) throws SaxonApiException {
        DecimalFormatManager dfm = this.env.getDecimalFormatManager();
        if (dfm == null) {
            dfm = new DecimalFormatManager(HostLanguage.XPATH, this.env.getXPathVersion());
            this.env.setDecimalFormatManager(dfm);
        }
        DecimalSymbols symbols = dfm.obtainNamedDecimalFormat(format.getStructuredQName());
        try {
            switch (property) {
                case "decimal-separator": {
                    symbols.setDecimalSeparator(value);
                    break;
                }
                case "grouping-separator": {
                    symbols.setGroupingSeparator(value);
                    break;
                }
                case "exponent-separator": {
                    symbols.setExponentSeparator(value);
                    break;
                }
                case "infinity": {
                    symbols.setInfinity(value);
                    break;
                }
                case "NaN": {
                    symbols.setNaN(value);
                    break;
                }
                case "minus-sign": {
                    symbols.setMinusSign(value);
                    break;
                }
                case "percent": {
                    symbols.setPercent(value);
                    break;
                }
                case "per-mille": {
                    symbols.setPerMille(value);
                    break;
                }
                case "zero-digit": {
                    symbols.setZeroDigit(value);
                    break;
                }
                case "digit": {
                    symbols.setDigit(value);
                    break;
                }
                case "pattern-separator": {
                    symbols.setPatternSeparator(value);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Unknown decimal format attribute " + property);
                }
            }
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public StaticContext getUnderlyingStaticContext() {
        return this.env;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.sxpath;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.EarlyEvaluationContext;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.functions.registry.ConstructorFunctionLibrary;
import net.sf.saxon.functions.registry.XPath20FunctionSet;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.UnprefixedElementMatchingPolicy;
import net.sf.saxon.trans.DecimalFormatManager;
import net.sf.saxon.trans.KeyManager;
import net.sf.saxon.trans.XmlProcessingIncident;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;

public abstract class AbstractStaticContext
implements StaticContext {
    private String baseURI = null;
    private Configuration config;
    private PackageData packageData;
    private Location containingLocation = Loc.NONE;
    private String defaultCollationName;
    private FunctionLibraryList libraryList = new FunctionLibraryList();
    private String defaultFunctionNamespace = "http://www.w3.org/2005/xpath-functions";
    private String defaultElementNamespace = "";
    private boolean backwardsCompatible = false;
    private int xpathLanguageLevel = 31;
    protected boolean usingDefaultFunctionLibrary;
    private Map<StructuredQName, ItemType> typeAliases = new HashMap<StructuredQName, ItemType>();
    private UnprefixedElementMatchingPolicy unprefixedElementPolicy = UnprefixedElementMatchingPolicy.DEFAULT_NAMESPACE;
    private BiConsumer<String, Location> warningHandler = (message, locator) -> {
        XmlProcessingIncident incident = new XmlProcessingIncident((String)message, "SXWN9000", (Location)locator).asWarning();
        this.config.makeErrorReporter().report(incident);
    };

    protected void setConfiguration(Configuration config) {
        this.config = config;
        this.defaultCollationName = config.getDefaultCollationName();
    }

    @Override
    public Configuration getConfiguration() {
        return this.config;
    }

    public void setPackageData(PackageData packageData) {
        this.packageData = packageData;
    }

    @Override
    public PackageData getPackageData() {
        return this.packageData;
    }

    public void setSchemaAware(boolean aware) {
        this.getPackageData().setSchemaAware(aware);
    }

    @Override
    public RetainedStaticContext makeRetainedStaticContext() {
        return new RetainedStaticContext(this);
    }

    protected final void setDefaultFunctionLibrary() {
        FunctionLibraryList lib = new FunctionLibraryList();
        lib.addFunctionLibrary(this.config.getXPath31FunctionSet());
        lib.addFunctionLibrary(this.getConfiguration().getBuiltInExtensionLibraryList());
        lib.addFunctionLibrary(new ConstructorFunctionLibrary(this.getConfiguration()));
        lib.addFunctionLibrary(this.config.getIntegratedFunctionLibrary());
        this.config.addExtensionBinders(lib);
        this.setFunctionLibrary(lib);
    }

    public final void setDefaultFunctionLibrary(int version) {
        FunctionLibraryList lib = new FunctionLibraryList();
        switch (version) {
            default: {
                lib.addFunctionLibrary(XPath20FunctionSet.getInstance());
                break;
            }
            case 30: 
            case 305: {
                lib.addFunctionLibrary(this.config.getXPath30FunctionSet());
                break;
            }
            case 31: {
                lib.addFunctionLibrary(this.config.getXPath31FunctionSet());
            }
        }
        lib.addFunctionLibrary(this.getConfiguration().getBuiltInExtensionLibraryList());
        lib.addFunctionLibrary(new ConstructorFunctionLibrary(this.getConfiguration()));
        lib.addFunctionLibrary(this.config.getIntegratedFunctionLibrary());
        this.config.addExtensionBinders(lib);
        this.setFunctionLibrary(lib);
    }

    protected final void addFunctionLibrary(FunctionLibrary library) {
        this.libraryList.addFunctionLibrary(library);
    }

    @Override
    public XPathContext makeEarlyEvaluationContext() {
        return new EarlyEvaluationContext(this.getConfiguration());
    }

    @Override
    public Location getContainingLocation() {
        return this.containingLocation;
    }

    public void setContainingLocation(Location location) {
        this.containingLocation = location;
    }

    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;
    }

    @Override
    public String getStaticBaseURI() {
        return this.baseURI == null ? "" : this.baseURI;
    }

    @Override
    public FunctionLibrary getFunctionLibrary() {
        return this.libraryList;
    }

    public void setFunctionLibrary(FunctionLibraryList lib) {
        this.libraryList = lib;
        this.usingDefaultFunctionLibrary = false;
    }

    public void setDefaultCollationName(String collationName) {
        this.defaultCollationName = collationName;
    }

    @Override
    public String getDefaultCollationName() {
        return this.defaultCollationName;
    }

    public void setWarningHandler(BiConsumer<String, Location> handler) {
        this.warningHandler = handler;
    }

    public BiConsumer<String, Location> getWarningHandler() {
        return this.warningHandler;
    }

    @Override
    public void issueWarning(String s, Location locator) {
        this.getWarningHandler().accept(s, locator);
    }

    @Override
    public String getSystemId() {
        return "";
    }

    @Override
    public String getDefaultElementNamespace() {
        return this.defaultElementNamespace;
    }

    public void setDefaultElementNamespace(String uri) {
        this.defaultElementNamespace = uri;
    }

    public void setDefaultFunctionNamespace(String uri) {
        this.defaultFunctionNamespace = uri;
    }

    @Override
    public String getDefaultFunctionNamespace() {
        return this.defaultFunctionNamespace;
    }

    public void setXPathLanguageLevel(int level) {
        this.xpathLanguageLevel = level;
    }

    @Override
    public int getXPathVersion() {
        return this.xpathLanguageLevel;
    }

    public void setBackwardsCompatibilityMode(boolean option) {
        this.backwardsCompatible = option;
    }

    @Override
    public boolean isInBackwardsCompatibleMode() {
        return this.backwardsCompatible;
    }

    public void setDecimalFormatManager(DecimalFormatManager manager) {
        this.getPackageData().setDecimalFormatManager(manager);
    }

    @Override
    public ItemType getRequiredContextItemType() {
        return AnyItemType.getInstance();
    }

    @Override
    public DecimalFormatManager getDecimalFormatManager() {
        DecimalFormatManager manager = this.getPackageData().getDecimalFormatManager();
        if (manager == null) {
            manager = new DecimalFormatManager(HostLanguage.XPATH, this.xpathLanguageLevel);
            this.getPackageData().setDecimalFormatManager(manager);
        }
        return manager;
    }

    @Override
    public KeyManager getKeyManager() {
        return this.getPackageData().getKeyManager();
    }

    public void setTypeAlias(StructuredQName name, ItemType type) {
        this.typeAliases.put(name, type);
    }

    @Override
    public ItemType resolveTypeAlias(StructuredQName typeName) {
        return this.typeAliases.get(typeName);
    }

    public void setUnprefixedElementMatchingPolicy(UnprefixedElementMatchingPolicy policy) {
        this.unprefixedElementPolicy = policy;
    }

    @Override
    public UnprefixedElementMatchingPolicy getUnprefixedElementMatchingPolicy() {
        return this.unprefixedElementPolicy;
    }
}


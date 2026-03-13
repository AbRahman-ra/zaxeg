/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.Set;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.EarlyEvaluationContext;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.GlobalVariableReference;
import net.sf.saxon.expr.LocalVariableReference;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.SuppliedParameterReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.parser.OptimizerOptions;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.functions.registry.VendorFunctionSetHE;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.UnprefixedElementMatchingPolicy;
import net.sf.saxon.style.SourceBinding;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.style.XSLGlobalVariable;
import net.sf.saxon.style.XSLOverride;
import net.sf.saxon.trans.DecimalFormatManager;
import net.sf.saxon.trans.KeyManager;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.AttributeLocation;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;

public class ExpressionContext
implements StaticContext {
    private StyleElement element;
    private StructuredQName attributeName;
    private Location containingLocation = null;
    private RetainedStaticContext retainedStaticContext = null;

    public ExpressionContext(StyleElement styleElement, StructuredQName attributeName) {
        this.element = styleElement;
        this.attributeName = attributeName;
    }

    @Override
    public Configuration getConfiguration() {
        return this.element.getConfiguration();
    }

    @Override
    public StylesheetPackage getPackageData() {
        return this.element.getPackageData();
    }

    public boolean isSchemaAware() {
        return this.element.isSchemaAware();
    }

    @Override
    public XPathContext makeEarlyEvaluationContext() {
        return new EarlyEvaluationContext(this.getConfiguration());
    }

    @Override
    public RetainedStaticContext makeRetainedStaticContext() {
        if (this.retainedStaticContext == null) {
            this.retainedStaticContext = this.element.changesRetainedStaticContext() || !(this.element.getParent() instanceof StyleElement) ? new RetainedStaticContext(this) : ((StyleElement)this.element.getParent()).getStaticContext().makeRetainedStaticContext();
        }
        return this.retainedStaticContext;
    }

    @Override
    public Location getContainingLocation() {
        if (this.containingLocation == null) {
            this.containingLocation = this.attributeName == null ? this.element : new AttributeLocation(this.element, this.attributeName);
        }
        return this.containingLocation;
    }

    @Override
    public void issueWarning(String s, Location locator) {
        this.element.compileWarning(s, "SXWN9000", locator);
    }

    @Override
    public String getSystemId() {
        return this.element.getSystemId();
    }

    @Override
    public String getStaticBaseURI() {
        return this.element.getBaseURI();
    }

    @Override
    public NamespaceResolver getNamespaceResolver() {
        return this.element.getAllNamespaces();
    }

    @Override
    public ItemType getRequiredContextItemType() {
        return AnyItemType.getInstance();
    }

    @Override
    public DecimalFormatManager getDecimalFormatManager() {
        return this.element.getCompilation().getPrincipalStylesheetModule().getDecimalFormatManager();
    }

    @Override
    public OptimizerOptions getOptimizerOptions() {
        return this.element.getCompilation().getCompilerInfo().getOptimizerOptions();
    }

    @Override
    public Expression bindVariable(StructuredQName qName) throws XPathException {
        SourceBinding sourceBinding = this.element.bindVariable(qName);
        if (sourceBinding == null) {
            AxisIterator catchers;
            StyleElement catcher;
            if (qName.hasURI("http://www.w3.org/1999/XSL/Transform") && qName.getLocalPart().equals("original")) {
                this.element.getXslOriginal(206);
                return new GlobalVariableReference(qName);
            }
            SymbolicName sn = new SymbolicName(206, qName);
            Component comp = this.element.getCompilation().getPrincipalStylesheetModule().getComponent(sn);
            if (comp != null) {
                this.element.iterateAxis(1).forEachOrFail(parent -> {
                    if (parent instanceof XSLGlobalVariable && ((XSLGlobalVariable)parent).getVariableQName().equals(qName)) {
                        XPathException err = new XPathException("Variable " + qName.getDisplayName() + " cannot be used within its own declaration", "XPST0008");
                        err.setIsStaticError(true);
                        throw err;
                    }
                });
                GlobalVariable var = (GlobalVariable)comp.getActor();
                GlobalVariableReference vref = new GlobalVariableReference(var);
                vref.setStaticType(var.getRequiredType(), null, 0);
                return vref;
            }
            if (this.getXPathVersion() >= 30 && qName.hasURI("http://www.w3.org/2005/xqt-errors") && (catcher = (StyleElement)(catchers = this.element.iterateAxis(1, new NameTest(1, 139, this.element.getNamePool()))).next()) != null) {
                for (StructuredQName errorVariable : StandardNames.errorVariables) {
                    if (!errorVariable.getLocalPart().equals(qName.getLocalPart())) continue;
                    SystemFunction f = VendorFunctionSetHE.getInstance().makeFunction("dynamic-error-info", 1);
                    return f.makeFunctionCall(new StringLiteral(qName.getLocalPart()));
                }
            }
            XPathException err = new XPathException("Variable " + qName.getDisplayName() + " has not been declared (or its declaration is not in scope)", "XPST0008");
            err.setIsStaticError(true);
            throw err;
        }
        if (sourceBinding.hasProperty(SourceBinding.BindingProperty.IMPLICITLY_DECLARED)) {
            SuppliedParameterReference supRef = new SuppliedParameterReference(0);
            supRef.setSuppliedType(sourceBinding.getDeclaredType());
            return supRef;
        }
        if (sourceBinding.hasProperty(SourceBinding.BindingProperty.GLOBAL)) {
            GlobalVariableReference var = new GlobalVariableReference(qName);
            GlobalVariable compiledVar = ((XSLGlobalVariable)sourceBinding.getSourceElement()).getCompiledVariable();
            if (compiledVar != null && this.element.getCompilation().getCompilerInfo().isJustInTimeCompilation()) {
                var.fixup(compiledVar);
                var.setStaticType(compiledVar.getRequiredType(), sourceBinding.getConstantValue(), 0);
            } else {
                sourceBinding.registerReference(var);
            }
            return var;
        }
        LocalVariableReference var = new LocalVariableReference(qName);
        sourceBinding.registerReference(var);
        return var;
    }

    @Override
    public FunctionLibrary getFunctionLibrary() {
        FunctionLibraryList lib = this.element.getContainingPackage().getFunctionLibrary();
        StyleElement containingOverride = this.element.findAncestorElement(186);
        if (containingOverride != null) {
            FunctionLibraryList libList = new FunctionLibraryList();
            libList.addFunctionLibrary(lib);
            ((XSLOverride)containingOverride).addXSLOverrideFunctionLibrary(libList);
            return libList;
        }
        return lib;
    }

    @Override
    public String getDefaultCollationName() {
        return this.element.getDefaultCollationName();
    }

    @Override
    public String getDefaultElementNamespace() {
        return this.element.getDefaultXPathNamespace();
    }

    @Override
    public String getDefaultFunctionNamespace() {
        return "http://www.w3.org/2005/xpath-functions";
    }

    @Override
    public boolean isInBackwardsCompatibleMode() {
        return this.element.xPath10ModeIsEnabled();
    }

    @Override
    public int getXPathVersion() {
        return this.getConfiguration().getConfigurationProperty(Feature.XPATH_VERSION_FOR_XSLT);
    }

    @Override
    public boolean isImportedSchema(String namespace) {
        return this.element.getPrincipalStylesheetModule().isImportedSchema(namespace);
    }

    @Override
    public Set<String> getImportedSchemaNamespaces() {
        return this.element.getPrincipalStylesheetModule().getImportedSchemaTable();
    }

    @Override
    public KeyManager getKeyManager() {
        return this.element.getCompilation().getPrincipalStylesheetModule().getKeyManager();
    }

    public StyleElement getStyleElement() {
        return this.element;
    }

    @Override
    public ItemType resolveTypeAlias(StructuredQName typeName) {
        return this.getPackageData().obtainTypeAliasManager().getItemType(typeName);
    }

    @Override
    public UnprefixedElementMatchingPolicy getUnprefixedElementMatchingPolicy() {
        return this.element.getCompilation().getCompilerInfo().getUnprefixedElementMatchingPolicy();
    }
}


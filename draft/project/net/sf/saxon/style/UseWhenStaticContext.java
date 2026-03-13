/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.Collections;
import java.util.Set;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.functions.registry.ConstructorFunctionLibrary;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.sxpath.AbstractStaticContext;
import net.sf.saxon.trans.DecimalFormatManager;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingIncident;
import net.sf.saxon.type.ItemType;

public class UseWhenStaticContext
extends AbstractStaticContext
implements StaticContext {
    private NamespaceResolver namespaceContext;
    private FunctionLibrary functionLibrary;
    private Compilation compilation;

    public UseWhenStaticContext(Compilation compilation, NamespaceResolver namespaceContext) {
        Configuration config = compilation.getConfiguration();
        this.setConfiguration(config);
        this.compilation = compilation;
        this.setPackageData(compilation.getPackageData());
        this.namespaceContext = namespaceContext;
        this.setXPathLanguageLevel(31);
        FunctionLibraryList lib = new FunctionLibraryList();
        lib.addFunctionLibrary(config.getUseWhenFunctionSet());
        lib.addFunctionLibrary(this.getConfiguration().getBuiltInExtensionLibraryList());
        lib.addFunctionLibrary(new ConstructorFunctionLibrary(this.getConfiguration()));
        lib.addFunctionLibrary(config.getIntegratedFunctionLibrary());
        config.addExtensionBinders(lib);
        this.functionLibrary = lib;
    }

    @Override
    public RetainedStaticContext makeRetainedStaticContext() {
        return new RetainedStaticContext(this);
    }

    public Compilation getCompilation() {
        return this.compilation;
    }

    @Override
    public void issueWarning(String s, Location locator) {
        this.compilation.getCompilerInfo().getErrorReporter().report(new XmlProcessingIncident(s, "SXWN9000", locator).asWarning());
    }

    @Override
    public String getSystemId() {
        return this.getStaticBaseURI();
    }

    @Override
    public Expression bindVariable(StructuredQName qName) throws XPathException {
        GroundedValue val = this.compilation.getStaticVariable(qName);
        if (val != null) {
            return Literal.makeLiteral(val);
        }
        XPathException err = new XPathException("Variables (other than XSLT 3.0 static variables) cannot be used in a static expression: " + qName.getDisplayName());
        err.setErrorCode("XPST0008");
        err.setIsStaticError(true);
        throw err;
    }

    @Override
    public FunctionLibrary getFunctionLibrary() {
        return this.functionLibrary;
    }

    @Override
    public String getDefaultCollationName() {
        return "http://www.w3.org/2005/xpath-functions/collation/codepoint";
    }

    @Override
    public String getDefaultFunctionNamespace() {
        return "http://www.w3.org/2005/xpath-functions";
    }

    @Override
    public boolean isInBackwardsCompatibleMode() {
        return false;
    }

    @Override
    public boolean isImportedSchema(String namespace) {
        return false;
    }

    @Override
    public Set<String> getImportedSchemaNamespaces() {
        return Collections.emptySet();
    }

    @Override
    public NamespaceResolver getNamespaceResolver() {
        return this.namespaceContext;
    }

    @Override
    public DecimalFormatManager getDecimalFormatManager() {
        return null;
    }

    public int getColumnNumber() {
        return 0;
    }

    public String getPublicId() {
        return null;
    }

    public int getLineNumber() {
        return -1;
    }

    @Override
    public ItemType resolveTypeAlias(StructuredQName typeName) {
        return null;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.Set;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.OptimizerOptions;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.UnprefixedElementMatchingPolicy;
import net.sf.saxon.trans.DecimalFormatManager;
import net.sf.saxon.trans.KeyManager;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;

public interface StaticContext {
    public Configuration getConfiguration();

    public PackageData getPackageData();

    public XPathContext makeEarlyEvaluationContext();

    public RetainedStaticContext makeRetainedStaticContext();

    public Location getContainingLocation();

    public void issueWarning(String var1, Location var2);

    public String getSystemId();

    public String getStaticBaseURI();

    public Expression bindVariable(StructuredQName var1) throws XPathException;

    public FunctionLibrary getFunctionLibrary();

    public String getDefaultCollationName();

    public String getDefaultElementNamespace();

    default public UnprefixedElementMatchingPolicy getUnprefixedElementMatchingPolicy() {
        return UnprefixedElementMatchingPolicy.DEFAULT_NAMESPACE;
    }

    public String getDefaultFunctionNamespace();

    public boolean isInBackwardsCompatibleMode();

    public boolean isImportedSchema(String var1);

    public Set<String> getImportedSchemaNamespaces();

    public NamespaceResolver getNamespaceResolver();

    public ItemType getRequiredContextItemType();

    public DecimalFormatManager getDecimalFormatManager();

    public int getXPathVersion();

    public KeyManager getKeyManager();

    public ItemType resolveTypeAlias(StructuredQName var1);

    default public OptimizerOptions getOptimizerOptions() {
        return this.getConfiguration().getOptimizerOptions();
    }
}


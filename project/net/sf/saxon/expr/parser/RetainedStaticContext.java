/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.parser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import net.sf.saxon.Configuration;
import net.sf.saxon.Version;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.trans.DecimalFormatManager;
import net.sf.saxon.trans.XPathException;

public class RetainedStaticContext
implements NamespaceResolver {
    private Configuration config;
    private PackageData packageData;
    private URI staticBaseUri;
    private String staticBaseUriString;
    private String defaultCollationName;
    private NamespaceResolver namespaces;
    private String defaultFunctionNamespace = "http://www.w3.org/2005/xpath-functions";
    private String defaultElementNamespace;
    private DecimalFormatManager decimalFormatManager;
    private boolean backwardsCompatibility;

    public RetainedStaticContext(Configuration config) {
        this.config = config;
        this.packageData = new PackageData(config);
        this.namespaces = NamespaceMap.emptyMap();
        this.defaultCollationName = "http://www.w3.org/2005/xpath-functions/collation/codepoint";
    }

    public RetainedStaticContext(StaticContext sc) {
        this.config = sc.getConfiguration();
        this.packageData = sc.getPackageData();
        if (sc.getStaticBaseURI() != null) {
            this.staticBaseUriString = sc.getStaticBaseURI();
            try {
                this.staticBaseUri = ExpressionTool.getBaseURI(sc, null, true);
            } catch (XPathException e) {
                this.staticBaseUri = null;
            }
        }
        this.defaultCollationName = sc.getDefaultCollationName();
        this.decimalFormatManager = sc.getDecimalFormatManager();
        this.defaultElementNamespace = sc.getDefaultElementNamespace();
        this.defaultFunctionNamespace = sc.getDefaultFunctionNamespace();
        this.backwardsCompatibility = sc.isInBackwardsCompatibleMode();
        if (!Version.platform.JAXPStaticContextCheck(this, sc)) {
            NamespaceResolver resolver = sc.getNamespaceResolver();
            if (resolver instanceof NamespaceMap) {
                this.namespaces = resolver;
            } else {
                NamespaceMap map = NamespaceMap.emptyMap();
                Iterator<String> it = resolver.iteratePrefixes();
                while (it.hasNext()) {
                    String prefix = it.next();
                    if (prefix.equals("xml")) continue;
                    map = map.put(prefix, resolver.getURIForPrefix(prefix, true));
                }
                this.namespaces = map;
            }
        }
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    public void setPackageData(PackageData packageData) {
        this.packageData = packageData;
    }

    public PackageData getPackageData() {
        return this.packageData;
    }

    public void setStaticBaseUriString(String baseUri) {
        if (baseUri != null) {
            this.staticBaseUriString = baseUri;
            try {
                this.staticBaseUri = new URI(baseUri);
            } catch (URISyntaxException e) {
                this.staticBaseUri = null;
            }
        }
    }

    public URI getStaticBaseUri() throws XPathException {
        if (this.staticBaseUri == null) {
            if (this.staticBaseUriString == null) {
                return null;
            }
            throw new XPathException("Supplied static base URI " + this.staticBaseUriString + " is not a valid URI");
        }
        return this.staticBaseUri;
    }

    public String getStaticBaseUriString() {
        return this.staticBaseUriString;
    }

    public String getDefaultCollationName() {
        return this.defaultCollationName;
    }

    public void setDefaultCollationName(String defaultCollationName) {
        this.defaultCollationName = defaultCollationName;
    }

    public String getDefaultFunctionNamespace() {
        return this.defaultFunctionNamespace;
    }

    public void setDefaultFunctionNamespace(String defaultFunctionNamespace) {
        this.defaultFunctionNamespace = defaultFunctionNamespace;
    }

    public String getDefaultElementNamespace() {
        return this.defaultElementNamespace == null ? "" : this.defaultElementNamespace;
    }

    public void setDefaultElementNamespace(String ns) {
        this.defaultElementNamespace = ns;
    }

    public DecimalFormatManager getDecimalFormatManager() {
        return this.decimalFormatManager;
    }

    public void setDecimalFormatManager(DecimalFormatManager decimalFormatManager) {
        this.decimalFormatManager = decimalFormatManager;
    }

    public boolean isBackwardsCompatibility() {
        return this.backwardsCompatibility;
    }

    public void setBackwardsCompatibility(boolean backwardsCompatibility) {
        this.backwardsCompatibility = backwardsCompatibility;
    }

    public void declareNamespace(String prefix, String uri) {
        if (!(this.namespaces instanceof NamespaceMap)) {
            throw new UnsupportedOperationException();
        }
        this.namespaces = ((NamespaceMap)this.namespaces).put(prefix, uri);
    }

    @Override
    public String getURIForPrefix(String prefix, boolean useDefault) {
        return this.namespaces.getURIForPrefix(prefix, useDefault);
    }

    @Override
    public Iterator<String> iteratePrefixes() {
        return this.namespaces.iteratePrefixes();
    }

    public boolean declaresSameNamespaces(RetainedStaticContext other) {
        return this.namespaces.equals(other.namespaces);
    }

    public int hashCode() {
        int h = -2074620978;
        if (this.staticBaseUriString != null) {
            h ^= this.staticBaseUriString.hashCode();
        }
        h ^= this.defaultCollationName.hashCode();
        h ^= this.defaultFunctionNamespace.hashCode();
        return h ^= this.namespaces.hashCode();
    }

    public boolean equals(Object other) {
        if (!(other instanceof RetainedStaticContext)) {
            return false;
        }
        RetainedStaticContext r = (RetainedStaticContext)other;
        return ExpressionTool.equalOrNull(this.staticBaseUriString, r.staticBaseUriString) && this.defaultCollationName.equals(r.defaultCollationName) && this.defaultFunctionNamespace.equals(r.defaultFunctionNamespace) && this.namespaces.equals(r.namespaces);
    }

    public void setNamespaces(NamespaceResolver namespaces) {
        this.namespaces = namespaces;
    }
}


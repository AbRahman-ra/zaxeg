/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.xpath;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.Source;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.sxpath.AbstractStaticContext;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaException;
import net.sf.saxon.xpath.JAXPVariableReference;
import net.sf.saxon.xpath.XPathFunctionLibrary;

public class JAXPXPathStaticContext
extends AbstractStaticContext
implements NamespaceResolver {
    private SlotManager stackFrameMap;
    private XPathFunctionLibrary xpathFunctionLibrary;
    private NamespaceContext namespaceContext = new MinimalNamespaceContext();
    private XPathVariableResolver variableResolver;

    public JAXPXPathStaticContext(Configuration config) {
        this.setConfiguration(config);
        this.stackFrameMap = config.makeSlotManager();
        this.setDefaultFunctionLibrary(31);
        this.xpathFunctionLibrary = new XPathFunctionLibrary();
        this.addFunctionLibrary(this.xpathFunctionLibrary);
        this.setPackageData(new PackageData(this.getConfiguration()));
    }

    public void setNamespaceContext(NamespaceContext context) {
        this.namespaceContext = context;
    }

    public NamespaceContext getNamespaceContext() {
        return this.namespaceContext;
    }

    public SlotManager getStackFrameMap() {
        return this.stackFrameMap;
    }

    public void setXPathVariableResolver(XPathVariableResolver resolver) {
        this.variableResolver = resolver;
    }

    public XPathVariableResolver getXPathVariableResolver() {
        return this.variableResolver;
    }

    public void setXPathFunctionResolver(XPathFunctionResolver xPathFunctionResolver) {
        if (this.xpathFunctionLibrary != null) {
            this.xpathFunctionLibrary.setXPathFunctionResolver(xPathFunctionResolver);
        }
    }

    public XPathFunctionResolver getXPathFunctionResolver() {
        if (this.xpathFunctionLibrary != null) {
            return this.xpathFunctionLibrary.getXPathFunctionResolver();
        }
        return null;
    }

    @Override
    public NamespaceResolver getNamespaceResolver() {
        return this;
    }

    @Override
    public String getURIForPrefix(String prefix, boolean useDefault) {
        if (prefix.isEmpty()) {
            if (useDefault) {
                return this.getDefaultElementNamespace();
            }
            return "";
        }
        return this.namespaceContext.getNamespaceURI(prefix);
    }

    @Override
    public Iterator<String> iteratePrefixes() {
        if (this.namespaceContext instanceof NamespaceResolver) {
            return ((NamespaceResolver)((Object)this.namespaceContext)).iteratePrefixes();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public final Expression bindVariable(StructuredQName qName) throws XPathException {
        if (this.variableResolver != null) {
            return new JAXPVariableReference(qName, this.variableResolver);
        }
        throw new XPathException("Variable is used in XPath expression, but no JAXP VariableResolver is available");
    }

    public void importSchema(Source source) throws SchemaException {
        this.getConfiguration().addSchemaSource(source, this.getConfiguration().makeErrorReporter());
        this.setSchemaAware(true);
    }

    @Override
    public boolean isImportedSchema(String namespace) {
        return this.getConfiguration().isSchemaAvailable(namespace);
    }

    @Override
    public Set<String> getImportedSchemaNamespaces() {
        return this.getConfiguration().getImportedNamespaces();
    }

    private static class MinimalNamespaceContext
    implements NamespaceContext,
    NamespaceResolver {
        private MinimalNamespaceContext() {
        }

        @Override
        public String getNamespaceURI(String prefix) {
            if (prefix == null) {
                throw new IllegalArgumentException("prefix");
            }
            if (prefix.equals("")) {
                return "";
            }
            if (prefix.equals("xml")) {
                return "http://www.w3.org/XML/1998/namespace";
            }
            if (prefix.equals("xs")) {
                return "http://www.w3.org/2001/XMLSchema";
            }
            if (prefix.equals("xsi")) {
                return "http://www.w3.org/2001/XMLSchema-instance";
            }
            if (prefix.equals("saxon")) {
                return "http://saxon.sf.net/";
            }
            return null;
        }

        @Override
        public String getPrefix(String namespaceURI) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator getPrefixes(String namespaceURI) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<String> iteratePrefixes() {
            String[] prefixes = new String[]{"", "xml", "xs", "xsi", "saxon"};
            return Arrays.asList(prefixes).iterator();
        }

        @Override
        public String getURIForPrefix(String prefix, boolean useDefault) {
            return this.getNamespaceURI(prefix);
        }
    }
}


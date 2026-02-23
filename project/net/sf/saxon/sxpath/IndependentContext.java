/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.sxpath;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LocalVariableReference;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.parser.OptimizerOptions;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.sxpath.AbstractStaticContext;
import net.sf.saxon.sxpath.XPathStaticContext;
import net.sf.saxon.sxpath.XPathVariable;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.QNameValue;

public class IndependentContext
extends AbstractStaticContext
implements XPathStaticContext,
NamespaceResolver {
    protected HashMap<String, String> namespaces = new HashMap(10);
    protected HashMap<StructuredQName, XPathVariable> variables = new HashMap(20);
    protected NamespaceResolver externalResolver = null;
    protected ItemType requiredContextItemType = AnyItemType.getInstance();
    protected Set<String> importedSchemaNamespaces = new HashSet<String>();
    protected boolean autoDeclare = false;
    protected Executable executable;
    protected RetainedStaticContext retainedStaticContext;
    protected OptimizerOptions optimizerOptions;
    protected boolean parentlessContextItem;

    public IndependentContext() {
        this(new Configuration());
    }

    public IndependentContext(Configuration config) {
        this.setConfiguration(config);
        this.clearNamespaces();
        this.setDefaultFunctionLibrary(31);
        this.usingDefaultFunctionLibrary = true;
        this.setDefaultCollationName(config.getDefaultCollationName());
        this.setOptimizerOptions(config.getOptimizerOptions());
        PackageData pd = new PackageData(config);
        pd.setHostLanguage(HostLanguage.XPATH);
        pd.setSchemaAware(false);
        this.setPackageData(pd);
    }

    public IndependentContext(IndependentContext ic) {
        this(ic.getConfiguration());
        this.setPackageData(ic.getPackageData());
        this.setBaseURI(ic.getStaticBaseURI());
        this.setContainingLocation(ic.getContainingLocation());
        this.setDefaultElementNamespace(ic.getDefaultElementNamespace());
        this.setDefaultFunctionNamespace(ic.getDefaultFunctionNamespace());
        this.setBackwardsCompatibilityMode(ic.isInBackwardsCompatibleMode());
        this.namespaces = new HashMap<String, String>(ic.namespaces);
        this.variables = new HashMap(10);
        FunctionLibraryList libList = (FunctionLibraryList)ic.getFunctionLibrary();
        if (libList != null) {
            this.setFunctionLibrary((FunctionLibraryList)libList.copy());
        }
        this.setImportedSchemaNamespaces(ic.importedSchemaNamespaces);
        this.externalResolver = ic.externalResolver;
        this.autoDeclare = ic.autoDeclare;
        this.setXPathLanguageLevel(ic.getXPathVersion());
        this.requiredContextItemType = ic.requiredContextItemType;
        this.setExecutable(ic.getExecutable());
    }

    @Override
    public RetainedStaticContext makeRetainedStaticContext() {
        if (this.retainedStaticContext == null) {
            this.retainedStaticContext = new RetainedStaticContext(this);
        }
        return this.retainedStaticContext;
    }

    public void declareNamespace(String prefix, String uri) {
        if (prefix == null) {
            throw new NullPointerException("Null prefix supplied to declareNamespace()");
        }
        if (uri == null) {
            throw new NullPointerException("Null namespace URI supplied to declareNamespace()");
        }
        if ("".equals(prefix)) {
            this.setDefaultElementNamespace(uri);
        } else {
            this.namespaces.put(prefix, uri);
        }
    }

    @Override
    public void setDefaultElementNamespace(String uri) {
        if (uri == null) {
            uri = "";
        }
        super.setDefaultElementNamespace(uri);
        this.namespaces.put("", uri);
    }

    public void clearNamespaces() {
        this.namespaces.clear();
        this.declareNamespace("xml", "http://www.w3.org/XML/1998/namespace");
        this.declareNamespace("xsl", "http://www.w3.org/1999/XSL/Transform");
        this.declareNamespace("saxon", "http://saxon.sf.net/");
        this.declareNamespace("xs", "http://www.w3.org/2001/XMLSchema");
        this.declareNamespace("", "");
    }

    public void clearAllNamespaces() {
        this.namespaces.clear();
        this.declareNamespace("xml", "http://www.w3.org/XML/1998/namespace");
        this.declareNamespace("", "");
    }

    public void setNamespaces(NodeInfo node) {
        this.namespaces.clear();
        int kind = node.getNodeKind();
        if (kind == 2 || kind == 3 || kind == 8 || kind == 7 || kind == 13) {
            node = node.getParent();
        }
        if (node == null) {
            return;
        }
        AxisIterator iter = node.iterateAxis(8);
        NodeInfo ns;
        while ((ns = iter.next()) != null) {
            String prefix = ns.getLocalPart();
            if ("".equals(prefix)) {
                this.setDefaultElementNamespace(ns.getStringValue());
                continue;
            }
            this.declareNamespace(ns.getLocalPart(), ns.getStringValue());
        }
        return;
    }

    @Override
    public void setNamespaceResolver(NamespaceResolver resolver) {
        this.externalResolver = resolver;
    }

    public void setAllowUndeclaredVariables(boolean allow) {
        this.autoDeclare = allow;
    }

    public boolean isAllowUndeclaredVariables() {
        return this.autoDeclare;
    }

    @Override
    public XPathVariable declareVariable(QNameValue qname) {
        return this.declareVariable(qname.getStructuredQName());
    }

    @Override
    public XPathVariable declareVariable(String namespaceURI, String localName) {
        StructuredQName qName = new StructuredQName("", namespaceURI, localName);
        return this.declareVariable(qName);
    }

    public XPathVariable declareVariable(StructuredQName qName) {
        XPathVariable var = this.variables.get(qName);
        if (var != null) {
            return var;
        }
        var = XPathVariable.make(qName);
        int slot = this.variables.size();
        var.setSlotNumber(slot);
        this.variables.put(qName, var);
        return var;
    }

    public Iterator<XPathVariable> iterateExternalVariables() {
        return this.variables.values().iterator();
    }

    public XPathVariable getExternalVariable(StructuredQName qName) {
        return this.variables.get(qName);
    }

    public int getSlotNumber(QNameValue qname) {
        StructuredQName sq = qname.getStructuredQName();
        XPathVariable var = this.variables.get(sq);
        if (var == null) {
            return -1;
        }
        return var.getLocalSlotNumber();
    }

    @Override
    public NamespaceResolver getNamespaceResolver() {
        if (this.externalResolver != null) {
            return this.externalResolver;
        }
        return this;
    }

    @Override
    public String getURIForPrefix(String prefix, boolean useDefault) {
        if (this.externalResolver != null) {
            return this.externalResolver.getURIForPrefix(prefix, useDefault);
        }
        if (prefix.isEmpty()) {
            return useDefault ? this.getDefaultElementNamespace() : "";
        }
        return this.namespaces.get(prefix);
    }

    @Override
    public Iterator<String> iteratePrefixes() {
        if (this.externalResolver != null) {
            return this.externalResolver.iteratePrefixes();
        }
        return this.namespaces.keySet().iterator();
    }

    @Override
    public Expression bindVariable(StructuredQName qName) throws XPathException {
        XPathVariable var = this.variables.get(qName);
        if (var == null) {
            if (this.autoDeclare) {
                return new LocalVariableReference(this.declareVariable(qName));
            }
            throw new XPathException("Undeclared variable in XPath expression: $" + qName.getClarkName(), "XPST0008");
        }
        return new LocalVariableReference(var);
    }

    @Override
    public SlotManager getStackFrameMap() {
        SlotManager map = this.getConfiguration().makeSlotManager();
        XPathVariable[] va = new XPathVariable[this.variables.size()];
        XPathVariable[] xPathVariableArray = this.variables.values().iterator();
        while (xPathVariableArray.hasNext()) {
            XPathVariable var;
            va[var.getLocalSlotNumber()] = var = xPathVariableArray.next();
        }
        for (XPathVariable v : va) {
            map.allocateSlotNumber(v.getVariableQName());
        }
        return map;
    }

    public Collection<XPathVariable> getDeclaredVariables() {
        return this.variables.values();
    }

    @Override
    public boolean isImportedSchema(String namespace) {
        return this.importedSchemaNamespaces.contains(namespace);
    }

    @Override
    public Set<String> getImportedSchemaNamespaces() {
        return this.importedSchemaNamespaces;
    }

    public void setImportedSchemaNamespaces(Set<String> namespaces) {
        this.importedSchemaNamespaces = namespaces;
        if (!namespaces.isEmpty()) {
            this.setSchemaAware(true);
        }
    }

    public void setRequiredContextItemType(ItemType type) {
        this.requiredContextItemType = type;
    }

    @Override
    public ItemType getRequiredContextItemType() {
        return this.requiredContextItemType;
    }

    public void setOptimizerOptions(OptimizerOptions options) {
        this.optimizerOptions = options;
    }

    @Override
    public OptimizerOptions getOptimizerOptions() {
        return this.optimizerOptions;
    }

    public void setExecutable(Executable exec) {
        this.executable = exec;
    }

    public Executable getExecutable() {
        return this.executable;
    }

    public int getColumnNumber() {
        return -1;
    }

    public String getPublicId() {
        return null;
    }

    public int getLineNumber() {
        return -1;
    }

    @Override
    public boolean isContextItemParentless() {
        return this.parentlessContextItem;
    }

    public void setContextItemParentless(boolean parentless) {
        this.parentlessContextItem = parentless;
    }
}


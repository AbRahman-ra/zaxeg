/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LocalVariableReference;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.instruct.WithParam;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.expr.sort.LRUCache;
import net.sf.saxon.functions.ExecutableFunctionLibrary;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.functions.registry.BuiltInFunctionSet;
import net.sf.saxon.functions.registry.XPath31FunctionSet;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.style.PublicStylesheetFunctionLibrary;
import net.sf.saxon.style.StylesheetFunctionLibrary;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.sxpath.XPathVariable;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.tree.iter.ManualIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public final class EvaluateInstr
extends Expression {
    private Operand xpathOp;
    private SequenceType requiredType;
    private Operand contextItemOp;
    private Operand baseUriOp;
    private Operand namespaceContextOp;
    private Operand schemaAwareOp;
    private Operand optionsOp;
    private Set<String> importedSchemaNamespaces;
    private WithParam[] actualParams;
    private Operand dynamicParamsOp;
    private String defaultXPathNamespace = null;

    public EvaluateInstr(Expression xpath, SequenceType requiredType, Expression contextItemExpr, Expression baseUriExpr, Expression namespaceContextExpr, Expression schemaAwareExpr) {
        if (xpath != null) {
            this.xpathOp = new Operand(this, xpath, OperandRole.SINGLE_ATOMIC);
        }
        if (contextItemExpr != null) {
            this.contextItemOp = new Operand(this, contextItemExpr, OperandRole.NAVIGATE);
        }
        if (baseUriExpr != null) {
            this.baseUriOp = new Operand(this, baseUriExpr, OperandRole.SINGLE_ATOMIC);
        }
        if (namespaceContextExpr != null) {
            this.namespaceContextOp = new Operand(this, namespaceContextExpr, OperandRole.INSPECT);
        }
        if (schemaAwareExpr != null) {
            this.schemaAwareOp = new Operand(this, schemaAwareExpr, OperandRole.SINGLE_ATOMIC);
        }
        this.requiredType = requiredType;
    }

    public void setOptionsExpression(Expression options) {
        this.optionsOp = new Operand(this, options, OperandRole.ABSORB);
    }

    public void setActualParameters(WithParam[] params) {
        this.setActualParams(params);
    }

    public void setDefaultXPathNamespace(String defaultXPathNamespace) {
        this.defaultXPathNamespace = defaultXPathNamespace;
    }

    @Override
    public boolean isInstruction() {
        return true;
    }

    public void importSchemaNamespace(String ns) {
        if (this.importedSchemaNamespaces == null) {
            this.importedSchemaNamespaces = new HashSet<String>();
        }
        this.importedSchemaNamespaces.add(ns);
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.importedSchemaNamespaces = visitor.getStaticContext().getImportedSchemaNamespaces();
        this.typeCheckChildren(visitor, contextInfo);
        WithParam.typeCheck(this.getActualParams(), visitor, contextInfo);
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        this.optimizeChildren(visitor, contextItemType);
        return this;
    }

    @Override
    public final ItemType getItemType() {
        return this.requiredType.getPrimaryType();
    }

    @Override
    protected int computeCardinality() {
        return this.requiredType.getCardinality();
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        throw new UnsupportedOperationException("Cannot do document projection when xsl:evaluate is used");
    }

    @Override
    public int getIntrinsicDependencies() {
        return 639;
    }

    @Override
    public Iterable<Operand> operands() {
        ArrayList<Operand> sub = new ArrayList<Operand>(8);
        if (this.xpathOp != null) {
            sub.add(this.xpathOp);
        }
        if (this.contextItemOp != null) {
            sub.add(this.contextItemOp);
        }
        if (this.baseUriOp != null) {
            sub.add(this.baseUriOp);
        }
        if (this.namespaceContextOp != null) {
            sub.add(this.namespaceContextOp);
        }
        if (this.schemaAwareOp != null) {
            sub.add(this.schemaAwareOp);
        }
        if (this.dynamicParamsOp != null) {
            sub.add(this.dynamicParamsOp);
        }
        if (this.optionsOp != null) {
            sub.add(this.optionsOp);
        }
        WithParam.gatherOperands(this, this.getActualParams(), sub);
        return sub;
    }

    @Override
    public int getImplementationMethod() {
        return 2;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        EvaluateInstr e2 = new EvaluateInstr(this.getXpath().copy(rebindings), this.requiredType, this.getContextItemExpr().copy(rebindings), this.getBaseUriExpr() == null ? null : this.getBaseUriExpr().copy(rebindings), this.getNamespaceContextExpr() == null ? null : this.getNamespaceContextExpr().copy(rebindings), this.getSchemaAwareExpr() == null ? null : this.getSchemaAwareExpr().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, e2);
        e2.setRetainedStaticContext(this.getRetainedStaticContext());
        e2.importedSchemaNamespaces = this.importedSchemaNamespaces;
        e2.setActualParams(WithParam.copy(e2, this.getActualParams(), rebindings));
        if (this.optionsOp != null) {
            e2.setOptionsExpression(this.optionsOp.getChildExpression().copy(rebindings));
        }
        if (this.dynamicParamsOp != null) {
            e2.setDynamicParams(this.dynamicParamsOp.getChildExpression().copy(rebindings));
        }
        return e2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public SequenceIterator iterate(final XPathContext context) throws XPathException {
        LRUCache<String, Object[]> cache;
        Controller controller;
        boolean isSchemaAware;
        String schemaAwareAttr;
        Configuration config = context.getConfiguration();
        if (config.getBooleanProperty(Feature.DISABLE_XSL_EVALUATE)) {
            throw new XPathException("xsl:evaluate has been disabled", "XTDE3175");
        }
        final String exprText = this.getXpath().evaluateAsString(context).toString();
        String baseUri = this.getBaseUriExpr() == null ? this.getStaticBaseURIString() : Whitespace.trim(this.getBaseUriExpr().evaluateAsString(context));
        Item focus = this.getContextItemExpr().evaluateItem(context);
        NodeInfo namespaceContextBase = null;
        if (this.getNamespaceContextExpr() != null) {
            namespaceContextBase = (NodeInfo)this.getNamespaceContextExpr().evaluateItem(context);
        }
        if ("yes".equals(schemaAwareAttr = Whitespace.trim(this.getSchemaAwareExpr().evaluateAsString(context))) || "true".equals(schemaAwareAttr) || "1".equals(schemaAwareAttr)) {
            isSchemaAware = true;
        } else if ("no".equals(schemaAwareAttr) || "false".equals(schemaAwareAttr) || "0".equals(schemaAwareAttr)) {
            isSchemaAware = false;
        } else {
            XPathException err = new XPathException("The schema-aware attribute of xsl:evaluate must be yes|no|true|false|0|1");
            err.setErrorCode("XTDE0030");
            err.setLocation(this.getLocation());
            err.setXPathContext(context);
            throw err;
        }
        Expression expr = null;
        SlotManager slotMap = null;
        FastStringBuffer fsb = new FastStringBuffer(exprText.length() + (baseUri == null ? 4 : baseUri.length()) + 40);
        fsb.append(baseUri);
        fsb.append("##");
        fsb.append(schemaAwareAttr);
        fsb.append("##");
        fsb.append(exprText);
        if (namespaceContextBase != null) {
            fsb.append("##");
            namespaceContextBase.generateId(fsb);
        }
        String cacheKey = fsb.toString();
        Collection<XPathVariable> declaredVars = null;
        Controller controller2 = controller = context.getController();
        synchronized (controller2) {
            cache = (LRUCache<String, Object[]>)controller.getUserData(this.getLocation(), "xsl:evaluate");
            if (cache == null) {
                cache = new LRUCache<String, Object[]>(100);
                controller.setUserData(this.getLocation(), "xsl:evaluate", cache);
            } else {
                Object[] o = (Object[])cache.get(cacheKey);
                if (o != null) {
                    expr = (Expression)o[0];
                    slotMap = (SlotManager)o[1];
                    declaredVars = (Collection<XPathVariable>)o[2];
                }
            }
        }
        MapItem dynamicParams = null;
        if (this.dynamicParamsOp != null) {
            dynamicParams = (MapItem)this.dynamicParamsOp.getChildExpression().evaluateItem(context);
        }
        if (expr == null) {
            GroundedValue defaultCollation;
            HashTrieMap options = this.optionsOp == null ? new HashTrieMap() : (MapItem)this.optionsOp.getChildExpression().evaluateItem(context);
            IndependentContext env = new IndependentContext(config){

                @Override
                public void issueWarning(String s, Location locator) {
                    String message = "In dynamic expression {" + exprText + "}: " + s;
                    context.getController().warning(message, null, EvaluateInstr.this.getLocation());
                }
            };
            env.setBaseURI(baseUri);
            env.setExecutable(context.getController().getExecutable());
            env.setXPathLanguageLevel(31);
            env.setDefaultCollationName(this.getRetainedStaticContext().getDefaultCollationName());
            if (this.getNamespaceContextExpr() != null) {
                env.setNamespaces(namespaceContextBase);
            } else {
                env.setNamespaceResolver(this.getRetainedStaticContext());
                env.setDefaultElementNamespace(this.getRetainedStaticContext().getDefaultElementNamespace());
            }
            FunctionLibraryList libraryList0 = ((StylesheetPackage)this.getRetainedStaticContext().getPackageData()).getFunctionLibrary();
            FunctionLibraryList libraryList1 = new FunctionLibraryList();
            for (FunctionLibrary lib : libraryList0.getLibraryList()) {
                if (lib instanceof BuiltInFunctionSet && ((BuiltInFunctionSet)lib).getNamespace().equals("http://www.w3.org/2005/xpath-functions")) {
                    libraryList1.addFunctionLibrary(XPath31FunctionSet.getInstance());
                    continue;
                }
                if (lib instanceof StylesheetFunctionLibrary || lib instanceof ExecutableFunctionLibrary) {
                    libraryList1.addFunctionLibrary(new PublicStylesheetFunctionLibrary(lib));
                    continue;
                }
                libraryList1.addFunctionLibrary(lib);
            }
            env.setFunctionLibrary(libraryList1);
            env.setDecimalFormatManager(this.getRetainedStaticContext().getDecimalFormatManager());
            env.setXPathLanguageLevel(config.getConfigurationProperty(Feature.XPATH_VERSION_FOR_XSLT));
            if (isSchemaAware) {
                GroundedValue allowAny = options.get(new StringValue("allow-any-namespace"));
                if (allowAny != null && allowAny.effectiveBooleanValue()) {
                    env.setImportedSchemaNamespaces(config.getImportedNamespaces());
                } else {
                    env.setImportedSchemaNamespaces(this.importedSchemaNamespaces);
                }
            }
            if ((defaultCollation = options.get(new StringValue("default-collation"))) != null) {
                env.setDefaultCollationName(defaultCollation.head().getStringValue());
            }
            HashMap<StructuredQName, Integer> locals = new HashMap<StructuredQName, Integer>();
            if (dynamicParams != null) {
                dynamicParams.keys().forEachOrFail(paramName -> {
                    if (!(paramName instanceof QNameValue)) {
                        XPathException err = new XPathException("Parameter names supplied to xsl:evaluate must have type xs:QName, not " + ((AtomicValue)paramName).getItemType().getPrimitiveItemType().getDisplayName(), "XTTE3165");
                        err.setIsTypeError(true);
                        throw err;
                    }
                    XPathVariable var = env.declareVariable((QNameValue)paramName);
                    locals.put(((QNameValue)paramName).getStructuredQName(), var.getLocalSlotNumber());
                });
            }
            if (this.getActualParams() != null) {
                for (WithParam actualParam : this.getActualParams()) {
                    StructuredQName name = actualParam.getVariableQName();
                    if (locals.get(name) != null) continue;
                    XPathVariable var = env.declareVariable(name);
                    locals.put(name, var.getLocalSlotNumber());
                }
            }
            try {
                expr = ExpressionTool.make(exprText, env, 0, 0, null);
            } catch (XPathException e2) {
                XPathException err = new XPathException("Static error in XPath expression supplied to xsl:evaluate: " + e2.getMessage() + ". Expression: {" + exprText + "}");
                err.setErrorCode("XTDE3160");
                err.setLocation(this.getLocation());
                throw err;
            }
            expr.setRetainedStaticContext(env.makeRetainedStaticContext());
            RoleDiagnostic role = new RoleDiagnostic(12, exprText, 0);
            ExpressionVisitor visitor = ExpressionVisitor.make(env);
            TypeChecker tc = config.getTypeChecker(false);
            expr = tc.staticTypeCheck(expr, this.requiredType, role, visitor);
            ItemType contextItemType = Type.ITEM_TYPE;
            expr = ExpressionTool.resolveCallsToCurrentFunction(expr);
            ContextItemStaticInfo cit = config.makeContextItemStaticInfo(contextItemType, context.getContextItem() == null);
            expr = expr.typeCheck(visitor, cit).optimize(visitor, cit);
            slotMap = env.getStackFrameMap();
            ExpressionTool.allocateSlots(expr, slotMap.getNumberOfVariables(), slotMap);
            if (cacheKey != null) {
                declaredVars = env.getDeclaredVariables();
                cache.put(cacheKey, new Object[]{expr, slotMap, declaredVars});
            }
        }
        XPathContextMajor c2 = context.newContext();
        if (focus == null) {
            c2.setCurrentIterator(null);
        } else {
            ManualIterator mono = new ManualIterator(focus);
            c2.setCurrentIterator(mono);
        }
        c2.openStackFrame(slotMap);
        if (this.getActualParams() != null) {
            for (int i = 0; i < this.getActualParams().length; ++i) {
                int slot = slotMap.getVariableMap().indexOf(this.getActualParams()[i].getVariableQName());
                c2.setLocalVariable(slot, this.getActualParams()[i].getSelectValue(context));
            }
        }
        if (dynamicParams != null) {
            QNameValue paramName2;
            AtomicIterator<? extends AtomicValue> iter = dynamicParams.keys();
            while ((paramName2 = (QNameValue)iter.next()) != null) {
                int slot = slotMap.getVariableMap().indexOf(paramName2.getStructuredQName());
                if (slot < 0) continue;
                c2.setLocalVariable(slot, dynamicParams.get(paramName2));
            }
        }
        for (XPathVariable var : declaredVars) {
            StructuredQName name = var.getVariableQName();
            Predicate<Expression> nameMatch = e -> e instanceof LocalVariableReference && ((LocalVariableReference)e).getVariableName().equals(name) && ((LocalVariableReference)e).getBinding() instanceof XPathVariable;
            if (dynamicParams == null || dynamicParams.get(new QNameValue(name, BuiltInAtomicType.QNAME)) != null || this.isActualParam(name) || !ExpressionTool.contains(expr, false, nameMatch)) continue;
            throw new XPathException("No value has been supplied for variable " + name.getDisplayName(), "XPST0008");
        }
        try {
            return expr.iterate(c2);
        } catch (XPathException err) {
            XPathException e2 = new XPathException("Dynamic error in expression {" + exprText + "} called using xsl:evaluate", err);
            e2.setLocation(this.getLocation());
            e2.setErrorCodeQName(err.getErrorCodeQName());
            throw e2;
        }
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("evaluate", this);
        if (!SequenceType.ANY_SEQUENCE.equals(this.requiredType)) {
            out.emitAttribute("as", this.requiredType.toAlphaCode());
        }
        if (this.importedSchemaNamespaces != null && !this.importedSchemaNamespaces.isEmpty()) {
            FastStringBuffer buff = new FastStringBuffer(256);
            for (String s : this.importedSchemaNamespaces) {
                if (s.isEmpty()) {
                    s = "##";
                }
                buff.append(s);
                buff.cat(' ');
            }
            buff.setLength(buff.length() - 1);
            out.emitAttribute("schNS", buff.toString());
        }
        if (this.defaultXPathNamespace != null) {
            out.emitAttribute("dxns", this.defaultXPathNamespace);
        }
        out.setChildRole("xpath");
        this.getXpath().export(out);
        if (this.getContextItemExpr() != null) {
            out.setChildRole("cxt");
            this.getContextItemExpr().export(out);
        }
        if (this.getBaseUriExpr() != null) {
            out.setChildRole("baseUri");
            this.getBaseUriExpr().export(out);
        }
        if (this.getNamespaceContextExpr() != null) {
            out.setChildRole("nsCxt");
            this.getNamespaceContextExpr().export(out);
        }
        if (this.getSchemaAwareExpr() != null) {
            out.setChildRole("sa");
            this.getSchemaAwareExpr().export(out);
        }
        if (this.optionsOp != null) {
            out.setChildRole("options");
            this.optionsOp.getChildExpression().export(out);
        }
        WithParam.exportParameters(this.actualParams, out, false);
        if (this.dynamicParamsOp != null) {
            out.setChildRole("wp");
            this.getDynamicParams().export(out);
        }
        out.endElement();
    }

    public Expression getXpath() {
        return this.xpathOp.getChildExpression();
    }

    public void setXpath(Expression xpath) {
        if (this.xpathOp == null) {
            this.xpathOp = new Operand(this, xpath, OperandRole.SINGLE_ATOMIC);
        } else {
            this.xpathOp.setChildExpression(xpath);
        }
    }

    public Expression getContextItemExpr() {
        return this.contextItemOp == null ? null : this.contextItemOp.getChildExpression();
    }

    public void setContextItemExpr(Expression contextItemExpr) {
        if (this.contextItemOp == null) {
            this.contextItemOp = new Operand(this, contextItemExpr, OperandRole.NAVIGATE);
        } else {
            this.contextItemOp.setChildExpression(contextItemExpr);
        }
    }

    public Expression getBaseUriExpr() {
        return this.baseUriOp == null ? null : this.baseUriOp.getChildExpression();
    }

    public void setBaseUriExpr(Expression baseUriExpr) {
        if (this.baseUriOp == null) {
            this.baseUriOp = new Operand(this, baseUriExpr, OperandRole.SINGLE_ATOMIC);
        } else {
            this.baseUriOp.setChildExpression(baseUriExpr);
        }
    }

    public Expression getNamespaceContextExpr() {
        return this.namespaceContextOp == null ? null : this.namespaceContextOp.getChildExpression();
    }

    public void setNamespaceContextExpr(Expression namespaceContextExpr) {
        if (this.namespaceContextOp == null) {
            this.namespaceContextOp = new Operand(this, namespaceContextExpr, OperandRole.INSPECT);
        } else {
            this.namespaceContextOp.setChildExpression(namespaceContextExpr);
        }
    }

    public Expression getSchemaAwareExpr() {
        return this.schemaAwareOp == null ? null : this.schemaAwareOp.getChildExpression();
    }

    public void setSchemaAwareExpr(Expression schemaAwareExpr) {
        if (this.schemaAwareOp == null) {
            this.schemaAwareOp = new Operand(this, schemaAwareExpr, OperandRole.SINGLE_ATOMIC);
        } else {
            this.schemaAwareOp.setChildExpression(schemaAwareExpr);
        }
    }

    public WithParam[] getActualParams() {
        return this.actualParams;
    }

    public void setActualParams(WithParam[] actualParams) {
        this.actualParams = actualParams;
    }

    public boolean isActualParam(StructuredQName name) {
        for (WithParam wp : this.actualParams) {
            if (!wp.getVariableQName().equals(name)) continue;
            return true;
        }
        return false;
    }

    public void setDynamicParams(Expression params) {
        if (this.dynamicParamsOp == null) {
            this.dynamicParamsOp = new Operand(this, params, OperandRole.SINGLE_ATOMIC);
        } else {
            this.dynamicParamsOp.setChildExpression(params);
        }
    }

    public Expression getDynamicParams() {
        return this.dynamicParamsOp.getChildExpression();
    }
}


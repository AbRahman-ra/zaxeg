/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.hof;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.ExportAgent;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.GlobalContextRequirement;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.functions.OptionsParameter;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.functions.hof.UserFunctionReference;
import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.lib.StandardModuleURIResolver;
import net.sf.saxon.ma.map.DictionaryMap;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.ma.map.SingleEntryMap;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.QueryLibrary;
import net.sf.saxon.query.QueryModule;
import net.sf.saxon.query.QueryReader;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.query.XQueryFunction;
import net.sf.saxon.query.XQueryFunctionLibrary;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.DecimalValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.SequenceType;

public class LoadXqueryModule
extends SystemFunction
implements Callable {
    public static OptionsParameter makeOptionsParameter() {
        OptionsParameter op = new OptionsParameter();
        op.addAllowedOption("xquery-version", SequenceType.SINGLE_DECIMAL);
        op.addAllowedOption("location-hints", SequenceType.STRING_SEQUENCE);
        op.addAllowedOption("context-item", SequenceType.OPTIONAL_ITEM);
        op.addAllowedOption("variables", SequenceType.makeSequenceType(new MapType(BuiltInAtomicType.QNAME, SequenceType.ANY_SEQUENCE), 16384));
        op.addAllowedOption("vendor-options", SequenceType.makeSequenceType(new MapType(BuiltInAtomicType.QNAME, SequenceType.ANY_SEQUENCE), 16384));
        return op;
    }

    @Override
    public XPathContext makeNewContext(XPathContext callingContext, ContextOriginator originator) {
        return callingContext;
    }

    @Override
    public MapItem call(XPathContext context, Sequence[] args) throws XPathException {
        StreamSource[] streamSources;
        String moduleUri;
        Sequence xqueryVersionOption = null;
        Sequence locationHintsOption = null;
        Sequence variablesOption = null;
        Sequence contextItemOption = null;
        Sequence vendorOptionsOption = null;
        if (args.length == 2) {
            MapItem suppliedOptions = (MapItem)args[1].head();
            Map<String, Sequence> checkedOptions = this.getDetails().optionDetails.processSuppliedOptions(suppliedOptions, context);
            xqueryVersionOption = checkedOptions.get("xquery-version");
            if (xqueryVersionOption != null && ((DecimalValue)xqueryVersionOption.head()).getDoubleValue() * 10.0 > 31.0) {
                throw new XPathException("No XQuery version " + xqueryVersionOption + " processor is available", "FOQM0006");
            }
            locationHintsOption = checkedOptions.get("location-hints");
            variablesOption = checkedOptions.get("variables");
            contextItemOption = checkedOptions.get("context-item");
            vendorOptionsOption = checkedOptions.get("vendor-options");
        }
        int qv = 31;
        if (xqueryVersionOption != null) {
            BigDecimal decimalVn = ((DecimalValue)xqueryVersionOption.head()).getDecimalValue();
            if (decimalVn.equals(new BigDecimal("1.0")) || decimalVn.equals(new BigDecimal("3.0")) || decimalVn.equals(new BigDecimal("3.1"))) {
                qv = decimalVn.multiply(BigDecimal.TEN).intValue();
            } else {
                throw new XPathException("Unsupported XQuery version " + decimalVn, "FOQM0006");
            }
        }
        if ((moduleUri = args[0].head().getStringValue()).isEmpty()) {
            throw new XPathException("First argument of fn:load-xquery-module() must not be a zero length string", "FOQM0001");
        }
        ArrayList<String> locationHints = new ArrayList<String>();
        if (locationHintsOption != null) {
            Item hint;
            SequenceIterator iterator = locationHintsOption.iterate();
            while ((hint = iterator.next()) != null) {
                locationHints.add(hint.getStringValue());
            }
        }
        Configuration config = context.getConfiguration();
        StaticQueryContext staticQueryContext = config.newStaticQueryContext();
        ModuleURIResolver moduleURIResolver = config.getModuleURIResolver();
        if (moduleURIResolver == null) {
            moduleURIResolver = new StandardModuleURIResolver(config);
        }
        staticQueryContext.setModuleURIResolver(moduleURIResolver);
        String baseURI = this.getRetainedStaticContext().getStaticBaseUriString();
        staticQueryContext.setBaseURI(baseURI);
        try {
            String[] hints = locationHints.toArray(new String[0]);
            streamSources = staticQueryContext.getModuleURIResolver().resolve(moduleUri, baseURI, hints);
            if (streamSources == null) {
                streamSources = new StandardModuleURIResolver(config).resolve(moduleUri, baseURI, hints);
            }
        } catch (XPathException e) {
            e.maybeSetErrorCode("FOQM0002");
            throw e;
        }
        if (streamSources.length == 0) {
            throw new XPathException("No library module found with specified target namespace " + moduleUri, "FOQM0002");
        }
        try {
            String sourceQuery = QueryReader.readSourceQuery(streamSources[0], config.getValidCharacterChecker());
            staticQueryContext.compileLibrary(sourceQuery);
        } catch (XPathException e) {
            throw new XPathException(e.getMessage(), "FOQM0003");
        }
        QueryLibrary lib = staticQueryContext.getCompiledLibrary(moduleUri);
        if (lib == null) {
            throw new XPathException("The library module located does not have the expected namespace " + moduleUri, "FOQM0002");
        }
        QueryModule main = new QueryModule(staticQueryContext);
        main.setPackageData(lib.getPackageData());
        main.setExecutable(lib.getExecutable());
        lib.link(main);
        XQueryExpression xqe = new XQueryExpression(new ContextItemExpression(), main, false);
        DynamicQueryContext dqc = new DynamicQueryContext(context.getConfiguration());
        if (variablesOption != null) {
            Item key;
            MapItem extVariables = (MapItem)variablesOption.head();
            AtomicIterator<? extends AtomicValue> iterator = extVariables.keys();
            while ((key = iterator.next()) != null) {
                dqc.setParameter(((QNameValue)key).getStructuredQName(), extVariables.get((AtomicValue)key).materialize());
            }
        }
        if (contextItemOption != null) {
            ItemType req;
            Item contextItem = contextItemOption.head();
            GlobalContextRequirement gcr = main.getExecutable().getGlobalContextRequirement();
            if (gcr != null && (req = gcr.getRequiredItemType()) != null && !req.matches(contextItem, config.getTypeHierarchy())) {
                throw new XPathException("Required context item type is " + req, "FOQM0005");
            }
            dqc.setContextItem(contextItemOption.head());
        }
        Controller newController = xqe.newController(dqc);
        XPathContextMajor newContext = newController.newXPathContext();
        HashTrieMap variablesMap = new HashTrieMap();
        for (GlobalVariable var : lib.getGlobalVariables()) {
            GroundedValue value;
            QNameValue qNameValue = new QNameValue(var.getVariableQName(), BuiltInAtomicType.QNAME);
            if (!qNameValue.getNamespaceURI().equals(moduleUri)) continue;
            try {
                value = var.evaluateVariable(newContext);
            } catch (XPathException e) {
                e.setIsGlobalError(false);
                if (e.getErrorCodeLocalPart().equals("XPTY0004")) {
                    throw new XPathException(e.getMessage(), "FOQM0005");
                }
                throw e;
            }
            variablesMap = variablesMap.addEntry(qNameValue, value);
        }
        HashTrieMap functionsMap = new HashTrieMap();
        XQueryFunctionLibrary functionLib = lib.getGlobalFunctionLibrary();
        Iterator<XQueryFunction> functionIterator = functionLib.getFunctionDefinitions();
        ExportAgent agent = out -> {
            XPathException err = new XPathException("Cannot export a stylesheet that statically incorporates XQuery functions", "SXST0069");
            err.setIsStaticError(true);
            throw err;
        };
        if (functionIterator.hasNext()) {
            while (functionIterator.hasNext()) {
                XQueryFunction function = functionIterator.next();
                QNameValue functionQName = new QNameValue(function.getFunctionName(), BuiltInAtomicType.QNAME);
                if (!functionQName.getNamespaceURI().equals(moduleUri)) continue;
                UserFunction userFunction = function.getUserFunction();
                UserFunctionReference.BoundUserFunction buf = new UserFunctionReference.BoundUserFunction(agent, userFunction, null, newController);
                MapItem newMap = functionsMap.get(functionQName) != null ? ((MapItem)functionsMap.get(functionQName)).addEntry(new Int64Value(function.getNumberOfArguments()), buf) : new SingleEntryMap(Int64Value.makeIntegerValue(function.getNumberOfArguments()), buf);
                functionsMap = functionsMap.addEntry(functionQName, newMap);
            }
        }
        DictionaryMap map = new DictionaryMap();
        map.initialPut("variables", variablesMap);
        map.initialPut("functions", functionsMap);
        return map;
    }
}


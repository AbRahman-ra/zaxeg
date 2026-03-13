/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.sxpath;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.LoopLifter;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.functions.registry.ConstructorFunctionLibrary;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.sxpath.XPathStaticContext;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;

public class XPathEvaluator {
    private XPathStaticContext staticContext;

    public XPathEvaluator(Configuration config) {
        this.staticContext = new IndependentContext(config);
    }

    public Configuration getConfiguration() {
        return this.staticContext.getConfiguration();
    }

    public void setStaticContext(XPathStaticContext context) {
        this.staticContext = context;
    }

    public XPathStaticContext getStaticContext() {
        return this.staticContext;
    }

    public XPathExpression createExpression(String expression) throws XPathException {
        Configuration config = this.getConfiguration();
        Executable exec = new Executable(config);
        exec.setTopLevelPackage(this.staticContext.getPackageData());
        exec.setSchemaAware(this.staticContext.getPackageData().isSchemaAware());
        exec.setHostLanguage(HostLanguage.XPATH);
        FunctionLibraryList userlib = exec.getFunctionLibrary();
        FunctionLibraryList lib = new FunctionLibraryList();
        lib.addFunctionLibrary(config.getXPath31FunctionSet());
        lib.addFunctionLibrary(config.getBuiltInExtensionLibraryList());
        lib.addFunctionLibrary(new ConstructorFunctionLibrary(config));
        lib.addFunctionLibrary(config.getIntegratedFunctionLibrary());
        config.addExtensionBinders(lib);
        if (userlib != null) {
            lib.addFunctionLibrary(userlib);
        }
        exec.setFunctionLibrary(lib);
        Optimizer opt = config.obtainOptimizer();
        Expression exp = ExpressionTool.make(expression, this.staticContext, 0, -1, null);
        RetainedStaticContext rsc = this.staticContext.makeRetainedStaticContext();
        exp.setRetainedStaticContext(rsc);
        ExpressionVisitor visitor = ExpressionVisitor.make(this.staticContext);
        ItemType contextItemType = this.staticContext.getRequiredContextItemType();
        ContextItemStaticInfo cit = config.makeContextItemStaticInfo(contextItemType, true);
        cit.setParentless(this.staticContext.isContextItemParentless());
        exp = exp.typeCheck(visitor, cit);
        if (opt.isOptionSet(256)) {
            exp = exp.optimize(visitor, cit);
        }
        if (opt.isOptionSet(1)) {
            exp.setParentExpression(null);
            exp = LoopLifter.process(exp, visitor, cit);
        }
        exp = this.postProcess(exp, visitor, cit);
        exp.setRetainedStaticContext(rsc);
        SlotManager map = this.staticContext.getStackFrameMap();
        int numberOfExternalVariables = map.getNumberOfVariables();
        ExpressionTool.allocateSlots(exp, numberOfExternalVariables, map);
        XPathExpression xpe = new XPathExpression(this.staticContext, exp, exec);
        xpe.setStackFrameMap(map, numberOfExternalVariables);
        return xpe;
    }

    protected Expression postProcess(Expression exp, ExpressionVisitor visitor, ContextItemStaticInfo cit) throws XPathException {
        return exp;
    }

    public XPathExpression createPattern(String pattern) throws XPathException {
        Configuration config = this.getConfiguration();
        Executable exec = new Executable(config);
        Pattern pat = Pattern.make(pattern, this.staticContext, new PackageData(config));
        ExpressionVisitor visitor = ExpressionVisitor.make(this.staticContext);
        pat.typeCheck(visitor, config.makeContextItemStaticInfo(Type.NODE_TYPE, true));
        SlotManager map = this.staticContext.getStackFrameMap();
        int slots = map.getNumberOfVariables();
        slots = pat.allocateSlots(map, slots);
        XPathExpression xpe = new XPathExpression(this.staticContext, pat, exec);
        xpe.setStackFrameMap(map, slots);
        return xpe;
    }
}


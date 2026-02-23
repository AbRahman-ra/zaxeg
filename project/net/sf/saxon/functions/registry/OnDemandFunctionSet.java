/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.registry;

import java.util.List;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.om.Function;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;

public class OnDemandFunctionSet
implements FunctionLibrary {
    private Configuration config;
    private String namespace;
    private String libraryClass;
    private FunctionLibrary library;

    public OnDemandFunctionSet(Configuration config, String namespace, String libraryClass) {
        this.config = config;
        this.namespace = namespace;
        this.libraryClass = libraryClass;
    }

    private boolean load(SymbolicName.F functionName, List<String> reasons) {
        if (functionName.getComponentName().hasURI(this.namespace)) {
            if (this.library == null) {
                try {
                    Object lib = this.config.getDynamicLoader().getInstance(this.libraryClass, null);
                    if (!(lib instanceof FunctionLibrary)) {
                        if (reasons != null) {
                            reasons.add("Class " + this.libraryClass + " was loaded but it is not a FunctionLibrary");
                        }
                        return false;
                    }
                    this.library = (FunctionLibrary)lib;
                } catch (XPathException e) {
                    if (reasons != null) {
                        reasons.add("Failed to load class " + this.libraryClass + ": " + e.getMessage());
                    }
                    return false;
                }
            }
            this.library.setConfiguration(this.config);
            return true;
        }
        return false;
    }

    @Override
    public boolean isAvailable(SymbolicName.F functionName) {
        boolean match = this.load(functionName, null);
        return match && this.library.isAvailable(functionName);
    }

    @Override
    public Expression bind(SymbolicName.F functionName, Expression[] staticArgs, StaticContext env, List<String> reasons) {
        boolean match = this.load(functionName, reasons);
        if (match) {
            return this.library.bind(functionName, staticArgs, env, reasons);
        }
        return null;
    }

    @Override
    public FunctionLibrary copy() {
        return this;
    }

    @Override
    public Function getFunctionItem(SymbolicName.F functionName, StaticContext staticContext) throws XPathException {
        boolean match = this.load(functionName, null);
        if (match) {
            return this.library.getFunctionItem(functionName, staticContext);
        }
        return null;
    }
}


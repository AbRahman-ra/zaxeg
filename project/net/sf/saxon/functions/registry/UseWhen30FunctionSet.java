/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.registry;

import net.sf.saxon.functions.AvailableSystemProperties;
import net.sf.saxon.functions.ElementAvailable;
import net.sf.saxon.functions.FunctionAvailable;
import net.sf.saxon.functions.SystemProperty;
import net.sf.saxon.functions.TypeAvailable;
import net.sf.saxon.functions.registry.BuiltInFunctionSet;
import net.sf.saxon.functions.registry.XPath31FunctionSet;
import net.sf.saxon.type.BuiltInAtomicType;

public class UseWhen30FunctionSet
extends BuiltInFunctionSet {
    private static UseWhen30FunctionSet THE_INSTANCE = new UseWhen30FunctionSet();

    public static UseWhen30FunctionSet getInstance() {
        return THE_INSTANCE;
    }

    protected UseWhen30FunctionSet() {
        this.init();
    }

    protected void init() {
        this.addXPathFunctions();
        this.register("available-system-properties", 0, AvailableSystemProperties.class, BuiltInAtomicType.QNAME, 57344, 512);
        this.register("element-available", 1, ElementAvailable.class, BuiltInAtomicType.BOOLEAN, 16384, 16).arg(0, BuiltInAtomicType.STRING, 16384, null);
        this.register("function-available", 1, FunctionAvailable.class, BuiltInAtomicType.BOOLEAN, 16384, 528).arg(0, BuiltInAtomicType.STRING, 16384, null);
        this.register("function-available", 2, FunctionAvailable.class, BuiltInAtomicType.BOOLEAN, 16384, 528).arg(0, BuiltInAtomicType.STRING, 16384, null).arg(1, BuiltInAtomicType.INTEGER, 16384, null);
        this.register("system-property", 1, SystemProperty.class, BuiltInAtomicType.STRING, 16384, 528).arg(0, BuiltInAtomicType.STRING, 16384, null);
        this.register("type-available", 1, TypeAvailable.class, BuiltInAtomicType.BOOLEAN, 16384, 16).arg(0, BuiltInAtomicType.STRING, 16384, null);
    }

    protected void addXPathFunctions() {
        this.importFunctionSet(XPath31FunctionSet.getInstance());
    }
}


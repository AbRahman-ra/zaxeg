/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.registry;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.functions.registry.BuiltInFunctionSet;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.NumericType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.StringValue;

public class ExsltCommonFunctionSet
extends BuiltInFunctionSet {
    private static ExsltCommonFunctionSet THE_INSTANCE = new ExsltCommonFunctionSet();

    public static ExsltCommonFunctionSet getInstance() {
        return THE_INSTANCE;
    }

    private ExsltCommonFunctionSet() {
        this.init();
    }

    private void init() {
        this.register("node-set", 1, NodeSetFn.class, AnyItemType.getInstance(), 24576, 0).arg(0, AnyItemType.getInstance(), 24576, EMPTY);
        this.register("object-type", 1, ObjectTypeFn.class, BuiltInAtomicType.STRING, 16384, 0).arg(0, AnyItemType.getInstance(), 16384, null);
    }

    @Override
    public String getNamespace() {
        return "http://exslt.org/common";
    }

    @Override
    public String getConventionalPrefix() {
        return "exsltCommon";
    }

    public static class ObjectTypeFn
    extends SystemFunction {
        @Override
        public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
            Item value;
            ItemType type;
            TypeHierarchy th = context.getConfiguration().getTypeHierarchy();
            if (th.isSubType(type = SequenceTool.getItemType(value = arguments[0].head(), th), AnyNodeTest.getInstance())) {
                return new StringValue("node-set");
            }
            if (th.isSubType(type, BuiltInAtomicType.STRING)) {
                return new StringValue("string");
            }
            if (NumericType.isNumericType(type)) {
                return new StringValue("number");
            }
            if (th.isSubType(type, BuiltInAtomicType.BOOLEAN)) {
                return new StringValue("boolean");
            }
            return new StringValue(type.toString());
        }
    }

    public static class NodeSetFn
    extends SystemFunction {
        @Override
        public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
            return arguments[0];
        }
    }
}


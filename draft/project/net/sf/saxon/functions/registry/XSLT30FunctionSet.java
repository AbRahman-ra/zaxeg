/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.registry;

import net.sf.saxon.functions.AccumulatorFn;
import net.sf.saxon.functions.AvailableSystemProperties;
import net.sf.saxon.functions.Current;
import net.sf.saxon.functions.CurrentGroup;
import net.sf.saxon.functions.CurrentGroupingKey;
import net.sf.saxon.functions.CurrentMergeGroup;
import net.sf.saxon.functions.CurrentMergeKey;
import net.sf.saxon.functions.CurrentOutputUri;
import net.sf.saxon.functions.DocumentFn;
import net.sf.saxon.functions.ElementAvailable;
import net.sf.saxon.functions.FunctionAvailable;
import net.sf.saxon.functions.KeyFn;
import net.sf.saxon.functions.RegexGroup;
import net.sf.saxon.functions.StreamAvailable;
import net.sf.saxon.functions.SystemProperty;
import net.sf.saxon.functions.TypeAvailable;
import net.sf.saxon.functions.UnparsedEntity;
import net.sf.saxon.functions.registry.BuiltInFunctionSet;
import net.sf.saxon.functions.registry.XPath31FunctionSet;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Type;

public class XSLT30FunctionSet
extends BuiltInFunctionSet {
    private static XSLT30FunctionSet THE_INSTANCE = new XSLT30FunctionSet();

    public static XSLT30FunctionSet getInstance() {
        return THE_INSTANCE;
    }

    private XSLT30FunctionSet() {
        this.init();
    }

    private void init() {
        this.importFunctionSet(XPath31FunctionSet.getInstance());
        this.register("accumulator-after", 1, AccumulatorFn.AccumulatorAfter.class, AnyItemType.getInstance(), 57344, 516).arg(0, BuiltInAtomicType.STRING, 16384, null);
        this.register("accumulator-before", 1, AccumulatorFn.AccumulatorBefore.class, AnyItemType.getInstance(), 57344, 516).arg(0, BuiltInAtomicType.STRING, 16384, null);
        this.register("available-system-properties", 0, AvailableSystemProperties.class, BuiltInAtomicType.QNAME, 57344, 512);
        this.register("current", 0, Current.class, Type.ITEM_TYPE, 16384, 512);
        this.register("current-group", 0, CurrentGroup.class, Type.ITEM_TYPE, 57344, 512);
        this.register("current-grouping-key", 0, CurrentGroupingKey.class, BuiltInAtomicType.ANY_ATOMIC, 57344, 512);
        this.register("current-merge-group", 0, CurrentMergeGroup.class, AnyItemType.getInstance(), 57344, 512);
        this.register("current-merge-group", 1, CurrentMergeGroup.class, AnyItemType.getInstance(), 57344, 512).arg(0, BuiltInAtomicType.STRING, 16384, null);
        this.register("current-merge-key", 0, CurrentMergeKey.class, BuiltInAtomicType.ANY_ATOMIC, 57344, 512);
        this.register("current-output-uri", 0, CurrentOutputUri.class, BuiltInAtomicType.ANY_URI, 24576, 512);
        this.register("document", 1, DocumentFn.class, Type.NODE_TYPE, 57344, 1544).arg(0, Type.ITEM_TYPE, 57344, null);
        this.register("document", 2, DocumentFn.class, Type.NODE_TYPE, 57344, 1544).arg(0, Type.ITEM_TYPE, 57344, null).arg(1, Type.NODE_TYPE, 16384, null);
        this.register("element-available", 1, ElementAvailable.class, BuiltInAtomicType.BOOLEAN, 16384, 16).arg(0, BuiltInAtomicType.STRING, 16384, null);
        this.register("function-available", 1, FunctionAvailable.class, BuiltInAtomicType.BOOLEAN, 16384, 528).arg(0, BuiltInAtomicType.STRING, 16384, null);
        this.register("function-available", 2, FunctionAvailable.class, BuiltInAtomicType.BOOLEAN, 16384, 528).arg(0, BuiltInAtomicType.STRING, 16384, null).arg(1, BuiltInAtomicType.INTEGER, 16384, null);
        this.register("key", 2, KeyFn.class, Type.NODE_TYPE, 57344, 16912).arg(0, BuiltInAtomicType.STRING, 16384, null).arg(1, BuiltInAtomicType.ANY_ATOMIC, 57344, EMPTY);
        this.register("key", 3, KeyFn.class, Type.NODE_TYPE, 57344, 528).arg(0, BuiltInAtomicType.STRING, 16384, null).arg(1, BuiltInAtomicType.ANY_ATOMIC, 57344, EMPTY).arg(2, Type.NODE_TYPE, 16384, null);
        this.register("regex-group", 1, RegexGroup.class, BuiltInAtomicType.STRING, 16384, 8704).arg(0, BuiltInAtomicType.INTEGER, 16384, null);
        this.register("stream-available", 1, StreamAvailable.class, BuiltInAtomicType.BOOLEAN, 16384, 512).arg(0, BuiltInAtomicType.STRING, 24576, null);
        this.register("system-property", 1, SystemProperty.class, BuiltInAtomicType.STRING, 16384, 528).arg(0, BuiltInAtomicType.STRING, 16384, null);
        this.register("type-available", 1, TypeAvailable.class, BuiltInAtomicType.BOOLEAN, 16384, 16).arg(0, BuiltInAtomicType.STRING, 16384, null);
        this.register("unparsed-entity-public-id", 1, UnparsedEntity.UnparsedEntityPublicId.class, BuiltInAtomicType.STRING, 16384, 16896).arg(0, BuiltInAtomicType.STRING, 16384, null);
        this.register("unparsed-entity-public-id", 2, UnparsedEntity.UnparsedEntityPublicId.class, BuiltInAtomicType.STRING, 16384, 0).arg(0, BuiltInAtomicType.STRING, 16384, null).arg(1, Type.NODE_TYPE, 16384, null);
        this.register("unparsed-entity-uri", 1, UnparsedEntity.UnparsedEntityUri.class, BuiltInAtomicType.ANY_URI, 16384, 16896).arg(0, BuiltInAtomicType.STRING, 16384, null);
        this.register("unparsed-entity-uri", 2, UnparsedEntity.UnparsedEntityUri.class, BuiltInAtomicType.ANY_URI, 16384, 0).arg(0, BuiltInAtomicType.STRING, 16384, null).arg(1, Type.NODE_TYPE, 16384, null);
    }
}


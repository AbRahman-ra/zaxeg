/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.registry;

import net.sf.saxon.functions.CollatingFunctionFree;
import net.sf.saxon.functions.CollationKeyFn;
import net.sf.saxon.functions.ContainsToken;
import net.sf.saxon.functions.ContextItemAccessorFunction;
import net.sf.saxon.functions.CopyOfFn;
import net.sf.saxon.functions.DynamicContextAccessor;
import net.sf.saxon.functions.GenerateId_1;
import net.sf.saxon.functions.HasChildren_1;
import net.sf.saxon.functions.HeadFn;
import net.sf.saxon.functions.Innermost;
import net.sf.saxon.functions.ParseIetfDate;
import net.sf.saxon.functions.ParseXml;
import net.sf.saxon.functions.ParseXmlFragment;
import net.sf.saxon.functions.Serialize;
import net.sf.saxon.functions.SnapshotFn;
import net.sf.saxon.functions.Sort_1;
import net.sf.saxon.functions.Sort_2;
import net.sf.saxon.functions.StringJoin;
import net.sf.saxon.functions.Tokenize_1;
import net.sf.saxon.functions.Trace;
import net.sf.saxon.functions.TransformFn;
import net.sf.saxon.functions.hof.LoadXqueryModule;
import net.sf.saxon.functions.hof.RandomNumberGenerator;
import net.sf.saxon.functions.hof.Sort_3;
import net.sf.saxon.functions.registry.BuiltInFunctionSet;
import net.sf.saxon.functions.registry.XPath20FunctionSet;
import net.sf.saxon.functions.registry.XPath30FunctionSet;
import net.sf.saxon.ma.json.JsonDoc;
import net.sf.saxon.ma.json.JsonToXMLFn;
import net.sf.saxon.ma.json.ParseJsonFn;
import net.sf.saxon.ma.json.XMLToJsonFn;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class XPath31FunctionSet
extends BuiltInFunctionSet {
    private static XPath31FunctionSet THE_INSTANCE = new XPath31FunctionSet();

    public static XPath31FunctionSet getInstance() {
        return THE_INSTANCE;
    }

    private XPath31FunctionSet() {
        this.init();
    }

    private void init() {
        this.importFunctionSet(XPath20FunctionSet.getInstance());
        this.importFunctionSet(XPath30FunctionSet.getInstance());
        this.register("collation-key", 1, CollationKeyFn.class, BuiltInAtomicType.BASE64_BINARY, 24576, 32).arg(0, BuiltInAtomicType.STRING, 16384, null);
        this.register("collation-key", 2, CollatingFunctionFree.class, BuiltInAtomicType.BASE64_BINARY, 24576, 32).arg(0, BuiltInAtomicType.STRING, 16384, null).arg(1, BuiltInAtomicType.STRING, 16384, null);
        this.register("contains-token", 2, ContainsToken.class, BuiltInAtomicType.BOOLEAN, 16384, 32).arg(0, BuiltInAtomicType.STRING, 57344, null).arg(1, BuiltInAtomicType.STRING, 16384, null);
        this.register("contains-token", 3, CollatingFunctionFree.class, BuiltInAtomicType.BOOLEAN, 16384, 8).arg(0, BuiltInAtomicType.STRING, 57344, null).arg(1, BuiltInAtomicType.STRING, 16384, null).arg(2, BuiltInAtomicType.STRING, 16384, null);
        this.register("copy-of", 0, CopyOfFn.class, AnyItemType.getInstance(), 57344, 65536);
        this.register("copy-of", 1, CopyOfFn.class, AnyItemType.getInstance(), 57344, 65536).arg(0, AnyItemType.getInstance(), 0x200E000, EMPTY);
        this.register("default-language", 0, DynamicContextAccessor.DefaultLanguage.class, BuiltInAtomicType.LANGUAGE, 16384, 64);
        this.register("generate-id", 0, ContextItemAccessorFunction.class, BuiltInAtomicType.STRING, 16384, 516);
        this.register("generate-id", 1, GenerateId_1.class, BuiltInAtomicType.STRING, 16384, 0).arg(0, Type.NODE_TYPE, 0x1006000, StringValue.EMPTY_STRING);
        this.register("has-children", 0, ContextItemAccessorFunction.class, BuiltInAtomicType.BOOLEAN, 16384, 516);
        this.register("has-children", 1, HasChildren_1.class, BuiltInAtomicType.BOOLEAN, 24576, 0).arg(0, AnyNodeTest.getInstance(), 0x1006000, null);
        this.register("head", 1, HeadFn.class, AnyItemType.getInstance(), 24576, 256).arg(0, AnyItemType.getInstance(), 0x400E000, null);
        this.register("innermost", 1, Innermost.class, AnyNodeTest.getInstance(), 57344, 0).arg(0, AnyNodeTest.getInstance(), 0x800E000, null);
        this.register("json-doc", 1, JsonDoc.class, AnyItemType.getInstance(), 24576, 512).arg(0, BuiltInAtomicType.STRING, 24576, null);
        this.register("json-doc", 2, JsonDoc.class, AnyItemType.getInstance(), 24576, 512).arg(0, BuiltInAtomicType.STRING, 24576, null).arg(1, MapType.ANY_MAP_TYPE, 16384, null).optionDetails(ParseJsonFn.OPTION_DETAILS);
        this.register("json-to-xml", 1, JsonToXMLFn.class, AnyItemType.getInstance(), 24576, 66048).arg(0, BuiltInAtomicType.STRING, 24576, null);
        this.register("json-to-xml", 2, JsonToXMLFn.class, AnyItemType.getInstance(), 24576, 66048).arg(0, BuiltInAtomicType.STRING, 24576, null).arg(1, MapType.ANY_MAP_TYPE, 16384, null).optionDetails(JsonToXMLFn.OPTION_DETAILS);
        this.register("load-xquery-module", 1, LoadXqueryModule.class, MapType.ANY_MAP_TYPE, 16384, 512).arg(0, BuiltInAtomicType.STRING, 16384, null);
        this.register("load-xquery-module", 2, LoadXqueryModule.class, MapType.ANY_MAP_TYPE, 16384, 512).arg(0, BuiltInAtomicType.STRING, 16384, null).arg(1, MapType.ANY_MAP_TYPE, 16384, EMPTY).optionDetails(LoadXqueryModule.makeOptionsParameter());
        this.register("parse-ietf-date", 1, ParseIetfDate.class, BuiltInAtomicType.DATE_TIME, 24576, 0).arg(0, BuiltInAtomicType.STRING, 24576, EMPTY);
        this.register("parse-json", 1, ParseJsonFn.class, AnyItemType.getInstance(), 24576, 0).arg(0, BuiltInAtomicType.STRING, 24576, EMPTY);
        this.register("parse-json", 2, ParseJsonFn.class, AnyItemType.getInstance(), 24576, 0).arg(0, BuiltInAtomicType.STRING, 24576, EMPTY).arg(1, MapType.ANY_MAP_TYPE, 16384, null).optionDetails(ParseJsonFn.OPTION_DETAILS);
        this.register("parse-xml", 1, ParseXml.class, NodeKindTest.DOCUMENT, 24576, 66048).arg(0, BuiltInAtomicType.STRING, 24576, EMPTY);
        this.register("random-number-generator", 0, RandomNumberGenerator.class, RandomNumberGenerator.RETURN_TYPE, 16384, 512);
        this.register("random-number-generator", 1, RandomNumberGenerator.class, RandomNumberGenerator.RETURN_TYPE, 16384, 512).arg(0, BuiltInAtomicType.ANY_ATOMIC, 24576, null);
        this.register("parse-xml-fragment", 1, ParseXmlFragment.class, NodeKindTest.DOCUMENT, 24576, 66048).arg(0, BuiltInAtomicType.STRING, 24576, EMPTY);
        this.register("serialize", 2, Serialize.class, BuiltInAtomicType.STRING, 16384, 0).arg(0, AnyItemType.getInstance(), 57344, null).arg(1, Type.ITEM_TYPE, 24576, null).optionDetails(Serialize.makeOptionsParameter());
        this.register("snapshot", 0, ContextItemAccessorFunction.class, AnyItemType.getInstance(), 57344, 66052);
        this.register("snapshot", 1, SnapshotFn.class, AnyNodeTest.getInstance(), 57344, 65536).arg(0, AnyItemType.getInstance(), 0x200E000, EMPTY);
        this.register("sort", 1, Sort_1.class, AnyItemType.getInstance(), 57344, 0).arg(0, AnyItemType.getInstance(), 57344, null);
        this.register("sort", 2, Sort_2.class, AnyItemType.getInstance(), 57344, 0).arg(0, AnyItemType.getInstance(), 57344, null).arg(1, BuiltInAtomicType.STRING, 24576, null);
        SpecificFunctionType ft = new SpecificFunctionType(new SequenceType[]{SequenceType.SINGLE_ITEM}, SequenceType.ATOMIC_SEQUENCE);
        this.register("sort", 3, Sort_3.class, AnyItemType.getInstance(), 57344, 0).arg(0, AnyItemType.getInstance(), 57344, null).arg(1, BuiltInAtomicType.STRING, 24576, null).arg(2, ft, 16384, null);
        this.register("string-join", 1, StringJoin.class, BuiltInAtomicType.STRING, 16384, 0).arg(0, BuiltInAtomicType.ANY_ATOMIC, 57344, StringValue.EMPTY_STRING);
        this.register("string-join", 2, StringJoin.class, BuiltInAtomicType.STRING, 16384, 0).arg(0, BuiltInAtomicType.ANY_ATOMIC, 57344, StringValue.EMPTY_STRING).arg(1, BuiltInAtomicType.STRING, 16384, null);
        this.register("tokenize", 1, Tokenize_1.class, BuiltInAtomicType.STRING, 57344, 0).arg(0, BuiltInAtomicType.STRING, 24576, EMPTY);
        this.register("trace", 1, Trace.class, Type.ITEM_TYPE, 57344, 513).arg(0, Type.ITEM_TYPE, 0x400E000, null);
        this.register("transform", 1, TransformFn.class, MapType.ANY_MAP_TYPE, 16384, 512).arg(0, MapType.ANY_MAP_TYPE, 16384, EMPTY).optionDetails(TransformFn.makeOptionsParameter());
        this.register("xml-to-json", 1, XMLToJsonFn.class, BuiltInAtomicType.STRING, 24576, 512).arg(0, AnyNodeTest.getInstance(), 0x2006000, EMPTY);
        this.register("xml-to-json", 2, XMLToJsonFn.class, BuiltInAtomicType.STRING, 24576, 512).arg(0, AnyNodeTest.getInstance(), 0x2006000, EMPTY).arg(1, MapType.ANY_MAP_TYPE, 0x2004000, null).optionDetails(XMLToJsonFn.makeOptionsParameter());
    }
}


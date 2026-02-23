/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.registry;

import net.sf.saxon.functions.AnalyzeStringFn;
import net.sf.saxon.functions.ApplyFn;
import net.sf.saxon.functions.AvailableEnvironmentVariables;
import net.sf.saxon.functions.ContextItemAccessorFunction;
import net.sf.saxon.functions.EnvironmentVariable;
import net.sf.saxon.functions.FormatDate;
import net.sf.saxon.functions.FormatInteger;
import net.sf.saxon.functions.FormatNumber;
import net.sf.saxon.functions.GenerateId_1;
import net.sf.saxon.functions.HasChildren_1;
import net.sf.saxon.functions.HeadFn;
import net.sf.saxon.functions.Innermost;
import net.sf.saxon.functions.Outermost;
import net.sf.saxon.functions.ParseXml;
import net.sf.saxon.functions.ParseXmlFragment;
import net.sf.saxon.functions.Path_1;
import net.sf.saxon.functions.RegexFunctionSansFlags;
import net.sf.saxon.functions.Round;
import net.sf.saxon.functions.Serialize;
import net.sf.saxon.functions.Sort_1;
import net.sf.saxon.functions.StringJoin;
import net.sf.saxon.functions.SuperId;
import net.sf.saxon.functions.TailFn;
import net.sf.saxon.functions.UnparsedText;
import net.sf.saxon.functions.UnparsedTextAvailable;
import net.sf.saxon.functions.UnparsedTextLines;
import net.sf.saxon.functions.UriCollection;
import net.sf.saxon.functions.hof.FilterFn;
import net.sf.saxon.functions.hof.FoldLeftFn;
import net.sf.saxon.functions.hof.FoldRightFn;
import net.sf.saxon.functions.hof.ForEachFn;
import net.sf.saxon.functions.hof.ForEachPairFn;
import net.sf.saxon.functions.hof.FunctionArity;
import net.sf.saxon.functions.hof.FunctionLookup;
import net.sf.saxon.functions.hof.FunctionName;
import net.sf.saxon.functions.registry.BuiltInFunctionSet;
import net.sf.saxon.functions.registry.XPath20FunctionSet;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.type.AnyFunctionType;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.NumericType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class XPath30FunctionSet
extends BuiltInFunctionSet {
    private static XPath30FunctionSet THE_INSTANCE = new XPath30FunctionSet();

    public static XPath30FunctionSet getInstance() {
        return THE_INSTANCE;
    }

    private XPath30FunctionSet() {
        this.init();
    }

    private void init() {
        this.importFunctionSet(XPath20FunctionSet.getInstance());
        this.register("analyze-string", 2, RegexFunctionSansFlags.class, NodeKindTest.ELEMENT, 16384, 66048).arg(0, BuiltInAtomicType.STRING, 24576, null).arg(1, BuiltInAtomicType.STRING, 16384, null);
        this.register("analyze-string", 3, AnalyzeStringFn.class, NodeKindTest.ELEMENT, 16384, 66048).arg(0, BuiltInAtomicType.STRING, 24576, null).arg(1, BuiltInAtomicType.STRING, 16384, null).arg(2, BuiltInAtomicType.STRING, 16384, null);
        this.register("apply", 2, ApplyFn.class, AnyItemType.getInstance(), 57344, 512).arg(0, AnyFunctionType.getInstance(), 16384, null).arg(1, ArrayItemType.ANY_ARRAY_TYPE, 16384, null);
        this.register("available-environment-variables", 0, AvailableEnvironmentVariables.class, BuiltInAtomicType.STRING, 57344, 512);
        this.register("data", 0, ContextItemAccessorFunction.class, BuiltInAtomicType.ANY_ATOMIC, 57344, 516);
        this.register("document-uri", 0, ContextItemAccessorFunction.class, BuiltInAtomicType.ANY_URI, 24576, 516);
        this.register("element-with-id", 1, SuperId.ElementWithId.class, NodeKindTest.ELEMENT, 57344, 1540).arg(0, BuiltInAtomicType.STRING, 57344, EMPTY);
        this.register("element-with-id", 2, SuperId.ElementWithId.class, NodeKindTest.ELEMENT, 57344, 1024).arg(0, BuiltInAtomicType.STRING, 57344, EMPTY).arg(1, Type.NODE_TYPE, 16384, null);
        this.register("environment-variable", 1, EnvironmentVariable.class, BuiltInAtomicType.STRING, 24576, 512).arg(0, BuiltInAtomicType.STRING, 16384, null);
        SpecificFunctionType predicate = new SpecificFunctionType(new SequenceType[]{SequenceType.SINGLE_ITEM}, SequenceType.SINGLE_BOOLEAN);
        this.register("filter", 2, FilterFn.class, AnyItemType.getInstance(), 57344, 513).arg(0, AnyItemType.getInstance(), 0x400E000, EMPTY).arg(1, predicate, 16384, null);
        SpecificFunctionType foldLeftArg = new SpecificFunctionType(new SequenceType[]{SequenceType.ANY_SEQUENCE, SequenceType.SINGLE_ITEM}, SequenceType.ANY_SEQUENCE);
        this.register("fold-left", 3, FoldLeftFn.class, AnyItemType.getInstance(), 57344, 512).arg(0, AnyItemType.getInstance(), 57344, null).arg(1, AnyItemType.getInstance(), 57344, null).arg(2, foldLeftArg, 16384, null);
        SpecificFunctionType foldRightArg = new SpecificFunctionType(new SequenceType[]{SequenceType.SINGLE_ITEM, SequenceType.ANY_SEQUENCE}, SequenceType.ANY_SEQUENCE);
        this.register("fold-right", 3, FoldRightFn.class, AnyItemType.getInstance(), 57344, 512).arg(0, AnyItemType.getInstance(), 57344, null).arg(1, AnyItemType.getInstance(), 57344, null).arg(2, foldRightArg, 16384, null);
        SpecificFunctionType forEachArg = new SpecificFunctionType(new SequenceType[]{SequenceType.SINGLE_ITEM}, SequenceType.ANY_SEQUENCE);
        this.register("for-each", 2, ForEachFn.class, AnyItemType.getInstance(), 57344, 512).arg(0, AnyItemType.getInstance(), 57344, EMPTY).arg(1, forEachArg, 16384, null);
        SpecificFunctionType forEachPairArg = new SpecificFunctionType(new SequenceType[]{SequenceType.SINGLE_ITEM, SequenceType.SINGLE_ITEM}, SequenceType.ANY_SEQUENCE);
        this.register("for-each-pair", 3, ForEachPairFn.class, AnyItemType.getInstance(), 57344, 512).arg(0, AnyItemType.getInstance(), 57344, EMPTY).arg(1, AnyItemType.getInstance(), 57344, EMPTY).arg(2, forEachPairArg, 16384, null);
        this.register("format-date", 2, FormatDate.class, BuiltInAtomicType.STRING, 24576, 32768).arg(0, BuiltInAtomicType.DATE, 24576, null).arg(1, BuiltInAtomicType.STRING, 16384, null);
        this.register("format-date", 5, FormatDate.class, BuiltInAtomicType.STRING, 24576, 32768).arg(0, BuiltInAtomicType.DATE, 24576, null).arg(1, BuiltInAtomicType.STRING, 16384, null).arg(2, BuiltInAtomicType.STRING, 24576, null).arg(3, BuiltInAtomicType.STRING, 24576, null).arg(4, BuiltInAtomicType.STRING, 24576, null);
        this.register("format-dateTime", 2, FormatDate.class, BuiltInAtomicType.STRING, 24576, 32768).arg(0, BuiltInAtomicType.DATE_TIME, 24576, null).arg(1, BuiltInAtomicType.STRING, 16384, null);
        this.register("format-dateTime", 5, FormatDate.class, BuiltInAtomicType.STRING, 24576, 32768).arg(0, BuiltInAtomicType.DATE_TIME, 24576, null).arg(1, BuiltInAtomicType.STRING, 16384, null).arg(2, BuiltInAtomicType.STRING, 24576, null).arg(3, BuiltInAtomicType.STRING, 24576, null).arg(4, BuiltInAtomicType.STRING, 24576, null);
        this.register("format-integer", 2, FormatInteger.class, AnyItemType.getInstance(), 16384, 0).arg(0, BuiltInAtomicType.INTEGER, 24576, null).arg(1, BuiltInAtomicType.STRING, 16384, null);
        this.register("format-integer", 3, FormatInteger.class, AnyItemType.getInstance(), 16384, 0).arg(0, BuiltInAtomicType.INTEGER, 24576, null).arg(1, BuiltInAtomicType.STRING, 16384, null).arg(2, BuiltInAtomicType.STRING, 24576, null);
        this.register("format-number", 2, FormatNumber.class, BuiltInAtomicType.STRING, 16384, 512).arg(0, NumericType.getInstance(), 24576, null).arg(1, BuiltInAtomicType.STRING, 16384, null);
        this.register("format-number", 3, FormatNumber.class, BuiltInAtomicType.STRING, 16384, 528).arg(0, NumericType.getInstance(), 24576, null).arg(1, BuiltInAtomicType.STRING, 16384, null).arg(2, BuiltInAtomicType.STRING, 24576, null);
        this.register("format-time", 2, FormatDate.class, BuiltInAtomicType.STRING, 24576, 32768).arg(0, BuiltInAtomicType.TIME, 24576, null).arg(1, BuiltInAtomicType.STRING, 16384, null);
        this.register("format-time", 5, FormatDate.class, BuiltInAtomicType.STRING, 24576, 32768).arg(0, BuiltInAtomicType.TIME, 24576, null).arg(1, BuiltInAtomicType.STRING, 16384, null).arg(2, BuiltInAtomicType.STRING, 24576, null).arg(3, BuiltInAtomicType.STRING, 24576, null).arg(4, BuiltInAtomicType.STRING, 24576, null);
        this.register("function-arity", 1, FunctionArity.class, BuiltInAtomicType.INTEGER, 16384, 0).arg(0, AnyFunctionType.getInstance(), 16384, null);
        this.register("function-lookup", 2, FunctionLookup.class, AnyFunctionType.getInstance(), 24576, 23100).arg(0, BuiltInAtomicType.QNAME, 16384, null).arg(1, BuiltInAtomicType.INTEGER, 16384, null);
        this.register("function-name", 1, FunctionName.class, BuiltInAtomicType.QNAME, 24576, 0).arg(0, AnyFunctionType.getInstance(), 16384, null);
        this.register("generate-id", 0, ContextItemAccessorFunction.class, BuiltInAtomicType.STRING, 16384, 516);
        this.register("generate-id", 1, GenerateId_1.class, BuiltInAtomicType.STRING, 16384, 0).arg(0, Type.NODE_TYPE, 0x1006000, StringValue.EMPTY_STRING);
        this.register("has-children", 0, ContextItemAccessorFunction.class, BuiltInAtomicType.BOOLEAN, 16384, 516);
        this.register("has-children", 1, HasChildren_1.class, BuiltInAtomicType.BOOLEAN, 24576, 0).arg(0, AnyNodeTest.getInstance(), 0x1006000, null);
        this.register("head", 1, HeadFn.class, AnyItemType.getInstance(), 24576, 256).arg(0, AnyItemType.getInstance(), 0x400E000, null);
        this.register("innermost", 1, Innermost.class, AnyNodeTest.getInstance(), 57344, 0).arg(0, AnyNodeTest.getInstance(), 0x800E000, null);
        this.register("nilled", 0, ContextItemAccessorFunction.class, BuiltInAtomicType.BOOLEAN, 24576, 516);
        this.register("node-name", 0, ContextItemAccessorFunction.class, BuiltInAtomicType.QNAME, 24576, 516);
        this.register("outermost", 1, Outermost.class, AnyNodeTest.getInstance(), 57344, 257).arg(0, AnyNodeTest.getInstance(), 0x400E000, null);
        this.register("parse-xml", 1, ParseXml.class, NodeKindTest.DOCUMENT, 24576, 66048).arg(0, BuiltInAtomicType.STRING, 24576, EMPTY);
        this.register("parse-xml-fragment", 1, ParseXmlFragment.class, NodeKindTest.DOCUMENT, 24576, 66048).arg(0, BuiltInAtomicType.STRING, 24576, EMPTY);
        this.register("path", 0, ContextItemAccessorFunction.class, BuiltInAtomicType.STRING, 24576, 516);
        this.register("path", 1, Path_1.class, BuiltInAtomicType.STRING, 24576, 0).arg(0, AnyNodeTest.getInstance(), 0x8006000, null);
        this.register("round", 2, Round.class, NumericType.getInstance(), 24576, 2).arg(0, NumericType.getInstance(), 24576, EMPTY).arg(1, BuiltInAtomicType.INTEGER, 16384, null);
        this.register("serialize", 1, Serialize.class, BuiltInAtomicType.STRING, 16384, 0).arg(0, AnyItemType.getInstance(), 57344, null);
        this.register("sort", 1, Sort_1.class, AnyItemType.getInstance(), 57344, 0).arg(0, AnyItemType.getInstance(), 57344, null);
        this.register("string-join", 1, StringJoin.class, BuiltInAtomicType.STRING, 16384, 0).arg(0, BuiltInAtomicType.ANY_ATOMIC, 57344, StringValue.EMPTY_STRING);
        this.register("tail", 1, TailFn.class, AnyItemType.getInstance(), 57344, 257).arg(0, AnyItemType.getInstance(), 0x400E000, null);
        this.register("unparsed-text", 1, UnparsedText.class, BuiltInAtomicType.STRING, 24576, 520).arg(0, BuiltInAtomicType.STRING, 24576, null);
        this.register("unparsed-text", 2, UnparsedText.class, BuiltInAtomicType.STRING, 24576, 520).arg(0, BuiltInAtomicType.STRING, 24576, null).arg(1, BuiltInAtomicType.STRING, 16384, null);
        this.register("unparsed-text-available", 1, UnparsedTextAvailable.class, BuiltInAtomicType.BOOLEAN, 16384, 520).arg(0, BuiltInAtomicType.STRING, 24576, BooleanValue.FALSE);
        this.register("unparsed-text-available", 2, UnparsedTextAvailable.class, BuiltInAtomicType.BOOLEAN, 16384, 520).arg(0, BuiltInAtomicType.STRING, 24576, BooleanValue.FALSE).arg(1, BuiltInAtomicType.STRING, 16384, null);
        this.register("unparsed-text-lines", 1, UnparsedTextLines.class, BuiltInAtomicType.STRING, 57344, 520).arg(0, BuiltInAtomicType.STRING, 24576, null);
        this.register("unparsed-text-lines", 2, UnparsedTextLines.class, BuiltInAtomicType.STRING, 57344, 520).arg(0, BuiltInAtomicType.STRING, 24576, null).arg(1, BuiltInAtomicType.STRING, 16384, null);
        this.register("uri-collection", 0, UriCollection.class, BuiltInAtomicType.ANY_URI, 57344, 512);
        this.register("uri-collection", 1, UriCollection.class, BuiltInAtomicType.ANY_URI, 57344, 512).arg(0, BuiltInAtomicType.STRING, 24576, null);
    }
}


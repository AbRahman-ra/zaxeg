/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.registry;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.XPathParser;
import net.sf.saxon.functions.ApplyFn;
import net.sf.saxon.functions.Doc_2;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.functions.registry.BuiltInFunctionSet;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.ma.map.MapCreate;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.ma.map.MapUntypedContains;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyElementImpl;
import net.sf.saxon.type.AnyFunctionType;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.NumericType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.StringValue;

public class VendorFunctionSetHE
extends BuiltInFunctionSet {
    private static final VendorFunctionSetHE THE_INSTANCE = new VendorFunctionSetHE();

    public static VendorFunctionSetHE getInstance() {
        return THE_INSTANCE;
    }

    private VendorFunctionSetHE() {
        this.init();
    }

    private void init() {
        this.register("is-whole-number", 1, IsWholeNumberFn.class, BuiltInAtomicType.BOOLEAN, 16384, 0).arg(0, NumericType.getInstance(), 24576, EMPTY);
        this.register("dynamic-error-info", 1, DynamicErrorInfoFn.class, AnyItemType.getInstance(), 57344, 31236).arg(0, BuiltInAtomicType.STRING, 16384, null);
        this.register("apply", 2, ApplyFn.class, AnyItemType.getInstance(), 57344, 512).arg(0, AnyFunctionType.getInstance(), 16384, null).arg(1, ArrayItemType.ANY_ARRAY_TYPE, 16384, null);
        this.register("create-map", 1, MapCreate.class, MapType.ANY_MAP_TYPE, 16384, 0).arg(0, MapType.ANY_MAP_TYPE, 57344, null);
        this.register("doc", 2, Doc_2.class, NodeKindTest.DOCUMENT, 16384, 512).arg(0, BuiltInAtomicType.STRING, 16384, null).arg(1, MapType.ANY_MAP_TYPE, 16384, EMPTY).optionDetails(Doc_2.makeOptionsParameter());
        this.register("has-local-namespaces", 1, HasLocalNamespaces.class, BuiltInAtomicType.BOOLEAN, 16384, 0).arg(0, NodeKindTest.ELEMENT, 16384, null);
        this.register("has-uniform-namespaces", 1, HasUniformNamespaces.class, BuiltInAtomicType.BOOLEAN, 16384, 0).arg(0, NodeKindTest.ELEMENT, 16384, null);
        this.register("map-untyped-contains", 2, MapUntypedContains.class, BuiltInAtomicType.BOOLEAN, 16384, 0).arg(0, MapType.ANY_MAP_TYPE, 57344, null).arg(1, BuiltInAtomicType.ANY_ATOMIC, 16384, null);
    }

    @Override
    public String getNamespace() {
        return "http://saxon.sf.net/";
    }

    @Override
    public String getConventionalPrefix() {
        return "saxon";
    }

    public static class DynamicErrorInfoFn
    extends SystemFunction {
        @Override
        public int getSpecialProperties(Expression[] arguments) {
            return 0;
        }

        @Override
        public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
            String var = arguments[0].head().getStringValue();
            XPathException error = context.getCurrentException();
            if (error == null) {
                return EmptySequence.getInstance();
            }
            Location locator = error.getLocator();
            switch (var) {
                case "code": {
                    StructuredQName errorCodeQName = error.getErrorCodeQName();
                    if (errorCodeQName == null) {
                        errorCodeQName = new StructuredQName("saxon", "http://saxon.sf.net/", "XXXX9999");
                    }
                    return new QNameValue(errorCodeQName, BuiltInAtomicType.QNAME);
                }
                case "description": {
                    String s = error.getMessage();
                    if (error.getCause() != null) {
                        s = s + "(" + error.getCause().getMessage() + ")";
                    }
                    return new StringValue(s);
                }
                case "value": {
                    Sequence value = error.getErrorObject();
                    if (value == null) {
                        return EmptySequence.getInstance();
                    }
                    return value;
                }
                case "module": {
                    String module;
                    String string = module = locator == null ? null : locator.getSystemId();
                    if (module == null) {
                        return EmptySequence.getInstance();
                    }
                    return new StringValue(module);
                }
                case "line-number": {
                    int line;
                    int n = line = locator == null ? -1 : locator.getLineNumber();
                    if (line == -1) {
                        return EmptySequence.getInstance();
                    }
                    return new Int64Value(line);
                }
                case "column-number": {
                    int column = -1;
                    if (locator == null) {
                        return EmptySequence.getInstance();
                    }
                    column = locator instanceof XPathParser.NestedLocation ? ((XPathParser.NestedLocation)locator).getContainingLocation().getColumnNumber() : locator.getColumnNumber();
                    if (column == -1) {
                        return EmptySequence.getInstance();
                    }
                    return new Int64Value(column);
                }
            }
            return EmptySequence.getInstance();
        }
    }

    public static class HasUniformNamespaces
    extends SystemFunction {
        @Override
        public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
            NodeInfo val = (NodeInfo)arguments[0].head();
            if (val instanceof TinyElementImpl) {
                return BooleanValue.get(((TinyElementImpl)val).hasUniformNamespaces());
            }
            return BooleanValue.FALSE;
        }
    }

    public static class HasLocalNamespaces
    extends SystemFunction {
        @Override
        public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
            NodeInfo child = (NodeInfo)arguments[0].head();
            NodeInfo parent = child.getParent();
            return BooleanValue.get(parent == null || parent.getNodeKind() == 9 || child.getAllNamespaces() != parent.getAllNamespaces());
        }
    }

    public static class IsWholeNumberFn
    extends SystemFunction {
        @Override
        public Expression makeOptimizedFunctionCall(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo, Expression ... arguments) throws XPathException {
            if (arguments[0].getItemType().getPrimitiveItemType() == BuiltInAtomicType.INTEGER) {
                return Literal.makeLiteral(BooleanValue.TRUE);
            }
            return null;
        }

        @Override
        public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
            NumericValue val = (NumericValue)arguments[0].head();
            return BooleanValue.get(val != null && val.isWholeNumber());
        }
    }
}


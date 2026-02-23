/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.XPathParser;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.ma.arrays.SimpleArrayItem;
import net.sf.saxon.ma.map.DictionaryMap;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.ma.map.TupleItemType;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.CombinedNodeTest;
import net.sf.saxon.pattern.ContentTypeTest;
import net.sf.saxon.pattern.DocumentNodeTest;
import net.sf.saxon.pattern.LocalNameTest;
import net.sf.saxon.pattern.MultipleNodeKindTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NamespaceTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.SchemaNodeTest;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyFunctionType;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ExternalObjectType;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.type.LocalUnionType;
import net.sf.saxon.type.MissingComponentException;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.PrimitiveUType;
import net.sf.saxon.type.SchemaDeclaration;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.type.UType;
import net.sf.saxon.type.UnionType;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class AlphaCode {
    public static MapItem toXdmMap(String input) {
        MapItemCallBack callBack = new MapItemCallBack();
        AlphaCodeParser parser = new AlphaCodeParser(input, callBack);
        return (MapItem)parser.parseType();
    }

    public static String fromXdmMap(MapItem map) {
        StringBuilder out = new StringBuilder();
        StringValue indicator = (StringValue)map.get(new StringValue("o"));
        out.append(indicator == null ? "1" : indicator.getStringValue());
        StringValue alphaCode = (StringValue)map.get(new StringValue("p"));
        out.append(alphaCode == null ? "" : alphaCode.getStringValue());
        out.append(" ");
        block19: for (KeyValuePair kvp : map.keyValuePairs()) {
            String key;
            switch (key = kvp.key.getStringValue()) {
                case "o": 
                case "p": {
                    continue block19;
                }
                case "n": 
                case "c": 
                case "t": {
                    out.append(key);
                    out.append(((StringValue)kvp.value).getStringValue());
                    out.append(" ");
                    continue block19;
                }
                case "k": 
                case "r": 
                case "v": 
                case "e": {
                    out.append(key);
                    out.append('[');
                    out.append(AlphaCode.fromXdmMap((MapItem)kvp.value));
                    out.append(']');
                    out.append(" ");
                    continue block19;
                }
                case "a": 
                case "u": {
                    out.append(key);
                    out.append('[');
                    ArrayItem types = (ArrayItem)kvp.value;
                    boolean first = true;
                    for (GroundedValue t : types.members()) {
                        if (first) {
                            first = false;
                        } else {
                            out.append(",");
                        }
                        out.append(AlphaCode.fromXdmMap((MapItem)t));
                    }
                    out.append(']');
                    out.append(" ");
                    continue block19;
                }
            }
            throw new IllegalStateException("Unexpected key '" + key + "'");
        }
        return out.toString();
    }

    public static SequenceType toSequenceType(String input, Configuration config) {
        TreeCallBack callBack = new TreeCallBack();
        AlphaCodeParser parser = new AlphaCodeParser(input, callBack);
        AlphaCodeTree tree = (AlphaCodeTree)parser.parseType();
        return AlphaCode.sequenceTypeFromTree(tree, config);
    }

    public static ItemType toItemType(String input, Configuration config) {
        SequenceType st = AlphaCode.toSequenceType(input, config);
        if (st.getCardinality() != 16384) {
            throw new IllegalArgumentException("Supplied alphacode has a cardinality other than 1");
        }
        return st.getPrimaryType();
    }

    private static SequenceType sequenceTypeFromTree(AlphaCodeTree tree, Configuration config) {
        ItemType itemType;
        block98: {
            String principal;
            block100: {
                String name;
                ContentTypeTest contentTest;
                block102: {
                    block101: {
                        block99: {
                            block97: {
                                principal = tree.principal;
                                itemType = null;
                                if (!principal.isEmpty()) break block97;
                                itemType = AnyItemType.getInstance();
                                break block98;
                            }
                            if (!principal.startsWith("A")) break block99;
                            BuiltInAtomicType builtIn = BuiltInAtomicType.fromAlphaCode(principal);
                            if (builtIn == null) {
                                throw new IllegalArgumentException("Unknown type " + principal);
                            }
                            itemType = builtIn;
                            if (tree.name != null) {
                                SchemaType type = config.getSchemaType(StructuredQName.fromEQName(tree.name));
                                if (!(type instanceof PlainType)) {
                                    throw new IllegalArgumentException("Schema type " + tree.name + " is not known");
                                }
                                itemType = (PlainType)((Object)type);
                            } else if (builtIn == BuiltInAtomicType.ANY_ATOMIC && tree.members != null) {
                                ArrayList<AtomicType> members = new ArrayList<AtomicType>();
                                for (AlphaCodeTree m : tree.members) {
                                    SequenceType st = AlphaCode.sequenceTypeFromTree(m, config);
                                    if (!st.getPrimaryType().isAtomicType()) continue;
                                    AtomicType primaryType = (AtomicType)st.getPrimaryType();
                                    members.add(primaryType);
                                }
                                itemType = new LocalUnionType(members);
                            }
                            break block98;
                        }
                        if (!principal.startsWith("N")) break block100;
                        String contentName = tree.content;
                        contentTest = null;
                        boolean nillable = tree.nillable;
                        if (contentName != null) {
                            StructuredQName contentQName = StructuredQName.fromEQName(contentName);
                            SchemaType contentType = config.getSchemaType(contentQName);
                            if (contentType == null) {
                                throw new IllegalArgumentException("Unknown type " + contentName);
                            }
                            contentTest = new ContentTypeTest(principal.equals("NE") ? 1 : 2, contentType, config, nillable);
                        }
                        if (tree.vennOperands == null) break block101;
                        if (tree.vennOperands.length == 2) {
                            NodeTest nt0 = (NodeTest)AlphaCode.sequenceTypeFromTree(tree.vennOperands[0], config).getPrimaryType();
                            NodeTest nt1 = (NodeTest)AlphaCode.sequenceTypeFromTree(tree.vennOperands[1], config).getPrimaryType();
                            itemType = new CombinedNodeTest(nt0, tree.vennOperator, nt1);
                        } else {
                            assert (tree.vennOperator == 1);
                            UType u = UType.VOID;
                            for (int i = 0; i < tree.vennOperands.length; ++i) {
                                ItemType it = AlphaCode.sequenceTypeFromTree(tree.vennOperands[i], config).getPrimaryType();
                                assert (it instanceof NodeKindTest);
                                u = u.union(it.getUType());
                            }
                            itemType = new MultipleNodeKindTest(u);
                        }
                        break block98;
                    }
                    int kind = 0;
                    if (principal.length() >= 2) {
                        switch (principal.substring(0, 2)) {
                            case "NT": {
                                kind = 3;
                                break;
                            }
                            case "NC": {
                                kind = 8;
                                break;
                            }
                            case "NN": {
                                kind = 13;
                                break;
                            }
                            case "NP": {
                                kind = 7;
                                break;
                            }
                            case "ND": {
                                kind = 9;
                                break;
                            }
                            case "NE": {
                                kind = 1;
                                break;
                            }
                            case "NA": {
                                kind = 2;
                            }
                        }
                    }
                    name = tree.name;
                    NodeTest partialNameTest = null;
                    if (name != null && name.contains("*")) {
                        if (name.startsWith("*:")) {
                            partialNameTest = new LocalNameTest(config.getNamePool(), kind, name.substring(2));
                        } else if (name.endsWith("}*")) {
                            String uri = name.substring(2, name.length() - 2);
                            partialNameTest = new NamespaceTest(config.getNamePool(), kind, uri);
                        }
                    }
                    if (partialNameTest == null) break block102;
                    itemType = partialNameTest;
                    break block98;
                }
                StructuredQName qName = name == null ? null : StructuredQName.fromEQName(name);
                switch (principal) {
                    case "N": {
                        itemType = AnyNodeTest.getInstance();
                        break;
                    }
                    case "NT": {
                        itemType = NodeKindTest.TEXT;
                        break;
                    }
                    case "NC": {
                        itemType = NodeKindTest.COMMENT;
                        break;
                    }
                    case "NN": {
                        if (name == null) {
                            itemType = NodeKindTest.NAMESPACE;
                            break;
                        }
                        itemType = new NameTest(13, "", qName.getLocalPart(), config.getNamePool());
                        break;
                    }
                    case "NP": {
                        if (name == null) {
                            itemType = NodeKindTest.PROCESSING_INSTRUCTION;
                            break;
                        }
                        itemType = new NameTest(7, "", qName.getLocalPart(), config.getNamePool());
                        break;
                    }
                    case "ND": {
                        AlphaCodeTree elementType = tree.elementType;
                        if (elementType == null) {
                            itemType = NodeKindTest.DOCUMENT;
                            break;
                        }
                        ItemType e = AlphaCode.sequenceTypeFromTree(elementType, config).getPrimaryType();
                        itemType = new DocumentNodeTest((NodeTest)e);
                        break;
                    }
                    case "NE": {
                        if (qName == null) {
                            if (contentTest == null) {
                                itemType = NodeKindTest.ELEMENT;
                                break;
                            }
                            itemType = contentTest;
                            break;
                        }
                        itemType = new NameTest(1, qName.getURI(), qName.getLocalPart(), config.getNamePool());
                        if (contentTest != null) {
                            itemType = new CombinedNodeTest((NodeTest)itemType, 23, contentTest);
                            break;
                        }
                        break block98;
                    }
                    case "NA": {
                        if (qName == null) {
                            if (contentTest == null) {
                                itemType = NodeKindTest.ATTRIBUTE;
                                break;
                            }
                            itemType = contentTest;
                            break;
                        }
                        itemType = new NameTest(2, qName.getURI(), qName.getLocalPart(), config.getNamePool());
                        if (contentTest != null) {
                            itemType = new CombinedNodeTest((NodeTest)itemType, 23, contentTest);
                            break;
                        }
                        break block98;
                    }
                    case "NES": {
                        assert (qName != null);
                        SchemaDeclaration decl = config.getElementDeclaration(qName);
                        if (decl != null) {
                            try {
                                itemType = decl.makeSchemaNodeTest();
                            } catch (MissingComponentException missingComponentException) {
                                // empty catch block
                            }
                        }
                        if (itemType == null) {
                            itemType = new NameTest(1, qName.getURI(), qName.getLocalPart(), config.getNamePool());
                            break;
                        }
                        break block98;
                    }
                    case "NAS": {
                        assert (qName != null);
                        SchemaDeclaration decl = config.getAttributeDeclaration(qName);
                        if (decl != null) {
                            try {
                                itemType = decl.makeSchemaNodeTest();
                            } catch (MissingComponentException missingComponentException) {
                                // empty catch block
                            }
                        }
                        if (itemType == null) {
                            itemType = new NameTest(2, qName.getURI(), qName.getLocalPart(), config.getNamePool());
                            break;
                        }
                        break block98;
                    }
                    default: {
                        itemType = AnyNodeTest.getInstance();
                    }
                }
                break block98;
            }
            if (principal.startsWith("F")) {
                if (principal.equals("FA")) {
                    AlphaCodeTree valueType = tree.valueType;
                    itemType = valueType == null ? ArrayItemType.ANY_ARRAY_TYPE : new ArrayItemType(AlphaCode.sequenceTypeFromTree(valueType, config));
                } else if (principal.equals("FM")) {
                    if (tree.fieldNames == null) {
                        AlphaCodeTree keyType = tree.keyType;
                        AlphaCodeTree valueType = tree.valueType;
                        if (keyType != null && valueType != null) {
                            AtomicType a = (AtomicType)AlphaCode.sequenceTypeFromTree(keyType, config).getPrimaryType();
                            SequenceType v = AlphaCode.sequenceTypeFromTree(valueType, config);
                            itemType = new MapType(a, v);
                        } else {
                            itemType = MapType.ANY_MAP_TYPE;
                        }
                    } else {
                        ArrayList<SequenceType> fieldTypes = new ArrayList<SequenceType>(tree.argTypes.size());
                        for (AlphaCodeTree t : tree.argTypes) {
                            fieldTypes.add(AlphaCode.sequenceTypeFromTree(t, config));
                        }
                        itemType = new TupleItemType(tree.fieldNames, fieldTypes, tree.extensibleTupleType);
                    }
                } else {
                    AlphaCodeTree returnType = tree.resultType;
                    List<AlphaCodeTree> argTypes = tree.argTypes;
                    if (argTypes == null) {
                        itemType = AnyFunctionType.getInstance();
                    } else {
                        SequenceType r = returnType == null ? SequenceType.ANY_SEQUENCE : AlphaCode.sequenceTypeFromTree(returnType, config);
                        SequenceType[] a = new SequenceType[argTypes.size()];
                        for (int i = 0; i < a.length; ++i) {
                            a[i] = AlphaCode.sequenceTypeFromTree(argTypes.get(i), config);
                        }
                        itemType = new SpecificFunctionType(a, r);
                    }
                }
            } else if (principal.startsWith("X")) {
                Class theClass = Object.class;
                if (tree.name != null) {
                    String className = StructuredQName.fromEQName(tree.name).getLocalPart();
                    try {
                        theClass = Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        theClass = Object.class;
                    }
                }
                itemType = new JavaExternalObjectType(config, theClass);
            }
        }
        String indicator = tree.cardinality;
        int cardinality = Cardinality.fromOccurrenceIndicator(indicator);
        return SequenceType.makeSequenceType(itemType, cardinality);
    }

    private static AlphaCodeTree makeTree(SequenceType sequenceType) {
        AlphaCodeTree tree = AlphaCode.makeTree(sequenceType.getPrimaryType());
        if (sequenceType.getCardinality() != 16384) {
            tree.cardinality = Cardinality.getOccurrenceIndicator(sequenceType.getCardinality());
        }
        return tree;
    }

    private static AlphaCodeTree makeTree(ItemType primary) {
        AlphaCodeTree result = new AlphaCodeTree();
        result.principal = primary.getBasicAlphaCode();
        result.cardinality = "1";
        if (primary instanceof AtomicType && !((AtomicType)primary).isBuiltInType()) {
            result.name = ((AtomicType)primary).getEQName();
        } else if (primary instanceof UnionType) {
            StructuredQName name = ((UnionType)primary).getTypeName();
            if (name.getURI().equals("http://www.w3.org/2001/XMLSchema")) {
                result.name = "~" + name.getLocalPart();
            } else if (name.getURI().equals("http://ns.saxonica.com/anonymous-type")) {
                try {
                    ArrayList<AlphaCodeTree> memberMaps = new ArrayList<AlphaCodeTree>();
                    for (PlainType plainType : ((UnionType)primary).getPlainMemberTypes()) {
                        memberMaps.add(AlphaCode.makeTree(plainType));
                    }
                    result.members = memberMaps;
                } catch (MissingComponentException memberMaps) {}
            } else {
                result.name = name.getEQName();
            }
        } else if (primary instanceof NameTest) {
            StructuredQName name = ((NameTest)primary).getMatchingNodeName();
            result.name = name.getEQName();
        } else if (primary instanceof SchemaNodeTest) {
            StructuredQName name = ((SchemaNodeTest)((Object)primary)).getNodeName();
            result.name = name.getEQName();
        } else if (primary instanceof LocalNameTest) {
            result.name = "*:" + ((LocalNameTest)primary).getLocalName();
        } else if (primary instanceof NamespaceTest) {
            result.name = "Q{" + ((NamespaceTest)primary).getNamespaceURI() + "}*";
        } else if (primary instanceof CombinedNodeTest) {
            CombinedNodeTest combi = (CombinedNodeTest)primary;
            String c = combi.getContentTypeForAlphaCode();
            if (c != null) {
                result.content = c;
                result.name = combi.getMatchingNodeName().getEQName();
                result.nillable = combi.isNillable();
            } else {
                result.vennOperator = combi.getOperator();
                result.vennOperands = new AlphaCodeTree[2];
                result.vennOperands[0] = AlphaCode.makeTree(combi.getOperand(0));
                result.vennOperands[1] = AlphaCode.makeTree(combi.getOperand(1));
            }
        } else if (primary instanceof MultipleNodeKindTest) {
            result.vennOperator = 1;
            Set<PrimitiveUType> types = primary.getUType().decompose();
            result.vennOperands = new AlphaCodeTree[types.size()];
            int i = 0;
            for (PrimitiveUType primitiveUType : types) {
                result.vennOperands[i++] = AlphaCode.makeTree(primitiveUType.toItemType());
            }
        } else if (primary instanceof ContentTypeTest) {
            result.content = ((ContentTypeTest)primary).getContentType().getEQName();
        } else if (primary instanceof DocumentNodeTest) {
            NodeTest content = ((DocumentNodeTest)primary).getElementTest();
            result.elementType = AlphaCode.makeTree(content);
        } else if (primary instanceof FunctionItemType) {
            if (primary instanceof ArrayItemType) {
                SequenceType memberType = ((ArrayItemType)primary).getMemberType();
                if (memberType != SequenceType.ANY_SEQUENCE) {
                    result.valueType = AlphaCode.makeTree(memberType);
                }
            } else if (primary instanceof TupleItemType) {
                result.extensibleTupleType = ((TupleItemType)primary).isExtensible();
                result.fieldNames = new ArrayList<String>();
                result.argTypes = new ArrayList<AlphaCodeTree>();
                for (String s : ((TupleItemType)primary).getFieldNames()) {
                    result.fieldNames.add(s);
                    result.argTypes.add(AlphaCode.makeTree(((TupleItemType)primary).getFieldType(s)));
                }
            } else if (primary instanceof MapType) {
                SequenceType valueType;
                AtomicType keyType = ((MapType)primary).getKeyType();
                if (keyType != BuiltInAtomicType.ANY_ATOMIC) {
                    result.keyType = AlphaCode.makeTree(keyType);
                }
                if ((valueType = ((MapType)primary).getValueType()) != SequenceType.ANY_SEQUENCE) {
                    result.valueType = AlphaCode.makeTree(valueType);
                }
            } else {
                SequenceType[] argTypes;
                SequenceType resultType = ((FunctionItemType)primary).getResultType();
                if (resultType != SequenceType.ANY_SEQUENCE) {
                    result.resultType = AlphaCode.makeTree(resultType);
                }
                if ((argTypes = ((FunctionItemType)primary).getArgumentTypes()) != null) {
                    ArrayList<AlphaCodeTree> argMaps = new ArrayList<AlphaCodeTree>();
                    for (SequenceType at : argTypes) {
                        argMaps.add(AlphaCode.makeTree(at));
                    }
                    result.argTypes = argMaps;
                }
            }
        } else if (primary instanceof ExternalObjectType) {
            result.name = ((ExternalObjectType)primary).getName();
        }
        return result;
    }

    private static String abbreviateEQName(String in) {
        if (in.startsWith("Q{http://www.w3.org/2001/XMLSchema}")) {
            return "~" + in.substring("Q{http://www.w3.org/2001/XMLSchema}".length());
        }
        return in;
    }

    private static void alphaCodeFromTree(AlphaCodeTree tree, boolean withCardinality, StringBuilder sb) {
        boolean first;
        if (withCardinality) {
            sb.append(tree.cardinality);
        }
        sb.append(tree.principal);
        if (tree.name != null) {
            sb.append(" n").append(AlphaCode.abbreviateEQName(tree.name));
        }
        if (tree.content != null) {
            sb.append(" c").append(AlphaCode.abbreviateEQName(tree.content));
            if (tree.nillable) {
                sb.append("?");
            }
        }
        if (tree.keyType != null) {
            sb.append(" k[");
            AlphaCode.alphaCodeFromTree(tree.keyType, false, sb);
            sb.append("]");
        }
        if (tree.valueType != null) {
            sb.append(" v[");
            AlphaCode.alphaCodeFromTree(tree.valueType, true, sb);
            sb.append("]");
        }
        if (tree.resultType != null) {
            sb.append(" r[");
            AlphaCode.alphaCodeFromTree(tree.resultType, true, sb);
            sb.append("]");
        }
        if (tree.argTypes != null) {
            sb.append(" a[");
            first = true;
            for (AlphaCodeTree a : tree.argTypes) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                AlphaCode.alphaCodeFromTree(a, true, sb);
            }
            sb.append("]");
        }
        if (tree.members != null) {
            sb.append(" m[");
            first = true;
            for (AlphaCodeTree a : tree.members) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                AlphaCode.alphaCodeFromTree(a, false, sb);
            }
            sb.append("]");
        }
        if (tree.elementType != null) {
            sb.append(" e[");
            AlphaCode.alphaCodeFromTree(tree.elementType, false, sb);
            sb.append("]");
        }
        if (tree.vennOperands != null) {
            String operator = tree.vennOperator == 23 ? "i" : (tree.vennOperator == 1 ? "u" : "d");
            sb.append(" ").append(operator).append("[");
            for (int i = 0; i < tree.vennOperands.length; ++i) {
                if (i != 0) {
                    sb.append(",");
                }
                AlphaCode.alphaCodeFromTree(tree.vennOperands[i], false, sb);
            }
            sb.append("]");
        }
        if (tree.fieldNames != null) {
            sb.append(tree.extensibleTupleType ? " F[" : " f[");
            boolean first2 = true;
            for (String s : tree.fieldNames) {
                if (!first2) {
                    sb.append(",");
                } else {
                    first2 = false;
                }
                sb.append(s.replace("\\", "\\\\").replace(",", "\\,").replace("]", "\\]"));
            }
            sb.append("]");
        }
    }

    public static String fromItemType(ItemType type) {
        AlphaCodeTree tree = AlphaCode.makeTree(type);
        StringBuilder sb = new StringBuilder();
        AlphaCode.alphaCodeFromTree(tree, false, sb);
        return sb.toString().trim();
    }

    public static String fromSequenceType(SequenceType type) {
        if (type == SequenceType.EMPTY_SEQUENCE) {
            return "0";
        }
        String s = AlphaCode.fromItemType(type.getPrimaryType());
        if (type.getCardinality() == 16384) {
            return "1" + s;
        }
        return Cardinality.getOccurrenceIndicator(type.getCardinality()) + s;
    }

    public static String fromLexicalSequenceType(XPathContext context, String input) throws XPathException {
        XPathParser parser = context.getConfiguration().newExpressionParser("XP", false, 31);
        IndependentContext env = new IndependentContext(context.getConfiguration());
        env.declareNamespace("xs", "http://www.w3.org/2001/XMLSchema");
        env.declareNamespace("fn", "http://www.w3.org/2005/xpath-functions");
        SequenceType st = parser.parseSequenceType(input, env);
        return AlphaCode.fromSequenceType(st);
    }

    private static class AlphaCodeTree {
        String cardinality;
        String principal;
        String name;
        String content;
        boolean nillable;
        List<AlphaCodeTree> members;
        AlphaCodeTree keyType;
        AlphaCodeTree valueType;
        AlphaCodeTree resultType;
        List<AlphaCodeTree> argTypes;
        AlphaCodeTree elementType;
        int vennOperator;
        AlphaCodeTree[] vennOperands;
        List<String> fieldNames;
        boolean extensibleTupleType;

        private AlphaCodeTree() {
        }
    }

    private static class AlphaCodeParser<T> {
        private String input;
        private int position = 0;
        private ParserCallBack<T> callBack;

        private AlphaCodeParser(String input, ParserCallBack<T> callBack) {
            this.input = input;
            this.callBack = callBack;
        }

        private int nextChar() {
            if (this.position >= this.input.length()) {
                return -1;
            }
            return this.input.charAt(this.position++);
        }

        private String nextToken() {
            int inBraces = 0;
            int start = this.position;
            while (this.position < this.input.length()) {
                char ch = this.input.charAt(this.position++);
                switch (ch) {
                    case '{': {
                        ++inBraces;
                        break;
                    }
                    case '}': {
                        --inBraces;
                        break;
                    }
                    case ',': 
                    case ']': {
                        if (inBraces != 0) break;
                        return this.input.substring(start, --this.position);
                    }
                    case ' ': {
                        if (inBraces != 0) break;
                        return this.input.substring(start, this.position - 1);
                    }
                }
            }
            return this.input.substring(start, this.position);
        }

        private void expect(char c) {
            int d = this.nextChar();
            if (d != c) {
                throw new IllegalStateException("Expected '" + c + "', found '" + (d == -1 ? "<eof>" : Character.valueOf((char)d)) + "'");
            }
        }

        T parseType() {
            T container = this.callBack.makeContainer();
            int indicator = this.nextChar();
            if (indicator < 0) {
                this.callBack.setStringProperty(container, "o", "1");
            } else if ("*+1?0\u00b0".indexOf((char)indicator) >= 0) {
                if (indicator == 176) {
                    indicator = 48;
                }
                this.callBack.setStringProperty(container, "o", "" + (char)indicator);
            } else {
                this.callBack.setStringProperty(container, "o", "1");
                --this.position;
            }
            String primary = this.nextToken();
            this.callBack.setStringProperty(container, "p", primary);
            block8: while (this.position < this.input.length()) {
                char c = this.input.charAt(this.position);
                switch (c) {
                    case ',': 
                    case ']': {
                        return container;
                    }
                    case ' ': {
                        ++this.position;
                        continue block8;
                    }
                    case 'c': 
                    case 'n': {
                        ++this.position;
                        String token = this.nextToken();
                        if (token.startsWith("~")) {
                            token = "Q{http://www.w3.org/2001/XMLSchema}" + token.substring(1);
                        }
                        if (c == 'c' && token.endsWith("?")) {
                            this.callBack.setStringProperty(container, "z", "1");
                            token = token.substring(0, token.length() - 1);
                        }
                        this.callBack.setStringProperty(container, "" + c, token);
                        continue block8;
                    }
                    case 'e': 
                    case 'k': 
                    case 'r': 
                    case 'v': {
                        ++this.position;
                        this.expect('[');
                        T nestedType = this.parseType();
                        this.expect(']');
                        this.callBack.setTypeProperty(container, "" + c, nestedType);
                        continue block8;
                    }
                    case 'a': 
                    case 'd': 
                    case 'i': 
                    case 'm': 
                    case 'u': {
                        ++this.position;
                        this.expect('[');
                        ArrayList<T> nestedTypes = new ArrayList<T>();
                        if (this.input.charAt(this.position) == ']') {
                            ++this.position;
                            this.callBack.setMultiTypeProperty(container, "" + c, nestedTypes);
                            continue block8;
                        }
                        while (true) {
                            nestedTypes.add(this.parseType());
                            if (this.input.charAt(this.position) != ',') break;
                            ++this.position;
                        }
                        this.expect(']');
                        this.callBack.setMultiTypeProperty(container, "" + c, nestedTypes);
                        continue block8;
                    }
                    case 'F': 
                    case 'f': {
                        if (c == 'F') {
                            this.callBack.setStringProperty(container, "x", "1");
                        }
                        ++this.position;
                        this.expect('[');
                        ArrayList<String> fieldNames = new ArrayList<String>();
                        StringBuilder currName = new StringBuilder();
                        boolean escaped = false;
                        while (true) {
                            char ch;
                            if ((ch = this.input.charAt(this.position++)) == '\\' && !escaped) {
                                escaped = true;
                                continue;
                            }
                            if (ch == ',' && !escaped) {
                                fieldNames.add(currName.toString());
                                currName.setLength(0);
                                escaped = false;
                                continue;
                            }
                            if (ch == ']' && !escaped) {
                                fieldNames.add(currName.toString());
                                currName.setLength(0);
                                this.callBack.setMultiStringProperty(container, "f", fieldNames);
                                continue block8;
                            }
                            currName.append(ch);
                            escaped = false;
                        }
                    }
                }
                throw new IllegalStateException("Expected one of n|c|t|k|r|v|a|u, found '" + c + "'");
            }
            return container;
        }
    }

    private static class TreeCallBack
    implements ParserCallBack<AlphaCodeTree> {
        private TreeCallBack() {
        }

        @Override
        public AlphaCodeTree makeContainer() {
            return new AlphaCodeTree();
        }

        @Override
        public void setStringProperty(AlphaCodeTree tree, String key, String value) {
            switch (key) {
                case "o": {
                    tree.cardinality = value;
                    break;
                }
                case "p": {
                    tree.principal = value;
                    break;
                }
                case "n": {
                    tree.name = value;
                    break;
                }
                case "c": {
                    tree.content = value;
                    break;
                }
                case "z": {
                    tree.nillable = true;
                    break;
                }
                case "x": {
                    tree.extensibleTupleType = true;
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Bad alphacode component " + key);
                }
            }
        }

        @Override
        public void setMultiStringProperty(AlphaCodeTree tree, String key, List<String> value) {
            if (!key.equals("f")) {
                throw new IllegalArgumentException("Bad alphacode component " + key);
            }
            tree.fieldNames = value;
        }

        @Override
        public void setTypeProperty(AlphaCodeTree tree, String key, AlphaCodeTree value) {
            switch (key) {
                case "k": {
                    tree.keyType = value;
                    break;
                }
                case "v": {
                    tree.valueType = value;
                    break;
                }
                case "r": {
                    tree.resultType = value;
                    break;
                }
                case "e": {
                    tree.elementType = value;
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Bad alphacode component " + key);
                }
            }
        }

        @Override
        public void setMultiTypeProperty(AlphaCodeTree tree, String key, List<AlphaCodeTree> value) {
            switch (key) {
                case "a": {
                    tree.argTypes = value;
                    break;
                }
                case "m": {
                    tree.members = value;
                    break;
                }
                case "i": {
                    tree.vennOperands = value.toArray(new AlphaCodeTree[0]);
                    tree.vennOperator = 23;
                    break;
                }
                case "u": {
                    tree.vennOperands = value.toArray(new AlphaCodeTree[0]);
                    tree.vennOperator = 1;
                    break;
                }
                case "d": {
                    tree.vennOperands = value.toArray(new AlphaCodeTree[0]);
                    tree.vennOperator = 24;
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Bad alphacode component " + key);
                }
            }
        }
    }

    private static class MapItemCallBack
    implements ParserCallBack<DictionaryMap> {
        private MapItemCallBack() {
        }

        @Override
        public DictionaryMap makeContainer() {
            return new DictionaryMap();
        }

        @Override
        public void setStringProperty(DictionaryMap container, String key, String value) {
            container.initialPut(key, new StringValue(value));
        }

        @Override
        public void setMultiStringProperty(DictionaryMap container, String key, List<String> value) {
            ArrayList<StringValue> xdmValue = new ArrayList<StringValue>();
            for (String v : value) {
                xdmValue.add(new StringValue(v));
            }
            container.initialPut(key, new SequenceExtent(xdmValue));
        }

        @Override
        public void setTypeProperty(DictionaryMap container, String key, DictionaryMap value) {
            container.initialPut(key, value);
        }

        @Override
        public void setMultiTypeProperty(DictionaryMap container, String key, List<DictionaryMap> value) {
            ArrayList<GroundedValue> contents = new ArrayList<GroundedValue>(value);
            container.initialPut(key, new SimpleArrayItem(contents));
        }
    }

    private static interface ParserCallBack<T> {
        public T makeContainer();

        public void setStringProperty(T var1, String var2, String var3);

        public void setMultiStringProperty(T var1, String var2, List<String> var3);

        public void setTypeProperty(T var1, String var2, T var3);

        public void setMultiTypeProperty(T var1, String var2, List<T> var3);
    }
}


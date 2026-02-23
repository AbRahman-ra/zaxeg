/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.SequenceCopier;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.functions.AccessorFn;
import net.sf.saxon.functions.OptionsParameter;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.SerializerFactory;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.serialize.CharacterMap;
import net.sf.saxon.serialize.CharacterMapIndex;
import net.sf.saxon.serialize.SerializationParamsHandler;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.z.IntHashMap;

public class Serialize
extends SystemFunction
implements Callable {
    private String[] paramNames = new String[]{"allow-duplicate-names", "byte-order-mark", "cdata-section-elements", "doctype-public", "doctype-system", "encoding", "escape-uri-attributes", "html-version", "include-content-type", "indent", "item-separator", "json-node-output-method", "media-type", "method", "normalization-form", "omit-xml-declaration", "standalone", "suppress-indentation", "undeclare-prefixes", "use-character-maps", "version"};
    private String[] paramNamesSaxon = new String[]{"attribute-order", "canonical", "character-representation", "double-space", "indent-spaces", "line-length", "newline", "property-order", "recognize-binary", "require-well-formed", "single-quotes", "supply-source-locator", "suppress-indentation"};
    private static final Map<String, SequenceType> requiredTypes = new HashMap<String, SequenceType>(40);
    private static final Map<String, SequenceType> requiredTypesSaxon;

    public static OptionsParameter makeOptionsParameter() {
        SequenceType listOfQNames = BuiltInAtomicType.QNAME.zeroOrMore();
        OptionsParameter op = new OptionsParameter();
        op.addAllowedOption("allow-duplicate-names", SequenceType.SINGLE_BOOLEAN);
        op.addAllowedOption("byte-order-mark", SequenceType.SINGLE_BOOLEAN);
        op.addAllowedOption("cdata-section-elements", listOfQNames, EmptySequence.getInstance());
        op.addAllowedOption("doctype-public", SequenceType.SINGLE_STRING);
        op.addAllowedOption("doctype-system", SequenceType.SINGLE_STRING);
        op.addAllowedOption("encoding", SequenceType.SINGLE_STRING);
        op.addAllowedOption("escape-uri-attributes", SequenceType.SINGLE_BOOLEAN);
        op.addAllowedOption("html-version", SequenceType.SINGLE_DECIMAL);
        op.addAllowedOption("include-content-type", SequenceType.SINGLE_BOOLEAN);
        op.addAllowedOption("indent", SequenceType.SINGLE_BOOLEAN);
        op.addAllowedOption("item-separator", SequenceType.SINGLE_STRING);
        op.addAllowedOption("json-node-output-method", SequenceType.SINGLE_STRING);
        op.addAllowedOption("media-type", SequenceType.SINGLE_STRING);
        op.addAllowedOption("method", SequenceType.SINGLE_STRING);
        op.addAllowedOption("normalization-form", SequenceType.SINGLE_STRING);
        op.addAllowedOption("omit-xml-declaration", SequenceType.SINGLE_BOOLEAN);
        op.addAllowedOption("standalone", SequenceType.OPTIONAL_BOOLEAN);
        op.addAllowedOption("suppress-indentation", listOfQNames);
        op.addAllowedOption("undeclare-prefixes", SequenceType.SINGLE_BOOLEAN);
        op.addAllowedOption("use-character-maps", SequenceType.makeSequenceType(MapType.ANY_MAP_TYPE, 16384));
        op.addAllowedOption("version", SequenceType.SINGLE_STRING);
        op.addAllowedOption(Serialize.sx("attribute-order"), SequenceType.ATOMIC_SEQUENCE);
        op.addAllowedOption(Serialize.sx("canonical"), SequenceType.SINGLE_BOOLEAN);
        op.addAllowedOption(Serialize.sx("character-representation"), SequenceType.SINGLE_STRING);
        op.addAllowedOption(Serialize.sx("double-space"), listOfQNames);
        op.addAllowedOption(Serialize.sx("indent-spaces"), SequenceType.SINGLE_INTEGER);
        op.addAllowedOption(Serialize.sx("line-length"), SequenceType.SINGLE_INTEGER);
        op.addAllowedOption(Serialize.sx("newline"), SequenceType.SINGLE_STRING);
        op.addAllowedOption(Serialize.sx("property-order"), SequenceType.STRING_SEQUENCE);
        op.addAllowedOption(Serialize.sx("recognize-binary"), SequenceType.SINGLE_BOOLEAN);
        op.addAllowedOption(Serialize.sx("require-well-formed"), SequenceType.SINGLE_BOOLEAN);
        op.addAllowedOption(Serialize.sx("single-quotes"), SequenceType.SINGLE_BOOLEAN);
        op.addAllowedOption(Serialize.sx("supply-source-locator"), SequenceType.SINGLE_BOOLEAN);
        return op;
    }

    private static String sx(String s) {
        return "Q{http://saxon.sf.net/}" + s;
    }

    private boolean isParamName(String string) {
        for (String s : this.paramNames) {
            if (!s.equals(string)) continue;
            return true;
        }
        return false;
    }

    private boolean isParamNameSaxon(String string) {
        for (String s : this.paramNamesSaxon) {
            if (!s.equals(string)) continue;
            return true;
        }
        return false;
    }

    private MapItem checkOptions(MapItem map, XPathContext context) throws XPathException {
        Item key;
        HashTrieMap result = new HashTrieMap();
        TypeHierarchy th = context.getConfiguration().getTypeHierarchy();
        AtomicIterator<? extends AtomicValue> keysIterator = map.keys();
        while ((key = keysIterator.next()) != null) {
            Sequence converted;
            RoleDiagnostic role;
            String keyName;
            if (key instanceof StringValue) {
                keyName = ((AtomicValue)key).getStringValue();
                if (!this.isParamName(keyName)) continue;
                role = new RoleDiagnostic(15, keyName, 0);
                role.setErrorCode("XPTY0004");
                converted = th.applyFunctionConversionRules(map.get((AtomicValue)key), requiredTypes.get(keyName), role, Loc.NONE);
                result = result.addEntry((AtomicValue)key, converted.materialize());
                continue;
            }
            if (!(key instanceof QNameValue)) break;
            if (((AtomicValue)key).getComponent(AccessorFn.Component.NAMESPACE).getStringValue().equals("")) {
                throw new XPathException("A serialization parameter supplied with a QName key must have non-absent namespace", "SEPM0017");
            }
            if (!((AtomicValue)key).getComponent(AccessorFn.Component.NAMESPACE).getStringValue().equals("http://saxon.sf.net/") || !this.isParamNameSaxon(keyName = ((QNameValue)key).getLocalName())) continue;
            role = new RoleDiagnostic(15, keyName, 0);
            converted = th.applyFunctionConversionRules(map.get((AtomicValue)key), requiredTypesSaxon.get(keyName), role, Loc.NONE);
            result = result.addEntry((AtomicValue)key, converted.materialize());
        }
        return result;
    }

    private String toYesNoTypeString(Sequence seqVal) throws XPathException {
        boolean booleanValue = ((BooleanValue)seqVal.head()).getBooleanValue();
        String s = booleanValue ? "yes" : "no";
        return s;
    }

    private String toYesNoOmitTypeString(Sequence seqVal) throws XPathException {
        String stringVal = "";
        if (seqVal instanceof EmptySequence) {
            stringVal = "omit";
        } else if (seqVal.head() instanceof BooleanValue) {
            stringVal = this.toYesNoTypeString(seqVal);
        }
        return stringVal;
    }

    private String toQNamesTypeString(Sequence seqVal, boolean allowStar) throws XPathException {
        Item item;
        SequenceIterator iterator = seqVal.iterate();
        StringBuilder stringVal = new StringBuilder();
        while ((item = iterator.next()) != null) {
            if (item instanceof QNameValue) {
                QNameValue qNameValue = (QNameValue)item;
                stringVal.append(" Q{").append(qNameValue.getComponent(AccessorFn.Component.NAMESPACE).getStringValue()).append('}').append(qNameValue.getComponent(AccessorFn.Component.LOCALNAME).getStringValue());
                continue;
            }
            if (allowStar && item instanceof StringValue && item.getStringValue().equals("*")) {
                stringVal.append(" *");
                continue;
            }
            throw new XPathException("Invalid serialization parameter value: expected sequence of QNames " + (allowStar ? "(or *) " : ""), "SEPM0017");
        }
        return stringVal.toString();
    }

    private String toSpaceSeparatedString(Sequence seqVal) throws XPathException {
        Item item;
        SequenceIterator iterator = seqVal.iterate();
        StringBuilder stringVal = new StringBuilder();
        while ((item = iterator.next()) != null) {
            stringVal.append(" ").append(item.getStringValue());
        }
        return stringVal.toString();
    }

    private String toMethodTypeString(Sequence seqVal) throws XPathException {
        String stringVal;
        if (seqVal.head() instanceof QNameValue) {
            QNameValue qNameValue = (QNameValue)seqVal.head();
            stringVal = '{' + qNameValue.getComponent(AccessorFn.Component.NAMESPACE).toString() + '}' + qNameValue.getComponent(AccessorFn.Component.LOCALNAME);
        } else {
            stringVal = seqVal.head().getStringValue();
        }
        return stringVal;
    }

    private static MapItem checkCharacterMapOptions(MapItem map, XPathContext context) throws XPathException {
        TypeHierarchy th = context.getConfiguration().getTypeHierarchy();
        for (KeyValuePair pair : map.keyValuePairs()) {
            AtomicValue key = pair.key;
            if (!(key instanceof StringValue)) {
                throw new XPathException("Keys in a character map must all be strings. Found a value of type " + key.getItemType(), "XPTY0004");
            }
            if (((StringValue)key).getStringLength() != 1) {
                throw new XPathException("Keys in a character map must all be one-character strings. Found " + Err.wrap(key.toString()), "SEPM0016");
            }
            if (SequenceType.SINGLE_STRING.matches(pair.value, th)) continue;
            throw new XPathException("Values in a character map must all be single strings. Found " + Err.wrap(key.toString()), "XPTY0004");
        }
        return map;
    }

    private CharacterMap toCharacterMap(Sequence seqVal, XPathContext context) throws XPathException {
        MapItem charMap = Serialize.checkCharacterMapOptions((MapItem)seqVal.head(), context);
        return Serialize.toCharacterMap(charMap);
    }

    public static CharacterMap toCharacterMap(MapItem charMap) throws XPathException {
        Item charKey;
        AtomicIterator<? extends AtomicValue> iterator = charMap.keys();
        IntHashMap<String> intHashMap = new IntHashMap<String>();
        while ((charKey = iterator.next()) != null) {
            String ch = ((AtomicValue)charKey).getStringValue();
            String str = charMap.get((AtomicValue)charKey).head().getStringValue();
            UnicodeString chValue = UnicodeString.makeUnicodeString(ch);
            if (chValue.uLength() != 1) {
                throw new XPathException("In the serialization parameter for the character map, each character to be mapped must be a single Unicode character", "SEPM0016");
            }
            int code = chValue.uCharAt(0);
            String prev = intHashMap.put(code, str);
            if (prev == null) continue;
            throw new XPathException("In the serialization parameters, the character map contains two entries for the character \\u" + Integer.toHexString(65536 + code).substring(1), "SEPM0018");
        }
        StructuredQName name = new StructuredQName("output", "http://www.w3.org/2010/xslt-xquery-serialization", "serialization-parameters");
        return new CharacterMap(name, intHashMap);
    }

    private SerializationProperties serializationParamsFromMap(Map<String, Sequence> map, XPathContext context) throws XPathException {
        Properties props = new Properties();
        CharacterMapIndex charMapIndex = new CharacterMapIndex();
        Sequence seqVal = map.get("allow-duplicate-names");
        if (seqVal != null) {
            props.setProperty("allow-duplicate-names", this.toYesNoTypeString(seqVal));
        }
        if ((seqVal = map.get("byte-order-mark")) != null) {
            props.setProperty("byte-order-mark", this.toYesNoTypeString(seqVal));
        }
        if ((seqVal = map.get("cdata-section-elements")) != null) {
            props.setProperty("cdata-section-elements", this.toQNamesTypeString(seqVal, false));
        }
        if ((seqVal = map.get("doctype-public")) != null) {
            props.setProperty("doctype-public", seqVal.head().getStringValue());
        }
        if ((seqVal = map.get("doctype-system")) != null) {
            props.setProperty("doctype-system", seqVal.head().getStringValue());
        }
        if ((seqVal = map.get("encoding")) != null) {
            props.setProperty("encoding", seqVal.head().getStringValue());
        }
        if ((seqVal = map.get("escape-uri-attributes")) != null) {
            props.setProperty("escape-uri-attributes", this.toYesNoTypeString(seqVal));
        }
        if ((seqVal = map.get("html-version")) != null) {
            props.setProperty("html-version", seqVal.head().getStringValue());
        }
        if ((seqVal = map.get("include-content-type")) != null) {
            props.setProperty("include-content-type", this.toYesNoTypeString(seqVal));
        }
        if ((seqVal = map.get("indent")) != null) {
            props.setProperty("indent", this.toYesNoTypeString(seqVal));
        }
        if ((seqVal = map.get("item-separator")) != null) {
            props.setProperty("item-separator", seqVal.head().getStringValue());
        }
        if ((seqVal = map.get("json-node-output-method")) != null) {
            props.setProperty("json-node-output-method", this.toMethodTypeString(seqVal));
        }
        if ((seqVal = map.get("media-type")) != null) {
            props.setProperty("media-type", seqVal.head().getStringValue());
        }
        if ((seqVal = map.get("method")) != null) {
            props.setProperty("method", this.toMethodTypeString(seqVal));
        }
        if ((seqVal = map.get("normalization-form")) != null) {
            props.setProperty("normalization-form", seqVal.head().getStringValue());
        }
        if ((seqVal = map.get("omit-xml-declaration")) != null) {
            props.setProperty("omit-xml-declaration", this.toYesNoTypeString(seqVal));
        }
        if ((seqVal = map.get("standalone")) != null) {
            props.setProperty("standalone", this.toYesNoOmitTypeString(seqVal));
        }
        if ((seqVal = map.get("suppress-indentation")) != null) {
            props.setProperty("suppress-indentation", this.toQNamesTypeString(seqVal, false));
        }
        if ((seqVal = map.get("undeclare-prefixes")) != null) {
            props.setProperty("undeclare-prefixes", this.toYesNoTypeString(seqVal));
        }
        if ((seqVal = map.get("use-character-maps")) != null) {
            CharacterMap characterMap = this.toCharacterMap(seqVal, context);
            charMapIndex.putCharacterMap(new StructuredQName("", "", "charMap"), characterMap);
            props.setProperty("use-character-maps", "charMap");
        }
        if ((seqVal = map.get("version")) != null) {
            props.setProperty("version", seqVal.head().getStringValue());
        }
        if ((seqVal = map.get(Serialize.sx("attribute-order"))) != null) {
            props.setProperty("{http://saxon.sf.net/}attribute-order", this.toQNamesTypeString(seqVal, true));
        }
        if ((seqVal = map.get(Serialize.sx("canonical"))) != null) {
            props.setProperty("{http://saxon.sf.net/}canonical", this.toYesNoTypeString(seqVal));
        }
        if ((seqVal = map.get(Serialize.sx("character-representation"))) != null) {
            props.setProperty("{http://saxon.sf.net/}character-representation", seqVal.head().getStringValue());
        }
        if ((seqVal = map.get(Serialize.sx("double-space"))) != null) {
            props.setProperty("{http://saxon.sf.net/}double-space", this.toQNamesTypeString(seqVal, false));
        }
        if ((seqVal = map.get(Serialize.sx("indent-spaces"))) != null) {
            props.setProperty("{http://saxon.sf.net/}indent-spaces", seqVal.head().getStringValue());
        }
        if ((seqVal = map.get(Serialize.sx("line-length"))) != null) {
            props.setProperty("{http://saxon.sf.net/}line-length", seqVal.head().getStringValue());
        }
        if ((seqVal = map.get(Serialize.sx("newline"))) != null) {
            props.setProperty("{http://saxon.sf.net/}newline", seqVal.head().getStringValue());
        }
        if ((seqVal = map.get(Serialize.sx("property-order"))) != null) {
            props.setProperty("{http://saxon.sf.net/}property-order", this.toSpaceSeparatedString(seqVal));
        }
        if ((seqVal = map.get(Serialize.sx("recognize-binary"))) != null) {
            props.setProperty("{http://saxon.sf.net/}recognize-binary", this.toYesNoTypeString(seqVal));
        }
        if ((seqVal = map.get(Serialize.sx("require-well-formed"))) != null) {
            props.setProperty("{http://saxon.sf.net/}require-well-formed", this.toYesNoTypeString(seqVal));
        }
        if ((seqVal = map.get(Serialize.sx("single-quotes"))) != null) {
            props.setProperty("{http://saxon.sf.net/}single-quotes", this.toYesNoTypeString(seqVal));
        }
        if ((seqVal = map.get(Serialize.sx("supply-source-locator"))) != null) {
            props.setProperty("{http://saxon.sf.net/}supply-source-locator", this.toYesNoTypeString(seqVal));
        }
        return new SerializationProperties(props, charMapIndex);
    }

    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        return this.evalSerialize(arguments[0].iterate(), arguments.length == 1 ? null : arguments[1].head(), context);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private StringValue evalSerialize(SequenceIterator iter, Item param, XPathContext context) throws XPathException {
        SerializationProperties params;
        if (param != null) {
            if (param instanceof NodeInfo) {
                NodeInfo paramNode = (NodeInfo)param;
                if (paramNode.getNodeKind() != 1 || !"http://www.w3.org/2010/xslt-xquery-serialization".equals(paramNode.getURI()) || !"serialization-parameters".equals(paramNode.getLocalPart())) {
                    throw new XPathException("Second argument to fn:serialize() must be an element named {http://www.w3.org/2010/xslt-xquery-serialization}serialization-parameters", "XPTY0004");
                }
                SerializationParamsHandler sph = new SerializationParamsHandler();
                sph.setSerializationParams(paramNode);
                params = sph.getSerializationProperties();
            } else {
                Item k;
                if (!(param instanceof MapItem)) throw new XPathException("Second argument to fn:serialize() must either be an element named {http://www.w3.org/2010/xslt-xquery-serialization}serialization-parameters, or a map (if using XPath 3.1)", "XPTY0004");
                MapItem paramMap = (MapItem)param;
                AtomicIterator<? extends AtomicValue> keyIter = ((MapItem)param).keys();
                while ((k = keyIter.next()) != null) {
                    if (!(k instanceof QNameValue)) continue;
                    String s = ((QNameValue)k).getStructuredQName().getEQName();
                    paramMap = paramMap.addEntry(new StringValue(s), paramMap.get((AtomicValue)k));
                }
                Map<String, Sequence> checkedOptions = this.getDetails().optionDetails.processSuppliedOptions(paramMap, context);
                params = this.serializationParamsFromMap(checkedOptions, context);
            }
        } else {
            params = new SerializationProperties(new Properties());
        }
        Properties props = params.getProperties();
        if (props.getProperty("method") == null) {
            props.setProperty("method", "xml");
        }
        if (props.getProperty("omit-xml-declaration") == null) {
            props.setProperty("omit-xml-declaration", "true");
        }
        try {
            StringWriter result = new StringWriter();
            SerializerFactory sf = context.getConfiguration().getSerializerFactory();
            PipelineConfiguration pipe = context.getConfiguration().makePipelineConfiguration();
            Receiver out = sf.getReceiver((Result)new StreamResult(result), params, pipe);
            SequenceCopier.copySequence(iter, out);
            return new StringValue(result.toString());
        } catch (XPathException e) {
            e.maybeSetErrorCode("SENR0001");
            throw e;
        }
    }

    static {
        requiredTypes.put("allow-duplicate-names", SequenceType.SINGLE_BOOLEAN);
        requiredTypes.put("byte-order-mark", SequenceType.SINGLE_BOOLEAN);
        requiredTypes.put("cdata-section-elements", BuiltInAtomicType.QNAME.zeroOrMore());
        requiredTypes.put("doctype-public", SequenceType.SINGLE_STRING);
        requiredTypes.put("doctype-system", SequenceType.SINGLE_STRING);
        requiredTypes.put("encoding", SequenceType.SINGLE_STRING);
        requiredTypes.put("escape-uri-attributes", SequenceType.SINGLE_BOOLEAN);
        requiredTypes.put("html-version", SequenceType.SINGLE_DECIMAL);
        requiredTypes.put("include-content-type", SequenceType.SINGLE_BOOLEAN);
        requiredTypes.put("indent", SequenceType.SINGLE_BOOLEAN);
        requiredTypes.put("item-separator", SequenceType.SINGLE_STRING);
        requiredTypes.put("json-node-output-method", SequenceType.SINGLE_STRING);
        requiredTypes.put("media-type", SequenceType.SINGLE_STRING);
        requiredTypes.put("method", SequenceType.SINGLE_STRING);
        requiredTypes.put("normalization-form", SequenceType.SINGLE_STRING);
        requiredTypes.put("omit-xml-declaration", SequenceType.SINGLE_BOOLEAN);
        requiredTypes.put("standalone", SequenceType.OPTIONAL_BOOLEAN);
        requiredTypes.put("suppress-indentation", BuiltInAtomicType.QNAME.zeroOrMore());
        requiredTypes.put("undeclare-prefixes", SequenceType.SINGLE_BOOLEAN);
        requiredTypes.put("use-character-maps", SequenceType.makeSequenceType(MapType.ANY_MAP_TYPE, 16384));
        requiredTypes.put("version", SequenceType.SINGLE_STRING);
        requiredTypesSaxon = new HashMap<String, SequenceType>(20);
        requiredTypesSaxon.put("attribute-order", BuiltInAtomicType.QNAME.zeroOrMore());
        requiredTypesSaxon.put("canonical", SequenceType.SINGLE_BOOLEAN);
        requiredTypesSaxon.put("character-representation", SequenceType.SINGLE_STRING);
        requiredTypesSaxon.put("double-space", BuiltInAtomicType.QNAME.zeroOrMore());
        requiredTypesSaxon.put("indent-spaces", SequenceType.SINGLE_INTEGER);
        requiredTypesSaxon.put("line-length", SequenceType.SINGLE_INTEGER);
        requiredTypesSaxon.put("newline", SequenceType.SINGLE_STRING);
        requiredTypesSaxon.put("recognize-binary", SequenceType.SINGLE_BOOLEAN);
        requiredTypesSaxon.put("require-well-formed", SequenceType.SINGLE_BOOLEAN);
        requiredTypesSaxon.put("single-quotes", SequenceType.SINGLE_BOOLEAN);
        requiredTypesSaxon.put("supply-source-locator", SequenceType.SINGLE_BOOLEAN);
        requiredTypesSaxon.put("suppress-indentation", BuiltInAtomicType.QNAME.zeroOrMore());
    }
}


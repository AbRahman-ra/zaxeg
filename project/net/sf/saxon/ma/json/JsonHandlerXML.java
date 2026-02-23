/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.json;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import javax.xml.transform.sax.SAXSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.lib.StandardEntityResolver;
import net.sf.saxon.ma.json.JsonHandler;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnySimpleType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Untyped;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class JsonHandlerXML
extends JsonHandler {
    private Outputter out;
    private Builder builder;
    private Stack<String> keys;
    private Stack<Boolean> inMap = new Stack();
    private boolean allowAnyTopLevel;
    public boolean validate;
    private boolean checkForDuplicates;
    private static final String SCHEMA_URI = "http://www.w3.org/2005/xpath-functions.xsd";
    private static final String JSON_NS = "http://www.w3.org/2005/xpath-functions";
    public static final String PREFIX = "";
    private NamePool namePool;
    private FingerprintedQName mapQN;
    private FingerprintedQName arrayQN;
    private FingerprintedQName stringQN;
    private FingerprintedQName numberQN;
    private FingerprintedQName booleanQN;
    private FingerprintedQName nullQN;
    private FingerprintedQName keyQN;
    private FingerprintedQName escapedQN;
    private FingerprintedQName escapedKeyQN;
    private static final Untyped UNTYPED = Untyped.getInstance();
    private static final AnySimpleType SIMPLE_TYPE = AnySimpleType.getInstance();
    private static final BuiltInAtomicType BOOLEAN_TYPE = BuiltInAtomicType.BOOLEAN;
    private static final BuiltInAtomicType STRING_TYPE = BuiltInAtomicType.STRING;
    public HashMap<String, SchemaType> types;
    private Stack<HashSet<String>> mapKeys = new Stack();

    private FingerprintedQName qname(String s) {
        FingerprintedQName fp = new FingerprintedQName(PREFIX, PREFIX, s);
        fp.obtainFingerprint(this.namePool);
        return fp;
    }

    private FingerprintedQName qnameNS(String s) {
        FingerprintedQName fp = new FingerprintedQName(PREFIX, JSON_NS, s);
        fp.obtainFingerprint(this.namePool);
        return fp;
    }

    public JsonHandlerXML(XPathContext context, String staticBaseUri, int flags) throws XPathException {
        this.init(context, flags);
        this.builder = context.getController().makeBuilder();
        this.builder.setSystemId(staticBaseUri);
        this.builder.setTiming(false);
        this.out = new ComplexContentOutputter(this.builder);
        this.out.open();
        this.out.startDocument(0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void init(XPathContext context, int flags) throws XPathException {
        this.keys = new Stack();
        this.setContext(context);
        this.charChecker = context.getConfiguration().getValidCharacterChecker();
        this.escape = (flags & 1) != 0;
        this.allowAnyTopLevel = (flags & 2) != 0;
        this.validate = (flags & 8) != 0;
        this.checkForDuplicates = this.validate || (flags & 0x20) == 0;
        this.types = new HashMap();
        this.namePool = context.getConfiguration().getNamePool();
        this.mapQN = this.qnameNS("map");
        this.arrayQN = this.qnameNS("array");
        this.stringQN = this.qnameNS("string");
        this.numberQN = this.qnameNS("number");
        this.booleanQN = this.qnameNS("boolean");
        this.nullQN = this.qnameNS("null");
        this.keyQN = this.qname("key");
        this.escapedQN = this.qname("escaped");
        this.escapedKeyQN = this.qname("escaped-key");
        if (this.validate) {
            try {
                String[] typeNames;
                Configuration config;
                Configuration configuration = config = context.getConfiguration();
                synchronized (configuration) {
                    config.checkLicensedFeature(1, "validation", -1);
                    if (!config.isSchemaAvailable(JSON_NS)) {
                        InputSource is = new StandardEntityResolver(config).resolveEntity(null, SCHEMA_URI);
                        if (config.isTiming()) {
                            config.getLogger().info("Loading a schema from resources for: http://www.w3.org/2005/xpath-functions");
                        }
                        config.addSchemaSource(new SAXSource(is));
                    }
                }
                for (String t : typeNames = new String[]{"mapType", "arrayType", "stringType", "numberType", "booleanType", "nullType", "mapWithinMapType", "arrayWithinMapType", "stringWithinMapType", "numberWithinMapType", "booleanWithinMapType", "nullWithinMapType"}) {
                    this.setType(t, config.getSchemaType(new StructuredQName(PREFIX, JSON_NS, t)));
                }
            } catch (SchemaException | SAXException e) {
                throw new XPathException(e);
            }
        }
    }

    public void setType(String name, SchemaType st) {
        this.types.put(name, st);
    }

    @Override
    public boolean setKey(String unEscaped, String reEscaped) {
        this.keys.push(unEscaped);
        return this.checkForDuplicates && !this.mapKeys.peek().add(reEscaped);
    }

    @Override
    public Item getResult() throws XPathException {
        this.out.endDocument();
        this.out.close();
        return this.builder.getCurrentRoot();
    }

    private boolean containsEscape(String literal) {
        return literal.indexOf(92) >= 0;
    }

    private boolean isInMap() {
        return !this.inMap.isEmpty() && this.inMap.peek() != false;
    }

    private void startElement(FingerprintedQName qn, String typeName) throws XPathException {
        this.startElement(qn, this.types.get(typeName));
    }

    private void startElement(FingerprintedQName qn, SchemaType st) throws XPathException {
        this.out.startElement(qn, this.validate && st != null ? st : UNTYPED, Loc.NONE, 0);
        if (this.isInMap()) {
            String k = this.keys.pop();
            k = this.reEscape(k);
            if (this.escape) {
                this.markAsEscaped(k, true);
            }
            this.out.attribute(this.keyQN, this.validate ? STRING_TYPE : SIMPLE_TYPE, k, Loc.NONE, 0);
        }
    }

    private void startContent() throws XPathException {
        this.out.startContent();
    }

    private void characters(String s) throws XPathException {
        this.out.characters(s, Loc.NONE, 0);
    }

    private void endElement() throws XPathException {
        this.out.endElement();
    }

    @Override
    public void startArray() throws XPathException {
        this.startElement(this.arrayQN, this.isInMap() ? "arrayWithinMapType" : "arrayType");
        this.inMap.push(false);
        this.startContent();
    }

    @Override
    public void endArray() throws XPathException {
        this.inMap.pop();
        this.endElement();
    }

    @Override
    public void startMap() throws XPathException {
        this.startElement(this.mapQN, this.isInMap() ? "mapWithinMapType" : "mapType");
        if (this.checkForDuplicates) {
            this.mapKeys.push(new HashSet());
        }
        this.inMap.push(true);
        this.startContent();
    }

    @Override
    public void endMap() throws XPathException {
        this.inMap.pop();
        if (this.checkForDuplicates) {
            this.mapKeys.pop();
        }
        this.endElement();
    }

    @Override
    public void writeNumeric(String asString, double asDouble) throws XPathException {
        this.startElement(this.numberQN, this.isInMap() ? "numberWithinMapType" : "numberType");
        this.startContent();
        this.characters(asString);
        this.endElement();
    }

    @Override
    public void writeString(String val) throws XPathException {
        this.startElement(this.stringQN, this.isInMap() ? "stringWithinMapType" : "stringType");
        String escaped = this.reEscape(val);
        if (this.escape) {
            this.markAsEscaped(escaped, false);
        }
        this.startContent();
        this.characters(escaped.toString());
        this.endElement();
    }

    @Override
    protected void markAsEscaped(CharSequence escaped, boolean isKey) throws XPathException {
        if (this.containsEscape(escaped.toString()) && this.escape) {
            FingerprintedQName name = isKey ? this.escapedKeyQN : this.escapedQN;
            this.out.attribute(name, this.validate ? BOOLEAN_TYPE : SIMPLE_TYPE, "true", Loc.NONE, 0);
        }
    }

    @Override
    public void writeBoolean(boolean value) throws XPathException {
        this.startElement(this.booleanQN, this.isInMap() ? "booleanWithinMapType" : "booleanType");
        this.startContent();
        this.characters(Boolean.toString(value));
        this.endElement();
    }

    @Override
    public void writeNull() throws XPathException {
        this.startElement(this.nullQN, this.isInMap() ? "nullWithinMapType" : "nullType");
        this.startContent();
        this.endElement();
    }
}


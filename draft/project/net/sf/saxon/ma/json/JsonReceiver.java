/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.json;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.function.IntPredicate;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.CharSequenceConsumer;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.StringToDouble11;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public class JsonReceiver
implements Receiver {
    private PipelineConfiguration pipe;
    private CharSequenceConsumer output;
    private FastStringBuffer textBuffer = new FastStringBuffer(128);
    private Stack<NodeName> stack = new Stack();
    private boolean atStart = true;
    private boolean indenting = false;
    private boolean escaped = false;
    private Stack<Set<String>> keyChecker = new Stack();
    private Function numberFormatter;
    private static final String ERR_INPUT = "FOJS0006";

    public JsonReceiver(PipelineConfiguration pipe, CharSequenceConsumer output) {
        Objects.requireNonNull(pipe);
        Objects.requireNonNull(output);
        this.setPipelineConfiguration(pipe);
        this.output = output;
    }

    @Override
    public void setPipelineConfiguration(PipelineConfiguration pipe) {
        this.pipe = pipe;
    }

    @Override
    public PipelineConfiguration getPipelineConfiguration() {
        return this.pipe;
    }

    @Override
    public void setSystemId(String systemId) {
    }

    public void setIndenting(boolean indenting) {
        this.indenting = indenting;
    }

    public boolean isIndenting() {
        return this.indenting;
    }

    public void setNumberFormatter(Function formatter) {
        assert (formatter.getArity() == 1);
        this.numberFormatter = formatter;
    }

    public Function getNumberFormatter() {
        return this.numberFormatter;
    }

    @Override
    public void open() throws XPathException {
        this.output.open();
    }

    @Override
    public void startDocument(int properties) throws XPathException {
    }

    @Override
    public void endDocument() throws XPathException {
    }

    @Override
    public void setUnparsedEntity(String name, String systemID, String publicID) throws XPathException {
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        String local;
        String parent = this.stack.empty() ? null : this.stack.peek().getLocalPart();
        boolean inMap = "map".equals(parent) || this.stack.isEmpty();
        this.stack.push(elemName);
        if (!elemName.hasURI("http://www.w3.org/2005/xpath-functions")) {
            throw new XPathException("xml-to-json: element found in wrong namespace: " + elemName.getStructuredQName().getEQName(), ERR_INPUT);
        }
        String key = null;
        String escapedAtt = null;
        String escapedKey = null;
        for (AttributeInfo att : attributes) {
            NodeName attName = att.getNodeName();
            if (attName.hasURI("")) {
                if (attName.getLocalPart().equals("key")) {
                    if (!inMap) {
                        throw new XPathException("xml-to-json: The key attribute is allowed only on elements within a map", ERR_INPUT);
                    }
                    key = att.getValue();
                    continue;
                }
                if (attName.getLocalPart().equals("escaped-key")) {
                    if (!inMap) {
                        throw new XPathException("xml-to-json: The escaped-key attribute is allowed only on elements within a map", ERR_INPUT);
                    }
                    escapedKey = att.getValue();
                    continue;
                }
                if (attName.getLocalPart().equals("escaped")) {
                    boolean allowed;
                    boolean bl = allowed = this.stack.size() == 1 || elemName.getLocalPart().equals("string");
                    if (!allowed) {
                        throw new XPathException("xml-to-json: The escaped attribute is allowed only on the <string> element", ERR_INPUT);
                    }
                    escapedAtt = att.getValue();
                    continue;
                }
                throw new XPathException("xml-to-json: Disallowed attribute in input: " + attName.getDisplayName(), ERR_INPUT);
            }
            if (!attName.hasURI("http://www.w3.org/2005/xpath-functions")) continue;
            throw new XPathException("xml-to-json: Disallowed attribute in input: " + attName.getDisplayName(), ERR_INPUT);
        }
        if (!this.atStart) {
            this.output.cat(",");
            if (this.indenting) {
                this.indent(this.stack.size());
            }
        }
        if (inMap && !this.keyChecker.isEmpty()) {
            if (key == null) {
                throw new XPathException("xml-to-json: Child elements of <map> must have a key attribute", ERR_INPUT);
            }
            boolean alreadyEscaped = false;
            if (escapedKey != null) {
                try {
                    alreadyEscaped = StringConverter.StringToBoolean.INSTANCE.convertString(escapedKey).asAtomic().effectiveBooleanValue();
                } catch (XPathException e) {
                    throw new XPathException("xml-to-json: Value of escaped-key attribute '" + Err.wrap(escapedKey) + "' is not a valid xs:boolean", ERR_INPUT);
                }
            }
            key = (alreadyEscaped ? JsonReceiver.handleEscapedString(key) : JsonReceiver.escape(key, false, new ControlChar())).toString();
            String normalizedKey = alreadyEscaped ? JsonReceiver.unescape(key) : key;
            boolean added = this.keyChecker.peek().add(normalizedKey);
            if (!added) {
                throw new XPathException("xml-to-json: duplicate key value " + Err.wrap(key), ERR_INPUT);
            }
            this.output.cat("\"").cat(key).cat("\"").cat(this.indenting ? " : " : ":");
        }
        switch (local = elemName.getLocalPart()) {
            case "array": {
                if (this.indenting) {
                    this.indent(this.stack.size());
                    this.output.cat("[ ");
                } else {
                    this.output.cat("[");
                }
                this.atStart = true;
                break;
            }
            case "map": {
                if (this.indenting) {
                    this.indent(this.stack.size());
                    this.output.cat("{ ");
                } else {
                    this.output.cat("{");
                }
                this.atStart = true;
                this.keyChecker.push(new HashSet());
                break;
            }
            case "null": {
                this.checkParent(local, parent);
                this.output.cat("null");
                this.atStart = false;
                break;
            }
            case "string": {
                if (escapedAtt != null) {
                    try {
                        this.escaped = StringConverter.StringToBoolean.INSTANCE.convertString(escapedAtt).asAtomic().effectiveBooleanValue();
                    } catch (XPathException e) {
                        throw new XPathException("xml-to-json: value of escaped attribute (" + this.escaped + ") is not a valid xs:boolean", ERR_INPUT);
                    }
                }
                this.checkParent(local, parent);
                this.atStart = false;
                break;
            }
            case "boolean": 
            case "number": {
                this.checkParent(local, parent);
                this.atStart = false;
                break;
            }
            default: {
                throw new XPathException("xml-to-json: unknown element <" + local + ">", ERR_INPUT);
            }
        }
        this.textBuffer.setLength(0);
    }

    private void checkParent(String child, String parent) throws XPathException {
        if ("null".equals(parent) || "string".equals(parent) || "number".equals(parent) || "boolean".equals(parent)) {
            throw new XPathException("xml-to-json: A " + Err.wrap(child, 1) + " element cannot appear as a child of " + Err.wrap(parent, 1), ERR_INPUT);
        }
    }

    @Override
    public void endElement() throws XPathException {
        NodeName name = this.stack.pop();
        String local = name.getLocalPart();
        if (local.equals("boolean")) {
            try {
                boolean b = StringConverter.StringToBoolean.INSTANCE.convertString(this.textBuffer).asAtomic().effectiveBooleanValue();
                this.output.cat(b ? "true" : "false");
            } catch (XPathException e) {
                throw new XPathException("xml-to-json: Value of <boolean> element is not a valid xs:boolean", ERR_INPUT);
            }
        } else if (local.equals("number")) {
            if (this.numberFormatter == null) {
                try {
                    double d = StringToDouble11.getInstance().stringToNumber(this.textBuffer);
                    if (Double.isNaN(d) || Double.isInfinite(d)) {
                        throw new XPathException("xml-to-json: Infinity and NaN are not allowed", ERR_INPUT);
                    }
                    this.output.cat(new DoubleValue(d).getStringValueCS());
                } catch (NumberFormatException e) {
                    throw new XPathException("xml-to-json: Invalid number: " + this.textBuffer, ERR_INPUT);
                }
            } else {
                Sequence result = SystemFunction.dynamicCall(this.numberFormatter, this.pipe.getXPathContext(), new Sequence[]{new StringValue(this.textBuffer)});
                this.output.cat(((StringValue)result).getStringValueCS());
            }
        } else if (local.equals("string")) {
            this.output.cat("\"");
            String str = this.textBuffer.toString();
            if (this.escaped) {
                this.output.cat(JsonReceiver.handleEscapedString(str));
            } else {
                this.output.cat(JsonReceiver.escape(str, false, new ControlChar()));
            }
            this.output.cat("\"");
        } else if (!Whitespace.isWhite(this.textBuffer)) {
            throw new XPathException("xml-to-json: Element " + name.getDisplayName() + " must have no text content", ERR_INPUT);
        }
        this.textBuffer.setLength(0);
        this.escaped = false;
        if (local.equals("array")) {
            this.output.cat(this.indenting ? " ]" : "]");
        } else if (local.equals("map")) {
            this.keyChecker.pop();
            this.output.cat(this.indenting ? " }" : "}");
        }
        this.atStart = false;
    }

    private static CharSequence handleEscapedString(String str) throws XPathException {
        JsonReceiver.unescape(str);
        FastStringBuffer out = new FastStringBuffer(str.length() * 2);
        boolean afterEscapeChar = false;
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            if (c == '\"' && !afterEscapeChar) {
                out.append("\\\"");
            } else if (c < ' ' || c >= '\u007f' && c < '\u00a0') {
                if (c == '\b') {
                    out.append("\\b");
                } else if (c == '\f') {
                    out.append("\\f");
                } else if (c == '\n') {
                    out.append("\\n");
                } else if (c == '\r') {
                    out.append("\\r");
                } else if (c == '\t') {
                    out.append("\\t");
                } else {
                    out.append("\\u");
                    String hex = Integer.toHexString(c).toUpperCase();
                    while (hex.length() < 4) {
                        hex = "0" + hex;
                    }
                    out.append(hex);
                }
            } else if (c == '/' && !afterEscapeChar) {
                out.append("\\/");
            } else {
                out.cat(c);
            }
            afterEscapeChar = c == '\\' && !afterEscapeChar;
        }
        return out;
    }

    public static CharSequence escape(CharSequence in, boolean forXml, IntPredicate hexEscapes) throws XPathException {
        FastStringBuffer out = new FastStringBuffer(in.length());
        block10: for (int i = 0; i < in.length(); ++i) {
            char c = in.charAt(i);
            switch (c) {
                case '\"': {
                    out.append(forXml ? "\"" : "\\\"");
                    continue block10;
                }
                case '\b': {
                    out.append("\\b");
                    continue block10;
                }
                case '\f': {
                    out.append("\\f");
                    continue block10;
                }
                case '\n': {
                    out.append("\\n");
                    continue block10;
                }
                case '\r': {
                    out.append("\\r");
                    continue block10;
                }
                case '\t': {
                    out.append("\\t");
                    continue block10;
                }
                case '/': {
                    out.append(forXml ? "/" : "\\/");
                    continue block10;
                }
                case '\\': {
                    out.append("\\\\");
                    continue block10;
                }
                default: {
                    if (hexEscapes.test(c)) {
                        out.append("\\u");
                        String hex = Integer.toHexString(c).toUpperCase();
                        while (hex.length() < 4) {
                            hex = "0" + hex;
                        }
                        out.append(hex);
                        continue block10;
                    }
                    out.cat(c);
                }
            }
        }
        return out;
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        this.textBuffer.cat(chars);
    }

    @Override
    public void processingInstruction(String name, CharSequence data, Location locationId, int properties) throws XPathException {
    }

    @Override
    public void comment(CharSequence content, Location locationId, int properties) throws XPathException {
    }

    @Override
    public void close() throws XPathException {
        if (this.output != null) {
            this.output.close();
            this.output = null;
        }
    }

    @Override
    public boolean usesTypeAnnotations() {
        return false;
    }

    @Override
    public String getSystemId() {
        return null;
    }

    private void indent(int depth) throws XPathException {
        this.output.cat("\n");
        for (int i = 0; i < depth; ++i) {
            this.output.cat("  ");
        }
    }

    private static String unescape(String literal) throws XPathException {
        if (literal.indexOf(92) < 0) {
            return literal;
        }
        FastStringBuffer buffer = new FastStringBuffer(literal.length());
        for (int i = 0; i < literal.length(); ++i) {
            char c = literal.charAt(i);
            if (c == '\\') {
                if (i++ == literal.length() - 1) {
                    throw new XPathException("String '" + Err.wrap(literal) + "' ends in backslash ", "FOJS0007");
                }
                switch (literal.charAt(i)) {
                    case '\"': {
                        buffer.cat('\"');
                        break;
                    }
                    case '\\': {
                        buffer.cat('\\');
                        break;
                    }
                    case '/': {
                        buffer.cat('/');
                        break;
                    }
                    case 'b': {
                        buffer.cat('\b');
                        break;
                    }
                    case 'f': {
                        buffer.cat('\f');
                        break;
                    }
                    case 'n': {
                        buffer.cat('\n');
                        break;
                    }
                    case 'r': {
                        buffer.cat('\r');
                        break;
                    }
                    case 't': {
                        buffer.cat('\t');
                        break;
                    }
                    case 'u': {
                        try {
                            String hex = literal.substring(i + 1, i + 5);
                            int code = Integer.parseInt(hex, 16);
                            buffer.cat((char)code);
                            i += 4;
                            break;
                        } catch (Exception e) {
                            throw new XPathException("Invalid hex escape sequence in string '" + Err.wrap(literal) + "'", "FOJS0007");
                        }
                    }
                    default: {
                        char next = literal.charAt(i);
                        String xx = next < '\u0100' ? next + "" : "x" + Integer.toHexString(next);
                        throw new XPathException("Unknown escape sequence \\" + xx, "FOJS0007");
                    }
                }
                continue;
            }
            buffer.cat(c);
        }
        return buffer.toString();
    }

    private static class ControlChar
    implements IntPredicate {
        private ControlChar() {
        }

        @Override
        public boolean test(int c) {
            return c < 31 || c >= 127 && c <= 159;
        }
    }
}


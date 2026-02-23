/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.json;

import java.util.Map;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.ma.json.JsonHandler;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.StringToDouble;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.StringValue;

public class JsonParser {
    public static final int ESCAPE = 1;
    public static final int ALLOW_ANY_TOP_LEVEL = 2;
    public static final int LIBERAL = 4;
    public static final int VALIDATE = 8;
    public static final int DEBUG = 16;
    public static final int DUPLICATES_RETAINED = 32;
    public static final int DUPLICATES_LAST = 64;
    public static final int DUPLICATES_FIRST = 128;
    public static final int DUPLICATES_REJECTED = 256;
    public static final int DUPLICATES_SPECIFIED = 480;
    private static final String ERR_GRAMMAR = "FOJS0001";
    private static final String ERR_DUPLICATE = "FOJS0003";
    private static final String ERR_SCHEMA = "FOJS0004";
    private static final String ERR_OPTIONS = "FOJS0005";
    private static final String ERR_LIMITS = "FOJS0001";

    public void parse(String input, int flags, JsonHandler handler, XPathContext context) throws XPathException {
        if (input.isEmpty()) {
            this.invalidJSON("An empty string is not valid JSON", "FOJS0001", 1);
        }
        JsonTokenizer t = new JsonTokenizer(input);
        t.next();
        this.parseConstruct(handler, t, flags, context);
        if (t.next() != JsonToken.EOF) {
            this.invalidJSON("Unexpected token beyond end of JSON input", "FOJS0001", t.lineNumber);
        }
    }

    public static int getFlags(Map<String, Sequence> options, XPathContext context, Boolean allowValidate) throws XPathException {
        boolean escape;
        int flags = 0;
        BooleanValue debug = (BooleanValue)options.get("debug");
        if (debug != null && debug.getBooleanValue()) {
            flags |= 0x10;
        }
        if (escape = ((BooleanValue)options.get("escape")).getBooleanValue()) {
            flags |= 1;
            if (options.get("fallback") != null) {
                throw new XPathException("Cannot specify a fallback function when escape=true", ERR_OPTIONS);
            }
        }
        if (((BooleanValue)options.get("liberal")).getBooleanValue()) {
            flags |= 4;
            flags |= 2;
        }
        boolean validate = false;
        if (allowValidate.booleanValue() && (validate = ((BooleanValue)options.get("validate")).getBooleanValue())) {
            if (!context.getController().getExecutable().isSchemaAware()) {
                JsonParser.error("Requiring validation on non-schema-aware processor", ERR_SCHEMA);
            }
            flags |= 8;
        }
        if (options.containsKey("duplicates")) {
            String duplicates;
            switch (duplicates = ((StringValue)options.get("duplicates")).getStringValue()) {
                case "reject": {
                    flags |= 0x100;
                    break;
                }
                case "use-last": {
                    flags |= 0x40;
                    break;
                }
                case "use-first": {
                    flags |= 0x80;
                    break;
                }
                case "retain": {
                    flags |= 0x20;
                    break;
                }
                default: {
                    JsonParser.error("Invalid value for 'duplicates' option", ERR_OPTIONS);
                }
            }
            if (validate && "retain".equals(duplicates)) {
                JsonParser.error("The options validate:true and duplicates:retain cannot be used together", ERR_OPTIONS);
            }
        }
        return flags;
    }

    private void parseConstruct(JsonHandler handler, JsonTokenizer tokenizer, int flags, XPathContext context) throws XPathException {
        boolean debug;
        boolean bl = debug = (flags & 0x10) != 0;
        if (debug) {
            System.err.println("token:" + (Object)((Object)tokenizer.currentToken) + " :" + tokenizer.currentTokenValue);
        }
        switch (tokenizer.currentToken) {
            case LCURLY: {
                this.parseObject(handler, tokenizer, flags, context);
                break;
            }
            case LSQB: {
                this.parseArray(handler, tokenizer, flags, context);
                break;
            }
            case NUMERIC_LITERAL: {
                String lexical = tokenizer.currentTokenValue.toString();
                double d = this.parseNumericLiteral(lexical, flags, tokenizer.lineNumber);
                handler.writeNumeric(lexical, d);
                break;
            }
            case TRUE: {
                handler.writeBoolean(true);
                break;
            }
            case FALSE: {
                handler.writeBoolean(false);
                break;
            }
            case NULL: {
                handler.writeNull();
                break;
            }
            case STRING_LITERAL: {
                String literal = tokenizer.currentTokenValue.toString();
                handler.writeString(JsonParser.unescape(literal, flags, "FOJS0001", tokenizer.lineNumber));
                break;
            }
            default: {
                this.invalidJSON("Unexpected symbol: " + tokenizer.currentTokenValue, "FOJS0001", tokenizer.lineNumber);
            }
        }
    }

    private void parseObject(JsonHandler handler, JsonTokenizer tokenizer, int flags, XPathContext context) throws XPathException {
        boolean liberal = (flags & 4) != 0;
        handler.startMap();
        JsonToken tok = tokenizer.next();
        while (tok != JsonToken.RCURLY) {
            if (!(tok == JsonToken.STRING_LITERAL || tok == JsonToken.UNQUOTED_STRING && liberal)) {
                this.invalidJSON("Property name must be a string literal", "FOJS0001", tokenizer.lineNumber);
            }
            String key = tokenizer.currentTokenValue.toString();
            key = JsonParser.unescape(key, flags, "FOJS0001", tokenizer.lineNumber);
            String reEscaped = handler.reEscape(key);
            tok = tokenizer.next();
            if (tok != JsonToken.COLON) {
                this.invalidJSON("Missing colon after \"" + Err.wrap(key) + "\"", "FOJS0001", tokenizer.lineNumber);
            }
            tokenizer.next();
            boolean duplicate = handler.setKey(key, reEscaped);
            if (duplicate && (flags & 0x100) != 0) {
                this.invalidJSON("Duplicate key value \"" + Err.wrap(key) + "\"", ERR_DUPLICATE, tokenizer.lineNumber);
            }
            try {
                if (!duplicate || (flags & 0x60) != 0) {
                    this.parseConstruct(handler, tokenizer, flags, context);
                } else {
                    JsonHandler h2 = new JsonHandler();
                    h2.setContext(context);
                    this.parseConstruct(h2, tokenizer, flags, context);
                }
            } catch (StackOverflowError e) {
                this.invalidJSON("Objects are too deeply nested", "FOJS0001", tokenizer.lineNumber);
            }
            tok = tokenizer.next();
            if (tok == JsonToken.COMMA) {
                tok = tokenizer.next();
                if (tok != JsonToken.RCURLY) continue;
                if (liberal) break;
                this.invalidJSON("Trailing comma after entry in object", "FOJS0001", tokenizer.lineNumber);
                continue;
            }
            if (tok == JsonToken.RCURLY) break;
            this.invalidJSON("Unexpected token after value of \"" + Err.wrap(key) + "\" property", "FOJS0001", tokenizer.lineNumber);
        }
        handler.endMap();
    }

    private void parseArray(JsonHandler handler, JsonTokenizer tokenizer, int flags, XPathContext context) throws XPathException {
        boolean liberal = (flags & 4) != 0;
        handler.startArray();
        JsonToken tok = tokenizer.next();
        if (tok == JsonToken.RSQB) {
            handler.endArray();
            return;
        }
        while (true) {
            try {
                this.parseConstruct(handler, tokenizer, flags, context);
            } catch (StackOverflowError e) {
                this.invalidJSON("Arrays are too deeply nested", "FOJS0001", tokenizer.lineNumber);
            }
            tok = tokenizer.next();
            if (tok == JsonToken.COMMA) {
                tok = tokenizer.next();
                if (tok != JsonToken.RSQB) continue;
                if (liberal) break;
                this.invalidJSON("Trailing comma after entry in array", "FOJS0001", tokenizer.lineNumber);
                continue;
            }
            if (tok == JsonToken.RSQB) break;
            this.invalidJSON("Unexpected token (" + JsonParser.toString(tok, tokenizer.currentTokenValue.toString()) + ") after entry in array", "FOJS0001", tokenizer.lineNumber);
        }
        handler.endArray();
    }

    private double parseNumericLiteral(String token, int flags, int lineNumber) throws XPathException {
        try {
            if ((flags & 4) == 0) {
                if (token.startsWith("+")) {
                    this.invalidJSON("Leading + sign not allowed: " + token, "FOJS0001", lineNumber);
                } else {
                    String t = token;
                    if (t.startsWith("-")) {
                        t = t.substring(1);
                    }
                    if (!(!t.startsWith("0") || t.equals("0") || t.startsWith("0.") || t.startsWith("0e") || t.startsWith("0E"))) {
                        this.invalidJSON("Redundant leading zeroes not allowed: " + token, "FOJS0001", lineNumber);
                    }
                    if (t.endsWith(".") || t.contains(".e") || t.contains(".E")) {
                        this.invalidJSON("Empty fractional part not allowed", "FOJS0001", lineNumber);
                    }
                    if (t.startsWith(".")) {
                        this.invalidJSON("Empty integer part not allowed", "FOJS0001", lineNumber);
                    }
                }
            }
            return StringToDouble.getInstance().stringToNumber(token);
        } catch (NumberFormatException e) {
            this.invalidJSON("Invalid numeric literal: " + e.getMessage(), "FOJS0001", lineNumber);
            return Double.NaN;
        }
    }

    public static String unescape(String literal, int flags, String errorCode, int lineNumber) throws XPathException {
        if (literal.indexOf(92) < 0) {
            return literal;
        }
        boolean liberal = (flags & 4) != 0;
        FastStringBuffer buffer = new FastStringBuffer(literal.length());
        block13: for (int i = 0; i < literal.length(); ++i) {
            char c = literal.charAt(i);
            if (c == '\\') {
                if (i++ == literal.length() - 1) {
                    throw new XPathException("Invalid JSON escape: String " + Err.wrap(literal) + " ends in backslash", errorCode);
                }
                switch (literal.charAt(i)) {
                    case '\"': {
                        buffer.cat('\"');
                        continue block13;
                    }
                    case '\\': {
                        buffer.cat('\\');
                        continue block13;
                    }
                    case '/': {
                        buffer.cat('/');
                        continue block13;
                    }
                    case 'b': {
                        buffer.cat('\b');
                        continue block13;
                    }
                    case 'f': {
                        buffer.cat('\f');
                        continue block13;
                    }
                    case 'n': {
                        buffer.cat('\n');
                        continue block13;
                    }
                    case 'r': {
                        buffer.cat('\r');
                        continue block13;
                    }
                    case 't': {
                        buffer.cat('\t');
                        continue block13;
                    }
                    case 'u': {
                        try {
                            String hex = literal.substring(i + 1, i + 5);
                            int code = Integer.parseInt(hex, 16);
                            buffer.cat((char)code);
                            i += 4;
                            continue block13;
                        } catch (Exception e) {
                            if (liberal) {
                                buffer.append("\\u");
                                continue block13;
                            }
                            throw new XPathException("Invalid JSON escape: \\u must be followed by four hex characters", errorCode);
                        }
                    }
                    default: {
                        if (liberal) {
                            buffer.cat(literal.charAt(i));
                            continue block13;
                        }
                        char next = literal.charAt(i);
                        String xx = next < '\u0100' ? next + "" : "x" + Integer.toHexString(next);
                        throw new XPathException("Unknown escape sequence \\" + xx, errorCode);
                    }
                }
            }
            buffer.cat(c);
        }
        return buffer.toString();
    }

    private static void error(String message, String code) throws XPathException {
        throw new XPathException(message, code);
    }

    private void invalidJSON(String message, String code, int lineNumber) throws XPathException {
        JsonParser.error("Invalid JSON input on line " + lineNumber + ": " + message, code);
    }

    public static String toString(JsonToken token, String currentTokenValue) {
        switch (token) {
            case LSQB: {
                return "[";
            }
            case RSQB: {
                return "]";
            }
            case LCURLY: {
                return "{";
            }
            case RCURLY: {
                return "}";
            }
            case STRING_LITERAL: {
                return "string (\"" + currentTokenValue + "\")";
            }
            case NUMERIC_LITERAL: {
                return "number (" + currentTokenValue + ")";
            }
            case TRUE: {
                return "true";
            }
            case FALSE: {
                return "false";
            }
            case NULL: {
                return "null";
            }
            case COLON: {
                return ":";
            }
            case COMMA: {
                return ",";
            }
            case EOF: {
                return "<eof>";
            }
        }
        return "<" + (Object)((Object)token) + ">";
    }

    private class JsonTokenizer {
        private String input;
        private int position;
        private int lineNumber = 1;
        public JsonToken currentToken;
        public FastStringBuffer currentTokenValue = new FastStringBuffer(64);

        JsonTokenizer(String input) {
            this.input = input;
            this.position = 0;
            if (!input.isEmpty() && input.charAt(0) == '\ufeff') {
                ++this.position;
            }
        }

        public JsonToken next() throws XPathException {
            this.currentToken = this.readToken();
            return this.currentToken;
        }

        private JsonToken readToken() throws XPathException {
            if (this.position >= this.input.length()) {
                return JsonToken.EOF;
            }
            block26: while (true) {
                char c = this.input.charAt(this.position);
                switch (c) {
                    case '\n': 
                    case '\r': {
                        ++this.lineNumber;
                    }
                    case '\t': 
                    case ' ': {
                        if (++this.position < this.input.length()) continue block26;
                        return JsonToken.EOF;
                    }
                }
                break;
            }
            char ch = this.input.charAt(this.position++);
            switch (ch) {
                case '[': {
                    return JsonToken.LSQB;
                }
                case '{': {
                    return JsonToken.LCURLY;
                }
                case ']': {
                    return JsonToken.RSQB;
                }
                case '}': {
                    return JsonToken.RCURLY;
                }
                case '\"': {
                    this.currentTokenValue.setLength(0);
                    boolean afterBackslash = false;
                    while (true) {
                        char c;
                        if (this.position >= this.input.length()) {
                            JsonParser.this.invalidJSON("Unclosed quotes in string literal", "FOJS0001", this.lineNumber);
                        }
                        if ((c = this.input.charAt(this.position++)) < ' ') {
                            JsonParser.this.invalidJSON("Unescaped control character (x" + Integer.toHexString(c) + ")", "FOJS0001", this.lineNumber);
                        }
                        if (afterBackslash && c == 'u') {
                            try {
                                String hex = this.input.substring(this.position, this.position + 4);
                                Integer.parseInt(hex, 16);
                            } catch (Exception e) {
                                JsonParser.this.invalidJSON("\\u must be followed by four hex characters", "FOJS0001", this.lineNumber);
                            }
                        }
                        if (c == '\"' && !afterBackslash) break;
                        this.currentTokenValue.cat(c);
                        afterBackslash = c == '\\' && !afterBackslash;
                    }
                    return JsonToken.STRING_LITERAL;
                }
                case ':': {
                    return JsonToken.COLON;
                }
                case ',': {
                    return JsonToken.COMMA;
                }
                case '+': 
                case '-': 
                case '.': 
                case '0': 
                case '1': 
                case '2': 
                case '3': 
                case '4': 
                case '5': 
                case '6': 
                case '7': 
                case '8': 
                case '9': {
                    this.currentTokenValue.setLength(0);
                    this.currentTokenValue.cat(ch);
                    if (this.position < this.input.length()) {
                        char c;
                        while ((c = this.input.charAt(this.position)) >= '0' && c <= '9' || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') {
                            this.currentTokenValue.cat(c);
                            if (++this.position < this.input.length()) continue;
                            break;
                        }
                    }
                    return JsonToken.NUMERIC_LITERAL;
                }
            }
            if (NameChecker.isNCNameChar(ch)) {
                String val;
                char c;
                this.currentTokenValue.setLength(0);
                this.currentTokenValue.cat(ch);
                while (this.position < this.input.length() && NameChecker.isNCNameChar(c = this.input.charAt(this.position))) {
                    this.currentTokenValue.cat(c);
                    ++this.position;
                }
                switch (val = this.currentTokenValue.toString()) {
                    case "true": {
                        return JsonToken.TRUE;
                    }
                    case "false": {
                        return JsonToken.FALSE;
                    }
                    case "null": {
                        return JsonToken.NULL;
                    }
                }
                return JsonToken.UNQUOTED_STRING;
            }
            char c = this.input.charAt(--this.position);
            JsonParser.this.invalidJSON("Unexpected character '" + c + "' (\\u" + Integer.toHexString(c) + ") at position " + this.position, "FOJS0001", this.lineNumber);
            return JsonToken.EOF;
        }
    }

    private static enum JsonToken {
        LSQB,
        RSQB,
        LCURLY,
        RCURLY,
        STRING_LITERAL,
        NUMERIC_LITERAL,
        TRUE,
        FALSE,
        NULL,
        COLON,
        COMMA,
        UNQUOTED_STRING,
        EOF;

    }
}


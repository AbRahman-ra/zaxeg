/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.parser;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Whitespace;

public final class Tokenizer {
    private int state = 0;
    public static final int DEFAULT_STATE = 0;
    public static final int BARE_NAME_STATE = 1;
    public static final int SEQUENCE_TYPE_STATE = 2;
    public static final int OPERATOR_STATE = 3;
    public int currentToken = 0;
    public String currentTokenValue = null;
    public int currentTokenStartOffset = 0;
    private int nextToken = 0;
    private String nextTokenValue = null;
    private int nextTokenStartOffset = 0;
    public String input;
    public int inputOffset = 0;
    private int inputLength;
    private int lineNumber = 1;
    private int nextLineNumber = 1;
    private List<Integer> newlineOffsets = null;
    private int precedingToken = -1;
    private String precedingTokenValue = "";
    public boolean disallowUnionKeyword;
    public boolean isXQuery = false;
    public int languageLevel = 20;
    public boolean allowSaxonExtensions = false;

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
        if (state == 0) {
            this.precedingToken = -1;
            this.precedingTokenValue = "";
            this.currentToken = -1;
        } else if (state == 3) {
            this.precedingToken = 204;
            this.precedingTokenValue = ")";
            this.currentToken = 204;
        }
    }

    public void tokenize(String input, int start, int end) throws XPathException {
        this.nextToken = 0;
        this.nextTokenValue = null;
        this.nextTokenStartOffset = 0;
        this.inputOffset = start;
        this.input = input;
        this.lineNumber = 0;
        this.nextLineNumber = 0;
        this.inputLength = end == -1 ? input.length() : end;
        this.lookAhead();
        this.next();
    }

    public void next() throws XPathException {
        this.precedingToken = this.currentToken;
        this.precedingTokenValue = this.currentTokenValue;
        this.currentToken = this.nextToken;
        this.currentTokenValue = this.nextTokenValue;
        if (this.currentTokenValue == null) {
            this.currentTokenValue = "";
        }
        this.currentTokenStartOffset = this.nextTokenStartOffset;
        this.lineNumber = this.nextLineNumber;
        switch (this.currentToken) {
            case 201: {
                int optype = this.getBinaryOp(this.currentTokenValue);
                if (optype == -1 || this.followsOperator(this.precedingToken)) break;
                this.currentToken = optype;
                break;
            }
            case 12: {
                if (!this.isXQuery || !this.followsOperator(this.precedingToken)) break;
                this.currentToken = 217;
                break;
            }
            case 207: {
                if (this.followsOperator(this.precedingToken)) break;
                this.currentToken = 17;
            }
        }
        if (this.currentToken == 217 || this.currentToken == 215) {
            return;
        }
        int oldPrecedingToken = this.precedingToken;
        this.lookAhead();
        if (this.currentToken == 201) {
            if (this.state == 1) {
                return;
            }
            if (oldPrecedingToken == 21) {
                return;
            }
            switch (this.nextToken) {
                case 5: {
                    int op = this.getBinaryOp(this.currentTokenValue);
                    if (op == -1 || this.followsOperator(oldPrecedingToken)) {
                        this.currentToken = this.getFunctionType(this.currentTokenValue);
                        this.lookAhead();
                        break;
                    }
                    this.currentToken = op;
                    break;
                }
                case 59: {
                    if (this.state == 2) break;
                    this.currentToken = 60;
                    this.lookAhead();
                    break;
                }
                case 41: {
                    this.lookAhead();
                    this.currentToken = 36;
                    break;
                }
                case 44: {
                    this.lookAhead();
                    this.currentToken = 43;
                    break;
                }
                case 42: {
                    this.lookAhead();
                    this.currentToken = 208;
                    break;
                }
                case 21: {
                    switch (this.currentTokenValue) {
                        case "for": {
                            this.currentToken = 211;
                            break;
                        }
                        case "some": {
                            this.currentToken = 32;
                            break;
                        }
                        case "every": {
                            this.currentToken = 33;
                            break;
                        }
                        case "let": {
                            this.currentToken = 216;
                            break;
                        }
                        case "count": {
                            this.currentToken = 220;
                            break;
                        }
                        case "copy": {
                            this.currentToken = 219;
                        }
                    }
                    break;
                }
                case 106: {
                    if (!this.currentTokenValue.equals("declare")) break;
                    this.currentToken = 123;
                    break;
                }
                case 201: {
                    String composite;
                    Integer val;
                    int candidate = -1;
                    switch (this.currentTokenValue) {
                        case "element": {
                            candidate = 61;
                            break;
                        }
                        case "attribute": {
                            candidate = 62;
                            break;
                        }
                        case "processing-instruction": {
                            candidate = 63;
                            break;
                        }
                        case "namespace": {
                            candidate = 64;
                        }
                    }
                    if (candidate != -1) {
                        String qname = this.nextTokenValue;
                        String saveTokenValue = this.currentTokenValue;
                        int savePosition = this.inputOffset;
                        this.lookAhead();
                        if (this.nextToken == 59) {
                            this.currentToken = candidate;
                            this.currentTokenValue = qname;
                            this.lookAhead();
                            return;
                        }
                        this.currentToken = 201;
                        this.currentTokenValue = saveTokenValue;
                        this.inputOffset = savePosition;
                        this.nextToken = 201;
                        this.nextTokenValue = qname;
                    }
                    if ((val = Token.doubleKeywords.get(composite = this.currentTokenValue + ' ' + this.nextTokenValue)) == null) break;
                    this.currentToken = val;
                    this.currentTokenValue = composite;
                    if (this.currentToken == 114) {
                        this.lookAhead();
                        if (this.nextToken != 201 || !this.nextTokenValue.equals("of")) {
                            throw new XPathException("After '" + composite + "', expected 'of'");
                        }
                        this.lookAhead();
                        if (this.nextToken != 201 || !this.nextTokenValue.equals("node")) {
                            throw new XPathException("After 'replace value of', expected 'node'");
                        }
                        this.nextToken = this.currentToken;
                    }
                    this.lookAhead();
                    return;
                }
            }
        }
    }

    int peekAhead() {
        return this.nextToken;
    }

    public void treatCurrentAsOperator() {
        switch (this.currentToken) {
            case 201: {
                int optype = this.getBinaryOp(this.currentTokenValue);
                if (optype == -1) break;
                this.currentToken = optype;
                break;
            }
            case 207: {
                this.currentToken = 17;
            }
        }
    }

    public void lookAhead() throws XPathException {
        char c;
        this.precedingToken = this.nextToken;
        this.precedingTokenValue = this.nextTokenValue;
        this.nextTokenValue = null;
        this.nextTokenStartOffset = this.inputOffset;
        block44: while (true) {
            if (this.inputOffset >= this.inputLength) {
                this.nextToken = 0;
                return;
            }
            c = this.input.charAt(this.inputOffset++);
            switch (c) {
                case '/': {
                    if (this.inputOffset < this.inputLength && this.input.charAt(this.inputOffset) == '/') {
                        ++this.inputOffset;
                        this.nextToken = 8;
                        return;
                    }
                    this.nextToken = 2;
                    return;
                }
                case ':': {
                    if (this.inputOffset < this.inputLength) {
                        if (this.input.charAt(this.inputOffset) == ':') {
                            ++this.inputOffset;
                            this.nextToken = 41;
                            return;
                        }
                        if (this.input.charAt(this.inputOffset) == '=') {
                            this.nextToken = 58;
                            ++this.inputOffset;
                            return;
                        }
                        this.nextToken = 76;
                        return;
                    }
                    throw new XPathException("Unexpected colon at start of token");
                }
                case '@': {
                    this.nextToken = 3;
                    return;
                }
                case '?': {
                    this.nextToken = 213;
                    return;
                }
                case '[': {
                    this.nextToken = 4;
                    return;
                }
                case ']': {
                    this.nextToken = 203;
                    return;
                }
                case '{': {
                    this.nextToken = 59;
                    return;
                }
                case '}': {
                    this.nextToken = 215;
                    return;
                }
                case ';': {
                    this.nextToken = 149;
                    this.state = 0;
                    return;
                }
                case '%': {
                    this.nextToken = 106;
                    return;
                }
                case '(': {
                    if (this.inputOffset < this.inputLength && this.input.charAt(this.inputOffset) == '#') {
                        ++this.inputOffset;
                        int pragmaStart = this.inputOffset;
                        int nestingDepth = 1;
                        while (nestingDepth > 0 && this.inputOffset < this.inputLength - 1) {
                            if (this.input.charAt(this.inputOffset) == '\n') {
                                this.incrementLineNumber();
                            } else if (this.input.charAt(this.inputOffset) == '#' && this.input.charAt(this.inputOffset + 1) == ')') {
                                --nestingDepth;
                                ++this.inputOffset;
                            } else if (this.input.charAt(this.inputOffset) == '(' && this.input.charAt(this.inputOffset + 1) == '#') {
                                ++nestingDepth;
                                ++this.inputOffset;
                            }
                            ++this.inputOffset;
                        }
                        if (nestingDepth > 0) {
                            throw new XPathException("Unclosed XQuery pragma");
                        }
                        this.nextToken = 218;
                        this.nextTokenValue = this.input.substring(pragmaStart, this.inputOffset - 2);
                        return;
                    }
                    if (this.inputOffset < this.inputLength && this.input.charAt(this.inputOffset) == ':') {
                        ++this.inputOffset;
                        int nestingDepth = 1;
                        while (nestingDepth > 0 && this.inputOffset < this.inputLength - 1) {
                            if (this.input.charAt(this.inputOffset) == '\n') {
                                this.incrementLineNumber();
                            } else if (this.input.charAt(this.inputOffset) == ':' && this.input.charAt(this.inputOffset + 1) == ')') {
                                --nestingDepth;
                                ++this.inputOffset;
                            } else if (this.input.charAt(this.inputOffset) == '(' && this.input.charAt(this.inputOffset + 1) == ':') {
                                ++nestingDepth;
                                ++this.inputOffset;
                            }
                            ++this.inputOffset;
                        }
                        if (nestingDepth > 0) {
                            throw new XPathException("Unclosed XPath comment");
                        }
                        this.lookAhead();
                    } else {
                        this.nextToken = 5;
                    }
                    return;
                }
                case ')': {
                    this.nextToken = 204;
                    return;
                }
                case '+': {
                    this.nextToken = 15;
                    return;
                }
                case '-': {
                    this.nextToken = 16;
                    return;
                }
                case '=': {
                    if (this.inputOffset < this.inputLength && this.input.charAt(this.inputOffset) == '>') {
                        ++this.inputOffset;
                        this.nextToken = 77;
                        return;
                    }
                    this.nextToken = 6;
                    return;
                }
                case '!': {
                    if (this.inputOffset < this.inputLength && this.input.charAt(this.inputOffset) == '=') {
                        ++this.inputOffset;
                        this.nextToken = 22;
                        return;
                    }
                    this.nextToken = 40;
                    return;
                }
                case '*': {
                    if (this.inputOffset < this.inputLength && this.input.charAt(this.inputOffset) == ':' && this.inputOffset + 1 < this.inputLength && (this.input.charAt(this.inputOffset + 1) > '\u007f' || NameChecker.isNCNameStartChar(this.input.charAt(this.inputOffset + 1)))) {
                        ++this.inputOffset;
                        this.nextToken = 70;
                        return;
                    }
                    this.nextToken = 207;
                    return;
                }
                case ',': {
                    this.nextToken = 7;
                    return;
                }
                case '$': {
                    this.nextToken = 21;
                    return;
                }
                case '|': {
                    if (this.inputOffset < this.inputLength && this.input.charAt(this.inputOffset) == '|') {
                        ++this.inputOffset;
                        this.nextToken = 30;
                        return;
                    }
                    this.nextToken = 1;
                    return;
                }
                case '#': {
                    this.nextToken = 44;
                    return;
                }
                case '<': {
                    if (this.inputOffset < this.inputLength && this.input.charAt(this.inputOffset) == '=') {
                        ++this.inputOffset;
                        this.nextToken = 14;
                        return;
                    }
                    if (this.inputOffset < this.inputLength && this.input.charAt(this.inputOffset) == '<') {
                        ++this.inputOffset;
                        this.nextToken = 38;
                        return;
                    }
                    this.nextToken = 12;
                    return;
                }
                case '>': {
                    if (this.inputOffset < this.inputLength && this.input.charAt(this.inputOffset) == '=') {
                        ++this.inputOffset;
                        this.nextToken = 13;
                        return;
                    }
                    if (this.inputOffset < this.inputLength && this.input.charAt(this.inputOffset) == '>') {
                        ++this.inputOffset;
                        this.nextToken = 39;
                        return;
                    }
                    this.nextToken = 11;
                    return;
                }
                case '.': {
                    if (this.inputOffset < this.inputLength && this.input.charAt(this.inputOffset) == '.') {
                        ++this.inputOffset;
                        this.nextToken = 206;
                        return;
                    }
                    if (this.inputOffset < this.inputLength && this.input.charAt(this.inputOffset) == '{') {
                        ++this.inputOffset;
                        this.nextTokenValue = ".";
                        this.nextToken = 60;
                        return;
                    }
                    if (this.inputOffset == this.inputLength || this.input.charAt(this.inputOffset) < '0' || this.input.charAt(this.inputOffset) > '9') {
                        this.nextToken = 205;
                        return;
                    }
                }
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
                    boolean allowE = true;
                    boolean allowSign = false;
                    boolean allowDot = true;
                    block47: while (true) {
                        switch (c) {
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
                                allowSign = false;
                                break;
                            }
                            case '.': {
                                if (allowDot) {
                                    allowDot = false;
                                    allowSign = false;
                                    break;
                                }
                                --this.inputOffset;
                                break block47;
                            }
                            case 'E': 
                            case 'e': {
                                if (allowE) {
                                    allowSign = true;
                                    allowE = false;
                                    break;
                                }
                                --this.inputOffset;
                                break block47;
                            }
                            case '+': 
                            case '-': {
                                if (allowSign) {
                                    allowSign = false;
                                    break;
                                }
                                --this.inputOffset;
                                break block47;
                            }
                            default: {
                                if ('a' <= c && c <= 'z' || c > '\u007f') {
                                    throw new XPathException("Separator needed after numeric literal");
                                }
                                --this.inputOffset;
                                break block47;
                            }
                        }
                        if (this.inputOffset >= this.inputLength) break;
                        c = this.input.charAt(this.inputOffset++);
                    }
                    this.nextTokenValue = this.input.substring(this.nextTokenStartOffset, this.inputOffset);
                    this.nextToken = 209;
                    return;
                }
                case '\"': 
                case '\'': {
                    this.nextTokenValue = "";
                    while (true) {
                        char n;
                        this.inputOffset = this.input.indexOf(c, this.inputOffset);
                        if (this.inputOffset < 0) {
                            this.inputOffset = this.nextTokenStartOffset + 1;
                            throw new XPathException("Unmatched quote in expression");
                        }
                        this.nextTokenValue = this.nextTokenValue + this.input.substring(this.nextTokenStartOffset + 1, this.inputOffset++);
                        if (this.inputOffset >= this.inputLength || (n = this.input.charAt(this.inputOffset)) != c) break;
                        this.nextTokenValue = this.nextTokenValue + c;
                        this.nextTokenStartOffset = this.inputOffset++;
                    }
                    if (this.nextTokenValue.indexOf(10) >= 0) {
                        for (int i = 0; i < this.nextTokenValue.length(); ++i) {
                            if (this.nextTokenValue.charAt(i) != '\n') continue;
                            this.incrementLineNumber(this.nextTokenStartOffset + i + 1);
                        }
                    }
                    this.nextToken = 202;
                    return;
                }
                case '`': {
                    if (this.isXQuery && this.inputOffset < this.inputLength - 1 && this.input.charAt(this.inputOffset) == '`' && this.input.charAt(this.inputOffset + 1) == '[') {
                        this.inputOffset += 2;
                        int j = this.inputOffset;
                        int newlines = 0;
                        while (true) {
                            if (j >= this.inputLength) {
                                throw new XPathException("Unclosed string template in expression");
                            }
                            if (this.input.charAt(j) == '\n') {
                                ++newlines;
                            } else {
                                if (this.input.charAt(j) == '`' && j + 1 < this.inputLength && this.input.charAt(j + 1) == '{') {
                                    this.nextToken = 78;
                                    this.nextTokenValue = this.input.substring(this.inputOffset, j);
                                    this.inputOffset = j + 2;
                                    this.incrementLineNumber(newlines);
                                    return;
                                }
                                if (this.input.charAt(j) == ']' && j + 2 < this.inputLength && this.input.charAt(j + 1) == '`' && this.input.charAt(j + 2) == '`') {
                                    this.nextToken = 222;
                                    this.nextTokenValue = this.input.substring(this.inputOffset, j);
                                    this.inputOffset = j + 3;
                                    this.incrementLineNumber(newlines);
                                    return;
                                }
                            }
                            ++j;
                        }
                    }
                    throw new XPathException("Invalid character '`' (backtick) in expression");
                }
                case '\n': {
                    this.incrementLineNumber();
                }
                case '\t': 
                case '\r': 
                case ' ': {
                    this.nextTokenStartOffset = this.inputOffset;
                    continue block44;
                }
                case 'Q': 
                case '\u00b6': {
                    if (this.inputOffset < this.inputLength && this.input.charAt(this.inputOffset) == '{') {
                        int close;
                        if ((close = this.input.indexOf(125, this.inputOffset++)) < this.inputOffset) {
                            throw new XPathException("Missing closing brace in EQName");
                        }
                        String uri = this.input.substring(this.inputOffset, close);
                        if ((uri = Whitespace.collapseWhitespace(uri).toString()).contains("{")) {
                            throw new XPathException("EQName must not contain opening brace");
                        }
                        int start = this.inputOffset = close + 1;
                        boolean isStar = false;
                        while (this.inputOffset < this.inputLength) {
                            char c2 = this.input.charAt(this.inputOffset);
                            if (c2 > '\u0080' || Character.isLetterOrDigit(c2) || c2 == '_' || c2 == '.' || c2 == '-') {
                                ++this.inputOffset;
                                continue;
                            }
                            if (c2 != '*' || start != this.inputOffset) break;
                            ++this.inputOffset;
                            isStar = true;
                            break;
                        }
                        String localName = this.input.substring(start, this.inputOffset);
                        this.nextTokenValue = "Q{" + uri + "}" + localName;
                        this.nextToken = isStar ? 208 : 201;
                        return;
                    }
                }
                default: {
                    if (c >= '\u0080' || Character.isLetter(c)) break block44;
                    throw new XPathException("Invalid character '" + c + "' in expression");
                }
                case '_': 
            }
            break;
        }
        boolean foundColon = false;
        block52: while (this.inputOffset < this.inputLength) {
            c = this.input.charAt(this.inputOffset);
            switch (c) {
                case ':': {
                    if (foundColon) break block52;
                    if (this.precedingToken == 213 || this.precedingToken == 70) {
                        this.nextTokenValue = this.input.substring(this.nextTokenStartOffset, this.inputOffset);
                        this.nextToken = 201;
                        return;
                    }
                    if (this.inputOffset + 1 < this.inputLength) {
                        char nc = this.input.charAt(this.inputOffset + 1);
                        if (nc == ':') {
                            this.nextTokenValue = this.input.substring(this.nextTokenStartOffset, this.inputOffset);
                            this.nextToken = 36;
                            this.inputOffset += 2;
                            return;
                        }
                        if (nc == '*') {
                            this.nextTokenValue = this.input.substring(this.nextTokenStartOffset, this.inputOffset);
                            this.nextToken = 208;
                            this.inputOffset += 2;
                            return;
                        }
                        if (nc != '_' && nc <= '\u007f' && !Character.isLetter(nc)) {
                            this.nextTokenValue = this.input.substring(this.nextTokenStartOffset, this.inputOffset);
                            this.nextToken = 201;
                            return;
                        }
                    }
                    foundColon = true;
                    break;
                }
                case '-': 
                case '.': {
                    if (this.precedingToken > Token.LAST_OPERATOR && this.precedingToken != 213 && this.precedingToken != 70 && this.getBinaryOp(this.input.substring(this.nextTokenStartOffset, this.inputOffset)) != -1 && (this.precedingToken != 201 || this.getBinaryOp(this.precedingTokenValue) == -1)) {
                        this.nextToken = this.getBinaryOp(this.input.substring(this.nextTokenStartOffset, this.inputOffset));
                        return;
                    }
                }
                case '_': {
                    break;
                }
                default: {
                    if (c < '\u0080' && !Character.isLetterOrDigit(c)) break block52;
                }
            }
            ++this.inputOffset;
        }
        this.nextTokenValue = this.input.substring(this.nextTokenStartOffset, this.inputOffset);
        this.nextToken = 201;
    }

    int getBinaryOp(String s) {
        switch (s) {
            case "after": {
                return 118;
            }
            case "and": {
                return 10;
            }
            case "as": {
                return 71;
            }
            case "before": {
                return 119;
            }
            case "case": {
                return 67;
            }
            case "default": {
                return 212;
            }
            case "div": {
                return 18;
            }
            case "else": {
                return 27;
            }
            case "eq": {
                return 50;
            }
            case "except": {
                return 24;
            }
            case "ge": {
                return 54;
            }
            case "gt": {
                return 52;
            }
            case "idiv": {
                return 56;
            }
            case "in": {
                return 31;
            }
            case "intersect": {
                return 23;
            }
            case "into": {
                return 120;
            }
            case "is": {
                return 20;
            }
            case "le": {
                return 55;
            }
            case "lt": {
                return 53;
            }
            case "mod": {
                return 19;
            }
            case "modify": {
                return 68;
            }
            case "ne": {
                return 51;
            }
            case "or": {
                return 9;
            }
            case "otherwise": {
                return 79;
            }
            case "return": {
                return 25;
            }
            case "satisfies": {
                return 34;
            }
            case "then": {
                return 26;
            }
            case "to": {
                return 29;
            }
            case "union": {
                return 1;
            }
            case "where": {
                return 28;
            }
            case "with": {
                return 121;
            }
            case "orElse": {
                return this.allowSaxonExtensions ? 81 : -1;
            }
            case "andAlso": {
                return this.allowSaxonExtensions ? 80 : -1;
            }
        }
        return -1;
    }

    private int getFunctionType(String s) {
        switch (s) {
            case "if": {
                return 37;
            }
            case "map": 
            case "namespace-node": 
            case "array": 
            case "function": {
                return this.languageLevel == 20 ? 35 : 69;
            }
            case "node": 
            case "schema-attribute": 
            case "schema-element": 
            case "processing-instruction": 
            case "empty-sequence": 
            case "document-node": 
            case "comment": 
            case "element": 
            case "item": 
            case "text": 
            case "attribute": {
                return 69;
            }
            case "atomic": 
            case "tuple": 
            case "type": 
            case "union": {
                return this.allowSaxonExtensions ? 69 : 35;
            }
            case "switch": {
                return this.languageLevel == 20 ? 35 : 66;
            }
            case "otherwise": {
                return 79;
            }
            case "typeswitch": {
                return 65;
            }
        }
        return 35;
    }

    private boolean followsOperator(int precedingToken) {
        return precedingToken <= Token.LAST_OPERATOR;
    }

    public char nextChar() throws StringIndexOutOfBoundsException {
        char c;
        if ((c = this.input.charAt(this.inputOffset++)) == '\n') {
            this.incrementLineNumber();
            ++this.lineNumber;
        }
        return c;
    }

    private void incrementLineNumber() {
        ++this.nextLineNumber;
        if (this.newlineOffsets == null) {
            this.newlineOffsets = new ArrayList<Integer>(20);
        }
        this.newlineOffsets.add(this.inputOffset - 1);
    }

    public void incrementLineNumber(int offset) {
        ++this.nextLineNumber;
        if (this.newlineOffsets == null) {
            this.newlineOffsets = new ArrayList<Integer>(20);
        }
        this.newlineOffsets.add(offset);
    }

    public void unreadChar() {
        if (this.input.charAt(--this.inputOffset) == '\n') {
            --this.nextLineNumber;
            --this.lineNumber;
            if (this.newlineOffsets != null) {
                this.newlineOffsets.remove(this.newlineOffsets.size() - 1);
            }
        }
    }

    String recentText(int offset) {
        if (offset == -1) {
            if (this.inputOffset > this.inputLength) {
                this.inputOffset = this.inputLength;
            }
            if (this.inputOffset < 34) {
                return this.input.substring(0, this.inputOffset);
            }
            return Whitespace.collapseWhitespace("..." + this.input.substring(this.inputOffset - 30, this.inputOffset)).toString();
        }
        int end = offset + 30;
        if (end > this.inputLength) {
            end = this.inputLength;
        }
        return Whitespace.collapseWhitespace((offset > 0 ? "..." : "") + this.input.substring(offset, end)).toString();
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public int getColumnNumber() {
        return (int)(this.getLineAndColumn(this.currentTokenStartOffset) & Integer.MAX_VALUE);
    }

    private long getLineAndColumn(int offset) {
        if (this.newlineOffsets == null) {
            return offset;
        }
        for (int line = this.newlineOffsets.size() - 1; line >= 0; --line) {
            int nloffset = this.newlineOffsets.get(line);
            if (offset <= nloffset) continue;
            return (long)(line + 1) << 32 | (long)(offset - nloffset);
        }
        return offset;
    }

    public int getLineNumber(int offset) {
        return (int)(this.getLineAndColumn(offset) >> 32);
    }

    public int getColumnNumber(int offset) {
        return (int)(this.getLineAndColumn(offset) & Integer.MAX_VALUE);
    }
}


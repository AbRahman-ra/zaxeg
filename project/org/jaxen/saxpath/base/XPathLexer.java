/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.saxpath.base;

import org.jaxen.saxpath.base.Token;
import org.jaxen.saxpath.base.Verifier;

class XPathLexer {
    private String xpath;
    private int currentPosition;
    private int endPosition;
    private boolean expectOperator = false;

    XPathLexer(String xpath) {
        this.setXPath(xpath);
    }

    private void setXPath(String xpath) {
        this.xpath = xpath;
        this.currentPosition = 0;
        this.endPosition = xpath.length();
    }

    String getXPath() {
        return this.xpath;
    }

    Token nextToken() {
        Token token = null;
        do {
            token = null;
            block0 : switch (this.LA(1)) {
                case '$': {
                    token = this.dollar();
                    break;
                }
                case '\"': 
                case '\'': {
                    token = this.literal();
                    break;
                }
                case '/': {
                    token = this.slashes();
                    break;
                }
                case ',': {
                    token = this.comma();
                    break;
                }
                case '(': {
                    token = this.leftParen();
                    break;
                }
                case ')': {
                    token = this.rightParen();
                    break;
                }
                case '[': {
                    token = this.leftBracket();
                    break;
                }
                case ']': {
                    token = this.rightBracket();
                    break;
                }
                case '+': {
                    token = this.plus();
                    break;
                }
                case '-': {
                    token = this.minus();
                    break;
                }
                case '<': 
                case '>': {
                    token = this.relationalOperator();
                    break;
                }
                case '=': {
                    token = this.equals();
                    break;
                }
                case '!': {
                    if (this.LA(2) != '=') break;
                    token = this.notEquals();
                    break;
                }
                case '|': {
                    token = this.pipe();
                    break;
                }
                case '@': {
                    token = this.at();
                    break;
                }
                case ':': {
                    if (this.LA(2) == ':') {
                        token = this.doubleColon();
                        break;
                    }
                    token = this.colon();
                    break;
                }
                case '*': {
                    token = this.star();
                    break;
                }
                case '.': {
                    switch (this.LA(2)) {
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
                            token = this.number();
                            break block0;
                        }
                    }
                    token = this.dots();
                    break;
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
                    token = this.number();
                    break;
                }
                case '\t': 
                case '\n': 
                case '\r': 
                case ' ': {
                    token = this.whitespace();
                    break;
                }
                default: {
                    if (!Verifier.isXMLNCNameStartCharacter(this.LA(1))) break;
                    token = this.identifierOrOperatorName();
                }
            }
            if (token != null) continue;
            token = !this.hasMoreChars() ? new Token(-1, this.getXPath(), this.currentPosition, this.endPosition) : new Token(-3, this.getXPath(), this.currentPosition, this.endPosition);
        } while (token.getTokenType() == -2);
        switch (token.getTokenType()) {
            case 1: 
            case 2: 
            case 3: 
            case 4: 
            case 5: 
            case 6: 
            case 7: 
            case 8: 
            case 10: 
            case 11: 
            case 12: 
            case 13: 
            case 17: 
            case 18: 
            case 19: 
            case 20: 
            case 21: 
            case 23: 
            case 25: 
            case 27: 
            case 28: 
            case 30: 
            case 31: {
                this.expectOperator = false;
                break;
            }
            default: {
                this.expectOperator = true;
            }
        }
        return token;
    }

    private Token identifierOrOperatorName() {
        Token token = null;
        token = this.expectOperator ? this.operatorName() : this.identifier();
        return token;
    }

    private Token identifier() {
        Token token = null;
        int start = this.currentPosition;
        while (this.hasMoreChars() && Verifier.isXMLNCNameCharacter(this.LA(1))) {
            this.consume();
        }
        token = new Token(16, this.getXPath(), start, this.currentPosition);
        return token;
    }

    private Token operatorName() {
        Token token = null;
        switch (this.LA(1)) {
            case 'a': {
                token = this.and();
                break;
            }
            case 'o': {
                token = this.or();
                break;
            }
            case 'm': {
                token = this.mod();
                break;
            }
            case 'd': {
                token = this.div();
            }
        }
        return token;
    }

    private Token mod() {
        Token token = null;
        if (this.LA(1) == 'm' && this.LA(2) == 'o' && this.LA(3) == 'd') {
            token = new Token(10, this.getXPath(), this.currentPosition, this.currentPosition + 3);
            this.consume();
            this.consume();
            this.consume();
        }
        return token;
    }

    private Token div() {
        Token token = null;
        if (this.LA(1) == 'd' && this.LA(2) == 'i' && this.LA(3) == 'v') {
            token = new Token(11, this.getXPath(), this.currentPosition, this.currentPosition + 3);
            this.consume();
            this.consume();
            this.consume();
        }
        return token;
    }

    private Token and() {
        Token token = null;
        if (this.LA(1) == 'a' && this.LA(2) == 'n' && this.LA(3) == 'd') {
            token = new Token(27, this.getXPath(), this.currentPosition, this.currentPosition + 3);
            this.consume();
            this.consume();
            this.consume();
        }
        return token;
    }

    private Token or() {
        Token token = null;
        if (this.LA(1) == 'o' && this.LA(2) == 'r') {
            token = new Token(28, this.getXPath(), this.currentPosition, this.currentPosition + 2);
            this.consume();
            this.consume();
        }
        return token;
    }

    private Token number() {
        int start = this.currentPosition;
        boolean periodAllowed = true;
        block4: while (true) {
            switch (this.LA(1)) {
                case '.': {
                    if (!periodAllowed) break block4;
                    periodAllowed = false;
                    this.consume();
                    continue block4;
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
                    this.consume();
                    continue block4;
                }
            }
            break;
        }
        return new Token(29, this.getXPath(), start, this.currentPosition);
    }

    private Token whitespace() {
        this.consume();
        block3: while (this.hasMoreChars()) {
            switch (this.LA(1)) {
                case '\t': 
                case '\n': 
                case '\r': 
                case ' ': {
                    this.consume();
                    continue block3;
                }
            }
        }
        return new Token(-2, this.getXPath(), 0, 0);
    }

    private Token comma() {
        Token token = new Token(30, this.getXPath(), this.currentPosition, this.currentPosition + 1);
        this.consume();
        return token;
    }

    private Token equals() {
        Token token = new Token(1, this.getXPath(), this.currentPosition, this.currentPosition + 1);
        this.consume();
        return token;
    }

    private Token minus() {
        Token token = new Token(8, this.getXPath(), this.currentPosition, this.currentPosition + 1);
        this.consume();
        return token;
    }

    private Token plus() {
        Token token = new Token(7, this.getXPath(), this.currentPosition, this.currentPosition + 1);
        this.consume();
        return token;
    }

    private Token dollar() {
        Token token = new Token(25, this.getXPath(), this.currentPosition, this.currentPosition + 1);
        this.consume();
        return token;
    }

    private Token pipe() {
        Token token = new Token(18, this.getXPath(), this.currentPosition, this.currentPosition + 1);
        this.consume();
        return token;
    }

    private Token at() {
        Token token = new Token(17, this.getXPath(), this.currentPosition, this.currentPosition + 1);
        this.consume();
        return token;
    }

    private Token colon() {
        Token token = new Token(19, this.getXPath(), this.currentPosition, this.currentPosition + 1);
        this.consume();
        return token;
    }

    private Token doubleColon() {
        Token token = new Token(20, this.getXPath(), this.currentPosition, this.currentPosition + 2);
        this.consume();
        this.consume();
        return token;
    }

    private Token notEquals() {
        Token token = new Token(2, this.getXPath(), this.currentPosition, this.currentPosition + 2);
        this.consume();
        this.consume();
        return token;
    }

    private Token relationalOperator() {
        Token token = null;
        switch (this.LA(1)) {
            case '<': {
                if (this.LA(2) == '=') {
                    token = new Token(4, this.getXPath(), this.currentPosition, this.currentPosition + 2);
                    this.consume();
                } else {
                    token = new Token(3, this.getXPath(), this.currentPosition, this.currentPosition + 1);
                }
                this.consume();
                break;
            }
            case '>': {
                if (this.LA(2) == '=') {
                    token = new Token(6, this.getXPath(), this.currentPosition, this.currentPosition + 2);
                    this.consume();
                } else {
                    token = new Token(5, this.getXPath(), this.currentPosition, this.currentPosition + 1);
                }
                this.consume();
            }
        }
        return token;
    }

    private Token star() {
        int tokenType = this.expectOperator ? 31 : 9;
        Token token = new Token(tokenType, this.getXPath(), this.currentPosition, this.currentPosition + 1);
        this.consume();
        return token;
    }

    private Token literal() {
        Token token = null;
        char match = this.LA(1);
        this.consume();
        int start = this.currentPosition;
        while (token == null && this.hasMoreChars()) {
            if (this.LA(1) == match) {
                token = new Token(26, this.getXPath(), start, this.currentPosition);
            }
            this.consume();
        }
        return token;
    }

    private Token dots() {
        Token token = null;
        switch (this.LA(2)) {
            case '.': {
                token = new Token(15, this.getXPath(), this.currentPosition, this.currentPosition + 2);
                this.consume();
                this.consume();
                break;
            }
            default: {
                token = new Token(14, this.getXPath(), this.currentPosition, this.currentPosition + 1);
                this.consume();
            }
        }
        return token;
    }

    private Token leftBracket() {
        Token token = new Token(21, this.getXPath(), this.currentPosition, this.currentPosition + 1);
        this.consume();
        return token;
    }

    private Token rightBracket() {
        Token token = new Token(22, this.getXPath(), this.currentPosition, this.currentPosition + 1);
        this.consume();
        return token;
    }

    private Token leftParen() {
        Token token = new Token(23, this.getXPath(), this.currentPosition, this.currentPosition + 1);
        this.consume();
        return token;
    }

    private Token rightParen() {
        Token token = new Token(24, this.getXPath(), this.currentPosition, this.currentPosition + 1);
        this.consume();
        return token;
    }

    private Token slashes() {
        Token token = null;
        switch (this.LA(2)) {
            case '/': {
                token = new Token(13, this.getXPath(), this.currentPosition, this.currentPosition + 2);
                this.consume();
                this.consume();
                break;
            }
            default: {
                token = new Token(12, this.getXPath(), this.currentPosition, this.currentPosition + 1);
                this.consume();
            }
        }
        return token;
    }

    private char LA(int i) {
        if (this.currentPosition + (i - 1) >= this.endPosition) {
            return '\uffff';
        }
        return this.getXPath().charAt(this.currentPosition + (i - 1));
    }

    private void consume() {
        ++this.currentPosition;
    }

    private boolean hasMoreChars() {
        return this.currentPosition < this.endPosition;
    }
}


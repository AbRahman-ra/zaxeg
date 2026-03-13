/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.saxpath.base;

import java.util.ArrayList;
import org.jaxen.saxpath.Axis;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.saxpath.XPathHandler;
import org.jaxen.saxpath.XPathSyntaxException;
import org.jaxen.saxpath.base.Token;
import org.jaxen.saxpath.base.TokenTypes;
import org.jaxen.saxpath.base.XPathLexer;
import org.jaxen.saxpath.helpers.DefaultXPathHandler;

public class XPathReader
implements org.jaxen.saxpath.XPathReader {
    private ArrayList tokens;
    private XPathLexer lexer;
    private XPathHandler handler;
    private static XPathHandler defaultHandler = new DefaultXPathHandler();

    public XPathReader() {
        this.setXPathHandler(defaultHandler);
    }

    public void setXPathHandler(XPathHandler handler) {
        this.handler = handler;
    }

    public XPathHandler getXPathHandler() {
        return this.handler;
    }

    public void parse(String xpath) throws SAXPathException {
        this.setUpParse(xpath);
        this.getXPathHandler().startXPath();
        this.expr();
        this.getXPathHandler().endXPath();
        if (this.LA(1) != -1) {
            XPathSyntaxException ex = this.createSyntaxException("Unexpected '" + this.LT(1).getTokenText() + "'");
            throw ex;
        }
        this.lexer = null;
        this.tokens = null;
    }

    void setUpParse(String xpath) {
        this.tokens = new ArrayList();
        this.lexer = new XPathLexer(xpath);
    }

    private void pathExpr() throws SAXPathException {
        this.getXPathHandler().startPathExpr();
        switch (this.LA(1)) {
            case 26: 
            case 29: {
                this.filterExpr();
                if (this.LA(1) != 12 && this.LA(1) != 13) break;
                XPathSyntaxException ex = this.createSyntaxException("Node-set expected");
                throw ex;
            }
            case 23: 
            case 25: {
                this.filterExpr();
                if (this.LA(1) != 12 && this.LA(1) != 13) break;
                this.locationPath(false);
                break;
            }
            case 16: {
                if (this.LA(2) == 23 && !this.isNodeTypeName(this.LT(1)) || this.LA(2) == 19 && this.LA(4) == 23) {
                    this.filterExpr();
                    if (this.LA(1) != 12 && this.LA(1) != 13) break;
                    this.locationPath(false);
                    break;
                }
                this.locationPath(false);
                break;
            }
            case 9: 
            case 14: 
            case 15: 
            case 17: {
                this.locationPath(false);
                break;
            }
            case 12: 
            case 13: {
                this.locationPath(true);
                break;
            }
            default: {
                XPathSyntaxException ex = this.createSyntaxException("Unexpected '" + this.LT(1).getTokenText() + "'");
                throw ex;
            }
        }
        this.getXPathHandler().endPathExpr();
    }

    private void literal() throws SAXPathException {
        Token token = this.match(26);
        this.getXPathHandler().literal(token.getTokenText());
    }

    private void functionCall() throws SAXPathException {
        String prefix = null;
        String functionName = null;
        if (this.LA(2) == 19) {
            prefix = this.match(16).getTokenText();
            this.match(19);
        } else {
            prefix = "";
        }
        functionName = this.match(16).getTokenText();
        this.getXPathHandler().startFunction(prefix, functionName);
        this.match(23);
        this.arguments();
        this.match(24);
        this.getXPathHandler().endFunction();
    }

    private void arguments() throws SAXPathException {
        while (this.LA(1) != 24) {
            this.expr();
            if (this.LA(1) != 30) break;
            this.match(30);
        }
    }

    private void filterExpr() throws SAXPathException {
        this.getXPathHandler().startFilterExpr();
        switch (this.LA(1)) {
            case 29: {
                Token token = this.match(29);
                this.getXPathHandler().number(Double.parseDouble(token.getTokenText()));
                break;
            }
            case 26: {
                this.literal();
                break;
            }
            case 23: {
                this.match(23);
                this.expr();
                this.match(24);
                break;
            }
            case 16: {
                this.functionCall();
                break;
            }
            case 25: {
                this.variableReference();
            }
        }
        this.predicates();
        this.getXPathHandler().endFilterExpr();
    }

    private void variableReference() throws SAXPathException {
        this.match(25);
        String prefix = null;
        String variableName = null;
        if (this.LA(2) == 19) {
            prefix = this.match(16).getTokenText();
            this.match(19);
        } else {
            prefix = "";
        }
        variableName = this.match(16).getTokenText();
        this.getXPathHandler().variableReference(prefix, variableName);
    }

    void locationPath(boolean isAbsolute) throws SAXPathException {
        switch (this.LA(1)) {
            case 12: 
            case 13: {
                if (isAbsolute) {
                    this.absoluteLocationPath();
                    break;
                }
                this.relativeLocationPath();
                break;
            }
            case 9: 
            case 14: 
            case 15: 
            case 16: 
            case 17: {
                this.relativeLocationPath();
                break;
            }
            default: {
                XPathSyntaxException ex = this.createSyntaxException("Unexpected '" + this.LT(1).getTokenText() + "'");
                throw ex;
            }
        }
    }

    private void absoluteLocationPath() throws SAXPathException {
        this.getXPathHandler().startAbsoluteLocationPath();
        block0 : switch (this.LA(1)) {
            case 12: {
                this.match(12);
                switch (this.LA(1)) {
                    case 9: 
                    case 14: 
                    case 15: 
                    case 16: 
                    case 17: {
                        this.steps();
                    }
                }
                break;
            }
            case 13: {
                this.getXPathHandler().startAllNodeStep(12);
                this.getXPathHandler().endAllNodeStep();
                this.match(13);
                switch (this.LA(1)) {
                    case 9: 
                    case 14: 
                    case 15: 
                    case 16: 
                    case 17: {
                        this.steps();
                        break block0;
                    }
                }
                XPathSyntaxException ex = this.createSyntaxException("Location path cannot end with //");
                throw ex;
            }
        }
        this.getXPathHandler().endAbsoluteLocationPath();
    }

    private void relativeLocationPath() throws SAXPathException {
        this.getXPathHandler().startRelativeLocationPath();
        switch (this.LA(1)) {
            case 12: {
                this.match(12);
                break;
            }
            case 13: {
                this.getXPathHandler().startAllNodeStep(12);
                this.getXPathHandler().endAllNodeStep();
                this.match(13);
            }
        }
        this.steps();
        this.getXPathHandler().endRelativeLocationPath();
    }

    private void steps() throws SAXPathException {
        switch (this.LA(1)) {
            case 9: 
            case 14: 
            case 15: 
            case 16: 
            case 17: {
                this.step();
                break;
            }
            case -1: {
                return;
            }
            default: {
                XPathSyntaxException ex = this.createSyntaxException("Expected one of '.', '..', '@', '*', <QName>");
                throw ex;
            }
        }
        block11: while (true) {
            if (this.LA(1) == 12 || this.LA(1) == 13) {
                switch (this.LA(1)) {
                    case 12: {
                        this.match(12);
                        break;
                    }
                    case 13: {
                        this.getXPathHandler().startAllNodeStep(12);
                        this.getXPathHandler().endAllNodeStep();
                        this.match(13);
                    }
                }
            } else {
                return;
            }
            switch (this.LA(1)) {
                case 9: 
                case 14: 
                case 15: 
                case 16: 
                case 17: {
                    this.step();
                    continue block11;
                }
            }
            break;
        }
        XPathSyntaxException ex = this.createSyntaxException("Expected one of '.', '..', '@', '*', <QName>");
        throw ex;
    }

    void step() throws SAXPathException {
        int axis = 0;
        switch (this.LA(1)) {
            case 14: 
            case 15: {
                this.abbrStep();
                return;
            }
            case 17: {
                axis = this.axisSpecifier();
                break;
            }
            case 16: {
                if (this.LA(2) == 20) {
                    axis = this.axisSpecifier();
                    break;
                }
                axis = 1;
                break;
            }
            case 9: {
                axis = 1;
            }
        }
        this.nodeTest(axis);
    }

    private int axisSpecifier() throws SAXPathException {
        int axis = 0;
        switch (this.LA(1)) {
            case 17: {
                this.match(17);
                axis = 9;
                break;
            }
            case 16: {
                Token token = this.LT(1);
                axis = Axis.lookup(token.getTokenText());
                if (axis == 0) {
                    this.throwInvalidAxis(token.getTokenText());
                }
                this.match(16);
                this.match(20);
                break;
            }
        }
        return axis;
    }

    private void nodeTest(int axis) throws SAXPathException {
        block0 : switch (this.LA(1)) {
            case 16: {
                switch (this.LA(2)) {
                    case 23: {
                        this.nodeTypeTest(axis);
                        break block0;
                    }
                }
                this.nameTest(axis);
                break;
            }
            case 9: {
                this.nameTest(axis);
                break;
            }
            default: {
                XPathSyntaxException ex = this.createSyntaxException("Expected <QName> or *");
                throw ex;
            }
        }
    }

    private void nodeTypeTest(int axis) throws SAXPathException {
        Token nodeTypeToken = this.match(16);
        String nodeType = nodeTypeToken.getTokenText();
        this.match(23);
        if ("processing-instruction".equals(nodeType)) {
            String piName = "";
            if (this.LA(1) == 26) {
                piName = this.match(26).getTokenText();
            }
            this.match(24);
            this.getXPathHandler().startProcessingInstructionNodeStep(axis, piName);
            this.predicates();
            this.getXPathHandler().endProcessingInstructionNodeStep();
        } else if ("node".equals(nodeType)) {
            this.match(24);
            this.getXPathHandler().startAllNodeStep(axis);
            this.predicates();
            this.getXPathHandler().endAllNodeStep();
        } else if ("text".equals(nodeType)) {
            this.match(24);
            this.getXPathHandler().startTextNodeStep(axis);
            this.predicates();
            this.getXPathHandler().endTextNodeStep();
        } else if ("comment".equals(nodeType)) {
            this.match(24);
            this.getXPathHandler().startCommentNodeStep(axis);
            this.predicates();
            this.getXPathHandler().endCommentNodeStep();
        } else {
            XPathSyntaxException ex = this.createSyntaxException("Expected node-type");
            throw ex;
        }
    }

    private void nameTest(int axis) throws SAXPathException {
        String prefix = null;
        String localName = null;
        switch (this.LA(2)) {
            case 19: {
                switch (this.LA(1)) {
                    case 16: {
                        prefix = this.match(16).getTokenText();
                        this.match(19);
                    }
                }
            }
        }
        switch (this.LA(1)) {
            case 16: {
                localName = this.match(16).getTokenText();
                break;
            }
            case 9: {
                this.match(9);
                localName = "*";
            }
        }
        if (prefix == null) {
            prefix = "";
        }
        this.getXPathHandler().startNameStep(axis, prefix, localName);
        this.predicates();
        this.getXPathHandler().endNameStep();
    }

    private void abbrStep() throws SAXPathException {
        switch (this.LA(1)) {
            case 14: {
                this.match(14);
                this.getXPathHandler().startAllNodeStep(11);
                this.predicates();
                this.getXPathHandler().endAllNodeStep();
                break;
            }
            case 15: {
                this.match(15);
                this.getXPathHandler().startAllNodeStep(3);
                this.predicates();
                this.getXPathHandler().endAllNodeStep();
            }
        }
    }

    private void predicates() throws SAXPathException {
        while (this.LA(1) == 21) {
            this.predicate();
        }
    }

    void predicate() throws SAXPathException {
        this.getXPathHandler().startPredicate();
        this.match(21);
        this.predicateExpr();
        this.match(22);
        this.getXPathHandler().endPredicate();
    }

    private void predicateExpr() throws SAXPathException {
        this.expr();
    }

    private void expr() throws SAXPathException {
        this.orExpr();
    }

    private void orExpr() throws SAXPathException {
        this.getXPathHandler().startOrExpr();
        this.andExpr();
        boolean create = false;
        switch (this.LA(1)) {
            case 28: {
                create = true;
                this.match(28);
                this.orExpr();
            }
        }
        this.getXPathHandler().endOrExpr(create);
    }

    private void andExpr() throws SAXPathException {
        this.getXPathHandler().startAndExpr();
        this.equalityExpr();
        boolean create = false;
        switch (this.LA(1)) {
            case 27: {
                create = true;
                this.match(27);
                this.andExpr();
            }
        }
        this.getXPathHandler().endAndExpr(create);
    }

    private void equalityExpr() throws SAXPathException {
        this.relationalExpr();
        int la = this.LA(1);
        while (la == 1 || la == 2) {
            switch (la) {
                case 1: {
                    this.match(1);
                    this.getXPathHandler().startEqualityExpr();
                    this.relationalExpr();
                    this.getXPathHandler().endEqualityExpr(1);
                    break;
                }
                case 2: {
                    this.match(2);
                    this.getXPathHandler().startEqualityExpr();
                    this.relationalExpr();
                    this.getXPathHandler().endEqualityExpr(2);
                }
            }
            la = this.LA(1);
        }
    }

    private void relationalExpr() throws SAXPathException {
        this.additiveExpr();
        int la = this.LA(1);
        while (la == 3 || la == 5 || la == 4 || la == 6) {
            switch (la) {
                case 3: {
                    this.match(3);
                    this.getXPathHandler().startRelationalExpr();
                    this.additiveExpr();
                    this.getXPathHandler().endRelationalExpr(3);
                    break;
                }
                case 5: {
                    this.match(5);
                    this.getXPathHandler().startRelationalExpr();
                    this.additiveExpr();
                    this.getXPathHandler().endRelationalExpr(5);
                    break;
                }
                case 6: {
                    this.match(6);
                    this.getXPathHandler().startRelationalExpr();
                    this.additiveExpr();
                    this.getXPathHandler().endRelationalExpr(6);
                    break;
                }
                case 4: {
                    this.match(4);
                    this.getXPathHandler().startRelationalExpr();
                    this.additiveExpr();
                    this.getXPathHandler().endRelationalExpr(4);
                }
            }
            la = this.LA(1);
        }
    }

    private void additiveExpr() throws SAXPathException {
        this.multiplicativeExpr();
        int la = this.LA(1);
        while (la == 7 || la == 8) {
            switch (la) {
                case 7: {
                    this.match(7);
                    this.getXPathHandler().startAdditiveExpr();
                    this.multiplicativeExpr();
                    this.getXPathHandler().endAdditiveExpr(7);
                    break;
                }
                case 8: {
                    this.match(8);
                    this.getXPathHandler().startAdditiveExpr();
                    this.multiplicativeExpr();
                    this.getXPathHandler().endAdditiveExpr(8);
                }
            }
            la = this.LA(1);
        }
    }

    private void multiplicativeExpr() throws SAXPathException {
        this.unaryExpr();
        int la = this.LA(1);
        while (la == 31 || la == 11 || la == 10) {
            switch (la) {
                case 9: 
                case 31: {
                    this.match(31);
                    this.getXPathHandler().startMultiplicativeExpr();
                    this.unaryExpr();
                    this.getXPathHandler().endMultiplicativeExpr(9);
                    break;
                }
                case 11: {
                    this.match(11);
                    this.getXPathHandler().startMultiplicativeExpr();
                    this.unaryExpr();
                    this.getXPathHandler().endMultiplicativeExpr(11);
                    break;
                }
                case 10: {
                    this.match(10);
                    this.getXPathHandler().startMultiplicativeExpr();
                    this.unaryExpr();
                    this.getXPathHandler().endMultiplicativeExpr(10);
                }
            }
            la = this.LA(1);
        }
    }

    private void unaryExpr() throws SAXPathException {
        switch (this.LA(1)) {
            case 8: {
                this.getXPathHandler().startUnaryExpr();
                this.match(8);
                this.unaryExpr();
                this.getXPathHandler().endUnaryExpr(12);
                break;
            }
            default: {
                this.unionExpr();
            }
        }
    }

    private void unionExpr() throws SAXPathException {
        this.getXPathHandler().startUnionExpr();
        this.pathExpr();
        boolean create = false;
        switch (this.LA(1)) {
            case 18: {
                this.match(18);
                create = true;
                this.expr();
            }
        }
        this.getXPathHandler().endUnionExpr(create);
    }

    private Token match(int tokenType) throws XPathSyntaxException {
        this.LT(1);
        Token token = (Token)this.tokens.get(0);
        if (token.getTokenType() == tokenType) {
            this.tokens.remove(0);
            return token;
        }
        XPathSyntaxException ex = this.createSyntaxException("Expected: " + TokenTypes.getTokenText(tokenType));
        throw ex;
    }

    private int LA(int position) {
        return this.LT(position).getTokenType();
    }

    private Token LT(int position) {
        if (this.tokens.size() <= position - 1) {
            for (int i = 0; i < position; ++i) {
                this.tokens.add(this.lexer.nextToken());
            }
        }
        return (Token)this.tokens.get(position - 1);
    }

    private boolean isNodeTypeName(Token name) {
        String text = name.getTokenText();
        return "node".equals(text) || "comment".equals(text) || "text".equals(text) || "processing-instruction".equals(text);
    }

    private XPathSyntaxException createSyntaxException(String message) {
        String xpath = this.lexer.getXPath();
        int position = this.LT(1).getTokenBegin();
        return new XPathSyntaxException(xpath, position, message);
    }

    private void throwInvalidAxis(String invalidAxis) throws SAXPathException {
        String xpath = this.lexer.getXPath();
        int position = this.LT(1).getTokenBegin();
        String message = "Expected valid axis name instead of [" + invalidAxis + "]";
        throw new XPathSyntaxException(xpath, position, message);
    }
}


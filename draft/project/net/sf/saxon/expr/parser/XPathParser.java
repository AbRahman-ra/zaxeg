/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.function.IntPredicate;
import net.sf.saxon.Configuration;
import net.sf.saxon.Version;
import net.sf.saxon.expr.AndExpression;
import net.sf.saxon.expr.Assignation;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.CastExpression;
import net.sf.saxon.expr.CastableExpression;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.ErrorExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.ForExpression;
import net.sf.saxon.expr.HomogeneityChecker;
import net.sf.saxon.expr.IdentityComparison;
import net.sf.saxon.expr.InstanceOfExpression;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.ListCastableFunction;
import net.sf.saxon.expr.ListConstructorFunction;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.LocalBinding;
import net.sf.saxon.expr.LocalVariableReference;
import net.sf.saxon.expr.LookupAllExpression;
import net.sf.saxon.expr.LookupExpression;
import net.sf.saxon.expr.OrExpression;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.QuantifiedExpression;
import net.sf.saxon.expr.RangeExpression;
import net.sf.saxon.expr.RootExpression;
import net.sf.saxon.expr.SlashExpression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.StaticFunctionCall;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.TreatExpression;
import net.sf.saxon.expr.UnionCastableFunction;
import net.sf.saxon.expr.UnionConstructorFunction;
import net.sf.saxon.expr.ValueComparison;
import net.sf.saxon.expr.VennExpression;
import net.sf.saxon.expr.flwor.Clause;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.Choose;
import net.sf.saxon.expr.instruct.ForEach;
import net.sf.saxon.expr.parser.CodeInjector;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.ParserExtension;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.expr.parser.Tokenizer;
import net.sf.saxon.functions.ApplyFn;
import net.sf.saxon.functions.Concat;
import net.sf.saxon.functions.CurrentGroupCall;
import net.sf.saxon.functions.CurrentGroupingKeyCall;
import net.sf.saxon.functions.CurrentMergeGroup;
import net.sf.saxon.functions.CurrentMergeKey;
import net.sf.saxon.functions.RegexGroup;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.functions.registry.VendorFunctionSetHE;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.ma.arrays.ArrayFunctionSet;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.ma.arrays.SimpleArrayItem;
import net.sf.saxon.ma.arrays.SquareArrayConstructor;
import net.sf.saxon.ma.map.DictionaryMap;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.ma.map.MapFunctionSet;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.ma.map.SingleEntryMap;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.QNameException;
import net.sf.saxon.om.QNameParser;
import net.sf.saxon.om.StandardNames;
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
import net.sf.saxon.pattern.QNameTest;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.UnprefixedElementMatchingPolicy;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.AnySimpleType;
import net.sf.saxon.type.AnyType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.CastingTarget;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.type.ListType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.UnionType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;
import net.sf.saxon.z.IntArraySet;
import net.sf.saxon.z.IntSet;

public class XPathParser {
    protected Tokenizer t;
    protected StaticContext env;
    protected Stack<LocalBinding> rangeVariables = new Stack();
    protected QNameParser qNameParser;
    protected ParserExtension parserExtension = new ParserExtension();
    protected IntPredicate charChecker;
    protected boolean allowXPath30Syntax = false;
    protected boolean allowXPath30XSLTExtensions = false;
    protected boolean allowXPath31Syntax = false;
    protected boolean allowSaxonExtensions = false;
    protected boolean scanOnly = false;
    private boolean allowAbsentExpression = false;
    protected CodeInjector codeInjector = null;
    private Accelerator accelerator = null;
    protected ParsedLanguage language = ParsedLanguage.XPATH;
    protected int languageVersion = 20;
    protected int catchDepth = 0;
    private static final String[] reservedFunctionNames30 = new String[]{"attribute", "comment", "document-node", "element", "empty-sequence", "function", "if", "item", "namespace-node", "node", "processing-instruction", "schema-attribute", "schema-element", "switch", "text", "typeswitch"};
    private static final String[] reservedFunctionNames31 = new String[]{"array", "attribute", "comment", "document-node", "element", "empty-sequence", "function", "if", "item", "map", "namespace-node", "node", "processing-instruction", "schema-attribute", "schema-element", "switch", "text", "typeswitch"};
    private Location mostRecentLocation = Loc.NONE;

    public void setCodeInjector(CodeInjector injector) {
        this.codeInjector = injector;
    }

    public CodeInjector getCodeInjector() {
        return this.codeInjector;
    }

    public void setAccelerator(Accelerator accelerator) {
        this.accelerator = accelerator;
    }

    public Tokenizer getTokenizer() {
        return this.t;
    }

    public StaticContext getStaticContext() {
        return this.env;
    }

    public void setParserExtension(ParserExtension extension) {
        this.parserExtension = extension;
    }

    public void setCatchDepth(int depth) {
        this.catchDepth = depth;
    }

    public void nextToken() throws XPathException {
        try {
            this.t.next();
        } catch (XPathException e) {
            this.grumble(e.getMessage());
        }
    }

    public void expect(int token) throws XPathException {
        if (this.t.currentToken != token) {
            this.grumble("expected \"" + Token.tokens[token] + "\", found " + this.currentTokenDisplay());
        }
    }

    public void grumble(String message) throws XPathException {
        this.grumble(message, this.language == ParsedLanguage.XSLT_PATTERN ? "XTSE0340" : "XPST0003");
    }

    public void grumble(String message, String errorCode) throws XPathException {
        this.grumble(message, new StructuredQName("", "http://www.w3.org/2005/xqt-errors", errorCode), -1);
    }

    public void grumble(String message, String errorCode, int offset) throws XPathException {
        this.grumble(message, new StructuredQName("", "http://www.w3.org/2005/xqt-errors", errorCode), offset);
    }

    protected void grumble(String message, StructuredQName errorCode, int offset) throws XPathException {
        int column;
        int line;
        if (errorCode == null) {
            errorCode = new StructuredQName("err", "http://www.w3.org/2005/xqt-errors", "XPST0003");
        }
        String nearbyText = this.t.recentText(-1);
        if (offset == -1) {
            line = this.t.getLineNumber();
            column = this.t.getColumnNumber();
        } else {
            line = this.t.getLineNumber(offset);
            column = this.t.getColumnNumber(offset);
        }
        Location loc = this.makeNestedLocation(this.env.getContainingLocation(), line, column, nearbyText);
        XPathException err = new XPathException(message);
        err.setLocation(loc);
        err.setIsSyntaxError("XPST0003".equals(errorCode.getLocalPart()));
        err.setIsStaticError(true);
        err.setHostLanguage(this.getLanguage());
        err.setErrorCodeQName(errorCode);
        throw err;
    }

    protected void warning(String message) {
        if (!this.env.getConfiguration().getBooleanProperty(Feature.SUPPRESS_XPATH_WARNINGS)) {
            String s = this.t.recentText(-1);
            String prefix = (message.startsWith("...") ? "near" : "in") + ' ' + Err.wrap(s) + ":\n    ";
            this.env.issueWarning(prefix + message, this.makeLocation());
        }
    }

    public void setLanguage(ParsedLanguage language, int version) {
        if (version == 0) {
            version = 30;
        }
        if (version == 305) {
            version = 30;
            this.allowXPath30XSLTExtensions = true;
        }
        switch (language) {
            case XPATH: {
                if (version == 20 || version == 30 || version == 31) break;
                throw new IllegalArgumentException("Unsupported language version " + version);
            }
            case XSLT_PATTERN: 
            case SEQUENCE_TYPE: {
                if (version == 20 || version == 30 || version == 31) break;
                throw new IllegalArgumentException("Unsupported language version " + version);
            }
            case XQUERY: {
                if (version == 10 || version == 30 || version == 31) break;
                throw new IllegalArgumentException("Unsupported language version " + version);
            }
            default: {
                throw new IllegalArgumentException("Unknown language " + (Object)((Object)language));
            }
        }
        this.language = language;
        this.languageVersion = version;
        this.allowXPath30Syntax = this.languageVersion >= 30;
        this.allowXPath31Syntax = this.languageVersion >= 31;
    }

    protected String getLanguage() {
        switch (this.language) {
            case XPATH: {
                return "XPath";
            }
            case XSLT_PATTERN: {
                return "XSLT Pattern";
            }
            case SEQUENCE_TYPE: {
                return "SequenceType";
            }
            case XQUERY: {
                return "XQuery";
            }
            case EXTENDED_ITEM_TYPE: {
                return "Extended ItemType";
            }
        }
        return "XPath";
    }

    public boolean isAllowXPath31Syntax() {
        return this.allowXPath31Syntax;
    }

    public void setQNameParser(QNameParser qp) {
        this.qNameParser = qp;
    }

    public QNameParser getQNameParser() {
        return this.qNameParser;
    }

    protected String currentTokenDisplay() {
        if (this.t.currentToken == 201) {
            return "name \"" + this.t.currentTokenValue + '\"';
        }
        if (this.t.currentToken == -1) {
            return "(unknown token)";
        }
        return '\"' + Token.tokens[this.t.currentToken] + '\"';
    }

    public Expression parse(String expression, int start, int terminator, StaticContext env) throws XPathException {
        this.env = env;
        int languageVersion = env.getXPathVersion();
        if (languageVersion == 20 && this.language == ParsedLanguage.XQUERY) {
            languageVersion = 10;
        }
        this.setLanguage(this.language, languageVersion);
        Expression exp = null;
        int offset = start;
        if (this.accelerator != null && env.getUnprefixedElementMatchingPolicy() == UnprefixedElementMatchingPolicy.DEFAULT_NAMESPACE && terminator != -1 && (expression.length() - start < 30 || terminator == 215)) {
            this.t = new Tokenizer();
            this.t.languageLevel = env.getXPathVersion();
            exp = this.accelerator.parse(this.t, env, expression, start, terminator);
        }
        if (exp == null) {
            this.qNameParser = new QNameParser(env.getNamespaceResolver()).withAcceptEQName(this.allowXPath30Syntax).withErrorOnBadSyntax(this.language == ParsedLanguage.XSLT_PATTERN ? "XTSE0340" : "XPST0003").withErrorOnUnresolvedPrefix("XPST0081");
            this.charChecker = env.getConfiguration().getValidCharacterChecker();
            this.t = new Tokenizer();
            this.t.languageLevel = env.getXPathVersion();
            this.allowSaxonExtensions = this.t.allowSaxonExtensions = env.getConfiguration().getBooleanProperty(Feature.ALLOW_SYNTAX_EXTENSIONS);
            offset = this.t.currentTokenStartOffset;
            this.customizeTokenizer(this.t);
            try {
                this.t.tokenize(expression, start, -1);
            } catch (XPathException err) {
                this.grumble(err.getMessage());
            }
            if (this.t.currentToken == terminator) {
                if (this.allowAbsentExpression) {
                    Literal result = Literal.makeEmptySequence();
                    result.setRetainedStaticContext(env.makeRetainedStaticContext());
                    this.setLocation(result);
                    return result;
                }
                this.grumble("The expression is empty");
            }
            exp = this.parseExpression();
            if (this.t.currentToken != terminator && terminator != -1) {
                if (this.t.currentToken == 0 && terminator == 215) {
                    this.grumble("Missing curly brace after expression in value template", "XTSE0350");
                } else {
                    this.grumble("Unexpected token " + this.currentTokenDisplay() + " beyond end of expression");
                }
            }
            this.setLocation(exp, offset);
        }
        exp.setRetainedStaticContextThoroughly(env.makeRetainedStaticContext());
        return exp;
    }

    protected void customizeTokenizer(Tokenizer t) {
    }

    public SequenceType parseSequenceType(String input, StaticContext env) throws XPathException {
        this.env = env;
        if (this.qNameParser == null) {
            this.qNameParser = new QNameParser(env.getNamespaceResolver());
            if (this.languageVersion >= 30) {
                this.qNameParser = this.qNameParser.withAcceptEQName(true);
            }
        }
        this.language = ParsedLanguage.SEQUENCE_TYPE;
        this.t = new Tokenizer();
        this.t.languageLevel = env.getXPathVersion();
        this.allowSaxonExtensions = this.t.allowSaxonExtensions = env.getConfiguration().getBooleanProperty(Feature.ALLOW_SYNTAX_EXTENSIONS);
        try {
            this.t.tokenize(input, 0, -1);
        } catch (XPathException err) {
            this.grumble(err.getMessage());
        }
        SequenceType req = this.parseSequenceType();
        if (this.t.currentToken != 0) {
            this.grumble("Unexpected token " + this.currentTokenDisplay() + " beyond end of SequenceType");
        }
        return req;
    }

    public ItemType parseExtendedItemType(String input, StaticContext env) throws XPathException {
        this.env = env;
        this.language = ParsedLanguage.EXTENDED_ITEM_TYPE;
        this.t = new Tokenizer();
        this.t.languageLevel = env.getXPathVersion();
        this.t.allowSaxonExtensions = true;
        this.allowSaxonExtensions = true;
        try {
            this.t.tokenize(input, 0, -1);
        } catch (XPathException err) {
            this.grumble(err.getMessage());
        }
        ItemType req = this.parseItemType();
        if (this.t.currentToken != 0) {
            this.grumble("Unexpected token " + this.currentTokenDisplay() + " beyond end of ItemType");
        }
        return req;
    }

    public SequenceType parseExtendedSequenceType(String input, StaticContext env) throws XPathException {
        this.env = env;
        this.language = ParsedLanguage.EXTENDED_ITEM_TYPE;
        this.t = new Tokenizer();
        this.t.languageLevel = env.getXPathVersion();
        this.t.allowSaxonExtensions = true;
        this.allowSaxonExtensions = true;
        try {
            this.t.tokenize(input, 0, -1);
        } catch (XPathException err) {
            this.grumble(err.getMessage());
        }
        SequenceType req = this.parseSequenceType();
        if (this.t.currentToken != 0) {
            this.grumble("Unexpected token " + this.currentTokenDisplay() + " beyond end of SequenceType");
        }
        return req;
    }

    public Expression parseExpression() throws XPathException {
        int offset = this.t.currentTokenStartOffset;
        Expression exp = this.parseExprSingle();
        ArrayList<Expression> list = null;
        while (this.t.currentToken == 7) {
            if (list == null) {
                list = new ArrayList<Expression>(10);
                list.add(exp);
            }
            this.nextToken();
            Expression next = this.parseExprSingle();
            this.setLocation(next);
            list.add(next);
        }
        if (list != null) {
            exp = Block.makeBlock(list);
            this.setLocation(exp, offset);
        }
        return exp;
    }

    public Expression parseExprSingle() throws XPathException {
        Expression e = this.parserExtension.parseExtendedExprSingle(this);
        if (e != null) {
            return e;
        }
        int peek = this.t.peekAhead();
        if (peek == 0 || peek == 7 || peek == 204 || peek == 203) {
            switch (this.t.currentToken) {
                case 202: {
                    return this.parseStringLiteral(true);
                }
                case 209: {
                    return this.parseNumericLiteral(true);
                }
                case 70: 
                case 201: 
                case 207: 
                case 208: {
                    return this.parseBasicStep(true);
                }
                case 205: {
                    this.nextToken();
                    ContextItemExpression cie = new ContextItemExpression();
                    this.setLocation(cie);
                    return cie;
                }
                case 206: {
                    this.nextToken();
                    AxisExpression pne = new AxisExpression(9, null);
                    this.setLocation(pne);
                    return pne;
                }
            }
        }
        switch (this.t.currentToken) {
            case 0: {
                this.grumble("Expected an expression, but reached the end of the input");
            }
            case 73: 
            case 74: 
            case 211: 
            case 216: {
                return this.parseFLWORExpression();
            }
            case 32: 
            case 33: {
                return this.parseQuantifiedExpression();
            }
            case 75: {
                return this.parserExtension.parseForMemberExpression(this);
            }
            case 37: {
                return this.parseIfExpression();
            }
            case 66: {
                return this.parseSwitchExpression();
            }
            case 65: {
                return this.parseTypeswitchExpression();
            }
            case 102: 
            case 103: 
            case 104: 
            case 105: {
                return this.parseValidateExpression();
            }
            case 218: {
                return this.parseExtensionExpression();
            }
            case 60: {
                if (!this.t.currentTokenValue.equals("try")) break;
                return this.parseTryCatchExpression();
            }
        }
        return this.parseBinaryExpression(this.parseUnaryExpression(), 4);
    }

    public Expression parseBinaryExpression(Expression lhs, int minPrecedence) throws XPathException {
        block5: while (this.getCurrentOperatorPrecedence() >= minPrecedence) {
            int offset = this.t.currentTokenStartOffset;
            int operator = this.t.currentToken;
            int prec = this.getCurrentOperatorPrecedence();
            switch (operator) {
                case 45: 
                case 47: {
                    this.nextToken();
                    SequenceType seq = this.parseSequenceType();
                    lhs = this.makeSequenceTypeExpression(lhs, operator, seq);
                    this.setLocation(lhs, offset);
                    if (this.getCurrentOperatorPrecedence() < prec) continue block5;
                    this.grumble("Left operand of '" + Token.tokens[this.t.currentToken] + "' needs parentheses");
                    continue block5;
                }
                case 46: 
                case 57: {
                    boolean allowEmpty;
                    CastingTarget at;
                    this.nextToken();
                    if (this.allowSaxonExtensions && this.t.currentToken == 69 && this.t.currentTokenValue.equals("union")) {
                        at = (CastingTarget)((Object)this.parseItemType());
                    } else {
                        this.expect(201);
                        at = this.getSimpleType(this.t.currentTokenValue);
                        if (at == BuiltInAtomicType.ANY_ATOMIC) {
                            this.grumble("No value is castable to xs:anyAtomicType", "XPST0080");
                        }
                        if (at == BuiltInAtomicType.NOTATION) {
                            this.grumble("No value is castable to xs:NOTATION", "XPST0080");
                        }
                        this.nextToken();
                    }
                    boolean bl = allowEmpty = this.t.currentToken == 213;
                    if (allowEmpty) {
                        this.nextToken();
                    }
                    lhs = this.makeSingleTypeExpression(lhs, operator, at, allowEmpty);
                    this.setLocation(lhs, offset);
                    if (this.getCurrentOperatorPrecedence() < prec) continue block5;
                    this.grumble("Left operand of '" + Token.tokens[this.t.currentToken] + "' needs parentheses");
                    continue block5;
                }
                case 77: {
                    lhs = this.parseArrowPostfix(lhs);
                    continue block5;
                }
            }
            this.nextToken();
            Expression rhs = this.parseUnaryExpression();
            while (this.getCurrentOperatorPrecedence() > prec) {
                rhs = this.parseBinaryExpression(rhs, this.getCurrentOperatorPrecedence());
            }
            if (this.getCurrentOperatorPrecedence() == prec && !this.allowMultipleOperators()) {
                String tok = Token.tokens[this.t.currentToken];
                String message = "Left operand of '" + Token.tokens[this.t.currentToken] + "' needs parentheses";
                if (tok.equals("<") || tok.equals(">")) {
                    message = message + ". Or perhaps an XQuery element constructor appears where it is not allowed";
                }
                this.grumble(message);
            }
            lhs = this.makeBinaryExpression(lhs, operator, rhs);
            this.setLocation(lhs, offset);
        }
        return lhs;
    }

    private boolean allowMultipleOperators() {
        switch (this.t.currentToken) {
            case 6: 
            case 11: 
            case 12: 
            case 13: 
            case 14: 
            case 20: 
            case 22: 
            case 29: 
            case 38: 
            case 39: 
            case 50: 
            case 51: 
            case 52: 
            case 53: 
            case 54: 
            case 55: {
                return false;
            }
        }
        return true;
    }

    private int getCurrentOperatorPrecedence() {
        return XPathParser.operatorPrecedence(this.t.currentToken);
    }

    public static int operatorPrecedence(int operator) {
        switch (operator) {
            case 9: 
            case 81: {
                return 4;
            }
            case 10: 
            case 80: {
                return 5;
            }
            case 6: 
            case 11: 
            case 12: 
            case 13: 
            case 14: 
            case 20: 
            case 22: 
            case 38: 
            case 39: 
            case 50: 
            case 51: 
            case 52: 
            case 53: 
            case 54: 
            case 55: {
                return 6;
            }
            case 30: {
                return 7;
            }
            case 29: {
                return 8;
            }
            case 15: 
            case 16: {
                return 9;
            }
            case 17: 
            case 18: 
            case 19: 
            case 56: {
                return 10;
            }
            case 79: {
                return 11;
            }
            case 1: {
                return 12;
            }
            case 23: 
            case 24: {
                return 13;
            }
            case 45: {
                return 14;
            }
            case 47: {
                return 15;
            }
            case 57: {
                return 16;
            }
            case 46: {
                return 17;
            }
            case 77: {
                return 18;
            }
        }
        return -1;
    }

    private Expression makeBinaryExpression(Expression lhs, int operator, Expression rhs) throws XPathException {
        switch (operator) {
            case 9: {
                return new OrExpression(lhs, rhs);
            }
            case 10: {
                return new AndExpression(lhs, rhs);
            }
            case 50: 
            case 51: 
            case 52: 
            case 53: 
            case 54: 
            case 55: {
                return new ValueComparison(lhs, operator, rhs);
            }
            case 6: 
            case 11: 
            case 12: 
            case 13: 
            case 14: 
            case 22: {
                return this.env.getConfiguration().getTypeChecker(this.env.isInBackwardsCompatibleMode()).makeGeneralComparison(lhs, operator, rhs);
            }
            case 20: 
            case 38: 
            case 39: {
                return new IdentityComparison(lhs, operator, rhs);
            }
            case 29: {
                return new RangeExpression(lhs, rhs);
            }
            case 30: {
                if (!this.allowXPath30Syntax) {
                    this.grumble("Concatenation operator ('||') requires XPath 3.0 to be enabled");
                }
                RetainedStaticContext rsc = new RetainedStaticContext(this.env);
                if (lhs.isCallOn(Concat.class)) {
                    Expression[] args = ((SystemFunctionCall)lhs).getArguments();
                    Expression[] newArgs = new Expression[args.length + 1];
                    System.arraycopy(args, 0, newArgs, 0, args.length);
                    newArgs[args.length] = rhs;
                    return SystemFunction.makeCall("concat", rsc, newArgs);
                }
                return SystemFunction.makeCall("concat", rsc, lhs, rhs);
            }
            case 15: 
            case 16: 
            case 17: 
            case 18: 
            case 19: 
            case 56: {
                return this.env.getConfiguration().getTypeChecker(this.env.isInBackwardsCompatibleMode()).makeArithmeticExpression(lhs, operator, rhs);
            }
            case 79: {
                return this.makeOtherwiseExpression(lhs, rhs);
            }
            case 1: 
            case 23: 
            case 24: {
                return new VennExpression(lhs, operator, rhs);
            }
            case 81: {
                RetainedStaticContext rsc = new RetainedStaticContext(this.env);
                rhs = SystemFunction.makeCall("boolean", rsc, rhs);
                return Choose.makeConditional(lhs, Literal.makeLiteral(BooleanValue.TRUE), rhs);
            }
            case 80: {
                RetainedStaticContext rsc = new RetainedStaticContext(this.env);
                rhs = SystemFunction.makeCall("boolean", rsc, rhs);
                return Choose.makeConditional(lhs, rhs, Literal.makeLiteral(BooleanValue.FALSE));
            }
        }
        throw new IllegalArgumentException(Token.tokens[operator]);
    }

    private Expression makeOtherwiseExpression(Expression lhs, Expression rhs) {
        LetExpression let = new LetExpression();
        let.setVariableQName(new StructuredQName("vv", "http://ns.saxonica.com/anonymous-type", "n" + lhs.hashCode()));
        let.setSequence(lhs);
        let.setRequiredType(SequenceType.ANY_SEQUENCE);
        LocalVariableReference v1 = new LocalVariableReference(let.getVariableQName());
        v1.setBinding(let);
        let.addReference(v1, false);
        LocalVariableReference v2 = new LocalVariableReference(let.getVariableQName());
        v2.setBinding(let);
        let.addReference(v2, false);
        RetainedStaticContext rsc = new RetainedStaticContext(this.env);
        Expression[] conditions = new Expression[]{SystemFunction.makeCall("exists", rsc, v1), Literal.makeLiteral(BooleanValue.TRUE, lhs)};
        Expression[] actions = new Expression[]{v2, rhs};
        let.setAction(new Choose(conditions, actions));
        return let;
    }

    private Expression makeSequenceTypeExpression(Expression lhs, int operator, SequenceType type) {
        switch (operator) {
            case 45: {
                return new InstanceOfExpression(lhs, type);
            }
            case 47: {
                return TreatExpression.make(lhs, type);
            }
        }
        throw new IllegalArgumentException();
    }

    private Expression makeSingleTypeExpression(Expression lhs, int operator, CastingTarget type, boolean allowEmpty) throws XPathException {
        if (type instanceof AtomicType && type != ErrorType.getInstance()) {
            switch (operator) {
                case 57: {
                    CastableExpression castable = new CastableExpression(lhs, (AtomicType)type, allowEmpty);
                    if (lhs instanceof StringLiteral) {
                        castable.setOperandIsStringLiteral(true);
                    }
                    return castable;
                }
                case 46: {
                    CastExpression cast = new CastExpression(lhs, (AtomicType)type, allowEmpty);
                    if (lhs instanceof StringLiteral) {
                        cast.setOperandIsStringLiteral(true);
                    }
                    return cast;
                }
            }
            throw new IllegalArgumentException();
        }
        if (this.allowXPath30Syntax) {
            switch (operator) {
                case 57: {
                    if (type instanceof UnionType) {
                        NamespaceResolver resolver = this.env.getNamespaceResolver();
                        UnionCastableFunction ucf = new UnionCastableFunction((UnionType)type, resolver, allowEmpty);
                        return new StaticFunctionCall(ucf, new Expression[]{lhs});
                    }
                    if (!(type instanceof ListType)) break;
                    NamespaceResolver resolver = this.env.getNamespaceResolver();
                    ListCastableFunction lcf = new ListCastableFunction((ListType)type, resolver, allowEmpty);
                    return new StaticFunctionCall(lcf, new Expression[]{lhs});
                }
                case 46: {
                    if (type instanceof UnionType) {
                        NamespaceResolver resolver = this.env.getNamespaceResolver();
                        UnionConstructorFunction ucf = new UnionConstructorFunction((UnionType)type, resolver, allowEmpty);
                        return new StaticFunctionCall(ucf, new Expression[]{lhs});
                    }
                    if (!(type instanceof ListType)) break;
                    NamespaceResolver resolver = this.env.getNamespaceResolver();
                    ListConstructorFunction lcf = new ListConstructorFunction((ListType)type, resolver, allowEmpty);
                    return new StaticFunctionCall(lcf, new Expression[]{lhs});
                }
                default: {
                    throw new IllegalArgumentException();
                }
            }
            throw new XPathException("Cannot cast to " + type.getClass(), "XPST0051");
        }
        throw new XPathException("Casting to list or union types requires XPath 3.0 to be enabled", "XPST0051");
    }

    protected Expression parseTypeswitchExpression() throws XPathException {
        this.grumble("typeswitch is not allowed in XPath");
        return new ErrorExpression();
    }

    protected Expression parseSwitchExpression() throws XPathException {
        this.grumble("switch is not allowed in XPath");
        return new ErrorExpression();
    }

    protected Expression parseValidateExpression() throws XPathException {
        this.grumble("validate{} expressions are not allowed in XPath");
        return new ErrorExpression();
    }

    protected Expression parseExtensionExpression() throws XPathException {
        this.grumble("extension expressions (#...#) are not allowed in XPath");
        return new ErrorExpression();
    }

    protected Expression parseTryCatchExpression() throws XPathException {
        this.grumble("try/catch expressions are not allowed in XPath");
        return new ErrorExpression();
    }

    protected Expression parseFLWORExpression() throws XPathException {
        if (this.t.currentToken == 216 && !this.allowXPath30Syntax) {
            this.grumble("'let' is not permitted in XPath 2.0");
        }
        if (this.t.currentToken == 74 || this.t.currentToken == 73) {
            this.grumble("sliding/tumbling windows can only be used in XQuery");
        }
        int clauses = 0;
        int operator = this.t.currentToken;
        Assignation first = null;
        Assignation previous = null;
        do {
            Assignation v;
            int offset = this.t.currentTokenStartOffset;
            this.nextToken();
            this.expect(21);
            this.nextToken();
            this.expect(201);
            String var = this.t.currentTokenValue;
            if (operator == 211) {
                v = new ForExpression();
                v.setRequiredType(SequenceType.SINGLE_ITEM);
            } else {
                v = new LetExpression();
                v.setRequiredType(SequenceType.ANY_SEQUENCE);
            }
            ++clauses;
            this.setLocation(v, offset);
            v.setVariableQName(this.makeStructuredQName(var, ""));
            this.nextToken();
            this.expect(operator == 216 ? 58 : 31);
            this.nextToken();
            v.setSequence(this.parseExprSingle());
            this.declareRangeVariable(v);
            if (previous == null) {
                first = v;
            } else {
                previous.setAction(v);
            }
            previous = v;
        } while (this.t.currentToken == 7);
        this.expect(25);
        this.nextToken();
        previous.setAction(this.parseExprSingle());
        for (int i = 0; i < clauses; ++i) {
            this.undeclareRangeVariable();
        }
        return this.makeTracer(first, first.getVariableQName());
    }

    private Expression parseQuantifiedExpression() throws XPathException {
        int clauses = 0;
        int operator = this.t.currentToken;
        QuantifiedExpression first = null;
        Assignation previous = null;
        do {
            int offset = this.t.currentTokenStartOffset;
            this.nextToken();
            this.expect(21);
            this.nextToken();
            this.expect(201);
            String var = this.t.currentTokenValue;
            ++clauses;
            QuantifiedExpression v = new QuantifiedExpression();
            v.setRequiredType(SequenceType.SINGLE_ITEM);
            v.setOperator(operator);
            this.setLocation(v, offset);
            v.setVariableQName(this.makeStructuredQName(var, ""));
            this.nextToken();
            if (this.t.currentToken == 71 && this.language == ParsedLanguage.XQUERY) {
                this.nextToken();
                SequenceType type = this.parseSequenceType();
                if (type.getCardinality() != 16384) {
                    this.warning("Occurrence indicator on singleton range variable has no effect");
                    type = SequenceType.makeSequenceType(type.getPrimaryType(), 16384);
                }
                v.setRequiredType(type);
            }
            this.expect(31);
            this.nextToken();
            v.setSequence(this.parseExprSingle());
            this.declareRangeVariable(v);
            if (previous != null) {
                previous.setAction(v);
            } else {
                first = v;
            }
            previous = v;
        } while (this.t.currentToken == 7);
        this.expect(34);
        this.nextToken();
        previous.setAction(this.parseExprSingle());
        for (int i = 0; i < clauses; ++i) {
            this.undeclareRangeVariable();
        }
        return this.makeTracer(first, first.getVariableQName());
    }

    private Expression parseIfExpression() throws XPathException {
        int ifoffset = this.t.currentTokenStartOffset;
        this.nextToken();
        Expression condition = this.parseExpression();
        this.expect(204);
        this.nextToken();
        int thenoffset = this.t.currentTokenStartOffset;
        this.expect(26);
        this.nextToken();
        Expression thenExp = this.makeTracer(this.parseExprSingle(), null);
        int elseoffset = this.t.currentTokenStartOffset;
        this.expect(27);
        this.nextToken();
        Expression elseExp = this.makeTracer(this.parseExprSingle(), null);
        Expression ifExp = Choose.makeConditional(condition, thenExp, elseExp);
        this.setLocation(ifExp, ifoffset);
        return this.makeTracer(ifExp, null);
    }

    private ItemType getPlainType(String qname) throws XPathException {
        if (this.scanOnly) {
            return BuiltInAtomicType.STRING;
        }
        StructuredQName sq = null;
        try {
            sq = this.qNameParser.parse(qname, this.env.getDefaultElementNamespace());
        } catch (XPathException e) {
            this.grumble(e.getMessage(), e.getErrorCodeLocalPart());
            return null;
        }
        return this.getPlainType(sq);
    }

    public ItemType getPlainType(StructuredQName sq) throws XPathException {
        Configuration config = this.env.getConfiguration();
        String uri = sq.getURI();
        if (uri.isEmpty()) {
            uri = this.env.getDefaultElementNamespace();
        }
        String local = sq.getLocalPart();
        String qname = sq.getDisplayName();
        boolean builtInNamespace = uri.equals("http://www.w3.org/2001/XMLSchema");
        if (builtInNamespace) {
            ItemType t = Type.getBuiltInItemType(uri, local);
            if (t == null) {
                this.grumble("Unknown atomic type " + qname, "XPST0051");
                assert (false);
            }
            if (t instanceof BuiltInAtomicType) {
                this.checkAllowedType(this.env, (BuiltInAtomicType)t);
                return t;
            }
            if (t.isPlainType()) {
                return t;
            }
            this.grumble("The type " + qname + " is not atomic", "XPST0051");
            assert (false);
        } else {
            if (uri.equals("http://saxon.sf.net/java-type")) {
                Class theClass;
                try {
                    String className = JavaExternalObjectType.localNameToClassName(local);
                    theClass = config.getClass(className, false, null);
                } catch (XPathException err) {
                    this.grumble("Unknown Java class " + local, "XPST0051");
                    return AnyItemType.getInstance();
                }
                return config.getJavaExternalObjectType(theClass);
            }
            if (uri.equals("http://saxon.sf.net/clitype")) {
                return Version.platform.getExternalObjectType(config, uri, local);
            }
            SchemaType st = config.getSchemaType(sq);
            if (st == null) {
                this.grumble("Unknown simple type " + qname, "XPST0051");
            } else {
                if (st.isAtomicType()) {
                    if (!this.env.isImportedSchema(uri)) {
                        this.grumble("Atomic type " + qname + " exists, but its schema definition has not been imported", "XPST0051");
                    }
                    return (AtomicType)st;
                }
                if (st instanceof ItemType && ((ItemType)((Object)st)).isPlainType() && this.allowXPath30Syntax) {
                    if (!this.env.isImportedSchema(uri)) {
                        this.grumble("Type " + qname + " exists, but its schema definition has not been imported", "XPST0051");
                    }
                    return (ItemType)((Object)st);
                }
                if (st.isComplexType()) {
                    this.grumble("Type (" + qname + ") is a complex type", "XPST0051");
                    return BuiltInAtomicType.ANY_ATOMIC;
                }
                if (((SimpleType)st).isListType()) {
                    this.grumble("Type (" + qname + ") is a list type", "XPST0051");
                    return BuiltInAtomicType.ANY_ATOMIC;
                }
                if (this.allowXPath30Syntax) {
                    this.grumble("Type (" + qname + ") is a union type that cannot be used as an item type", "XPST0051");
                    return BuiltInAtomicType.ANY_ATOMIC;
                }
                this.grumble("The union type (" + qname + ") cannot be used as an item type unless XPath 3.0 is enabled", "XPST0051");
                return BuiltInAtomicType.ANY_ATOMIC;
            }
        }
        this.grumble("Unknown atomic type " + qname, "XPST0051");
        return BuiltInAtomicType.ANY_ATOMIC;
    }

    private void checkAllowedType(StaticContext env, BuiltInAtomicType type) throws XPathException {
        String s = XPathParser.whyDisallowedType(env.getPackageData(), type);
        if (s != null) {
            this.grumble(s, "XPST0080");
        }
    }

    public static String whyDisallowedType(PackageData pack, BuiltInAtomicType type) {
        if (!type.isAllowedInXSD10() && pack.getConfiguration().getXsdVersion() == 10) {
            return "The built-in atomic type " + type.getDisplayName() + " is not recognized unless XSD 1.1 is enabled";
        }
        return null;
    }

    private CastingTarget getSimpleType(String qname) throws XPathException {
        StructuredQName sq;
        block18: {
            if (this.scanOnly) {
                return BuiltInAtomicType.STRING;
            }
            sq = null;
            try {
                sq = this.qNameParser.parse(qname, this.env.getDefaultElementNamespace());
            } catch (XPathException e) {
                this.grumble(e.getMessage(), e.getErrorCodeLocalPart());
                if ($assertionsDisabled) break block18;
                throw new AssertionError();
            }
        }
        String uri = sq.getURI();
        String local = sq.getLocalPart();
        boolean builtInNamespace = uri.equals("http://www.w3.org/2001/XMLSchema");
        if (builtInNamespace) {
            SimpleType target = Type.getBuiltInSimpleType(uri, local);
            if (target == null) {
                this.grumble("Unknown simple type " + qname, this.allowXPath30Syntax ? "XQST0052" : "XPST0051");
            } else if (!(target instanceof CastingTarget)) {
                this.grumble("Unsuitable type for cast: " + target.getDescription(), "XPST0080");
            }
            CastingTarget t = (CastingTarget)((Object)target);
            if (t instanceof BuiltInAtomicType) {
                this.checkAllowedType(this.env, (BuiltInAtomicType)t);
            }
            return t;
        }
        if (uri.equals("http://saxon.sf.net/clitype")) {
            return (AtomicType)((Object)Version.platform.getExternalObjectType(this.env.getConfiguration(), uri, local));
        }
        SchemaType st = this.env.getConfiguration().getSchemaType(new StructuredQName("", uri, local));
        if (st == null) {
            if (this.allowXPath30Syntax) {
                this.grumble("Unknown simple type " + qname, "XQST0052");
            } else {
                this.grumble("Unknown simple type " + qname, "XPST0051");
            }
            return BuiltInAtomicType.ANY_ATOMIC;
        }
        if (this.allowXPath30Syntax) {
            if (!this.env.isImportedSchema(uri)) {
                this.grumble("Simple type " + qname + " exists, but its target namespace has not been imported in the static context");
            }
            return (CastingTarget)((Object)st);
        }
        if (st.isAtomicType()) {
            if (!this.env.isImportedSchema(uri)) {
                this.grumble("Atomic type " + qname + " exists, but its target namespace has not been imported in the static context");
            }
            return (AtomicType)st;
        }
        if (st.isComplexType()) {
            this.grumble("Cannot cast to a complex type (" + qname + ")", "XPST0051");
            return BuiltInAtomicType.ANY_ATOMIC;
        }
        if (((SimpleType)st).isListType()) {
            this.grumble("Casting to a list type (" + qname + ") requires XPath 3.0", "XPST0051");
            return BuiltInAtomicType.ANY_ATOMIC;
        }
        this.grumble("casting to a union type (" + qname + ") requires XPath 3.0", "XPST0051");
        return BuiltInAtomicType.ANY_ATOMIC;
    }

    public SequenceType parseSequenceType() throws XPathException {
        int occurrenceFlag;
        boolean disallowIndicator = this.t.currentTokenValue.equals("empty-sequence");
        ItemType primaryType = this.parseItemType();
        if (disallowIndicator) {
            return SequenceType.makeSequenceType(primaryType, 8192);
        }
        switch (this.t.currentToken) {
            case 17: 
            case 207: {
                occurrenceFlag = 57344;
                this.t.currentToken = 204;
                this.nextToken();
                break;
            }
            case 15: {
                occurrenceFlag = 49152;
                this.t.currentToken = 204;
                this.nextToken();
                break;
            }
            case 213: {
                occurrenceFlag = 24576;
                this.t.currentToken = 204;
                this.nextToken();
                break;
            }
            default: {
                occurrenceFlag = 16384;
            }
        }
        return SequenceType.makeSequenceType(primaryType, occurrenceFlag);
    }

    public ItemType parseItemType() throws XPathException {
        ItemType extended = this.parserExtension.parseExtendedItemType(this);
        return extended == null ? this.parseSimpleItemType() : extended;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private ItemType parseSimpleItemType() throws XPathException {
        if (this.t.currentToken == 5) {
            return this.parseParenthesizedItemType();
        }
        if (this.t.currentToken != 201) {
            if (this.t.currentToken == 69) {
                switch (this.t.currentTokenValue) {
                    case "item": {
                        this.nextToken();
                        this.expect(204);
                        this.nextToken();
                        return AnyItemType.getInstance();
                    }
                    case "function": {
                        this.checkLanguageVersion30();
                        AnnotationList annotations = AnnotationList.EMPTY;
                        return this.parseFunctionItemType(annotations);
                    }
                    case "map": {
                        return this.parseMapItemType();
                    }
                    case "array": {
                        return this.parseArrayItemType();
                    }
                    case "empty-sequence": {
                        this.nextToken();
                        this.expect(204);
                        this.nextToken();
                        return ErrorType.getInstance();
                    }
                    default: {
                        return this.parseKindTest();
                    }
                }
            }
            if (this.t.currentToken == 106) {
                AnnotationList annotations = this.parseAnnotationsList();
                if (this.t.currentTokenValue.equals("function")) {
                    return this.parseFunctionItemType(annotations);
                }
                this.grumble("Expected 'function' to follow annotation assertions, found " + Token.tokens[this.t.currentToken]);
                return null;
            }
            if (this.language == ParsedLanguage.EXTENDED_ITEM_TYPE && this.t.currentToken == 208) {
                String tokv = this.t.currentTokenValue;
                this.nextToken();
                return this.makeNamespaceTest((short)1, tokv);
            }
            if (this.language == ParsedLanguage.EXTENDED_ITEM_TYPE && this.t.currentToken == 70) {
                this.nextToken();
                this.expect(201);
                String tokv = this.t.currentTokenValue;
                this.nextToken();
                return this.makeLocalNameTest((short)1, tokv);
            }
            if (this.language == ParsedLanguage.EXTENDED_ITEM_TYPE && this.t.currentToken == 3) {
                this.nextToken();
                if (this.t.currentToken == 208) {
                    String tokv = this.t.currentTokenValue;
                    this.nextToken();
                    return this.makeNamespaceTest((short)2, tokv);
                }
                if (this.t.currentToken == 70) {
                    this.nextToken();
                    this.expect(201);
                    String tokv = this.t.currentTokenValue;
                    this.nextToken();
                    return this.makeLocalNameTest((short)2, tokv);
                }
                this.grumble("Expected NodeTest after '@'");
                return BuiltInAtomicType.ANY_ATOMIC;
            }
            this.grumble("Expected type name in SequenceType, found " + Token.tokens[this.t.currentToken]);
            return BuiltInAtomicType.ANY_ATOMIC;
        }
        ItemType primaryType = this.getPlainType(this.t.currentTokenValue);
        this.nextToken();
        return primaryType;
    }

    protected ItemType parseFunctionItemType(AnnotationList annotations) throws XPathException {
        return this.parserExtension.parseFunctionItemType(this, annotations);
    }

    protected ItemType parseMapItemType() throws XPathException {
        this.checkMapExtensions();
        Tokenizer t = this.getTokenizer();
        this.nextToken();
        if (t.currentToken == 207 || t.currentToken == 17) {
            this.nextToken();
            this.expect(204);
            this.nextToken();
            return MapType.ANY_MAP_TYPE;
        }
        ItemType keyType = this.parseItemType();
        this.expect(7);
        this.nextToken();
        SequenceType valueType = this.parseSequenceType();
        this.expect(204);
        this.nextToken();
        if (!(keyType instanceof AtomicType)) {
            this.grumble("Key type of a map must be atomic");
            return null;
        }
        return new MapType((AtomicType)keyType, valueType);
    }

    protected ItemType parseArrayItemType() throws XPathException {
        this.checkLanguageVersion31();
        Tokenizer t = this.getTokenizer();
        this.nextToken();
        if (t.currentToken == 207 || t.currentToken == 17) {
            this.nextToken();
            this.expect(204);
            this.nextToken();
            return ArrayItemType.ANY_ARRAY_TYPE;
        }
        SequenceType memberType = this.parseSequenceType();
        this.expect(204);
        this.nextToken();
        return new ArrayItemType(memberType);
    }

    private ItemType parseParenthesizedItemType() throws XPathException {
        if (!this.allowXPath30Syntax) {
            this.grumble("Parenthesized item types require 3.0 to be enabled");
        }
        this.nextToken();
        ItemType primaryType = this.parseItemType();
        while (primaryType instanceof NodeTest && this.language == ParsedLanguage.EXTENDED_ITEM_TYPE && this.t.currentToken != 204) {
            switch (this.t.currentToken) {
                case 1: 
                case 23: 
                case 24: {
                    int op = this.t.currentToken;
                    this.nextToken();
                    primaryType = new CombinedNodeTest((NodeTest)primaryType, op, (NodeTest)this.parseItemType());
                }
            }
        }
        this.expect(204);
        this.nextToken();
        return primaryType;
    }

    private Expression parseUnaryExpression() throws XPathException {
        Expression exp;
        switch (this.t.currentToken) {
            case 16: {
                this.nextToken();
                Expression operand = this.parseUnaryExpression();
                exp = this.makeUnaryExpression(299, operand);
                break;
            }
            case 15: {
                this.nextToken();
                Expression operand = this.parseUnaryExpression();
                exp = this.makeUnaryExpression(15, operand);
                break;
            }
            case 102: 
            case 103: 
            case 104: 
            case 105: {
                exp = this.parseValidateExpression();
                break;
            }
            case 218: {
                exp = this.parseExtensionExpression();
                break;
            }
            case 60: {
                if (this.t.currentTokenValue.equals("validate")) {
                    exp = this.parseValidateExpression();
                    break;
                }
            }
            default: {
                exp = this.parseSimpleMappingExpression();
            }
        }
        this.setLocation(exp);
        return exp;
    }

    private Expression makeUnaryExpression(int operator, Expression operand) {
        AtomicValue val;
        if (Literal.isAtomic(operand) && (val = (AtomicValue)((Literal)operand).getValue()) instanceof NumericValue) {
            if (this.env.isInBackwardsCompatibleMode()) {
                val = new DoubleValue(((NumericValue)val).getDoubleValue());
            }
            NumericValue value = operator == 299 ? ((NumericValue)val).negate() : (NumericValue)val;
            return Literal.makeLiteral(value);
        }
        return this.env.getConfiguration().getTypeChecker(this.env.isInBackwardsCompatibleMode()).makeArithmeticExpression(Literal.makeLiteral(Int64Value.ZERO), operator, operand);
    }

    protected boolean atStartOfRelativePath() {
        switch (this.t.currentToken) {
            case 3: 
            case 5: 
            case 21: 
            case 35: 
            case 36: 
            case 43: 
            case 61: 
            case 62: 
            case 63: 
            case 64: 
            case 69: 
            case 70: 
            case 201: 
            case 202: 
            case 205: 
            case 206: 
            case 207: 
            case 208: 
            case 209: 
            case 218: {
                return true;
            }
            case 60: {
                return this.t.currentTokenValue.equals("ordered") || this.t.currentTokenValue.equals("unordered");
            }
        }
        return false;
    }

    protected boolean disallowedAtStartOfRelativePath() {
        switch (this.t.currentToken) {
            case 45: 
            case 46: 
            case 47: 
            case 57: {
                return true;
            }
        }
        return false;
    }

    protected Expression parsePathExpression() throws XPathException {
        int offset = this.t.currentTokenStartOffset;
        switch (this.t.currentToken) {
            case 2: {
                this.nextToken();
                RootExpression start = new RootExpression();
                this.setLocation(start);
                if (this.disallowedAtStartOfRelativePath()) {
                    this.grumble("Operator '" + Token.tokens[this.t.currentToken] + "' is not allowed after '/'");
                }
                if (this.atStartOfRelativePath()) {
                    Expression path = this.parseRemainingPath(start);
                    this.setLocation(path, offset);
                    return path;
                }
                return start;
            }
            case 8: {
                this.nextToken();
                RootExpression start2 = new RootExpression();
                this.setLocation(start2, offset);
                AxisExpression axisExp = new AxisExpression(5, null);
                this.setLocation(axisExp, offset);
                Expression slashExp = ExpressionTool.makePathExpression(start2, axisExp);
                this.setLocation(slashExp, offset);
                Expression exp = this.parseRemainingPath(slashExp);
                this.setLocation(exp, offset);
                return exp;
            }
        }
        if (this.t.currentToken == 201 && (this.t.currentTokenValue.equals("true") || this.t.currentTokenValue.equals("false"))) {
            this.warning("The expression is looking for a child element named '" + this.t.currentTokenValue + "' - perhaps " + this.t.currentTokenValue + "() was intended? To avoid this warning, use child::" + this.t.currentTokenValue + " or ./" + this.t.currentTokenValue + ".");
        }
        if (this.t.currentToken == 201 && this.t.getBinaryOp(this.t.currentTokenValue) != -1 && this.language != ParsedLanguage.XSLT_PATTERN && (offset > 0 || this.t.peekAhead() != 0)) {
            String s = this.t.currentTokenValue;
            this.warning("The keyword '" + s + "' in this context means 'child::" + s + "'. If this was intended, use 'child::" + s + "' or './" + s + "' to avoid this warning.");
        }
        return this.parseRelativePath();
    }

    protected Expression parseSimpleMappingExpression() throws XPathException {
        int offset = this.t.currentTokenStartOffset;
        Expression exp = this.parsePathExpression();
        while (this.t.currentToken == 40) {
            if (!this.allowXPath30Syntax) {
                this.grumble("XPath '!' operator requires XPath 3.0 to be enabled");
            }
            this.nextToken();
            Expression next = this.parsePathExpression();
            exp = new ForEach(exp, next);
            this.setLocation(exp, offset);
        }
        return exp;
    }

    protected Expression parseRelativePath() throws XPathException {
        int offset = this.t.currentTokenStartOffset;
        Expression exp = this.parseStepExpression(this.language == ParsedLanguage.XSLT_PATTERN);
        while (this.t.currentToken == 2 || this.t.currentToken == 8) {
            int op = this.t.currentToken;
            this.nextToken();
            Expression next = this.parseStepExpression(false);
            if (op == 2) {
                exp = new HomogeneityChecker(new SlashExpression(exp, next));
            } else {
                AxisExpression ae = new AxisExpression(5, null);
                this.setLocation(ae, offset);
                Expression one = ExpressionTool.makePathExpression(exp, ae);
                this.setLocation(one, offset);
                exp = ExpressionTool.makePathExpression(one, next);
                exp = new HomogeneityChecker(exp);
            }
            this.setLocation(exp, offset);
        }
        return exp;
    }

    protected Expression parseRemainingPath(Expression start) throws XPathException {
        int offset = this.t.currentTokenStartOffset;
        Expression exp = start;
        int op = 2;
        while (true) {
            Expression next = this.parseStepExpression(false);
            if (op == 2) {
                exp = new HomogeneityChecker(new SlashExpression(exp, next));
            } else if (op == 8) {
                AxisExpression descOrSelf = new AxisExpression(5, null);
                this.setLocation(descOrSelf);
                Expression step = ExpressionTool.makePathExpression(descOrSelf, next);
                this.setLocation(step);
                exp = ExpressionTool.makePathExpression(exp, step);
                exp = new HomogeneityChecker(exp);
            } else {
                if (!this.allowXPath30Syntax) {
                    this.grumble("XPath '!' operator requires XPath 3.0 to be enabled");
                }
                exp = new ForEach(exp, next);
            }
            this.setLocation(exp, offset);
            op = this.t.currentToken;
            if (op != 2 && op != 8 && op != 40) break;
            this.nextToken();
        }
        return exp;
    }

    protected Expression parseStepExpression(boolean firstInPattern) throws XPathException {
        boolean reverse;
        Expression step = this.parseBasicStep(firstInPattern);
        boolean bl = reverse = step instanceof AxisExpression && !AxisInfo.isForwards[((AxisExpression)step).getAxis()];
        while (true) {
            if (this.t.currentToken == 4) {
                step = this.parsePredicate(step);
                continue;
            }
            if (this.t.currentToken == 5) {
                step = this.parseDynamicFunctionCall(step, null);
                this.setLocation(step);
                continue;
            }
            if (this.t.currentToken != 213) break;
            step = this.parseLookup(step);
            this.setLocation(step);
        }
        if (reverse) {
            RetainedStaticContext rsc = this.env.makeRetainedStaticContext();
            step = SystemFunction.makeCall("reverse", rsc, step);
            assert (step != null);
            return step;
        }
        return step;
    }

    protected Expression parsePredicate(Expression step) throws XPathException {
        this.nextToken();
        Expression predicate = this.parsePredicate();
        this.expect(203);
        this.nextToken();
        step = new FilterExpression(step, predicate);
        this.setLocation(step);
        return step;
    }

    protected Expression parseArrowPostfix(Expression lhs) throws XPathException {
        this.checkLanguageVersion31();
        this.nextToken();
        int token = this.getTokenizer().currentToken;
        if (token == 201 || token == 35) {
            return this.parseFunctionCall(lhs);
        }
        if (token == 21) {
            Expression var = this.parseVariableReference();
            this.expect(5);
            return this.parseDynamicFunctionCall(var, lhs);
        }
        if (token == 5) {
            Expression var = this.parseParenthesizedExpression();
            this.expect(5);
            return this.parseDynamicFunctionCall(var, lhs);
        }
        this.grumble("Unexpected " + Token.tokens[token] + " after '=>'");
        return null;
    }

    protected Expression parsePredicate() throws XPathException {
        return this.parseExpression();
    }

    protected boolean isReservedInQuery(String uri) {
        return NamespaceConstant.isReservedInQuery31(uri);
    }

    protected Expression parseBasicStep(boolean firstInPattern) throws XPathException {
        switch (this.t.currentToken) {
            case 21: {
                return this.parseVariableReference();
            }
            case 5: {
                return this.parseParenthesizedExpression();
            }
            case 4: {
                return this.parseArraySquareConstructor();
            }
            case 202: {
                return this.parseStringLiteral(true);
            }
            case 222: {
                return this.parseStringLiteral(true);
            }
            case 78: {
                return this.parseStringConstructor();
            }
            case 209: {
                return this.parseNumericLiteral(true);
            }
            case 35: {
                return this.parseFunctionCall(null);
            }
            case 213: {
                return this.parseLookup(new ContextItemExpression());
            }
            case 205: {
                this.nextToken();
                ContextItemExpression cie = new ContextItemExpression();
                this.setLocation(cie);
                return cie;
            }
            case 206: {
                this.nextToken();
                AxisExpression pne = new AxisExpression(9, null);
                this.setLocation(pne);
                return pne;
            }
            case 106: {
                AnnotationList annotations = this.parseAnnotationsList();
                if (!this.t.currentTokenValue.equals("function")) {
                    this.grumble("Expected 'function' to follow the annotation assertion");
                }
                annotations.check(this.env.getConfiguration(), "IF");
                return this.parseInlineFunction(annotations);
            }
            case 69: {
                if (this.t.currentTokenValue.equals("function")) {
                    AnnotationList annotations = AnnotationList.EMPTY;
                    return this.parseInlineFunction(annotations);
                }
            }
            case 70: 
            case 201: 
            case 207: 
            case 208: {
                int defaultAxis = 3;
                if (this.t.currentToken == 69 && (this.t.currentTokenValue.equals("attribute") || this.t.currentTokenValue.equals("schema-attribute"))) {
                    defaultAxis = 2;
                } else if (this.t.currentToken == 69 && this.t.currentTokenValue.equals("namespace-node")) {
                    defaultAxis = 8;
                    this.testPermittedAxis(8, "XQST0134");
                } else if (firstInPattern && this.t.currentToken == 69 && this.t.currentTokenValue.equals("document-node")) {
                    defaultAxis = 12;
                }
                NodeTest test = this.parseNodeTest((short)1);
                if (test instanceof AnyNodeTest) {
                    test = defaultAxis == 3 ? MultipleNodeKindTest.CHILD_NODE : NodeKindTest.ATTRIBUTE;
                }
                AxisExpression ae = new AxisExpression(defaultAxis, test);
                this.setLocation(ae);
                return ae;
            }
            case 3: {
                this.nextToken();
                switch (this.t.currentToken) {
                    case 69: 
                    case 70: 
                    case 201: 
                    case 207: 
                    case 208: {
                        AxisExpression ae2 = new AxisExpression(2, this.parseNodeTest((short)2));
                        this.setLocation(ae2);
                        return ae2;
                    }
                }
                this.grumble("@ must be followed by a NodeTest");
                break;
            }
            case 36: {
                int axis;
                try {
                    axis = AxisInfo.getAxisNumber(this.t.currentTokenValue);
                } catch (XPathException err) {
                    this.grumble(err.getMessage());
                    axis = 3;
                }
                this.testPermittedAxis(axis, "XPST0003");
                short principalNodeType = AxisInfo.principalNodeType[axis];
                this.nextToken();
                switch (this.t.currentToken) {
                    case 69: 
                    case 70: 
                    case 201: 
                    case 207: 
                    case 208: {
                        AxisExpression ax = new AxisExpression(axis, this.parseNodeTest(principalNodeType));
                        this.setLocation(ax);
                        return ax;
                    }
                }
                this.grumble("Unexpected token " + this.currentTokenDisplay() + " after axis name");
                break;
            }
            case 60: {
                switch (this.t.currentTokenValue) {
                    case "map": {
                        return this.parseMapExpression();
                    }
                    case "array": {
                        return this.parseArrayCurlyConstructor();
                    }
                    case "fn": 
                    case ".": {
                        return this.parserExtension.parseDotFunction(this);
                    }
                    case "_": {
                        return this.parserExtension.parseUnderscoreFunction(this);
                    }
                }
            }
            case 61: 
            case 62: 
            case 63: 
            case 64: 
            case 217: {
                return this.parseConstructor();
            }
            case 43: {
                return this.parseNamedFunctionReference();
            }
            default: {
                this.grumble("Unexpected token " + this.currentTokenDisplay() + " at start of expression");
            }
        }
        return new ErrorExpression();
    }

    public Expression parseParenthesizedExpression() throws XPathException {
        this.nextToken();
        if (this.t.currentToken == 204) {
            this.nextToken();
            return Literal.makeEmptySequence();
        }
        Expression seq = this.parseExpression();
        this.expect(204);
        this.nextToken();
        return seq;
    }

    protected void testPermittedAxis(int axis, String errorCode) throws XPathException {
        if (axis == 13) {
            this.grumble("The preceding-or-ancestor axis is for internal use only", errorCode);
        }
    }

    public Expression parseNumericLiteral(boolean traceable) throws XPathException {
        int offset = this.t.currentTokenStartOffset;
        NumericValue number = NumericValue.parseNumber(this.t.currentTokenValue);
        if (number.isNaN()) {
            this.grumble("Invalid numeric literal " + Err.wrap(this.t.currentTokenValue, 4));
        }
        this.nextToken();
        Literal lit = Literal.makeLiteral(number);
        this.setLocation(lit, offset);
        return traceable ? this.makeTracer(lit, null) : lit;
    }

    protected Expression parseStringLiteral(boolean traceable) throws XPathException {
        Literal literal = this.makeStringLiteral(this.t.currentTokenValue);
        this.nextToken();
        return traceable ? this.makeTracer(literal, null) : literal;
    }

    protected Expression parseStringConstructor() throws XPathException {
        this.grumble("String constructor expressions are allowed only in XQuery");
        return null;
    }

    public Expression parseVariableReference() throws XPathException {
        Expression ref;
        int offset = this.t.currentTokenStartOffset;
        this.nextToken();
        if (this.t.currentToken == 209) {
            return this.parserExtension.bindNumericParameterReference(this);
        }
        this.expect(201);
        String var = this.t.currentTokenValue;
        this.nextToken();
        if (this.scanOnly) {
            return new ContextItemExpression();
        }
        StructuredQName vtest = this.makeStructuredQName(var, "");
        assert (vtest != null);
        LocalBinding b = this.findRangeVariable(vtest);
        if (b != null) {
            ref = new LocalVariableReference(b);
        } else {
            if (this.catchDepth > 0) {
                for (StructuredQName errorVariable : StandardNames.errorVariables) {
                    if (!errorVariable.getLocalPart().equals(vtest.getLocalPart())) continue;
                    StructuredQName functionName = new StructuredQName("saxon", "http://saxon.sf.net/", "dynamic-error-info");
                    SymbolicName.F sn = new SymbolicName.F(functionName, 1);
                    Expression[] args = new Expression[]{new StringLiteral(vtest.getLocalPart())};
                    return VendorFunctionSetHE.getInstance().bind(sn, args, this.env, new ArrayList<String>());
                }
            }
            try {
                ref = this.env.bindVariable(vtest);
            } catch (XPathException err) {
                err.maybeSetLocation(this.makeLocation());
                throw err;
            }
        }
        this.setLocation(ref, offset);
        return ref;
    }

    protected Literal makeStringLiteral(String currentTokenValue) throws XPathException {
        StringLiteral literal = new StringLiteral(currentTokenValue);
        this.setLocation(literal);
        return literal;
    }

    protected CharSequence unescape(String token) throws XPathException {
        return token;
    }

    protected Expression parseConstructor() throws XPathException {
        this.grumble("Node constructor expressions are allowed only in XQuery, not in XPath");
        return new ErrorExpression();
    }

    public Expression parseDynamicFunctionCall(Expression functionItem, Expression prefixArgument) throws XPathException {
        this.checkLanguageVersion30();
        ArrayList<Expression> args = new ArrayList<Expression>(10);
        if (prefixArgument != null) {
            args.add(prefixArgument);
        }
        IntArraySet placeMarkers = null;
        this.nextToken();
        if (this.t.currentToken != 204) {
            while (true) {
                Expression arg;
                if ((arg = this.parseFunctionArgument()) == null) {
                    if (placeMarkers == null) {
                        placeMarkers = new IntArraySet();
                    }
                    placeMarkers.add(args.size());
                    arg = Literal.makeEmptySequence();
                }
                args.add(arg);
                if (this.t.currentToken != 7) break;
                this.nextToken();
            }
            this.expect(204);
        }
        this.nextToken();
        if (placeMarkers == null) {
            return this.generateApplyCall(functionItem, args);
        }
        return this.parserExtension.createDynamicCurriedFunction(this, functionItem, args, placeMarkers);
    }

    protected Expression generateApplyCall(Expression functionItem, ArrayList<Expression> args) throws XPathException {
        SquareArrayConstructor block = new SquareArrayConstructor(args);
        RetainedStaticContext rsc = new RetainedStaticContext(this.getStaticContext());
        SystemFunction fn = VendorFunctionSetHE.getInstance().makeFunction("apply", 2);
        fn.setRetainedStaticContext(rsc);
        Expression call = fn.makeFunctionCall(functionItem, block);
        ((ApplyFn)fn).setDynamicFunctionCall(functionItem.toShortString());
        this.setLocation(call, this.t.currentTokenStartOffset);
        return call;
    }

    protected Expression parseLookup(Expression lhs) throws XPathException {
        Expression result;
        this.checkLanguageVersion31();
        Tokenizer t = this.getTokenizer();
        int offset = t.currentTokenStartOffset;
        t.setState(1);
        t.currentToken = 5;
        this.nextToken();
        int token = t.currentToken;
        t.setState(3);
        if (token == 201) {
            String name = t.currentTokenValue;
            if (!NameChecker.isValidNCName(name)) {
                this.grumble("The name following '?' must be a valid NCName");
            }
            this.nextToken();
            result = this.lookupName(lhs, name);
        } else if (token == 209) {
            NumericValue number = NumericValue.parseNumber(t.currentTokenValue);
            if (!(number instanceof IntegerValue)) {
                this.grumble("Number following '?' must be an integer");
            }
            this.nextToken();
            result = XPathParser.lookup(this, lhs, Literal.makeLiteral(number));
        } else if (token == 17 || token == 207) {
            this.nextToken();
            result = XPathParser.lookupStar(lhs);
        } else if (token == 5) {
            result = XPathParser.lookup(this, lhs, this.parseParenthesizedExpression());
        } else if (token == 202) {
            this.checkSyntaxExtensions("string literal after '?'");
            result = this.lookupName(lhs, t.currentTokenValue);
            this.nextToken();
        } else if (token == 21) {
            this.checkSyntaxExtensions("variable reference after '?'");
            result = XPathParser.lookup(this, lhs, this.parseVariableReference());
            this.nextToken();
        } else {
            this.grumble("Unexpected " + Token.tokens[token] + " after '?'");
            return null;
        }
        this.setLocation(result, offset);
        return result;
    }

    private static Expression lookup(XPathParser parser, Expression lhs, Expression rhs) {
        return new LookupExpression(lhs, rhs);
    }

    private Expression lookupName(Expression lhs, String rhs) {
        return new LookupExpression(lhs, new StringLiteral(rhs));
    }

    private static Expression lookupStar(Expression lhs) {
        return new LookupAllExpression(lhs);
    }

    protected NodeTest parseNodeTest(short nodeType) throws XPathException {
        int tok = this.t.currentToken;
        String tokv = this.t.currentTokenValue;
        switch (tok) {
            case 201: {
                this.nextToken();
                return this.makeNameTest(nodeType, tokv, nodeType == 1);
            }
            case 208: {
                this.nextToken();
                return this.makeNamespaceTest(nodeType, tokv);
            }
            case 70: {
                this.nextToken();
                tokv = this.t.currentTokenValue;
                this.expect(201);
                this.nextToken();
                return this.makeLocalNameTest(nodeType, tokv);
            }
            case 207: {
                this.nextToken();
                return NodeKindTest.makeNodeKindTest(nodeType);
            }
            case 69: {
                return this.parseKindTest();
            }
        }
        this.grumble("Unrecognized node test");
        throw new XPathException("");
    }

    /*
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    private NodeTest parseKindTest() throws XPathException {
        pool = this.env.getConfiguration().getNamePool();
        typeName = this.t.currentTokenValue;
        schemaDeclaration = typeName.startsWith("schema-");
        primaryType = this.getSystemType(typeName);
        fp = -1;
        empty = false;
        this.nextToken();
        if (this.t.currentToken == 204) {
            if (schemaDeclaration) {
                this.grumble("schema-element() and schema-attribute() require a name to be supplied");
                return null;
            }
            empty = true;
            this.nextToken();
        }
        switch (primaryType) {
            case 88: {
                this.grumble("item() is not allowed in a path expression");
                return null;
            }
            case 0: {
                if (empty) {
                    return AnyNodeTest.getInstance();
                }
                this.grumble("Expected ')': no arguments are allowed in node()");
                return null;
            }
            case 3: {
                if (empty) {
                    return NodeKindTest.TEXT;
                }
                this.grumble("Expected ')': no arguments are allowed in text()");
                return null;
            }
            case 8: {
                if (empty) {
                    return NodeKindTest.COMMENT;
                }
                this.grumble("Expected ')': no arguments are allowed in comment()");
                return null;
            }
            case 13: {
                if (empty) {
                    if (!this.isNamespaceTestAllowed()) {
                        this.grumble("namespace-node() test is not allowed in XPath 2.0/XQuery 1.0");
                    }
                    return NodeKindTest.NAMESPACE;
                }
                if (this.language == ParsedLanguage.EXTENDED_ITEM_TYPE && this.t.currentToken == 201) {
                    nsName = this.t.currentTokenValue;
                    this.nextToken();
                    this.expect(204);
                    this.nextToken();
                    return new NameTest(13, "", nsName, pool);
                }
                this.grumble("No arguments are allowed in namespace-node()");
                return null;
            }
            case 9: {
                if (empty) {
                    return NodeKindTest.DOCUMENT;
                }
                try {
                    innerType = this.getSystemType(this.t.currentTokenValue);
                } catch (XPathException err) {
                    innerType = 88;
                }
                if (innerType != 1) {
                    this.grumble("Argument to document-node() must be an element type descriptor");
                    return null;
                }
                inner = this.parseKindTest();
                this.expect(204);
                this.nextToken();
                return new DocumentNodeTest(inner);
            }
            case 7: {
                if (empty) {
                    return NodeKindTest.PROCESSING_INSTRUCTION;
                }
                if (this.t.currentToken != 202) ** GOTO lbl71
                piName = Whitespace.trim(this.unescape(this.t.currentTokenValue));
                if (!NameChecker.isValidNCName(piName)) {
                    this.grumble("Processing instruction name must be a valid NCName", "XPTY0004");
                } else {
                    fp = pool.allocateFingerprint("", piName);
                }
                ** GOTO lbl83
lbl71:
                // 1 sources

                if (this.t.currentToken != 201) ** GOTO lbl82
                try {
                    parts = NameChecker.getQNameParts(this.t.currentTokenValue);
                    if (!parts[0].isEmpty()) ** GOTO lbl77
                    fp = pool.allocateFingerprint("", parts[1]);
                    ** GOTO lbl83
lbl77:
                    // 1 sources

                    this.grumble("Processing instruction name must not contain a colon");
                } catch (QNameException e) {
                    this.grumble("Invalid processing instruction name. " + e.getMessage());
                }
                ** GOTO lbl83
lbl82:
                // 1 sources

                this.grumble("Processing instruction name must be a QName or a string literal");
lbl83:
                // 6 sources

                this.nextToken();
                this.expect(204);
                this.nextToken();
                return new NameTest(7, fp, pool);
            }
            case 1: 
            case 2: {
                nodeName = "";
                nodeTest = null;
                if (empty) {
                    return NodeKindTest.makeNodeKindTest(primaryType);
                }
                if (this.t.currentToken == 207 || this.t.currentToken == 17) {
                    if (schemaDeclaration) {
                        this.grumble("schema-element() and schema-attribute() must specify an actual name, not '*'");
                        return null;
                    }
                    nodeTest = NodeKindTest.makeNodeKindTest(primaryType);
                    this.nextToken();
                } else if (this.t.currentToken == 201) {
                    nodeName = this.t.currentTokenValue;
                    fp = this.makeFingerprint(this.t.currentTokenValue, primaryType == 1);
                    this.nextToken();
                } else if ((this.t.currentToken == 208 || this.t.currentToken == 70) && this.allowSaxonExtensions) {
                    nodeTest = this.parseNodeTest((short)primaryType);
                } else {
                    this.grumble("Unexpected " + Token.tokens[this.t.currentToken] + " after '(' in SequenceType");
                }
                suri = null;
                if (fp != -1) {
                    suri = pool.getURI(fp);
                }
                if (this.t.currentToken == 204) {
                    this.nextToken();
                    if (fp == -1) {
                        return nodeTest;
                    }
                    if (primaryType == 2) {
                        if (schemaDeclaration) {
                            attributeDecl = this.env.getConfiguration().getAttributeDeclaration(fp & 1048575);
                            if (!this.env.isImportedSchema(suri)) {
                                this.grumble("No schema has been imported for namespace '" + suri + '\'', "XPST0008");
                            }
                            if (attributeDecl == null) {
                                this.grumble("There is no declaration for attribute @" + nodeName + " in an imported schema", "XPST0008");
                                return null;
                            }
                            return attributeDecl.makeSchemaNodeTest();
                        }
                        return new NameTest(2, fp, pool);
                    }
                    if (schemaDeclaration) {
                        if (!this.env.isImportedSchema(suri)) {
                            this.grumble("No schema has been imported for namespace '" + suri + '\'', "XPST0008");
                        }
                        if ((elementDecl = this.env.getConfiguration().getElementDeclaration(fp & 1048575)) == null) {
                            this.grumble("There is no declaration for element <" + nodeName + "> in an imported schema", "XPST0008");
                            return null;
                        }
                        return elementDecl.makeSchemaNodeTest();
                    }
                    return this.makeNameTest((short)1, nodeName, true);
                }
                if (this.t.currentToken == 7) {
                    if (schemaDeclaration) {
                        this.grumble("schema-element() and schema-attribute() must have one argument only");
                        return null;
                    }
                    this.nextToken();
                    if (this.t.currentToken == 207) {
                        this.grumble("'*' is no longer permitted as the second argument of element() and attribute()");
                        return null;
                    }
                    if (this.t.currentToken == 201) {
                        contentType = this.makeStructuredQName(this.t.currentTokenValue, this.env.getDefaultElementNamespace());
                        if (!XPathParser.$assertionsDisabled && contentType == null) {
                            throw new AssertionError();
                        }
                        uri = contentType.getURI();
                        lname = contentType.getLocalPart();
                        if (uri.equals("http://www.w3.org/2001/XMLSchema")) {
                            schemaType = this.env.getConfiguration().getSchemaType(contentType);
                        } else {
                            if (!this.env.isImportedSchema(uri)) {
                                this.grumble("No schema has been imported for namespace '" + uri + '\'', "XPST0008");
                            }
                            schemaType = this.env.getConfiguration().getSchemaType(contentType);
                        }
                        if (schemaType == null) {
                            this.grumble("Unknown type name " + contentType.getEQName(), "XPST0008");
                            return null;
                        }
                        if (primaryType == 2 && schemaType.isComplexType()) {
                            this.warning("An attribute cannot have a complex type");
                        }
                        typeTest = new ContentTypeTest(primaryType, schemaType, this.env.getConfiguration(), false);
                        if (fp == -1 && (nodeTest == null || nodeTest instanceof NodeKindTest)) {
                            result /* !! */  = typeTest;
                            if (primaryType == 2) {
                                this.nextToken();
                            } else {
                                this.nextToken();
                                if (this.t.currentToken == 213) {
                                    typeTest.setNillable(true);
                                    this.nextToken();
                                }
                            }
                        } else if (primaryType == 2) {
                            if (nodeTest == null) {
                                nodeTest = new NameTest(2, fp, pool);
                            }
                            result /* !! */  = schemaType == AnyType.getInstance() || schemaType == AnySimpleType.getInstance() ? nodeTest : new CombinedNodeTest(nodeTest, 23, typeTest);
                            this.nextToken();
                        } else {
                            if (nodeTest == null) {
                                nodeTest = new NameTest(1, fp, pool);
                            }
                            result /* !! */  = new CombinedNodeTest(nodeTest, 23, typeTest);
                            this.nextToken();
                            if (this.t.currentToken == 213) {
                                typeTest.setNillable(true);
                                this.nextToken();
                            }
                        }
                    } else {
                        this.grumble("Unexpected " + Token.tokens[this.t.currentToken] + " after ',' in SequenceType");
                        return null;
                    }
                    this.expect(204);
                    this.nextToken();
                    return result /* !! */ ;
                }
                this.grumble("Expected ')' or ',' in SequenceType");
                return null;
            }
        }
        this.grumble("Unknown node kind");
        return null;
    }

    protected boolean isNamespaceTestAllowed() {
        return this.allowXPath30Syntax;
    }

    private int getSystemType(String name) throws XPathException {
        if ("item".equals(name)) {
            return 88;
        }
        if ("document-node".equals(name)) {
            return 9;
        }
        if ("element".equals(name)) {
            return 1;
        }
        if ("schema-element".equals(name)) {
            return 1;
        }
        if ("attribute".equals(name)) {
            return 2;
        }
        if ("schema-attribute".equals(name)) {
            return 2;
        }
        if ("text".equals(name)) {
            return 3;
        }
        if ("comment".equals(name)) {
            return 8;
        }
        if ("processing-instruction".equals(name)) {
            return 7;
        }
        if ("namespace-node".equals(name)) {
            return 13;
        }
        if ("node".equals(name)) {
            return 0;
        }
        this.grumble("Unknown type " + name);
        return -1;
    }

    protected void checkLanguageVersion30() throws XPathException {
        if (!this.allowXPath30Syntax) {
            this.grumble("To use XPath 3.0 syntax, you must configure the XPath parser to handle it");
        }
    }

    protected void checkLanguageVersion31() throws XPathException {
        if (!this.allowXPath31Syntax) {
            this.grumble("The XPath parser is not configured to allow use of XPath 3.1 syntax");
        }
    }

    protected void checkMapExtensions() throws XPathException {
        if (!this.allowXPath31Syntax && !this.allowXPath30XSLTExtensions) {
            this.grumble("The XPath parser is not configured to allow use of the map syntax from XSLT 3.0 or XPath 3.1");
        }
    }

    public void checkSyntaxExtensions(String construct) throws XPathException {
        if (!this.allowSaxonExtensions) {
            this.grumble("Saxon XPath syntax extensions have not been enabled: " + construct + " is not allowed");
        }
    }

    protected Expression parseMapExpression() throws XPathException {
        Expression result;
        this.checkMapExtensions();
        Tokenizer t = this.getTokenizer();
        int offset = t.currentTokenStartOffset;
        ArrayList<Expression> entries = new ArrayList<Expression>();
        this.nextToken();
        if (t.currentToken != 215) {
            while (true) {
                Expression key = this.parseExprSingle();
                if (t.currentToken == 58) {
                    this.grumble("The ':=' notation is no longer accepted in map expressions: use ':' instead");
                }
                this.expect(76);
                this.nextToken();
                Expression value = this.parseExprSingle();
                Expression entry = key instanceof Literal && ((Literal)key).getValue() instanceof AtomicValue && value instanceof Literal ? Literal.makeLiteral(new SingleEntryMap((AtomicValue)((Literal)key).getValue(), ((Literal)value).getValue())) : MapFunctionSet.getInstance().makeFunction("entry", 2).makeFunctionCall(key, value);
                entries.add(entry);
                if (t.currentToken == 215) break;
                this.expect(7);
                this.nextToken();
            }
        }
        t.lookAhead();
        this.nextToken();
        switch (entries.size()) {
            case 0: {
                result = Literal.makeLiteral(new HashTrieMap());
                break;
            }
            case 1: {
                result = (Expression)entries.get(0);
                break;
            }
            default: {
                Expression[] entriesArray = new Expression[entries.size()];
                Block block = new Block(entries.toArray(entriesArray));
                DictionaryMap options = new DictionaryMap();
                options.initialPut("duplicates", new StringValue("reject"));
                options.initialPut("duplicates-error-code", new StringValue("XQDY0137"));
                result = MapFunctionSet.getInstance().makeFunction("merge", 2).makeFunctionCall(block, Literal.makeLiteral(options));
            }
        }
        this.setLocation(result, offset);
        return result;
    }

    protected Expression parseArraySquareConstructor() throws XPathException {
        this.checkLanguageVersion31();
        Tokenizer t = this.getTokenizer();
        int offset = t.currentTokenStartOffset;
        ArrayList<Expression> members = new ArrayList<Expression>();
        this.nextToken();
        if (t.currentToken == 203) {
            this.nextToken();
            SquareArrayConstructor block = new SquareArrayConstructor(members);
            this.setLocation(block, offset);
            return block;
        }
        while (true) {
            Expression member = this.parseExprSingle();
            members.add(member);
            if (t.currentToken != 7) break;
            this.nextToken();
        }
        if (t.currentToken != 203) {
            this.grumble("Expected ',' or ']', found " + Token.tokens[t.currentToken]);
            return new ErrorExpression();
        }
        this.nextToken();
        SquareArrayConstructor block = new SquareArrayConstructor(members);
        this.setLocation(block, offset);
        return block;
    }

    protected Expression parseArrayCurlyConstructor() throws XPathException {
        this.checkLanguageVersion31();
        Tokenizer t = this.getTokenizer();
        int offset = t.currentTokenStartOffset;
        this.nextToken();
        if (t.currentToken == 215) {
            t.lookAhead();
            this.nextToken();
            return Literal.makeLiteral(SimpleArrayItem.EMPTY_ARRAY);
        }
        Expression body = this.parseExpression();
        this.expect(215);
        t.lookAhead();
        this.nextToken();
        SystemFunction sf = ArrayFunctionSet.getInstance().makeFunction("_from-sequence", 1);
        Expression result = sf.makeFunctionCall(body);
        this.setLocation(result, offset);
        return result;
    }

    public Expression parseFunctionCall(Expression prefixArgument) throws XPathException {
        String fname = this.t.currentTokenValue;
        int offset = this.t.currentTokenStartOffset;
        ArrayList<Expression> args = new ArrayList<Expression>(10);
        if (prefixArgument != null) {
            args.add(prefixArgument);
        }
        StructuredQName functionName = this.resolveFunctionName(fname);
        IntArraySet placeMarkers = null;
        this.nextToken();
        if (this.t.currentToken != 204) {
            while (true) {
                Expression arg;
                if ((arg = this.parseFunctionArgument()) == null) {
                    if (placeMarkers == null) {
                        placeMarkers = new IntArraySet();
                    }
                    placeMarkers.add(args.size());
                    arg = Literal.makeEmptySequence();
                }
                args.add(arg);
                if (this.t.currentToken != 7) break;
                this.nextToken();
            }
            this.expect(204);
        }
        this.nextToken();
        if (this.scanOnly) {
            return new StringLiteral(StringValue.EMPTY_STRING);
        }
        Expression[] arguments = new Expression[args.size()];
        args.toArray(arguments);
        if (placeMarkers != null) {
            return this.parserExtension.makeCurriedFunction(this, offset, functionName, arguments, placeMarkers);
        }
        SymbolicName.F sn = new SymbolicName.F(functionName, args.size());
        ArrayList<String> reasons = new ArrayList<String>();
        Expression fcall = this.env.getFunctionLibrary().bind(sn, arguments, this.env, reasons);
        if (fcall == null) {
            return this.reportMissingFunction(offset, functionName, arguments, reasons);
        }
        if (this.language == ParsedLanguage.XSLT_PATTERN) {
            if (fcall.isCallOn(RegexGroup.class)) {
                return Literal.makeEmptySequence();
            }
            if (fcall instanceof CurrentGroupCall) {
                this.grumble("The current-group() function cannot be used in a pattern", "XTSE1060", offset);
                return new ErrorExpression();
            }
            if (fcall instanceof CurrentGroupingKeyCall) {
                this.grumble("The current-grouping-key() function cannot be used in a pattern", "XTSE1070", offset);
                return new ErrorExpression();
            }
            if (fcall.isCallOn(CurrentMergeGroup.class)) {
                this.grumble("The current-merge-group() function cannot be used in a pattern", "XTSE3470", offset);
                return new ErrorExpression();
            }
            if (fcall.isCallOn(CurrentMergeKey.class)) {
                this.grumble("The current-merge-key() function cannot be used in a pattern", "XTSE3500", offset);
                return new ErrorExpression();
            }
        }
        this.setLocation(fcall, offset);
        for (Expression argument : arguments) {
            if (fcall == argument || functionName.hasURI("http://saxonica.com/ns/globalJS")) continue;
            fcall.adoptChildExpression(argument);
        }
        return this.makeTracer(fcall, functionName);
    }

    public Expression reportMissingFunction(int offset, StructuredQName functionName, Expression[] arguments, List<String> reasons) throws XPathException {
        StringBuilder sb = new StringBuilder();
        sb.append("Cannot find a ").append(arguments.length).append("-argument function named ").append(functionName.getEQName()).append("()");
        Configuration config = this.env.getConfiguration();
        for (String reason : reasons) {
            sb.append(". ").append(reason);
        }
        if (config.getBooleanProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS)) {
            boolean existsWithDifferentArity = false;
            for (int i = 0; i < arguments.length + 5; ++i) {
                if (i == arguments.length) continue;
                SymbolicName.F sn = new SymbolicName.F(functionName, i);
                if (!this.env.getFunctionLibrary().isAvailable(sn)) continue;
                existsWithDifferentArity = true;
                break;
            }
            if (existsWithDifferentArity) {
                sb.append(". The namespace URI and local name are recognized, but the number of arguments is wrong");
            } else {
                String supplementary = XPathParser.getMissingFunctionExplanation(functionName, config);
                if (supplementary != null) {
                    sb.append(". ").append(supplementary);
                }
            }
        } else {
            sb.append(". External function calls have been disabled");
        }
        if (this.env.isInBackwardsCompatibleMode()) {
            return new ErrorExpression(sb.toString(), "XTDE1425", false);
        }
        this.grumble(sb.toString(), "XPST0017", offset);
        return null;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static String getMissingFunctionExplanation(StructuredQName functionName, Configuration config) {
        String actualURI = functionName.getURI();
        String similarNamespace = NamespaceConstant.findSimilarNamespace(actualURI);
        if (similarNamespace != null) {
            if (!similarNamespace.equals(actualURI)) return "Perhaps the intended namespace was '" + similarNamespace + "'";
            switch (similarNamespace) {
                case "http://www.w3.org/2005/xpath-functions": {
                    return null;
                }
                case "http://saxon.sf.net/": {
                    if (config.getEditionCode().equals("HE")) {
                        return "Saxon extension functions are not available under Saxon-HE";
                    }
                    if (config.isLicensedFeature(8)) return null;
                    return "Saxon extension functions require a Saxon-PE or Saxon-EE license";
                }
                case "http://www.w3.org/1999/XSL/Transform": {
                    if (!functionName.getLocalPart().equals("original")) return "There are no functions defined in the XSLT namespace";
                    return "Function name xsl:original is only available within an overriding function";
                }
            }
            return null;
        } else {
            if (actualURI.contains("java")) {
                if (config.getEditionCode().equals("HE")) {
                    return "Reflexive calls to Java methods are not available under Saxon-HE";
                }
                if (config.isLicensedFeature(8)) return "For diagnostics on calls to Java methods, use the -TJ command line option or set the Configuration property FeatureKeys.TRACE_EXTERNAL_FUNCTIONS";
                return "Reflexive calls to Java methods require a Saxon-PE or Saxon-EE license, and none was found";
            }
            if (!actualURI.startsWith("clitype:")) return null;
            if (config.getEditionCode().equals("HE")) {
                return "Reflexive calls to external .NET methods are not available under Saxon-HE";
            }
            if (config.isLicensedFeature(8)) return "For diagnostics on calls to .NET methods, use the -TJ command line option or call processor.SetProperty(\"http://saxon.sf.net/feature/trace-external-functions\", \"true\")";
            return "Reflexive calls to external .NET methods require a Saxon-PE or Saxon-EE license, and none was found";
        }
    }

    protected StructuredQName resolveFunctionName(String fname) throws XPathException {
        ItemType t;
        StructuredQName functionName;
        block4: {
            if (this.scanOnly) {
                return new StructuredQName("", "http://saxon.sf.net/", "dummy");
            }
            functionName = null;
            try {
                functionName = this.qNameParser.parse(fname, this.env.getDefaultFunctionNamespace());
            } catch (XPathException e) {
                this.grumble(e.getMessage(), e.getErrorCodeLocalPart());
                if ($assertionsDisabled) break block4;
                throw new AssertionError();
            }
        }
        if (functionName.hasURI("http://www.w3.org/2001/XMLSchema") && (t = Type.getBuiltInItemType(functionName.getURI(), functionName.getLocalPart())) instanceof BuiltInAtomicType) {
            this.checkAllowedType(this.env, (BuiltInAtomicType)t);
        }
        return functionName;
    }

    public Expression parseFunctionArgument() throws XPathException {
        int next;
        if (this.t.currentToken == 213 && ((next = this.t.peekAhead()) == 7 || next == 204)) {
            this.nextToken();
            return this.parserExtension.makeArgumentPlaceMarker(this);
        }
        return this.parseExprSingle();
    }

    protected Expression parseNamedFunctionReference() throws XPathException {
        return this.parserExtension.parseNamedFunctionReference(this);
    }

    protected AnnotationList parseAnnotationsList() throws XPathException {
        this.grumble("Inline functions are not allowed in Saxon-HE");
        return null;
    }

    protected Expression parseInlineFunction(AnnotationList annotations) throws XPathException {
        return this.parserExtension.parseInlineFunction(this, annotations);
    }

    protected Expression makeCurriedFunction(int offset, StructuredQName name, Expression[] args, IntSet placeMarkers) throws XPathException {
        this.grumble("Partial function application is not allowed in Saxon-HE");
        return new ErrorExpression();
    }

    protected static boolean isReservedFunctionName30(String name) {
        int x = Arrays.binarySearch(reservedFunctionNames30, name);
        return x >= 0;
    }

    public static boolean isReservedFunctionName31(String name) {
        int x = Arrays.binarySearch(reservedFunctionNames31, name);
        return x >= 0;
    }

    public Stack<LocalBinding> getRangeVariables() {
        return this.rangeVariables;
    }

    public void setRangeVariables(Stack<LocalBinding> variables) {
        this.rangeVariables = variables;
    }

    public void declareRangeVariable(LocalBinding declaration) {
        this.rangeVariables.push(declaration);
    }

    public void undeclareRangeVariable() {
        this.rangeVariables.pop();
    }

    protected LocalBinding findRangeVariable(StructuredQName qName) {
        for (int v = this.rangeVariables.size() - 1; v >= 0; --v) {
            LocalBinding b = (LocalBinding)this.rangeVariables.elementAt(v);
            if (!b.getVariableQName().equals(qName)) continue;
            return b;
        }
        return this.parserExtension.findOuterRangeVariable(this, qName);
    }

    public void setRangeVariableStack(Stack<LocalBinding> stack) {
        this.rangeVariables = stack;
    }

    public final int makeFingerprint(String qname, boolean useDefault) throws XPathException {
        if (this.scanOnly) {
            return 386;
        }
        try {
            String defaultNS = useDefault ? this.env.getDefaultElementNamespace() : "";
            StructuredQName sq = this.qNameParser.parse(qname, defaultNS);
            return this.env.getConfiguration().getNamePool().allocateFingerprint(sq.getURI(), sq.getLocalPart());
        } catch (XPathException e) {
            this.grumble(e.getMessage(), e.getErrorCodeLocalPart());
            return -1;
        }
    }

    public final StructuredQName makeStructuredQNameSilently(String qname, String defaultUri) throws XPathException {
        if (this.scanOnly) {
            return new StructuredQName("", "http://saxon.sf.net/", "dummy");
        }
        return this.qNameParser.parse(qname, defaultUri);
    }

    public final StructuredQName makeStructuredQName(String qname, String defaultUri) throws XPathException {
        try {
            return this.makeStructuredQNameSilently(qname, defaultUri);
        } catch (XPathException err) {
            this.grumble(err.getMessage(), err.getErrorCodeLocalPart());
            return new StructuredQName("", "", "error");
        }
    }

    public final NodeName makeNodeName(String qname, boolean useDefault) throws XPathException {
        StructuredQName sq = this.makeStructuredQNameSilently(qname, useDefault ? this.env.getDefaultElementNamespace() : "");
        String prefix = sq.getPrefix();
        String uri = sq.getURI();
        String local = sq.getLocalPart();
        if (uri.isEmpty()) {
            int fp = this.env.getConfiguration().getNamePool().allocateFingerprint("", local);
            return new NoNamespaceName(local, fp);
        }
        int fp = this.env.getConfiguration().getNamePool().allocateFingerprint(uri, local);
        return new FingerprintedQName(prefix, uri, local, fp);
    }

    public NodeTest makeNameTest(short nodeType, String qname, boolean useDefault) throws XPathException {
        NamePool pool = this.env.getConfiguration().getNamePool();
        String defaultNS = "";
        if (useDefault && nodeType == 1 && !qname.startsWith("Q{") && !qname.contains(":")) {
            UnprefixedElementMatchingPolicy policy = this.env.getUnprefixedElementMatchingPolicy();
            switch (policy) {
                case DEFAULT_NAMESPACE: {
                    defaultNS = this.env.getDefaultElementNamespace();
                    break;
                }
                case DEFAULT_NAMESPACE_OR_NONE: {
                    defaultNS = this.env.getDefaultElementNamespace();
                    StructuredQName q = this.makeStructuredQName(qname, defaultNS);
                    int fp1 = pool.allocateFingerprint(q.getURI(), q.getLocalPart());
                    NameTest test1 = new NameTest((int)nodeType, fp1, pool);
                    int fp2 = pool.allocateFingerprint("", q.getLocalPart());
                    NameTest test2 = new NameTest((int)nodeType, fp2, pool);
                    return new CombinedNodeTest(test1, 1, test2);
                }
                case ANY_NAMESPACE: {
                    if (!NameChecker.isValidNCName(qname)) {
                        this.grumble("Invalid name '" + qname + "'");
                    }
                    return new LocalNameTest(pool, nodeType, qname);
                }
            }
        }
        StructuredQName q = this.makeStructuredQName(qname, defaultNS);
        int fp = pool.allocateFingerprint(q.getURI(), q.getLocalPart());
        return new NameTest((int)nodeType, fp, pool);
    }

    public QNameTest makeQNameTest(short nodeType, String qname) throws XPathException {
        NamePool pool = this.env.getConfiguration().getNamePool();
        StructuredQName q = this.makeStructuredQName(qname, "");
        assert (q != null);
        int fp = pool.allocateFingerprint(q.getURI(), q.getLocalPart());
        return new NameTest((int)nodeType, fp, pool);
    }

    public NamespaceTest makeNamespaceTest(short nodeType, String prefix) throws XPathException {
        NamePool pool = this.env.getConfiguration().getNamePool();
        if (this.scanOnly) {
            return new NamespaceTest(pool, nodeType, "http://saxon.sf.net/");
        }
        if (prefix.startsWith("Q{")) {
            String uri = prefix.substring(2, prefix.length() - 2);
            return new NamespaceTest(pool, nodeType, uri);
        }
        try {
            StructuredQName sq = this.qNameParser.parse(prefix + ":dummy", "");
            return new NamespaceTest(pool, nodeType, sq.getURI());
        } catch (XPathException err) {
            this.grumble(err.getMessage(), err.getErrorCodeLocalPart());
            return null;
        }
    }

    public LocalNameTest makeLocalNameTest(short nodeType, String localName) throws XPathException {
        if (!NameChecker.isValidNCName(localName)) {
            this.grumble("Local name [" + localName + "] contains invalid characters");
        }
        return new LocalNameTest(this.env.getConfiguration().getNamePool(), nodeType, localName);
    }

    protected void setLocation(Expression exp) {
        this.setLocation(exp, this.t.currentTokenStartOffset);
    }

    public void setLocation(Expression exp, int offset) {
        if (exp != null && (exp.getLocation() == null || exp.getLocation() == Loc.NONE)) {
            exp.setLocation(this.makeLocation(offset));
        }
    }

    public Location makeLocation(int offset) {
        int line = this.t.getLineNumber(offset);
        int column = this.t.getColumnNumber(offset);
        return this.makeNestedLocation(this.env.getContainingLocation(), line, column, null);
    }

    public void setLocation(Clause clause, int offset) {
        int line = this.t.getLineNumber(offset);
        int column = this.t.getColumnNumber(offset);
        Location loc = this.makeNestedLocation(this.env.getContainingLocation(), line, column, null);
        clause.setLocation(loc);
        clause.setPackageData(this.env.getPackageData());
    }

    public Location makeLocation() {
        if (this.t.getLineNumber() == this.mostRecentLocation.getLineNumber() && this.t.getColumnNumber() == this.mostRecentLocation.getColumnNumber() && (this.env.getSystemId() == null && this.mostRecentLocation.getSystemId() == null || this.env.getSystemId().equals(this.mostRecentLocation.getSystemId()))) {
            return this.mostRecentLocation;
        }
        int line = this.t.getLineNumber();
        int column = this.t.getColumnNumber();
        this.mostRecentLocation = this.makeNestedLocation(this.env.getContainingLocation(), line, column, null);
        return this.mostRecentLocation;
    }

    public Location makeNestedLocation(Location containingLoc, int line, int column, String nearbyText) {
        if (containingLoc instanceof Loc && containingLoc.getLineNumber() <= 1 && containingLoc.getColumnNumber() == -1 && nearbyText == null) {
            return new Loc(this.env.getSystemId(), line + 1, column + 1);
        }
        return new NestedLocation(containingLoc, line, column, nearbyText);
    }

    public Expression makeTracer(Expression exp, StructuredQName qName) {
        exp.setRetainedStaticContextLocally(this.env.makeRetainedStaticContext());
        if (this.codeInjector != null) {
            return this.codeInjector.inject(exp);
        }
        return exp;
    }

    protected boolean isKeyword(String s) {
        return this.t.currentToken == 201 && this.t.currentTokenValue.equals(s);
    }

    public void setScanOnly(boolean scanOnly) {
        this.scanOnly = scanOnly;
    }

    public void setAllowAbsentExpression(boolean allowEmpty) {
        this.allowAbsentExpression = allowEmpty;
    }

    public boolean isAllowAbsentExpression(boolean allowEmpty) {
        return this.allowAbsentExpression;
    }

    public static class NestedLocation
    implements Location {
        private final Location containingLocation;
        private final int localLineNumber;
        private final int localColumnNumber;
        private String nearbyText;

        public NestedLocation(Location containingLocation, int localLineNumber, int localColumnNumber) {
            this.containingLocation = containingLocation.saveLocation();
            this.localLineNumber = localLineNumber;
            this.localColumnNumber = localColumnNumber;
        }

        public NestedLocation(Location containingLocation, int localLineNumber, int localColumnNumber, String nearbyText) {
            this.containingLocation = containingLocation.saveLocation();
            this.localLineNumber = localLineNumber;
            this.localColumnNumber = localColumnNumber;
            this.nearbyText = nearbyText;
        }

        public Location getContainingLocation() {
            return this.containingLocation;
        }

        @Override
        public int getColumnNumber() {
            return this.localColumnNumber;
        }

        @Override
        public String getSystemId() {
            return this.containingLocation.getSystemId();
        }

        @Override
        public String getPublicId() {
            return this.containingLocation.getPublicId();
        }

        public int getLocalLineNumber() {
            return this.localLineNumber;
        }

        @Override
        public int getLineNumber() {
            return this.containingLocation.getLineNumber() + this.localLineNumber;
        }

        public String getNearbyText() {
            return this.nearbyText;
        }

        @Override
        public Location saveLocation() {
            return this;
        }
    }

    public static interface Accelerator {
        public Expression parse(Tokenizer var1, StaticContext var2, String var3, int var4, int var5);
    }

    public static enum ParsedLanguage {
        XPATH,
        XSLT_PATTERN,
        SEQUENCE_TYPE,
        XQUERY,
        EXTENDED_ITEM_TYPE;

    }
}


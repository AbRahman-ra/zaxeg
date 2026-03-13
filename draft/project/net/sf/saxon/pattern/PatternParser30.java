/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import net.sf.saxon.expr.AndExpression;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.InstanceOfExpression;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.LocalVariableReference;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.ValueComparison;
import net.sf.saxon.expr.VennExpression;
import net.sf.saxon.expr.instruct.Choose;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Tokenizer;
import net.sf.saxon.expr.parser.XPathParser;
import net.sf.saxon.functions.Doc;
import net.sf.saxon.functions.KeyFn;
import net.sf.saxon.functions.Root_1;
import net.sf.saxon.functions.SuperId;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.BooleanExpressionPattern;
import net.sf.saxon.pattern.ItemTypePattern;
import net.sf.saxon.pattern.NodeTestPattern;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.pattern.PatternMaker;
import net.sf.saxon.pattern.PatternParser;
import net.sf.saxon.pattern.UnionPattern;
import net.sf.saxon.pattern.UniversalPattern;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.NumericType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.SequenceType;

public class PatternParser30
extends XPathParser
implements PatternParser {
    int inPredicate = 0;

    @Override
    public Pattern parsePattern(String pattern, StaticContext env) throws XPathException {
        String[] branches;
        Pattern pat;
        this.env = env;
        this.charChecker = env.getConfiguration().getValidCharacterChecker();
        this.language = XPathParser.ParsedLanguage.XSLT_PATTERN;
        String trimmed = pattern.trim();
        if (trimmed.startsWith("(:")) {
            this.t = new Tokenizer();
            this.t.languageLevel = 30;
            this.t.tokenize(trimmed, 0, -1);
            int start = this.t.currentTokenStartOffset;
            trimmed = trimmed.substring(start);
        }
        this.allowSaxonExtensions = env.getConfiguration().getBooleanProperty(Feature.ALLOW_SYNTAX_EXTENSIONS);
        if (this.isSelectionPattern(trimmed)) {
            Expression e = this.parse(pattern, 0, 0, env);
            if (e instanceof Pattern) {
                return (Pattern)e;
            }
            if (e instanceof ContextItemExpression) {
                return new UniversalPattern();
            }
            if (e instanceof FilterExpression) {
                Expression predicate = null;
                while (e instanceof FilterExpression) {
                    Expression filter = ((FilterExpression)e).getActionExpression();
                    e = ((FilterExpression)e).getSelectExpression();
                    ItemType filterType = filter.getItemType();
                    TypeHierarchy th = env.getConfiguration().getTypeHierarchy();
                    Affinity rel = th.relationship(filterType, NumericType.getInstance());
                    if (rel != Affinity.DISJOINT) {
                        if (rel == Affinity.SAME_TYPE || rel == Affinity.SUBSUMED_BY) {
                            filter = new ValueComparison(filter, 50, Literal.makeLiteral(Int64Value.PLUS_ONE));
                        } else {
                            LetExpression let = new LetExpression();
                            StructuredQName varName = new StructuredQName("vv", "http://saxon.sf.net/generated-variable", "v" + filter.hashCode());
                            let.setVariableQName(varName);
                            InstanceOfExpression condition = new InstanceOfExpression(new LocalVariableReference(let), SequenceType.SINGLE_NUMERIC);
                            LocalVariableReference ref = new LocalVariableReference(let);
                            ref.setStaticType(SequenceType.SINGLE_NUMERIC, null, 0);
                            ValueComparison comparison = new ValueComparison(ref, 50, Literal.makeLiteral(Int64Value.PLUS_ONE));
                            Choose choice = new Choose(new Expression[]{condition, Literal.makeLiteral(BooleanValue.TRUE)}, new Expression[]{comparison, new LocalVariableReference(let)});
                            let.setSequence(filter);
                            let.setAction(choice);
                            let.setRequiredType(SequenceType.ANY_SEQUENCE);
                            let.setRetainedStaticContext(env.makeRetainedStaticContext());
                            filter = let;
                        }
                    }
                    if (predicate == null) {
                        predicate = filter;
                        continue;
                    }
                    predicate = new AndExpression(filter, predicate);
                }
                if (e instanceof ContextItemExpression) {
                    return new BooleanExpressionPattern(predicate);
                }
            }
            this.grumble("Pattern starting with '.' must be followed by a sequence of predicates");
            return null;
        }
        Expression exp = this.parse(pattern, 0, 0, env);
        exp.setRetainedStaticContext(env.makeRetainedStaticContext());
        if (exp instanceof VennExpression) {
            this.checkNoPredicatePattern(((VennExpression)exp).getLhsExpression());
            this.checkNoPredicatePattern(((VennExpression)exp).getRhsExpression());
        }
        ExpressionVisitor visitor = ExpressionVisitor.make(env);
        visitor.setOptimizeForPatternMatching(true);
        ContextItemStaticInfo cit = visitor.getConfiguration().makeContextItemStaticInfo(AnyNodeTest.getInstance(), true);
        try {
            pat = PatternMaker.fromExpression(exp.simplify().typeCheck(visitor, cit), env.getConfiguration(), true);
        } catch (XPathException e) {
            pat = PatternMaker.fromExpression(exp.simplify(), env.getConfiguration(), true);
        }
        pat.setOriginalText(pattern);
        if (pat instanceof UnionPattern && (branches = pattern.split("\\|")).length == 2) {
            ((UnionPattern)pat).p1.setOriginalText(branches[0]);
            ((UnionPattern)pat).p2.setOriginalText(branches[1]);
        }
        if (!(!(exp instanceof FilterExpression) || !(((FilterExpression)exp).getBase() instanceof ContextItemExpression) || this.allowSaxonExtensions && (pattern.startsWith("tuple") || pattern.startsWith("map") || pattern.startsWith("array") || pattern.startsWith("union")))) {
            this.grumble("A predicatePattern can appear only at the outermost level (parentheses not allowed)");
        }
        if (exp instanceof FilterExpression && pat instanceof NodeTestPattern) {
            pat.setPriority(0.5);
        }
        return pat;
    }

    private boolean isSelectionPattern(String pattern) throws XPathException {
        if (pattern.startsWith(".")) {
            return true;
        }
        if (pattern.matches("^(type|tuple|map|array|union|atomic)\\s*\\(.+")) {
            this.checkSyntaxExtensions("Patterns matching " + pattern.replace("\\(.*$", "") + " types");
            return true;
        }
        return false;
    }

    private void checkNoPredicatePattern(Expression exp) throws XPathException {
        if (exp instanceof ContextItemExpression) {
            this.grumble("A predicatePattern can appear only at the outermost level (union operator not allowed)");
        }
        if (exp instanceof FilterExpression) {
            this.checkNoPredicatePattern(((FilterExpression)exp).getBase());
        }
        if (exp instanceof VennExpression) {
            this.checkNoPredicatePattern(((VennExpression)exp).getLhsExpression());
            this.checkNoPredicatePattern(((VennExpression)exp).getRhsExpression());
        }
    }

    @Override
    protected void customizeTokenizer(Tokenizer t) {
    }

    @Override
    public Expression parseExpression() throws XPathException {
        Tokenizer t = this.getTokenizer();
        if (this.inPredicate > 0) {
            return super.parseExpression();
        }
        if (this.allowSaxonExtensions && t.currentToken == 69 && (t.currentTokenValue.equals("tuple") || t.currentTokenValue.equals("type") || t.currentTokenValue.equals("map") || t.currentTokenValue.equals("array"))) {
            ItemType type = this.parseItemType();
            Pattern expr = new ItemTypePattern(type);
            expr.setRetainedStaticContext(this.env.makeRetainedStaticContext());
            this.setLocation(expr);
            while (t.currentToken == 4) {
                expr = this.parsePredicate(expr).toPattern(this.env.getConfiguration());
            }
            return expr;
        }
        if (this.allowSaxonExtensions && t.currentToken == 69 && t.currentTokenValue.equals("atomic")) {
            this.nextToken();
            this.expect(201);
            StructuredQName typeName = this.makeStructuredQName(t.currentTokenValue, this.env.getDefaultElementNamespace());
            this.nextToken();
            this.expect(204);
            this.nextToken();
            SchemaType type = this.env.getConfiguration().getSchemaType(typeName);
            if (type == null || !type.isAtomicType()) {
                this.grumble("Unknown atomic type " + typeName);
            }
            AtomicType at = (AtomicType)type;
            Expression expr = new ItemTypePattern(at);
            this.setLocation(expr);
            while (t.currentToken == 4) {
                expr = this.parsePredicate(expr);
            }
            return expr;
        }
        return this.parseBinaryExpression(this.parsePathExpression(), 10);
    }

    @Override
    protected Expression parseBasicStep(boolean firstInPattern) throws XPathException {
        if (this.inPredicate > 0) {
            return super.parseBasicStep(firstInPattern);
        }
        switch (this.t.currentToken) {
            case 21: {
                if (!firstInPattern) {
                    this.grumble("In an XSLT 3.0 pattern, a variable reference is allowed only as the first step in a path");
                    return null;
                }
                return super.parseBasicStep(firstInPattern);
            }
            case 43: 
            case 60: 
            case 61: 
            case 62: 
            case 63: 
            case 64: 
            case 202: 
            case 206: 
            case 209: 
            case 217: {
                this.grumble("Token " + this.currentTokenDisplay() + " not allowed here in an XSLT pattern");
                return null;
            }
            case 35: {
                if (!firstInPattern) {
                    this.grumble("In an XSLT pattern, a function call is allowed only as the first step in a path");
                }
                return super.parseBasicStep(firstInPattern);
            }
            case 69: {
                switch (this.t.currentTokenValue) {
                    case "type": 
                    case "tuple": 
                    case "union": 
                    case "map": 
                    case "array": 
                    case "atomic": {
                        return this.parserExtension.parseTypePattern(this);
                    }
                }
                return super.parseBasicStep(firstInPattern);
            }
        }
        return super.parseBasicStep(firstInPattern);
    }

    @Override
    protected void testPermittedAxis(int axis, String errorCode) throws XPathException {
        super.testPermittedAxis(axis, errorCode);
        if (this.inPredicate == 0 && !AxisInfo.isSubtreeAxis[axis]) {
            this.grumble("The " + AxisInfo.axisName[axis] + " is not allowed in a pattern");
        }
    }

    @Override
    protected Expression parsePredicate() throws XPathException {
        boolean disallow = this.t.disallowUnionKeyword;
        this.t.disallowUnionKeyword = false;
        ++this.inPredicate;
        Expression exp = this.parseExpression();
        --this.inPredicate;
        this.t.disallowUnionKeyword = disallow;
        return exp;
    }

    @Override
    public Expression parseFunctionCall(Expression prefixArgument) throws XPathException {
        Expression fn = super.parseFunctionCall(prefixArgument);
        if (!(this.inPredicate > 0 || fn.isCallOn(SuperId.class) || fn.isCallOn(KeyFn.class) || fn.isCallOn(Doc.class) || fn.isCallOn(Root_1.class))) {
            this.grumble("The " + fn.toString() + " function is not allowed at the head of a pattern");
        }
        return fn;
    }

    @Override
    public Expression parseFunctionArgument() throws XPathException {
        if (this.inPredicate > 0) {
            return super.parseFunctionArgument();
        }
        switch (this.t.currentToken) {
            case 21: {
                return this.parseVariableReference();
            }
            case 202: {
                return this.parseStringLiteral(true);
            }
            case 209: {
                return this.parseNumericLiteral(true);
            }
        }
        this.grumble("A function argument in an XSLT pattern must be a variable reference or literal");
        return null;
    }

    @Override
    public Expression makeTracer(Expression exp, StructuredQName qName) {
        return exp;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.LocalBinding;
import net.sf.saxon.expr.LocalVariableReference;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.instruct.UserFunctionParameter;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.expr.parser.Tokenizer;
import net.sf.saxon.expr.parser.XPathParser;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.functions.hof.FunctionLiteral;
import net.sf.saxon.functions.hof.PartialApply;
import net.sf.saxon.functions.hof.UnresolvedXQueryFunctionItem;
import net.sf.saxon.functions.hof.UserFunctionReference;
import net.sf.saxon.functions.registry.BuiltInFunctionSet;
import net.sf.saxon.functions.registry.XPath31FunctionSet;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.query.XQueryFunction;
import net.sf.saxon.query.XQueryParser;
import net.sf.saxon.style.ExpressionContext;
import net.sf.saxon.style.SourceBinding;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.VisibilityProvenance;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyFunctionType;
import net.sf.saxon.type.AnyFunctionTypeWithAssertions;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.z.IntIterator;
import net.sf.saxon.z.IntSet;

public class ParserExtension {
    protected Stack<InlineFunctionDetails> inlineFunctionStack = new Stack();

    private void needExtension(XPathParser p, String what) throws XPathException {
        p.grumble(what + " require support for Saxon extensions, available in Saxon-PE or higher");
    }

    private void needUpdate(XPathParser p, String what) throws XPathException {
        p.grumble(what + " requires support for XQuery Update, available in Saxon-EE or higher");
    }

    public Expression parseNamedFunctionReference(XPathParser p) throws XPathException {
        BuiltInFunctionSet.Entry details;
        StructuredQName functionName;
        int arity;
        StaticContext env;
        int offset;
        block9: {
            Tokenizer t = p.getTokenizer();
            String fname = t.currentTokenValue;
            offset = t.currentTokenStartOffset;
            env = p.getStaticContext();
            p.nextToken();
            p.expect(209);
            NumericValue number = NumericValue.parseNumber(t.currentTokenValue);
            if (!(number instanceof IntegerValue)) {
                p.grumble("Number following '#' must be an integer");
            }
            if (number.compareTo(0L) < 0 || number.compareTo(Integer.MAX_VALUE) > 0) {
                p.grumble("Number following '#' is out of range", "FOAR0002");
            }
            arity = (int)number.longValue();
            p.nextToken();
            functionName = null;
            try {
                functionName = p.getQNameParser().parse(fname, env.getDefaultFunctionNamespace());
                if (functionName.getPrefix().equals("") && XPathParser.isReservedFunctionName31(functionName.getLocalPart())) {
                    p.grumble("The unprefixed function name '" + functionName.getLocalPart() + "' is reserved in XPath 3.1");
                }
            } catch (XPathException e) {
                p.grumble(e.getMessage(), e.getErrorCodeLocalPart());
                if ($assertionsDisabled || functionName != null) break block9;
                throw new AssertionError();
            }
        }
        Function fcf = null;
        try {
            FunctionLibrary lib = env.getFunctionLibrary();
            SymbolicName.F sn = new SymbolicName.F(functionName, arity);
            fcf = lib.getFunctionItem(sn, env);
            if (fcf == null) {
                p.grumble("Function " + functionName.getEQName() + "#" + arity + " not found", "XPST0017", offset);
            }
        } catch (XPathException e) {
            p.grumble(e.getMessage(), "XPST0017", offset);
        }
        if (functionName.hasURI("http://www.w3.org/2005/xpath-functions") && fcf instanceof SystemFunction && (details = ((SystemFunction)fcf).getDetails()) != null && (details.properties & 0x583C) != 0) {
            SystemFunction lookup = XPath31FunctionSet.getInstance().makeFunction("function-lookup", 2);
            lookup.setRetainedStaticContext(env.makeRetainedStaticContext());
            return lookup.makeFunctionCall(Literal.makeLiteral(new QNameValue(functionName, BuiltInAtomicType.QNAME)), Literal.makeLiteral(Int64Value.makeIntegerValue(arity)));
        }
        Expression ref = ParserExtension.makeNamedFunctionReference(functionName, fcf);
        p.setLocation(ref, offset);
        return ref;
    }

    private static Expression makeNamedFunctionReference(StructuredQName functionName, Function fcf) {
        if (fcf instanceof UserFunction && !functionName.hasURI("http://www.w3.org/1999/XSL/Transform")) {
            return new UserFunctionReference((UserFunction)fcf);
        }
        if (fcf instanceof UnresolvedXQueryFunctionItem) {
            return ((UnresolvedXQueryFunctionItem)fcf).getFunctionReference();
        }
        return new FunctionLiteral(fcf);
    }

    public ItemType parseFunctionItemType(XPathParser p, AnnotationList annotations) throws XPathException {
        Tokenizer t = p.getTokenizer();
        p.nextToken();
        ArrayList<SequenceType> argTypes = new ArrayList<SequenceType>(3);
        if (t.currentToken == 207 || t.currentToken == 17) {
            p.nextToken();
            p.expect(204);
            p.nextToken();
            if (annotations.isEmpty()) {
                return AnyFunctionType.getInstance();
            }
            return new AnyFunctionTypeWithAssertions(annotations, p.getStaticContext().getConfiguration());
        }
        while (t.currentToken != 204) {
            SequenceType arg = p.parseSequenceType();
            argTypes.add(arg);
            if (t.currentToken == 204) break;
            if (t.currentToken == 7) {
                p.nextToken();
                continue;
            }
            p.grumble("Expected ',' or ')' after function argument type, found '" + Token.tokens[t.currentToken] + '\'');
        }
        p.nextToken();
        if (t.currentToken == 71) {
            p.nextToken();
            SequenceType resultType = p.parseSequenceType();
            SequenceType[] argArray = new SequenceType[argTypes.size()];
            argArray = argTypes.toArray(argArray);
            return new SpecificFunctionType(argArray, resultType, annotations);
        }
        if (!argTypes.isEmpty()) {
            p.grumble("Result type must be given if an argument type is given: expected 'as (type)'");
            return null;
        }
        p.grumble("function() is no longer allowed for a general function type: must be function(*)");
        return null;
    }

    public ItemType parseExtendedItemType(XPathParser p) throws XPathException {
        Tokenizer t = p.getTokenizer();
        if (t.currentToken == 69 && t.currentTokenValue.equals("tuple")) {
            this.needExtension(p, "Tuple types");
        } else if (t.currentToken == 69 && t.currentTokenValue.equals("union")) {
            this.needExtension(p, "Inline union types");
        }
        return null;
    }

    public Expression parseTypePattern(XPathParser p) throws XPathException {
        this.needExtension(p, "type-based patterns");
        return null;
    }

    public Expression makeArgumentPlaceMarker(XPathParser p) {
        return null;
    }

    protected Expression parseInlineFunction(XPathParser p, AnnotationList annotations) throws XPathException {
        Expression body;
        Tokenizer t = p.getTokenizer();
        int offset = t.currentTokenStartOffset;
        InlineFunctionDetails details = new InlineFunctionDetails();
        details.outerVariables = new Stack();
        for (LocalBinding lb : p.getRangeVariables()) {
            details.outerVariables.push(lb);
        }
        details.outerVariablesUsed = new ArrayList<LocalBinding>(4);
        details.implicitParams = new ArrayList<UserFunctionParameter>(4);
        this.inlineFunctionStack.push(details);
        p.setRangeVariables(new Stack<LocalBinding>());
        p.nextToken();
        HashSet<StructuredQName> paramNames = new HashSet<StructuredQName>(8);
        ArrayList<UserFunctionParameter> params = new ArrayList<UserFunctionParameter>(8);
        SequenceType resultType = SequenceType.ANY_SEQUENCE;
        int paramSlot = 0;
        while (t.currentToken != 204) {
            p.expect(21);
            p.nextToken();
            p.expect(201);
            String argName = t.currentTokenValue;
            StructuredQName argQName = p.makeStructuredQName(argName, "");
            if (paramNames.contains(argQName)) {
                p.grumble("Duplicate parameter name " + Err.wrap(t.currentTokenValue, 5), "XQST0039");
            }
            paramNames.add(argQName);
            SequenceType paramType = SequenceType.ANY_SEQUENCE;
            p.nextToken();
            if (t.currentToken == 71) {
                p.nextToken();
                paramType = p.parseSequenceType();
            }
            UserFunctionParameter arg = new UserFunctionParameter();
            arg.setRequiredType(paramType);
            arg.setVariableQName(argQName);
            arg.setSlotNumber(paramSlot++);
            params.add(arg);
            p.declareRangeVariable(arg);
            if (t.currentToken == 204) break;
            if (t.currentToken == 7) {
                p.nextToken();
                continue;
            }
            p.grumble("Expected ',' or ')' after function argument, found '" + Token.tokens[t.currentToken] + '\'');
        }
        t.setState(1);
        p.nextToken();
        if (t.currentToken == 71) {
            t.setState(2);
            p.nextToken();
            resultType = p.parseSequenceType();
        }
        p.expect(59);
        t.setState(0);
        p.nextToken();
        if (t.currentToken == 215 && p.isAllowXPath31Syntax()) {
            t.lookAhead();
            p.nextToken();
            body = Literal.makeEmptySequence();
        } else {
            body = p.parseExpression();
            p.expect(215);
            t.lookAhead();
            p.nextToken();
        }
        ExpressionTool.setDeepRetainedStaticContext(body, p.getStaticContext().makeRetainedStaticContext());
        int arity = paramNames.size();
        for (int i = 0; i < arity; ++i) {
            p.undeclareRangeVariable();
        }
        Expression result = ParserExtension.makeInlineFunctionValue(p, annotations, details, params, resultType, body);
        p.setLocation(result, offset);
        p.setRangeVariables(details.outerVariables);
        this.inlineFunctionStack.pop();
        return result;
    }

    public static Expression makeInlineFunctionValue(XPathParser p, AnnotationList annotations, InlineFunctionDetails details, List<UserFunctionParameter> params, SequenceType resultType, Expression body) {
        Expression result;
        List<UserFunctionParameter> implicitParams;
        int arity = params.size();
        UserFunction uf = new UserFunction();
        uf.setFunctionName(new StructuredQName("anon", "http://ns.saxonica.com/anonymous-type", "f_" + uf.hashCode()));
        uf.setPackageData(p.getStaticContext().getPackageData());
        uf.setBody(body);
        uf.setAnnotations(annotations);
        uf.setResultType(resultType);
        uf.incrementReferenceCount();
        if (uf.getPackageData() instanceof StylesheetPackage) {
            StylesheetPackage pack = (StylesheetPackage)uf.getPackageData();
            Component comp = Component.makeComponent(uf, Visibility.PRIVATE, VisibilityProvenance.DEFAULTED, pack, pack);
            uf.setDeclaringComponent(comp);
        }
        if (!(implicitParams = details.implicitParams).isEmpty()) {
            int extraParams = implicitParams.size();
            int expandedArity = params.size() + extraParams;
            UserFunctionParameter[] paramArray = new UserFunctionParameter[expandedArity];
            for (int i = 0; i < params.size(); ++i) {
                paramArray[i] = params.get(i);
            }
            int k = params.size();
            for (UserFunctionParameter implicitParam : implicitParams) {
                paramArray[k++] = implicitParam;
            }
            uf.setParameterDefinitions(paramArray);
            SlotManager stackFrame = p.getStaticContext().getConfiguration().makeSlotManager();
            for (int i = 0; i < expandedArity; ++i) {
                int slot = stackFrame.allocateSlotNumber(paramArray[i].getVariableQName());
                paramArray[i].setSlotNumber(slot);
            }
            ExpressionTool.allocateSlots(body, expandedArity, stackFrame);
            uf.setStackFrameMap(stackFrame);
            result = new UserFunctionReference(uf);
            Expression[] partialArgs = new Expression[expandedArity];
            for (int i = 0; i < arity; ++i) {
                partialArgs[i] = null;
            }
            for (int ip = 0; ip < implicitParams.size(); ++ip) {
                LocalVariableReference var;
                UserFunctionParameter ufp = implicitParams.get(ip);
                LocalBinding binding = details.outerVariablesUsed.get(ip);
                if (binding instanceof TemporaryXSLTVariableBinding) {
                    var = new LocalVariableReference(binding);
                    ((TemporaryXSLTVariableBinding)binding).declaration.registerReference(var);
                } else {
                    var = new LocalVariableReference(binding);
                }
                var.setStaticType(binding.getRequiredType(), null, 0);
                ufp.setRequiredType(binding.getRequiredType());
                partialArgs[ip + arity] = var;
            }
            result = new PartialApply(result, partialArgs);
        } else {
            UserFunctionParameter[] paramArray = params.toArray(new UserFunctionParameter[0]);
            uf.setParameterDefinitions(paramArray);
            SlotManager stackFrame = p.getStaticContext().getConfiguration().makeSlotManager();
            for (UserFunctionParameter param : paramArray) {
                stackFrame.allocateSlotNumber(param.getVariableQName());
            }
            ExpressionTool.allocateSlots(body, params.size(), stackFrame);
            uf.setStackFrameMap(stackFrame);
            result = new UserFunctionReference(uf);
        }
        if (uf.getPackageData() instanceof StylesheetPackage) {
            ((StylesheetPackage)uf.getPackageData()).addComponent(uf.getDeclaringComponent());
        }
        return result;
    }

    public Expression parseDotFunction(XPathParser p) throws XPathException {
        this.needExtension(p, "Dot functions");
        return null;
    }

    public Expression parseUnderscoreFunction(XPathParser p) throws XPathException {
        this.needExtension(p, "Underscore functions");
        return null;
    }

    public Expression bindNumericParameterReference(XPathParser p) throws XPathException {
        this.needExtension(p, "Underscore functions");
        return null;
    }

    public Expression makeCurriedFunction(XPathParser parser, int offset, StructuredQName name, Expression[] args, IntSet placeMarkers) throws XPathException {
        SymbolicName.F sn;
        StaticContext env = parser.getStaticContext();
        FunctionLibrary lib = env.getFunctionLibrary();
        Function target = lib.getFunctionItem(sn = new SymbolicName.F(name, args.length), env);
        if (target == null) {
            return parser.reportMissingFunction(offset, name, args, new ArrayList<String>());
        }
        Expression targetExp = ParserExtension.makeNamedFunctionReference(name, target);
        parser.setLocation(targetExp, offset);
        return ParserExtension.curryFunction(targetExp, args, placeMarkers);
    }

    public static Expression curryFunction(Expression functionExp, Expression[] args, IntSet placeMarkers) {
        IntIterator ii = placeMarkers.iterator();
        while (ii.hasNext()) {
            args[ii.next()] = null;
        }
        return new PartialApply(functionExp, args);
    }

    public LocalBinding findOuterRangeVariable(XPathParser p, StructuredQName qName) {
        return ParserExtension.findOuterRangeVariable(qName, this.inlineFunctionStack, p.getStaticContext());
    }

    public static LocalBinding findOuterRangeVariable(StructuredQName qName, Stack<InlineFunctionDetails> inlineFunctionStack, StaticContext env) {
        LocalBinding b2 = ParserExtension.findOuterXPathRangeVariable(qName, inlineFunctionStack);
        if (b2 != null) {
            return b2;
        }
        if (env instanceof ExpressionContext && !inlineFunctionStack.isEmpty()) {
            b2 = ParserExtension.findOuterXSLTVariable(qName, inlineFunctionStack, env);
        }
        return b2;
    }

    private static LocalBinding findOuterXPathRangeVariable(StructuredQName qName, Stack<InlineFunctionDetails> inlineFunctionStack) {
        for (int s = inlineFunctionStack.size() - 1; s >= 0; --s) {
            InlineFunctionDetails details = (InlineFunctionDetails)inlineFunctionStack.get(s);
            Stack<LocalBinding> outerVariables = details.outerVariables;
            for (int v = outerVariables.size() - 1; v >= 0; --v) {
                LocalBinding b2 = (LocalBinding)outerVariables.elementAt(v);
                if (!b2.getVariableQName().equals(qName)) continue;
                for (int bs = s; bs <= inlineFunctionStack.size() - 1; ++bs) {
                    details = (InlineFunctionDetails)inlineFunctionStack.get(bs);
                    boolean found = false;
                    for (int p = 0; p < details.outerVariablesUsed.size() - 1; ++p) {
                        if (details.outerVariablesUsed.get(p) != b2) continue;
                        b2 = details.implicitParams.get(p);
                        found = true;
                        break;
                    }
                    if (found) continue;
                    details.outerVariablesUsed.add(b2);
                    UserFunctionParameter ufp = new UserFunctionParameter();
                    ufp.setVariableQName(qName);
                    ufp.setRequiredType(b2.getRequiredType());
                    details.implicitParams.add(ufp);
                    b2 = ufp;
                }
                return b2;
            }
            LocalBinding b2 = ParserExtension.bindParametersInNestedFunctions(qName, inlineFunctionStack, s);
            if (b2 == null) continue;
            return b2;
        }
        return null;
    }

    private static LocalBinding bindParametersInNestedFunctions(StructuredQName qName, Stack<InlineFunctionDetails> inlineFunctionStack, int start) {
        InlineFunctionDetails details = (InlineFunctionDetails)inlineFunctionStack.get(start);
        List<UserFunctionParameter> params = details.implicitParams;
        for (UserFunctionParameter param : params) {
            if (!param.getVariableQName().equals(qName)) continue;
            LocalBinding b2 = param;
            for (int bs = start + 1; bs <= inlineFunctionStack.size() - 1; ++bs) {
                details = (InlineFunctionDetails)inlineFunctionStack.get(bs);
                boolean found = false;
                for (int p = 0; p < details.outerVariablesUsed.size() - 1; ++p) {
                    if (details.outerVariablesUsed.get(p) != param) continue;
                    b2 = details.implicitParams.get(p);
                    found = true;
                    break;
                }
                if (found) continue;
                details.outerVariablesUsed.add(param);
                UserFunctionParameter ufp = new UserFunctionParameter();
                ufp.setVariableQName(qName);
                ufp.setRequiredType(param.getRequiredType());
                details.implicitParams.add(ufp);
                b2 = ufp;
            }
            if (b2 == null) continue;
            return b2;
        }
        return null;
    }

    private static LocalBinding findOuterXSLTVariable(StructuredQName qName, Stack<InlineFunctionDetails> inlineFunctionStack, StaticContext env) {
        SourceBinding decl = ((ExpressionContext)env).getStyleElement().bindLocalVariable(qName);
        if (decl != null) {
            LocalBinding innermostBinding;
            InlineFunctionDetails details = (InlineFunctionDetails)inlineFunctionStack.get(0);
            boolean found = false;
            for (int p = 0; p < details.outerVariablesUsed.size(); ++p) {
                if (!details.outerVariablesUsed.get(p).getVariableQName().equals(qName)) continue;
                found = true;
                break;
            }
            if (!found) {
                details.outerVariablesUsed.add(new TemporaryXSLTVariableBinding(decl));
                UserFunctionParameter ufp = new UserFunctionParameter();
                ufp.setVariableQName(qName);
                ufp.setRequiredType(decl.getInferredType(true));
                details.implicitParams.add(ufp);
            }
            if ((innermostBinding = ParserExtension.bindParametersInNestedFunctions(qName, inlineFunctionStack, 0)) != null) {
                return innermostBinding;
            }
        }
        return null;
    }

    public Expression createDynamicCurriedFunction(XPathParser p, Expression functionItem, ArrayList<Expression> args, IntSet placeMarkers) {
        Expression[] arguments = new Expression[args.size()];
        args.toArray(arguments);
        Expression result = ParserExtension.curryFunction(functionItem, arguments, placeMarkers);
        p.setLocation(result, p.getTokenizer().currentTokenStartOffset);
        return result;
    }

    public void handleExternalFunctionDeclaration(XQueryParser p, XQueryFunction func) throws XPathException {
        this.needExtension(p, "External function declarations");
    }

    public void parseTypeAliasDeclaration(XQueryParser p) throws XPathException {
        this.needExtension(p, "Type alias declarations");
    }

    public void parseRevalidationDeclaration(XQueryParser p) throws XPathException {
        this.needUpdate(p, "A revalidation declaration");
    }

    public void parseUpdatingFunctionDeclaration(XQueryParser p) throws XPathException {
        this.needUpdate(p, "An updating function");
    }

    protected Expression parseExtendedExprSingle(XPathParser p) throws XPathException {
        return null;
    }

    protected Expression parseForMemberExpression(XPathParser p) throws XPathException {
        return null;
    }

    public static class TemporaryXSLTVariableBinding
    implements LocalBinding {
        SourceBinding declaration;

        public TemporaryXSLTVariableBinding(SourceBinding decl) {
            this.declaration = decl;
        }

        @Override
        public SequenceType getRequiredType() {
            return this.declaration.getInferredType(true);
        }

        @Override
        public Sequence evaluateVariable(XPathContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isGlobal() {
            return false;
        }

        @Override
        public boolean isAssignable() {
            return false;
        }

        @Override
        public int getLocalSlotNumber() {
            return 0;
        }

        @Override
        public StructuredQName getVariableQName() {
            return this.declaration.getVariableQName();
        }

        @Override
        public void addReference(VariableReference ref, boolean isLoopingReference) {
        }

        @Override
        public IntegerValue[] getIntegerBoundsForVariable() {
            return null;
        }

        @Override
        public void setIndexedVariable() {
        }

        @Override
        public boolean isIndexedVariable() {
            return false;
        }
    }

    public static class InlineFunctionDetails {
        public Stack<LocalBinding> outerVariables;
        public List<LocalBinding> outerVariablesUsed;
        public List<UserFunctionParameter> implicitParams;
    }
}


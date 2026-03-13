/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.QNameParser;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.NumericValue;

public class FunctionAvailable
extends SystemFunction {
    @Override
    public Expression makeFunctionCall(Expression ... arguments) {
        PackageData pack = this.getRetainedStaticContext().getPackageData();
        if (pack instanceof StylesheetPackage) {
            ((StylesheetPackage)pack).setRetainUnusedFunctions();
        }
        return super.makeFunctionCall(arguments);
    }

    @Override
    public Expression makeOptimizedFunctionCall(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo, Expression ... arguments) throws XPathException {
        if (arguments[0] instanceof Literal && (arguments.length == 1 || arguments[1] instanceof Literal)) {
            String lexicalQName = ((Literal)arguments[0]).getValue().getStringValue();
            StaticContext env = visitor.getStaticContext();
            boolean b = false;
            QNameParser qp = new QNameParser(this.getRetainedStaticContext()).withAcceptEQName(true).withErrorOnBadSyntax("XTDE1400").withErrorOnUnresolvedPrefix("XTDE1400");
            StructuredQName functionName = qp.parse(lexicalQName, env.getDefaultFunctionNamespace());
            int minArity = 0;
            int maxArity = 20;
            if (this.getArity() == 2) {
                maxArity = minArity = (int)((NumericValue)arguments[1].evaluateItem(env.makeEarlyEvaluationContext())).longValue();
            }
            for (int i = minArity; i <= maxArity; ++i) {
                SymbolicName.F sn = new SymbolicName.F(functionName, i);
                if (!env.getFunctionLibrary().isAvailable(sn)) continue;
                b = true;
                break;
            }
            return Literal.makeLiteral(BooleanValue.get(b));
        }
        return null;
    }

    private boolean isFunctionAvailable(String lexicalName, String edition, int arity, XPathContext context) throws XPathException {
        StructuredQName qName;
        if (arity == -1) {
            for (int i = 0; i < 20; ++i) {
                if (!this.isFunctionAvailable(lexicalName, edition, i, context)) continue;
                return true;
            }
            return false;
        }
        try {
            if (NameChecker.isValidNCName(lexicalName)) {
                String uri = "http://www.w3.org/2005/xpath-functions";
                qName = new StructuredQName("", uri, lexicalName);
            } else {
                qName = StructuredQName.fromLexicalQName(lexicalName, false, true, this.getRetainedStaticContext());
            }
        } catch (XPathException e) {
            e.setErrorCode("XTDE1400");
            e.setXPathContext(context);
            throw e;
        }
        FunctionLibraryList lib = context.getController().getExecutable().getFunctionLibrary();
        SymbolicName.F sn = new SymbolicName.F(qName, arity);
        return lib.isAvailable(sn);
    }

    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        String lexicalQName = arguments[0].head().getStringValue();
        int arity = -1;
        if (arguments.length == 2) {
            arity = (int)((NumericValue)arguments[1].head()).longValue();
        }
        return BooleanValue.get(this.isFunctionAvailable(lexicalQName, this.getRetainedStaticContext().getPackageData().getTargetEdition(), arity, context));
    }
}


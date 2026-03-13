/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.net.URI;
import java.net.URISyntaxException;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.functions.CollatingFunctionFixed;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;

public class CollatingFunctionFree
extends SystemFunction {
    private int getCollationArgument() {
        return this.getArity() - 1;
    }

    @Override
    public Expression makeOptimizedFunctionCall(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo, Expression ... arguments) throws XPathException {
        Expression c = arguments[arguments.length - 1];
        if (c instanceof Literal) {
            String coll = ((Literal)c).getValue().getStringValue();
            try {
                URI collUri = new URI(coll);
                if (!collUri.isAbsolute()) {
                    collUri = ResolveURI.makeAbsolute(coll, this.getStaticBaseUriString());
                    coll = collUri.toASCIIString();
                }
            } catch (URISyntaxException e) {
                visitor.getStaticContext().issueWarning("Cannot resolve relative collation URI " + coll, c.getLocation());
            }
            CollatingFunctionFixed fn = this.bindCollation(coll);
            Expression[] newArgs = new Expression[arguments.length - 1];
            System.arraycopy(arguments, 0, newArgs, 0, newArgs.length);
            return fn.makeFunctionCall(newArgs);
        }
        return null;
    }

    public CollatingFunctionFixed bindCollation(String collationName) throws XPathException {
        Configuration config = this.getRetainedStaticContext().getConfiguration();
        CollatingFunctionFixed fixed = (CollatingFunctionFixed)config.makeSystemFunction(this.getFunctionName().getLocalPart(), this.getArity() - 1);
        fixed.setRetainedStaticContext(this.getRetainedStaticContext());
        fixed.setCollationName(collationName);
        return fixed;
    }

    public static String expandCollationURI(String collationName, URI expressionBaseURI) throws XPathException {
        try {
            URI collationURI = new URI(collationName);
            if (!collationURI.isAbsolute()) {
                if (expressionBaseURI == null) {
                    throw new XPathException("Cannot resolve relative collation URI '" + collationName + "': unknown or invalid base URI", "FOCH0002");
                }
                collationURI = expressionBaseURI.resolve(collationURI);
                collationName = collationURI.toString();
            }
        } catch (URISyntaxException e) {
            throw new XPathException("Collation name '" + collationName + "' is not a valid URI", "FOCH0002");
        }
        return collationName;
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] args) throws XPathException {
        int c = this.getCollationArgument();
        String collation = args[c].head().getStringValue();
        collation = CollatingFunctionFree.expandCollationURI(collation, this.getRetainedStaticContext().getStaticBaseUri());
        CollatingFunctionFixed fixed = this.bindCollation(collation);
        Sequence[] retainedArgs = new Sequence[args.length - 1];
        System.arraycopy(args, 0, retainedArgs, 0, c);
        if (c + 1 < this.getArity()) {
            System.arraycopy(args, c + 1, retainedArgs, c, this.getArity() - c);
        }
        return fixed.call(context, retainedArgs);
    }

    @Override
    public String getStreamerName() {
        try {
            return this.bindCollation("http://www.w3.org/2005/xpath-functions/collation/codepoint").getStreamerName();
        } catch (XPathException e) {
            throw new AssertionError((Object)e);
        }
    }
}


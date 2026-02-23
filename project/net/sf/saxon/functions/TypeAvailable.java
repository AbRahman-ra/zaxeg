/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.BooleanValue;

public class TypeAvailable
extends SystemFunction {
    private boolean typeAvailable(String lexicalName, Configuration config) throws XPathException {
        StructuredQName qName;
        String uri;
        try {
            if (lexicalName.indexOf(58) < 0 && !lexicalName.startsWith("Q{")) {
                uri = this.getRetainedStaticContext().getURIForPrefix("", true);
                qName = new StructuredQName("", uri, lexicalName);
            } else {
                qName = StructuredQName.fromLexicalQName(lexicalName, false, true, this.getRetainedStaticContext());
            }
        } catch (XPathException e) {
            e.setErrorCode("XTDE1428");
            throw e;
        }
        uri = qName.getURI();
        if (uri.equals("http://saxon.sf.net/java-type")) {
            try {
                String className = JavaExternalObjectType.localNameToClassName(qName.getLocalPart());
                config.getClass(className, false, null);
                return true;
            } catch (XPathException err) {
                return false;
            }
        }
        SchemaType type = config.getSchemaType(qName);
        if (type == null) {
            return false;
        }
        return config.getXsdVersion() != 10 || !(type instanceof BuiltInAtomicType) || ((BuiltInAtomicType)type).isAllowedInXSD10();
    }

    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        String lexicalQName = arguments[0].head().getStringValue();
        return BooleanValue.get(this.typeAvailable(lexicalQName, context.getConfiguration()));
    }

    @Override
    public Expression makeFunctionCall(Expression[] arguments) {
        try {
            if (arguments[0] instanceof Literal) {
                boolean b = this.typeAvailable(((Literal)arguments[0]).getValue().getStringValue(), this.getRetainedStaticContext().getConfiguration());
                return Literal.makeLiteral(BooleanValue.get(b));
            }
        } catch (XPathException xPathException) {
            // empty catch block
        }
        return super.makeFunctionCall(arguments);
    }
}


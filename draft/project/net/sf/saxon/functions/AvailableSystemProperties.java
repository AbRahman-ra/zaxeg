/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.ArrayList;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.SequenceExtent;

public class AvailableSystemProperties
extends SystemFunction {
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        ArrayList<QNameValue> myList = new ArrayList<QNameValue>();
        myList.add(new QNameValue("xsl", "http://www.w3.org/1999/XSL/Transform", "version"));
        myList.add(new QNameValue("xsl", "http://www.w3.org/1999/XSL/Transform", "vendor"));
        myList.add(new QNameValue("xsl", "http://www.w3.org/1999/XSL/Transform", "vendor-url"));
        myList.add(new QNameValue("xsl", "http://www.w3.org/1999/XSL/Transform", "product-name"));
        myList.add(new QNameValue("xsl", "http://www.w3.org/1999/XSL/Transform", "product-version"));
        myList.add(new QNameValue("xsl", "http://www.w3.org/1999/XSL/Transform", "is-schema-aware"));
        myList.add(new QNameValue("xsl", "http://www.w3.org/1999/XSL/Transform", "supports-serialization"));
        myList.add(new QNameValue("xsl", "http://www.w3.org/1999/XSL/Transform", "supports-backwards-compatibility"));
        myList.add(new QNameValue("xsl", "http://www.w3.org/1999/XSL/Transform", "supports-namespace-axis"));
        myList.add(new QNameValue("xsl", "http://www.w3.org/1999/XSL/Transform", "supports-streaming"));
        myList.add(new QNameValue("xsl", "http://www.w3.org/1999/XSL/Transform", "supports-dynamic-evaluation"));
        myList.add(new QNameValue("xsl", "http://www.w3.org/1999/XSL/Transform", "supports-higher-order-functions"));
        myList.add(new QNameValue("xsl", "http://www.w3.org/1999/XSL/Transform", "xpath-version"));
        myList.add(new QNameValue("xsl", "http://www.w3.org/1999/XSL/Transform", "xsd-version"));
        if (context.getConfiguration().getBooleanProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS)) {
            for (Object s : System.getProperties().keySet()) {
                myList.add(new QNameValue("", "", s.toString()));
            }
        }
        return SequenceExtent.makeSequenceExtent(myList);
    }

    @Override
    public Expression makeFunctionCall(Expression[] arguments) {
        return new SystemFunctionCall(this, arguments){

            @Override
            public Expression preEvaluate(ExpressionVisitor visitor) {
                return this;
            }
        };
    }
}


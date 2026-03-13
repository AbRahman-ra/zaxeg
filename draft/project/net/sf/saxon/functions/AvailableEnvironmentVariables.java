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
import net.sf.saxon.lib.EnvironmentVariableResolver;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.StringValue;

public class AvailableEnvironmentVariables
extends SystemFunction {
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        EnvironmentVariableResolver resolver = context.getConfiguration().getConfigurationProperty(Feature.ENVIRONMENT_VARIABLE_RESOLVER);
        ArrayList<StringValue> myList = new ArrayList<StringValue>();
        if (context.getConfiguration().getBooleanProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS)) {
            for (String s : resolver.getAvailableEnvironmentVariables()) {
                myList.add(new StringValue(s));
            }
        }
        return new SequenceExtent(myList);
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


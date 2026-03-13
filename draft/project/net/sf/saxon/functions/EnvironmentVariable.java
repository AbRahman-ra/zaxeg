/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.EnvironmentVariableResolver;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

public class EnvironmentVariable
extends SystemFunction {
    @Override
    public ZeroOrOne call(XPathContext context, Sequence[] arguments) throws XPathException {
        return new ZeroOrOne<StringValue>(EnvironmentVariable.getVariable((StringValue)arguments[0].head(), context));
    }

    private static StringValue getVariable(StringValue environVar, XPathContext context) {
        EnvironmentVariableResolver resolver = context.getConfiguration().getConfigurationProperty(Feature.ENVIRONMENT_VARIABLE_RESOLVER);
        String environVarName = environVar.getStringValue();
        String environValue = "";
        if (context.getConfiguration().getBooleanProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS)) {
            try {
                environValue = resolver.getEnvironmentVariable(environVarName);
                if (environValue == null) {
                    return null;
                }
            } catch (NullPointerException | SecurityException runtimeException) {
                // empty catch block
            }
        }
        return new StringValue(environValue);
    }
}


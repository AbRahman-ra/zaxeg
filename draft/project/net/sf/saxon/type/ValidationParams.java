/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import java.util.HashMap;
import java.util.Map;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.sxpath.XPathDynamicContext;
import net.sf.saxon.sxpath.XPathVariable;
import net.sf.saxon.trans.XPathException;

public class ValidationParams
extends HashMap<StructuredQName, Sequence> {
    public ValidationParams() {
        super(20);
    }

    public static void setValidationParams(Map<StructuredQName, XPathVariable> declaredParams, ValidationParams actualParams, XPathDynamicContext context) throws XPathException {
        for (StructuredQName p : declaredParams.keySet()) {
            XPathVariable var = declaredParams.get(p);
            Sequence paramValue = (Sequence)actualParams.get(p);
            if (paramValue != null) {
                context.setVariable(var, paramValue);
                continue;
            }
            context.setVariable(var, var.getDefaultValue());
        }
    }
}


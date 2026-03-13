/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;

public class IsIdRef
extends ExtensionFunctionDefinition {
    private static final StructuredQName qName = new StructuredQName("", "http://saxon.sf.net/", "is-idref");

    @Override
    public StructuredQName getFunctionQName() {
        return qName;
    }

    @Override
    public int getMinimumNumberOfArguments() {
        return 0;
    }

    @Override
    public int getMaximumNumberOfArguments() {
        return 0;
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[0];
    }

    @Override
    public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
        return SequenceType.SINGLE_BOOLEAN;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression() {
        return new IsIdRefCall();
    }

    private static class IsIdRefCall
    extends ExtensionFunctionCall {
        private IsIdRefCall() {
        }

        @Override
        public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
            Item contextItem = context.getContextItem();
            return BooleanValue.get(contextItem instanceof NodeInfo && ((NodeInfo)contextItem).isIdref());
        }

        @Override
        public boolean effectiveBooleanValue(XPathContext context, Sequence[] arguments) throws XPathException {
            Item contextItem = context.getContextItem();
            return contextItem instanceof NodeInfo && ((NodeInfo)contextItem).isIdref();
        }
    }
}


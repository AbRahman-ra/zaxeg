/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.ListConstructorFunction;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.ListType;
import net.sf.saxon.type.MissingComponentException;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class ListCastableFunction
extends ListConstructorFunction {
    public ListCastableFunction(ListType targetType, NamespaceResolver resolver, boolean allowEmpty) throws MissingComponentException {
        super(targetType, resolver, allowEmpty);
    }

    @Override
    public FunctionItemType getFunctionItemType() {
        return new SpecificFunctionType(new SequenceType[]{SequenceType.ANY_SEQUENCE}, SequenceType.SINGLE_BOOLEAN);
    }

    @Override
    public StructuredQName getFunctionName() {
        return null;
    }

    @Override
    public BooleanValue call(XPathContext context, Sequence[] args) throws XPathException {
        SequenceIterator iter = args[0].iterate();
        AtomicValue val = (AtomicValue)iter.next();
        if (val == null) {
            return BooleanValue.get(this.allowEmpty);
        }
        if (iter.next() != null) {
            return BooleanValue.FALSE;
        }
        if (!(val instanceof StringValue) || val instanceof AnyURIValue) {
            return BooleanValue.FALSE;
        }
        ConversionRules rules = context.getConfiguration().getConversionRules();
        CharSequence cs = val.getStringValueCS();
        ValidationFailure failure = this.targetType.validateContent(cs, this.nsResolver, rules);
        return BooleanValue.get(failure == null);
    }
}


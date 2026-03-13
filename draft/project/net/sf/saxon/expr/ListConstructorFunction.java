/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.AbstractFunction;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.EmptyAtomicSequence;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.ListType;
import net.sf.saxon.type.MissingComponentException;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class ListConstructorFunction
extends AbstractFunction {
    protected ListType targetType;
    protected NamespaceResolver nsResolver;
    protected boolean allowEmpty;
    protected SimpleType memberType;

    public ListConstructorFunction(ListType targetType, NamespaceResolver resolver, boolean allowEmpty) throws MissingComponentException {
        this.targetType = targetType;
        this.nsResolver = resolver;
        this.allowEmpty = allowEmpty;
        this.memberType = targetType.getItemType();
    }

    public ListType getTargetType() {
        return this.targetType;
    }

    public SimpleType getMemberType() {
        return this.memberType;
    }

    public boolean isAllowEmpty() {
        return this.allowEmpty;
    }

    @Override
    public FunctionItemType getFunctionItemType() {
        AtomicType resultType = BuiltInAtomicType.ANY_ATOMIC;
        if (this.memberType.isAtomicType()) {
            resultType = (AtomicType)this.memberType;
        }
        SequenceType argType = this.allowEmpty ? SequenceType.OPTIONAL_ATOMIC : SequenceType.SINGLE_ATOMIC;
        return new SpecificFunctionType(new SequenceType[]{argType}, SequenceType.makeSequenceType(resultType, 57344));
    }

    @Override
    public StructuredQName getFunctionName() {
        return this.targetType.getStructuredQName();
    }

    @Override
    public String getDescription() {
        return this.getFunctionName().getDisplayName();
    }

    @Override
    public int getArity() {
        return 1;
    }

    @Override
    public AtomicSequence call(XPathContext context, Sequence[] args) throws XPathException {
        AtomicValue val = (AtomicValue)args[0].head();
        if (val == null) {
            if (this.allowEmpty) {
                return EmptyAtomicSequence.getInstance();
            }
            XPathException e = new XPathException("Cast expression does not allow an empty sequence to be supplied", "XPTY0004");
            e.setIsTypeError(true);
            throw e;
        }
        if (!(val instanceof StringValue) || val instanceof AnyURIValue) {
            XPathException e = new XPathException("Only xs:string and xs:untypedAtomic can be cast to a list type", "XPTY0004");
            e.setIsTypeError(true);
            throw e;
        }
        ConversionRules rules = context.getConfiguration().getConversionRules();
        CharSequence cs = val.getStringValueCS();
        ValidationFailure failure = this.targetType.validateContent(cs, this.nsResolver, rules);
        if (failure != null) {
            throw failure.makeException();
        }
        return this.targetType.getTypedValue(cs, this.nsResolver, rules);
    }
}


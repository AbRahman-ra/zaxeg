/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.OperandRole;
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
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.Converter;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.type.UnionType;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class UnionConstructorFunction
extends AbstractFunction {
    protected UnionType targetType;
    protected NamespaceResolver resolver;
    protected boolean allowEmpty;

    public UnionConstructorFunction(UnionType targetType, NamespaceResolver resolver, boolean allowEmpty) {
        this.targetType = targetType;
        this.resolver = resolver;
        this.allowEmpty = allowEmpty;
    }

    protected OperandRole getOperandRole() {
        return OperandRole.SINGLE_ATOMIC;
    }

    public boolean isAllowEmpty() {
        return this.allowEmpty;
    }

    public UnionType getTargetType() {
        return this.targetType;
    }

    public NamespaceResolver getNamespaceResolver() {
        return this.resolver;
    }

    @Override
    public FunctionItemType getFunctionItemType() {
        SequenceType resultType = this.targetType.getResultTypeOfCast();
        SequenceType argType = this.allowEmpty ? SequenceType.OPTIONAL_ATOMIC : SequenceType.SINGLE_ATOMIC;
        return new SpecificFunctionType(new SequenceType[]{argType}, resultType);
    }

    @Override
    public StructuredQName getFunctionName() {
        return this.targetType.getTypeName();
    }

    @Override
    public String getDescription() {
        return this.getFunctionName().getDisplayName();
    }

    @Override
    public int getArity() {
        return 1;
    }

    public AtomicSequence cast(AtomicValue value, XPathContext context) throws XPathException {
        ConversionRules rules = context.getConfiguration().getConversionRules();
        if (value == null) {
            throw new NullPointerException();
        }
        if (value instanceof StringValue && !(value instanceof AnyURIValue)) {
            try {
                return this.targetType.getTypedValue(value.getStringValueCS(), this.resolver, rules);
            } catch (ValidationException e) {
                e.setErrorCode("FORG0001");
                throw e;
            }
        }
        AtomicType label = value.getItemType();
        Iterable<? extends PlainType> memberTypes = this.targetType.getPlainMemberTypes();
        if (this.targetType.isPlainType()) {
            for (PlainType plainType : memberTypes) {
                if (!label.equals(plainType)) continue;
                return value;
            }
            for (PlainType plainType : memberTypes) {
                AtomicType t = label;
                while (t != null) {
                    if (t.equals(plainType)) {
                        return value;
                    }
                    t = t.getBaseType() instanceof AtomicType ? (AtomicType)t.getBaseType() : null;
                }
            }
        }
        for (PlainType plainType : memberTypes) {
            ConversionResult result;
            Converter c;
            if (!(plainType instanceof AtomicType) || (c = rules.getConverter(value.getItemType(), (AtomicType)plainType)) == null || !((result = c.convert(value)) instanceof AtomicValue)) continue;
            if (!this.targetType.isPlainType()) {
                ValidationFailure vf = this.targetType.checkAgainstFacets((AtomicValue)result, rules);
                if (vf != null) continue;
                return (AtomicValue)result;
            }
            return (AtomicValue)result;
        }
        throw new XPathException("Cannot convert the supplied value to " + this.targetType.getDescription(), "FORG0001");
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
        return this.cast(val, context);
    }

    public static AtomicSequence cast(AtomicValue value, UnionType targetType, NamespaceResolver nsResolver, ConversionRules rules) throws XPathException {
        if (value == null) {
            throw new NullPointerException();
        }
        if (value instanceof StringValue && !(value instanceof AnyURIValue)) {
            try {
                return targetType.getTypedValue(value.getStringValueCS(), nsResolver, rules);
            } catch (ValidationException e) {
                e.setErrorCode("FORG0001");
                throw e;
            }
        }
        AtomicType label = value.getItemType();
        Iterable<? extends PlainType> memberTypes = targetType.getPlainMemberTypes();
        for (PlainType plainType : memberTypes) {
            if (!label.equals(plainType)) continue;
            return value;
        }
        for (PlainType plainType : memberTypes) {
            AtomicType t = label;
            while (t != null) {
                if (t.equals(plainType)) {
                    return value;
                }
                t = t.getBaseType() instanceof AtomicType ? (AtomicType)t.getBaseType() : null;
            }
        }
        for (PlainType plainType : memberTypes) {
            ConversionResult result;
            Converter c;
            if (!(plainType instanceof AtomicType) || (c = rules.getConverter(value.getItemType(), (AtomicType)plainType)) == null || !((result = c.convert(value)) instanceof AtomicValue)) continue;
            return (AtomicValue)result;
        }
        throw new XPathException("Cannot convert the supplied value to " + targetType.getDescription(), "FORG0001");
    }
}


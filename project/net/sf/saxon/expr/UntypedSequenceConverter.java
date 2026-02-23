/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.AtomicSequenceConverter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AlphaCode;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.Converter;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.NumericType;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.UntypedAtomicValue;

public final class UntypedSequenceConverter
extends AtomicSequenceConverter {
    public UntypedSequenceConverter(Expression sequence, PlainType requiredItemType) {
        super(sequence, requiredItemType);
    }

    public static UntypedSequenceConverter makeUntypedSequenceConverter(Configuration config, Expression operand, PlainType requiredItemType) throws XPathException {
        Converter untypedConverter;
        UntypedSequenceConverter atomicSeqConverter = new UntypedSequenceConverter(operand, requiredItemType);
        ConversionRules rules = config.getConversionRules();
        if (requiredItemType.isNamespaceSensitive()) {
            throw new XPathException("Cannot convert untyped atomic values to a namespace-sensitive type", "XPTY0117");
        }
        if (requiredItemType.isAtomicType()) {
            untypedConverter = rules.getConverter(BuiltInAtomicType.UNTYPED_ATOMIC, (AtomicType)requiredItemType);
        } else if (requiredItemType == NumericType.getInstance()) {
            untypedConverter = rules.getConverter(BuiltInAtomicType.UNTYPED_ATOMIC, BuiltInAtomicType.DOUBLE);
            atomicSeqConverter.requiredItemType = BuiltInAtomicType.DOUBLE;
        } else {
            untypedConverter = new StringConverter.StringToUnionConverter(requiredItemType, rules);
        }
        UntypedConverter converter = new UntypedConverter(rules, untypedConverter);
        atomicSeqConverter.setConverter(converter);
        return atomicSeqConverter;
    }

    public static UntypedSequenceConverter makeUntypedSequenceRejector(Configuration config, final Expression operand, final PlainType requiredItemType) {
        UntypedSequenceConverter atomicSeqConverter = new UntypedSequenceConverter(operand, requiredItemType);
        ConversionRules rules = config.getConversionRules();
        Converter untypedConverter = new Converter(){

            @Override
            public ConversionResult convert(AtomicValue input) {
                ValidationFailure vf = new ValidationFailure("Implicit conversion of untypedAtomic value to " + requiredItemType.toString() + " is not allowed");
                vf.setErrorCode("XPTY0117");
                vf.setLocator(operand.getLocation());
                return vf;
            }
        };
        UntypedConverter converter = new UntypedConverter(rules, untypedConverter);
        atomicSeqConverter.setConverter(converter);
        return atomicSeqConverter;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Expression base;
        Expression e2 = super.typeCheck(visitor, contextInfo);
        if (e2 != this) {
            return e2;
        }
        TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
        if (th.relationship((base = this.getBaseExpression()).getItemType(), BuiltInAtomicType.UNTYPED_ATOMIC) == Affinity.DISJOINT || base.hasSpecialProperty(0x4000000)) {
            return this.getBaseExpression();
        }
        return this;
    }

    @Override
    public int computeSpecialProperties() {
        int p = super.computeSpecialProperties();
        return p | 0x800000 | 0x4000000;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        UntypedSequenceConverter atomicConverter = new UntypedSequenceConverter(this.getBaseExpression().copy(rebindings), this.getRequiredItemType());
        ExpressionTool.copyLocationInfo(this, atomicConverter);
        atomicConverter.setConverter(this.converter);
        atomicConverter.setRoleDiagnostic(this.getRoleDiagnostic());
        return atomicConverter;
    }

    @Override
    public ItemType getItemType() {
        if (this.getBaseExpression().getItemType() == BuiltInAtomicType.UNTYPED_ATOMIC) {
            return this.getRequiredItemType();
        }
        TypeHierarchy th = this.getConfiguration().getTypeHierarchy();
        return Type.getCommonSuperType(this.getRequiredItemType(), this.getBaseExpression().getItemType(), th);
    }

    @Override
    public int computeCardinality() {
        return this.getBaseExpression().getCardinality();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof UntypedSequenceConverter && this.getBaseExpression().isEqual(((UntypedSequenceConverter)other).getBaseExpression());
    }

    @Override
    public int computeHashCode() {
        return super.computeHashCode();
    }

    @Override
    protected String displayOperator(Configuration config) {
        return "convertUntyped";
    }

    @Override
    public String getExpressionName() {
        return "convertUntyped";
    }

    @Override
    public String toShortString() {
        return this.getBaseExpression().toShortString();
    }

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        destination.startElement("cvUntyped", this);
        destination.emitAttribute("to", AlphaCode.fromItemType(this.getRequiredItemType()));
        if (this.getRoleDiagnostic() != null) {
            destination.emitAttribute("diag", this.getRoleDiagnostic().save());
        }
        this.getBaseExpression().export(destination);
        destination.endElement();
    }

    public static class UntypedConverter
    extends Converter {
        Converter untypedConverter = null;

        public UntypedConverter(ConversionRules rules, Converter converter) {
            super(rules);
            this.untypedConverter = converter;
        }

        @Override
        public ConversionResult convert(AtomicValue input) {
            if (input instanceof UntypedAtomicValue) {
                return this.untypedConverter.convert(input);
            }
            return input;
        }
    }
}


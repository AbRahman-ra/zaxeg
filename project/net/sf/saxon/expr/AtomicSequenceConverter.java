/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.EarlyEvaluationContext;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemMappingFunction;
import net.sf.saxon.expr.ItemMappingIterator;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.UnaryExpression;
import net.sf.saxon.expr.UntypedSequenceConverter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AlphaCode;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.Converter;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;

public class AtomicSequenceConverter
extends UnaryExpression {
    public static ToStringMappingFunction TO_STRING_MAPPER = new ToStringMappingFunction();
    protected PlainType requiredItemType;
    protected Converter converter;
    private RoleDiagnostic roleDiagnostic;

    public AtomicSequenceConverter(Expression sequence, PlainType requiredItemType) {
        super(sequence);
        this.requiredItemType = requiredItemType;
    }

    public void allocateConverterStatically(Configuration config, boolean allowNull) {
        this.converter = this.allocateConverter(config, allowNull, this.getBaseExpression().getItemType());
    }

    public Converter allocateConverter(Configuration config, boolean allowNull) {
        return this.allocateConverter(config, allowNull, this.getBaseExpression().getItemType());
    }

    protected Converter getConverterDynamically(XPathContext context) {
        if (this.converter != null) {
            return this.converter;
        }
        return this.allocateConverter(context.getConfiguration(), false);
    }

    public Converter allocateConverter(Configuration config, boolean allowNull, ItemType sourceType) {
        final ConversionRules rules = config.getConversionRules();
        Converter converter = null;
        if (sourceType instanceof ErrorType) {
            converter = Converter.IdentityConverter.INSTANCE;
        } else if (!(sourceType instanceof AtomicType)) {
            converter = null;
        } else if (this.requiredItemType instanceof AtomicType) {
            converter = rules.getConverter((AtomicType)sourceType, (AtomicType)this.requiredItemType);
        } else if (((SimpleType)((Object)this.requiredItemType)).isUnionType()) {
            converter = new StringConverter.StringToUnionConverter(this.requiredItemType, rules);
        }
        if (converter == null && !allowNull) {
            converter = new Converter(rules){

                @Override
                public ConversionResult convert(AtomicValue input) {
                    Converter converter = rules.getConverter(input.getPrimitiveType(), (AtomicType)AtomicSequenceConverter.this.requiredItemType);
                    if (converter == null) {
                        return new ValidationFailure("Cannot convert value from " + input.getPrimitiveType() + " to " + AtomicSequenceConverter.this.requiredItemType);
                    }
                    return converter.convert(input);
                }
            };
        }
        return converter;
    }

    @Override
    protected OperandRole getOperandRole() {
        return OperandRole.ATOMIC_SEQUENCE;
    }

    public PlainType getRequiredItemType() {
        return this.requiredItemType;
    }

    public Converter getConverter() {
        return this.converter;
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public void setRoleDiagnostic(RoleDiagnostic role) {
        if (role != null && !"XPTY0004".equals(role.getErrorCode())) {
            this.roleDiagnostic = role;
        }
    }

    public RoleDiagnostic getRoleDiagnostic() {
        return this.roleDiagnostic;
    }

    @Override
    public Expression simplify() throws XPathException {
        Expression operand = this.getBaseExpression().simplify();
        this.setBaseExpression(operand);
        if (operand instanceof Literal && this.requiredItemType instanceof AtomicType) {
            if (Literal.isEmptySequence(operand)) {
                return operand;
            }
            Configuration config = this.getConfiguration();
            this.allocateConverterStatically(config, true);
            if (this.converter != null) {
                GroundedValue val = this.iterate(new EarlyEvaluationContext(config)).materialize();
                return Literal.makeLiteral(val, operand);
            }
        }
        return this;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.typeCheckChildren(visitor, contextInfo);
        Configuration config = visitor.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();
        Expression operand = this.getBaseExpression();
        if (th.isSubType(operand.getItemType(), this.requiredItemType)) {
            return operand;
        }
        if (this.converter == null) {
            this.allocateConverterStatically(config, true);
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Expression e = super.optimize(visitor, contextInfo);
        if (e != this) {
            return e;
        }
        if (this.getBaseExpression() instanceof UntypedSequenceConverter) {
            UntypedSequenceConverter asc = (UntypedSequenceConverter)this.getBaseExpression();
            ItemType ascType = asc.getItemType();
            if (ascType == this.requiredItemType) {
                return this.getBaseExpression();
            }
            if (!(this.requiredItemType != BuiltInAtomicType.STRING && this.requiredItemType != BuiltInAtomicType.UNTYPED_ATOMIC || ascType != BuiltInAtomicType.STRING && ascType != BuiltInAtomicType.UNTYPED_ATOMIC)) {
                UntypedSequenceConverter old = (UntypedSequenceConverter)this.getBaseExpression();
                UntypedSequenceConverter asc2 = new UntypedSequenceConverter(old.getBaseExpression(), this.requiredItemType);
                return asc2.typeCheck(visitor, contextInfo).optimize(visitor, contextInfo);
            }
        } else if (this.getBaseExpression() instanceof AtomicSequenceConverter) {
            AtomicSequenceConverter asc = (AtomicSequenceConverter)this.getBaseExpression();
            ItemType ascType = asc.getItemType();
            if (ascType == this.requiredItemType) {
                return this.getBaseExpression();
            }
            if (!(this.requiredItemType != BuiltInAtomicType.STRING && this.requiredItemType != BuiltInAtomicType.UNTYPED_ATOMIC || ascType != BuiltInAtomicType.STRING && ascType != BuiltInAtomicType.UNTYPED_ATOMIC)) {
                AtomicSequenceConverter old = (AtomicSequenceConverter)this.getBaseExpression();
                AtomicSequenceConverter asc2 = new AtomicSequenceConverter(old.getBaseExpression(), this.requiredItemType);
                return asc2.typeCheck(visitor, contextInfo).optimize(visitor, contextInfo);
            }
        }
        return this;
    }

    @Override
    public int getImplementationMethod() {
        return 2;
    }

    @Override
    public int computeSpecialProperties() {
        int p = super.computeSpecialProperties() | 0x800000;
        p = this.requiredItemType == BuiltInAtomicType.UNTYPED_ATOMIC ? (p &= 0xFBFFFFFF) : (p |= 0x4000000);
        return p;
    }

    @Override
    public String getStreamerName() {
        return "AtomicSequenceConverter";
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        AtomicSequenceConverter atomicConverter = new AtomicSequenceConverter(this.getBaseExpression().copy(rebindings), this.requiredItemType);
        ExpressionTool.copyLocationInfo(this, atomicConverter);
        atomicConverter.setConverter(this.converter);
        atomicConverter.setRoleDiagnostic(this.getRoleDiagnostic());
        return atomicConverter;
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        SequenceIterator base = this.getBaseExpression().iterate(context);
        Converter conv = this.getConverterDynamically(context);
        if (conv == Converter.ToStringConverter.INSTANCE) {
            return new ItemMappingIterator(base, TO_STRING_MAPPER, true);
        }
        AtomicSequenceMappingFunction mapper = new AtomicSequenceMappingFunction();
        mapper.setConverter(conv);
        if (this.roleDiagnostic != null) {
            mapper.setErrorCode(this.roleDiagnostic.getErrorCode());
        }
        return new ItemMappingIterator(base, mapper, true);
    }

    @Override
    public AtomicValue evaluateItem(XPathContext context) throws XPathException {
        Converter conv = this.getConverterDynamically(context);
        AtomicValue item = (AtomicValue)this.getBaseExpression().evaluateItem(context);
        if (item == null) {
            return null;
        }
        ConversionResult result = conv.convert(item);
        if (this.roleDiagnostic != null && result instanceof ValidationFailure) {
            ((ValidationFailure)result).setErrorCode(this.roleDiagnostic.getErrorCode());
        }
        return result.asAtomic();
    }

    @Override
    public ItemType getItemType() {
        return this.requiredItemType;
    }

    @Override
    public int computeCardinality() {
        return this.getBaseExpression().getCardinality();
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) && this.requiredItemType.equals(((AtomicSequenceConverter)other).requiredItemType);
    }

    @Override
    public int computeHashCode() {
        return super.computeHashCode() ^ this.requiredItemType.hashCode();
    }

    @Override
    public String getExpressionName() {
        return "convert";
    }

    @Override
    protected String displayOperator(Configuration config) {
        return "convert";
    }

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        destination.startElement("convert", this);
        destination.emitAttribute("from", AlphaCode.fromItemType(this.getBaseExpression().getItemType()));
        destination.emitAttribute("to", AlphaCode.fromItemType(this.requiredItemType));
        if (this.converter instanceof Converter.PromoterToDouble || this.converter instanceof Converter.PromoterToFloat) {
            destination.emitAttribute("flags", "p");
        }
        if (this.getRoleDiagnostic() != null) {
            destination.emitAttribute("diag", this.getRoleDiagnostic().save());
        }
        this.getBaseExpression().export(destination);
        destination.endElement();
    }

    public static class ToStringMappingFunction
    implements ItemMappingFunction {
        @Override
        public StringValue mapItem(Item item) {
            return StringValue.makeStringValue(item.getStringValueCS());
        }
    }

    public static class AtomicSequenceMappingFunction
    implements ItemMappingFunction {
        private Converter converter;
        private String errorCode;

        public void setConverter(Converter converter) {
            this.converter = converter;
        }

        public void setErrorCode(String code) {
            this.errorCode = code;
        }

        @Override
        public AtomicValue mapItem(Item item) throws XPathException {
            ConversionResult result = this.converter.convert((AtomicValue)item);
            if (this.errorCode != null && result instanceof ValidationFailure) {
                ((ValidationFailure)result).setErrorCode(this.errorCode);
            }
            return result.asAtomic();
        }
    }
}


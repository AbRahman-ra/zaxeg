/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import java.math.BigDecimal;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.BigIntegerValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DateValue;
import net.sf.saxon.value.DayTimeDurationValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.DurationValue;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.GDayValue;
import net.sf.saxon.value.GMonthDayValue;
import net.sf.saxon.value.GMonthValue;
import net.sf.saxon.value.GYearMonthValue;
import net.sf.saxon.value.GYearValue;
import net.sf.saxon.value.HexBinaryValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.NotationValue;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.TimeValue;
import net.sf.saxon.value.UntypedAtomicValue;
import net.sf.saxon.value.YearMonthDurationValue;

public abstract class Converter {
    private ConversionRules conversionRules;

    public static AtomicValue convert(AtomicValue value, AtomicType targetType, ConversionRules rules) throws ValidationException {
        Converter converter = rules.getConverter(value.getPrimitiveType(), targetType);
        if (converter == null) {
            ValidationFailure ve = new ValidationFailure("Cannot convert value from " + value.getPrimitiveType() + " to " + targetType);
            ve.setErrorCode("FORG0001");
            throw ve.makeException();
        }
        return converter.convert(value).asAtomic();
    }

    protected Converter() {
    }

    protected Converter(ConversionRules rules) {
        this.setConversionRules(rules);
    }

    public abstract ConversionResult convert(AtomicValue var1);

    public final void setConversionRules(ConversionRules rules) {
        this.conversionRules = rules;
    }

    public final ConversionRules getConversionRules() {
        return this.conversionRules;
    }

    public boolean isAlwaysSuccessful() {
        return false;
    }

    public Converter setNamespaceResolver(NamespaceResolver resolver) {
        return this;
    }

    public NamespaceResolver getNamespaceResolver() {
        return null;
    }

    public static class PromoterToFloat
    extends Converter {
        private StringConverter stringToFloat = null;

        @Override
        public ConversionResult convert(AtomicValue input) {
            if (input instanceof FloatValue) {
                return input;
            }
            if (input instanceof DoubleValue) {
                ValidationFailure err = new ValidationFailure("Cannot promote from xs:double to xs:float");
                err.setErrorCode("XPTY0004");
                return err;
            }
            if (input instanceof NumericValue) {
                return new FloatValue((float)((NumericValue)input).getDoubleValue());
            }
            if (input instanceof UntypedAtomicValue) {
                if (this.stringToFloat == null) {
                    this.stringToFloat = BuiltInAtomicType.FLOAT.getStringConverter(this.getConversionRules());
                }
                return this.stringToFloat.convert(input);
            }
            ValidationFailure err = new ValidationFailure("Cannot promote non-numeric value to xs:double");
            err.setErrorCode("XPTY0004");
            return err;
        }
    }

    public static class PromoterToDouble
    extends Converter {
        private StringConverter stringToDouble = null;

        @Override
        public ConversionResult convert(AtomicValue input) {
            if (input instanceof DoubleValue) {
                return input;
            }
            if (input instanceof NumericValue) {
                return new DoubleValue(((NumericValue)input).getDoubleValue());
            }
            if (input instanceof UntypedAtomicValue) {
                if (this.stringToDouble == null) {
                    this.stringToDouble = BuiltInAtomicType.DOUBLE.getStringConverter(this.getConversionRules());
                }
                return this.stringToDouble.convert(input);
            }
            ValidationFailure err = new ValidationFailure("Cannot promote non-numeric value to xs:double");
            err.setErrorCode("XPTY0004");
            return err;
        }
    }

    public static class QNameToNotation
    extends UnfailingConverter {
        public static final QNameToNotation INSTANCE = new QNameToNotation();

        @Override
        public NotationValue convert(AtomicValue input) {
            return new NotationValue(((QNameValue)input).getStructuredQName(), BuiltInAtomicType.NOTATION);
        }
    }

    public static class NotationToQName
    extends UnfailingConverter {
        public static final NotationToQName INSTANCE = new NotationToQName();

        @Override
        public QNameValue convert(AtomicValue input) {
            return new QNameValue(((NotationValue)input).getStructuredQName(), BuiltInAtomicType.QNAME);
        }
    }

    public static class HexBinaryToBase64Binary
    extends UnfailingConverter {
        public static final HexBinaryToBase64Binary INSTANCE = new HexBinaryToBase64Binary();

        @Override
        public Base64BinaryValue convert(AtomicValue input) {
            return new Base64BinaryValue(((HexBinaryValue)input).getBinaryValue());
        }
    }

    public static class Base64BinaryToHexBinary
    extends UnfailingConverter {
        public static final Base64BinaryToHexBinary INSTANCE = new Base64BinaryToHexBinary();

        @Override
        public HexBinaryValue convert(AtomicValue input) {
            return new HexBinaryValue(((Base64BinaryValue)input).getBinaryValue());
        }
    }

    public static class NumericToBoolean
    extends UnfailingConverter {
        public static final NumericToBoolean INSTANCE = new NumericToBoolean();

        @Override
        public BooleanValue convert(AtomicValue input) {
            return BooleanValue.get(((NumericValue)input).effectiveBooleanValue());
        }
    }

    public static class DateTimeToTime
    extends UnfailingConverter {
        public static final DateTimeToTime INSTANCE = new DateTimeToTime();

        @Override
        public TimeValue convert(AtomicValue input) {
            DateTimeValue dt = (DateTimeValue)input;
            return new TimeValue(dt.getHour(), dt.getMinute(), dt.getSecond(), dt.getNanosecond(), dt.getTimezoneInMinutes(), "");
        }
    }

    public static class DateTimeToGDay
    extends UnfailingConverter {
        public static final DateTimeToGDay INSTANCE = new DateTimeToGDay();

        @Override
        public GDayValue convert(AtomicValue input) {
            DateTimeValue dt = (DateTimeValue)input;
            return new GDayValue(dt.getDay(), dt.getTimezoneInMinutes());
        }
    }

    public static class DateTimeToGMonthDay
    extends UnfailingConverter {
        public static final DateTimeToGMonthDay INSTANCE = new DateTimeToGMonthDay();

        @Override
        public GMonthDayValue convert(AtomicValue input) {
            DateTimeValue dt = (DateTimeValue)input;
            return new GMonthDayValue(dt.getMonth(), dt.getDay(), dt.getTimezoneInMinutes());
        }
    }

    public static class DateTimeToGYear
    extends UnfailingConverter {
        public static final DateTimeToGYear INSTANCE = new DateTimeToGYear();

        @Override
        public GYearValue convert(AtomicValue input) {
            DateTimeValue dt = (DateTimeValue)input;
            return new GYearValue(dt.getYear(), dt.getTimezoneInMinutes(), dt.isXsd10Rules());
        }
    }

    public static class DateTimeToGYearMonth
    extends UnfailingConverter {
        public static final DateTimeToGYearMonth INSTANCE = new DateTimeToGYearMonth();

        @Override
        public GYearMonthValue convert(AtomicValue input) {
            DateTimeValue dt = (DateTimeValue)input;
            return new GYearMonthValue(dt.getYear(), dt.getMonth(), dt.getTimezoneInMinutes(), dt.isXsd10Rules());
        }
    }

    public static class DateTimeToGMonth
    extends UnfailingConverter {
        public static final DateTimeToGMonth INSTANCE = new DateTimeToGMonth();

        @Override
        public GMonthValue convert(AtomicValue input) {
            DateTimeValue dt = (DateTimeValue)input;
            return new GMonthValue(dt.getMonth(), dt.getTimezoneInMinutes());
        }
    }

    public static class DateTimeToDate
    extends UnfailingConverter {
        public static final DateTimeToDate INSTANCE = new DateTimeToDate();

        @Override
        public DateValue convert(AtomicValue input) {
            DateTimeValue dt = (DateTimeValue)input;
            return new DateValue(dt.getYear(), dt.getMonth(), dt.getDay(), dt.getTimezoneInMinutes(), dt.isXsd10Rules());
        }
    }

    public static class DateToDateTime
    extends UnfailingConverter {
        public static final DateToDateTime INSTANCE = new DateToDateTime();

        @Override
        public DateTimeValue convert(AtomicValue input) {
            return ((DateValue)input).toDateTime();
        }
    }

    public static class DurationToYearMonthDuration
    extends UnfailingConverter {
        public static final DurationToYearMonthDuration INSTANCE = new DurationToYearMonthDuration();

        @Override
        public YearMonthDurationValue convert(AtomicValue input) {
            return YearMonthDurationValue.fromMonths(((DurationValue)input).getTotalMonths());
        }
    }

    public static class DurationToDayTimeDuration
    extends UnfailingConverter {
        public static final DurationToDayTimeDuration INSTANCE = new DurationToDayTimeDuration();

        @Override
        public DayTimeDurationValue convert(AtomicValue duration) {
            DurationValue d = (DurationValue)duration;
            if (d.signum() < 0) {
                return new DayTimeDurationValue(-d.getDays(), -d.getHours(), -d.getMinutes(), -d.getSeconds(), -d.getNanoseconds());
            }
            return new DayTimeDurationValue(d.getDays(), d.getHours(), d.getMinutes(), d.getSeconds(), d.getNanoseconds());
        }
    }

    public static class BooleanToInteger
    extends UnfailingConverter {
        public static final BooleanToInteger INSTANCE = new BooleanToInteger();

        @Override
        public Int64Value convert(AtomicValue input) {
            return ((BooleanValue)input).getBooleanValue() ? Int64Value.PLUS_ONE : Int64Value.ZERO;
        }
    }

    public static class NumericToInteger
    extends Converter {
        public static final NumericToInteger INSTANCE = new NumericToInteger();

        @Override
        public ConversionResult convert(AtomicValue input) {
            NumericValue in = (NumericValue)input;
            try {
                if (in instanceof IntegerValue) {
                    return in;
                }
                if (in instanceof DoubleValue) {
                    return IntegerValue.makeIntegerValue((DoubleValue)in);
                }
                if (in instanceof FloatValue) {
                    return IntegerValue.makeIntegerValue(new DoubleValue(in.getDoubleValue()));
                }
                return BigIntegerValue.makeIntegerValue(in.getDecimalValue().toBigInteger());
            } catch (ValidationException e) {
                return e.getValidationFailure();
            }
        }
    }

    public static class DecimalToInteger
    extends UnfailingConverter {
        public static final DecimalToInteger INSTANCE = new DecimalToInteger();

        @Override
        public IntegerValue convert(AtomicValue input) {
            if (input instanceof IntegerValue) {
                return (IntegerValue)input;
            }
            return BigIntegerValue.makeIntegerValue(((BigDecimalValue)input).getDecimalValue().toBigInteger());
        }
    }

    public static class FloatToInteger
    extends Converter {
        public static final FloatToInteger INSTANCE = new FloatToInteger();

        @Override
        public ConversionResult convert(AtomicValue input) {
            return IntegerValue.makeIntegerValue(new DoubleValue(((FloatValue)input).getDoubleValue()));
        }
    }

    public static class DoubleToInteger
    extends Converter {
        public static final DoubleToInteger INSTANCE = new DoubleToInteger();

        @Override
        public ConversionResult convert(AtomicValue input) {
            return IntegerValue.makeIntegerValue((DoubleValue)input);
        }
    }

    public static class BooleanToDecimal
    extends UnfailingConverter {
        public static final BooleanToDecimal INSTANCE = new BooleanToDecimal();

        @Override
        public BigDecimalValue convert(AtomicValue input) {
            return ((BooleanValue)input).getBooleanValue() ? BigDecimalValue.ONE : BigDecimalValue.ZERO;
        }
    }

    public static class NumericToDecimal
    extends Converter {
        public static final NumericToDecimal INSTANCE = new NumericToDecimal();

        @Override
        public ConversionResult convert(AtomicValue input) {
            try {
                BigDecimal decimal = ((NumericValue)input).getDecimalValue();
                return new BigDecimalValue(decimal);
            } catch (ValidationException e) {
                return e.getValidationFailure();
            }
        }
    }

    public static class IntegerToDecimal
    extends UnfailingConverter {
        public static final IntegerToDecimal INSTANCE = new IntegerToDecimal();

        @Override
        public BigDecimalValue convert(AtomicValue input) {
            if (input instanceof Int64Value) {
                return new BigDecimalValue(((Int64Value)input).longValue());
            }
            return new BigDecimalValue(((BigIntegerValue)input).asDecimal());
        }
    }

    public static class FloatToDecimal
    extends Converter {
        public static final FloatToDecimal INSTANCE = new FloatToDecimal();

        @Override
        public ConversionResult convert(AtomicValue input) {
            try {
                return new BigDecimalValue(((FloatValue)input).getFloatValue());
            } catch (ValidationException e) {
                return e.getValidationFailure();
            }
        }
    }

    public static class DoubleToDecimal
    extends Converter {
        public static final DoubleToDecimal INSTANCE = new DoubleToDecimal();

        @Override
        public ConversionResult convert(AtomicValue input) {
            try {
                return new BigDecimalValue(((DoubleValue)input).getDoubleValue());
            } catch (ValidationException e) {
                return e.getValidationFailure();
            }
        }
    }

    public static class BooleanToDouble
    extends UnfailingConverter {
        public static final BooleanToDouble INSTANCE = new BooleanToDouble();

        @Override
        public DoubleValue convert(AtomicValue input) {
            return new DoubleValue(((BooleanValue)input).getBooleanValue() ? 1.0 : 0.0);
        }
    }

    public static class NumericToDouble
    extends UnfailingConverter {
        public static final NumericToDouble INSTANCE = new NumericToDouble();

        @Override
        public DoubleValue convert(AtomicValue input) {
            if (input instanceof DoubleValue) {
                return (DoubleValue)input;
            }
            return new DoubleValue(((NumericValue)input).getDoubleValue());
        }
    }

    public static class BooleanToFloat
    extends UnfailingConverter {
        public static final BooleanToFloat INSTANCE = new BooleanToFloat();

        @Override
        public FloatValue convert(AtomicValue input) {
            return new FloatValue(((BooleanValue)input).getBooleanValue() ? 1.0f : 0.0f);
        }
    }

    public static class NumericToFloat
    extends UnfailingConverter {
        public static final NumericToFloat INSTANCE = new NumericToFloat();

        @Override
        public FloatValue convert(AtomicValue input) {
            return new FloatValue(((NumericValue)input).getFloatValue());
        }
    }

    public static class ToStringConverter
    extends UnfailingConverter {
        public static final ToStringConverter INSTANCE = new ToStringConverter();

        @Override
        public StringValue convert(AtomicValue input) {
            return new StringValue(input.getStringValueCS());
        }
    }

    public static class ToUntypedAtomicConverter
    extends UnfailingConverter {
        public static final ToUntypedAtomicConverter INSTANCE = new ToUntypedAtomicConverter();

        @Override
        public UntypedAtomicValue convert(AtomicValue input) {
            return new UntypedAtomicValue(input.getStringValueCS());
        }
    }

    public static class TwoPhaseConverter
    extends Converter {
        private final Converter phaseOne;
        private final Converter phaseTwo;

        public TwoPhaseConverter(Converter phaseOne, Converter phaseTwo) {
            this.phaseOne = phaseOne;
            this.phaseTwo = phaseTwo;
        }

        public static TwoPhaseConverter makeTwoPhaseConverter(AtomicType inputType, AtomicType viaType, AtomicType outputType, ConversionRules rules) {
            return new TwoPhaseConverter(rules.getConverter(inputType, viaType), rules.getConverter(viaType, outputType));
        }

        @Override
        public Converter setNamespaceResolver(NamespaceResolver resolver) {
            return new TwoPhaseConverter(this.phaseOne.setNamespaceResolver(resolver), this.phaseTwo.setNamespaceResolver(resolver));
        }

        @Override
        public ConversionResult convert(AtomicValue input) {
            ConversionResult temp = this.phaseOne.convert(input);
            if (temp instanceof ValidationFailure) {
                return temp;
            }
            AtomicValue aTemp = (AtomicValue)temp;
            if (this.phaseTwo instanceof DownCastingConverter) {
                return ((DownCastingConverter)this.phaseTwo).convert(aTemp, aTemp.getCanonicalLexicalRepresentation());
            }
            return this.phaseTwo.convert(aTemp);
        }
    }

    public static class DownCastingConverter
    extends Converter {
        private final AtomicType newType;

        public DownCastingConverter(AtomicType annotation, ConversionRules rules) {
            this.newType = annotation;
            this.setConversionRules(rules);
        }

        public AtomicType getTargetType() {
            return this.newType;
        }

        @Override
        public ConversionResult convert(AtomicValue input) {
            return this.convert(input, input.getCanonicalLexicalRepresentation());
        }

        public ConversionResult convert(AtomicValue input, CharSequence lexicalForm) {
            ValidationFailure f = this.newType.validate(input, lexicalForm, this.getConversionRules());
            if (f == null) {
                return input.copyAsSubType(this.newType);
            }
            return f;
        }

        public ValidationFailure validate(AtomicValue input, CharSequence lexicalForm) {
            return this.newType.validate(input, lexicalForm, this.getConversionRules());
        }
    }

    public static class UpCastingConverter
    extends UnfailingConverter {
        private final AtomicType newTypeAnnotation;

        public UpCastingConverter(AtomicType annotation) {
            this.newTypeAnnotation = annotation;
        }

        @Override
        public AtomicValue convert(AtomicValue input) {
            return input.copyAsSubType(this.newTypeAnnotation);
        }
    }

    public static class IdentityConverter
    extends Converter {
        public static final IdentityConverter INSTANCE = new IdentityConverter();

        @Override
        public ConversionResult convert(AtomicValue input) {
            return input;
        }

        @Override
        public boolean isAlwaysSuccessful() {
            return true;
        }
    }

    public static abstract class UnfailingConverter
    extends Converter {
        @Override
        public abstract AtomicValue convert(AtomicValue var1);

        @Override
        public final boolean isAlwaysSuccessful() {
            return true;
        }
    }
}


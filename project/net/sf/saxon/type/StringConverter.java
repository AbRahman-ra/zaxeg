/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import java.util.regex.Pattern;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.QNameException;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.Converter;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.UnionType;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.BigIntegerValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DateValue;
import net.sf.saxon.value.DayTimeDurationValue;
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
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.TimeValue;
import net.sf.saxon.value.UntypedAtomicValue;
import net.sf.saxon.value.Whitespace;
import net.sf.saxon.value.YearMonthDurationValue;

public abstract class StringConverter
extends Converter {
    protected StringConverter() {
    }

    protected StringConverter(ConversionRules rules) {
        super(rules);
    }

    public abstract ConversionResult convertString(CharSequence var1);

    public ValidationFailure validate(CharSequence input) {
        ConversionResult result = this.convertString(input);
        return result instanceof ValidationFailure ? (ValidationFailure)result : null;
    }

    @Override
    public ConversionResult convert(AtomicValue input) {
        return this.convertString(input.getStringValueCS());
    }

    public static class StringToUnionConverter
    extends StringConverter {
        PlainType targetType;
        ConversionRules rules;

        public StringToUnionConverter(PlainType targetType, ConversionRules rules) {
            if (!targetType.isPlainType()) {
                throw new IllegalArgumentException();
            }
            if (targetType.isNamespaceSensitive()) {
                throw new IllegalArgumentException();
            }
            this.targetType = targetType;
            this.rules = rules;
        }

        @Override
        public ConversionResult convertString(CharSequence input) {
            try {
                return ((UnionType)((Object)this.targetType)).getTypedValue(input, null, this.rules).head();
            } catch (ValidationException err) {
                return err.getValidationFailure();
            }
        }
    }

    public static class StringToAnyURI
    extends StringConverter {
        public StringToAnyURI(ConversionRules rules) {
            super(rules);
        }

        @Override
        public ConversionResult convertString(CharSequence input) {
            if (this.getConversionRules().isValidURI(input)) {
                return new AnyURIValue(input);
            }
            return new ValidationFailure("Invalid URI: " + input);
        }

        @Override
        public ValidationFailure validate(CharSequence input) {
            if (this.getConversionRules().isValidURI(input)) {
                return null;
            }
            return new ValidationFailure("Invalid URI: " + input);
        }
    }

    public static class StringToNotation
    extends StringConverter {
        private NamespaceResolver nsResolver;

        public StringToNotation(ConversionRules rules) {
            super(rules);
        }

        @Override
        public StringToNotation setNamespaceResolver(NamespaceResolver resolver) {
            StringToNotation c = new StringToNotation(this.getConversionRules());
            c.nsResolver = resolver;
            return c;
        }

        @Override
        public NamespaceResolver getNamespaceResolver() {
            return this.nsResolver;
        }

        @Override
        public ConversionResult convertString(CharSequence input) {
            if (this.getNamespaceResolver() == null) {
                throw new UnsupportedOperationException("Cannot validate a NOTATION without a namespace resolver");
            }
            try {
                String[] parts = NameChecker.getQNameParts(Whitespace.trimWhitespace(input));
                String uri = this.getNamespaceResolver().getURIForPrefix(parts[0], true);
                if (uri == null) {
                    return new ValidationFailure("Namespace prefix " + Err.wrap(parts[0]) + " has not been declared");
                }
                if (!this.getConversionRules().isDeclaredNotation(uri, parts[1])) {
                    return new ValidationFailure("Notation {" + uri + "}" + parts[1] + " is not declared in the schema");
                }
                return new NotationValue(parts[0], uri, parts[1], false);
            } catch (QNameException err) {
                return new ValidationFailure("Invalid lexical QName " + Err.wrap(input));
            } catch (XPathException err) {
                return new ValidationFailure(err.getMessage());
            }
        }
    }

    public static class StringToQName
    extends StringConverter {
        private NamespaceResolver nsResolver;

        public StringToQName(ConversionRules rules) {
            super(rules);
        }

        @Override
        public StringToQName setNamespaceResolver(NamespaceResolver resolver) {
            StringToQName c = new StringToQName(this.getConversionRules());
            c.nsResolver = resolver;
            return c;
        }

        @Override
        public NamespaceResolver getNamespaceResolver() {
            return this.nsResolver;
        }

        @Override
        public ConversionResult convertString(CharSequence input) {
            if (this.nsResolver == null) {
                throw new UnsupportedOperationException("Cannot validate a QName without a namespace resolver");
            }
            try {
                String[] parts = NameChecker.getQNameParts(Whitespace.trimWhitespace(input));
                String uri = this.nsResolver.getURIForPrefix(parts[0], true);
                if (uri == null) {
                    ValidationFailure failure = new ValidationFailure("Namespace prefix " + Err.wrap(parts[0]) + " has not been declared");
                    failure.setErrorCode("FONS0004");
                    return failure;
                }
                return new QNameValue(parts[0], uri, parts[1], BuiltInAtomicType.QNAME, false);
            } catch (QNameException err) {
                return new ValidationFailure("Invalid lexical QName " + Err.wrap(input));
            } catch (XPathException err) {
                return new ValidationFailure(err.getMessage());
            }
        }
    }

    public static class StringToBase64Binary
    extends StringConverter {
        public static final StringToBase64Binary INSTANCE = new StringToBase64Binary();

        @Override
        public ConversionResult convertString(CharSequence input) {
            try {
                return new Base64BinaryValue(input);
            } catch (XPathException e) {
                return ValidationFailure.fromException(e);
            }
        }
    }

    public static class StringToHexBinary
    extends StringConverter {
        public static final StringToHexBinary INSTANCE = new StringToHexBinary();

        @Override
        public ConversionResult convertString(CharSequence input) {
            try {
                return new HexBinaryValue(input);
            } catch (XPathException e) {
                return ValidationFailure.fromException(e);
            }
        }
    }

    public static class StringToBoolean
    extends StringConverter {
        public static final StringToBoolean INSTANCE = new StringToBoolean();

        @Override
        public ConversionResult convertString(CharSequence input) {
            return BooleanValue.fromString(input);
        }
    }

    public static class StringToTime
    extends StringConverter {
        public static final StringToTime INSTANCE = new StringToTime();

        @Override
        public ConversionResult convertString(CharSequence input) {
            return TimeValue.makeTimeValue(input);
        }
    }

    public static class StringToGDay
    extends StringConverter {
        public static final StringToGDay INSTANCE = new StringToGDay();

        @Override
        public ConversionResult convertString(CharSequence input) {
            return GDayValue.makeGDayValue(input);
        }
    }

    public static class StringToGMonthDay
    extends StringConverter {
        public static final StringToGMonthDay INSTANCE = new StringToGMonthDay();

        @Override
        public ConversionResult convertString(CharSequence input) {
            return GMonthDayValue.makeGMonthDayValue(input);
        }
    }

    public static class StringToGYear
    extends StringConverter {
        public StringToGYear(ConversionRules rules) {
            super(rules);
        }

        @Override
        public ConversionResult convertString(CharSequence input) {
            return GYearValue.makeGYearValue(input, this.getConversionRules());
        }
    }

    public static class StringToGYearMonth
    extends StringConverter {
        public StringToGYearMonth(ConversionRules rules) {
            super(rules);
        }

        @Override
        public ConversionResult convertString(CharSequence input) {
            return GYearMonthValue.makeGYearMonthValue(input, this.getConversionRules());
        }
    }

    public static class StringToGMonth
    extends StringConverter {
        public static final StringToGMonth INSTANCE = new StringToGMonth();

        @Override
        public ConversionResult convertString(CharSequence input) {
            return GMonthValue.makeGMonthValue(input);
        }
    }

    public static class StringToDate
    extends StringConverter {
        public StringToDate(ConversionRules rules) {
            super(rules);
        }

        @Override
        public ConversionResult convertString(CharSequence input) {
            return DateValue.makeDateValue(input, this.getConversionRules());
        }
    }

    public static class StringToDateTimeStamp
    extends StringConverter {
        public StringToDateTimeStamp(ConversionRules rules) {
            super(rules);
        }

        @Override
        public ConversionResult convertString(CharSequence input) {
            ConversionResult val = DateTimeValue.makeDateTimeValue(input, this.getConversionRules());
            if (val instanceof DateTimeValue) {
                if (!((DateTimeValue)val).hasTimezone()) {
                    return new ValidationFailure("Supplied DateTimeStamp value " + input + " has no time zone");
                }
                ((DateTimeValue)val).setTypeLabel(BuiltInAtomicType.DATE_TIME_STAMP);
            }
            return val;
        }
    }

    public static class StringToDateTime
    extends StringConverter {
        public StringToDateTime(ConversionRules rules) {
            super(rules);
        }

        @Override
        public ConversionResult convertString(CharSequence input) {
            return DateTimeValue.makeDateTimeValue(input, this.getConversionRules());
        }
    }

    public static class StringToYearMonthDuration
    extends StringConverter {
        public static final StringToYearMonthDuration INSTANCE = new StringToYearMonthDuration();

        @Override
        public ConversionResult convertString(CharSequence input) {
            return YearMonthDurationValue.makeYearMonthDurationValue(input);
        }
    }

    public static class StringToDayTimeDuration
    extends StringConverter {
        public static final StringToDayTimeDuration INSTANCE = new StringToDayTimeDuration();

        @Override
        public ConversionResult convertString(CharSequence input) {
            return DayTimeDurationValue.makeDayTimeDurationValue(input);
        }
    }

    public static class StringToDuration
    extends StringConverter {
        public static final StringToDuration INSTANCE = new StringToDuration();

        @Override
        public ConversionResult convertString(CharSequence input) {
            return DurationValue.makeDuration(input);
        }
    }

    public static class StringToIntegerSubtype
    extends StringConverter {
        BuiltInAtomicType targetType;

        public StringToIntegerSubtype(BuiltInAtomicType targetType) {
            this.targetType = targetType;
        }

        @Override
        public ConversionResult convertString(CharSequence input) {
            ConversionResult iv = IntegerValue.stringToInteger(input);
            if (iv instanceof Int64Value) {
                boolean ok = IntegerValue.checkRange(((Int64Value)iv).longValue(), this.targetType);
                if (ok) {
                    return ((Int64Value)iv).copyAsSubType(this.targetType);
                }
                return new ValidationFailure("Integer value is out of range for type " + this.targetType);
            }
            if (iv instanceof BigIntegerValue) {
                boolean ok = IntegerValue.checkBigRange(((BigIntegerValue)iv).asBigInteger(), this.targetType);
                if (ok) {
                    ((BigIntegerValue)iv).setTypeLabel(this.targetType);
                    return iv;
                }
                return new ValidationFailure("Integer value is out of range for type " + this.targetType);
            }
            assert (iv instanceof ValidationFailure);
            return iv;
        }
    }

    public static class StringToInteger
    extends StringConverter {
        public static final StringToInteger INSTANCE = new StringToInteger();

        public ConversionResult convert(StringValue input) {
            return IntegerValue.stringToInteger(input.getStringValueCS());
        }

        @Override
        public ConversionResult convertString(CharSequence input) {
            return IntegerValue.stringToInteger(input);
        }

        @Override
        public ValidationFailure validate(CharSequence input) {
            return IntegerValue.castableAsInteger(input);
        }
    }

    public static class StringToDecimal
    extends StringConverter {
        public static final StringToDecimal INSTANCE = new StringToDecimal();

        @Override
        public ConversionResult convertString(CharSequence input) {
            return BigDecimalValue.makeDecimalValue(input, true);
        }

        @Override
        public ValidationFailure validate(CharSequence input) {
            if (BigDecimalValue.castableAsDecimal(input)) {
                return null;
            }
            return new ValidationFailure("Cannot convert string to decimal: " + input);
        }
    }

    public static class StringToFloat
    extends StringConverter {
        public StringToFloat(ConversionRules rules) {
            super(rules);
        }

        @Override
        public ConversionResult convertString(CharSequence input) {
            try {
                float flt = (float)this.getConversionRules().getStringToDoubleConverter().stringToNumber(input);
                return new FloatValue(flt);
            } catch (NumberFormatException err) {
                ValidationFailure ve = new ValidationFailure("Cannot convert string to float: " + input);
                ve.setErrorCode("FORG0001");
                return ve;
            }
        }
    }

    public static class StringToDerivedStringSubtype
    extends StringConverter {
        AtomicType targetType;
        StringConverter builtInValidator;
        int whitespaceAction;

        public StringToDerivedStringSubtype(ConversionRules rules, AtomicType targetType) {
            super(rules);
            this.targetType = targetType;
            this.whitespaceAction = targetType.getWhitespaceAction();
            this.builtInValidator = ((AtomicType)targetType.getBuiltInBaseType()).getStringConverter(rules);
        }

        @Override
        public ConversionResult convertString(CharSequence input) {
            CharSequence cs = Whitespace.applyWhitespaceNormalization(this.whitespaceAction, input);
            ValidationFailure f = this.builtInValidator.validate(cs);
            if (f != null) {
                return f;
            }
            try {
                cs = this.targetType.preprocess(cs);
            } catch (ValidationException err) {
                return err.getValidationFailure();
            }
            StringValue sv = new StringValue(cs);
            f = this.targetType.validate(sv, cs, this.getConversionRules());
            if (f == null) {
                sv.setTypeLabel(this.targetType);
                return sv;
            }
            return f;
        }
    }

    public static class StringToStringSubtype
    extends StringConverter {
        AtomicType targetType;
        int whitespaceAction;

        public StringToStringSubtype(ConversionRules rules, AtomicType targetType) {
            super(rules);
            this.targetType = targetType;
            this.whitespaceAction = targetType.getWhitespaceAction();
        }

        @Override
        public ConversionResult convertString(CharSequence input) {
            CharSequence cs = Whitespace.applyWhitespaceNormalization(this.whitespaceAction, input);
            try {
                cs = this.targetType.preprocess(cs);
            } catch (ValidationException err) {
                return err.getValidationFailure();
            }
            StringValue sv = new StringValue(cs);
            ValidationFailure f = this.targetType.validate(sv, cs, this.getConversionRules());
            if (f == null) {
                sv.setTypeLabel(this.targetType);
                return sv;
            }
            return f;
        }

        @Override
        public ValidationFailure validate(CharSequence input) {
            CharSequence cs = Whitespace.applyWhitespaceNormalization(this.whitespaceAction, input);
            try {
                cs = this.targetType.preprocess(cs);
            } catch (ValidationException err) {
                return err.getValidationFailure();
            }
            return this.targetType.validate(new StringValue(cs), cs, this.getConversionRules());
        }
    }

    public static class StringToName
    extends StringToNCName {
        public static final StringToName INSTANCE = new StringToName();

        public StringToName() {
            super(BuiltInAtomicType.NAME);
        }

        @Override
        public ConversionResult convertString(CharSequence input) {
            ValidationFailure vf = this.validate(input);
            if (vf == null) {
                return new StringValue(Whitespace.trimWhitespace(input), BuiltInAtomicType.NAME);
            }
            return vf;
        }

        @Override
        public ValidationFailure validate(CharSequence input) {
            CharSequence trimmed = Whitespace.trimWhitespace(input);
            if (NameChecker.isValidNCName(trimmed)) {
                return null;
            }
            FastStringBuffer buff = new FastStringBuffer(trimmed.length());
            buff.cat(trimmed);
            for (int i = 0; i < buff.length(); ++i) {
                if (buff.charAt(i) != ':') continue;
                buff.setCharAt(i, '_');
            }
            if (NameChecker.isValidNCName(buff)) {
                return null;
            }
            return new ValidationFailure("The value '" + trimmed + "' is not a valid xs:Name");
        }
    }

    public static class StringToNMTOKEN
    extends StringConverter {
        public static final StringToNMTOKEN INSTANCE = new StringToNMTOKEN();

        @Override
        public ConversionResult convertString(CharSequence input) {
            CharSequence trimmed = Whitespace.trimWhitespace(input);
            if (NameChecker.isValidNmtoken(trimmed)) {
                return new StringValue(trimmed, BuiltInAtomicType.NMTOKEN);
            }
            return new ValidationFailure("The value '" + input + "' is not a valid xs:NMTOKEN");
        }

        @Override
        public ValidationFailure validate(CharSequence input) {
            if (NameChecker.isValidNmtoken(Whitespace.trimWhitespace(input))) {
                return null;
            }
            return new ValidationFailure("The value '" + input + "' is not a valid xs:NMTOKEN");
        }
    }

    public static class StringToNCName
    extends StringConverter {
        public static final StringToNCName TO_ID = new StringToNCName(BuiltInAtomicType.ID);
        public static final StringToNCName TO_ENTITY = new StringToNCName(BuiltInAtomicType.ENTITY);
        public static final StringToNCName TO_NCNAME = new StringToNCName(BuiltInAtomicType.NCNAME);
        public static final StringToNCName TO_IDREF = new StringToNCName(BuiltInAtomicType.IDREF);
        AtomicType targetType;

        public StringToNCName(AtomicType targetType) {
            this.targetType = targetType;
        }

        @Override
        public ConversionResult convertString(CharSequence input) {
            CharSequence trimmed = Whitespace.trimWhitespace(input);
            if (NameChecker.isValidNCName(trimmed)) {
                return new StringValue(trimmed, this.targetType);
            }
            return new ValidationFailure("The value '" + input + "' is not a valid " + this.targetType.getDisplayName());
        }

        @Override
        public ValidationFailure validate(CharSequence input) {
            if (NameChecker.isValidNCName(Whitespace.trimWhitespace(input))) {
                return null;
            }
            return new ValidationFailure("The value '" + input + "' is not a valid " + this.targetType.getDisplayName());
        }
    }

    public static class StringToLanguage
    extends StringConverter {
        private static final Pattern regex = Pattern.compile("[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*");
        public static final StringToLanguage INSTANCE = new StringToLanguage();

        @Override
        public ConversionResult convertString(CharSequence input) {
            CharSequence trimmed = Whitespace.trimWhitespace(input);
            if (!regex.matcher(trimmed).matches()) {
                return new ValidationFailure("The value '" + input + "' is not a valid xs:language");
            }
            return new StringValue(trimmed, BuiltInAtomicType.LANGUAGE);
        }

        @Override
        public ValidationFailure validate(CharSequence input) {
            if (regex.matcher(Whitespace.trimWhitespace(input)).matches()) {
                return null;
            }
            return new ValidationFailure("The value '" + input + "' is not a valid xs:language");
        }
    }

    public static class StringToToken
    extends StringConverter {
        public static final StringToToken INSTANCE = new StringToToken();

        @Override
        public ConversionResult convertString(CharSequence input) {
            return new StringValue(Whitespace.collapseWhitespace(input), BuiltInAtomicType.TOKEN);
        }

        @Override
        public ValidationFailure validate(CharSequence input) {
            return null;
        }

        @Override
        public boolean isAlwaysSuccessful() {
            return true;
        }
    }

    public static class StringToNormalizedString
    extends StringConverter {
        public static final StringToNormalizedString INSTANCE = new StringToNormalizedString();

        @Override
        public ConversionResult convertString(CharSequence input) {
            return new StringValue(Whitespace.normalizeWhitespace(input), BuiltInAtomicType.NORMALIZED_STRING);
        }

        @Override
        public ValidationFailure validate(CharSequence input) {
            return null;
        }

        @Override
        public boolean isAlwaysSuccessful() {
            return true;
        }
    }

    public static class StringToUntypedAtomic
    extends StringConverter {
        public static final StringToUntypedAtomic INSTANCE = new StringToUntypedAtomic();

        @Override
        public UntypedAtomicValue convert(AtomicValue input) {
            return new UntypedAtomicValue(input.getStringValueCS());
        }

        @Override
        public ConversionResult convertString(CharSequence input) {
            return new UntypedAtomicValue(input);
        }

        @Override
        public ValidationFailure validate(CharSequence input) {
            return null;
        }

        @Override
        public boolean isAlwaysSuccessful() {
            return true;
        }
    }

    public static class StringToString
    extends StringConverter {
        public static final StringToString INSTANCE = new StringToString();

        @Override
        public ConversionResult convert(AtomicValue input) {
            return new StringValue(input.getStringValueCS());
        }

        @Override
        public ConversionResult convertString(CharSequence input) {
            return new StringValue(input);
        }

        @Override
        public ValidationFailure validate(CharSequence input) {
            return null;
        }

        @Override
        public boolean isAlwaysSuccessful() {
            return true;
        }
    }

    public static class StringToNonStringDerivedType
    extends StringConverter {
        private StringConverter phaseOne;
        private Converter.DownCastingConverter phaseTwo;

        public StringToNonStringDerivedType(StringConverter phaseOne, Converter.DownCastingConverter phaseTwo) {
            this.phaseOne = phaseOne;
            this.phaseTwo = phaseTwo;
        }

        @Override
        public StringToNonStringDerivedType setNamespaceResolver(NamespaceResolver resolver) {
            return new StringToNonStringDerivedType((StringConverter)this.phaseOne.setNamespaceResolver(resolver), (Converter.DownCastingConverter)this.phaseTwo.setNamespaceResolver(resolver));
        }

        public ConversionResult convert(StringValue input) {
            CharSequence in = input.getStringValueCS();
            try {
                in = this.phaseTwo.getTargetType().preprocess(in);
            } catch (ValidationException err) {
                return err.getValidationFailure();
            }
            ConversionResult temp = this.phaseOne.convertString(in);
            if (temp instanceof ValidationFailure) {
                return temp;
            }
            return this.phaseTwo.convert((AtomicValue)temp, in);
        }

        @Override
        public ConversionResult convertString(CharSequence input) {
            try {
                input = this.phaseTwo.getTargetType().preprocess(input);
            } catch (ValidationException err) {
                return err.getValidationFailure();
            }
            ConversionResult temp = this.phaseOne.convertString(input);
            if (temp instanceof ValidationFailure) {
                return temp;
            }
            return this.phaseTwo.convert((AtomicValue)temp, input);
        }

        @Override
        public ValidationFailure validate(CharSequence input) {
            try {
                input = this.phaseTwo.getTargetType().preprocess(input);
            } catch (ValidationException err) {
                return err.getValidationFailure();
            }
            ConversionResult temp = this.phaseOne.convertString(input);
            if (temp instanceof ValidationFailure) {
                return (ValidationFailure)temp;
            }
            return this.phaseTwo.validate((AtomicValue)temp, input);
        }
    }
}


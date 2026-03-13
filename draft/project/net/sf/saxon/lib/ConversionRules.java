/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import net.sf.saxon.expr.sort.LRUCache;
import net.sf.saxon.lib.URIChecker;
import net.sf.saxon.om.NotationSet;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Converter;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.type.StringToDouble;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.StringToDouble11;

public class ConversionRules {
    private StringToDouble stringToDouble = StringToDouble11.getInstance();
    private NotationSet notationSet;
    private URIChecker uriChecker;
    private boolean allowYearZero = true;
    private TypeHierarchy typeHierarchy;
    private LRUCache<Integer, Converter> converterCache = new LRUCache(100, true);
    public static final ConversionRules DEFAULT = new ConversionRules();

    public ConversionRules copy() {
        ConversionRules cr = new ConversionRules();
        this.copyTo(cr);
        return cr;
    }

    public void copyTo(ConversionRules cr) {
        cr.stringToDouble = this.stringToDouble;
        cr.notationSet = this.notationSet;
        cr.uriChecker = this.uriChecker;
        cr.allowYearZero = this.allowYearZero;
        cr.typeHierarchy = this.typeHierarchy;
        cr.converterCache.clear();
    }

    public void setTypeHierarchy(TypeHierarchy typeHierarchy) {
        this.typeHierarchy = typeHierarchy;
    }

    public void setStringToDoubleConverter(StringToDouble converter) {
        this.stringToDouble = converter;
    }

    public StringToDouble getStringToDoubleConverter() {
        return this.stringToDouble;
    }

    public void setNotationSet(NotationSet notations) {
        this.notationSet = notations;
    }

    public boolean isDeclaredNotation(String uri, String local) {
        if (this.notationSet == null) {
            return true;
        }
        return this.notationSet.isDeclaredNotation(uri, local);
    }

    public void setURIChecker(URIChecker checker) {
        this.uriChecker = checker;
    }

    public boolean isValidURI(CharSequence string) {
        return this.uriChecker == null || this.uriChecker.isValidURI(string);
    }

    public void setAllowYearZero(boolean allowed) {
        this.allowYearZero = allowed;
    }

    public boolean isAllowYearZero() {
        return this.allowYearZero;
    }

    public Converter getConverter(AtomicType source, AtomicType target) {
        if (source == target) {
            return Converter.IdentityConverter.INSTANCE;
        }
        if (source == BuiltInAtomicType.STRING || source == BuiltInAtomicType.UNTYPED_ATOMIC) {
            return target.getStringConverter(this);
        }
        if (target == BuiltInAtomicType.STRING) {
            return Converter.ToStringConverter.INSTANCE;
        }
        if (target == BuiltInAtomicType.UNTYPED_ATOMIC) {
            return Converter.ToUntypedAtomicConverter.INSTANCE;
        }
        int key = source.getPrimitiveType() << 20 | target.getFingerprint();
        Converter converter = this.converterCache.get(key);
        if (converter == null) {
            converter = this.makeConverter(source, target);
            if (converter != null) {
                this.converterCache.put(key, converter);
            } else {
                return null;
            }
        }
        return converter;
    }

    private Converter makeConverter(AtomicType sourceType, AtomicType targetType) {
        if (sourceType == targetType) {
            return Converter.IdentityConverter.INSTANCE;
        }
        int tt = targetType.getFingerprint();
        int tp = targetType.getPrimitiveType();
        int st = sourceType.getPrimitiveType();
        if (!(st != 513 && st != 631 || tp != 513 && tp != 631)) {
            return this.makeStringConverter(targetType);
        }
        if (!targetType.isPrimitiveType()) {
            AtomicType primTarget = targetType.getPrimitiveItemType();
            if (sourceType == primTarget) {
                return new Converter.DownCastingConverter(targetType, this);
            }
            if (st == 513 || st == 631) {
                return this.makeStringConverter(targetType);
            }
            Converter stageOne = this.makeConverter(sourceType, primTarget);
            if (stageOne == null) {
                return null;
            }
            Converter.DownCastingConverter stageTwo = new Converter.DownCastingConverter(targetType, this);
            return new Converter.TwoPhaseConverter(stageOne, stageTwo);
        }
        if (st == tt) {
            if (this.typeHierarchy != null && this.typeHierarchy.isSubType(sourceType, targetType)) {
                return new Converter.UpCastingConverter(targetType);
            }
            Converter.UpCastingConverter upcast = new Converter.UpCastingConverter(sourceType.getPrimitiveItemType());
            Converter.DownCastingConverter downcast = new Converter.DownCastingConverter(targetType, this);
            return new Converter.TwoPhaseConverter(upcast, downcast);
        }
        switch (tt) {
            case 631: {
                return Converter.ToUntypedAtomicConverter.INSTANCE;
            }
            case 513: {
                return Converter.ToStringConverter.INSTANCE;
            }
            case 516: {
                switch (st) {
                    case 513: 
                    case 631: {
                        return new StringConverter.StringToFloat(this);
                    }
                    case 515: 
                    case 517: 
                    case 533: 
                    case 635: {
                        return Converter.NumericToFloat.INSTANCE;
                    }
                    case 514: {
                        return Converter.BooleanToFloat.INSTANCE;
                    }
                }
                return null;
            }
            case 517: 
            case 635: {
                switch (st) {
                    case 513: 
                    case 631: {
                        return this.stringToDouble;
                    }
                    case 515: 
                    case 516: 
                    case 533: 
                    case 635: {
                        return Converter.NumericToDouble.INSTANCE;
                    }
                    case 514: {
                        return Converter.BooleanToDouble.INSTANCE;
                    }
                }
                return null;
            }
            case 515: {
                switch (st) {
                    case 513: 
                    case 631: {
                        return StringConverter.StringToDecimal.INSTANCE;
                    }
                    case 516: {
                        return Converter.FloatToDecimal.INSTANCE;
                    }
                    case 517: {
                        return Converter.DoubleToDecimal.INSTANCE;
                    }
                    case 533: {
                        return Converter.IntegerToDecimal.INSTANCE;
                    }
                    case 635: {
                        return Converter.NumericToDecimal.INSTANCE;
                    }
                    case 514: {
                        return Converter.BooleanToDecimal.INSTANCE;
                    }
                }
                return null;
            }
            case 533: {
                switch (st) {
                    case 513: 
                    case 631: {
                        return StringConverter.StringToInteger.INSTANCE;
                    }
                    case 516: {
                        return Converter.FloatToInteger.INSTANCE;
                    }
                    case 517: {
                        return Converter.DoubleToInteger.INSTANCE;
                    }
                    case 515: {
                        return Converter.DecimalToInteger.INSTANCE;
                    }
                    case 635: {
                        return Converter.NumericToInteger.INSTANCE;
                    }
                    case 514: {
                        return Converter.BooleanToInteger.INSTANCE;
                    }
                }
                return null;
            }
            case 518: {
                switch (st) {
                    case 513: 
                    case 631: {
                        return StringConverter.StringToDuration.INSTANCE;
                    }
                    case 633: 
                    case 634: {
                        return new Converter.UpCastingConverter(BuiltInAtomicType.DURATION);
                    }
                }
                return null;
            }
            case 633: {
                switch (st) {
                    case 513: 
                    case 631: {
                        return StringConverter.StringToYearMonthDuration.INSTANCE;
                    }
                    case 518: 
                    case 634: {
                        return Converter.DurationToYearMonthDuration.INSTANCE;
                    }
                }
                return null;
            }
            case 634: {
                switch (st) {
                    case 513: 
                    case 631: {
                        return StringConverter.StringToDayTimeDuration.INSTANCE;
                    }
                    case 518: 
                    case 633: {
                        return Converter.DurationToDayTimeDuration.INSTANCE;
                    }
                }
                return null;
            }
            case 519: {
                switch (st) {
                    case 513: 
                    case 631: {
                        return new StringConverter.StringToDateTime(this);
                    }
                    case 521: {
                        return Converter.DateToDateTime.INSTANCE;
                    }
                }
                return null;
            }
            case 520: {
                switch (st) {
                    case 513: 
                    case 631: {
                        return StringConverter.StringToTime.INSTANCE;
                    }
                    case 519: {
                        return Converter.DateTimeToTime.INSTANCE;
                    }
                }
                return null;
            }
            case 521: {
                switch (st) {
                    case 513: 
                    case 631: {
                        return new StringConverter.StringToDate(this);
                    }
                    case 519: {
                        return Converter.DateTimeToDate.INSTANCE;
                    }
                }
                return null;
            }
            case 522: {
                switch (st) {
                    case 513: 
                    case 631: {
                        return new StringConverter.StringToGYearMonth(this);
                    }
                    case 521: {
                        return Converter.TwoPhaseConverter.makeTwoPhaseConverter(BuiltInAtomicType.DATE, BuiltInAtomicType.DATE_TIME, BuiltInAtomicType.G_YEAR_MONTH, this);
                    }
                    case 519: {
                        return Converter.DateTimeToGYearMonth.INSTANCE;
                    }
                }
                return null;
            }
            case 523: {
                switch (st) {
                    case 513: 
                    case 631: {
                        return new StringConverter.StringToGYear(this);
                    }
                    case 521: {
                        return Converter.TwoPhaseConverter.makeTwoPhaseConverter(BuiltInAtomicType.DATE, BuiltInAtomicType.DATE_TIME, BuiltInAtomicType.G_YEAR, this);
                    }
                    case 519: {
                        return Converter.DateTimeToGYear.INSTANCE;
                    }
                }
                return null;
            }
            case 524: {
                switch (st) {
                    case 513: 
                    case 631: {
                        return StringConverter.StringToGMonthDay.INSTANCE;
                    }
                    case 521: {
                        return Converter.TwoPhaseConverter.makeTwoPhaseConverter(BuiltInAtomicType.DATE, BuiltInAtomicType.DATE_TIME, BuiltInAtomicType.G_MONTH_DAY, this);
                    }
                    case 519: {
                        return Converter.DateTimeToGMonthDay.INSTANCE;
                    }
                }
                return null;
            }
            case 525: {
                switch (st) {
                    case 513: 
                    case 631: {
                        return StringConverter.StringToGDay.INSTANCE;
                    }
                    case 521: {
                        return Converter.TwoPhaseConverter.makeTwoPhaseConverter(BuiltInAtomicType.DATE, BuiltInAtomicType.DATE_TIME, BuiltInAtomicType.G_DAY, this);
                    }
                    case 519: {
                        return Converter.DateTimeToGDay.INSTANCE;
                    }
                }
                return null;
            }
            case 526: {
                switch (st) {
                    case 513: 
                    case 631: {
                        return StringConverter.StringToGMonth.INSTANCE;
                    }
                    case 521: {
                        return Converter.TwoPhaseConverter.makeTwoPhaseConverter(BuiltInAtomicType.DATE, BuiltInAtomicType.DATE_TIME, BuiltInAtomicType.G_MONTH, this);
                    }
                    case 519: {
                        return Converter.DateTimeToGMonth.INSTANCE;
                    }
                }
                return null;
            }
            case 514: {
                switch (st) {
                    case 513: 
                    case 631: {
                        return StringConverter.StringToBoolean.INSTANCE;
                    }
                    case 515: 
                    case 516: 
                    case 517: 
                    case 533: 
                    case 635: {
                        return Converter.NumericToBoolean.INSTANCE;
                    }
                }
                return null;
            }
            case 528: {
                switch (st) {
                    case 513: 
                    case 631: {
                        return StringConverter.StringToBase64Binary.INSTANCE;
                    }
                    case 527: {
                        return Converter.HexBinaryToBase64Binary.INSTANCE;
                    }
                }
                return null;
            }
            case 527: {
                switch (st) {
                    case 513: 
                    case 631: {
                        return StringConverter.StringToHexBinary.INSTANCE;
                    }
                    case 528: {
                        return Converter.Base64BinaryToHexBinary.INSTANCE;
                    }
                }
                return null;
            }
            case 529: {
                switch (st) {
                    case 513: 
                    case 631: {
                        return new StringConverter.StringToAnyURI(this);
                    }
                }
                return null;
            }
            case 530: {
                switch (st) {
                    case 513: 
                    case 631: {
                        return new StringConverter.StringToQName(this);
                    }
                    case 531: {
                        return Converter.NotationToQName.INSTANCE;
                    }
                }
                return null;
            }
            case 531: {
                switch (st) {
                    case 513: 
                    case 631: {
                        return new StringConverter.StringToNotation(this);
                    }
                    case 530: {
                        return Converter.QNameToNotation.INSTANCE;
                    }
                }
                return null;
            }
            case 632: {
                return Converter.IdentityConverter.INSTANCE;
            }
        }
        throw new IllegalArgumentException("Unknown primitive type " + tt);
    }

    public StringConverter makeStringConverter(AtomicType targetType) {
        int tt = targetType.getPrimitiveType();
        if (targetType.isBuiltInType()) {
            if (tt == 513) {
                switch (targetType.getFingerprint()) {
                    case 513: {
                        return StringConverter.StringToString.INSTANCE;
                    }
                    case 553: {
                        return StringConverter.StringToNormalizedString.INSTANCE;
                    }
                    case 554: {
                        return StringConverter.StringToToken.INSTANCE;
                    }
                    case 555: {
                        return StringConverter.StringToLanguage.INSTANCE;
                    }
                    case 558: {
                        return StringConverter.StringToName.INSTANCE;
                    }
                    case 559: {
                        return StringConverter.StringToNCName.TO_NCNAME;
                    }
                    case 560: {
                        return StringConverter.StringToNCName.TO_ID;
                    }
                    case 561: {
                        return StringConverter.StringToNCName.TO_IDREF;
                    }
                    case 563: {
                        return StringConverter.StringToNCName.TO_ENTITY;
                    }
                    case 556: {
                        return StringConverter.StringToNMTOKEN.INSTANCE;
                    }
                }
                throw new AssertionError((Object)"Unknown built-in subtype of xs:string");
            }
            if (tt == 631) {
                return StringConverter.StringToUntypedAtomic.INSTANCE;
            }
            if (targetType.isPrimitiveType()) {
                Converter converter = this.getConverter(BuiltInAtomicType.STRING, targetType);
                assert (converter != null);
                return (StringConverter)converter;
            }
            if (tt == 533) {
                return new StringConverter.StringToIntegerSubtype((BuiltInAtomicType)targetType);
            }
            switch (targetType.getFingerprint()) {
                case 634: {
                    return StringConverter.StringToDayTimeDuration.INSTANCE;
                }
                case 633: {
                    return StringConverter.StringToYearMonthDuration.INSTANCE;
                }
                case 565: {
                    StringConverter.StringToDateTime first = new StringConverter.StringToDateTime(this);
                    Converter.DownCastingConverter second = new Converter.DownCastingConverter(targetType, this);
                    return new StringConverter.StringToNonStringDerivedType(first, second);
                }
            }
            throw new AssertionError((Object)("Unknown built in type " + targetType));
        }
        if (tt == 513) {
            if (targetType.getBuiltInBaseType() == BuiltInAtomicType.STRING) {
                return new StringConverter.StringToStringSubtype(this, targetType);
            }
            return new StringConverter.StringToDerivedStringSubtype(this, targetType);
        }
        StringConverter first = targetType.getPrimitiveItemType().getStringConverter(this);
        Converter.DownCastingConverter second = new Converter.DownCastingConverter(targetType, this);
        return new StringConverter.StringToNonStringDerivedType(first, second);
    }
}


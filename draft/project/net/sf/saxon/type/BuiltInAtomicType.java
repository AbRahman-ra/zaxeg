/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.instruct.ValueOf;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.om.AtomicArray;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnySimpleType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.NumericType;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.SchemaComponent;
import net.sf.saxon.type.SchemaException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.CalendarValue;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.NotationValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.UntypedAtomicValue;

public class BuiltInAtomicType
implements AtomicType,
ItemType.WithSequenceTypeCache {
    private int fingerprint;
    private int baseFingerprint;
    private int primitiveFingerprint;
    private UType uType;
    private String alphaCode;
    private boolean ordered = false;
    public StringConverter stringConverter;
    private SequenceType _one;
    private SequenceType _oneOrMore;
    private SequenceType _zeroOrOne;
    private SequenceType _zeroOrMore;
    private static Map<String, BuiltInAtomicType> byAlphaCode = new HashMap<String, BuiltInAtomicType>(60);
    public static final BuiltInAtomicType ANY_ATOMIC = BuiltInAtomicType.makeAtomicType(632, AnySimpleType.getInstance(), "A", true);
    public static final BuiltInAtomicType STRING = BuiltInAtomicType.makeAtomicType(513, ANY_ATOMIC, "AS", true);
    public static final BuiltInAtomicType BOOLEAN = BuiltInAtomicType.makeAtomicType(514, ANY_ATOMIC, "AB", true);
    public static final BuiltInAtomicType DURATION = BuiltInAtomicType.makeAtomicType(518, ANY_ATOMIC, "AR", false);
    public static final BuiltInAtomicType DATE_TIME = BuiltInAtomicType.makeAtomicType(519, ANY_ATOMIC, "AM", true);
    public static final BuiltInAtomicType DATE = BuiltInAtomicType.makeAtomicType(521, ANY_ATOMIC, "AA", true);
    public static final BuiltInAtomicType TIME = BuiltInAtomicType.makeAtomicType(520, ANY_ATOMIC, "AT", true);
    public static final BuiltInAtomicType G_YEAR_MONTH = BuiltInAtomicType.makeAtomicType(522, ANY_ATOMIC, "AH", false);
    public static final BuiltInAtomicType G_MONTH = BuiltInAtomicType.makeAtomicType(526, ANY_ATOMIC, "AI", false);
    public static final BuiltInAtomicType G_MONTH_DAY = BuiltInAtomicType.makeAtomicType(524, ANY_ATOMIC, "AJ", false);
    public static final BuiltInAtomicType G_YEAR = BuiltInAtomicType.makeAtomicType(523, ANY_ATOMIC, "AG", false);
    public static final BuiltInAtomicType G_DAY = BuiltInAtomicType.makeAtomicType(525, ANY_ATOMIC, "AK", false);
    public static final BuiltInAtomicType HEX_BINARY = BuiltInAtomicType.makeAtomicType(527, ANY_ATOMIC, "AX", true);
    public static final BuiltInAtomicType BASE64_BINARY = BuiltInAtomicType.makeAtomicType(528, ANY_ATOMIC, "A2", true);
    public static final BuiltInAtomicType ANY_URI = BuiltInAtomicType.makeAtomicType(529, ANY_ATOMIC, "AU", true);
    public static final BuiltInAtomicType QNAME = BuiltInAtomicType.makeAtomicType(530, ANY_ATOMIC, "AQ", false);
    public static final BuiltInAtomicType NOTATION = BuiltInAtomicType.makeAtomicType(531, ANY_ATOMIC, "AN", false);
    public static final BuiltInAtomicType UNTYPED_ATOMIC = BuiltInAtomicType.makeAtomicType(631, ANY_ATOMIC, "AZ", true);
    public static final BuiltInAtomicType DECIMAL = BuiltInAtomicType.makeAtomicType(515, ANY_ATOMIC, "AD", true);
    public static final BuiltInAtomicType FLOAT = BuiltInAtomicType.makeAtomicType(516, ANY_ATOMIC, "AF", true);
    public static final BuiltInAtomicType DOUBLE = BuiltInAtomicType.makeAtomicType(517, ANY_ATOMIC, "AO", true);
    public static final BuiltInAtomicType INTEGER = BuiltInAtomicType.makeAtomicType(533, DECIMAL, "ADI", true);
    public static final BuiltInAtomicType NON_POSITIVE_INTEGER = BuiltInAtomicType.makeAtomicType(534, INTEGER, "ADIN", true);
    public static final BuiltInAtomicType NEGATIVE_INTEGER = BuiltInAtomicType.makeAtomicType(535, NON_POSITIVE_INTEGER, "ADINN", true);
    public static final BuiltInAtomicType LONG = BuiltInAtomicType.makeAtomicType(536, INTEGER, "ADIL", true);
    public static final BuiltInAtomicType INT = BuiltInAtomicType.makeAtomicType(537, LONG, "ADILI", true);
    public static final BuiltInAtomicType SHORT = BuiltInAtomicType.makeAtomicType(538, INT, "ADILIS", true);
    public static final BuiltInAtomicType BYTE = BuiltInAtomicType.makeAtomicType(539, SHORT, "ADILISB", true);
    public static final BuiltInAtomicType NON_NEGATIVE_INTEGER = BuiltInAtomicType.makeAtomicType(540, INTEGER, "ADIP", true);
    public static final BuiltInAtomicType POSITIVE_INTEGER = BuiltInAtomicType.makeAtomicType(541, NON_NEGATIVE_INTEGER, "ADIPP", true);
    public static final BuiltInAtomicType UNSIGNED_LONG = BuiltInAtomicType.makeAtomicType(542, NON_NEGATIVE_INTEGER, "ADIPL", true);
    public static final BuiltInAtomicType UNSIGNED_INT = BuiltInAtomicType.makeAtomicType(543, UNSIGNED_LONG, "ADIPLI", true);
    public static final BuiltInAtomicType UNSIGNED_SHORT = BuiltInAtomicType.makeAtomicType(544, UNSIGNED_INT, "ADIPLIS", true);
    public static final BuiltInAtomicType UNSIGNED_BYTE = BuiltInAtomicType.makeAtomicType(545, UNSIGNED_SHORT, "ADIPLISB", true);
    public static final BuiltInAtomicType YEAR_MONTH_DURATION = BuiltInAtomicType.makeAtomicType(633, DURATION, "ARY", true);
    public static final BuiltInAtomicType DAY_TIME_DURATION = BuiltInAtomicType.makeAtomicType(634, DURATION, "ARD", true);
    public static final BuiltInAtomicType NORMALIZED_STRING = BuiltInAtomicType.makeAtomicType(553, STRING, "ASN", true);
    public static final BuiltInAtomicType TOKEN = BuiltInAtomicType.makeAtomicType(554, NORMALIZED_STRING, "ASNT", true);
    public static final BuiltInAtomicType LANGUAGE = BuiltInAtomicType.makeAtomicType(555, TOKEN, "ASNTL", true);
    public static final BuiltInAtomicType NAME = BuiltInAtomicType.makeAtomicType(558, TOKEN, "ASNTN", true);
    public static final BuiltInAtomicType NMTOKEN = BuiltInAtomicType.makeAtomicType(556, TOKEN, "ASNTK", true);
    public static final BuiltInAtomicType NCNAME = BuiltInAtomicType.makeAtomicType(559, NAME, "ASNTNC", true);
    public static final BuiltInAtomicType ID = BuiltInAtomicType.makeAtomicType(560, NCNAME, "ASNTNCI", true);
    public static final BuiltInAtomicType IDREF = BuiltInAtomicType.makeAtomicType(561, NCNAME, "ASNTNCR", true);
    public static final BuiltInAtomicType ENTITY = BuiltInAtomicType.makeAtomicType(563, NCNAME, "ASNTNCE", true);
    public static final BuiltInAtomicType DATE_TIME_STAMP = BuiltInAtomicType.makeAtomicType(565, DATE_TIME, "AMP", true);

    public static BuiltInAtomicType fromAlphaCode(String code) {
        return byAlphaCode.get(code);
    }

    private BuiltInAtomicType(int fingerprint) {
        this.fingerprint = fingerprint;
    }

    @Override
    public String getName() {
        return StandardNames.getLocalName(this.fingerprint);
    }

    @Override
    public UType getUType() {
        return this.uType;
    }

    @Override
    public String getTargetNamespace() {
        return "http://www.w3.org/2001/XMLSchema";
    }

    @Override
    public String getEQName() {
        return "Q{http://www.w3.org/2001/XMLSchema}" + this.getName();
    }

    @Override
    public boolean isAbstract() {
        switch (this.fingerprint) {
            case 531: 
            case 573: 
            case 632: 
            case 635: {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isBuiltInType() {
        return true;
    }

    @Override
    public StructuredQName getTypeName() {
        return new StructuredQName(StandardNames.getPrefix(this.fingerprint), StandardNames.getURI(this.fingerprint), StandardNames.getLocalName(this.fingerprint));
    }

    @Override
    public String getBasicAlphaCode() {
        return this.alphaCode;
    }

    @Override
    public SequenceType one() {
        if (this._one == null) {
            this._one = new SequenceType(this, 16384);
        }
        return this._one;
    }

    @Override
    public SequenceType zeroOrOne() {
        if (this._zeroOrOne == null) {
            this._zeroOrOne = new SequenceType(this, 24576);
        }
        return this._zeroOrOne;
    }

    @Override
    public SequenceType oneOrMore() {
        if (this._oneOrMore == null) {
            this._oneOrMore = new SequenceType(this, 49152);
        }
        return this._oneOrMore;
    }

    @Override
    public SequenceType zeroOrMore() {
        if (this._zeroOrMore == null) {
            this._zeroOrMore = new SequenceType(this, 57344);
        }
        return this._zeroOrMore;
    }

    @Override
    public int getRedefinitionLevel() {
        return 0;
    }

    @Override
    public boolean isOrdered(boolean optimistic) {
        return this.ordered || optimistic && (this == DURATION || this == ANY_ATOMIC);
    }

    @Override
    public String getSystemId() {
        return null;
    }

    public boolean isPrimitiveNumeric() {
        switch (this.getFingerprint()) {
            case 515: 
            case 516: 
            case 517: 
            case 533: {
                return true;
            }
        }
        return false;
    }

    @Override
    public final SchemaComponent.ValidationStatus getValidationStatus() {
        return SchemaComponent.ValidationStatus.VALIDATED;
    }

    @Override
    public final int getBlock() {
        return 0;
    }

    @Override
    public final int getDerivationMethod() {
        return 1;
    }

    @Override
    public final boolean allowsDerivation(int derivation) {
        return true;
    }

    @Override
    public int getFinalProhibitions() {
        return 0;
    }

    public final void setBaseTypeFingerprint(int baseFingerprint) {
        this.baseFingerprint = baseFingerprint;
    }

    @Override
    public final int getFingerprint() {
        return this.fingerprint;
    }

    @Override
    public final StructuredQName getStructuredQName() {
        return new StructuredQName("xs", "http://www.w3.org/2001/XMLSchema", StandardNames.getLocalName(this.fingerprint));
    }

    @Override
    public String getDisplayName() {
        return StandardNames.getDisplayName(this.fingerprint);
    }

    @Override
    public final boolean isPrimitiveType() {
        return Type.isPrimitiveAtomicType(this.fingerprint);
    }

    @Override
    public final boolean isComplexType() {
        return false;
    }

    @Override
    public final boolean isAnonymousType() {
        return false;
    }

    @Override
    public boolean isPlainType() {
        return true;
    }

    @Override
    public final SchemaType getBaseType() {
        if (this.baseFingerprint == -1) {
            return null;
        }
        return BuiltInType.getSchemaType(this.baseFingerprint);
    }

    @Override
    public boolean matches(Item item, TypeHierarchy th) {
        return item instanceof AtomicValue && Type.isSubType(((AtomicValue)item).getItemType(), this);
    }

    @Override
    public BuiltInAtomicType getPrimitiveItemType() {
        if (this.isPrimitiveType()) {
            return this;
        }
        ItemType s = (ItemType)((Object)this.getBaseType());
        assert (s != null);
        if (s.isPlainType()) {
            return (BuiltInAtomicType)s.getPrimitiveItemType();
        }
        return this;
    }

    @Override
    public int getPrimitiveType() {
        return this.primitiveFingerprint;
    }

    public boolean isAllowedInXSD10() {
        return this.getFingerprint() != 565;
    }

    @Override
    public String toString() {
        return this.getDisplayName();
    }

    @Override
    public AtomicType getAtomizedItemType() {
        return this;
    }

    @Override
    public boolean isAtomizable(TypeHierarchy th) {
        return true;
    }

    public SchemaType getKnownBaseType() {
        return this.getBaseType();
    }

    @Override
    public boolean isSameType(SchemaType other) {
        return other.getFingerprint() == this.getFingerprint();
    }

    @Override
    public String getDescription() {
        return this.getDisplayName();
    }

    @Override
    public void checkTypeDerivationIsOK(SchemaType type, int block) throws SchemaException {
        if (type != AnySimpleType.getInstance() && !this.isSameType(type)) {
            SchemaType base = this.getBaseType();
            if (base == null) {
                throw new SchemaException("The type " + this.getDescription() + " is not validly derived from the type " + type.getDescription());
            }
            try {
                base.checkTypeDerivationIsOK(type, block);
            } catch (SchemaException se) {
                throw new SchemaException("The type " + this.getDescription() + " is not validly derived from the type " + type.getDescription());
            }
        }
    }

    @Override
    public final boolean isSimpleType() {
        return true;
    }

    @Override
    public boolean isAtomicType() {
        return true;
    }

    @Override
    public boolean isIdType() {
        return this.fingerprint == 560;
    }

    @Override
    public boolean isIdRefType() {
        return this.fingerprint == 561;
    }

    @Override
    public boolean isListType() {
        return false;
    }

    @Override
    public boolean isUnionType() {
        return false;
    }

    @Override
    public int getWhitespaceAction() {
        switch (this.getFingerprint()) {
            case 513: {
                return 0;
            }
            case 553: {
                return 1;
            }
        }
        return 2;
    }

    @Override
    public SchemaType getBuiltInBaseType() {
        BuiltInAtomicType base;
        for (base = this; base != null && base.getFingerprint() > 1023; base = (BuiltInAtomicType)base.getBaseType()) {
        }
        return base;
    }

    @Override
    public boolean isNamespaceSensitive() {
        BuiltInAtomicType base = this;
        int fp = base.getFingerprint();
        while (fp > 1023) {
            base = (BuiltInAtomicType)base.getBaseType();
            assert (base != null);
            fp = base.getFingerprint();
        }
        return fp == 530 || fp == 531;
    }

    @Override
    public ValidationFailure validateContent(CharSequence value, NamespaceResolver nsResolver, ConversionRules rules) {
        int f = this.getFingerprint();
        if (f == 513 || f == 573 || f == 631 || f == 632) {
            return null;
        }
        StringConverter converter = this.stringConverter;
        if (converter == null) {
            converter = this.getStringConverter(rules);
            if (this.isNamespaceSensitive()) {
                NotationValue nv;
                if (nsResolver == null) {
                    throw new UnsupportedOperationException("Cannot validate a QName without a namespace resolver");
                }
                ConversionResult result = (converter = (StringConverter)converter.setNamespaceResolver(nsResolver)).convertString(value);
                if (result instanceof ValidationFailure) {
                    return (ValidationFailure)result;
                }
                if (this.fingerprint == 531 && !rules.isDeclaredNotation((nv = (NotationValue)result).getNamespaceURI(), nv.getLocalName())) {
                    return new ValidationFailure("Notation {" + nv.getNamespaceURI() + "}" + nv.getLocalName() + " is not declared in the schema");
                }
                return null;
            }
        }
        return converter.validate(value);
    }

    @Override
    public StringConverter getStringConverter(ConversionRules rules) {
        if (this.stringConverter != null) {
            return this.stringConverter;
        }
        switch (this.fingerprint) {
            case 517: 
            case 635: {
                return rules.getStringToDoubleConverter();
            }
            case 516: {
                return new StringConverter.StringToFloat(rules);
            }
            case 519: {
                return new StringConverter.StringToDateTime(rules);
            }
            case 565: {
                return new StringConverter.StringToDateTimeStamp(rules);
            }
            case 521: {
                return new StringConverter.StringToDate(rules);
            }
            case 523: {
                return new StringConverter.StringToGYear(rules);
            }
            case 522: {
                return new StringConverter.StringToGYearMonth(rules);
            }
            case 529: {
                return new StringConverter.StringToAnyURI(rules);
            }
            case 530: {
                return new StringConverter.StringToQName(rules);
            }
            case 531: {
                return new StringConverter.StringToNotation(rules);
            }
        }
        throw new AssertionError((Object)("No string converter available for " + this));
    }

    @Override
    public AtomicSequence atomize(NodeInfo node) throws XPathException {
        CharSequence stringValue = node.getStringValueCS();
        if (stringValue.length() == 0 && node.isNilled()) {
            return AtomicArray.EMPTY_ATOMIC_ARRAY;
        }
        if (this.fingerprint == 513) {
            return StringValue.makeStringValue(stringValue);
        }
        if (this.fingerprint == 631) {
            return new UntypedAtomicValue(stringValue);
        }
        StringConverter converter = this.stringConverter;
        if (converter == null) {
            converter = this.getStringConverter(node.getConfiguration().getConversionRules());
            if (this.isNamespaceSensitive()) {
                NodeInfo container = node.getNodeKind() == 1 ? node : node.getParent();
                converter = (StringConverter)converter.setNamespaceResolver(container.getAllNamespaces());
            }
        }
        return converter.convertString(stringValue).asAtomic();
    }

    @Override
    public AtomicSequence getTypedValue(CharSequence value, NamespaceResolver resolver, ConversionRules rules) throws ValidationException {
        if (this.fingerprint == 513) {
            return StringValue.makeStringValue(value);
        }
        if (this.fingerprint == 631) {
            return new UntypedAtomicValue(value);
        }
        StringConverter converter = this.getStringConverter(rules);
        if (this.isNamespaceSensitive()) {
            converter = (StringConverter)converter.setNamespaceResolver(resolver);
        }
        return converter.convertString(value).asAtomic();
    }

    public boolean equals(Object obj) {
        return obj instanceof BuiltInAtomicType && this.getFingerprint() == ((BuiltInAtomicType)obj).getFingerprint();
    }

    public int hashCode() {
        return this.getFingerprint();
    }

    @Override
    public ValidationFailure validate(AtomicValue primValue, CharSequence lexicalValue, ConversionRules rules) {
        switch (this.fingerprint) {
            case 513: 
            case 514: 
            case 515: 
            case 516: 
            case 517: 
            case 518: 
            case 519: 
            case 520: 
            case 521: 
            case 522: 
            case 523: 
            case 524: 
            case 525: 
            case 526: 
            case 527: 
            case 528: 
            case 529: 
            case 530: 
            case 531: 
            case 533: 
            case 631: 
            case 635: {
                return null;
            }
            case 534: 
            case 535: 
            case 536: 
            case 537: 
            case 538: 
            case 539: 
            case 540: 
            case 541: 
            case 542: 
            case 543: 
            case 544: 
            case 545: {
                return ((IntegerValue)primValue).validateAgainstSubType(this);
            }
            case 633: 
            case 634: {
                return null;
            }
            case 565: {
                return ((CalendarValue)primValue).getTimezoneInMinutes() == Integer.MIN_VALUE ? new ValidationFailure("xs:dateTimeStamp value must have a timezone") : null;
            }
            case 553: 
            case 554: 
            case 555: 
            case 556: 
            case 558: 
            case 559: 
            case 560: 
            case 561: 
            case 563: {
                return this.stringConverter.validate(primValue.getStringValueCS());
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void analyzeContentExpression(Expression expression, int kind) throws XPathException {
        BuiltInAtomicType.analyzeContentExpression(this, expression, kind);
    }

    public static void analyzeContentExpression(SimpleType simpleType, Expression expression, int kind) throws XPathException {
        if (kind == 1) {
            expression.checkPermittedContents(simpleType, true);
        } else if (kind == 2 && (expression instanceof ValueOf || expression instanceof Literal)) {
            expression.checkPermittedContents(simpleType, true);
        }
    }

    private static BuiltInAtomicType makeAtomicType(int fingerprint, SimpleType baseType, String code, boolean ordered) {
        BuiltInAtomicType t = new BuiltInAtomicType(fingerprint);
        t.setBaseTypeFingerprint(baseType.getFingerprint());
        t.primitiveFingerprint = t.isPrimitiveType() ? fingerprint : ((AtomicType)baseType).getPrimitiveType();
        t.uType = UType.fromTypeCode(t.primitiveFingerprint);
        t.ordered = ordered;
        t.alphaCode = code;
        BuiltInType.register(fingerprint, t);
        byAlphaCode.put(code, t);
        return t;
    }

    @Override
    public CharSequence preprocess(CharSequence input) {
        return input;
    }

    @Override
    public CharSequence postprocess(CharSequence input) {
        return input;
    }

    public Set<? extends PlainType> getPlainMemberTypes() {
        return Collections.singleton(this);
    }

    public boolean isNumericType() {
        BuiltInAtomicType p = this.getPrimitiveItemType();
        return p == NumericType.getInstance() || p == DECIMAL || p == DOUBLE || p == FLOAT || p == INTEGER;
    }

    static {
        BuiltInAtomicType.ANY_ATOMIC.stringConverter = StringConverter.StringToString.INSTANCE;
        BuiltInAtomicType.STRING.stringConverter = StringConverter.StringToString.INSTANCE;
        BuiltInAtomicType.LANGUAGE.stringConverter = StringConverter.StringToLanguage.INSTANCE;
        BuiltInAtomicType.NORMALIZED_STRING.stringConverter = StringConverter.StringToNormalizedString.INSTANCE;
        BuiltInAtomicType.TOKEN.stringConverter = StringConverter.StringToToken.INSTANCE;
        BuiltInAtomicType.NCNAME.stringConverter = StringConverter.StringToNCName.TO_NCNAME;
        BuiltInAtomicType.NAME.stringConverter = StringConverter.StringToName.INSTANCE;
        BuiltInAtomicType.NMTOKEN.stringConverter = StringConverter.StringToNMTOKEN.INSTANCE;
        BuiltInAtomicType.ID.stringConverter = StringConverter.StringToNCName.TO_ID;
        BuiltInAtomicType.IDREF.stringConverter = StringConverter.StringToNCName.TO_IDREF;
        BuiltInAtomicType.ENTITY.stringConverter = StringConverter.StringToNCName.TO_ENTITY;
        BuiltInAtomicType.DECIMAL.stringConverter = StringConverter.StringToDecimal.INSTANCE;
        BuiltInAtomicType.INTEGER.stringConverter = StringConverter.StringToInteger.INSTANCE;
        BuiltInAtomicType.DURATION.stringConverter = StringConverter.StringToDuration.INSTANCE;
        BuiltInAtomicType.G_MONTH.stringConverter = StringConverter.StringToGMonth.INSTANCE;
        BuiltInAtomicType.G_MONTH_DAY.stringConverter = StringConverter.StringToGMonthDay.INSTANCE;
        BuiltInAtomicType.G_DAY.stringConverter = StringConverter.StringToGDay.INSTANCE;
        BuiltInAtomicType.DAY_TIME_DURATION.stringConverter = StringConverter.StringToDayTimeDuration.INSTANCE;
        BuiltInAtomicType.YEAR_MONTH_DURATION.stringConverter = StringConverter.StringToYearMonthDuration.INSTANCE;
        BuiltInAtomicType.TIME.stringConverter = StringConverter.StringToTime.INSTANCE;
        BuiltInAtomicType.BOOLEAN.stringConverter = StringConverter.StringToBoolean.INSTANCE;
        BuiltInAtomicType.HEX_BINARY.stringConverter = StringConverter.StringToHexBinary.INSTANCE;
        BuiltInAtomicType.BASE64_BINARY.stringConverter = StringConverter.StringToBase64Binary.INSTANCE;
        BuiltInAtomicType.UNTYPED_ATOMIC.stringConverter = StringConverter.StringToUntypedAtomic.INSTANCE;
        BuiltInAtomicType.NON_POSITIVE_INTEGER.stringConverter = new StringConverter.StringToIntegerSubtype(NON_POSITIVE_INTEGER);
        BuiltInAtomicType.NEGATIVE_INTEGER.stringConverter = new StringConverter.StringToIntegerSubtype(NEGATIVE_INTEGER);
        BuiltInAtomicType.LONG.stringConverter = new StringConverter.StringToIntegerSubtype(LONG);
        BuiltInAtomicType.INT.stringConverter = new StringConverter.StringToIntegerSubtype(INT);
        BuiltInAtomicType.SHORT.stringConverter = new StringConverter.StringToIntegerSubtype(SHORT);
        BuiltInAtomicType.BYTE.stringConverter = new StringConverter.StringToIntegerSubtype(BYTE);
        BuiltInAtomicType.NON_NEGATIVE_INTEGER.stringConverter = new StringConverter.StringToIntegerSubtype(NON_NEGATIVE_INTEGER);
        BuiltInAtomicType.POSITIVE_INTEGER.stringConverter = new StringConverter.StringToIntegerSubtype(POSITIVE_INTEGER);
        BuiltInAtomicType.UNSIGNED_LONG.stringConverter = new StringConverter.StringToIntegerSubtype(UNSIGNED_LONG);
        BuiltInAtomicType.UNSIGNED_INT.stringConverter = new StringConverter.StringToIntegerSubtype(UNSIGNED_INT);
        BuiltInAtomicType.UNSIGNED_SHORT.stringConverter = new StringConverter.StringToIntegerSubtype(UNSIGNED_SHORT);
        BuiltInAtomicType.UNSIGNED_BYTE.stringConverter = new StringConverter.StringToIntegerSubtype(UNSIGNED_BYTE);
    }
}


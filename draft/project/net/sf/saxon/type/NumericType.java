/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import java.util.Arrays;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Genre;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnySimpleType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.BuiltInType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.LocalUnionType;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.SchemaComponent;
import net.sf.saxon.type.SchemaException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.StringToDouble;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.SequenceType;

public class NumericType
extends LocalUnionType
implements SimpleType {
    private static NumericType THE_INSTANCE;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static NumericType getInstance() {
        Class<NumericType> clazz = NumericType.class;
        synchronized (NumericType.class) {
            if (THE_INSTANCE == null) {
                THE_INSTANCE = new NumericType();
                BuiltInType.register(635, THE_INSTANCE);
            }
            // ** MonitorExit[var0] (shouldn't be in output)
            return THE_INSTANCE;
        }
    }

    private NumericType() {
        super(Arrays.asList(BuiltInAtomicType.DOUBLE, BuiltInAtomicType.FLOAT, BuiltInAtomicType.DECIMAL));
    }

    @Override
    public StructuredQName getTypeName() {
        return new StructuredQName("xs", "http://www.w3.org/2001/XMLSchema", "numeric");
    }

    @Override
    public Genre getGenre() {
        return Genre.ATOMIC;
    }

    @Override
    public String getBasicAlphaCode() {
        return "A";
    }

    @Override
    public boolean containsListType() {
        return false;
    }

    @Override
    public synchronized Iterable<AtomicType> getPlainMemberTypes() {
        return this.getMemberTypes();
    }

    public static boolean isNumericType(ItemType type) {
        return type.isPlainType() && UType.NUMERIC.subsumes(type.getUType());
    }

    @Override
    public SequenceType getResultTypeOfCast() {
        return SequenceType.ATOMIC_SEQUENCE;
    }

    @Override
    public boolean isPlainType() {
        return true;
    }

    @Override
    public double getDefaultPriority() {
        return 0.125;
    }

    @Override
    public boolean matches(Item item, TypeHierarchy th) {
        return item instanceof NumericValue;
    }

    @Override
    public AtomicType getPrimitiveItemType() {
        return BuiltInAtomicType.ANY_ATOMIC;
    }

    @Override
    public int getPrimitiveType() {
        return BuiltInAtomicType.ANY_ATOMIC.getFingerprint();
    }

    @Override
    public UType getUType() {
        return UType.NUMERIC;
    }

    @Override
    public PlainType getAtomizedItemType() {
        return this;
    }

    @Override
    public boolean isAtomizable(TypeHierarchy th) {
        return true;
    }

    @Override
    public boolean isAtomicType() {
        return false;
    }

    @Override
    public boolean isListType() {
        return false;
    }

    @Override
    public boolean isUnionType() {
        return true;
    }

    @Override
    public boolean isBuiltInType() {
        return true;
    }

    @Override
    public SchemaType getBuiltInBaseType() {
        return AnySimpleType.getInstance();
    }

    @Override
    public DoubleValue getTypedValue(CharSequence value, NamespaceResolver resolver, ConversionRules rules) throws ValidationException {
        try {
            double d = StringToDouble.getInstance().stringToNumber(value);
            return new DoubleValue(d);
        } catch (NumberFormatException e) {
            String message = String.format("Cannot convert string \"%s\" to xs:numeric", value);
            throw new ValidationFailure(message).makeException();
        }
    }

    @Override
    public ValidationFailure validateContent(CharSequence value, NamespaceResolver nsResolver, ConversionRules rules) {
        try {
            StringToDouble.getInstance().stringToNumber(value);
            return null;
        } catch (NumberFormatException e) {
            return new ValidationFailure(e.getMessage());
        }
    }

    @Override
    public ValidationFailure checkAgainstFacets(AtomicValue value, ConversionRules rules) {
        return null;
    }

    @Override
    public boolean isNamespaceSensitive() {
        return false;
    }

    @Override
    public int getWhitespaceAction() {
        return 2;
    }

    @Override
    public CharSequence preprocess(CharSequence input) {
        return input;
    }

    @Override
    public CharSequence postprocess(CharSequence input) {
        return input;
    }

    @Override
    public String getName() {
        return "numeric";
    }

    @Override
    public String getTargetNamespace() {
        return "http://www.w3.org/2001/XMLSchema";
    }

    @Override
    public int getFingerprint() {
        return 635;
    }

    @Override
    public String getDisplayName() {
        return "xs:numeric";
    }

    @Override
    public String getEQName() {
        return "Q(http://www.w3.org/2001/XMLSchema}numeric";
    }

    @Override
    public StructuredQName getStructuredQName() {
        return new StructuredQName("xs", "http://www.w3.org/2001/XMLSchema", "numeric");
    }

    @Override
    public boolean isComplexType() {
        return false;
    }

    @Override
    public boolean isSimpleType() {
        return true;
    }

    @Override
    public boolean isAnonymousType() {
        return false;
    }

    @Override
    public int getBlock() {
        return 0;
    }

    @Override
    public SchemaType getBaseType() {
        return AnySimpleType.getInstance();
    }

    @Override
    public int getDerivationMethod() {
        return 1;
    }

    @Override
    public int getFinalProhibitions() {
        return 0;
    }

    @Override
    public boolean allowsDerivation(int derivation) {
        return true;
    }

    @Override
    public void analyzeContentExpression(Expression expression, int kind) throws XPathException {
        BuiltInAtomicType.analyzeContentExpression(this, expression, kind);
    }

    @Override
    public AtomicSequence atomize(NodeInfo node) throws XPathException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSameType(SchemaType other) {
        return other instanceof NumericType;
    }

    @Override
    public String getDescription() {
        return "xs:numeric";
    }

    @Override
    public String getSystemId() {
        return null;
    }

    @Override
    public boolean isIdType() {
        return false;
    }

    @Override
    public boolean isIdRefType() {
        return false;
    }

    @Override
    public SchemaComponent.ValidationStatus getValidationStatus() {
        return SchemaComponent.ValidationStatus.VALIDATED;
    }

    @Override
    public int getRedefinitionLevel() {
        return 0;
    }

    @Override
    public String toExportString() {
        return this.toString();
    }

    @Override
    public String toString() {
        return "xs:numeric";
    }

    @Override
    public void checkTypeDerivationIsOK(SchemaType base, int block) throws SchemaException {
    }
}


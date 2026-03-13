/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.type.AnyType;
import net.sf.saxon.type.SchemaComponent;
import net.sf.saxon.type.SchemaException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.UntypedAtomicValue;

public enum AnySimpleType implements SimpleType
{
    INSTANCE;

    public static final StructuredQName NAME;

    @Override
    public String getName() {
        return "anySimpleType";
    }

    @Override
    public String getTargetNamespace() {
        return "http://www.w3.org/2001/XMLSchema";
    }

    @Override
    public String getEQName() {
        return "Q{http://www.w3.org/2001/XMLSchema}anySimpleType";
    }

    @Override
    public boolean isBuiltInType() {
        return true;
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
    public int getRedefinitionLevel() {
        return 0;
    }

    @Override
    public String getSystemId() {
        return null;
    }

    public static AnySimpleType getInstance() {
        return INSTANCE;
    }

    @Override
    public SchemaComponent.ValidationStatus getValidationStatus() {
        return SchemaComponent.ValidationStatus.VALIDATED;
    }

    @Override
    public SchemaType getBaseType() {
        return AnyType.getInstance();
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
    public int getFingerprint() {
        return 573;
    }

    @Override
    public StructuredQName getStructuredQName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "xs:anySimpleType";
    }

    @Override
    public String getDisplayName() {
        return "xs:anySimpleType";
    }

    @Override
    public boolean isSameType(SchemaType other) {
        return other == INSTANCE;
    }

    @Override
    public AtomicSequence atomize(NodeInfo node) {
        return new UntypedAtomicValue(node.getStringValueCS());
    }

    @Override
    public void checkTypeDerivationIsOK(SchemaType type, int block) throws SchemaException {
        if (type == this) {
            return;
        }
        throw new SchemaException("Cannot derive xs:anySimpleType from another type");
    }

    @Override
    public boolean isAtomicType() {
        return false;
    }

    @Override
    public boolean isAnonymousType() {
        return false;
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
    public SchemaType getBuiltInBaseType() {
        return this;
    }

    @Override
    public AtomicSequence getTypedValue(CharSequence value, NamespaceResolver resolver, ConversionRules rules) {
        return new UntypedAtomicValue(value);
    }

    @Override
    public ValidationFailure validateContent(CharSequence value, NamespaceResolver nsResolver, ConversionRules rules) {
        return null;
    }

    @Override
    public boolean isNamespaceSensitive() {
        return false;
    }

    @Override
    public int getBlock() {
        return 0;
    }

    @Override
    public int getDerivationMethod() {
        return 1;
    }

    @Override
    public boolean allowsDerivation(int derivation) {
        return true;
    }

    @Override
    public int getFinalProhibitions() {
        return 0;
    }

    @Override
    public int getWhitespaceAction() {
        return 0;
    }

    @Override
    public void analyzeContentExpression(Expression expression, int kind) {
    }

    @Override
    public CharSequence preprocess(CharSequence input) {
        return input;
    }

    @Override
    public CharSequence postprocess(CharSequence input) throws ValidationException {
        return input;
    }

    static {
        NAME = new StructuredQName("xs", "http://www.w3.org/2001/XMLSchema", "anySimpleType");
    }
}


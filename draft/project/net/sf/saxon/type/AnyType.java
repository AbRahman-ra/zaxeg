/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.type.AnySimpleType;
import net.sf.saxon.type.ComplexType;
import net.sf.saxon.type.SchemaComponent;
import net.sf.saxon.type.SchemaException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.value.UntypedAtomicValue;
import net.sf.saxon.z.IntHashSet;

public enum AnyType implements ComplexType
{
    INSTANCE;

    public static final StructuredQName QNAME;

    public static AnyType getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return "anyType";
    }

    @Override
    public String getEQName() {
        return "Q{http://www.w3.org/2001/XMLSchema}anyType";
    }

    @Override
    public String getTargetNamespace() {
        return "http://www.w3.org/2001/XMLSchema";
    }

    @Override
    public int getVariety() {
        return 3;
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
    public SchemaType getBaseType() {
        return null;
    }

    public SchemaType getKnownBaseType() throws IllegalStateException {
        return null;
    }

    @Override
    public int getDerivationMethod() {
        return 0;
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
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isComplexType() {
        return true;
    }

    @Override
    public boolean isAnonymousType() {
        return false;
    }

    @Override
    public boolean isSimpleType() {
        return false;
    }

    @Override
    public boolean isAtomicType() {
        return false;
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
    public int getBlock() {
        return 0;
    }

    @Override
    public boolean isComplexContent() {
        return true;
    }

    @Override
    public boolean isSimpleContent() {
        return false;
    }

    @Override
    public boolean isAllContent() {
        return false;
    }

    @Override
    public SimpleType getSimpleContentType() {
        return null;
    }

    @Override
    public boolean isRestricted() {
        return false;
    }

    @Override
    public boolean isEmptyContent() {
        return false;
    }

    @Override
    public boolean isEmptiable() {
        return true;
    }

    @Override
    public boolean isMixedContent() {
        return true;
    }

    @Override
    public int getFingerprint() {
        return 572;
    }

    @Override
    public StructuredQName getStructuredQName() {
        return QNAME;
    }

    @Override
    public String getDescription() {
        return "xs:anyType";
    }

    @Override
    public String getDisplayName() {
        return "xs:anyType";
    }

    @Override
    public String getSystemId() {
        return null;
    }

    @Override
    public boolean isSameType(SchemaType other) {
        return other instanceof AnyType;
    }

    @Override
    public void analyzeContentExpression(Expression expression, int kind) {
    }

    @Override
    public AtomicSequence atomize(NodeInfo node) {
        return new UntypedAtomicValue(node.getStringValue());
    }

    @Override
    public void checkTypeDerivationIsOK(SchemaType type, int block) throws SchemaException {
        if (!(type instanceof AnyType)) {
            throw new SchemaException("Cannot derive xs:anyType from another type");
        }
    }

    @Override
    public SchemaType getElementParticleType(int elementName, boolean considerExtensions) {
        return this;
    }

    @Override
    public int getElementParticleCardinality(int elementName, boolean considerExtensions) {
        return 57344;
    }

    @Override
    public SimpleType getAttributeUseType(StructuredQName attributeName) {
        return AnySimpleType.getInstance();
    }

    @Override
    public int getAttributeUseCardinality(StructuredQName attributeName) {
        return 24576;
    }

    @Override
    public boolean allowsAttributes() {
        return true;
    }

    @Override
    public void gatherAllPermittedChildren(IntHashSet children, boolean ignoreWildcards) {
        children.add(-1);
    }

    @Override
    public void gatherAllPermittedDescendants(IntHashSet descendants) {
        descendants.add(-1);
    }

    @Override
    public SchemaType getDescendantElementType(int fingerprint) {
        return this;
    }

    @Override
    public int getDescendantElementCardinality(int elementFingerprint) {
        return 57344;
    }

    @Override
    public boolean containsElementWildcard() {
        return true;
    }

    @Override
    public boolean hasAssertions() {
        return false;
    }

    static {
        QNAME = new StructuredQName("xs", "http://www.w3.org/2001/XMLSchema", "anyType");
    }
}


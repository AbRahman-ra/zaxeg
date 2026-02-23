/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import java.util.Collections;
import java.util.Optional;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnySimpleType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.SchemaComponent;
import net.sf.saxon.type.SchemaException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.type.UnionType;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.UntypedAtomicValue;

public final class ErrorType
extends NodeTest
implements AtomicType,
UnionType,
PlainType {
    private static ErrorType theInstance = new ErrorType();

    private ErrorType() {
    }

    @Override
    public UType getUType() {
        return UType.VOID;
    }

    @Override
    public String getName() {
        return "error";
    }

    @Override
    public String getTargetNamespace() {
        return "http://www.w3.org/2001/XMLSchema";
    }

    @Override
    public String getEQName() {
        return "Q{http://www.w3.org/2001/XMLSchema}error";
    }

    @Override
    public boolean containsListType() {
        return false;
    }

    public Iterable<PlainType> getPlainMemberTypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isBuiltInType() {
        return true;
    }

    @Override
    public int getRedefinitionLevel() {
        return 0;
    }

    @Override
    public String getSystemId() {
        return null;
    }

    public static ErrorType getInstance() {
        return theInstance;
    }

    @Override
    public SchemaComponent.ValidationStatus getValidationStatus() {
        return SchemaComponent.ValidationStatus.VALIDATED;
    }

    @Override
    public SchemaType getBaseType() {
        return AnySimpleType.getInstance();
    }

    public SchemaType getKnownBaseType() {
        return this.getBaseType();
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
        return 575;
    }

    @Override
    public StructuredQName getMatchingNodeName() {
        return StandardNames.getStructuredQName(575);
    }

    @Override
    public StructuredQName getTypeName() {
        return new StructuredQName("xs", "http://www.w3.org/2001/XMLSchema", "error");
    }

    @Override
    public String getDescription() {
        return "xs:error";
    }

    @Override
    public String getDisplayName() {
        return "xs:error";
    }

    @Override
    public boolean isSameType(SchemaType other) {
        return other instanceof ErrorType;
    }

    @Override
    public AtomicSequence atomize(NodeInfo node) {
        return new UntypedAtomicValue(node.getStringValueCS());
    }

    @Override
    public void checkTypeDerivationIsOK(SchemaType type, int block) throws SchemaException {
        if (type == this || type == AnySimpleType.getInstance()) {
            return;
        }
        throw new SchemaException("Type xs:error is not validly derived from " + type.getDescription());
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
    public boolean isAnonymousType() {
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
    public SchemaType getBuiltInBaseType() {
        return this;
    }

    @Override
    public AtomicSequence getTypedValue(CharSequence value, NamespaceResolver resolver, ConversionRules rules) throws ValidationException {
        throw new ValidationFailure("Cast to xs:error always fails").makeException();
    }

    @Override
    public StringConverter getStringConverter(ConversionRules rules) {
        return null;
    }

    @Override
    public ValidationFailure validateContent(CharSequence value, NamespaceResolver nsResolver, ConversionRules rules) {
        return new ValidationFailure("No content is ever valid against the type xs:error");
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
        return false;
    }

    @Override
    public int getFinalProhibitions() {
        return 15;
    }

    @Override
    public int getWhitespaceAction() {
        return 2;
    }

    @Override
    public void analyzeContentExpression(Expression expression, int kind) throws XPathException {
        throw new XPathException("No expression can ever return a value of type xs:error");
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
    public boolean isPlainType() {
        return true;
    }

    @Override
    public boolean matches(Item item, TypeHierarchy th) {
        return false;
    }

    @Override
    public boolean matches(int nodeKind, NodeName name, SchemaType annotation) {
        return false;
    }

    @Override
    public AtomicType getPrimitiveItemType() {
        return this;
    }

    @Override
    public int getPrimitiveType() {
        return 88;
    }

    @Override
    public double getDefaultPriority() {
        return -1000.0;
    }

    @Override
    public AtomicType getAtomizedItemType() {
        return BuiltInAtomicType.UNTYPED_ATOMIC;
    }

    @Override
    public boolean isAtomizable(TypeHierarchy th) {
        return false;
    }

    @Override
    public SequenceType getResultTypeOfCast() {
        return SequenceType.OPTIONAL_ITEM;
    }

    @Override
    public String toExportString() {
        return this.toString();
    }

    @Override
    public String toString() {
        return "xs:error";
    }

    @Override
    public ValidationFailure validate(AtomicValue primValue, CharSequence lexicalValue, ConversionRules rules) {
        return new ValidationFailure("No value is valid against type xs:error");
    }

    @Override
    public boolean isOrdered(boolean optimistic) {
        return false;
    }

    @Override
    public boolean isAbstract() {
        return true;
    }

    @Override
    public boolean isPrimitiveType() {
        return false;
    }

    @Override
    public StructuredQName getStructuredQName() {
        return new StructuredQName("xs", "http://www.w3.org/2001/XMLSchema", "error");
    }

    @Override
    public ValidationFailure checkAgainstFacets(AtomicValue value, ConversionRules rules) {
        return null;
    }

    @Override
    public Optional<String> explainMismatch(Item item, TypeHierarchy th) {
        return Optional.of("Evaluation of the supplied expression will always fail");
    }
}


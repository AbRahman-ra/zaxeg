/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.MappingFunction;
import net.sf.saxon.expr.MappingIterator;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.om.AtomicArray;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.type.AnySimpleType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.BuiltInType;
import net.sf.saxon.type.ListType;
import net.sf.saxon.type.SchemaComponent;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public class BuiltInListType
implements ListType {
    private int fingerprint;
    public static BuiltInListType ENTITIES = BuiltInListType.makeListType("http://www.w3.org/2001/XMLSchema", "ENTITIES");
    public static BuiltInListType IDREFS = BuiltInListType.makeListType("http://www.w3.org/2001/XMLSchema", "IDREFS");
    public static BuiltInListType NMTOKENS = BuiltInListType.makeListType("http://www.w3.org/2001/XMLSchema", "NMTOKENS");
    public static BuiltInListType ANY_URIS = BuiltInListType.makeListType("http://www.w3.org/2001/XMLSchema-instance", "anonymous_schemaLocationType");
    private BuiltInAtomicType itemType;

    @Override
    public boolean isBuiltInType() {
        return true;
    }

    @Override
    public String getSystemId() {
        return null;
    }

    @Override
    public int getRedefinitionLevel() {
        return 0;
    }

    @Override
    public int getWhitespaceAction() {
        return 2;
    }

    public BuiltInListType(int fingerprint) {
        this.fingerprint = fingerprint;
        switch (fingerprint) {
            case 564: {
                this.itemType = BuiltInAtomicType.ENTITY;
                break;
            }
            case 562: {
                this.itemType = BuiltInAtomicType.IDREF;
                break;
            }
            case 557: {
                this.itemType = BuiltInAtomicType.NMTOKEN;
                break;
            }
            case 645: {
                this.itemType = BuiltInAtomicType.ANY_URI;
            }
        }
    }

    @Override
    public SchemaComponent.ValidationStatus getValidationStatus() {
        return SchemaComponent.ValidationStatus.VALIDATED;
    }

    @Override
    public SchemaType getBaseType() {
        return AnySimpleType.getInstance();
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
        return this.fingerprint == 562;
    }

    @Override
    public boolean isListType() {
        return true;
    }

    @Override
    public boolean isUnionType() {
        return false;
    }

    @Override
    public boolean isAnonymousType() {
        return false;
    }

    @Override
    public SchemaType getBuiltInBaseType() {
        return this;
    }

    @Override
    public boolean isNamespaceSensitive() {
        return false;
    }

    @Override
    public String getName() {
        return StandardNames.getLocalName(this.fingerprint);
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
    public int getFingerprint() {
        return this.fingerprint;
    }

    @Override
    public String getDisplayName() {
        return StandardNames.getDisplayName(this.fingerprint);
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
    public int getBlock() {
        return 0;
    }

    public SchemaType getKnownBaseType() throws IllegalStateException {
        return AnySimpleType.getInstance();
    }

    @Override
    public int getDerivationMethod() {
        return 8;
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
    public AtomicSequence atomize(NodeInfo node) throws XPathException {
        try {
            return this.getTypedValue(node.getStringValue(), node.getAllNamespaces(), node.getConfiguration().getConversionRules());
        } catch (ValidationException err) {
            throw new XPathException("Internal error: value doesn't match its type annotation. " + err.getMessage());
        }
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
    public void checkTypeDerivationIsOK(SchemaType type, int block) {
    }

    public String getLocalName() {
        return StandardNames.getLocalName(this.fingerprint);
    }

    @Override
    public StructuredQName getStructuredQName() {
        return new StructuredQName("xs", "http://www.w3.org/2001/XMLSchema", this.getLocalName());
    }

    @Override
    public SimpleType getItemType() {
        return this.itemType;
    }

    public String applyWhitespaceNormalization(String value) {
        return Whitespace.collapseWhitespace(value).toString();
    }

    @Override
    public void analyzeContentExpression(Expression expression, int kind) throws XPathException {
        BuiltInAtomicType.analyzeContentExpression(this, expression, kind);
    }

    @Override
    public ValidationFailure validateContent(CharSequence value, NamespaceResolver nsResolver, ConversionRules rules) {
        StringValue val;
        SimpleType base = this.getItemType();
        Whitespace.Tokenizer iter = new Whitespace.Tokenizer(value);
        boolean found = false;
        while ((val = iter.next()) != null) {
            found = true;
            ValidationFailure v = base.validateContent(val.getStringValue(), nsResolver, rules);
            if (v == null) continue;
            return v;
        }
        if (!found) {
            return new ValidationFailure("The built-in list type " + StandardNames.getDisplayName(this.fingerprint) + " does not allow a zero-length list");
        }
        return null;
    }

    @Override
    public AtomicSequence getTypedValue(CharSequence value, NamespaceResolver resolver, ConversionRules rules) throws ValidationException {
        Whitespace.Tokenizer iter = new Whitespace.Tokenizer(value);
        ListTypeMappingFunction map = new ListTypeMappingFunction();
        map.resolver = resolver;
        map.atomicType = (AtomicType)this.getItemType();
        map.rules = rules;
        try {
            return new AtomicArray(new MappingIterator(iter, map));
        } catch (XPathException err) {
            throw new ValidationException(err);
        }
    }

    private static BuiltInListType makeListType(String namespace, String lname) {
        BuiltInListType t = new BuiltInListType(StandardNames.getFingerprint(namespace, lname));
        BuiltInType.register(t.getFingerprint(), t);
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

    private static class ListTypeMappingFunction
    implements MappingFunction {
        public NamespaceResolver resolver;
        public AtomicType atomicType;
        public ConversionRules rules;

        private ListTypeMappingFunction() {
        }

        @Override
        public AtomicIterator<AtomicValue> map(Item item) throws XPathException {
            try {
                return this.atomicType.getTypedValue(item.getStringValueCS(), this.resolver, this.rules).iterate();
            } catch (ValidationException err) {
                throw new XPathException(err);
            }
        }
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import java.util.List;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.om.Genre;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.Err;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.type.UnionType;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.SequenceType;

public class LocalUnionType
implements PlainType,
UnionType {
    private List<AtomicType> memberTypes;

    @Override
    public Genre getGenre() {
        return Genre.ATOMIC;
    }

    @Override
    public StructuredQName getTypeName() {
        return new StructuredQName("", "http://ns.saxonica.com/anonymous-type", "U" + this.hashCode());
    }

    public LocalUnionType(List<AtomicType> memberTypes) {
        this.memberTypes = memberTypes;
    }

    public List<AtomicType> getMemberTypes() {
        return this.memberTypes;
    }

    @Override
    public boolean isAtomicType() {
        return false;
    }

    @Override
    public boolean containsListType() {
        return false;
    }

    @Override
    public boolean isPlainType() {
        return true;
    }

    @Override
    public boolean isTrueItemType() {
        return true;
    }

    @Override
    public SequenceType getResultTypeOfCast() {
        return SequenceType.makeSequenceType(this, 24576);
    }

    public boolean isIdType() {
        return this.memberTypes.stream().anyMatch(AtomicType::isIdType);
    }

    public boolean isIdRefType() {
        return this.memberTypes.stream().anyMatch(AtomicType::isIdRefType);
    }

    public boolean isBuiltInType() {
        return false;
    }

    public boolean isListType() {
        return false;
    }

    public boolean isUnionType() {
        return true;
    }

    @Override
    public UType getUType() {
        UType u = UType.VOID;
        for (AtomicType at : this.memberTypes) {
            u = u.union(at.getUType());
        }
        return u;
    }

    @Override
    public String getBasicAlphaCode() {
        return "A";
    }

    @Override
    public boolean isNamespaceSensitive() {
        return this.memberTypes.stream().anyMatch(PlainType::isNamespaceSensitive);
    }

    public ValidationFailure validateContent(CharSequence value, NamespaceResolver nsResolver, ConversionRules rules) {
        for (AtomicType at : this.memberTypes) {
            ValidationFailure err = at.validateContent(value, nsResolver, rules);
            if (err != null) continue;
            return null;
        }
        return new ValidationFailure("Value " + Err.wrap(value, 4) + " does not match any member of union type " + this.toString());
    }

    @Override
    public ValidationFailure checkAgainstFacets(AtomicValue value, ConversionRules rules) {
        return null;
    }

    @Override
    public AtomicValue getTypedValue(CharSequence value, NamespaceResolver resolver, ConversionRules rules) throws ValidationException {
        for (AtomicType type : this.memberTypes) {
            StringConverter converter = rules.makeStringConverter(type);
            converter.setNamespaceResolver(resolver);
            ConversionResult outcome = converter.convertString(value);
            if (!(outcome instanceof AtomicValue)) continue;
            return (AtomicValue)outcome;
        }
        ValidationFailure ve = new ValidationFailure("Value " + Err.wrap(value, 4) + " does not match any member of union type " + this.toString());
        throw ve.makeException();
    }

    @Override
    public boolean matches(Item item, TypeHierarchy th) {
        if (item instanceof AtomicValue) {
            return this.memberTypes.stream().anyMatch(at -> at.matches(item, th));
        }
        return false;
    }

    @Override
    public AtomicType getPrimitiveItemType() {
        return BuiltInAtomicType.ANY_ATOMIC;
    }

    @Override
    public int getPrimitiveType() {
        return 632;
    }

    @Override
    public PlainType getAtomizedItemType() {
        return this;
    }

    @Override
    public boolean isAtomizable(TypeHierarchy th) {
        return true;
    }

    public Iterable<AtomicType> getPlainMemberTypes() {
        return this.memberTypes;
    }

    @Override
    public double getDefaultPriority() {
        double result = 1.0;
        for (AtomicType t : this.memberTypes) {
            result *= t.getDefaultPriority();
        }
        return result;
    }

    @Override
    public String toString() {
        FastStringBuffer fsb = new FastStringBuffer(256);
        fsb.append("union(");
        for (AtomicType at : this.memberTypes) {
            String member = at.getDisplayName();
            fsb.append(member);
            fsb.append(", ");
        }
        fsb.setLength(fsb.length() - 2);
        fsb.append(")");
        return fsb.toString();
    }

    @Override
    public String toExportString() {
        FastStringBuffer fsb = new FastStringBuffer(256);
        fsb.append("union(");
        for (AtomicType at : this.memberTypes) {
            fsb.append(at.toExportString());
            fsb.append(", ");
        }
        fsb.setLength(fsb.length() - 2);
        fsb.append(")");
        return fsb.toString();
    }
}


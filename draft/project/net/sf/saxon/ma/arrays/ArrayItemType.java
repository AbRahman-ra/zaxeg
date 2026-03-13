/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.arrays;

import java.util.Optional;
import java.util.function.Function;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.om.Genre;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyFunctionType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.SequenceType;

public class ArrayItemType
extends AnyFunctionType {
    public static final ArrayItemType ANY_ARRAY_TYPE = new ArrayItemType(SequenceType.ANY_SEQUENCE);
    public static final SequenceType SINGLE_ARRAY = SequenceType.makeSequenceType(ANY_ARRAY_TYPE, 16384);
    private SequenceType memberType;

    public ArrayItemType(SequenceType memberType) {
        this.memberType = memberType;
    }

    @Override
    public Genre getGenre() {
        return Genre.ARRAY;
    }

    public SequenceType getMemberType() {
        return this.memberType;
    }

    @Override
    public boolean isMapType() {
        return false;
    }

    @Override
    public boolean isArrayType() {
        return true;
    }

    @Override
    public String getBasicAlphaCode() {
        return "FA";
    }

    @Override
    public boolean isAtomizable(TypeHierarchy th) {
        return true;
    }

    @Override
    public PlainType getAtomizedItemType() {
        return this.memberType.getPrimaryType().getAtomizedItemType();
    }

    public int getArity() {
        return 1;
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[]{BuiltInAtomicType.INTEGER.one()};
    }

    @Override
    public double getDefaultPriority() {
        return this.memberType.getPrimaryType().getNormalizedDefaultPriority();
    }

    @Override
    public boolean matches(Item item, TypeHierarchy th) throws XPathException {
        if (!(item instanceof ArrayItem)) {
            return false;
        }
        if (this == ANY_ARRAY_TYPE) {
            return true;
        }
        for (Sequence sequence : ((ArrayItem)item).members()) {
            if (this.memberType.matches(sequence, th)) continue;
            return false;
        }
        return true;
    }

    @Override
    public SequenceType getResultType() {
        return this.memberType;
    }

    @Override
    public String toString() {
        return this.makeString(SequenceType::toString);
    }

    private String makeString(Function<SequenceType, String> show) {
        if (this.equals(ANY_ARRAY_TYPE)) {
            return "array(*)";
        }
        FastStringBuffer sb = new FastStringBuffer(100);
        sb.append("array(");
        sb.append(show.apply(this.memberType));
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String toExportString() {
        return this.makeString(SequenceType::toExportString);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof ArrayItemType) {
            ArrayItemType f2 = (ArrayItemType)other;
            return this.memberType.equals(f2.memberType);
        }
        return false;
    }

    public int hashCode() {
        return this.memberType.hashCode();
    }

    @Override
    public Affinity relationship(FunctionItemType other, TypeHierarchy th) {
        if (other == AnyFunctionType.getInstance()) {
            return Affinity.SUBSUMED_BY;
        }
        if (this.equals(other)) {
            return Affinity.SAME_TYPE;
        }
        if (other == ANY_ARRAY_TYPE) {
            return Affinity.SUBSUMED_BY;
        }
        if (other.isMapType()) {
            return Affinity.DISJOINT;
        }
        if (other instanceof ArrayItemType) {
            ArrayItemType f2 = (ArrayItemType)other;
            Affinity rel = th.sequenceTypeRelationship(this.memberType, f2.memberType);
            return rel == Affinity.DISJOINT ? Affinity.OVERLAPS : rel;
        }
        Affinity rel = new SpecificFunctionType(this.getArgumentTypes(), this.getResultType()).relationship(other, th);
        if (rel == Affinity.SUBSUMES || rel == Affinity.SAME_TYPE) {
            rel = Affinity.OVERLAPS;
        }
        return rel;
    }

    @Override
    public Expression makeFunctionSequenceCoercer(Expression exp, RoleDiagnostic role) throws XPathException {
        return new SpecificFunctionType(this.getArgumentTypes(), this.getResultType()).makeFunctionSequenceCoercer(exp, role);
    }

    @Override
    public Optional<String> explainMismatch(Item item, TypeHierarchy th) {
        if (item instanceof ArrayItem) {
            for (int i = 0; i < ((ArrayItem)item).arrayLength(); ++i) {
                try {
                    GroundedValue member = ((ArrayItem)item).get(i);
                    if (this.memberType.matches(member, th)) continue;
                    String s = "The " + RoleDiagnostic.ordinal(i + 1) + " member of the supplied array {" + Err.depictSequence(member) + "} does not match the required member type " + this.memberType;
                    Optional<String> more = this.memberType.explainMismatch(member, th);
                    if (more.isPresent()) {
                        s = s + ". " + more.get();
                    }
                    return Optional.of(s);
                } catch (XPathException e) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }
}


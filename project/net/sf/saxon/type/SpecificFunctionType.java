/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.functions.hof.FunctionSequenceCoercer;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyFunctionType;
import net.sf.saxon.type.AnyFunctionTypeWithAssertions;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.SequenceType;

public class SpecificFunctionType
extends AnyFunctionType {
    private SequenceType[] argTypes;
    private SequenceType resultType;
    private AnnotationList annotations;
    private Configuration config;

    public SpecificFunctionType(SequenceType[] argTypes, SequenceType resultType) {
        this.argTypes = Objects.requireNonNull(argTypes);
        this.resultType = Objects.requireNonNull(resultType);
        this.annotations = AnnotationList.EMPTY;
    }

    public SpecificFunctionType(SequenceType[] argTypes, SequenceType resultType, AnnotationList annotations) {
        this.argTypes = Objects.requireNonNull(argTypes);
        this.resultType = Objects.requireNonNull(resultType);
        this.annotations = Objects.requireNonNull(annotations);
    }

    public int getArity() {
        return this.argTypes.length;
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return this.argTypes;
    }

    @Override
    public SequenceType getResultType() {
        return this.resultType;
    }

    @Override
    public AnnotationList getAnnotationAssertions() {
        return this.annotations;
    }

    @Override
    public boolean isAtomizable(TypeHierarchy th) {
        if (this.getArity() != 1) {
            return false;
        }
        ItemType argType = this.getArgumentTypes()[0].getPrimaryType();
        return th.isSubType(BuiltInAtomicType.INTEGER, argType);
    }

    @Override
    public String toString() {
        FastStringBuffer sb = new FastStringBuffer(100);
        sb.append("(function(");
        for (int i = 0; i < this.argTypes.length; ++i) {
            sb.append(this.argTypes[i].toString());
            if (i >= this.argTypes.length - 1) continue;
            sb.append(", ");
        }
        sb.append(") as ");
        sb.append(this.resultType.toString());
        sb.cat(')');
        return sb.toString();
    }

    @Override
    public String toExportString() {
        FastStringBuffer sb = new FastStringBuffer(100);
        sb.append("(function(");
        for (int i = 0; i < this.argTypes.length; ++i) {
            sb.append(this.argTypes[i].toExportString());
            if (i >= this.argTypes.length - 1) continue;
            sb.append(", ");
        }
        sb.append(") as ");
        sb.append(this.resultType.toExportString());
        sb.cat(')');
        return sb.toString();
    }

    public boolean equals(Object other) {
        if (other instanceof SpecificFunctionType) {
            SpecificFunctionType f2 = (SpecificFunctionType)other;
            if (!this.resultType.equals(f2.resultType)) {
                return false;
            }
            if (this.argTypes.length != f2.argTypes.length) {
                return false;
            }
            for (int i = 0; i < this.argTypes.length; ++i) {
                if (this.argTypes[i].equals(f2.argTypes[i])) continue;
                return false;
            }
            return this.getAnnotationAssertions().equals(f2.getAnnotationAssertions());
        }
        return false;
    }

    public int hashCode() {
        int h = this.resultType.hashCode() ^ this.argTypes.length;
        for (SequenceType argType : this.argTypes) {
            h ^= argType.hashCode();
        }
        return h;
    }

    @Override
    public Affinity relationship(FunctionItemType other, TypeHierarchy th) {
        if (other == AnyFunctionType.getInstance() || other instanceof AnyFunctionTypeWithAssertions) {
            return Affinity.SUBSUMED_BY;
        }
        if (this.equals(other)) {
            return Affinity.SAME_TYPE;
        }
        if (other instanceof ArrayItemType || other instanceof MapType) {
            Affinity rrel = other.relationship(this, th);
            switch (rrel) {
                case SUBSUMES: {
                    return Affinity.SUBSUMED_BY;
                }
                case SUBSUMED_BY: {
                    return Affinity.SUBSUMES;
                }
            }
            return rrel;
        }
        if (this.argTypes.length != other.getArgumentTypes().length) {
            return Affinity.DISJOINT;
        }
        boolean wider = false;
        boolean narrower = false;
        block16: for (int i = 0; i < this.argTypes.length; ++i) {
            Affinity argRel = th.sequenceTypeRelationship(this.argTypes[i], other.getArgumentTypes()[i]);
            switch (argRel) {
                case DISJOINT: {
                    return Affinity.DISJOINT;
                }
                case SUBSUMES: {
                    narrower = true;
                    continue block16;
                }
                case SUBSUMED_BY: {
                    wider = true;
                    continue block16;
                }
                case OVERLAPS: {
                    wider = true;
                    narrower = true;
                    continue block16;
                }
            }
        }
        Affinity resRel = th.sequenceTypeRelationship(this.resultType, other.getResultType());
        switch (resRel) {
            case DISJOINT: {
                return Affinity.DISJOINT;
            }
            case SUBSUMES: {
                wider = true;
                break;
            }
            case SUBSUMED_BY: {
                narrower = true;
                break;
            }
            case OVERLAPS: {
                wider = true;
                narrower = true;
                break;
            }
        }
        if (wider) {
            if (narrower) {
                return Affinity.OVERLAPS;
            }
            return Affinity.SUBSUMES;
        }
        if (narrower) {
            return Affinity.SUBSUMED_BY;
        }
        return Affinity.SAME_TYPE;
    }

    @Override
    public double getDefaultPriority() {
        double prio = 1.0;
        for (SequenceType st : this.getArgumentTypes()) {
            prio *= st.getPrimaryType().getNormalizedDefaultPriority();
        }
        return prio;
    }

    @Override
    public boolean matches(Item item, TypeHierarchy th) {
        if (!(item instanceof Function)) {
            return false;
        }
        if (item instanceof MapItem) {
            if (this.getArity() == 1 && this.argTypes[0].getCardinality() == 16384 && this.argTypes[0].getPrimaryType().isPlainType() && Cardinality.allowsZero(this.resultType.getCardinality())) {
                for (KeyValuePair pair : ((MapItem)item).keyValuePairs()) {
                    try {
                        if (this.resultType.matches(pair.value, th)) continue;
                        return false;
                    } catch (XPathException xPathException) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        if (item instanceof ArrayItem) {
            if (this.getArity() == 1 && this.argTypes[0].getCardinality() == 16384 && this.argTypes[0].getPrimaryType().isPlainType()) {
                Affinity rel = th.relationship(this.argTypes[0].getPrimaryType(), BuiltInAtomicType.INTEGER);
                if (rel != Affinity.SAME_TYPE && rel != Affinity.SUBSUMED_BY) {
                    return false;
                }
                for (Sequence sequence : ((ArrayItem)item).members()) {
                    try {
                        if (this.resultType.matches(sequence, th)) continue;
                        return false;
                    } catch (XPathException e) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        Affinity rel = th.relationship(((Function)item).getFunctionItemType(), this);
        return rel == Affinity.SAME_TYPE || rel == Affinity.SUBSUMED_BY;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public Optional<String> explainMismatch(Item item, TypeHierarchy th) {
        Object s;
        if (!(item instanceof Function)) {
            return Optional.empty();
        }
        if (item instanceof MapItem) {
            Iterator<KeyValuePair> iterator;
            if (this.getArity() != 1) {
                String s2 = "The function arity is " + this.getArity() + "; a map can only be supplied for a function type with arity 1";
                return Optional.of(s2);
            }
            if (this.argTypes[0].getCardinality() == 16384 && this.argTypes[0].getPrimaryType().isPlainType()) {
                iterator = ((MapItem)item).keyValuePairs().iterator();
            } else {
                String s3 = "The function argument is of type " + this.argTypes[0] + "; a map can only be supplied for a function type whose argument type is atomic";
                return Optional.of(s3);
            }
            while (iterator.hasNext()) {
                KeyValuePair pair = iterator.next();
                try {
                    if (this.resultType.matches(pair.value, th)) continue;
                    String s4 = "The supplied map contains an entry with key (" + pair.key + ") whose corresponding value (" + Err.depictSequence(pair.value) + ") is not an instance of the return type in the function signature (" + this.resultType + ")";
                    Optional<String> more = this.resultType.explainMismatch(pair.value, th);
                    if (more.isPresent()) {
                        s4 = s4 + ". " + more.get();
                    }
                    return Optional.of(s4);
                } catch (XPathException e) {
                    return Optional.empty();
                }
            }
        }
        if (item instanceof ArrayItem) {
            String s5;
            if (this.getArity() != 1) {
                s5 = "The function arity is " + this.getArity() + "; an array can only be supplied for a function type with arity 1";
                return Optional.of(s5);
            }
            if (this.argTypes[0].getCardinality() == 16384 && this.argTypes[0].getPrimaryType().isPlainType()) {
                Affinity rel = th.relationship(this.argTypes[0].getPrimaryType(), BuiltInAtomicType.INTEGER);
                if (rel != Affinity.SAME_TYPE && rel != Affinity.SUBSUMED_BY) {
                    s = "The function expects an argument of type " + this.argTypes[0] + "; an array can only be supplied for a function that expects an integer";
                    return Optional.of(s);
                }
                s = ((ArrayItem)item).members().iterator();
            } else {
                s5 = "The function argument is of type " + this.argTypes[0] + "; an array can only be supplied for a function type whose argument type is xs:integer";
                return Optional.of(s5);
            }
            while (s.hasNext()) {
                GroundedValue member = (GroundedValue)s.next();
                try {
                    if (this.resultType.matches(member, th)) continue;
                    String s6 = "The supplied array contains an entry (" + Err.depictSequence(member) + ") is not an instance of the return type in the function signature (" + this.resultType + ")";
                    Optional<String> more = this.resultType.explainMismatch(member, th);
                    if (more.isPresent()) {
                        s6 = s6 + ". " + more.get();
                    }
                    return Optional.of(s6);
                } catch (XPathException e) {
                    return Optional.empty();
                }
            }
        }
        FunctionItemType other = ((Function)item).getFunctionItemType();
        if (this.getArity() != ((Function)item).getArity()) {
            s = "The required function arity is " + this.getArity() + "; the supplied function has arity " + ((Function)item).getArity();
            return Optional.of(s);
        }
        Affinity rel = th.sequenceTypeRelationship(this.resultType, other.getResultType());
        if (rel != Affinity.SAME_TYPE && rel != Affinity.SUBSUMES) {
            String s7 = "The return type of the required function is " + this.resultType + " but the returntype of the supplied function is " + other.getResultType();
            return Optional.of(s7);
        }
        int j = 0;
        while (j < this.getArity()) {
            rel = th.sequenceTypeRelationship(this.argTypes[j], other.getArgumentTypes()[j]);
            if (rel != Affinity.SAME_TYPE && rel != Affinity.SUBSUMED_BY) {
                String s8 = "The type of the " + RoleDiagnostic.ordinal(j + 1) + " argument of the required function is " + this.argTypes[j] + " but the declaredtype of the corresponding argument of the supplied function is " + other.getArgumentTypes()[j];
                return Optional.of(s8);
            }
            ++j;
        }
        return Optional.empty();
    }

    @Override
    public Expression makeFunctionSequenceCoercer(Expression exp, RoleDiagnostic role) throws XPathException {
        return new FunctionSequenceCoercer(exp, this, role);
    }
}


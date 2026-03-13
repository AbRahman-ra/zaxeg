/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.map;

import java.util.Optional;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.TupleItemType;
import net.sf.saxon.om.Genre;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyFunctionType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.SequenceType;

public class MapType
extends AnyFunctionType {
    public static final MapType ANY_MAP_TYPE = new MapType(BuiltInAtomicType.ANY_ATOMIC, SequenceType.ANY_SEQUENCE);
    public static final MapType EMPTY_MAP_TYPE = new MapType(BuiltInAtomicType.ANY_ATOMIC, SequenceType.ANY_SEQUENCE, true);
    public static final SequenceType OPTIONAL_MAP_ITEM = SequenceType.makeSequenceType(ANY_MAP_TYPE, 24576);
    public static final SequenceType SINGLE_MAP_ITEM = SequenceType.makeSequenceType(ANY_MAP_TYPE, 16384);
    public static final SequenceType SEQUENCE_OF_MAPS = SequenceType.makeSequenceType(ANY_MAP_TYPE, 57344);
    private AtomicType keyType;
    private SequenceType valueType;
    private boolean mustBeEmpty;

    public MapType(AtomicType keyType, SequenceType valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
        this.mustBeEmpty = false;
    }

    public MapType(AtomicType keyType, SequenceType valueType, boolean mustBeEmpty) {
        this.keyType = keyType;
        this.valueType = valueType;
        this.mustBeEmpty = mustBeEmpty;
    }

    @Override
    public Genre getGenre() {
        return Genre.MAP;
    }

    public AtomicType getKeyType() {
        return this.keyType;
    }

    public SequenceType getValueType() {
        return this.valueType;
    }

    @Override
    public boolean isMapType() {
        return true;
    }

    @Override
    public boolean isArrayType() {
        return false;
    }

    @Override
    public String getBasicAlphaCode() {
        return "FM";
    }

    @Override
    public boolean isAtomizable(TypeHierarchy th) {
        return false;
    }

    @Override
    public double getDefaultPriority() {
        return this.keyType.getNormalizedDefaultPriority() * this.valueType.getPrimaryType().getNormalizedDefaultPriority();
    }

    @Override
    public boolean matches(Item item, TypeHierarchy th) {
        if (!(item instanceof MapItem)) {
            return false;
        }
        if (((MapItem)item).isEmpty()) {
            return true;
        }
        if (this.mustBeEmpty) {
            return false;
        }
        if (this == ANY_MAP_TYPE) {
            return true;
        }
        return ((MapItem)item).conforms(this.keyType, this.valueType, th);
    }

    public int getArity() {
        return 1;
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[]{SequenceType.makeSequenceType(BuiltInAtomicType.ANY_ATOMIC, 16384)};
    }

    @Override
    public SequenceType getResultType() {
        if (Cardinality.allowsZero(this.valueType.getCardinality())) {
            return this.valueType;
        }
        return SequenceType.makeSequenceType(this.valueType.getPrimaryType(), Cardinality.union(this.valueType.getCardinality(), 8192));
    }

    @Override
    public String toString() {
        if (this == ANY_MAP_TYPE) {
            return "map(*)";
        }
        if (this == EMPTY_MAP_TYPE) {
            return "map{}";
        }
        FastStringBuffer sb = new FastStringBuffer(100);
        sb.append("map(");
        sb.append(this.keyType.toString());
        sb.append(", ");
        sb.append(this.valueType.toString());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String toExportString() {
        if (this == ANY_MAP_TYPE) {
            return "map(*)";
        }
        if (this == EMPTY_MAP_TYPE) {
            return "map{}";
        }
        FastStringBuffer sb = new FastStringBuffer(100);
        sb.append("map(");
        sb.append(this.keyType.toExportString());
        sb.append(", ");
        sb.append(this.valueType.toExportString());
        sb.append(")");
        return sb.toString();
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof MapType) {
            MapType f2 = (MapType)other;
            return this.keyType.equals(f2.keyType) && this.valueType.equals(f2.valueType) && this.mustBeEmpty == f2.mustBeEmpty;
        }
        return false;
    }

    public int hashCode() {
        return this.keyType.hashCode() ^ this.valueType.hashCode();
    }

    @Override
    public Affinity relationship(FunctionItemType other, TypeHierarchy th) {
        if (other == AnyFunctionType.getInstance()) {
            return Affinity.SUBSUMED_BY;
        }
        if (this.equals(other)) {
            return Affinity.SAME_TYPE;
        }
        if (other == ANY_MAP_TYPE) {
            return Affinity.SUBSUMED_BY;
        }
        if (other.isArrayType()) {
            return Affinity.DISJOINT;
        }
        if (other instanceof TupleItemType) {
            return TypeHierarchy.inverseRelationship(other.relationship(this, th));
        }
        if (other instanceof MapType) {
            MapType f2 = (MapType)other;
            Affinity keyRel = th.relationship(this.keyType, f2.keyType);
            if (keyRel == Affinity.DISJOINT) {
                return Affinity.OVERLAPS;
            }
            Affinity valueRel = th.sequenceTypeRelationship(this.valueType, f2.valueType);
            if (valueRel == Affinity.DISJOINT) {
                return Affinity.OVERLAPS;
            }
            if (keyRel == valueRel) {
                return keyRel;
            }
            if (!(keyRel != Affinity.SAME_TYPE && keyRel != Affinity.SUBSUMES || valueRel != Affinity.SAME_TYPE && valueRel != Affinity.SUBSUMES)) {
                return Affinity.SUBSUMES;
            }
            if (!(keyRel != Affinity.SAME_TYPE && keyRel != Affinity.SUBSUMED_BY || valueRel != Affinity.SAME_TYPE && valueRel != Affinity.SUBSUMED_BY)) {
                return Affinity.SUBSUMED_BY;
            }
            return Affinity.OVERLAPS;
        }
        SequenceType st = this.getResultType();
        if (!Cardinality.allowsZero(st.getCardinality())) {
            st = SequenceType.makeSequenceType(st.getPrimaryType(), Cardinality.union(st.getCardinality(), 8192));
        }
        return new SpecificFunctionType(new SequenceType[]{SequenceType.ATOMIC_SEQUENCE}, st).relationship(other, th);
    }

    @Override
    public Optional<String> explainMismatch(Item item, TypeHierarchy th) {
        if (item instanceof MapItem) {
            for (KeyValuePair kvp : ((MapItem)item).keyValuePairs()) {
                if (!this.keyType.matches(kvp.key, th)) {
                    String s = "The map contains a key (" + kvp.key + ") of type " + kvp.key.getItemType() + " that is not an instance of the required type " + this.keyType;
                    return Optional.of(s);
                }
                try {
                    if (this.valueType.matches(kvp.value, th)) continue;
                    String s = "The map contains an entry with key (" + kvp.key + ") whose corresponding value (" + Err.depictSequence(kvp.value) + ") is not an instance of the required type " + this.valueType;
                    Optional<String> more = this.valueType.explainMismatch(kvp.value, th);
                    if (more.isPresent()) {
                        s = s + ". " + more.get();
                    }
                    return Optional.of(s);
                } catch (XPathException xPathException) {
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Expression makeFunctionSequenceCoercer(Expression exp, RoleDiagnostic role) throws XPathException {
        return new SpecificFunctionType(this.getArgumentTypes(), this.getResultType()).makeFunctionSequenceCoercer(exp, role);
    }
}


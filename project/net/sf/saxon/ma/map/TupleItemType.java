/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.map;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.ma.map.TupleType;
import net.sf.saxon.om.Genre;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyFunctionType;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class TupleItemType
extends AnyFunctionType
implements TupleType {
    private Map<String, SequenceType> fields = new HashMap<String, SequenceType>();
    private boolean extensible;

    public TupleItemType(List<String> names, List<SequenceType> types, boolean extensible) {
        for (int i = 0; i < names.size(); ++i) {
            this.fields.put(names.get(i), types.get(i));
        }
        this.extensible = extensible;
    }

    @Override
    public Genre getGenre() {
        return Genre.MAP;
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
    public Iterable<String> getFieldNames() {
        return this.fields.keySet();
    }

    @Override
    public SequenceType getFieldType(String field) {
        return this.fields.get(field);
    }

    @Override
    public boolean isExtensible() {
        return this.extensible;
    }

    @Override
    public boolean matches(Item item, TypeHierarchy th) throws XPathException {
        if (!(item instanceof MapItem)) {
            return false;
        }
        MapItem map = (MapItem)item;
        for (Map.Entry<String, SequenceType> field : this.fields.entrySet()) {
            EmptySequence val = map.get(new StringValue(field.getKey()));
            if (val == null) {
                val = EmptySequence.getInstance();
            }
            if (field.getValue().matches(val, th)) continue;
            return false;
        }
        if (!this.extensible) {
            Item key;
            AtomicIterator<? extends AtomicValue> keyIter = map.keys();
            while ((key = keyIter.next()) != null) {
                if (key instanceof StringValue && this.fields.containsKey(((AtomicValue)key).getStringValue())) continue;
                return false;
            }
        }
        return true;
    }

    public int getArity() {
        return 1;
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[]{SequenceType.SINGLE_ATOMIC};
    }

    @Override
    public SequenceType getResultType() {
        if (this.extensible) {
            return SequenceType.ANY_SEQUENCE;
        }
        ItemType resultType = null;
        boolean allowsMany = false;
        for (Map.Entry<String, SequenceType> field : this.fields.entrySet()) {
            resultType = resultType == null ? field.getValue().getPrimaryType() : Type.getCommonSuperType(resultType, field.getValue().getPrimaryType());
            allowsMany = allowsMany || Cardinality.allowsMany(field.getValue().getCardinality());
        }
        return SequenceType.makeSequenceType(resultType, allowsMany ? 57344 : 24576);
    }

    @Override
    public double getDefaultPriority() {
        double prio = 1.0;
        for (SequenceType st : this.fields.values()) {
            prio *= st.getPrimaryType().getNormalizedDefaultPriority();
        }
        return this.extensible ? 0.5 + prio / 2.0 : prio;
    }

    @Override
    public String toString() {
        return this.makeString(SequenceType::toString);
    }

    @Override
    public String toExportString() {
        return this.makeString(SequenceType::toExportString);
    }

    @Override
    public String getBasicAlphaCode() {
        return "FM";
    }

    private String makeString(Function<SequenceType, String> show) {
        FastStringBuffer sb = new FastStringBuffer(100);
        sb.append("tuple(");
        boolean first = true;
        for (Map.Entry<String, SequenceType> field : this.fields.entrySet()) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(field.getKey());
            sb.append(": ");
            sb.append(show.apply(field.getValue()));
        }
        if (this.isExtensible()) {
            sb.append(", *");
        }
        sb.append(")");
        return sb.toString();
    }

    public boolean equals(Object other) {
        return this == other || other instanceof TupleItemType && this.extensible == ((TupleItemType)other).extensible && this.fields.equals(((TupleItemType)other).fields);
    }

    public int hashCode() {
        return this.fields.hashCode();
    }

    @Override
    public Affinity relationship(FunctionItemType other, TypeHierarchy th) {
        if (other == AnyFunctionType.getInstance()) {
            return Affinity.SUBSUMED_BY;
        }
        if (other instanceof TupleItemType) {
            return this.tupleTypeRelationship((TupleItemType)other, th);
        }
        if (other == MapType.ANY_MAP_TYPE) {
            return Affinity.SUBSUMED_BY;
        }
        if (other.isArrayType()) {
            return Affinity.DISJOINT;
        }
        if (other instanceof MapType) {
            return this.tupleToMapRelationship((MapType)other, th);
        }
        Affinity rel = new SpecificFunctionType(this.getArgumentTypes(), this.getResultType()).relationship(other, th);
        return rel;
    }

    private Affinity tupleToMapRelationship(MapType other, TypeHierarchy th) {
        BuiltInAtomicType tupleKeyType = this.isExtensible() ? BuiltInAtomicType.ANY_ATOMIC : BuiltInAtomicType.STRING;
        Affinity keyRel = th.relationship(tupleKeyType, other.getKeyType());
        if (keyRel == Affinity.DISJOINT) {
            return Affinity.DISJOINT;
        }
        if (other.getValueType().getPrimaryType().equals(AnyItemType.getInstance()) && other.getValueType().getCardinality() == 57344) {
            if (keyRel == Affinity.SUBSUMED_BY || keyRel == Affinity.SAME_TYPE) {
                return Affinity.SUBSUMED_BY;
            }
            return Affinity.OVERLAPS;
        }
        if (this.isExtensible()) {
            return Affinity.OVERLAPS;
        }
        for (SequenceType entry : this.fields.values()) {
            Affinity rel = th.sequenceTypeRelationship(entry, other.getValueType());
            if (rel == Affinity.SUBSUMED_BY || rel == Affinity.SAME_TYPE) continue;
            return Affinity.OVERLAPS;
        }
        return Affinity.SUBSUMED_BY;
    }

    private Affinity tupleTypeRelationship(TupleItemType other, TypeHierarchy th) {
        HashSet<String> keys = new HashSet<String>(this.fields.keySet());
        keys.addAll(other.fields.keySet());
        boolean foundSubsuming = false;
        boolean foundSubsumed = false;
        boolean foundOverlap = false;
        if (this.isExtensible()) {
            if (!other.isExtensible()) {
                foundSubsuming = true;
            }
        } else if (other.isExtensible()) {
            foundSubsumed = true;
        }
        for (String key : keys) {
            SequenceType t1 = this.fields.get(key);
            SequenceType t2 = other.fields.get(key);
            if (t1 == null) {
                if (this.isExtensible()) {
                    foundSubsuming = true;
                    continue;
                }
                if (Cardinality.allowsZero(t2.getCardinality())) {
                    foundOverlap = true;
                    continue;
                }
                return Affinity.DISJOINT;
            }
            if (t2 == null) {
                if (other.isExtensible()) {
                    foundSubsumed = true;
                    continue;
                }
                if (Cardinality.allowsZero(t1.getCardinality())) {
                    foundOverlap = true;
                    continue;
                }
                return Affinity.DISJOINT;
            }
            Affinity a = th.sequenceTypeRelationship(t1, t2);
            switch (a) {
                case SAME_TYPE: {
                    break;
                }
                case SUBSUMED_BY: {
                    foundSubsumed = true;
                    break;
                }
                case SUBSUMES: {
                    foundSubsuming = true;
                    break;
                }
                case OVERLAPS: {
                    foundOverlap = true;
                    break;
                }
                case DISJOINT: {
                    return Affinity.DISJOINT;
                }
            }
        }
        if (foundOverlap || foundSubsumed && foundSubsuming) {
            return Affinity.OVERLAPS;
        }
        if (foundSubsuming) {
            return Affinity.SUBSUMES;
        }
        if (foundSubsumed) {
            return Affinity.SUBSUMED_BY;
        }
        return Affinity.SAME_TYPE;
    }

    @Override
    public Optional<String> explainMismatch(Item item, TypeHierarchy th) {
        if (item instanceof MapItem) {
            for (Map.Entry<String, SequenceType> entry : this.fields.entrySet()) {
                String key = entry.getKey();
                SequenceType required = entry.getValue();
                GroundedValue value = ((MapItem)item).get(new StringValue(key));
                if (value == null) {
                    if (Cardinality.allowsZero(required.getCardinality())) continue;
                    return Optional.of("Field " + key + " is absent; it must have a value");
                }
                try {
                    if (required.matches(value, th)) continue;
                    String s = "Field " + key + " has value " + Err.depictSequence(value) + " which does not match the required type " + required.toString();
                    Optional<String> more = required.explainMismatch(value, th);
                    if (more.isPresent()) {
                        s = s + ". " + more.get();
                    }
                    return Optional.of(s);
                } catch (XPathException err) {
                    return Optional.empty();
                }
            }
            if (!this.extensible) {
                Item key;
                AtomicIterator<? extends AtomicValue> keyIter = ((MapItem)item).keys();
                while ((key = keyIter.next()) != null) {
                    if (!(key instanceof StringValue)) {
                        return Optional.of("Undeclared field " + key + " is present, but it is not a string, and the tuple type is not extensible");
                    }
                    if (this.fields.containsKey(((AtomicValue)key).getStringValue())) continue;
                    return Optional.of("Undeclared field " + key + " is present, but the tuple type is not extensible");
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


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.CardinalityCheckingIterator;
import net.sf.saxon.expr.ItemMappingFunction;
import net.sf.saxon.expr.ItemMappingIterator;
import net.sf.saxon.expr.ItemTypeCheckingFunction;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.functions.hof.FunctionSequenceCoercer;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.lib.FunctionAnnotationHandler;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.DocumentNodeTest;
import net.sf.saxon.pattern.LocalNameTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NamespaceTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.QNameTest;
import net.sf.saxon.pattern.SameNameTest;
import net.sf.saxon.query.Annotation;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyExternalObjectType;
import net.sf.saxon.type.AnyFunctionType;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.AnyType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Converter;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.type.MissingComponentException;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.type.UType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.UntypedAtomicValue;
import net.sf.saxon.z.IntHashSet;
import net.sf.saxon.z.IntSet;
import net.sf.saxon.z.IntUniversalSet;

public class TypeHierarchy {
    private Map<ItemTypePair, Affinity> map;
    protected Configuration config;

    public TypeHierarchy(Configuration config) {
        this.config = config;
        this.map = new ConcurrentHashMap<ItemTypePair, Affinity>();
    }

    public Sequence applyFunctionConversionRules(Sequence value, SequenceType requiredType, RoleDiagnostic role, Location locator) throws XPathException {
        GroundedValue groundedValue = value.materialize();
        if (requiredType.matches(groundedValue, this)) {
            return groundedValue;
        }
        ItemType suppliedItemType = SequenceTool.getItemType(groundedValue, this);
        SequenceIterator iterator = groundedValue.iterate();
        ItemType requiredItemType = requiredType.getPrimaryType();
        if (requiredItemType.isPlainType()) {
            if (!suppliedItemType.isPlainType()) {
                try {
                    iterator = Atomizer.getAtomizingIterator(iterator, false);
                } catch (XPathException e) {
                    ValidationFailure vf = new ValidationFailure("Failed to atomize the " + role.getMessage() + ": " + e.getMessage());
                    vf.setErrorCode("XPTY0117");
                    throw vf.makeException();
                }
                suppliedItemType = suppliedItemType.getAtomizedItemType();
            }
            if (this.relationship(suppliedItemType, BuiltInAtomicType.UNTYPED_ATOMIC) != Affinity.DISJOINT && !this.isSubType(BuiltInAtomicType.UNTYPED_ATOMIC, requiredItemType)) {
                ItemMappingFunction converter;
                boolean nsSensitive = ((SimpleType)((Object)requiredItemType)).isNamespaceSensitive();
                if (nsSensitive) {
                    converter = item -> {
                        if (item instanceof UntypedAtomicValue) {
                            ValidationFailure vf = new ValidationFailure("Failed to convert the " + role.getMessage() + ": Implicit conversion of untypedAtomic value to " + requiredItemType + " is not allowed");
                            vf.setErrorCode("XPTY0117");
                            throw vf.makeException();
                        }
                        return item;
                    };
                } else if (((SimpleType)((Object)requiredItemType)).isUnionType()) {
                    ConversionRules rules = this.config.getConversionRules();
                    converter = item -> {
                        if (item instanceof UntypedAtomicValue) {
                            try {
                                return ((SimpleType)((Object)requiredItemType)).getTypedValue(item.getStringValueCS(), null, rules).head();
                            } catch (ValidationException ve) {
                                ve.setErrorCode("XPTY0004");
                                throw ve;
                            }
                        }
                        return item;
                    };
                } else {
                    converter = item -> {
                        if (item instanceof UntypedAtomicValue) {
                            return Converter.convert((UntypedAtomicValue)item, (AtomicType)requiredItemType, this.config.getConversionRules());
                        }
                        return item;
                    };
                }
                iterator = new ItemMappingIterator(iterator, converter, true);
            }
            if (requiredItemType.equals(BuiltInAtomicType.DOUBLE)) {
                ItemMappingFunction promoter = item -> {
                    if (item instanceof NumericValue) {
                        return (DoubleValue)Converter.convert((NumericValue)item, BuiltInAtomicType.DOUBLE, this.config.getConversionRules()).asAtomic();
                    }
                    throw new XPathException("Failed to convert the " + role.getMessage() + ": Cannot promote non-numeric value to xs:double", "XPTY0004");
                };
                iterator = new ItemMappingIterator(iterator, promoter, true);
            } else if (requiredItemType.equals(BuiltInAtomicType.FLOAT)) {
                ItemMappingFunction promoter = item -> {
                    if (item instanceof DoubleValue) {
                        throw new XPathException("Failed to convert the " + role.getMessage() + ": Cannot promote xs:double value to xs:float", "XPTY0004");
                    }
                    if (item instanceof NumericValue) {
                        return (FloatValue)Converter.convert((NumericValue)item, BuiltInAtomicType.FLOAT, this.config.getConversionRules()).asAtomic();
                    }
                    throw new XPathException("Failed to convert the " + role.getMessage() + ": Cannot promote non-numeric value to xs:float", "XPTY0004");
                };
                iterator = new ItemMappingIterator(iterator, promoter, true);
            }
            if (requiredItemType.equals(BuiltInAtomicType.STRING) && this.relationship(suppliedItemType, BuiltInAtomicType.ANY_URI) != Affinity.DISJOINT) {
                ItemMappingFunction promoter = item -> {
                    if (item instanceof AnyURIValue) {
                        return new StringValue(item.getStringValueCS());
                    }
                    return item;
                };
                iterator = new ItemMappingIterator(iterator, promoter, true);
            }
        }
        iterator = this.applyFunctionCoercion(iterator, suppliedItemType, requiredItemType, locator);
        Affinity relation = this.relationship(suppliedItemType, requiredItemType);
        if (relation != Affinity.SAME_TYPE && relation != Affinity.SUBSUMED_BY) {
            ItemTypeCheckingFunction itemChecker = new ItemTypeCheckingFunction(requiredItemType, role, locator, this.config);
            iterator = new ItemMappingIterator(iterator, itemChecker, true);
        }
        if (requiredType.getCardinality() != 57344) {
            iterator = new CardinalityCheckingIterator(iterator, requiredType.getCardinality(), role, locator);
        }
        return SequenceTool.toMemoSequence(iterator);
    }

    protected SequenceIterator applyFunctionCoercion(SequenceIterator iterator, ItemType suppliedItemType, ItemType requiredItemType, Location locator) {
        if (requiredItemType instanceof FunctionItemType && !((FunctionItemType)requiredItemType).isMapType() && !((FunctionItemType)requiredItemType).isArrayType() && this.relationship(requiredItemType, suppliedItemType) != Affinity.SUBSUMES) {
            if (requiredItemType == AnyFunctionType.getInstance()) {
                return iterator;
            }
            FunctionSequenceCoercer.Coercer coercer = new FunctionSequenceCoercer.Coercer((SpecificFunctionType)requiredItemType, this.config, locator);
            return new ItemMappingIterator(iterator, coercer, true);
        }
        return iterator;
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    public boolean isSubType(ItemType subtype, ItemType supertype) {
        Affinity relation = this.relationship(subtype, supertype);
        return relation == Affinity.SAME_TYPE || relation == Affinity.SUBSUMED_BY;
    }

    public Affinity relationship(ItemType t1, ItemType t2) {
        Objects.requireNonNull(t1);
        Objects.requireNonNull(t2);
        t1 = TypeHierarchy.stabilize(t1);
        t2 = TypeHierarchy.stabilize(t2);
        if (t1.equals(t2)) {
            return Affinity.SAME_TYPE;
        }
        if (t2 instanceof AnyItemType) {
            return Affinity.SUBSUMED_BY;
        }
        if (t1 instanceof AnyItemType) {
            return Affinity.SUBSUMES;
        }
        if (t1 instanceof BuiltInAtomicType && t2 instanceof BuiltInAtomicType) {
            if (t1.getBasicAlphaCode().startsWith(t2.getBasicAlphaCode())) {
                return Affinity.SUBSUMED_BY;
            }
            if (t2.getBasicAlphaCode().startsWith(t1.getBasicAlphaCode())) {
                return Affinity.SUBSUMES;
            }
            return Affinity.DISJOINT;
        }
        if (t1 instanceof ErrorType) {
            return Affinity.SUBSUMED_BY;
        }
        if (t2 instanceof ErrorType) {
            return Affinity.SUBSUMES;
        }
        ItemTypePair pair = new ItemTypePair(t1, t2);
        Affinity result = this.map.get(pair);
        if (result == null) {
            result = this.computeRelationship(t1, t2);
            this.map.put(pair, result);
        }
        return result;
    }

    private static ItemType stabilize(ItemType in) {
        if (in instanceof SameNameTest) {
            return ((SameNameTest)in).getEquivalentNameTest();
        }
        return in;
    }

    private Affinity computeRelationship(ItemType t1, ItemType t2) {
        TypeHierarchy.requireTrueItemType(t1);
        TypeHierarchy.requireTrueItemType(t2);
        try {
            if (t1 == t2) {
                return Affinity.SAME_TYPE;
            }
            if (t1 instanceof AnyItemType) {
                if (t2 instanceof AnyItemType) {
                    return Affinity.SAME_TYPE;
                }
                return Affinity.SUBSUMES;
            }
            if (t2 instanceof AnyItemType) {
                return Affinity.SUBSUMED_BY;
            }
            if (t1.isPlainType()) {
                if (t2 instanceof NodeTest || t2 instanceof FunctionItemType || t2 instanceof JavaExternalObjectType) {
                    return Affinity.DISJOINT;
                }
                if (t1 == BuiltInAtomicType.ANY_ATOMIC && t2.isPlainType()) {
                    return Affinity.SUBSUMES;
                }
                if (t2 == BuiltInAtomicType.ANY_ATOMIC) {
                    return Affinity.SUBSUMED_BY;
                }
                if (t1 instanceof AtomicType && t2 instanceof AtomicType) {
                    SchemaType st;
                    if (((AtomicType)t1).getFingerprint() == ((AtomicType)t2).getFingerprint()) {
                        return Affinity.SAME_TYPE;
                    }
                    AtomicType t = (AtomicType)t2;
                    while (true) {
                        if (((AtomicType)t1).getFingerprint() == t.getFingerprint()) {
                            return Affinity.SUBSUMES;
                        }
                        st = t.getBaseType();
                        if (!(st instanceof AtomicType)) break;
                        t = (AtomicType)st;
                    }
                    t = (AtomicType)t1;
                    while (true) {
                        if (t.getFingerprint() == ((AtomicType)t2).getFingerprint()) {
                            return Affinity.SUBSUMED_BY;
                        }
                        st = t.getBaseType();
                        if (!(st instanceof AtomicType)) break;
                        t = (AtomicType)st;
                    }
                    return Affinity.DISJOINT;
                }
                if (!t1.isAtomicType() && t2.isPlainType()) {
                    Set<? extends PlainType> s2;
                    Set<? extends PlainType> s1 = TypeHierarchy.toSet(((PlainType)t1).getPlainMemberTypes());
                    if (!this.unionOverlaps(s1, s2 = TypeHierarchy.toSet(((PlainType)t2).getPlainMemberTypes()))) {
                        return Affinity.DISJOINT;
                    }
                    boolean gt = s1.containsAll(s2);
                    boolean lt = s2.containsAll(s1);
                    if (gt && lt) {
                        return Affinity.SAME_TYPE;
                    }
                    if (gt) {
                        return Affinity.SUBSUMES;
                    }
                    if (lt) {
                        return Affinity.SUBSUMED_BY;
                    }
                    if (this.unionSubsumes(s1, s2)) {
                        return Affinity.SUBSUMES;
                    }
                    if (this.unionSubsumes(s2, s1)) {
                        return Affinity.SUBSUMED_BY;
                    }
                    return Affinity.OVERLAPS;
                }
                if (t1 instanceof AtomicType) {
                    Affinity r = this.relationship(t2, t1);
                    return TypeHierarchy.inverseRelationship(r);
                }
                throw new IllegalStateException();
            }
            if (t1 instanceof NodeTest) {
                IntSet n2;
                IntSet n1;
                UType m2;
                if (t2.isPlainType() || t2 instanceof FunctionItemType) {
                    return Affinity.DISJOINT;
                }
                if (t1 instanceof AnyNodeTest) {
                    if (t2 instanceof AnyNodeTest) {
                        return Affinity.SAME_TYPE;
                    }
                    return Affinity.SUBSUMES;
                }
                if (t2 instanceof AnyNodeTest) {
                    return Affinity.SUBSUMED_BY;
                }
                if (t2 instanceof ErrorType) {
                    return Affinity.DISJOINT;
                }
                UType m1 = t1.getUType();
                if (!m1.overlaps(m2 = t2.getUType())) {
                    return Affinity.DISJOINT;
                }
                Affinity nodeKindRelationship = m1.equals(m2) ? Affinity.SAME_TYPE : (m2.subsumes(m1) ? Affinity.SUBSUMED_BY : (m1.subsumes(m2) ? Affinity.SUBSUMES : Affinity.OVERLAPS));
                Optional<IntSet> on1 = ((NodeTest)t1).getRequiredNodeNames();
                Optional<IntSet> on2 = ((NodeTest)t2).getRequiredNodeNames();
                Affinity nodeNameRelationship = t1 instanceof QNameTest && t2 instanceof QNameTest ? TypeHierarchy.nameTestRelationship((QNameTest)((Object)t1), (QNameTest)((Object)t2)) : (on1.isPresent() && on1.get() instanceof IntUniversalSet ? (on2.isPresent() && on2.get() instanceof IntUniversalSet ? Affinity.SAME_TYPE : Affinity.SUBSUMES) : (on2.isPresent() && on2.get() instanceof IntUniversalSet ? Affinity.SUBSUMED_BY : (!on1.isPresent() || !on2.isPresent() ? (t1.equals(t2) ? Affinity.SAME_TYPE : Affinity.OVERLAPS) : ((n1 = on1.get()).containsAll(n2 = on2.get()) ? (n1.size() == n2.size() ? Affinity.SAME_TYPE : Affinity.SUBSUMES) : (n2.containsAll(n1) ? Affinity.SUBSUMED_BY : (IntHashSet.containsSome(n1, n2) ? Affinity.OVERLAPS : Affinity.DISJOINT))))));
                Affinity contentRelationship = this.computeContentRelationship(t1, t2, on1, on2);
                if (nodeKindRelationship == Affinity.SAME_TYPE && nodeNameRelationship == Affinity.SAME_TYPE && contentRelationship == Affinity.SAME_TYPE) {
                    return Affinity.SAME_TYPE;
                }
                if (!(nodeKindRelationship != Affinity.SAME_TYPE && nodeKindRelationship != Affinity.SUBSUMES || nodeNameRelationship != Affinity.SAME_TYPE && nodeNameRelationship != Affinity.SUBSUMES || contentRelationship != Affinity.SAME_TYPE && contentRelationship != Affinity.SUBSUMES)) {
                    return Affinity.SUBSUMES;
                }
                if (!(nodeKindRelationship != Affinity.SAME_TYPE && nodeKindRelationship != Affinity.SUBSUMED_BY || nodeNameRelationship != Affinity.SAME_TYPE && nodeNameRelationship != Affinity.SUBSUMED_BY || contentRelationship != Affinity.SAME_TYPE && contentRelationship != Affinity.SUBSUMED_BY)) {
                    return Affinity.SUBSUMED_BY;
                }
                if (nodeNameRelationship == Affinity.DISJOINT || contentRelationship == Affinity.DISJOINT) {
                    return Affinity.DISJOINT;
                }
                return Affinity.OVERLAPS;
            }
            if (t1 instanceof AnyExternalObjectType) {
                if (!(t2 instanceof AnyExternalObjectType)) {
                    return Affinity.DISJOINT;
                }
                if (t1 instanceof JavaExternalObjectType) {
                    if (t2 == AnyExternalObjectType.THE_INSTANCE) {
                        return Affinity.SUBSUMED_BY;
                    }
                    if (t2 instanceof JavaExternalObjectType) {
                        return ((JavaExternalObjectType)t1).getRelationship((JavaExternalObjectType)t2);
                    }
                    return Affinity.DISJOINT;
                }
                if (t2 instanceof JavaExternalObjectType) {
                    return Affinity.SUBSUMES;
                }
                return Affinity.DISJOINT;
            }
            if (t1 instanceof MapType && t2 instanceof MapType) {
                Affinity valueRel;
                if (t1 == MapType.EMPTY_MAP_TYPE) {
                    return Affinity.SUBSUMED_BY;
                }
                if (t2 == MapType.EMPTY_MAP_TYPE) {
                    return Affinity.SUBSUMES;
                }
                if (t1 == MapType.ANY_MAP_TYPE) {
                    return Affinity.SUBSUMES;
                }
                if (t2 == MapType.ANY_MAP_TYPE) {
                    return Affinity.SUBSUMED_BY;
                }
                AtomicType k1 = ((MapType)t1).getKeyType();
                AtomicType k2 = ((MapType)t2).getKeyType();
                SequenceType v1 = ((MapType)t1).getValueType();
                SequenceType v2 = ((MapType)t2).getValueType();
                Affinity keyRel = this.relationship(k1, k2);
                Affinity rel = TypeHierarchy.combineRelationships(keyRel, valueRel = this.sequenceTypeRelationship(v1, v2));
                if (rel == Affinity.SAME_TYPE || rel == Affinity.SUBSUMES || rel == Affinity.SUBSUMED_BY) {
                    return rel;
                }
            }
            if (t2 instanceof FunctionItemType) {
                Affinity signatureRelationship = ((FunctionItemType)t1).relationship((FunctionItemType)t2, this);
                if (signatureRelationship == Affinity.DISJOINT) {
                    return Affinity.DISJOINT;
                }
                Affinity assertionRelationship = Affinity.SAME_TYPE;
                AnnotationList first = ((FunctionItemType)t1).getAnnotationAssertions();
                AnnotationList second = ((FunctionItemType)t2).getAnnotationAssertions();
                HashSet<String> namespaces = new HashSet<String>();
                for (Annotation a : first) {
                    namespaces.add(a.getAnnotationQName().getURI());
                }
                for (Annotation a : second) {
                    namespaces.add(a.getAnnotationQName().getURI());
                }
                for (String ns : namespaces) {
                    FunctionAnnotationHandler handler = this.config.getFunctionAnnotationHandler(ns);
                    if (handler == null) continue;
                    Affinity localRel = Affinity.SAME_TYPE;
                    AnnotationList firstFiltered = first.filterByNamespace(ns);
                    AnnotationList secondFiltered = second.filterByNamespace(ns);
                    if (firstFiltered.isEmpty()) {
                        if (!secondFiltered.isEmpty()) {
                            localRel = Affinity.SUBSUMES;
                        }
                    } else {
                        localRel = secondFiltered.isEmpty() ? Affinity.SUBSUMED_BY : handler.relationship(firstFiltered, secondFiltered);
                    }
                    assertionRelationship = TypeHierarchy.combineRelationships(assertionRelationship, localRel);
                }
                return TypeHierarchy.combineRelationships(signatureRelationship, assertionRelationship);
            }
            return Affinity.DISJOINT;
        } catch (MissingComponentException e) {
            return Affinity.OVERLAPS;
        }
    }

    private static void requireTrueItemType(ItemType t) {
        Objects.requireNonNull(t);
        if (!t.isTrueItemType()) {
            throw new AssertionError((Object)(t + " is a non-pure union type"));
        }
    }

    private static Affinity nameTestRelationship(QNameTest t1, QNameTest t2) {
        if (t1.equals(t2)) {
            return Affinity.SAME_TYPE;
        }
        if (t2 instanceof NameTest) {
            return t1.matches(((NameTest)t2).getMatchingNodeName()) ? Affinity.SUBSUMES : Affinity.DISJOINT;
        }
        if (t1 instanceof NameTest) {
            return t2.matches(((NameTest)t1).getMatchingNodeName()) ? Affinity.SUBSUMED_BY : Affinity.DISJOINT;
        }
        if (t2 instanceof SameNameTest) {
            return t1.matches(((SameNameTest)t2).getMatchingNodeName()) ? Affinity.SUBSUMES : Affinity.DISJOINT;
        }
        if (t1 instanceof SameNameTest) {
            return t2.matches(((SameNameTest)t1).getMatchingNodeName()) ? Affinity.SUBSUMED_BY : Affinity.DISJOINT;
        }
        if (t1 instanceof NamespaceTest && t2 instanceof NamespaceTest) {
            return Affinity.DISJOINT;
        }
        if (t1 instanceof LocalNameTest && t2 instanceof LocalNameTest) {
            return Affinity.DISJOINT;
        }
        return Affinity.OVERLAPS;
    }

    private static Affinity combineRelationships(Affinity rel1, Affinity rel2) {
        if (rel1 == Affinity.SAME_TYPE && rel2 == Affinity.SAME_TYPE) {
            return Affinity.SAME_TYPE;
        }
        if (!(rel1 != Affinity.SAME_TYPE && rel1 != Affinity.SUBSUMES || rel2 != Affinity.SAME_TYPE && rel2 != Affinity.SUBSUMES)) {
            return Affinity.SUBSUMES;
        }
        if (!(rel1 != Affinity.SAME_TYPE && rel1 != Affinity.SUBSUMED_BY || rel2 != Affinity.SAME_TYPE && rel2 != Affinity.SUBSUMED_BY)) {
            return Affinity.SUBSUMED_BY;
        }
        if (rel1 == Affinity.DISJOINT || rel2 == Affinity.DISJOINT) {
            return Affinity.DISJOINT;
        }
        return Affinity.OVERLAPS;
    }

    private static <X> Set<X> toSet(Iterable<X> in) {
        HashSet<X> s = new HashSet<X>();
        for (X x : in) {
            s.add(x);
        }
        return s;
    }

    private boolean unionSubsumes(Set<? extends PlainType> s1, Set<? extends PlainType> s2) {
        for (PlainType plainType : s2) {
            boolean t2isSubsumed = false;
            for (PlainType plainType2 : s1) {
                Affinity rel = this.relationship(plainType2, plainType);
                if (rel != Affinity.SUBSUMES && rel != Affinity.SAME_TYPE) continue;
                t2isSubsumed = true;
                break;
            }
            if (t2isSubsumed) continue;
            return false;
        }
        return true;
    }

    private boolean unionOverlaps(Set<? extends PlainType> s1, Set<? extends PlainType> s2) {
        for (PlainType plainType : s2) {
            for (PlainType plainType2 : s1) {
                Affinity rel = this.relationship(plainType2, plainType);
                if (rel == Affinity.DISJOINT) continue;
                return true;
            }
        }
        return false;
    }

    protected Affinity computeContentRelationship(ItemType t1, ItemType t2, Optional<IntSet> n1, Optional<IntSet> n2) {
        Affinity contentRelationship;
        if (t1 instanceof DocumentNodeTest) {
            contentRelationship = t2 instanceof DocumentNodeTest ? this.relationship(((DocumentNodeTest)t1).getElementTest(), ((DocumentNodeTest)t2).getElementTest()) : Affinity.SUBSUMED_BY;
        } else if (t2 instanceof DocumentNodeTest) {
            contentRelationship = Affinity.SUBSUMES;
        } else {
            SchemaType s1 = ((NodeTest)t1).getContentType();
            SchemaType s2 = ((NodeTest)t2).getContentType();
            contentRelationship = this.schemaTypeRelationship(s1, s2);
        }
        boolean nillable1 = ((NodeTest)t1).isNillable();
        boolean nillable2 = ((NodeTest)t2).isNillable();
        if (nillable1 != nillable2) {
            switch (contentRelationship) {
                case SUBSUMES: {
                    if (nillable2) {
                        contentRelationship = Affinity.OVERLAPS;
                        break;
                    }
                }
                case SUBSUMED_BY: {
                    if (nillable1) {
                        contentRelationship = Affinity.OVERLAPS;
                        break;
                    }
                }
                case SAME_TYPE: {
                    if (nillable1) {
                        contentRelationship = Affinity.SUBSUMES;
                        break;
                    }
                    contentRelationship = Affinity.SUBSUMED_BY;
                    break;
                }
            }
        }
        return contentRelationship;
    }

    public Affinity sequenceTypeRelationship(SequenceType s1, SequenceType s2) {
        Affinity cardRel;
        int c2;
        int c1 = s1.getCardinality();
        if (c1 == (c2 = s2.getCardinality())) {
            cardRel = Affinity.SAME_TYPE;
        } else if (Cardinality.subsumes(c1, c2)) {
            cardRel = Affinity.SUBSUMES;
        } else if (Cardinality.subsumes(c2, c1)) {
            cardRel = Affinity.SUBSUMED_BY;
        } else {
            if (c1 == 8192 && !Cardinality.allowsZero(c2)) {
                return Affinity.DISJOINT;
            }
            if (c2 == 8192 && !Cardinality.allowsZero(c1)) {
                return Affinity.DISJOINT;
            }
            cardRel = Affinity.OVERLAPS;
        }
        Affinity itemRel = this.relationship(s1.getPrimaryType(), s2.getPrimaryType());
        if (itemRel == Affinity.DISJOINT) {
            return Affinity.DISJOINT;
        }
        if (cardRel == Affinity.SAME_TYPE || cardRel == itemRel) {
            return itemRel;
        }
        if (itemRel == Affinity.SAME_TYPE) {
            return cardRel;
        }
        return Affinity.OVERLAPS;
    }

    public Affinity schemaTypeRelationship(SchemaType s1, SchemaType s2) {
        if (s1.isSameType(s2)) {
            return Affinity.SAME_TYPE;
        }
        if (s1 instanceof AnyType) {
            return Affinity.SUBSUMES;
        }
        if (s2 instanceof AnyType) {
            return Affinity.SUBSUMED_BY;
        }
        if (s1 instanceof Untyped && (s2 == BuiltInAtomicType.ANY_ATOMIC || s2 == BuiltInAtomicType.UNTYPED_ATOMIC)) {
            return Affinity.OVERLAPS;
        }
        if (s2 instanceof Untyped && (s1 == BuiltInAtomicType.ANY_ATOMIC || s1 == BuiltInAtomicType.UNTYPED_ATOMIC)) {
            return Affinity.OVERLAPS;
        }
        if (s1 instanceof PlainType && ((PlainType)((Object)s1)).isPlainType() && s2 instanceof PlainType && ((PlainType)((Object)s2)).isPlainType()) {
            return this.relationship((ItemType)((Object)s1), (ItemType)((Object)s2));
        }
        SchemaType t1 = s1;
        while ((t1 = t1.getBaseType()) != null) {
            if (!t1.isSameType(s2)) continue;
            return Affinity.SUBSUMED_BY;
        }
        SchemaType t2 = s2;
        while ((t2 = t2.getBaseType()) != null) {
            if (!t2.isSameType(s1)) continue;
            return Affinity.SUBSUMES;
        }
        return Affinity.DISJOINT;
    }

    public static Affinity inverseRelationship(Affinity relation) {
        switch (relation) {
            case SAME_TYPE: {
                return Affinity.SAME_TYPE;
            }
            case SUBSUMES: {
                return Affinity.SUBSUMED_BY;
            }
            case SUBSUMED_BY: {
                return Affinity.SUBSUMES;
            }
            case OVERLAPS: {
                return Affinity.OVERLAPS;
            }
            case DISJOINT: {
                return Affinity.DISJOINT;
            }
        }
        throw new IllegalArgumentException();
    }

    public ItemType getGenericFunctionItemType() {
        return AnyItemType.getInstance();
    }

    private static class ItemTypePair {
        ItemType s;
        ItemType t;

        public ItemTypePair(ItemType s, ItemType t) {
            this.s = s;
            this.t = t;
        }

        public int hashCode() {
            return this.s.hashCode() ^ this.t.hashCode();
        }

        public boolean equals(Object obj) {
            if (obj instanceof ItemTypePair) {
                ItemTypePair pair = (ItemTypePair)obj;
                return this.s.equals(pair.s) && this.t.equals(pair.t);
            }
            return false;
        }
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import net.sf.saxon.om.Genre;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.tree.tiny.NodeVectorTree;
import net.sf.saxon.type.AnySimpleType;
import net.sf.saxon.type.AnyType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.PrimitiveUType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.z.IntSet;
import net.sf.saxon.z.IntUniversalSet;

public abstract class NodeTest
implements Predicate<NodeInfo>,
ItemType.WithSequenceTypeCache {
    private SequenceType _one;
    private SequenceType _oneOrMore;
    private SequenceType _zeroOrOne;
    private SequenceType _zeroOrMore;

    @Override
    public Genre getGenre() {
        return Genre.NODE;
    }

    @Override
    public abstract double getDefaultPriority();

    @Override
    public boolean matches(Item item, TypeHierarchy th) {
        return item instanceof NodeInfo && this.test((NodeInfo)item);
    }

    @Override
    public ItemType getPrimitiveItemType() {
        int p = this.getPrimitiveType();
        if (p == 0) {
            return AnyNodeTest.getInstance();
        }
        return NodeKindTest.makeNodeKindTest(p);
    }

    @Override
    public int getPrimitiveType() {
        return 0;
    }

    public int getFingerprint() {
        return -1;
    }

    public StructuredQName getMatchingNodeName() {
        return null;
    }

    @Override
    public String getBasicAlphaCode() {
        switch (this.getPrimitiveType()) {
            case 0: {
                return "N";
            }
            case 1: {
                return "NE";
            }
            case 2: {
                return "NA";
            }
            case 3: {
                return "NT";
            }
            case 8: {
                return "NC";
            }
            case 7: {
                return "NP";
            }
            case 9: {
                return "ND";
            }
            case 13: {
                return "NN";
            }
        }
        return "*";
    }

    @Override
    public boolean isAtomicType() {
        return false;
    }

    @Override
    public boolean isPlainType() {
        return false;
    }

    @Override
    public AtomicType getAtomizedItemType() {
        return BuiltInAtomicType.ANY_ATOMIC;
    }

    @Override
    public boolean isAtomizable(TypeHierarchy th) {
        return true;
    }

    public IntPredicate getMatcher(NodeVectorTree tree) {
        return nodeNr -> this.test(tree.getNode(nodeNr));
    }

    public abstract boolean matches(int var1, NodeName var2, SchemaType var3);

    @Override
    public boolean test(NodeInfo node) {
        return this.matches(node.getNodeKind(), NameOfNode.makeName(node), node.getSchemaType());
    }

    public SchemaType getContentType() {
        Set<PrimitiveUType> m = this.getUType().decompose();
        Iterator<PrimitiveUType> it = m.iterator();
        if (m.size() == 1 && it.hasNext()) {
            PrimitiveUType p = it.next();
            switch (p) {
                case DOCUMENT: {
                    return AnyType.getInstance();
                }
                case ELEMENT: {
                    return AnyType.getInstance();
                }
                case ATTRIBUTE: {
                    return AnySimpleType.getInstance();
                }
                case COMMENT: {
                    return BuiltInAtomicType.STRING;
                }
                case TEXT: {
                    return BuiltInAtomicType.UNTYPED_ATOMIC;
                }
                case PI: {
                    return BuiltInAtomicType.STRING;
                }
                case NAMESPACE: {
                    return BuiltInAtomicType.STRING;
                }
            }
        }
        return AnyType.getInstance();
    }

    public Optional<IntSet> getRequiredNodeNames() {
        return Optional.of(IntUniversalSet.getInstance());
    }

    public boolean isNillable() {
        return true;
    }

    public NodeTest copy() {
        return this;
    }

    @Override
    public SequenceType one() {
        if (this._one == null) {
            this._one = new SequenceType(this, 16384);
        }
        return this._one;
    }

    @Override
    public SequenceType zeroOrOne() {
        if (this._zeroOrOne == null) {
            this._zeroOrOne = new SequenceType(this, 24576);
        }
        return this._zeroOrOne;
    }

    @Override
    public SequenceType oneOrMore() {
        if (this._oneOrMore == null) {
            this._oneOrMore = new SequenceType(this, 49152);
        }
        return this._oneOrMore;
    }

    @Override
    public SequenceType zeroOrMore() {
        if (this._zeroOrMore == null) {
            this._zeroOrMore = new SequenceType(this, 57344);
        }
        return this._zeroOrMore;
    }

    @Override
    public Optional<String> explainMismatch(Item item, TypeHierarchy th) {
        if (item instanceof NodeInfo) {
            UType actualKind = UType.getUType(item);
            if (!this.getUType().overlaps(actualKind)) {
                return Optional.of("The supplied value is " + actualKind.toStringWithIndefiniteArticle());
            }
            return Optional.empty();
        }
        return Optional.of("The supplied value is " + item.getGenre().getDescription());
    }

    public String toShortString() {
        return this.toString();
    }
}


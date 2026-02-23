/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import java.util.Optional;
import java.util.function.IntPredicate;
import net.sf.saxon.Configuration;
import net.sf.saxon.functions.Nilled_1;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.tree.tiny.NodeVectorTree;
import net.sf.saxon.tree.tiny.TinyTree;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ComplexType;
import net.sf.saxon.type.ListType;
import net.sf.saxon.type.MissingComponentException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.type.Untyped;

public class ContentTypeTest
extends NodeTest {
    private int kind;
    private SchemaType schemaType;
    private Configuration config;
    private boolean nillable = false;

    public ContentTypeTest(int nodeKind, SchemaType schemaType, Configuration config, boolean nillable) {
        this.kind = nodeKind;
        this.schemaType = schemaType;
        this.config = config;
        this.nillable = nillable;
    }

    @Override
    public UType getUType() {
        return this.kind == 1 ? UType.ELEMENT : UType.ATTRIBUTE;
    }

    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    @Override
    public boolean isNillable() {
        return this.nillable;
    }

    public SchemaType getSchemaType() {
        return this.schemaType;
    }

    public int getNodeKind() {
        return this.kind;
    }

    @Override
    public boolean matches(int nodeKind, NodeName name, SchemaType annotation) {
        return this.kind == nodeKind && this.matchesAnnotation(annotation);
    }

    @Override
    public IntPredicate getMatcher(NodeVectorTree tree) {
        byte[] nodeKindArray = tree.getNodeKindArray();
        return nodeNr -> (nodeKindArray[nodeNr] & 0xF) == this.kind && this.matchesAnnotation(((TinyTree)tree).getSchemaType(nodeNr)) && (this.nillable || !((TinyTree)tree).isNilled(nodeNr));
    }

    @Override
    public boolean test(NodeInfo node) {
        return node.getNodeKind() == this.kind && this.matchesAnnotation(node.getSchemaType()) && (this.nillable || !Nilled_1.isNilled(node));
    }

    private boolean matchesAnnotation(SchemaType annotation) {
        if (annotation == null) {
            return false;
        }
        if (this.schemaType == AnyType.getInstance()) {
            return true;
        }
        if (annotation.equals(this.schemaType)) {
            return true;
        }
        Affinity r = this.config.getTypeHierarchy().schemaTypeRelationship(annotation, this.schemaType);
        return r == Affinity.SAME_TYPE || r == Affinity.SUBSUMED_BY;
    }

    @Override
    public final double getDefaultPriority() {
        return 0.0;
    }

    @Override
    public int getPrimitiveType() {
        return this.kind;
    }

    @Override
    public SchemaType getContentType() {
        return this.schemaType;
    }

    @Override
    public AtomicType getAtomizedItemType() {
        SchemaType type = this.schemaType;
        try {
            if (type.isAtomicType()) {
                return (AtomicType)type;
            }
            if (type instanceof ListType) {
                SimpleType mem = ((ListType)type).getItemType();
                if (mem.isAtomicType()) {
                    return (AtomicType)mem;
                }
            } else if (type instanceof ComplexType && ((ComplexType)type).isSimpleContent()) {
                SimpleType mem;
                SimpleType ctype = ((ComplexType)type).getSimpleContentType();
                assert (ctype != null);
                if (ctype.isAtomicType()) {
                    return (AtomicType)ctype;
                }
                if (ctype instanceof ListType && (mem = ((ListType)ctype).getItemType()).isAtomicType()) {
                    return (AtomicType)mem;
                }
            }
        } catch (MissingComponentException e) {
            return BuiltInAtomicType.ANY_ATOMIC;
        }
        return BuiltInAtomicType.ANY_ATOMIC;
    }

    @Override
    public boolean isAtomizable(TypeHierarchy th) {
        return !this.schemaType.isComplexType() || ((ComplexType)this.schemaType).getVariety() != 2;
    }

    @Override
    public String toString() {
        return (this.kind == 1 ? "element(*, " : "attribute(*, ") + this.schemaType.getEQName() + ')';
    }

    @Override
    public String toExportString() {
        return (this.kind == 1 ? "element(*, " : "attribute(*, ") + this.schemaType.getNearestNamedType().getEQName() + ')';
    }

    public int hashCode() {
        return this.kind << 20 ^ this.schemaType.hashCode();
    }

    public boolean equals(Object other) {
        return other instanceof ContentTypeTest && ((ContentTypeTest)other).kind == this.kind && ((ContentTypeTest)other).schemaType == this.schemaType && ((ContentTypeTest)other).nillable == this.nillable;
    }

    @Override
    public Optional<String> explainMismatch(Item item, TypeHierarchy th) {
        Optional<String> explanation = super.explainMismatch(item, th);
        if (explanation.isPresent()) {
            return explanation;
        }
        NodeInfo node = (NodeInfo)item;
        if (!this.matchesAnnotation(((NodeInfo)item).getSchemaType())) {
            if (node.getSchemaType() == Untyped.getInstance()) {
                return Optional.of("The supplied node has not been schema-validated");
            }
            if (node.getSchemaType() == BuiltInAtomicType.UNTYPED_ATOMIC) {
                return Optional.of("The supplied node has not been schema-validated");
            }
            return Optional.of("The supplied node has the wrong type annotation (" + node.getSchemaType().getDescription() + ")");
        }
        if (Nilled_1.isNilled(node) && !this.nillable) {
            return Optional.of("The supplied node has xsi:nil='true', which the required type does not allow");
        }
        return Optional.empty();
    }
}


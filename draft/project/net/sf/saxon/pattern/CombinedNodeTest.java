/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import java.util.Optional;
import java.util.function.IntPredicate;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.ma.map.DictionaryMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.ContentTypeTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.tree.tiny.NodeVectorTree;
import net.sf.saxon.type.AnySimpleType;
import net.sf.saxon.type.AnyType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.z.IntExceptPredicate;
import net.sf.saxon.z.IntSet;

public class CombinedNodeTest
extends NodeTest {
    private NodeTest nodetest1;
    private NodeTest nodetest2;
    private int operator;

    public CombinedNodeTest(NodeTest nt1, int operator, NodeTest nt2) {
        this.nodetest1 = nt1;
        this.operator = operator;
        this.nodetest2 = nt2;
    }

    @Override
    public UType getUType() {
        UType u1 = this.nodetest1.getUType();
        UType u2 = this.nodetest2.getUType();
        switch (this.operator) {
            case 1: {
                return u1.union(u2);
            }
            case 23: {
                return u1.intersection(u2);
            }
            case 24: {
                return u1;
            }
        }
        throw new IllegalArgumentException("Unknown operator in Combined Node Test");
    }

    @Override
    public boolean matches(int nodeKind, NodeName name, SchemaType annotation) {
        switch (this.operator) {
            case 1: {
                return this.nodetest1 == null || this.nodetest2 == null || this.nodetest1.matches(nodeKind, name, annotation) || this.nodetest2.matches(nodeKind, name, annotation);
            }
            case 23: {
                return !(this.nodetest1 != null && !this.nodetest1.matches(nodeKind, name, annotation) || this.nodetest2 != null && !this.nodetest2.matches(nodeKind, name, annotation));
            }
            case 24: {
                return (this.nodetest1 == null || this.nodetest1.matches(nodeKind, name, annotation)) && this.nodetest2 != null && !this.nodetest2.matches(nodeKind, name, annotation);
            }
        }
        throw new IllegalArgumentException("Unknown operator in Combined Node Test");
    }

    @Override
    public IntPredicate getMatcher(NodeVectorTree tree) {
        switch (this.operator) {
            case 1: {
                return this.nodetest1.getMatcher(tree).or(this.nodetest2.getMatcher(tree));
            }
            case 23: {
                return this.nodetest1.getMatcher(tree).and(this.nodetest2.getMatcher(tree));
            }
            case 24: {
                return new IntExceptPredicate(this.nodetest1.getMatcher(tree), this.nodetest2.getMatcher(tree));
            }
        }
        throw new IllegalArgumentException("Unknown operator in Combined Node Test");
    }

    @Override
    public boolean test(NodeInfo node) {
        switch (this.operator) {
            case 1: {
                return this.nodetest1 == null || this.nodetest2 == null || this.nodetest1.test(node) || this.nodetest2.test(node);
            }
            case 23: {
                return !(this.nodetest1 != null && !this.nodetest1.test(node) || this.nodetest2 != null && !this.nodetest2.test(node));
            }
            case 24: {
                return (this.nodetest1 == null || this.nodetest1.test(node)) && this.nodetest2 != null && !this.nodetest2.test(node);
            }
        }
        throw new IllegalArgumentException("Unknown operator in Combined Node Test");
    }

    @Override
    public String toString() {
        return this.makeString(false);
    }

    private String makeString(boolean forExport) {
        if (this.nodetest1 instanceof NameTest && this.operator == 23) {
            int kind = this.nodetest1.getPrimitiveType();
            String skind = kind == 1 ? "element(" : "attribute(";
            String content = "";
            if (this.nodetest2 instanceof ContentTypeTest) {
                SchemaType schemaType = ((ContentTypeTest)this.nodetest2).getSchemaType();
                if (forExport) {
                    schemaType = schemaType.getNearestNamedType();
                }
                content = ", " + schemaType.getEQName();
                if (this.nodetest2.isNillable()) {
                    content = content + "?";
                }
            }
            String name = this.nodetest1.getMatchingNodeName().getEQName();
            return skind + name + content + ')';
        }
        String nt1 = this.nodetest1 == null ? "item()" : this.nodetest1.toString();
        String nt2 = this.nodetest2 == null ? "item()" : this.nodetest2.toString();
        return '(' + nt1 + ' ' + Token.tokens[this.operator] + ' ' + nt2 + ')';
    }

    @Override
    public String toExportString() {
        return this.makeString(true);
    }

    public String getContentTypeForAlphaCode() {
        if (this.nodetest1 instanceof NameTest && this.operator == 23 && this.nodetest2 instanceof ContentTypeTest) {
            return CombinedNodeTest.getContentTypeForAlphaCode((NameTest)this.nodetest1, (ContentTypeTest)this.nodetest2);
        }
        if (this.nodetest2 instanceof NameTest && this.operator == 23 && this.nodetest1 instanceof ContentTypeTest) {
            return CombinedNodeTest.getContentTypeForAlphaCode((NameTest)this.nodetest2, (ContentTypeTest)this.nodetest1);
        }
        return null;
    }

    private static String getContentTypeForAlphaCode(NameTest nodetest1, ContentTypeTest nodetest2) {
        if (nodetest1.getNodeKind() == 1) {
            if (nodetest2.getContentType() == Untyped.getInstance() && nodetest2.isNillable()) {
                return null;
            }
            SchemaType contentType = nodetest2.getContentType();
            return contentType.getEQName();
        }
        if (nodetest1.getNodeKind() == 2) {
            if (nodetest2.getContentType() == BuiltInAtomicType.UNTYPED_ATOMIC) {
                return null;
            }
            SchemaType contentType = nodetest2.getContentType();
            return contentType.getEQName();
        }
        throw new IllegalStateException();
    }

    public void addTypeDetails(DictionaryMap map) {
        if (this.nodetest1 instanceof NameTest && this.operator == 23) {
            SchemaType schemaType;
            map.initialPut("n", new StringValue(this.nodetest1.getMatchingNodeName().getEQName()));
            if (this.nodetest2 instanceof ContentTypeTest && (schemaType = ((ContentTypeTest)this.nodetest2).getSchemaType()) != Untyped.getInstance() && schemaType != BuiltInAtomicType.UNTYPED_ATOMIC) {
                map.initialPut("c", new StringValue(schemaType.getEQName() + (this.nodetest2.isNillable() ? "?" : "")));
            }
        }
    }

    @Override
    public int getPrimitiveType() {
        UType mask = this.getUType();
        if (mask.equals(UType.ELEMENT)) {
            return 1;
        }
        if (mask.equals(UType.ATTRIBUTE)) {
            return 2;
        }
        if (mask.equals(UType.DOCUMENT)) {
            return 9;
        }
        return 0;
    }

    @Override
    public Optional<IntSet> getRequiredNodeNames() {
        Optional<IntSet> os1 = this.nodetest1.getRequiredNodeNames();
        Optional<IntSet> os2 = this.nodetest2.getRequiredNodeNames();
        if (os1.isPresent() && os2.isPresent()) {
            IntSet s1 = os1.get();
            IntSet s2 = os2.get();
            switch (this.operator) {
                case 1: {
                    return Optional.of(s1.union(s2));
                }
                case 23: {
                    return Optional.of(s1.intersect(s2));
                }
                case 24: {
                    return Optional.of(s1.except(s2));
                }
            }
            throw new IllegalStateException();
        }
        return Optional.empty();
    }

    @Override
    public SchemaType getContentType() {
        SchemaType type2;
        SchemaType type1 = this.nodetest1.getContentType();
        if (type1.isSameType(type2 = this.nodetest2.getContentType())) {
            return type1;
        }
        if (this.operator == 23) {
            if (type2 instanceof AnyType || type2 instanceof AnySimpleType && type1.isSimpleType()) {
                return type1;
            }
            if (type1 instanceof AnyType || type1 instanceof AnySimpleType && type2.isSimpleType()) {
                return type2;
            }
        }
        return AnyType.getInstance();
    }

    @Override
    public AtomicType getAtomizedItemType() {
        AtomicType type2;
        AtomicType type1 = this.nodetest1.getAtomizedItemType();
        if (type1.isSameType(type2 = this.nodetest2.getAtomizedItemType())) {
            return type1;
        }
        if (this.operator == 23) {
            if (type2.equals(BuiltInAtomicType.ANY_ATOMIC)) {
                return type1;
            }
            if (type1.equals(BuiltInAtomicType.ANY_ATOMIC)) {
                return type2;
            }
        }
        return BuiltInAtomicType.ANY_ATOMIC;
    }

    @Override
    public boolean isAtomizable(TypeHierarchy th) {
        switch (this.operator) {
            case 1: {
                return this.nodetest1.isAtomizable(th) || this.nodetest2.isAtomizable(th);
            }
            case 23: {
                return this.nodetest1.isAtomizable(th) && this.nodetest2.isAtomizable(th);
            }
            case 24: {
                return this.nodetest1.isAtomizable(th);
            }
        }
        return true;
    }

    @Override
    public int getFingerprint() {
        int fp2;
        int fp1 = this.nodetest1.getFingerprint();
        if (fp1 == (fp2 = this.nodetest2.getFingerprint())) {
            return fp1;
        }
        if (fp2 == -1 && this.operator == 23) {
            return fp1;
        }
        if (fp1 == -1 && this.operator == 23) {
            return fp2;
        }
        return -1;
    }

    @Override
    public StructuredQName getMatchingNodeName() {
        StructuredQName n1 = this.nodetest1.getMatchingNodeName();
        StructuredQName n2 = this.nodetest2.getMatchingNodeName();
        if (n1 != null && n1.equals(n2)) {
            return n1;
        }
        if (n1 == null && this.operator == 23) {
            return n2;
        }
        if (n2 == null && this.operator == 23) {
            return n1;
        }
        return null;
    }

    @Override
    public boolean isNillable() {
        return this.nodetest1.isNillable() && this.nodetest2.isNillable();
    }

    public int hashCode() {
        return this.nodetest1.hashCode() ^ this.nodetest2.hashCode();
    }

    public boolean equals(Object other) {
        return other instanceof CombinedNodeTest && ((CombinedNodeTest)other).nodetest1.equals(this.nodetest1) && ((CombinedNodeTest)other).nodetest2.equals(this.nodetest2) && ((CombinedNodeTest)other).operator == this.operator;
    }

    @Override
    public double getDefaultPriority() {
        if (this.operator == 1) {
            return this.nodetest1.getDefaultPriority();
        }
        return this.nodetest1 instanceof NameTest ? 0.25 : 0.125;
    }

    public NodeTest[] getComponentNodeTests() {
        return new NodeTest[]{this.nodetest1, this.nodetest2};
    }

    public int getOperator() {
        return this.operator;
    }

    public NodeTest getOperand(int which) {
        return which == 0 ? this.nodetest1 : this.nodetest2;
    }

    @Override
    public Optional<String> explainMismatch(Item item, TypeHierarchy th) {
        Optional<String> explanation = super.explainMismatch(item, th);
        if (explanation.isPresent()) {
            return explanation;
        }
        if (this.operator == 23) {
            if (!this.nodetest1.test((NodeInfo)item)) {
                return this.nodetest1.explainMismatch(item, th);
            }
            if (!this.nodetest2.test((NodeInfo)item)) {
                return this.nodetest2.explainMismatch(item, th);
            }
        }
        return Optional.empty();
    }
}


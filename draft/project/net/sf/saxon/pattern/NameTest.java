/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import java.util.Optional;
import java.util.function.IntPredicate;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.QNameTest;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.tree.tiny.NodeVectorTree;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.z.IntSet;
import net.sf.saxon.z.IntSingletonSet;

public class NameTest
extends NodeTest
implements QNameTest {
    private int nodeKind;
    private int fingerprint;
    private UType uType;
    private NamePool namePool;
    private String uri = null;
    private String localName = null;

    public NameTest(int nodeKind, String uri, String localName, NamePool namePool) {
        this.uri = uri;
        this.localName = localName;
        this.nodeKind = nodeKind;
        this.fingerprint = namePool.allocateFingerprint(uri, localName) & 0xFFFFF;
        this.namePool = namePool;
        this.uType = UType.fromTypeCode(nodeKind);
    }

    public NameTest(int nodeKind, int nameCode, NamePool namePool) {
        this.nodeKind = nodeKind;
        this.fingerprint = nameCode & 0xFFFFF;
        this.namePool = namePool;
        this.uType = UType.fromTypeCode(nodeKind);
    }

    public NameTest(int nodeKind, NodeName name, NamePool pool) {
        this.uri = name.getURI();
        this.localName = name.getLocalPart();
        this.nodeKind = nodeKind;
        this.fingerprint = name.obtainFingerprint(pool);
        this.namePool = pool;
        this.uType = UType.fromTypeCode(nodeKind);
    }

    public NamePool getNamePool() {
        return this.namePool;
    }

    public int getNodeKind() {
        return this.nodeKind;
    }

    @Override
    public UType getUType() {
        return this.uType;
    }

    @Override
    public boolean matches(int nodeKind, NodeName name, SchemaType annotation) {
        if (nodeKind != this.nodeKind) {
            return false;
        }
        if (name.hasFingerprint()) {
            return name.getFingerprint() == this.fingerprint;
        }
        this.computeUriAndLocal();
        return name.hasURI(this.uri) && name.getLocalPart().equals(this.localName);
    }

    @Override
    public IntPredicate getMatcher(NodeVectorTree tree) {
        byte[] nodeKindArray = tree.getNodeKindArray();
        int[] nameCodeArray = tree.getNameCodeArray();
        return nodeNr -> (nameCodeArray[nodeNr] & 0xFFFFF) == this.fingerprint && (nodeKindArray[nodeNr] & 0xF) == this.nodeKind;
    }

    @Override
    public boolean test(NodeInfo node) {
        if (node.getNodeKind() != this.nodeKind) {
            return false;
        }
        if (node.hasFingerprint()) {
            return node.getFingerprint() == this.fingerprint;
        }
        this.computeUriAndLocal();
        return this.localName.equals(node.getLocalPart()) && this.uri.equals(node.getURI());
    }

    private void computeUriAndLocal() {
        if (this.uri == null || this.localName == null) {
            StructuredQName name = this.namePool.getUnprefixedQName(this.fingerprint);
            this.uri = name.getURI();
            this.localName = name.getLocalPart();
        }
    }

    @Override
    public boolean matches(StructuredQName qname) {
        this.computeUriAndLocal();
        return qname.getLocalPart().equals(this.localName) && qname.hasURI(this.uri);
    }

    @Override
    public final double getDefaultPriority() {
        return 0.0;
    }

    @Override
    public int getFingerprint() {
        return this.fingerprint;
    }

    @Override
    public StructuredQName getMatchingNodeName() {
        this.computeUriAndLocal();
        return new StructuredQName("", this.uri, this.localName);
    }

    @Override
    public int getPrimitiveType() {
        return this.nodeKind;
    }

    @Override
    public Optional<IntSet> getRequiredNodeNames() {
        return Optional.of(new IntSingletonSet(this.fingerprint));
    }

    public String getNamespaceURI() {
        this.computeUriAndLocal();
        return this.uri;
    }

    public String getLocalPart() {
        this.computeUriAndLocal();
        return this.localName;
    }

    @Override
    public String toString() {
        switch (this.nodeKind) {
            case 1: {
                return "element(" + this.namePool.getEQName(this.fingerprint) + ")";
            }
            case 2: {
                return "attribute(" + this.namePool.getEQName(this.fingerprint) + ")";
            }
            case 7: {
                return "processing-instruction(" + this.namePool.getLocalName(this.fingerprint) + ')';
            }
            case 13: {
                return "namespace-node(" + this.namePool.getLocalName(this.fingerprint) + ')';
            }
        }
        return this.namePool.getEQName(this.fingerprint);
    }

    public int hashCode() {
        return this.nodeKind << 20 ^ this.fingerprint;
    }

    public boolean equals(Object other) {
        return other instanceof NameTest && ((NameTest)other).namePool == this.namePool && ((NameTest)other).nodeKind == this.nodeKind && ((NameTest)other).fingerprint == this.fingerprint;
    }

    @Override
    public String getFullAlphaCode() {
        return this.getBasicAlphaCode() + " n" + this.getMatchingNodeName().getEQName();
    }

    @Override
    public String exportQNameTest() {
        return this.getMatchingNodeName().getEQName();
    }

    @Override
    public String generateJavaScriptNameTest(int targetVersion) {
        this.computeUriAndLocal();
        return "q.uri==='" + ExpressionPresenter.jsEscape(this.uri) + "'&&q.local==='" + this.localName + "'";
    }

    @Override
    public Optional<String> explainMismatch(Item item, TypeHierarchy th) {
        Optional<String> explanation = super.explainMismatch(item, th);
        if (explanation.isPresent()) {
            return explanation;
        }
        return Optional.of("The node has the wrong name");
    }

    @Override
    public String toShortString() {
        switch (this.nodeKind) {
            case 1: {
                return this.getNamespaceURI().isEmpty() ? this.namePool.getLocalName(this.getFingerprint()) : this.toString();
            }
            case 2: {
                return "@" + (this.getNamespaceURI().isEmpty() ? this.namePool.getLocalName(this.getFingerprint()) : this.toString());
            }
        }
        return this.toString();
    }
}


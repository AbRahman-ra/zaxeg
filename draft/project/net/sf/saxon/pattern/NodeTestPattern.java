/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AlphaCode;
import net.sf.saxon.type.SchemaDeclaration;
import net.sf.saxon.type.UType;

public class NodeTestPattern
extends Pattern {
    private NodeTest nodeTest;

    public NodeTestPattern(NodeTest test) {
        this.nodeTest = test;
        this.setPriority(test.getDefaultPriority());
    }

    @Override
    public boolean matches(Item item, XPathContext context) {
        return item instanceof NodeInfo && this.nodeTest.test((NodeInfo)item);
    }

    @Override
    public NodeTest getItemType() {
        return this.nodeTest;
    }

    @Override
    public UType getUType() {
        return this.nodeTest.getUType();
    }

    @Override
    public int getFingerprint() {
        return this.nodeTest.getFingerprint();
    }

    @Override
    public String reconstruct() {
        return this.nodeTest.toString();
    }

    @Override
    public String toShortString() {
        if (this.getOriginalText() != null) {
            return this.getOriginalText();
        }
        return this.nodeTest.toShortString();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof NodeTestPattern && ((NodeTestPattern)other).nodeTest.equals(this.nodeTest);
    }

    @Override
    public int computeHashCode() {
        return 0x7AEFFEA8 ^ this.nodeTest.hashCode();
    }

    @Override
    public Pattern convertToTypedPattern(String val) throws XPathException {
        if (this.nodeTest instanceof NameTest && this.nodeTest.getUType() == UType.ELEMENT) {
            SchemaDeclaration decl = this.getConfiguration().getElementDeclaration(this.nodeTest.getMatchingNodeName());
            if (decl == null) {
                if ("lax".equals(val)) {
                    return this;
                }
                throw new XPathException("The mode specifies typed='strict', but there is no schema element declaration named " + this.nodeTest, "XTSE3105");
            }
            NodeTest schemaNodeTest = decl.makeSchemaNodeTest();
            return new NodeTestPattern(schemaNodeTest);
        }
        return this;
    }

    @Override
    public void export(ExpressionPresenter presenter) throws XPathException {
        presenter.startElement("p.nodeTest");
        presenter.emitAttribute("test", AlphaCode.fromItemType(this.nodeTest));
        presenter.endElement();
    }

    @Override
    public Pattern copy(RebindingMap rebindings) {
        NodeTestPattern n = new NodeTestPattern(this.nodeTest.copy());
        n.setPriority(this.getDefaultPriority());
        ExpressionTool.copyLocationInfo(this, n);
        n.setOriginalText(this.getOriginalText());
        return n;
    }

    public NodeTest getNodeTest() {
        return this.nodeTest;
    }
}


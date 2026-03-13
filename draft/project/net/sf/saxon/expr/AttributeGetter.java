/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyElementImpl;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.UntypedAtomicValue;

public final class AttributeGetter
extends Expression {
    public static final int CHECK_CONTEXT_ITEM_IS_NODE = 2;
    private FingerprintedQName attributeName;
    private int requiredChecks = 2;

    public AttributeGetter(FingerprintedQName attributeName) {
        this.attributeName = attributeName;
    }

    public FingerprintedQName getAttributeName() {
        return this.attributeName;
    }

    public void setRequiredChecks(int checks) {
        this.requiredChecks = checks;
    }

    public int getRequiredChecks() {
        return this.requiredChecks;
    }

    @Override
    public ItemType getItemType() {
        return BuiltInAtomicType.UNTYPED_ATOMIC;
    }

    @Override
    public int computeCardinality() {
        return 24576;
    }

    @Override
    protected int computeSpecialProperties() {
        return 0x800000;
    }

    @Override
    public int getIntrinsicDependencies() {
        return 2;
    }

    @Override
    public AttributeGetter copy(RebindingMap rebindings) {
        AttributeGetter ag2 = new AttributeGetter(this.attributeName);
        ag2.setRequiredChecks(this.requiredChecks);
        return ag2;
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        if (pathMapNodeSet == null) {
            ContextItemExpression cie = new ContextItemExpression();
            pathMapNodeSet = new PathMap.PathMapNodeSet(pathMap.makeNewRoot(cie));
        }
        return pathMapNodeSet.createArc(2, new NameTest(2, this.attributeName, this.getConfiguration().getNamePool()));
    }

    @Override
    public int getImplementationMethod() {
        return 1;
    }

    @Override
    public Item evaluateItem(XPathContext context) throws XPathException {
        Item item = context.getContextItem();
        if (item instanceof TinyElementImpl) {
            String val = ((TinyElementImpl)item).getAttributeValue(this.attributeName.getFingerprint());
            return val == null ? null : new UntypedAtomicValue(val);
        }
        if (item == null) {
            this.dynamicError("The context item for @" + this.attributeName.getDisplayName() + " is absent", "XPDY0002", context);
        }
        if (!(item instanceof NodeInfo)) {
            this.typeError("The context item for @" + this.attributeName.getDisplayName() + " is not a node", "XPDY0002", context);
        }
        assert (item instanceof NodeInfo);
        NodeInfo node = (NodeInfo)item;
        if (node.getNodeKind() == 1) {
            String val = node.getAttributeValue(this.attributeName.getURI(), this.attributeName.getLocalPart());
            return val == null ? null : new UntypedAtomicValue(val);
        }
        return null;
    }

    @Override
    public String getExpressionName() {
        return "attGetter";
    }

    @Override
    public String toShortString() {
        return "@" + this.attributeName.getDisplayName();
    }

    @Override
    public String toString() {
        return "data(@" + this.attributeName.getDisplayName() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AttributeGetter && ((AttributeGetter)obj).attributeName.equals(this.attributeName);
    }

    @Override
    public int computeHashCode() {
        return 0x14673 ^ this.attributeName.hashCode();
    }

    @Override
    public void export(ExpressionPresenter out) {
        out.startElement("attVal", this);
        out.emitAttribute("name", this.attributeName.getStructuredQName());
        out.emitAttribute("chk", "" + this.requiredChecks);
        out.endElement();
    }
}


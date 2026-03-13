/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemMappingFunction;
import net.sf.saxon.expr.ItemMappingIterator;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.UnaryExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;

public class EmptyTextNodeRemover
extends UnaryExpression
implements ItemMappingFunction {
    public EmptyTextNodeRemover(Expression p0) {
        super(p0);
    }

    @Override
    public ItemType getItemType() {
        return this.getBaseExpression().getItemType();
    }

    @Override
    public int computeCardinality() {
        return this.getBaseExpression().getCardinality() | 0x2000;
    }

    @Override
    protected OperandRole getOperandRole() {
        return OperandRole.SAME_FOCUS_ACTION;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        EmptyTextNodeRemover e2 = new EmptyTextNodeRemover(this.getBaseExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, e2);
        return e2;
    }

    @Override
    public int getImplementationMethod() {
        return 26;
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        return new ItemMappingIterator(this.getBaseExpression().iterate(context), this);
    }

    @Override
    public Item mapItem(Item item) throws XPathException {
        if (item instanceof NodeInfo && ((NodeInfo)item).getNodeKind() == 3 && item.getStringValueCS().length() == 0) {
            return null;
        }
        return item;
    }

    @Override
    public String getStreamerName() {
        return "EmptyTextNodeRemover";
    }

    @Override
    public String getExpressionName() {
        return "emptyTextNodeRemover";
    }
}


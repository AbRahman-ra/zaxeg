/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.WherePopulatedOutputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemMappingFunction;
import net.sf.saxon.expr.ItemMappingIterator;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.UnaryExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.HexBinaryValue;
import net.sf.saxon.value.StringValue;

public class WherePopulated
extends UnaryExpression
implements ItemMappingFunction {
    public WherePopulated(Expression base) {
        super(base);
    }

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    protected OperandRole getOperandRole() {
        return new OperandRole(0, OperandUsage.TRANSMISSION);
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        return new WherePopulated(this.getBaseExpression().copy(rebindings));
    }

    @Override
    public int getImplementationMethod() {
        return 6;
    }

    @Override
    public int computeCardinality() {
        return super.computeCardinality() | 0x2000;
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        return new ItemMappingIterator(this.getBaseExpression().iterate(context), this);
    }

    @Override
    public void process(Outputter output, XPathContext context) throws XPathException {
        WherePopulatedOutputter filter = new WherePopulatedOutputter(output);
        this.getBaseExpression().process(filter, context);
    }

    @Override
    public Item mapItem(Item item) throws XPathException {
        return WherePopulated.isDeemedEmpty(item) ? null : item;
    }

    public static boolean isDeemedEmpty(Item item) {
        if (item instanceof NodeInfo) {
            int kind = ((NodeInfo)item).getNodeKind();
            switch (kind) {
                case 1: 
                case 9: {
                    return !((NodeInfo)item).hasChildNodes();
                }
            }
            return item.getStringValueCS().length() == 0;
        }
        if (item instanceof StringValue || item instanceof HexBinaryValue || item instanceof Base64BinaryValue) {
            return item.getStringValueCS().length() == 0;
        }
        if (item instanceof MapItem) {
            return ((MapItem)item).isEmpty();
        }
        return false;
    }

    @Override
    public String getExpressionName() {
        return "wherePop";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("condCont", this);
        this.getBaseExpression().export(out);
        out.endElement();
    }

    @Override
    public String getStreamerName() {
        return "WherePopulated";
    }
}


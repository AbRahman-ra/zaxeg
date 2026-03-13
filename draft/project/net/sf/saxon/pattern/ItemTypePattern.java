/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AlphaCode;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.UType;

public class ItemTypePattern
extends Pattern {
    private ItemType itemType;

    public ItemTypePattern(ItemType test) {
        this.itemType = test;
        this.setPriority(test.getDefaultPriority());
    }

    @Override
    public boolean matches(Item item, XPathContext context) throws XPathException {
        return this.itemType.matches(item, context.getConfiguration().getTypeHierarchy());
    }

    @Override
    public ItemType getItemType() {
        return this.itemType;
    }

    @Override
    public UType getUType() {
        return this.itemType.getUType();
    }

    @Override
    public String reconstruct() {
        return this.itemType.toString();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ItemTypePattern && ((ItemTypePattern)other).itemType.equals(this.itemType);
    }

    @Override
    public int computeHashCode() {
        return 0x7A83D1A8 ^ this.itemType.hashCode();
    }

    @Override
    public void export(ExpressionPresenter presenter) throws XPathException {
        presenter.startElement("p.nodeTest");
        presenter.emitAttribute("test", AlphaCode.fromItemType(this.itemType));
        presenter.endElement();
    }

    @Override
    public Pattern copy(RebindingMap rebindings) {
        return new ItemTypePattern(this.itemType);
    }
}


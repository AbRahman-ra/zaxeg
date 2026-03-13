/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.UType;

public class UniversalPattern
extends Pattern {
    public UniversalPattern() {
        this.setPriority(-1.0);
    }

    @Override
    public boolean matches(Item item, XPathContext context) {
        return true;
    }

    @Override
    public UType getUType() {
        return UType.ANY;
    }

    @Override
    public ItemType getItemType() {
        return AnyItemType.getInstance();
    }

    @Override
    public int getFingerprint() {
        return -1;
    }

    @Override
    public String reconstruct() {
        return ".";
    }

    @Override
    public void export(ExpressionPresenter presenter) {
        presenter.startElement("p.any");
        presenter.endElement();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof UniversalPattern;
    }

    @Override
    public int computeHashCode() {
        return 2062339752;
    }

    @Override
    public Pattern copy(RebindingMap rebindings) {
        return this;
    }
}


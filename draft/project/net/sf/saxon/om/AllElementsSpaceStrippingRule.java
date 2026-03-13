/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.Stripper;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.type.SchemaType;

public class AllElementsSpaceStrippingRule
implements SpaceStrippingRule {
    private static final AllElementsSpaceStrippingRule THE_INSTANCE = new AllElementsSpaceStrippingRule();

    public static AllElementsSpaceStrippingRule getInstance() {
        return THE_INSTANCE;
    }

    @Override
    public int isSpacePreserving(NodeName fingerprint, SchemaType schemaType) {
        return 0;
    }

    @Override
    public ProxyReceiver makeStripper(Receiver next) {
        return new Stripper(this, next);
    }

    @Override
    public void export(ExpressionPresenter presenter) {
        presenter.startElement("strip.all");
        presenter.endElement();
    }
}


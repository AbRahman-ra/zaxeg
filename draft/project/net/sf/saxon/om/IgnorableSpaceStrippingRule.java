/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.event.IgnorableWhitespaceStripper;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.type.ComplexType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Untyped;

public class IgnorableSpaceStrippingRule
implements SpaceStrippingRule {
    private static final IgnorableSpaceStrippingRule THE_INSTANCE = new IgnorableSpaceStrippingRule();

    public static IgnorableSpaceStrippingRule getInstance() {
        return THE_INSTANCE;
    }

    @Override
    public int isSpacePreserving(NodeName name, SchemaType schemaType) {
        if (schemaType != Untyped.getInstance() && schemaType.isComplexType() && !((ComplexType)schemaType).isSimpleContent() && !((ComplexType)schemaType).isMixedContent()) {
            return 2;
        }
        return 1;
    }

    @Override
    public ProxyReceiver makeStripper(Receiver next) {
        return new IgnorableWhitespaceStripper(next);
    }

    @Override
    public void export(ExpressionPresenter presenter) {
        presenter.startElement("strip.ignorable");
        presenter.endElement();
    }
}


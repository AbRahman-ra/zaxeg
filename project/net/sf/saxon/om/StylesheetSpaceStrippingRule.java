/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.Arrays;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.Stripper;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.type.SchemaType;

public class StylesheetSpaceStrippingRule
implements SpaceStrippingRule {
    private static final int[] specials = new int[]{131, 132, 133, 136, 138, 141, 142, 153, 169, 172, 177, 178, 199, 202};
    private NamePool namePool;

    public StylesheetSpaceStrippingRule(NamePool pool) {
        this.namePool = pool;
    }

    @Override
    public int isSpacePreserving(NodeName elementName, SchemaType schemaType) {
        int fingerprint = elementName.obtainFingerprint(this.namePool);
        if (fingerprint == 201) {
            return 1;
        }
        if (Arrays.binarySearch(specials, fingerprint) >= 0) {
            return 2;
        }
        return 0;
    }

    @Override
    public ProxyReceiver makeStripper(Receiver next) {
        return new Stripper(this, next);
    }

    @Override
    public void export(ExpressionPresenter presenter) {
    }
}


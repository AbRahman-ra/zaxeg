/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.SequenceNormalizer;
import net.sf.saxon.om.Item;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;

public class SequenceNormalizerWithSpaceSeparator
extends SequenceNormalizer {
    public SequenceNormalizerWithSpaceSeparator(Receiver next) {
        super(next);
    }

    @Override
    public void append(Item item, Location locationId, int copyNamespaces) throws XPathException {
        this.decompose(item, locationId, copyNamespaces);
    }

    @Override
    protected String getErrorCodeForDecomposingFunctionItems() {
        return "SENR0001";
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.om.Item;
import net.sf.saxon.trans.XPathException;

@FunctionalInterface
public interface ItemConsumer<T extends Item> {
    public void accept(T var1) throws XPathException;
}


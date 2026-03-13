/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.util;

import org.jaxen.Navigator;
import org.jaxen.util.DescendantAxisIterator;
import org.jaxen.util.SingleObjectIterator;

public class DescendantOrSelfAxisIterator
extends DescendantAxisIterator {
    public DescendantOrSelfAxisIterator(Object contextNode, Navigator navigator) {
        super(navigator, new SingleObjectIterator(contextNode));
    }
}


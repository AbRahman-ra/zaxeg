/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.util.Collections;
import net.sf.saxon.s9api.XdmValue;

public class XdmEmptySequence
extends XdmValue {
    private static XdmEmptySequence THE_INSTANCE = new XdmEmptySequence();

    public static XdmEmptySequence getInstance() {
        return THE_INSTANCE;
    }

    private XdmEmptySequence() {
        super(Collections.emptyList());
    }

    @Override
    public int size() {
        return 0;
    }
}


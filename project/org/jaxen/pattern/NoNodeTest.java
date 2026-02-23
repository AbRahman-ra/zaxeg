/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.pattern;

import org.jaxen.Context;
import org.jaxen.pattern.NodeTest;

public class NoNodeTest
extends NodeTest {
    private static NoNodeTest instance = new NoNodeTest();

    public static NoNodeTest getInstance() {
        return instance;
    }

    public boolean matches(Object node, Context context) {
        return false;
    }

    public double getPriority() {
        return -0.5;
    }

    public short getMatchType() {
        return 14;
    }

    public String getText() {
        return "";
    }
}


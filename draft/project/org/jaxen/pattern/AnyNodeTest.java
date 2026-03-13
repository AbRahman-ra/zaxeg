/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.pattern;

import org.jaxen.Context;
import org.jaxen.pattern.NodeTest;

public class AnyNodeTest
extends NodeTest {
    private static AnyNodeTest instance = new AnyNodeTest();

    public static AnyNodeTest getInstance() {
        return instance;
    }

    private AnyNodeTest() {
    }

    public boolean matches(Object node, Context context) {
        return true;
    }

    public double getPriority() {
        return -0.5;
    }

    public short getMatchType() {
        return 0;
    }

    public String getText() {
        return "*";
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.pattern;

import org.jaxen.Context;
import org.jaxen.pattern.NodeTest;

public class AnyChildNodeTest
extends NodeTest {
    private static AnyChildNodeTest instance = new AnyChildNodeTest();

    public static AnyChildNodeTest getInstance() {
        return instance;
    }

    public boolean matches(Object node, Context context) {
        short type = context.getNavigator().getNodeType(node);
        return type == 1 || type == 3 || type == 8 || type == 7;
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


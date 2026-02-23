/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.pattern;

import org.jaxen.Context;
import org.jaxen.pattern.NodeTest;

public class NodeTypeTest
extends NodeTest {
    public static final NodeTypeTest DOCUMENT_TEST = new NodeTypeTest(9);
    public static final NodeTypeTest ELEMENT_TEST = new NodeTypeTest(1);
    public static final NodeTypeTest ATTRIBUTE_TEST = new NodeTypeTest(2);
    public static final NodeTypeTest COMMENT_TEST = new NodeTypeTest(8);
    public static final NodeTypeTest TEXT_TEST = new NodeTypeTest(3);
    public static final NodeTypeTest PROCESSING_INSTRUCTION_TEST = new NodeTypeTest(7);
    public static final NodeTypeTest NAMESPACE_TEST = new NodeTypeTest(13);
    private short nodeType;

    public NodeTypeTest(short nodeType) {
        this.nodeType = nodeType;
    }

    public boolean matches(Object node, Context context) {
        return this.nodeType == context.getNavigator().getNodeType(node);
    }

    public double getPriority() {
        return -0.5;
    }

    public short getMatchType() {
        return this.nodeType;
    }

    public String getText() {
        switch (this.nodeType) {
            case 1: {
                return "child()";
            }
            case 2: {
                return "@*";
            }
            case 13: {
                return "namespace()";
            }
            case 9: {
                return "/";
            }
            case 8: {
                return "comment()";
            }
            case 3: {
                return "text()";
            }
            case 7: {
                return "processing-instruction()";
            }
        }
        return "";
    }

    public String toString() {
        return super.toString() + "[ type: " + this.nodeType + " ]";
    }
}


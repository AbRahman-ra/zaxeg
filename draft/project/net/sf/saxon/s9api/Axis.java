/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

public enum Axis {
    ANCESTOR(0),
    ANCESTOR_OR_SELF(1),
    ATTRIBUTE(2),
    CHILD(3),
    DESCENDANT(4),
    DESCENDANT_OR_SELF(5),
    FOLLOWING(6),
    FOLLOWING_SIBLING(7),
    PARENT(9),
    PRECEDING(10),
    PRECEDING_SIBLING(11),
    SELF(12),
    NAMESPACE(8);

    private int number;

    private Axis(int number) {
        this.number = number;
    }

    public int getAxisNumber() {
        return this.number;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

public enum Genre {
    ANY("any item"),
    ATOMIC("an atomic value"),
    NODE("a node"),
    FUNCTION("a function"),
    MAP("a map"),
    ARRAY("an array"),
    EXTERNAL("an external object");

    private String description;

    private Genre(String desc) {
        this.description = desc;
    }

    public String getDescription() {
        return this.description;
    }
}


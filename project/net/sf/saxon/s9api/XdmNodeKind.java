/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

public enum XdmNodeKind {
    DOCUMENT(9),
    ELEMENT(1),
    ATTRIBUTE(2),
    TEXT(3),
    COMMENT(8),
    PROCESSING_INSTRUCTION(7),
    NAMESPACE(13);

    private int number;

    private XdmNodeKind(int number) {
        this.number = number;
    }

    protected int getNumber() {
        return this.number;
    }

    public static XdmNodeKind forType(int type) {
        switch (type) {
            case 9: {
                return DOCUMENT;
            }
            case 1: {
                return ELEMENT;
            }
            case 2: {
                return ATTRIBUTE;
            }
            case 3: {
                return TEXT;
            }
            case 8: {
                return COMMENT;
            }
            case 7: {
                return PROCESSING_INSTRUCTION;
            }
            case 13: {
                return NAMESPACE;
            }
        }
        throw new IllegalArgumentException();
    }
}


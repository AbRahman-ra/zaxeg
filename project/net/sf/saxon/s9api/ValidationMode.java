/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

public enum ValidationMode {
    STRICT(1),
    LAX(2),
    PRESERVE(3),
    STRIP(4),
    DEFAULT(0);

    private int number;

    private ValidationMode(int number) {
        this.number = number;
    }

    protected int getNumber() {
        return this.number;
    }

    protected static ValidationMode get(int number) {
        switch (number) {
            case 1: {
                return STRICT;
            }
            case 2: {
                return LAX;
            }
            case 4: {
                return STRIP;
            }
            case 3: {
                return PRESERVE;
            }
        }
        return DEFAULT;
    }
}


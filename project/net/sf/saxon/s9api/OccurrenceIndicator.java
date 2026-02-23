/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import net.sf.saxon.value.Cardinality;

public enum OccurrenceIndicator {
    ZERO,
    ZERO_OR_ONE,
    ZERO_OR_MORE,
    ONE,
    ONE_OR_MORE;


    protected int getCardinality() {
        switch (this) {
            case ZERO: {
                return 8192;
            }
            case ZERO_OR_ONE: {
                return 24576;
            }
            case ZERO_OR_MORE: {
                return 57344;
            }
            case ONE: {
                return 16384;
            }
            case ONE_OR_MORE: {
                return 49152;
            }
        }
        return 8192;
    }

    protected static OccurrenceIndicator getOccurrenceIndicator(int cardinality) {
        switch (cardinality) {
            case 8192: {
                return ZERO;
            }
            case 24576: {
                return ZERO_OR_ONE;
            }
            case 57344: {
                return ZERO_OR_MORE;
            }
            case 16384: {
                return ONE;
            }
            case 49152: {
                return ONE_OR_MORE;
            }
        }
        return ZERO_OR_MORE;
    }

    public boolean allowsZero() {
        return Cardinality.allowsZero(this.getCardinality());
    }

    public boolean allowsMany() {
        return Cardinality.allowsMany(this.getCardinality());
    }

    public boolean subsumes(OccurrenceIndicator other) {
        return Cardinality.subsumes(this.getCardinality(), other.getCardinality());
    }

    public String toString() {
        switch (this) {
            case ZERO: {
                return "0";
            }
            case ZERO_OR_ONE: {
                return "?";
            }
            case ZERO_OR_MORE: {
                return "*";
            }
            case ONE: {
                return "";
            }
            case ONE_OR_MORE: {
                return "+";
            }
        }
        return "!!!";
    }
}


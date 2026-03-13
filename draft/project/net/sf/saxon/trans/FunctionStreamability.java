/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

public enum FunctionStreamability {
    UNCLASSIFIED("unclassified"),
    ABSORBING("absorbing"),
    INSPECTION("inspection"),
    FILTER("filter"),
    SHALLOW_DESCENT("shallow-descent"),
    DEEP_DESCENT("deep-descent"),
    ASCENT("ascent");

    public String streamabilityStr;

    public boolean isConsuming() {
        return this == ABSORBING || this == SHALLOW_DESCENT || this == DEEP_DESCENT;
    }

    public boolean isStreaming() {
        return this != UNCLASSIFIED;
    }

    private FunctionStreamability(String v) {
        this.streamabilityStr = v;
    }

    public static FunctionStreamability of(String v) {
        switch (v) {
            default: {
                return UNCLASSIFIED;
            }
            case "absorbing": {
                return ABSORBING;
            }
            case "inspection": {
                return INSPECTION;
            }
            case "filter": {
                return FILTER;
            }
            case "shallow-descent": {
                return SHALLOW_DESCENT;
            }
            case "deep-descent": {
                return DEEP_DESCENT;
            }
            case "ascent": 
        }
        return ASCENT;
    }
}


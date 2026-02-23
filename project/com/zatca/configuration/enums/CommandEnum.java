/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.configuration.enums;

public enum CommandEnum {
    VALIDATE_XML("validatexml"),
    VALIDATE_QR("validateqr"),
    GENERATE_HASH("generateHash"),
    GENERATE("generate");

    private String name;

    private CommandEnum(String name) {
        this.name = name;
    }

    public static CommandEnum asCommandEnum(String str) {
        for (CommandEnum me : CommandEnum.values()) {
            if (!me.name().equalsIgnoreCase(str)) continue;
            return me;
        }
        return null;
    }
}


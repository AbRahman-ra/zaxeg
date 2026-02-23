/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.util.logging.Level;
import net.sf.saxon.lib.Logger;

public class SystemLogger
extends Logger {
    private java.util.logging.Logger log;

    public SystemLogger(java.util.logging.Logger log) {
        this.log = log;
    }

    private Level getLevel(int severity) {
        switch (severity) {
            case 0: {
                return Level.INFO;
            }
            case 1: {
                return Level.WARNING;
            }
        }
        return Level.SEVERE;
    }

    @Override
    public void println(String message, int severity) {
        this.log.log(this.getLevel(severity), message);
    }
}


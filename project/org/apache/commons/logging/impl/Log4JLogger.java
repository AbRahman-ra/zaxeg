/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.logging.impl;

import java.io.Serializable;
import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

public class Log4JLogger
implements Log,
Serializable {
    private static final String FQCN = (class$org$apache$commons$logging$impl$Log4JLogger == null ? (class$org$apache$commons$logging$impl$Log4JLogger = Log4JLogger.class$("org.apache.commons.logging.impl.Log4JLogger")) : class$org$apache$commons$logging$impl$Log4JLogger).getName();
    private transient Logger logger = null;
    private String name = null;
    private static Priority traceLevel;
    static /* synthetic */ Class class$org$apache$commons$logging$impl$Log4JLogger;
    static /* synthetic */ Class class$org$apache$log4j$Level;
    static /* synthetic */ Class class$org$apache$log4j$Priority;

    public Log4JLogger() {
    }

    public Log4JLogger(String name) {
        this.name = name;
        this.logger = this.getLogger();
    }

    public Log4JLogger(Logger logger) {
        if (logger == null) {
            throw new IllegalArgumentException("Warning - null logger in constructor; possible log4j misconfiguration.");
        }
        this.name = logger.getName();
        this.logger = logger;
    }

    public void trace(Object message) {
        this.getLogger().log(FQCN, traceLevel, message, null);
    }

    public void trace(Object message, Throwable t) {
        this.getLogger().log(FQCN, traceLevel, message, t);
    }

    public void debug(Object message) {
        this.getLogger().log(FQCN, Priority.DEBUG, message, null);
    }

    public void debug(Object message, Throwable t) {
        this.getLogger().log(FQCN, Priority.DEBUG, message, t);
    }

    public void info(Object message) {
        this.getLogger().log(FQCN, Priority.INFO, message, null);
    }

    public void info(Object message, Throwable t) {
        this.getLogger().log(FQCN, Priority.INFO, message, t);
    }

    public void warn(Object message) {
        this.getLogger().log(FQCN, Priority.WARN, message, null);
    }

    public void warn(Object message, Throwable t) {
        this.getLogger().log(FQCN, Priority.WARN, message, t);
    }

    public void error(Object message) {
        this.getLogger().log(FQCN, Priority.ERROR, message, null);
    }

    public void error(Object message, Throwable t) {
        this.getLogger().log(FQCN, Priority.ERROR, message, t);
    }

    public void fatal(Object message) {
        this.getLogger().log(FQCN, Priority.FATAL, message, null);
    }

    public void fatal(Object message, Throwable t) {
        this.getLogger().log(FQCN, Priority.FATAL, message, t);
    }

    public Logger getLogger() {
        if (this.logger == null) {
            this.logger = Logger.getLogger(this.name);
        }
        return this.logger;
    }

    public boolean isDebugEnabled() {
        return this.getLogger().isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return this.getLogger().isEnabledFor(Priority.ERROR);
    }

    public boolean isFatalEnabled() {
        return this.getLogger().isEnabledFor(Priority.FATAL);
    }

    public boolean isInfoEnabled() {
        return this.getLogger().isInfoEnabled();
    }

    public boolean isTraceEnabled() {
        return this.getLogger().isEnabledFor(traceLevel);
    }

    public boolean isWarnEnabled() {
        return this.getLogger().isEnabledFor(Priority.WARN);
    }

    static /* synthetic */ Class class$(String x0) {
        try {
            return Class.forName(x0);
        } catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }

    static {
        if (!(class$org$apache$log4j$Priority == null ? (class$org$apache$log4j$Priority = Log4JLogger.class$("org.apache.log4j.Priority")) : class$org$apache$log4j$Priority).isAssignableFrom(class$org$apache$log4j$Level == null ? (class$org$apache$log4j$Level = Log4JLogger.class$("org.apache.log4j.Level")) : class$org$apache$log4j$Level)) {
            throw new InstantiationError("Log4J 1.2 not available");
        }
        try {
            traceLevel = (Priority)(class$org$apache$log4j$Level == null ? (class$org$apache$log4j$Level = Log4JLogger.class$("org.apache.log4j.Level")) : class$org$apache$log4j$Level).getDeclaredField("TRACE").get(null);
        } catch (Exception ex) {
            traceLevel = Priority.DEBUG;
        }
    }
}


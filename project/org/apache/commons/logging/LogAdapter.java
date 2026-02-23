/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.logging;

import java.io.Serializable;
import java.util.logging.LogRecord;
import org.apache.commons.logging.Log;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

final class LogAdapter {
    private static final String LOG4J_SPI = "org.apache.logging.log4j.spi.ExtendedLogger";
    private static final String LOG4J_SLF4J_PROVIDER = "org.apache.logging.slf4j.SLF4JProvider";
    private static final String SLF4J_SPI = "org.slf4j.spi.LocationAwareLogger";
    private static final String SLF4J_API = "org.slf4j.Logger";
    private static final LogApi logApi = LogAdapter.isPresent("org.apache.logging.log4j.spi.ExtendedLogger") ? (LogAdapter.isPresent("org.apache.logging.slf4j.SLF4JProvider") && LogAdapter.isPresent("org.slf4j.spi.LocationAwareLogger") ? LogApi.SLF4J_LAL : LogApi.LOG4J) : (LogAdapter.isPresent("org.slf4j.spi.LocationAwareLogger") ? LogApi.SLF4J_LAL : (LogAdapter.isPresent("org.slf4j.Logger") ? LogApi.SLF4J : LogApi.JUL));

    private LogAdapter() {
    }

    public static Log createLog(String name) {
        switch (logApi) {
            case LOG4J: {
                return Log4jAdapter.createLog(name);
            }
            case SLF4J_LAL: {
                return Slf4jAdapter.createLocationAwareLog(name);
            }
            case SLF4J: {
                return Slf4jAdapter.createLog(name);
            }
        }
        return JavaUtilAdapter.createLog(name);
    }

    private static boolean isPresent(String className) {
        try {
            Class.forName(className, false, LogAdapter.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    private static class LocationResolvingLogRecord
    extends LogRecord {
        private static final String FQCN = JavaUtilLog.class.getName();
        private volatile boolean resolved;

        public LocationResolvingLogRecord(java.util.logging.Level level, String msg) {
            super(level, msg);
        }

        @Override
        public String getSourceClassName() {
            if (!this.resolved) {
                this.resolve();
            }
            return super.getSourceClassName();
        }

        @Override
        public void setSourceClassName(String sourceClassName) {
            super.setSourceClassName(sourceClassName);
            this.resolved = true;
        }

        @Override
        public String getSourceMethodName() {
            if (!this.resolved) {
                this.resolve();
            }
            return super.getSourceMethodName();
        }

        @Override
        public void setSourceMethodName(String sourceMethodName) {
            super.setSourceMethodName(sourceMethodName);
            this.resolved = true;
        }

        private void resolve() {
            StackTraceElement[] stack = new Throwable().getStackTrace();
            String sourceClassName = null;
            String sourceMethodName = null;
            boolean found = false;
            for (StackTraceElement element : stack) {
                String className = element.getClassName();
                if (FQCN.equals(className)) {
                    found = true;
                    continue;
                }
                if (!found) continue;
                sourceClassName = className;
                sourceMethodName = element.getMethodName();
                break;
            }
            this.setSourceClassName(sourceClassName);
            this.setSourceMethodName(sourceMethodName);
        }

        protected Object writeReplace() {
            LogRecord serialized = new LogRecord(this.getLevel(), this.getMessage());
            serialized.setLoggerName(this.getLoggerName());
            serialized.setResourceBundle(this.getResourceBundle());
            serialized.setResourceBundleName(this.getResourceBundleName());
            serialized.setSourceClassName(this.getSourceClassName());
            serialized.setSourceMethodName(this.getSourceMethodName());
            serialized.setSequenceNumber(this.getSequenceNumber());
            serialized.setParameters(this.getParameters());
            serialized.setThreadID(this.getThreadID());
            serialized.setMillis(this.getMillis());
            serialized.setThrown(this.getThrown());
            return serialized;
        }
    }

    private static class JavaUtilLog
    implements Log,
    Serializable {
        private final String name;
        private final transient java.util.logging.Logger logger;

        public JavaUtilLog(String name) {
            this.name = name;
            this.logger = java.util.logging.Logger.getLogger(name);
        }

        @Override
        public boolean isFatalEnabled() {
            return this.isErrorEnabled();
        }

        @Override
        public boolean isErrorEnabled() {
            return this.logger.isLoggable(java.util.logging.Level.SEVERE);
        }

        @Override
        public boolean isWarnEnabled() {
            return this.logger.isLoggable(java.util.logging.Level.WARNING);
        }

        @Override
        public boolean isInfoEnabled() {
            return this.logger.isLoggable(java.util.logging.Level.INFO);
        }

        @Override
        public boolean isDebugEnabled() {
            return this.logger.isLoggable(java.util.logging.Level.FINE);
        }

        @Override
        public boolean isTraceEnabled() {
            return this.logger.isLoggable(java.util.logging.Level.FINEST);
        }

        @Override
        public void fatal(Object message) {
            this.error(message);
        }

        @Override
        public void fatal(Object message, Throwable exception) {
            this.error(message, exception);
        }

        @Override
        public void error(Object message) {
            this.log(java.util.logging.Level.SEVERE, message, null);
        }

        @Override
        public void error(Object message, Throwable exception) {
            this.log(java.util.logging.Level.SEVERE, message, exception);
        }

        @Override
        public void warn(Object message) {
            this.log(java.util.logging.Level.WARNING, message, null);
        }

        @Override
        public void warn(Object message, Throwable exception) {
            this.log(java.util.logging.Level.WARNING, message, exception);
        }

        @Override
        public void info(Object message) {
            this.log(java.util.logging.Level.INFO, message, null);
        }

        @Override
        public void info(Object message, Throwable exception) {
            this.log(java.util.logging.Level.INFO, message, exception);
        }

        @Override
        public void debug(Object message) {
            this.log(java.util.logging.Level.FINE, message, null);
        }

        @Override
        public void debug(Object message, Throwable exception) {
            this.log(java.util.logging.Level.FINE, message, exception);
        }

        @Override
        public void trace(Object message) {
            this.log(java.util.logging.Level.FINEST, message, null);
        }

        @Override
        public void trace(Object message, Throwable exception) {
            this.log(java.util.logging.Level.FINEST, message, exception);
        }

        private void log(java.util.logging.Level level, Object message, Throwable exception) {
            if (this.logger.isLoggable(level)) {
                LogRecord rec;
                if (message instanceof LogRecord) {
                    rec = (LogRecord)message;
                } else {
                    rec = new LocationResolvingLogRecord(level, String.valueOf(message));
                    rec.setLoggerName(this.name);
                    rec.setResourceBundleName(this.logger.getResourceBundleName());
                    rec.setResourceBundle(this.logger.getResourceBundle());
                    rec.setThrown(exception);
                }
                this.logger.log(rec);
            }
        }

        protected Object readResolve() {
            return new JavaUtilLog(this.name);
        }
    }

    private static class Slf4jLocationAwareLog
    extends Slf4jLog<LocationAwareLogger>
    implements Serializable {
        private static final String FQCN = Slf4jLocationAwareLog.class.getName();

        public Slf4jLocationAwareLog(LocationAwareLogger logger) {
            super(logger);
        }

        @Override
        public void fatal(Object message) {
            this.error(message);
        }

        @Override
        public void fatal(Object message, Throwable exception) {
            this.error(message, exception);
        }

        @Override
        public void error(Object message) {
            if (message instanceof String || ((LocationAwareLogger)this.logger).isErrorEnabled()) {
                ((LocationAwareLogger)this.logger).log(null, FQCN, 40, String.valueOf(message), null, null);
            }
        }

        @Override
        public void error(Object message, Throwable exception) {
            if (message instanceof String || ((LocationAwareLogger)this.logger).isErrorEnabled()) {
                ((LocationAwareLogger)this.logger).log(null, FQCN, 40, String.valueOf(message), null, exception);
            }
        }

        @Override
        public void warn(Object message) {
            if (message instanceof String || ((LocationAwareLogger)this.logger).isWarnEnabled()) {
                ((LocationAwareLogger)this.logger).log(null, FQCN, 30, String.valueOf(message), null, null);
            }
        }

        @Override
        public void warn(Object message, Throwable exception) {
            if (message instanceof String || ((LocationAwareLogger)this.logger).isWarnEnabled()) {
                ((LocationAwareLogger)this.logger).log(null, FQCN, 30, String.valueOf(message), null, exception);
            }
        }

        @Override
        public void info(Object message) {
            if (message instanceof String || ((LocationAwareLogger)this.logger).isInfoEnabled()) {
                ((LocationAwareLogger)this.logger).log(null, FQCN, 20, String.valueOf(message), null, null);
            }
        }

        @Override
        public void info(Object message, Throwable exception) {
            if (message instanceof String || ((LocationAwareLogger)this.logger).isInfoEnabled()) {
                ((LocationAwareLogger)this.logger).log(null, FQCN, 20, String.valueOf(message), null, exception);
            }
        }

        @Override
        public void debug(Object message) {
            if (message instanceof String || ((LocationAwareLogger)this.logger).isDebugEnabled()) {
                ((LocationAwareLogger)this.logger).log(null, FQCN, 10, String.valueOf(message), null, null);
            }
        }

        @Override
        public void debug(Object message, Throwable exception) {
            if (message instanceof String || ((LocationAwareLogger)this.logger).isDebugEnabled()) {
                ((LocationAwareLogger)this.logger).log(null, FQCN, 10, String.valueOf(message), null, exception);
            }
        }

        @Override
        public void trace(Object message) {
            if (message instanceof String || ((LocationAwareLogger)this.logger).isTraceEnabled()) {
                ((LocationAwareLogger)this.logger).log(null, FQCN, 0, String.valueOf(message), null, null);
            }
        }

        @Override
        public void trace(Object message, Throwable exception) {
            if (message instanceof String || ((LocationAwareLogger)this.logger).isTraceEnabled()) {
                ((LocationAwareLogger)this.logger).log(null, FQCN, 0, String.valueOf(message), null, exception);
            }
        }

        @Override
        protected Object readResolve() {
            return Slf4jAdapter.createLocationAwareLog(this.name);
        }
    }

    private static class Slf4jLog<T extends Logger>
    implements Log,
    Serializable {
        protected final String name;
        protected final transient T logger;

        public Slf4jLog(T logger) {
            this.name = logger.getName();
            this.logger = logger;
        }

        @Override
        public boolean isFatalEnabled() {
            return this.isErrorEnabled();
        }

        @Override
        public boolean isErrorEnabled() {
            return this.logger.isErrorEnabled();
        }

        @Override
        public boolean isWarnEnabled() {
            return this.logger.isWarnEnabled();
        }

        @Override
        public boolean isInfoEnabled() {
            return this.logger.isInfoEnabled();
        }

        @Override
        public boolean isDebugEnabled() {
            return this.logger.isDebugEnabled();
        }

        @Override
        public boolean isTraceEnabled() {
            return this.logger.isTraceEnabled();
        }

        @Override
        public void fatal(Object message) {
            this.error(message);
        }

        @Override
        public void fatal(Object message, Throwable exception) {
            this.error(message, exception);
        }

        @Override
        public void error(Object message) {
            if (message instanceof String || this.logger.isErrorEnabled()) {
                this.logger.error(String.valueOf(message));
            }
        }

        @Override
        public void error(Object message, Throwable exception) {
            if (message instanceof String || this.logger.isErrorEnabled()) {
                this.logger.error(String.valueOf(message), exception);
            }
        }

        @Override
        public void warn(Object message) {
            if (message instanceof String || this.logger.isWarnEnabled()) {
                this.logger.warn(String.valueOf(message));
            }
        }

        @Override
        public void warn(Object message, Throwable exception) {
            if (message instanceof String || this.logger.isWarnEnabled()) {
                this.logger.warn(String.valueOf(message), exception);
            }
        }

        @Override
        public void info(Object message) {
            if (message instanceof String || this.logger.isInfoEnabled()) {
                this.logger.info(String.valueOf(message));
            }
        }

        @Override
        public void info(Object message, Throwable exception) {
            if (message instanceof String || this.logger.isInfoEnabled()) {
                this.logger.info(String.valueOf(message), exception);
            }
        }

        @Override
        public void debug(Object message) {
            if (message instanceof String || this.logger.isDebugEnabled()) {
                this.logger.debug(String.valueOf(message));
            }
        }

        @Override
        public void debug(Object message, Throwable exception) {
            if (message instanceof String || this.logger.isDebugEnabled()) {
                this.logger.debug(String.valueOf(message), exception);
            }
        }

        @Override
        public void trace(Object message) {
            if (message instanceof String || this.logger.isTraceEnabled()) {
                this.logger.trace(String.valueOf(message));
            }
        }

        @Override
        public void trace(Object message, Throwable exception) {
            if (message instanceof String || this.logger.isTraceEnabled()) {
                this.logger.trace(String.valueOf(message), exception);
            }
        }

        protected Object readResolve() {
            return Slf4jAdapter.createLog(this.name);
        }
    }

    private static class Log4jLog
    implements Log,
    Serializable {
        private static final String FQCN = Log4jLog.class.getName();
        private static final LoggerContext loggerContext = LogManager.getContext(Log4jLog.class.getClassLoader(), false);
        private final ExtendedLogger logger;

        public Log4jLog(String name) {
            LoggerContext context = loggerContext;
            if (context == null) {
                context = LogManager.getContext(Log4jLog.class.getClassLoader(), false);
            }
            this.logger = context.getLogger(name);
        }

        @Override
        public boolean isFatalEnabled() {
            return this.logger.isEnabled(Level.FATAL);
        }

        @Override
        public boolean isErrorEnabled() {
            return this.logger.isEnabled(Level.ERROR);
        }

        @Override
        public boolean isWarnEnabled() {
            return this.logger.isEnabled(Level.WARN);
        }

        @Override
        public boolean isInfoEnabled() {
            return this.logger.isEnabled(Level.INFO);
        }

        @Override
        public boolean isDebugEnabled() {
            return this.logger.isEnabled(Level.DEBUG);
        }

        @Override
        public boolean isTraceEnabled() {
            return this.logger.isEnabled(Level.TRACE);
        }

        @Override
        public void fatal(Object message) {
            this.log(Level.FATAL, message, null);
        }

        @Override
        public void fatal(Object message, Throwable exception) {
            this.log(Level.FATAL, message, exception);
        }

        @Override
        public void error(Object message) {
            this.log(Level.ERROR, message, null);
        }

        @Override
        public void error(Object message, Throwable exception) {
            this.log(Level.ERROR, message, exception);
        }

        @Override
        public void warn(Object message) {
            this.log(Level.WARN, message, null);
        }

        @Override
        public void warn(Object message, Throwable exception) {
            this.log(Level.WARN, message, exception);
        }

        @Override
        public void info(Object message) {
            this.log(Level.INFO, message, null);
        }

        @Override
        public void info(Object message, Throwable exception) {
            this.log(Level.INFO, message, exception);
        }

        @Override
        public void debug(Object message) {
            this.log(Level.DEBUG, message, null);
        }

        @Override
        public void debug(Object message, Throwable exception) {
            this.log(Level.DEBUG, message, exception);
        }

        @Override
        public void trace(Object message) {
            this.log(Level.TRACE, message, null);
        }

        @Override
        public void trace(Object message, Throwable exception) {
            this.log(Level.TRACE, message, exception);
        }

        private void log(Level level, Object message, Throwable exception) {
            if (message instanceof String) {
                if (exception != null) {
                    this.logger.logIfEnabled(FQCN, level, (Marker)null, (String)message, exception);
                } else {
                    this.logger.logIfEnabled(FQCN, level, null, (String)message);
                }
            } else {
                this.logger.logIfEnabled(FQCN, level, null, message, exception);
            }
        }
    }

    private static class JavaUtilAdapter {
        private JavaUtilAdapter() {
        }

        public static Log createLog(String name) {
            return new JavaUtilLog(name);
        }
    }

    private static class Slf4jAdapter {
        private Slf4jAdapter() {
        }

        public static Log createLocationAwareLog(String name) {
            Logger logger = LoggerFactory.getLogger(name);
            return logger instanceof LocationAwareLogger ? new Slf4jLocationAwareLog((LocationAwareLogger)logger) : new Slf4jLog<Logger>(logger);
        }

        public static Log createLog(String name) {
            return new Slf4jLog<Logger>(LoggerFactory.getLogger(name));
        }
    }

    private static class Log4jAdapter {
        private Log4jAdapter() {
        }

        public static Log createLog(String name) {
            return new Log4jLog(name);
        }
    }

    private static enum LogApi {
        LOG4J,
        SLF4J_LAL,
        SLF4J,
        JUL;

    }
}


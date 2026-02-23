/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.springframework.core.log;

import java.util.function.Function;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

public abstract class LogFormatUtils {
    private static final Pattern NEWLINE_PATTERN = Pattern.compile("[\n\r]");
    private static final Pattern CONTROL_CHARACTER_PATTERN = Pattern.compile("\\p{Cc}");

    public static String formatValue(@Nullable Object value, boolean limitLength) {
        return LogFormatUtils.formatValue(value, limitLength ? 100 : -1, limitLength);
    }

    public static String formatValue(@Nullable Object value, int maxLength, boolean replaceNewlinesAndControlCharacters) {
        String result;
        if (value == null) {
            return "";
        }
        try {
            result = ObjectUtils.nullSafeToString(value);
        } catch (Throwable ex) {
            result = ObjectUtils.nullSafeToString(ex);
        }
        if (maxLength != -1) {
            String string = result = result.length() > maxLength ? result.substring(0, maxLength) + " (truncated)..." : result;
        }
        if (replaceNewlinesAndControlCharacters) {
            result = NEWLINE_PATTERN.matcher(result).replaceAll("<EOL>");
            result = CONTROL_CHARACTER_PATTERN.matcher(result).replaceAll("?");
        }
        if (value instanceof CharSequence) {
            result = "\"" + result + "\"";
        }
        return result;
    }

    public static void traceDebug(Log logger, Function<Boolean, String> messageFactory) {
        if (logger.isDebugEnabled()) {
            boolean traceEnabled = logger.isTraceEnabled();
            String logMessage = messageFactory.apply(traceEnabled);
            if (traceEnabled) {
                logger.trace(logMessage);
            } else {
                logger.debug(logMessage);
            }
        }
    }
}


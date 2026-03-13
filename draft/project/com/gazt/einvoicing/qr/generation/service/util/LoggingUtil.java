/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.gazt.einvoicing.qr.generation.service.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggingUtil {
    private static final String APPLICATION = "GAZT-E-INVOICING";
    private static Logger log = LogManager.getLogger(LoggingUtil.class);

    private LoggingUtil() {
    }

    public static void logInfo(String source, String message) {
        log.info("{} : {} : " + message, (Object)APPLICATION, (Object)source);
    }

    public static void logInfo(String source, String message, Object ... arguments) {
        log.info("{} : {} : " + String.format(message, arguments), (Object)APPLICATION, (Object)source);
    }

    public static void logDebug(String source, String message, Object ... arguments) {
        log.debug("{}: {} : " + String.format(message, arguments), (Object)APPLICATION, (Object)source);
    }

    public static void logError(String source, String message, Object ... arguments) {
        log.error("{}: {} : " + String.format(message, arguments), (Object)APPLICATION, (Object)source);
    }

    public static void logError(String message, Object ... arguments) {
        log.error("{}: REST : " + String.format(message, arguments), (Object)APPLICATION);
    }
}


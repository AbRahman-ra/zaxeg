/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.xml.security.Init;

public class I18n {
    public static final String NOT_INITIALIZED_MSG = "You must initialize the xml-security library correctly before you use it. Call the static method \"org.apache.xml.security.Init.init();\" to do that before you use any functionality from that library.";
    private static ResourceBundle resourceBundle;
    private static boolean alreadyInitialized;

    private I18n() {
    }

    public static String translate(String message, Object[] args) {
        return I18n.getExceptionMessage(message, args);
    }

    public static String translate(String message) {
        return I18n.getExceptionMessage(message);
    }

    public static String getExceptionMessage(String msgID) {
        try {
            return resourceBundle.getString(msgID);
        } catch (Throwable t) {
            if (Init.isInitialized()) {
                return "No message with ID \"" + msgID + "\" found in resource bundle \"" + "org/apache/xml/security/resource/xmlsecurity" + "\"";
            }
            return NOT_INITIALIZED_MSG;
        }
    }

    public static String getExceptionMessage(String msgID, Exception originalException) {
        try {
            Object[] exArgs = new Object[]{originalException.getMessage()};
            return MessageFormat.format(resourceBundle.getString(msgID), exArgs);
        } catch (Throwable t) {
            if (Init.isInitialized()) {
                return "No message with ID \"" + msgID + "\" found in resource bundle \"" + "org/apache/xml/security/resource/xmlsecurity" + "\". Original Exception was a " + originalException.getClass().getName() + " and message " + originalException.getMessage();
            }
            return NOT_INITIALIZED_MSG;
        }
    }

    public static String getExceptionMessage(String msgID, Object[] exArgs) {
        try {
            return MessageFormat.format(resourceBundle.getString(msgID), exArgs);
        } catch (Throwable t) {
            if (Init.isInitialized()) {
                return "No message with ID \"" + msgID + "\" found in resource bundle \"" + "org/apache/xml/security/resource/xmlsecurity" + "\"";
            }
            return NOT_INITIALIZED_MSG;
        }
    }

    public static synchronized void init(String languageCode, String countryCode) {
        if (alreadyInitialized) {
            return;
        }
        resourceBundle = ResourceBundle.getBundle("org/apache/xml/security/resource/xmlsecurity", new Locale(languageCode, countryCode));
        alreadyInitialized = true;
    }

    static {
        alreadyInitialized = false;
    }
}


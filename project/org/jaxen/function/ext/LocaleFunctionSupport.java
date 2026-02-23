/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.function.ext;

import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import org.jaxen.Function;
import org.jaxen.Navigator;
import org.jaxen.function.StringFunction;

public abstract class LocaleFunctionSupport
implements Function {
    protected Locale getLocale(Object value, Navigator navigator) {
        if (value instanceof Locale) {
            return (Locale)value;
        }
        if (value instanceof List) {
            List list = (List)value;
            if (!list.isEmpty()) {
                return this.getLocale(list.get(0), navigator);
            }
        } else {
            String text = StringFunction.evaluate(value, navigator);
            if (text != null && text.length() > 0) {
                return this.findLocale(text);
            }
        }
        return null;
    }

    protected Locale findLocale(String localeText) {
        StringTokenizer tokens = new StringTokenizer(localeText, "-");
        if (tokens.hasMoreTokens()) {
            String language = tokens.nextToken();
            if (!tokens.hasMoreTokens()) {
                return this.findLocaleForLanguage(language);
            }
            String country = tokens.nextToken();
            if (!tokens.hasMoreTokens()) {
                return new Locale(language, country);
            }
            String variant = tokens.nextToken();
            return new Locale(language, country, variant);
        }
        return null;
    }

    protected Locale findLocaleForLanguage(String language) {
        Locale[] locales = Locale.getAvailableLocales();
        int size = locales.length;
        for (int i = 0; i < size; ++i) {
            String variant;
            String country;
            Locale locale = locales[i];
            if (!language.equals(locale.getLanguage()) || (country = locale.getCountry()) != null && country.length() != 0 || (variant = locale.getVariant()) != null && variant.length() != 0) continue;
            return locale;
        }
        return null;
    }
}


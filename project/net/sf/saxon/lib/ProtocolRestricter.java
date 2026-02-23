/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class ProtocolRestricter
implements Predicate<URI> {
    private String rule;
    private List<Predicate<URI>> permitted = new ArrayList<Predicate<URI>>();

    public static Predicate<URI> make(String value) {
        Objects.requireNonNull(value);
        value = value.trim();
        if (value.equals("all")) {
            return uri -> true;
        }
        return new ProtocolRestricter(value);
    }

    private ProtocolRestricter(String value) {
        String[] tokens;
        this.rule = value;
        for (String token : tokens = value.split(",\\s*")) {
            if (token.startsWith("jar:") && token.length() > 4) {
                String subScheme = token.substring(4).toLowerCase();
                this.permitted.add(uri -> uri.getScheme().equals("jar") && uri.getSchemeSpecificPart().toLowerCase().startsWith(subScheme));
                continue;
            }
            this.permitted.add(uri -> uri.getScheme().equals(token));
        }
    }

    @Override
    public boolean test(URI uri) {
        for (Predicate<URI> pred : this.permitted) {
            if (!pred.test(uri)) continue;
            return true;
        }
        return false;
    }

    public String toString() {
        return this.rule;
    }
}


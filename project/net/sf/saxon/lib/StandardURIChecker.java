/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.net.URI;
import java.net.URISyntaxException;
import net.sf.saxon.expr.sort.LRUCache;
import net.sf.saxon.functions.IriToUri;
import net.sf.saxon.lib.URIChecker;
import net.sf.saxon.value.Whitespace;

public class StandardURIChecker
implements URIChecker {
    private static StandardURIChecker THE_INSTANCE = new StandardURIChecker();
    private static ThreadLocal<LRUCache<CharSequence, Boolean>> caches = new ThreadLocal();

    public static StandardURIChecker getInstance() {
        return THE_INSTANCE;
    }

    protected StandardURIChecker() {
    }

    @Override
    public boolean isValidURI(CharSequence value) {
        LRUCache<CharSequence, Boolean> cache = caches.get();
        if (cache == null) {
            cache = new LRUCache(50);
            caches.set(cache);
        }
        if (cache.get(value) != null) {
            return true;
        }
        String sv = Whitespace.trim(value);
        if (sv.isEmpty()) {
            return true;
        }
        try {
            new URI(sv);
            cache.put(value, Boolean.TRUE);
            return true;
        } catch (URISyntaxException uRISyntaxException) {
            sv = IriToUri.iriToUri(sv).toString();
            try {
                new URI(sv);
                cache.put(value, Boolean.TRUE);
                return true;
            } catch (URISyntaxException e) {
                return false;
            }
        }
    }

    public static void main(String[] args) {
        System.err.println(args[0] + " is valid? - " + StandardURIChecker.getInstance().isValidURI(args[0]));
    }
}


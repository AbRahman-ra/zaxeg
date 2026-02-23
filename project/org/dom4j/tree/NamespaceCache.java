/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.tree;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.Map;
import org.dom4j.Namespace;
import org.dom4j.tree.ConcurrentReaderHashMap;

public class NamespaceCache {
    private static final String CONCURRENTREADERHASHMAP_CLASS = "EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap";
    protected static Map cache;
    protected static Map noPrefixCache;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Namespace get(String prefix, String uri) {
        Map uriCache = this.getURICache(uri);
        WeakReference ref = (WeakReference)uriCache.get(prefix);
        Namespace answer = null;
        if (ref != null) {
            answer = (Namespace)ref.get();
        }
        if (answer == null) {
            Map map = uriCache;
            synchronized (map) {
                ref = (WeakReference)uriCache.get(prefix);
                if (ref != null) {
                    answer = (Namespace)ref.get();
                }
                if (answer == null) {
                    answer = this.createNamespace(prefix, uri);
                    uriCache.put(prefix, new WeakReference<Namespace>(answer));
                }
            }
        }
        return answer;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Namespace get(String uri) {
        WeakReference ref = (WeakReference)noPrefixCache.get(uri);
        Namespace answer = null;
        if (ref != null) {
            answer = (Namespace)ref.get();
        }
        if (answer == null) {
            Map map = noPrefixCache;
            synchronized (map) {
                ref = (WeakReference)noPrefixCache.get(uri);
                if (ref != null) {
                    answer = (Namespace)ref.get();
                }
                if (answer == null) {
                    answer = this.createNamespace("", uri);
                    noPrefixCache.put(uri, new WeakReference<Namespace>(answer));
                }
            }
        }
        return answer;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Map getURICache(String uri) {
        Map answer = (Map)cache.get(uri);
        if (answer == null) {
            Map map = cache;
            synchronized (map) {
                answer = (Map)cache.get(uri);
                if (answer == null) {
                    answer = new ConcurrentReaderHashMap();
                    cache.put(uri, answer);
                }
            }
        }
        return answer;
    }

    protected Namespace createNamespace(String prefix, String uri) {
        return new Namespace(prefix, uri);
    }

    static {
        try {
            Class<?> clazz = Class.forName("java.util.concurrent.ConcurrentHashMap");
            Constructor<?> construct = clazz.getConstructor(Integer.TYPE, Float.TYPE, Integer.TYPE);
            cache = (Map)construct.newInstance(new Integer(11), new Float(0.75f), new Integer(1));
            noPrefixCache = (Map)construct.newInstance(new Integer(11), new Float(0.75f), new Integer(1));
        } catch (Throwable t1) {
            try {
                Class<?> clazz = Class.forName(CONCURRENTREADERHASHMAP_CLASS);
                cache = (Map)clazz.newInstance();
                noPrefixCache = (Map)clazz.newInstance();
            } catch (Throwable t2) {
                cache = new ConcurrentReaderHashMap();
                noPrefixCache = new ConcurrentReaderHashMap();
            }
        }
    }
}


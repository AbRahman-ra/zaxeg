/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.util;

import java.lang.ref.WeakReference;
import org.dom4j.util.SingletonStrategy;

public class PerThreadSingleton
implements SingletonStrategy {
    private String singletonClassName = null;
    private ThreadLocal perThreadCache = new ThreadLocal();

    public void reset() {
        this.perThreadCache = new ThreadLocal();
    }

    public Object instance() {
        Object singletonInstancePerThread = null;
        WeakReference ref = (WeakReference)this.perThreadCache.get();
        if (ref == null || ref.get() == null) {
            Class<?> clazz = null;
            try {
                clazz = Thread.currentThread().getContextClassLoader().loadClass(this.singletonClassName);
                singletonInstancePerThread = clazz.newInstance();
            } catch (Exception ignore) {
                try {
                    clazz = Class.forName(this.singletonClassName);
                    singletonInstancePerThread = clazz.newInstance();
                } catch (Exception ignore2) {
                    // empty catch block
                }
            }
            this.perThreadCache.set(new WeakReference<Object>(singletonInstancePerThread));
        } else {
            singletonInstancePerThread = ref.get();
        }
        return singletonInstancePerThread;
    }

    public void setSingletonClassName(String singletonClassName) {
        this.singletonClassName = singletonClassName;
    }
}


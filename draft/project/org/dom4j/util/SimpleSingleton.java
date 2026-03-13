/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.util;

import org.dom4j.util.SingletonStrategy;

public class SimpleSingleton
implements SingletonStrategy {
    private String singletonClassName = null;
    private Object singletonInstance = null;

    public Object instance() {
        return this.singletonInstance;
    }

    public void reset() {
        if (this.singletonClassName != null) {
            Class<?> clazz = null;
            try {
                clazz = Thread.currentThread().getContextClassLoader().loadClass(this.singletonClassName);
                this.singletonInstance = clazz.newInstance();
            } catch (Exception ignore) {
                try {
                    clazz = Class.forName(this.singletonClassName);
                    this.singletonInstance = clazz.newInstance();
                } catch (Exception exception) {
                    // empty catch block
                }
            }
        }
    }

    public void setSingletonClassName(String singletonClassName) {
        this.singletonClassName = singletonClassName;
        this.reset();
    }
}


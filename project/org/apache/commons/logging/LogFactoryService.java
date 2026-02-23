/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.logging;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogAdapter;
import org.apache.commons.logging.LogFactory;

@Deprecated
public class LogFactoryService
extends LogFactory {
    private final Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();

    public Log getInstance(Class<?> clazz) {
        return this.getInstance(clazz.getName());
    }

    @Override
    public Log getInstance(String name) {
        return LogAdapter.createLog(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (value != null) {
            this.attributes.put(name, value);
        } else {
            this.attributes.remove(name);
        }
    }

    @Override
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    @Override
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    @Override
    public String[] getAttributeNames() {
        return this.attributes.keySet().toArray(new String[0]);
    }

    @Override
    public void release() {
    }
}


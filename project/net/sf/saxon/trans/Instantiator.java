/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import net.sf.saxon.Configuration;
import net.sf.saxon.trans.Maker;
import net.sf.saxon.trans.XPathException;

public class Instantiator<T>
implements Maker<T> {
    private String className;
    private Configuration config;

    public Instantiator(String className, Configuration config) {
        this.className = className;
        this.config = config;
    }

    @Override
    public T make() throws XPathException {
        Object o = this.config.getInstance(this.className, null);
        try {
            return (T)o;
        } catch (ClassCastException e) {
            throw new XPathException("Instantiating " + this.className + " produced an instance of incompatible class " + o.getClass());
        }
    }
}


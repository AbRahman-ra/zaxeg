/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen;

import java.util.Iterator;
import org.jaxen.Navigator;
import org.jaxen.UnsupportedAxisException;

public interface NamedAccessNavigator
extends Navigator {
    public Iterator getChildAxisIterator(Object var1, String var2, String var3, String var4) throws UnsupportedAxisException;

    public Iterator getAttributeAxisIterator(Object var1, String var2, String var3, String var4) throws UnsupportedAxisException;
}


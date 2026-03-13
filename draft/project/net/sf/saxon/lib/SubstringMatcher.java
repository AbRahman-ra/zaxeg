/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import net.sf.saxon.lib.StringCollator;

public interface SubstringMatcher
extends StringCollator {
    public boolean contains(String var1, String var2);

    public boolean startsWith(String var1, String var2);

    public boolean endsWith(String var1, String var2);

    public String substringBefore(String var1, String var2);

    public String substringAfter(String var1, String var2);
}


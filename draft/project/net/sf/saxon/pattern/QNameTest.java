/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import net.sf.saxon.om.StructuredQName;

public interface QNameTest {
    public boolean matches(StructuredQName var1);

    public String exportQNameTest();

    public String generateJavaScriptNameTest(int var1);
}


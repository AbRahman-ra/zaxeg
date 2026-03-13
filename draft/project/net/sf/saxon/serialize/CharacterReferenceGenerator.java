/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.io.IOException;
import java.io.Writer;

public interface CharacterReferenceGenerator {
    public void outputCharacterReference(int var1, Writer var2) throws IOException;
}


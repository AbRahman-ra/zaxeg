/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.io.IOException;
import java.io.Writer;
import net.sf.saxon.serialize.CharacterReferenceGenerator;

public class HexCharacterReferenceGenerator
implements CharacterReferenceGenerator {
    public static final HexCharacterReferenceGenerator THE_INSTANCE = new HexCharacterReferenceGenerator();

    private HexCharacterReferenceGenerator() {
    }

    @Override
    public void outputCharacterReference(int charval, Writer writer) throws IOException {
        writer.write("&#x");
        writer.write(Integer.toHexString(charval));
        writer.write(59);
    }
}


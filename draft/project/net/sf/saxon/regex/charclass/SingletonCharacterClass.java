/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex.charclass;

import net.sf.saxon.regex.charclass.CharacterClass;
import net.sf.saxon.z.IntSet;
import net.sf.saxon.z.IntSingletonSet;

public class SingletonCharacterClass
implements CharacterClass {
    private int codepoint;

    public SingletonCharacterClass(int codepoint) {
        this.codepoint = codepoint;
    }

    @Override
    public boolean test(int value) {
        return value == this.codepoint;
    }

    @Override
    public boolean isDisjoint(CharacterClass other) {
        return !other.test(this.codepoint);
    }

    public int getCodepoint() {
        return this.codepoint;
    }

    @Override
    public IntSet getIntSet() {
        return new IntSingletonSet(this.codepoint);
    }
}


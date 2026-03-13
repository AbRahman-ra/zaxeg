/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex.charclass;

import net.sf.saxon.regex.charclass.CharacterClass;
import net.sf.saxon.regex.charclass.InverseCharacterClass;
import net.sf.saxon.z.IntEmptySet;
import net.sf.saxon.z.IntSet;

public class EmptyCharacterClass
implements CharacterClass {
    private static final EmptyCharacterClass THE_INSTANCE = new EmptyCharacterClass();
    private static final InverseCharacterClass COMPLEMENT = new InverseCharacterClass(THE_INSTANCE);

    public static EmptyCharacterClass getInstance() {
        return THE_INSTANCE;
    }

    public static CharacterClass getComplement() {
        return COMPLEMENT;
    }

    private EmptyCharacterClass() {
    }

    @Override
    public boolean test(int value) {
        return false;
    }

    @Override
    public boolean isDisjoint(CharacterClass other) {
        return true;
    }

    @Override
    public IntSet getIntSet() {
        return IntEmptySet.getInstance();
    }
}


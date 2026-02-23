/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex.charclass;

import net.sf.saxon.regex.charclass.CharacterClass;
import net.sf.saxon.z.IntComplementSet;
import net.sf.saxon.z.IntSet;

public class InverseCharacterClass
implements CharacterClass {
    private CharacterClass complement;

    public InverseCharacterClass(CharacterClass complement) {
        this.complement = complement;
    }

    public CharacterClass getComplement() {
        return this.complement;
    }

    @Override
    public boolean test(int value) {
        return !this.complement.test(value);
    }

    @Override
    public boolean isDisjoint(CharacterClass other) {
        return other == this.complement;
    }

    @Override
    public IntSet getIntSet() {
        IntSet comp = this.complement.getIntSet();
        return comp == null ? null : new IntComplementSet(this.complement.getIntSet());
    }
}


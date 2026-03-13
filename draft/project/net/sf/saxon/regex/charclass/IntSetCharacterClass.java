/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex.charclass;

import net.sf.saxon.regex.charclass.CharacterClass;
import net.sf.saxon.regex.charclass.InverseCharacterClass;
import net.sf.saxon.z.IntSet;

public class IntSetCharacterClass
implements CharacterClass {
    private IntSet intSet;

    public IntSetCharacterClass(IntSet intSet) {
        this.intSet = intSet;
    }

    @Override
    public IntSet getIntSet() {
        return this.intSet;
    }

    @Override
    public boolean test(int value) {
        return this.intSet.contains(value);
    }

    @Override
    public boolean isDisjoint(CharacterClass other) {
        if (other instanceof IntSetCharacterClass) {
            return this.intSet.intersect(((IntSetCharacterClass)other).intSet).isEmpty();
        }
        if (other instanceof InverseCharacterClass) {
            return other.isDisjoint(this);
        }
        return false;
    }
}


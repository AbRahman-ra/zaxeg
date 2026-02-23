/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex.charclass;

import java.util.function.IntPredicate;
import net.sf.saxon.regex.charclass.CharacterClass;
import net.sf.saxon.regex.charclass.InverseCharacterClass;
import net.sf.saxon.z.IntSet;

public class PredicateCharacterClass
implements CharacterClass {
    private IntPredicate predicate;

    public PredicateCharacterClass(IntPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(int value) {
        return this.predicate.test(value);
    }

    @Override
    public boolean isDisjoint(CharacterClass other) {
        return other instanceof InverseCharacterClass && other.isDisjoint(this);
    }

    @Override
    public IntSet getIntSet() {
        return null;
    }
}


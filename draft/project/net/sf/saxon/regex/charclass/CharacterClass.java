/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex.charclass;

import java.util.function.IntPredicate;
import net.sf.saxon.z.IntSet;

public interface CharacterClass
extends IntPredicate {
    public boolean isDisjoint(CharacterClass var1);

    public IntSet getIntSet();
}


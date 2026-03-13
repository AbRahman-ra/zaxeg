/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import java.util.Optional;
import net.sf.saxon.om.Genre;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.SequenceType;

public interface ItemType {
    public Genre getGenre();

    public boolean isAtomicType();

    public boolean isPlainType();

    default public boolean isTrueItemType() {
        return true;
    }

    public boolean matches(Item var1, TypeHierarchy var2) throws XPathException;

    public ItemType getPrimitiveItemType();

    public int getPrimitiveType();

    public UType getUType();

    public double getDefaultPriority();

    default public double getNormalizedDefaultPriority() {
        return (this.getDefaultPriority() + 1.0) / 2.0;
    }

    public PlainType getAtomizedItemType();

    public boolean isAtomizable(TypeHierarchy var1);

    public String getBasicAlphaCode();

    default public String getFullAlphaCode() {
        return this.getBasicAlphaCode();
    }

    default public String toExportString() {
        return this.toString();
    }

    public String toString();

    default public Optional<String> explainMismatch(Item item, TypeHierarchy th) {
        return Optional.empty();
    }

    public static interface WithSequenceTypeCache
    extends ItemType {
        public SequenceType one();

        public SequenceType zeroOrOne();

        public SequenceType oneOrMore();

        public SequenceType zeroOrMore();
    }
}


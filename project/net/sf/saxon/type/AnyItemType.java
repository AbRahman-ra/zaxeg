/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import net.sf.saxon.om.Genre;
import net.sf.saxon.om.Item;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.SequenceType;

public class AnyItemType
implements ItemType.WithSequenceTypeCache {
    private SequenceType _one;
    private SequenceType _oneOrMore;
    private SequenceType _zeroOrOne;
    private SequenceType _zeroOrMore;
    private static AnyItemType theInstance = new AnyItemType();

    private AnyItemType() {
    }

    public static AnyItemType getInstance() {
        return theInstance;
    }

    @Override
    public Genre getGenre() {
        return Genre.ANY;
    }

    @Override
    public UType getUType() {
        return UType.ANY;
    }

    @Override
    public boolean isAtomicType() {
        return false;
    }

    @Override
    public String getBasicAlphaCode() {
        return "";
    }

    @Override
    public boolean isPlainType() {
        return false;
    }

    @Override
    public boolean matches(Item item, TypeHierarchy th) {
        return true;
    }

    @Override
    public ItemType getPrimitiveItemType() {
        return this;
    }

    @Override
    public int getPrimitiveType() {
        return 88;
    }

    @Override
    public AtomicType getAtomizedItemType() {
        return BuiltInAtomicType.ANY_ATOMIC;
    }

    @Override
    public boolean isAtomizable(TypeHierarchy th) {
        return true;
    }

    @Override
    public double getDefaultPriority() {
        return -2.0;
    }

    @Override
    public String toString() {
        return "item()";
    }

    public int hashCode() {
        return "AnyItemType".hashCode();
    }

    @Override
    public SequenceType one() {
        if (this._one == null) {
            this._one = new SequenceType(this, 16384);
        }
        return this._one;
    }

    @Override
    public SequenceType zeroOrOne() {
        if (this._zeroOrOne == null) {
            this._zeroOrOne = new SequenceType(this, 24576);
        }
        return this._zeroOrOne;
    }

    @Override
    public SequenceType oneOrMore() {
        if (this._oneOrMore == null) {
            this._oneOrMore = new SequenceType(this, 49152);
        }
        return this._oneOrMore;
    }

    @Override
    public SequenceType zeroOrMore() {
        if (this._zeroOrMore == null) {
            this._zeroOrMore = new SequenceType(this, 57344);
        }
        return this._zeroOrMore;
    }
}


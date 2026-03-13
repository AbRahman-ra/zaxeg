/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import java.util.Optional;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Genre;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.CastingTarget;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.MissingComponentException;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.SequenceType;

public interface UnionType
extends ItemType,
CastingTarget {
    public StructuredQName getTypeName();

    default public StructuredQName getStructuredQName() {
        return this.getTypeName();
    }

    public boolean containsListType() throws MissingComponentException;

    public Iterable<? extends PlainType> getPlainMemberTypes() throws MissingComponentException;

    public SequenceType getResultTypeOfCast();

    public AtomicSequence getTypedValue(CharSequence var1, NamespaceResolver var2, ConversionRules var3) throws ValidationException;

    public ValidationFailure checkAgainstFacets(AtomicValue var1, ConversionRules var2);

    @Override
    default public Optional<String> explainMismatch(Item item, TypeHierarchy th) {
        if (item.getGenre() == Genre.ATOMIC) {
            FastStringBuffer message = new FastStringBuffer(256);
            message.append("The required type is a union type allowing any of ");
            String punctuation = "(";
            try {
                for (PlainType plainType : this.getPlainMemberTypes()) {
                    message.append(punctuation);
                    punctuation = ", ";
                    message.append(plainType.getTypeName().getDisplayName());
                }
            } catch (MissingComponentException e) {
                message.append("*member types unobtainable*");
            }
            message.append("), but the supplied type ");
            message.append(((AtomicValue)item).getItemType().getDisplayName());
            message.append(" is not any of these");
            return Optional.of(message.toString());
        }
        return Optional.empty();
    }

    default public String getDescription() {
        if (this instanceof SimpleType) {
            return ((SimpleType)((Object)this)).getDescription();
        }
        return this.toString();
    }
}


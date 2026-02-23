/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import java.util.Optional;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.om.Genre;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.CastingTarget;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;

public interface AtomicType
extends SimpleType,
PlainType,
CastingTarget {
    @Override
    default public Genre getGenre() {
        return Genre.ATOMIC;
    }

    public ValidationFailure validate(AtomicValue var1, CharSequence var2, ConversionRules var3);

    public boolean isOrdered(boolean var1);

    public boolean isAbstract();

    public boolean isPrimitiveType();

    @Override
    public boolean isIdType();

    @Override
    public boolean isIdRefType();

    @Override
    public boolean isBuiltInType();

    @Override
    public StructuredQName getTypeName();

    public StringConverter getStringConverter(ConversionRules var1);

    @Override
    default public Optional<String> explainMismatch(Item item, TypeHierarchy th) {
        if (item instanceof AtomicValue) {
            return Optional.of("The supplied value is of type " + ((AtomicValue)item).getItemType());
        }
        return Optional.of("The supplied value is " + item.getGenre().getDescription());
    }

    @Override
    default public double getDefaultPriority() {
        if (this == BuiltInAtomicType.ANY_ATOMIC) {
            return 0.0;
        }
        double factor = 1.0;
        SchemaType at = this;
        do {
            factor *= 0.5;
        } while ((at = at.getBaseType()) != BuiltInAtomicType.ANY_ATOMIC);
        return 1.0 - factor;
    }
}


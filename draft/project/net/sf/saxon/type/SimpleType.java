/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.type.ValidationFailure;

public interface SimpleType
extends SchemaType {
    @Override
    public boolean isAtomicType();

    public boolean isListType();

    public boolean isUnionType();

    public boolean isBuiltInType();

    public SchemaType getBuiltInBaseType();

    public AtomicSequence getTypedValue(CharSequence var1, NamespaceResolver var2, ConversionRules var3) throws ValidationException;

    public ValidationFailure validateContent(CharSequence var1, NamespaceResolver var2, ConversionRules var3);

    public boolean isNamespaceSensitive();

    public int getWhitespaceAction();

    public CharSequence preprocess(CharSequence var1) throws ValidationException;

    public CharSequence postprocess(CharSequence var1) throws ValidationException;
}


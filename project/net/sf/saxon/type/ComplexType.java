/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.type.MissingComponentException;
import net.sf.saxon.type.SchemaException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.z.IntHashSet;

public interface ComplexType
extends SchemaType {
    public static final int VARIETY_EMPTY = 0;
    public static final int VARIETY_SIMPLE = 1;
    public static final int VARIETY_ELEMENT_ONLY = 2;
    public static final int VARIETY_MIXED = 3;
    public static final int OPEN_CONTENT_ABSENT = 0;
    public static final int OPEN_CONTENT_NONE = 1;
    public static final int OPEN_CONTENT_INTERLEAVE = 2;
    public static final int OPEN_CONTENT_SUFFIX = 3;

    public int getVariety();

    public boolean isAbstract();

    public boolean isComplexContent();

    public boolean isSimpleContent();

    public boolean isAllContent();

    public SimpleType getSimpleContentType() throws MissingComponentException;

    public boolean isRestricted();

    public boolean isEmptyContent();

    public boolean isEmptiable() throws SchemaException;

    public boolean isMixedContent();

    public SchemaType getElementParticleType(int var1, boolean var2) throws MissingComponentException;

    public int getElementParticleCardinality(int var1, boolean var2) throws MissingComponentException;

    public SimpleType getAttributeUseType(StructuredQName var1) throws SchemaException;

    public int getAttributeUseCardinality(StructuredQName var1) throws SchemaException;

    public boolean allowsAttributes() throws MissingComponentException;

    public void gatherAllPermittedChildren(IntHashSet var1, boolean var2) throws SchemaException;

    public void gatherAllPermittedDescendants(IntHashSet var1) throws SchemaException;

    public SchemaType getDescendantElementType(int var1) throws SchemaException;

    public int getDescendantElementCardinality(int var1) throws SchemaException;

    public boolean containsElementWildcard() throws MissingComponentException;

    public boolean hasAssertions();
}


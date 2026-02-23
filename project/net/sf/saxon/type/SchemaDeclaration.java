/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.type.MissingComponentException;
import net.sf.saxon.type.SchemaType;

public interface SchemaDeclaration {
    public int getFingerprint();

    public StructuredQName getComponentName();

    public SchemaType getType() throws MissingComponentException;

    public NodeTest makeSchemaNodeTest() throws MissingComponentException;

    public boolean isNillable();

    public boolean isAbstract();

    public boolean hasTypeAlternatives();
}


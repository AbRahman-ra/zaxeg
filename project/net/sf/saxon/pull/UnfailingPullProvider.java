/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pull;

import java.util.List;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.pull.PullProvider;
import net.sf.saxon.pull.UnparsedEntity;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.AtomicValue;

public interface UnfailingPullProvider
extends PullProvider {
    @Override
    public PullProvider.Event next() throws XPathException;

    @Override
    public PullProvider.Event current();

    @Override
    public AttributeMap getAttributes();

    @Override
    public NamespaceBinding[] getNamespaceDeclarations();

    @Override
    public PullProvider.Event skipToMatchingEnd();

    @Override
    public void close();

    @Override
    public NodeName getNodeName();

    @Override
    public CharSequence getStringValue() throws XPathException;

    @Override
    public SchemaType getSchemaType();

    @Override
    public AtomicValue getAtomicValue();

    @Override
    public Location getSourceLocator();

    @Override
    public List<UnparsedEntity> getUnparsedEntities();
}


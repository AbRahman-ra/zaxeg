/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pull;

import java.util.List;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.pull.UnparsedEntity;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.AtomicValue;

public interface PullProvider {
    public void setPipelineConfiguration(PipelineConfiguration var1);

    public PipelineConfiguration getPipelineConfiguration();

    public Event next() throws XPathException;

    public Event current();

    public AttributeMap getAttributes() throws XPathException;

    public NamespaceBinding[] getNamespaceDeclarations() throws XPathException;

    public Event skipToMatchingEnd() throws XPathException;

    public void close();

    public NodeName getNodeName();

    public CharSequence getStringValue() throws XPathException;

    public SchemaType getSchemaType();

    public AtomicValue getAtomicValue();

    public Location getSourceLocator();

    public List<UnparsedEntity> getUnparsedEntities();

    public static enum Event {
        START_OF_INPUT,
        ATOMIC_VALUE,
        START_DOCUMENT,
        END_DOCUMENT,
        START_ELEMENT,
        END_ELEMENT,
        ATTRIBUTE,
        NAMESPACE,
        TEXT,
        COMMENT,
        PROCESSING_INSTRUCTION,
        END_OF_INPUT;

    }
}


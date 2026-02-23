/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pull;

import java.util.List;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.pull.PullProvider;
import net.sf.saxon.pull.UnparsedEntity;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.AtomicValue;

public class PullFilter
implements PullProvider {
    private PullProvider base;
    private PipelineConfiguration pipe;
    protected PullProvider.Event currentEvent;

    public PullFilter(PullProvider base) {
        this.base = base;
        if (base.getPipelineConfiguration() != null) {
            this.setPipelineConfiguration(base.getPipelineConfiguration());
        }
        this.currentEvent = base.current();
    }

    @Override
    public void setPipelineConfiguration(PipelineConfiguration pipe) {
        this.pipe = pipe;
        this.base.setPipelineConfiguration(pipe);
    }

    @Override
    public PipelineConfiguration getPipelineConfiguration() {
        return this.pipe;
    }

    public final NamePool getNamePool() {
        return this.getPipelineConfiguration().getConfiguration().getNamePool();
    }

    public PullProvider getUnderlyingProvider() {
        return this.base;
    }

    @Override
    public PullProvider.Event next() throws XPathException {
        return this.base.next();
    }

    @Override
    public PullProvider.Event current() {
        return this.currentEvent;
    }

    @Override
    public AttributeMap getAttributes() throws XPathException {
        return this.base.getAttributes();
    }

    @Override
    public NamespaceBinding[] getNamespaceDeclarations() throws XPathException {
        return this.base.getNamespaceDeclarations();
    }

    @Override
    public PullProvider.Event skipToMatchingEnd() throws XPathException {
        return this.base.skipToMatchingEnd();
    }

    @Override
    public void close() {
        this.base.close();
    }

    @Override
    public NodeName getNodeName() {
        return this.base.getNodeName();
    }

    @Override
    public CharSequence getStringValue() throws XPathException {
        return this.base.getStringValue();
    }

    @Override
    public AtomicValue getAtomicValue() {
        return this.base.getAtomicValue();
    }

    @Override
    public SchemaType getSchemaType() {
        return this.base.getSchemaType();
    }

    @Override
    public Location getSourceLocator() {
        return this.base.getSourceLocator();
    }

    @Override
    public List<UnparsedEntity> getUnparsedEntities() {
        return this.base.getUnparsedEntities();
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.BuilderMonitor;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trans.CommandLineOptions;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyDocumentImpl;

public abstract class Builder
implements Receiver {
    public static final int UNSPECIFIED_TREE_MODEL = -1;
    public static final int LINKED_TREE = 0;
    public static final int TINY_TREE = 1;
    public static final int TINY_TREE_CONDENSED = 2;
    public static final int JDOM_TREE = 3;
    public static final int JDOM2_TREE = 4;
    public static final int AXIOM_TREE = 5;
    public static final int DOMINO_TREE = 6;
    public static final int MUTABLE_LINKED_TREE = 7;
    protected PipelineConfiguration pipe;
    protected Configuration config;
    protected NamePool namePool;
    protected String systemId;
    protected String baseURI;
    protected boolean uniformBaseURI = true;
    protected NodeInfo currentRoot;
    protected boolean lineNumbering = false;
    protected boolean useEventLocation = true;
    protected boolean started = false;
    protected boolean timing = false;
    protected boolean open = false;
    private long startTime;

    public Builder() {
    }

    public Builder(PipelineConfiguration pipe) {
        this.pipe = pipe;
        this.config = pipe.getConfiguration();
        this.lineNumbering = this.config.isLineNumbering();
        this.namePool = this.config.getNamePool();
    }

    @Override
    public void setPipelineConfiguration(PipelineConfiguration pipe) {
        this.pipe = pipe;
        this.config = pipe.getConfiguration();
        this.lineNumbering = this.lineNumbering || this.config.isLineNumbering();
        this.namePool = this.config.getNamePool();
    }

    @Override
    public PipelineConfiguration getPipelineConfiguration() {
        return this.pipe;
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    public BuilderMonitor getBuilderMonitor() {
        return null;
    }

    public void setUseEventLocation(boolean useEventLocation) {
        this.useEventLocation = useEventLocation;
    }

    public boolean isUseEventLocation() {
        return this.useEventLocation;
    }

    @Override
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    @Override
    public String getSystemId() {
        return this.systemId;
    }

    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;
    }

    public String getBaseURI() {
        return this.baseURI;
    }

    public void setLineNumbering(boolean lineNumbering) {
        this.lineNumbering = lineNumbering;
    }

    public void setTiming(boolean on) {
        this.timing = on;
    }

    public boolean isTiming() {
        return this.timing;
    }

    @Override
    public void open() {
        if (this.timing && !this.open) {
            String sysId = this.getSystemId();
            if (sysId == null) {
                sysId = "(unknown systemId)";
            }
            this.getConfiguration().getLogger().info("Building tree for " + sysId + " using " + this.getClass());
            this.startTime = System.nanoTime();
        }
        this.open = true;
    }

    @Override
    public void close() throws XPathException {
        if (this.timing && this.open) {
            long endTime = System.nanoTime();
            this.getConfiguration().getLogger().info("Tree built in " + CommandLineOptions.showExecutionTimeNano(endTime - this.startTime));
            if (this.currentRoot instanceof TinyDocumentImpl) {
                ((TinyDocumentImpl)this.currentRoot).showSize();
            }
            this.startTime = endTime;
        }
        this.open = false;
    }

    @Override
    public boolean usesTypeAnnotations() {
        return true;
    }

    public NodeInfo getCurrentRoot() {
        return this.currentRoot;
    }

    public void reset() {
        this.systemId = null;
        this.baseURI = null;
        this.currentRoot = null;
        this.lineNumbering = false;
        this.started = false;
        this.timing = false;
        this.open = false;
    }
}


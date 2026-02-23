/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.wrapper;

import java.util.Iterator;
import net.sf.saxon.Configuration;
import net.sf.saxon.om.GenericTreeInfo;
import net.sf.saxon.tree.wrapper.VirtualCopy;

public class VirtualTreeInfo
extends GenericTreeInfo {
    private boolean copyAccumulators;

    public VirtualTreeInfo(Configuration config) {
        super(config);
    }

    public VirtualTreeInfo(Configuration config, VirtualCopy vc) {
        super(config, vc);
    }

    public void setCopyAccumulators(boolean copy) {
        this.copyAccumulators = copy;
    }

    public boolean isCopyAccumulators() {
        return this.copyAccumulators;
    }

    @Override
    public Iterator<String> getUnparsedEntityNames() {
        return ((VirtualCopy)this.getRootNode()).getOriginalNode().getTreeInfo().getUnparsedEntityNames();
    }

    @Override
    public String[] getUnparsedEntity(String name) {
        return ((VirtualCopy)this.getRootNode()).getOriginalNode().getTreeInfo().getUnparsedEntity(name);
    }
}


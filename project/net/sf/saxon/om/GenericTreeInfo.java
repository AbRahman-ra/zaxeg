/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.sf.saxon.Configuration;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.tree.util.DocumentNumberAllocator;

public class GenericTreeInfo
implements TreeInfo {
    private Configuration config;
    protected NodeInfo root;
    private String systemId;
    private Map<String, Object> userData;
    private long documentNumber = -1L;
    private SpaceStrippingRule spaceStrippingRule = NoElementsSpaceStrippingRule.getInstance();

    public GenericTreeInfo(Configuration config) {
        this.config = config;
    }

    public GenericTreeInfo(Configuration config, NodeInfo root) {
        this.config = config;
        this.setRootNode(root);
    }

    public void setConfiguration(Configuration config) {
        this.config = config;
    }

    @Override
    public Configuration getConfiguration() {
        return this.config;
    }

    public void setRootNode(NodeInfo root) {
        if (root.getParent() != null) {
            throw new IllegalArgumentException("The root node of a tree must be parentless");
        }
        this.root = root;
    }

    @Override
    public NodeInfo getRootNode() {
        return this.root;
    }

    @Override
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    @Override
    public String getSystemId() {
        return this.systemId;
    }

    public String getPublicId() {
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public long getDocumentNumber() {
        if (this.documentNumber == -1L) {
            DocumentNumberAllocator dna = this.config.getDocumentNumberAllocator();
            GenericTreeInfo genericTreeInfo = this;
            synchronized (genericTreeInfo) {
                if (this.documentNumber == -1L) {
                    this.documentNumber = dna.allocateDocumentNumber();
                }
            }
        }
        return this.documentNumber;
    }

    public synchronized void setDocumentNumber(long documentNumber) {
        this.documentNumber = documentNumber;
    }

    @Override
    public NodeInfo selectID(String id, boolean getParent) {
        return null;
    }

    @Override
    public Iterator<String> getUnparsedEntityNames() {
        List e = Collections.emptyList();
        return e.iterator();
    }

    @Override
    public String[] getUnparsedEntity(String name) {
        return null;
    }

    @Override
    public void setSpaceStrippingRule(SpaceStrippingRule rule) {
        this.spaceStrippingRule = rule;
    }

    @Override
    public SpaceStrippingRule getSpaceStrippingRule() {
        return this.spaceStrippingRule;
    }

    @Override
    public void setUserData(String key, Object value) {
        if (this.userData == null) {
            this.userData = new HashMap<String, Object>();
        }
        this.userData.put(key, value);
    }

    @Override
    public Object getUserData(String key) {
        if (this.userData == null) {
            return this.userData;
        }
        return this.userData.get(key);
    }

    public boolean isStreamed() {
        return false;
    }
}


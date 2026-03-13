/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.sf.saxon.om.DocumentKey;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.trans.KeyManager;

public final class DocumentPool {
    private Map<DocumentKey, TreeInfo> documentNameMap = new HashMap<DocumentKey, TreeInfo>(10);
    private Set<DocumentKey> unavailableDocuments = new HashSet<DocumentKey>(10);

    public synchronized void add(TreeInfo doc, String uri) {
        if (uri != null) {
            this.documentNameMap.put(new DocumentKey(uri), doc);
        }
    }

    public synchronized void add(TreeInfo doc, DocumentKey uri) {
        if (uri != null) {
            this.documentNameMap.put(uri, doc);
        }
    }

    public synchronized TreeInfo find(String uri) {
        return this.documentNameMap.get(new DocumentKey(uri));
    }

    public synchronized TreeInfo find(DocumentKey uri) {
        return this.documentNameMap.get(uri);
    }

    public synchronized String getDocumentURI(NodeInfo doc) {
        for (DocumentKey uri : this.documentNameMap.keySet()) {
            TreeInfo found = this.find(uri);
            if (found == null || !found.getRootNode().equals(doc)) continue;
            return uri.toString();
        }
        return null;
    }

    public synchronized boolean contains(TreeInfo doc) {
        return this.documentNameMap.values().contains(doc);
    }

    public synchronized TreeInfo discard(TreeInfo doc) {
        for (Map.Entry<DocumentKey, TreeInfo> e : this.documentNameMap.entrySet()) {
            DocumentKey name = e.getKey();
            TreeInfo entry = e.getValue();
            if (!entry.equals(doc)) continue;
            this.documentNameMap.remove(name);
            return doc;
        }
        return doc;
    }

    public void discardIndexes(KeyManager keyManager) {
        for (TreeInfo doc : this.documentNameMap.values()) {
            keyManager.clearDocumentIndexes(doc);
        }
    }

    public void markUnavailable(DocumentKey uri) {
        this.unavailableDocuments.add(uri);
    }

    public boolean isMarkedUnavailable(DocumentKey uri) {
        return this.unavailableDocuments.contains(uri);
    }
}


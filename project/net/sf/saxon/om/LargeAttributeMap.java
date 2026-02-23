/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.sf.saxon.ma.trie.ImmutableHashTrieMap;
import net.sf.saxon.ma.trie.ImmutableMap;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.tree.util.FastStringBuffer;

public class LargeAttributeMap
implements AttributeMap {
    private ImmutableHashTrieMap<NodeName, AttributeInfoLink> attributes;
    private NodeName first = null;
    private NodeName last = null;
    private int size;

    private LargeAttributeMap() {
    }

    public LargeAttributeMap(List<AttributeInfo> atts) {
        assert (!atts.isEmpty());
        this.attributes = ImmutableHashTrieMap.empty();
        this.size = atts.size();
        AttributeInfoLink current = null;
        for (AttributeInfo att : atts) {
            if (this.attributes.get(att.getNodeName()) != null) {
                throw new IllegalArgumentException("Attribute map contains duplicates");
            }
            AttributeInfoLink link = new AttributeInfoLink();
            link.payload = att;
            if (current == null) {
                this.first = att.getNodeName();
            } else {
                current.next = att.getNodeName();
                link.prior = current.payload.getNodeName();
            }
            current = link;
            this.attributes = this.attributes.put((Object)att.getNodeName(), (Object)link);
        }
        this.last = current.payload.getNodeName();
    }

    private LargeAttributeMap(ImmutableHashTrieMap<NodeName, AttributeInfoLink> attributes, int size, NodeName first, NodeName last) {
        this.attributes = attributes;
        this.size = size;
        this.first = first;
        this.last = last;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public AttributeInfo get(NodeName name) {
        AttributeInfoLink link = this.attributes.get(name);
        return link == null ? null : link.payload;
    }

    @Override
    public AttributeInfo get(String uri, String local) {
        FingerprintedQName name = new FingerprintedQName("", uri, local);
        return this.get(name);
    }

    @Override
    public AttributeInfo getByFingerprint(int fingerprint, NamePool namePool) {
        FingerprintedQName name = new FingerprintedQName(namePool.getStructuredQName(fingerprint), fingerprint);
        return this.get(name);
    }

    @Override
    public AttributeMap put(AttributeInfo att) {
        AttributeInfoLink existing = this.attributes.get(att.getNodeName());
        AttributeInfoLink link = new AttributeInfoLink();
        NodeName last2 = this.last;
        link.payload = att;
        if (existing == null) {
            link.prior = this.last;
            last2 = att.getNodeName();
            AttributeInfoLink oldLast = this.attributes.get(this.last);
            AttributeInfoLink penult = new AttributeInfoLink();
            penult.payload = oldLast.payload;
            penult.next = att.getNodeName();
            penult.prior = oldLast.prior;
            this.attributes = this.attributes.put((Object)this.last, (Object)penult);
        } else {
            link.prior = existing.prior;
            link.next = existing.next;
        }
        ImmutableMap att2 = this.attributes.put((Object)att.getNodeName(), (Object)link);
        int size2 = existing == null ? this.size + 1 : this.size;
        return new LargeAttributeMap((ImmutableHashTrieMap<NodeName, AttributeInfoLink>)att2, size2, this.first, last2);
    }

    @Override
    public AttributeMap remove(NodeName name) {
        if (this.attributes.get(name) == null) {
            return this;
        }
        NodeName first2 = this.first;
        NodeName last2 = this.last;
        ImmutableMap att2 = this.attributes.remove((Object)name);
        AttributeInfoLink existing = this.attributes.get(name);
        if (existing.prior != null) {
            AttributeInfoLink priorLink = this.attributes.get(existing.prior);
            AttributeInfoLink priorLink2 = new AttributeInfoLink();
            priorLink2.payload = priorLink.payload;
            priorLink2.prior = priorLink.prior;
            priorLink2.next = existing.next;
            ((ImmutableHashTrieMap)att2).put(existing.prior, priorLink2);
        } else {
            first2 = existing.next;
        }
        if (existing.next != null) {
            AttributeInfoLink nextLink = this.attributes.get(existing.next);
            AttributeInfoLink nextLink2 = new AttributeInfoLink();
            nextLink2.payload = nextLink.payload;
            nextLink2.next = nextLink.next;
            nextLink2.prior = existing.prior;
            ((ImmutableHashTrieMap)att2).put(existing.next, nextLink2);
        } else {
            last2 = existing.prior;
        }
        return new LargeAttributeMap((ImmutableHashTrieMap<NodeName, AttributeInfoLink>)att2, this.size - 1, first2, last2);
    }

    @Override
    public Iterator<AttributeInfo> iterator() {
        return new Iterator<AttributeInfo>(){
            NodeName current;
            {
                this.current = LargeAttributeMap.this.first;
            }

            @Override
            public boolean hasNext() {
                return this.current != null;
            }

            @Override
            public AttributeInfo next() {
                AttributeInfoLink link = (AttributeInfoLink)LargeAttributeMap.this.attributes.get(this.current);
                this.current = link.next;
                return link.payload;
            }
        };
    }

    @Override
    public synchronized List<AttributeInfo> asList() {
        ArrayList<AttributeInfo> result = new ArrayList<AttributeInfo>(this.size);
        this.iterator().forEachRemaining(result::add);
        return result;
    }

    public String toString() {
        FastStringBuffer sb = new FastStringBuffer(256);
        for (AttributeInfo att : this) {
            sb.cat(att.getNodeName().getDisplayName()).cat("=\"").cat(att.getValue()).cat("\" ");
        }
        return sb.toString().trim();
    }

    private static class AttributeInfoLink {
        AttributeInfo payload;
        NodeName prior;
        NodeName next;

        private AttributeInfoLink() {
        }
    }
}


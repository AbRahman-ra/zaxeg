/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.linked;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.linked.AttributeImpl;
import net.sf.saxon.tree.linked.ElementImpl;

public class AttributeMapWithIdentity
implements AttributeMap {
    private List<AttributeInfo> attributes;

    AttributeMapWithIdentity(List<AttributeInfo> attributes) {
        this.attributes = attributes;
    }

    @Override
    public int size() {
        int count = 0;
        for (AttributeInfo att : this.attributes) {
            if (att instanceof AttributeInfo.Deleted) continue;
            ++count;
        }
        return count;
    }

    public AxisIterator iterateAttributes(ElementImpl owner) {
        ArrayList<NodeInfo> list = new ArrayList<NodeInfo>(this.attributes.size());
        for (int i = 0; i < this.attributes.size(); ++i) {
            AttributeInfo att = this.attributes.get(i);
            if (att instanceof AttributeInfo.Deleted) continue;
            list.add(new AttributeImpl(owner, i));
        }
        return new ListIterator.OfNodes((List<NodeInfo>)list);
    }

    private boolean isDeleted(AttributeInfo info) {
        return info instanceof AttributeInfo.Deleted;
    }

    @Override
    public AttributeInfo get(NodeName name) {
        for (AttributeInfo info : this.attributes) {
            if (!info.getNodeName().equals(name) || info instanceof AttributeInfo.Deleted) continue;
            return info;
        }
        return null;
    }

    @Override
    public AttributeInfo get(String uri, String local) {
        for (AttributeInfo info : this.attributes) {
            NodeName name = info.getNodeName();
            if (!name.getLocalPart().equals(local) || !name.hasURI(uri) || info instanceof AttributeInfo.Deleted) continue;
            return info;
        }
        return null;
    }

    public int getIndex(String uri, String local) {
        for (int i = 0; i < this.attributes.size(); ++i) {
            AttributeInfo info = this.attributes.get(i);
            NodeName name = info.getNodeName();
            if (!name.getLocalPart().equals(local) || !name.hasURI(uri)) continue;
            return i;
        }
        return -1;
    }

    public AttributeMapWithIdentity set(int index, AttributeInfo info) {
        ArrayList<AttributeInfo> newList = new ArrayList<AttributeInfo>(this.attributes);
        if (index >= 0 && index < this.attributes.size()) {
            newList.set(index, info);
        } else if (index == this.attributes.size()) {
            newList.add(info);
        }
        return new AttributeMapWithIdentity(newList);
    }

    public AttributeMapWithIdentity add(AttributeInfo info) {
        ArrayList<AttributeInfo> newList = new ArrayList<AttributeInfo>(this.attributes);
        newList.add(info);
        return new AttributeMapWithIdentity(newList);
    }

    public AttributeMapWithIdentity remove(int index) {
        ArrayList<AttributeInfo> newList = new ArrayList<AttributeInfo>(this.attributes);
        if (index >= 0 && index < this.attributes.size()) {
            AttributeInfo.Deleted del = new AttributeInfo.Deleted(this.attributes.get(index));
            newList.set(index, del);
        }
        return new AttributeMapWithIdentity(newList);
    }

    @Override
    public AttributeInfo getByFingerprint(int fingerprint, NamePool namePool) {
        for (AttributeInfo info : this.attributes) {
            NodeName name = info.getNodeName();
            if (name.obtainFingerprint(namePool) != fingerprint) continue;
            return info;
        }
        return null;
    }

    @Override
    public Iterator<AttributeInfo> iterator() {
        return this.attributes.stream().filter(info -> !(info instanceof AttributeInfo.Deleted)).iterator();
    }

    @Override
    public List<AttributeInfo> asList() {
        return this.attributes.stream().filter(info -> !(info instanceof AttributeInfo.Deleted)).collect(Collectors.toList());
    }

    @Override
    public AttributeInfo itemAt(int index) {
        return this.attributes.get(index);
    }
}


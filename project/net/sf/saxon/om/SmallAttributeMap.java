/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeName;

public class SmallAttributeMap
implements AttributeMap {
    static final int LIMIT = 8;
    private List<AttributeInfo> attributes;

    public SmallAttributeMap(List<AttributeInfo> attributes) {
        this.attributes = new ArrayList<AttributeInfo>(attributes);
    }

    @Override
    public int size() {
        return this.attributes.size();
    }

    @Override
    public AttributeInfo get(NodeName name) {
        for (AttributeInfo info : this.attributes) {
            if (!info.getNodeName().equals(name)) continue;
            return info;
        }
        return null;
    }

    @Override
    public AttributeInfo get(String uri, String local) {
        for (AttributeInfo info : this.attributes) {
            NodeName name = info.getNodeName();
            if (!name.getLocalPart().equals(local) || !name.hasURI(uri)) continue;
            return info;
        }
        return null;
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
        return this.attributes.iterator();
    }

    @Override
    public List<AttributeInfo> asList() {
        return new ArrayList<AttributeInfo>(this.attributes);
    }

    @Override
    public AttributeInfo itemAt(int index) {
        return this.attributes.get(index);
    }
}


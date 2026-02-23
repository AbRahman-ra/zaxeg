/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.EmptyAttributeMap;
import net.sf.saxon.om.LargeAttributeMap;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.SingletonAttributeMap;
import net.sf.saxon.om.SmallAttributeMap;

public interface AttributeMap
extends Iterable<AttributeInfo> {
    public int size();

    default public AttributeInfo get(NodeName name) {
        for (AttributeInfo att : this) {
            if (!att.getNodeName().equals(name)) continue;
            return att;
        }
        return null;
    }

    default public AttributeInfo get(String uri, String local) {
        for (AttributeInfo att : this) {
            NodeName attName = att.getNodeName();
            if (!attName.getLocalPart().equals(local) || !attName.hasURI(uri)) continue;
            return att;
        }
        return null;
    }

    default public AttributeInfo getByFingerprint(int fingerprint, NamePool namePool) {
        for (AttributeInfo att : this) {
            NodeName attName = att.getNodeName();
            if (attName.obtainFingerprint(namePool) != fingerprint) continue;
            return att;
        }
        return null;
    }

    default public String getValue(String uri, String local) {
        AttributeInfo att = this.get(uri, local);
        return att == null ? null : att.getValue();
    }

    default public AttributeMap put(AttributeInfo att) {
        ArrayList<AttributeInfo> list = new ArrayList<AttributeInfo>(this.size() + 1);
        for (AttributeInfo a : this) {
            if (a.getNodeName().equals(att.getNodeName())) continue;
            list.add(a);
        }
        list.add(att);
        return AttributeMap.fromList(list);
    }

    default public AttributeMap remove(NodeName name) {
        ArrayList<AttributeInfo> list = new ArrayList<AttributeInfo>(this.size());
        for (AttributeInfo a : this) {
            if (a.getNodeName().equals(name)) continue;
            list.add(a);
        }
        return AttributeMap.fromList(list);
    }

    default public void verify() {
    }

    default public AttributeMap apply(Function<AttributeInfo, AttributeInfo> mapper) {
        ArrayList<AttributeInfo> list = new ArrayList<AttributeInfo>(this.size());
        for (AttributeInfo a : this) {
            list.add(mapper.apply(a));
        }
        return AttributeMap.fromList(list);
    }

    default public List<AttributeInfo> asList() {
        ArrayList<AttributeInfo> list = new ArrayList<AttributeInfo>(this.size());
        for (AttributeInfo a : this) {
            list.add(a);
        }
        return list;
    }

    default public AttributeInfo itemAt(int index) {
        return this.asList().get(index);
    }

    public static AttributeMap fromList(List<AttributeInfo> list) {
        int n = list.size();
        if (n == 0) {
            return EmptyAttributeMap.getInstance();
        }
        if (n == 1) {
            return SingletonAttributeMap.of(list.get(0));
        }
        if (n <= 8) {
            return new SmallAttributeMap(list);
        }
        return new LargeAttributeMap(list);
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.EmptyAttributeMap;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.SmallAttributeMap;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.tree.jiter.MonoIterator;
import net.sf.saxon.type.SimpleType;

public class SingletonAttributeMap
extends AttributeInfo
implements AttributeMap {
    private SingletonAttributeMap(NodeName nodeName, SimpleType type, String value, Location location, int properties) {
        super(nodeName, type, value, location, properties);
    }

    public static SingletonAttributeMap of(AttributeInfo att) {
        if (att instanceof SingletonAttributeMap) {
            return (SingletonAttributeMap)att;
        }
        return new SingletonAttributeMap(att.getNodeName(), att.getType(), att.getValue(), att.getLocation(), att.getProperties());
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public AttributeInfo get(NodeName name) {
        return name.equals(this.getNodeName()) ? this : null;
    }

    @Override
    public AttributeInfo get(String uri, String local) {
        return this.getNodeName().getLocalPart().equals(local) && this.getNodeName().hasURI(uri) ? this : null;
    }

    @Override
    public AttributeInfo getByFingerprint(int fingerprint, NamePool namePool) {
        return this.getNodeName().obtainFingerprint(namePool) == fingerprint ? this : null;
    }

    @Override
    public AttributeMap put(AttributeInfo att) {
        if (this.getNodeName().equals(att.getNodeName())) {
            return SingletonAttributeMap.of(att);
        }
        ArrayList<AttributeInfo> list = new ArrayList<AttributeInfo>(2);
        list.add(this);
        list.add(att);
        return new SmallAttributeMap(list);
    }

    @Override
    public AttributeMap remove(NodeName name) {
        return name.equals(this.getNodeName()) ? EmptyAttributeMap.getInstance() : this;
    }

    @Override
    public Iterator<AttributeInfo> iterator() {
        return new MonoIterator<AttributeInfo>(this);
    }

    @Override
    public AttributeMap apply(Function<AttributeInfo, AttributeInfo> mapper) {
        return SingletonAttributeMap.of(mapper.apply(this));
    }

    @Override
    public List<AttributeInfo> asList() {
        ArrayList<AttributeInfo> list = new ArrayList<AttributeInfo>(1);
        list.add(this);
        return list;
    }

    @Override
    public AttributeInfo itemAt(int index) {
        if (index == 0) {
            return this;
        }
        throw new IndexOutOfBoundsException(index + " of 1");
    }
}


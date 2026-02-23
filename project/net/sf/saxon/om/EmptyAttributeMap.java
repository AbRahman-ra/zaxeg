/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.SingletonAttributeMap;

public class EmptyAttributeMap
implements AttributeMap {
    private static EmptyAttributeMap THE_INSTANCE = new EmptyAttributeMap();

    private EmptyAttributeMap() {
    }

    public static EmptyAttributeMap getInstance() {
        return THE_INSTANCE;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public AttributeInfo get(NodeName name) {
        return null;
    }

    @Override
    public AttributeInfo get(String uri, String local) {
        return null;
    }

    @Override
    public AttributeInfo getByFingerprint(int fingerprint, NamePool namePool) {
        return null;
    }

    @Override
    public AttributeMap put(AttributeInfo att) {
        return SingletonAttributeMap.of(att);
    }

    @Override
    public AttributeMap remove(NodeName name) {
        return this;
    }

    @Override
    public Iterator<AttributeInfo> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public AttributeMap apply(Function<AttributeInfo, AttributeInfo> mapper) {
        return this;
    }
}


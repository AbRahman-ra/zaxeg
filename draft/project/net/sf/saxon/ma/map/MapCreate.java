/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.map;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

public class MapCreate
extends SystemFunction {
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        MapItem next;
        SequenceIterator iter = arguments[0].iterate();
        MapItem baseMap = (MapItem)iter.next();
        if (baseMap == null) {
            return new HashTrieMap();
        }
        if (!(baseMap instanceof HashTrieMap)) {
            baseMap = HashTrieMap.copy(baseMap);
        }
        while ((next = (MapItem)iter.next()) != null) {
            for (KeyValuePair pair : next.keyValuePairs()) {
                if (baseMap.get(pair.key) != null) {
                    throw new XPathException("Duplicate key value (" + pair.key + ") in map", "XQDY0137");
                }
                baseMap = baseMap.addEntry(pair.key, pair.value);
            }
        }
        return baseMap;
    }

    @Override
    public String getStreamerName() {
        return "NewMap";
    }
}


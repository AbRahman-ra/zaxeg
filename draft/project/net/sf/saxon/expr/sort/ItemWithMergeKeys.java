/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.SortKeyDefinition;
import net.sf.saxon.expr.sort.SortKeyDefinitionList;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;

public class ItemWithMergeKeys {
    Item baseItem;
    List<AtomicValue> sortKeyValues;
    String sourceName;

    ItemWithMergeKeys(Item bItem, SortKeyDefinitionList sKeys, String name, XPathContext context) throws XPathException {
        this.baseItem = bItem;
        this.sourceName = name;
        this.sortKeyValues = new ArrayList<AtomicValue>(sKeys.size());
        for (SortKeyDefinition sKey : sKeys) {
            this.sortKeyValues.add((AtomicValue)sKey.getSortKey().evaluateItem(context));
        }
    }
}


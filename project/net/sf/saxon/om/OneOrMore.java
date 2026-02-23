/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceExtent;

public class OneOrMore<T extends Item>
extends SequenceExtent {
    public OneOrMore(T[] content) {
        super((Item[])content);
        if (content.length == 0) {
            throw new IllegalArgumentException();
        }
    }

    public OneOrMore(List<T> content) {
        super(content);
        if (content.isEmpty()) {
            throw new IllegalArgumentException();
        }
    }

    public static OneOrMore<Item> makeOneOrMore(Sequence sequence) throws XPathException {
        ArrayList content = new ArrayList();
        sequence.iterate().forEachOrFail(content::add);
        if (content.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return new OneOrMore<Item>((List<Item>)content);
    }
}


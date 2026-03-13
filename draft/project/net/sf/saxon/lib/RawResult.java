/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.Result;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.value.SequenceExtent;

public class RawResult
implements Result {
    private String systemId;
    private List<Item> content = new ArrayList<Item>();

    @Override
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    @Override
    public String getSystemId() {
        return this.systemId;
    }

    public void append(Item item) {
        this.content.add(item);
    }

    public Sequence getResultSequence() {
        return new SequenceExtent(this.content);
    }
}


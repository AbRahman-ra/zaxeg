/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.Controller;
import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.SequenceWriter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceExtent;

public final class SequenceCollector
extends SequenceWriter {
    private List<Item> list;

    public SequenceCollector(PipelineConfiguration pipe) {
        this(pipe, 50);
    }

    public SequenceCollector(PipelineConfiguration pipe, int estimatedSize) {
        super(pipe);
        this.list = new ArrayList<Item>(estimatedSize);
    }

    public static Outputter allocateSequenceOutputter(XPathContext context, HostLanguage hostLang) {
        Controller controller = context.getController();
        PipelineConfiguration pipe = controller.makePipelineConfiguration();
        pipe.setHostLanguage(hostLang);
        SequenceCollector collector = new SequenceCollector(pipe, 20);
        return new ComplexContentOutputter(collector);
    }

    public void reset() {
        this.list = new ArrayList<Item>(Math.min(this.list.size() + 10, 50));
    }

    @Override
    public void write(Item item) {
        this.list.add(item);
    }

    public Sequence getSequence() {
        switch (this.list.size()) {
            case 0: {
                return EmptySequence.getInstance();
            }
            case 1: {
                return this.list.get(0);
            }
        }
        return new SequenceExtent(this.list);
    }

    public SequenceIterator iterate() {
        if (this.list.isEmpty()) {
            return EmptyIterator.emptyIterator();
        }
        return new ListIterator<Item>(this.list);
    }

    public List<Item> getList() {
        return this.list;
    }

    public Item getFirstItem() {
        if (this.list.isEmpty()) {
            return null;
        }
        return this.list.get(0);
    }
}


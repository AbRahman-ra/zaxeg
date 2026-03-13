/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.ItemTypeFactory;
import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.Processor;

public class SequenceType {
    private ItemType itemType;
    private OccurrenceIndicator occurrenceIndicator;
    public static final SequenceType ANY = new SequenceType(ItemType.ANY_ITEM, OccurrenceIndicator.ZERO_OR_MORE);
    public static final SequenceType EMPTY = new SequenceType(ItemType.ERROR, OccurrenceIndicator.ZERO);

    private SequenceType(ItemType itemType, OccurrenceIndicator occurrenceIndicator) {
        this.itemType = itemType;
        this.occurrenceIndicator = occurrenceIndicator;
    }

    public static SequenceType makeSequenceType(ItemType itemType, OccurrenceIndicator occurrenceIndicator) {
        return new SequenceType(itemType, occurrenceIndicator);
    }

    public ItemType getItemType() {
        return this.itemType;
    }

    public OccurrenceIndicator getOccurrenceIndicator() {
        return this.occurrenceIndicator;
    }

    public final boolean equals(Object other) {
        return other instanceof SequenceType && ((SequenceType)other).getOccurrenceIndicator().equals((Object)this.getOccurrenceIndicator()) && ((SequenceType)other).getItemType().equals(this.getItemType());
    }

    public final int hashCode() {
        return this.getItemType().hashCode() ^ this.getOccurrenceIndicator().hashCode() << 17;
    }

    public net.sf.saxon.value.SequenceType getUnderlyingSequenceType() {
        return net.sf.saxon.value.SequenceType.makeSequenceType(this.itemType.getUnderlyingItemType(), this.occurrenceIndicator.getCardinality());
    }

    public static SequenceType fromUnderlyingSequenceType(Processor processor, net.sf.saxon.value.SequenceType st) {
        ItemTypeFactory factory = new ItemTypeFactory(processor);
        ItemType it = factory.exposeItemType(st.getPrimaryType());
        OccurrenceIndicator oc = OccurrenceIndicator.getOccurrenceIndicator(st.getCardinality());
        return SequenceType.makeSequenceType(it, oc);
    }
}


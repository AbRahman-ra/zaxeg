/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.util.Objects;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.TypeHierarchy;

class ConstructedItemType
extends ItemType {
    private net.sf.saxon.type.ItemType underlyingType;
    private Processor processor;

    protected ConstructedItemType(net.sf.saxon.type.ItemType underlyingType, Processor processor) {
        Objects.requireNonNull(processor);
        Objects.requireNonNull(underlyingType);
        this.processor = processor;
        this.underlyingType = underlyingType;
    }

    @Override
    public ConversionRules getConversionRules() {
        return this.processor.getUnderlyingConfiguration().getConversionRules();
    }

    @Override
    public boolean matches(XdmItem item) throws SaxonApiUncheckedException {
        try {
            TypeHierarchy th = this.processor.getUnderlyingConfiguration().getTypeHierarchy();
            return this.underlyingType.matches(item.getUnderlyingValue(), th);
        } catch (XPathException e) {
            throw new SaxonApiUncheckedException(e);
        }
    }

    @Override
    public boolean subsumes(ItemType other) {
        TypeHierarchy th = this.processor.getUnderlyingConfiguration().getTypeHierarchy();
        return th.isSubType(other.getUnderlyingItemType(), this.underlyingType);
    }

    @Override
    public net.sf.saxon.type.ItemType getUnderlyingItemType() {
        return this.underlyingType;
    }

    protected Processor getProcessor() {
        return this.processor;
    }
}


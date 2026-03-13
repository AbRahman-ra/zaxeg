/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.style.XSLSortOrMergeKey;
import net.sf.saxon.type.ItemType;

public class XSLMergeKey
extends XSLSortOrMergeKey {
    @Override
    public void prepareAttributes() {
        super.prepareAttributes();
        if (this.stable != null) {
            this.compileError("The @stable attribute is not allowed in xsl:merge-key", "XTSE0090");
        }
    }

    protected ItemType getReturnedItemType() {
        return null;
    }

    @Override
    protected String getErrorCode() {
        return "XTSE3200";
    }
}


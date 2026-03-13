/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SequenceType;
import net.sf.saxon.s9api.XdmValue;

public interface ExtensionFunction {
    public QName getName();

    default public SequenceType getResultType() {
        return SequenceType.ANY;
    }

    public SequenceType[] getArgumentTypes();

    public XdmValue call(XdmValue[] var1) throws SaxonApiException;
}


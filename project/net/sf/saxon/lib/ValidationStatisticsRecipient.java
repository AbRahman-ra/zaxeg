/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.util.Map;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.type.SchemaComponent;

public interface ValidationStatisticsRecipient {
    public void notifyValidationStatistics(Map<SchemaComponent, Integer> var1) throws SaxonApiException;
}


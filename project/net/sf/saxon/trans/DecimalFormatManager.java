/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import java.util.HashMap;
import java.util.Map;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.trans.DecimalSymbols;
import net.sf.saxon.trans.XPathException;

public class DecimalFormatManager {
    private DecimalSymbols defaultDFS;
    private HashMap<StructuredQName, DecimalSymbols> formatTable = new HashMap(10);
    private HostLanguage language;
    private int languageLevel;

    public DecimalFormatManager(HostLanguage language, int languageLevel) {
        this.defaultDFS = new DecimalSymbols(language, languageLevel);
        this.language = language;
        this.languageLevel = languageLevel;
    }

    public DecimalSymbols getDefaultDecimalFormat() {
        return this.defaultDFS;
    }

    public DecimalSymbols getNamedDecimalFormat(StructuredQName qName) {
        DecimalSymbols ds = this.formatTable.get(qName);
        if (ds == null) {
            return null;
        }
        return ds;
    }

    public DecimalSymbols obtainNamedDecimalFormat(StructuredQName qName) {
        DecimalSymbols ds = this.formatTable.get(qName);
        if (ds == null) {
            ds = new DecimalSymbols(this.language, this.languageLevel);
            this.formatTable.put(qName, ds);
        }
        return ds;
    }

    public Iterable<StructuredQName> getDecimalFormatNames() {
        return this.formatTable.keySet();
    }

    public void checkConsistency() throws XPathException {
        this.defaultDFS.checkConsistency(null);
        for (Map.Entry<StructuredQName, DecimalSymbols> entry : this.formatTable.entrySet()) {
            entry.getValue().checkConsistency(entry.getKey());
        }
    }
}


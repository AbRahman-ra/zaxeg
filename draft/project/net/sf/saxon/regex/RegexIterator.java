/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

public interface RegexIterator
extends SequenceIterator {
    @Override
    public StringValue next() throws XPathException;

    public boolean isMatching();

    public int getNumberOfGroups();

    public String getRegexGroup(int var1);

    public void processMatchingSubstring(MatchHandler var1) throws XPathException;

    public static interface MatchHandler {
        public void characters(CharSequence var1) throws XPathException;

        public void onGroupStart(int var1) throws XPathException;

        public void onGroupEnd(int var1) throws XPathException;
    }
}


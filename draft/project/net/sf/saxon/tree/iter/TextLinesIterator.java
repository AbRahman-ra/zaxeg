/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import java.io.IOException;
import java.io.LineNumberReader;
import java.net.URI;
import java.util.function.IntPredicate;
import net.sf.saxon.functions.UnparsedTextFunction;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.charcode.UTF16CharacterSet;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

public abstract class TextLinesIterator
implements SequenceIterator {
    protected LineNumberReader reader;
    protected IntPredicate checker;
    StringValue current = null;
    int position = 0;
    protected Location location;
    protected URI uri;

    protected TextLinesIterator() {
    }

    public TextLinesIterator(LineNumberReader reader, Location location, URI uri, IntPredicate checker) throws XPathException {
        this.reader = reader;
        this.location = location;
        this.uri = uri;
        this.checker = checker;
    }

    @Override
    public StringValue next() throws XPathException {
        if (this.position < 0) {
            this.close();
            return null;
        }
        try {
            String s = this.reader.readLine();
            if (s == null) {
                this.current = null;
                this.position = -1;
                this.close();
                return null;
            }
            if (this.position == 0 && s.startsWith("\ufeff")) {
                s = s.substring(1);
            }
            this.checkLine(this.checker, s);
            this.current = new StringValue(s);
            ++this.position;
            return this.current;
        } catch (IOException err) {
            this.close();
            XPathException e = UnparsedTextFunction.handleIOError(this.uri, err, null);
            if (this.location != null) {
                e.setLocator(this.location);
            }
            throw e;
        }
    }

    @Override
    public void close() {
        try {
            this.reader.close();
        } catch (IOException iOException) {
            // empty catch block
        }
    }

    private void checkLine(IntPredicate checker, String buffer) throws XPathException {
        int c = 0;
        while (c < buffer.length()) {
            int ch32;
            if (UTF16CharacterSet.isHighSurrogate(ch32 = buffer.charAt(c++))) {
                char low = buffer.charAt(c++);
                ch32 = UTF16CharacterSet.combinePair((char)ch32, low);
            }
            if (checker.test(ch32)) continue;
            XPathException err = new XPathException("The unparsed-text file contains a character that is illegal in XML (line=" + this.position + " column=" + (c + 1) + " value=hex " + Integer.toHexString(ch32) + ')');
            err.setErrorCode("FOUT1190");
            err.setLocator(this.location);
            throw err;
        }
    }
}


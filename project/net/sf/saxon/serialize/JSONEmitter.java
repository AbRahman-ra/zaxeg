/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.io.IOException;
import java.io.Writer;
import java.util.Properties;
import java.util.Stack;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.ma.json.JsonReceiver;
import net.sf.saxon.serialize.CharacterMap;
import net.sf.saxon.serialize.ExpandedStreamResult;
import net.sf.saxon.serialize.charcode.CharacterSet;
import net.sf.saxon.serialize.codenorm.Normalizer;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.NumericValue;

public class JSONEmitter {
    private ExpandedStreamResult result;
    private Writer writer;
    private Normalizer normalizer;
    private CharacterMap characterMap;
    private Properties outputProperties;
    private CharacterSet characterSet;
    private boolean isIndenting;
    private int indentSpaces = 2;
    private int maxLineLength;
    private boolean first = true;
    private boolean afterKey = false;
    private int level;
    private Stack<Boolean> oneLinerStack = new Stack();
    private boolean unfailing = false;

    public JSONEmitter(PipelineConfiguration pipe, StreamResult result, Properties outputProperties) throws XPathException {
        this.setOutputProperties(outputProperties);
        this.result = new ExpandedStreamResult(pipe.getConfiguration(), result, outputProperties);
    }

    public void setOutputProperties(Properties details) {
        String spaces;
        String max;
        this.outputProperties = details;
        if ("yes".equals(details.getProperty("indent"))) {
            this.isIndenting = true;
        }
        if ("yes".equals(details.getProperty("{http://saxon.sf.net/}unfailing"))) {
            this.unfailing = true;
        }
        if ((max = details.getProperty("{http://saxon.sf.net/}line-length")) != null) {
            try {
                this.maxLineLength = Integer.parseInt(max);
            } catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        if ((spaces = details.getProperty("{http://saxon.sf.net/}indent-spaces")) != null) {
            try {
                this.indentSpaces = Integer.parseInt(spaces);
            } catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
    }

    public Properties getOutputProperties() {
        return this.outputProperties;
    }

    public void setNormalizer(Normalizer normalizer) {
        this.normalizer = normalizer;
    }

    public void setCharacterMap(CharacterMap map) {
        this.characterMap = map;
    }

    public void writeKey(String key) throws XPathException {
        this.conditionalComma(false);
        this.emit('\"');
        this.emit(this.escape(key));
        this.emit("\":");
        if (this.isIndenting) {
            this.emit(" ");
        }
        this.afterKey = true;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void writeAtomicValue(AtomicValue item) throws XPathException {
        this.conditionalComma(false);
        if (item == null) {
            this.emit("null");
            return;
        } else if (item instanceof NumericValue) {
            NumericValue num = (NumericValue)item;
            if (num.isNaN()) {
                if (!this.unfailing) throw new XPathException("JSON has no way of representing NaN", "SERE0020");
                this.emit("NaN");
                return;
            } else if (Double.isInfinite(num.getDoubleValue())) {
                if (!this.unfailing) throw new XPathException("JSON has no way of representing Infinity", "SERE0020");
                this.emit(num.getDoubleValue() < 0.0 ? "-INF" : "INF");
                return;
            } else if (item instanceof IntegerValue) {
                this.emit(num.longValue() + "");
                return;
            } else if (num.isWholeNumber() && !num.isNegativeZero() && num.abs().compareTo(1000000000000000000L) < 0) {
                this.emit(num.longValue() + "");
                return;
            } else {
                this.emit(num.getStringValue());
            }
            return;
        } else if (item instanceof BooleanValue) {
            this.emit(item.getStringValue());
            return;
        } else {
            this.emit('\"');
            this.emit(this.escape(item.getStringValue()));
            this.emit('\"');
        }
    }

    public void startArray(boolean oneLiner) throws XPathException {
        this.emitOpen('[', oneLiner);
        ++this.level;
    }

    public void endArray() throws XPathException {
        this.emitClose(']', this.level--);
    }

    public void startMap(boolean oneLiner) throws XPathException {
        this.emitOpen('{', oneLiner);
        ++this.level;
    }

    public void endMap() throws XPathException {
        this.emitClose('}', this.level--);
    }

    private void emitOpen(char bracket, boolean oneLiner) throws XPathException {
        this.conditionalComma(true);
        this.oneLinerStack.push(oneLiner);
        this.emit(bracket);
        this.first = true;
        if (this.isIndenting && oneLiner) {
            this.emit(' ');
        }
    }

    private void emitClose(char bracket, int level) throws XPathException {
        boolean oneLiner = this.oneLinerStack.pop();
        if (this.isIndenting) {
            if (oneLiner) {
                this.emit(' ');
            } else {
                this.indent(level - 1);
            }
        }
        this.emit(bracket);
        this.first = false;
    }

    private void conditionalComma(boolean opening) throws XPathException {
        boolean actuallyIndenting;
        boolean wasFirst = this.first;
        boolean bl = actuallyIndenting = this.isIndenting && this.level != 0 && this.oneLinerStack.peek() == false;
        if (this.first) {
            this.first = false;
        } else if (!this.afterKey) {
            this.emit(',');
        }
        if (wasFirst && this.afterKey) {
            this.emit(' ');
        } else if (actuallyIndenting && !this.afterKey) {
            this.emit('\n');
            for (int i = 0; i < this.indentSpaces * this.level; ++i) {
                this.emit(' ');
            }
        }
        this.afterKey = false;
    }

    private void indent(int level) throws XPathException {
        this.emit('\n');
        for (int i = 0; i < this.indentSpaces * level; ++i) {
            this.emit(' ');
        }
    }

    private CharSequence escape(CharSequence cs) throws XPathException {
        if (this.characterMap != null) {
            int start;
            FastStringBuffer out = new FastStringBuffer(cs.length());
            cs = this.characterMap.map(cs, true);
            String s = cs.toString();
            int prev = 0;
            while ((start = s.indexOf(0, prev)) >= 0) {
                out.cat(this.simpleEscape(s.substring(prev, start)));
                int end = s.indexOf(0, start + 1);
                out.append(s.substring(start + 1, end));
                prev = end + 1;
            }
            out.cat(this.simpleEscape(s.substring(prev)));
            return out;
        }
        return this.simpleEscape(cs);
    }

    private CharSequence simpleEscape(CharSequence cs) throws XPathException {
        if (this.normalizer != null) {
            cs = this.normalizer.normalize(cs);
        }
        return JsonReceiver.escape(cs, false, c -> c < 31 || c >= 127 && c <= 159 || !this.characterSet.inCharset(c));
    }

    private void emit(CharSequence s) throws XPathException {
        if (this.writer == null) {
            this.writer = this.result.obtainWriter();
            this.characterSet = this.result.getCharacterSet();
        }
        try {
            this.writer.append(s);
        } catch (IOException e) {
            throw new XPathException(e);
        }
    }

    private void emit(char c) throws XPathException {
        this.emit(c + "");
    }

    public void close() throws XPathException {
        if (this.first) {
            this.emit("null");
        }
        if (this.writer != null) {
            try {
                this.writer.close();
            } catch (IOException iOException) {
                // empty catch block
            }
        }
    }
}


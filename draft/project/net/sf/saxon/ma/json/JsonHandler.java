/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.json;

import java.util.Map;
import java.util.function.IntPredicate;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.ma.json.JsonReceiver;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.serialize.charcode.UTF16CharacterSet;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class JsonHandler {
    public boolean escape;
    protected IntPredicate charChecker;
    private XPathContext context;
    private Function fallbackFunction = null;
    private static final String REPLACEMENT = "\ufffd";

    public void setContext(XPathContext context) {
        this.context = context;
    }

    public XPathContext getContext() {
        return this.context;
    }

    public Sequence getResult() throws XPathException {
        return null;
    }

    public boolean setKey(String unEscaped, String reEscaped) {
        return false;
    }

    public void startArray() throws XPathException {
    }

    public void endArray() throws XPathException {
    }

    public void startMap() throws XPathException {
    }

    public void endMap() throws XPathException {
    }

    public void writeNumeric(String asString, double asDouble) throws XPathException {
    }

    public void writeString(String val) throws XPathException {
    }

    public String reEscape(String val) throws XPathException {
        CharSequence escaped;
        if (this.escape) {
            escaped = JsonReceiver.escape(val, true, new IntPredicate(){

                @Override
                public boolean test(int value) {
                    return value >= 0 && value <= 31 || value >= 127 && value <= 159 || !JsonHandler.this.charChecker.test(value) || value == 92;
                }
            });
        } else {
            FastStringBuffer buffer = new FastStringBuffer(val);
            this.handleInvalidCharacters(buffer);
            escaped = buffer;
        }
        return escaped.toString();
    }

    public void writeBoolean(boolean value) throws XPathException {
    }

    public void writeNull() throws XPathException {
    }

    protected void handleInvalidCharacters(FastStringBuffer buffer) throws XPathException {
        IntPredicate charChecker = this.context.getConfiguration().getValidCharacterChecker();
        for (int i = 0; i < buffer.length(); ++i) {
            char ch = buffer.charAt(i);
            if (UTF16CharacterSet.isHighSurrogate(ch)) {
                if (i + 1 < buffer.length() && UTF16CharacterSet.isLowSurrogate(buffer.charAt(i + 1))) continue;
                this.substitute(buffer, i, 1, this.context);
                continue;
            }
            if (UTF16CharacterSet.isLowSurrogate(ch)) {
                if (i == 0 || !UTF16CharacterSet.isHighSurrogate(buffer.charAt(i - 1))) {
                    this.substitute(buffer, i, 1, this.context);
                    continue;
                }
                int pair = UTF16CharacterSet.combinePair(buffer.charAt(i - 1), ch);
                if (charChecker.test(pair)) continue;
                this.substitute(buffer, i - 1, 2, this.context);
                continue;
            }
            if (charChecker.test(ch)) continue;
            this.substitute(buffer, i, 1, this.context);
        }
    }

    protected void markAsEscaped(CharSequence escaped, boolean isKey) throws XPathException {
    }

    private void substitute(FastStringBuffer buffer, int offset, int count, XPathContext context) throws XPathException {
        FastStringBuffer escaped = new FastStringBuffer(count * 6);
        for (int j = 0; j < count; ++j) {
            escaped.append("\\u");
            String hex = Integer.toHexString(buffer.charAt(offset + j));
            while (hex.length() < 4) {
                hex = "0" + hex;
            }
            hex = hex.toUpperCase();
            escaped.append(hex);
        }
        String replacement = this.replace(escaped.toString(), context);
        if (replacement.length() == count) {
            for (int j = 0; j < count; ++j) {
                buffer.setCharAt(offset + j, replacement.charAt(j));
            }
        } else {
            int j;
            for (j = 0; j < count; ++j) {
                buffer.removeCharAt(offset + j);
            }
            for (j = 0; j < replacement.length(); ++j) {
                buffer.insert(offset + j, replacement.charAt(j));
            }
        }
    }

    private String replace(String s, XPathContext context) throws XPathException {
        if (this.fallbackFunction != null) {
            Sequence[] args = new Sequence[]{new StringValue(s)};
            Item result = SystemFunction.dynamicCall(this.fallbackFunction, context, args).head();
            Item first = result.head();
            return first == null ? "" : first.getStringValue();
        }
        return REPLACEMENT;
    }

    public void setFallbackFunction(Map<String, Sequence> options, XPathContext context) throws XPathException {
        Sequence val = options.get("fallback");
        if (val != null) {
            Item fn = val.head();
            if (fn instanceof Function) {
                this.fallbackFunction = (Function)fn;
                if (this.fallbackFunction.getArity() != 1) {
                    throw new XPathException("Fallback function must have arity=1", "FOJS0005");
                }
                SpecificFunctionType required = new SpecificFunctionType(new SequenceType[]{SequenceType.SINGLE_STRING}, SequenceType.ANY_SEQUENCE);
                if (!required.matches(this.fallbackFunction, context.getConfiguration().getTypeHierarchy())) {
                    throw new XPathException("Fallback function does not match the required type", "FOJS0005");
                }
            } else {
                throw new XPathException("Value of option 'fallback' is not a function", "FOJS0005");
            }
        }
    }
}


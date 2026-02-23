/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.StatefulSystemFunction;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.z.IntToIntHashMap;
import net.sf.saxon.z.IntToIntMap;

public class Translate
extends SystemFunction
implements Callable,
StatefulSystemFunction {
    private IntToIntMap staticMap = null;

    @Override
    public Expression fixArguments(Expression ... arguments) {
        if (arguments[1] instanceof StringLiteral && arguments[2] instanceof StringLiteral) {
            this.staticMap = Translate.buildMap(((StringLiteral)arguments[1]).getValue(), ((StringLiteral)arguments[2]).getValue());
        }
        return null;
    }

    public IntToIntMap getStaticMap() {
        return this.staticMap;
    }

    public static CharSequence translate(StringValue sv0, StringValue sv1, StringValue sv2) {
        if (sv0.containsSurrogatePairs() || sv1.containsSurrogatePairs() || sv2.containsSurrogatePairs()) {
            return Translate.translateUsingMap(sv0, Translate.buildMap(sv1, sv2));
        }
        if (sv0.getStringLength() * sv1.getStringLength() > 1000) {
            return Translate.translateUsingMap(sv0, Translate.buildMap(sv1, sv2));
        }
        CharSequence cs0 = sv0.getStringValueCS();
        CharSequence cs1 = sv1.getStringValueCS();
        CharSequence cs2 = sv2.getStringValueCS();
        String st1 = cs1.toString();
        FastStringBuffer sb = new FastStringBuffer(cs0.length());
        int s2len = cs2.length();
        int s0len = cs0.length();
        for (int i = 0; i < s0len; ++i) {
            char c = cs0.charAt(i);
            int j = st1.indexOf(c);
            if (j >= s2len) continue;
            sb.cat(j < 0 ? c : cs2.charAt(j));
        }
        return sb;
    }

    private static IntToIntMap buildMap(StringValue arg1, StringValue arg2) {
        UnicodeString a1 = arg1.getUnicodeString();
        UnicodeString a2 = arg2.getUnicodeString();
        IntToIntHashMap map = new IntToIntHashMap(a1.uLength(), 0.5);
        for (int i = 0; i < a1.uLength(); ++i) {
            if (map.find(a1.uCharAt(i))) continue;
            map.put(a1.uCharAt(i), i > a2.uLength() - 1 ? -1 : a2.uCharAt(i));
        }
        return map;
    }

    public static CharSequence translateUsingMap(StringValue in, IntToIntMap map) {
        UnicodeString us = in.getUnicodeString();
        int len = us.uLength();
        FastStringBuffer sb = new FastStringBuffer(len);
        for (int i = 0; i < len; ++i) {
            int c = us.uCharAt(i);
            int newchar = map.get(c);
            if (newchar == Integer.MAX_VALUE) {
                newchar = c;
            }
            if (newchar == -1) continue;
            sb.appendWideChar(newchar);
        }
        return sb;
    }

    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        StringValue sv0 = (StringValue)arguments[0].head();
        if (sv0 == null) {
            return StringValue.EMPTY_STRING;
        }
        if (this.staticMap != null) {
            return new StringValue(Translate.translateUsingMap(sv0, this.staticMap));
        }
        StringValue sv1 = (StringValue)arguments[1].head();
        StringValue sv2 = (StringValue)arguments[2].head();
        return new StringValue(Translate.translate(sv0, sv1, sv2));
    }

    @Override
    public String getCompilerName() {
        return "TranslateCompiler";
    }

    @Override
    public Translate copy() {
        Translate copy = (Translate)SystemFunction.makeFunction(this.getFunctionName().getLocalPart(), this.getRetainedStaticContext(), this.getArity());
        copy.staticMap = this.staticMap;
        return copy;
    }
}


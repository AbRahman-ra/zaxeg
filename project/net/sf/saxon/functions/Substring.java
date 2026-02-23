/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FunctionCall;
import net.sf.saxon.expr.StaticFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.functions.Number_1;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.One;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.regex.EmptyString;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.StringValue;

public class Substring
extends SystemFunction
implements Callable {
    @Override
    public Expression typeCheckCaller(FunctionCall caller, ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Expression a2;
        Expression a1;
        Expression e2 = super.typeCheckCaller(caller, visitor, contextInfo);
        if (e2 != caller) {
            return e2;
        }
        TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
        if (caller.getArg(1).isCallOn(Number_1.class) && th.isSubType((a1 = ((StaticFunctionCall)caller.getArg(1)).getArg(0)).getItemType(), BuiltInAtomicType.INTEGER)) {
            caller.setArg(1, a1);
        }
        if (this.getArity() > 2 && caller.getArg(2).isCallOn(Number_1.class) && th.isSubType((a2 = ((StaticFunctionCall)caller.getArg(2)).getArg(0)).getItemType(), BuiltInAtomicType.INTEGER)) {
            caller.setArg(2, a2);
        }
        return caller;
    }

    public static UnicodeString substring(StringValue sv, NumericValue start) {
        long lstart;
        UnicodeString s = sv.getUnicodeString();
        int slength = s.uLength();
        if (start instanceof Int64Value) {
            lstart = ((Int64Value)start).longValue();
            if (lstart > (long)slength) {
                return EmptyString.THE_INSTANCE;
            }
            if (lstart <= 0L) {
                lstart = 1L;
            }
        } else {
            if (start.isNaN()) {
                return EmptyString.THE_INSTANCE;
            }
            if (start.signum() <= 0) {
                return s;
            }
            if (start.compareTo(slength) > 0) {
                return EmptyString.THE_INSTANCE;
            }
            lstart = Math.round(start.getDoubleValue());
        }
        if (lstart > (long)s.uLength()) {
            return EmptyString.THE_INSTANCE;
        }
        return s.uSubstring((int)lstart - 1, s.uLength());
    }

    public static UnicodeString substring(StringValue sv, NumericValue start, NumericValue len) {
        long lend;
        long llen;
        long lstart;
        int slength = sv.getStringLengthUpperBound();
        if (start instanceof Int64Value) {
            lstart = ((Int64Value)start).longValue();
            if (lstart > (long)slength) {
                return EmptyString.THE_INSTANCE;
            }
        } else {
            if (start.isNaN()) {
                return EmptyString.THE_INSTANCE;
            }
            if (start.compareTo(slength) > 0) {
                return EmptyString.THE_INSTANCE;
            }
            double dstart = start.getDoubleValue();
            long l = lstart = Double.isInfinite(dstart) ? -2147483647L : Math.round(dstart);
        }
        if (len instanceof Int64Value) {
            llen = ((Int64Value)len).longValue();
            if (llen <= 0L) {
                return EmptyString.THE_INSTANCE;
            }
        } else {
            if (len.isNaN()) {
                return EmptyString.THE_INSTANCE;
            }
            if (len.signum() <= 0) {
                return EmptyString.THE_INSTANCE;
            }
            double dlen = len.getDoubleValue();
            llen = Double.isInfinite(dlen) ? Integer.MAX_VALUE : Math.round(len.getDoubleValue());
        }
        if ((lend = lstart + llen) < lstart) {
            return EmptyString.THE_INSTANCE;
        }
        int a1 = (int)lstart - 1;
        UnicodeString us = sv.getUnicodeString();
        int clength = us.uLength();
        if (a1 >= clength) {
            return EmptyString.THE_INSTANCE;
        }
        int a2 = Math.min(clength, (int)lend - 1);
        if (a1 < 0) {
            if (a2 < 0) {
                return EmptyString.THE_INSTANCE;
            }
            a1 = 0;
        }
        return us.uSubstring(a1, a2);
    }

    @Override
    public ZeroOrOne call(XPathContext context, Sequence[] arguments) throws XPathException {
        StringValue arg0 = (StringValue)arguments[0].head();
        if (arg0 == null) {
            return One.string("");
        }
        NumericValue arg1 = (NumericValue)arguments[1].head();
        if (arguments.length == 2) {
            return new ZeroOrOne<StringValue>(StringValue.makeStringValue(Substring.substring(arg0, arg1)));
        }
        NumericValue arg2 = (NumericValue)arguments[2].head();
        return new ZeroOrOne<StringValue>(StringValue.makeStringValue(Substring.substring(arg0, arg1, arg2)));
    }

    @Override
    public String getCompilerName() {
        return "SubstringCompiler";
    }
}


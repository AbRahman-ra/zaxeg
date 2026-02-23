/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.SimpleNodeConstructor;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.functions.ScalarSystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.One;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;

public class String_1
extends ScalarSystemFunction {
    @Override
    public AtomicValue evaluate(Item arg, XPathContext context) throws XPathException {
        CharSequence result;
        try {
            result = arg.getStringValueCS();
        } catch (UnsupportedOperationException err) {
            throw new XPathException(err.getMessage(), "FOTY0014");
        }
        return new StringValue(result);
    }

    @Override
    public ZeroOrOne resultWhenEmpty() {
        return new One<StringValue>(StringValue.EMPTY_STRING);
    }

    @Override
    public Expression makeOptimizedFunctionCall(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo, Expression ... arguments) throws XPathException {
        Expression arg;
        TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
        if (th.isSubType((arg = arguments[0]).getItemType(), BuiltInAtomicType.STRING) && arg.getCardinality() == 16384) {
            return arg;
        }
        if (arg instanceof SimpleNodeConstructor) {
            return ((SimpleNodeConstructor)arg).getSelect();
        }
        return null;
    }

    @Override
    public String getCompilerName() {
        return "StringFnCompiler";
    }

    @Override
    public String getStreamerName() {
        return "StringFn";
    }
}


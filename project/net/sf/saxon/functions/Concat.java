/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.Arrays;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.oper.OperandArray;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.functions.PushableFunction;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.CharSequenceConsumer;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class Concat
extends SystemFunction
implements PushableFunction {
    @Override
    protected Sequence resultIfEmpty(int arg) {
        return null;
    }

    @Override
    public OperandRole[] getOperandRoles() {
        OperandRole[] roles = new OperandRole[this.getArity()];
        OperandRole operandRole = new OperandRole(0, OperandUsage.ABSORPTION);
        for (int i = 0; i < this.getArity(); ++i) {
            roles[i] = operandRole;
        }
        return roles;
    }

    @Override
    public FunctionItemType getFunctionItemType() {
        Object[] argTypes = new SequenceType[this.getArity()];
        Arrays.fill(argTypes, SequenceType.OPTIONAL_ATOMIC);
        return new SpecificFunctionType((SequenceType[])argTypes, SequenceType.SINGLE_STRING);
    }

    @Override
    public Expression makeOptimizedFunctionCall(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo, Expression ... arguments) throws XPathException {
        if (OperandArray.every(arguments, arg -> arg.getCardinality() == 16384 && arg.getItemType() == BuiltInAtomicType.BOOLEAN)) {
            visitor.getStaticContext().issueWarning("Did you intend to apply string concatenation to boolean operands? Perhaps you intended 'or' rather than '||'. To suppress this warning, use string() on the arguments.", arguments[0].getLocation());
        }
        return new SystemFunctionCall.Optimized(this, arguments){

            @Override
            public CharSequence evaluateAsString(XPathContext context) throws XPathException {
                FastStringBuffer buffer = new FastStringBuffer(256);
                for (Operand o : this.operands()) {
                    Item it = o.getChildExpression().evaluateItem(context);
                    if (it == null) continue;
                    buffer.cat(it.getStringValueCS());
                }
                return buffer;
            }

            @Override
            public Item evaluateItem(XPathContext context) throws XPathException {
                return new StringValue(this.evaluateAsString(context));
            }
        };
    }

    private boolean isSingleBoolean(Expression arg) {
        return arg.getCardinality() == 16384 && arg.getItemType() == BuiltInAtomicType.BOOLEAN;
    }

    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        FastStringBuffer fsb = new FastStringBuffer(64);
        for (Sequence arg : arguments) {
            Item item = arg.head();
            if (item == null) continue;
            fsb.cat(item.getStringValueCS());
        }
        return new StringValue(fsb);
    }

    @Override
    public void process(Outputter destination, XPathContext context, Sequence[] arguments) throws XPathException {
        CharSequenceConsumer output = destination.getStringReceiver(false);
        output.open();
        for (Sequence arg : arguments) {
            Item item = arg.head();
            if (item == null) continue;
            output.cat(item.getStringValueCS());
        }
        output.close();
    }

    @Override
    public SequenceType getRequiredType(int arg) {
        return this.getDetails().argumentTypes[0];
    }

    @Override
    public String getCompilerName() {
        return "ConcatCompiler";
    }
}


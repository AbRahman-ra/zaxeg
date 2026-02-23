/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.functions.Fold;
import net.sf.saxon.functions.FoldingFunction;
import net.sf.saxon.functions.PushableFunction;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.One;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.CharSequenceConsumer;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.Cardinality;

public class StringJoin
extends FoldingFunction
implements PushableFunction {
    private boolean returnEmptyIfEmpty;

    public void setReturnEmptyIfEmpty(boolean option) {
        this.returnEmptyIfEmpty = option;
    }

    public boolean isReturnEmptyIfEmpty() {
        return this.returnEmptyIfEmpty;
    }

    @Override
    public int getCardinality(Expression[] arguments) {
        if (this.returnEmptyIfEmpty) {
            return 24576;
        }
        return 16384;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof StringJoin && super.equals(o) && this.returnEmptyIfEmpty == ((StringJoin)o).returnEmptyIfEmpty;
    }

    @Override
    public Expression makeOptimizedFunctionCall(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo, Expression ... arguments) throws XPathException {
        Expression e2 = super.makeOptimizedFunctionCall(visitor, contextInfo, arguments);
        if (e2 != null) {
            return e2;
        }
        int card = arguments[0].getCardinality();
        if (!Cardinality.allowsMany(card)) {
            if (Cardinality.allowsZero(card) || arguments[0].getItemType().getPrimitiveItemType() != BuiltInAtomicType.STRING) {
                return SystemFunction.makeCall("string", this.getRetainedStaticContext(), arguments[0]);
            }
            return arguments[0];
        }
        return null;
    }

    @Override
    public Fold getFold(XPathContext context, Sequence ... additionalArguments) throws XPathException {
        CharSequence separator = "";
        if (additionalArguments.length > 0) {
            separator = additionalArguments[0].head().getStringValueCS();
        }
        return new StringJoinFold(separator);
    }

    @Override
    public void process(Outputter destination, XPathContext context, Sequence[] arguments) throws XPathException {
        Item it;
        String separator = arguments.length > 1 ? arguments[1].head().getStringValueCS() : "";
        CharSequenceConsumer output = destination.getStringReceiver(false);
        output.open();
        boolean first = true;
        SequenceIterator iter = arguments[0].iterate();
        while ((it = iter.next()) != null) {
            if (first) {
                first = false;
            } else {
                output.cat(separator);
            }
            output.cat(it.getStringValueCS());
        }
        output.close();
    }

    @Override
    public String getCompilerName() {
        return "StringJoinCompiler";
    }

    private class StringJoinFold
    implements Fold {
        private int position = 0;
        private CharSequence separator;
        private FastStringBuffer data;

        public StringJoinFold(CharSequence separator) {
            this.separator = separator;
            this.data = new FastStringBuffer(64);
        }

        @Override
        public void processItem(Item item) {
            if (this.position == 0) {
                this.data.cat(item.getStringValueCS());
                this.position = 1;
            } else {
                this.data.cat(this.separator).append(item.getStringValueCS());
            }
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public ZeroOrOne result() {
            if (this.position == 0 && StringJoin.this.returnEmptyIfEmpty) {
                return ZeroOrOne.empty();
            }
            return One.string(this.data.toString());
        }
    }
}


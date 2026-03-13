/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.hof;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.Fold;
import net.sf.saxon.functions.FoldingFunction;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyFunctionType;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;

public class FoldLeftFn
extends FoldingFunction {
    @Override
    public Fold getFold(XPathContext context, Sequence ... arguments) throws XPathException {
        Sequence arg0 = arguments[0];
        return new FoldLeftFold(context, arg0.materialize(), (Function)arguments[1].head());
    }

    @Override
    public ItemType getResultItemType(Expression[] args) {
        ItemType functionArgType = args[2].getItemType();
        if (functionArgType instanceof AnyFunctionType) {
            return ((AnyFunctionType)args[2].getItemType()).getResultType().getPrimaryType();
        }
        return AnyItemType.getInstance();
    }

    public class FoldLeftFold
    implements Fold {
        private XPathContext context;
        private Function function;
        private Sequence data;
        private int counter;

        public FoldLeftFold(XPathContext context, GroundedValue zero, Function function) {
            this.context = context;
            this.function = function;
            this.data = zero;
            this.counter = 0;
        }

        @Override
        public void processItem(Item item) throws XPathException {
            Sequence[] args = new Sequence[]{this.data, item};
            Sequence result = SystemFunction.dynamicCall(this.function, this.context, args);
            this.data = this.counter++ % 32 == 0 ? result.materialize() : result;
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public Sequence result() {
            return this.data;
        }
    }
}


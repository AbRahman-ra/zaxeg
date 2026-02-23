/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

public class BlockIterator
implements SequenceIterator {
    private Operand[] operanda;
    private int currentChildExpr = 0;
    private SequenceIterator currentIter;
    private XPathContext context;
    private int position = 0;

    public BlockIterator(Operand[] operanda, XPathContext context) {
        this.operanda = operanda;
        this.currentChildExpr = 0;
        this.context = context;
    }

    @Override
    public Item next() throws XPathException {
        if (this.position < 0) {
            return null;
        }
        do {
            Item current;
            if (this.currentIter == null) {
                this.currentIter = this.operanda[this.currentChildExpr++].getChildExpression().iterate(this.context);
            }
            if ((current = this.currentIter.next()) != null) {
                ++this.position;
                return current;
            }
            this.currentIter = null;
        } while (this.currentChildExpr < this.operanda.length);
        this.position = -1;
        return null;
    }

    @Override
    public void close() {
        if (this.currentIter != null) {
            this.currentIter.close();
        }
    }
}


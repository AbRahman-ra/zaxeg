/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.Properties;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.sort.DocumentOrderIterator;
import net.sf.saxon.expr.sort.GlobalOrderComparer;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.Navigator;

public class Outermost
extends SystemFunction {
    boolean presorted = false;

    @Override
    public Expression makeOptimizedFunctionCall(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo, Expression ... arguments) throws XPathException {
        if ((arguments[0].getSpecialProperties() & 0x80000) != 0) {
            return arguments[0];
        }
        this.presorted = (arguments[0].getSpecialProperties() & 0x20000) != 0;
        return null;
    }

    @Override
    public int getSpecialProperties(Expression[] arguments) {
        return 655360;
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        SequenceIterator in = arguments[0].iterate();
        if (!this.presorted) {
            in = new DocumentOrderIterator(in, GlobalOrderComparer.getInstance());
        }
        OutermostIterator out = new OutermostIterator(in);
        return SequenceTool.toLazySequence(out);
    }

    @Override
    public void exportAttributes(ExpressionPresenter out) {
        super.exportAttributes(out);
        if (this.presorted) {
            out.emitAttribute("flags", "p");
        }
    }

    @Override
    public void importAttributes(Properties attributes) throws XPathException {
        super.importAttributes(attributes);
        String flags = attributes.getProperty("flags");
        if (flags != null && flags.contains("p")) {
            this.presorted = true;
        }
    }

    @Override
    public String getStreamerName() {
        return "Outermost";
    }

    private class OutermostIterator
    implements SequenceIterator {
        SequenceIterator in;
        NodeInfo current = null;
        int position = 0;

        public OutermostIterator(SequenceIterator in) {
            this.in = in;
        }

        @Override
        public NodeInfo next() throws XPathException {
            NodeInfo next;
            do {
                if ((next = (NodeInfo)this.in.next()) != null) continue;
                this.current = null;
                this.position = -1;
                return null;
            } while (this.current != null && Navigator.isAncestorOrSelf(this.current, next));
            this.current = next;
            ++this.position;
            return this.current;
        }

        @Override
        public void close() {
            this.in.close();
        }
    }
}


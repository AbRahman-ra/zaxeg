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

public class Innermost
extends SystemFunction {
    boolean presorted = false;

    @Override
    public int getSpecialProperties(Expression[] arguments) {
        return 655360;
    }

    @Override
    public Expression makeOptimizedFunctionCall(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo, Expression ... arguments) throws XPathException {
        if ((arguments[0].getSpecialProperties() & 0x80000) != 0) {
            return arguments[0];
        }
        if ((arguments[0].getSpecialProperties() & 0x20000) != 0) {
            this.presorted = true;
        }
        return super.makeOptimizedFunctionCall(visitor, contextInfo, arguments);
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        return SequenceTool.toLazySequence(this.innermost(arguments[0].iterate()));
    }

    public SequenceIterator innermost(SequenceIterator in) throws XPathException {
        if (!this.presorted) {
            in = new DocumentOrderIterator(in, GlobalOrderComparer.getInstance());
        }
        return new InnermostIterator(in);
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

    private class InnermostIterator
    implements SequenceIterator {
        SequenceIterator in;
        NodeInfo pending = null;
        int position = 0;

        public InnermostIterator(SequenceIterator in) throws XPathException {
            this.in = in;
            this.pending = (NodeInfo)in.next();
        }

        @Override
        public NodeInfo next() throws XPathException {
            NodeInfo next;
            if (this.pending == null) {
                this.position = -1;
                return null;
            }
            while (true) {
                if ((next = (NodeInfo)this.in.next()) == null) {
                    NodeInfo current = this.pending;
                    ++this.position;
                    this.pending = null;
                    return current;
                }
                if (!Navigator.isAncestorOrSelf(this.pending, next)) break;
                this.pending = next;
            }
            ++this.position;
            NodeInfo current = this.pending;
            this.pending = next;
            return current;
        }

        @Override
        public void close() {
            this.in.close();
        }
    }
}


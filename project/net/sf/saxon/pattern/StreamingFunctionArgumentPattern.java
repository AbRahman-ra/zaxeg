/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.UType;

public class StreamingFunctionArgumentPattern
extends Pattern {
    private static StreamingFunctionArgumentPattern THE_INSTANCE = new StreamingFunctionArgumentPattern();

    public static StreamingFunctionArgumentPattern getInstance() {
        return THE_INSTANCE;
    }

    protected StreamingFunctionArgumentPattern() {
    }

    @Override
    public UType getUType() {
        return UType.ANY_NODE;
    }

    @Override
    public Pattern typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        return this;
    }

    @Override
    public boolean matches(Item item, XPathContext context) throws XPathException {
        Item j;
        Sequence arg = context.getStackFrame().getStackFrameValues()[0];
        SequenceIterator iter = arg.iterate();
        while ((j = iter.next()) != null) {
            if (j != item) continue;
            return true;
        }
        return false;
    }

    @Override
    public ItemType getItemType() {
        return AnyNodeTest.getInstance();
    }

    @Override
    public String reconstruct() {
        return "$$";
    }

    @Override
    public void export(ExpressionPresenter presenter) throws XPathException {
        presenter.startElement("p.streamingArg");
        presenter.endElement();
    }

    @Override
    public Pattern copy(RebindingMap rebindings) {
        return this;
    }
}


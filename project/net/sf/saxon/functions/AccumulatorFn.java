/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.accum.Accumulator;
import net.sf.saxon.expr.accum.AccumulatorManager;
import net.sf.saxon.expr.accum.AccumulatorRegistry;
import net.sf.saxon.expr.accum.IAccumulatorData;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XsltController;
import net.sf.saxon.type.ItemType;

public abstract class AccumulatorFn
extends SystemFunction {
    public abstract Phase getPhase();

    private Sequence getAccumulatorValue(String name, Phase phase, XPathContext context) throws XPathException {
        AccumulatorRegistry registry = this.getRetainedStaticContext().getPackageData().getAccumulatorRegistry();
        Accumulator accumulator = this.getAccumulator(name, registry);
        Item node = context.getContextItem();
        if (node == null) {
            throw new XPathException("No context item for evaluation of accumulator function", "XTDE3350", context);
        }
        if (!(node instanceof NodeInfo)) {
            throw new XPathException("Context item for evaluation of accumulator function must be a node", "XTTE3360", context);
        }
        int kind = ((NodeInfo)node).getNodeKind();
        if (kind == 2 || kind == 13) {
            throw new XPathException("Context item for evaluation of accumulator function must not be an attribute or namespace node", "XTTE3360", context);
        }
        Sequence streamedAccVal = registry.getStreamingAccumulatorValue((NodeInfo)node, accumulator, phase);
        if (streamedAccVal != null) {
            return streamedAccVal;
        }
        TreeInfo root = ((NodeInfo)node).getTreeInfo();
        XsltController controller = (XsltController)context.getController();
        if (!accumulator.isUniversallyApplicable() && !controller.getAccumulatorManager().isApplicable(root, accumulator)) {
            throw new XPathException("Accumulator " + name + " is not applicable to the current document", "XTDE3362");
        }
        AccumulatorManager manager = controller.getAccumulatorManager();
        IAccumulatorData data = manager.getAccumulatorData(root, accumulator, context);
        return data.getValue((NodeInfo)node, phase == Phase.AFTER);
    }

    private Accumulator getAccumulator(String name, AccumulatorRegistry registry) throws XPathException {
        Accumulator accumulator;
        StructuredQName qName;
        try {
            qName = StructuredQName.fromLexicalQName(name, false, true, this.getRetainedStaticContext());
        } catch (XPathException err) {
            throw new XPathException("Invalid accumulator name: " + err.getMessage(), "XTDE3340");
        }
        Accumulator accumulator2 = accumulator = registry == null ? null : registry.getAccumulator(qName);
        if (accumulator == null) {
            throw new XPathException("Accumulator " + name + " has not been declared", "XTDE3340");
        }
        return accumulator;
    }

    @Override
    public ItemType getResultItemType(Expression[] args) {
        try {
            if (args[0] instanceof StringLiteral) {
                AccumulatorRegistry registry = this.getRetainedStaticContext().getPackageData().getAccumulatorRegistry();
                Accumulator accumulator = this.getAccumulator(((StringLiteral)args[0]).getStringValue(), registry);
                return accumulator.getType().getPrimaryType();
            }
        } catch (Exception exception) {
            // empty catch block
        }
        return super.getResultItemType(args);
    }

    @Override
    public int getCardinality(Expression[] args) {
        try {
            if (args[0] instanceof StringLiteral) {
                AccumulatorRegistry registry = this.getRetainedStaticContext().getPackageData().getAccumulatorRegistry();
                Accumulator accumulator = this.getAccumulator(((StringLiteral)args[0]).getStringValue(), registry);
                return accumulator.getType().getCardinality();
            }
        } catch (Exception exception) {
            // empty catch block
        }
        return super.getCardinality(args);
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        String name = arguments[0].head().getStringValue();
        return this.getAccumulatorValue(name, this.getPhase(), context);
    }

    public static class AccumulatorAfter
    extends AccumulatorFn {
        @Override
        public Phase getPhase() {
            return Phase.AFTER;
        }

        @Override
        public String getStreamerName() {
            return "AccumulatorAfter";
        }
    }

    public static class AccumulatorBefore
    extends AccumulatorFn {
        @Override
        public Phase getPhase() {
            return Phase.BEFORE;
        }
    }

    public static enum Phase {
        AFTER,
        BEFORE;

    }
}


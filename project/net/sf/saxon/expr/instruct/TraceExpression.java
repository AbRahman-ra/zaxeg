/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.HashMap;
import java.util.Iterator;
import net.sf.saxon.Controller;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.PendingUpdateList;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trace.Traceable;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;

public class TraceExpression
extends Instruction
implements Traceable {
    private Operand baseOp;
    private HashMap<String, Object> properties = new HashMap(10);

    public TraceExpression(Expression child) {
        this.baseOp = new Operand(this, child, OperandRole.SAME_FOCUS_ACTION);
        this.adoptChildExpression(child);
        child.gatherProperties((k, v) -> this.properties.put((String)k, v));
    }

    public Expression getChild() {
        return this.baseOp.getChildExpression();
    }

    public Expression getBody() {
        return this.baseOp.getChildExpression();
    }

    @Override
    public Iterable<Operand> operands() {
        return this.baseOp;
    }

    public void setProperty(String name, Object value) {
        this.properties.put(name, value);
    }

    @Override
    public Object getProperty(String name) {
        return this.properties.get(name);
    }

    @Override
    public Iterator<String> getProperties() {
        return this.properties.keySet().iterator();
    }

    @Override
    public String getExpressionName() {
        return "trace";
    }

    @Override
    public String getStreamerName() {
        return "TraceExpr";
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        TraceExpression t = new TraceExpression(this.getChild().copy(rebindings));
        t.setLocation(this.getLocation());
        t.properties = this.properties;
        return t;
    }

    @Override
    public boolean isUpdatingExpression() {
        return this.getChild().isUpdatingExpression();
    }

    @Override
    public boolean isVacuousExpression() {
        return this.getChild().isVacuousExpression();
    }

    @Override
    public void checkForUpdatingSubexpressions() throws XPathException {
        this.getChild().checkForUpdatingSubexpressions();
    }

    @Override
    public int getImplementationMethod() {
        return this.getChild().getImplementationMethod();
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        Controller controller = context.getController();
        assert (controller != null);
        TraceListener listener = controller.getTraceListener();
        Expression child = this.getChild();
        if (controller.isTracing()) {
            assert (listener != null);
            listener.enter(child, this.properties, context);
            child.process(output, context);
            listener.leave(child);
        } else {
            child.process(output, context);
        }
        return null;
    }

    @Override
    public ItemType getItemType() {
        return this.getChild().getItemType();
    }

    @Override
    public int getCardinality() {
        return this.getChild().getCardinality();
    }

    @Override
    public int getDependencies() {
        return this.getChild().getDependencies();
    }

    @Override
    public final boolean mayCreateNewNodes() {
        return !this.getChild().hasSpecialProperty(0x800000);
    }

    @Override
    public int getNetCost() {
        return 0;
    }

    @Override
    public Item evaluateItem(XPathContext context) throws XPathException {
        Controller controller = context.getController();
        assert (controller != null);
        Expression child = this.getChild();
        if (controller.isTracing()) {
            TraceListener listener = controller.getTraceListener();
            listener.enter(child, this.properties, context);
            Item result = child.evaluateItem(context);
            listener.leave(child);
            return result;
        }
        return child.evaluateItem(context);
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        Controller controller = context.getController();
        assert (controller != null);
        Expression child = this.getChild();
        if (controller.isTracing()) {
            TraceListener listener = controller.getTraceListener();
            listener.enter(child, this.properties, context);
            SequenceIterator result = child.iterate(context);
            listener.leave(child);
            return result;
        }
        return child.iterate(context);
    }

    @Override
    public int getInstructionNameCode() {
        if (this.getChild() instanceof Instruction) {
            return ((Instruction)this.getChild()).getInstructionNameCode();
        }
        return -1;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        this.getChild().export(out);
    }

    @Override
    public void evaluatePendingUpdates(XPathContext context, PendingUpdateList pul) throws XPathException {
        Controller controller = context.getController();
        assert (controller != null);
        if (controller.isTracing()) {
            TraceListener listener = controller.getTraceListener();
            listener.enter(this.getChild(), this.properties, context);
            this.getChild().evaluatePendingUpdates(context, pul);
            listener.leave(this.getChild());
        } else {
            this.getChild().evaluatePendingUpdates(context, pul);
        }
    }

    @Override
    public String toShortString() {
        return this.getChild().toShortString();
    }
}


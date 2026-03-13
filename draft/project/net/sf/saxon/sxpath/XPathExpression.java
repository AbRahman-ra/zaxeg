/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.sxpath;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.sxpath.XPathDynamicContext;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;

public class XPathExpression {
    private StaticContext env;
    private Expression expression;
    private SlotManager stackFrameMap;
    private Executable executable;
    private int numberOfExternalVariables;

    protected XPathExpression(StaticContext env, Expression exp, Executable exec) {
        this.expression = exp;
        this.env = env;
        this.executable = exec;
    }

    public Executable getExecutable() {
        return this.executable;
    }

    protected void setStackFrameMap(SlotManager map, int numberOfExternalVariables) {
        this.stackFrameMap = map;
        this.numberOfExternalVariables = numberOfExternalVariables;
    }

    public XPathDynamicContext createDynamicContext() {
        XPathContextMajor context = new XPathContextMajor(null, this.executable);
        context.openStackFrame(this.stackFrameMap);
        return new XPathDynamicContext(this.env.getRequiredContextItemType(), context, this.stackFrameMap);
    }

    public XPathDynamicContext createDynamicContext(Item contextItem) throws XPathException {
        this.checkContextItemType(contextItem);
        XPathContextMajor context = new XPathContextMajor(contextItem, this.executable);
        context.openStackFrame(this.stackFrameMap);
        return new XPathDynamicContext(this.env.getRequiredContextItemType(), context, this.stackFrameMap);
    }

    public XPathDynamicContext createDynamicContext(Controller controller, Item contextItem) throws XPathException {
        this.checkContextItemType(contextItem);
        if (controller == null) {
            return this.createDynamicContext(contextItem);
        }
        XPathContextMajor context = controller.newXPathContext();
        context.openStackFrame(this.stackFrameMap);
        XPathDynamicContext dc = new XPathDynamicContext(this.env.getRequiredContextItemType(), context, this.stackFrameMap);
        if (contextItem != null) {
            dc.setContextItem(contextItem);
        }
        return dc;
    }

    private void checkContextItemType(Item contextItem) throws XPathException {
        TypeHierarchy th;
        ItemType type;
        if (contextItem != null && !(type = this.env.getRequiredContextItemType()).matches(contextItem, th = this.env.getConfiguration().getTypeHierarchy())) {
            throw new XPathException("Supplied context item does not match required context item type " + type);
        }
    }

    public SequenceIterator iterate(XPathDynamicContext context) throws XPathException {
        context.checkExternalVariables(this.stackFrameMap, this.numberOfExternalVariables);
        return this.expression.iterate(context.getXPathContextObject());
    }

    public List<Item> evaluate(XPathDynamicContext context) throws XPathException {
        ArrayList<Item> list = new ArrayList<Item>(20);
        this.expression.iterate(context.getXPathContextObject()).forEachOrFail(list::add);
        return list;
    }

    public Item evaluateSingle(XPathDynamicContext context) throws XPathException {
        SequenceIterator iter = this.expression.iterate(context.getXPathContextObject());
        Item result = iter.next();
        iter.close();
        return result;
    }

    public boolean effectiveBooleanValue(XPathDynamicContext context) throws XPathException {
        return this.expression.effectiveBooleanValue(context.getXPathContextObject());
    }

    public Expression getInternalExpression() {
        return this.expression;
    }
}


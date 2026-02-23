/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.CastExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.instruct.ValueOf;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.functions.String_1;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.tree.util.Orphan;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.Cardinality;

public abstract class SimpleNodeConstructor
extends Instruction {
    protected Operand selectOp;

    public SimpleNodeConstructor() {
        Literal select = Literal.makeEmptySequence();
        this.selectOp = new Operand(this, select, OperandRole.SINGLE_ATOMIC);
    }

    public void setSelect(Expression select) {
        this.selectOp.setChildExpression(select);
    }

    public Expression getSelect() {
        return this.selectOp.getChildExpression();
    }

    @Override
    public Iterable<Operand> operands() {
        return this.selectOp;
    }

    @Override
    public final boolean mayCreateNewNodes() {
        return true;
    }

    @Override
    public boolean alwaysCreatesNewNodes() {
        return true;
    }

    @Override
    public int computeCardinality() {
        return this.getSelect().getCardinality();
    }

    @Override
    public int computeSpecialProperties() {
        return super.computeSpecialProperties() | 0x1000000;
    }

    @Override
    public boolean allowExtractingCommonSubexpressions() {
        return false;
    }

    public abstract void localTypeCheck(ExpressionVisitor var1, ContextItemStaticInfo var2) throws XPathException;

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Expression arg;
        Expression valSelect;
        this.typeCheckChildren(visitor, contextInfo);
        this.localTypeCheck(visitor, contextInfo);
        TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
        if (this.getSelect() instanceof ValueOf && th.isSubType((valSelect = ((ValueOf)this.getSelect()).getSelect()).getItemType(), BuiltInAtomicType.STRING) && !Cardinality.allowsMany(valSelect.getCardinality())) {
            this.setSelect(valSelect);
        }
        if (this.getSelect().isCallOn(String_1.class)) {
            SystemFunctionCall fn = (SystemFunctionCall)this.getSelect();
            Expression arg2 = fn.getArg(0);
            if (arg2.getItemType() == BuiltInAtomicType.UNTYPED_ATOMIC && !Cardinality.allowsMany(arg2.getCardinality())) {
                this.setSelect(arg2);
            }
        } else if (this.getSelect() instanceof CastExpression && ((CastExpression)this.getSelect()).getTargetType() == BuiltInAtomicType.STRING && (arg = ((CastExpression)this.getSelect()).getBaseExpression()).getItemType() == BuiltInAtomicType.UNTYPED_ATOMIC && !Cardinality.allowsMany(arg.getCardinality())) {
            this.setSelect(arg);
        }
        this.adoptChildExpression(this.getSelect());
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        this.optimizeChildren(visitor, contextItemType);
        if (this.getSelect().isCallOn(String_1.class)) {
            SystemFunctionCall sf = (SystemFunctionCall)this.getSelect();
            TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
            if (th.isSubType(sf.getArg(0).getItemType(), BuiltInAtomicType.STRING) && !Cardinality.allowsMany(sf.getArg(0).getCardinality())) {
                this.setSelect(sf.getArg(0));
            }
        }
        return this;
    }

    @Override
    public String getStreamerName() {
        return "SimpleNodeConstructor";
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        CharSequence value = this.getSelect().evaluateAsString(context);
        try {
            this.processValue(value, output, context);
        } catch (XPathException e) {
            e.maybeSetLocation(this.getLocation());
            throw e;
        }
        return null;
    }

    public abstract void processValue(CharSequence var1, Outputter var2, XPathContext var3) throws XPathException;

    @Override
    public Item evaluateItem(XPathContext context) throws XPathException {
        String content;
        Item contentItem = this.getSelect().evaluateItem(context);
        if (contentItem == null) {
            content = "";
        } else {
            content = contentItem.getStringValue();
            content = this.checkContent(content, context);
        }
        Orphan o = new Orphan(context.getConfiguration());
        o.setNodeKind((short)this.getItemType().getPrimitiveType());
        o.setStringValue(content);
        o.setNodeName(this.evaluateNodeName(context));
        return o;
    }

    protected String checkContent(String data, XPathContext context) throws XPathException {
        return data;
    }

    public NodeName evaluateNodeName(XPathContext context) throws XPathException {
        return null;
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        return SingletonIterator.makeIterator(this.evaluateItem(context));
    }

    public boolean isLocal() {
        return ExpressionTool.isLocalConstructor(this);
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import javax.xml.transform.SourceLocator;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.ParameterSet;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.instruct.TailCallReturner;
import net.sf.saxon.expr.instruct.TerminationException;
import net.sf.saxon.expr.instruct.WithParam;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;

public abstract class Instruction
extends Expression
implements TailCallReturner {
    @Override
    public int getImplementationMethod() {
        return 4;
    }

    @Override
    public boolean isInstruction() {
        return true;
    }

    public int getInstructionNameCode() {
        return -1;
    }

    @Override
    public String getExpressionName() {
        int code = this.getInstructionNameCode();
        if (code >= 0 & code < 1024) {
            return StandardNames.getDisplayName(code);
        }
        return super.getExpressionName();
    }

    @Override
    public ItemType getItemType() {
        return Type.ITEM_TYPE;
    }

    @Override
    public int computeCardinality() {
        return 57344;
    }

    @Override
    public abstract Iterable<Operand> operands();

    @Override
    public abstract TailCall processLeavingTail(Outputter var1, XPathContext var2) throws XPathException;

    @Override
    public void process(Outputter output, XPathContext context) throws XPathException {
        try {
            for (TailCall tc = this.processLeavingTail(output, context); tc != null; tc = tc.processLeavingTail()) {
            }
        } catch (XPathException err) {
            err.maybeSetFailingExpression(this);
            err.maybeSetContext(context);
            throw err;
        }
    }

    public SourceLocator getSourceLocator() {
        return this.getLocation();
    }

    protected static XPathException dynamicError(Location loc, XPathException error, XPathContext context) {
        if (error instanceof TerminationException) {
            return error;
        }
        error.maybeSetLocation(loc);
        error.maybeSetContext(context);
        return error;
    }

    public static ParameterSet assembleParams(XPathContext context, WithParam[] actualParams) throws XPathException {
        if (actualParams == null || actualParams.length == 0) {
            return null;
        }
        ParameterSet params = new ParameterSet(actualParams.length);
        for (WithParam actualParam : actualParams) {
            params.put(actualParam.getVariableQName(), actualParam.getSelectValue(context), actualParam.isTypeChecked());
        }
        return params;
    }

    public static ParameterSet assembleTunnelParams(XPathContext context, WithParam[] actualParams) throws XPathException {
        ParameterSet existingParams = context.getTunnelParameters();
        if (existingParams == null) {
            return Instruction.assembleParams(context, actualParams);
        }
        if (actualParams == null || actualParams.length == 0) {
            return existingParams;
        }
        ParameterSet newParams = new ParameterSet(existingParams, actualParams.length);
        for (WithParam actualParam : actualParams) {
            newParams.put(actualParam.getVariableQName(), actualParam.getSelectValue(context), false);
        }
        return newParams;
    }

    @Override
    public int computeSpecialProperties() {
        int p = super.computeSpecialProperties();
        if (this.alwaysCreatesNewNodes()) {
            p |= 0x400000;
        }
        if (this.mayCreateNewNodes()) {
            return p;
        }
        return p | 0x800000;
    }

    @Override
    public int getNetCost() {
        return 20;
    }

    public boolean mayCreateNewNodes() {
        return false;
    }

    public boolean alwaysCreatesNewNodes() {
        return false;
    }

    protected final boolean someOperandCreatesNewNodes() {
        for (Operand o : this.operands()) {
            Expression child = o.getChildExpression();
            int props = child.getSpecialProperties();
            if ((props & 0x800000) != 0) continue;
            return true;
        }
        return false;
    }

    @Override
    public Item evaluateItem(XPathContext context) throws XPathException {
        int m = this.getImplementationMethod();
        if ((m & 1) != 0) {
            throw new AssertionError((Object)("evaluateItem() is not implemented in the subclass " + this.getClass()));
        }
        if ((m & 2) != 0) {
            return this.iterate(context).next();
        }
        return ExpressionTool.getItemFromProcessMethod(this, context);
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        int m = this.getImplementationMethod();
        if ((m & 1) != 0) {
            Item item = this.evaluateItem(context);
            if (item == null) {
                return EmptyIterator.emptyIterator();
            }
            return SingletonIterator.makeIterator(item);
        }
        if ((m & 2) != 0) {
            throw new AssertionError((Object)("iterate() is not implemented in the subclass " + this.getClass()));
        }
        return ExpressionTool.getIteratorFromProcessMethod(this, context);
    }

    @Override
    public final CharSequence evaluateAsString(XPathContext context) throws XPathException {
        Item item = this.evaluateItem(context);
        if (item == null) {
            return "";
        }
        return item.getStringValue();
    }

    public boolean isXSLT() {
        return this.getPackageData().isXSLT();
    }
}


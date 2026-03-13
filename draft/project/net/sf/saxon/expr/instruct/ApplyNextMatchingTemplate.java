/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.ArrayList;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.instruct.ITemplateCall;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.WithParam;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.trans.XPathException;

public abstract class ApplyNextMatchingTemplate
extends Instruction
implements ITemplateCall {
    private WithParam[] actualParams;
    private WithParam[] tunnelParams;

    @Override
    public int getImplementationMethod() {
        return super.getImplementationMethod() | 8;
    }

    @Override
    public WithParam[] getActualParams() {
        return this.actualParams;
    }

    @Override
    public WithParam[] getTunnelParams() {
        return this.tunnelParams;
    }

    public void setActualParams(WithParam[] params) {
        this.actualParams = params;
    }

    public void setTunnelParams(WithParam[] params) {
        this.tunnelParams = params;
    }

    @Override
    public Iterable<Operand> operands() {
        ArrayList<Operand> operanda = new ArrayList<Operand>(this.actualParams.length + this.tunnelParams.length);
        WithParam.gatherOperands(this, this.actualParams, operanda);
        WithParam.gatherOperands(this, this.tunnelParams, operanda);
        return operanda;
    }

    @Override
    public Expression simplify() throws XPathException {
        WithParam.simplify(this.getActualParams());
        WithParam.simplify(this.getTunnelParams());
        return this;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        WithParam.typeCheck(this.actualParams, visitor, contextInfo);
        WithParam.typeCheck(this.tunnelParams, visitor, contextInfo);
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        WithParam.optimize(visitor, this.actualParams, contextInfo);
        WithParam.optimize(visitor, this.tunnelParams, contextInfo);
        return this;
    }

    @Override
    public int getIntrinsicDependencies() {
        return 2;
    }

    @Override
    public final boolean mayCreateNewNodes() {
        return true;
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        if (pathMapNodeSet == null) {
            ContextItemExpression cie = new ContextItemExpression();
            pathMapNodeSet = new PathMap.PathMapNodeSet(pathMap.makeNewRoot(cie));
        }
        pathMapNodeSet.addDescendants();
        return new PathMap.PathMapNodeSet(pathMap.makeNewRoot(this));
    }
}


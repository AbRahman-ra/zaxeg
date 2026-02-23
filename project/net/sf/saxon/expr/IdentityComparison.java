/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.BinaryExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.expr.sort.GlobalOrderComparer;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.SequenceType;

public final class IdentityComparison
extends BinaryExpression {
    private boolean generateIdEmulation = false;

    public IdentityComparison(Expression p1, int op, Expression p2) {
        super(p1, op, p2);
    }

    public void setGenerateIdEmulation(boolean flag) {
        this.generateIdEmulation = flag;
    }

    public boolean isGenerateIdEmulation() {
        return this.generateIdEmulation;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getLhs().typeCheck(visitor, contextInfo);
        this.getRhs().typeCheck(visitor, contextInfo);
        if (!this.generateIdEmulation && (Literal.isEmptySequence(this.getLhsExpression()) || Literal.isEmptySequence(this.getRhsExpression()))) {
            return Literal.makeEmptySequence();
        }
        RoleDiagnostic role0 = new RoleDiagnostic(1, Token.tokens[this.operator], 0);
        TypeChecker tc = visitor.getConfiguration().getTypeChecker(false);
        this.setLhsExpression(tc.staticTypeCheck(this.getLhsExpression(), SequenceType.OPTIONAL_NODE, role0, visitor));
        RoleDiagnostic role1 = new RoleDiagnostic(1, Token.tokens[this.operator], 1);
        this.setRhsExpression(tc.staticTypeCheck(this.getRhsExpression(), SequenceType.OPTIONAL_NODE, role1, visitor));
        if (!Cardinality.allowsZero(this.getLhsExpression().getCardinality()) && !Cardinality.allowsZero(this.getRhsExpression().getCardinality())) {
            this.generateIdEmulation = false;
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        Expression r = super.optimize(visitor, contextItemType);
        if (r != this && !this.generateIdEmulation && (Literal.isEmptySequence(this.getLhsExpression()) || Literal.isEmptySequence(this.getRhsExpression()))) {
            return Literal.makeEmptySequence();
        }
        return r;
    }

    @Override
    protected OperandRole getOperandRole(int arg) {
        return OperandRole.INSPECT;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        IdentityComparison ic = new IdentityComparison(this.getLhsExpression().copy(rebindings), this.operator, this.getRhsExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, ic);
        ic.generateIdEmulation = this.generateIdEmulation;
        return ic;
    }

    @Override
    protected String tag() {
        switch (this.operator) {
            case 20: {
                return "is";
            }
            case 38: {
                return "precedes";
            }
            case 39: {
                return "follows";
            }
        }
        return "?";
    }

    @Override
    public BooleanValue evaluateItem(XPathContext context) throws XPathException {
        NodeInfo node0 = IdentityComparison.getNode(this.getLhsExpression(), context);
        if (node0 == null) {
            if (this.generateIdEmulation) {
                return BooleanValue.get(IdentityComparison.getNode(this.getRhsExpression(), context) == null);
            }
            return null;
        }
        NodeInfo node1 = IdentityComparison.getNode(this.getRhsExpression(), context);
        if (node1 == null) {
            if (this.generateIdEmulation) {
                return BooleanValue.FALSE;
            }
            return null;
        }
        return BooleanValue.get(this.compareIdentity(node0, node1));
    }

    @Override
    public boolean effectiveBooleanValue(XPathContext context) throws XPathException {
        NodeInfo node0 = IdentityComparison.getNode(this.getLhsExpression(), context);
        if (node0 == null) {
            return this.generateIdEmulation && IdentityComparison.getNode(this.getRhsExpression(), context) == null;
        }
        NodeInfo node1 = IdentityComparison.getNode(this.getRhsExpression(), context);
        return node1 != null && this.compareIdentity(node0, node1);
    }

    private boolean compareIdentity(NodeInfo node0, NodeInfo node1) {
        switch (this.operator) {
            case 20: {
                return node0.equals(node1);
            }
            case 38: {
                return GlobalOrderComparer.getInstance().compare(node0, node1) < 0;
            }
            case 39: {
                return GlobalOrderComparer.getInstance().compare(node0, node1) > 0;
            }
        }
        throw new UnsupportedOperationException("Unknown node identity test");
    }

    private static NodeInfo getNode(Expression exp, XPathContext c) throws XPathException {
        return (NodeInfo)exp.evaluateItem(c);
    }

    @Override
    public ItemType getItemType() {
        return BuiltInAtomicType.BOOLEAN;
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        return UType.BOOLEAN;
    }

    @Override
    public String getExpressionName() {
        return "nodeComparison";
    }
}


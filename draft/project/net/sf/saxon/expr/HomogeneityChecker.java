/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.UnaryExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.sort.DocumentSorter;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.HomogeneityCheckerIterator;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;

public class HomogeneityChecker
extends UnaryExpression {
    public HomogeneityChecker(Expression base) {
        super(base);
    }

    @Override
    protected OperandRole getOperandRole() {
        return OperandRole.INSPECT;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        if (this.getBaseExpression() instanceof HomogeneityChecker) {
            return this.getBaseExpression().typeCheck(visitor, contextInfo);
        }
        this.getOperand().typeCheck(visitor, contextInfo);
        TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
        ItemType type = this.getBaseExpression().getItemType();
        if (type.equals(ErrorType.getInstance())) {
            return Literal.makeEmptySequence();
        }
        Affinity rel = th.relationship(type, AnyNodeTest.getInstance());
        if (rel == Affinity.DISJOINT) {
            return this.getBaseExpression();
        }
        if (rel == Affinity.SAME_TYPE || rel == Affinity.SUBSUMED_BY) {
            Expression savedBase = this.getBaseExpression();
            Expression parent = this.getParentExpression();
            this.getOperand().detachChild();
            DocumentSorter ds = new DocumentSorter(savedBase);
            ExpressionTool.copyLocationInfo(this, ds);
            ds.setParentExpression(parent);
            return ds;
        }
        return this;
    }

    @Override
    public Pattern toPattern(Configuration config) throws XPathException {
        return this.getBaseExpression().toPattern(config);
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        if (this.getBaseExpression() instanceof HomogeneityChecker) {
            return this.getBaseExpression().optimize(visitor, contextInfo);
        }
        return super.optimize(visitor, contextInfo);
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        HomogeneityChecker hc = new HomogeneityChecker(this.getBaseExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, hc);
        return hc;
    }

    @Override
    public int getImplementationMethod() {
        return 2;
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        SequenceIterator base = this.getBaseExpression().iterate(context);
        return new HomogeneityCheckerIterator(base, this.getLocation());
    }

    @Override
    public String getExpressionName() {
        return "homCheck";
    }
}


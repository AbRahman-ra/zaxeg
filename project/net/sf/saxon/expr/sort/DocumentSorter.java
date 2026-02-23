/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FirstItemExpression;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.SlashExpression;
import net.sf.saxon.expr.UnaryExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.sort.ConditionalSorter;
import net.sf.saxon.expr.sort.DocumentOrderIterator;
import net.sf.saxon.expr.sort.GlobalOrderComparer;
import net.sf.saxon.expr.sort.ItemOrderComparer;
import net.sf.saxon.expr.sort.LocalOrderComparer;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.SequenceType;

public class DocumentSorter
extends UnaryExpression {
    private ItemOrderComparer comparer;

    public DocumentSorter(Expression base) {
        super(base);
        int props = base.getSpecialProperties();
        this.comparer = (props & 0x10000) != 0 || (props & 0x1000000) != 0 ? LocalOrderComparer.getInstance() : GlobalOrderComparer.getInstance();
    }

    public DocumentSorter(Expression base, boolean intraDocument) {
        super(base);
        this.comparer = intraDocument ? LocalOrderComparer.getInstance() : GlobalOrderComparer.getInstance();
    }

    @Override
    protected OperandRole getOperandRole() {
        return OperandRole.SAME_FOCUS_ACTION;
    }

    @Override
    public String getExpressionName() {
        return "docOrder";
    }

    public ItemOrderComparer getComparer() {
        return this.comparer;
    }

    @Override
    public Expression simplify() throws XPathException {
        Expression operand = this.getBaseExpression().simplify();
        if (operand.hasSpecialProperty(131072)) {
            return operand;
        }
        return this;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Expression e2 = super.typeCheck(visitor, contextInfo);
        if (e2 != this) {
            return e2;
        }
        TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
        if (th.relationship(this.getBaseExpression().getItemType(), AnyNodeTest.getInstance()) == Affinity.DISJOINT) {
            return this.getBaseExpression();
        }
        RoleDiagnostic role = new RoleDiagnostic(20, "document-order sorter", 0);
        Expression operand = visitor.getConfiguration().getTypeChecker(false).staticTypeCheck(this.getBaseExpression(), SequenceType.NODE_SEQUENCE, role, visitor);
        this.setBaseExpression(operand);
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getOperand().optimize(visitor, contextInfo);
        Expression sortable = this.getBaseExpression();
        boolean tryHarder = sortable.isStaticPropertiesKnown();
        while (true) {
            if (sortable.hasSpecialProperty(131072)) {
                return sortable;
            }
            if (!Cardinality.allowsMany(sortable.getCardinality())) {
                return sortable;
            }
            if (sortable instanceof SlashExpression) {
                SlashExpression slash = (SlashExpression)sortable;
                Expression lhs = slash.getLhsExpression();
                Expression rhs = slash.getRhsExpression();
                if (lhs instanceof ConditionalSorter && slash.getRhsExpression().hasSpecialProperty(524288)) {
                    ConditionalSorter c = (ConditionalSorter)lhs;
                    DocumentSorter d = c.getDocumentSorter();
                    Expression condition = c.getCondition();
                    Expression s = new SlashExpression(d.getBaseExpression(), rhs);
                    s = ((Expression)s).optimize(visitor, contextInfo);
                    return new ConditionalSorter(condition, new DocumentSorter(s));
                }
                if (lhs instanceof DocumentSorter && rhs instanceof AxisExpression && ((AxisExpression)rhs).getAxis() == 3) {
                    SlashExpression s1 = new SlashExpression(((DocumentSorter)lhs).getBaseExpression(), rhs);
                    ExpressionTool.copyLocationInfo(this, s1);
                    return new DocumentSorter(s1).optimize(visitor, contextInfo);
                }
                if (!ExpressionTool.dependsOnFocus(rhs) && !rhs.hasSpecialProperty(0x2000000) && rhs.hasSpecialProperty(0x800000)) {
                    Expression e1 = FirstItemExpression.makeFirstItemExpression(slash.getLhsExpression());
                    DocumentSorter e2 = new DocumentSorter(slash.getRhsExpression());
                    SlashExpression e3 = new SlashExpression(e1, e2);
                    ExpressionTool.copyLocationInfo(this, e3);
                    return e3.optimize(visitor, contextInfo);
                }
            }
            if (!tryHarder) break;
            sortable.resetLocalStaticProperties();
            tryHarder = false;
        }
        if (sortable instanceof SlashExpression && !visitor.isOptimizeForStreaming() && !(this.getParentExpression() instanceof ConditionalSorter)) {
            return visitor.obtainOptimizer().makeConditionalDocumentSorter(this, (SlashExpression)sortable);
        }
        return this;
    }

    @Override
    public int getNetCost() {
        return 30;
    }

    @Override
    public Expression unordered(boolean retainAllNodes, boolean forStreaming) throws XPathException {
        Expression operand = this.getBaseExpression().unordered(retainAllNodes, forStreaming);
        if (operand.hasSpecialProperty(131072)) {
            return operand;
        }
        if (!retainAllNodes) {
            return operand;
        }
        if (operand instanceof SlashExpression) {
            SlashExpression exp = (SlashExpression)operand;
            Expression a = exp.getSelectExpression();
            Expression b = exp.getActionExpression();
            a = ExpressionTool.unfilteredExpression(a, false);
            b = ExpressionTool.unfilteredExpression(b, false);
            if (a instanceof AxisExpression && (((AxisExpression)a).getAxis() == 4 || ((AxisExpression)a).getAxis() == 5) && b instanceof AxisExpression && ((AxisExpression)b).getAxis() == 3) {
                return operand.unordered(retainAllNodes, false);
            }
        }
        this.setBaseExpression(operand);
        return this;
    }

    @Override
    public int computeSpecialProperties() {
        return this.getBaseExpression().getSpecialProperties() | 0x20000;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        DocumentSorter ds = new DocumentSorter(this.getBaseExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, ds);
        return ds;
    }

    @Override
    public Pattern toPattern(Configuration config) throws XPathException {
        return this.getBaseExpression().toPattern(config);
    }

    @Override
    public int getImplementationMethod() {
        return 2;
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        return new DocumentOrderIterator(this.getBaseExpression().iterate(context), this.comparer);
    }

    @Override
    public boolean effectiveBooleanValue(XPathContext context) throws XPathException {
        return this.getBaseExpression().effectiveBooleanValue(context);
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("docOrder", this);
        out.emitAttribute("intra", this.comparer instanceof LocalOrderComparer ? "1" : "0");
        this.getBaseExpression().export(out);
        out.endElement();
    }

    @Override
    public String getStreamerName() {
        return "DocumentSorterAdjunct";
    }
}


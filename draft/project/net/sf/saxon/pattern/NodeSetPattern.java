/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.GlobalVariableReference;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.SingletonIntersectExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMinor;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ManualIterator;
import net.sf.saxon.type.AlphaCode;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.SequenceType;

public class NodeSetPattern
extends Pattern {
    private Operand selectionOp;
    private ItemType itemType;

    public NodeSetPattern(Expression exp) {
        this.selectionOp = new Operand(this, exp, OperandRole.NAVIGATE);
    }

    @Override
    public Iterable<Operand> operands() {
        return this.selectionOp;
    }

    public Expression getSelectionExpression() {
        return this.selectionOp.getChildExpression();
    }

    @Override
    public Pattern typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        this.selectionOp.setChildExpression(this.getSelectionExpression().typeCheck(visitor, contextItemType));
        RoleDiagnostic role = new RoleDiagnostic(19, this.getSelectionExpression().toString(), 0);
        TypeChecker tc = visitor.getConfiguration().getTypeChecker(false);
        Expression checked = this.getSelectionExpression();
        try {
            tc.staticTypeCheck(this.getSelectionExpression(), SequenceType.NODE_SEQUENCE, role, visitor);
        } catch (XPathException e) {
            visitor.issueWarning("Pattern will never match anything. " + e.getMessage(), this.getLocation());
            checked = Literal.makeEmptySequence();
        }
        this.selectionOp.setChildExpression(checked);
        this.itemType = this.getSelectionExpression().getItemType();
        return this;
    }

    @Override
    public Pattern optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        visitor.obtainOptimizer().optimizeNodeSetPattern(this);
        return this;
    }

    public void setItemType(ItemType type) {
        this.itemType = type;
    }

    @Override
    public int getDependencies() {
        return this.getSelectionExpression().getDependencies();
    }

    @Override
    public int allocateSlots(SlotManager slotManager, int nextFree) {
        return ExpressionTool.allocateSlots(this.getSelectionExpression(), nextFree, slotManager);
    }

    @Override
    public SequenceIterator selectNodes(TreeInfo doc, XPathContext context) throws XPathException {
        XPathContextMinor c2 = context.newMinorContext();
        ManualIterator mi = new ManualIterator(doc.getRootNode());
        c2.setCurrentIterator(mi);
        return this.getSelectionExpression().iterate(c2);
    }

    @Override
    public boolean matches(Item item, XPathContext context) throws XPathException {
        if (item instanceof NodeInfo) {
            Expression exp = this.getSelectionExpression();
            if (exp instanceof GlobalVariableReference) {
                GroundedValue value = ((GlobalVariableReference)exp).evaluateVariable(context);
                return value.containsNode((NodeInfo)item);
            }
            SequenceIterator iter = exp.iterate(context);
            return SingletonIntersectExpression.containsNode(iter, (NodeInfo)item);
        }
        return false;
    }

    @Override
    public UType getUType() {
        return this.getItemType().getUType();
    }

    @Override
    public ItemType getItemType() {
        if (this.itemType == null) {
            this.itemType = this.getSelectionExpression().getItemType();
        }
        if (this.itemType instanceof NodeTest) {
            return this.itemType;
        }
        return AnyNodeTest.getInstance();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof NodeSetPattern && ((NodeSetPattern)other).getSelectionExpression().isEqual(this.getSelectionExpression());
    }

    @Override
    public int computeHashCode() {
        return 0x73108728 ^ this.getSelectionExpression().hashCode();
    }

    @Override
    public Pattern copy(RebindingMap rebindings) {
        NodeSetPattern n = new NodeSetPattern(this.getSelectionExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, n);
        n.setOriginalText(this.getOriginalText());
        return n;
    }

    @Override
    public void export(ExpressionPresenter presenter) throws XPathException {
        presenter.startElement("p.nodeSet");
        if (this.itemType != null) {
            presenter.emitAttribute("test", AlphaCode.fromItemType(this.itemType));
        }
        this.getSelectionExpression().export(presenter);
        presenter.endElement();
    }
}


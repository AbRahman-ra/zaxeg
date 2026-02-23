/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.CardinalityChecker;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FirstItemExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.PendingUpdateList;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.BlockIterator;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.LocalParam;
import net.sf.saxon.expr.instruct.Message;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.instruct.TailCallReturner;
import net.sf.saxon.expr.instruct.ValueOf;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.iter.UnfailingIterator;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.IntegerRange;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.SequenceType;

public class Block
extends Instruction {
    private final Operand[] operanda;
    private boolean allNodesUntyped;

    public Block(Expression[] children) {
        this.operanda = new Operand[children.length];
        for (int i = 0; i < children.length; ++i) {
            this.operanda[i] = new Operand(this, children[i], OperandRole.SAME_FOCUS_ACTION);
        }
        for (Expression e : children) {
            this.adoptChildExpression(e);
        }
    }

    @Override
    public boolean isInstruction() {
        return false;
    }

    private Expression child(int n) {
        return this.operanda[n].getChildExpression();
    }

    private void setChild(int n, Expression child) {
        this.operanda[n].setChildExpression(child);
    }

    private int size() {
        return this.operanda.length;
    }

    @Override
    public Iterable<Operand> operands() {
        return Arrays.asList(this.operanda);
    }

    @Override
    public boolean hasVariableBinding(Binding binding) {
        if (binding instanceof LocalParam) {
            for (Operand o : this.operanda) {
                if (o.getChildExpression() != binding) continue;
                return true;
            }
        }
        return false;
    }

    public static Expression makeBlock(Expression e1, Expression e2) {
        if (e1 == null || Literal.isEmptySequence(e1)) {
            return e2;
        }
        if (e2 == null || Literal.isEmptySequence(e2)) {
            return e1;
        }
        if (e1 instanceof Block || e2 instanceof Block) {
            ArrayList<Expression> list = new ArrayList<Expression>(10);
            if (e1 instanceof Block) {
                for (Operand o : e1.operands()) {
                    list.add(o.getChildExpression());
                }
            } else {
                list.add(e1);
            }
            if (e2 instanceof Block) {
                for (Operand o : e2.operands()) {
                    list.add(o.getChildExpression());
                }
            } else {
                list.add(e2);
            }
            Expression[] exps = new Expression[list.size()];
            exps = list.toArray(exps);
            return new Block(exps);
        }
        Expression[] exps = new Expression[]{e1, e2};
        return new Block(exps);
    }

    public static Expression makeBlock(List<Expression> list) {
        if (list.isEmpty()) {
            return Literal.makeEmptySequence();
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        Expression[] exps = new Expression[list.size()];
        exps = list.toArray(exps);
        return new Block(exps);
    }

    @Override
    public String getExpressionName() {
        return "sequence";
    }

    public Operand[] getOperanda() {
        return this.operanda;
    }

    @Override
    public int computeSpecialProperties() {
        if (this.size() == 0) {
            return 0xDFF0000;
        }
        int p = super.computeSpecialProperties();
        if (this.allNodesUntyped) {
            p |= 0x8000000;
        }
        boolean allAxisExpressions = true;
        boolean allChildAxis = true;
        boolean allSubtreeAxis = true;
        for (Operand o : this.operands()) {
            Expression child = o.getChildExpression();
            if (!(child instanceof AxisExpression)) {
                allAxisExpressions = false;
                allChildAxis = false;
                allSubtreeAxis = false;
                break;
            }
            int axis = ((AxisExpression)child).getAxis();
            if (axis != 3) {
                allChildAxis = false;
            }
            if (AxisInfo.isSubtreeAxis[axis]) continue;
            allSubtreeAxis = false;
        }
        if (allAxisExpressions) {
            p |= 0x1810000;
            if (allChildAxis) {
                p |= 0x80000;
            }
            if (allSubtreeAxis) {
                p |= 0x100000;
            }
            if (this.size() == 2 && ((AxisExpression)this.child(0)).getAxis() == 2 && ((AxisExpression)this.child(1)).getAxis() == 3) {
                p |= 0x20000;
            }
        }
        return p;
    }

    @Override
    public boolean implementsStaticTypeCheck() {
        return true;
    }

    @Override
    public Expression staticTypeCheck(SequenceType req, boolean backwardsCompatible, RoleDiagnostic role, ExpressionVisitor visitor) throws XPathException {
        TypeChecker tc = visitor.getConfiguration().getTypeChecker(backwardsCompatible);
        if (backwardsCompatible && !Cardinality.allowsMany(req.getCardinality())) {
            Expression first = FirstItemExpression.makeFirstItemExpression(this);
            return tc.staticTypeCheck(first, req, role, visitor);
        }
        Expression[] checked = new Expression[this.operanda.length];
        SequenceType subReq = req;
        if (req.getCardinality() != 57344) {
            subReq = SequenceType.makeSequenceType(req.getPrimaryType(), 57344);
        }
        for (int i = 0; i < this.operanda.length; ++i) {
            checked[i] = tc.staticTypeCheck(this.operanda[i].getChildExpression(), subReq, role, visitor);
        }
        Block b2 = new Block(checked);
        ExpressionTool.copyLocationInfo(this, b2);
        b2.allNodesUntyped = this.allNodesUntyped;
        int reqCard = req.getCardinality();
        int suppliedCard = b2.getCardinality();
        if (!Cardinality.subsumes(req.getCardinality(), suppliedCard)) {
            if ((reqCard & suppliedCard) == 0) {
                XPathException err = new XPathException("The required cardinality of the " + role.getMessage() + " is " + Cardinality.toString(reqCard) + ", but the supplied cardinality is " + Cardinality.toString(suppliedCard), role.getErrorCode(), this.getLocation());
                err.setIsTypeError(true);
                err.setFailingExpression(this);
                throw err;
            }
            return CardinalityChecker.makeCardinalityChecker(b2, reqCard, role);
        }
        return b2;
    }

    public static boolean neverReturnsTypedNodes(Instruction insn, TypeHierarchy th) {
        for (Operand o : insn.operands()) {
            ItemType it;
            Expression exp = o.getChildExpression();
            if (exp.hasSpecialProperty(0x8000000) || th.relationship(it = exp.getItemType(), NodeKindTest.ELEMENT) == Affinity.DISJOINT && th.relationship(it, NodeKindTest.ATTRIBUTE) == Affinity.DISJOINT) continue;
            return false;
        }
        return true;
    }

    public Expression mergeAdjacentTextInstructions() {
        boolean[] isLiteralText = new boolean[this.size()];
        boolean hasAdjacentTextNodes = false;
        for (int i = 0; i < this.size(); ++i) {
            boolean bl = isLiteralText[i] = this.child(i) instanceof ValueOf && ((ValueOf)this.child(i)).getSelect() instanceof StringLiteral && !((ValueOf)this.child(i)).isDisableOutputEscaping();
            if (i <= 0 || !isLiteralText[i] || !isLiteralText[i - 1]) continue;
            hasAdjacentTextNodes = true;
        }
        if (hasAdjacentTextNodes) {
            ArrayList<Expression> content = new ArrayList<Expression>(this.size());
            String pendingText = null;
            for (int i = 0; i < this.size(); ++i) {
                if (isLiteralText[i]) {
                    pendingText = (pendingText == null ? "" : pendingText) + ((StringLiteral)((ValueOf)this.child(i)).getSelect()).getStringValue();
                    continue;
                }
                if (pendingText != null) {
                    ValueOf inst = new ValueOf(new StringLiteral(pendingText), false, false);
                    content.add(inst);
                    pendingText = null;
                }
                content.add(this.child(i));
            }
            if (pendingText != null) {
                ValueOf inst = new ValueOf(new StringLiteral(pendingText), false, false);
                content.add(inst);
            }
            return Block.makeBlock(content);
        }
        return this;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        Expression[] c2 = new Expression[this.size()];
        for (int c = 0; c < this.size(); ++c) {
            c2[c] = this.child(c).copy(rebindings);
        }
        Block b2 = new Block(c2);
        for (int c = 0; c < this.size(); ++c) {
            b2.adoptChildExpression(c2[c]);
        }
        b2.allNodesUntyped = this.allNodesUntyped;
        ExpressionTool.copyLocationInfo(this, b2);
        return b2;
    }

    @Override
    public final ItemType getItemType() {
        if (this.size() == 0) {
            return ErrorType.getInstance();
        }
        ItemType t1 = null;
        TypeHierarchy th = this.getConfiguration().getTypeHierarchy();
        for (int i = 0; i < this.size(); ++i) {
            Expression child = this.child(i);
            if (child instanceof Message) continue;
            ItemType t = child.getItemType();
            ItemType itemType = t1 = t1 == null ? t : Type.getCommonSuperType(t1, t, th);
            if (!(t1 instanceof AnyItemType)) continue;
            return t1;
        }
        return t1 == null ? ErrorType.getInstance() : t1;
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        if (this.isInstruction()) {
            return super.getStaticUType(contextItemType);
        }
        if (this.size() == 0) {
            return UType.VOID;
        }
        UType t1 = this.child(0).getStaticUType(contextItemType);
        for (int i = 1; i < this.size(); ++i) {
            if ((t1 = t1.union(this.child(i).getStaticUType(contextItemType))) != UType.ANY) continue;
            return t1;
        }
        return t1;
    }

    @Override
    public int computeCardinality() {
        if (this.size() == 0) {
            return 8192;
        }
        int c1 = this.child(0).getCardinality();
        for (int i = 1; i < this.size() && (c1 = Cardinality.sum(c1, this.child(i).getCardinality())) != 32768; ++i) {
        }
        return c1;
    }

    @Override
    public final boolean mayCreateNewNodes() {
        return this.someOperandCreatesNewNodes();
    }

    @Override
    public void checkForUpdatingSubexpressions() throws XPathException {
        if (this.size() < 2) {
            return;
        }
        boolean updating = false;
        boolean nonUpdating = false;
        for (Operand o : this.operands()) {
            Expression child = o.getChildExpression();
            if (ExpressionTool.isNotAllowedInUpdatingContext(child)) {
                if (updating) {
                    XPathException err = new XPathException("If any subexpression is updating, then all must be updating", "XUST0001");
                    err.setLocation(child.getLocation());
                    throw err;
                }
                nonUpdating = true;
            }
            if (!child.isUpdatingExpression()) continue;
            if (nonUpdating) {
                XPathException err = new XPathException("If any subexpression is updating, then all must be updating", "XUST0001");
                err.setLocation(child.getLocation());
                throw err;
            }
            updating = true;
        }
    }

    @Override
    public boolean isVacuousExpression() {
        for (Operand o : this.operands()) {
            Expression child = o.getChildExpression();
            if (child.isVacuousExpression()) continue;
            return false;
        }
        return true;
    }

    @Override
    public Expression simplify() throws XPathException {
        boolean allAtomic = true;
        boolean nested = false;
        for (int c = 0; c < this.size(); ++c) {
            this.setChild(c, this.child(c).simplify());
            if (!Literal.isAtomic(this.child(c))) {
                allAtomic = false;
            }
            if (this.child(c) instanceof Block) {
                nested = true;
                continue;
            }
            if (!Literal.isEmptySequence(this.child(c))) continue;
            nested = true;
        }
        if (this.size() == 1) {
            Expression e = this.getOperanda()[0].getChildExpression();
            e.setParentExpression(this.getParentExpression());
            return e;
        }
        if (this.size() == 0) {
            Literal result = Literal.makeEmptySequence();
            ExpressionTool.copyLocationInfo(this, result);
            result.setParentExpression(this.getParentExpression());
            return result;
        }
        if (nested) {
            ArrayList<Expression> list = new ArrayList<Expression>(this.size() * 2);
            this.flatten(list);
            Expression[] children = new Expression[list.size()];
            for (int i = 0; i < list.size(); ++i) {
                children[i] = (Expression)list.get(i);
            }
            Block newBlock = new Block(children);
            ExpressionTool.copyLocationInfo(this, newBlock);
            return newBlock.simplify();
        }
        if (allAtomic) {
            Item[] values = new AtomicValue[this.size()];
            for (int c = 0; c < this.size(); ++c) {
                values[c] = (AtomicValue)((Literal)this.child(c)).getValue();
            }
            Literal result = Literal.makeLiteral(new SequenceExtent(values), this);
            result.setParentExpression(this.getParentExpression());
            return result;
        }
        return this;
    }

    private void flatten(List<Expression> targetList) throws XPathException {
        ArrayList<Item> currentLiteralList = null;
        for (Operand o : this.operands()) {
            Expression child = o.getChildExpression();
            if (Literal.isEmptySequence(child)) continue;
            if (child instanceof Block) {
                this.flushCurrentLiteralList(currentLiteralList, targetList);
                currentLiteralList = null;
                ((Block)child).flatten(targetList);
                continue;
            }
            if (child instanceof Literal && !(((Literal)child).getValue() instanceof IntegerRange)) {
                Item item;
                UnfailingIterator iterator = ((Literal)child).getValue().iterate();
                if (currentLiteralList == null) {
                    currentLiteralList = new ArrayList<Item>(10);
                }
                while ((item = iterator.next()) != null) {
                    currentLiteralList.add(item);
                }
                continue;
            }
            this.flushCurrentLiteralList(currentLiteralList, targetList);
            currentLiteralList = null;
            targetList.add(child);
        }
        this.flushCurrentLiteralList(currentLiteralList, targetList);
    }

    private void flushCurrentLiteralList(List<Item> currentLiteralList, List<Expression> list) {
        if (currentLiteralList != null) {
            ListIterator<Item> iter = new ListIterator<Item>(currentLiteralList);
            Literal lit = Literal.makeLiteral(iter.materialize(), this);
            list.add(lit);
        }
    }

    public boolean isCandidateForSharedAppend() {
        for (Operand o : this.operands()) {
            Expression exp = o.getChildExpression();
            if (!(exp instanceof VariableReference) && !(exp instanceof Literal)) continue;
            return true;
        }
        return false;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.typeCheckChildren(visitor, contextInfo);
        if (Block.neverReturnsTypedNodes(this, visitor.getConfiguration().getTypeHierarchy())) {
            this.resetLocalStaticProperties();
            this.allNodesUntyped = true;
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.optimizeChildren(visitor, contextInfo);
        boolean canSimplify = false;
        boolean prevLiteral = false;
        for (Operand o : this.operands()) {
            Expression child = o.getChildExpression();
            if (child instanceof Block) {
                canSimplify = true;
                break;
            }
            if (child instanceof Literal && !(((Literal)child).getValue() instanceof IntegerRange)) {
                if (prevLiteral || Literal.isEmptySequence(child)) {
                    canSimplify = true;
                    break;
                }
                prevLiteral = true;
                continue;
            }
            prevLiteral = false;
        }
        if (canSimplify) {
            ArrayList<Expression> list = new ArrayList<Expression>(this.size() * 2);
            this.flatten(list);
            Expression result = Block.makeBlock(list);
            result.setRetainedStaticContext(this.getRetainedStaticContext());
            return result;
        }
        if (this.size() == 0) {
            return Literal.makeEmptySequence();
        }
        if (this.size() == 1) {
            return this.child(0);
        }
        return this;
    }

    @Override
    public void checkPermittedContents(SchemaType parentType, boolean whole) throws XPathException {
        for (Operand o : this.operands()) {
            Expression child = o.getChildExpression();
            child.checkPermittedContents(parentType, false);
        }
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("sequence", this);
        for (Operand o : this.operands()) {
            Expression child = o.getChildExpression();
            child.export(out);
        }
        out.endElement();
    }

    @Override
    public String toShortString() {
        return "(" + this.child(0).toShortString() + ", ...)";
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        TailCall tc = null;
        for (Operand o : this.operands()) {
            Expression child = o.getChildExpression();
            try {
                if (child instanceof TailCallReturner) {
                    tc = ((TailCallReturner)((Object)child)).processLeavingTail(output, context);
                    continue;
                }
                child.process(output, context);
                tc = null;
            } catch (XPathException e) {
                e.maybeSetLocation(child.getLocation());
                e.maybeSetContext(context);
                throw e;
            }
        }
        return tc;
    }

    @Override
    public int getImplementationMethod() {
        return 6;
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        if (this.size() == 0) {
            return EmptyIterator.emptyIterator();
        }
        if (this.size() == 1) {
            return this.child(0).iterate(context);
        }
        return new BlockIterator(this.operanda, context);
    }

    @Override
    public void evaluatePendingUpdates(XPathContext context, PendingUpdateList pul) throws XPathException {
        for (Operand o : this.operands()) {
            Expression child = o.getChildExpression();
            child.evaluatePendingUpdates(context, pul);
        }
    }

    @Override
    public String getStreamerName() {
        return "Block";
    }
}


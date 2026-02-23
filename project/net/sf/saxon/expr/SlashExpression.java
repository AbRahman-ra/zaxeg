/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.BinaryExpression;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.ContextMappingIterator;
import net.sf.saxon.expr.ContextSwitchingExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.ItemChecker;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.MappingIterator;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.RootExpression;
import net.sf.saxon.expr.SimpleStepExpression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.VennExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMinor;
import net.sf.saxon.expr.instruct.CopyOf;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.expr.sort.DocumentSorter;
import net.sf.saxon.functions.Doc;
import net.sf.saxon.functions.DocumentFn;
import net.sf.saxon.functions.KeyFn;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.AncestorQualifiedPattern;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.GeneralNodePattern;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.NodeTestPattern;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.pattern.PatternMaker;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;

public class SlashExpression
extends BinaryExpression
implements ContextSwitchingExpression {
    boolean contextFree;

    public SlashExpression(Expression start, Expression step) {
        super(start, 2, step);
    }

    @Override
    protected OperandRole getOperandRole(int arg) {
        return arg == 0 ? OperandRole.FOCUS_CONTROLLING_SELECT : OperandRole.FOCUS_CONTROLLED_ACTION;
    }

    public Expression getStart() {
        return this.getLhsExpression();
    }

    public void setStart(Expression start) {
        this.setLhsExpression(start);
    }

    public Expression getStep() {
        return this.getRhsExpression();
    }

    public void setStep(Expression step) {
        this.setRhsExpression(step);
    }

    @Override
    public String getExpressionName() {
        return "pathExpression";
    }

    @Override
    public Expression getSelectExpression() {
        return this.getStart();
    }

    @Override
    public Expression getActionExpression() {
        return this.getStep();
    }

    @Override
    public final ItemType getItemType() {
        return this.getStep().getItemType();
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        return this.getStep().getStaticUType(this.getStart().getStaticUType(contextItemType));
    }

    @Override
    public IntegerValue[] getIntegerBounds() {
        return this.getStep().getIntegerBounds();
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getLhs().typeCheck(visitor, contextInfo);
        if (Literal.isEmptySequence(this.getStart())) {
            return this.getStart();
        }
        Configuration config = visitor.getConfiguration();
        TypeChecker tc = config.getTypeChecker(false);
        RoleDiagnostic role0 = new RoleDiagnostic(1, "/", 0);
        role0.setErrorCode("XPTY0019");
        this.setStart(tc.staticTypeCheck(this.getStart(), SequenceType.NODE_SEQUENCE, role0, visitor));
        ItemType startType = this.getStart().getItemType();
        if (startType == ErrorType.getInstance()) {
            return Literal.makeEmptySequence();
        }
        ContextItemStaticInfo cit = config.makeContextItemStaticInfo(startType, false);
        cit.setContextSettingExpression(this.getStart());
        this.getRhs().typeCheck(visitor, cit);
        SlashExpression e2 = this.simplifyDescendantPath(visitor.getStaticContext());
        if (e2 != null) {
            return ((Expression)e2).typeCheck(visitor, contextInfo);
        }
        if (this.getStart() instanceof ContextItemExpression && this.getStep().hasSpecialProperty(131072)) {
            return this.getStep();
        }
        if (this.getStep() instanceof ContextItemExpression && this.getStart().hasSpecialProperty(131072)) {
            return this.getStart();
        }
        if (this.getStep() instanceof AxisExpression && ((AxisExpression)this.getStep()).getAxis() == 12 && config.getTypeHierarchy().isSubType(startType, this.getStep().getItemType())) {
            return this.getStart();
        }
        return this;
    }

    public SlashExpression simplifyDescendantPath(StaticContext env) {
        Expression underlyingStep = this.getStep();
        while (underlyingStep instanceof FilterExpression) {
            if (((FilterExpression)underlyingStep).isPositional(env.getConfiguration().getTypeHierarchy())) {
                return null;
            }
            underlyingStep = ((FilterExpression)underlyingStep).getSelectExpression();
        }
        if (!(underlyingStep instanceof AxisExpression)) {
            return null;
        }
        Expression st = this.getStart();
        if (st instanceof AxisExpression) {
            AxisExpression stax = (AxisExpression)st;
            if (stax.getAxis() != 5) {
                return null;
            }
            ContextItemExpression cie = new ContextItemExpression();
            ExpressionTool.copyLocationInfo(this, cie);
            st = ExpressionTool.makePathExpression(cie, stax.copy(new RebindingMap()));
            ExpressionTool.copyLocationInfo(this, st);
        }
        if (!(st instanceof SlashExpression)) {
            return null;
        }
        SlashExpression startPath = (SlashExpression)st;
        if (!(startPath.getStep() instanceof AxisExpression)) {
            return null;
        }
        AxisExpression mid = (AxisExpression)startPath.getStep();
        if (mid.getAxis() != 5) {
            return null;
        }
        NodeTest test = mid.getNodeTest();
        if (test != null && !(test instanceof AnyNodeTest)) {
            return null;
        }
        int underlyingAxis = ((AxisExpression)underlyingStep).getAxis();
        if (underlyingAxis == 3 || underlyingAxis == 4 || underlyingAxis == 5) {
            int newAxis = underlyingAxis == 5 ? 5 : 4;
            Expression newStep = new AxisExpression(newAxis, ((AxisExpression)underlyingStep).getNodeTest());
            ExpressionTool.copyLocationInfo(this, newStep);
            underlyingStep = this.getStep();
            Stack<Expression> filters = new Stack<Expression>();
            while (underlyingStep instanceof FilterExpression) {
                filters.add(((FilterExpression)underlyingStep).getFilter());
                underlyingStep = ((FilterExpression)underlyingStep).getSelectExpression();
            }
            while (!filters.isEmpty()) {
                newStep = new FilterExpression(newStep, (Expression)filters.pop());
                ExpressionTool.copyLocationInfo(this.getStep(), newStep);
            }
            Expression newPath = ExpressionTool.makePathExpression(startPath.getStart(), newStep);
            if (!(newPath instanceof SlashExpression)) {
                return null;
            }
            ExpressionTool.copyLocationInfo(this, newPath);
            return (SlashExpression)newPath;
        }
        if (underlyingAxis == 2) {
            AxisExpression newStep = new AxisExpression(5, NodeKindTest.ELEMENT);
            ExpressionTool.copyLocationInfo(this, newStep);
            Expression e2 = ExpressionTool.makePathExpression(startPath.getStart(), newStep);
            Expression e3 = ExpressionTool.makePathExpression(e2, this.getStep());
            if (!(e3 instanceof SlashExpression)) {
                return null;
            }
            ExpressionTool.copyLocationInfo(this, e3);
            return (SlashExpression)e3;
        }
        return null;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        Expression rawStep;
        SystemFunctionCall keyCall;
        Expression k;
        Expression lastStep;
        Configuration config = visitor.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();
        Optimizer opt = visitor.obtainOptimizer();
        this.getLhs().optimize(visitor, contextItemType);
        if (Literal.isEmptySequence(this.getStart())) {
            return Literal.makeEmptySequence();
        }
        ContextItemStaticInfo cit = visitor.getConfiguration().makeContextItemStaticInfo(this.getStart().getItemType(), false);
        cit.setContextSettingExpression(this.getStart());
        this.getRhs().optimize(visitor, cit);
        if (Literal.isEmptySequence(this.getStep())) {
            return Literal.makeEmptySequence();
        }
        if (this.getStart() instanceof RootExpression && th.isSubType(contextItemType.getItemType(), NodeKindTest.DOCUMENT)) {
            return this.getStep();
        }
        Expression e2 = this.simplifyDescendantPath(visitor.getStaticContext());
        if (e2 != null) {
            return ((Expression)e2).optimize(visitor, contextItemType);
        }
        Expression firstStep = this.getFirstStep();
        if (!firstStep.isCallOn(Doc.class) && !firstStep.isCallOn(DocumentFn.class) && (lastStep = this.getLastStep()) instanceof FilterExpression && !((FilterExpression)lastStep).isPositional(th)) {
            Expression leading = this.getLeadingSteps();
            Expression p2 = ExpressionTool.makePathExpression(leading, ((FilterExpression)lastStep).getSelectExpression());
            FilterExpression f2 = new FilterExpression(p2, ((FilterExpression)lastStep).getFilter());
            ExpressionTool.copyLocationInfo(this, f2);
            return ((Expression)f2).optimize(visitor, contextItemType);
        }
        if (!visitor.isOptimizeForStreaming() && (k = opt.convertPathExpressionToKey(this, visitor)) != null) {
            return k.typeCheck(visitor, contextItemType).optimize(visitor, contextItemType);
        }
        e2 = this.tryToMakeSorted(visitor, contextItemType);
        if (e2 != null) {
            return e2;
        }
        if (this.getStep() instanceof AxisExpression) {
            if (!Cardinality.allowsMany(this.getStart().getCardinality())) {
                SimpleStepExpression sse = new SimpleStepExpression(this.getStart(), this.getStep());
                ExpressionTool.copyLocationInfo(this, sse);
                sse.setParentExpression(this.getParentExpression());
                return sse;
            }
            this.contextFree = true;
        }
        if (this.getStart() instanceof RootExpression && this.getStep().isCallOn(KeyFn.class) && (keyCall = (SystemFunctionCall)this.getStep()).getArity() == 3 && keyCall.getArg(2) instanceof ContextItemExpression) {
            keyCall.setArg(2, new RootExpression());
            keyCall.setParentExpression(this.getParentExpression());
            ExpressionTool.resetStaticProperties(keyCall);
            return keyCall;
        }
        k = this.promoteFocusIndependentSubexpressions(visitor, contextItemType);
        if (k != this) {
            return k;
        }
        if (visitor.isOptimizeForStreaming() && (rawStep = ExpressionTool.unfilteredExpression(this.getStep(), true)) instanceof CopyOf && ((CopyOf)rawStep).getSelect() instanceof ContextItemExpression) {
            ((CopyOf)rawStep).setSelect(this.getStart());
            rawStep.resetLocalStaticProperties();
            this.getStep().resetLocalStaticProperties();
            return this.getStep();
        }
        return this;
    }

    public SlashExpression tryToMakeAbsolute() {
        SlashExpression se;
        SlashExpression se2;
        ItemType contextItemType;
        Expression first = this.getFirstStep();
        if (first.getItemType().getPrimitiveType() == 9) {
            return this;
        }
        if (first instanceof AxisExpression && (contextItemType = ((AxisExpression)first).getContextItemType()) != null && contextItemType.getPrimitiveType() == 9) {
            RootExpression root = new RootExpression();
            ExpressionTool.copyLocationInfo(this, root);
            Expression path = ExpressionTool.makePathExpression(root, this.copy(new RebindingMap()));
            if (!(path instanceof SlashExpression)) {
                return null;
            }
            ExpressionTool.copyLocationInfo(this, path);
            return (SlashExpression)path;
        }
        if (first instanceof DocumentSorter && ((DocumentSorter)first).getBaseExpression() instanceof SlashExpression && (se2 = (se = (SlashExpression)((DocumentSorter)first).getBaseExpression()).tryToMakeAbsolute()) != null) {
            if (se2 == se) {
                return this;
            }
            Expression rest = this.getRemainingSteps();
            DocumentSorter ds = new DocumentSorter(se2);
            return new SlashExpression(ds, rest);
        }
        return null;
    }

    @Override
    public double getCost() {
        int factor = Cardinality.allowsMany(this.getLhsExpression().getCardinality()) ? 5 : 1;
        double lh = this.getLhsExpression().getCost() + 1.0;
        double rh = this.getRhsExpression().getCost();
        double product = lh + (double)factor * rh;
        return Math.max(product, 1.0E9);
    }

    public Expression tryToMakeSorted(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        Configuration config = visitor.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();
        Optimizer opt = visitor.obtainOptimizer();
        Expression s1 = ExpressionTool.unfilteredExpression(this.getStart(), false);
        if (!(s1 instanceof AxisExpression) || ((AxisExpression)s1).getAxis() != 4) {
            return null;
        }
        Expression s2 = ExpressionTool.unfilteredExpression(this.getStep(), false);
        if (!(s2 instanceof AxisExpression) || ((AxisExpression)s2).getAxis() != 3) {
            return null;
        }
        Expression x = this.getStart().copy(new RebindingMap());
        AxisExpression ax = (AxisExpression)ExpressionTool.unfilteredExpression(x, false);
        ax.setAxis(9);
        Expression y = this.getStep().copy(new RebindingMap());
        AxisExpression ay = (AxisExpression)ExpressionTool.unfilteredExpression(y, false);
        ay.setAxis(4);
        BinaryExpression k = new FilterExpression(y, x);
        if (!th.isSubType(contextItemType.getItemType(), NodeKindTest.DOCUMENT)) {
            k = new SlashExpression(new AxisExpression(3, NodeKindTest.ELEMENT), k);
            ExpressionTool.copyLocationInfo(this, k);
            opt.trace("Rewrote descendant::X/child::Y as child::*/descendant::Y[parent::X]", k);
        } else {
            ExpressionTool.copyLocationInfo(this, k);
            opt.trace("Rewrote descendant::X/child::Y as descendant::Y[parent::X]", k);
        }
        return k;
    }

    protected Expression promoteFocusIndependentSubexpressions(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        return this;
    }

    @Override
    public Expression unordered(boolean retainAllNodes, boolean forStreaming) throws XPathException {
        this.setStart(this.getStart().unordered(retainAllNodes, forStreaming));
        this.setStep(this.getStep().unordered(retainAllNodes, forStreaming));
        return this;
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        PathMap.PathMapNodeSet target = this.getStart().addToPathMap(pathMap, pathMapNodeSet);
        return this.getStep().addToPathMap(pathMap, target);
    }

    @Override
    public int getImplementationMethod() {
        return 2;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        Expression exp = ExpressionTool.makePathExpression(this.getStart().copy(rebindings), this.getStep().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    @Override
    public int computeSpecialProperties() {
        int startProperties = this.getStart().getSpecialProperties();
        int stepProperties = this.getStep().getSpecialProperties();
        if ((stepProperties & 0x400000) != 0) {
            return 0x8A0000;
        }
        int p = 0;
        if (!Cardinality.allowsMany(this.getStart().getCardinality())) {
            startProperties |= 0x10A0000;
        }
        if (!Cardinality.allowsMany(this.getStep().getCardinality())) {
            stepProperties |= 0x10A0000;
        }
        if ((startProperties & stepProperties & 0x10000) != 0) {
            p |= 0x10000;
        }
        if ((startProperties & 0x1000000) != 0 && (stepProperties & 0x10000) != 0) {
            p |= 0x1000000;
        }
        if ((startProperties & stepProperties & 0x80000) != 0) {
            p |= 0x80000;
        }
        if ((startProperties & stepProperties & 0x100000) != 0) {
            p |= 0x100000;
        }
        if (this.testNaturallySorted(startProperties, stepProperties)) {
            p |= 0x20000;
        }
        if (this.testNaturallyReverseSorted()) {
            p |= 0x40000;
        }
        if ((startProperties & stepProperties & 0x800000) != 0) {
            p |= 0x800000;
        }
        return p;
    }

    private boolean testNaturallySorted(int startProperties, int stepProperties) {
        if ((stepProperties & 0x20000) == 0) {
            return false;
        }
        if (Cardinality.allowsMany(this.getStart().getCardinality())) {
            if ((startProperties & 0x20000) == 0) {
                return false;
            }
        } else {
            return true;
        }
        if ((stepProperties & 0x200000) != 0) {
            return true;
        }
        if ((stepProperties & 0x400000) != 0) {
            return true;
        }
        return (startProperties & 0x80000) != 0 && (stepProperties & 0x100000) != 0;
    }

    private boolean testNaturallyReverseSorted() {
        if (!Cardinality.allowsMany(this.getStart().getCardinality()) && this.getStep() instanceof AxisExpression) {
            return !AxisInfo.isForwards[((AxisExpression)this.getStep()).getAxis()];
        }
        return !Cardinality.allowsMany(this.getStep().getCardinality()) && this.getStart() instanceof AxisExpression && !AxisInfo.isForwards[((AxisExpression)this.getStart()).getAxis()];
    }

    @Override
    public int computeCardinality() {
        int c1 = this.getStart().getCardinality();
        int c2 = this.getStep().getCardinality();
        return Cardinality.multiply(c1, c2);
    }

    @Override
    public Pattern toPattern(Configuration config) throws XPathException {
        AxisExpression mid;
        SlashExpression start;
        ItemChecker checker;
        Expression head = this.getLeadingSteps();
        Expression tail = this.getLastStep();
        if (head instanceof ItemChecker && (checker = (ItemChecker)head).getBaseExpression() instanceof ContextItemExpression) {
            return tail.toPattern(config);
        }
        Pattern tailPattern = tail.toPattern(config);
        if (tailPattern instanceof NodeTestPattern) {
            if (tailPattern.getItemType() instanceof ErrorType) {
                return tailPattern;
            }
        } else if (tailPattern instanceof GeneralNodePattern) {
            return new GeneralNodePattern(this, (NodeTest)tailPattern.getItemType());
        }
        int axis = 9;
        Pattern headPattern = null;
        if (head instanceof SlashExpression && (start = (SlashExpression)head).getActionExpression() instanceof AxisExpression && (mid = (AxisExpression)start.getActionExpression()).getAxis() == 5 && (mid.getNodeTest() == null || mid.getNodeTest() instanceof AnyNodeTest)) {
            axis = 0;
            headPattern = start.getSelectExpression().toPattern(config);
        }
        if (headPattern == null) {
            if (tail instanceof VennExpression) {
                SlashExpression lhExpansion = new SlashExpression(head.copy(new RebindingMap()), ((VennExpression)tail).getLhsExpression());
                SlashExpression rhExpansion = new SlashExpression(head.copy(new RebindingMap()), ((VennExpression)tail).getRhsExpression());
                VennExpression topExpansion = new VennExpression(lhExpansion, ((VennExpression)tail).operator, rhExpansion);
                return topExpansion.toPattern(config);
            }
            axis = PatternMaker.getAxisForPathStep(tail);
            headPattern = head.toPattern(config);
        }
        return new AncestorQualifiedPattern(tailPattern, headPattern, axis);
    }

    public boolean isContextFree() {
        return this.contextFree;
    }

    public void setContextFree(boolean free) {
        this.contextFree = free;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SlashExpression)) {
            return false;
        }
        SlashExpression p = (SlashExpression)other;
        return this.getStart().isEqual(p.getStart()) && this.getStep().isEqual(p.getStep());
    }

    @Override
    public int computeHashCode() {
        return "SlashExpression".hashCode() + this.getStart().hashCode() + this.getStep().hashCode();
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        Expression step = this.getStep();
        if (this.contextFree && step instanceof AxisExpression) {
            return new MappingIterator(this.getStart().iterate(context), item -> ((AxisExpression)step).iterate((NodeInfo)item));
        }
        XPathContextMinor context2 = context.newMinorContext();
        context2.trackFocus(this.getStart().iterate(context));
        return new ContextMappingIterator(step::iterate, context2);
    }

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        destination.startElement("slash", this);
        if (this instanceof SimpleStepExpression) {
            destination.emitAttribute("simple", "1");
        } else if (this.isContextFree()) {
            destination.emitAttribute("simple", "2");
        }
        this.getStart().export(destination);
        this.getStep().export(destination);
        destination.endElement();
    }

    @Override
    public String toString() {
        return ExpressionTool.parenthesize(this.getStart()) + "/" + ExpressionTool.parenthesize(this.getStep());
    }

    @Override
    public String toShortString() {
        return ExpressionTool.parenthesizeShort(this.getStart()) + "/" + ExpressionTool.parenthesizeShort(this.getStep());
    }

    public Expression getFirstStep() {
        if (this.getStart() instanceof SlashExpression) {
            return ((SlashExpression)this.getStart()).getFirstStep();
        }
        return this.getStart();
    }

    public Expression getRemainingSteps() {
        if (this.getStart() instanceof SlashExpression) {
            ArrayList<Expression> list = new ArrayList<Expression>(8);
            this.gatherSteps(list);
            Expression rem = this.rebuildSteps(list.subList(1, list.size()));
            ExpressionTool.copyLocationInfo(this, rem);
            return rem;
        }
        return this.getStep();
    }

    private void gatherSteps(List<Expression> list) {
        if (this.getStart() instanceof SlashExpression) {
            ((SlashExpression)this.getStart()).gatherSteps(list);
        } else {
            list.add(this.getStart());
        }
        if (this.getStep() instanceof SlashExpression) {
            ((SlashExpression)this.getStep()).gatherSteps(list);
        } else {
            list.add(this.getStep());
        }
    }

    private Expression rebuildSteps(List<Expression> list) {
        if (list.size() == 1) {
            return list.get(0).copy(new RebindingMap());
        }
        return new SlashExpression(list.get(0).copy(new RebindingMap()), this.rebuildSteps(list.subList(1, list.size())));
    }

    public Expression getLastStep() {
        if (this.getStep() instanceof SlashExpression) {
            return ((SlashExpression)this.getStep()).getLastStep();
        }
        return this.getStep();
    }

    public Expression getLeadingSteps() {
        if (this.getStep() instanceof SlashExpression) {
            ArrayList<Expression> list = new ArrayList<Expression>(8);
            this.gatherSteps(list);
            Expression rem = this.rebuildSteps(list.subList(0, list.size() - 1));
            ExpressionTool.copyLocationInfo(this, rem);
            return rem;
        }
        return this.getStart();
    }

    public boolean isAbsolute() {
        return this.getFirstStep().getItemType().getPrimitiveType() == 9;
    }

    @Override
    public String getStreamerName() {
        return "ForEach";
    }
}


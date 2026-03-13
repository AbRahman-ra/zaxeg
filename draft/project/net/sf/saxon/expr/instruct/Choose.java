/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.ArrayList;
import java.util.Arrays;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.ErrorExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.PendingUpdateList;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.ConditionalInstruction;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.instruct.TailCallReturner;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.functions.BooleanFn;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.SequenceType;

public class Choose
extends Instruction
implements ConditionalInstruction {
    private Operand[] conditionOps;
    private Operand[] actionOps;
    private boolean isInstruction;
    public static final OperandRole CHOICE_ACTION = new OperandRole(8, OperandUsage.TRANSMISSION, SequenceType.ANY_SEQUENCE);

    public Choose(Expression[] conditions, Expression[] actions) {
        int i;
        this.conditionOps = new Operand[conditions.length];
        for (i = 0; i < conditions.length; ++i) {
            this.conditionOps[i] = new Operand(this, conditions[i], OperandRole.INSPECT);
        }
        this.actionOps = new Operand[actions.length];
        for (i = 0; i < actions.length; ++i) {
            this.actionOps[i] = new Operand(this, actions[i], CHOICE_ACTION);
        }
    }

    public static Expression makeConditional(Expression condition, Expression thenExp, Expression elseExp) {
        if (Literal.isEmptySequence(elseExp)) {
            Expression[] conditions = new Expression[]{condition};
            Expression[] actions = new Expression[]{thenExp};
            return new Choose(conditions, actions);
        }
        Expression[] conditions = new Expression[]{condition, Literal.makeLiteral(BooleanValue.TRUE, condition)};
        Expression[] actions = new Expression[]{thenExp, elseExp};
        return new Choose(conditions, actions);
    }

    public static Expression makeConditional(Expression condition, Expression thenExp) {
        Expression[] conditions = new Expression[]{condition};
        Expression[] actions = new Expression[]{thenExp};
        return new Choose(conditions, actions);
    }

    public void setInstruction(boolean inst) {
        this.isInstruction = inst;
    }

    @Override
    public boolean isInstruction() {
        return this.isInstruction;
    }

    public int size() {
        return this.conditionOps.length;
    }

    public static boolean isSingleBranchChoice(Expression exp) {
        return exp instanceof Choose && ((Choose)exp).size() == 1;
    }

    public int getNumberOfConditions() {
        return this.size();
    }

    public Expression getCondition(int i) {
        return this.conditionOps[i].getChildExpression();
    }

    public void setCondition(int i, Expression condition) {
        this.conditionOps[i].setChildExpression(condition);
    }

    public Iterable<Operand> conditions() {
        return Arrays.asList(this.conditionOps);
    }

    public Operand getActionOperand(int i) {
        return this.actionOps[i];
    }

    public Expression getAction(int i) {
        return this.actionOps[i].getChildExpression();
    }

    public void setAction(int i, Expression action) {
        this.actionOps[i].setChildExpression(action);
    }

    public Iterable<Operand> actions() {
        return Arrays.asList(this.actionOps);
    }

    @Override
    public Iterable<Operand> operands() {
        ArrayList<Operand> operanda = new ArrayList<Operand>(this.size() * 2);
        for (int i = 0; i < this.size(); ++i) {
            operanda.add(this.conditionOps[i]);
            operanda.add(this.actionOps[i]);
        }
        return operanda;
    }

    @Override
    public boolean allowExtractingCommonSubexpressions() {
        return false;
    }

    public void atomizeActions() {
        for (int i = 0; i < this.size(); ++i) {
            this.setAction(i, Atomizer.makeAtomizer(this.getAction(i), null));
        }
    }

    @Override
    public int getInstructionNameCode() {
        return this.size() == 1 ? 160 : 142;
    }

    @Override
    public Expression simplify() throws XPathException {
        for (int i = 0; i < this.size(); ++i) {
            this.setCondition(i, this.getCondition(i).simplify());
            try {
                this.setAction(i, this.getAction(i).simplify());
                continue;
            } catch (XPathException err) {
                if (err.isTypeError()) {
                    throw err;
                }
                this.setAction(i, new ErrorExpression(new XmlProcessingException(err)));
            }
        }
        return this;
    }

    private Expression removeRedundantBranches(ExpressionVisitor visitor) {
        Expression result = this.removeRedundantBranches0(visitor);
        if (result != this) {
            ExpressionTool.copyLocationInfo(this, result);
        }
        return result;
    }

    private Expression removeRedundantBranches0(ExpressionVisitor visitor) {
        int i;
        Expression[] actions;
        Expression[] conditions;
        boolean compress = false;
        for (int i2 = 0; i2 < this.size(); ++i2) {
            Expression condition = this.getCondition(i2);
            if (!(condition instanceof Literal)) continue;
            compress = true;
            break;
        }
        int size = this.size();
        boolean changed = false;
        if (compress) {
            conditions = new ArrayList(size);
            actions = new ArrayList(size);
            for (i = 0; i < size; ++i) {
                Expression condition = this.getCondition(i);
                if (!Literal.hasEffectiveBooleanValue(condition, false)) {
                    conditions.add(condition);
                    actions.add(this.getAction(i));
                }
                if (Literal.hasEffectiveBooleanValue(condition, true)) break;
            }
            if (conditions.isEmpty()) {
                Literal lit = Literal.makeEmptySequence();
                ExpressionTool.copyLocationInfo(this, lit);
                return lit;
            }
            if (conditions.size() == 1 && Literal.hasEffectiveBooleanValue((Expression)conditions.get(0), true)) {
                return (Expression)actions.get(0);
            }
            if (conditions.size() != size) {
                Expression[] c = conditions.toArray(new Expression[conditions.size()]);
                Expression[] a = actions.toArray(new Expression[actions.size()]);
                Choose result = new Choose(c, a);
                result.setRetainedStaticContext(this.getRetainedStaticContext());
                return result;
            }
        }
        if (this.size() == 1 && Literal.hasEffectiveBooleanValue(this.getCondition(0), true)) {
            return this.getAction(0);
        }
        if (Literal.isEmptySequence(this.getAction(this.size() - 1))) {
            if (this.size() == 1) {
                Literal lit = Literal.makeEmptySequence();
                ExpressionTool.copyLocationInfo(this, lit);
                return lit;
            }
            conditions = new Expression[size - 1];
            actions = new Expression[size - 1];
            for (i = 0; i < size - 1; ++i) {
                conditions[i] = this.getCondition(i);
                actions[i] = this.getAction(i);
            }
            return new Choose(conditions, actions);
        }
        if (Literal.hasEffectiveBooleanValue(this.getCondition(size - 1), true) && this.getAction(size - 1) instanceof Choose) {
            int i3;
            Choose choose2 = (Choose)this.getAction(size - 1);
            int newLen = size + choose2.size() - 1;
            Expression[] c2 = new Expression[newLen];
            Expression[] a2 = new Expression[newLen];
            for (i3 = 0; i3 < size - 1; ++i3) {
                c2[i3] = this.getCondition(i3);
                a2[i3] = this.getAction(i3);
            }
            for (i3 = 0; i3 < choose2.size(); ++i3) {
                c2[i3 + size - 1] = choose2.getCondition(i3);
                a2[i3 + size - 1] = choose2.getAction(i3);
            }
            return new Choose(c2, a2);
        }
        if (size == 2 && Literal.isConstantBoolean(this.getAction(0), true) && Literal.isConstantBoolean(this.getAction(1), false) && Literal.hasEffectiveBooleanValue(this.getCondition(1), true)) {
            TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
            if (th.isSubType(this.getCondition(0).getItemType(), BuiltInAtomicType.BOOLEAN) && this.getCondition(0).getCardinality() == 16384) {
                return this.getCondition(0);
            }
            return SystemFunction.makeCall("boolean", this.getRetainedStaticContext(), this.getCondition(0));
        }
        return this;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Optimizer opt;
        int i;
        TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
        for (i = 0; i < this.size(); ++i) {
            this.conditionOps[i].typeCheck(visitor, contextInfo);
            XPathException err = TypeChecker.ebvError(this.getCondition(i), th);
            if (err == null) continue;
            err.setLocator(this.getCondition(i).getLocation());
            err.maybeSetFailingExpression(this.getCondition(i));
            throw err;
        }
        for (i = 0; i < this.size(); ++i) {
            if (Literal.hasEffectiveBooleanValue(this.getCondition(i), false)) continue;
            try {
                this.actionOps[i].typeCheck(visitor, contextInfo);
            } catch (XPathException err) {
                err.maybeSetLocation(this.getLocation());
                err.maybeSetFailingExpression(this.getAction(i));
                if (err.isStaticError()) {
                    throw err;
                }
                if (err.isTypeError()) {
                    if (Literal.isEmptySequence(this.getAction(i)) || Literal.hasEffectiveBooleanValue(this.getCondition(i), false)) {
                        this.setAction(i, new ErrorExpression(new XmlProcessingException(err)));
                    }
                    throw err;
                }
                this.setAction(i, new ErrorExpression(new XmlProcessingException(err)));
            }
            if (Literal.hasEffectiveBooleanValue(this.getCondition(i), true)) break;
        }
        if ((opt = visitor.obtainOptimizer()).isOptionSet(32768)) {
            Expression reduced = this.removeRedundantBranches(visitor);
            if (reduced != this) {
                return reduced.typeCheck(visitor, contextInfo);
            }
            return reduced;
        }
        return this;
    }

    @Override
    public boolean implementsStaticTypeCheck() {
        return true;
    }

    @Override
    public Expression staticTypeCheck(SequenceType req, boolean backwardsCompatible, RoleDiagnostic role, ExpressionVisitor visitor) throws XPathException {
        int size = this.size();
        TypeChecker tc = this.getConfiguration().getTypeChecker(backwardsCompatible);
        for (int i = 0; i < size; ++i) {
            try {
                this.setAction(i, tc.staticTypeCheck(this.getAction(i), req, role, visitor));
                continue;
            } catch (XPathException err) {
                if (err.isStaticError()) {
                    throw err;
                }
                ErrorExpression ee = new ErrorExpression(new XmlProcessingException(err));
                ExpressionTool.copyLocationInfo(this.getAction(i), ee);
                this.setAction(i, ee);
            }
        }
        if (!Literal.hasEffectiveBooleanValue(this.getCondition(size - 1), true) && !Cardinality.allowsZero(req.getCardinality())) {
            Expression[] c = new Expression[size + 1];
            Expression[] a = new Expression[size + 1];
            for (int i = 0; i < size; ++i) {
                c[i] = this.getCondition(i);
                a[i] = this.getAction(i);
            }
            c[size] = Literal.makeLiteral(BooleanValue.TRUE, this);
            String cond = size == 1 ? "The condition is not" : "None of the conditions is";
            String message = "Conditional expression: " + cond + " satisfied, so an empty sequence is returned, but this is not allowed as the " + role.getMessage();
            ErrorExpression errExp = new ErrorExpression(message, role.getErrorCode(), true);
            ExpressionTool.copyLocationInfo(this, errExp);
            a[size] = errExp;
            return new Choose(c, a);
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        int i;
        int size = this.size();
        for (i = 0; i < size; ++i) {
            boolean b;
            this.conditionOps[i].optimize(visitor, contextItemType);
            Expression ebv = BooleanFn.rewriteEffectiveBooleanValue(this.getCondition(i), visitor, contextItemType);
            if (ebv != null && ebv != this.getCondition(i)) {
                this.setCondition(i, ebv);
            }
            if (!(this.getCondition(i) instanceof Literal) || ((Literal)this.getCondition(i)).getValue() instanceof BooleanValue) continue;
            try {
                b = ((Literal)this.getCondition(i)).getValue().effectiveBooleanValue();
            } catch (XPathException err) {
                err.setLocation(this.getLocation());
                throw err;
            }
            this.setCondition(i, Literal.makeLiteral(BooleanValue.get(b), this));
        }
        for (i = 0; i < size; ++i) {
            if (Literal.hasEffectiveBooleanValue(this.getCondition(i), false)) continue;
            try {
                this.actionOps[i].optimize(visitor, contextItemType);
            } catch (XPathException err) {
                if (err.isTypeError()) {
                    throw err;
                }
                ErrorExpression ee = new ErrorExpression(new XmlProcessingException(err));
                ExpressionTool.copyLocationInfo(this.actionOps[i].getChildExpression(), ee);
                this.setAction(i, ee);
            }
            if (this.getAction(i) instanceof ErrorExpression && ((ErrorExpression)this.getAction(i)).isTypeError() && !Literal.isConstantBoolean(this.getCondition(i), false) && !Literal.isConstantBoolean(this.getCondition(i), true)) {
                visitor.issueWarning("Branch " + (i + 1) + " of conditional will fail with a type error if executed. " + ((ErrorExpression)this.getAction(i)).getMessage(), this.getAction(i).getLocation());
            }
            if (Literal.hasEffectiveBooleanValue(this.getCondition(i), true)) break;
        }
        if (size == 0) {
            return Literal.makeEmptySequence();
        }
        Optimizer opt = visitor.obtainOptimizer();
        if (opt.isOptionSet(32768)) {
            Expression e = this.removeRedundantBranches(visitor);
            if (e instanceof Choose) {
                return visitor.obtainOptimizer().trySwitch((Choose)e, visitor);
            }
            return e;
        }
        return this;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        int size = this.size();
        Expression[] c2 = new Expression[size];
        Expression[] a2 = new Expression[size];
        for (int c = 0; c < size; ++c) {
            c2[c] = this.getCondition(c).copy(rebindings);
            a2[c] = this.getAction(c).copy(rebindings);
        }
        Choose ch2 = new Choose(c2, a2);
        ExpressionTool.copyLocationInfo(this, ch2);
        ch2.setInstruction(this.isInstruction());
        return ch2;
    }

    @Override
    public void checkForUpdatingSubexpressions() throws XPathException {
        for (Operand o : this.conditions()) {
            Expression condition = o.getChildExpression();
            condition.checkForUpdatingSubexpressions();
            if (!condition.isUpdatingExpression()) continue;
            XPathException err = new XPathException("Updating expression appears in a context where it is not permitted", "XUST0001");
            err.setLocator(condition.getLocation());
            throw err;
        }
        boolean updating = false;
        boolean nonUpdating = false;
        for (Operand o : this.actions()) {
            Expression act = o.getChildExpression();
            act.checkForUpdatingSubexpressions();
            if (ExpressionTool.isNotAllowedInUpdatingContext(act)) {
                if (updating) {
                    XPathException err = new XPathException("If any branch of a conditional is an updating expression, then all must be updating expressions (or vacuous)", "XUST0001");
                    err.setLocator(act.getLocation());
                    throw err;
                }
                nonUpdating = true;
            }
            if (!act.isUpdatingExpression()) continue;
            if (nonUpdating) {
                XPathException err = new XPathException("If any branch of a conditional is an updating expression, then all must be updating expressions (or vacuous)", "XUST0001");
                err.setLocator(act.getLocation());
                throw err;
            }
            updating = true;
        }
    }

    @Override
    public boolean isUpdatingExpression() {
        for (Operand o : this.actions()) {
            if (!o.getChildExpression().isUpdatingExpression()) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean isVacuousExpression() {
        for (Operand action : this.actions()) {
            if (action.getChildExpression().isVacuousExpression()) continue;
            return false;
        }
        return true;
    }

    @Override
    public int getImplementationMethod() {
        int m = 14;
        if (!Cardinality.allowsMany(this.getCardinality())) {
            m |= 1;
        }
        return m;
    }

    @Override
    public int markTailFunctionCalls(StructuredQName qName, int arity) {
        int result = 0;
        for (Operand action : this.actions()) {
            result = Math.max(result, action.getChildExpression().markTailFunctionCalls(qName, arity));
        }
        return result;
    }

    @Override
    public ItemType getItemType() {
        TypeHierarchy th = this.getConfiguration().getTypeHierarchy();
        ItemType type = this.getAction(0).getItemType();
        for (int i = 1; i < this.size(); ++i) {
            type = Type.getCommonSuperType(type, this.getAction(i).getItemType(), th);
        }
        return type;
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        if (this.isInstruction()) {
            return super.getStaticUType(contextItemType);
        }
        UType type = this.getAction(0).getStaticUType(contextItemType);
        for (int i = 1; i < this.size(); ++i) {
            type = type.union(this.getAction(i).getStaticUType(contextItemType));
        }
        return type;
    }

    @Override
    public int computeCardinality() {
        int card = 0;
        boolean includesTrue = false;
        for (int i = 0; i < this.size(); ++i) {
            card = Cardinality.union(card, this.getAction(i).getCardinality());
            if (!Literal.hasEffectiveBooleanValue(this.getCondition(i), true)) continue;
            includesTrue = true;
        }
        if (!includesTrue) {
            card = Cardinality.union(card, 8192);
        }
        return card;
    }

    @Override
    public int computeSpecialProperties() {
        int props = this.getAction(0).getSpecialProperties();
        for (int i = 1; i < this.size(); ++i) {
            props &= this.getAction(i).getSpecialProperties();
        }
        return props;
    }

    @Override
    public final boolean mayCreateNewNodes() {
        for (Operand action : this.actions()) {
            int props = action.getChildExpression().getSpecialProperties();
            if ((props & 0x800000) != 0) continue;
            return true;
        }
        return false;
    }

    @Override
    public Expression unordered(boolean retainAllNodes, boolean forStreaming) throws XPathException {
        for (int i = 0; i < this.size(); ++i) {
            this.setAction(i, this.getAction(i).unordered(retainAllNodes, forStreaming));
        }
        return this;
    }

    @Override
    public void checkPermittedContents(SchemaType parentType, boolean whole) throws XPathException {
        for (Operand action : this.actions()) {
            action.getChildExpression().checkPermittedContents(parentType, whole);
        }
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        for (Operand condition : this.conditions()) {
            condition.getChildExpression().addToPathMap(pathMap, pathMapNodeSet);
        }
        PathMap.PathMapNodeSet result = new PathMap.PathMapNodeSet();
        for (Operand action : this.actions()) {
            PathMap.PathMapNodeSet temp = action.getChildExpression().addToPathMap(pathMap, pathMapNodeSet);
            result.addNodeSet(temp);
        }
        return result;
    }

    @Override
    public String toString() {
        FastStringBuffer sb = new FastStringBuffer(64);
        sb.append("if (");
        for (int i = 0; i < this.size(); ++i) {
            sb.append(this.getCondition(i).toString());
            sb.append(") then (");
            sb.append(this.getAction(i).toString());
            if (i == this.size() - 1) {
                sb.append(")");
                continue;
            }
            sb.append(") else if (");
        }
        return sb.toString();
    }

    @Override
    public String toShortString() {
        return "if(" + this.getCondition(0).toShortString() + ") then ... else ...";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("choose", this);
        for (int i = 0; i < this.size(); ++i) {
            this.getCondition(i).export(out);
            this.getAction(i).export(out);
        }
        out.endElement();
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        int i = this.choose(context);
        if (i >= 0) {
            Expression action = this.getAction(i);
            if (action instanceof TailCallReturner) {
                return ((TailCallReturner)((Object)action)).processLeavingTail(output, context);
            }
            action.process(output, context);
            return null;
        }
        return null;
    }

    private int choose(XPathContext context) throws XPathException {
        int size = this.size();
        for (int i = 0; i < size; ++i) {
            boolean b;
            try {
                b = this.getCondition(i).effectiveBooleanValue(context);
            } catch (XPathException e) {
                e.maybeSetFailingExpression(this.getCondition(i));
                throw e;
            }
            if (!b) continue;
            return i;
        }
        return -1;
    }

    @Override
    public Item evaluateItem(XPathContext context) throws XPathException {
        int i = this.choose(context);
        return i < 0 ? null : this.getAction(i).evaluateItem(context);
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        int i = this.choose(context);
        return i < 0 ? EmptyIterator.emptyIterator() : this.getAction(i).iterate(context);
    }

    @Override
    public void evaluatePendingUpdates(XPathContext context, PendingUpdateList pul) throws XPathException {
        int i = this.choose(context);
        if (i >= 0) {
            this.getAction(i).evaluatePendingUpdates(context, pul);
        }
    }

    @Override
    public String getExpressionName() {
        return "choose";
    }

    @Override
    public String getStreamerName() {
        return "Choose";
    }
}


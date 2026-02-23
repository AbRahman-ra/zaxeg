/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.AndExpression;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.BooleanExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ForExpression;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.PendingUpdateList;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.Clause;
import net.sf.saxon.expr.flwor.ForClause;
import net.sf.saxon.expr.flwor.LetClause;
import net.sf.saxon.expr.flwor.LocalVariableBinding;
import net.sf.saxon.expr.flwor.OperandProcessor;
import net.sf.saxon.expr.flwor.OuterForExpression;
import net.sf.saxon.expr.flwor.ReturnClauseIterator;
import net.sf.saxon.expr.flwor.ReturnClausePush;
import net.sf.saxon.expr.flwor.SingularityPull;
import net.sf.saxon.expr.flwor.TuplePull;
import net.sf.saxon.expr.flwor.TuplePush;
import net.sf.saxon.expr.flwor.WhereClause;
import net.sf.saxon.expr.parser.CodeInjector;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.QueryModule;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.SequenceType;

public class FLWORExpression
extends Expression {
    public List<Clause> clauses;
    public Operand returnClauseOp;
    private static final OperandRole SINGLE_RETURN = new OperandRole(0, OperandUsage.TRANSMISSION, SequenceType.ANY_SEQUENCE);
    private static final OperandRole REPEATED_RETURN = new OperandRole(4, OperandUsage.TRANSMISSION, SequenceType.ANY_SEQUENCE);

    public void init(List<Clause> clauses, Expression returnClause) {
        this.clauses = clauses;
        boolean looping = false;
        for (Clause c : clauses) {
            if (!FLWORExpression.isLoopingClause(c)) continue;
            looping = true;
            break;
        }
        this.returnClauseOp = new Operand(this, returnClause, looping ? REPEATED_RETURN : SINGLE_RETURN);
    }

    public List<Clause> getClauseList() {
        return this.clauses;
    }

    public static boolean isLoopingClause(Clause c) {
        return c.getClauseKey() == Clause.ClauseName.FOR || c.getClauseKey() == Clause.ClauseName.GROUP_BY || c.getClauseKey() == Clause.ClauseName.WINDOW;
    }

    public Expression getReturnClause() {
        return this.returnClauseOp.getChildExpression();
    }

    @Override
    public boolean hasVariableBinding(Binding binding) {
        for (Clause c : this.clauses) {
            if (!this.clauseHasBinding(c, binding)) continue;
            return true;
        }
        return false;
    }

    private boolean clauseHasBinding(Clause c, Binding binding) {
        for (LocalVariableBinding b : c.getRangeVariables()) {
            if (b != binding) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean allowExtractingCommonSubexpressions() {
        return false;
    }

    @Override
    public Expression simplify() throws XPathException {
        OperandProcessor simplifier = op -> op.setChildExpression(op.getChildExpression().simplify());
        for (Clause c : this.clauses) {
            c.processOperands(simplifier);
        }
        this.returnClauseOp.setChildExpression(this.getReturnClause().simplify());
        return this;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        OperandProcessor typeChecker = op -> op.typeCheck(visitor, contextInfo);
        for (int i = 0; i < this.clauses.size(); ++i) {
            LocalVariableBinding[] bindings;
            this.clauses.get(i).processOperands(typeChecker);
            this.clauses.get(i).typeCheck(visitor, contextInfo);
            for (LocalVariableBinding b : bindings = this.clauses.get(i).getRangeVariables()) {
                ArrayList<VariableReference> references = new ArrayList<VariableReference>();
                for (int j = i; j < this.clauses.size(); ++j) {
                    this.clauses.get(j).gatherVariableReferences(visitor, b, references);
                }
                ExpressionTool.gatherVariableReferences(this.getReturnClause(), b, references);
                this.clauses.get(i).refineVariableType(visitor, references, this.getReturnClause());
            }
        }
        this.returnClauseOp.typeCheck(visitor, contextInfo);
        return this;
    }

    @Override
    public boolean implementsStaticTypeCheck() {
        block3: for (Clause c : this.clauses) {
            switch (c.getClauseKey()) {
                case LET: 
                case WHERE: {
                    continue block3;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public Expression staticTypeCheck(SequenceType req, boolean backwardsCompatible, RoleDiagnostic role, ExpressionVisitor visitor) throws XPathException {
        TypeChecker tc = visitor.getConfiguration().getTypeChecker(backwardsCompatible);
        this.returnClauseOp.setChildExpression(tc.staticTypeCheck(this.getReturnClause(), req, role, visitor));
        return this;
    }

    @Override
    public ItemType getItemType() {
        return this.getReturnClause().getItemType();
    }

    @Override
    protected int computeCardinality() {
        return 57344;
    }

    @Override
    public Iterable<Operand> operands() {
        ArrayList<Operand> list = new ArrayList<Operand>(5);
        boolean repeatable = false;
        try {
            for (Clause c : this.clauses) {
                c.processOperands(list::add);
                if (!(c instanceof ForClause)) continue;
                repeatable = true;
            }
        } catch (XPathException e) {
            throw new IllegalStateException(e);
        }
        list.add(this.returnClauseOp);
        return list;
    }

    @Override
    public void checkForUpdatingSubexpressions() throws XPathException {
        OperandProcessor processor = op -> {
            op.getChildExpression().checkForUpdatingSubexpressions();
            if (op.getChildExpression().isUpdatingExpression()) {
                throw new XPathException("An updating expression cannot be used in a clause of a FLWOR expression", "XUST0001");
            }
        };
        for (Clause c : this.clauses) {
            c.processOperands(processor);
        }
        this.getReturnClause().checkForUpdatingSubexpressions();
    }

    @Override
    public boolean isUpdatingExpression() {
        return this.getReturnClause().isUpdatingExpression();
    }

    @Override
    public int getImplementationMethod() {
        return 6;
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        for (Clause c : this.clauses) {
            c.addToPathMap(pathMap, pathMapNodeSet);
        }
        return this.getReturnClause().addToPathMap(pathMap, pathMapNodeSet);
    }

    public void injectCode(CodeInjector injector) {
        if (injector != null) {
            ArrayList<Clause> expandedList = new ArrayList<Clause>(this.clauses.size() * 2);
            expandedList.add(this.clauses.get(0));
            for (int i = 1; i < this.clauses.size(); ++i) {
                Clause extra = injector.injectClause(this, this.clauses.get(i - 1));
                if (extra != null) {
                    expandedList.add(extra);
                }
                expandedList.add(this.clauses.get(i));
            }
            Clause extra = injector.injectClause(this, this.clauses.get(this.clauses.size() - 1));
            if (extra != null) {
                expandedList.add(extra);
            }
            this.clauses = expandedList;
        }
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("FLWOR", this);
        for (Clause c : this.clauses) {
            c.explain(out);
        }
        out.startSubsidiaryElement("return");
        this.getReturnClause().export(out);
        out.endSubsidiaryElement();
        out.endElement();
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        ArrayList<Clause> newClauses = new ArrayList<Clause>();
        FLWORExpression f2 = new FLWORExpression();
        for (Clause c : this.clauses) {
            Clause c2 = c.copy(f2, rebindings);
            c2.setLocation(c.getLocation());
            c2.setRepeated(c.isRepeated());
            LocalVariableBinding[] oldBindings = c.getRangeVariables();
            LocalVariableBinding[] newBindings = c2.getRangeVariables();
            assert (oldBindings.length == newBindings.length);
            for (int i = 0; i < oldBindings.length; ++i) {
                rebindings.put(oldBindings[i], newBindings[i]);
            }
            newClauses.add(c2);
        }
        f2.init(newClauses, this.getReturnClause().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, f2);
        return f2;
    }

    @Override
    public Expression unordered(boolean retainAllNodes, boolean forStreaming) throws XPathException {
        for (Clause c : this.clauses) {
            if (!(c instanceof ForClause) || ((ForClause)c).getPositionVariable() != null) continue;
            ((ForClause)c).setSequence(((ForClause)c).getSequence().unordered(retainAllNodes, forStreaming));
        }
        this.returnClauseOp.setChildExpression(this.getReturnClause().unordered(retainAllNodes, forStreaming));
        return this;
    }

    private Binding[] extendBindingList(Binding[] bindings, LocalVariableBinding[] moreBindings) {
        if (bindings == null) {
            bindings = new Binding[]{};
        }
        if (moreBindings == null || moreBindings.length == 0) {
            return bindings;
        }
        Binding[] b2 = new Binding[bindings.length + moreBindings.length];
        System.arraycopy(bindings, 0, b2, 0, bindings.length);
        System.arraycopy(moreBindings, 0, b2, bindings.length, moreBindings.length);
        return b2;
    }

    @Override
    public int getEvaluationMethod() {
        return 4;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        boolean tryAgain;
        Clause c;
        for (Clause c2 : this.clauses) {
            c2.processOperands(op -> op.optimize(visitor, contextItemType));
            c2.optimize(visitor, contextItemType);
        }
        this.returnClauseOp.setChildExpression(this.getReturnClause().optimize(visitor, contextItemType));
        if (this.clauses.size() == 1 && ((c = this.clauses.get(0)) instanceof LetClause || c instanceof ForClause && ((ForClause)c).getPositionVariable() == null)) {
            return this.rewriteForOrLet(visitor, contextItemType);
        }
        boolean changed = false;
        do {
            tryAgain = false;
            for (Clause clause : this.clauses) {
                boolean simpleSeq;
                if (clause.getClauseKey() != Clause.ClauseName.LET) continue;
                LetClause lc = (LetClause)clause;
                if (!ExpressionTool.dependsOnVariable(this, new Binding[]{lc.getRangeVariable()})) {
                    this.clauses.remove(clause);
                    tryAgain = true;
                    break;
                }
                boolean suppressInlining = false;
                for (Clause c2 : this.clauses) {
                    if (!c2.containsNonInlineableVariableReference(lc.getRangeVariable())) continue;
                    suppressInlining = true;
                    break;
                }
                if (suppressInlining) continue;
                boolean oneRef = lc.getRangeVariable().getNominalReferenceCount() == 1;
                boolean bl = simpleSeq = lc.getSequence() instanceof VariableReference || lc.getSequence() instanceof Literal;
                if (!oneRef && !simpleSeq) continue;
                ExpressionTool.replaceVariableReferences(this, lc.getRangeVariable(), lc.getSequence(), true);
                this.clauses.remove(clause);
                if (this.clauses.isEmpty()) {
                    return this.getReturnClause();
                }
                tryAgain = true;
                break;
            }
            changed |= tryAgain;
        } while (tryAgain);
        if (changed) {
            for (int i = this.clauses.size() - 1; i >= 1; --i) {
                if (this.clauses.get(i).getClauseKey() != Clause.ClauseName.TRACE || this.clauses.get(i - 1).getClauseKey() != Clause.ClauseName.TRACE) continue;
                this.clauses.remove(i);
            }
        }
        boolean depends = false;
        for (Clause w : this.clauses) {
            if (!(w instanceof WhereClause) || !ExpressionTool.dependsOnFocus(((WhereClause)w).getPredicate())) continue;
            depends = true;
            break;
        }
        if (depends && contextItemType != null) {
            Expression expression = ExpressionTool.tryToFactorOutDot(this, contextItemType.getItemType());
            if (expression == null || expression == this) {
                return this;
            }
            this.resetLocalStaticProperties();
            return expression.optimize(visitor, contextItemType);
        }
        Expression expression = this.rewriteWhereClause(visitor, contextItemType);
        if (expression != null && expression != this) {
            return expression.optimize(visitor, contextItemType);
        }
        boolean allForOrLetExpr = true;
        for (Clause c4 : this.clauses) {
            if (c4 instanceof ForClause) {
                if (((ForClause)c4).getPositionVariable() == null) continue;
                allForOrLetExpr = false;
                break;
            }
            if (c4 instanceof LetClause) continue;
            allForOrLetExpr = false;
            break;
        }
        if (allForOrLetExpr) {
            return this.rewriteForOrLet(visitor, contextItemType);
        }
        return this;
    }

    private Expression rewriteWhereClause(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        int whereIndex = 0;
        class WhereClauseStruct {
            int whereIndex = 0;
            WhereClause whereClause;

            WhereClauseStruct() {
            }
        }
        ArrayList<WhereClauseStruct> whereList = new ArrayList<WhereClauseStruct>();
        for (Clause c : this.clauses) {
            if (c instanceof WhereClause) {
                WhereClauseStruct wStruct = new WhereClauseStruct();
                wStruct.whereClause = (WhereClause)c;
                wStruct.whereIndex = this.clauses.size() - whereIndex;
                whereList.add(wStruct);
            }
            ++whereIndex;
        }
        if (whereList.size() == 0) {
            return null;
        }
        while (!whereList.isEmpty()) {
            WhereClause whereClause = ((WhereClauseStruct)whereList.get((int)0)).whereClause;
            whereIndex = ((WhereClauseStruct)whereList.get((int)0)).whereIndex;
            Expression condition = whereClause.getPredicate();
            ArrayList<Expression> list = new ArrayList<Expression>(5);
            BooleanExpression.listAndComponents(condition, list);
            for (int i = list.size() - 1; i >= 0; --i) {
                Expression term = (Expression)list.get(i);
                for (int c = this.clauses.size() - whereIndex - 1; c >= 0; --c) {
                    Clause clause = this.clauses.get(c);
                    Binding[] bindingList = clause.getRangeVariables();
                    if (!ExpressionTool.dependsOnVariable(term, bindingList) && clause.getClauseKey() != Clause.ClauseName.COUNT) continue;
                    Expression removedExpr = (Expression)list.remove(i);
                    if (list.isEmpty()) {
                        this.clauses.remove(this.clauses.size() - whereIndex);
                    } else {
                        whereClause.setPredicate(this.makeAndCondition(list));
                    }
                    if (clause instanceof ForClause && !((ForClause)clause).isAllowingEmpty()) {
                        boolean added = ((ForClause)clause).addPredicate(this, visitor, contextItemType, term);
                        if (added) break;
                        this.clauses.add(c + 1, new WhereClause(this, removedExpr));
                        break;
                    }
                    WhereClause newWhere = new WhereClause(this, term);
                    this.clauses.add(c + 1, newWhere);
                    break;
                }
                if (list.size() - 1 != i) continue;
                list.remove(i);
                if (list.isEmpty()) {
                    this.clauses.remove(this.clauses.size() - whereIndex);
                } else {
                    whereClause.setPredicate(this.makeAndCondition(list));
                }
                WhereClause newWhere = new WhereClause(this, term);
                this.clauses.add(0, newWhere);
            }
            whereList.remove(0);
        }
        return this;
    }

    private Expression makeAndCondition(List<Expression> list) {
        if (list.size() == 1) {
            return list.get(0);
        }
        return new AndExpression(list.get(0), this.makeAndCondition(list.subList(1, list.size())));
    }

    private Expression rewriteForOrLet(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        Expression action = this.getReturnClause();
        CodeInjector injector = null;
        if (visitor.getStaticContext() instanceof QueryModule) {
            injector = ((QueryModule)visitor.getStaticContext()).getCodeInjector();
        }
        for (int i = this.clauses.size() - 1; i >= 0; --i) {
            if (this.clauses.get(i) instanceof ForClause) {
                ForClause forClause = (ForClause)this.clauses.get(i);
                ForExpression forExpr = forClause.isAllowingEmpty() ? new OuterForExpression() : new ForExpression();
                forExpr.setLocation(forClause.getLocation());
                forExpr.setRetainedStaticContext(this.getRetainedStaticContext());
                forExpr.setAction(action);
                forExpr.setSequence(forClause.getSequence());
                forExpr.setVariableQName(forClause.getRangeVariable().getVariableQName());
                forExpr.setRequiredType(forClause.getRangeVariable().getRequiredType());
                ExpressionTool.rebindVariableReferences(action, forClause.getRangeVariable(), forExpr);
                action = forExpr;
                continue;
            }
            LetClause letClause = (LetClause)this.clauses.get(i);
            LetExpression letExpr = new LetExpression();
            letExpr.setLocation(letClause.getLocation());
            letExpr.setRetainedStaticContext(this.getRetainedStaticContext());
            letExpr.setAction(action);
            letExpr.setSequence(letClause.getSequence());
            letExpr.setVariableQName(letClause.getRangeVariable().getVariableQName());
            letExpr.setRequiredType(letClause.getRangeVariable().getRequiredType());
            if (letClause.getRangeVariable().isIndexedVariable()) {
                letExpr.setIndexedVariable();
            }
            ExpressionTool.rebindVariableReferences(action, letClause.getRangeVariable(), letExpr);
            action = letExpr;
        }
        action = action.typeCheck(visitor, contextItemType);
        action = action.optimize(visitor, contextItemType);
        return action;
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        TuplePull stream = new SingularityPull();
        for (Clause c : this.clauses) {
            stream = c.getPullStream(stream, context);
        }
        return new ReturnClauseIterator(stream, this, context);
    }

    @Override
    public void process(Outputter output, XPathContext context) throws XPathException {
        TuplePush destination = new ReturnClausePush(output, this.getReturnClause());
        for (int i = this.clauses.size() - 1; i >= 0; --i) {
            Clause c = this.clauses.get(i);
            destination = c.getPushStream(destination, output, context);
        }
        ((TuplePush)destination).processTuple(context);
        ((TuplePush)destination).close();
    }

    @Override
    public void evaluatePendingUpdates(XPathContext context, PendingUpdateList pul) throws XPathException {
        TuplePull stream = new SingularityPull();
        for (Clause c : this.clauses) {
            stream = c.getPullStream(stream, context);
        }
        while (((TuplePull)stream).nextTuple(context)) {
            this.getReturnClause().evaluatePendingUpdates(context, pul);
        }
    }

    @Override
    public String getExpressionName() {
        return "FLWOR";
    }

    @Override
    public String toShortString() {
        FastStringBuffer sb = new FastStringBuffer(64);
        sb.append(this.clauses.get(0).toShortString());
        sb.append(" ... return ");
        sb.append(this.getReturnClause().toShortString());
        return sb.toString();
    }

    @Override
    public String toString() {
        FastStringBuffer sb = new FastStringBuffer(64);
        for (Clause c : this.clauses) {
            sb.append(c.toString());
            sb.cat(' ');
        }
        sb.append(" return ");
        sb.append(this.getReturnClause().toString());
        return sb.toString();
    }

    public boolean hasLoopingVariableReference(Binding binding) {
        boolean boundOutside;
        int bindingClause = -1;
        for (int i = 0; i < this.clauses.size(); ++i) {
            if (!this.clauseHasBinding(this.clauses.get(i), binding)) continue;
            bindingClause = i;
            break;
        }
        boolean bl = boundOutside = bindingClause < 0;
        if (boundOutside) {
            bindingClause = 0;
        }
        int lastReferencingClause = this.clauses.size();
        if (!ExpressionTool.dependsOnVariable(this.getReturnClause(), new Binding[]{binding})) {
            ArrayList response = new ArrayList();
            OperandProcessor checker = op -> {
                if (response.isEmpty() && ExpressionTool.dependsOnVariable(op.getChildExpression(), new Binding[]{binding})) {
                    response.add(true);
                }
            };
            for (int i = this.clauses.size() - 1; i >= 0; --i) {
                try {
                    this.clauses.get(i).processOperands(checker);
                    if (response.isEmpty()) continue;
                    lastReferencingClause = i;
                    break;
                } catch (XPathException e) {
                    assert (false);
                    continue;
                }
            }
        }
        for (int i = lastReferencingClause - 1; i >= bindingClause; --i) {
            if (!FLWORExpression.isLoopingClause(this.clauses.get(i))) continue;
            return true;
        }
        return false;
    }
}


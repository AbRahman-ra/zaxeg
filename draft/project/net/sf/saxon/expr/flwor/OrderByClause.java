/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.Clause;
import net.sf.saxon.expr.flwor.FLWORExpression;
import net.sf.saxon.expr.flwor.OperandProcessor;
import net.sf.saxon.expr.flwor.OrderByClausePull;
import net.sf.saxon.expr.flwor.OrderByClausePush;
import net.sf.saxon.expr.flwor.TupleExpression;
import net.sf.saxon.expr.flwor.TuplePull;
import net.sf.saxon.expr.flwor.TuplePush;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.SortKeyDefinition;
import net.sf.saxon.expr.sort.SortKeyDefinitionList;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.SequenceType;

public class OrderByClause
extends Clause {
    public static final OperandRole SORT_KEYS_ROLE = new OperandRole(20, OperandUsage.NAVIGATION, SequenceType.ANY_SEQUENCE, expr -> expr instanceof SortKeyDefinitionList);
    Operand sortKeysOp;
    AtomicComparer[] comparators;
    Operand tupleOp;

    public OrderByClause(FLWORExpression flwor, SortKeyDefinition[] sortKeys, TupleExpression tupleExpression) {
        this.sortKeysOp = new Operand(flwor, new SortKeyDefinitionList(sortKeys), SORT_KEYS_ROLE);
        this.tupleOp = new Operand(flwor, tupleExpression, OperandRole.FLWOR_TUPLE_CONSTRAINED);
    }

    @Override
    public Clause.ClauseName getClauseKey() {
        return Clause.ClauseName.ORDER_BY;
    }

    @Override
    public boolean containsNonInlineableVariableReference(Binding binding) {
        return this.getTupleExpression().includesBinding(binding);
    }

    @Override
    public OrderByClause copy(FLWORExpression flwor, RebindingMap rebindings) {
        SortKeyDefinitionList sortKeys = this.getSortKeyDefinitions();
        SortKeyDefinition[] sk2 = new SortKeyDefinition[sortKeys.size()];
        for (int i = 0; i < sortKeys.size(); ++i) {
            sk2[i] = sortKeys.getSortKeyDefinition(i).copy(rebindings);
        }
        OrderByClause obc = new OrderByClause(flwor, sk2, (TupleExpression)this.getTupleExpression().copy(rebindings));
        obc.setLocation(this.getLocation());
        obc.setPackageData(this.getPackageData());
        obc.comparators = this.comparators;
        return obc;
    }

    public SortKeyDefinitionList getSortKeyDefinitions() {
        return (SortKeyDefinitionList)this.sortKeysOp.getChildExpression();
    }

    public AtomicComparer[] getAtomicComparers() {
        return this.comparators;
    }

    public TupleExpression getTupleExpression() {
        return (TupleExpression)this.tupleOp.getChildExpression();
    }

    @Override
    public TuplePull getPullStream(TuplePull base, XPathContext context) {
        return new OrderByClausePull(base, this.getTupleExpression(), this, context);
    }

    @Override
    public TuplePush getPushStream(TuplePush destination, Outputter output, XPathContext context) {
        return new OrderByClausePush(output, destination, this.getTupleExpression(), this, context);
    }

    @Override
    public void processOperands(OperandProcessor processor) throws XPathException {
        processor.processOperand(this.tupleOp);
        processor.processOperand(this.sortKeysOp);
    }

    @Override
    public void typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        boolean allKeysFixed = true;
        SortKeyDefinitionList sortKeys = this.getSortKeyDefinitions();
        for (SortKeyDefinition sk : sortKeys) {
            if (sk.isFixed()) continue;
            allKeysFixed = false;
            break;
        }
        if (allKeysFixed) {
            this.comparators = new AtomicComparer[sortKeys.size()];
        }
        int i = 0;
        TypeChecker tc = visitor.getConfiguration().getTypeChecker(false);
        for (SortKeyDefinition skd : sortKeys) {
            Expression sortKey = skd.getSortKey();
            RoleDiagnostic role = new RoleDiagnostic(6, "", i);
            role.setErrorCode("XPTY0004");
            sortKey = tc.staticTypeCheck(sortKey, SequenceType.OPTIONAL_ATOMIC, role, visitor);
            skd.setSortKey(sortKey, false);
            skd.typeCheck(visitor, contextInfo);
            if (skd.isFixed()) {
                AtomicComparer comp = skd.makeComparator(visitor.getStaticContext().makeEarlyEvaluationContext());
                skd.setFinalComparator(comp);
                if (allKeysFixed) {
                    this.comparators[i] = comp;
                }
            }
            ++i;
        }
    }

    @Override
    public void addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        SortKeyDefinitionList sortKeys = this.getSortKeyDefinitions();
        for (SortKeyDefinition skd : sortKeys) {
            Expression sortKey = skd.getSortKey();
            sortKey.addToPathMap(pathMap, pathMapNodeSet);
        }
    }

    @Override
    public void explain(ExpressionPresenter out) throws XPathException {
        out.startElement("order-by");
        for (SortKeyDefinition k : this.getSortKeyDefinitions()) {
            out.startSubsidiaryElement("key");
            k.getSortKey().export(out);
            out.endSubsidiaryElement();
        }
        out.endElement();
    }

    public String toString() {
        FastStringBuffer fsb = new FastStringBuffer(64);
        fsb.append("order by ... ");
        return fsb.toString();
    }

    public AtomicValue evaluateSortKey(int n, XPathContext c) throws XPathException {
        SortKeyDefinitionList sortKeys = this.getSortKeyDefinitions();
        return (AtomicValue)sortKeys.getSortKeyDefinition(n).getSortKey().evaluateItem(c);
    }
}


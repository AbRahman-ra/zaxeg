/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FirstItemExpression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.SortKeyDefinition;
import net.sf.saxon.expr.sort.SortKeyDefinitionList;
import net.sf.saxon.expr.sort.SortKeyEvaluator;
import net.sf.saxon.expr.sort.SortedIterator;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.SequenceType;

public class SortExpression
extends Expression
implements SortKeyEvaluator {
    private Operand selectOp;
    private Operand sortOp;
    private transient AtomicComparer[] comparators = null;
    private static final OperandRole SAME_FOCUS_SORT_KEY = new OperandRole(4, OperandUsage.ABSORPTION, SequenceType.OPTIONAL_ATOMIC);
    private static final OperandRole NEW_FOCUS_SORT_KEY = new OperandRole(6, OperandUsage.ABSORPTION, SequenceType.OPTIONAL_ATOMIC);

    public SortExpression(Expression select, SortKeyDefinitionList sortKeys) {
        this.selectOp = new Operand(this, select, OperandRole.FOCUS_CONTROLLING_SELECT);
        this.sortOp = new Operand(this, sortKeys, OperandRole.ATOMIC_SEQUENCE);
        this.adoptChildExpression(select);
        this.adoptChildExpression(sortKeys);
    }

    @Override
    public String getExpressionName() {
        return "sort";
    }

    public Operand getBaseOperand() {
        return this.selectOp;
    }

    public Expression getBaseExpression() {
        return this.getSelect();
    }

    public AtomicComparer[] getComparators() {
        return this.comparators;
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operandList(this.selectOp, this.sortOp);
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        PathMap.PathMapNodeSet target = this.getSelect().addToPathMap(pathMap, pathMapNodeSet);
        for (SortKeyDefinition sortKeyDefinition : this.getSortKeyDefinitionList()) {
            if (sortKeyDefinition.isSetContextForSortKey()) {
                sortKeyDefinition.getSortKey().addToPathMap(pathMap, target);
            } else {
                sortKeyDefinition.getSortKey().addToPathMap(pathMap, pathMapNodeSet);
            }
            Expression e = sortKeyDefinition.getOrder();
            if (e != null) {
                e.addToPathMap(pathMap, pathMapNodeSet);
            }
            if ((e = sortKeyDefinition.getCaseOrder()) != null) {
                e.addToPathMap(pathMap, pathMapNodeSet);
            }
            if ((e = sortKeyDefinition.getDataTypeExpression()) != null) {
                e.addToPathMap(pathMap, pathMapNodeSet);
            }
            if ((e = sortKeyDefinition.getLanguage()) != null) {
                e.addToPathMap(pathMap, pathMapNodeSet);
            }
            if ((e = sortKeyDefinition.getCollationNameExpression()) == null) continue;
            e.addToPathMap(pathMap, pathMapNodeSet);
        }
        return target;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.selectOp.typeCheck(visitor, contextInfo);
        Expression select2 = this.getSelect();
        if (select2 != this.getSelect()) {
            this.adoptChildExpression(select2);
            this.setSelect(select2);
        }
        if (!Cardinality.allowsMany(select2.getCardinality())) {
            return select2;
        }
        ItemType sortedItemType = this.getSelect().getItemType();
        boolean allKeysFixed = true;
        for (SortKeyDefinition sortKeyDefinition : this.getSortKeyDefinitionList()) {
            if (sortKeyDefinition.isFixed()) continue;
            allKeysFixed = false;
            break;
        }
        if (allKeysFixed) {
            this.comparators = new AtomicComparer[this.getSortKeyDefinitionList().size()];
        }
        TypeChecker tc = visitor.getConfiguration().getTypeChecker(false);
        for (int i = 0; i < this.getSortKeyDefinitionList().size(); ++i) {
            SortKeyDefinition sortKeyDef = this.getSortKeyDefinition(i);
            Expression sortKey = sortKeyDef.getSortKey();
            if (sortKeyDef.isSetContextForSortKey()) {
                ContextItemStaticInfo cit = visitor.getConfiguration().makeContextItemStaticInfo(sortedItemType, false);
                sortKey = sortKey.typeCheck(visitor, cit);
            } else {
                sortKey = sortKey.typeCheck(visitor, contextInfo);
            }
            if (sortKeyDef.isBackwardsCompatible()) {
                sortKey = FirstItemExpression.makeFirstItemExpression(sortKey);
            } else {
                RoleDiagnostic role = new RoleDiagnostic(4, "xsl:sort/select", 0);
                role.setErrorCode("XTTE1020");
                sortKey = tc.staticTypeCheck(sortKey, SequenceType.OPTIONAL_ATOMIC, role, visitor);
            }
            sortKeyDef.setSortKey(sortKey, sortKeyDef.isSetContextForSortKey());
            sortKeyDef.typeCheck(visitor, contextInfo);
            if (sortKeyDef.isFixed()) {
                AtomicComparer comp = sortKeyDef.makeComparator(visitor.getStaticContext().makeEarlyEvaluationContext());
                sortKeyDef.setFinalComparator(comp);
                if (allKeysFixed) {
                    this.comparators[i] = comp;
                }
            }
            if (!sortKeyDef.isSetContextForSortKey() || ExpressionTool.dependsOnFocus(sortKey)) continue;
            visitor.getStaticContext().issueWarning("Sort key will have no effect because its value does not depend on the context item", sortKey.getLocation());
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        ContextItemStaticInfo cit;
        this.selectOp.optimize(visitor, contextItemType);
        if (this.getSortKeyDefinition(0).isSetContextForSortKey()) {
            ItemType sortedItemType = this.getSelect().getItemType();
            cit = visitor.getConfiguration().makeContextItemStaticInfo(sortedItemType, false);
        } else {
            cit = contextItemType;
        }
        for (SortKeyDefinition sortKeyDefinition : this.getSortKeyDefinitionList()) {
            Expression sortKey = sortKeyDefinition.getSortKey();
            sortKey = sortKey.optimize(visitor, cit);
            sortKeyDefinition.setSortKey(sortKey, true);
        }
        if (Cardinality.allowsMany(this.getSelect().getCardinality())) {
            return this;
        }
        return this.getSelect();
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        int len = this.getSortKeyDefinitionList().size();
        SortKeyDefinition[] sk2 = new SortKeyDefinition[len];
        for (int i = 0; i < len; ++i) {
            sk2[i] = this.getSortKeyDefinition(i).copy(rebindings);
        }
        SortExpression se2 = new SortExpression(this.getSelect().copy(rebindings), new SortKeyDefinitionList(sk2));
        ExpressionTool.copyLocationInfo(this, se2);
        se2.comparators = this.comparators;
        return se2;
    }

    public boolean isSortKey(Expression child) {
        for (SortKeyDefinition sortKeyDefinition : this.getSortKeyDefinitionList()) {
            Expression exp = sortKeyDefinition.getSortKey();
            if (exp != child) continue;
            return true;
        }
        return false;
    }

    @Override
    public int computeCardinality() {
        return this.getSelect().getCardinality();
    }

    @Override
    public ItemType getItemType() {
        return this.getSelect().getItemType();
    }

    @Override
    public int computeSpecialProperties() {
        int props = 0;
        if (this.getSelect().hasSpecialProperty(65536)) {
            props |= 0x10000;
        }
        if (this.getSelect().hasSpecialProperty(0x1000000)) {
            props |= 0x1000000;
        }
        if (this.getSelect().hasSpecialProperty(0x800000)) {
            props |= 0x800000;
        }
        return props;
    }

    @Override
    public int getImplementationMethod() {
        return 2;
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        SequenceIterator iter = this.getSelect().iterate(context);
        if (iter instanceof EmptyIterator) {
            return iter;
        }
        AtomicComparer[] comps = this.comparators;
        if (this.comparators == null) {
            int len = this.getSortKeyDefinitionList().size();
            comps = new AtomicComparer[len];
            for (int s = 0; s < len; ++s) {
                AtomicComparer comp = this.getSortKeyDefinition(s).getFinalComparator();
                if (comp == null) {
                    comp = this.getSortKeyDefinition(s).makeComparator(context);
                }
                comps[s] = comp;
            }
        }
        iter = new SortedIterator(context, iter, this, comps, this.getSortKeyDefinition(0).isSetContextForSortKey());
        ((SortedIterator)iter).setHostLanguage(this.getPackageData().getHostLanguage());
        return iter;
    }

    @Override
    public AtomicValue evaluateSortKey(int n, XPathContext c) throws XPathException {
        return (AtomicValue)this.getSortKeyDefinition(n).getSortKey().evaluateItem(c);
    }

    @Override
    public String toShortString() {
        return "sort(" + this.getBaseExpression().toShortString() + ")";
    }

    @Override
    public String getStreamerName() {
        return "SortExpression";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("sort", this);
        out.setChildRole("select");
        this.getSelect().export(out);
        this.getSortKeyDefinitionList().export(out);
        out.endElement();
    }

    public Expression getSelect() {
        return this.selectOp.getChildExpression();
    }

    public void setSelect(Expression select) {
        this.selectOp.setChildExpression(select);
    }

    public SortKeyDefinitionList getSortKeyDefinitionList() {
        return (SortKeyDefinitionList)this.sortOp.getChildExpression();
    }

    public SortKeyDefinition getSortKeyDefinition(int i) {
        return this.getSortKeyDefinitionList().getSortKeyDefinition(i);
    }

    public void setSortKeyDefinitionList(SortKeyDefinitionList skd) {
        this.sortOp.setChildExpression(skd);
    }
}


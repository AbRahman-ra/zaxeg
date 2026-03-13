/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.PseudoExpression;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.sort.SortKeyDefinition;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;

public class SortKeyDefinitionList
extends PseudoExpression
implements Iterable<SortKeyDefinition> {
    private SortKeyDefinition[] sortKeyDefinitions;

    public SortKeyDefinitionList(SortKeyDefinition[] sortKeyDefinitions) {
        this.sortKeyDefinitions = sortKeyDefinitions;
    }

    @Override
    public Iterable<Operand> operands() {
        ArrayList<Operand> list = new ArrayList<Operand>(this.size());
        for (SortKeyDefinition skd : this.sortKeyDefinitions) {
            list.add(new Operand(this, skd, OperandRole.INSPECT));
        }
        return list;
    }

    @Override
    public boolean isLiftable(boolean forStreaming) {
        return false;
    }

    public int size() {
        return this.sortKeyDefinitions.length;
    }

    public SortKeyDefinition getSortKeyDefinition(int i) {
        return this.sortKeyDefinitions[i];
    }

    @Override
    public Iterator<SortKeyDefinition> iterator() {
        return Arrays.asList(this.sortKeyDefinitions).iterator();
    }

    @Override
    public SortKeyDefinitionList copy(RebindingMap rebindings) {
        SortKeyDefinition[] s2 = new SortKeyDefinition[this.sortKeyDefinitions.length];
        for (int i = 0; i < this.sortKeyDefinitions.length; ++i) {
            s2[i] = this.sortKeyDefinitions[i].copy(rebindings);
        }
        return new SortKeyDefinitionList(s2);
    }

    @Override
    public int getImplementationMethod() {
        return 0;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        for (SortKeyDefinition skd : this.sortKeyDefinitions) {
            skd.export(out);
        }
    }
}


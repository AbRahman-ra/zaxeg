/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.Clause;
import net.sf.saxon.expr.flwor.CountClausePull;
import net.sf.saxon.expr.flwor.CountClausePush;
import net.sf.saxon.expr.flwor.FLWORExpression;
import net.sf.saxon.expr.flwor.LocalVariableBinding;
import net.sf.saxon.expr.flwor.OperandProcessor;
import net.sf.saxon.expr.flwor.TuplePull;
import net.sf.saxon.expr.flwor.TuplePush;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;

public class CountClause
extends Clause {
    private LocalVariableBinding rangeVariable;

    @Override
    public Clause.ClauseName getClauseKey() {
        return Clause.ClauseName.COUNT;
    }

    @Override
    public CountClause copy(FLWORExpression flwor, RebindingMap rebindings) {
        CountClause c2 = new CountClause();
        c2.rangeVariable = this.rangeVariable.copy();
        c2.setPackageData(this.getPackageData());
        c2.setLocation(this.getLocation());
        return c2;
    }

    public void setRangeVariable(LocalVariableBinding binding) {
        this.rangeVariable = binding;
    }

    public LocalVariableBinding getRangeVariable() {
        return this.rangeVariable;
    }

    @Override
    public LocalVariableBinding[] getRangeVariables() {
        return new LocalVariableBinding[]{this.rangeVariable};
    }

    @Override
    public TuplePull getPullStream(TuplePull base, XPathContext context) {
        return new CountClausePull(base, this);
    }

    @Override
    public TuplePush getPushStream(TuplePush destination, Outputter output, XPathContext context) {
        return new CountClausePush(output, destination, this);
    }

    @Override
    public void processOperands(OperandProcessor processor) throws XPathException {
    }

    @Override
    public void addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
    }

    @Override
    public void explain(ExpressionPresenter out) throws XPathException {
        out.startElement("count");
        out.emitAttribute("var", this.getRangeVariable().getVariableQName());
        out.endElement();
    }

    public String toString() {
        FastStringBuffer fsb = new FastStringBuffer(64);
        fsb.append("count $");
        fsb.append(this.rangeVariable.getVariableQName().getDisplayName());
        return fsb.toString();
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.Clause;
import net.sf.saxon.expr.flwor.FLWORExpression;
import net.sf.saxon.expr.flwor.OperandProcessor;
import net.sf.saxon.expr.flwor.TraceClausePull;
import net.sf.saxon.expr.flwor.TraceClausePush;
import net.sf.saxon.expr.flwor.TuplePull;
import net.sf.saxon.expr.flwor.TuplePush;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;

public class TraceClause
extends Clause {
    private Clause target;
    private NamespaceResolver nsResolver;

    public TraceClause(FLWORExpression expression, Clause target) {
        this.target = target;
        this.nsResolver = expression.getRetainedStaticContext();
    }

    public NamespaceResolver getNamespaceResolver() {
        return this.nsResolver;
    }

    public void setNamespaceResolver(NamespaceResolver nsResolver) {
        this.nsResolver = nsResolver;
    }

    @Override
    public Clause.ClauseName getClauseKey() {
        return Clause.ClauseName.TRACE;
    }

    @Override
    public TraceClause copy(FLWORExpression flwor, RebindingMap rebindings) {
        return new TraceClause(flwor, this.target);
    }

    @Override
    public TuplePull getPullStream(TuplePull base, XPathContext context) {
        return new TraceClausePull(base, this, this.target);
    }

    @Override
    public TuplePush getPushStream(TuplePush destination, Outputter output, XPathContext context) {
        return new TraceClausePush(output, destination, this, this.target);
    }

    @Override
    public void processOperands(OperandProcessor processor) throws XPathException {
    }

    @Override
    public void addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
    }

    @Override
    public void explain(ExpressionPresenter out) throws XPathException {
        out.startElement("trace");
        out.endElement();
    }

    public String toString() {
        return "trace";
    }
}


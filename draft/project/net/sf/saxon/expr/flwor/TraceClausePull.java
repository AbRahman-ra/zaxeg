/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import java.util.Collections;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.Clause;
import net.sf.saxon.expr.flwor.ClauseInfo;
import net.sf.saxon.expr.flwor.TraceClause;
import net.sf.saxon.expr.flwor.TuplePull;
import net.sf.saxon.trans.XPathException;

public class TraceClausePull
extends TuplePull {
    private TuplePull base;
    private Clause baseClause;
    private TraceClause traceClause;

    public TraceClausePull(TuplePull base, TraceClause traceClause, Clause baseClause) {
        this.base = base;
        this.traceClause = traceClause;
        this.baseClause = baseClause;
    }

    @Override
    public boolean nextTuple(XPathContext context) throws XPathException {
        Controller controller = context.getController();
        if (controller.isTracing()) {
            ClauseInfo baseInfo = new ClauseInfo(this.baseClause);
            baseInfo.setNamespaceResolver(this.traceClause.getNamespaceResolver());
            controller.getTraceListener().enter(baseInfo, Collections.emptyMap(), context);
            boolean b = this.base.nextTuple(context);
            controller.getTraceListener().leave(baseInfo);
            return b;
        }
        return this.base.nextTuple(context);
    }

    @Override
    public void close() {
        this.base.close();
    }
}


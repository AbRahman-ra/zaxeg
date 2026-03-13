/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import java.util.Collections;
import net.sf.saxon.Controller;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.Clause;
import net.sf.saxon.expr.flwor.ClauseInfo;
import net.sf.saxon.expr.flwor.TraceClause;
import net.sf.saxon.expr.flwor.TuplePush;
import net.sf.saxon.trans.XPathException;

public class TraceClausePush
extends TuplePush {
    private TuplePush destination;
    TraceClause traceClause;
    private Clause baseClause;

    public TraceClausePush(Outputter outputter, TuplePush destination, TraceClause traceClause, Clause baseClause) {
        super(outputter);
        this.destination = destination;
        this.traceClause = traceClause;
        this.baseClause = baseClause;
    }

    @Override
    public void processTuple(XPathContext context) throws XPathException {
        Controller controller = context.getController();
        if (controller.isTracing()) {
            ClauseInfo baseInfo = new ClauseInfo(this.baseClause);
            baseInfo.setNamespaceResolver(this.traceClause.getNamespaceResolver());
            controller.getTraceListener().enter(baseInfo, Collections.emptyMap(), context);
            this.destination.processTuple(context);
            controller.getTraceListener().leave(baseInfo);
        } else {
            this.destination.processTuple(context);
        }
    }

    @Override
    public void close() throws XPathException {
        this.destination.close();
    }
}


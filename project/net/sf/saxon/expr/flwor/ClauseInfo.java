/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import net.sf.saxon.expr.flwor.Clause;
import net.sf.saxon.expr.flwor.LocalVariableBinding;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.Traceable;

public class ClauseInfo
implements Traceable {
    private Clause clause;
    private NamespaceResolver nsResolver;

    public ClauseInfo(Clause clause) {
        this.clause = clause;
    }

    public Clause getClause() {
        return this.clause;
    }

    @Override
    public Location getLocation() {
        return this.clause.getLocation();
    }

    @Override
    public StructuredQName getObjectName() {
        LocalVariableBinding[] vars = this.clause.getRangeVariables();
        if (vars != null && vars.length > 0) {
            return vars[0].getVariableQName();
        }
        return null;
    }

    public NamespaceResolver getNamespaceResolver() {
        return this.nsResolver;
    }

    public void setNamespaceResolver(NamespaceResolver nsResolver) {
        this.nsResolver = nsResolver;
    }

    public String getSystemId() {
        return this.clause.getLocation().getSystemId();
    }

    public int getLineNumber() {
        return this.clause.getLocation().getLineNumber();
    }

    public String getPublicId() {
        return null;
    }

    public int getColumnNumber() {
        return -1;
    }
}


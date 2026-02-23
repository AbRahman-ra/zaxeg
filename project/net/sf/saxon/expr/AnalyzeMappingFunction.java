/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.ContextMappingFunction;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.regex.RegexIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;

public class AnalyzeMappingFunction
implements ContextMappingFunction {
    private RegexIterator base;
    private XPathContext c2;
    private Expression nonMatchExpr;
    private Expression matchingExpr;

    public AnalyzeMappingFunction(RegexIterator base, XPathContext c2, Expression nonMatchExpr, Expression matchingExpr) {
        this.base = base;
        this.c2 = c2;
        this.nonMatchExpr = nonMatchExpr;
        this.matchingExpr = matchingExpr;
    }

    @Override
    public SequenceIterator map(XPathContext context) throws XPathException {
        if (this.base.isMatching()) {
            if (this.matchingExpr != null) {
                return this.matchingExpr.iterate(this.c2);
            }
        } else if (this.nonMatchExpr != null) {
            return this.nonMatchExpr.iterate(this.c2);
        }
        return EmptyIterator.getInstance();
    }
}


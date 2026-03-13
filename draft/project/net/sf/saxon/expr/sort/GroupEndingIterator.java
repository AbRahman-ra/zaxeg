/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import java.util.ArrayList;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.GroupIterator;
import net.sf.saxon.expr.sort.GroupMatchingIterator;
import net.sf.saxon.functions.Count;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.LookaheadIterator;

public class GroupEndingIterator
extends GroupMatchingIterator
implements GroupIterator,
LookaheadIterator {
    public GroupEndingIterator(Expression select, Pattern endPattern, XPathContext context) throws XPathException {
        this.select = select;
        this.pattern = endPattern;
        this.baseContext = context;
        this.runningContext = context.newMinorContext();
        this.population = this.runningContext.trackFocus(select.iterate(context));
        this.next = this.population.next();
    }

    @Override
    public int getLength() throws XPathException {
        GroupEndingIterator another = new GroupEndingIterator(this.select, this.pattern, this.baseContext);
        return Count.steppingCount(another);
    }

    @Override
    protected void advance() throws XPathException {
        this.currentMembers = new ArrayList(20);
        this.currentMembers.add(this.current);
        this.next = this.current;
        while (this.next != null) {
            if (this.pattern.matches(this.next, this.runningContext)) {
                this.next = this.population.next();
                if (this.next == null) continue;
                break;
            }
            this.next = this.population.next();
            if (this.next == null) continue;
            this.currentMembers.add(this.next);
        }
    }
}


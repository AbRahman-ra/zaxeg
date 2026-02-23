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
import net.sf.saxon.om.Item;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.LookaheadIterator;

public class GroupStartingIterator
extends GroupMatchingIterator
implements LookaheadIterator,
GroupIterator {
    public GroupStartingIterator(Expression select, Pattern startPattern, XPathContext context) throws XPathException {
        this.select = select;
        this.pattern = startPattern;
        this.baseContext = context;
        this.runningContext = context.newMinorContext();
        this.population = this.runningContext.trackFocus(select.iterate(context));
        this.next = this.population.next();
    }

    @Override
    public int getLength() throws XPathException {
        GroupStartingIterator another = new GroupStartingIterator(this.select, this.pattern, this.baseContext);
        return Count.steppingCount(another);
    }

    @Override
    protected void advance() throws XPathException {
        Item nextCandidate;
        this.currentMembers = new ArrayList(10);
        this.currentMembers.add(this.current);
        while ((nextCandidate = this.population.next()) != null) {
            if (this.pattern.matches(nextCandidate, this.runningContext)) {
                this.next = nextCandidate;
                return;
            }
            this.currentMembers.add(nextCandidate);
        }
        this.next = null;
    }
}


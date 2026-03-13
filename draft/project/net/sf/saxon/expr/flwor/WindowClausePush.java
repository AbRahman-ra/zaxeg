/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.LocalVariableBinding;
import net.sf.saxon.expr.flwor.TuplePush;
import net.sf.saxon.expr.flwor.WindowClause;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.SequenceExtent;

public class WindowClausePush
extends TuplePush {
    private WindowClause windowClause;
    private TuplePush destination;
    List<WindowClause.Window> currentWindows = new ArrayList<WindowClause.Window>();

    public WindowClausePush(Outputter outputter, TuplePush destination, WindowClause windowClause) {
        super(outputter);
        this.windowClause = windowClause;
        this.destination = destination;
    }

    @Override
    public void processTuple(XPathContext context) throws XPathException {
        this.currentWindows = new ArrayList<WindowClause.Window>();
        boolean autoclose = this.windowClause.isTumblingWindow() && this.windowClause.getEndCondition() == null;
        Item previousPrevious = null;
        Item previous = null;
        Item current = null;
        Item next = null;
        int position = -1;
        SequenceIterator iter = this.windowClause.getSequence().iterate(context);
        boolean finished = false;
        while (!finished) {
            previousPrevious = previous;
            previous = current;
            current = next;
            next = iter.next();
            if (next == null) {
                finished = true;
            }
            if (++position <= 0) continue;
            if ((this.windowClause.isSlidingWindow() || this.currentWindows.isEmpty() || autoclose) && this.windowClause.matchesStart(previous, current, next, position, context)) {
                if (autoclose && !this.currentWindows.isEmpty()) {
                    WindowClause.Window w = this.currentWindows.get(0);
                    w.endItem = previous;
                    w.endPreviousItem = previousPrevious;
                    w.endNextItem = current;
                    w.endPosition = position - 1;
                    this.despatch(w, this.getOutputter(), context);
                    this.currentWindows.clear();
                }
                WindowClause.Window window = new WindowClause.Window();
                window.startPosition = position;
                window.startItem = current;
                window.startPreviousItem = previous;
                window.startNextItem = next;
                window.contents = new ArrayList<Item>();
                this.currentWindows.add(window);
            }
            for (WindowClause.Window window : this.currentWindows) {
                if (window.isFinished()) continue;
                window.contents.add(current);
            }
            if (this.windowClause.getEndCondition() == null) continue;
            ArrayList<WindowClause.Window> removals = new ArrayList<WindowClause.Window>();
            for (WindowClause.Window w : this.currentWindows) {
                if (w.isFinished() || !this.windowClause.matchesEnd(w, previous, current, next, position, context)) continue;
                w.endItem = current;
                w.endPreviousItem = previous;
                w.endNextItem = next;
                w.endPosition = position;
                this.despatch(w, this.getOutputter(), context);
                if (!w.isDespatched()) continue;
                removals.add(w);
            }
            for (WindowClause.Window w : removals) {
                this.currentWindows.remove(w);
            }
        }
        if (this.windowClause.isIncludeUnclosedWindows()) {
            for (WindowClause.Window window : this.currentWindows) {
                window.endItem = current;
                window.endPreviousItem = previous;
                window.endNextItem = null;
                window.endPosition = position;
                this.despatch(window, this.getOutputter(), context);
            }
        }
    }

    private void despatch(WindowClause.Window w, Outputter output, XPathContext context) throws XPathException {
        this.windowClause.checkWindowContents(w);
        while (true) {
            int earliestStart = Integer.MAX_VALUE;
            WindowClause.Window earliestWindow = null;
            for (WindowClause.Window u : this.currentWindows) {
                if (u.startPosition >= earliestStart || u.isDespatched()) continue;
                earliestStart = u.startPosition;
                earliestWindow = u;
            }
            if (earliestWindow == null || !earliestWindow.isFinished()) {
                return;
            }
            WindowClause clause = this.windowClause;
            LocalVariableBinding binding = clause.getVariableBinding(0);
            context.setLocalVariable(binding.getLocalSlotNumber(), SequenceExtent.makeSequenceExtent(earliestWindow.contents));
            binding = clause.getVariableBinding(1);
            if (binding != null) {
                context.setLocalVariable(binding.getLocalSlotNumber(), WindowClause.makeValue(earliestWindow.startItem));
            }
            if ((binding = clause.getVariableBinding(2)) != null) {
                context.setLocalVariable(binding.getLocalSlotNumber(), new Int64Value(earliestWindow.startPosition));
            }
            if ((binding = clause.getVariableBinding(4)) != null) {
                context.setLocalVariable(binding.getLocalSlotNumber(), WindowClause.makeValue(earliestWindow.startNextItem));
            }
            if ((binding = clause.getVariableBinding(3)) != null) {
                context.setLocalVariable(binding.getLocalSlotNumber(), WindowClause.makeValue(earliestWindow.startPreviousItem));
            }
            if ((binding = clause.getVariableBinding(5)) != null) {
                context.setLocalVariable(binding.getLocalSlotNumber(), WindowClause.makeValue(earliestWindow.endItem));
            }
            if ((binding = clause.getVariableBinding(6)) != null) {
                context.setLocalVariable(binding.getLocalSlotNumber(), new Int64Value(earliestWindow.endPosition));
            }
            if ((binding = clause.getVariableBinding(8)) != null) {
                context.setLocalVariable(binding.getLocalSlotNumber(), WindowClause.makeValue(earliestWindow.endNextItem));
            }
            if ((binding = clause.getVariableBinding(7)) != null) {
                context.setLocalVariable(binding.getLocalSlotNumber(), WindowClause.makeValue(earliestWindow.endPreviousItem));
            }
            this.destination.processTuple(context);
            earliestWindow.isDespatched = true;
        }
    }

    @Override
    public void close() throws XPathException {
        this.currentWindows = null;
        this.destination.close();
    }
}


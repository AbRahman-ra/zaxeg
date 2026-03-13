/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.LocalVariableBinding;
import net.sf.saxon.expr.flwor.TuplePull;
import net.sf.saxon.expr.flwor.WindowClause;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.SequenceExtent;

public class WindowClausePull
extends TuplePull {
    private WindowClause windowClause;
    private TuplePull source;
    private SequenceIterator baseIterator;
    private boolean finished = false;
    private XPathContext context;
    private Item previous = null;
    private Item current = null;
    private Item next = null;
    private int position = -1;
    private List<WindowClause.Window> currentWindows = new ArrayList<WindowClause.Window>();

    WindowClausePull(TuplePull source, WindowClause windowClause, XPathContext context) {
        this.windowClause = windowClause;
        this.source = source;
        this.context = context;
    }

    @Override
    public boolean nextTuple(XPathContext context) throws XPathException {
        boolean deliver = false;
        boolean pending = this.lookForEarliest();
        if (pending) {
            return true;
        }
        if ((this.finished || this.baseIterator == null) && this.source.nextTuple(context)) {
            this.baseIterator = this.windowClause.getSequence().iterate(context);
            this.finished = false;
            this.previous = null;
            this.position = -1;
            this.current = null;
            this.next = null;
        }
        while (!this.finished) {
            boolean implicitEndCondition;
            boolean autoclose = this.windowClause.isTumblingWindow() && this.windowClause.getEndCondition() == null;
            deliver = false;
            Item oldPrevious = this.previous;
            this.previous = this.current;
            this.current = this.next;
            this.next = this.baseIterator.next();
            if (this.next == null) {
                this.finished = true;
            }
            ++this.position;
            if (this.position <= 0) continue;
            if ((this.windowClause.isSlidingWindow() || this.currentWindows.isEmpty() || autoclose) && this.windowClause.matchesStart(this.previous, this.current, this.next, this.position, context)) {
                if (autoclose && !this.currentWindows.isEmpty()) {
                    WindowClause.Window w = this.currentWindows.get(0);
                    w.endItem = this.previous;
                    w.endPreviousItem = oldPrevious;
                    w.endNextItem = this.current;
                    w.endPosition = this.position - 1;
                    deliver = this.despatch(w, context);
                    this.currentWindows.clear();
                }
                WindowClause.Window window = new WindowClause.Window();
                window.startPosition = this.position;
                window.startItem = this.current;
                window.startPreviousItem = this.previous;
                window.startNextItem = this.next;
                window.contents = new ArrayList<Item>();
                this.currentWindows.add(window);
            }
            for (WindowClause.Window active : this.currentWindows) {
                if (active.isFinished()) continue;
                active.contents.add(this.current);
            }
            boolean explicitEndCondition = this.windowClause.getEndCondition() != null;
            boolean bl = implicitEndCondition = this.finished && this.windowClause.isIncludeUnclosedWindows();
            if (explicitEndCondition || implicitEndCondition) {
                ArrayList<WindowClause.Window> removals = new ArrayList<WindowClause.Window>();
                for (WindowClause.Window w : this.currentWindows) {
                    if (w.isFinished() || !implicitEndCondition && !this.windowClause.matchesEnd(w, this.previous, this.current, this.next, this.position, context)) continue;
                    w.endItem = this.current;
                    w.endPreviousItem = this.previous;
                    w.endNextItem = this.next;
                    w.endPosition = this.position;
                    if (deliver) continue;
                    deliver = this.despatch(w, context);
                    if (!w.isDespatched()) continue;
                    removals.add(w);
                }
                for (WindowClause.Window w : removals) {
                    this.currentWindows.remove(w);
                }
            }
            if (!deliver) continue;
            return true;
        }
        return false;
    }

    private boolean despatch(WindowClause.Window w, XPathContext context) throws XPathException {
        this.windowClause.checkWindowContents(w);
        return this.lookForEarliest();
    }

    private boolean lookForEarliest() throws XPathException {
        int earliestStart = Integer.MAX_VALUE;
        WindowClause.Window earliestWindow = null;
        for (WindowClause.Window u : this.currentWindows) {
            if (u.startPosition >= earliestStart || u.isDespatched()) continue;
            earliestStart = u.startPosition;
            earliestWindow = u;
        }
        if (earliestWindow == null || !earliestWindow.isFinished()) {
            return false;
        }
        this.processWindow(earliestWindow, this.context);
        return true;
    }

    private void processWindow(WindowClause.Window w, XPathContext context) throws XPathException {
        WindowClause clause = this.windowClause;
        LocalVariableBinding binding = clause.getVariableBinding(0);
        context.setLocalVariable(binding.getLocalSlotNumber(), SequenceExtent.makeSequenceExtent(w.contents));
        binding = clause.getVariableBinding(1);
        if (binding != null) {
            context.setLocalVariable(binding.getLocalSlotNumber(), WindowClause.makeValue(w.startItem));
        }
        if ((binding = clause.getVariableBinding(2)) != null) {
            context.setLocalVariable(binding.getLocalSlotNumber(), new Int64Value(w.startPosition));
        }
        if ((binding = clause.getVariableBinding(4)) != null) {
            context.setLocalVariable(binding.getLocalSlotNumber(), WindowClause.makeValue(w.startNextItem));
        }
        if ((binding = clause.getVariableBinding(3)) != null) {
            context.setLocalVariable(binding.getLocalSlotNumber(), WindowClause.makeValue(w.startPreviousItem));
        }
        if ((binding = clause.getVariableBinding(5)) != null) {
            context.setLocalVariable(binding.getLocalSlotNumber(), WindowClause.makeValue(w.endItem));
        }
        if ((binding = clause.getVariableBinding(6)) != null) {
            context.setLocalVariable(binding.getLocalSlotNumber(), new Int64Value(w.endPosition));
        }
        if ((binding = clause.getVariableBinding(8)) != null) {
            context.setLocalVariable(binding.getLocalSlotNumber(), WindowClause.makeValue(w.endNextItem));
        }
        if ((binding = clause.getVariableBinding(7)) != null) {
            context.setLocalVariable(binding.getLocalSlotNumber(), WindowClause.makeValue(w.endPreviousItem));
        }
        w.isDespatched = true;
    }

    @Override
    public void close() {
        this.baseIterator.close();
        this.source.close();
    }
}


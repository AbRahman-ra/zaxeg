/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.om.StructuredQName;

public class SlotManager {
    public static SlotManager EMPTY = new SlotManager(0);
    private ArrayList<StructuredQName> variableMap = new ArrayList(10);
    private int numberOfVariables = 0;

    public SlotManager() {
        this.numberOfVariables = 0;
        this.variableMap = new ArrayList();
    }

    public SlotManager(int n) {
        this.numberOfVariables = n;
        this.variableMap = new ArrayList(n);
    }

    public int getNumberOfVariables() {
        return this.numberOfVariables;
    }

    public void setNumberOfVariables(int numberOfVariables) {
        this.numberOfVariables = numberOfVariables;
        this.variableMap.trimToSize();
    }

    public int allocateSlotNumber(StructuredQName qName) {
        this.variableMap.add(qName);
        return this.numberOfVariables++;
    }

    public List<StructuredQName> getVariableMap() {
        return this.variableMap;
    }

    public void showStackFrame(XPathContext context, Logger logger) {
    }
}


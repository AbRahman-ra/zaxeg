/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans.rules;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.ParameterSet;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingIncident;
import net.sf.saxon.trans.rules.BuiltInRuleSet;
import net.sf.saxon.tree.util.Navigator;

public class RuleSetWithWarnings
implements BuiltInRuleSet {
    private BuiltInRuleSet baseRuleSet;

    public RuleSetWithWarnings(BuiltInRuleSet baseRuleSet) {
        this.baseRuleSet = baseRuleSet;
    }

    public BuiltInRuleSet getBaseRuleSet() {
        return this.baseRuleSet;
    }

    @Override
    public void process(Item item, ParameterSet parameters, ParameterSet tunnelParams, Outputter output, XPathContext context, Location locationId) throws XPathException {
        this.outputWarning(item, context);
        this.baseRuleSet.process(item, parameters, tunnelParams, output, context, locationId);
    }

    @Override
    public String getName() {
        return this.baseRuleSet + " with warnings";
    }

    public void outputWarning(Item item, XPathContext context) {
        String id = item instanceof NodeInfo ? "the node " + Navigator.getPath((NodeInfo)item) : "the atomic value " + item.getStringValue();
        XmlProcessingIncident warning = new XmlProcessingIncident("No user-defined template rule matches " + id, "XTDE0555").asWarning();
        context.getController().getErrorReporter().report(warning);
    }

    @Override
    public int[] getActionForParentNodes(int nodeKind) {
        return this.baseRuleSet.getActionForParentNodes(nodeKind);
    }
}


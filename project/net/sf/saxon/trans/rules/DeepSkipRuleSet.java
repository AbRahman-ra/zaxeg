/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans.rules;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.ParameterSet;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.BuiltInRuleSet;

public class DeepSkipRuleSet
implements BuiltInRuleSet {
    private static DeepSkipRuleSet THE_INSTANCE = new DeepSkipRuleSet();

    public static DeepSkipRuleSet getInstance() {
        return THE_INSTANCE;
    }

    private DeepSkipRuleSet() {
    }

    @Override
    public void process(Item item, ParameterSet parameters, ParameterSet tunnelParams, Outputter output, XPathContext context, Location locationId) throws XPathException {
        if (item instanceof NodeInfo && ((NodeInfo)item).getNodeKind() == 9) {
            XPathContextMajor c2 = context.newContext();
            c2.setOrigin(this);
            c2.trackFocus(((NodeInfo)item).iterateAxis(3));
            c2.setCurrentComponent(c2.getCurrentMode());
            for (TailCall tc = c2.getCurrentMode().getActor().applyTemplates(parameters, tunnelParams, null, output, c2, locationId); tc != null; tc = tc.processLeavingTail()) {
            }
        }
    }

    @Override
    public String getName() {
        return "deep-skip";
    }

    @Override
    public int[] getActionForParentNodes(int nodeKind) {
        if (nodeKind == 9) {
            return new int[]{7};
        }
        return new int[]{3};
    }
}


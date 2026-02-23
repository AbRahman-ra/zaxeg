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
import net.sf.saxon.trans.rules.BuiltInRuleSet;
import net.sf.saxon.tree.util.Navigator;

public class FailRuleSet
implements BuiltInRuleSet {
    private static FailRuleSet THE_INSTANCE = new FailRuleSet();

    public static FailRuleSet getInstance() {
        return THE_INSTANCE;
    }

    private FailRuleSet() {
    }

    @Override
    public void process(Item item, ParameterSet parameters, ParameterSet tunnelParams, Outputter output, XPathContext context, Location locationId) throws XPathException {
        String id = item instanceof NodeInfo ? "the node " + Navigator.getPath((NodeInfo)item) : "the atomic value " + item.getStringValue();
        XPathException err = new XPathException("No user-defined template rule matches " + id, "XTDE0555");
        err.setLocator(locationId.saveLocation());
        throw err;
    }

    @Override
    public String getName() {
        return "fail";
    }

    @Override
    public int[] getActionForParentNodes(int nodeKind) {
        return new int[]{4};
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans.rules;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.ParameterSet;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.ma.arrays.ArrayFunctionSet;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.BuiltInRuleSet;
import net.sf.saxon.value.AtomicValue;

public class TextOnlyCopyRuleSet
implements BuiltInRuleSet {
    private static TextOnlyCopyRuleSet THE_INSTANCE = new TextOnlyCopyRuleSet();

    public static TextOnlyCopyRuleSet getInstance() {
        return THE_INSTANCE;
    }

    private TextOnlyCopyRuleSet() {
    }

    @Override
    public void process(Item item, ParameterSet parameters, ParameterSet tunnelParams, Outputter output, XPathContext context, Location locationId) throws XPathException {
        if (item instanceof NodeInfo) {
            NodeInfo node = (NodeInfo)item;
            switch (node.getNodeKind()) {
                case 1: 
                case 9: {
                    XPathContextMajor c2 = context.newContext();
                    c2.setOrigin(this);
                    c2.trackFocus(node.iterateAxis(3));
                    c2.setCurrentComponent(c2.getCurrentMode());
                    for (TailCall tc = c2.getCurrentMode().getActor().applyTemplates(parameters, tunnelParams, null, output, c2, locationId); tc != null; tc = tc.processLeavingTail()) {
                    }
                    return;
                }
                case 2: 
                case 3: {
                    output.characters(item.getStringValueCS(), locationId, 0);
                    return;
                }
            }
        } else if (item instanceof ArrayItem) {
            Sequence seq = ArrayFunctionSet.ArrayToSequence.toSequence((ArrayItem)item);
            SequenceIterator members = seq.iterate();
            XPathContextMajor c2 = context.newContext();
            c2.setOrigin(this);
            c2.trackFocus(members);
            c2.setCurrentComponent(c2.getCurrentMode());
            for (TailCall tc = c2.getCurrentMode().getActor().applyTemplates(parameters, tunnelParams, null, output, c2, locationId); tc != null; tc = tc.processLeavingTail()) {
            }
        } else if (item instanceof AtomicValue) {
            output.characters(item.getStringValueCS(), locationId, 0);
        }
    }

    @Override
    public String getName() {
        return "text-only";
    }

    @Override
    public int[] getActionForParentNodes(int nodeKind) {
        return new int[]{7};
    }
}


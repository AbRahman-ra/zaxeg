/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans.rules;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.ParameterSet;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.BuiltInRuleSet;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.SimpleType;

public class DeepCopyRuleSet
implements BuiltInRuleSet {
    private static DeepCopyRuleSet THE_INSTANCE = new DeepCopyRuleSet();

    public static DeepCopyRuleSet getInstance() {
        return THE_INSTANCE;
    }

    private DeepCopyRuleSet() {
    }

    @Override
    public void process(Item item, ParameterSet parameters, ParameterSet tunnelParams, Outputter out, XPathContext context, Location locationId) throws XPathException {
        if (item instanceof NodeInfo) {
            NodeInfo node = (NodeInfo)item;
            switch (node.getNodeKind()) {
                case 1: 
                case 9: {
                    if (out.getSystemId() == null) {
                        out.setSystemId(node.getBaseURI());
                    }
                    Navigator.copy(node, out, 6, locationId);
                    return;
                }
                case 3: {
                    out.characters(item.getStringValueCS(), locationId, 0);
                    return;
                }
                case 8: {
                    out.comment(node.getStringValueCS(), locationId, 0);
                    return;
                }
                case 7: {
                    out.processingInstruction(node.getLocalPart(), node.getStringValue(), locationId, 0);
                    return;
                }
                case 2: {
                    out.attribute(NameOfNode.makeName(node), (SimpleType)node.getSchemaType(), node.getStringValue(), locationId, 0);
                    return;
                }
                case 13: {
                    out.namespace(node.getLocalPart(), node.getStringValue(), 0);
                    return;
                }
            }
        } else {
            out.append(item, locationId, 0);
        }
    }

    @Override
    public int[] getActionForParentNodes(int nodeKind) {
        return new int[]{1};
    }

    @Override
    public String getName() {
        return "deep-copy";
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans.rules;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.ParameterSet;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.BuiltInRuleSet;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.type.SimpleType;

public class ShallowCopyRuleSet
implements BuiltInRuleSet {
    private static ShallowCopyRuleSet THE_INSTANCE = new ShallowCopyRuleSet();

    public static ShallowCopyRuleSet getInstance() {
        return THE_INSTANCE;
    }

    private ShallowCopyRuleSet() {
    }

    @Override
    public void process(Item item, ParameterSet parameters, ParameterSet tunnelParams, Outputter out, XPathContext context, Location locationId) throws XPathException {
        if (item instanceof NodeInfo) {
            NodeInfo node = (NodeInfo)item;
            switch (node.getNodeKind()) {
                case 9: {
                    PipelineConfiguration pipe = out.getPipelineConfiguration();
                    if (out.getSystemId() == null) {
                        out.setSystemId(node.getBaseURI());
                    }
                    out.startDocument(0);
                    XPathContextMajor c2 = context.newContext();
                    c2.setOrigin(this);
                    c2.trackFocus(node.iterateAxis(3));
                    c2.setCurrentComponent(c2.getCurrentMode());
                    pipe.setXPathContext(c2);
                    for (TailCall tc = context.getCurrentMode().getActor().applyTemplates(parameters, tunnelParams, null, out, c2, locationId); tc != null; tc = tc.processLeavingTail()) {
                    }
                    out.endDocument();
                    pipe.setXPathContext(context);
                    return;
                }
                case 1: {
                    TailCall tc;
                    PipelineConfiguration pipe = out.getPipelineConfiguration();
                    if (out.getSystemId() == null) {
                        out.setSystemId(node.getBaseURI());
                    }
                    NodeName fqn = NameOfNode.makeName(node);
                    out.startElement(fqn, node.getSchemaType(), locationId, 0);
                    for (NamespaceBinding ns : node.getAllNamespaces()) {
                        out.namespace(ns.getPrefix(), ns.getURI(), 0);
                    }
                    XPathContextMajor c2 = context.newContext();
                    c2.setCurrentComponent(c2.getCurrentMode());
                    pipe.setXPathContext(c2);
                    AxisIterator attributes = node.iterateAxis(2);
                    if (attributes != EmptyIterator.ofNodes()) {
                        c2.setOrigin(this);
                        c2.trackFocus(attributes);
                        for (tc = c2.getCurrentMode().getActor().applyTemplates(parameters, tunnelParams, null, out, c2, locationId); tc != null; tc = tc.processLeavingTail()) {
                        }
                    }
                    if (node.hasChildNodes()) {
                        c2.trackFocus(node.iterateAxis(3));
                        for (tc = c2.getCurrentMode().getActor().applyTemplates(parameters, tunnelParams, null, out, c2, locationId); tc != null; tc = tc.processLeavingTail()) {
                        }
                    }
                    out.endElement();
                    pipe.setXPathContext(context);
                    return;
                }
                case 3: {
                    out.characters(node.getStringValueCS(), locationId, 0);
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
    public String getName() {
        return "shallow-copy";
    }

    @Override
    public int[] getActionForParentNodes(int nodeKind) {
        return new int[]{5, 6, 7};
    }
}


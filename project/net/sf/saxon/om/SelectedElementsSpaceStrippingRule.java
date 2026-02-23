/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.Stripper;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.pattern.LocalNameTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NamespaceTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.NodeTestPattern;
import net.sf.saxon.style.StylesheetModule;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.Rule;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AlphaCode;
import net.sf.saxon.type.SchemaType;

public class SelectedElementsSpaceStrippingRule
implements SpaceStrippingRule {
    private Rule anyElementRule = null;
    private Rule unnamedElementRuleChain = null;
    private HashMap<NodeName, Rule> namedElementRules = new HashMap(32);
    private int sequence = 0;
    private boolean rejectDuplicates;

    public SelectedElementsSpaceStrippingRule(boolean rejectDuplicates) {
        this.rejectDuplicates = rejectDuplicates;
    }

    @Override
    public int isSpacePreserving(NodeName fingerprint, SchemaType schemaType) throws XPathException {
        Rule rule = this.getRule(fingerprint);
        if (rule == null) {
            return 1;
        }
        return rule.getAction() == Stripper.PRESERVE ? 1 : 0;
    }

    public void addRule(NodeTest test, Stripper.StripRuleTarget action, StylesheetModule module, int lineNumber) throws XPathException {
        int precedence = module.getPrecedence();
        int minImportPrecedence = module.getMinImportPrecedence();
        NodeTestPattern pattern = new NodeTestPattern(test);
        this.addRule(pattern, action, precedence, minImportPrecedence);
    }

    public void addRule(NodeTestPattern pattern, Stripper.StripRuleTarget action, int precedence, int minImportPrecedence) throws XPathException {
        NodeTest test = pattern.getNodeTest();
        double priority = test.getDefaultPriority();
        Rule newRule = new Rule(pattern, action, precedence, minImportPrecedence, priority, this.sequence++, 0);
        int prio = priority == 0.0 ? 2 : (priority == -0.25 ? 1 : 0);
        newRule.setRank((precedence << 18) + (prio << 16) + this.sequence);
        if (test instanceof NodeKindTest) {
            newRule.setAlwaysMatches(true);
            this.anyElementRule = this.addRuleToList(newRule, this.anyElementRule, true);
        } else if (test instanceof NameTest) {
            newRule.setAlwaysMatches(true);
            int fp = test.getFingerprint();
            NamePool pool = ((NameTest)test).getNamePool();
            FingerprintedQName key = new FingerprintedQName(pool.getUnprefixedQName(fp), pool);
            Rule chain = this.namedElementRules.get(key);
            this.namedElementRules.put(key, this.addRuleToList(newRule, chain, true));
        } else {
            this.unnamedElementRuleChain = this.addRuleToList(newRule, this.unnamedElementRuleChain, false);
        }
    }

    private Rule addRuleToList(Rule newRule, Rule list, boolean dropRemainder) throws XPathException {
        Rule rule;
        if (list == null) {
            return newRule;
        }
        int precedence = newRule.getPrecedence();
        Rule prev = null;
        for (rule = list; rule != null; rule = rule.getNext()) {
            if (rule.getPrecedence() <= precedence) {
                if (this.rejectDuplicates && rule.getPrecedence() == precedence && !rule.getAction().equals(newRule.getAction())) {
                    throw new XPathException("There are conflicting xsl:strip-space and xsl:preserve-space declarations for " + rule.getPattern() + " at the same import precedence", "XTSE0270");
                }
                newRule.setNext(dropRemainder ? null : rule);
                if (prev == null) {
                    return newRule;
                }
                prev.setNext(newRule);
                break;
            }
            prev = rule;
        }
        if (rule == null) {
            prev.setNext(newRule);
            newRule.setNext(null);
        }
        return list;
    }

    public Rule getRule(NodeName nodeName) {
        Rule bestRule = this.namedElementRules.get(nodeName);
        if (this.unnamedElementRuleChain != null) {
            bestRule = this.searchRuleChain(nodeName, bestRule, this.unnamedElementRuleChain);
        }
        if (this.anyElementRule != null) {
            bestRule = this.searchRuleChain(nodeName, bestRule, this.anyElementRule);
        }
        return bestRule;
    }

    private Rule searchRuleChain(NodeName nodeName, Rule bestRule, Rule head) {
        while (head != null) {
            block5: {
                block3: {
                    block4: {
                        if (bestRule == null) break block3;
                        int rank = head.compareRank(bestRule);
                        if (rank < 0) break;
                        if (rank != 0) break block4;
                        if (head.isAlwaysMatches() || ((NodeTest)head.getPattern().getItemType()).matches(1, nodeName, null)) {
                            bestRule = head;
                            break;
                        }
                        break block5;
                    }
                    if (!head.isAlwaysMatches() && !((NodeTest)head.getPattern().getItemType()).matches(1, nodeName, null)) break block5;
                    bestRule = head;
                    break block5;
                }
                if (head.isAlwaysMatches() || ((NodeTest)head.getPattern().getItemType()).matches(1, nodeName, null)) {
                    bestRule = head;
                    break;
                }
            }
            head = head.getNext();
        }
        return bestRule;
    }

    public Iterator<Rule> getRankedRules() {
        Rule rule;
        TreeMap<Integer, Rule> treeMap = new TreeMap<Integer, Rule>();
        for (rule = this.anyElementRule; rule != null; rule = rule.getNext()) {
            treeMap.put(-rule.getRank(), rule);
        }
        for (rule = this.unnamedElementRuleChain; rule != null; rule = rule.getNext()) {
            treeMap.put(-rule.getRank(), rule);
        }
        for (Rule r : this.namedElementRules.values()) {
            treeMap.put(-r.getRank(), r);
        }
        return treeMap.values().iterator();
    }

    @Override
    public ProxyReceiver makeStripper(Receiver next) {
        return new Stripper(this, next);
    }

    @Override
    public void export(ExpressionPresenter presenter) throws XPathException {
        Rule rule;
        presenter.startElement("strip");
        for (rule = this.anyElementRule; rule != null; rule = rule.getNext()) {
            SelectedElementsSpaceStrippingRule.exportRule(rule, presenter);
        }
        for (rule = this.unnamedElementRuleChain; rule != null; rule = rule.getNext()) {
            SelectedElementsSpaceStrippingRule.exportRule(rule, presenter);
        }
        for (Rule r : this.namedElementRules.values()) {
            SelectedElementsSpaceStrippingRule.exportRule(r, presenter);
        }
        presenter.endElement();
    }

    private static void exportRule(Rule rule, ExpressionPresenter presenter) {
        String which = rule.getAction() == Stripper.STRIP ? "s" : "p";
        presenter.startElement(which);
        presenter.emitAttribute("test", AlphaCode.fromItemType(rule.getPattern().getItemType()));
        presenter.emitAttribute("prec", rule.getPrecedence() + "");
        presenter.endElement();
    }

    private static void exportRuleJS(Rule rule, FastStringBuffer fsb) {
        String which = rule.getAction() == Stripper.STRIP ? "true" : "false";
        NodeTest test = (NodeTest)rule.getPattern().getItemType();
        if (test instanceof NodeKindTest) {
            fsb.append("return " + which + ";");
        } else if (test instanceof NameTest) {
            fsb.append("if (uri=='" + test.getMatchingNodeName().getURI() + "' && local=='" + test.getMatchingNodeName().getLocalPart() + "') return " + which + ";");
        } else if (test instanceof NamespaceTest) {
            fsb.append("if (uri=='" + ((NamespaceTest)test).getNamespaceURI() + "') return " + which + ";");
        } else if (test instanceof LocalNameTest) {
            fsb.append("if (local=='" + ((LocalNameTest)test).getLocalName() + "') return " + which + ";");
        } else {
            throw new IllegalStateException("Cannot export " + test.getClass());
        }
    }
}


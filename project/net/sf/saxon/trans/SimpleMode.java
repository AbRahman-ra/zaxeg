/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.saxon.expr.ComponentBinding;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.instruct.TemplateRule;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTestPattern;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.style.StylesheetModule;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.trans.RecoveryPolicy;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.BuiltInRuleSet;
import net.sf.saxon.trans.rules.DeepCopyRuleSet;
import net.sf.saxon.trans.rules.DeepSkipRuleSet;
import net.sf.saxon.trans.rules.FailRuleSet;
import net.sf.saxon.trans.rules.Rule;
import net.sf.saxon.trans.rules.RuleChain;
import net.sf.saxon.trans.rules.RuleSearchState;
import net.sf.saxon.trans.rules.RuleSetWithWarnings;
import net.sf.saxon.trans.rules.RuleTarget;
import net.sf.saxon.trans.rules.ShallowCopyRuleSet;
import net.sf.saxon.trans.rules.ShallowSkipRuleSet;
import net.sf.saxon.trans.rules.TextOnlyCopyRuleSet;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Whitespace;
import net.sf.saxon.z.IntHashMap;
import net.sf.saxon.z.IntIterator;

public class SimpleMode
extends Mode {
    protected final RuleChain genericRuleChain = new RuleChain();
    protected RuleChain atomicValueRuleChain = new RuleChain();
    protected RuleChain functionItemRuleChain = new RuleChain();
    protected RuleChain documentRuleChain = new RuleChain();
    protected RuleChain textRuleChain = new RuleChain();
    protected RuleChain commentRuleChain = new RuleChain();
    protected RuleChain processingInstructionRuleChain = new RuleChain();
    protected RuleChain namespaceRuleChain = new RuleChain();
    protected RuleChain unnamedElementRuleChain = new RuleChain();
    protected RuleChain unnamedAttributeRuleChain = new RuleChain();
    protected IntHashMap<RuleChain> namedElementRuleChains = new IntHashMap(32);
    protected IntHashMap<RuleChain> namedAttributeRuleChains = new IntHashMap(8);
    protected Map<StructuredQName, RuleChain> qNamedElementRuleChains;
    protected Map<StructuredQName, RuleChain> qNamedAttributeRuleChains;
    private BuiltInRuleSet builtInRuleSet = TextOnlyCopyRuleSet.getInstance();
    private Rule mostRecentRule;
    private int mostRecentModuleHash;
    private int stackFrameSlotsNeeded = 0;
    private int highestRank;
    private Map<String, Integer> explicitPropertyPrecedences = new HashMap<String, Integer>();
    private Map<String, String> explicitPropertyValues = new HashMap<String, String>();

    public SimpleMode(StructuredQName modeName) {
        super(modeName);
    }

    public void setBuiltInRuleSet(BuiltInRuleSet defaultRules) {
        this.builtInRuleSet = defaultRules;
        this.hasRules = true;
    }

    @Override
    public BuiltInRuleSet getBuiltInRuleSet() {
        return this.builtInRuleSet;
    }

    @Override
    public SimpleMode getActivePart() {
        return this;
    }

    public void checkForConflictingProperties() throws XPathException {
        for (Map.Entry<String, String> entry : this.getActivePart().explicitPropertyValues.entrySet()) {
            String prop = entry.getKey();
            String value = entry.getValue();
            if (value.equals("##conflict##")) {
                throw new XPathException("For " + this.getLabel() + ", there are conflicting values for xsl:mode/@" + prop + " at the same import precedence", "XTSE0545");
            }
            if (prop.equals("typed")) {
                this.mustBeTyped = "yes".equals(value) || "strict".equals(value) || "lax".equals(value);
                this.mustBeUntyped = "no".equals(value);
                continue;
            }
            if (!prop.equals("on-no-match")) continue;
            BuiltInRuleSet base = null;
            switch (value) {
                case "text-only-copy": {
                    base = TextOnlyCopyRuleSet.getInstance();
                    break;
                }
                case "shallow-copy": {
                    base = ShallowCopyRuleSet.getInstance();
                    break;
                }
                case "deep-copy": {
                    base = DeepCopyRuleSet.getInstance();
                    break;
                }
                case "shallow-skip": {
                    base = ShallowSkipRuleSet.getInstance();
                    break;
                }
                case "deep-skip": {
                    base = DeepSkipRuleSet.getInstance();
                    break;
                }
                case "fail": {
                    base = FailRuleSet.getInstance();
                    break;
                }
            }
            if ("yes".equals(this.explicitPropertyValues.get("warning-on-no-match"))) {
                base = new RuleSetWithWarnings(base);
            }
            this.setBuiltInRuleSet(base);
        }
    }

    public String getLabel() {
        return this.isUnnamedMode() ? "the unnamed mode" : "mode " + this.modeName.getDisplayName();
    }

    public static void copyRules(SimpleMode from, SimpleMode to) {
        try {
            from.processRules(r -> {
                Rule r2 = r.copy(false);
                to.addRule(r2.getPattern(), r2);
            });
        } catch (XPathException e) {
            throw new AssertionError((Object)e);
        }
        to.mostRecentRule = from.mostRecentRule;
        to.mostRecentModuleHash = from.mostRecentModuleHash;
    }

    protected RuleSearchState makeRuleSearchState(RuleChain chain, XPathContext context) {
        return new RuleSearchState();
    }

    @Override
    public boolean isEmpty() {
        return !this.hasRules;
    }

    public void setExplicitProperty(String name, String value, int precedence) {
        Integer p = this.explicitPropertyPrecedences.get(name);
        if (p != null) {
            String v;
            if (p < precedence) {
                this.explicitPropertyPrecedences.put(name, precedence);
                this.explicitPropertyValues.put(name, value);
            } else if (p == precedence && (v = this.explicitPropertyValues.get(name)) != null & !v.equals(value)) {
                this.explicitPropertyValues.put(name, "##conflict##");
            }
        } else {
            this.explicitPropertyPrecedences.put(name, precedence);
            this.explicitPropertyValues.put(name, value);
        }
        String typed = this.explicitPropertyValues.get("typed");
        this.mustBeTyped = "yes".equals(typed) || "strict".equals(typed) || "lax".equals(typed);
        this.mustBeUntyped = "no".equals(typed);
    }

    public String getPropertyValue(String name) {
        return this.explicitPropertyValues.get(name);
    }

    @Override
    public Set<String> getExplicitNamespaces(NamePool pool) {
        HashSet<String> namespaces = new HashSet<String>();
        IntIterator ii = this.namedElementRuleChains.keyIterator();
        while (ii.hasNext()) {
            int fp = ii.next();
            namespaces.add(pool.getURI(fp));
        }
        return namespaces;
    }

    public void addRule(Pattern pattern, RuleTarget action, StylesheetModule module, int precedence, double priority, int position, int part) {
        this.hasRules = true;
        if (pattern.getItemType() instanceof ErrorType) {
            return;
        }
        int moduleHash = module.hashCode();
        int minImportPrecedence = module.getMinImportPrecedence();
        Rule newRule = this.makeRule(pattern, action, precedence, minImportPrecedence, priority, position, part);
        if (pattern instanceof NodeTestPattern) {
            int kind;
            ItemType test = pattern.getItemType();
            if (test instanceof AnyNodeTest) {
                newRule.setAlwaysMatches(true);
            } else if (test instanceof NodeKindTest) {
                newRule.setAlwaysMatches(true);
            } else if (test instanceof NameTest && ((kind = test.getPrimitiveType()) == 1 || kind == 2)) {
                newRule.setAlwaysMatches(true);
            }
        }
        this.mostRecentRule = newRule;
        this.mostRecentModuleHash = moduleHash;
        this.addRule(pattern, newRule);
    }

    public Rule makeRule(Pattern pattern, RuleTarget action, int precedence, int minImportPrecedence, double priority, int sequence, int part) {
        return new Rule(pattern, action, precedence, minImportPrecedence, priority, sequence, part);
    }

    public void addRule(Pattern pattern, Rule newRule) {
        UType uType = pattern.getUType();
        if (uType.equals(UType.ELEMENT)) {
            int fp = pattern.getFingerprint();
            this.addRuleToNamedOrUnnamedChain(newRule, fp, this.unnamedElementRuleChain, this.namedElementRuleChains);
        } else if (uType.equals(UType.ATTRIBUTE)) {
            int fp = pattern.getFingerprint();
            this.addRuleToNamedOrUnnamedChain(newRule, fp, this.unnamedAttributeRuleChain, this.namedAttributeRuleChains);
        } else if (uType.equals(UType.DOCUMENT)) {
            this.addRuleToList(newRule, this.documentRuleChain);
        } else if (uType.equals(UType.TEXT)) {
            this.addRuleToList(newRule, this.textRuleChain);
        } else if (uType.equals(UType.COMMENT)) {
            this.addRuleToList(newRule, this.commentRuleChain);
        } else if (uType.equals(UType.PI)) {
            this.addRuleToList(newRule, this.processingInstructionRuleChain);
        } else if (uType.equals(UType.NAMESPACE)) {
            this.addRuleToList(newRule, this.namespaceRuleChain);
        } else if (UType.ANY_ATOMIC.subsumes(uType)) {
            this.addRuleToList(newRule, this.atomicValueRuleChain);
        } else if (UType.FUNCTION.subsumes(uType)) {
            this.addRuleToList(newRule, this.functionItemRuleChain);
        } else {
            this.addRuleToList(newRule, this.genericRuleChain);
        }
    }

    protected void addRuleToNamedOrUnnamedChain(Rule newRule, int fp, RuleChain unnamedRuleChain, IntHashMap<RuleChain> namedRuleChains) {
        if (fp == -1) {
            this.addRuleToList(newRule, unnamedRuleChain);
        } else {
            RuleChain chain = namedRuleChains.get(fp);
            if (chain == null) {
                chain = new RuleChain(newRule);
                namedRuleChains.put(fp, chain);
            } else {
                this.addRuleToList(newRule, chain);
            }
        }
    }

    private void addRuleToList(Rule newRule, RuleChain list) {
        if (list.head() == null) {
            list.setHead(newRule);
        } else {
            Rule rule;
            int precedence = newRule.getPrecedence();
            double priority = newRule.getPriority();
            Rule prev = null;
            for (rule = list.head(); rule != null; rule = rule.getNext()) {
                if (rule.getPrecedence() < precedence || rule.getPrecedence() == precedence && rule.getPriority() <= priority) {
                    newRule.setNext(rule);
                    if (prev == null) {
                        list.setHead(newRule);
                        break;
                    }
                    prev.setNext(newRule);
                    break;
                }
                prev = rule;
            }
            if (rule == null) {
                assert (prev != null);
                prev.setNext(newRule);
                newRule.setNext(null);
            }
        }
    }

    public void allocatePatternSlots(int slots) {
        this.stackFrameSlotsNeeded = Math.max(this.stackFrameSlotsNeeded, slots);
    }

    @Override
    public Rule getRule(Item item, XPathContext context) throws XPathException {
        if (this.stackFrameSlotsNeeded > 0) {
            context = this.makeNewContext(context);
        }
        Rule bestRule = null;
        if (item instanceof NodeInfo) {
            RuleChain unnamedNodeChain;
            NodeInfo node = (NodeInfo)item;
            switch (node.getNodeKind()) {
                case 9: {
                    unnamedNodeChain = this.documentRuleChain;
                    break;
                }
                case 1: {
                    unnamedNodeChain = this.unnamedElementRuleChain;
                    RuleChain namedNodeChain = node.hasFingerprint() ? this.namedElementRuleChains.get(node.getFingerprint()) : this.getNamedRuleChain(context, 1, node.getURI(), node.getLocalPart());
                    if (namedNodeChain == null) break;
                    bestRule = this.searchRuleChain(node, context, null, namedNodeChain);
                    break;
                }
                case 2: {
                    unnamedNodeChain = this.unnamedAttributeRuleChain;
                    RuleChain namedNodeChain = node.hasFingerprint() ? this.namedAttributeRuleChains.get(node.getFingerprint()) : this.getNamedRuleChain(context, 2, node.getURI(), node.getLocalPart());
                    if (namedNodeChain == null) break;
                    bestRule = this.searchRuleChain(node, context, null, namedNodeChain);
                    break;
                }
                case 3: {
                    unnamedNodeChain = this.textRuleChain;
                    break;
                }
                case 8: {
                    unnamedNodeChain = this.commentRuleChain;
                    break;
                }
                case 7: {
                    unnamedNodeChain = this.processingInstructionRuleChain;
                    break;
                }
                case 13: {
                    unnamedNodeChain = this.namespaceRuleChain;
                    break;
                }
                default: {
                    throw new AssertionError((Object)"Unknown node kind");
                }
            }
            if (unnamedNodeChain != null) {
                bestRule = this.searchRuleChain(node, context, bestRule, unnamedNodeChain);
            }
            bestRule = this.searchRuleChain(node, context, bestRule, this.genericRuleChain);
        } else if (item instanceof AtomicValue) {
            if (this.atomicValueRuleChain != null) {
                bestRule = this.searchRuleChain(item, context, bestRule, this.atomicValueRuleChain);
            }
            bestRule = this.searchRuleChain(item, context, bestRule, this.genericRuleChain);
        } else if (item instanceof Function) {
            if (this.functionItemRuleChain != null) {
                bestRule = this.searchRuleChain(item, context, bestRule, this.functionItemRuleChain);
            }
            bestRule = this.searchRuleChain(item, context, bestRule, this.genericRuleChain);
        }
        return bestRule;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected RuleChain getNamedRuleChain(XPathContext c, int kind, String uri, String local) {
        SimpleMode simpleMode = this;
        synchronized (simpleMode) {
            if (this.qNamedElementRuleChains == null) {
                this.qNamedElementRuleChains = new HashMap<StructuredQName, RuleChain>(this.namedElementRuleChains.size());
                this.qNamedAttributeRuleChains = new HashMap<StructuredQName, RuleChain>(this.namedAttributeRuleChains.size());
                NamePool pool = c.getNamePool();
                SimpleMode.indexByQName(pool, this.namedElementRuleChains, this.qNamedElementRuleChains);
                SimpleMode.indexByQName(pool, this.namedAttributeRuleChains, this.qNamedAttributeRuleChains);
            }
        }
        return (kind == 1 ? this.qNamedElementRuleChains : this.qNamedAttributeRuleChains).get(new StructuredQName("", uri, local));
    }

    private static void indexByQName(NamePool pool, IntHashMap<RuleChain> indexByFP, Map<StructuredQName, RuleChain> indexByQN) {
        IntIterator ii = indexByFP.keyIterator();
        while (ii.hasNext()) {
            int fp = ii.next();
            RuleChain eChain = indexByFP.get(fp);
            StructuredQName name = pool.getStructuredQName(fp);
            indexByQN.put(name, eChain);
        }
    }

    protected Rule searchRuleChain(Item item, XPathContext context, Rule bestRule, RuleChain chain) throws XPathException {
        Rule head;
        while (!(context instanceof XPathContextMajor)) {
            context = context.getCaller();
        }
        RuleSearchState ruleSearchState = this.makeRuleSearchState(chain, context);
        Rule rule = head = chain == null ? null : chain.head();
        while (head != null) {
            if (bestRule != null) {
                int rank = head.compareRank(bestRule);
                if (rank < 0) break;
                if (rank == 0) {
                    if (this.ruleMatches(head, item, (XPathContextMajor)context, ruleSearchState)) {
                        int seqComp;
                        if (head.getSequence() != bestRule.getSequence()) {
                            this.reportAmbiguity(item, bestRule, head, context);
                        }
                        if ((seqComp = Integer.compare(bestRule.getSequence(), head.getSequence())) > 0) {
                            return bestRule;
                        }
                        if (seqComp < 0) {
                            return head;
                        }
                        bestRule = bestRule.getPartNumber() > head.getPartNumber() ? bestRule : head;
                        break;
                    }
                } else if (this.ruleMatches(head, item, (XPathContextMajor)context, ruleSearchState)) {
                    bestRule = head;
                }
            } else if (this.ruleMatches(head, item, (XPathContextMajor)context, ruleSearchState)) {
                bestRule = head;
                if (this.getRecoveryPolicy() == RecoveryPolicy.RECOVER_SILENTLY) break;
            }
            head = head.getNext();
        }
        return bestRule;
    }

    protected boolean ruleMatches(Rule r, Item item, XPathContextMajor context, RuleSearchState pre) throws XPathException {
        return r.isAlwaysMatches() || r.matches(item, context);
    }

    @Override
    public Rule getRule(Item item, XPathContext context, Mode.RuleFilter filter) throws XPathException {
        if (this.stackFrameSlotsNeeded > 0) {
            context = this.makeNewContext(context);
        }
        Rule bestRule = null;
        if (item instanceof NodeInfo) {
            RuleSearchState ruleSearchState;
            RuleChain unnamedNodeChain;
            NodeInfo node = (NodeInfo)item;
            switch (node.getNodeKind()) {
                case 9: {
                    unnamedNodeChain = this.documentRuleChain;
                    break;
                }
                case 1: {
                    unnamedNodeChain = this.unnamedElementRuleChain;
                    RuleChain namedNodeChain = node.hasFingerprint() ? this.namedElementRuleChains.get(node.getFingerprint()) : this.getNamedRuleChain(context, 1, node.getURI(), node.getLocalPart());
                    if (namedNodeChain == null) break;
                    ruleSearchState = this.makeRuleSearchState(namedNodeChain, context);
                    bestRule = this.searchRuleChain(item, context, null, namedNodeChain, ruleSearchState, filter);
                    break;
                }
                case 2: {
                    unnamedNodeChain = this.unnamedAttributeRuleChain;
                    RuleChain namedNodeChain = node.hasFingerprint() ? this.namedAttributeRuleChains.get(node.getFingerprint()) : this.getNamedRuleChain(context, 2, node.getURI(), node.getLocalPart());
                    if (namedNodeChain == null) break;
                    ruleSearchState = this.makeRuleSearchState(namedNodeChain, context);
                    bestRule = this.searchRuleChain(item, context, null, namedNodeChain, ruleSearchState, filter);
                    break;
                }
                case 3: {
                    unnamedNodeChain = this.textRuleChain;
                    break;
                }
                case 8: {
                    unnamedNodeChain = this.commentRuleChain;
                    break;
                }
                case 7: {
                    unnamedNodeChain = this.processingInstructionRuleChain;
                    break;
                }
                case 13: {
                    unnamedNodeChain = this.namespaceRuleChain;
                    break;
                }
                default: {
                    throw new AssertionError((Object)"Unknown node kind");
                }
            }
            ruleSearchState = this.makeRuleSearchState(unnamedNodeChain, context);
            bestRule = this.searchRuleChain(item, context, bestRule, unnamedNodeChain, ruleSearchState, filter);
            ruleSearchState = this.makeRuleSearchState(this.genericRuleChain, context);
            return this.searchRuleChain(item, context, bestRule, this.genericRuleChain, ruleSearchState, filter);
        }
        if (item instanceof AtomicValue) {
            RuleSearchState ruleSearchState;
            if (this.atomicValueRuleChain != null) {
                ruleSearchState = this.makeRuleSearchState(this.atomicValueRuleChain, context);
                bestRule = this.searchRuleChain(item, context, bestRule, this.atomicValueRuleChain, ruleSearchState, filter);
            }
            ruleSearchState = this.makeRuleSearchState(this.genericRuleChain, context);
            bestRule = this.searchRuleChain(item, context, bestRule, this.genericRuleChain, ruleSearchState, filter);
            return bestRule;
        }
        if (item instanceof Function) {
            RuleSearchState ruleSearchState;
            if (this.functionItemRuleChain != null) {
                ruleSearchState = this.makeRuleSearchState(this.functionItemRuleChain, context);
                bestRule = this.searchRuleChain(item, context, bestRule, this.functionItemRuleChain, ruleSearchState, filter);
            }
            ruleSearchState = this.makeRuleSearchState(this.genericRuleChain, context);
            bestRule = this.searchRuleChain(item, context, bestRule, this.genericRuleChain, ruleSearchState, filter);
            return bestRule;
        }
        return null;
    }

    protected Rule searchRuleChain(Item item, XPathContext context, Rule bestRule, RuleChain chain, RuleSearchState ruleSearchState, Mode.RuleFilter filter) throws XPathException {
        Rule head;
        Rule rule = head = chain == null ? null : chain.head();
        while (!(context instanceof XPathContextMajor)) {
            context = context.getCaller();
        }
        while (head != null) {
            if (filter == null || filter.testRule(head)) {
                if (bestRule != null) {
                    int rank = head.compareRank(bestRule);
                    if (rank < 0) break;
                    if (rank == 0) {
                        if (this.ruleMatches(head, item, (XPathContextMajor)context, ruleSearchState)) {
                            this.reportAmbiguity(item, bestRule, head, context);
                            bestRule = bestRule.getSequence() > head.getSequence() ? bestRule : head;
                            break;
                        }
                    } else if (this.ruleMatches(head, item, (XPathContextMajor)context, ruleSearchState)) {
                        bestRule = head;
                    }
                } else if (this.ruleMatches(head, item, (XPathContextMajor)context, ruleSearchState)) {
                    bestRule = head;
                    if (this.getRecoveryPolicy() == RecoveryPolicy.RECOVER_SILENTLY) break;
                }
            }
            head = head.getNext();
        }
        return bestRule;
    }

    protected void reportAmbiguity(Item item, Rule r1, Rule r2, XPathContext c) throws XPathException {
        if (this.getRecoveryPolicy() == RecoveryPolicy.RECOVER_SILENTLY) {
            return;
        }
        if (r1.getAction() == r2.getAction() && r1.getSequence() == r2.getSequence()) {
            return;
        }
        String errorCode = "XTDE0540";
        String path = item instanceof NodeInfo ? Navigator.getPath((NodeInfo)item) : item.toShortString();
        Pattern pat1 = r1.getPattern();
        Pattern pat2 = r2.getPattern();
        String message = r1.getAction() == r2.getAction() ? "Ambiguous rule match for " + path + ". Matches \"" + SimpleMode.showPattern(pat1) + "\" on line " + pat1.getLocation().getLineNumber() + " of " + pat1.getLocation().getSystemId() + ", a rule which appears in the stylesheet more than once, because the containing module was included more than once" : "Ambiguous rule match for " + path + '\n' + "Matches both \"" + SimpleMode.showPattern(pat1) + "\" on line " + pat1.getLocation().getLineNumber() + " of " + pat1.getLocation().getSystemId() + "\nand \"" + SimpleMode.showPattern(pat2) + "\" on line " + pat2.getLocation().getLineNumber() + " of " + pat2.getLocation().getSystemId();
        switch (this.getRecoveryPolicy()) {
            case DO_NOT_RECOVER: {
                throw new XPathException(message, errorCode, this.getLocation());
            }
            case RECOVER_WITH_WARNINGS: {
                c.getController().warning(message, errorCode, this.getLocation());
                break;
            }
        }
    }

    private static String showPattern(Pattern p) {
        return Whitespace.collapseWhitespace(p.toShortString()).toString();
    }

    public void prepareStreamability() throws XPathException {
    }

    @Override
    public void allocateAllBindingSlots(StylesheetPackage pack) {
        if (this.getDeclaringComponent().getDeclaringPackage() == pack && !this.bindingSlotsAllocated) {
            SimpleMode.forceAllocateAllBindingSlots(pack, this, this.getDeclaringComponent().getComponentBindings());
            this.bindingSlotsAllocated = true;
        }
    }

    public static void forceAllocateAllBindingSlots(StylesheetPackage pack, SimpleMode mode, List<ComponentBinding> bindings) {
        HashSet rulesProcessed = new HashSet();
        IdentityHashMap patternsProcessed = new IdentityHashMap();
        try {
            mode.processRules(r -> {
                TemplateRule tr;
                Pattern pattern = r.getPattern();
                if (!patternsProcessed.containsKey(pattern)) {
                    SimpleMode.allocateBindingSlotsRecursive(pack, mode, pattern, bindings);
                    patternsProcessed.put(pattern, true);
                }
                if ((tr = (TemplateRule)r.getAction()).getBody() != null && !rulesProcessed.contains(tr)) {
                    SimpleMode.allocateBindingSlotsRecursive(pack, mode, tr.getBody(), bindings);
                    rulesProcessed.add(tr);
                }
            });
        } catch (XPathException e) {
            throw new AssertionError((Object)e);
        }
    }

    public void computeStreamability() throws XPathException {
    }

    public void invertStreamableTemplates() throws XPathException {
    }

    @Override
    public void explainTemplateRules(final ExpressionPresenter out) throws XPathException {
        Mode.RuleAction action = r -> r.export(out, this.isDeclaredStreamable());
        RuleGroupAction group = new RuleGroupAction(){
            String type;

            @Override
            public void start() {
                out.startElement("ruleSet");
                out.emitAttribute("type", this.type);
            }

            @Override
            public void setString(String type) {
                this.type = type;
            }

            @Override
            public void start(int i) {
                out.startElement("ruleChain");
                out.emitAttribute("key", out.getNamePool().getClarkName(i));
            }

            @Override
            public void end() {
                out.endElement();
            }
        };
        try {
            this.processRules(action, group);
        } catch (XPathException xPathException) {
            // empty catch block
        }
    }

    @Override
    public void exportTemplateRules(ExpressionPresenter out) throws XPathException {
        Mode.RuleAction action = r -> r.export(out, this.isDeclaredStreamable());
        this.processRules(action);
    }

    @Override
    public void processRules(Mode.RuleAction action) throws XPathException {
        this.processRules(action, null);
    }

    public void processRules(Mode.RuleAction action, RuleGroupAction group) throws XPathException {
        this.processRuleChain(this.documentRuleChain, action, this.setGroup(group, "document-node()"));
        this.processRuleChain(this.unnamedElementRuleChain, action, this.setGroup(group, "element()"));
        this.processRuleChains(this.namedElementRuleChains, action, this.setGroup(group, "namedElements"));
        this.processRuleChain(this.unnamedAttributeRuleChain, action, this.setGroup(group, "attribute()"));
        this.processRuleChains(this.namedAttributeRuleChains, action, this.setGroup(group, "namedAttributes"));
        this.processRuleChain(this.textRuleChain, action, this.setGroup(group, "text()"));
        this.processRuleChain(this.commentRuleChain, action, this.setGroup(group, "comment()"));
        this.processRuleChain(this.processingInstructionRuleChain, action, this.setGroup(group, "processing-instruction()"));
        this.processRuleChain(this.namespaceRuleChain, action, this.setGroup(group, "namespace()"));
        this.processRuleChain(this.genericRuleChain, action, this.setGroup(group, "node()"));
        this.processRuleChain(this.atomicValueRuleChain, action, this.setGroup(group, "atomicValue"));
        this.processRuleChain(this.functionItemRuleChain, action, this.setGroup(group, "function()"));
    }

    protected RuleGroupAction setGroup(RuleGroupAction group, String type) {
        if (group != null) {
            group.setString(type);
        }
        return group;
    }

    public void processRuleChains(IntHashMap<RuleChain> chains, Mode.RuleAction action, RuleGroupAction group) throws XPathException {
        if (chains.size() > 0) {
            if (group != null) {
                group.start();
            }
            IntIterator ii = chains.keyIterator();
            while (ii.hasNext()) {
                int i = ii.next();
                if (group != null) {
                    group.start(i);
                }
                RuleChain r = chains.get(i);
                this.processRuleChain(r, action, null);
                if (group == null) continue;
                group.end();
            }
            if (group != null) {
                group.end();
            }
        }
    }

    public void processRuleChain(RuleChain chain, Mode.RuleAction action) throws XPathException {
        Rule r;
        Rule rule = r = chain == null ? null : chain.head();
        while (r != null) {
            action.processRule(r);
            r = r.getNext();
        }
    }

    public void processRuleChain(RuleChain chain, Mode.RuleAction action, RuleGroupAction group) throws XPathException {
        Rule r;
        Rule rule = r = chain == null ? null : chain.head();
        if (r != null) {
            if (group != null) {
                group.start();
            }
            while (r != null) {
                action.processRule(r);
                r = r.getNext();
            }
            if (group != null) {
                group.end();
            }
        }
    }

    public void optimizeRules() {
    }

    @Override
    public int getMaxPrecedence() {
        try {
            MaxPrecedenceAction action = new MaxPrecedenceAction();
            this.processRules(action);
            return action.max;
        } catch (XPathException e) {
            throw new AssertionError((Object)e);
        }
    }

    @Override
    public void computeRankings(int start) throws XPathException {
        RuleSorter sorter = new RuleSorter(start);
        this.processRules(sorter::addRule);
        sorter.allocateRanks();
        this.highestRank = start + sorter.getNumberOfRules();
    }

    @Override
    public int getMaxRank() {
        return this.highestRank;
    }

    public void allocateAllPatternSlots() {
        ArrayList<Integer> count = new ArrayList<Integer>(1);
        count.add(0);
        SlotManager slotManager = new SlotManager();
        Mode.RuleAction slotAllocator = r -> {
            int slots = r.getPattern().allocateSlots(slotManager, 0);
            int max = Math.max((Integer)count.get(0), slots);
            count.set(0, max);
        };
        try {
            this.processRules(slotAllocator);
        } catch (XPathException e) {
            throw new AssertionError((Object)e);
        }
        this.stackFrameSlotsNeeded = (Integer)count.get(0);
    }

    @Override
    public int getStackFrameSlotsNeeded() {
        return this.stackFrameSlotsNeeded;
    }

    public void setStackFrameSlotsNeeded(int slots) {
        this.stackFrameSlotsNeeded = slots;
    }

    public static interface RuleGroupAction {
        public void setString(String var1);

        public void start();

        public void start(int var1);

        public void end();
    }

    private static class RuleSorter {
        public ArrayList<Rule> rules = new ArrayList(100);
        private int start;

        public RuleSorter(int start) {
            this.start = start;
        }

        public void addRule(Rule rule) {
            this.rules.add(rule);
        }

        public void allocateRanks() {
            this.rules.sort(Rule::compareComputedRank);
            int rank = this.start;
            for (int i = 0; i < this.rules.size(); ++i) {
                if (i > 0 && this.rules.get(i - 1).compareComputedRank(this.rules.get(i)) != 0) {
                    ++rank;
                }
                this.rules.get(i).setRank(rank);
            }
        }

        public int getNumberOfRules() {
            return this.rules.size();
        }
    }

    private static class MaxPrecedenceAction
    implements Mode.RuleAction {
        public int max = 0;

        private MaxPrecedenceAction() {
        }

        @Override
        public void processRule(Rule r) {
            if (r.getPrecedence() > this.max) {
                this.max = r.getPrecedence();
            }
        }
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import java.util.Collections;
import java.util.Set;
import net.sf.saxon.Controller;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.accum.Accumulator;
import net.sf.saxon.expr.instruct.Actor;
import net.sf.saxon.expr.instruct.ParameterSet;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.instruct.TemplateRule;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.om.FocusIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trace.ModeTraceListener;
import net.sf.saxon.trans.RecoveryPolicy;
import net.sf.saxon.trans.SimpleMode;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.BuiltInRuleSet;
import net.sf.saxon.trans.rules.DeepCopyRuleSet;
import net.sf.saxon.trans.rules.DeepSkipRuleSet;
import net.sf.saxon.trans.rules.FailRuleSet;
import net.sf.saxon.trans.rules.Rule;
import net.sf.saxon.trans.rules.RuleSetWithWarnings;
import net.sf.saxon.trans.rules.ShallowCopyRuleSet;
import net.sf.saxon.trans.rules.ShallowSkipRuleSet;
import net.sf.saxon.trans.rules.TextOnlyCopyRuleSet;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.SequenceType;

public abstract class Mode
extends Actor {
    public static final StructuredQName OMNI_MODE = new StructuredQName("saxon", "http://saxon.sf.net/", "_omniMode");
    public static final StructuredQName UNNAMED_MODE_NAME = new StructuredQName("xsl", "http://www.w3.org/1999/XSL/Transform", "unnamed");
    public static final StructuredQName DEFAULT_MODE_NAME = new StructuredQName("xsl", "http://www.w3.org/1999/XSL/Transform", "default");
    protected StructuredQName modeName;
    private boolean streamable;
    public static final int RECOVER_WITH_WARNINGS = 1;
    private RecoveryPolicy recoveryPolicy = RecoveryPolicy.RECOVER_WITH_WARNINGS;
    public boolean mustBeTyped = false;
    public boolean mustBeUntyped = false;
    boolean hasRules = false;
    boolean bindingSlotsAllocated = false;
    boolean modeTracing = false;
    SequenceType defaultResultType = null;
    private Set<? extends Accumulator> accumulators;

    public Mode(StructuredQName modeName) {
        this.modeName = modeName;
    }

    @Override
    public Component.M getDeclaringComponent() {
        return (Component.M)super.getDeclaringComponent();
    }

    public abstract BuiltInRuleSet getBuiltInRuleSet();

    public boolean isUnnamedMode() {
        return this.modeName.equals(UNNAMED_MODE_NAME);
    }

    public StructuredQName getModeName() {
        return this.modeName;
    }

    public abstract SimpleMode getActivePart();

    public abstract int getMaxPrecedence();

    public abstract int getMaxRank();

    public abstract void computeRankings(int var1) throws XPathException;

    public String getModeTitle() {
        return this.isUnnamedMode() ? "The unnamed mode" : "Mode " + this.getModeName().getDisplayName();
    }

    public void setModeTracing(boolean tracing) {
        this.modeTracing = tracing;
    }

    public boolean isModeTracing() {
        return this.modeTracing;
    }

    public Set<? extends Accumulator> getAccumulators() {
        return this.accumulators == null ? Collections.emptySet() : this.accumulators;
    }

    public void setAccumulators(Set<? extends Accumulator> accumulators) {
        this.accumulators = accumulators;
    }

    @Override
    public SymbolicName getSymbolicName() {
        return new SymbolicName(174, this.getModeName());
    }

    public StructuredQName getObjectName() {
        return this.getModeName();
    }

    public abstract boolean isEmpty();

    public void setRecoveryPolicy(RecoveryPolicy policy) {
        this.recoveryPolicy = policy;
    }

    public void setHasRules(boolean hasRules) {
        this.hasRules = hasRules;
    }

    public RecoveryPolicy getRecoveryPolicy() {
        return this.recoveryPolicy;
    }

    public void setStreamable(boolean streamable) {
        this.streamable = streamable;
    }

    public boolean isDeclaredStreamable() {
        return this.streamable;
    }

    public abstract Set<String> getExplicitNamespaces(NamePool var1);

    public void setDefaultResultType(SequenceType type) {
        this.defaultResultType = type;
    }

    public SequenceType getDefaultResultType() {
        return this.defaultResultType;
    }

    public abstract void processRules(RuleAction var1) throws XPathException;

    public XPathContext makeNewContext(XPathContext context) {
        XPathContextMajor c2 = context.newContext();
        c2.setOrigin(context.getController());
        c2.openStackFrame(this.getStackFrameSlotsNeeded());
        if (!(context.getCurrentComponent().getActor() instanceof Accumulator)) {
            c2.setCurrentComponent(context.getCurrentMode());
        }
        return c2;
    }

    public abstract Rule getRule(Item var1, XPathContext var2) throws XPathException;

    public abstract Rule getRule(Item var1, XPathContext var2, RuleFilter var3) throws XPathException;

    public Rule getRule(Item item, int min, int max, XPathContext context) throws XPathException {
        RuleFilter filter = r -> {
            int p = r.getPrecedence();
            return p >= min && p <= max;
        };
        return this.getRule(item, context, filter);
    }

    public Rule getNextMatchRule(Item item, Rule currentRule, XPathContext context) throws XPathException {
        RuleFilter filter = r -> {
            int comp = r.compareRank(currentRule);
            if (comp < 0) {
                return true;
            }
            if (comp == 0) {
                int seqComp = Integer.compare(r.getSequence(), currentRule.getSequence());
                if (seqComp < 0) {
                    return true;
                }
                if (seqComp == 0) {
                    return r.getPartNumber() < currentRule.getPartNumber();
                }
            }
            return false;
        };
        return this.getRule(item, context, filter);
    }

    public abstract void exportTemplateRules(ExpressionPresenter var1) throws XPathException;

    public abstract void explainTemplateRules(ExpressionPresenter var1) throws XPathException;

    public TailCall applyTemplates(ParameterSet parameters, ParameterSet tunnelParameters, NodeInfo separator, Outputter output, XPathContextMajor context, Location locationId) throws XPathException {
        Controller controller = context.getController();
        boolean tracing = this.modeTracing || controller.isTracing();
        FocusIterator iterator = context.getCurrentIterator();
        TailCall tc = null;
        TraceListener traceListener = null;
        if (tracing && (traceListener = controller.getTraceListener()) == null) {
            traceListener = new ModeTraceListener();
            controller.setTraceListener(traceListener);
            traceListener.open(controller);
        }
        boolean lookahead = iterator.getProperties().contains((Object)SequenceIterator.Property.LOOKAHEAD);
        TemplateRule previousTemplate = null;
        boolean first = true;
        while (true) {
            int kind;
            SchemaType annotation;
            Item item;
            if (tc != null) {
                if (lookahead && !((LookaheadIterator)((Object)iterator)).hasNext()) break;
                while ((tc = tc.processLeavingTail()) != null) {
                }
            }
            if ((item = iterator.next()) == null) break;
            if (separator != null) {
                if (first) {
                    first = false;
                } else {
                    output.append(separator);
                }
            }
            if (this.mustBeTyped) {
                int kind2;
                if (!(!(item instanceof NodeInfo) || (kind2 = ((NodeInfo)item).getNodeKind()) != 1 && kind2 != 2 || (annotation = ((NodeInfo)item).getSchemaType()) != Untyped.getInstance() && annotation != BuiltInAtomicType.UNTYPED_ATOMIC)) {
                    throw new XPathException(this.getModeTitle() + " requires typed nodes, but the input is untyped", "XTTE3100");
                }
            } else if (this.mustBeUntyped && item instanceof NodeInfo && ((kind = ((NodeInfo)item).getNodeKind()) == 1 || kind == 2) && (annotation = ((NodeInfo)item).getSchemaType()) != Untyped.getInstance() && annotation != BuiltInAtomicType.UNTYPED_ATOMIC) {
                throw new XPathException(this.getModeTitle() + " requires untyped nodes, but the input is typed", "XTTE3110");
            }
            if (tracing) {
                traceListener.startRuleSearch();
            }
            Rule rule = this.getRule(item, context);
            if (tracing) {
                traceListener.endRuleSearch(rule != null ? rule : this.getBuiltInRuleSet(), this, item);
            }
            if (rule == null) {
                this.getBuiltInRuleSet().process(item, parameters, tunnelParameters, output, context, locationId);
                continue;
            }
            TemplateRule template = (TemplateRule)rule.getAction();
            if (template != previousTemplate) {
                previousTemplate = template;
                template.initialize();
                context.openStackFrame(template.getStackFrameMap());
                context.setLocalParameters(parameters);
                context.setTunnelParameters(tunnelParameters);
                context.setCurrentMergeGroupIterator(null);
            }
            context.setCurrentTemplateRule(rule);
            if (tracing) {
                traceListener.startCurrentItem(item);
                if (this.modeTracing) {
                    traceListener.enter(template, Collections.emptyMap(), context);
                }
                if ((tc = template.applyLeavingTail(output, context)) != null) {
                    while ((tc = tc.processLeavingTail()) != null) {
                    }
                }
                if (this.modeTracing) {
                    traceListener.leave(template);
                }
                traceListener.endCurrentItem(item);
                continue;
            }
            tc = template.applyLeavingTail(output, context);
        }
        return tc;
    }

    public abstract int getStackFrameSlotsNeeded();

    public String getCodeForBuiltInRuleSet(BuiltInRuleSet builtInRuleSet) {
        if (builtInRuleSet instanceof ShallowCopyRuleSet) {
            return "SC";
        }
        if (builtInRuleSet instanceof ShallowSkipRuleSet) {
            return "SS";
        }
        if (builtInRuleSet instanceof DeepCopyRuleSet) {
            return "DC";
        }
        if (builtInRuleSet instanceof DeepSkipRuleSet) {
            return "DS";
        }
        if (builtInRuleSet instanceof FailRuleSet) {
            return "FF";
        }
        if (builtInRuleSet instanceof TextOnlyCopyRuleSet) {
            return "TC";
        }
        if (builtInRuleSet instanceof RuleSetWithWarnings) {
            return this.getCodeForBuiltInRuleSet(((RuleSetWithWarnings)builtInRuleSet).getBaseRuleSet()) + "+W";
        }
        return "???";
    }

    public BuiltInRuleSet getBuiltInRuleSetForCode(String code) {
        BuiltInRuleSet base;
        if (code.startsWith("SC")) {
            base = ShallowCopyRuleSet.getInstance();
        } else if (code.startsWith("SS")) {
            base = ShallowSkipRuleSet.getInstance();
        } else if (code.startsWith("DC")) {
            base = DeepCopyRuleSet.getInstance();
        } else if (code.startsWith("DS")) {
            base = DeepSkipRuleSet.getInstance();
        } else if (code.startsWith("FF")) {
            base = FailRuleSet.getInstance();
        } else if (code.startsWith("TC")) {
            base = TextOnlyCopyRuleSet.getInstance();
        } else {
            throw new IllegalArgumentException(code);
        }
        if (code.endsWith("+W")) {
            base = new RuleSetWithWarnings(base);
        }
        return base;
    }

    @Override
    public final void export(ExpressionPresenter presenter) throws XPathException {
        int s = presenter.startElement("mode");
        if (!this.isUnnamedMode()) {
            presenter.emitAttribute("name", this.getModeName());
        }
        presenter.emitAttribute("onNo", this.getCodeForBuiltInRuleSet(this.getBuiltInRuleSet()));
        String flags = "";
        if (this.isDeclaredStreamable()) {
            flags = flags + "s";
        }
        if (this.isUnnamedMode()) {
            flags = flags + "d";
        }
        if (this.mustBeTyped) {
            flags = flags + "t";
        }
        if (this.mustBeUntyped) {
            flags = flags + "u";
        }
        if (this.recoveryPolicy == RecoveryPolicy.DO_NOT_RECOVER) {
            flags = flags + "F";
        } else if (this.recoveryPolicy == RecoveryPolicy.RECOVER_WITH_WARNINGS) {
            flags = flags + "W";
        }
        if (!this.hasRules) {
            flags = flags + "e";
        }
        if (!flags.isEmpty()) {
            presenter.emitAttribute("flags", flags);
        }
        this.exportUseAccumulators(presenter);
        presenter.emitAttribute("patternSlots", this.getStackFrameSlotsNeeded() + "");
        this.exportTemplateRules(presenter);
        int e = presenter.endElement();
        if (s != e) {
            throw new IllegalStateException("Export tree unbalanced for mode " + this.getModeName());
        }
    }

    protected void exportUseAccumulators(ExpressionPresenter presenter) {
    }

    public boolean isMustBeTyped() {
        return this.mustBeTyped;
    }

    public void explain(ExpressionPresenter presenter) throws XPathException {
        int s = presenter.startElement("mode");
        if (!this.isUnnamedMode()) {
            presenter.emitAttribute("name", this.getModeName());
        }
        presenter.emitAttribute("onNo", this.getCodeForBuiltInRuleSet(this.getBuiltInRuleSet()));
        String flags = "";
        if (this.isDeclaredStreamable()) {
            flags = flags + "s";
        }
        if (this.isUnnamedMode()) {
            flags = flags + "d";
        }
        if (this.mustBeTyped) {
            flags = flags + "t";
        }
        if (this.mustBeUntyped) {
            flags = flags + "u";
        }
        if (this.recoveryPolicy == RecoveryPolicy.DO_NOT_RECOVER) {
            flags = flags + "F";
        } else if (this.recoveryPolicy == RecoveryPolicy.RECOVER_WITH_WARNINGS) {
            flags = flags + "W";
        }
        if (!flags.isEmpty()) {
            presenter.emitAttribute("flags", flags);
        }
        presenter.emitAttribute("patternSlots", this.getStackFrameSlotsNeeded() + "");
        this.explainTemplateRules(presenter);
        int e = presenter.endElement();
        if (s != e) {
            throw new IllegalStateException("tree unbalanced");
        }
    }

    public static interface RuleAction {
        public void processRule(Rule var1) throws XPathException;
    }

    protected static interface RuleFilter {
        public boolean testRule(Rule var1);
    }
}


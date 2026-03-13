/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.ComponentBinding;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.trans.SimpleMode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.BuiltInRuleSet;
import net.sf.saxon.trans.rules.Rule;

public class CompoundMode
extends Mode {
    private Mode base;
    private SimpleMode overrides;
    private int overridingPrecedence;

    public CompoundMode(Mode base, SimpleMode overrides) {
        super(base.getModeName());
        if (!base.getModeName().equals(overrides.getModeName())) {
            throw new AssertionError((Object)"Base and overriding modes must have the same name");
        }
        if (base.getModeName().equals(Mode.UNNAMED_MODE_NAME)) {
            throw new AssertionError((Object)"Cannot override an unnamed mode");
        }
        if (base.getModeName().equals(Mode.OMNI_MODE)) {
            throw new AssertionError((Object)"Cannot override mode='#all'");
        }
        this.base = base;
        this.overrides = overrides;
        this.mustBeTyped = base.mustBeTyped;
        this.mustBeUntyped = base.mustBeUntyped;
        this.overridingPrecedence = base.getMaxPrecedence() + 1;
    }

    @Override
    public BuiltInRuleSet getBuiltInRuleSet() {
        return this.base.getBuiltInRuleSet();
    }

    @Override
    public SimpleMode getActivePart() {
        return this.overrides;
    }

    @Override
    public boolean isEmpty() {
        return this.base.isEmpty() && this.overrides.isEmpty();
    }

    @Override
    public int getMaxPrecedence() {
        return this.overridingPrecedence;
    }

    @Override
    public int getMaxRank() {
        return this.overrides.getMaxRank();
    }

    @Override
    public void computeRankings(int start) throws XPathException {
        this.overrides.computeRankings(this.base.getMaxRank() + 1);
    }

    @Override
    public void processRules(Mode.RuleAction action) throws XPathException {
        this.overrides.processRules(action);
        this.base.processRules(action);
    }

    @Override
    public Set<String> getExplicitNamespaces(NamePool pool) {
        HashSet<String> r = new HashSet<String>();
        r.addAll(this.base.getExplicitNamespaces(pool));
        r.addAll(this.overrides.getExplicitNamespaces(pool));
        return r;
    }

    @Override
    public void allocateAllBindingSlots(StylesheetPackage pack) {
        if (!this.bindingSlotsAllocated) {
            List<ComponentBinding> baseBindings = this.base.getDeclaringComponent().getComponentBindings();
            ArrayList<ComponentBinding> newBindings = new ArrayList<ComponentBinding>(baseBindings);
            Component.M comp = this.getDeclaringComponent();
            comp.setComponentBindings(newBindings);
            SimpleMode.forceAllocateAllBindingSlots(pack, this.overrides, newBindings);
            this.bindingSlotsAllocated = true;
        }
    }

    @Override
    public Rule getRule(Item item, XPathContext context) throws XPathException {
        Rule r = this.overrides.getRule(item, context);
        if (r == null) {
            r = this.base.getRule(item, context);
        }
        return r;
    }

    @Override
    public int getStackFrameSlotsNeeded() {
        return Math.max(this.base.getStackFrameSlotsNeeded(), this.overrides.getStackFrameSlotsNeeded());
    }

    @Override
    public Rule getRule(Item item, XPathContext context, Mode.RuleFilter filter) throws XPathException {
        Rule r = this.overrides.getRule(item, context, filter);
        if (r == null) {
            r = this.base.getRule(item, context, filter);
        }
        return r;
    }

    @Override
    public void exportTemplateRules(ExpressionPresenter presenter) throws XPathException {
        this.overrides.exportTemplateRules(presenter);
        this.base.exportTemplateRules(presenter);
    }

    @Override
    public void explainTemplateRules(ExpressionPresenter presenter) throws XPathException {
        this.overrides.explainTemplateRules(presenter);
        this.base.explainTemplateRules(presenter);
    }
}


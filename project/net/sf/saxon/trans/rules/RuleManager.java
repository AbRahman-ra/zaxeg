/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans.rules;

import java.util.Collection;
import java.util.HashMap;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.TemplateRule;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.CombinedNodeTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.NodeTestPattern;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.pattern.UnionPattern;
import net.sf.saxon.style.StylesheetModule;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.CompilerInfo;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.trans.SimpleMode;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.VisibilityProvenance;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.Rule;

public final class RuleManager {
    private StylesheetPackage stylesheetPackage;
    private Configuration config;
    private SimpleMode unnamedMode;
    private HashMap<StructuredQName, Mode> modes;
    private SimpleMode omniMode = null;
    private boolean unnamedModeExplicit;
    private CompilerInfo compilerInfo;
    private int nextSequenceNumber = 0;

    public RuleManager(StylesheetPackage pack) {
        this(pack, pack.getConfiguration().getDefaultXsltCompilerInfo());
    }

    public RuleManager(StylesheetPackage pack, CompilerInfo compilerInfo) {
        this.stylesheetPackage = pack;
        this.config = pack.getConfiguration();
        this.compilerInfo = compilerInfo;
        this.unnamedMode = this.config.makeMode(Mode.UNNAMED_MODE_NAME, this.compilerInfo);
        Component c = this.unnamedMode.makeDeclaringComponent(Visibility.PRIVATE, this.stylesheetPackage);
        c.setVisibility(Visibility.PRIVATE, VisibilityProvenance.DEFAULTED);
        this.stylesheetPackage.addComponent(c);
        this.modes = new HashMap(5);
    }

    public void setUnnamedModeExplicit(boolean declared) {
        this.unnamedModeExplicit = declared;
    }

    public boolean isUnnamedModeExplicit() {
        return this.unnamedModeExplicit;
    }

    public void setCompilerInfo(CompilerInfo compilerInfo) {
        this.compilerInfo = compilerInfo;
    }

    public Collection<Mode> getAllNamedModes() {
        return this.modes.values();
    }

    public SimpleMode getUnnamedMode() {
        return this.unnamedMode;
    }

    public Mode obtainMode(StructuredQName modeName, boolean createIfAbsent) {
        if (modeName == null || modeName.equals(Mode.UNNAMED_MODE_NAME)) {
            return this.unnamedMode;
        }
        if (modeName.equals(Mode.OMNI_MODE)) {
            if (this.omniMode == null) {
                this.omniMode = this.config.makeMode(modeName, this.compilerInfo);
            }
            return this.omniMode;
        }
        Mode m = this.modes.get(modeName);
        if (m == null && createIfAbsent) {
            m = this.config.makeMode(modeName, this.compilerInfo);
            this.modes.put(modeName, m);
            Component c = m.makeDeclaringComponent(Visibility.PRIVATE, this.stylesheetPackage);
            c.setVisibility(Visibility.PRIVATE, VisibilityProvenance.DEFAULTED);
            this.stylesheetPackage.addComponent(c);
        }
        return m;
    }

    public void registerMode(Mode mode) {
        this.modes.put(mode.getModeName(), mode);
    }

    public boolean existsOmniMode() {
        return this.omniMode != null;
    }

    public int allocateSequenceNumber() {
        return this.nextSequenceNumber++;
    }

    public int registerRule(Pattern pattern, TemplateRule eh, Mode mode, StylesheetModule module, double priority, int position, int part) {
        if (pattern instanceof UnionPattern) {
            UnionPattern up = (UnionPattern)pattern;
            Pattern p1 = up.getLHS();
            Pattern p2 = up.getRHS();
            int lhsParts = this.registerRule(p1, eh, mode, module, priority, position, part);
            int rhsParts = this.registerRule(p2, eh, mode, module, priority, position, lhsParts);
            return lhsParts + rhsParts;
        }
        if (pattern instanceof NodeTestPattern && pattern.getItemType() instanceof CombinedNodeTest && ((CombinedNodeTest)pattern.getItemType()).getOperator() == 1) {
            CombinedNodeTest cnt = (CombinedNodeTest)pattern.getItemType();
            NodeTest[] nt = cnt.getComponentNodeTests();
            NodeTestPattern nt0 = new NodeTestPattern(nt[0]);
            ExpressionTool.copyLocationInfo(pattern, nt0);
            int lhsParts = this.registerRule(nt0, eh, mode, module, priority, position, part);
            NodeTestPattern nt1 = new NodeTestPattern(nt[1]);
            ExpressionTool.copyLocationInfo(pattern, nt1);
            int rhsParts = this.registerRule(nt1, eh, mode, module, priority, position, lhsParts);
            return lhsParts + rhsParts;
        }
        if (Double.isNaN(priority)) {
            priority = pattern.getDefaultPriority();
        } else {
            part = 0;
        }
        if (mode instanceof SimpleMode) {
            ((SimpleMode)mode).addRule(pattern, eh, module, module.getPrecedence(), priority, position, part);
        } else {
            mode.getActivePart().addRule(pattern, eh, module, mode.getMaxPrecedence(), priority, position, part);
        }
        return 1;
    }

    public Rule getTemplateRule(Item item, Mode mode, int min, int max, XPathContext c) throws XPathException {
        if (mode == null) {
            mode = this.unnamedMode;
        }
        return mode.getRule(item, min, max, c);
    }

    public void computeRankings() throws XPathException {
        this.unnamedMode.computeRankings(0);
        for (Mode mode : this.modes.values()) {
            mode.computeRankings(0);
        }
    }

    public void invertStreamableTemplates() throws XPathException {
        this.unnamedMode.invertStreamableTemplates();
        for (Mode mode : this.modes.values()) {
            mode.getActivePart().invertStreamableTemplates();
        }
    }

    public void checkConsistency() throws XPathException {
        this.unnamedMode.checkForConflictingProperties();
        for (Mode mode : this.modes.values()) {
            mode.getActivePart().checkForConflictingProperties();
        }
    }

    public void explainTemplateRules(ExpressionPresenter presenter) throws XPathException {
        this.unnamedMode.explain(presenter);
        for (Mode mode : this.modes.values()) {
            mode.explain(presenter);
        }
    }

    public void optimizeRules() {
        this.unnamedMode.optimizeRules();
        for (Mode mode : this.modes.values()) {
            mode.getActivePart().optimizeRules();
        }
    }
}


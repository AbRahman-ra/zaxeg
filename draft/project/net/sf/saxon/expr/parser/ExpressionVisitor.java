/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.parser;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.s9api.Location;

public class ExpressionVisitor {
    private StaticContext staticContext;
    private boolean optimizeForStreaming = false;
    private boolean optimizeForPatternMatching = false;
    private Configuration config;
    private Optimizer optimizer;
    private int depth = 0;
    private boolean suppressWarnings = false;
    private static final int MAX_DEPTH = 500;

    public ExpressionVisitor(Configuration config) {
        this.config = config;
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    public StaticContext getStaticContext() {
        return this.staticContext;
    }

    public void setStaticContext(StaticContext staticContext) {
        this.staticContext = staticContext;
    }

    public static ExpressionVisitor make(StaticContext env) {
        ExpressionVisitor visitor = new ExpressionVisitor(env.getConfiguration());
        visitor.setStaticContext(env);
        return visitor;
    }

    public void issueWarning(String message, Location locator) {
        if (!this.isSuppressWarnings()) {
            this.staticContext.issueWarning(message, locator);
        }
    }

    public XPathContext makeDynamicContext() {
        return this.staticContext.makeEarlyEvaluationContext();
    }

    public Optimizer obtainOptimizer() {
        if (this.optimizer == null) {
            this.optimizer = this.config.obtainOptimizer(this.staticContext.getOptimizerOptions());
        }
        return this.optimizer;
    }

    public void setOptimizeForStreaming(boolean option) {
        this.optimizeForStreaming = option;
    }

    public boolean isOptimizeForStreaming() {
        return this.optimizeForStreaming;
    }

    public void setOptimizeForPatternMatching(boolean option) {
        this.optimizeForPatternMatching = option;
    }

    public boolean isOptimizeForPatternMatching() {
        return this.optimizeForPatternMatching;
    }

    public String getTargetEdition() {
        return this.staticContext.getPackageData().getTargetEdition();
    }

    public boolean incrementAndTestDepth() {
        return this.depth++ < 500;
    }

    public void decrementDepth() {
        --this.depth;
    }

    public boolean isSuppressWarnings() {
        return this.suppressWarnings;
    }

    public void setSuppressWarnings(boolean suppressWarnings) {
        this.suppressWarnings = suppressWarnings;
    }
}


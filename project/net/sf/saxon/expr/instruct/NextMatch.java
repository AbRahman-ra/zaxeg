/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.Arrays;
import net.sf.saxon.Controller;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.ApplyNextMatchingTemplate;
import net.sf.saxon.expr.instruct.ParameterSet;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.instruct.TemplateRule;
import net.sf.saxon.expr.instruct.WithParam;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.Rule;

public class NextMatch
extends ApplyNextMatchingTemplate {
    boolean useTailRecursion;

    public NextMatch(boolean useTailRecursion) {
        this.useTailRecursion = useTailRecursion;
    }

    @Override
    public int getInstructionNameCode() {
        return 178;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        NextMatch nm2 = new NextMatch(this.useTailRecursion);
        nm2.setActualParams(WithParam.copy(nm2, this.getActualParams(), rebindings));
        nm2.setTunnelParams(WithParam.copy(nm2, this.getTunnelParams(), rebindings));
        ExpressionTool.copyLocationInfo(this, nm2);
        return nm2;
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        Item currentItem;
        Controller controller = context.getController();
        assert (controller != null);
        ParameterSet params = NextMatch.assembleParams(context, this.getActualParams());
        ParameterSet tunnels = NextMatch.assembleTunnelParams(context, this.getTunnelParams());
        Rule currentRule = context.getCurrentTemplateRule();
        if (currentRule == null) {
            XPathException e = new XPathException("There is no current template rule", "XTDE0560");
            e.setXPathContext(context);
            e.setLocation(this.getLocation());
            throw e;
        }
        Component.M modeComponent = context.getCurrentMode();
        if (modeComponent == null) {
            throw new AssertionError((Object)"Current mode is null");
        }
        Mode mode = modeComponent.getActor();
        Rule rule = mode.getNextMatchRule(currentItem = context.getCurrentIterator().current(), currentRule, context);
        if (rule == null) {
            mode.getBuiltInRuleSet().process(currentItem, params, tunnels, output, context, this.getLocation());
        } else {
            if (this.useTailRecursion) {
                Arrays.fill(context.getStackFrame().getStackFrameValues(), null);
                ((XPathContextMajor)context).setCurrentComponent(modeComponent);
                return new NextMatchPackage(rule, params, tunnels, output, context);
            }
            TemplateRule nh = (TemplateRule)rule.getAction();
            nh.initialize();
            XPathContextMajor c2 = context.newContext();
            c2.setOrigin(this);
            c2.openStackFrame(nh.getStackFrameMap());
            c2.setLocalParameters(params);
            c2.setTunnelParameters(tunnels);
            c2.setCurrentTemplateRule(rule);
            c2.setCurrentComponent(modeComponent);
            c2.setCurrentMergeGroupIterator(null);
            nh.apply(output, c2);
        }
        return null;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("nextMatch", this);
        String flags = "i";
        if (this.useTailRecursion) {
            flags = "t";
        }
        out.emitAttribute("flags", flags);
        if (this.getActualParams().length != 0) {
            WithParam.exportParameters(this.getActualParams(), out, false);
        }
        if (this.getTunnelParams().length != 0) {
            WithParam.exportParameters(this.getTunnelParams(), out, true);
        }
        out.endElement();
    }

    @Override
    public String getStreamerName() {
        return "NextMatch";
    }

    private class NextMatchPackage
    implements TailCall {
        private Rule rule;
        private ParameterSet params;
        private ParameterSet tunnelParams;
        private Outputter output;
        private XPathContext evaluationContext;

        public NextMatchPackage(Rule rule, ParameterSet params, ParameterSet tunnelParams, Outputter output, XPathContext evaluationContext) {
            this.rule = rule;
            this.params = params;
            this.tunnelParams = tunnelParams;
            this.output = output;
            this.evaluationContext = evaluationContext;
        }

        @Override
        public TailCall processLeavingTail() throws XPathException {
            TemplateRule nh = (TemplateRule)this.rule.getAction();
            nh.initialize();
            XPathContextMajor c2 = this.evaluationContext.newContext();
            c2.setOrigin(NextMatch.this);
            c2.setLocalParameters(this.params);
            c2.setTunnelParameters(this.tunnelParams);
            c2.openStackFrame(nh.getStackFrameMap());
            c2.setCurrentTemplateRule(this.rule);
            c2.setCurrentComponent(this.evaluationContext.getCurrentComponent());
            c2.setCurrentMergeGroupIterator(null);
            return nh.applyLeavingTail(this.output, c2);
        }
    }
}


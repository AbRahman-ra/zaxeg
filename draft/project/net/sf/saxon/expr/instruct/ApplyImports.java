/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.Controller;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.ApplyNextMatchingTemplate;
import net.sf.saxon.expr.instruct.ITemplateCall;
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

public class ApplyImports
extends ApplyNextMatchingTemplate
implements ITemplateCall {
    @Override
    public int getInstructionNameCode() {
        return 132;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        ApplyImports ai2 = new ApplyImports();
        ai2.setActualParams(WithParam.copy(ai2, this.getActualParams(), rebindings));
        ai2.setTunnelParams(WithParam.copy(ai2, this.getTunnelParams(), rebindings));
        ExpressionTool.copyLocationInfo(this, ai2);
        return ai2;
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        Controller controller = context.getController();
        assert (controller != null);
        ParameterSet params = ApplyImports.assembleParams(context, this.getActualParams());
        ParameterSet tunnels = ApplyImports.assembleTunnelParams(context, this.getTunnelParams());
        Rule currentTemplateRule = context.getCurrentTemplateRule();
        if (currentTemplateRule == null) {
            XPathException e = new XPathException("There is no current template rule");
            e.setXPathContext(context);
            e.setErrorCode("XTDE0560");
            e.setLocation(this.getLocation());
            throw e;
        }
        int min = currentTemplateRule.getMinImportPrecedence();
        int max = currentTemplateRule.getPrecedence() - 1;
        Component.M modeComponent = context.getCurrentMode();
        if (modeComponent == null) {
            throw new AssertionError((Object)"Current mode is null");
        }
        Item currentItem = context.getCurrentIterator().current();
        Mode mode = modeComponent.getActor();
        Rule rule = mode.getRule(currentItem, min, max, context);
        if (rule == null) {
            mode.getBuiltInRuleSet().process(currentItem, params, tunnels, output, context, this.getLocation());
        } else {
            XPathContextMajor c2 = context.newContext();
            TemplateRule nh = (TemplateRule)rule.getAction();
            nh.initialize();
            c2.setOrigin(this);
            c2.setLocalParameters(params);
            c2.setTunnelParameters(tunnels);
            c2.openStackFrame(nh.getStackFrameMap());
            c2.setCurrentTemplateRule(rule);
            c2.setCurrentComponent(modeComponent);
            c2.setCurrentMergeGroupIterator(null);
            nh.apply(output, c2);
        }
        return null;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("applyImports", this);
        out.emitAttribute("flags", "i");
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
        return "ApplyImports";
    }
}


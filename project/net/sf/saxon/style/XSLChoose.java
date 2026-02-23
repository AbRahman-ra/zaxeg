/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.Arrays;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.instruct.Choose;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLOtherwise;
import net.sf.saxon.style.XSLWhen;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;

public class XSLChoose
extends StyleElement {
    private StyleElement otherwise;
    private int numberOfWhens = 0;

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            this.checkUnknownAttribute(attName);
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof XSLWhen) {
                if (this.otherwise != null) {
                    this.otherwise.compileError("xsl:otherwise must come last", "XTSE0010");
                }
                ++this.numberOfWhens;
                continue;
            }
            if (nodeInfo instanceof XSLOtherwise) {
                if (this.otherwise != null) {
                    ((XSLOtherwise)nodeInfo).compileError("Only one xsl:otherwise is allowed in an xsl:choose", "XTSE0010");
                    continue;
                }
                this.otherwise = (StyleElement)nodeInfo;
                continue;
            }
            if (nodeInfo instanceof StyleElement) {
                ((StyleElement)nodeInfo).compileError("Only xsl:when and xsl:otherwise are allowed here", "XTSE0010");
                continue;
            }
            this.compileError("Only xsl:when and xsl:otherwise are allowed within xsl:choose", "XTSE0010");
        }
        if (this.numberOfWhens == 0) {
            this.compileError("xsl:choose must contain at least one xsl:when", "XTSE0010");
        }
    }

    @Override
    public boolean markTailCalls() {
        boolean found = false;
        for (NodeInfo nodeInfo : this.children(StyleElement.class::isInstance)) {
            found |= ((StyleElement)nodeInfo).markTailCalls();
        }
        return found;
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        int entries = this.numberOfWhens + (this.otherwise == null ? 0 : 1);
        Expression[] conditions = new Expression[entries];
        Expression[] actions = new Expression[entries];
        int w = 0;
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof XSLWhen) {
                conditions[w] = ((XSLWhen)nodeInfo).getCondition();
                Expression b = ((XSLWhen)nodeInfo).compileSequenceConstructor(exec, decl, true);
                if (b == null) {
                    b = Literal.makeEmptySequence();
                }
                try {
                    actions[w] = b = b.simplify();
                } catch (XPathException e) {
                    this.compileError(e);
                }
                if (this.getCompilation().getCompilerInfo().isCompileWithTracing()) {
                    actions[w] = XSLChoose.makeTraceInstruction((XSLWhen)nodeInfo, actions[w]);
                }
                if (conditions[w] instanceof Literal && ((Literal)conditions[w]).getValue() instanceof BooleanValue) {
                    if (((BooleanValue)((Literal)conditions[w]).getValue()).getBooleanValue()) {
                        entries = w + 1;
                        break;
                    }
                    --w;
                    --entries;
                }
                ++w;
                continue;
            }
            if (!(nodeInfo instanceof XSLOtherwise)) continue;
            Literal otherwise = Literal.makeLiteral(BooleanValue.TRUE);
            otherwise.setRetainedStaticContext(this.makeRetainedStaticContext());
            conditions[w] = otherwise;
            Expression b = ((XSLOtherwise)nodeInfo).compileSequenceConstructor(exec, decl, true);
            if (b == null) {
                b = Literal.makeEmptySequence();
                b.setRetainedStaticContext(this.makeRetainedStaticContext());
            }
            try {
                actions[w] = b = b.simplify();
            } catch (XPathException e) {
                this.compileError(e);
            }
            if (this.getCompilation().getCompilerInfo().isCompileWithTracing()) {
                actions[w] = XSLChoose.makeTraceInstruction((XSLOtherwise)nodeInfo, actions[w]);
            }
            ++w;
        }
        if (conditions.length != entries) {
            if (entries == 0) {
                return null;
            }
            if (entries == 1 && conditions[0] instanceof Literal && ((Literal)conditions[0]).getValue() instanceof BooleanValue) {
                if (((BooleanValue)((Literal)conditions[0]).getValue()).getBooleanValue()) {
                    return actions[0];
                }
                return null;
            }
            conditions = Arrays.copyOf(conditions, entries);
            actions = Arrays.copyOf(actions, entries);
        }
        Choose choose = new Choose(conditions, actions);
        choose.setInstruction(true);
        return choose;
    }
}


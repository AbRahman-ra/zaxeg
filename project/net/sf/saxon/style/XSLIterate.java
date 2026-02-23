/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.ArrayList;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.instruct.IterateInstr;
import net.sf.saxon.expr.instruct.LocalParam;
import net.sf.saxon.expr.instruct.LocalParamBlock;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLLocalParam;
import net.sf.saxon.style.XSLOnCompletion;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ListIterator;

public class XSLIterate
extends StyleElement {
    Expression select = null;
    boolean compilable;

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    protected boolean isPermittedChild(StyleElement child) {
        return child instanceof XSLLocalParam || child instanceof XSLOnCompletion;
    }

    @Override
    public boolean mayContainSequenceConstructor() {
        return true;
    }

    @Override
    protected boolean mayContainParam() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        String selectAtt = null;
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String value = att.getValue();
            String f = attName.getDisplayName();
            if (f.equals("select")) {
                selectAtt = value;
                this.select = this.makeExpression(selectAtt, att);
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        if (selectAtt == null) {
            this.reportAbsence("select");
        }
    }

    public void setCompilable(boolean compilable) {
        this.compilable = compilable;
    }

    public boolean isCompilable() {
        return this.compilable;
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.select = this.typeCheck("select", this.select);
        if (!this.hasChildNodes()) {
            this.compileWarning("An empty xsl:iterate instruction has no effect", "SXWN9009");
        }
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        void var7_9;
        ArrayList<NodeInfo> nonFinallyChildren = new ArrayList<NodeInfo>();
        Expression finallyExp = null;
        ArrayList<XSLLocalParam> params = new ArrayList<XSLLocalParam>();
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof XSLLocalParam) {
                params.add((XSLLocalParam)nodeInfo);
                continue;
            }
            if (nodeInfo instanceof XSLOnCompletion) {
                finallyExp = ((XSLOnCompletion)nodeInfo).compile(exec, decl);
                continue;
            }
            nonFinallyChildren.add(nodeInfo);
        }
        LocalParam[] compiledParams = new LocalParam[params.size()];
        boolean bl = false;
        while (var7_9 < params.size()) {
            compiledParams[var7_9] = (LocalParam)((XSLLocalParam)params.get((int)var7_9)).compile(exec, decl);
            if (compiledParams[var7_9].isImplicitlyRequiredParam()) {
                this.compileError("The parameter must be given an initial value because () is not valid, given the declared type", "XTSE3520");
            }
            ++var7_9;
        }
        LocalParamBlock localParamBlock = new LocalParamBlock(compiledParams);
        Expression action = this.compileSequenceConstructor(exec, decl, new ListIterator(nonFinallyChildren), false);
        if (action == null) {
            return Literal.makeEmptySequence();
        }
        try {
            action = action.simplify();
            return new IterateInstr(this.select, localParamBlock, action, finallyExp);
        } catch (XPathException err) {
            this.compileError(err);
            return null;
        }
    }
}


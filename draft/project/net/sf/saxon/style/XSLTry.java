/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.TryCatch;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.pattern.QNameTest;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLCatch;
import net.sf.saxon.style.XSLFallback;
import net.sf.saxon.trans.XPathException;

public class XSLTry
extends StyleElement {
    private Expression select;
    private boolean rollbackOutput = true;
    private List<QNameTest> catchTests = new ArrayList<QNameTest>();
    private List<Expression> catchExprs = new ArrayList<Expression>();

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    public boolean mayContainSequenceConstructor() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        String selectAtt = null;
        String rollbackOutputAtt = null;
        block8: for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            switch (f) {
                case "select": {
                    selectAtt = value;
                    this.select = this.makeExpression(selectAtt, att);
                    continue block8;
                }
                case "rollback-output": {
                    rollbackOutputAtt = value;
                    continue block8;
                }
            }
            this.checkUnknownAttribute(attName);
        }
        if (rollbackOutputAtt != null) {
            this.rollbackOutput = this.processBooleanAttribute("rollback-output", rollbackOutputAtt);
        }
    }

    @Override
    protected boolean isPermittedChild(StyleElement child) {
        return child instanceof XSLCatch;
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.select = this.typeCheck("select", this.select);
        boolean foundCatch = false;
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof XSLCatch) {
                foundCatch = true;
                continue;
            }
            if (nodeInfo instanceof XSLFallback) continue;
            if (foundCatch) {
                this.compileError("xsl:catch elements must come after all other children of xsl:try (excepting xsl:fallback)", "XTSE0010");
            }
            if (this.select == null) continue;
            this.compileError("An " + this.getDisplayName() + " element with a select attribute must be empty", "XTSE3140");
        }
        if (!foundCatch) {
            this.compileError("xsl:try must have at least one xsl:catch child element", "XTSE0010");
        }
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        Expression content = this.compileSequenceConstructor(exec, decl, true);
        if (this.select == null) {
            this.select = content;
        }
        TryCatch expr = new TryCatch(this.select);
        for (int i = 0; i < this.catchTests.size(); ++i) {
            expr.addCatchExpression(this.catchTests.get(i), this.catchExprs.get(i));
        }
        expr.setRollbackOutput(this.rollbackOutput);
        return expr;
    }

    public void addCatchClause(QNameTest nameTest, Expression catchExpr) {
        this.catchTests.add(nameTest);
        this.catchExprs.add(catchExpr);
    }
}


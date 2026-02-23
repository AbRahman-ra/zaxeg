/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.sort.SortExpression;
import net.sf.saxon.expr.sort.SortKeyDefinitionList;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLFallback;
import net.sf.saxon.style.XSLSort;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Whitespace;

public class XSLPerformSort
extends StyleElement {
    Expression select = null;

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    public boolean mayContainSequenceConstructor() {
        return true;
    }

    @Override
    protected boolean isPermittedChild(StyleElement child) {
        return child instanceof XSLSort;
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
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.checkSortComesFirst(true);
        if (this.select != null) {
            for (NodeInfo nodeInfo : this.children()) {
                if (nodeInfo instanceof XSLSort || nodeInfo instanceof XSLFallback) continue;
                if (nodeInfo.getNodeKind() == 3 && !Whitespace.isWhite(nodeInfo.getStringValueCS())) {
                    this.compileError("Within xsl:perform-sort, significant text must not appear if there is a select attribute", "XTSE1040");
                    continue;
                }
                ((StyleElement)nodeInfo).compileError("Within xsl:perform-sort, child instructions are not allowed if there is a select attribute", "XTSE1040");
            }
        }
        this.select = this.typeCheck("select", this.select);
    }

    @Override
    public Expression compile(Compilation compilation, ComponentDeclaration decl) throws XPathException {
        SortKeyDefinitionList sortKeys = this.makeSortKeys(compilation, decl);
        if (this.select != null) {
            return new SortExpression(this.select, sortKeys);
        }
        Expression body = this.compileSequenceConstructor(compilation, decl, true);
        if (body == null) {
            body = Literal.makeEmptySequence();
        }
        try {
            return new SortExpression(body.simplify(), sortKeys);
        } catch (XPathException e) {
            this.compileError(e);
            return null;
        }
    }
}


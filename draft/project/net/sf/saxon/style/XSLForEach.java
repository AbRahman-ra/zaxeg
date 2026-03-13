/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.instruct.ForEach;
import net.sf.saxon.expr.sort.SortExpression;
import net.sf.saxon.expr.sort.SortKeyDefinitionList;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLSort;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.Whitespace;

public class XSLForEach
extends StyleElement {
    private Expression select = null;
    private boolean containsTailCall = false;
    private Expression threads = null;
    private Expression separator = null;

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    protected boolean isPermittedChild(StyleElement child) {
        return child instanceof XSLSort;
    }

    @Override
    protected boolean markTailCalls() {
        assert (this.select != null);
        if (Cardinality.allowsMany(this.select.getCardinality())) {
            return false;
        }
        StyleElement last = this.getLastChildInstruction();
        this.containsTailCall = last != null && last.markTailCalls();
        return this.containsTailCall;
    }

    @Override
    public boolean mayContainSequenceConstructor() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        String selectAtt = null;
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            if (f.equals("select")) {
                selectAtt = value;
                this.select = this.makeExpression(selectAtt, att);
                continue;
            }
            if (f.equals("separator")) {
                this.requireSyntaxExtensions("separator");
                this.separator = this.makeAttributeValueTemplate(value, att);
                continue;
            }
            if (attName.getLocalPart().equals("threads") && attName.hasURI("http://saxon.sf.net/")) {
                String threadsAtt = Whitespace.trim(value);
                this.threads = this.makeAttributeValueTemplate(threadsAtt, att);
                if (this.getCompilation().getCompilerInfo().isCompileWithTracing()) {
                    this.compileWarning("saxon:threads - no multithreading takes place when compiling with trace enabled", "SXWN9012");
                    this.threads = new StringLiteral("0");
                    continue;
                }
                if ("EE".equals(this.getConfiguration().getEditionCode())) continue;
                this.compileWarning("saxon:threads - ignored when not running Saxon-EE", "SXWN9013");
                this.threads = new StringLiteral("0");
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        if (selectAtt == null) {
            this.reportAbsence("select");
            this.select = Literal.makeEmptySequence();
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.checkSortComesFirst(false);
        this.select = this.typeCheck("select", this.select);
        if (this.separator != null) {
            this.separator = this.typeCheck("separator", this.separator);
        }
        if (this.threads != null) {
            this.threads = this.typeCheck("threads", this.threads);
        }
        if (!this.hasChildNodes()) {
            this.compileWarning("An empty xsl:for-each instruction has no effect", "SXWN9009");
        }
    }

    @Override
    public Expression compile(Compilation compilation, ComponentDeclaration decl) throws XPathException {
        Expression block;
        SortKeyDefinitionList sortKeys = this.makeSortKeys(compilation, decl);
        Expression sortedSequence = this.select;
        if (sortKeys != null) {
            sortedSequence = new SortExpression(this.select, sortKeys);
        }
        if ((block = this.compileSequenceConstructor(compilation, decl, true)) == null) {
            return Literal.makeEmptySequence();
        }
        try {
            ForEach result = new ForEach(sortedSequence, block.simplify(), this.containsTailCall, this.threads);
            result.setInstruction(true);
            result.setLocation(this.allocateLocation());
            if (this.separator != null) {
                result.setSeparatorExpression(this.separator);
            }
            return result;
        } catch (XPathException err) {
            this.compileError(err);
            return null;
        }
    }
}


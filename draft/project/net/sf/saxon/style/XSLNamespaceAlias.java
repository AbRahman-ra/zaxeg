/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Whitespace;

public class XSLNamespaceAlias
extends StyleElement {
    private String stylesheetURI;
    private NamespaceBinding resultNamespaceBinding;

    @Override
    public boolean isDeclaration() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        String stylesheetPrefix = null;
        String resultPrefix = null;
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            if (f.equals("stylesheet-prefix")) {
                stylesheetPrefix = Whitespace.trim(value);
                continue;
            }
            if (f.equals("result-prefix")) {
                resultPrefix = Whitespace.trim(value);
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        if (stylesheetPrefix == null) {
            this.reportAbsence("stylesheet-prefix");
            stylesheetPrefix = "";
        }
        if (stylesheetPrefix.equals("#default")) {
            stylesheetPrefix = "";
        }
        if (resultPrefix == null) {
            this.reportAbsence("result-prefix");
            resultPrefix = "";
        }
        if (resultPrefix.equals("#default")) {
            resultPrefix = "";
        }
        this.stylesheetURI = this.getURIForPrefix(stylesheetPrefix, true);
        if (this.stylesheetURI == null) {
            this.compileError("stylesheet-prefix " + stylesheetPrefix + " has not been declared", "XTSE0812");
            this.stylesheetURI = "";
            this.resultNamespaceBinding = NamespaceBinding.DEFAULT_UNDECLARATION;
            return;
        }
        String resultURI = this.getURIForPrefix(resultPrefix, true);
        if (resultURI == null) {
            this.compileError("result-prefix " + resultPrefix + " has not been declared", "XTSE0812");
            this.stylesheetURI = "";
            resultURI = "";
        }
        this.resultNamespaceBinding = new NamespaceBinding(resultPrefix, resultURI);
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.checkTopLevel("XTSE0010", false);
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        return null;
    }

    @Override
    public void index(ComponentDeclaration decl, PrincipalStylesheetModule top) throws XPathException {
        top.addNamespaceAlias(decl);
    }

    public String getStylesheetURI() {
        return this.stylesheetURI;
    }

    public NamespaceBinding getResultNamespaceBinding() {
        return this.resultNamespaceBinding;
    }
}


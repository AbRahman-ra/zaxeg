/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.LocalNameTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NamespaceTest;
import net.sf.saxon.pattern.QNameTest;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLAccept;
import net.sf.saxon.trans.ComponentTest;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Whitespace;

public abstract class XSLAcceptExpose
extends StyleElement {
    private Set<ComponentTest> explicitComponentTests = new HashSet<ComponentTest>();
    private Set<ComponentTest> wildcardComponentTests = new HashSet<ComponentTest>();
    private Visibility visibility;

    @Override
    public Visibility getVisibility() {
        return this.visibility;
    }

    public Set<ComponentTest> getExplicitComponentTests() throws XPathException {
        this.prepareAttributes();
        return this.explicitComponentTests;
    }

    public Set<ComponentTest> getWildcardComponentTests() throws XPathException {
        this.prepareAttributes();
        return this.wildcardComponentTests;
    }

    @Override
    protected void prepareAttributes() {
        if (this.visibility != null) {
            return;
        }
        String componentAtt = null;
        String namesAtt = null;
        String visibilityAtt = null;
        block28: for (AttributeInfo att : this.attributes()) {
            Object attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            switch (f) {
                case "names": {
                    namesAtt = Whitespace.trim(value);
                    continue block28;
                }
                case "component": {
                    componentAtt = Whitespace.trim(value);
                    continue block28;
                }
                case "visibility": {
                    visibilityAtt = Whitespace.trim(value);
                    continue block28;
                }
            }
            this.checkUnknownAttribute((NodeName)attName);
        }
        if (visibilityAtt == null) {
            this.reportAbsence("visibility");
            this.visibility = Visibility.PRIVATE;
        } else {
            this.visibility = this.interpretVisibilityValue(visibilityAtt, this instanceof XSLAccept ? "ha" : "");
            if (this.visibility == null) {
                this.visibility = Visibility.PRIVATE;
            }
        }
        int componentTypeCode = 158;
        if (componentAtt == null) {
            this.reportAbsence("component");
        } else {
            String local = Whitespace.trim(componentAtt);
            switch (local) {
                case "function": {
                    componentTypeCode = 158;
                    break;
                }
                case "template": {
                    componentTypeCode = 200;
                    break;
                }
                case "variable": {
                    componentTypeCode = 206;
                    break;
                }
                case "attribute-set": {
                    componentTypeCode = 136;
                    break;
                }
                case "mode": {
                    componentTypeCode = 174;
                    break;
                }
                case "*": {
                    componentTypeCode = -1;
                    break;
                }
                default: {
                    this.compileError("The component type is not one of the allowed names (function, template, variable, attribute-set, or mode)", "XTSE0020");
                    return;
                }
            }
        }
        if (namesAtt == null) {
            this.reportAbsence("names");
            namesAtt = "";
        }
        StringTokenizer st = new StringTokenizer(namesAtt, " \t\r\n");
        while (st.hasMoreTokens()) {
            StructuredQName name;
            String tok = st.nextToken();
            int hash = tok.lastIndexOf(35);
            if (hash > 0 && tok.indexOf(125, hash) < 0) {
                if (componentTypeCode == -1) {
                    this.compileErrorInAttribute("When component='*' is specified, all names must be wildcards", this instanceof XSLAccept ? "XTSE3032" : "XTSE3022", "names");
                    continue;
                }
                if (componentTypeCode == 158) {
                    name = this.makeQName(tok.substring(0, hash), null, "names");
                    NameTest test = new NameTest(1, name.getURI(), name.getLocalPart(), this.getNamePool());
                    int arity = 0;
                    try {
                        arity = Integer.parseInt(tok.substring(hash + 1));
                    } catch (Exception err) {
                        this.compileErrorInAttribute("Malformed function arity in '" + tok + "'", "XTSE0020", "names");
                    }
                    this.explicitComponentTests.add(new ComponentTest(componentTypeCode, test, arity));
                    continue;
                }
                this.compileErrorInAttribute("Cannot specify arity for components other than functions", "XTSE3020", "names");
                continue;
            }
            if (tok.equals("*")) {
                AnyNodeTest test = AnyNodeTest.getInstance();
                this.addWildCardTest(componentTypeCode, test);
                continue;
            }
            if (tok.endsWith(":*")) {
                String prefix;
                String uri;
                if (tok.length() == 2) {
                    this.compileErrorInAttribute("No prefix before ':*'", "XTSE0020", "names");
                }
                if ((uri = this.getURIForPrefix(prefix = tok.substring(0, tok.length() - 2), false)) == null) {
                    this.compileErrorInAttribute("Undeclared prefix " + prefix, "XTSE0020", "names");
                    uri = "http://ns.saxonica.com/anonymous-type";
                }
                NamespaceTest test = new NamespaceTest(this.getNamePool(), 1, uri);
                this.addWildCardTest(componentTypeCode, test);
                continue;
            }
            if (tok.startsWith("Q{") && tok.endsWith("}*")) {
                String uri = tok.substring(2, tok.length() - 2);
                NamespaceTest test = new NamespaceTest(this.getNamePool(), 1, uri);
                this.wildcardComponentTests.add(new ComponentTest(componentTypeCode, test, -1));
                continue;
            }
            if (tok.startsWith("*:")) {
                if (tok.length() == 2) {
                    this.compileErrorInAttribute("No local name after '*:'", "XTSE0020", "names");
                }
                String localname = tok.substring(2);
                LocalNameTest test = new LocalNameTest(this.getNamePool(), 1, localname);
                this.addWildCardTest(componentTypeCode, test);
                continue;
            }
            if (componentTypeCode == -1) {
                this.compileErrorInAttribute("When component='*' is specified, all names must be wildcards", this instanceof XSLAccept ? "XTSE3032" : "XTSE3022", "names");
                continue;
            }
            if (componentTypeCode == 158) {
                this.compileErrorInAttribute("When the name identifies a function, the arity must be given (XSLT 3.0 erratum E36)", "XTSE3020", "names");
                continue;
            }
            name = this.makeQName(tok, null, "names");
            NameTest test = new NameTest(1, name.getURI(), name.getLocalPart(), this.getNamePool());
            this.explicitComponentTests.add(new ComponentTest(componentTypeCode, test, -1));
        }
    }

    private void addWildCardTest(int componentTypeCode, QNameTest test) {
        if (componentTypeCode == -1) {
            this.wildcardComponentTests.add(new ComponentTest(158, test, -1));
            this.wildcardComponentTests.add(new ComponentTest(200, test, -1));
            this.wildcardComponentTests.add(new ComponentTest(206, test, -1));
            this.wildcardComponentTests.add(new ComponentTest(136, test, -1));
            this.wildcardComponentTests.add(new ComponentTest(174, test, -1));
        } else {
            this.wildcardComponentTests.add(new ComponentTest(componentTypeCode, test, -1));
        }
    }
}


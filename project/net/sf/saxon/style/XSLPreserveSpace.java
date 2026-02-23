/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.StringTokenizer;
import net.sf.saxon.event.Stripper;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.QNameException;
import net.sf.saxon.om.SelectedElementsSpaceStrippingRule;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.pattern.LocalNameTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NamespaceTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.trans.XPathException;

public class XSLPreserveSpace
extends StyleElement {
    private String elements;

    @Override
    public boolean isDeclaration() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            if (f.equals("elements")) {
                this.elements = att.getValue();
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        if (this.elements == null) {
            this.reportAbsence("elements");
            this.elements = "*";
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.checkEmpty();
        this.checkTopLevel("XTSE0010", false);
    }

    @Override
    public void index(ComponentDeclaration decl, PrincipalStylesheetModule top) {
        String elements;
        if (this.getFingerprint() == 198 && this.getFingerprint() == 198 && (elements = this.getAttributeValue("", "elements")) != null && !elements.trim().isEmpty()) {
            top.getStylesheetPackage().setStripsWhitespace(true);
        }
    }

    @Override
    public void compileDeclaration(Compilation compilation, ComponentDeclaration decl) {
        Stripper.StripRuleTarget preserve = this.getFingerprint() == 191 ? Stripper.PRESERVE : Stripper.STRIP;
        PrincipalStylesheetModule psm = this.getCompilation().getPrincipalStylesheetModule();
        SpaceStrippingRule stripperRules = psm.getStylesheetPackage().getStripperRules();
        if (!(stripperRules instanceof SelectedElementsSpaceStrippingRule)) {
            stripperRules = new SelectedElementsSpaceStrippingRule(true);
            psm.getStylesheetPackage().setStripperRules(stripperRules);
        }
        SelectedElementsSpaceStrippingRule rules = (SelectedElementsSpaceStrippingRule)stripperRules;
        StringTokenizer st = new StringTokenizer(this.elements, " \t\n\r", false);
        try {
            while (st.hasMoreTokens()) {
                String localName;
                String uri;
                String uri2;
                NodeTest nt;
                String s = st.nextToken();
                if (s.equals("*")) {
                    nt = NodeKindTest.ELEMENT;
                    rules.addRule(nt, preserve, decl.getModule(), decl.getSourceElement().getLineNumber());
                    continue;
                }
                if (s.startsWith("Q{")) {
                    int brace = s.indexOf(125);
                    if (brace < 0) {
                        this.compileError("No closing '}' in EQName");
                        continue;
                    }
                    if (brace == s.length() - 1) {
                        this.compileError("Missing local part in EQName");
                        continue;
                    }
                    uri2 = s.substring(2, brace);
                    String local = s.substring(brace + 1);
                    nt = local.equals("*") ? new NamespaceTest(this.getNamePool(), 1, uri2) : new NameTest(1, uri2, local, this.getNamePool());
                    rules.addRule(nt, preserve, decl.getModule(), decl.getSourceElement().getLineNumber());
                    continue;
                }
                if (s.endsWith(":*")) {
                    String prefix;
                    if (s.length() == 2) {
                        this.compileError("No prefix before ':*'");
                    }
                    if ((uri2 = this.getURIForPrefix(prefix = s.substring(0, s.length() - 2), false)) == null) {
                        this.undeclaredNamespaceError(prefix, "XTSE0280", "elements");
                    }
                    nt = new NamespaceTest(this.getNamePool(), 1, uri2);
                    rules.addRule(nt, preserve, decl.getModule(), decl.getSourceElement().getLineNumber());
                    continue;
                }
                if (s.startsWith("*:")) {
                    if (s.length() == 2) {
                        this.compileErrorInAttribute("No local name after '*:'", "XTSE0010", "elements");
                    }
                    String localname = s.substring(2);
                    nt = new LocalNameTest(this.getNamePool(), 1, localname);
                    rules.addRule(nt, preserve, decl.getModule(), decl.getSourceElement().getLineNumber());
                    continue;
                }
                try {
                    String[] parts = NameChecker.getQNameParts(s);
                    String prefix = parts[0];
                    if (parts[0].equals("")) {
                        uri = this.getDefaultXPathNamespace();
                    } else {
                        uri = this.getURIForPrefix(prefix, false);
                        if (uri == null) {
                            this.undeclaredNamespaceError(prefix, "XTSE0280", "elements");
                        }
                    }
                    localName = parts[1];
                } catch (QNameException err) {
                    this.compileError("Element name " + s + " is not a valid QName", "XTSE0280");
                    return;
                }
                NamePool target = this.getNamePool();
                int nameCode = target.allocateFingerprint(uri, localName);
                nt = new NameTest(1, nameCode, this.getNamePool());
                rules.addRule(nt, preserve, decl.getModule(), decl.getSourceElement().getLineNumber());
            }
        } catch (XPathException e) {
            e.maybeSetLocation(this.allocateLocation());
            this.compileError(e);
        }
    }
}


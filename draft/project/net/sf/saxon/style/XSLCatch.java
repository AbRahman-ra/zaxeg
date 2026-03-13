/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.QNameException;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.LocalNameTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NamespaceTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.QNameTest;
import net.sf.saxon.pattern.UnionQNameTest;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLTry;
import net.sf.saxon.trans.XPathException;

public class XSLCatch
extends StyleElement {
    private Expression select;
    private QNameTest nameTest;

    @Override
    public boolean isInstruction() {
        return false;
    }

    @Override
    public boolean mayContainSequenceConstructor() {
        return true;
    }

    @Override
    protected boolean seesAvuncularVariables() {
        return false;
    }

    @Override
    public void prepareAttributes() {
        String selectAtt = null;
        String errorAtt = null;
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            if (f.equals("select")) {
                selectAtt = value;
                this.select = this.makeExpression(selectAtt, att);
                continue;
            }
            if (f.equals("errors")) {
                errorAtt = value;
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        if (errorAtt == null) {
            this.nameTest = AnyNodeTest.getInstance();
        } else {
            List<QNameTest> tests = this.parseNameTests(errorAtt);
            if (tests.size() == 0) {
                this.compileError("xsl:catch/@errors must not be empty");
            }
            this.nameTest = tests.size() == 1 ? tests.get(0) : new UnionQNameTest(tests);
        }
    }

    private List<QNameTest> parseNameTests(String elements) {
        ArrayList<QNameTest> result = new ArrayList<QNameTest>();
        StringTokenizer st = new StringTokenizer(elements, " \t\n\r", false);
        while (st.hasMoreTokens()) {
            String localName;
            String uri;
            String prefix;
            NodeTest nt;
            String s = st.nextToken();
            if (s.equals("*")) {
                nt = AnyNodeTest.getInstance();
                result.add((QNameTest)((Object)nt));
                continue;
            }
            if (s.endsWith(":*")) {
                if (s.length() == 2) {
                    this.compileError("No prefix before ':*'");
                    result.add(AnyNodeTest.getInstance());
                }
                prefix = s.substring(0, s.length() - 2);
                String uri2 = this.getURIForPrefix(prefix, false);
                nt = new NamespaceTest(this.getNamePool(), 1, uri2);
                result.add((QNameTest)((Object)nt));
                continue;
            }
            if (s.startsWith("*:")) {
                if (s.length() == 2) {
                    this.compileErrorInAttribute("No local name after '*:'", "XTSE0010", "errors");
                    result.add(AnyNodeTest.getInstance());
                }
                String localname = s.substring(2);
                nt = new LocalNameTest(this.getNamePool(), 1, localname);
                result.add((QNameTest)((Object)nt));
                continue;
            }
            try {
                String[] parts = NameChecker.getQNameParts(s);
                prefix = parts[0];
                if (parts[0].equals("")) {
                    uri = "";
                } else {
                    uri = this.getURIForPrefix(prefix, false);
                    if (uri == null) {
                        this.undeclaredNamespaceError(prefix, "XTSE0280", "errors");
                        result.add(AnyNodeTest.getInstance());
                        break;
                    }
                }
                localName = parts[1];
            } catch (QNameException err) {
                this.compileErrorInAttribute("Error code " + s + " is not a valid QName", "XTSE0280", "errors");
                result.add(AnyNodeTest.getInstance());
                break;
            }
            NamePool target = this.getNamePool();
            int nameCode = target.allocateFingerprint(uri, localName);
            nt = new NameTest(1, nameCode, this.getNamePool());
            result.add((QNameTest)((Object)nt));
        }
        return result;
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.select = this.typeCheck("select", this.select);
        if (this.select != null && this.hasChildNodes()) {
            this.compileError("An xsl:catch element with a select attribute must be empty", "XTSE3150");
        }
        if (!(this.getParent() instanceof XSLTry)) {
            this.compileError("xsl:catch may appear only as a child of xsl:try");
        }
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        if (this.select == null) {
            this.select = this.compileSequenceConstructor(exec, decl, true);
        }
        ((XSLTry)this.getParent()).addCatchClause(this.nameTest, this.select);
        return null;
    }
}


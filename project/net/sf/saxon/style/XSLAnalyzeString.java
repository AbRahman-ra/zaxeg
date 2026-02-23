/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.ArrayList;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.instruct.AnalyzeString;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.regex.RegularExpression;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLFallback;
import net.sf.saxon.style.XSLMatchingSubstring;
import net.sf.saxon.trans.XPathException;

public class XSLAnalyzeString
extends StyleElement {
    private Expression select;
    private Expression regex;
    private Expression flags;
    private StyleElement matching;
    private StyleElement nonMatching;
    private RegularExpression pattern;

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    public boolean mayContainFallback() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        String selectAtt = null;
        String regexAtt = null;
        String flagsAtt = null;
        block12: for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            switch (f) {
                case "regex": {
                    regexAtt = value;
                    this.regex = this.makeAttributeValueTemplate(regexAtt, att);
                    continue block12;
                }
                case "select": {
                    selectAtt = value;
                    this.select = this.makeExpression(selectAtt, att);
                    continue block12;
                }
                case "flags": {
                    flagsAtt = value;
                    this.flags = this.makeAttributeValueTemplate(flagsAtt, att);
                    continue block12;
                }
            }
            this.checkUnknownAttribute(attName);
        }
        if (selectAtt == null) {
            this.reportAbsence("select");
            this.select = this.makeExpression(".", null);
        }
        if (regexAtt == null) {
            this.reportAbsence("regex");
            this.regex = this.makeAttributeValueTemplate("xxx", null);
        }
        if (flagsAtt == null) {
            flagsAtt = "";
            this.flags = this.makeAttributeValueTemplate("", null);
        }
        if (this.regex instanceof StringLiteral && this.flags instanceof StringLiteral) {
            try {
                String regex = ((StringLiteral)this.regex).getStringValue();
                String flagstr = ((StringLiteral)this.flags).getStringValue();
                ArrayList<String> warnings = new ArrayList<String>();
                this.pattern = this.getConfiguration().compileRegularExpression(regex, flagstr, this.getEffectiveVersion() >= 30 ? "XP30" : "XP20", warnings);
                for (String w : warnings) {
                    this.issueWarning(w, this);
                }
            } catch (XPathException err) {
                if ("FORX0001".equals(err.getErrorCodeLocalPart())) {
                    this.invalidFlags("Error in regular expression flags: " + err.getMessage());
                }
                this.invalidRegex("Error in regular expression: " + err.getMessage());
            }
        }
    }

    private void invalidRegex(String message) {
        this.compileErrorInAttribute(message, "XTDE1140", "regex");
        this.setDummyRegex();
    }

    private void invalidFlags(String message) {
        this.compileErrorInAttribute(message, "XTDE1145", "flags");
        this.setDummyRegex();
    }

    private void setDummyRegex() {
        try {
            this.pattern = this.getConfiguration().compileRegularExpression("x", "", "XP20", null);
        } catch (XPathException err) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        boolean foundFallback = false;
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof XSLFallback) {
                foundFallback = true;
                continue;
            }
            if (nodeInfo instanceof XSLMatchingSubstring) {
                boolean b = nodeInfo.getLocalPart().equals("matching-substring");
                if (b) {
                    if (this.matching != null || this.nonMatching != null || foundFallback) {
                        this.compileError("xsl:matching-substring element must come first", "XTSE0010");
                    }
                    this.matching = (StyleElement)nodeInfo;
                    continue;
                }
                if (this.nonMatching != null || foundFallback) {
                    this.compileError("xsl:non-matching-substring cannot appear here", "XTSE0010");
                }
                this.nonMatching = (StyleElement)nodeInfo;
                continue;
            }
            this.compileError("Only xsl:matching-substring and xsl:non-matching-substring are allowed here", "XTSE0010");
        }
        if (this.matching == null && this.nonMatching == null) {
            this.compileError("At least one xsl:matching-substring or xsl:non-matching-substring element must be present", "XTSE1130");
        }
        this.select = this.typeCheck("select", this.select);
        this.regex = this.typeCheck("regex", this.regex);
        this.flags = this.typeCheck("flags", this.flags);
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        Expression matchingBlock = null;
        if (this.matching != null) {
            matchingBlock = this.matching.compileSequenceConstructor(exec, decl, false);
        }
        Expression nonMatchingBlock = null;
        if (this.nonMatching != null) {
            nonMatchingBlock = this.nonMatching.compileSequenceConstructor(exec, decl, false);
        }
        try {
            return new AnalyzeString(this.select, this.regex, this.flags, matchingBlock == null ? null : matchingBlock.simplify(), nonMatchingBlock == null ? null : nonMatchingBlock.simplify(), this.pattern);
        } catch (XPathException e) {
            this.compileError(e);
            return null;
        }
    }
}


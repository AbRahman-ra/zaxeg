/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.trans.DecimalFormatManager;
import net.sf.saxon.trans.DecimalSymbols;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.AttributeLocation;
import net.sf.saxon.value.Whitespace;

public class XSLDecimalFormat
extends StyleElement {
    boolean prepared = false;
    String name;
    String decimalSeparator;
    String groupingSeparator;
    String exponentSeparator;
    String infinity;
    String minusSign;
    String NaN;
    String percent;
    String perMille;
    String zeroDigit;
    String digit;
    String patternSeparator;
    DecimalSymbols symbols;

    @Override
    public boolean isDeclaration() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        if (this.prepared) {
            return;
        }
        this.prepared = true;
        block28: for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            switch (f) {
                case "name": {
                    this.name = Whitespace.trim(value);
                    continue block28;
                }
                case "decimal-separator": {
                    this.decimalSeparator = value;
                    continue block28;
                }
                case "grouping-separator": {
                    this.groupingSeparator = value;
                    continue block28;
                }
                case "infinity": {
                    this.infinity = value;
                    continue block28;
                }
                case "minus-sign": {
                    this.minusSign = value;
                    continue block28;
                }
                case "NaN": {
                    this.NaN = value;
                    continue block28;
                }
                case "percent": {
                    this.percent = value;
                    continue block28;
                }
                case "per-mille": {
                    this.perMille = value;
                    continue block28;
                }
                case "zero-digit": {
                    this.zeroDigit = value;
                    continue block28;
                }
                case "digit": {
                    this.digit = value;
                    continue block28;
                }
                case "exponent-separator": {
                    this.exponentSeparator = value;
                    continue block28;
                }
                case "pattern-separator": {
                    this.patternSeparator = value;
                    continue block28;
                }
            }
            this.checkUnknownAttribute(attName);
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.checkTopLevel("XTSE0010", false);
        this.checkEmpty();
        int precedence = decl.getPrecedence();
        if (this.symbols == null) {
            return;
        }
        if (this.decimalSeparator != null) {
            this.setProp(0, this.decimalSeparator, precedence);
        }
        if (this.groupingSeparator != null) {
            this.setProp(1, this.groupingSeparator, precedence);
        }
        if (this.infinity != null) {
            this.setProp(9, this.infinity, precedence);
        }
        if (this.minusSign != null) {
            this.setProp(3, this.minusSign, precedence);
        }
        if (this.NaN != null) {
            this.setProp(10, this.NaN, precedence);
        }
        if (this.percent != null) {
            this.setProp(4, this.percent, precedence);
        }
        if (this.perMille != null) {
            this.setProp(5, this.perMille, precedence);
        }
        if (this.zeroDigit != null) {
            this.setProp(6, this.zeroDigit, precedence);
        }
        if (this.digit != null) {
            this.setProp(2, this.digit, precedence);
        }
        if (this.exponentSeparator != null) {
            this.setProp(7, this.exponentSeparator, precedence);
        }
        if (this.patternSeparator != null) {
            this.setProp(8, this.patternSeparator, precedence);
        }
    }

    private void setProp(int propertyCode, String value, int precedence) throws XPathException {
        try {
            this.symbols.setProperty(propertyCode, value, precedence);
        } catch (XPathException err) {
            String attName = DecimalSymbols.propertyNames[propertyCode];
            err.setLocation(new AttributeLocation(this, StructuredQName.fromClarkName(attName)));
            throw err;
        }
    }

    @Override
    public void index(ComponentDeclaration decl, PrincipalStylesheetModule top) {
        this.prepareAttributes();
        DecimalFormatManager dfm = this.getCompilation().getPrincipalStylesheetModule().getDecimalFormatManager();
        if (this.name == null) {
            this.symbols = dfm.getDefaultDecimalFormat();
        } else {
            StructuredQName formatName = this.makeQName(this.name, null, "name");
            this.symbols = dfm.obtainNamedDecimalFormat(formatName);
            this.symbols.setHostLanguage(HostLanguage.XSLT, 30);
        }
    }

    @Override
    public void compileDeclaration(Compilation compilation, ComponentDeclaration decl) throws XPathException {
    }
}


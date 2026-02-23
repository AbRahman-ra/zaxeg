/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.NumberSequenceFormatter;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.instruct.NumberInstruction;
import net.sf.saxon.expr.instruct.ValueOf;
import net.sf.saxon.expr.number.NumberFormatter;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Whitespace;

public class XSLNumber
extends StyleElement {
    private int level;
    private Pattern count = null;
    private Pattern from = null;
    private Expression select = null;
    private Expression value = null;
    private Expression format = null;
    private Expression groupSize = null;
    private Expression groupSeparator = null;
    private Expression letterValue = null;
    private Expression lang = null;
    private Expression ordinal = null;
    private Expression startAt = null;
    private NumberFormatter formatter = null;

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        String selectAtt = null;
        String valueAtt = null;
        String countAtt = null;
        String fromAtt = null;
        String levelAtt = null;
        String formatAtt = null;
        AttributeInfo gsizeAtt = null;
        AttributeInfo gsepAtt = null;
        String langAtt = null;
        String letterValueAtt = null;
        String ordinalAtt = null;
        String startAtAtt = null;
        block28: for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String attValue = att.getValue();
            switch (f) {
                case "select": {
                    selectAtt = attValue;
                    this.select = this.makeExpression(selectAtt, att);
                    continue block28;
                }
                case "value": {
                    valueAtt = attValue;
                    this.value = this.makeExpression(valueAtt, att);
                    continue block28;
                }
                case "count": {
                    countAtt = attValue;
                    continue block28;
                }
                case "from": {
                    fromAtt = attValue;
                    continue block28;
                }
                case "level": {
                    levelAtt = Whitespace.trim(attValue);
                    continue block28;
                }
                case "format": {
                    formatAtt = attValue;
                    this.format = this.makeAttributeValueTemplate(formatAtt, att);
                    continue block28;
                }
                case "lang": {
                    langAtt = attValue;
                    this.lang = this.makeAttributeValueTemplate(langAtt, att);
                    continue block28;
                }
                case "letter-value": {
                    letterValueAtt = Whitespace.trim(attValue);
                    this.letterValue = this.makeAttributeValueTemplate(letterValueAtt, att);
                    continue block28;
                }
                case "grouping-size": {
                    gsizeAtt = att;
                    continue block28;
                }
                case "grouping-separator": {
                    gsepAtt = att;
                    continue block28;
                }
                case "ordinal": {
                    ordinalAtt = attValue;
                    this.ordinal = this.makeAttributeValueTemplate(ordinalAtt, att);
                    continue block28;
                }
                case "start-at": {
                    startAtAtt = attValue;
                    this.startAt = this.makeAttributeValueTemplate(startAtAtt, att);
                    continue block28;
                }
            }
            this.checkUnknownAttribute(attName);
        }
        if (valueAtt != null) {
            if (selectAtt != null) {
                this.compileError("The select attribute and value attribute must not both be present", "XTSE0975");
            }
            if (countAtt != null) {
                this.compileError("The count attribute and value attribute must not both be present", "XTSE0975");
            }
            if (fromAtt != null) {
                this.compileError("The from attribute and value attribute must not both be present", "XTSE0975");
            }
            if (levelAtt != null) {
                this.compileError("The level attribute and value attribute must not both be present", "XTSE0975");
            }
        }
        if (countAtt != null) {
            this.count = this.makePattern(countAtt, "count");
        }
        if (fromAtt != null) {
            this.from = this.makePattern(fromAtt, "from");
        }
        if (levelAtt == null) {
            this.level = 0;
        } else if (levelAtt.equals("single")) {
            this.level = 0;
        } else if (levelAtt.equals("multiple")) {
            this.level = 1;
        } else if (levelAtt.equals("any")) {
            this.level = 2;
        } else {
            this.invalidAttribute("level", "single|any|multiple");
        }
        if (this.level == 0 && this.from == null && this.count == null) {
            this.level = 3;
        }
        if (formatAtt != null) {
            if (this.format instanceof StringLiteral) {
                this.formatter = new NumberFormatter();
                this.formatter.prepare(((StringLiteral)this.format).getStringValue());
            }
        } else {
            this.formatter = new NumberFormatter();
            this.formatter.prepare("1");
        }
        if (gsepAtt != null && gsizeAtt != null) {
            this.groupSize = this.makeAttributeValueTemplate(gsizeAtt.getValue(), gsizeAtt);
            this.groupSeparator = this.makeAttributeValueTemplate(gsepAtt.getValue(), gsepAtt);
        }
        if (startAtAtt != null) {
            if (startAtAtt.indexOf(123) < 0 && !startAtAtt.matches("-?[0-9]+(\\s+-?[0-9]+)*")) {
                this.compileErrorInAttribute("Invalid format for start-at attribute", "XTSE0020", "start-at");
            }
        } else {
            this.startAt = new StringLiteral("1");
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.checkEmpty();
        this.select = this.typeCheck("select", this.select);
        this.value = this.typeCheck("value", this.value);
        this.format = this.typeCheck("format", this.format);
        this.groupSize = this.typeCheck("group-size", this.groupSize);
        this.groupSeparator = this.typeCheck("group-separator", this.groupSeparator);
        this.letterValue = this.typeCheck("letter-value", this.letterValue);
        this.ordinal = this.typeCheck("ordinal", this.ordinal);
        this.lang = this.typeCheck("lang", this.lang);
        this.from = this.typeCheck("from", this.from);
        this.count = this.typeCheck("count", this.count);
        this.startAt = this.typeCheck("start-at", this.startAt);
        String errorCode = "XTTE1000";
        if (this.value == null && this.select == null) {
            errorCode = "XTTE0990";
            ContextItemExpression implicitSelect = new ContextItemExpression();
            implicitSelect.setLocation(this.allocateLocation());
            implicitSelect.setErrorCodeForUndefinedContext(errorCode, false);
            this.select = implicitSelect;
        }
        if (this.select != null) {
            try {
                RoleDiagnostic role = new RoleDiagnostic(4, "xsl:number/select", 0);
                role.setErrorCode(errorCode);
                this.select = this.getConfiguration().getTypeChecker(false).staticTypeCheck(this.select, SequenceType.SINGLE_NODE, role, this.makeExpressionVisitor());
            } catch (XPathException err) {
                this.compileError(err);
            }
        }
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        boolean valueSpecified;
        boolean bl = valueSpecified = this.value != null;
        if (this.value == null) {
            this.value = new NumberInstruction(this.select, this.level, this.count, this.from);
            this.value.setLocation(this.allocateLocation());
        }
        NumberSequenceFormatter numFormatter = new NumberSequenceFormatter(this.value, this.format, this.groupSize, this.groupSeparator, this.letterValue, this.ordinal, this.startAt, this.lang, this.formatter, this.xPath10ModeIsEnabled() && valueSpecified);
        numFormatter.setLocation(this.allocateLocation());
        ValueOf inst = new ValueOf(numFormatter, false, false);
        inst.setLocation(this.allocateLocation());
        inst.setIsNumberingInstruction();
        return inst;
    }
}


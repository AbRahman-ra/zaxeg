/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.HashSet;
import java.util.Set;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.accum.Accumulator;
import net.sf.saxon.expr.accum.AccumulatorRegistry;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.om.AllElementsSpaceStrippingRule;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.IgnorableSpaceStrippingRule;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Whitespace;

public class XSLSourceDocument
extends StyleElement {
    private Expression href = null;
    private Set<Accumulator> accumulators = new HashSet<Accumulator>();
    private boolean streaming = false;
    private ParseOptions parseOptions;

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    public boolean mayContainSequenceConstructor() {
        return true;
    }

    @Override
    protected boolean isWithinDeclaredStreamableConstruct() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        this.parseOptions = new ParseOptions(this.getConfiguration().getParseOptions());
        String hrefAtt = null;
        String validationAtt = null;
        String typeAtt = null;
        String useAccumulatorsAtt = null;
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            StructuredQName name = attName.getStructuredQName();
            String value = att.getValue();
            String f = name.getClarkName();
            if (f.equals("href")) {
                hrefAtt = value;
                this.href = this.makeAttributeValueTemplate(hrefAtt, att);
                continue;
            }
            if (f.equals("validation")) {
                validationAtt = Whitespace.trim(value);
                continue;
            }
            if (f.equals("type")) {
                typeAtt = Whitespace.trim(value);
                continue;
            }
            if (f.equals("use-accumulators")) {
                useAccumulatorsAtt = Whitespace.trim(value);
                continue;
            }
            if (f.equals("streamable")) {
                this.streaming = this.processStreamableAtt(value);
                continue;
            }
            if (attName.hasURI("http://saxon.sf.net/")) {
                String local;
                this.isExtensionAttributeAllowed(attName.getDisplayName());
                block8 : switch (local = attName.getLocalPart()) {
                    case "dtd-validation": {
                        this.parseOptions.setDTDValidationMode(this.processBooleanAttribute(f, value) ? 1 : 4);
                        break;
                    }
                    case "expand-attribute-defaults": {
                        this.parseOptions.setExpandAttributeDefaults(this.processBooleanAttribute(f, value));
                        break;
                    }
                    case "line-numbering": {
                        this.parseOptions.setLineNumbering(this.processBooleanAttribute(f, value));
                        break;
                    }
                    case "xinclude": {
                        this.parseOptions.setXIncludeAware(this.processBooleanAttribute(f, value));
                        break;
                    }
                    case "validation-params": {
                        break;
                    }
                    case "strip-space": {
                        switch (Whitespace.normalizeWhitespace(value).toString()) {
                            case "#all": {
                                this.parseOptions.setSpaceStrippingRule(AllElementsSpaceStrippingRule.getInstance());
                                break block8;
                            }
                            case "#none": {
                                this.parseOptions.setSpaceStrippingRule(NoElementsSpaceStrippingRule.getInstance());
                                break block8;
                            }
                            case "#ignorable": {
                                this.parseOptions.setSpaceStrippingRule(IgnorableSpaceStrippingRule.getInstance());
                                break block8;
                            }
                            case "#default": {
                                this.parseOptions.setSpaceStrippingRule(null);
                                break block8;
                            }
                        }
                        this.invalidAttribute("saxon:strip-space", "#all|#none|#ignorable|#default");
                        break;
                    }
                    default: {
                        this.checkUnknownAttribute(attName);
                    }
                }
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        if (hrefAtt == null) {
            this.reportAbsence("href");
        }
        if (validationAtt != null) {
            int validation = this.validateValidationAttribute(validationAtt);
            this.parseOptions.setSchemaValidationMode(validation);
        }
        if (typeAtt != null) {
            if (!this.isSchemaAware()) {
                this.compileError("The @type attribute is available only with a schema-aware XSLT processor", "XTSE1660");
            }
            this.parseOptions.setSchemaValidationMode(8);
            this.parseOptions.setTopLevelType(this.getSchemaType(typeAtt));
        }
        if (typeAtt != null && validationAtt != null) {
            this.compileError("The @validation and @type attributes are mutually exclusive", "XTSE1505");
        }
        if (useAccumulatorsAtt == null) {
            useAccumulatorsAtt = "";
        }
        AccumulatorRegistry registry = this.getPrincipalStylesheetModule().getStylesheetPackage().getAccumulatorRegistry();
        this.accumulators = registry.getUsedAccumulators(useAccumulatorsAtt, this);
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.href = this.typeCheck("select", this.href);
        if (!this.hasChildNodes()) {
            this.compileWarning("An empty xsl:source-document instruction has no effect", "SXWN9009");
        }
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        Configuration config = this.getConfiguration();
        if (this.parseOptions.getSpaceStrippingRule() == null) {
            this.parseOptions.setSpaceStrippingRule(this.getPackageData().getSpaceStrippingRule());
        }
        this.parseOptions.setApplicableAccumulators(this.accumulators);
        Expression action = this.compileSequenceConstructor(exec, decl, false);
        if (action == null) {
            return Literal.makeEmptySequence();
        }
        try {
            ExpressionVisitor visitor = this.makeExpressionVisitor();
            action = action.simplify();
            action = action.typeCheck(visitor, config.makeContextItemStaticInfo(NodeKindTest.DOCUMENT, false));
            return config.makeStreamInstruction(this.href, action, this.streaming, this.parseOptions, null, this.allocateLocation(), this.makeRetainedStaticContext());
        } catch (XPathException err) {
            this.compileError(err);
            return null;
        }
    }
}


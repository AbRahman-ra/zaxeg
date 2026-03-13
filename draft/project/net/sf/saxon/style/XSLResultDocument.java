/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import net.sf.saxon.expr.ErrorExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.instruct.ResultDocument;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.LiteralResultElement;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLFunction;
import net.sf.saxon.style.XSLGeneralVariable;
import net.sf.saxon.style.XSLOutput;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.Whitespace;

public class XSLResultDocument
extends StyleElement {
    public static final HashSet<String> fans = new HashSet(40);
    private Expression href;
    private StructuredQName formatQName;
    private Expression formatExpression;
    private int validationAction = 4;
    private SchemaType schemaType = null;
    private Map<StructuredQName, Expression> serializationAttributes = new HashMap<StructuredQName, Expression>(10);
    private boolean async = true;
    public static StructuredQName METHOD;
    public static StructuredQName BUILD_TREE;

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    public boolean mayContainSequenceConstructor() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        String formatAttribute = null;
        String hrefAttribute = null;
        String validationAtt = null;
        String typeAtt = null;
        String useCharacterMapsAtt = null;
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            StructuredQName name = attName.getStructuredQName();
            String value = att.getValue();
            String f = name.getClarkName();
            if (f.equals("format")) {
                formatAttribute = Whitespace.trim(value);
                this.formatExpression = this.makeAttributeValueTemplate(formatAttribute, att);
                continue;
            }
            if (f.equals("href")) {
                hrefAttribute = Whitespace.trim(value);
                this.href = this.makeAttributeValueTemplate(hrefAttribute, att);
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
            if (f.equals("use-character-maps")) {
                useCharacterMapsAtt = Whitespace.trim(value);
                continue;
            }
            if (fans.contains(f) || f.startsWith("{") && !"{http://saxon.sf.net/}asynchronous".equals(f)) {
                String val = value;
                if (!f.equals("item-separator") && !f.equals("{http://saxon.sf.net/}newline")) {
                    val = Whitespace.trim(value);
                }
                Expression exp = this.makeAttributeValueTemplate(val, att);
                this.serializationAttributes.put(name, exp);
                continue;
            }
            if (name.getLocalPart().equals("asynchronous") && name.hasURI("http://saxon.sf.net/")) {
                this.async = this.processBooleanAttribute("saxon:asynchronous", value);
                if (this.getCompilation().getCompilerInfo().isCompileWithTracing()) {
                    this.async = false;
                    continue;
                }
                if ("EE".equals(this.getConfiguration().getEditionCode())) continue;
                this.compileWarning("saxon:asynchronous - ignored when not running Saxon-EE", "SXWN9013");
                this.async = false;
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        if (formatAttribute != null) {
            if (this.formatExpression instanceof StringLiteral) {
                this.formatQName = this.makeQName(((StringLiteral)this.formatExpression).getStringValue(), "XTDE1460", "format");
                this.formatExpression = null;
            } else {
                this.getPrincipalStylesheetModule().setNeedsDynamicOutputProperties(true);
            }
        }
        this.validationAction = validationAtt == null ? this.getDefaultValidation() : this.validateValidationAttribute(validationAtt);
        if (typeAtt != null) {
            if (!this.isSchemaAware()) {
                this.compileError("The @type attribute is available only with a schema-aware XSLT processor", "XTSE1660");
            }
            this.schemaType = this.getSchemaType(typeAtt);
            this.validationAction = 8;
        }
        if (typeAtt != null && validationAtt != null) {
            this.compileError("The @validation and @type attributes are mutually exclusive", "XTSE1505");
        }
        if (useCharacterMapsAtt != null) {
            String s = XSLOutput.prepareCharacterMaps(this, useCharacterMapsAtt, new Properties());
            this.serializationAttributes.put(new StructuredQName("", "", "use-character-maps"), new StringLiteral(s));
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        if (this.href != null && !this.getConfiguration().getBooleanProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS)) {
            this.compileError("xsl:result-document is disabled when extension functions are disabled");
        }
        this.href = this.typeCheck("href", this.href);
        this.formatExpression = this.typeCheck("format", this.formatExpression);
        for (StructuredQName prop : this.serializationAttributes.keySet()) {
            Expression exp2;
            Expression exp1 = this.serializationAttributes.get(prop);
            if (exp1 == (exp2 = this.typeCheck(prop.getDisplayName(), exp1))) continue;
            this.serializationAttributes.put(prop, exp2);
        }
        this.getContainingPackage().setCreatesSecondaryResultDocuments(true);
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        AxisIterator kids;
        NodeInfo first;
        Properties globalProps;
        NodeInfo node;
        AxisIterator ai = this.iterateAxis(0);
        while ((node = ai.next()) != null) {
            if (!(node instanceof XSLGeneralVariable) && !(node instanceof XSLFunction)) continue;
            this.issueWarning("An xsl:result-document instruction inside " + node.getDisplayName() + " will always fail at run-time", this);
            return new ErrorExpression("Call to xsl:result-document while in temporary output state", "XTDE1480", false);
        }
        if (this.formatExpression == null) {
            try {
                globalProps = this.getPrincipalStylesheetModule().gatherOutputProperties(this.formatQName);
            } catch (XPathException err) {
                this.compileError("Named output format has not been defined", "XTDE1460");
                return null;
            }
        } else {
            globalProps = new Properties();
            this.getPrincipalStylesheetModule().setNeedsDynamicOutputProperties(true);
        }
        String method = null;
        if (this.formatExpression == null && globalProps.getProperty("method") == null && this.serializationAttributes.get(METHOD) == null && (first = (kids = this.iterateAxis(3)).next()) instanceof LiteralResultElement) {
            method = first.getURI().equals("http://www.w3.org/1999/xhtml") && first.getLocalPart().equals("html") ? (this.getEffectiveVersion() == 10 ? "xml" : "xhtml") : (first.getLocalPart().equalsIgnoreCase("html") && first.getURI().isEmpty() ? "html" : "xml");
            globalProps.setProperty("method", method);
        }
        Properties localProps = new Properties();
        HashSet<StructuredQName> fixed = new HashSet<StructuredQName>(10);
        NamespaceResolver namespaceResolver = this.getStaticContext().getNamespaceResolver();
        for (StructuredQName property : this.serializationAttributes.keySet()) {
            Expression exp = this.serializationAttributes.get(property);
            if (!(exp instanceof StringLiteral)) continue;
            String s = ((StringLiteral)exp).getStringValue();
            String lname = property.getLocalPart();
            String uri = property.getURI();
            try {
                ResultDocument.setSerializationProperty(localProps, uri, lname, s, namespaceResolver, false, exec.getConfiguration());
                fixed.add(property);
                if (!property.equals(METHOD)) continue;
                method = s;
            } catch (XPathException e) {
                if ("http://saxon.sf.net/".equals(e.getErrorCodeNamespace())) {
                    this.compileWarning(e.getMessage(), e.getErrorCodeQName());
                    continue;
                }
                e.setErrorCode("XTSE0020");
                this.compileError(e);
            }
        }
        for (StructuredQName p : fixed) {
            this.serializationAttributes.remove(p);
        }
        ResultDocument inst = new ResultDocument(globalProps, localProps, this.href, this.formatExpression, this.validationAction, this.schemaType, this.serializationAttributes, this.getContainingPackage().getCharacterMapIndex());
        Expression content = this.compileSequenceConstructor(exec, decl, true);
        if (content == null) {
            content = Literal.makeLiteral(EmptySequence.getInstance());
        }
        inst.setContentExpression(content);
        inst.setAsynchronous(this.async);
        return inst;
    }

    static {
        fans.add("allow-duplicate-names");
        fans.add("build-tree");
        fans.add("byte-order-mark");
        fans.add("cdata-section-elements");
        fans.add("doctype-public");
        fans.add("doctype-system");
        fans.add("encoding");
        fans.add("escape-uri-attributes");
        fans.add("html-version");
        fans.add("include-content-type");
        fans.add("indent");
        fans.add("item-separator");
        fans.add("json-node-output-method");
        fans.add("media-type");
        fans.add("method");
        fans.add("normalization-form");
        fans.add("omit-xml-declaration");
        fans.add("output-version");
        fans.add("parameter-document");
        fans.add("standalone");
        fans.add("suppress-indentation");
        fans.add("undeclare-prefixes");
        fans.add("{http://saxon.sf.net/}attribute-order");
        fans.add("{http://saxon.sf.net/}canonical");
        fans.add("{http://saxon.sf.net/}character-representation");
        fans.add("{http://saxon.sf.net/}double-space");
        fans.add("{http://saxon.sf.net/}indent-spaces");
        fans.add("{http://saxon.sf.net/}line-length");
        fans.add("{http://saxon.sf.net/}newline");
        fans.add("{http://saxon.sf.net/}next-in-chain");
        fans.add("{http://saxon.sf.net/}recognize-binary");
        fans.add("{http://saxon.sf.net/}require-well-formed");
        fans.add("{http://saxon.sf.net/}property-order");
        fans.add("{http://saxon.sf.net/}single-quotes");
        fans.add("{http://saxon.sf.net/}supply-source-locator");
        METHOD = new StructuredQName("", "", "method");
        BUILD_TREE = new StructuredQName("", "", "build-tree");
    }
}


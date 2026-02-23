/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import net.sf.saxon.functions.ResolveQName;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.lib.SaxonOutputKeys;
import net.sf.saxon.lib.SerializerFactory;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.QNameException;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLResultDocument;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Whitespace;

public class XSLOutput
extends StyleElement {
    private StructuredQName outputFormatName;
    private String method = null;
    private String outputVersion = null;
    private String useCharacterMaps = null;
    private Map<String, String> serializationAttributes = new HashMap<String, String>(10);
    private HashMap<String, String> userAttributes = null;

    @Override
    public boolean isDeclaration() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        String nameAtt = null;
        for (AttributeInfo att : this.attributes()) {
            String val;
            NodeName attName = att.getNodeName();
            String value = att.getValue();
            String f = attName.getStructuredQName().getClarkName();
            if (f.equals("name")) {
                nameAtt = Whitespace.trim(value);
                continue;
            }
            if (f.equals("version")) {
                String outputVersion = Whitespace.trim(value);
                this.serializationAttributes.put(f, outputVersion);
                continue;
            }
            if (f.equals("use-character-maps")) {
                this.useCharacterMaps = value;
                continue;
            }
            if (f.equals("parameter-document")) {
                val = Whitespace.trim(value);
                try {
                    val = ResolveURI.makeAbsolute(val, this.getBaseURI()).toASCIIString();
                } catch (URISyntaxException e) {
                    this.compileError(XPathException.makeXPathException(e));
                }
                this.serializationAttributes.put(f, val);
                continue;
            }
            if (XSLResultDocument.fans.contains(f) && !f.equals("output-version")) {
                val = value;
                if (!f.equals("item-separator") && !f.equals("{http://saxon.sf.net/}newline")) {
                    val = Whitespace.trim(val);
                }
                this.serializationAttributes.put(f, val);
                continue;
            }
            String attributeURI = attName.getURI();
            if ("".equals(attributeURI) || "http://www.w3.org/1999/XSL/Transform".equals(attributeURI) || "http://saxon.sf.net/".equals(attributeURI)) {
                this.checkUnknownAttribute(attName);
                continue;
            }
            String name = '{' + attributeURI + '}' + attName.getLocalPart();
            if (this.userAttributes == null) {
                this.userAttributes = new HashMap(5);
            }
            this.userAttributes.put(name, value);
        }
        if (nameAtt != null) {
            this.outputFormatName = this.makeQName(nameAtt, "XTSE1570", "name");
        }
    }

    public StructuredQName getFormatQName() {
        return this.outputFormatName;
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.checkTopLevel("XTSE0010", false);
        this.checkEmpty();
    }

    @Override
    public void compileDeclaration(Compilation compilation, ComponentDeclaration decl) {
    }

    @Override
    protected void processVersionAttribute(String ns) {
        this.version = ((StyleElement)this.getParent()).getEffectiveVersion();
    }

    protected void gatherOutputProperties(Properties details, HashMap<String, Integer> precedences, int thisPrecedence) throws XPathException {
        SerializerFactory sf = this.getConfiguration().getSerializerFactory();
        if (this.method != null) {
            if ("xml".equals(this.method) || "html".equals(this.method) || "text".equals(this.method) || "xhtml".equals(this.method) || "json".equals(this.method) || "adaptive".equals(this.method)) {
                this.checkAndPut(sf, "method", this.method, details, precedences, thisPrecedence);
            } else {
                try {
                    String[] parts = NameChecker.getQNameParts(this.method);
                    String prefix = parts[0];
                    if (prefix.isEmpty()) {
                        this.compileError("method must be xml, html, xhtml, text, json, adaptive, or a prefixed name", "XTSE1570");
                    } else {
                        String uri = this.getURIForPrefix(prefix, false);
                        if (uri == null) {
                            this.undeclaredNamespaceError(prefix, "XTSE0280", "method");
                        }
                        this.checkAndPut(sf, "method", '{' + uri + '}' + parts[1], details, precedences, thisPrecedence);
                    }
                } catch (QNameException e) {
                    this.compileError("Invalid method name. " + e.getMessage(), "XTSE1570");
                }
            }
        }
        for (Map.Entry<String, String> entry : this.serializationAttributes.entrySet()) {
            this.checkAndPut(sf, entry.getKey(), entry.getValue(), details, precedences, thisPrecedence);
        }
        if (this.serializationAttributes.containsKey("{http://saxon.sf.net/}next-in-chain")) {
            this.checkAndPut(sf, "{http://saxon.sf.net/}next-in-chain-base-uri", this.getSystemId(), details, precedences, thisPrecedence);
        }
        if (this.useCharacterMaps != null) {
            String s = XSLOutput.prepareCharacterMaps(this, this.useCharacterMaps, details);
            details.setProperty("use-character-maps", s);
        }
        if (this.userAttributes != null) {
            for (Map.Entry<String, String> e : this.userAttributes.entrySet()) {
                details.setProperty(e.getKey(), e.getValue());
            }
        }
    }

    private void checkAndPut(SerializerFactory sf, String property, String value, Properties props, HashMap<String, Integer> precedences, int thisPrecedence) {
        try {
            if (XSLOutput.isListOfNames(property)) {
                boolean useDefaultNS = !property.equals("{http://saxon.sf.net/}attribute-order");
                boolean allowStar = property.equals("{http://saxon.sf.net/}attribute-order");
                value = SaxonOutputKeys.parseListOfNodeNames(value, this, useDefaultNS, false, allowStar, "XTSE0280");
            }
            if (XSLOutput.isQName(property) && value.contains(":")) {
                value = ResolveQName.resolveQName(value, this).getEQName();
            }
            value = sf.checkOutputProperty(property, value);
        } catch (XPathException err) {
            String code;
            String string = code = property.equals("method") ? "XTSE1570" : "XTSE0020";
            if (property.contains("{")) {
                this.compileError(err.getMessage(), code);
            } else {
                this.compileErrorInAttribute(err.getMessage(), code, property);
            }
            return;
        }
        String old = props.getProperty(property);
        if (old == null) {
            props.setProperty(property, value);
            precedences.put(property, thisPrecedence);
        } else if (!old.equals(value)) {
            if (XSLOutput.isListOfNames(property)) {
                props.setProperty(property, old + " " + value);
                precedences.put(property, thisPrecedence);
            } else {
                Integer oldPrec = precedences.get(property);
                if (oldPrec == null) {
                    return;
                }
                if (oldPrec <= thisPrecedence) {
                    if (oldPrec == thisPrecedence) {
                        this.compileError("Conflicting values for output property " + property, "XTSE1560");
                    } else {
                        throw new IllegalStateException("Output properties must be processed in decreasing precedence order");
                    }
                }
            }
        }
    }

    private static boolean isListOfNames(String property) {
        return property.equals("cdata-section-elements") || property.equals("suppress-indentation") || property.equals("{http://saxon.sf.net/}attribute-order") || property.equals("{http://saxon.sf.net/}double-space");
    }

    private static boolean isQName(String property) {
        return property.equals("method") || property.equals("json-node-output-method");
    }

    public static String prepareCharacterMaps(StyleElement element, String useCharacterMaps, Properties details) {
        PrincipalStylesheetModule psm = element.getPrincipalStylesheetModule();
        String existing = details.getProperty("use-character-maps");
        if (existing == null) {
            existing = "";
        }
        StringBuilder s = new StringBuilder();
        StringTokenizer st = new StringTokenizer(useCharacterMaps, " \t\n\r", false);
        while (st.hasMoreTokens()) {
            String displayname = st.nextToken();
            StructuredQName qName = element.makeQName(displayname, null, "use-character-maps");
            ComponentDeclaration decl = psm.getCharacterMap(qName);
            if (decl == null) {
                element.compileErrorInAttribute("No character-map named '" + displayname + "' has been defined", "XTSE1590", "use-character-maps");
            }
            s.append(" ").append(qName.getClarkName());
        }
        existing = s + existing;
        return existing;
    }
}


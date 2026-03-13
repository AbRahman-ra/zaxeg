/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.SchemaURIResolver;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.trans.LicenseException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.linked.NodeImpl;
import net.sf.saxon.type.SchemaException;
import net.sf.saxon.value.Whitespace;

public class XSLImportSchema
extends StyleElement {
    @Override
    public boolean isDeclaration() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        String namespace = null;
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String value = att.getValue();
            String f = attName.getDisplayName();
            if (f.equals("schema-location")) continue;
            if (f.equals("namespace")) {
                namespace = Whitespace.trim(value);
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        if ("".equals(namespace)) {
            this.compileError("The zero-length string is not a valid namespace URI. For a schema with no namespace, omit the namespace attribute");
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.checkTopLevel("XTSE0010", false);
    }

    @Override
    public void index(ComponentDeclaration decl, PrincipalStylesheetModule top) throws XPathException {
    }

    public void readSchema() throws XPathException {
        try {
            String schemaLoc = Whitespace.trim(this.getAttributeValue("", "schema-location"));
            String namespace = Whitespace.trim(this.getAttributeValue("", "namespace"));
            namespace = namespace == null ? "" : namespace.trim();
            Configuration config = this.getConfiguration();
            try {
                config.checkLicensedFeature(2, "xsl:import-schema", this.getPackageData().getLocalLicenseId());
            } catch (LicenseException err) {
                XPathException xe = new XPathException(err);
                xe.setErrorCode("XTSE1650");
                xe.setLocator(this);
                throw xe;
            }
            NodeImpl inlineSchema = null;
            for (NodeImpl child : this.children()) {
                if (inlineSchema != null) {
                    this.compileError(this.getDisplayName() + " must not have more than one child element");
                }
                if ((inlineSchema = child).getFingerprint() != 617) {
                    this.compileError("The only child element permitted for " + this.getDisplayName() + " is xs:schema");
                }
                if (schemaLoc != null) {
                    this.compileError("The schema-location attribute must be absent if an inline schema is present", "XTSE0215");
                }
                if (namespace.isEmpty() && (namespace = inlineSchema.getAttributeValue("", "targetNamespace")) == null) {
                    namespace = "";
                }
                namespace = config.readInlineSchema(inlineSchema, namespace, this.getCompilation().getCompilerInfo().getErrorReporter());
                this.getPrincipalStylesheetModule().addImportedSchema(namespace);
            }
            if (inlineSchema != null) {
                return;
            }
            if (namespace.equals("http://www.w3.org/XML/1998/namespace") || namespace.equals("http://www.w3.org/2005/xpath-functions") || namespace.equals("http://www.w3.org/2001/XMLSchema-instance")) {
                config.addSchemaForBuiltInNamespace(namespace);
                this.getPrincipalStylesheetModule().addImportedSchema(namespace);
                return;
            }
            boolean namespaceKnown = config.isSchemaAvailable(namespace);
            if (schemaLoc == null && !namespaceKnown) {
                this.compileWarning("No schema for this namespace is known, and no schema-location was supplied, so no schema has been imported", "SXWN9006");
                return;
            }
            if (namespaceKnown && !config.getBooleanProperty(Feature.MULTIPLE_SCHEMA_IMPORTS) && schemaLoc != null) {
                this.compileWarning("The schema document at " + schemaLoc + " is ignored because a schema for this namespace is already loaded", "SXWN9006");
            }
            if (!namespaceKnown) {
                PipelineConfiguration pipe = config.makePipelineConfiguration();
                SchemaURIResolver schemaResolver = config.makeSchemaURIResolver(this.getCompilation().getCompilerInfo().getURIResolver());
                pipe.setSchemaURIResolver(schemaResolver);
                pipe.setErrorReporter(this.getCompilation().getCompilerInfo().getErrorReporter());
                namespace = config.readSchema(pipe, this.getBaseURI(), schemaLoc, namespace);
            }
            this.getPrincipalStylesheetModule().addImportedSchema(namespace);
        } catch (SchemaException err) {
            String errorCode = err.getErrorCodeLocalPart() == null ? "XTSE0220" : err.getErrorCodeLocalPart();
            this.compileError(err.getMessage(), errorCode);
        }
    }

    @Override
    public void compileDeclaration(Compilation compilation, ComponentDeclaration decl) throws XPathException {
    }
}


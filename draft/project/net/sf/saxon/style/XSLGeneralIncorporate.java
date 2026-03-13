/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import net.sf.saxon.Configuration;
import net.sf.saxon.functions.DocumentFn;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.DocumentKey;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.LiteralResultElement;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.StylesheetModule;
import net.sf.saxon.style.XSLImport;
import net.sf.saxon.style.XSLInclude;
import net.sf.saxon.style.XSLStylesheet;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.linked.DocumentImpl;
import net.sf.saxon.tree.linked.ElementImpl;
import net.sf.saxon.value.Whitespace;

public abstract class XSLGeneralIncorporate
extends StyleElement {
    private String href;
    private DocumentImpl targetDoc;

    @Override
    public boolean isDeclaration() {
        return true;
    }

    public abstract boolean isImport();

    @Override
    public void prepareAttributes() {
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            if (f.equals("href")) {
                this.href = Whitespace.trim(value);
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        if (this.href == null) {
            this.reportAbsence("href");
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.validateInstruction();
    }

    public void validateInstruction() {
        this.checkEmpty();
        this.checkTopLevel(this.isImport() ? "XTSE0190" : "XTSE0170", false);
    }

    public StylesheetModule getIncludedStylesheet(StylesheetModule importer, int precedence) {
        if (this.href == null) {
            return null;
        }
        try {
            StylesheetModule incModule;
            PrincipalStylesheetModule psm = importer.getPrincipalStylesheetModule();
            URIResolver resolver = this.getCompilation().getCompilerInfo().getURIResolver();
            Configuration config = this.getConfiguration();
            DocumentKey key = DocumentFn.computeDocumentKey(this.href, this.getBaseURI(), this.getCompilation().getPackageData(), resolver, false);
            XSLStylesheet includedSheet = (XSLStylesheet)psm.getStylesheetDocument(key);
            if (includedSheet != null) {
                incModule = new StylesheetModule(includedSheet, precedence);
                incModule.setImporter(importer);
                if (this.checkForRecursion(importer, incModule.getRootElement())) {
                    return null;
                }
            } else {
                DocumentImpl includedDoc = this.targetDoc;
                assert (includedDoc != null);
                ElementImpl outermost = includedDoc.getDocumentElement();
                if (outermost instanceof LiteralResultElement) {
                    includedDoc = ((LiteralResultElement)outermost).makeStylesheet(false);
                    outermost = includedDoc.getDocumentElement();
                }
                if (!(outermost instanceof XSLStylesheet)) {
                    String verb = this instanceof XSLImport ? "Imported" : "Included";
                    this.compileError(verb + " document " + this.href + " is not a stylesheet", "XTSE0165");
                    return null;
                }
                includedSheet = (XSLStylesheet)outermost;
                psm.putStylesheetDocument(key, includedSheet);
                incModule = new StylesheetModule(includedSheet, precedence);
                incModule.setImporter(importer);
                ComponentDeclaration decl = new ComponentDeclaration(incModule, includedSheet);
                includedSheet.validate(decl);
                if (includedSheet.validationError != null) {
                    if (this.reportingCircumstances == StyleElement.OnFailure.REPORT_ALWAYS) {
                        includedSheet.compileError(includedSheet.validationError);
                    } else if (includedSheet.reportingCircumstances == StyleElement.OnFailure.REPORT_UNLESS_FORWARDS_COMPATIBLE) {
                        includedSheet.compileError(includedSheet.validationError);
                    }
                }
            }
            incModule.spliceIncludes();
            importer.setInputTypeAnnotations(includedSheet.getInputTypeAnnotationsAttribute() | incModule.getInputTypeAnnotations());
            return incModule;
        } catch (XPathException err) {
            err.setErrorCode("XTSE0165");
            err.setIsStaticError(true);
            this.compileError(err);
            return null;
        }
    }

    public void setTargetDocument(DocumentImpl doc) {
        this.targetDoc = doc;
    }

    private boolean checkForRecursion(StylesheetModule importer, Source source) {
        if (source.getSystemId() != null) {
            for (StylesheetModule anc = importer; anc != null; anc = anc.getImporter()) {
                if (!DocumentKey.normalizeURI(source.getSystemId()).equals(DocumentKey.normalizeURI(anc.getRootElement().getSystemId()))) continue;
                this.compileError("A stylesheet cannot " + this.getLocalPart() + " itself", this instanceof XSLInclude ? "XTSE0180" : "XTSE0210");
                return true;
            }
        }
        return false;
    }

    @Override
    public void compileDeclaration(Compilation compilation, ComponentDeclaration decl) {
    }
}


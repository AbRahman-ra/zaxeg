/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingIncident;

public class AbsentExtensionElement
extends StyleElement {
    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    public boolean mayContainSequenceConstructor() {
        return true;
    }

    @Override
    public void processAllAttributes() throws XPathException {
        if (this.reportingCircumstances == StyleElement.OnFailure.IGNORED_INSTRUCTION) {
            return;
        }
        if (this.reportingCircumstances == StyleElement.OnFailure.REPORT_ALWAYS) {
            this.compileError(this.validationError);
        }
        if (!this.isTopLevel() || !this.forwardsCompatibleModeIsEnabled()) {
            super.processAllAttributes();
        }
    }

    @Override
    public void prepareAttributes() {
    }

    @Override
    public void validateSubtree(ComponentDeclaration decl, boolean excludeStylesheet) throws XPathException {
        if (!(this.reportingCircumstances == StyleElement.OnFailure.IGNORED_INSTRUCTION || this.isTopLevel() && this.forwardsCompatibleModeIsEnabled())) {
            super.validateSubtree(decl, excludeStylesheet);
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        if (this.isTopLevel() || this.reportingCircumstances == StyleElement.OnFailure.IGNORED_INSTRUCTION) {
            return null;
        }
        if (this.validationError == null) {
            this.validationError = new XmlProcessingIncident("Unknown instruction");
        }
        return this.fallbackProcessing(exec, decl, this);
    }
}


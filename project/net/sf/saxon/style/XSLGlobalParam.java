/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.EnumSet;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.instruct.GlobalParam;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.SourceBinding;
import net.sf.saxon.style.XSLGlobalVariable;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;

public class XSLGlobalParam
extends XSLGlobalVariable {
    Expression conversion = null;

    @Override
    protected EnumSet<SourceBinding.BindingProperty> getPermittedAttributes() {
        return EnumSet.of(SourceBinding.BindingProperty.REQUIRED, SourceBinding.BindingProperty.SELECT, SourceBinding.BindingProperty.AS, SourceBinding.BindingProperty.STATIC);
    }

    public XSLGlobalParam() {
        this.sourceBinding.setProperty(SourceBinding.BindingProperty.PARAM, true);
    }

    @Override
    public Visibility getVisibility() {
        String statik = this.getAttributeValue("static");
        if (statik == null) {
            return Visibility.PUBLIC;
        }
        boolean isStatic = this.processBooleanAttribute("static", statik);
        return isStatic ? Visibility.PRIVATE : Visibility.PUBLIC;
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        if (this.sourceBinding.hasProperty(SourceBinding.BindingProperty.REQUIRED)) {
            if (this.sourceBinding.getSelectExpression() != null) {
                this.compileError("The select attribute must be absent when required='yes'", "XTSE0010");
            }
            if (this.hasChildNodes()) {
                this.compileError("A parameter specifying required='yes' must have empty content", "XTSE0010");
            }
            Visibility vis = this.getVisibility();
            if (!this.sourceBinding.isStatic() && vis != Visibility.PUBLIC && vis != Visibility.FINAL && vis != Visibility.ABSTRACT) {
                this.compileError("The visibility of a required non-static parameter must be public, final, or abstract", "XTSE3370");
            }
        }
        super.validate(decl);
    }

    @Override
    public void compileDeclaration(Compilation compilation, ComponentDeclaration decl) throws XPathException {
        if (this.sourceBinding.isStatic()) {
            super.compileDeclaration(compilation, decl);
        } else if (!this.redundant) {
            this.sourceBinding.handleSequenceConstructor(compilation, decl);
            GlobalParam binding = (GlobalParam)this.compiledVariable;
            binding.setPackageData(this.getCompilation().getPackageData());
            binding.obtainDeclaringComponent(this);
            Expression select = this.sourceBinding.getSelectExpression();
            binding.setBody(select);
            binding.setVariableQName(this.sourceBinding.getVariableQName());
            this.initializeBinding(binding);
            if (select != null && compilation.getCompilerInfo().getCodeInjector() != null) {
                compilation.getCompilerInfo().getCodeInjector().process(binding);
            }
            binding.setRequiredType(this.getRequiredType());
            binding.setRequiredParam(this.sourceBinding.hasProperty(SourceBinding.BindingProperty.REQUIRED));
            binding.setImplicitlyRequiredParam(this.sourceBinding.hasProperty(SourceBinding.BindingProperty.IMPLICITLY_REQUIRED));
            this.sourceBinding.fixupBinding(binding);
            Component overridden = this.getOverriddenComponent();
            if (overridden != null) {
                this.checkCompatibility(overridden);
            }
        }
    }

    @Override
    public SequenceType getRequiredType() {
        SequenceType declaredType = this.sourceBinding.getDeclaredType();
        if (declaredType != null) {
            return declaredType;
        }
        return SequenceType.ANY_SEQUENCE;
    }

    public void insertBytecodeCandidate(Optimizer opt) {
    }
}


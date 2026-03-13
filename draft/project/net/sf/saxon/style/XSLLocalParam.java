/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.EnumSet;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SuppliedParameterReference;
import net.sf.saxon.expr.instruct.LocalParam;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.SourceBinding;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLFunction;
import net.sf.saxon.style.XSLGeneralVariable;
import net.sf.saxon.style.XSLTemplate;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.linked.NodeImpl;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Whitespace;

public class XSLLocalParam
extends XSLGeneralVariable {
    private EnumSet<SourceBinding.BindingProperty> permittedAttributes = EnumSet.of(SourceBinding.BindingProperty.TUNNEL, SourceBinding.BindingProperty.REQUIRED, SourceBinding.BindingProperty.SELECT, SourceBinding.BindingProperty.AS);
    Expression conversion = null;
    private int slotNumber = -9876;
    private LocalParam compiledParam;
    private boolean prepared = false;

    @Override
    public SourceBinding getBindingInformation(StructuredQName name) {
        if (name.equals(this.sourceBinding.getVariableQName())) {
            return this.sourceBinding;
        }
        return null;
    }

    public int getSlotNumber() {
        return this.slotNumber;
    }

    @Override
    public void prepareAttributes() {
        if (!this.prepared) {
            this.prepared = true;
            this.sourceBinding.setProperty(SourceBinding.BindingProperty.PARAM, true);
            if (this.getParent() instanceof XSLFunction) {
                this.permittedAttributes.remove((Object)SourceBinding.BindingProperty.SELECT);
                this.sourceBinding.setProperty(SourceBinding.BindingProperty.DISALLOWS_CONTENT, true);
            }
            this.sourceBinding.prepareAttributes(this.permittedAttributes);
            if (this.sourceBinding.hasProperty(SourceBinding.BindingProperty.TUNNEL) && !(this.getParent() instanceof XSLTemplate)) {
                this.compileError("For attribute 'tunnel' within an " + this.getParent().getDisplayName() + " parameter, the only permitted value is 'no'", "XTSE0020");
            }
            if (this.getParent() instanceof XSLFunction && this.getAttributeValue("", "required") != null && !this.sourceBinding.hasProperty(SourceBinding.BindingProperty.REQUIRED)) {
                this.compileError("For attribute 'required' within an " + this.getParent().getDisplayName() + " parameter, the only permitted value is 'yes'", "XTSE0020");
            }
        }
    }

    public void prepareTemplateSignatureAttributes() throws XPathException {
        if (!this.prepared) {
            this.sourceBinding.setProperty(SourceBinding.BindingProperty.PARAM, true);
            this.sourceBinding.prepareTemplateSignatureAttributes();
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        StructuredQName name = this.sourceBinding.getVariableQName();
        NodeImpl parent = this.getParent();
        if (!(parent instanceof StyleElement) || !((StyleElement)parent).mayContainParam()) {
            this.compileError("xsl:param must be immediately within a template, function or stylesheet", "XTSE0010");
        }
        if (this.hasChildNodes() && this.getParent() instanceof XSLFunction) {
            this.compileError("Function parameters cannot have a default value", "XTSE0760");
        }
        this.iterateAxis(11).forEachOrFail(node -> {
            if (node instanceof XSLLocalParam) {
                if (name.equals(((XSLLocalParam)node).sourceBinding.getVariableQName())) {
                    this.compileError("The name of the parameter is not unique", "XTSE0580");
                }
            } else if (node instanceof StyleElement && ((StyleElement)node).getFingerprint() != 144) {
                this.compileError("xsl:param must not be preceded by other instructions", "XTSE0010");
            } else if (!Whitespace.isWhite(node.getStringValueCS())) {
                this.compileError("xsl:param must not be preceded by text", "XTSE0010");
            }
        });
        SlotManager p = this.getContainingSlotManager();
        if (p == null) {
            this.compileError("Local variable must be declared within a template or function", "XTSE0010");
        } else {
            this.slotNumber = p.allocateSlotNumber(name);
        }
        if (this.sourceBinding.hasProperty(SourceBinding.BindingProperty.REQUIRED)) {
            if (this.sourceBinding.getSelectExpression() != null) {
                this.compileError("The select attribute must be omitted when required='yes'", "XTSE0010");
            }
            if (this.hasChildNodes()) {
                this.compileError("A parameter specifying required='yes' must have empty content", "XTSE0010");
            }
        }
        super.validate(decl);
    }

    public boolean isTunnelParam() {
        return this.sourceBinding.hasProperty(SourceBinding.BindingProperty.TUNNEL);
    }

    public boolean isRequiredParam() {
        return this.sourceBinding.hasProperty(SourceBinding.BindingProperty.REQUIRED);
    }

    @Override
    public void fixupReferences() throws XPathException {
        this.sourceBinding.fixupReferences(null);
        super.fixupReferences();
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        if (this.getParent() instanceof XSLFunction) {
            return null;
        }
        SequenceType declaredType = this.getRequiredType();
        StructuredQName name = this.sourceBinding.getVariableQName();
        int slot = this.getSlotNumber();
        if (declaredType != null) {
            SuppliedParameterReference pref = new SuppliedParameterReference(slot);
            pref.setRetainedStaticContext(this.makeRetainedStaticContext());
            pref.setLocation(this.allocateLocation());
            RoleDiagnostic role = new RoleDiagnostic(8, name.getDisplayName(), 0);
            role.setErrorCode("XTTE0590");
            this.conversion = exec.getConfiguration().getTypeChecker(false).staticTypeCheck(pref, declaredType, role, this.makeExpressionVisitor());
        }
        this.sourceBinding.handleSequenceConstructor(exec, decl);
        LocalParam binding = new LocalParam();
        binding.setSelectExpression(this.sourceBinding.getSelectExpression());
        binding.setConversion(this.conversion);
        binding.setVariableQName(name);
        binding.setSlotNumber(slot);
        binding.setRequiredType(this.getRequiredType());
        binding.setRequiredParam(this.sourceBinding.hasProperty(SourceBinding.BindingProperty.REQUIRED));
        binding.setImplicitlyRequiredParam(this.sourceBinding.hasProperty(SourceBinding.BindingProperty.IMPLICITLY_REQUIRED));
        binding.setTunnel(this.sourceBinding.hasProperty(SourceBinding.BindingProperty.TUNNEL));
        this.sourceBinding.fixupBinding(binding);
        this.compiledParam = binding;
        return this.compiledParam;
    }

    public LocalParam getCompiledParam() {
        return this.compiledParam;
    }

    public SequenceType getRequiredType() {
        SequenceType declaredType = this.sourceBinding.getDeclaredType();
        if (declaredType != null) {
            return declaredType;
        }
        return SequenceType.ANY_SEQUENCE;
    }
}


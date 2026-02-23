/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.EnumSet;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.instruct.WithParam;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.SourceBinding;
import net.sf.saxon.style.XSLGeneralVariable;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.value.SequenceType;

public class XSLWithParam
extends XSLGeneralVariable {
    private EnumSet<SourceBinding.BindingProperty> allowedAttributes = EnumSet.of(SourceBinding.BindingProperty.SELECT, SourceBinding.BindingProperty.AS, SourceBinding.BindingProperty.TUNNEL);

    @Override
    protected void prepareAttributes() {
        this.sourceBinding.prepareAttributes(this.allowedAttributes);
    }

    public boolean isTunnelParam() {
        return this.sourceBinding.hasProperty(SourceBinding.BindingProperty.TUNNEL);
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        NodeInfo prev;
        super.validate(decl);
        AxisIterator iter = this.iterateAxis(11);
        while ((prev = iter.next()) != null) {
            if (!(prev instanceof XSLWithParam) || !this.sourceBinding.getVariableQName().equals(((XSLWithParam)prev).sourceBinding.getVariableQName())) continue;
            this.compileError("Duplicate parameter name", "XTSE0670");
        }
    }

    public void checkAgainstRequiredType(SequenceType required) throws XPathException {
        this.sourceBinding.checkAgainstRequiredType(required);
    }

    public WithParam compileWithParam(Expression parent, Compilation exec, ComponentDeclaration decl) throws XPathException {
        this.sourceBinding.handleSequenceConstructor(exec, decl);
        WithParam inst = new WithParam();
        inst.setSelectExpression(parent, this.sourceBinding.getSelectExpression());
        inst.setVariableQName(this.sourceBinding.getVariableQName());
        inst.setRequiredType(this.sourceBinding.getInferredType(true));
        return inst;
    }
}


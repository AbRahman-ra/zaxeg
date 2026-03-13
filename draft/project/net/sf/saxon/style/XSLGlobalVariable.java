/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.EnumSet;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.instruct.Actor;
import net.sf.saxon.expr.instruct.GlobalParam;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.SourceBinding;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.StylesheetComponent;
import net.sf.saxon.style.XSLGlobalParam;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.SequenceType;

public class XSLGlobalVariable
extends StyleElement
implements StylesheetComponent {
    private SlotManager slotManager;
    protected SourceBinding sourceBinding = new SourceBinding(this);
    protected GlobalVariable compiledVariable = null;
    private int state = 0;
    protected boolean redundant = false;

    public SourceBinding getSourceBinding() {
        return this.sourceBinding;
    }

    public StructuredQName getVariableQName() {
        return this.sourceBinding.getVariableQName();
    }

    @Override
    public StructuredQName getObjectName() {
        return this.sourceBinding.getVariableQName();
    }

    @Override
    public boolean mayContainSequenceConstructor() {
        return true;
    }

    public boolean isGlobal() {
        return this.isTopLevel();
    }

    @Override
    public void postValidate() throws XPathException {
        this.sourceBinding.postValidate();
    }

    public GlobalVariable getCompiledVariable() {
        return this.compiledVariable;
    }

    public XSLGlobalVariable() {
        this.sourceBinding.setProperty(SourceBinding.BindingProperty.GLOBAL, true);
    }

    protected EnumSet<SourceBinding.BindingProperty> getPermittedAttributes() {
        return EnumSet.of(SourceBinding.BindingProperty.ASSIGNABLE, SourceBinding.BindingProperty.SELECT, SourceBinding.BindingProperty.AS, SourceBinding.BindingProperty.STATIC, SourceBinding.BindingProperty.VISIBILITY);
    }

    @Override
    public Actor getActor() throws XPathException {
        GlobalVariable gv = this.getCompiledVariable();
        if (gv == null) {
            gv = this instanceof XSLGlobalParam ? new GlobalParam() : new GlobalVariable();
            gv.setPackageData(this.getCompilation().getPackageData());
            gv.obtainDeclaringComponent(this);
            gv.setRequiredType(this.sourceBinding.getDeclaredType());
            gv.setDeclaredVisibility(this.getDeclaredVisibility());
            gv.setVariableQName(this.sourceBinding.getVariableQName());
            gv.setSystemId(this.getSystemId());
            gv.setLineNumber(this.getLineNumber());
            RetainedStaticContext rsc = this.makeRetainedStaticContext();
            gv.setRetainedStaticContext(rsc);
            if (gv.getBody() != null) {
                gv.getBody().setRetainedStaticContext(rsc);
            }
            this.compiledVariable = gv;
        }
        return gv;
    }

    @Override
    public SymbolicName getSymbolicName() {
        return new SymbolicName(206, this.getObjectName());
    }

    @Override
    public void checkCompatibility(Component component) {
        SequenceType st1 = this.getSourceBinding().getDeclaredType();
        if (st1 == null) {
            st1 = SequenceType.ANY_SEQUENCE;
        }
        GlobalVariable other = (GlobalVariable)component.getActor();
        TypeHierarchy th = component.getDeclaringPackage().getConfiguration().getTypeHierarchy();
        Affinity relation = th.sequenceTypeRelationship(st1, other.getRequiredType());
        if (relation != Affinity.SAME_TYPE) {
            this.compileError("The declared type of the overriding variable $" + this.getVariableQName().getDisplayName() + " is different from that of the overridden variable", "XTSE3070");
        }
    }

    @Override
    public SourceBinding getBindingInformation(StructuredQName name) {
        if (name.equals(this.sourceBinding.getVariableQName())) {
            return this.sourceBinding;
        }
        return null;
    }

    @Override
    public void prepareAttributes() {
        if (this.state == 2) {
            return;
        }
        if (this.state == 1) {
            this.compileError("Circular reference to variable", "XTDE0640");
        }
        this.state = 1;
        this.sourceBinding.prepareAttributes(this.getPermittedAttributes());
        this.state = 2;
    }

    @Override
    public void index(ComponentDeclaration decl, PrincipalStylesheetModule top) throws XPathException {
        top.indexVariableDeclaration(decl);
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.slotManager = this.getConfiguration().makeSlotManager();
        this.sourceBinding.validate();
    }

    public boolean isAssignable() {
        return this.sourceBinding.hasProperty(SourceBinding.BindingProperty.ASSIGNABLE);
    }

    @Override
    public boolean isDeclaration() {
        return true;
    }

    @Override
    public boolean isInstruction() {
        return false;
    }

    public SequenceType getRequiredType() {
        return this.sourceBinding.getInferredType(true);
    }

    @Override
    public void fixupReferences() throws XPathException {
        this.sourceBinding.fixupReferences(this.compiledVariable);
        super.fixupReferences();
    }

    @Override
    public void compileDeclaration(Compilation compilation, ComponentDeclaration decl) throws XPathException {
        if (!this.redundant) {
            this.sourceBinding.handleSequenceConstructor(compilation, decl);
            GlobalVariable inst = this.getCompiledVariable();
            if (inst == null) {
                inst = new GlobalVariable();
                inst.setPackageData(this.getCompilation().getPackageData());
                inst.obtainDeclaringComponent(this);
                inst.setVariableQName(this.sourceBinding.getVariableQName());
            }
            if (this.sourceBinding.isStatic()) {
                inst.setStatic(true);
                GroundedValue value = compilation.getStaticVariable(this.sourceBinding.getVariableQName());
                if (value == null) {
                    throw new AssertionError();
                }
                Literal select = Literal.makeLiteral(value);
                select.setRetainedStaticContext(this.makeRetainedStaticContext());
                inst.setBody(select);
            } else {
                Expression select = this.sourceBinding.getSelectExpression();
                inst.setBody(select);
                if (compilation.getCompilerInfo().getCodeInjector() != null) {
                    compilation.getCompilerInfo().getCodeInjector().process(inst);
                }
            }
            inst.setRetainedStaticContext(this.makeRetainedStaticContext());
            this.initializeBinding(inst);
            inst.setAssignable(this.isAssignable());
            inst.setRequiredType(this.getRequiredType());
            this.sourceBinding.fixupBinding(inst);
            this.compiledVariable = inst;
            Component overridden = this.getOverriddenComponent();
            if (overridden != null) {
                this.checkCompatibility(overridden);
            }
        }
    }

    protected void initializeBinding(GlobalVariable var) {
        Expression select = var.getBody();
        Expression exp2 = select;
        if (exp2 != null) {
            try {
                ExpressionVisitor visitor = this.makeExpressionVisitor();
                exp2 = select.simplify().typeCheck(visitor, this.getConfiguration().makeContextItemStaticInfo(Type.ITEM_TYPE, true));
            } catch (XPathException err) {
                this.compileError(err);
            }
            exp2 = XSLGlobalVariable.makeTraceInstruction(this, exp2);
            this.allocateLocalSlots(exp2);
        }
        if (this.slotManager != null && this.slotManager.getNumberOfVariables() > 0) {
            var.setContainsLocals(this.slotManager);
        }
        if (exp2 != select) {
            var.setBody(exp2);
        }
    }

    @Override
    public SlotManager getSlotManager() {
        return this.slotManager;
    }

    @Override
    public void optimize(ComponentDeclaration declaration) throws XPathException {
        if (!this.redundant && this.compiledVariable.getBody() != null) {
            Expression exp2 = this.compiledVariable.getBody();
            ExpressionVisitor visitor = this.makeExpressionVisitor();
            exp2 = ExpressionTool.optimizeComponentBody(exp2, this.getCompilation(), visitor, this.getConfiguration().makeContextItemStaticInfo(AnyItemType.getInstance(), true), false);
            this.allocateLocalSlots(exp2);
            if (this.slotManager != null && this.slotManager.getNumberOfVariables() > 0) {
                this.compiledVariable.setContainsLocals(this.slotManager);
            }
            if (exp2 != this.compiledVariable.getBody()) {
                this.compiledVariable.setBody(exp2);
            }
        }
    }

    public void setRedundant(boolean redundant) {
        this.redundant = redundant;
    }

    @Override
    public void generateByteCode(Optimizer opt) {
    }
}


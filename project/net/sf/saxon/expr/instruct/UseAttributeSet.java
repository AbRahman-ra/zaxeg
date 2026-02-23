/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.ComponentInvocation;
import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.AttributeSet;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;

public class UseAttributeSet
extends Instruction
implements ComponentInvocation,
ContextOriginator {
    private StructuredQName targetName;
    private AttributeSet target;
    private boolean isDeclaredStreamable;
    private int bindingSlot = -1;

    public UseAttributeSet(StructuredQName name, boolean streamable) {
        this.targetName = name;
        this.isDeclaredStreamable = streamable;
    }

    @Override
    public boolean isInstruction() {
        return false;
    }

    public static Expression makeUseAttributeSets(StructuredQName[] targets, StyleElement instruction) throws XPathException {
        List<UseAttributeSet> list = UseAttributeSet.makeUseAttributeSetInstructions(targets, instruction);
        return UseAttributeSet.makeCompositeExpression(list);
    }

    public static List<UseAttributeSet> makeUseAttributeSetInstructions(StructuredQName[] targets, StyleElement instruction) throws XPathException {
        ArrayList<UseAttributeSet> list = new ArrayList<UseAttributeSet>(targets.length);
        for (StructuredQName name : targets) {
            UseAttributeSet use = UseAttributeSet.makeUseAttributeSet(name, instruction);
            if (use == null) continue;
            list.add(use);
        }
        return list;
    }

    public static Expression makeCompositeExpression(List<UseAttributeSet> targets) {
        if (targets.size() == 0) {
            return Literal.makeEmptySequence();
        }
        if (targets.size() == 1) {
            return targets.get(0);
        }
        return new Block(targets.toArray(new Expression[0]));
    }

    private static UseAttributeSet makeUseAttributeSet(StructuredQName name, StyleElement instruction) throws XPathException {
        AttributeSet target;
        if (name.hasURI("http://www.w3.org/1999/XSL/Transform") && name.getLocalPart().equals("original")) {
            target = (AttributeSet)instruction.getXslOriginal(136);
        } else {
            Component invokee = instruction.getContainingPackage().getComponent(new SymbolicName(136, name));
            instruction.getPrincipalStylesheetModule().getAttributeSetDeclarations(name);
            if (invokee == null) {
                instruction.compileError("Unknown attribute set " + name.getEQName(), "XTSE0710");
                return null;
            }
            target = (AttributeSet)invokee.getActor();
        }
        UseAttributeSet invocation = new UseAttributeSet(name, target.isDeclaredStreamable());
        invocation.setTarget(target);
        invocation.setBindingSlot(-1);
        invocation.setRetainedStaticContext(instruction.makeRetainedStaticContext());
        return invocation;
    }

    public boolean isDeclaredStreamable() {
        return this.isDeclaredStreamable;
    }

    public void setTarget(AttributeSet target) {
        this.target = target;
    }

    @Override
    public void setBindingSlot(int slot) {
        this.bindingSlot = slot;
    }

    @Override
    public int getBindingSlot() {
        return this.bindingSlot;
    }

    @Override
    public SymbolicName getSymbolicName() {
        return new SymbolicName(136, this.targetName);
    }

    public AttributeSet getTargetAttributeSet() {
        return this.target;
    }

    @Override
    public Component getFixedTarget() {
        if (this.target != null && this.bindingSlot < 0) {
            return this.target.getDeclaringComponent();
        }
        return null;
    }

    @Override
    public Iterable<Operand> operands() {
        return Collections.emptyList();
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        return this;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        UseAttributeSet ua = new UseAttributeSet(this.targetName, this.isDeclaredStreamable);
        ua.setTarget(this.target);
        ua.setBindingSlot(this.bindingSlot);
        return ua;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        return this;
    }

    @Override
    public ItemType getItemType() {
        return NodeKindTest.ATTRIBUTE;
    }

    @Override
    public int getIntrinsicDependencies() {
        return 639;
    }

    public StructuredQName getTargetAttributeSetName() {
        return this.targetName;
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        Component target;
        if (this.bindingSlot < 0) {
            target = this.getFixedTarget();
        } else {
            target = context.getTargetComponent(this.bindingSlot);
            if (target.isHiddenAbstractComponent()) {
                XPathException err = new XPathException("Cannot expand an abstract attribute set (" + this.targetName.getDisplayName() + ") with no implementation", "XTDE3052");
                err.setLocation(this.getLocation());
                throw err;
            }
        }
        if (target == null) {
            throw new AssertionError((Object)("Failed to locate attribute set " + this.getTargetAttributeSetName().getEQName()));
        }
        AttributeSet as = (AttributeSet)target.getActor();
        XPathContextMajor c2 = context.newContext();
        c2.setCurrentComponent(target);
        c2.setOrigin(this);
        SlotManager sm = as.getStackFrameMap();
        if (sm == null) {
            sm = SlotManager.EMPTY;
        }
        c2.openStackFrame(sm);
        as.expand(output, c2);
        return null;
    }

    @Override
    public String getExpressionName() {
        return "useAS";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("useAS", this);
        out.emitAttribute("name", this.targetName);
        out.emitAttribute("bSlot", "" + this.getBindingSlot());
        if (this.isDeclaredStreamable()) {
            out.emitAttribute("flags", "s");
        }
        out.endElement();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UseAttributeSet)) {
            return false;
        }
        return this.targetName.equals(((UseAttributeSet)obj).targetName);
    }

    @Override
    public int computeHashCode() {
        return 0x86423719 ^ this.targetName.hashCode();
    }

    @Override
    public String getStreamerName() {
        return "UseAttributeSet";
    }
}


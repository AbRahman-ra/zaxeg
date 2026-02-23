/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.ComponentBinding;
import net.sf.saxon.expr.ComponentInvocation;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ExpressionOwner;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.style.XSLGlobalParam;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.VisibilityProvenance;
import net.sf.saxon.trans.XPathException;

public abstract class Actor
implements ExpressionOwner,
Location {
    protected Expression body;
    private String systemId;
    private int lineNumber;
    private SlotManager stackFrameMap;
    private PackageData packageData;
    private Component declaringComponent;
    private Visibility declaredVisibility;
    private RetainedStaticContext retainedStaticContext;

    public abstract SymbolicName getSymbolicName();

    public StructuredQName getComponentName() {
        return this.getSymbolicName().getComponentName();
    }

    public String getTracingTag() {
        return StandardNames.getLocalName(this.getSymbolicName().getComponentKind());
    }

    public void setPackageData(PackageData packageData) {
        this.packageData = packageData;
    }

    public PackageData getPackageData() {
        return this.packageData;
    }

    public Component makeDeclaringComponent(Visibility visibility, StylesheetPackage declaringPackage) {
        if (this.declaringComponent == null) {
            this.declaringComponent = Component.makeComponent(this, visibility, VisibilityProvenance.DEFAULTED, declaringPackage, declaringPackage);
        }
        return this.declaringComponent;
    }

    public Component obtainDeclaringComponent(StyleElement declaration) {
        if (this.declaringComponent == null) {
            StylesheetPackage declaringPackage = declaration.getContainingPackage();
            Visibility defaultVisibility = declaration instanceof XSLGlobalParam ? Visibility.PUBLIC : Visibility.PRIVATE;
            Visibility declaredVisibility = declaration.getDeclaredVisibility();
            Visibility actualVisibility = declaredVisibility == null ? defaultVisibility : declaredVisibility;
            VisibilityProvenance provenance = declaredVisibility == null ? VisibilityProvenance.DEFAULTED : VisibilityProvenance.EXPLICIT;
            this.declaringComponent = Component.makeComponent(this, actualVisibility, provenance, declaringPackage, declaringPackage);
        }
        return this.declaringComponent;
    }

    public Component getDeclaringComponent() {
        return this.declaringComponent;
    }

    public void setDeclaringComponent(Component comp) {
        this.declaringComponent = comp;
    }

    public void allocateAllBindingSlots(StylesheetPackage pack) {
        if (this.getBody() != null && this.getDeclaringComponent().getDeclaringPackage() == pack && this.packageData.isXSLT()) {
            Actor.allocateBindingSlotsRecursive(pack, this, this.getBody(), this.getDeclaringComponent().getComponentBindings());
        }
    }

    public static void allocateBindingSlotsRecursive(StylesheetPackage pack, Actor p, Expression exp, List<ComponentBinding> bindings) {
        if (exp instanceof ComponentInvocation) {
            p.processComponentReference(pack, (ComponentInvocation)((Object)exp), bindings);
        }
        for (Operand o : exp.operands()) {
            Actor.allocateBindingSlotsRecursive(pack, p, o.getChildExpression(), bindings);
        }
    }

    private void processComponentReference(StylesheetPackage pack, ComponentInvocation invocation, List<ComponentBinding> bindings) {
        SymbolicName name = invocation.getSymbolicName();
        if (name == null) {
            return;
        }
        Component target = pack.getComponent(name);
        if (target == null && name.getComponentName().hasURI("http://www.w3.org/1999/XSL/Transform") && name.getComponentName().getLocalPart().equals("original")) {
            target = pack.getOverriddenComponent(this.getSymbolicName());
        }
        if (target == null) {
            throw new AssertionError((Object)("Target of component reference " + name + " is undefined"));
        }
        if (invocation.getBindingSlot() >= 0) {
            throw new AssertionError((Object)("**** Component reference " + name + " is already bound"));
        }
        int slot = bindings.size();
        ComponentBinding cb = new ComponentBinding(name, target);
        bindings.add(cb);
        invocation.setBindingSlot(slot);
    }

    public void setBody(Expression body) {
        this.body = body;
        if (body != null) {
            body.setParentExpression(null);
        }
    }

    public final Expression getBody() {
        return this.body;
    }

    @Override
    public final Expression getChildExpression() {
        return this.getBody();
    }

    public void setStackFrameMap(SlotManager map) {
        this.stackFrameMap = map;
    }

    public SlotManager getStackFrameMap() {
        return this.stackFrameMap;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public Location getLocation() {
        return this;
    }

    @Override
    public int getLineNumber() {
        return this.lineNumber;
    }

    @Override
    public String getSystemId() {
        return this.systemId;
    }

    @Override
    public int getColumnNumber() {
        return -1;
    }

    @Override
    public String getPublicId() {
        return null;
    }

    @Override
    public Location saveLocation() {
        return this;
    }

    public void setRetainedStaticContext(RetainedStaticContext rsc) {
        this.retainedStaticContext = rsc;
    }

    public RetainedStaticContext getRetainedStaticContext() {
        return this.retainedStaticContext;
    }

    public Object getProperty(String name) {
        return null;
    }

    public void setDeclaredVisibility(Visibility visibility) {
        this.declaredVisibility = visibility;
    }

    public Visibility getDeclaredVisibility() {
        return this.declaredVisibility;
    }

    public Iterator<String> getProperties() {
        List list = Collections.emptyList();
        return list.iterator();
    }

    public abstract void export(ExpressionPresenter var1) throws XPathException;

    public boolean isExportable() {
        return true;
    }

    @Override
    public void setChildExpression(Expression expr) {
        this.setBody(expr);
    }
}


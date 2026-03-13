/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.sf.saxon.expr.ComponentBinding;
import net.sf.saxon.expr.instruct.Actor;
import net.sf.saxon.expr.instruct.AttributeSet;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.NamedTemplate;
import net.sf.saxon.om.Function;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.VisibilityProvenance;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;

public class Component {
    protected Actor actor;
    private Visibility visibility;
    private List<ComponentBinding> bindings = new ArrayList<ComponentBinding>();
    private StylesheetPackage containingPackage;
    private StylesheetPackage declaringPackage;
    private VisibilityProvenance provenance;
    private Component baseComponent;

    private Component() {
    }

    public static Component makeComponent(Actor actor, Visibility visibility, VisibilityProvenance provenance, StylesheetPackage containingPackage, StylesheetPackage declaringPackage) {
        Component c = actor instanceof Mode ? new M() : new Component();
        c.actor = actor;
        c.visibility = visibility;
        c.provenance = provenance;
        c.containingPackage = containingPackage;
        c.declaringPackage = declaringPackage;
        return c;
    }

    public List<ComponentBinding> getComponentBindings() {
        return this.bindings;
    }

    public void setComponentBindings(List<ComponentBinding> bindings) {
        this.bindings = bindings;
    }

    public void setVisibility(Visibility visibility, VisibilityProvenance provenance) {
        this.visibility = visibility;
        this.provenance = provenance;
    }

    public Visibility getVisibility() {
        return this.visibility;
    }

    public VisibilityProvenance getVisibilityProvenance() {
        return this.provenance;
    }

    public boolean isHiddenAbstractComponent() {
        return this.visibility == Visibility.HIDDEN && this.baseComponent != null && this.baseComponent.getVisibility() == Visibility.ABSTRACT;
    }

    public Actor getActor() {
        return this.actor;
    }

    public StylesheetPackage getDeclaringPackage() {
        return this.declaringPackage;
    }

    public StylesheetPackage getContainingPackage() {
        return this.containingPackage;
    }

    public Component getBaseComponent() {
        return this.baseComponent;
    }

    public void setBaseComponent(Component original) {
        this.baseComponent = original;
    }

    public void export(ExpressionPresenter out, Map<Component, Integer> componentIdMap, Map<StylesheetPackage, Integer> packageIdMap) throws XPathException {
        out.startElement("co");
        int id = this.obtainComponentId(this, componentIdMap);
        out.emitAttribute("id", "" + id);
        if (this.provenance != VisibilityProvenance.DEFAULTED) {
            out.emitAttribute("vis", this.getVisibility().toString());
        }
        String refs = this.listComponentReferences(componentIdMap);
        out.emitAttribute("binds", refs);
        if (this.baseComponent != null && this.getActor() == this.baseComponent.getActor()) {
            int baseId = this.obtainComponentId(this.baseComponent, componentIdMap);
            out.emitAttribute("base", "" + baseId);
            out.emitAttribute("dpack", packageIdMap.get(this.declaringPackage) + "");
        } else {
            this.actor.export(out);
        }
        out.endElement();
    }

    public String listComponentReferences(Map<Component, Integer> componentIdMap) {
        FastStringBuffer fsb = new FastStringBuffer(128);
        for (ComponentBinding ref : this.getComponentBindings()) {
            Component target = ref.getTarget();
            int targetId = this.obtainComponentId(target, componentIdMap);
            if (fsb.length() != 0) {
                fsb.append(" ");
            }
            fsb.append("" + targetId);
        }
        return fsb.toString();
    }

    private int obtainComponentId(Component component, Map<Component, Integer> componentIdMap) {
        Integer id = componentIdMap.get(component);
        if (id == null) {
            id = componentIdMap.size();
            componentIdMap.put(component, id);
        }
        return id;
    }

    public int getComponentKind() {
        if (this.actor instanceof NamedTemplate) {
            return 200;
        }
        if (this.actor instanceof GlobalVariable) {
            return 206;
        }
        if (this.actor instanceof Function) {
            return 158;
        }
        if (this.actor instanceof AttributeSet) {
            return 136;
        }
        if (this.actor instanceof Mode) {
            return 174;
        }
        return -1;
    }

    public static class M
    extends Component {
        @Override
        public Mode getActor() {
            return (Mode)super.getActor();
        }

        public void setActor(Mode m) {
            this.actor = m;
        }
    }
}


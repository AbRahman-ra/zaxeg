/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;

public class GlobalContextRequirement {
    private boolean mayBeOmitted = true;
    private boolean absentFocus;
    private boolean external;
    private List<ItemType> requiredItemTypes = new ArrayList<ItemType>();
    private Expression defaultValue = null;

    public ItemType getRequiredItemType() {
        if (this.requiredItemTypes.isEmpty()) {
            return AnyItemType.getInstance();
        }
        return this.requiredItemTypes.get(0);
    }

    public List<ItemType> getRequiredItemTypes() {
        return this.requiredItemTypes;
    }

    public void addRequiredItemType(ItemType requiredItemType) {
        this.requiredItemTypes.add(requiredItemType);
    }

    public Expression getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(Expression defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("glob");
        String use = this.isMayBeOmitted() ? (this.isAbsentFocus() ? "pro" : "opt") : "req";
        out.emitAttribute("use", use);
        if (!this.getRequiredItemType().equals(AnyItemType.getInstance())) {
            out.emitAttribute("type", this.getRequiredItemType().toExportString());
        }
        out.endElement();
    }

    public void setAbsentFocus(boolean absent) {
        this.absentFocus = absent;
    }

    public boolean isAbsentFocus() {
        return this.absentFocus;
    }

    public void setMayBeOmitted(boolean mayOmit) {
        this.mayBeOmitted = mayOmit;
    }

    public boolean isMayBeOmitted() {
        return this.mayBeOmitted;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    public boolean isExternal() {
        return this.external;
    }
}


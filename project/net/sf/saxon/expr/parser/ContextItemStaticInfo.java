/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.parser;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.UType;

public class ContextItemStaticInfo {
    private ItemType itemType;
    private boolean contextMaybeUndefined;
    private Expression contextSettingExpression;
    private boolean parentless;
    public static final ContextItemStaticInfo DEFAULT = new ContextItemStaticInfo(AnyItemType.getInstance(), true);
    public static final ContextItemStaticInfo ABSENT = new ContextItemStaticInfo(ErrorType.getInstance(), true);

    public ContextItemStaticInfo(ItemType itemType, boolean maybeUndefined) {
        this.itemType = itemType;
        this.contextMaybeUndefined = maybeUndefined;
    }

    public void setContextSettingExpression(Expression setter) {
        this.contextSettingExpression = setter;
    }

    public Expression getContextSettingExpression() {
        return this.contextSettingExpression;
    }

    public ItemType getItemType() {
        return this.itemType;
    }

    public UType getContextItemUType() {
        return this.itemType.getUType();
    }

    public boolean isPossiblyAbsent() {
        return this.contextMaybeUndefined;
    }

    public void setContextPostureStriding() {
    }

    public void setContextPostureGrounded() {
    }

    public boolean isStrictStreamabilityRules() {
        return false;
    }

    public void setParentless(boolean parentless) {
        this.parentless = parentless;
    }

    public boolean isParentless() {
        return this.parentless;
    }
}


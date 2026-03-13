/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.function.Predicate;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.flwor.TupleExpression;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.value.SequenceType;

public class OperandRole {
    public static final int SETS_NEW_FOCUS = 1;
    public static final int USES_NEW_FOCUS = 2;
    public static final int HIGHER_ORDER = 4;
    public static final int IN_CHOICE_GROUP = 8;
    public static final int CONSTRAINED_CLASS = 16;
    public static final int SINGLETON = 32;
    public static final int HAS_SPECIAL_FOCUS_RULES = 64;
    public static final OperandRole SAME_FOCUS_ACTION = new OperandRole(0, OperandUsage.TRANSMISSION, SequenceType.ANY_SEQUENCE);
    public static final OperandRole FOCUS_CONTROLLING_SELECT = new OperandRole(1, OperandUsage.INSPECTION, SequenceType.ANY_SEQUENCE);
    public static final OperandRole FOCUS_CONTROLLED_ACTION = new OperandRole(6, OperandUsage.TRANSMISSION, SequenceType.ANY_SEQUENCE);
    public static final OperandRole INSPECT = new OperandRole(0, OperandUsage.INSPECTION, SequenceType.ANY_SEQUENCE);
    public static final OperandRole ABSORB = new OperandRole(0, OperandUsage.ABSORPTION, SequenceType.ANY_SEQUENCE);
    public static final OperandRole REPEAT_INSPECT = new OperandRole(4, OperandUsage.INSPECTION, SequenceType.ANY_SEQUENCE);
    public static final OperandRole NAVIGATE = new OperandRole(0, OperandUsage.NAVIGATION, SequenceType.ANY_SEQUENCE);
    public static final OperandRole REPEAT_NAVIGATE = new OperandRole(4, OperandUsage.NAVIGATION, SequenceType.ANY_SEQUENCE);
    public static final OperandRole FLWOR_TUPLE_CONSTRAINED = new OperandRole(20, OperandUsage.NAVIGATION, SequenceType.ANY_SEQUENCE, expr -> expr instanceof TupleExpression);
    public static final OperandRole SINGLE_ATOMIC = new OperandRole(0, OperandUsage.ABSORPTION, SequenceType.SINGLE_ATOMIC);
    public static final OperandRole ATOMIC_SEQUENCE = new OperandRole(0, OperandUsage.ABSORPTION, SequenceType.ATOMIC_SEQUENCE);
    public static final OperandRole NEW_FOCUS_ATOMIC = new OperandRole(6, OperandUsage.ABSORPTION, SequenceType.ATOMIC_SEQUENCE);
    public static final OperandRole PATTERN = new OperandRole(22, OperandUsage.ABSORPTION, SequenceType.ATOMIC_SEQUENCE, expr -> expr instanceof Pattern);
    int properties;
    private OperandUsage usage;
    private SequenceType requiredType = SequenceType.ANY_SEQUENCE;
    private Predicate<Expression> constraint;

    public OperandRole(int properties, OperandUsage usage) {
        this.properties = properties;
        this.usage = usage;
    }

    public OperandRole(int properties, OperandUsage usage, SequenceType requiredType) {
        this.properties = properties;
        this.usage = usage;
        this.requiredType = requiredType;
    }

    public OperandRole(int properties, OperandUsage usage, SequenceType requiredType, Predicate<Expression> constraint) {
        this.properties = properties;
        this.usage = usage;
        this.requiredType = requiredType;
        this.constraint = constraint;
    }

    public boolean setsNewFocus() {
        return (this.properties & 1) != 0;
    }

    public boolean hasSameFocus() {
        return (this.properties & 0x42) == 0;
    }

    public boolean hasSpecialFocusRules() {
        return (this.properties & 0x40) != 0;
    }

    public boolean isHigherOrder() {
        return (this.properties & 4) != 0;
    }

    public boolean isEvaluatedRepeatedly() {
        return (this.properties & 4) != 0 && (this.properties & 0x20) == 0;
    }

    public boolean isConstrainedClass() {
        return (this.properties & 0x10) != 0;
    }

    public void setConstraint(Predicate<Expression> constraint) {
        this.constraint = constraint;
    }

    public Predicate<Expression> getConstraint() {
        return this.constraint;
    }

    public OperandUsage getUsage() {
        return this.usage;
    }

    public SequenceType getRequiredType() {
        return this.requiredType;
    }

    public boolean isInChoiceGroup() {
        return (this.properties & 8) != 0;
    }

    public static OperandUsage getTypeDeterminedUsage(ItemType type) {
        if (type instanceof FunctionItemType) {
            return OperandUsage.INSPECTION;
        }
        if (type instanceof PlainType) {
            return OperandUsage.ABSORPTION;
        }
        return OperandUsage.NAVIGATION;
    }

    public OperandRole modifyProperty(int property, boolean on) {
        int newProp = on ? this.properties | property : this.properties & ~property;
        return new OperandRole(newProp, this.usage, this.requiredType);
    }

    public int getProperties() {
        return this.properties;
    }
}


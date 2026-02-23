/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.Iterator;
import net.sf.saxon.expr.ErrorExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ExpressionOwner;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.tree.jiter.MonoIterator;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.SequenceType;

public final class Operand
implements Iterable<Operand>,
ExpressionOwner {
    private final Expression parentExpression;
    private Expression childExpression;
    private OperandRole role;
    private static final boolean DEBUG = false;

    public Operand(Expression parentExpression, Expression childExpression, OperandRole role) {
        this.parentExpression = parentExpression;
        this.role = role;
        this.setChildExpression(childExpression);
    }

    public Expression getParentExpression() {
        return this.parentExpression;
    }

    @Override
    public Expression getChildExpression() {
        return this.childExpression;
    }

    @Override
    public void setChildExpression(Expression childExpression) {
        if (childExpression != this.childExpression) {
            if (this.role.isConstrainedClass() && (this.role.getConstraint() != null ? !this.role.getConstraint().test(childExpression) : this.childExpression != null && childExpression.getClass() != this.childExpression.getClass())) {
                throw new AssertionError();
            }
            this.childExpression = childExpression;
            this.parentExpression.adoptChildExpression(childExpression);
            this.parentExpression.resetLocalStaticProperties();
        }
    }

    public void detachChild() {
    }

    public OperandRole getOperandRole() {
        return this.role;
    }

    public void setOperandRole(OperandRole role) {
        this.role = role;
    }

    public boolean setsNewFocus() {
        return this.role.setsNewFocus();
    }

    public boolean hasSpecialFocusRules() {
        return this.role.hasSpecialFocusRules();
    }

    public boolean hasSameFocus() {
        return this.role.hasSameFocus();
    }

    public boolean isHigherOrder() {
        return this.role.isHigherOrder();
    }

    public boolean isEvaluatedRepeatedly() {
        return this.role.isEvaluatedRepeatedly();
    }

    public OperandUsage getUsage() {
        return this.role.getUsage();
    }

    public void setUsage(OperandUsage usage) {
        this.role = new OperandRole(this.role.properties, usage, this.role.getRequiredType());
    }

    public SequenceType getRequiredType() {
        return this.role.getRequiredType();
    }

    public boolean isInChoiceGroup() {
        return this.role.isInChoiceGroup();
    }

    @Override
    public Iterator<Operand> iterator() {
        return new MonoIterator<Operand>(this);
    }

    public void typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        try {
            this.setChildExpression(this.getChildExpression().typeCheck(visitor, contextInfo));
        } catch (XPathException e) {
            e.maybeSetLocation(this.getChildExpression().getLocation());
            if (!e.isReportableStatically()) {
                visitor.getStaticContext().issueWarning("Evaluation will always throw a dynamic error: " + e.getMessage(), this.getChildExpression().getLocation());
                this.setChildExpression(new ErrorExpression(new XmlProcessingException(e)));
            }
            throw e;
        }
    }

    public void optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        try {
            this.setChildExpression(this.getChildExpression().optimize(visitor, contextInfo));
        } catch (XPathException e) {
            e.maybeSetLocation(this.getChildExpression().getLocation());
            if (!e.isReportableStatically()) {
                visitor.getStaticContext().issueWarning("Evaluation will always throw a dynamic error: " + e.getMessage(), this.getChildExpression().getLocation());
                this.setChildExpression(new ErrorExpression(new XmlProcessingException(e)));
            }
            throw e;
        }
    }

    public static OperandUsage typeDeterminedUsage(ItemType type) {
        if (type.isPlainType()) {
            return OperandUsage.ABSORPTION;
        }
        if (type instanceof NodeTest || type == AnyItemType.getInstance()) {
            return OperandUsage.NAVIGATION;
        }
        return OperandUsage.INSPECTION;
    }
}


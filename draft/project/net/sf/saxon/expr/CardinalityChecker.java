/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.TypeCheckingFilter;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.CardinalityCheckingIterator;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemChecker;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.SingletonAtomizer;
import net.sf.saxon.expr.UnaryExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.pattern.DocumentNodeTest;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ArrayIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.IntegerValue;

public final class CardinalityChecker
extends UnaryExpression {
    private int requiredCardinality = -1;
    private RoleDiagnostic role;

    private CardinalityChecker(Expression sequence, int cardinality, RoleDiagnostic role) {
        super(sequence);
        this.requiredCardinality = cardinality;
        this.role = role;
    }

    public static Expression makeCardinalityChecker(Expression sequence, int cardinality, RoleDiagnostic role) {
        UnaryExpression result;
        if (sequence instanceof Literal && Cardinality.subsumes(cardinality, SequenceTool.getCardinality(((Literal)sequence).getValue()))) {
            return sequence;
        }
        if (sequence instanceof Atomizer && !Cardinality.allowsMany(cardinality)) {
            Expression base = ((Atomizer)sequence).getBaseExpression();
            result = new SingletonAtomizer(base, role, Cardinality.allowsZero(cardinality));
        } else {
            result = new CardinalityChecker(sequence, cardinality, role);
        }
        ExpressionTool.copyLocationInfo(sequence, result);
        return result;
    }

    @Override
    protected OperandRole getOperandRole() {
        return OperandRole.SAME_FOCUS_ACTION;
    }

    public int getRequiredCardinality() {
        return this.requiredCardinality;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getOperand().typeCheck(visitor, contextInfo);
        Expression base = this.getBaseExpression();
        if (this.requiredCardinality == 57344 || Cardinality.subsumes(this.requiredCardinality, base.getCardinality())) {
            return base;
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getOperand().optimize(visitor, contextInfo);
        Expression base = this.getBaseExpression();
        if (this.requiredCardinality == 57344 || Cardinality.subsumes(this.requiredCardinality, base.getCardinality())) {
            return base;
        }
        if ((base.getCardinality() & this.requiredCardinality) == 0) {
            XPathException err = new XPathException("The " + this.role.getMessage() + " does not satisfy the cardinality constraints", this.role.getErrorCode());
            err.setLocation(this.getLocation());
            err.setIsTypeError(this.role.isTypeError());
            throw err;
        }
        if (base instanceof ItemChecker) {
            ItemChecker checker = (ItemChecker)base;
            Expression other = checker.getBaseExpression();
            this.setBaseExpression(other);
            checker.setBaseExpression(this);
            checker.setParentExpression(null);
            return checker;
        }
        return this;
    }

    public void setErrorCode(String code) {
        this.role.setErrorCode(code);
    }

    public RoleDiagnostic getRoleLocator() {
        return this.role;
    }

    @Override
    public int getImplementationMethod() {
        int m = 22;
        if (!Cardinality.allowsMany(this.requiredCardinality)) {
            m |= 1;
        }
        return m;
    }

    @Override
    public IntegerValue[] getIntegerBounds() {
        return this.getBaseExpression().getIntegerBounds();
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        SequenceIterator base = this.getBaseExpression().iterate(context);
        if (base.getProperties().contains((Object)SequenceIterator.Property.LAST_POSITION_FINDER)) {
            int count = ((LastPositionFinder)((Object)base)).getLength();
            if (count == 0 && !Cardinality.allowsZero(this.requiredCardinality)) {
                this.typeError("An empty sequence is not allowed as the " + this.role.getMessage(), this.role.getErrorCode(), context);
            } else if (count == 1 && this.requiredCardinality == 8192) {
                this.typeError("The only value allowed for the " + this.role.getMessage() + " is an empty sequence", this.role.getErrorCode(), context);
            } else if (count > 1 && !Cardinality.allowsMany(this.requiredCardinality)) {
                this.typeError("A sequence of more than one item is not allowed as the " + this.role.getMessage() + CardinalityChecker.depictSequenceStart(base, 2), this.role.getErrorCode(), context);
            }
            return base;
        }
        return new CardinalityCheckingIterator(base, this.requiredCardinality, this.role, this.getLocation());
    }

    public static String depictSequenceStart(SequenceIterator seq, int max) {
        try {
            Item next;
            FastStringBuffer sb = new FastStringBuffer(64);
            int count = 0;
            sb.append(" (");
            while ((next = seq.next()) != null) {
                if (count++ > 0) {
                    sb.append(", ");
                }
                if (count > max) {
                    sb.append("...) ");
                    return sb.toString();
                }
                sb.cat(Err.depict(next));
            }
            sb.append(") ");
            return sb.toString();
        } catch (XPathException e) {
            return "";
        }
    }

    @Override
    public Item evaluateItem(XPathContext context) throws XPathException {
        SequenceIterator iter = this.getBaseExpression().iterate(context);
        Item first = iter.next();
        if (first == null) {
            if (!Cardinality.allowsZero(this.requiredCardinality)) {
                this.typeError("An empty sequence is not allowed as the " + this.role.getMessage(), this.role.getErrorCode(), context);
            }
            return null;
        }
        if (this.requiredCardinality == 8192) {
            this.typeError("An empty sequence is required as the " + this.role.getMessage(), this.role.getErrorCode(), context);
            return null;
        }
        Item second = iter.next();
        if (second != null) {
            Item[] leaders = new Item[]{first, second};
            this.typeError("A sequence of more than one item is not allowed as the " + this.role.getMessage() + CardinalityChecker.depictSequenceStart(new ArrayIterator(leaders), 2), this.role.getErrorCode(), context);
            return null;
        }
        return first;
    }

    @Override
    public void process(Outputter output, XPathContext context) throws XPathException {
        Expression next = this.getBaseExpression();
        ItemType type = Type.ITEM_TYPE;
        if (next instanceof ItemChecker) {
            type = ((ItemChecker)next).getRequiredType();
            next = ((ItemChecker)next).getBaseExpression();
        }
        if ((next.getImplementationMethod() & 4) != 0 && !(type instanceof DocumentNodeTest)) {
            TypeCheckingFilter filter = new TypeCheckingFilter(output);
            filter.setRequiredType(type, this.requiredCardinality, this.role, this.getLocation());
            next.process(filter, context);
            try {
                filter.finalCheck();
            } catch (XPathException e) {
                e.maybeSetLocation(this.getLocation());
                throw e;
            }
        } else {
            super.process(output, context);
        }
    }

    @Override
    public ItemType getItemType() {
        return this.getBaseExpression().getItemType();
    }

    @Override
    public int computeCardinality() {
        return this.requiredCardinality;
    }

    @Override
    public int computeSpecialProperties() {
        return this.getBaseExpression().getSpecialProperties();
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        CardinalityChecker c2 = new CardinalityChecker(this.getBaseExpression().copy(rebindings), this.requiredCardinality, this.role);
        ExpressionTool.copyLocationInfo(this, c2);
        return c2;
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) && this.requiredCardinality == ((CardinalityChecker)other).requiredCardinality;
    }

    @Override
    public int computeHashCode() {
        return super.computeHashCode() ^ this.requiredCardinality;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("check", this);
        String occ = Cardinality.getOccurrenceIndicator(this.requiredCardinality);
        if (occ.equals("")) {
            occ = "1";
        }
        out.emitAttribute("card", occ);
        out.emitAttribute("diag", this.role.save());
        this.getBaseExpression().export(out);
        out.endElement();
    }

    @Override
    public String toString() {
        Expression operand = this.getBaseExpression();
        switch (this.requiredCardinality) {
            case 16384: {
                return "exactly-one(" + operand + ")";
            }
            case 24576: {
                return "zero-or-one(" + operand + ")";
            }
            case 49152: {
                return "one-or-more(" + operand + ")";
            }
            case 8192: {
                return "must-be-empty(" + operand + ")";
            }
        }
        return "check(" + operand + ")";
    }

    @Override
    public String getExpressionName() {
        return "CheckCardinality";
    }

    @Override
    public String toShortString() {
        return this.getBaseExpression().toShortString();
    }

    @Override
    public String getStreamerName() {
        return "CardinalityChecker";
    }

    @Override
    public void setLocation(Location id) {
        super.setLocation(id);
    }
}


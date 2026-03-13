/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.ArrayList;
import java.util.Iterator;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.LookupExpression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.UnaryExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.ma.arrays.SquareArrayConstructor;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.ma.map.TupleType;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;

public class LookupAllExpression
extends UnaryExpression {
    public LookupAllExpression(Expression base) {
        super(base);
    }

    @Override
    protected OperandRole getOperandRole() {
        return OperandRole.INSPECT;
    }

    @Override
    public final ItemType getItemType() {
        ItemType base = this.getBaseExpression().getItemType();
        if (base instanceof MapType) {
            return ((MapType)base).getValueType().getPrimaryType();
        }
        if (base instanceof ArrayItemType) {
            return ((ArrayItemType)base).getMemberType().getPrimaryType();
        }
        return AnyItemType.getInstance();
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        return this.getItemType().getUType();
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        boolean isMapLookup;
        Configuration config = visitor.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();
        this.getOperand().typeCheck(visitor, contextInfo);
        ItemType containerType = this.getBaseExpression().getItemType();
        boolean isArrayLookup = containerType instanceof ArrayItemType;
        boolean bl = isMapLookup = containerType instanceof MapType || containerType instanceof TupleType;
        if (!isArrayLookup && !isMapLookup && th.relationship(containerType, MapType.ANY_MAP_TYPE) == Affinity.DISJOINT && th.relationship(containerType, ArrayItemType.getInstance()) == Affinity.DISJOINT) {
            XPathException err = new XPathException("The left-hand operand of '?' must be a map or an array; the supplied expression is of type " + containerType, "XPTY0004");
            err.setLocation(this.getLocation());
            err.setIsTypeError(true);
            err.setFailingExpression(this);
            throw err;
        }
        if (this.getBaseExpression() instanceof Literal) {
            return new Literal(this.iterate(visitor.makeDynamicContext()).materialize());
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        this.getOperand().optimize(visitor, contextItemType);
        if (this.getBaseExpression() instanceof Literal) {
            return new Literal(this.iterate(visitor.makeDynamicContext()).materialize());
        }
        if (this.getBaseExpression() instanceof SquareArrayConstructor) {
            ArrayList<Expression> children = new ArrayList<Expression>();
            for (Operand o : this.getBaseExpression().operands()) {
                children.add(o.getChildExpression().copy(new RebindingMap()));
            }
            Expression[] childExpressions = children.toArray(new Expression[0]);
            Block block = new Block(childExpressions);
            ExpressionTool.copyLocationInfo(this, block);
            return block;
        }
        return this;
    }

    @Override
    public double getCost() {
        return this.getBaseExpression().getCost() + 1.0;
    }

    @Override
    public int getImplementationMethod() {
        return 2;
    }

    @Override
    public LookupAllExpression copy(RebindingMap rebindings) {
        return new LookupAllExpression(this.getBaseExpression().copy(rebindings));
    }

    @Override
    public int computeCardinality() {
        return 57344;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof LookupAllExpression)) {
            return false;
        }
        LookupAllExpression p = (LookupAllExpression)other;
        return this.getBaseExpression().isEqual(p.getBaseExpression());
    }

    @Override
    public int computeHashCode() {
        return "LookupAll".hashCode() ^ this.getBaseExpression().hashCode();
    }

    @Override
    public SequenceIterator iterate(final XPathContext context) throws XPathException {
        return new SequenceIterator(){
            final SequenceIterator level0;
            Iterator<?> level1;
            SequenceIterator level2;
            {
                this.level0 = LookupAllExpression.this.getBaseExpression().iterate(context);
                this.level1 = null;
                this.level2 = null;
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            @Override
            public Item next() throws XPathException {
                if (this.level2 == null) {
                    if (this.level1 == null) {
                        Item base = this.level0.next();
                        if (base == null) {
                            return null;
                        }
                        if (base instanceof ArrayItem) {
                            this.level1 = ((ArrayItem)base).members().iterator();
                            return this.next();
                        }
                        if (base instanceof MapItem) {
                            this.level1 = ((MapItem)base).keyValuePairs().iterator();
                            return this.next();
                        }
                        LookupExpression.mustBeArrayOrMap(LookupAllExpression.this, base);
                        return null;
                    }
                    if (this.level1.hasNext()) {
                        Object nextEntry = this.level1.next();
                        if (nextEntry instanceof KeyValuePair) {
                            GroundedValue value = ((KeyValuePair)nextEntry).value;
                            this.level2 = value.iterate();
                            return this.next();
                        } else {
                            if (!(nextEntry instanceof GroundedValue)) throw new IllegalStateException();
                            this.level2 = ((GroundedValue)nextEntry).iterate();
                        }
                        return this.next();
                    } else {
                        this.level1 = null;
                    }
                    return this.next();
                }
                Item next = this.level2.next();
                if (next != null) return next;
                this.level2 = null;
                return this.next();
            }

            @Override
            public void close() {
                if (this.level0 != null) {
                    this.level0.close();
                }
                if (this.level2 != null) {
                    this.level2.close();
                }
            }
        };
    }

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        destination.startElement("lookupAll", this);
        this.getBaseExpression().export(destination);
        destination.endElement();
    }

    @Override
    public String toString() {
        return ExpressionTool.parenthesize(this.getBaseExpression()) + "?*";
    }

    @Override
    public String toShortString() {
        return this.getBaseExpression().toShortString() + "?*";
    }
}


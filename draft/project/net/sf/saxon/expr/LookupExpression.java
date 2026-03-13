/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.BinaryExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.MappingFunction;
import net.sf.saxon.expr.MappingIterator;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.ma.arrays.ArrayFunctionSet;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.ma.map.TupleItemType;
import net.sf.saxon.ma.map.TupleType;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.UnfailingIterator;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyExternalObjectType;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class LookupExpression
extends BinaryExpression {
    private boolean isClassified = false;
    protected boolean isArrayLookup = false;
    protected boolean isMapLookup = false;
    protected boolean isSingleContainer = false;
    protected boolean isSingleEntry = false;

    public LookupExpression(Expression start, Expression step) {
        super(start, 213, step);
    }

    @Override
    protected OperandRole getOperandRole(int arg) {
        return arg == 0 ? OperandRole.INSPECT : OperandRole.ABSORB;
    }

    @Override
    public String getExpressionName() {
        return "lookupExp";
    }

    @Override
    public ItemType getItemType() {
        if (this.isClassified) {
            if (this.isArrayLookup) {
                ItemType arrayType = this.getLhsExpression().getItemType();
                if (arrayType instanceof ArrayItemType) {
                    return ((ArrayItemType)arrayType).getMemberType().getPrimaryType();
                }
            } else if (this.isMapLookup) {
                ItemType mapType = this.getLhsExpression().getItemType();
                if (mapType instanceof TupleItemType && this.getRhsExpression() instanceof StringLiteral) {
                    String fieldName = ((StringLiteral)this.getRhsExpression()).getStringValue();
                    SequenceType fieldType = ((TupleItemType)mapType).getFieldType(fieldName);
                    if (fieldType == null) {
                        return ((TupleItemType)mapType).isExtensible() ? AnyItemType.getInstance() : ErrorType.getInstance();
                    }
                    return fieldType.getPrimaryType();
                }
                if (mapType instanceof MapType) {
                    return ((MapType)mapType).getValueType().getPrimaryType();
                }
            }
        }
        return AnyItemType.getInstance();
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        return this.getItemType().getUType();
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        String fieldName;
        TupleType tt;
        Configuration config = visitor.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();
        ItemType originalType = this.getLhsExpression().getItemType();
        this.getLhs().typeCheck(visitor, contextInfo);
        ItemType containerType = this.getLhsExpression().getItemType();
        this.isArrayLookup = containerType instanceof ArrayItemType;
        boolean isTupleLookup = containerType instanceof TupleType || originalType instanceof TupleType;
        boolean bl = this.isMapLookup = containerType instanceof MapType || isTupleLookup;
        if (containerType instanceof AnyExternalObjectType) {
            config.checkLicensedFeature(8, "use of lookup expressions on external objects", -1);
            return config.makeObjectLookupExpression(this.getLhsExpression(), this.getRhsExpression()).typeCheck(visitor, contextInfo);
        }
        boolean bl2 = this.isSingleContainer = this.getLhsExpression().getCardinality() == 16384;
        if (!this.isArrayLookup && !this.isMapLookup && th.relationship(containerType, MapType.ANY_MAP_TYPE) == Affinity.DISJOINT && th.relationship(containerType, ArrayItemType.getInstance()) == Affinity.DISJOINT && th.relationship(containerType, AnyExternalObjectType.THE_INSTANCE) == Affinity.DISJOINT) {
            XPathException err = new XPathException("The left-hand operand of '?' must be a map or an array; the supplied expression is of type " + containerType, "XPTY0004");
            err.setLocation(this.getLocation());
            err.setIsTypeError(true);
            err.setFailingExpression(this);
            throw err;
        }
        this.getRhs().typeCheck(visitor, contextInfo);
        RoleDiagnostic role = new RoleDiagnostic(1, "?", 1);
        TypeChecker tc = config.getTypeChecker(false);
        SequenceType req = BuiltInAtomicType.ANY_ATOMIC.zeroOrMore();
        if (this.isArrayLookup) {
            req = BuiltInAtomicType.INTEGER.zeroOrMore();
        }
        this.setRhsExpression(tc.staticTypeCheck(this.getRhsExpression(), req, role, visitor));
        boolean bl3 = this.isSingleEntry = this.getRhsExpression().getCardinality() == 16384;
        if (isTupleLookup && this.getRhsExpression() instanceof StringLiteral && !(tt = (TupleType)(containerType instanceof TupleType ? containerType : originalType)).isExtensible() && tt.getFieldType(fieldName = ((StringLiteral)this.getRhsExpression()).getStringValue()) == null) {
            XPathException err = new XPathException("Field " + fieldName + " is not defined in the tuple type", "XPTY0004");
            err.setIsTypeError(true);
            err.setLocation(this.getLocation());
            throw err;
        }
        this.isClassified = true;
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getLhs().optimize(visitor, contextInfo);
        this.getRhs().optimize(visitor, contextInfo);
        return this;
    }

    @Override
    public double getCost() {
        return this.getLhsExpression().getCost() * this.getRhsExpression().getCost();
    }

    @Override
    public int getImplementationMethod() {
        return 2;
    }

    @Override
    public LookupExpression copy(RebindingMap rebindings) {
        LookupExpression exp = new LookupExpression(this.getLhsExpression().copy(rebindings), this.getRhsExpression().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, exp);
        exp.isArrayLookup = this.isArrayLookup;
        exp.isMapLookup = this.isMapLookup;
        exp.isSingleEntry = this.isSingleEntry;
        exp.isSingleContainer = this.isSingleContainer;
        return exp;
    }

    @Override
    public int computeCardinality() {
        if (this.isSingleContainer && this.isSingleEntry) {
            if (this.isArrayLookup) {
                ItemType arrayType = this.getLhsExpression().getItemType();
                if (arrayType instanceof ArrayItemType) {
                    return ((ArrayItemType)arrayType).getMemberType().getCardinality();
                }
            } else if (this.isMapLookup) {
                ItemType mapType = this.getLhsExpression().getItemType();
                if (mapType instanceof TupleItemType && this.getRhsExpression() instanceof StringLiteral) {
                    String fieldName = ((StringLiteral)this.getRhsExpression()).getStringValue();
                    SequenceType fieldType = ((TupleItemType)mapType).getFieldType(fieldName);
                    if (fieldType == null) {
                        return ((TupleItemType)mapType).isExtensible() ? 57344 : 8192;
                    }
                    return fieldType.getCardinality();
                }
                if (mapType instanceof MapType) {
                    return Cardinality.union(((MapType)mapType).getValueType().getCardinality(), 8192);
                }
            }
        }
        return 57344;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof LookupExpression)) {
            return false;
        }
        LookupExpression p = (LookupExpression)other;
        return this.getLhsExpression().isEqual(p.getLhsExpression()) && this.getRhsExpression().isEqual(p.getRhsExpression());
    }

    @Override
    public int computeHashCode() {
        return "LookupExpression".hashCode() ^ this.getLhsExpression().hashCode() ^ this.getRhsExpression().hashCode();
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        Configuration config = context.getConfiguration();
        if (this.isArrayLookup) {
            if (this.isSingleContainer && this.isSingleEntry) {
                ArrayItem array = (ArrayItem)this.getLhsExpression().evaluateItem(context);
                IntegerValue subscript = (IntegerValue)this.getRhsExpression().evaluateItem(context);
                int index = ArrayFunctionSet.checkSubscript(subscript, array.arrayLength());
                return array.get(index - 1).iterate();
            }
            if (this.isSingleEntry) {
                SequenceIterator baseIterator = this.getLhsExpression().iterate(context);
                IntegerValue subscriptValue = (IntegerValue)this.getRhsExpression().evaluateItem(context);
                int subscript = subscriptValue.asSubscript() - 1;
                return new MappingIterator(baseIterator, baseItem -> {
                    ArrayItem array = (ArrayItem)baseItem;
                    if (subscript >= 0 && subscript < array.arrayLength()) {
                        return array.get(subscript).iterate();
                    }
                    ArrayFunctionSet.checkSubscript(subscriptValue, array.arrayLength());
                    return null;
                });
            }
            SequenceIterator baseIterator = this.getLhsExpression().iterate(context);
            GroundedValue rhs = this.getRhsExpression().iterate(context).materialize();
            return new MappingIterator(baseIterator, baseItem -> new MappingIterator(rhs.iterate(), index -> {
                ArrayItem array = (ArrayItem)baseItem;
                int subscript = ArrayFunctionSet.checkSubscript((IntegerValue)index, array.arrayLength()) - 1;
                return array.get(subscript).iterate();
            }));
        }
        if (this.isMapLookup) {
            if (this.isSingleContainer && this.isSingleEntry) {
                AtomicValue key;
                MapItem map = (MapItem)this.getLhsExpression().evaluateItem(context);
                GroundedValue value = map.get(key = (AtomicValue)this.getRhsExpression().evaluateItem(context));
                return value == null ? EmptyIterator.emptyIterator() : value.iterate();
            }
            if (this.isSingleEntry) {
                SequenceIterator baseIterator = this.getLhsExpression().iterate(context);
                AtomicValue key = (AtomicValue)this.getRhsExpression().evaluateItem(context);
                return new MappingIterator(baseIterator, baseItem -> {
                    GroundedValue value = ((MapItem)baseItem).get(key);
                    return value == null ? EmptyIterator.emptyIterator() : value.iterate();
                });
            }
            SequenceIterator baseIterator = this.getLhsExpression().iterate(context);
            GroundedValue rhs = this.getRhsExpression().iterate(context).materialize();
            return new MappingIterator(baseIterator, baseItem -> new MappingIterator(rhs.iterate(), index -> {
                GroundedValue value = ((MapItem)baseItem).get((AtomicValue)index);
                return value == null ? EmptyIterator.emptyIterator() : value.iterate();
            }));
        }
        SequenceIterator baseIterator = this.getLhsExpression().iterate(context);
        GroundedValue rhs = this.getRhsExpression().iterate(context).materialize();
        MappingFunction mappingFunction = baseItem -> {
            if (baseItem instanceof ArrayItem) {
                MappingFunction arrayAccess = index -> {
                    if (index instanceof IntegerValue) {
                        GroundedValue member = ((ArrayItem)baseItem).get((int)((IntegerValue)index).longValue() - 1);
                        return member.iterate();
                    }
                    XPathException exception = new XPathException("An item on the LHS of the '?' operator is an array, but a value on the RHS of the operator (" + baseItem.toShortString() + ") is not an integer", "XPTY0004");
                    exception.setIsTypeError(true);
                    exception.setLocation(this.getLocation());
                    exception.setFailingExpression(this);
                    throw exception;
                };
                UnfailingIterator rhsIter = rhs.iterate();
                return new MappingIterator(rhsIter, arrayAccess);
            }
            if (baseItem instanceof MapItem) {
                UnfailingIterator rhsIter = rhs.iterate();
                return new MappingIterator(rhsIter, key -> {
                    GroundedValue value = ((MapItem)baseItem).get((AtomicValue)key);
                    return value == null ? EmptyIterator.emptyIterator() : value.iterate();
                });
            }
            if (baseItem instanceof ObjectValue) {
                if (!(rhs instanceof StringValue)) {
                    XPathException exception = new XPathException("An item on the LHS of the '?' operator is an external object, but a value on the RHS of the operator (" + baseItem.toShortString() + ") is not a singleton string", "XPTY0004");
                    exception.setIsTypeError(true);
                    exception.setLocation(this.getLocation());
                    exception.setFailingExpression(this);
                    throw exception;
                }
                String key2 = rhs.getStringValue();
                return config.externalObjectAsMap((ObjectValue)baseItem, key2).get((StringValue)rhs).iterate();
            }
            LookupExpression.mustBeArrayOrMap(this, baseItem);
            return null;
        };
        return new MappingIterator(baseIterator, mappingFunction);
    }

    protected static void mustBeArrayOrMap(Expression exp, Item baseItem) throws XPathException {
        XPathException exception = new XPathException("The items on the LHS of the '?' operator must be maps or arrays; but value (" + baseItem.toShortString() + ") was supplied", "XPTY0004");
        exception.setIsTypeError(true);
        exception.setLocation(exp.getLocation());
        exception.setFailingExpression(exp);
        throw exception;
    }

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        destination.startElement("lookup", this);
        this.getLhsExpression().export(destination);
        this.getRhsExpression().export(destination);
        destination.endElement();
    }

    @Override
    public String toString() {
        Literal lit;
        String rhs = this.getRhsExpression() instanceof Literal ? ((lit = (Literal)this.getRhsExpression()) instanceof StringLiteral && NameChecker.isValidNCName(((StringLiteral)lit).getStringValue()) ? ((StringLiteral)lit).getStringValue() : (lit.getValue() instanceof Int64Value ? lit.getValue().toString() : ExpressionTool.parenthesize(lit))) : ExpressionTool.parenthesize(this.getRhsExpression());
        return ExpressionTool.parenthesize(this.getLhsExpression()) + "?" + rhs;
    }
}


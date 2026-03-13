/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.util.Optional;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.UnfailingIterator;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AlphaCode;
import net.sf.saxon.type.AnyFunctionType;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.NumericType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.Cardinality;

public final class SequenceType {
    private ItemType primaryType;
    private int cardinality;
    public static final SequenceType ANY_SEQUENCE = AnyItemType.getInstance().zeroOrMore();
    public static final SequenceType SINGLE_ITEM = AnyItemType.getInstance().one();
    public static final SequenceType OPTIONAL_ITEM = AnyItemType.getInstance().zeroOrOne();
    public static final SequenceType SINGLE_ATOMIC = BuiltInAtomicType.ANY_ATOMIC.one();
    public static final SequenceType OPTIONAL_ATOMIC = BuiltInAtomicType.ANY_ATOMIC.zeroOrOne();
    public static final SequenceType ATOMIC_SEQUENCE = BuiltInAtomicType.ANY_ATOMIC.zeroOrMore();
    public static final SequenceType SINGLE_STRING = BuiltInAtomicType.STRING.one();
    public static final SequenceType SINGLE_UNTYPED_ATOMIC = BuiltInAtomicType.UNTYPED_ATOMIC.one();
    public static final SequenceType OPTIONAL_STRING = BuiltInAtomicType.STRING.zeroOrOne();
    public static final SequenceType SINGLE_BOOLEAN = BuiltInAtomicType.BOOLEAN.one();
    public static final SequenceType OPTIONAL_BOOLEAN = BuiltInAtomicType.BOOLEAN.zeroOrOne();
    public static final SequenceType SINGLE_INTEGER = BuiltInAtomicType.INTEGER.one();
    public static final SequenceType SINGLE_DECIMAL = BuiltInAtomicType.DECIMAL.one();
    public static final SequenceType OPTIONAL_INTEGER = BuiltInAtomicType.INTEGER.zeroOrOne();
    public static final SequenceType SINGLE_SHORT = BuiltInAtomicType.SHORT.one();
    public static final SequenceType OPTIONAL_SHORT = BuiltInAtomicType.SHORT.zeroOrOne();
    public static final SequenceType SINGLE_BYTE = BuiltInAtomicType.BYTE.one();
    public static final SequenceType OPTIONAL_BYTE = BuiltInAtomicType.BYTE.zeroOrOne();
    public static final SequenceType SINGLE_DOUBLE = BuiltInAtomicType.DOUBLE.one();
    public static final SequenceType OPTIONAL_DOUBLE = BuiltInAtomicType.DOUBLE.zeroOrOne();
    public static final SequenceType SINGLE_FLOAT = BuiltInAtomicType.FLOAT.one();
    public static final SequenceType OPTIONAL_FLOAT = BuiltInAtomicType.FLOAT.zeroOrOne();
    public static final SequenceType OPTIONAL_DECIMAL = BuiltInAtomicType.DECIMAL.zeroOrOne();
    public static final SequenceType OPTIONAL_ANY_URI = BuiltInAtomicType.ANY_URI.zeroOrOne();
    public static final SequenceType OPTIONAL_DATE = BuiltInAtomicType.DATE.zeroOrOne();
    public static final SequenceType OPTIONAL_TIME = BuiltInAtomicType.TIME.zeroOrOne();
    public static final SequenceType OPTIONAL_G_YEAR = BuiltInAtomicType.G_YEAR.zeroOrOne();
    public static final SequenceType OPTIONAL_G_YEAR_MONTH = BuiltInAtomicType.G_YEAR_MONTH.zeroOrOne();
    public static final SequenceType OPTIONAL_G_MONTH = BuiltInAtomicType.G_MONTH.zeroOrOne();
    public static final SequenceType OPTIONAL_G_MONTH_DAY = BuiltInAtomicType.G_MONTH_DAY.zeroOrOne();
    public static final SequenceType OPTIONAL_G_DAY = BuiltInAtomicType.G_DAY.zeroOrOne();
    public static final SequenceType OPTIONAL_DATE_TIME = BuiltInAtomicType.DATE_TIME.zeroOrOne();
    public static final SequenceType OPTIONAL_DURATION = BuiltInAtomicType.DURATION.zeroOrOne();
    public static final SequenceType OPTIONAL_YEAR_MONTH_DURATION = BuiltInAtomicType.YEAR_MONTH_DURATION.zeroOrOne();
    public static final SequenceType OPTIONAL_DAY_TIME_DURATION = BuiltInAtomicType.DAY_TIME_DURATION.zeroOrOne();
    public static final SequenceType SINGLE_QNAME = BuiltInAtomicType.QNAME.one();
    public static final SequenceType OPTIONAL_QNAME = BuiltInAtomicType.QNAME.zeroOrOne();
    public static final SequenceType OPTIONAL_NOTATION = BuiltInAtomicType.NOTATION.zeroOrOne();
    public static final SequenceType OPTIONAL_BASE64_BINARY = BuiltInAtomicType.BASE64_BINARY.zeroOrOne();
    public static final SequenceType OPTIONAL_HEX_BINARY = BuiltInAtomicType.HEX_BINARY.zeroOrOne();
    public static final SequenceType OPTIONAL_NUMERIC = SequenceType.makeSequenceType(NumericType.getInstance(), 24576);
    public static final SequenceType SINGLE_NUMERIC = SequenceType.makeSequenceType(NumericType.getInstance(), 16384);
    public static final SequenceType OPTIONAL_NODE = AnyNodeTest.getInstance().zeroOrOne();
    public static final SequenceType SINGLE_NODE = AnyNodeTest.getInstance().one();
    public static final SequenceType OPTIONAL_DOCUMENT_NODE = NodeKindTest.DOCUMENT.zeroOrOne();
    public static final SequenceType NODE_SEQUENCE = AnyNodeTest.getInstance().zeroOrMore();
    public static final SequenceType STRING_SEQUENCE = BuiltInAtomicType.STRING.zeroOrMore();
    public static final SequenceType SINGLE_FUNCTION = SequenceType.makeSequenceType(AnyFunctionType.ANY_FUNCTION, 16384);
    public static final SequenceType OPTIONAL_FUNCTION_ITEM = SequenceType.makeSequenceType(AnyFunctionType.getInstance(), 24576);
    public static final SequenceType FUNCTION_ITEM_SEQUENCE = SequenceType.makeSequenceType(AnyFunctionType.getInstance(), 57344);
    public static final SequenceType EMPTY_SEQUENCE = new SequenceType(ErrorType.getInstance(), 8192);
    public static final SequenceType NON_EMPTY_SEQUENCE = SequenceType.makeSequenceType(AnyItemType.getInstance(), 49152);
    public static final SequenceType VOID = SequenceType.makeSequenceType(ErrorType.getInstance(), 32768);

    public SequenceType(ItemType primaryType, int cardinality) {
        this.primaryType = primaryType;
        this.cardinality = primaryType instanceof ErrorType && Cardinality.allowsZero(cardinality) ? 8192 : cardinality;
    }

    public static SequenceType makeSequenceType(ItemType primaryType, int cardinality) {
        if (primaryType instanceof ItemType.WithSequenceTypeCache) {
            ItemType.WithSequenceTypeCache bat = (ItemType.WithSequenceTypeCache)primaryType;
            switch (cardinality) {
                case 16384: {
                    return bat.one();
                }
                case 24576: {
                    return bat.zeroOrOne();
                }
                case 57344: {
                    return bat.zeroOrMore();
                }
                case 49152: {
                    return bat.oneOrMore();
                }
            }
        }
        if (cardinality == 8192) {
            return EMPTY_SEQUENCE;
        }
        return new SequenceType(primaryType, cardinality);
    }

    public ItemType getPrimaryType() {
        return this.primaryType;
    }

    public int getCardinality() {
        return this.cardinality;
    }

    public boolean matches(Sequence value, TypeHierarchy th) throws XPathException {
        Item item;
        int count = 0;
        SequenceIterator iter = value.iterate();
        while ((item = iter.next()) != null) {
            ++count;
            if (this.primaryType.matches(item, th)) continue;
            return false;
        }
        return !(count == 0 && !Cardinality.allowsZero(this.cardinality) || count > 1 && !Cardinality.allowsMany(this.cardinality));
    }

    public Optional<String> explainMismatch(GroundedValue value, TypeHierarchy th) {
        try {
            Item item;
            int count = 0;
            UnfailingIterator iter = value.iterate();
            while ((item = iter.next()) != null) {
                ++count;
                if (this.primaryType.matches(item, th)) continue;
                String s = "The " + RoleDiagnostic.ordinal(count) + " item is not an instance of the required type";
                Optional<String> more = this.primaryType.explainMismatch(item, th);
                if (more.isPresent()) {
                    s = count == 1 ? more.get() : s + ". " + more.get();
                } else if (count == 1) {
                    return Optional.empty();
                }
                return Optional.of(s);
            }
            if (count == 0 && !Cardinality.allowsZero(this.cardinality)) {
                return Optional.of("The type does not allow an empty sequence");
            }
            if (count > 1 && !Cardinality.allowsMany(this.cardinality)) {
                return Optional.of("The type does not allow a sequence of more than one item");
            }
            return Optional.empty();
        } catch (XPathException e) {
            return Optional.empty();
        }
    }

    public String toString() {
        if (this.cardinality == 8192) {
            return "empty-sequence()";
        }
        return this.primaryType + Cardinality.getOccurrenceIndicator(this.cardinality);
    }

    public String toExportString() {
        if (this.cardinality == 8192) {
            return "empty-sequence()";
        }
        return this.primaryType.toExportString() + Cardinality.getOccurrenceIndicator(this.cardinality);
    }

    public String toAlphaCode() {
        return AlphaCode.fromSequenceType(this);
    }

    public int hashCode() {
        return this.primaryType.hashCode() ^ this.cardinality;
    }

    public boolean equals(Object obj) {
        return obj instanceof SequenceType && this.primaryType.equals(((SequenceType)obj).primaryType) && this.cardinality == ((SequenceType)obj).cardinality;
    }

    public boolean isSameType(SequenceType other, TypeHierarchy th) {
        return this.cardinality == other.cardinality && th.relationship(this.primaryType, other.primaryType) == Affinity.SAME_TYPE;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.UnionConstructorFunction;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.type.UnionType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;

public class UnionCastableFunction
extends UnionConstructorFunction {
    public UnionCastableFunction(UnionType targetType, NamespaceResolver resolver, boolean allowEmpty) {
        super(targetType, resolver, allowEmpty);
    }

    @Override
    public FunctionItemType getFunctionItemType() {
        return new SpecificFunctionType(new SequenceType[]{SequenceType.ANY_SEQUENCE}, SequenceType.SINGLE_BOOLEAN);
    }

    @Override
    public StructuredQName getFunctionName() {
        return null;
    }

    private boolean effectiveBooleanValue(SequenceIterator iter, XPathContext context) throws XPathException {
        Item item;
        int count = 0;
        while ((item = iter.next()) != null) {
            if (item instanceof NodeInfo) {
                AtomicValue av;
                AtomicSequence atomizedValue = item.atomize();
                int length = SequenceTool.getLength(atomizedValue);
                if ((count += length) > 1) {
                    return false;
                }
                if (length == 0 || this.castable(av = atomizedValue.head(), context)) continue;
                return false;
            }
            if (item instanceof AtomicValue) {
                AtomicValue av = (AtomicValue)item;
                if (++count > 1) {
                    return false;
                }
                if (this.castable(av, context)) continue;
                return false;
            }
            throw new XPathException("Input to 'castable' operator cannot be atomized", "XPTY0004");
        }
        return count != 0 || this.allowEmpty;
    }

    private boolean castable(AtomicValue value, XPathContext context) {
        try {
            this.cast(value, context);
            return true;
        } catch (XPathException err) {
            return false;
        }
    }

    @Override
    public BooleanValue call(XPathContext context, Sequence[] args) throws XPathException {
        boolean value = this.effectiveBooleanValue(args[0].iterate(), context);
        return BooleanValue.get(value);
    }
}


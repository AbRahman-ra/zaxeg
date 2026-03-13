/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.sf.saxon.ma.arrays.ArrayFunctionSet;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.SimpleArrayItem;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmFunctionItem;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;

public class XdmArray
extends XdmFunctionItem {
    public XdmArray() {
        this.setValue(SimpleArrayItem.EMPTY_ARRAY);
    }

    public XdmArray(ArrayItem array) {
        this.setValue(array);
    }

    public XdmArray(XdmValue[] members) {
        ArrayList<GroundedValue> values = new ArrayList<GroundedValue>();
        for (XdmValue member : members) {
            values.add(member.getUnderlyingValue());
        }
        this.setValue(new SimpleArrayItem(values));
    }

    public XdmArray(Iterable<? extends XdmValue> members) {
        ArrayList<GroundedValue> values = new ArrayList<GroundedValue>();
        for (XdmValue xdmValue : members) {
            values.add(xdmValue.getUnderlyingValue());
        }
        this.setValue(new SimpleArrayItem(values));
    }

    public int arrayLength() {
        return this.getUnderlyingValue().arrayLength();
    }

    public XdmValue get(int n) throws IndexOutOfBoundsException {
        GroundedValue member = this.getUnderlyingValue().get(n);
        return XdmValue.wrap(member);
    }

    public XdmArray put(int n, XdmValue value) throws IndexOutOfBoundsException {
        GroundedValue member = value.getUnderlyingValue();
        return (XdmArray)XdmValue.wrap(this.getUnderlyingValue().put(n, member));
    }

    public XdmArray addMember(XdmValue value) {
        try {
            GroundedValue member = value.getUnderlyingValue();
            ArrayItem newArray = ArrayFunctionSet.ArrayAppend.append(this.getUnderlyingValue(), member);
            return (XdmArray)XdmValue.wrap(newArray);
        } catch (XPathException e) {
            throw new SaxonApiUncheckedException(e);
        }
    }

    public XdmArray concat(XdmArray value) {
        ArrayItem other = value.getUnderlyingValue();
        ArrayItem newArray = this.getUnderlyingValue().concat(other);
        return (XdmArray)XdmValue.wrap(newArray);
    }

    public List<XdmValue> asList() {
        Iterator<GroundedValue> members = this.getUnderlyingValue().members().iterator();
        ArrayList<XdmValue> result = new ArrayList<XdmValue>(this.getUnderlyingValue().getLength());
        while (members.hasNext()) {
            result.add(XdmValue.wrap(members.next()));
        }
        return result;
    }

    @Override
    public ArrayItem getUnderlyingValue() {
        return (ArrayItem)super.getUnderlyingValue();
    }

    public static XdmArray makeArray(Object[] input) throws IllegalArgumentException {
        ArrayList<XdmValue> result = new ArrayList<XdmValue>(input.length);
        for (Object o : input) {
            result.add(XdmValue.makeValue(o));
        }
        return new XdmArray((Iterable<? extends XdmValue>)result);
    }

    public static XdmArray makeArray(boolean[] input) {
        ArrayList<XdmAtomicValue> result = new ArrayList<XdmAtomicValue>(input.length);
        for (boolean o : input) {
            result.add(new XdmAtomicValue(o));
        }
        return new XdmArray((Iterable<? extends XdmValue>)result);
    }

    public static XdmArray makeArray(long[] input) {
        ArrayList<XdmAtomicValue> result = new ArrayList<XdmAtomicValue>(input.length);
        for (long o : input) {
            result.add(new XdmAtomicValue(o));
        }
        return new XdmArray((Iterable<? extends XdmValue>)result);
    }

    public static XdmArray makeArray(int[] input) {
        ArrayList<XdmAtomicValue> result = new ArrayList<XdmAtomicValue>(input.length);
        for (int o : input) {
            result.add(new XdmAtomicValue(o));
        }
        return new XdmArray((Iterable<? extends XdmValue>)result);
    }

    public static XdmArray makeArray(short[] input) {
        ArrayList<XdmAtomicValue> result = new ArrayList<XdmAtomicValue>(input.length);
        for (short o : input) {
            result.add(new XdmAtomicValue(o));
        }
        return new XdmArray((Iterable<? extends XdmValue>)result);
    }

    public static XdmArray makeArray(byte[] input) {
        ArrayList<XdmAtomicValue> result = new ArrayList<XdmAtomicValue>(input.length);
        for (byte o : input) {
            result.add(new XdmAtomicValue(o));
        }
        return new XdmArray((Iterable<? extends XdmValue>)result);
    }
}


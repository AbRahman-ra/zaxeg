/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.arrays;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.ma.arrays.AbstractArrayItem;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.ImmutableArrayItem;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.ExternalObject;
import net.sf.saxon.z.IntSet;

public class SimpleArrayItem
extends AbstractArrayItem
implements ArrayItem {
    public static final SimpleArrayItem EMPTY_ARRAY = new SimpleArrayItem(new ArrayList<GroundedValue>());
    private final List<GroundedValue> members;
    private boolean knownToBeGrounded = false;

    public SimpleArrayItem(List<GroundedValue> members) {
        this.members = members;
    }

    public static SimpleArrayItem makeSimpleArrayItem(SequenceIterator input) throws XPathException {
        ArrayList<GroundedValue> members = new ArrayList<GroundedValue>();
        input.forEachOrFail(item -> {
            if (item.getClass().getName().equals("com.saxonica.functions.extfn.ArrayMemberValue")) {
                members.add((GroundedValue)((ExternalObject)item).getObject());
            } else {
                members.add(item);
            }
        });
        SimpleArrayItem result = new SimpleArrayItem(members);
        result.knownToBeGrounded = true;
        return result;
    }

    @Override
    public OperandRole[] getOperandRoles() {
        return new OperandRole[]{OperandRole.SINGLE_ATOMIC};
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void makeGrounded() throws XPathException {
        if (!this.knownToBeGrounded) {
            SimpleArrayItem simpleArrayItem = this;
            synchronized (simpleArrayItem) {
                for (int i = 0; i < this.members.size(); ++i) {
                    this.members.set(i, ((Sequence)this.members.get(i)).materialize());
                }
                this.knownToBeGrounded = true;
            }
        }
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean isMap() {
        return false;
    }

    @Override
    public AnnotationList getAnnotations() {
        return AnnotationList.EMPTY;
    }

    @Override
    public GroundedValue get(int index) {
        return this.members.get(index);
    }

    @Override
    public ArrayItem put(int index, GroundedValue newValue) {
        ImmutableArrayItem a2 = new ImmutableArrayItem(this);
        return a2.put(index, newValue);
    }

    @Override
    public int arrayLength() {
        return this.members.size();
    }

    @Override
    public boolean isEmpty() {
        return this.members.isEmpty();
    }

    @Override
    public Iterable<GroundedValue> members() {
        return this.members;
    }

    @Override
    public ArrayItem removeSeveral(IntSet positions) {
        ImmutableArrayItem a2 = new ImmutableArrayItem(this);
        return a2.removeSeveral(positions);
    }

    @Override
    public ArrayItem remove(int pos) {
        ImmutableArrayItem a2 = new ImmutableArrayItem(this);
        return a2.remove(pos);
    }

    @Override
    public ArrayItem subArray(int start, int end) {
        return new SimpleArrayItem(this.members.subList(start, end));
    }

    @Override
    public ArrayItem insert(int position, GroundedValue member) {
        ImmutableArrayItem a2 = new ImmutableArrayItem(this);
        return a2.insert(position, member);
    }

    @Override
    public ArrayItem concat(ArrayItem other) {
        ImmutableArrayItem a2 = new ImmutableArrayItem(this);
        return a2.concat(other);
    }

    public List<GroundedValue> getMembers() {
        return this.members;
    }

    @Override
    public String toShortString() {
        int size = this.getLength();
        if (size == 0) {
            return "[]";
        }
        if (size > 5) {
            return "[(:size " + size + ":)]";
        }
        FastStringBuffer buff = new FastStringBuffer(256);
        buff.append("[");
        for (GroundedValue entry : this.members()) {
            buff.append(Err.depictSequence(entry).toString().trim());
            buff.append(", ");
        }
        if (size == 1) {
            buff.append("]");
        } else {
            buff.setCharAt(buff.length() - 2, ']');
        }
        return buff.toString().trim();
    }
}


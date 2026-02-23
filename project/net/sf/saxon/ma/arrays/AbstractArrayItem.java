/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.arrays;

import java.util.ArrayList;
import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.functions.DeepEqual;
import net.sf.saxon.ma.arrays.ArrayFunctionSet;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.om.AtomicArray;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;

public abstract class AbstractArrayItem
implements ArrayItem {
    private SequenceType memberType = null;

    @Override
    public OperandRole[] getOperandRoles() {
        return new OperandRole[]{OperandRole.SINGLE_ATOMIC};
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
    public AtomicSequence atomize() throws XPathException {
        ArrayList<AtomicValue> list = new ArrayList<AtomicValue>(this.arrayLength());
        for (GroundedValue seq : this.members()) {
            seq.iterate().forEachOrFail(item -> {
                AtomicSequence atoms = item.atomize();
                for (AtomicValue atom : atoms) {
                    list.add(atom);
                }
            });
        }
        return new AtomicArray(list);
    }

    @Override
    public AnnotationList getAnnotations() {
        return AnnotationList.EMPTY;
    }

    @Override
    public FunctionItemType getFunctionItemType() {
        return ArrayItemType.ANY_ARRAY_TYPE;
    }

    @Override
    public StructuredQName getFunctionName() {
        return null;
    }

    @Override
    public String getDescription() {
        return "array";
    }

    @Override
    public int getArity() {
        return 1;
    }

    @Override
    public XPathContext makeNewContext(XPathContext callingContext, ContextOriginator originator) {
        return callingContext;
    }

    @Override
    public GroundedValue call(XPathContext context, Sequence[] args) throws XPathException {
        IntegerValue subscript = (IntegerValue)args[0].head();
        return this.get(ArrayFunctionSet.checkSubscript(subscript, this.arrayLength()) - 1);
    }

    @Override
    public boolean deepEquals(Function other, XPathContext context, AtomicComparer comparer, int flags) throws XPathException {
        if (other instanceof ArrayItem) {
            ArrayItem that = (ArrayItem)other;
            if (this.arrayLength() != that.arrayLength()) {
                return false;
            }
            for (int i = 0; i < this.arrayLength(); ++i) {
                if (DeepEqual.deepEqual(this.get(i).iterate(), that.get(i).iterate(), comparer, context, flags)) continue;
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean effectiveBooleanValue() throws XPathException {
        throw new XPathException("Effective boolean value is not defined for arrays", "FORG0006");
    }

    @Override
    public String getStringValue() {
        throw new UnsupportedOperationException("An array does not have a string value");
    }

    @Override
    public CharSequence getStringValueCS() {
        throw new UnsupportedOperationException("An array does not have a string value");
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("array");
        out.emitAttribute("size", this.arrayLength() + "");
        for (GroundedValue mem : this.members()) {
            Literal.exportValue(mem, out);
        }
        out.endElement();
    }

    @Override
    public boolean isTrustedResultType() {
        return false;
    }

    public String toString() {
        FastStringBuffer buffer = new FastStringBuffer(256);
        buffer.append("[");
        for (GroundedValue seq : this.members()) {
            if (buffer.length() > 1) {
                buffer.append(", ");
            }
            buffer.append(seq.toString());
        }
        buffer.append("]");
        return buffer.toString();
    }

    @Override
    public SequenceType getMemberType(TypeHierarchy th) {
        if (this.memberType == null) {
            if (this.isEmpty()) {
                this.memberType = SequenceType.makeSequenceType(ErrorType.getInstance(), 16384);
            } else {
                ItemType contentType = null;
                int contentCard = 16384;
                for (GroundedValue s : this.members()) {
                    if (contentType == null) {
                        contentType = SequenceTool.getItemType(s, th);
                        contentCard = SequenceTool.getCardinality(s);
                        continue;
                    }
                    contentType = Type.getCommonSuperType(contentType, SequenceTool.getItemType(s, th));
                    contentCard = Cardinality.union(contentCard, SequenceTool.getCardinality(s));
                }
                this.memberType = SequenceType.makeSequenceType(contentType, contentCard);
            }
        }
        return this.memberType;
    }
}


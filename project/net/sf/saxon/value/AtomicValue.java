/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.util.Iterator;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.expr.sort.CodepointCollator;
import net.sf.saxon.functions.AccessorFn;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Genre;
import net.sf.saxon.om.IdentityComparable;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.NoDynamicContextException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingleAtomicIterator;
import net.sf.saxon.tree.jiter.MonoIterator;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ComplexType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.UType;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.Whitespace;

public abstract class AtomicValue
implements Item,
AtomicSequence,
ConversionResult,
IdentityComparable {
    protected AtomicType typeLabel;

    @Override
    public AtomicSequence atomize() throws XPathException {
        return this;
    }

    @Override
    public final AtomicValue head() {
        return this;
    }

    @Override
    public int getLength() {
        return 1;
    }

    public void setTypeLabel(AtomicType type) {
        this.typeLabel = type;
    }

    public abstract Comparable getSchemaComparable();

    public abstract AtomicMatchKey getXPathComparable(boolean var1, StringCollator var2, int var3) throws NoDynamicContextException;

    public AtomicMatchKey asMapKey() {
        try {
            return this.getXPathComparable(false, CodepointCollator.getInstance(), Integer.MIN_VALUE);
        } catch (NoDynamicContextException e) {
            throw new IllegalStateException("No implicit timezone available");
        }
    }

    public abstract boolean equals(Object var1);

    public boolean isIdentical(AtomicValue v) {
        return this.getSchemaComparable().equals(v.getSchemaComparable());
    }

    @Override
    public boolean isIdentical(IdentityComparable other) {
        return other instanceof AtomicValue && this.isIdentical((AtomicValue)other);
    }

    @Override
    public int identityHashCode() {
        return this.hashCode();
    }

    @Override
    public CharSequence getStringValueCS() {
        CharSequence cs = this.getPrimitiveStringValue();
        try {
            return this.typeLabel.postprocess(cs);
        } catch (XPathException err) {
            return cs;
        }
    }

    @Override
    public CharSequence getCanonicalLexicalRepresentation() {
        return this.getStringValueCS();
    }

    @Override
    public final AtomicValue itemAt(int n) {
        return n == 0 ? this.head() : null;
    }

    public final AtomicType getItemType() {
        return this.typeLabel;
    }

    public abstract BuiltInAtomicType getPrimitiveType();

    public final UType getUType() {
        return this.getItemType().getUType();
    }

    public final int getCardinality() {
        return 16384;
    }

    public abstract AtomicValue copyAsSubType(AtomicType var1);

    public boolean isNaN() {
        return false;
    }

    @Override
    public final String getStringValue() {
        return this.getStringValueCS().toString();
    }

    protected abstract CharSequence getPrimitiveStringValue();

    @Override
    public boolean effectiveBooleanValue() throws XPathException {
        XPathException err = new XPathException("Effective boolean value is not defined for an atomic value of type " + Type.displayTypeName(this));
        err.setIsTypeError(true);
        err.setErrorCode("FORG0006");
        throw err;
    }

    public AtomicValue getComponent(AccessorFn.Component component) throws XPathException {
        throw new UnsupportedOperationException("Data type does not support component extraction");
    }

    public void checkPermittedContents(SchemaType parentType, StaticContext env, boolean whole) throws XPathException {
        if (whole) {
            SimpleType stype = null;
            if (parentType instanceof SimpleType) {
                stype = (SimpleType)parentType;
            } else if (parentType instanceof ComplexType && ((ComplexType)parentType).isSimpleContent()) {
                stype = ((ComplexType)parentType).getSimpleContentType();
            }
            if (stype != null && !stype.isNamespaceSensitive()) {
                ValidationFailure err = stype.validateContent(this.getStringValueCS(), null, env.getConfiguration().getConversionRules());
                if (err != null) {
                    throw err.makeException();
                }
                return;
            }
        }
        if (parentType instanceof ComplexType && !((ComplexType)parentType).isSimpleContent() && !((ComplexType)parentType).isMixedContent() && !Whitespace.isWhite(this.getStringValueCS())) {
            XPathException err = new XPathException("Complex type " + parentType.getDescription() + " does not allow text content " + Err.wrap(this.getStringValueCS()));
            err.setIsTypeError(true);
            throw err;
        }
    }

    public void checkValidInJavascript() throws XPathException {
    }

    @Override
    public AtomicValue asAtomic() {
        return this;
    }

    public String toString() {
        return this.typeLabel + "(\"" + this.getStringValueCS() + "\")";
    }

    @Override
    public SingleAtomicIterator<? extends AtomicValue> iterate() {
        return new SingleAtomicIterator<AtomicValue>(this);
    }

    @Override
    public Iterator<AtomicValue> iterator() {
        return new MonoIterator<AtomicValue>(this);
    }

    @Override
    public final Genre getGenre() {
        return Genre.ATOMIC;
    }
}


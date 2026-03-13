/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.QualifiedNameValue;

public final class NotationValue
extends QualifiedNameValue {
    public NotationValue(String prefix, String uri, String localName, boolean check) throws XPathException {
        if (check && !NameChecker.isValidNCName(localName)) {
            XPathException err = new XPathException("Malformed local name in NOTATION: '" + localName + '\'');
            err.setErrorCode("FORG0001");
            throw err;
        }
        prefix = prefix == null ? "" : prefix;
        String string = uri = uri == null ? "" : uri;
        if (check && uri.isEmpty() && prefix.length() != 0) {
            XPathException err = new XPathException("NOTATION has null namespace but non-empty prefix");
            err.setErrorCode("FOCA0002");
            throw err;
        }
        this.qName = new StructuredQName(prefix, uri, localName);
        this.typeLabel = BuiltInAtomicType.NOTATION;
    }

    public NotationValue(String prefix, String uri, String localName) {
        this.qName = new StructuredQName(prefix, uri, localName);
        this.typeLabel = BuiltInAtomicType.NOTATION;
    }

    public NotationValue(String prefix, String uri, String localName, AtomicType typeLabel) {
        this.qName = new StructuredQName(prefix, uri, localName);
        this.typeLabel = typeLabel;
    }

    public NotationValue(StructuredQName qName, AtomicType typeLabel) {
        if (qName == null) {
            throw new NullPointerException("qName");
        }
        if (typeLabel == null) {
            throw new NullPointerException("typeLabel");
        }
        this.qName = qName;
        this.typeLabel = typeLabel;
    }

    @Override
    public AtomicValue copyAsSubType(AtomicType typeLabel) {
        NotationValue v = new NotationValue(this.getPrefix(), this.getNamespaceURI(), this.getLocalName());
        v.typeLabel = typeLabel;
        return v;
    }

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.NOTATION;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof NotationValue && this.qName.equals(((NotationValue)other).qName);
    }

    @Override
    public Comparable getSchemaComparable() {
        return new NotationComparable();
    }

    @Override
    public String toString() {
        return "NOTATION(" + this.getClarkName() + ')';
    }

    private class NotationComparable
    implements Comparable {
        private NotationComparable() {
        }

        public NotationValue getNotationValue() {
            return NotationValue.this;
        }

        public int compareTo(Object o) {
            return this.equals(o) ? 0 : Integer.MIN_VALUE;
        }

        public boolean equals(Object o) {
            return o instanceof NotationComparable && NotationValue.this.qName.equals(((NotationComparable)o).getNotationValue().qName);
        }

        public int hashCode() {
            return NotationValue.this.qName.hashCode();
        }
    }
}


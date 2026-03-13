/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import net.sf.saxon.functions.AccessorFn;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.QualifiedNameValue;
import net.sf.saxon.value.StringValue;

public class QNameValue
extends QualifiedNameValue {
    public QNameValue(String prefix, String uri, String localName) {
        this(prefix, uri, localName, BuiltInAtomicType.QNAME);
    }

    public QNameValue(String prefix, String uri, String localName, AtomicType type) {
        this.qName = new StructuredQName(prefix, uri, localName);
        if (type == null) {
            type = BuiltInAtomicType.QNAME;
        }
        this.typeLabel = type;
    }

    public QNameValue(String prefix, String uri, String localName, AtomicType type, boolean check) throws XPathException {
        if (!NameChecker.isValidNCName(localName)) {
            XPathException err = new XPathException("Malformed local name in QName: '" + localName + '\'');
            err.setErrorCode("FORG0001");
            throw err;
        }
        prefix = prefix == null ? "" : prefix;
        String string = uri = "".equals(uri) ? null : uri;
        if (check && uri == null && prefix.length() != 0) {
            XPathException err = new XPathException("QName has null namespace but non-empty prefix");
            err.setErrorCode("FOCA0002");
            throw err;
        }
        this.qName = new StructuredQName(prefix, uri, localName);
        this.typeLabel = type;
    }

    public QNameValue(StructuredQName qName, AtomicType typeLabel) {
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
        return new QNameValue(this.qName, typeLabel);
    }

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.QNAME;
    }

    @Override
    public AtomicValue getComponent(AccessorFn.Component part) {
        switch (part) {
            case LOCALNAME: {
                return new StringValue(this.getLocalName(), BuiltInAtomicType.NCNAME);
            }
            case NAMESPACE: {
                return new AnyURIValue(this.getNamespaceURI());
            }
            case PREFIX: {
                String prefix = this.getPrefix();
                if (prefix.isEmpty()) {
                    return null;
                }
                return new StringValue(prefix, BuiltInAtomicType.NCNAME);
            }
        }
        throw new UnsupportedOperationException("Component of QName must be URI, Local Name, or Prefix");
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof QNameValue && this.qName.equals(((QNameValue)other).qName);
    }

    @Override
    public Comparable getSchemaComparable() {
        return new QNameComparable();
    }

    private class QNameComparable
    implements Comparable {
        private QNameComparable() {
        }

        public QNameValue getQNameValue() {
            return QNameValue.this;
        }

        public int compareTo(Object o) {
            return this.equals(o) ? 0 : Integer.MIN_VALUE;
        }

        public boolean equals(Object o) {
            return o instanceof QNameComparable && QNameValue.this.qName.equals(((QNameComparable)o).getQNameValue().qName);
        }

        public int hashCode() {
            return QNameValue.this.qName.hashCode();
        }
    }
}


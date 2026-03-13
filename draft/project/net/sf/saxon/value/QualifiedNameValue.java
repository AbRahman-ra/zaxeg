/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import javax.xml.namespace.QName;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.NotationValue;
import net.sf.saxon.value.QNameValue;

public abstract class QualifiedNameValue
extends AtomicValue
implements AtomicMatchKey {
    protected StructuredQName qName;

    public static AtomicValue makeQName(String prefix, String uri, String local, AtomicType targetType, CharSequence lexicalForm, ConversionRules rules) throws XPathException {
        if (targetType.getFingerprint() == 530) {
            return new QNameValue(prefix, uri, local, BuiltInAtomicType.QNAME, true);
        }
        QualifiedNameValue qnv = targetType.getPrimitiveType() == 530 ? new QNameValue(prefix, uri, local, targetType, true) : new NotationValue(prefix, uri, local, null);
        ValidationFailure vf = targetType.validate(qnv, lexicalForm, rules);
        if (vf != null) {
            throw vf.makeException();
        }
        qnv.setTypeLabel(targetType);
        return qnv;
    }

    @Override
    public final String getPrimitiveStringValue() {
        return this.qName.getDisplayName();
    }

    public final String getClarkName() {
        return this.qName.getClarkName();
    }

    public final String getEQName() {
        return this.qName.getEQName();
    }

    public final String getLocalName() {
        return this.qName.getLocalPart();
    }

    public final String getNamespaceURI() {
        return this.qName.getURI();
    }

    public final String getPrefix() {
        return this.qName.getPrefix();
    }

    @Override
    public AtomicMatchKey getXPathComparable(boolean ordered, StringCollator collator, int implicitTimezone) {
        return ordered ? null : this;
    }

    public int hashCode() {
        return this.qName.hashCode();
    }

    @Override
    public boolean isIdentical(AtomicValue v) {
        return super.isIdentical(v) && this.qName.getPrefix().equals(((QualifiedNameValue)v).getPrefix());
    }

    @Override
    public int identityHashCode() {
        return this.qName.identityHashCode();
    }

    @Override
    public String toString() {
        return "QName(\"" + this.getNamespaceURI() + "\", \"" + this.getLocalName() + "\")";
    }

    public QName toJaxpQName() {
        return this.qName.toJaxpQName();
    }

    public StructuredQName getStructuredQName() {
        return this.qName;
    }
}


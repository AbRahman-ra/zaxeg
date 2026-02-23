/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.xpath;

import javax.xml.xpath.XPathVariableResolver;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.EmptySequence;

public class JAXPVariableReference
extends Expression
implements Callable {
    private StructuredQName name;
    private XPathVariableResolver resolver;

    public JAXPVariableReference(StructuredQName name, XPathVariableResolver resolver) {
        this.name = name;
        this.resolver = resolver;
    }

    @Override
    public String getExpressionName() {
        return "$" + this.name.getDisplayName();
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        return new JAXPVariableReference(this.name, this.resolver);
    }

    @Override
    public ItemType getItemType() {
        return AnyItemType.getInstance();
    }

    @Override
    public int computeCardinality() {
        return 57344;
    }

    @Override
    public int computeSpecialProperties() {
        return 0x800000;
    }

    @Override
    public int getImplementationMethod() {
        return 2;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof JAXPVariableReference && ((JAXPVariableReference)other).name.equals(this.name) && ((JAXPVariableReference)other).resolver == this.resolver;
    }

    @Override
    public int computeHashCode() {
        return this.name.hashCode();
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        Configuration config = context.getConfiguration();
        Object value = this.resolver.resolveVariable(this.name.toJaxpQName());
        if (value == null) {
            return EmptySequence.getInstance();
        }
        JPConverter converter = JPConverter.allocate(value.getClass(), null, config);
        return converter.convert(value, context);
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        return this.call(context, null).iterate();
    }

    @Override
    public String toString() {
        return this.getExpressionName();
    }

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        destination.startElement("jaxpVar", this);
        destination.emitAttribute("name", this.name);
        destination.endElement();
    }
}


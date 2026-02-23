/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.hof;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.AbstractFunction;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.Converter;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.SequenceType;

public class AtomicConstructorFunction
extends AbstractFunction {
    private AtomicType targetType;
    private NamespaceResolver nsResolver;

    public AtomicConstructorFunction(AtomicType targetType, NamespaceResolver resolver) {
        this.targetType = targetType;
        this.nsResolver = resolver;
    }

    @Override
    public FunctionItemType getFunctionItemType() {
        return new SpecificFunctionType(new SequenceType[]{SequenceType.OPTIONAL_ATOMIC}, SequenceType.makeSequenceType(this.targetType, 24576));
    }

    @Override
    public StructuredQName getFunctionName() {
        return this.targetType.getTypeName();
    }

    @Override
    public String getDescription() {
        return this.getFunctionName().getDisplayName();
    }

    @Override
    public int getArity() {
        return 1;
    }

    @Override
    public ZeroOrOne call(XPathContext context, Sequence[] args) throws XPathException {
        AtomicValue val = (AtomicValue)args[0].head();
        if (val == null) {
            return ZeroOrOne.empty();
        }
        Configuration config = context.getConfiguration();
        Converter converter = config.getConversionRules().getConverter(val.getItemType(), this.targetType);
        if (converter == null) {
            XPathException ex = new XPathException("Cannot convert " + val.getItemType() + " to " + this.targetType, "XPTY0004");
            ex.setIsTypeError(true);
            throw ex;
        }
        converter = converter.setNamespaceResolver(this.nsResolver);
        return new ZeroOrOne<AtomicValue>(converter.convert(val).asAtomic());
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("acFnRef");
        out.emitAttribute("name", this.targetType.getTypeName());
        out.endElement();
    }

    @Override
    public boolean isTrustedResultType() {
        return true;
    }
}


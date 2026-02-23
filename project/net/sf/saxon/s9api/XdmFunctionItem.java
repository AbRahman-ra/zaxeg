/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.util.stream.Stream;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.streams.Step;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.SequenceType;

public class XdmFunctionItem
extends XdmItem {
    protected XdmFunctionItem() {
    }

    public XdmFunctionItem(Function fi) {
        this.setValue(fi);
    }

    public QName getName() {
        Function fi = (Function)this.getUnderlyingValue();
        StructuredQName sq = fi.getFunctionName();
        return sq == null ? null : new QName(sq);
    }

    public int getArity() {
        Function fi = (Function)this.getUnderlyingValue();
        return fi.getArity();
    }

    @Override
    public boolean isAtomicValue() {
        return false;
    }

    public static XdmFunctionItem getSystemFunction(Processor processor, QName name, int arity) throws SaxonApiException {
        Configuration config = processor.getUnderlyingConfiguration();
        Function f = config.getSystemFunction(name.getStructuredQName(), arity);
        return f == null ? null : new XdmFunctionItem(f);
    }

    public java.util.function.Function<? super XdmValue, ? extends XdmValue> asFunction(Processor processor) {
        if (this.getArity() == 1) {
            return arg -> {
                try {
                    return this.call(processor, (XdmValue)arg);
                } catch (SaxonApiException e) {
                    throw new SaxonApiUncheckedException(e);
                }
            };
        }
        throw new IllegalStateException("Function arity must be one");
    }

    public Step<XdmItem> asStep(final Processor processor) {
        if (this.getArity() == 1) {
            return new Step<XdmItem>(){

                @Override
                public Stream<? extends XdmItem> apply(XdmItem arg) {
                    try {
                        return XdmFunctionItem.this.call(processor, arg).stream();
                    } catch (SaxonApiException e) {
                        throw new SaxonApiUncheckedException(e);
                    }
                }
            };
        }
        throw new IllegalStateException("Function arity must be one");
    }

    public XdmValue call(Processor processor, XdmValue ... arguments) throws SaxonApiException {
        if (arguments.length != this.getArity()) {
            throw new SaxonApiException("Supplied " + arguments.length + " arguments, required " + this.getArity());
        }
        try {
            Function fi = (Function)this.getUnderlyingValue();
            FunctionItemType type = fi.getFunctionItemType();
            Sequence[] argVals = new Sequence[arguments.length];
            TypeHierarchy th = processor.getUnderlyingConfiguration().getTypeHierarchy();
            for (int i = 0; i < arguments.length; ++i) {
                Sequence val;
                SequenceType required = type.getArgumentTypes()[i];
                if (!required.matches(val = arguments[i].getUnderlyingValue(), th)) {
                    RoleDiagnostic role = new RoleDiagnostic(0, "", i);
                    val = th.applyFunctionConversionRules(val, required, role, Loc.NONE);
                }
                argVals[i] = val;
            }
            Configuration config = processor.getUnderlyingConfiguration();
            Controller controller = new Controller(config);
            XPathContext context = controller.newXPathContext();
            context = fi.makeNewContext(context, controller);
            Sequence result = fi.call(context, argVals);
            if (!fi.isTrustedResultType()) {
                result = result.materialize();
                SequenceType required = type.getResultType();
                if (!required.matches(result, th)) {
                    RoleDiagnostic role = new RoleDiagnostic(5, "", 0);
                    result = th.applyFunctionConversionRules(result.materialize(), required, role, Loc.NONE);
                }
            }
            GroundedValue se = result.iterate().materialize();
            return XdmValue.wrap(se);
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }
}


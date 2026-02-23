/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.xpath;

import java.util.ArrayList;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FunctionCall;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.PJConverter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.EmptySequence;

public class XPathFunctionCall
extends FunctionCall
implements Callable {
    private StructuredQName name;
    private XPathFunction function;

    public XPathFunctionCall(StructuredQName name, XPathFunction function) {
        this.function = function;
    }

    @Override
    public StructuredQName getFunctionName() {
        return this.name;
    }

    @Override
    public Function getTargetFunction(XPathContext context) {
        return null;
    }

    @Override
    public Expression preEvaluate(ExpressionVisitor visitor) {
        return this;
    }

    @Override
    public int getIntrinsicDependencies() {
        return 0;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        return new XPathFunctionCall(this.name, this.function);
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        return this.addExternalFunctionCallToPathMap(pathMap, pathMapNodeSet);
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        Sequence[] argValues = new Sequence[this.getArity()];
        for (int i = 0; i < argValues.length; ++i) {
            argValues[i] = SequenceTool.toLazySequence(this.getArg(i).iterate(context));
        }
        return this.call(context, argValues).iterate();
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] argValues) throws XPathException {
        ArrayList convertedArgs = new ArrayList(argValues.length);
        Configuration config = context.getConfiguration();
        for (Sequence argValue : argValues) {
            ArrayList target = new ArrayList();
            argValue.iterate().forEachOrFail(item -> {
                PJConverter converter = PJConverter.allocate(config, Type.getItemType(item, config.getTypeHierarchy()), 16384, Object.class);
                target.add(converter.convert(item, Object.class, context));
            });
            if (target.size() == 1) {
                convertedArgs.add(target.get(0));
                continue;
            }
            convertedArgs.add(target);
        }
        try {
            Object result = this.function.evaluate(convertedArgs);
            if (result == null) {
                return EmptySequence.getInstance();
            }
            JPConverter converter = JPConverter.allocate(result.getClass(), null, config);
            return converter.convert(result, context);
        } catch (XPathFunctionException e) {
            throw new XPathException(e);
        }
    }

    @Override
    public ItemType getItemType() {
        return Type.ITEM_TYPE;
    }

    @Override
    public int computeCardinality() {
        return 57344;
    }
}


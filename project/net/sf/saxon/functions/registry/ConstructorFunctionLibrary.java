/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.registry;

import java.util.List;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.CastExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ListConstructorFunction;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.StaticFunctionCall;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.UnionConstructorFunction;
import net.sf.saxon.functions.CallableFunction;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.functions.hof.AtomicConstructorFunction;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnySimpleType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.ListType;
import net.sf.saxon.type.MissingComponentException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.UnionType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;

public class ConstructorFunctionLibrary
implements FunctionLibrary {
    private Configuration config;

    public ConstructorFunctionLibrary(Configuration config) {
        this.config = config;
    }

    @Override
    public Function getFunctionItem(SymbolicName.F functionName, StaticContext staticContext) throws XPathException {
        NamespaceResolver resolver;
        String localName;
        if (functionName.getArity() != 1) {
            return null;
        }
        String uri = functionName.getComponentName().getURI();
        SchemaType type = this.config.getSchemaType(new StructuredQName("", uri, localName = functionName.getComponentName().getLocalPart()));
        if (type == null || type.isComplexType()) {
            return null;
        }
        NamespaceResolver namespaceResolver = resolver = ((SimpleType)type).isNamespaceSensitive() ? staticContext.getNamespaceResolver() : null;
        if (type instanceof AtomicType) {
            return new AtomicConstructorFunction((AtomicType)type, resolver);
        }
        if (type instanceof ListType) {
            return new ListConstructorFunction((ListType)type, resolver, true);
        }
        Callable callable = (context, arguments) -> {
            AtomicValue value = (AtomicValue)arguments[0].head();
            if (value == null) {
                return EmptySequence.getInstance();
            }
            return UnionConstructorFunction.cast(value, (UnionType)((Object)type), resolver, context.getConfiguration().getConversionRules());
        };
        SequenceType returnType = ((UnionType)((Object)type)).getResultTypeOfCast();
        return new CallableFunction(1, callable, (FunctionItemType)new SpecificFunctionType(new SequenceType[]{SequenceType.OPTIONAL_ATOMIC}, returnType));
    }

    @Override
    public boolean isAvailable(SymbolicName.F functionName) {
        String localName;
        if (functionName.getArity() != 1) {
            return false;
        }
        String uri = functionName.getComponentName().getURI();
        SchemaType type = this.config.getSchemaType(new StructuredQName("", uri, localName = functionName.getComponentName().getLocalPart()));
        if (type == null || type.isComplexType()) {
            return false;
        }
        if (type.isAtomicType() && ((AtomicType)type).isAbstract()) {
            return false;
        }
        return type != AnySimpleType.getInstance();
    }

    @Override
    public Expression bind(SymbolicName.F functionName, Expression[] arguments, StaticContext env, List<String> reasons) {
        SchemaType st;
        String uri = functionName.getComponentName().getURI();
        String localName = functionName.getComponentName().getLocalPart();
        boolean builtInNamespace = uri.equals("http://www.w3.org/2001/XMLSchema");
        if (builtInNamespace) {
            if (functionName.getArity() != 1) {
                reasons.add("A constructor function must have exactly one argument");
                return null;
            }
            SimpleType type = Type.getBuiltInSimpleType(uri, localName);
            if (type != null) {
                if (type.isAtomicType()) {
                    if (((AtomicType)type).isAbstract()) {
                        reasons.add("Abstract type used in constructor function: {" + uri + '}' + localName);
                        return null;
                    }
                    CastExpression cast = new CastExpression(arguments[0], (AtomicType)type, true);
                    if (arguments[0] instanceof StringLiteral) {
                        cast.setOperandIsStringLiteral(true);
                    }
                    return cast;
                }
                if (type.isUnionType()) {
                    NamespaceResolver resolver = env.getNamespaceResolver();
                    UnionConstructorFunction ucf = new UnionConstructorFunction((UnionType)((Object)type), resolver, true);
                    return new StaticFunctionCall(ucf, arguments);
                }
                NamespaceResolver resolver = env.getNamespaceResolver();
                try {
                    ListConstructorFunction lcf = new ListConstructorFunction((ListType)type, resolver, true);
                    return new StaticFunctionCall(lcf, arguments);
                } catch (MissingComponentException e) {
                    reasons.add("Missing schema component: " + e.getMessage());
                    return null;
                }
            }
            reasons.add("Unknown constructor function: {" + uri + '}' + localName);
            return null;
        }
        if (arguments.length == 1 && (st = this.config.getSchemaType(new StructuredQName("", uri, localName))) instanceof SimpleType) {
            if (st instanceof AtomicType) {
                return new CastExpression(arguments[0], (AtomicType)st, true);
            }
            if (st instanceof ListType && env.getXPathVersion() >= 30) {
                NamespaceResolver resolver = env.getNamespaceResolver();
                try {
                    ListConstructorFunction lcf = new ListConstructorFunction((ListType)st, resolver, true);
                    return new StaticFunctionCall(lcf, arguments);
                } catch (MissingComponentException e) {
                    reasons.add("Missing schema component: " + e.getMessage());
                    return null;
                }
            }
            if (((SimpleType)st).isUnionType() && env.getXPathVersion() >= 30) {
                NamespaceResolver resolver = env.getNamespaceResolver();
                UnionConstructorFunction ucf = new UnionConstructorFunction((UnionType)((Object)st), resolver, true);
                return new StaticFunctionCall(ucf, arguments);
            }
        }
        return null;
    }

    @Override
    public FunctionLibrary copy() {
        return this;
    }
}


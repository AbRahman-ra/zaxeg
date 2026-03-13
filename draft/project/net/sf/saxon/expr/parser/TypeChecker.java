/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.parser;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.ArithmeticExpression;
import net.sf.saxon.expr.AtomicSequenceConverter;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.CardinalityChecker;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FunctionCall;
import net.sf.saxon.expr.GeneralComparison20;
import net.sf.saxon.expr.ItemChecker;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.UntypedSequenceConverter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.ma.map.TupleType;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.Converter;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.type.NumericType;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.SequenceType;

public class TypeChecker {
    public Expression staticTypeCheck(Expression supplied, SequenceType req, RoleDiagnostic role, ExpressionVisitor visitor) throws XPathException {
        String msg;
        Affinity relation;
        Expression cexp;
        boolean cardOK;
        if (supplied.implementsStaticTypeCheck()) {
            return supplied.staticTypeCheck(req, false, role, visitor);
        }
        Expression exp = supplied;
        StaticContext env = visitor.getStaticContext();
        Configuration config = env.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();
        ContextItemStaticInfo defaultContextInfo = config.getDefaultContextItemStaticInfo();
        ItemType reqItemType = req.getPrimaryType();
        int reqCard = req.getCardinality();
        boolean allowsMany = Cardinality.allowsMany(reqCard);
        ItemType suppliedItemType = null;
        int suppliedCard = -1;
        boolean bl = cardOK = reqCard == 57344;
        if (!cardOK) {
            suppliedCard = exp.getCardinality();
            cardOK = Cardinality.subsumes(reqCard, suppliedCard);
        }
        boolean itemTypeOK = reqItemType instanceof AnyItemType;
        if (reqCard == 8192) {
            itemTypeOK = true;
        }
        if (!itemTypeOK) {
            suppliedItemType = exp.getItemType();
            if (reqItemType == null || suppliedItemType == null) {
                throw new NullPointerException();
            }
            Affinity relation2 = th.relationship(reqItemType, suppliedItemType);
            boolean bl2 = itemTypeOK = relation2 == Affinity.SAME_TYPE || relation2 == Affinity.SUBSUMES;
        }
        if (!itemTypeOK) {
            if (reqItemType.isPlainType()) {
                Expression cexp2;
                if (!suppliedItemType.isPlainType() && suppliedCard != 8192) {
                    if (!suppliedItemType.isAtomizable(th)) {
                        String shortItemType = suppliedItemType instanceof TupleType ? "a tuple type" : (suppliedItemType instanceof MapType ? "a map type" : (suppliedItemType instanceof FunctionItemType ? "a function type" : (suppliedItemType instanceof NodeTest ? "an element type with element-only content" : suppliedItemType.toString())));
                        XPathException err = new XPathException("An atomic value is required for the " + role.getMessage() + ", but the supplied type is " + shortItemType + ", which cannot be atomized", "FOTY0013", supplied.getLocation());
                        err.setIsTypeError(true);
                        err.setFailingExpression(supplied);
                        throw err;
                    }
                    if (exp.getRetainedStaticContext() == null) {
                        exp.setRetainedStaticContextLocally(env.makeRetainedStaticContext());
                    }
                    cexp2 = Atomizer.makeAtomizer(exp, role);
                    ExpressionTool.copyLocationInfo(exp, cexp2);
                    exp = cexp2;
                    cexp2 = exp.simplify();
                    ExpressionTool.copyLocationInfo(exp, cexp2);
                    exp = cexp2;
                    suppliedItemType = exp.getItemType();
                    suppliedCard = exp.getCardinality();
                    cardOK = Cardinality.subsumes(reqCard, suppliedCard);
                }
                if (suppliedItemType.equals(BuiltInAtomicType.UNTYPED_ATOMIC) && !reqItemType.equals(BuiltInAtomicType.UNTYPED_ATOMIC) && !reqItemType.equals(BuiltInAtomicType.ANY_ATOMIC)) {
                    if (((PlainType)reqItemType).isNamespaceSensitive()) {
                        XPathException err = new XPathException("An untyped atomic value cannot be converted to a QName or NOTATION as required for the " + role.getMessage(), "XPTY0117", supplied.getLocation());
                        err.setIsTypeError(true);
                        throw err;
                    }
                    cexp2 = UntypedSequenceConverter.makeUntypedSequenceConverter(config, exp, (PlainType)reqItemType);
                    ((AtomicSequenceConverter)cexp2).setRoleDiagnostic(role);
                    ExpressionTool.copyLocationInfo(exp, cexp2);
                    try {
                        if (exp instanceof Literal) {
                            exp = Literal.makeLiteral(((AtomicSequenceConverter)cexp2).iterate(visitor.makeDynamicContext()).materialize(), exp);
                            ExpressionTool.copyLocationInfo(cexp2, exp);
                        } else {
                            exp = cexp2;
                        }
                    } catch (XPathException err) {
                        err.maybeSetLocation(exp.getLocation());
                        err.setFailingExpression(supplied);
                        err.setErrorCode(role.getErrorCode());
                        err.setIsStaticError(true);
                        throw err;
                    }
                    itemTypeOK = true;
                    suppliedItemType = reqItemType;
                }
                if (suppliedItemType.equals(BuiltInAtomicType.ANY_ATOMIC) && !reqItemType.equals(BuiltInAtomicType.UNTYPED_ATOMIC) && !reqItemType.equals(BuiltInAtomicType.ANY_ATOMIC) && !exp.hasSpecialProperty(0x4000000)) {
                    UntypedSequenceConverter conversion;
                    if (((PlainType)reqItemType).isNamespaceSensitive()) {
                        conversion = UntypedSequenceConverter.makeUntypedSequenceRejector(config, exp, (PlainType)reqItemType);
                    } else {
                        UntypedSequenceConverter usc = UntypedSequenceConverter.makeUntypedSequenceConverter(config, exp, (PlainType)reqItemType);
                        usc.setRoleDiagnostic(role);
                        conversion = usc;
                    }
                    ExpressionTool.copyLocationInfo(exp, conversion);
                    try {
                        if (exp instanceof Literal) {
                            exp = Literal.makeLiteral(((Expression)conversion).iterate(visitor.makeDynamicContext()).materialize(), exp);
                            ExpressionTool.copyLocationInfo(supplied, exp);
                        } else {
                            exp = conversion;
                        }
                        suppliedItemType = exp.getItemType();
                    } catch (XPathException err) {
                        err.maybeSetLocation(exp.getLocation());
                        err.setIsStaticError(true);
                        throw err;
                    }
                }
                if (reqItemType instanceof AtomicType) {
                    int rt = ((AtomicType)reqItemType).getFingerprint();
                    if (rt == 517 && th.relationship(suppliedItemType, NumericType.getInstance()) != Affinity.DISJOINT) {
                        cexp = TypeChecker.makePromoterToDouble(exp);
                        if (cexp instanceof AtomicSequenceConverter) {
                            ((AtomicSequenceConverter)cexp).setRoleDiagnostic(role);
                        }
                        ExpressionTool.copyLocationInfo(exp, cexp);
                        exp = cexp;
                        try {
                            exp = exp.simplify().typeCheck(visitor, defaultContextInfo);
                        } catch (XPathException err) {
                            err.maybeSetLocation(exp.getLocation());
                            err.setIsStaticError(true);
                            throw err;
                        }
                        suppliedItemType = BuiltInAtomicType.DOUBLE;
                        suppliedCard = -1;
                    } else if (rt == 516 && th.relationship(suppliedItemType, NumericType.getInstance()) != Affinity.DISJOINT && !th.isSubType(suppliedItemType, BuiltInAtomicType.DOUBLE)) {
                        cexp = TypeChecker.makePromoterToFloat(exp);
                        if (cexp instanceof AtomicSequenceConverter) {
                            ((AtomicSequenceConverter)cexp).setRoleDiagnostic(role);
                        }
                        ExpressionTool.copyLocationInfo(exp, cexp);
                        exp = cexp;
                        try {
                            exp = exp.simplify().typeCheck(visitor, defaultContextInfo);
                        } catch (XPathException err) {
                            err.maybeSetLocation(exp.getLocation());
                            err.setFailingExpression(supplied);
                            err.setIsStaticError(true);
                            throw err;
                        }
                        suppliedItemType = BuiltInAtomicType.FLOAT;
                        suppliedCard = -1;
                    }
                    if (rt == 513 && th.isSubType(suppliedItemType, BuiltInAtomicType.ANY_URI)) {
                        itemTypeOK = true;
                        cexp = TypeChecker.makePromoterToString(exp);
                        if (cexp instanceof AtomicSequenceConverter) {
                            ((AtomicSequenceConverter)cexp).setRoleDiagnostic(role);
                        }
                        ExpressionTool.copyLocationInfo(exp, cexp);
                        exp = cexp;
                        try {
                            exp = exp.simplify().typeCheck(visitor, defaultContextInfo);
                        } catch (XPathException err) {
                            err.maybeSetLocation(exp.getLocation());
                            err.setFailingExpression(supplied);
                            err.setIsStaticError(true);
                            throw err;
                        }
                        suppliedItemType = BuiltInAtomicType.STRING;
                        suppliedCard = -1;
                    }
                }
            } else if (reqItemType instanceof FunctionItemType && !((FunctionItemType)reqItemType).isMapType() && !((FunctionItemType)reqItemType).isArrayType()) {
                Affinity r = th.relationship(suppliedItemType, th.getGenericFunctionItemType());
                if (r != Affinity.DISJOINT) {
                    if (!(suppliedItemType instanceof FunctionItemType)) {
                        exp = new ItemChecker(exp, th.getGenericFunctionItemType(), role);
                        suppliedItemType = th.getGenericFunctionItemType();
                    }
                    exp = TypeChecker.makeFunctionSequenceCoercer(exp, (FunctionItemType)reqItemType, visitor, role);
                    itemTypeOK = true;
                }
            } else if (reqItemType instanceof JavaExternalObjectType && reqCard == 16384) {
                if (Sequence.class.isAssignableFrom(((JavaExternalObjectType)reqItemType).getJavaClass())) {
                    itemTypeOK = true;
                } else if (supplied instanceof FunctionCall && ((FunctionCall)supplied).adjustRequiredType((JavaExternalObjectType)reqItemType)) {
                    itemTypeOK = true;
                    cardOK = true;
                }
            }
        }
        if (itemTypeOK && cardOK) {
            return exp;
        }
        if (suppliedCard == -1) {
            suppliedCard = exp.getCardinality();
            if (!cardOK) {
                cardOK = Cardinality.subsumes(reqCard, suppliedCard);
            }
        }
        if (cardOK && suppliedCard == 8192) {
            return exp;
        }
        if (suppliedCard == 8192 && (reqCard & 0x2000) == 0) {
            XPathException err = new XPathException("An empty sequence is not allowed as the " + role.getMessage(), role.getErrorCode(), supplied.getLocation());
            err.setIsTypeError(role.isTypeError());
            err.setFailingExpression(supplied);
            throw err;
        }
        Affinity affinity = relation = itemTypeOK ? Affinity.SUBSUMED_BY : th.relationship(suppliedItemType, reqItemType);
        if (reqCard == 8192) {
            relation = Affinity.SAME_TYPE;
        }
        if (relation == Affinity.DISJOINT) {
            if (Cardinality.allowsZero(suppliedCard) && Cardinality.allowsZero(reqCard)) {
                if (suppliedCard != 8192) {
                    msg = role.composeErrorMessage(reqItemType, supplied, th);
                    msg = msg + ". The expression can succeed only if the supplied value is an empty sequence.";
                    visitor.issueWarning(msg, supplied.getLocation());
                }
            } else {
                msg = role.composeErrorMessage(reqItemType, supplied, th);
                XPathException err = new XPathException(msg, role.getErrorCode(), supplied.getLocation());
                err.setIsTypeError(role.isTypeError());
                err.setFailingExpression(supplied);
                throw err;
            }
        }
        if (relation != Affinity.SAME_TYPE && relation != Affinity.SUBSUMED_BY) {
            if (exp instanceof Literal) {
                if (req.matches(((Literal)exp).getValue(), th)) {
                    return exp;
                }
                msg = role.composeErrorMessage(reqItemType, supplied, th);
                XPathException err = new XPathException(msg, role.getErrorCode(), supplied.getLocation());
                err.setIsTypeError(role.isTypeError());
                throw err;
            }
            cexp = new ItemChecker(exp, reqItemType, role);
            ExpressionTool.copyLocationInfo(exp, cexp);
            exp = cexp;
        }
        if (!cardOK) {
            if (exp instanceof Literal) {
                XPathException err = new XPathException("Required cardinality of " + role.getMessage() + " is " + Cardinality.toString(reqCard) + "; supplied value has cardinality " + Cardinality.toString(suppliedCard), role.getErrorCode(), supplied.getLocation());
                err.setIsTypeError(role.isTypeError());
                throw err;
            }
            cexp = CardinalityChecker.makeCardinalityChecker(exp, reqCard, role);
            ExpressionTool.copyLocationInfo(exp, cexp);
            exp = cexp;
        }
        return exp;
    }

    public Expression makeArithmeticExpression(Expression lhs, int operator, Expression rhs) {
        return new ArithmeticExpression(lhs, operator, rhs);
    }

    public Expression makeGeneralComparison(Expression lhs, int operator, Expression rhs) {
        return new GeneralComparison20(lhs, operator, rhs);
    }

    public Expression processValueOf(Expression select, Configuration config) {
        return select;
    }

    private static Expression makeFunctionSequenceCoercer(Expression exp, FunctionItemType reqItemType, ExpressionVisitor visitor, RoleDiagnostic role) throws XPathException {
        return reqItemType.makeFunctionSequenceCoercer(exp, role);
    }

    public static Expression strictTypeCheck(Expression supplied, SequenceType req, RoleDiagnostic role, StaticContext env) throws XPathException {
        Expression cexp;
        Affinity relation;
        boolean itemTypeOK;
        boolean cardOK;
        Expression exp = supplied;
        TypeHierarchy th = env.getConfiguration().getTypeHierarchy();
        ItemType reqItemType = req.getPrimaryType();
        int reqCard = req.getCardinality();
        ItemType suppliedItemType = null;
        int suppliedCard = -1;
        boolean bl = cardOK = reqCard == 57344;
        if (!cardOK) {
            suppliedCard = exp.getCardinality();
            cardOK = Cardinality.subsumes(reqCard, suppliedCard);
        }
        if (!(itemTypeOK = req.getPrimaryType() instanceof AnyItemType)) {
            suppliedItemType = exp.getItemType();
            relation = th.relationship(reqItemType, suppliedItemType);
            boolean bl2 = itemTypeOK = relation == Affinity.SAME_TYPE || relation == Affinity.SUBSUMES;
        }
        if (itemTypeOK && cardOK) {
            return exp;
        }
        if (suppliedCard == -1) {
            suppliedCard = suppliedItemType instanceof ErrorType ? 8192 : exp.getCardinality();
            if (!cardOK) {
                cardOK = Cardinality.subsumes(reqCard, suppliedCard);
            }
        }
        if (cardOK && suppliedCard == 8192) {
            return exp;
        }
        if (suppliedItemType == null) {
            suppliedItemType = exp.getItemType();
        }
        if (suppliedCard == 8192 && (reqCard & 0x2000) == 0) {
            XPathException err = new XPathException("An empty sequence is not allowed as the " + role.getMessage(), role.getErrorCode(), supplied.getLocation());
            err.setIsTypeError(role.isTypeError());
            throw err;
        }
        relation = th.relationship(suppliedItemType, reqItemType);
        if (relation == Affinity.DISJOINT) {
            if (Cardinality.allowsZero(suppliedCard) && Cardinality.allowsZero(reqCard)) {
                if (suppliedCard != 8192) {
                    String msg = "Required item type of " + role.getMessage() + " is " + reqItemType + "; supplied value (" + supplied.toShortString() + ") has item type " + suppliedItemType + ". The expression can succeed only if the supplied value is an empty sequence.";
                    env.issueWarning(msg, supplied.getLocation());
                }
            } else {
                String msg = role.composeErrorMessage(reqItemType, supplied, th);
                XPathException err = new XPathException(msg, role.getErrorCode(), supplied.getLocation());
                err.setIsTypeError(role.isTypeError());
                throw err;
            }
        }
        if (relation != Affinity.SAME_TYPE && relation != Affinity.SUBSUMED_BY) {
            cexp = new ItemChecker(exp, reqItemType, role);
            cexp.adoptChildExpression(exp);
            exp = cexp;
        }
        if (!cardOK) {
            if (exp instanceof Literal) {
                XPathException err = new XPathException("Required cardinality of " + role.getMessage() + " is " + Cardinality.toString(reqCard) + "; supplied value has cardinality " + Cardinality.toString(suppliedCard), role.getErrorCode(), supplied.getLocation());
                err.setIsTypeError(role.isTypeError());
                throw err;
            }
            cexp = CardinalityChecker.makeCardinalityChecker(exp, reqCard, role);
            cexp.adoptChildExpression(exp);
            exp = cexp;
        }
        return exp;
    }

    public static XPathException testConformance(Sequence val, SequenceType requiredType, XPathContext context) throws XPathException {
        Item item;
        ItemType reqItemType = requiredType.getPrimaryType();
        SequenceIterator iter = val.iterate();
        int count = 0;
        while ((item = iter.next()) != null) {
            ++count;
            if (reqItemType.matches(item, context.getConfiguration().getTypeHierarchy())) continue;
            XPathException err = new XPathException("Required type is " + reqItemType + "; supplied value has type " + UType.getUType(val.materialize()));
            err.setIsTypeError(true);
            err.setErrorCode("XPTY0004");
            return err;
        }
        int reqCardinality = requiredType.getCardinality();
        if (count == 0 && !Cardinality.allowsZero(reqCardinality)) {
            XPathException err = new XPathException("Required type does not allow empty sequence, but supplied value is empty");
            err.setIsTypeError(true);
            err.setErrorCode("XPTY0004");
            return err;
        }
        if (count > 1 && !Cardinality.allowsMany(reqCardinality)) {
            XPathException err = new XPathException("Required type requires a singleton sequence; supplied value contains " + count + " items");
            err.setIsTypeError(true);
            err.setErrorCode("XPTY0004");
            return err;
        }
        if (count > 0 && reqCardinality == 8192) {
            XPathException err = new XPathException("Required type requires an empty sequence, but supplied value is non-empty");
            err.setIsTypeError(true);
            err.setErrorCode("XPTY0004");
            return err;
        }
        return null;
    }

    public static XPathException ebvError(Expression exp, TypeHierarchy th) {
        if (Cardinality.allowsZero(exp.getCardinality())) {
            return null;
        }
        ItemType t = exp.getItemType();
        if (th.relationship(t, Type.NODE_TYPE) == Affinity.DISJOINT && th.relationship(t, BuiltInAtomicType.BOOLEAN) == Affinity.DISJOINT && th.relationship(t, BuiltInAtomicType.STRING) == Affinity.DISJOINT && th.relationship(t, BuiltInAtomicType.ANY_URI) == Affinity.DISJOINT && th.relationship(t, BuiltInAtomicType.UNTYPED_ATOMIC) == Affinity.DISJOINT && th.relationship(t, NumericType.getInstance()) == Affinity.DISJOINT && !(t instanceof JavaExternalObjectType)) {
            XPathException err = new XPathException("Effective boolean value is defined only for sequences containing booleans, strings, numbers, URIs, or nodes");
            err.setErrorCode("FORG0006");
            err.setIsTypeError(true);
            return err;
        }
        return null;
    }

    private static Expression makePromoterToDouble(Expression exp) {
        return TypeChecker.makePromoter(exp, new Converter.PromoterToDouble(), BuiltInAtomicType.DOUBLE);
    }

    private static Expression makePromoterToFloat(Expression exp) {
        return TypeChecker.makePromoter(exp, new Converter.PromoterToFloat(), BuiltInAtomicType.FLOAT);
    }

    private static Expression makePromoterToString(Expression exp) {
        return TypeChecker.makePromoter(exp, new Converter.ToStringConverter(), BuiltInAtomicType.STRING);
    }

    private static Expression makePromoter(Expression exp, Converter converter, BuiltInAtomicType type) {
        ConversionResult result;
        ConversionRules rules = exp.getConfiguration().getConversionRules();
        converter.setConversionRules(rules);
        if (exp instanceof Literal && ((Literal)exp).getValue() instanceof AtomicValue && (result = converter.convert((AtomicValue)((Literal)exp).getValue())) instanceof AtomicValue) {
            Literal converted = Literal.makeLiteral((AtomicValue)result, exp);
            ExpressionTool.copyLocationInfo(exp, converted);
            return converted;
        }
        AtomicSequenceConverter asc = new AtomicSequenceConverter(exp, type);
        asc.setConverter(converter);
        ExpressionTool.copyLocationInfo(exp, asc);
        return asc;
    }
}


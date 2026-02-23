/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.compat;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.AtomicSequenceConverter;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FirstItemExpression;
import net.sf.saxon.expr.compat.ArithmeticExpression10;
import net.sf.saxon.expr.compat.GeneralComparison10;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.NumericType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.SequenceType;

public class TypeChecker10
extends TypeChecker {
    @Override
    public Expression staticTypeCheck(Expression supplied, SequenceType req, RoleDiagnostic role, ExpressionVisitor visitor) throws XPathException {
        if (supplied.implementsStaticTypeCheck()) {
            return supplied.staticTypeCheck(req, true, role, visitor);
        }
        Configuration config = visitor.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();
        if (!Cardinality.allowsMany(req.getCardinality()) && Cardinality.allowsMany(supplied.getCardinality())) {
            Expression cexp = FirstItemExpression.makeFirstItemExpression(supplied);
            cexp.adoptChildExpression(supplied);
            supplied = cexp;
        }
        ItemType reqItemType = req.getPrimaryType();
        if (req.getPrimaryType().equals(BuiltInAtomicType.STRING) && !Cardinality.allowsMany(req.getCardinality()) && !th.isSubType(supplied.getItemType(), BuiltInAtomicType.STRING)) {
            RetainedStaticContext rsc = new RetainedStaticContext(config);
            Expression fn = SystemFunction.makeCall("string", rsc, supplied);
            try {
                return fn.typeCheck(visitor, config.getDefaultContextItemStaticInfo());
            } catch (XPathException err) {
                err.maybeSetLocation(supplied.getLocation());
                err.setIsStaticError(true);
                throw err;
            }
        }
        if (reqItemType.equals(NumericType.getInstance()) || reqItemType.equals(BuiltInAtomicType.DOUBLE) && !Cardinality.allowsMany(req.getCardinality()) && !th.isSubType(supplied.getItemType(), BuiltInAtomicType.DOUBLE)) {
            RetainedStaticContext rsc = new RetainedStaticContext(config);
            Expression fn = SystemFunction.makeCall("number", rsc, supplied);
            try {
                return fn.typeCheck(visitor, config.getDefaultContextItemStaticInfo());
            } catch (XPathException err) {
                err.maybeSetLocation(supplied.getLocation());
                err.setIsStaticError(true);
                throw err;
            }
        }
        return super.staticTypeCheck(supplied, req, role, visitor);
    }

    @Override
    public Expression makeArithmeticExpression(Expression lhs, int operator, Expression rhs) {
        return new ArithmeticExpression10(lhs, operator, rhs);
    }

    @Override
    public Expression makeGeneralComparison(Expression lhs, int operator, Expression rhs) {
        return new GeneralComparison10(lhs, operator, rhs);
    }

    @Override
    public Expression processValueOf(Expression select, Configuration config) {
        TypeHierarchy th = config.getTypeHierarchy();
        if (!select.getItemType().isPlainType()) {
            select = Atomizer.makeAtomizer(select, null);
        }
        if (Cardinality.allowsMany(select.getCardinality())) {
            select = FirstItemExpression.makeFirstItemExpression(select);
        }
        if (!th.isSubType(select.getItemType(), BuiltInAtomicType.STRING)) {
            select = new AtomicSequenceConverter(select, BuiltInAtomicType.STRING);
            ((AtomicSequenceConverter)select).allocateConverterStatically(config, false);
        }
        return select;
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemChecker;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.SequenceType;

public class AnyFunctionType
implements FunctionItemType {
    public static final AnyFunctionType ANY_FUNCTION = new AnyFunctionType();

    public static AnyFunctionType getInstance() {
        return ANY_FUNCTION;
    }

    @Override
    public UType getUType() {
        return UType.FUNCTION;
    }

    @Override
    public boolean isAtomicType() {
        return false;
    }

    @Override
    public boolean isPlainType() {
        return false;
    }

    @Override
    public boolean isMapType() {
        return false;
    }

    @Override
    public boolean isArrayType() {
        return false;
    }

    @Override
    public double getDefaultPriority() {
        return -0.5;
    }

    @Override
    public String getBasicAlphaCode() {
        return "F";
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return null;
    }

    @Override
    public AnnotationList getAnnotationAssertions() {
        return AnnotationList.EMPTY;
    }

    @Override
    public boolean matches(Item item, TypeHierarchy th) throws XPathException {
        return item instanceof Function;
    }

    @Override
    public final ItemType getPrimitiveItemType() {
        return ANY_FUNCTION;
    }

    @Override
    public final int getPrimitiveType() {
        return 99;
    }

    @Override
    public String toString() {
        return "function(*)";
    }

    @Override
    public PlainType getAtomizedItemType() {
        return null;
    }

    @Override
    public boolean isAtomizable(TypeHierarchy th) {
        return true;
    }

    @Override
    public Affinity relationship(FunctionItemType other, TypeHierarchy th) {
        if (other == this) {
            return Affinity.SAME_TYPE;
        }
        return Affinity.SUBSUMES;
    }

    @Override
    public Expression makeFunctionSequenceCoercer(Expression exp, RoleDiagnostic role) throws XPathException {
        return new ItemChecker(exp, this, role);
    }

    @Override
    public SequenceType getResultType() {
        return SequenceType.ANY_SEQUENCE;
    }
}


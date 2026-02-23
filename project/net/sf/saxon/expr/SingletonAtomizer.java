/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.CardinalityChecker;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.UnaryExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.TerminationException;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.functions.Error;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Cardinality;

public final class SingletonAtomizer
extends UnaryExpression {
    private boolean allowEmpty;
    private RoleDiagnostic roleDiagnostic;

    public SingletonAtomizer(Expression sequence, RoleDiagnostic role, boolean allowEmpty) {
        super(sequence);
        this.allowEmpty = allowEmpty;
        this.roleDiagnostic = role;
    }

    @Override
    protected OperandRole getOperandRole() {
        return OperandRole.SINGLE_ATOMIC;
    }

    public boolean isAllowEmpty() {
        return this.allowEmpty;
    }

    @Override
    public Expression simplify() throws XPathException {
        Expression operand = this.getBaseExpression().simplify();
        if (operand instanceof Literal && ((Literal)operand).getValue() instanceof AtomicValue) {
            return operand;
        }
        this.setBaseExpression(operand);
        return this;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getOperand().typeCheck(visitor, contextInfo);
        Expression operand = this.getBaseExpression();
        ExpressionTool.resetStaticProperties(this);
        if (Literal.isEmptySequence(operand)) {
            if (!this.allowEmpty) {
                this.typeError("An empty sequence is not allowed as the " + this.roleDiagnostic.getMessage(), this.roleDiagnostic.getErrorCode(), null);
            }
            return operand;
        }
        ItemType operandType = operand.getItemType();
        if (operandType.isPlainType()) {
            return operand;
        }
        if (!operandType.isAtomizable(visitor.getConfiguration().getTypeHierarchy())) {
            XPathException err = operandType instanceof MapType ? new XPathException("Cannot atomize a map (" + this.toShortString() + ")", "FOTY0013") : (operandType instanceof FunctionItemType ? new XPathException("Cannot atomize a function item", "FOTY0013") : new XPathException("Cannot atomize an element that is defined in the schema to have element-only content", "FOTY0012"));
            err.setIsTypeError(true);
            err.setLocation(this.getLocation());
            err.setFailingExpression(this.getParentExpression());
            throw err;
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Expression exp = super.optimize(visitor, contextInfo);
        if (exp == this) {
            this.setBaseExpression(this.getBaseExpression().unordered(true, false));
            if (this.getBaseExpression().getItemType().isPlainType() && !Cardinality.allowsMany(this.getBaseExpression().getCardinality())) {
                return this.getBaseExpression();
            }
            return this;
        }
        return exp;
    }

    @Override
    public int computeSpecialProperties() {
        int p = super.computeSpecialProperties();
        return p | 0x800000;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        SingletonAtomizer e2 = new SingletonAtomizer(this.getBaseExpression().copy(rebindings), this.roleDiagnostic, this.allowEmpty);
        ExpressionTool.copyLocationInfo(this, e2);
        return e2;
    }

    @Override
    public int getImplementationMethod() {
        return 1;
    }

    @Override
    public String getStreamerName() {
        return "SingletonAtomizer";
    }

    public RoleDiagnostic getRole() {
        return this.roleDiagnostic;
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        ItemType operandItemType;
        TypeHierarchy th;
        PathMap.PathMapNodeSet result = this.getBaseExpression().addToPathMap(pathMap, pathMapNodeSet);
        if (result != null && ((th = this.getConfiguration().getTypeHierarchy()).relationship(NodeKindTest.ELEMENT, operandItemType = this.getBaseExpression().getItemType()) != Affinity.DISJOINT || th.relationship(NodeKindTest.DOCUMENT, operandItemType) != Affinity.DISJOINT)) {
            result.setAtomized();
        }
        return null;
    }

    @Override
    public AtomicValue evaluateItem(XPathContext context) throws XPathException {
        Item item;
        int found = 0;
        AtomicValue result = null;
        SequenceIterator iter = this.getBaseExpression().iterate(context);
        while ((item = iter.next()) != null) {
            AtomicSequence seq;
            try {
                seq = item.atomize();
            } catch (TerminationException | Error.UserDefinedXPathException e) {
                throw e;
            } catch (XPathException e) {
                if (this.roleDiagnostic == null) {
                    throw e;
                }
                String message = e.getMessage() + ". Failed while atomizing the " + this.roleDiagnostic.getMessage();
                XPathException e2 = new XPathException(message, e.getErrorCodeLocalPart(), e.getLocator());
                e2.setXPathContext(context);
                e2.maybeSetLocation(this.getLocation());
                throw e2;
            }
            if ((found += seq.getLength()) > 1) {
                this.typeError("A sequence of more than one item is not allowed as the " + this.roleDiagnostic.getMessage() + CardinalityChecker.depictSequenceStart(this.getBaseExpression().iterate(context), 3), this.roleDiagnostic.getErrorCode(), context);
            }
            if (found != 1) continue;
            result = seq.head();
        }
        if (found == 0 && !this.allowEmpty) {
            this.typeError("An empty sequence is not allowed as the " + this.roleDiagnostic.getMessage(), this.roleDiagnostic.getErrorCode(), null);
        }
        return result;
    }

    @Override
    public ItemType getItemType() {
        boolean isSchemaAware;
        block10: {
            isSchemaAware = true;
            try {
                isSchemaAware = this.getPackageData().isSchemaAware();
            } catch (NullPointerException err) {
                if (this.getConfiguration().isLicensedFeature(1)) break block10;
                isSchemaAware = false;
            }
        }
        ItemType in = this.getBaseExpression().getItemType();
        if (in.isPlainType()) {
            return in;
        }
        if (in instanceof NodeTest) {
            UType kinds = in.getUType();
            if (!isSchemaAware) {
                if (Atomizer.STRING_KINDS.subsumes(kinds)) {
                    return BuiltInAtomicType.STRING;
                }
                if (Atomizer.UNTYPED_IF_UNTYPED_KINDS.subsumes(kinds)) {
                    return BuiltInAtomicType.UNTYPED_ATOMIC;
                }
            } else if (Atomizer.UNTYPED_KINDS.subsumes(kinds)) {
                return BuiltInAtomicType.UNTYPED_ATOMIC;
            }
            return in.getAtomizedItemType();
        }
        if (in instanceof JavaExternalObjectType) {
            return in.getAtomizedItemType();
        }
        return BuiltInAtomicType.ANY_ATOMIC;
    }

    @Override
    public int computeCardinality() {
        if (this.allowEmpty) {
            return 24576;
        }
        return 16384;
    }

    @Override
    public String getExpressionName() {
        return "atomizeSingleton";
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("atomSing", this);
        if (this.allowEmpty) {
            out.emitAttribute("card", "?");
        }
        out.emitAttribute("diag", this.getRole().save());
        this.getBaseExpression().export(out);
        out.endElement();
    }

    @Override
    public String toShortString() {
        return this.getBaseExpression().toShortString();
    }
}


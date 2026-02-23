/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.hof;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.EarlyEvaluationContext;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemMappingFunction;
import net.sf.saxon.expr.ItemMappingIterator;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.UnaryExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.functions.hof.CoercedFunction;
import net.sf.saxon.lib.FunctionAnnotationHandler;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.Annotation;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.SequenceType;

public final class FunctionSequenceCoercer
extends UnaryExpression {
    private SpecificFunctionType requiredItemType;
    private RoleDiagnostic role;

    public FunctionSequenceCoercer(Expression sequence, SpecificFunctionType requiredItemType, RoleDiagnostic role) {
        super(sequence);
        this.requiredItemType = requiredItemType;
        this.role = role;
        ExpressionTool.copyLocationInfo(sequence, this);
    }

    @Override
    protected OperandRole getOperandRole() {
        return OperandRole.INSPECT;
    }

    @Override
    public Expression simplify() throws XPathException {
        this.setBaseExpression(this.getBaseExpression().simplify());
        if (this.getBaseExpression() instanceof Literal) {
            GroundedValue val = this.iterate(new EarlyEvaluationContext(this.getConfiguration())).materialize();
            return Literal.makeLiteral(val, this);
        }
        return this;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getOperand().typeCheck(visitor, contextInfo);
        TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
        if (th.isSubType(this.getBaseExpression().getItemType(), this.requiredItemType)) {
            return this.getBaseExpression();
        }
        return this;
    }

    @Override
    public int computeSpecialProperties() {
        int p = super.computeSpecialProperties();
        return p | 0x800000;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        FunctionSequenceCoercer fsc2 = new FunctionSequenceCoercer(this.getBaseExpression().copy(rebindings), this.requiredItemType, this.role);
        ExpressionTool.copyLocationInfo(this, fsc2);
        return fsc2;
    }

    @Override
    public int getImplementationMethod() {
        return 2;
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        SequenceIterator base = this.getBaseExpression().iterate(context);
        Coercer coercer = new Coercer(this.requiredItemType, context.getConfiguration(), this.getLocation());
        return new ItemMappingIterator(base, coercer, true);
    }

    @Override
    public Function evaluateItem(XPathContext context) throws XPathException {
        Item item = this.getBaseExpression().evaluateItem(context);
        if (item == null) {
            return null;
        }
        if (!(item instanceof Function)) {
            UType itemType = UType.getUType(item);
            throw new XPathException(this.role.composeErrorMessage((ItemType)this.requiredItemType, itemType), "XPTY0004");
        }
        try {
            FunctionSequenceCoercer.checkAnnotations((Function)item, this.requiredItemType, context.getConfiguration());
        } catch (XPathException err) {
            err.maybeSetLocation(this.getLocation());
            err.maybeSetContext(context);
            throw err;
        }
        return new CoercedFunction((Function)item, this.requiredItemType);
    }

    @Override
    public SpecificFunctionType getItemType() {
        return this.requiredItemType;
    }

    @Override
    public int computeCardinality() {
        return this.getBaseExpression().getCardinality();
    }

    public RoleDiagnostic getRole() {
        return this.role;
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) && this.requiredItemType.equals(((FunctionSequenceCoercer)other).requiredItemType);
    }

    @Override
    public int computeHashCode() {
        return super.computeHashCode() ^ this.requiredItemType.hashCode();
    }

    @Override
    public String getExpressionName() {
        return "fnCoercer";
    }

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        destination.startElement("fnCoercer", this);
        SequenceType st = SequenceType.makeSequenceType(this.requiredItemType, 16384);
        destination.emitAttribute("to", st.toAlphaCode());
        destination.emitAttribute("diag", this.role.save());
        this.getBaseExpression().export(destination);
        destination.endElement();
    }

    private static void checkAnnotations(Function item, FunctionItemType requiredItemType, Configuration config) throws XPathException {
        for (Annotation ann : requiredItemType.getAnnotationAssertions()) {
            FunctionAnnotationHandler handler = config.getFunctionAnnotationHandler(ann.getAnnotationQName().getURI());
            if (handler == null || handler.satisfiesAssertion(ann, item.getAnnotations())) continue;
            throw new XPathException("Supplied function does not satisfy the annotation assertions of the required function type", "XPTY0004");
        }
    }

    public static class Coercer
    implements ItemMappingFunction {
        private SpecificFunctionType requiredItemType;
        private Configuration config;
        private Location locator;

        public Coercer(SpecificFunctionType requiredItemType, Configuration config, Location locator) {
            this.requiredItemType = requiredItemType;
            this.config = config;
            this.locator = locator;
        }

        @Override
        public Function mapItem(Item item) throws XPathException {
            if (!(item instanceof Function)) {
                throw new XPathException("Function coercion attempted on an item which is not a function", "XPTY0004", this.locator);
            }
            try {
                FunctionSequenceCoercer.checkAnnotations((Function)item, this.requiredItemType, this.config);
                return new CoercedFunction((Function)item, this.requiredItemType);
            } catch (XPathException err) {
                err.maybeSetLocation(this.locator);
                throw err;
            }
        }
    }
}


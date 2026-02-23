/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.EnumSet;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.expr.Assignation;
import net.sf.saxon.expr.AttributeGetter;
import net.sf.saxon.expr.AxisAtomizingIterator;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ForExpression;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.UnaryExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.Choose;
import net.sf.saxon.expr.instruct.TerminationException;
import net.sf.saxon.expr.instruct.ValueOf;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.functions.Error;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.AtomicArray;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.AtomizedValueIterator;
import net.sf.saxon.om.EmptyAtomicSequence;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomizingIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.UnfailingIterator;
import net.sf.saxon.tree.iter.UntypedAtomizingIterator;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.EmptySequence;

public final class Atomizer
extends UnaryExpression {
    private boolean untyped = false;
    private boolean singleValued = false;
    private ItemType operandItemType = null;
    private RoleDiagnostic roleDiagnostic = null;
    public static final UType STRING_KINDS = UType.NAMESPACE.union(UType.COMMENT).union(UType.PI);
    public static final UType UNTYPED_KINDS = UType.TEXT.union(UType.DOCUMENT);
    public static final UType UNTYPED_IF_UNTYPED_KINDS = UType.TEXT.union(UType.ELEMENT).union(UType.DOCUMENT).union(UType.ATTRIBUTE);

    public Atomizer(Expression sequence, RoleDiagnostic role) {
        super(sequence);
        this.roleDiagnostic = role;
        sequence.setFlattened(true);
    }

    public static Expression makeAtomizer(Expression sequence, RoleDiagnostic role) {
        if (sequence instanceof Literal && ((Literal)sequence).getValue() instanceof AtomicSequence) {
            return sequence;
        }
        return new Atomizer(sequence, role);
    }

    @Override
    protected OperandRole getOperandRole() {
        return OperandRole.ATOMIC_SEQUENCE;
    }

    @Override
    public int getImplementationMethod() {
        return 10;
    }

    public ItemType getOperandItemType() {
        if (this.operandItemType == null) {
            this.operandItemType = this.getBaseExpression().getItemType();
        }
        return this.operandItemType;
    }

    public void setRoleDiagnostic(RoleDiagnostic role) {
        this.roleDiagnostic = role;
    }

    @Override
    public Expression simplify() throws XPathException {
        this.untyped = !this.getPackageData().isSchemaAware();
        this.computeSingleValued(this.getConfiguration().getTypeHierarchy());
        Expression operand = this.getBaseExpression().simplify();
        if (operand instanceof Literal) {
            Item i;
            GroundedValue val = ((Literal)operand).getValue();
            if (val instanceof AtomicValue) {
                return operand;
            }
            UnfailingIterator iter = val.iterate();
            while ((i = iter.next()) != null) {
                if (i instanceof NodeInfo) {
                    return this;
                }
                if (!(i instanceof Function)) continue;
                if (((Function)i).isArray()) {
                    return this;
                }
                if (((Function)i).isMap()) {
                    XPathException err = new XPathException(this.expandMessage("Cannot atomize a map (" + i.toShortString() + ")"), "FOTY0013");
                    err.setIsTypeError(true);
                    err.setLocation(this.getLocation());
                    throw err;
                }
                XPathException err = new XPathException(this.expandMessage("Cannot atomize a function item"), "FOTY0013");
                err.setIsTypeError(true);
                err.setLocation(this.getLocation());
                throw err;
            }
            return operand;
        }
        if (operand instanceof ValueOf && !ReceiverOption.contains(((ValueOf)operand).getOptions(), 1)) {
            return ((ValueOf)operand).convertToCastAsString();
        }
        this.setBaseExpression(operand);
        return this;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.getOperand().typeCheck(visitor, contextInfo);
        this.untyped |= !visitor.getStaticContext().getPackageData().isSchemaAware();
        TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
        this.computeSingleValued(th);
        this.resetLocalStaticProperties();
        ItemType operandType = this.getOperandItemType();
        if (th.isSubType(operandType, BuiltInAtomicType.ANY_ATOMIC)) {
            return this.getBaseExpression();
        }
        if (!operandType.isAtomizable(th)) {
            XPathException err;
            if (operandType instanceof FunctionItemType) {
                String thing = operandType instanceof MapType ? "map" : "function item";
                err = new XPathException(this.expandMessage("Cannot atomize a " + thing), "FOTY0013");
            } else {
                err = new XPathException(this.expandMessage("Cannot atomize an element that is defined in the schema to have element-only content"), "FOTY0012");
            }
            err.setIsTypeError(true);
            err.setLocation(this.getLocation());
            throw err;
        }
        this.getBaseExpression().setFlattened(true);
        return this;
    }

    private void computeSingleValued(TypeHierarchy th) {
        ItemType operandType = this.getOperandItemType();
        if (th.relationship(operandType, ArrayItemType.ANY_ARRAY_TYPE) != Affinity.DISJOINT) {
            this.singleValued = false;
        } else {
            ItemType nodeType;
            this.singleValued = this.untyped;
            if (!this.singleValued && (nodeType = this.getBaseExpression().getItemType()) instanceof NodeTest) {
                SchemaType st = ((NodeTest)nodeType).getContentType();
                if (st == Untyped.getInstance() || st.isAtomicType() || st.isComplexType() && st != AnyType.getInstance()) {
                    this.singleValued = true;
                }
                if (!nodeType.getUType().overlaps(UType.ELEMENT.union(UType.ATTRIBUTE))) {
                    this.singleValued = true;
                }
            }
        }
    }

    private String expandMessage(String message) {
        if (this.roleDiagnostic == null) {
            return message;
        }
        return message + ". Found while atomizing the " + this.roleDiagnostic.getMessage();
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Expression exp = super.optimize(visitor, contextInfo);
        if (exp == this) {
            Expression operand;
            TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
            if (th.isSubType((operand = this.getBaseExpression()).getItemType(), BuiltInAtomicType.ANY_ATOMIC)) {
                return operand;
            }
            if (operand instanceof ValueOf && !ReceiverOption.contains(((ValueOf)operand).getOptions(), 1)) {
                Expression cast = ((ValueOf)operand).convertToCastAsString();
                return cast.optimize(visitor, contextInfo);
            }
            if (operand instanceof LetExpression || operand instanceof ForExpression) {
                Expression action = ((Assignation)operand).getAction();
                ((Assignation)operand).setAction(new Atomizer(action, this.roleDiagnostic));
                return operand.optimize(visitor, contextInfo);
            }
            if (operand instanceof Choose) {
                ((Choose)operand).atomizeActions();
                return operand.optimize(visitor, contextInfo);
            }
            if (operand instanceof Block) {
                Operand[] children = ((Block)operand).getOperanda();
                Expression[] atomizedChildren = new Expression[children.length];
                for (int i = 0; i < children.length; ++i) {
                    atomizedChildren[i] = new Atomizer(children[i].getChildExpression(), this.roleDiagnostic);
                }
                Block newBlock = new Block(atomizedChildren);
                return newBlock.typeCheck(visitor, contextInfo).optimize(visitor, contextInfo);
            }
            if (this.untyped && operand instanceof AxisExpression && ((AxisExpression)operand).getAxis() == 2 && ((AxisExpression)operand).getNodeTest() instanceof NameTest && !((AxisExpression)operand).isContextPossiblyUndefined()) {
                StructuredQName name = ((AxisExpression)operand).getNodeTest().getMatchingNodeName();
                FingerprintedQName qName = new FingerprintedQName(name, visitor.getConfiguration().getNamePool());
                AttributeGetter ag = new AttributeGetter(qName);
                int checks = 0;
                if (!(((AxisExpression)operand).getContextItemType() instanceof NodeTest)) {
                    checks = 2;
                }
                ag.setRequiredChecks(checks);
                ExpressionTool.copyLocationInfo(this, ag);
                return ag;
            }
        }
        return exp;
    }

    public boolean isUntyped() {
        return this.untyped;
    }

    @Override
    public int computeSpecialProperties() {
        int p = super.computeSpecialProperties();
        return (p &= 0xF6C0FFFF) | 0x800000;
    }

    @Override
    public void resetLocalStaticProperties() {
        super.resetLocalStaticProperties();
        this.operandItemType = null;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        Atomizer copy = new Atomizer(this.getBaseExpression().copy(rebindings), this.roleDiagnostic);
        copy.untyped = this.untyped;
        copy.singleValued = this.singleValued;
        ExpressionTool.copyLocationInfo(this, copy);
        return copy;
    }

    @Override
    public String getStreamerName() {
        return "Atomizer";
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        try {
            SequenceIterator base = this.getBaseExpression().iterate(context);
            return Atomizer.getAtomizingIterator(base, this.untyped && this.operandItemType instanceof NodeTest);
        } catch (TerminationException | Error.UserDefinedXPathException e) {
            throw e;
        } catch (XPathException e) {
            if (this.roleDiagnostic == null) {
                throw e;
            }
            String message = this.expandMessage(e.getMessage());
            XPathException e2 = new XPathException(message, e.getErrorCodeLocalPart(), e.getLocator());
            e2.setXPathContext(context);
            e2.maybeSetLocation(this.getLocation());
            throw e2;
        }
    }

    @Override
    public AtomicValue evaluateItem(XPathContext context) throws XPathException {
        Item i = this.getBaseExpression().evaluateItem(context);
        if (i == null) {
            return null;
        }
        return i.atomize().head();
    }

    @Override
    public ItemType getItemType() {
        this.operandItemType = this.getBaseExpression().getItemType();
        TypeHierarchy th = this.getConfiguration().getTypeHierarchy();
        return Atomizer.getAtomizedItemType(this.getBaseExpression(), this.untyped, th);
    }

    public static ItemType getAtomizedItemType(Expression operand, boolean alwaysUntyped, TypeHierarchy th) {
        ItemType in = operand.getItemType();
        if (in.isPlainType()) {
            return in;
        }
        if (in instanceof NodeTest) {
            UType kinds = in.getUType();
            if (alwaysUntyped) {
                if (STRING_KINDS.subsumes(kinds)) {
                    return BuiltInAtomicType.STRING;
                }
                if (UNTYPED_IF_UNTYPED_KINDS.subsumes(kinds)) {
                    return BuiltInAtomicType.UNTYPED_ATOMIC;
                }
            } else if (UNTYPED_KINDS.subsumes(kinds)) {
                return BuiltInAtomicType.UNTYPED_ATOMIC;
            }
            return in.getAtomizedItemType();
        }
        if (in instanceof JavaExternalObjectType) {
            return in.getAtomizedItemType();
        }
        if (in instanceof ArrayItemType) {
            PlainType ait = ((ArrayItemType)in).getMemberType().getPrimaryType().getAtomizedItemType();
            return ait == null ? ErrorType.getInstance() : ait;
        }
        if (in instanceof FunctionItemType) {
            return ErrorType.getInstance();
        }
        return BuiltInAtomicType.ANY_ATOMIC;
    }

    @Override
    public int computeCardinality() {
        SchemaType schemaType;
        ItemType in = this.getOperandItemType();
        Expression operand = this.getBaseExpression();
        if (this.singleValued) {
            return operand.getCardinality();
        }
        if (this.untyped && in instanceof NodeTest) {
            return operand.getCardinality();
        }
        if (Cardinality.allowsMany(operand.getCardinality())) {
            return 57344;
        }
        if (in.isPlainType()) {
            return operand.getCardinality();
        }
        if (in instanceof NodeTest && (schemaType = ((NodeTest)in).getContentType()).isAtomicType()) {
            return operand.getCardinality();
        }
        return 57344;
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

    public static SequenceIterator getAtomizingIterator(SequenceIterator base, boolean oneToOne) throws XPathException {
        EnumSet<SequenceIterator.Property> properties = base.getProperties();
        if (properties.contains((Object)SequenceIterator.Property.LAST_POSITION_FINDER)) {
            int count = ((LastPositionFinder)((Object)base)).getLength();
            if (count == 0) {
                return EmptyIterator.emptyIterator();
            }
            if (count == 1) {
                Item first = base.next();
                return first.atomize().iterate();
            }
        } else if (properties.contains((Object)SequenceIterator.Property.ATOMIZING)) {
            return new AxisAtomizingIterator((AtomizedValueIterator)base);
        }
        if (oneToOne) {
            return new UntypedAtomizingIterator(base);
        }
        return new AtomizingIterator(base);
    }

    public static AtomicSequence atomize(Sequence sequence) throws XPathException {
        if (sequence instanceof AtomicSequence) {
            return (AtomicSequence)sequence;
        }
        if (sequence instanceof EmptySequence) {
            return EmptyAtomicSequence.getInstance();
        }
        SequenceIterator iter = Atomizer.getAtomizingIterator(sequence.iterate(), false);
        return new AtomicArray(iter);
    }

    @Override
    public String getExpressionName() {
        return "data";
    }

    @Override
    public String toString() {
        return "data(" + this.getBaseExpression().toString() + ")";
    }

    @Override
    public String toShortString() {
        return this.getBaseExpression().toShortString();
    }

    @Override
    protected void emitExtraAttributes(ExpressionPresenter out) {
        if (this.roleDiagnostic != null) {
            out.emitAttribute("diag", this.roleDiagnostic.save());
        }
    }
}


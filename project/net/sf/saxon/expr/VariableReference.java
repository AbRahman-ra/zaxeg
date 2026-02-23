/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Assignation;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.BindingReference;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.LocalBinding;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.LocalVariableBinding;
import net.sf.saxon.expr.instruct.GlobalParam;
import net.sf.saxon.expr.instruct.LocalParam;
import net.sf.saxon.expr.instruct.LocalParamBlock;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.lib.StandardDiagnostics;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;

public abstract class VariableReference
extends Expression
implements BindingReference {
    protected Binding binding = null;
    protected SequenceType staticType = null;
    protected GroundedValue constantValue = null;
    private StructuredQName variableName = null;
    private boolean flattened = false;
    private boolean inLoop = false;
    private boolean filtered = false;

    public VariableReference(StructuredQName name) {
        this.variableName = name;
    }

    public VariableReference(Binding binding) {
        this.variableName = binding.getVariableQName();
        this.fixup(binding);
    }

    public void setVariableName(StructuredQName name) {
        this.variableName = name;
    }

    public StructuredQName getVariableName() {
        return this.variableName;
    }

    @Override
    public abstract Expression copy(RebindingMap var1);

    @Override
    public int getNetCost() {
        return 0;
    }

    protected void copyFrom(VariableReference ref) {
        this.binding = ref.binding;
        this.staticType = ref.staticType;
        this.constantValue = ref.constantValue;
        this.variableName = ref.variableName;
        this.flattened = ref.flattened;
        this.inLoop = ref.inLoop;
        this.filtered = ref.filtered;
        ExpressionTool.copyLocationInfo(ref, this);
    }

    @Override
    public void setStaticType(SequenceType type, GroundedValue value, int properties) {
        if (type == null) {
            type = SequenceType.ANY_SEQUENCE;
        }
        this.staticType = type;
        this.constantValue = value;
        int dependencies = this.getDependencies();
        this.staticProperties = properties & 0xFFFEFFFF & 0xFFBFFFFF | 0x800000 | type.getCardinality() | dependencies;
    }

    @Override
    public void setFlattened(boolean flattened) {
        this.flattened = flattened;
    }

    public boolean isFlattened() {
        return this.flattened;
    }

    @Override
    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }

    public boolean isFiltered() {
        return this.filtered;
    }

    public boolean isInLoop() {
        return this.inLoop;
    }

    public void setInLoop(boolean inLoop) {
        this.inLoop = inLoop;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        if (this.constantValue != null) {
            this.binding = null;
            return Literal.makeLiteral(this.constantValue, this);
        }
        if (this.binding != null) {
            this.recomputeInLoop();
            this.binding.addReference(this, this.inLoop);
        }
        return this;
    }

    public void recomputeInLoop() {
        this.inLoop = ExpressionTool.isLoopingReference(this, this.binding);
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        Expression select;
        if (this.binding instanceof LetExpression && ((LetExpression)this.binding).getSequence() instanceof Literal && !((LetExpression)this.binding).isIndexedVariable) {
            Expression val = ((LetExpression)this.binding).getSequence();
            Optimizer.trace(visitor.getConfiguration(), "Replaced variable " + this.getDisplayName() + " by its value", val);
            this.binding = null;
            return val.copy(new RebindingMap());
        }
        if (this.constantValue != null) {
            this.binding = null;
            Literal result = Literal.makeLiteral(this.constantValue, this);
            ExpressionTool.copyLocationInfo(this, result);
            Optimizer.trace(visitor.getConfiguration(), "Replaced variable " + this.getDisplayName() + " by its value", result);
            return result;
        }
        if (this.binding instanceof GlobalParam && ((GlobalParam)this.binding).isStatic() && (select = ((GlobalParam)this.binding).getBody()) instanceof Literal) {
            this.binding = null;
            Optimizer.trace(visitor.getConfiguration(), "Replaced static parameter " + this.getDisplayName() + " by its value", select);
            return select;
        }
        return this;
    }

    @Override
    public void fixup(Binding newBinding) {
        boolean indexed = this.binding instanceof LocalBinding && ((LocalBinding)this.binding).isIndexedVariable();
        this.binding = newBinding;
        if (indexed && newBinding instanceof LocalBinding) {
            ((LocalBinding)newBinding).setIndexedVariable();
        }
        this.resetLocalStaticProperties();
    }

    public void refineVariableType(ItemType type, int cardinality, GroundedValue constantValue, int properties) {
        int newcard;
        ItemType oldItemType;
        TypeHierarchy th = this.getConfiguration().getTypeHierarchy();
        ItemType newItemType = oldItemType = this.getItemType();
        if (th.isSubType(type, oldItemType)) {
            newItemType = type;
        }
        if (oldItemType instanceof NodeTest && type instanceof AtomicType) {
            newItemType = type;
        }
        if ((newcard = cardinality & this.getCardinality()) == 0) {
            newcard = this.getCardinality();
        }
        SequenceType seqType = SequenceType.makeSequenceType(newItemType, newcard);
        this.setStaticType(seqType, constantValue, properties);
    }

    @Override
    public ItemType getItemType() {
        if (this.staticType == null || this.staticType.getPrimaryType() == AnyItemType.getInstance()) {
            SequenceType st;
            if (this.binding != null && (st = this.binding.getRequiredType()) != null) {
                return st.getPrimaryType();
            }
            return AnyItemType.getInstance();
        }
        return this.staticType.getPrimaryType();
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        if (this.binding != null) {
            if (this.binding.isGlobal() || this.binding instanceof LocalParam || this.binding instanceof LetExpression && ((LetExpression)this.binding).isInstruction() || this.binding instanceof LocalVariableBinding) {
                SequenceType st = this.binding.getRequiredType();
                if (st != null) {
                    return st.getPrimaryType().getUType();
                }
                return UType.ANY;
            }
            if (this.binding instanceof Assignation) {
                return ((Assignation)this.binding).getSequence().getStaticUType(contextItemType);
            }
        }
        return UType.ANY;
    }

    @Override
    public IntegerValue[] getIntegerBounds() {
        if (this.binding != null) {
            return this.binding.getIntegerBoundsForVariable();
        }
        return null;
    }

    @Override
    public int computeCardinality() {
        if (this.staticType == null) {
            if (this.binding == null) {
                return 57344;
            }
            if (this.binding instanceof LetExpression) {
                return this.binding.getRequiredType().getCardinality();
            }
            if (this.binding instanceof Assignation) {
                return 16384;
            }
            if (this.binding.getRequiredType() == null) {
                return 57344;
            }
            return this.binding.getRequiredType().getCardinality();
        }
        return this.staticType.getCardinality();
    }

    @Override
    public int computeSpecialProperties() {
        Expression exp;
        int p = super.computeSpecialProperties();
        if (this.binding == null || !this.binding.isAssignable()) {
            p |= 0x800000;
        }
        if (this.binding instanceof Assignation && (exp = ((Assignation)this.binding).getSequence()) != null) {
            p |= exp.getSpecialProperties() & 0x4000000;
        }
        if (this.staticType != null && !Cardinality.allowsMany(this.staticType.getCardinality()) && this.staticType.getPrimaryType() instanceof NodeTest) {
            p |= 0x1000000;
        }
        return p & 0xFFBFFFFF;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof VariableReference && this.binding == ((VariableReference)other).binding && this.binding != null;
    }

    @Override
    public int computeHashCode() {
        return this.binding == null ? 73619830 : this.binding.hashCode();
    }

    @Override
    public int getIntrinsicDependencies() {
        int d = 0;
        if (this.binding == null) {
            d |= 0x680;
        } else if (this.binding.isGlobal()) {
            if (this.binding.isAssignable()) {
                d |= 0x200;
            }
            if (this.binding instanceof GlobalParam) {
                d |= 0x400;
            }
        } else {
            d |= 0x80;
        }
        return d;
    }

    @Override
    public int getImplementationMethod() {
        return (Cardinality.allowsMany(this.getCardinality()) ? 0 : 1) | 2 | 4;
    }

    @Override
    public Expression getScopingExpression() {
        if (this.binding instanceof Expression) {
            if (this.binding instanceof LocalParam && ((LocalParam)this.binding).getParentExpression() instanceof LocalParamBlock) {
                LocalParamBlock block = (LocalParamBlock)((LocalParam)this.binding).getParentExpression();
                return block.getParentExpression();
            }
            return (Expression)((Object)this.binding);
        }
        for (Expression parent = this.getParentExpression(); parent != null; parent = parent.getParentExpression()) {
            if (!parent.hasVariableBinding(this.binding)) continue;
            return parent;
        }
        return null;
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        return pathMap.getPathForVariable(this.getBinding());
    }

    @Override
    public SequenceIterator iterate(XPathContext c) throws XPathException {
        try {
            Sequence actual = this.evaluateVariable(c);
            assert (actual != null);
            return actual.iterate();
        } catch (XPathException err) {
            err.maybeSetLocation(this.getLocation());
            throw err;
        } catch (NullPointerException err) {
            err.printStackTrace();
            String msg = "Internal error: no value for variable $" + this.getDisplayName() + " at line " + this.getLocation().getLineNumber() + (this.getLocation().getSystemId() == null ? "" : " of " + this.getLocation().getSystemId());
            new StandardDiagnostics().printStackTrace(c, c.getConfiguration().getLogger(), 2);
            throw new AssertionError((Object)msg);
        } catch (AssertionError err) {
            ((Throwable)((Object)err)).printStackTrace();
            String msg = ((Throwable)((Object)err)).getMessage() + ". Variable reference $" + this.getDisplayName() + " at line " + this.getLocation().getLineNumber() + (this.getLocation().getSystemId() == null ? "" : " of " + this.getLocation().getSystemId());
            new StandardDiagnostics().printStackTrace(c, c.getConfiguration().getLogger(), 2);
            throw new AssertionError((Object)msg);
        }
    }

    @Override
    public Item evaluateItem(XPathContext c) throws XPathException {
        try {
            Sequence actual = this.evaluateVariable(c);
            assert (actual != null);
            return actual.head();
        } catch (XPathException err) {
            err.maybeSetLocation(this.getLocation());
            throw err;
        }
    }

    @Override
    public void process(Outputter output, XPathContext c) throws XPathException {
        try {
            SequenceIterator iter = this.evaluateVariable(c).iterate();
            Location loc = this.getLocation();
            iter.forEachOrFail(item -> output.append(item, loc, 524288));
        } catch (XPathException err) {
            err.maybeSetLocation(this.getLocation());
            throw err;
        }
    }

    public Sequence evaluateVariable(XPathContext c) throws XPathException {
        try {
            return this.binding.evaluateVariable(c);
        } catch (NullPointerException err) {
            if (this.binding == null) {
                throw new IllegalStateException("Variable $" + this.variableName.getDisplayName() + " has not been fixed up");
            }
            throw err;
        }
    }

    public Binding getBinding() {
        return this.binding;
    }

    public String getDisplayName() {
        if (this.binding != null) {
            return this.binding.getVariableQName().getDisplayName();
        }
        return this.variableName.getDisplayName();
    }

    public String getEQName() {
        if (this.binding != null) {
            StructuredQName q = this.binding.getVariableQName();
            if (q.hasURI("")) {
                return q.getLocalPart();
            }
            return q.getEQName();
        }
        return this.variableName.getEQName();
    }

    @Override
    public String toString() {
        String d = this.getEQName();
        return "$" + (d == null ? "$" : d);
    }

    @Override
    public String toShortString() {
        return "$" + this.getDisplayName();
    }

    @Override
    public String getExpressionName() {
        return "varRef";
    }

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        destination.startElement("varRef", this);
        destination.emitAttribute("name", this.variableName);
        if (this.binding instanceof LocalBinding) {
            destination.emitAttribute("slot", "" + ((LocalBinding)this.binding).getLocalSlotNumber());
        }
        destination.endElement();
    }

    @Override
    public String getStreamerName() {
        return "VariableReference";
    }
}


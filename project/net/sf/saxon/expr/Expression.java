/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.ExportAgent;
import net.sf.saxon.expr.LocalVariableReference;
import net.sf.saxon.expr.Locatable;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.PendingUpdateList;
import net.sf.saxon.expr.SuppliedParameterReference;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.functions.KeyFn;
import net.sf.saxon.functions.SuperId;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.om.IdentityComparable;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeSetPattern;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trace.Traceable;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.tree.jiter.MonoIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.z.IntHashSet;
import net.sf.saxon.z.IntIterator;

public abstract class Expression
implements IdentityComparable,
ExportAgent,
Locatable,
Traceable {
    public static final int EVALUATE_METHOD = 1;
    public static final int ITERATE_METHOD = 2;
    public static final int PROCESS_METHOD = 4;
    public static final int WATCH_METHOD = 8;
    public static final int ITEM_FEED_METHOD = 16;
    public static final int EFFECTIVE_BOOLEAN_VALUE = 32;
    public static final int UPDATE_METHOD = 64;
    protected int staticProperties = -1;
    private Location location = Loc.NONE;
    private Expression parentExpression;
    private RetainedStaticContext retainedStaticContext;
    private int[] slotsUsed;
    private int evaluationMethod;
    private Map<String, Object> extraProperties;
    private double cost = -1.0;
    private int cachedHashCode = -1;
    public static final double MAX_COST = 1.0E9;
    public static final IntegerValue UNBOUNDED_LOWER = (IntegerValue)IntegerValue.makeIntegerValue(new DoubleValue(-1.0E100));
    public static final IntegerValue UNBOUNDED_UPPER = (IntegerValue)IntegerValue.makeIntegerValue(new DoubleValue(1.0E100));
    public static final IntegerValue MAX_STRING_LENGTH = Int64Value.makeIntegerValue(Integer.MAX_VALUE);
    public static final IntegerValue MAX_SEQUENCE_LENGTH = Int64Value.makeIntegerValue(Integer.MAX_VALUE);

    public String getExpressionName() {
        return this.getClass().getSimpleName();
    }

    public Iterable<Operand> operands() {
        return Collections.emptyList();
    }

    public Expression getInterpretedExpression() {
        return this;
    }

    public final Iterable<Operand> checkedOperands() {
        Iterable<Operand> ops = this.operands();
        for (Operand o : ops) {
            boolean badExpression;
            Expression child = o.getChildExpression();
            boolean badOperand = o.getParentExpression() != this;
            boolean bl = badExpression = child.getParentExpression() != this;
            if (badOperand || badExpression) {
                String message = "*** Bad parent pointer found in " + (badOperand ? "operand " : "expression ") + child.toShortString() + " at " + child.getLocation().getSystemId() + "#" + child.getLocation().getLineNumber() + " ***";
                try {
                    Logger logger;
                    Configuration config = this.getConfiguration();
                    Logger logger2 = logger = config == null ? null : config.getLogger();
                    if (logger == null) {
                        throw new IllegalStateException(message);
                    }
                    logger.warning(message);
                } catch (Exception err) {
                    throw new IllegalStateException(message);
                }
                child.setParentExpression(this);
            }
            if (child.getRetainedStaticContext() != null) continue;
            child.setRetainedStaticContext(this.getRetainedStaticContext());
        }
        return ops;
    }

    protected List<Operand> operandList(Operand ... a) {
        return Arrays.asList(a);
    }

    protected List<Operand> operandSparseList(Operand ... a) {
        ArrayList<Operand> operanda = new ArrayList<Operand>();
        for (Operand o : a) {
            if (o == null) continue;
            operanda.add(o);
        }
        return operanda;
    }

    public Expression getParentExpression() {
        return this.parentExpression;
    }

    public void setParentExpression(Expression parent) {
        this.parentExpression = parent;
    }

    public Expression verifyParentPointers() throws IllegalStateException {
        for (Operand o : this.operands()) {
            Expression parent = o.getChildExpression().getParentExpression();
            if (parent != this) {
                throw new IllegalStateException("Invalid parent pointer in " + parent.toShortString() + " subexpression " + o.getChildExpression().toShortString());
            }
            if (o.getParentExpression() != this) {
                throw new IllegalStateException("Invalid parent pointer in operand object " + parent.toShortString() + " subexpression " + o.getChildExpression().toShortString());
            }
            if (ExpressionTool.findOperand(parent, o.getChildExpression()) == null) {
                throw new IllegalStateException("Incorrect parent pointer in " + parent.toShortString() + " subexpression " + o.getChildExpression().toShortString());
            }
            o.getChildExpression().verifyParentPointers();
        }
        return this;
    }

    public void restoreParentPointers() {
        for (Operand o : this.operands()) {
            Expression child = o.getChildExpression();
            child.setParentExpression(this);
            child.restoreParentPointers();
        }
    }

    public abstract int getImplementationMethod();

    public boolean implementsStaticTypeCheck() {
        return false;
    }

    public boolean hasVariableBinding(Binding binding) {
        return false;
    }

    public boolean isLiftable(boolean forStreaming) {
        int p = this.getSpecialProperties();
        int d = this.getDependencies();
        return (p & 0x800000) != 0 && (p & 0x2000000) == 0 && (d & 0x200) == 0 && (d & 4) == 0 && (d & 8) == 0;
    }

    public Expression getScopingExpression() {
        int d = this.getIntrinsicDependencies() & 0x1E;
        if (d != 0) {
            if (d == 16) {
                return ExpressionTool.getContextDocumentSettingContainer(this);
            }
            return ExpressionTool.getFocusSettingContainer(this);
        }
        return null;
    }

    public boolean isMultiThreaded(Configuration config) {
        return false;
    }

    public boolean allowExtractingCommonSubexpressions() {
        return true;
    }

    public Expression simplify() throws XPathException {
        this.simplifyChildren();
        return this;
    }

    protected final void simplifyChildren() throws XPathException {
        for (Operand o : this.operands()) {
            Expression e;
            if (o == null || (e = o.getChildExpression()) == null) continue;
            Expression f = e.simplify();
            o.setChildExpression(f);
        }
    }

    public void setRetainedStaticContext(RetainedStaticContext rsc) {
        if (rsc != null) {
            this.retainedStaticContext = rsc;
            for (Operand o : this.operands()) {
                Expression child;
                if (o == null || (child = o.getChildExpression()) == null || child.retainedStaticContext != null) continue;
                child.setRetainedStaticContext(rsc);
            }
        }
    }

    public void setRetainedStaticContextThoroughly(RetainedStaticContext rsc) {
        if (rsc != null) {
            this.retainedStaticContext = rsc;
            for (Operand o : this.operands()) {
                Expression child;
                if (o == null || (child = o.getChildExpression()) == null) continue;
                if (child.getLocalRetainedStaticContext() == null) {
                    child.setRetainedStaticContextThoroughly(rsc);
                    continue;
                }
                rsc = child.getLocalRetainedStaticContext();
                for (Operand p : child.operands()) {
                    Expression grandchild = p.getChildExpression();
                    if (grandchild == null) continue;
                    grandchild.setRetainedStaticContextThoroughly(rsc);
                }
            }
        }
    }

    public void setRetainedStaticContextLocally(RetainedStaticContext rsc) {
        if (rsc != null) {
            this.retainedStaticContext = rsc;
        }
    }

    public final RetainedStaticContext getRetainedStaticContext() {
        if (this.retainedStaticContext == null) {
            Expression parent = this.getParentExpression();
            assert (parent != null);
            this.retainedStaticContext = parent.getRetainedStaticContext();
            assert (this.retainedStaticContext != null);
        }
        return this.retainedStaticContext;
    }

    public RetainedStaticContext getLocalRetainedStaticContext() {
        return this.retainedStaticContext;
    }

    public String getStaticBaseURIString() {
        return this.getRetainedStaticContext().getStaticBaseUriString();
    }

    public URI getStaticBaseURI() throws XPathException {
        return this.getRetainedStaticContext().getStaticBaseUri();
    }

    public boolean isCallOn(Class<? extends SystemFunction> function) {
        return false;
    }

    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.typeCheckChildren(visitor, contextInfo);
        return this;
    }

    protected final void typeCheckChildren(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        for (Operand o : this.operands()) {
            o.typeCheck(visitor, contextInfo);
        }
    }

    public Expression staticTypeCheck(SequenceType req, boolean backwardsCompatible, RoleDiagnostic role, ExpressionVisitor visitor) throws XPathException {
        throw new UnsupportedOperationException("staticTypeCheck");
    }

    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        if (visitor.incrementAndTestDepth()) {
            this.optimizeChildren(visitor, contextInfo);
            visitor.decrementDepth();
        }
        return this;
    }

    protected final void optimizeChildren(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        for (Operand o : this.operands()) {
            o.optimize(visitor, contextInfo);
        }
    }

    public void prepareForStreaming() throws XPathException {
    }

    public double getCost() {
        if (this.cost < 0.0) {
            double i = this.getNetCost();
            for (Operand o : this.operands()) {
                if ((i += o.getChildExpression().getCost()) > 1.0E9) break;
            }
            this.cost = i;
        }
        return this.cost;
    }

    public int getNetCost() {
        return 1;
    }

    public Expression unordered(boolean retainAllNodes, boolean forStreaming) throws XPathException {
        return this;
    }

    public final int getSpecialProperties() {
        if (this.staticProperties == -1) {
            this.computeStaticProperties();
        }
        return this.staticProperties & 0xFFF0000;
    }

    public boolean hasSpecialProperty(int property) {
        return (this.getSpecialProperties() & property) != 0;
    }

    public int getCardinality() {
        if (this.staticProperties == -1) {
            this.computeStaticProperties();
        }
        return this.staticProperties & 0xE000;
    }

    public abstract ItemType getItemType();

    public SequenceType getStaticType() {
        return SequenceType.makeSequenceType(this.getItemType(), this.getCardinality());
    }

    public UType getStaticUType(UType contextItemType) {
        return UType.ANY;
    }

    public int getDependencies() {
        if (this.staticProperties == -1) {
            this.computeStaticProperties();
        }
        return this.staticProperties & 0x2000FFF;
    }

    public IntegerValue[] getIntegerBounds() {
        return null;
    }

    public void setFlattened(boolean flattened) {
    }

    public void setFiltered(boolean filtered) {
    }

    public Item evaluateItem(XPathContext context) throws XPathException {
        return this.iterate(context).next();
    }

    public SequenceIterator iterate(XPathContext context) throws XPathException {
        Item value = this.evaluateItem(context);
        return value == null ? EmptyIterator.emptyIterator() : SingletonIterator.rawIterator(value);
    }

    public boolean effectiveBooleanValue(XPathContext context) throws XPathException {
        try {
            return ExpressionTool.effectiveBooleanValue(this.iterate(context));
        } catch (XPathException e) {
            e.maybeSetFailingExpression(this);
            e.maybeSetContext(context);
            throw e;
        }
    }

    public CharSequence evaluateAsString(XPathContext context) throws XPathException {
        Item o = this.evaluateItem(context);
        StringValue value = (StringValue)o;
        if (value == null) {
            return "";
        }
        return value.getStringValueCS();
    }

    public void process(Outputter output, XPathContext context) throws XPathException {
        block5: {
            int m = this.getImplementationMethod();
            boolean hasEvaluateMethod = (m & 1) != 0;
            boolean hasIterateMethod = (m & 2) != 0;
            try {
                if (!(!hasEvaluateMethod || hasIterateMethod && Cardinality.allowsMany(this.getCardinality()))) {
                    Item item = this.evaluateItem(context);
                    if (item != null) {
                        output.append(item, this.getLocation(), 524288);
                    }
                    break block5;
                }
                if (hasIterateMethod) {
                    this.iterate(context).forEachOrFail(it -> output.append(it, this.getLocation(), 524288));
                    break block5;
                }
                throw new AssertionError((Object)("process() is not implemented in the subclass " + this.getClass()));
            } catch (XPathException e) {
                e.maybeSetLocation(this.getLocation());
                e.maybeSetContext(context);
                throw e;
            }
        }
    }

    public void evaluatePendingUpdates(XPathContext context, PendingUpdateList pul) throws XPathException {
        if (!this.isVacuousExpression()) {
            throw new UnsupportedOperationException("Expression " + this.getClass() + " is not an updating expression");
        }
        this.iterate(context).next();
    }

    public String toString() {
        int dot;
        FastStringBuffer buff = new FastStringBuffer(64);
        String className = this.getClass().getName();
        while ((dot = className.indexOf(46)) >= 0) {
            className = className.substring(dot + 1);
        }
        buff.append(className);
        boolean first = true;
        for (Operand o : this.operands()) {
            buff.append(first ? "(" : ", ");
            buff.append(o.getChildExpression().toString());
            first = false;
        }
        if (!first) {
            buff.append(")");
        }
        return buff.toString();
    }

    public String toShortString() {
        return this.getExpressionName();
    }

    @Override
    public abstract void export(ExpressionPresenter var1) throws XPathException;

    public final void explain(Logger out) {
        ExpressionPresenter ep = new ExpressionPresenter(this.getConfiguration(), out);
        ExpressionPresenter.ExportOptions options = new ExpressionPresenter.ExportOptions();
        options.explaining = true;
        ep.setOptions(options);
        try {
            this.export(ep);
        } catch (XPathException e) {
            ep.startElement("failure");
            ep.emitAttribute("message", e.getMessage());
            ep.endElement();
        }
        ep.close();
    }

    public void checkPermittedContents(SchemaType parentType, boolean whole) throws XPathException {
    }

    public void adoptChildExpression(Expression child) {
        if (child == null) {
            return;
        }
        child.setParentExpression(this);
        if (child.retainedStaticContext == null) {
            child.retainedStaticContext = this.retainedStaticContext;
        }
        if (this.getLocation() == null || this.getLocation() == Loc.NONE) {
            ExpressionTool.copyLocationInfo(child, this);
        } else if (child.getLocation() == null || child.getLocation() == Loc.NONE) {
            ExpressionTool.copyLocationInfo(this, child);
        }
        this.resetLocalStaticProperties();
    }

    public void setLocation(Location id) {
        this.location = id;
    }

    @Override
    public final Location getLocation() {
        Expression exp = this;
        for (int limit = 0; limit < 10; ++limit) {
            if ((exp.location == null || exp.location == Loc.NONE) && exp.getParentExpression() != null) {
                exp = exp.getParentExpression();
                continue;
            }
            return exp.location;
        }
        return exp.location;
    }

    public Configuration getConfiguration() {
        try {
            return this.getRetainedStaticContext().getConfiguration();
        } catch (NullPointerException e) {
            throw new NullPointerException("Internal error: expression " + this.toShortString() + " has no retained static context");
        }
    }

    public PackageData getPackageData() {
        try {
            return this.getRetainedStaticContext().getPackageData();
        } catch (NullPointerException e) {
            throw new NullPointerException("Internal error: expression " + this.toShortString() + " has no retained static context");
        }
    }

    public boolean isInstruction() {
        return false;
    }

    public final void computeStaticProperties() {
        this.staticProperties = this.computeDependencies() | this.computeCardinality() | this.computeSpecialProperties();
    }

    public void resetLocalStaticProperties() {
        this.staticProperties = -1;
        this.cachedHashCode = -1;
    }

    public boolean isStaticPropertiesKnown() {
        return this.staticProperties != -1;
    }

    protected abstract int computeCardinality();

    protected int computeSpecialProperties() {
        return 0;
    }

    public int computeDependencies() {
        int dependencies = this.getIntrinsicDependencies();
        for (Operand o : this.operands()) {
            if (o.hasSameFocus()) {
                dependencies |= o.getChildExpression().getDependencies();
                continue;
            }
            dependencies |= o.getChildExpression().getDependencies() & 0xFFFFFFE1;
        }
        return dependencies;
    }

    public int getIntrinsicDependencies() {
        return 0;
    }

    public void setStaticProperty(int prop) {
        if (this.staticProperties == -1) {
            this.computeStaticProperties();
        }
        this.staticProperties |= prop;
    }

    public void checkForUpdatingSubexpressions() throws XPathException {
        for (Operand o : this.operands()) {
            Expression sub = o.getChildExpression();
            if (sub == null) {
                throw new NullPointerException();
            }
            sub.checkForUpdatingSubexpressions();
            if (!sub.isUpdatingExpression()) continue;
            XPathException err = new XPathException("Updating expression appears in a context where it is not permitted", "XUST0001");
            err.setLocation(sub.getLocation());
            throw err;
        }
    }

    public boolean isUpdatingExpression() {
        for (Operand o : this.operands()) {
            if (!o.getChildExpression().isUpdatingExpression()) continue;
            return true;
        }
        return false;
    }

    public boolean isVacuousExpression() {
        return false;
    }

    public abstract Expression copy(RebindingMap var1);

    public void suppressValidation(int parentValidationMode) {
    }

    public int markTailFunctionCalls(StructuredQName qName, int arity) {
        return 0;
    }

    public Pattern toPattern(Configuration config) throws XPathException {
        ItemType type = this.getItemType();
        if ((this.getDependencies() & 0xE) == 0 && (type instanceof NodeTest || this instanceof VariableReference)) {
            return new NodeSetPattern(this);
        }
        if (this.isCallOn(KeyFn.class) || this.isCallOn(SuperId.class)) {
            return new NodeSetPattern(this);
        }
        throw new XPathException("Cannot convert the expression {" + this + "} to a pattern");
    }

    public final synchronized int[] getSlotsUsed() {
        if (this.slotsUsed != null) {
            return this.slotsUsed;
        }
        IntHashSet slots = new IntHashSet(10);
        Expression.gatherSlotsUsed(this, slots);
        this.slotsUsed = new int[slots.size()];
        int i = 0;
        IntIterator iter = slots.iterator();
        while (iter.hasNext()) {
            this.slotsUsed[i++] = iter.next();
        }
        Arrays.sort(this.slotsUsed);
        return this.slotsUsed;
    }

    private static void gatherSlotsUsed(Expression exp, IntHashSet slots) {
        if ((exp = exp.getInterpretedExpression()) instanceof LocalVariableReference) {
            slots.add(((LocalVariableReference)exp).getSlotNumber());
        } else if (exp instanceof SuppliedParameterReference) {
            int slot = ((SuppliedParameterReference)exp).getSlotNumber();
            slots.add(slot);
        } else {
            for (Operand o : exp.operands()) {
                Expression.gatherSlotsUsed(o.getChildExpression(), slots);
            }
        }
    }

    protected void dynamicError(String message, String code, XPathContext context) throws XPathException {
        XPathException err = new XPathException(message, code, this.getLocation());
        err.setXPathContext(context);
        err.setFailingExpression(this);
        throw err;
    }

    protected void typeError(String message, String errorCode, XPathContext context) throws XPathException {
        XPathException e = new XPathException(message, errorCode, this.getLocation());
        e.setIsTypeError(true);
        e.setXPathContext(context);
        e.setFailingExpression(this);
        throw e;
    }

    public String getTracingTag() {
        return this.getExpressionName();
    }

    @Override
    public StructuredQName getObjectName() {
        return null;
    }

    public Object getProperty(String name) {
        if (name.equals("expression")) {
            return this.getLocation();
        }
        return null;
    }

    public Iterator<String> getProperties() {
        return new MonoIterator<String>("expression");
    }

    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        PathMap.PathMapNodeSet attachmentPoint;
        boolean dependsOnFocus = ExpressionTool.dependsOnFocus(this);
        if (pathMapNodeSet == null) {
            if (dependsOnFocus) {
                ContextItemExpression cie = new ContextItemExpression();
                ExpressionTool.copyLocationInfo(this, cie);
                pathMapNodeSet = new PathMap.PathMapNodeSet(pathMap.makeNewRoot(cie));
            }
            attachmentPoint = pathMapNodeSet;
        } else {
            attachmentPoint = dependsOnFocus ? pathMapNodeSet : null;
        }
        PathMap.PathMapNodeSet result = new PathMap.PathMapNodeSet();
        for (Operand o : this.operands()) {
            OperandUsage usage = o.getUsage();
            Expression child = o.getChildExpression();
            PathMap.PathMapNodeSet target = child.addToPathMap(pathMap, attachmentPoint);
            if (usage == OperandUsage.NAVIGATION) {
                target = target.createArc(1, NodeKindTest.ELEMENT);
                target = target.createArc(4, NodeKindTest.ELEMENT);
            }
            result.addNodeSet(target);
        }
        if (this.getItemType() instanceof AtomicType) {
            return null;
        }
        return result;
    }

    public boolean isSubtreeExpression() {
        if (ExpressionTool.dependsOnFocus(this)) {
            if ((this.getIntrinsicDependencies() & 0x1E) != 0) {
                return false;
            }
            for (Operand o : this.operands()) {
                if (o.getChildExpression().isSubtreeExpression()) continue;
                return false;
            }
            return true;
        }
        return true;
    }

    public void setEvaluationMethod(int method) {
        this.evaluationMethod = method;
    }

    public int getEvaluationMethod() {
        return this.evaluationMethod;
    }

    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public final boolean isEqual(Expression other) {
        return this == other || this.hashCode() == other.hashCode() && this.equals(other);
    }

    public final int hashCode() {
        if (this.cachedHashCode == -1) {
            this.cachedHashCode = this.computeHashCode();
        }
        return this.cachedHashCode;
    }

    protected boolean hasCompatibleStaticContext(Expression other) {
        boolean d2;
        boolean d1 = (this.getIntrinsicDependencies() & 0x800) != 0;
        boolean bl = d2 = (other.getIntrinsicDependencies() & 0x800) != 0;
        if (d1 != d2) {
            return false;
        }
        if (d1) {
            return this.getRetainedStaticContext().equals(other.getRetainedStaticContext());
        }
        return true;
    }

    protected int computeHashCode() {
        return super.hashCode();
    }

    @Override
    public boolean isIdentical(IdentityComparable other) {
        return this == other;
    }

    @Override
    public int identityHashCode() {
        return System.identityHashCode(this.getLocation());
    }

    public void setExtraProperty(String name, Object value) {
        if (this.extraProperties == null) {
            if (value == null) {
                return;
            }
            this.extraProperties = new HashMap<String, Object>(4);
        }
        if (value == null) {
            this.extraProperties.remove(name);
        } else {
            this.extraProperties.put(name, value);
        }
    }

    public Object getExtraProperty(String name) {
        if (this.extraProperties == null) {
            return null;
        }
        return this.extraProperties.get(name);
    }

    public String getStreamerName() {
        return null;
    }
}


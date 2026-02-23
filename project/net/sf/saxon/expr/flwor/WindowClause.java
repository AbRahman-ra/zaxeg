/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import java.util.Iterator;
import java.util.List;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemMappingIterator;
import net.sf.saxon.expr.ItemTypeCheckingFunction;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.Clause;
import net.sf.saxon.expr.flwor.FLWORExpression;
import net.sf.saxon.expr.flwor.LocalVariableBinding;
import net.sf.saxon.expr.flwor.OperandProcessor;
import net.sf.saxon.expr.flwor.TuplePull;
import net.sf.saxon.expr.flwor.TuplePush;
import net.sf.saxon.expr.flwor.WindowClausePull;
import net.sf.saxon.expr.flwor.WindowClausePush;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.functions.Count;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.z.IntHashMap;

public class WindowClause
extends Clause {
    private boolean sliding;
    private boolean includeUnclosedWindows = true;
    private Operand sequenceOp;
    private Operand startConditionOp;
    private Operand endConditionOp;
    private IntHashMap<LocalVariableBinding> windowVars = new IntHashMap(10);
    private ItemTypeCheckingFunction itemTypeChecker;
    private boolean windowMustBeSingleton;
    public static final int WINDOW_VAR = 0;
    public static final int START_ITEM = 1;
    public static final int START_ITEM_POSITION = 2;
    public static final int START_PREVIOUS_ITEM = 3;
    public static final int START_NEXT_ITEM = 4;
    public static final int END_ITEM = 5;
    public static final int END_ITEM_POSITION = 6;
    public static final int END_PREVIOUS_ITEM = 7;
    public static final int END_NEXT_ITEM = 8;

    @Override
    public Clause.ClauseName getClauseKey() {
        return Clause.ClauseName.WINDOW;
    }

    public void setIsSlidingWindow(boolean sliding) {
        this.sliding = sliding;
    }

    public boolean isSlidingWindow() {
        return this.sliding;
    }

    public boolean isTumblingWindow() {
        return !this.sliding;
    }

    public void setIncludeUnclosedWindows(boolean include) {
        this.includeUnclosedWindows = include;
    }

    public boolean isIncludeUnclosedWindows() {
        return this.includeUnclosedWindows;
    }

    public void initSequence(FLWORExpression flwor, Expression sequence) {
        this.sequenceOp = new Operand(flwor, sequence, OperandRole.INSPECT);
    }

    public void setSequence(Expression sequence) {
        this.sequenceOp.setChildExpression(sequence);
    }

    public Expression getSequence() {
        return this.sequenceOp.getChildExpression();
    }

    public void initStartCondition(FLWORExpression flwor, Expression startCondition) {
        this.startConditionOp = new Operand(flwor, startCondition, OperandRole.INSPECT);
    }

    public void setStartCondition(Expression startCondition) {
        this.startConditionOp.setChildExpression(startCondition);
    }

    public Expression getStartCondition() {
        return this.startConditionOp.getChildExpression();
    }

    public void initEndCondition(FLWORExpression flwor, Expression endCondition) {
        this.endConditionOp = new Operand(flwor, endCondition, OperandRole.INSPECT);
    }

    public void setEndCondition(Expression endCondition) {
        this.endConditionOp.setChildExpression(endCondition);
    }

    public Expression getEndCondition() {
        return this.endConditionOp == null ? null : this.endConditionOp.getChildExpression();
    }

    public void setVariableBinding(int role, LocalVariableBinding binding) throws XPathException {
        Iterator<LocalVariableBinding> iter = this.windowVars.valueIterator();
        while (iter.hasNext()) {
            if (!iter.next().getVariableQName().equals(binding.getVariableQName())) continue;
            throw new XPathException("Two variables in a window clause cannot have the same name (" + binding.getVariableQName().getDisplayName() + ")", "XQST0103");
        }
        this.windowVars.put(role, binding);
    }

    public LocalVariableBinding getVariableBinding(int role) {
        return this.windowVars.get(role);
    }

    public ItemTypeCheckingFunction getItemTypeChecker() {
        return this.itemTypeChecker;
    }

    public boolean isWindowMustBeSingleton() {
        return this.windowMustBeSingleton;
    }

    @Override
    public void typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        SequenceType requiredType = this.getVariableBinding(0).getRequiredType();
        ItemType required = requiredType.getPrimaryType();
        ItemType supplied = this.getSequence().getItemType();
        Configuration config = visitor.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();
        Affinity rel = th.relationship(required, supplied);
        switch (rel) {
            case SAME_TYPE: 
            case SUBSUMES: {
                break;
            }
            case OVERLAPS: 
            case SUBSUMED_BY: {
                RoleDiagnostic role = new RoleDiagnostic(3, this.getVariableBinding(0).getVariableQName().getDisplayName(), 0);
                this.itemTypeChecker = new ItemTypeCheckingFunction(required, role, this.getLocation(), config);
                break;
            }
            case DISJOINT: {
                String message = "The items in the window will always be instances of " + supplied + ", never of the required type " + required;
                throw new XPathException(message, "XPTY0004", this.getLocation());
            }
        }
        boolean bl = this.windowMustBeSingleton = !Cardinality.allowsMany(requiredType.getCardinality());
        if (requiredType.getCardinality() == 8192) {
            String message = "The value of the window variable can never be an empty sequence";
            throw new XPathException(message, "XPTY0004", this.getLocation());
        }
    }

    public void checkWindowContents(Window w) throws XPathException {
        if (this.windowMustBeSingleton && w.contents.size() > 1) {
            throw new XPathException("Required type of window allows only a single item; window has length " + w.contents.size(), "XPTY0004", this.getLocation());
        }
        ItemTypeCheckingFunction checker = this.getItemTypeChecker();
        if (checker != null) {
            ItemMappingIterator check = new ItemMappingIterator(new ListIterator<Item>(w.contents), checker);
            Count.count(check);
        }
    }

    @Override
    public Clause copy(FLWORExpression flwor, RebindingMap rebindings) {
        WindowClause wc = new WindowClause();
        wc.setLocation(this.getLocation());
        wc.setPackageData(this.getPackageData());
        wc.sliding = this.sliding;
        wc.includeUnclosedWindows = this.includeUnclosedWindows;
        wc.initSequence(flwor, this.getSequence().copy(rebindings));
        wc.initStartCondition(flwor, this.getStartCondition().copy(rebindings));
        if (this.getEndCondition() != null) {
            wc.initEndCondition(flwor, this.getEndCondition().copy(rebindings));
        }
        wc.windowVars = this.windowVars;
        return wc;
    }

    @Override
    public TuplePull getPullStream(TuplePull base, XPathContext context) {
        return new WindowClausePull(base, this, context);
    }

    @Override
    public TuplePush getPushStream(TuplePush destination, Outputter output, XPathContext context) {
        return new WindowClausePush(output, destination, this);
    }

    @Override
    public void processOperands(OperandProcessor processor) throws XPathException {
        processor.processOperand(this.sequenceOp);
        processor.processOperand(this.startConditionOp);
        if (this.endConditionOp != null) {
            processor.processOperand(this.endConditionOp);
        }
    }

    @Override
    public void addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        throw new UnsupportedOperationException("Cannot use document projection with windowing");
    }

    @Override
    public LocalVariableBinding[] getRangeVariables() {
        LocalVariableBinding[] vars = new LocalVariableBinding[this.windowVars.size()];
        int i = 0;
        Iterator<LocalVariableBinding> iter = this.windowVars.valueIterator();
        while (iter.hasNext()) {
            vars[i++] = iter.next();
        }
        return vars;
    }

    @Override
    public void explain(ExpressionPresenter out) throws XPathException {
        out.startElement(this.isSlidingWindow() ? "slidingWindow" : "tumblingWindow");
        out.startSubsidiaryElement("select");
        this.getSequence().export(out);
        out.endSubsidiaryElement();
        out.startSubsidiaryElement("start");
        this.getStartCondition().export(out);
        out.endSubsidiaryElement();
        if (this.endConditionOp != null) {
            out.startSubsidiaryElement("end");
            this.getEndCondition().export(out);
            out.endSubsidiaryElement();
        }
        out.endElement();
    }

    protected boolean matchesStart(Item previous, Item current, Item next, int position, XPathContext context) throws XPathException {
        WindowClause clause = this;
        LocalVariableBinding binding = clause.getVariableBinding(1);
        if (binding != null) {
            context.setLocalVariable(binding.getLocalSlotNumber(), current);
        }
        if ((binding = clause.getVariableBinding(2)) != null) {
            context.setLocalVariable(binding.getLocalSlotNumber(), new Int64Value(position));
        }
        if ((binding = clause.getVariableBinding(4)) != null) {
            context.setLocalVariable(binding.getLocalSlotNumber(), WindowClause.makeValue(next));
        }
        if ((binding = clause.getVariableBinding(3)) != null) {
            context.setLocalVariable(binding.getLocalSlotNumber(), WindowClause.makeValue(previous));
        }
        return clause.getStartCondition().effectiveBooleanValue(context);
    }

    protected boolean matchesEnd(Window window, Item previous, Item current, Item next, int position, XPathContext context) throws XPathException {
        WindowClause clause = this;
        LocalVariableBinding binding = clause.getVariableBinding(1);
        if (binding != null) {
            context.setLocalVariable(binding.getLocalSlotNumber(), window.startItem);
        }
        if ((binding = clause.getVariableBinding(2)) != null) {
            context.setLocalVariable(binding.getLocalSlotNumber(), new Int64Value(window.startPosition));
        }
        if ((binding = clause.getVariableBinding(4)) != null) {
            context.setLocalVariable(binding.getLocalSlotNumber(), WindowClause.makeValue(window.startNextItem));
        }
        if ((binding = clause.getVariableBinding(3)) != null) {
            context.setLocalVariable(binding.getLocalSlotNumber(), WindowClause.makeValue(window.startPreviousItem));
        }
        if ((binding = clause.getVariableBinding(5)) != null) {
            context.setLocalVariable(binding.getLocalSlotNumber(), current);
        }
        if ((binding = clause.getVariableBinding(6)) != null) {
            context.setLocalVariable(binding.getLocalSlotNumber(), new Int64Value(position));
        }
        if ((binding = clause.getVariableBinding(8)) != null) {
            context.setLocalVariable(binding.getLocalSlotNumber(), WindowClause.makeValue(next));
        }
        if ((binding = clause.getVariableBinding(7)) != null) {
            context.setLocalVariable(binding.getLocalSlotNumber(), WindowClause.makeValue(previous));
        }
        return clause.getEndCondition().effectiveBooleanValue(context);
    }

    protected static Sequence makeValue(Item item) {
        if (item == null) {
            return EmptySequence.getInstance();
        }
        return item;
    }

    protected static class Window {
        public Item startItem;
        public int startPosition;
        public Item startPreviousItem;
        public Item startNextItem;
        public Item endItem;
        public int endPosition = 0;
        public Item endPreviousItem;
        public Item endNextItem;
        public List<Item> contents;
        public boolean isDespatched = false;

        protected Window() {
        }

        public boolean isFinished() {
            return this.endPosition > 0;
        }

        public boolean isDespatched() {
            return this.isDespatched;
        }
    }
}


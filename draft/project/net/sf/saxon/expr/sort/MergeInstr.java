/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import java.util.ArrayList;
import java.util.Set;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.CardinalityChecker;
import net.sf.saxon.expr.ContextMappingFunction;
import net.sf.saxon.expr.ContextMappingIterator;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemMappingIterator;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.XPathContextMinor;
import net.sf.saxon.expr.accum.Accumulator;
import net.sf.saxon.expr.accum.AccumulatorManager;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.GroupIterator;
import net.sf.saxon.expr.sort.ItemOrderComparer;
import net.sf.saxon.expr.sort.ItemWithMergeKeys;
import net.sf.saxon.expr.sort.MergeGroupingIterator;
import net.sf.saxon.expr.sort.MergeIterator;
import net.sf.saxon.expr.sort.SortKeyDefinition;
import net.sf.saxon.expr.sort.SortKeyDefinitionList;
import net.sf.saxon.functions.Count;
import net.sf.saxon.functions.CurrentMergeGroup;
import net.sf.saxon.functions.CurrentMergeKey;
import net.sf.saxon.functions.DocumentFn;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.om.FocusIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.NoDynamicContextException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XsltController;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.ManualIterator;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;

public class MergeInstr
extends Instruction {
    protected MergeSource[] mergeSources;
    private Operand actionOp;
    protected AtomicComparer[] comparators;
    private static final OperandRole ROW_SELECT = new OperandRole(6, OperandUsage.INSPECTION, SequenceType.ANY_SEQUENCE);

    public MergeInstr init(MergeSource[] mSources, Expression action) {
        this.actionOp = new Operand(this, action, OperandRole.FOCUS_CONTROLLED_ACTION);
        this.mergeSources = mSources;
        for (MergeSource mSource : mSources) {
            this.adoptChildExpression(mSource.getForEachItem());
            this.adoptChildExpression(mSource.getForEachSource());
            this.adoptChildExpression(mSource.getRowSelect());
        }
        this.adoptChildExpression(action);
        return this;
    }

    public MergeSource[] getMergeSources() {
        return this.mergeSources;
    }

    public void setAction(Expression action) {
        this.actionOp.setChildExpression(action);
    }

    public Expression getAction() {
        return this.actionOp.getChildExpression();
    }

    @Override
    public int getInstructionNameCode() {
        return 169;
    }

    @Override
    public void checkPermittedContents(SchemaType parentType, boolean whole) throws XPathException {
        this.getAction().checkPermittedContents(parentType, false);
    }

    @Override
    public boolean allowExtractingCommonSubexpressions() {
        return false;
    }

    @Override
    public ItemType getItemType() {
        return this.getAction().getItemType();
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Configuration config = visitor.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();
        TypeChecker tc = config.getTypeChecker(false);
        ItemType inputType = null;
        for (MergeSource mergeSource : this.mergeSources) {
            ContextItemStaticInfo rowContextItemType = contextInfo;
            if (mergeSource.getForEachItem() != null) {
                mergeSource.forEachItemOp.typeCheck(visitor, contextInfo);
                rowContextItemType = config.makeContextItemStaticInfo(mergeSource.getForEachItem().getItemType(), false);
            } else if (mergeSource.getForEachSource() != null) {
                mergeSource.forEachStreamOp.typeCheck(visitor, contextInfo);
                RoleDiagnostic role = new RoleDiagnostic(4, "xsl:merge/for-each-source", 0);
                mergeSource.setForEachStream(tc.staticTypeCheck(mergeSource.getForEachSource(), SequenceType.STRING_SEQUENCE, role, visitor));
                rowContextItemType = config.makeContextItemStaticInfo(NodeKindTest.DOCUMENT, false);
            }
            mergeSource.rowSelectOp.typeCheck(visitor, rowContextItemType);
            ItemType rowItemType = mergeSource.getRowSelect().getItemType();
            inputType = inputType == null ? rowItemType : Type.getCommonSuperType(inputType, rowItemType, th);
            ContextItemStaticInfo cit = config.makeContextItemStaticInfo(inputType, false);
            if (mergeSource.mergeKeyDefinitions == null) continue;
            for (SortKeyDefinition skd : mergeSource.mergeKeyDefinitions) {
                Expression sortKey = skd.getSortKey();
                if ((sortKey = sortKey.typeCheck(visitor, cit)) != null) {
                    RoleDiagnostic role = new RoleDiagnostic(4, "xsl:merge-key/select", 0);
                    role.setErrorCode("XTTE1020");
                    sortKey = CardinalityChecker.makeCardinalityChecker(sortKey, 24576, role);
                    skd.setSortKey(sortKey, true);
                }
                Expression exp = skd.getLanguage().typeCheck(visitor, config.makeContextItemStaticInfo(inputType, false));
                skd.setLanguage(exp);
                exp = skd.getOrder().typeCheck(visitor, cit);
                skd.setOrder(exp);
                exp = skd.getCollationNameExpression();
                if (exp != null) {
                    exp = exp.typeCheck(visitor, cit);
                    skd.setCollationNameExpression(exp);
                }
                exp = skd.getCaseOrder().typeCheck(visitor, cit);
                skd.setCaseOrder(exp);
                exp = skd.getDataTypeExpression();
                if (exp == null) continue;
                exp = exp.typeCheck(visitor, cit);
                skd.setDataTypeExpression(exp);
            }
        }
        this.actionOp.typeCheck(visitor, config.makeContextItemStaticInfo(inputType, false));
        if (Literal.isEmptySequence(this.getAction())) {
            return this.getAction();
        }
        if (this.mergeSources.length == 1 && Literal.isEmptySequence(this.mergeSources[0].getRowSelect())) {
            return this.mergeSources[0].getRowSelect();
        }
        this.fixupGroupReferences();
        return this;
    }

    public void fixupGroupReferences() {
        MergeInstr.fixupGroupReferences(this, this, false);
    }

    private static void fixupGroupReferences(Expression exp, MergeInstr instr, boolean isInLoop) {
        block13: {
            if (exp == null) break block13;
            if (exp.isCallOn(CurrentMergeGroup.class)) {
                CurrentMergeGroup fn = (CurrentMergeGroup)((SystemFunctionCall)exp).getTargetFunction();
                fn.setControllingInstruction(instr, isInLoop);
            } else if (exp.isCallOn(CurrentMergeKey.class)) {
                CurrentMergeKey fn = (CurrentMergeKey)((SystemFunctionCall)exp).getTargetFunction();
                fn.setControllingInstruction(instr);
            } else if (exp instanceof MergeInstr) {
                MergeInstr instr2 = (MergeInstr)exp;
                if (instr2 == instr) {
                    MergeInstr.fixupGroupReferences(instr2.getAction(), instr, false);
                } else {
                    for (MergeSource m : instr2.getMergeSources()) {
                        for (SortKeyDefinition skd : m.mergeKeyDefinitions) {
                            MergeInstr.fixupGroupReferences(skd.getOrder(), instr, isInLoop);
                            MergeInstr.fixupGroupReferences(skd.getCaseOrder(), instr, isInLoop);
                            MergeInstr.fixupGroupReferences(skd.getDataTypeExpression(), instr, isInLoop);
                            MergeInstr.fixupGroupReferences(skd.getLanguage(), instr, isInLoop);
                            MergeInstr.fixupGroupReferences(skd.getCollationNameExpression(), instr, isInLoop);
                            MergeInstr.fixupGroupReferences(skd.getOrder(), instr, isInLoop);
                        }
                        if (m.forEachItemOp != null) {
                            MergeInstr.fixupGroupReferences(m.getForEachItem(), instr, isInLoop);
                        }
                        if (m.forEachStreamOp != null) {
                            MergeInstr.fixupGroupReferences(m.getForEachSource(), instr, isInLoop);
                        }
                        if (m.rowSelectOp == null) continue;
                        MergeInstr.fixupGroupReferences(m.getRowSelect(), instr, isInLoop);
                    }
                }
            } else {
                for (Operand o : exp.operands()) {
                    MergeInstr.fixupGroupReferences(o.getChildExpression(), instr, isInLoop || o.isEvaluatedRepeatedly());
                }
            }
        }
    }

    @Override
    public final boolean mayCreateNewNodes() {
        int props = this.getAction().getSpecialProperties();
        return (props & 0x800000) == 0;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Configuration config = visitor.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();
        ItemType inputType = null;
        for (MergeSource mergeSource : this.mergeSources) {
            ContextItemStaticInfo rowContextItemType = contextInfo;
            if (mergeSource.getForEachItem() != null) {
                mergeSource.forEachItemOp.optimize(visitor, contextInfo);
                rowContextItemType = config.makeContextItemStaticInfo(mergeSource.getForEachItem().getItemType(), false);
            } else if (mergeSource.getForEachSource() != null) {
                mergeSource.forEachStreamOp.optimize(visitor, contextInfo);
                rowContextItemType = config.makeContextItemStaticInfo(NodeKindTest.DOCUMENT, false);
            }
            mergeSource.rowSelectOp.optimize(visitor, rowContextItemType);
            ItemType rowItemType = mergeSource.getRowSelect().getItemType();
            inputType = inputType == null ? rowItemType : Type.getCommonSuperType(inputType, rowItemType, th);
        }
        ContextItemStaticInfo cit = config.makeContextItemStaticInfo(inputType, false);
        this.setAction(this.getAction().optimize(visitor, cit));
        if (Literal.isEmptySequence(this.getAction())) {
            return this.getAction();
        }
        if (this.mergeSources.length == 1 && Literal.isEmptySequence(this.mergeSources[0].getRowSelect())) {
            return this.mergeSources[0].getRowSelect();
        }
        return this;
    }

    @Override
    public void prepareForStreaming() throws XPathException {
        for (MergeSource mergeSource : this.mergeSources) {
            mergeSource.prepareForStreaming();
        }
    }

    private void checkMergeAtt(SortKeyDefinition[] sortKeyDefs) throws XPathException {
        for (int i = 1; i < sortKeyDefs.length; ++i) {
            if (sortKeyDefs[0].isEqual(sortKeyDefs[i])) continue;
            throw new XPathException("Corresponding xsl:merge-key attributes in different xsl:merge-source elements do not have the same effective values", "XTDE2210");
        }
    }

    private LastPositionFinder getLastPositionFinder(final XPathContext context) {
        return new LastPositionFinder(){
            private int last = -1;

            @Override
            public int getLength() throws XPathException {
                if (this.last >= 0) {
                    return this.last;
                }
                AtomicComparer[] comps = MergeInstr.this.getComparators(context);
                GroupIterator mgi = context.getCurrentMergeGroupIterator();
                XPathContextMajor c1 = context.newContext();
                c1.setCurrentMergeGroupIterator(mgi);
                SequenceIterator inputIterator = MergeInstr.this.getMergedInputIterator(context, comps, c1);
                inputIterator = new MergeGroupingIterator(inputIterator, MergeInstr.this.getComparer(MergeInstr.this.mergeSources[0].mergeKeyDefinitions, comps), null);
                this.last = Count.steppingCount(inputIterator);
                return this.last;
            }
        };
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        try {
            AtomicComparer[] comps = this.getComparators(context);
            GroupIterator mgi = context.getCurrentMergeGroupIterator();
            XPathContextMajor c1 = context.newContext();
            c1.setCurrentMergeGroupIterator(mgi);
            SequenceIterator inputIterator = this.getMergedInputIterator(context, comps, c1);
            inputIterator = new MergeGroupingIterator(inputIterator, this.getComparer(this.mergeSources[0].mergeKeyDefinitions, comps), this.getLastPositionFinder(context));
            c1.setCurrentMergeGroupIterator((GroupIterator)inputIterator);
            XPathContextMinor c3 = c1.newMinorContext();
            c3.trackFocus(inputIterator);
            return new ContextMappingIterator(cxt -> this.getAction().iterate(cxt), c3);
        } catch (XPathException e) {
            e.maybeSetLocation(this.getLocation());
            throw e;
        }
    }

    private SequenceIterator getMergedInputIterator(XPathContext context, AtomicComparer[] comps, XPathContextMajor c1) throws XPathException {
        SequenceIterator inputIterator = EmptyIterator.getInstance();
        for (MergeSource ms : this.mergeSources) {
            SequenceIterator anchorsIter = null;
            if (ms.streamable && ms.getForEachSource() != null) continue;
            if (ms.getForEachSource() != null) {
                ParseOptions options = new ParseOptions(context.getConfiguration().getParseOptions());
                options.setSchemaValidationMode(ms.validation);
                options.setTopLevelType(ms.schemaType);
                options.setApplicableAccumulators(ms.accumulators);
                SequenceIterator uriIter = ms.getForEachSource().iterate(c1);
                XsltController controller = (XsltController)context.getController();
                AccumulatorManager accumulatorManager = controller.getAccumulatorManager();
                anchorsIter = new ItemMappingIterator(uriIter, baseItem -> {
                    String uri = baseItem.getStringValue();
                    NodeInfo node = DocumentFn.makeDoc(uri, this.getRetainedStaticContext().getStaticBaseUriString(), this.getPackageData(), options, c1, this.getLocation(), true);
                    if (node != null) {
                        accumulatorManager.setApplicableAccumulators(node.getTreeInfo(), ms.accumulators);
                    }
                    return node;
                });
                XPathContextMinor c2 = c1.newMinorContext();
                FocusIterator anchorsIterFocus = c2.trackFocus(anchorsIter);
                while (anchorsIterFocus.next() != null) {
                    XPathContextMinor c4 = c2.newMinorContext();
                    FocusIterator rowIntr = c4.trackFocus(ms.getRowSelect().iterate(c2));
                    MergeKeyMappingFunction addMergeKeys = new MergeKeyMappingFunction(c4, ms);
                    ContextMappingIterator contextMapKeysItr = new ContextMappingIterator(addMergeKeys, c4);
                    inputIterator = this.makeMergeIterator(inputIterator, comps, ms, contextMapKeysItr);
                }
                continue;
            }
            if (ms.getForEachItem() != null) {
                anchorsIter = ms.getForEachItem().iterate(c1);
                XPathContextMinor c2 = c1.newMinorContext();
                FocusIterator anchorsIterFocus = c2.trackFocus(anchorsIter);
                while (anchorsIterFocus.next() != null) {
                    inputIterator = this.getInputIterator(comps, inputIterator, ms, c2);
                }
                continue;
            }
            inputIterator = this.getInputIterator(comps, inputIterator, ms, c1);
        }
        return inputIterator;
    }

    private SequenceIterator getInputIterator(AtomicComparer[] comps, SequenceIterator inputIterator, MergeSource ms, XPathContext c2) throws XPathException {
        XPathContextMinor c4 = c2.newMinorContext();
        c4.setTemporaryOutputState(171);
        FocusIterator rowIntr = c4.trackFocus(ms.getRowSelect().iterate(c2));
        MergeKeyMappingFunction addMergeKeys = new MergeKeyMappingFunction(c4, ms);
        ContextMappingIterator contextMapKeysItr = new ContextMappingIterator(addMergeKeys, c4);
        inputIterator = this.makeMergeIterator(inputIterator, comps, ms, contextMapKeysItr);
        return inputIterator;
    }

    private AtomicComparer[] getComparators(XPathContext context) throws XPathException {
        AtomicComparer[] comps = this.comparators;
        if (this.comparators == null) {
            SortKeyDefinition[] tempSKeys = new SortKeyDefinition[this.mergeSources.length];
            for (int i = 0; i < this.mergeSources[0].mergeKeyDefinitions.size(); ++i) {
                for (int j = 0; j < this.mergeSources.length; ++j) {
                    tempSKeys[j] = this.mergeSources[j].mergeKeyDefinitions.getSortKeyDefinition(i).fix(context);
                }
                this.checkMergeAtt(tempSKeys);
            }
            comps = new AtomicComparer[this.mergeSources[0].mergeKeyDefinitions.size()];
            for (int s = 0; s < this.mergeSources[0].mergeKeyDefinitions.size(); ++s) {
                AtomicComparer comp = this.mergeSources[0].mergeKeyDefinitions.getSortKeyDefinition(s).getFinalComparator();
                if (comp == null) {
                    comp = this.mergeSources[0].mergeKeyDefinitions.getSortKeyDefinition(s).makeComparator(context);
                }
                comps[s] = comp;
            }
        }
        return comps;
    }

    private SequenceIterator makeMergeIterator(SequenceIterator result, AtomicComparer[] comps, MergeSource ms, ContextMappingIterator contextMapKeysItr) throws XPathException {
        result = result == null || result instanceof EmptyIterator ? contextMapKeysItr : new MergeIterator(result, contextMapKeysItr, this.getComparer(ms.mergeKeyDefinitions, comps));
        return result;
    }

    @Override
    public Iterable<Operand> operands() {
        ArrayList<Operand> list = new ArrayList<Operand>(6);
        list.add(this.actionOp);
        if (this.mergeSources != null) {
            for (MergeSource ms : this.mergeSources) {
                if (ms.forEachItemOp != null) {
                    list.add(ms.forEachItemOp);
                }
                if (ms.forEachStreamOp != null) {
                    list.add(ms.forEachStreamOp);
                }
                if (ms.rowSelectOp != null) {
                    list.add(ms.rowSelectOp);
                }
                list.add(new Operand(this, ms.mergeKeyDefinitions, OperandRole.SINGLE_ATOMIC));
            }
        }
        return list;
    }

    public Expression getGroupingKey() {
        return this.mergeSources[0].mergeKeyDefinitions.getSortKeyDefinition(0).getSortKey();
    }

    public ItemOrderComparer getComparer(SortKeyDefinitionList sKeys, AtomicComparer[] comps) {
        return (a, b) -> {
            ObjectValue aObj = (ObjectValue)a;
            ObjectValue bObj = (ObjectValue)b;
            ItemWithMergeKeys aItem = (ItemWithMergeKeys)aObj.getObject();
            ItemWithMergeKeys bItem = (ItemWithMergeKeys)bObj.getObject();
            for (int i = 0; i < sKeys.size(); ++i) {
                int val;
                try {
                    val = comps[i].compareAtomicValues(aItem.sortKeyValues.get(i), bItem.sortKeyValues.get(i));
                } catch (NoDynamicContextException e) {
                    throw new IllegalStateException(e);
                }
                if (val == 0) continue;
                return val;
            }
            return 0;
        };
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        MergeInstr newMerge = new MergeInstr();
        MergeSource[] c2 = new MergeSource[this.mergeSources.length];
        Expression a2 = this.getAction().copy(rebindings);
        for (int c = 0; c < this.mergeSources.length; ++c) {
            c2[c] = this.mergeSources[c].copyMergeSource(newMerge, rebindings);
        }
        return newMerge.init(c2, a2);
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("merge", this);
        for (MergeSource mergeSource : this.mergeSources) {
            SchemaType type;
            out.startSubsidiaryElement("mergeSrc");
            if (mergeSource.sourceName != null && !mergeSource.sourceName.startsWith("saxon-merge-source-")) {
                out.emitAttribute("name", mergeSource.sourceName);
            }
            if (mergeSource.validation != 4 && mergeSource.validation != 8) {
                out.emitAttribute("validation", Validation.toString(mergeSource.validation));
            }
            if (mergeSource.validation == 8 && (type = mergeSource.schemaType) != null) {
                out.emitAttribute("type", type.getStructuredQName());
            }
            if (mergeSource.accumulators != null && !mergeSource.accumulators.isEmpty()) {
                FastStringBuffer fsb = new FastStringBuffer(256);
                for (Accumulator acc : mergeSource.accumulators) {
                    if (!fsb.isEmpty()) {
                        fsb.append(" ");
                    }
                    fsb.append(acc.getAccumulatorName().getEQName());
                }
                out.emitAttribute("accum", fsb.toString());
            }
            if (mergeSource.streamable) {
                out.emitAttribute("flags", "s");
            }
            if (mergeSource.getForEachItem() != null) {
                out.setChildRole("forEachItem");
                mergeSource.getForEachItem().export(out);
            }
            if (mergeSource.getForEachSource() != null) {
                out.setChildRole("forEachStream");
                mergeSource.getForEachSource().export(out);
            }
            out.setChildRole("selectRows");
            mergeSource.getRowSelect().export(out);
            mergeSource.getMergeKeyDefinitionSet().export(out);
            out.endSubsidiaryElement();
        }
        out.setChildRole("action");
        this.getAction().export(out);
        out.endElement();
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        try (SequenceIterator iter = this.iterate(context);){
            iter.forEachOrFail(it -> output.append(it, this.getLocation(), 524288));
        } catch (XPathException e) {
            e.maybeSetLocation(this.getLocation());
            e.maybeSetContext(context);
            throw e;
        }
        return null;
    }

    @Override
    public String getStreamerName() {
        return "MergeInstr";
    }

    public static class MergeKeyMappingFunction
    implements ContextMappingFunction {
        private MergeSource ms;
        private XPathContext baseContext;
        private XPathContext keyContext;
        private ManualIterator manualIterator;

        public MergeKeyMappingFunction(XPathContext baseContext, MergeSource ms) {
            this.baseContext = baseContext;
            this.ms = ms;
            this.keyContext = baseContext.newMinorContext();
            this.keyContext.setTemporaryOutputState(171);
            this.manualIterator = new ManualIterator();
            this.manualIterator.setPosition(1);
            this.keyContext.setCurrentIterator(this.manualIterator);
        }

        @Override
        public SequenceIterator map(XPathContext context) throws XPathException {
            Item currentItem = context.getContextItem();
            this.manualIterator.setContextItem(currentItem);
            ItemWithMergeKeys newItem = new ItemWithMergeKeys(currentItem, this.ms.mergeKeyDefinitions, this.ms.sourceName, this.keyContext);
            return SingletonIterator.makeIterator(new ObjectValue<ItemWithMergeKeys>(newItem));
        }
    }

    public static class MergeSource {
        private MergeInstr instruction;
        public Location location;
        private Operand forEachItemOp = null;
        private Operand forEachStreamOp = null;
        private Operand rowSelectOp = null;
        public String sourceName = null;
        public SortKeyDefinitionList mergeKeyDefinitions = null;
        public String baseURI = null;
        public int validation;
        public SchemaType schemaType;
        public boolean streamable;
        public Set<Accumulator> accumulators;
        public Object invertedAction;

        public MergeSource(MergeInstr mi) {
            this.instruction = mi;
        }

        public MergeSource(MergeInstr instruction, Expression forEachItem, Expression forEachStream, Expression rSelect, String name, SortKeyDefinitionList sKeys, String baseURI) {
            this.instruction = instruction;
            if (forEachItem != null) {
                this.initForEachItem(instruction, forEachItem);
            }
            if (forEachStream != null) {
                this.initForEachStream(instruction, forEachStream);
            }
            if (rSelect != null) {
                this.initRowSelect(instruction, rSelect);
            }
            this.sourceName = name;
            this.mergeKeyDefinitions = sKeys;
            this.baseURI = baseURI;
        }

        public void initForEachItem(MergeInstr instruction, Expression forEachItem) {
            this.forEachItemOp = new Operand(instruction, forEachItem, OperandRole.INSPECT);
        }

        public void initForEachStream(MergeInstr instruction, Expression forEachStream) {
            this.forEachStreamOp = new Operand(instruction, forEachStream, OperandRole.INSPECT);
        }

        public void initRowSelect(MergeInstr instruction, Expression rowSelect) {
            this.rowSelectOp = new Operand(instruction, rowSelect, ROW_SELECT);
        }

        public void setStreamable(boolean streamable) {
            this.streamable = streamable;
            if (streamable && this.instruction.getConfiguration().getBooleanProperty(Feature.STREAMING_FALLBACK)) {
                this.streamable = false;
                Expression select = this.rowSelectOp.getChildExpression();
                this.rowSelectOp.setChildExpression(SystemFunction.makeCall("snapshot", select.getRetainedStaticContext(), select));
            }
        }

        public MergeSource copyMergeSource(MergeInstr newInstr, RebindingMap rebindings) {
            SortKeyDefinition[] newKeyDef = new SortKeyDefinition[this.mergeKeyDefinitions.size()];
            for (int i = 0; i < this.mergeKeyDefinitions.size(); ++i) {
                newKeyDef[i] = this.mergeKeyDefinitions.getSortKeyDefinition(i).copy(rebindings);
            }
            MergeSource ms = new MergeSource(newInstr, MergeSource.copy(this.getForEachItem(), rebindings), MergeSource.copy(this.getForEachSource(), rebindings), MergeSource.copy(this.getRowSelect(), rebindings), this.sourceName, new SortKeyDefinitionList(newKeyDef), this.baseURI);
            ms.validation = this.validation;
            ms.schemaType = this.schemaType;
            ms.streamable = this.streamable;
            ms.location = this.location;
            return ms;
        }

        private static Expression copy(Expression exp, RebindingMap rebindings) {
            return exp == null ? null : exp.copy(rebindings);
        }

        public Expression getForEachItem() {
            return this.forEachItemOp == null ? null : this.forEachItemOp.getChildExpression();
        }

        public void setForEachItem(Expression forEachItem) {
            if (forEachItem != null) {
                this.forEachItemOp.setChildExpression(forEachItem);
            }
        }

        public Expression getForEachSource() {
            return this.forEachStreamOp == null ? null : this.forEachStreamOp.getChildExpression();
        }

        public void setForEachStream(Expression forEachStream) {
            if (forEachStream != null) {
                this.forEachStreamOp.setChildExpression(forEachStream);
            }
        }

        public Expression getRowSelect() {
            return this.rowSelectOp.getChildExpression();
        }

        public void setRowSelect(Expression rowSelect) {
            this.rowSelectOp.setChildExpression(rowSelect);
        }

        public SortKeyDefinitionList getMergeKeyDefinitionSet() {
            return this.mergeKeyDefinitions;
        }

        public void setMergeKeyDefinitionSet(SortKeyDefinitionList keys) {
            this.mergeKeyDefinitions = keys;
        }

        public void prepareForStreaming() throws XPathException {
        }
    }
}


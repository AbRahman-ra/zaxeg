/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.net.URI;
import java.net.URISyntaxException;
import net.sf.saxon.Controller;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.expr.CardinalityChecker;
import net.sf.saxon.expr.ContextMappingFunction;
import net.sf.saxon.expr.ContextMappingIterator;
import net.sf.saxon.expr.ContextSwitchingExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FirstItemExpression;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.XPathContextMinor;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.CodepointCollator;
import net.sf.saxon.expr.sort.GroupAdjacentIterator;
import net.sf.saxon.expr.sort.GroupByIterator;
import net.sf.saxon.expr.sort.GroupEndingIterator;
import net.sf.saxon.expr.sort.GroupIterator;
import net.sf.saxon.expr.sort.GroupStartingIterator;
import net.sf.saxon.expr.sort.SortKeyDefinition;
import net.sf.saxon.expr.sort.SortKeyDefinitionList;
import net.sf.saxon.expr.sort.SortKeyEvaluator;
import net.sf.saxon.expr.sort.SortedGroupIterator;
import net.sf.saxon.functions.CurrentGroupCall;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.om.FocusIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;

public class ForEachGroup
extends Instruction
implements SortKeyEvaluator,
ContextMappingFunction,
ContextSwitchingExpression {
    public static final int GROUP_BY = 0;
    public static final int GROUP_ADJACENT = 1;
    public static final int GROUP_STARTING = 2;
    public static final int GROUP_ENDING = 3;
    private byte algorithm;
    private int keyItemType;
    private StringCollator collator = null;
    private transient AtomicComparer[] sortComparators = null;
    private boolean composite = false;
    private boolean isInFork = false;
    private Operand selectOp;
    private Operand actionOp;
    private Operand keyOp;
    private Operand collationOp;
    private Operand sortKeysOp;

    public ForEachGroup(Expression select, Expression action, byte algorithm, Expression key, StringCollator collator, Expression collationNameExpression, SortKeyDefinitionList sortKeys) {
        this.selectOp = new Operand(this, select, OperandRole.FOCUS_CONTROLLING_SELECT);
        this.actionOp = new Operand(this, action, OperandRole.FOCUS_CONTROLLED_ACTION);
        OperandRole keyRole = algorithm == 3 || algorithm == 2 ? OperandRole.PATTERN : OperandRole.NEW_FOCUS_ATOMIC;
        this.keyOp = new Operand(this, key, keyRole);
        if (collationNameExpression != null) {
            this.collationOp = new Operand(this, collationNameExpression, OperandRole.SINGLE_ATOMIC);
        }
        if (sortKeys != null) {
            this.sortKeysOp = new Operand(this, sortKeys, OperandRole.SINGLE_ATOMIC);
        }
        this.algorithm = algorithm;
        this.collator = collator;
        for (Operand o : this.operands()) {
            this.adoptChildExpression(o.getChildExpression());
        }
    }

    @Override
    public int getInstructionNameCode() {
        return 157;
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operandSparseList(this.selectOp, this.actionOp, this.keyOp, this.collationOp, this.sortKeysOp);
    }

    @Override
    public Expression getSelectExpression() {
        return this.selectOp.getChildExpression();
    }

    @Override
    public Expression getActionExpression() {
        return this.actionOp.getChildExpression();
    }

    public byte getAlgorithm() {
        return this.algorithm;
    }

    public Expression getGroupingKey() {
        return this.keyOp.getChildExpression();
    }

    public int getKeyItemType() {
        return this.keyItemType;
    }

    public SortKeyDefinitionList getSortKeyDefinitions() {
        return this.sortKeysOp == null ? null : (SortKeyDefinitionList)this.sortKeysOp.getChildExpression();
    }

    public AtomicComparer[] getSortKeyComparators() {
        return this.sortComparators;
    }

    public StringCollator getCollation() {
        return this.collator;
    }

    public URI getBaseURI() {
        try {
            return this.getRetainedStaticContext().getStaticBaseUri();
        } catch (XPathException err) {
            return null;
        }
    }

    public boolean isComposite() {
        return this.composite;
    }

    public void setComposite(boolean composite) {
        this.composite = composite;
    }

    public boolean isInFork() {
        return this.isInFork;
    }

    public void setIsInFork(boolean inFork) {
        this.isInFork = inFork;
    }

    @Override
    public boolean allowExtractingCommonSubexpressions() {
        return false;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        ItemType selectedItemType;
        this.selectOp.typeCheck(visitor, contextInfo);
        if (this.collationOp != null) {
            this.collationOp.typeCheck(visitor, contextInfo);
        }
        if ((selectedItemType = this.getSelectExpression().getItemType()) == ErrorType.getInstance()) {
            return Literal.makeEmptySequence();
        }
        for (Operand o : this.operands()) {
            ForEachGroup.fixupGroupReferences(this, this, selectedItemType, false);
        }
        ContextItemStaticInfo cit = visitor.getConfiguration().makeContextItemStaticInfo(selectedItemType, false);
        cit.setContextSettingExpression(this.getSelectExpression());
        this.actionOp.typeCheck(visitor, cit);
        this.keyOp.typeCheck(visitor, cit);
        if (Literal.isEmptySequence(this.getSelectExpression())) {
            return this.getSelectExpression();
        }
        if (Literal.isEmptySequence(this.getActionExpression())) {
            return this.getActionExpression();
        }
        if (this.getSortKeyDefinitions() != null) {
            boolean allFixed = true;
            for (SortKeyDefinition sk : this.getSortKeyDefinitions()) {
                Expression sortKey = sk.getSortKey();
                sortKey = sortKey.typeCheck(visitor, cit);
                if (sk.isBackwardsCompatible()) {
                    sortKey = FirstItemExpression.makeFirstItemExpression(sortKey);
                } else {
                    RoleDiagnostic role = new RoleDiagnostic(4, "xsl:sort/select", 0);
                    role.setErrorCode("XTTE1020");
                    sortKey = CardinalityChecker.makeCardinalityChecker(sortKey, 24576, role);
                }
                sk.setSortKey(sortKey, true);
                sk.typeCheck(visitor, contextInfo);
                if (sk.isFixed()) {
                    AtomicComparer comp = sk.makeComparator(visitor.getStaticContext().makeEarlyEvaluationContext());
                    sk.setFinalComparator(comp);
                    continue;
                }
                allFixed = false;
            }
            if (allFixed) {
                this.sortComparators = new AtomicComparer[this.getSortKeyDefinitions().size()];
                for (int i = 0; i < this.getSortKeyDefinitions().size(); ++i) {
                    this.sortComparators[i] = this.getSortKeyDefinitions().getSortKeyDefinition(i).getFinalComparator();
                }
            }
        }
        this.keyItemType = this.getGroupingKey().getItemType().getPrimitiveType();
        return this;
    }

    private static void fixupGroupReferences(Expression exp, ForEachGroup feg, ItemType selectedItemType, boolean isInLoop) {
        block2: {
            block4: {
                ForEachGroup feg2;
                block5: {
                    block3: {
                        if (exp == null) break block2;
                        if (!(exp instanceof CurrentGroupCall)) break block3;
                        ((CurrentGroupCall)exp).setControllingInstruction(feg, selectedItemType, isInLoop);
                        break block2;
                    }
                    if (!(exp instanceof ForEachGroup)) break block4;
                    feg2 = (ForEachGroup)exp;
                    if (feg2 != feg) break block5;
                    ForEachGroup.fixupGroupReferences(feg2.getActionExpression(), feg, selectedItemType, false);
                    break block2;
                }
                ForEachGroup.fixupGroupReferences(feg2.getSelectExpression(), feg, selectedItemType, isInLoop);
                ForEachGroup.fixupGroupReferences(feg2.getGroupingKey(), feg, selectedItemType, isInLoop);
                if (feg2.getSortKeyDefinitions() == null) break block2;
                for (SortKeyDefinition skd : feg2.getSortKeyDefinitions()) {
                    ForEachGroup.fixupGroupReferences(skd.getOrder(), feg, selectedItemType, isInLoop);
                    ForEachGroup.fixupGroupReferences(skd.getCaseOrder(), feg, selectedItemType, isInLoop);
                    ForEachGroup.fixupGroupReferences(skd.getDataTypeExpression(), feg, selectedItemType, isInLoop);
                    ForEachGroup.fixupGroupReferences(skd.getLanguage(), feg, selectedItemType, isInLoop);
                    ForEachGroup.fixupGroupReferences(skd.getCollationNameExpression(), feg, selectedItemType, isInLoop);
                    ForEachGroup.fixupGroupReferences(skd.getOrder(), feg, selectedItemType, isInLoop);
                }
                break block2;
            }
            for (Operand o : exp.operands()) {
                ForEachGroup.fixupGroupReferences(o.getChildExpression(), feg, selectedItemType, isInLoop || o.isHigherOrder());
            }
        }
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        this.selectOp.optimize(visitor, contextItemType);
        ItemType selectedItemType = this.getSelectExpression().getItemType();
        ContextItemStaticInfo sit = visitor.getConfiguration().makeContextItemStaticInfo(selectedItemType, false);
        sit.setContextSettingExpression(this.getSelectExpression());
        this.actionOp.optimize(visitor, sit);
        this.keyOp.optimize(visitor, sit);
        if (Literal.isEmptySequence(this.getSelectExpression())) {
            return this.getSelectExpression();
        }
        if (Literal.isEmptySequence(this.getActionExpression())) {
            return this.getActionExpression();
        }
        if (this.getSortKeyDefinitions() != null) {
            for (SortKeyDefinition skd : this.getSortKeyDefinitions()) {
                Expression sortKey = skd.getSortKey();
                sortKey = sortKey.optimize(visitor, sit);
                skd.setSortKey(sortKey, true);
            }
        }
        if (this.collationOp != null) {
            this.collationOp.optimize(visitor, contextItemType);
        }
        if (this.collator == null && this.getCollationNameExpression() instanceof StringLiteral) {
            String collation = ((StringLiteral)this.getCollationNameExpression()).getStringValue();
            try {
                URI collationURI = new URI(collation);
                if (!collationURI.isAbsolute()) {
                    collationURI = this.getStaticBaseURI().resolve(collationURI);
                    String collationNameString = collationURI.toString();
                    this.setCollationNameExpression(new StringLiteral(collationNameString));
                    this.collator = visitor.getConfiguration().getCollation(collationNameString);
                    if (this.collator == null) {
                        XPathException err = new XPathException("Unknown collation " + Err.wrap(collationURI.toString(), 7));
                        err.setErrorCode("XTDE1110");
                        err.setLocation(this.getLocation());
                        throw err;
                    }
                }
            } catch (URISyntaxException err) {
                XPathException e = new XPathException("Collation name '" + this.getCollationNameExpression() + "' is not a valid URI");
                e.setErrorCode("XTDE1110");
                e.setLocation(this.getLocation());
                throw e;
            }
        }
        return this;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        SortKeyDefinition[] newKeyDef = null;
        if (this.getSortKeyDefinitions() != null) {
            newKeyDef = new SortKeyDefinition[this.getSortKeyDefinitions().size()];
            for (int i = 0; i < this.getSortKeyDefinitions().size(); ++i) {
                newKeyDef[i] = this.getSortKeyDefinitions().getSortKeyDefinition(i).copy(rebindings);
            }
        }
        ForEachGroup feg = new ForEachGroup(this.getSelectExpression().copy(rebindings), this.getActionExpression().copy(rebindings), this.algorithm, this.getGroupingKey().copy(rebindings), this.collator, this.getCollationNameExpression().copy(rebindings), newKeyDef == null ? null : new SortKeyDefinitionList(newKeyDef));
        ExpressionTool.copyLocationInfo(this, feg);
        feg.setComposite(this.isComposite());
        ForEachGroup.fixupGroupReferences(feg, feg, this.getSelectExpression().getItemType(), false);
        return feg;
    }

    @Override
    public ItemType getItemType() {
        return this.getActionExpression().getItemType();
    }

    @Override
    public int computeDependencies() {
        int dependencies = 0;
        dependencies |= this.getSelectExpression().getDependencies();
        dependencies |= this.getGroupingKey().getDependencies() & 0xFFFFFFE1;
        dependencies |= this.getActionExpression().getDependencies() & 0xFFFFFFC1;
        if (this.getSortKeyDefinitions() != null) {
            for (SortKeyDefinition skd : this.getSortKeyDefinitions()) {
                dependencies |= skd.getSortKey().getDependencies() & 0xFFFFFFE1;
                Expression e = skd.getCaseOrder();
                if (e != null && !(e instanceof Literal)) {
                    dependencies |= e.getDependencies();
                }
                if ((e = skd.getDataTypeExpression()) != null && !(e instanceof Literal)) {
                    dependencies |= e.getDependencies();
                }
                if ((e = skd.getLanguage()) == null || e instanceof Literal) continue;
                dependencies |= e.getDependencies();
            }
        }
        if (this.getCollationNameExpression() != null) {
            dependencies |= this.getCollationNameExpression().getDependencies();
        }
        return dependencies;
    }

    @Override
    public int computeSpecialProperties() {
        int p = super.computeSpecialProperties();
        return p |= this.getActionExpression().getSpecialProperties() & 0x8000000;
    }

    @Override
    public final boolean mayCreateNewNodes() {
        int props = this.getActionExpression().getSpecialProperties();
        return (props & 0x800000) == 0;
    }

    @Override
    public String getStreamerName() {
        return "ForEachGroup";
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        PathMap.PathMapNodeSet target = this.getSelectExpression().addToPathMap(pathMap, pathMapNodeSet);
        if (this.getCollationNameExpression() != null) {
            this.getCollationNameExpression().addToPathMap(pathMap, pathMapNodeSet);
        }
        if (this.getSortKeyDefinitions() != null) {
            for (SortKeyDefinition skd : this.getSortKeyDefinitions()) {
                skd.getSortKey().addToPathMap(pathMap, target);
                Expression e = skd.getOrder();
                if (e != null) {
                    e.addToPathMap(pathMap, pathMapNodeSet);
                }
                if ((e = skd.getCaseOrder()) != null) {
                    e.addToPathMap(pathMap, pathMapNodeSet);
                }
                if ((e = skd.getDataTypeExpression()) != null) {
                    e.addToPathMap(pathMap, pathMapNodeSet);
                }
                if ((e = skd.getLanguage()) != null) {
                    e.addToPathMap(pathMap, pathMapNodeSet);
                }
                if ((e = skd.getCollationNameExpression()) == null) continue;
                e.addToPathMap(pathMap, pathMapNodeSet);
            }
        }
        return this.getActionExpression().addToPathMap(pathMap, target);
    }

    @Override
    public void checkPermittedContents(SchemaType parentType, boolean whole) throws XPathException {
        this.getActionExpression().checkPermittedContents(parentType, false);
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        Controller controller = context.getController();
        assert (controller != null);
        PipelineConfiguration pipe = output.getPipelineConfiguration();
        GroupIterator groupIterator = this.getGroupIterator(context);
        XPathContextMajor c2 = context.newContext();
        c2.setOrigin(this);
        FocusIterator focusIterator = c2.trackFocus(groupIterator);
        c2.setCurrentGroupIterator(groupIterator);
        c2.setCurrentTemplateRule(null);
        pipe.setXPathContext(c2);
        if (controller.isTracing()) {
            Item item;
            TraceListener listener = controller.getTraceListener();
            assert (listener != null);
            while ((item = focusIterator.next()) != null) {
                listener.startCurrentItem(item);
                this.getActionExpression().process(output, c2);
                listener.endCurrentItem(item);
            }
        } else {
            while (focusIterator.next() != null) {
                this.getActionExpression().process(output, c2);
            }
        }
        pipe.setXPathContext(context);
        return null;
    }

    public Expression getCollationNameExpression() {
        return this.collationOp == null ? null : this.collationOp.getChildExpression();
    }

    private StringCollator getCollator(XPathContext context) throws XPathException {
        if (this.getCollationNameExpression() != null) {
            StringValue collationValue = (StringValue)this.getCollationNameExpression().evaluateItem(context);
            assert (collationValue != null);
            String cname = collationValue.getStringValue();
            try {
                return context.getConfiguration().getCollation(cname, this.getStaticBaseURIString(), "FOCH0002");
            } catch (XPathException e) {
                e.setLocation(this.getLocation());
                throw e;
            }
        }
        return CodepointCollator.getInstance();
    }

    private GroupIterator getGroupIterator(XPathContext context) throws XPathException {
        return this.getGroupIterator(this.getSelectExpression(), context);
    }

    public GroupIterator getGroupIterator(Expression select, XPathContext context) throws XPathException {
        LastPositionFinder groupIterator;
        switch (this.algorithm) {
            case 0: {
                StringCollator coll = this.collator;
                if (coll == null) {
                    coll = this.getCollator(context);
                }
                XPathContextMinor c2 = context.newMinorContext();
                FocusIterator population = c2.trackFocus(select.iterate(context));
                groupIterator = new GroupByIterator(population, this.getGroupingKey(), c2, coll, this.composite);
                break;
            }
            case 1: {
                StringCollator coll = this.collator;
                if (coll == null) {
                    coll = this.getCollator(context);
                }
                groupIterator = new GroupAdjacentIterator(select, this.getGroupingKey(), context, coll, this.composite);
                break;
            }
            case 2: {
                groupIterator = new GroupStartingIterator(select, (Pattern)this.getGroupingKey(), context);
                break;
            }
            case 3: {
                groupIterator = new GroupEndingIterator(select, (Pattern)this.getGroupingKey(), context);
                break;
            }
            default: {
                throw new AssertionError((Object)"Unknown grouping algorithm");
            }
        }
        if (this.getSortKeyDefinitions() != null) {
            AtomicComparer[] comps = this.sortComparators;
            XPathContextMinor xpc = context.newMinorContext();
            if (comps == null) {
                comps = new AtomicComparer[this.getSortKeyDefinitions().size()];
                for (int s = 0; s < this.getSortKeyDefinitions().size(); ++s) {
                    comps[s] = this.getSortKeyDefinitions().getSortKeyDefinition(s).makeComparator(xpc);
                }
            }
            groupIterator = new SortedGroupIterator(xpc, (GroupIterator)((Object)groupIterator), this, comps);
        }
        return groupIterator;
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        GroupIterator master = this.getGroupIterator(context);
        XPathContextMajor c2 = context.newContext();
        c2.setOrigin(this);
        c2.trackFocus(master);
        c2.setCurrentGroupIterator(master);
        c2.setCurrentTemplateRule(null);
        return new ContextMappingIterator(this, c2);
    }

    @Override
    public SequenceIterator map(XPathContext context) throws XPathException {
        return this.getActionExpression().iterate(context);
    }

    @Override
    public AtomicValue evaluateSortKey(int n, XPathContext c) throws XPathException {
        return (AtomicValue)this.getSortKeyDefinitions().getSortKeyDefinition(n).getSortKey().evaluateItem(c);
    }

    public SortKeyDefinitionList getSortKeyDefinitionList() {
        if (this.sortKeysOp == null) {
            return null;
        }
        return (SortKeyDefinitionList)this.sortKeysOp.getChildExpression();
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("forEachGroup", this);
        out.emitAttribute("algorithm", ForEachGroup.getAlgorithmName(this.algorithm));
        String flags = "";
        if (this.composite) {
            flags = "c";
        }
        if (this.isInFork()) {
            flags = flags + "k";
        }
        if (!flags.isEmpty()) {
            out.emitAttribute("flags", flags);
        }
        out.setChildRole("select");
        this.getSelectExpression().export(out);
        if (this.algorithm == 0 || this.algorithm == 1) {
            out.setChildRole("key");
            this.getGroupingKey().export(out);
        } else {
            out.setChildRole("match");
            this.getGroupingKey().export(out);
        }
        if (this.getSortKeyDefinitions() != null) {
            out.setChildRole("sort");
            this.getSortKeyDefinitionList().export(out);
        }
        if (this.getCollationNameExpression() != null) {
            out.setChildRole("collation");
            this.getCollationNameExpression().export(out);
        }
        out.setChildRole("content");
        this.getActionExpression().export(out);
        out.endElement();
    }

    private static String getAlgorithmName(byte algorithm) {
        switch (algorithm) {
            case 0: {
                return "by";
            }
            case 1: {
                return "adjacent";
            }
            case 2: {
                return "starting";
            }
            case 3: {
                return "ending";
            }
        }
        return "** unknown algorithm **";
    }

    public void setSelect(Expression select) {
        this.selectOp.setChildExpression(select);
    }

    public void setAction(Expression action) {
        this.actionOp.setChildExpression(action);
    }

    public void setKey(Expression key) {
        this.keyOp.setChildExpression(key);
    }

    public void setCollationNameExpression(Expression collationNameExpression) {
        if (this.collationOp == null) {
            this.collationOp = new Operand(this, collationNameExpression, OperandRole.SINGLE_ATOMIC);
        } else {
            this.collationOp.setChildExpression(collationNameExpression);
        }
    }

    public void setSortKeyDefinitions(SortKeyDefinitionList sortKeyDefinitions) {
        if (this.sortKeysOp == null) {
            this.sortKeysOp = new Operand(this, sortKeyDefinitions, OperandRole.SINGLE_ATOMIC);
        } else {
            this.sortKeysOp.setChildExpression(sortKeyDefinitions);
        }
    }
}


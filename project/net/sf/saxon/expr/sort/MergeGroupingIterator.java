/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.sort.GroupIterator;
import net.sf.saxon.expr.sort.ItemOrderComparer;
import net.sf.saxon.expr.sort.ItemWithMergeKeys;
import net.sf.saxon.om.AtomicArray;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.ObjectValue;

public class MergeGroupingIterator
implements GroupIterator,
LookaheadIterator,
LastPositionFinder {
    private SequenceIterator baseItr;
    private ObjectValue<ItemWithMergeKeys> currenti = null;
    private ObjectValue<ItemWithMergeKeys> next;
    private List<Item> currentMembers;
    private Map<String, List<Item>> currentSourceMembers;
    private ItemOrderComparer comparer;
    private int position = 0;
    List<AtomicValue> compositeMergeKey;
    private LastPositionFinder lastPositionFinder;

    public MergeGroupingIterator(SequenceIterator p1, ItemOrderComparer comp, LastPositionFinder lpf) throws XPathException {
        this.baseItr = p1;
        this.next = (ObjectValue)p1.next();
        if (this.next != null) {
            this.compositeMergeKey = this.next.getObject().sortKeyValues;
        }
        this.comparer = comp;
        this.lastPositionFinder = lpf;
    }

    /*
     * Exception decompiling
     */
    private void advance() throws XPathException {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [2[UNCONDITIONALDOLOOP]], but top level block is 0[TRYBLOCK]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:538)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         *     at async.DecompilerRunnable.cfrDecompilation(DecompilerRunnable.java:348)
         *     at async.DecompilerRunnable.call(DecompilerRunnable.java:309)
         *     at async.DecompilerRunnable.call(DecompilerRunnable.java:31)
         *     at java.util.concurrent.FutureTask.run(FutureTask.java:266)
         *     at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
         *     at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
         *     at java.lang.Thread.run(Thread.java:750)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    @Override
    public boolean hasNext() {
        return this.next != null;
    }

    @Override
    public Item next() throws XPathException {
        if (this.next == null) {
            this.currenti = null;
            this.position = -1;
            return null;
        }
        this.currenti = this.next;
        ++this.position;
        this.compositeMergeKey = ((ItemWithMergeKeys)this.next.getObject()).sortKeyValues;
        this.advance();
        return this.currenti.getObject().baseItem;
    }

    @Override
    public void close() {
        this.baseItr.close();
    }

    @Override
    public int getLength() throws XPathException {
        return this.lastPositionFinder.getLength();
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LOOKAHEAD, SequenceIterator.Property.LAST_POSITION_FINDER);
    }

    @Override
    public AtomicSequence getCurrentGroupingKey() {
        return new AtomicArray(this.compositeMergeKey);
    }

    @Override
    public SequenceIterator iterateCurrentGroup() {
        return new ListIterator<Item>(this.currentMembers);
    }

    public SequenceIterator iterateCurrentGroup(String source) {
        List<Item> sourceMembers = this.currentSourceMembers.get(source);
        if (sourceMembers == null) {
            return EmptyIterator.emptyIterator();
        }
        return new ListIterator<Item>(sourceMembers);
    }

    private static /* synthetic */ List lambda$advance$0(String k) {
        return new ArrayList();
    }
}


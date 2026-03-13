/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.accum;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.accum.Accumulator;
import net.sf.saxon.expr.accum.AccumulatorData;
import net.sf.saxon.expr.accum.FailedAccumulatorData;
import net.sf.saxon.expr.accum.IAccumulatorData;
import net.sf.saxon.expr.accum.PathMappedAccumulatorData;
import net.sf.saxon.expr.accum.VirtualAccumulatorData;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyTree;
import net.sf.saxon.tree.wrapper.VirtualCopy;
import net.sf.saxon.tree.wrapper.VirtualTreeInfo;

public class AccumulatorManager {
    private transient WeakHashMap<TreeInfo, Map<Accumulator, IAccumulatorData>> accumulatorDataIndex = new WeakHashMap();
    private transient WeakHashMap<TreeInfo, Set<? extends Accumulator>> applicableAccumulators = new WeakHashMap();
    private static AccumulatorData MARKER = new AccumulatorData(null);

    public void setApplicableAccumulators(TreeInfo tree, Set<? extends Accumulator> accumulators) {
        this.applicableAccumulators.put(tree, accumulators);
    }

    public boolean isApplicable(TreeInfo tree, Accumulator accumulator) {
        Set<? extends Accumulator> accSet = this.applicableAccumulators.get(tree);
        return accSet == null || accSet.contains(accumulator);
    }

    public synchronized IAccumulatorData getAccumulatorData(TreeInfo doc, Accumulator acc, XPathContext context) throws XPathException {
        Object original;
        Map<Accumulator, IAccumulatorData> map = this.accumulatorDataIndex.get(doc);
        if (map != null) {
            IAccumulatorData data = map.get(acc);
            if (data != null) {
                if (data == MARKER) {
                    throw new XPathException("Accumulator " + acc.getAccumulatorName().getDisplayName() + " requires access to its own value", "XTDE3400");
                }
                return data;
            }
        } else {
            map = new HashMap<Accumulator, IAccumulatorData>();
            map.put(acc, MARKER);
            this.accumulatorDataIndex.put(doc, map);
        }
        if (doc instanceof VirtualTreeInfo && ((VirtualTreeInfo)doc).isCopyAccumulators()) {
            original = ((VirtualCopy)doc.getRootNode()).getOriginalNode();
            IAccumulatorData originalData = this.getAccumulatorData(original.getTreeInfo(), acc, context);
            VirtualAccumulatorData vad = new VirtualAccumulatorData(originalData);
            map.put(acc, vad);
            return vad;
        }
        if (doc instanceof TinyTree && ((TinyTree)doc).getCopiedFrom() != null) {
            original = this.getAccumulatorData(((TinyTree)doc).getCopiedFrom().getTreeInfo(), acc, context);
            return new PathMappedAccumulatorData((IAccumulatorData)original, ((TinyTree)doc).getCopiedFrom());
        }
        AccumulatorData d = new AccumulatorData(acc);
        XPathContextMajor c2 = context.newCleanContext();
        c2.setCurrentComponent(acc.getDeclaringComponent());
        try {
            d.buildIndex(doc.getRootNode(), c2);
            map.put(acc, d);
            return d;
        } catch (XPathException err) {
            FailedAccumulatorData failed = new FailedAccumulatorData(acc, err);
            map.put(acc, failed);
            return failed;
        }
    }

    public synchronized void addAccumulatorData(TreeInfo doc, Accumulator acc, IAccumulatorData accData) {
        Map<Accumulator, IAccumulatorData> map = this.accumulatorDataIndex.get(doc);
        if (map != null) {
            IAccumulatorData data = map.get(acc);
            if (data != null) {
                return;
            }
        } else {
            map = new HashMap<Accumulator, IAccumulatorData>();
            this.accumulatorDataIndex.put(doc, map);
        }
        map.put(acc, accData);
    }
}


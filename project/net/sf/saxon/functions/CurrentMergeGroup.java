/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.HashSet;
import java.util.Set;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.GroupIterator;
import net.sf.saxon.expr.sort.MergeGroupingIterator;
import net.sf.saxon.expr.sort.MergeInstr;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;

public class CurrentMergeGroup
extends SystemFunction {
    private boolean isInLoop = false;
    private MergeInstr controllingInstruction = null;
    private Set<String> allowedNames = new HashSet<String>();

    public void setControllingInstruction(MergeInstr instruction, boolean isInLoop) {
        this.controllingInstruction = instruction;
        this.isInLoop = isInLoop;
        for (MergeInstr.MergeSource m : instruction.getMergeSources()) {
            String name = m.sourceName;
            if (name == null) continue;
            this.allowedNames.add(name);
        }
    }

    public MergeInstr getControllingInstruction() {
        return this.controllingInstruction;
    }

    public boolean isInLoop() {
        return this.isInLoop;
    }

    @Override
    public ItemType getResultItemType() {
        return AnyItemType.getInstance();
    }

    @Override
    public int getSpecialProperties(Expression[] arguments) {
        return 0;
    }

    @Override
    public Expression makeFunctionCall(Expression ... arguments) {
        return new SystemFunctionCall(this, arguments){

            @Override
            public Expression getScopingExpression() {
                return CurrentMergeGroup.this.getControllingInstruction();
            }
        };
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        String source = null;
        if (arguments.length > 0) {
            source = arguments[0].head().getStringValue();
        }
        return SequenceTool.toLazySequence(this.currentGroup(source, context));
    }

    private SequenceIterator currentGroup(String source, XPathContext c) throws XPathException {
        GroupIterator gi = c.getCurrentMergeGroupIterator();
        if (gi == null) {
            throw new XPathException("There is no current merge group", "XTDE3480");
        }
        if (source == null) {
            return gi.iterateCurrentGroup();
        }
        if (!this.allowedNames.contains(source)) {
            throw new XPathException("Supplied argument (" + source + ") is not the name of any xsl:merge-source in the containing xsl:merge instruction", "XTDE3490");
        }
        return ((MergeGroupingIterator)gi).iterateCurrentGroup(source);
    }

    @Override
    public String getStreamerName() {
        return "CurrentMergeGroup";
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.HashSet;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.sort.MergeInstr;
import net.sf.saxon.expr.sort.SortExpression;
import net.sf.saxon.expr.sort.SortKeyDefinition;
import net.sf.saxon.expr.sort.SortKeyDefinitionList;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLFallback;
import net.sf.saxon.style.XSLMergeAction;
import net.sf.saxon.style.XSLMergeSource;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Whitespace;

public class XSLMerge
extends StyleElement {
    private int numberOfMergeSources = 0;

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    public boolean mayContainSequenceConstructor() {
        return false;
    }

    @Override
    protected void prepareAttributes() {
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            this.checkUnknownAttribute(attName);
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        int childMask = 0;
        HashSet<String> mergeSourceNames = new HashSet<String>();
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof XSLMergeSource) {
                String name = ((XSLMergeSource)nodeInfo).getSourceName();
                if (mergeSourceNames.contains(name)) {
                    this.compileError("Duplicate xsl:merge-source/@name", "XTSE3190");
                }
                mergeSourceNames.add(name);
                childMask |= 1;
                ++this.numberOfMergeSources;
                continue;
            }
            if (nodeInfo instanceof XSLMergeAction) {
                if ((childMask & 2) == 2) {
                    this.compileError("xsl:merge must have only one xsl:merge-action child element", "XTSE0010");
                }
                childMask |= 2;
                continue;
            }
            if (nodeInfo.getNodeKind() == 3) {
                if (Whitespace.isWhite(nodeInfo.getStringValueCS())) continue;
                this.compileError("No character data is allowed within xsl:merge", "XXXX");
                continue;
            }
            if (nodeInfo instanceof XSLFallback) {
                if ((childMask & 2) != 0) continue;
                this.compileError("xsl:fallback child of xsl:merge can appear only after xsl:merge-action", "XTSE0010");
                continue;
            }
            this.compileError("Child element " + Err.wrap(nodeInfo.getDisplayName(), 1) + " is not allowed as a child of xsl:merge", "XTSE0010");
        }
        if (childMask == 1) {
            this.compileError("xsl:merge element requires an xsl:merge-action", "XTSE0010");
        } else if (childMask == 2) {
            this.compileError("xsl:merge element requires at least one xsl:merge-source child element", "XTSE0010");
        }
    }

    private void checkCompatibleMergeKeys(MergeInstr.MergeSource[] sources) {
        for (int i = 0; i < sources[0].mergeKeyDefinitions.size() && sources[0].mergeKeyDefinitions.getSortKeyDefinition(i).isFixed(); ++i) {
            for (int z = 1; z < sources.length && sources[z].mergeKeyDefinitions.getSortKeyDefinition(i).isFixed(); ++z) {
                if (this.compareSortKeyDefinitions(sources[z].mergeKeyDefinitions.getSortKeyDefinition(i), sources[0].mergeKeyDefinitions.getSortKeyDefinition(i))) continue;
                this.compileError("The " + RoleDiagnostic.ordinal(i + 1) + " merge key definition of the " + RoleDiagnostic.ordinal(z + 1) + " merge source is incompatible with the " + RoleDiagnostic.ordinal(i + 1) + " merge key definition of the first merge source", "XTDE2210");
            }
        }
    }

    private boolean compareSortKeyDefinitions(SortKeyDefinition sd1, SortKeyDefinition sd2) {
        return this.sameFixedExpression(sd1.getLanguage(), sd2.getLanguage()) && this.sameFixedExpression(sd1.getOrder(), sd2.getOrder()) && this.sameFixedExpression(sd1.getCollationNameExpression(), sd2.getCollationNameExpression()) && this.sameFixedExpression(sd1.getCaseOrder(), sd2.getCaseOrder()) && this.sameFixedExpression(sd1.getDataTypeExpression(), sd2.getDataTypeExpression());
    }

    private boolean sameFixedExpression(Expression e1, Expression e2) {
        return e1 == null && e2 == null || e1 != null && e1.equals(e2);
    }

    @Override
    public Expression compile(Compilation compilation, ComponentDeclaration decl) throws XPathException {
        MergeInstr merge = new MergeInstr();
        int entries = this.numberOfMergeSources;
        MergeInstr.MergeSource[] sources = new MergeInstr.MergeSource[entries];
        Expression action = Literal.makeEmptySequence();
        int w = 0;
        int sortKeyDefLen = 0;
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof XSLMergeSource) {
                XSLMergeSource source = (XSLMergeSource)nodeInfo;
                SortKeyDefinitionList sortKeyDefs = source.makeSortKeys(compilation, decl);
                if (sortKeyDefLen == 0) {
                    sortKeyDefLen = sortKeyDefs.size();
                } else if (sortKeyDefLen != sortKeyDefs.size()) {
                    this.compileError("Each xsl:merge-source must have the same number of xsl:merge-key children", "XTSE2200");
                }
                Expression select = source.getSelect();
                if (source.isSortBeforeMerge()) {
                    select = new SortExpression(select, sortKeyDefs.copy(new RebindingMap()));
                }
                MergeInstr.MergeSource ms = source.makeMergeSource(merge, select);
                ms.mergeKeyDefinitions = sortKeyDefs;
                sources[w++] = ms;
                continue;
            }
            if (!(nodeInfo instanceof XSLMergeAction)) continue;
            action = ((XSLMergeAction)nodeInfo).compileSequenceConstructor(compilation, decl, true);
            if (action == null) {
                action = Literal.makeEmptySequence();
            }
            try {
                action = action.simplify();
            } catch (XPathException e) {
                this.compileError(e);
            }
        }
        this.checkCompatibleMergeKeys(sources);
        merge.init(sources, action);
        return merge;
    }
}


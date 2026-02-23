/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.HashSet;
import java.util.Set;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.accum.Accumulator;
import net.sf.saxon.expr.accum.AccumulatorRegistry;
import net.sf.saxon.expr.sort.MergeInstr;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLMergeKey;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.Whitespace;

public class XSLMergeSource
extends StyleElement {
    private Expression forEachItem;
    private Expression forEachSource;
    private Expression select;
    private boolean sortBeforeMerge = false;
    private int mergeKeyCount = 0;
    private String sourceName;
    private int validationAction = 4;
    private SchemaType schemaType = null;
    private boolean streamable = false;
    private Set<Accumulator> accumulators = new HashSet<Accumulator>();

    @Override
    public boolean isInstruction() {
        return false;
    }

    @Override
    public boolean mayContainSequenceConstructor() {
        return false;
    }

    public Expression getForEachItem() {
        return this.forEachItem;
    }

    public Expression getForEachSource() {
        return this.forEachSource;
    }

    public Expression getSelect() {
        return this.select;
    }

    public boolean isSortBeforeMerge() {
        return this.sortBeforeMerge;
    }

    public String getSourceName() {
        return this.sourceName;
    }

    public int getValidationAction() {
        return this.validationAction;
    }

    public SchemaType getSchemaTypeAttribute() {
        return this.schemaType;
    }

    public MergeInstr.MergeSource makeMergeSource(MergeInstr mi, Expression select) {
        MergeInstr.MergeSource ms = new MergeInstr.MergeSource(mi);
        if (this.forEachItem != null) {
            ms.initForEachItem(mi, this.forEachItem);
        }
        if (this.forEachSource != null) {
            ms.initForEachStream(mi, this.forEachSource);
        }
        if (select != null) {
            this.select = select;
            ms.initRowSelect(mi, select);
        }
        ms.baseURI = this.getBaseURI();
        ms.sourceName = this.sourceName;
        ms.validation = this.validationAction;
        ms.schemaType = this.schemaType;
        ms.setStreamable(this.streamable);
        ms.accumulators = this.accumulators;
        return ms;
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        return null;
    }

    @Override
    protected void prepareAttributes() {
        String selectAtt = null;
        String forEachItemAtt = null;
        String forEachSourceAtt = null;
        String validationAtt = null;
        String typeAtt = null;
        String streamableAtt = null;
        String useAccumulatorsAtt = null;
        block22: for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            switch (f) {
                case "for-each-item": {
                    forEachItemAtt = value;
                    this.forEachItem = this.makeExpression(forEachItemAtt, att);
                    continue block22;
                }
                case "for-each-source": {
                    forEachSourceAtt = value;
                    this.forEachSource = this.makeExpression(forEachSourceAtt, att);
                    continue block22;
                }
                case "select": {
                    selectAtt = value;
                    this.select = this.makeExpression(selectAtt, att);
                    continue block22;
                }
                case "sort-before-merge": {
                    this.sortBeforeMerge = this.processBooleanAttribute("sort-before-merge", value);
                    continue block22;
                }
                case "name": {
                    String nameAtt = Whitespace.trim(value);
                    if (NameChecker.isValidNCName(nameAtt)) {
                        this.sourceName = nameAtt;
                        continue block22;
                    }
                    this.compileError("xsl:merge-source/@name (" + nameAtt + ") is not a valid NCName", "XTSE0020");
                    continue block22;
                }
                case "validation": {
                    validationAtt = Whitespace.trim(value);
                    continue block22;
                }
                case "type": {
                    typeAtt = Whitespace.trim(value);
                    continue block22;
                }
                case "streamable": {
                    streamableAtt = value;
                    continue block22;
                }
                case "use-accumulators": {
                    useAccumulatorsAtt = Whitespace.trim(value);
                    continue block22;
                }
            }
            this.checkUnknownAttribute(attName);
        }
        if (this.sourceName == null) {
            this.sourceName = "saxon-merge-source-" + this.hashCode();
        }
        if (forEachItemAtt != null && forEachSourceAtt != null) {
            this.compileError("The for-each-item and for-each-source attributes must not both be present", "XTSE3195");
        }
        if (selectAtt == null) {
            this.reportAbsence("select");
        }
        this.validationAction = validationAtt == null ? this.getDefaultValidation() : this.validateValidationAttribute(validationAtt);
        if (typeAtt != null) {
            if (!this.isSchemaAware()) {
                this.compileError("The @type attribute is available only with a schema-aware XSLT processor", "XTSE1660");
            }
            this.schemaType = this.getSchemaType(typeAtt);
            this.validationAction = 8;
        }
        if (typeAtt != null && validationAtt != null) {
            this.compileError("The @validation and @type attributes are mutually exclusive", "XTSE1505");
        }
        if ((typeAtt != null || validationAtt != null) && forEachSourceAtt == null) {
            this.compileError("The @type and @validation attributes can be used only when @for-each-stream is specified", "XTSE0020");
        }
        if (streamableAtt != null) {
            this.streamable = this.processStreamableAtt(streamableAtt);
            if (this.streamable && this.forEachSource == null) {
                this.compileError("Streaming on xsl:merge-source is possible only when @for-each-source is used", "XTSE3195");
            }
        } else if (this.forEachSource != null) {
            this.streamable = false;
        }
        if (useAccumulatorsAtt == null) {
            useAccumulatorsAtt = "";
        }
        AccumulatorRegistry registry = this.getPrincipalStylesheetModule().getStylesheetPackage().getAccumulatorRegistry();
        this.accumulators = registry.getUsedAccumulators(useAccumulatorsAtt, this);
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.forEachItem = this.typeCheck("for-each-item", this.forEachItem);
        this.forEachSource = this.typeCheck("for-each-source", this.forEachSource);
        this.select = this.typeCheck("select", this.select);
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof XSLMergeKey) {
                ++this.mergeKeyCount;
                continue;
            }
            if (nodeInfo.getNodeKind() == 3) {
                if (Whitespace.isWhite(nodeInfo.getStringValueCS())) continue;
                this.compileError("No character data is allowed within xsl:merge-source", "XTSE0010");
                continue;
            }
            if (!(nodeInfo instanceof StyleElement)) continue;
            ((StyleElement)nodeInfo).compileError("No children other than xsl:merge-key are allowed within xsl:merge-source", "XTSE0010");
        }
        if (this.mergeKeyCount == 0) {
            this.compileError("xsl:merge-source must have exactly at least one xsl:merge-key child element", "XTSE0010");
        }
    }
}


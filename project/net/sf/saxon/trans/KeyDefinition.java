/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import java.util.Map;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.instruct.Actor;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.UType;

public class KeyDefinition
extends Actor
implements ContextOriginator {
    private SymbolicName symbolicName;
    private Pattern match;
    private BuiltInAtomicType useType;
    private StringCollator collation;
    private String collationName;
    private boolean backwardsCompatible = false;
    private boolean strictComparison = false;
    private boolean convertUntypedToOther = false;
    private boolean rangeKey = false;
    private boolean composite = false;

    public KeyDefinition(SymbolicName symbolicName, Pattern match, Expression use, String collationName, StringCollator collation) {
        this.symbolicName = symbolicName;
        this.match = match;
        this.setBody(use);
        this.collation = collation;
        this.collationName = collationName;
    }

    @Override
    public SymbolicName getSymbolicName() {
        return this.symbolicName;
    }

    public void setRangeKey(boolean rangeKey) {
        this.rangeKey = rangeKey;
    }

    public boolean isRangeKey() {
        return this.rangeKey;
    }

    public void setComposite(boolean composite) {
        this.composite = composite;
    }

    public boolean isComposite() {
        return this.composite;
    }

    public void setIndexedItemType(BuiltInAtomicType itemType) {
        this.useType = itemType;
    }

    public BuiltInAtomicType getIndexedItemType() {
        if (this.useType == null) {
            return BuiltInAtomicType.ANY_ATOMIC;
        }
        return this.useType;
    }

    public void setBackwardsCompatible(boolean bc) {
        this.backwardsCompatible = bc;
    }

    public boolean isBackwardsCompatible() {
        return this.backwardsCompatible;
    }

    public void setStrictComparison(boolean strict) {
        this.strictComparison = strict;
    }

    public boolean isStrictComparison() {
        return this.strictComparison;
    }

    public void setConvertUntypedToOther(boolean convertToOther) {
        this.convertUntypedToOther = convertToOther;
    }

    public boolean isConvertUntypedToOther() {
        return this.convertUntypedToOther;
    }

    @Override
    public void setStackFrameMap(SlotManager map) {
        if (map != null) {
            super.setStackFrameMap(map);
        }
    }

    @Override
    public void allocateAllBindingSlots(StylesheetPackage pack) {
        super.allocateAllBindingSlots(pack);
        KeyDefinition.allocateBindingSlotsRecursive(pack, this, this.match, this.getDeclaringComponent().getComponentBindings());
    }

    public void setLocation(String systemId, int lineNumber) {
        this.setSystemId(systemId);
        this.setLineNumber(lineNumber);
    }

    public Pattern getMatch() {
        return this.match;
    }

    public Expression getUse() {
        return this.getBody();
    }

    public String getCollationName() {
        return this.collationName;
    }

    public StringCollator getCollation() {
        return this.collation;
    }

    public StructuredQName getObjectName() {
        return this.symbolicName.getComponentName();
    }

    public void export(ExpressionPresenter out, boolean reusable, Map<Component, Integer> componentIdMap) throws XPathException {
        out.startElement("key");
        out.emitAttribute("name", this.getObjectName());
        if (!"http://www.w3.org/2005/xpath-functions/collation/codepoint".equals(this.collationName)) {
            out.emitAttribute("collation", this.collationName);
        }
        out.emitAttribute("line", this.getLineNumber() + "");
        out.emitAttribute("module", this.getSystemId());
        if (this.getStackFrameMap() != null && this.getStackFrameMap().getNumberOfVariables() != 0) {
            out.emitAttribute("slots", this.getStackFrameMap().getNumberOfVariables() + "");
        }
        if (componentIdMap != null) {
            out.emitAttribute("binds", "" + this.getDeclaringComponent().listComponentReferences(componentIdMap));
        }
        String flags = "";
        if (this.backwardsCompatible) {
            flags = flags + "b";
        }
        if (this.isRangeKey()) {
            flags = flags + "r";
            out.emitAttribute("range", "1");
        }
        if (this.match.getUType().overlaps(UType.ATTRIBUTE)) {
            flags = flags + "a";
        }
        if (this.match.getUType().overlaps(UType.NAMESPACE)) {
            flags = flags + "n";
        }
        if (this.composite) {
            flags = flags + "c";
        }
        if (reusable) {
            flags = flags + "u";
        }
        if (!"".equals(flags)) {
            out.emitAttribute("flags", flags);
        }
        this.getMatch().export(out);
        this.getBody().export(out);
        out.endElement();
    }

    @Override
    public void export(ExpressionPresenter presenter) throws XPathException {
        throw new UnsupportedOperationException();
    }
}


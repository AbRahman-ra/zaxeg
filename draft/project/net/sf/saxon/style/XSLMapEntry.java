/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.instruct.SequenceInstr;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.ma.map.MapFunctionSet;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLFallback;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;

public class XSLMapEntry
extends StyleElement {
    Expression key = null;
    Expression select = null;

    @Override
    public boolean isInstruction() {
        return true;
    }

    protected ItemType getReturnedItemType() {
        return MapType.ANY_MAP_TYPE;
    }

    @Override
    public boolean mayContainSequenceConstructor() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        String keyAtt = null;
        String selectAtt = null;
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            if (f.equals("key")) {
                keyAtt = value;
                this.key = this.makeExpression(keyAtt, att);
                continue;
            }
            if (f.equals("select")) {
                selectAtt = value;
                this.select = this.makeExpression(selectAtt, att);
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        if (keyAtt == null) {
            this.reportAbsence("key");
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.key = this.typeCheck("key", this.key);
        this.select = this.typeCheck("select", this.select);
        if (this.select != null) {
            for (NodeInfo nodeInfo : this.children()) {
                if (nodeInfo instanceof XSLFallback) continue;
                this.compileError("An xsl:map-entry element with a select attribute must be empty", "XTSE3280");
                return;
            }
        }
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        if (this.select == null) {
            this.select = this.compileSequenceConstructor(exec, decl, false);
            this.select = this.select.simplify();
        }
        Expression exp = MapFunctionSet.getInstance().makeFunction("entry", 2).makeFunctionCall(this.key, this.select);
        if (this.getConfiguration().getBooleanProperty(Feature.STRICT_STREAMABILITY)) {
            exp = new SequenceInstr(exp);
        }
        return exp;
    }
}


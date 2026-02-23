/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.instruct.SequenceInstr;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.ma.map.MapFunctionSet;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class XSLMap
extends StyleElement {
    private Expression select = null;
    private Expression onDuplicates = null;

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
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            if (attName.hasURI("http://saxon.sf.net/")) {
                if (!this.isExtensionAttributeAllowed(attName.getDisplayName()) || !attName.getLocalPart().equals("on-duplicates")) continue;
                this.onDuplicates = this.makeExpression(value, att);
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        Expression optionsExp;
        this.select = this.compileSequenceConstructor(exec, decl, false);
        this.select = this.select.simplify();
        TypeChecker tc = this.getConfiguration().getTypeChecker(false);
        RoleDiagnostic role = new RoleDiagnostic(20, "xsl:map sequence constructor", 0);
        role.setErrorCode("XTTE3375");
        this.select = tc.staticTypeCheck(this.select, SequenceType.makeSequenceType(MapType.ANY_MAP_TYPE, 57344), role, this.makeExpressionVisitor());
        if (this.onDuplicates != null) {
            optionsExp = MapFunctionSet.getInstance().makeFunction("entry", 2).makeFunctionCall(Literal.makeLiteral(new QNameValue("", "http://saxon.sf.net/", "on-duplicates")), this.onDuplicates);
        } else {
            HashTrieMap options = new HashTrieMap();
            options.initialPut(new StringValue("duplicates"), new StringValue("reject"));
            options.initialPut(new QNameValue("", "http://saxon.sf.net/", "duplicates-error-code"), new StringValue("XTDE3365"));
            optionsExp = Literal.makeLiteral(options, this.select);
        }
        Expression exp = MapFunctionSet.getInstance().makeFunction("merge", 2).makeFunctionCall(this.select, optionsExp);
        if (this.getConfiguration().getBooleanProperty(Feature.STRICT_STREAMABILITY)) {
            exp = new SequenceInstr(exp);
        }
        return exp;
    }
}


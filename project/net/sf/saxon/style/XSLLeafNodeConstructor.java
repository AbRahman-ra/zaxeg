/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.AdjacentTextNodeMerger;
import net.sf.saxon.expr.AtomicSequenceConverter;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.instruct.SimpleNodeConstructor;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public abstract class XSLLeafNodeConstructor
extends StyleElement {
    protected Expression select = null;

    protected Expression prepareAttributesNameAndSelect() {
        Expression name = null;
        String nameAtt = null;
        String selectAtt = null;
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            if (f.equals("name")) {
                nameAtt = Whitespace.trim(value);
                name = this.makeAttributeValueTemplate(nameAtt, att);
                continue;
            }
            if (f.equals("select")) {
                selectAtt = value;
                this.select = this.makeExpression(selectAtt, att);
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        if (nameAtt == null) {
            this.reportAbsence("name");
        }
        return name;
    }

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    public boolean mayContainSequenceConstructor() {
        return true;
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        if (this.select != null && this.hasChildNodes()) {
            String errorCode = this.getErrorCodeForSelectPlusContent();
            this.compileError("An " + this.getDisplayName() + " element with a select attribute must be empty", errorCode);
        }
        AxisIterator kids = this.iterateAxis(3);
        NodeInfo first = kids.next();
        if (this.select == null) {
            if (first == null) {
                this.select = new StringLiteral(StringValue.EMPTY_STRING);
                this.select.setRetainedStaticContext(this.makeRetainedStaticContext());
            } else if (kids.next() == null && !this.isExpandingText() && first.getNodeKind() == 3) {
                this.select = new StringLiteral(first.getStringValue());
                this.select.setRetainedStaticContext(this.makeRetainedStaticContext());
            }
        }
    }

    protected abstract String getErrorCodeForSelectPlusContent();

    protected void compileContent(Compilation exec, ComponentDeclaration decl, SimpleNodeConstructor inst, Expression separator) throws XPathException {
        if (separator == null) {
            separator = new StringLiteral(StringValue.SINGLE_SPACE);
        }
        try {
            if (this.select == null) {
                this.select = this.compileSequenceConstructor(exec, decl, true);
            }
            this.select = XSLLeafNodeConstructor.makeSimpleContentConstructor(this.select, separator, this.getStaticContext());
            inst.setSelect(this.select);
        } catch (XPathException err) {
            this.compileError(err);
        }
    }

    public static Expression makeSimpleContentConstructor(Expression select, Expression separator, StaticContext env) {
        RetainedStaticContext rsc = select.getLocalRetainedStaticContext();
        if (rsc == null) {
            rsc = env.makeRetainedStaticContext();
        }
        select = AdjacentTextNodeMerger.makeAdjacentTextNodeMerger(select);
        select = Atomizer.makeAtomizer(select, null);
        select = new AtomicSequenceConverter(select, BuiltInAtomicType.STRING);
        select.setRetainedStaticContext(rsc);
        ((AtomicSequenceConverter)select).allocateConverterStatically(env.getConfiguration(), false);
        if (select.getCardinality() != 16384) {
            select = SystemFunction.makeCall("string-join", rsc, select, separator);
        }
        return select;
    }
}


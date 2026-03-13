/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LocalVariableReference;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.ExpressionContext;
import net.sf.saxon.style.SourceBinding;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.TextValueTemplateNode;
import net.sf.saxon.style.XSLGeneralVariable;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;

public class TextValueTemplateContext
extends ExpressionContext {
    TextValueTemplateNode textNode;

    public TextValueTemplateContext(StyleElement parent, TextValueTemplateNode textNode) {
        super(parent, null);
        this.textNode = textNode;
    }

    @Override
    public Expression bindVariable(StructuredQName qName) throws XPathException {
        SourceBinding siblingVar = this.bindLocalVariable(qName);
        if (siblingVar == null) {
            return super.bindVariable(qName);
        }
        LocalVariableReference var = new LocalVariableReference(qName);
        siblingVar.registerReference(var);
        return var;
    }

    private SourceBinding bindLocalVariable(StructuredQName qName) {
        NodeInfo curr = this.textNode;
        AxisIterator preceding = curr.iterateAxis(11);
        while ((curr = preceding.next()) != null) {
            SourceBinding sourceBinding;
            if (!(curr instanceof XSLGeneralVariable) || (sourceBinding = ((XSLGeneralVariable)curr).getBindingInformation(qName)) == null) continue;
            return sourceBinding;
        }
        return null;
    }
}


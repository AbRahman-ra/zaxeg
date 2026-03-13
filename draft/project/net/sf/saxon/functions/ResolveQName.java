/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.QNameValue;

public class ResolveQName
extends SystemFunction {
    @Override
    public ZeroOrOne call(XPathContext context, Sequence[] arguments) throws XPathException {
        AtomicValue lex = (AtomicValue)arguments[0].head();
        return new ZeroOrOne<Object>((lex == null ? null : ResolveQName.resolveQName(lex.getStringValueCS(), (NodeInfo)arguments[1].head())));
    }

    public static QNameValue resolveQName(CharSequence lexicalQName, NodeInfo element) throws XPathException {
        NamespaceMap resolver = element.getAllNamespaces();
        StructuredQName qName = StructuredQName.fromLexicalQName(lexicalQName, true, false, resolver);
        return new QNameValue(qName, BuiltInAtomicType.QNAME);
    }
}

